/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xalan" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, Lotus
 * Development Corporation., http://www.lotus.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.xpath;

// Java lib imports
import java.io.File;
import java.io.IOException;

import java.util.Stack;
import java.util.Vector;

import java.lang.reflect.Method;

// Xalan imports
import org.apache.xml.utils.IntStack;
import org.apache.xml.utils.ObjectStack;
import org.apache.xml.utils.NSInfo;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xml.utils.QName;
import org.apache.xml.utils.NodeVector;
import org.apache.xalan.res.XSLMessages;
import org.apache.xpath.res.XPATHErrorResources;
import org.apache.xpath.axes.ContextNodeList;
import org.apache.xpath.axes.SubContextList;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XNodeSet;
import org.apache.xpath.objects.XString;

import org.apache.xalan.extensions.ExpressionContext;

// SAX2 imports
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
// import org.xml.sax.Locator;

// TRaX imports
import javax.xml.transform.URIResolver;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.dom.DOMSource;

import javax.xml.transform.SourceLocator;
import javax.xml.transform.Source;
import javax.xml.transform.ErrorListener;

import org.apache.xml.dtm.DTMManager;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.dtm.DTMFilter;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMWSFilter;
import org.apache.xml.dtm.Axis;

// Utility imports.
import org.apache.xml.utils.SAXSourceLocator;
import org.apache.xml.utils.XMLString;
import org.apache.xml.utils.XMLStringFactory;
import org.apache.xml.utils.IntStack;

import org.apache.xpath.axes.DescendantIterator;

// For  handling.
import org.apache.xml.dtm.ref.sax2dtm.SAX2RTFDTM;

/**
 * <meta name="usage" content="advanced"/>
 * Default class for the runtime execution context for XPath.
 * 
 * <p>This class extends DTMManager but does not directly implement it.</p>
 */
public class XPathContext extends DTMManager // implements ExpressionContext
{
	IntStack m_last_pushed_rtfdtm=new IntStack();	
  /**
   * Stack of cached "reusable" DTMs for Result Tree Fragments.
   * This is a kluge to handle the problem of starting an RTF before
   * the old one is complete.
   * 
   * %REVIEW% I'm using a Vector rather than Stack so we can reuse
   * the DTMs if the problem occurs multiple times. I'm not sure that's
   * really a net win versus discarding the DTM and starting a new one...
   * but the retained RTF DTM will have been tail-pruned so should be small.
   */
  private Vector m_rtfdtm_stack=null;
  /** Index of currently active RTF DTM in m_rtfdtm_stack */
  private int m_which_rtfdtm=-1;
  
 /**
   * Most recent "reusable" DTM for Global Result Tree Fragments. No stack is
   * required since we're never going to pop these.
   */
  private SAX2RTFDTM m_global_rtfdtm=null;
  
	
  /**
   * Though XPathContext context extends 
   * the DTMManager, it really is a proxy for this object, which 
   * is the real DTMManager.
   */
  protected DTMManager m_dtmManager = DTMManager.newInstance(
                   org.apache.xpath.objects.XMLStringFactoryImpl.getFactory());
  
  /**
   * Return the DTMManager object.  Though XPathContext context extends 
   * the DTMManager, it really is a proxy for the real DTMManager.  If a 
   * caller needs to make a lot of calls to the DTMManager, it is faster 
   * if it gets the real one from this function.
   */
   public DTMManager getDTMManager()
   {
     return m_dtmManager;
   }
  
  /**
   * Get an instance of a DTM, loaded with the content from the
   * specified source.  If the unique flag is true, a new instance will
   * always be returned.  Otherwise it is up to the DTMManager to return a
   * new instance or an instance that it already created and may be being used
   * by someone else.
   * (I think more parameters will need to be added for error handling, and entity
   * resolution).
   *
   * @param source the specification of the source object, which may be null, 
   *               in which case it is assumed that node construction will take 
   *               by some other means.
   * @param unique true if the returned DTM must be unique, probably because it
   * is going to be mutated.
   * @param whiteSpaceFilter Enables filtering of whitespace nodes, and may 
   *                         be null.
   * @param incremental true if the construction should try and be incremental.
   * @param doIndexing true if the caller considers it worth it to use 
   *                   indexing schemes.
   *
   * @return a non-null DTM reference.
   */
  public DTM getDTM(javax.xml.transform.Source source, boolean unique, 
                    DTMWSFilter wsfilter,
                    boolean incremental,
                    boolean doIndexing)
  {
    return m_dtmManager.getDTM(source, unique, wsfilter, 
                               incremental, doIndexing);
  }
                             
  /**
   * Get an instance of a DTM that "owns" a node handle. 
   *
   * @param nodeHandle the nodeHandle.
   *
   * @return a non-null DTM reference.
   */
  public DTM getDTM(int nodeHandle)
  {
    return m_dtmManager.getDTM(nodeHandle);
  }

  /**
   * Given a W3C DOM node, try and return a DTM handle.
   * Note: calling this may be non-optimal.
   * 
   * @param node Non-null reference to a DOM node.
   * 
   * @return a valid DTM handle.
   */
  public int getDTMHandleFromNode(org.w3c.dom.Node node)
  {
    return m_dtmManager.getDTMHandleFromNode(node);
  }
//
//  
  /**
   * %TBD% Doc
   */
  public int getDTMIdentity(DTM dtm)
  {
    return m_dtmManager.getDTMIdentity(dtm);
  }
//  
  /**
   * Creates an empty <code>DocumentFragment</code> object. 
   * @return A new <code>DocumentFragment handle</code>.
   */
  public DTM createDocumentFragment()
  {
    return m_dtmManager.createDocumentFragment();
  }
//  
  /**
   * Release a DTM either to a lru pool, or completely remove reference.
   * DTMs without system IDs are always hard deleted.
   * State: experimental.
   * 
   * @param dtm The DTM to be released.
   * @param shouldHardDelete True if the DTM should be removed no matter what.
   * @return true if the DTM was removed, false if it was put back in a lru pool.
   */
  public boolean release(DTM dtm, boolean shouldHardDelete)
  {
    // %REVIEW% If it's a DTM which may contain multiple Result Tree
    // Fragments, we can't discard it unless we know not only that it
    // is empty, but that the XPathContext itself is going away. So do
    // _not_ accept the request. (May want to do it as part of
    // reset(), though.)
    if(m_rtfdtm_stack!=null && m_rtfdtm_stack.contains(dtm))
    {
      return false;
    }
  	
    return m_dtmManager.release(dtm, shouldHardDelete);
  }

  /**
   * Create a new <code>DTMIterator</code> based on an XPath
   * <a href="http://www.w3.org/TR/xpath#NT-LocationPath>LocationPath</a> or
   * a <a href="http://www.w3.org/TR/xpath#NT-UnionExpr">UnionExpr</a>.
   *
   * @param xpathCompiler ??? Somehow we need to pass in a subpart of the
   * expression.  I hate to do this with strings, since the larger expression
   * has already been parsed.
   *
   * @param pos The position in the expression.
   * @return The newly created <code>DTMIterator</code>.
   */
  public DTMIterator createDTMIterator(Object xpathCompiler, int pos)
  {
    return m_dtmManager.createDTMIterator(xpathCompiler, pos);
  }
//
  /**
   * Create a new <code>DTMIterator</code> based on an XPath
   * <a href="http://www.w3.org/TR/xpath#NT-LocationPath>LocationPath</a> or
   * a <a href="http://www.w3.org/TR/xpath#NT-UnionExpr">UnionExpr</a>.
   *
   * @param xpathString Must be a valid string expressing a
   * <a href="http://www.w3.org/TR/xpath#NT-LocationPath>LocationPath</a> or
   * a <a href="http://www.w3.org/TR/xpath#NT-UnionExpr">UnionExpr</a>.
   *
   * @param presolver An object that can resolve prefixes to namespace URLs.
   *
   * @return The newly created <code>DTMIterator</code>.
   */
  public DTMIterator createDTMIterator(String xpathString,
          PrefixResolver presolver)
  {
    return m_dtmManager.createDTMIterator(xpathString, presolver);
  }
//
  /**
   * Create a new <code>DTMIterator</code> based only on a whatToShow and
   * a DTMFilter.  The traversal semantics are defined as the descendant
   * access.
   *
   * @param whatToShow This flag specifies which node types may appear in
   *   the logical view of the tree presented by the iterator. See the
   *   description of <code>NodeFilter</code> for the set of possible
   *   <code>SHOW_</code> values.These flags can be combined using
   *   <code>OR</code>.
   * @param filter The <code>NodeFilter</code> to be used with this
   *   <code>TreeWalker</code>, or <code>null</code> to indicate no filter.
   * @param entityReferenceExpansion The value of this flag determines
   *   whether entity reference nodes are expanded.
   *
   * @return The newly created <code>NodeIterator</code>.
   */
  public DTMIterator createDTMIterator(int whatToShow,
          DTMFilter filter, boolean entityReferenceExpansion)
  {
    return m_dtmManager.createDTMIterator(whatToShow, filter, entityReferenceExpansion);
  }
  
  /**
   * Create a new <code>DTMIterator</code> that holds exactly one node.
   *
   * @param node The node handle that the DTMIterator will iterate to.
   *
   * @return The newly created <code>DTMIterator</code>.
   */
  public DTMIterator createDTMIterator(int node)
  {
    // DescendantIterator iter = new DescendantIterator();
    DTMIterator iter = new org.apache.xpath.axes.OneStepIteratorForward(Axis.SELF);
    iter.setRoot(node, this);
    return iter;
    // return m_dtmManager.createDTMIterator(node);
  }

  /**
   * Create an XPathContext instance.
   */
  public XPathContext()
  {
    m_prefixResolvers.push(null);
    m_currentNodes.push(DTM.NULL);
    m_currentExpressionNodes.push(DTM.NULL);
    m_saxLocations.push(null);
  }

  /**
   * Create an XPathContext instance.
   * @param owner Value that can be retrieved via the getOwnerObject() method.
   * @see #getOwnerObject
   */
  public XPathContext(Object owner)
  {
    m_owner = owner;
    try {
      m_ownerGetErrorListener = m_owner.getClass().getMethod("getErrorListener", new Class[] {});
    }
    catch (NoSuchMethodException nsme) {}
    m_prefixResolvers.push(null);
    m_currentNodes.push(DTM.NULL);
    m_currentExpressionNodes.push(DTM.NULL);
    m_saxLocations.push(null);
  }

  /**
   * Reset for new run.
   */
  public void reset()
  {
  	// These couldn't be disposed of earlier (see comments in release()); zap them now.
  	if(m_rtfdtm_stack!=null)
  		 for (java.util.Enumeration e = m_rtfdtm_stack.elements() ; e.hasMoreElements() ;) 
  		 	m_dtmManager.release((DTM)e.nextElement(), true);

    m_rtfdtm_stack=null; // drop our references too
    m_which_rtfdtm=-1;
    
    if(m_global_rtfdtm!=null)
  		 	m_dtmManager.release(m_global_rtfdtm,true);
    m_global_rtfdtm=null;
  	
    m_dtmManager = DTMManager.newInstance(
                   org.apache.xpath.objects.XMLStringFactoryImpl.getFactory());
                   
    m_saxLocations.removeAllElements();   
	m_axesIteratorStack.removeAllElements();
	m_contextNodeLists.removeAllElements();
	m_currentExpressionNodes.removeAllElements();
	m_currentNodes.removeAllElements();
	m_iteratorRoots.RemoveAllNoClear();
	m_predicatePos.removeAllElements();
	m_predicateRoots.RemoveAllNoClear();
	m_prefixResolvers.removeAllElements();
	
	m_prefixResolvers.push(null);
    m_currentNodes.push(DTM.NULL);
    m_currentExpressionNodes.push(DTM.NULL);
    m_saxLocations.push(null);
  }

  /** The current stylesheet locator. */
  ObjectStack m_saxLocations = new ObjectStack(RECURSIONLIMIT);

  /**
   * Set the current locater in the stylesheet.
   *
   * @param location The location within the stylesheet.
   */
  public void setSAXLocator(SourceLocator location)
  {
    m_saxLocations.setTop(location);
  }
  
  /**
   * Set the current locater in the stylesheet.
   *
   * @param location The location within the stylesheet.
   */
  public void pushSAXLocator(SourceLocator location)
  {
    m_saxLocations.push(location);
  }
  
  /**
   * Push a slot on the locations stack so that setSAXLocator can be 
   * repeatedly called.
   *
   * @param location The location within the stylesheet.
   */
  public void pushSAXLocatorNull()
  {
    m_saxLocations.push(null);
  }


  /**
   * Pop the current locater.
   */
  public void popSAXLocator()
  {
    m_saxLocations.pop();
  }

  /**
   * Get the current locater in the stylesheet.
   *
   * @return The location within the stylesheet, or null if not known.
   */
  public SourceLocator getSAXLocator()
  {
    return (SourceLocator) m_saxLocations.peek();
  }

  /** The owner context of this XPathContext.  In the case of XSLT, this will be a
   *  Transformer object.
   */
  private Object m_owner;

  /** The owner context of this XPathContext.  In the case of XSLT, this will be a
   *  Transformer object.
   */
  private Method m_ownerGetErrorListener;

  /**
   * Get the "owner" context of this context, which should be,
   * in the case of XSLT, the Transformer object.  This is needed
   * so that XSLT functions can get the Transformer.
   * @return The owner object passed into the constructor, or null.
   */
  public Object getOwnerObject()
  {
    return m_owner;
  }

  // ================ VarStack ===================

  /**
   * The stack of Variable stacks.  A VariableStack will be
   * pushed onto this stack for each template invocation.
   */
  private VariableStack m_variableStacks = new VariableStack();

  /**
   * Get the variable stack, which is in charge of variables and
   * parameters.
   *
   * @return the variable stack, which should not be null.
   */
  public final VariableStack getVarStack()
  {
    return m_variableStacks;
  }

  /** 
   * Get the variable stack, which is in charge of variables and
   * parameters.
   *
   * @param varStack non-null reference to the variable stack.
   */
  public final void setVarStack(VariableStack varStack)
  {
    m_variableStacks = varStack;
  }

  // ================ SourceTreeManager ===================

  /** The source tree manager, which associates Source objects to source 
   *  tree nodes. */
  private SourceTreeManager m_sourceTreeManager = new SourceTreeManager();

  /**
   * Get the SourceTreeManager associated with this execution context.
   *
   * @return the SourceTreeManager associated with this execution context.
   */
  public final SourceTreeManager getSourceTreeManager()
  {
    return m_sourceTreeManager;
  }

  /**
   * Set the SourceTreeManager associated with this execution context.
   *
   * @param mgr the SourceTreeManager to be associated with this 
   *        execution context.
   */
  public void setSourceTreeManager(SourceTreeManager mgr)
  {
    m_sourceTreeManager = mgr;
  }
  
  // =================================================

  /** The ErrorListener where errors and warnings are to be reported.   */
  private ErrorListener m_errorListener;

  /** A default ErrorListener in case our m_errorListener was not specified and our
   *  owner either does not have an ErrorListener or has a null one.
   */
  private ErrorListener m_defaultErrorListener;

  /**
   * Get the ErrorListener where errors and warnings are to be reported.
   *
   * @return A non-null ErrorListener reference.
   */
  public final ErrorListener getErrorListener()
  {

    if (null != m_errorListener)
        return m_errorListener;

    ErrorListener retval = null;

    try {
      if (null != m_ownerGetErrorListener)
        retval = (ErrorListener) m_ownerGetErrorListener.invoke(m_owner, new Object[] {});
    }
    catch (Exception e) {}

    if (null == retval)
    {
      if (null == m_defaultErrorListener) 
        m_defaultErrorListener = new org.apache.xml.utils.DefaultErrorHandler();
      retval = m_defaultErrorListener;
    }

    return retval;
  }

  /**
   * Set the ErrorListener where errors and warnings are to be reported.
   *
   * @param listener A non-null ErrorListener reference.
   */
  public void setErrorListener(ErrorListener listener) throws IllegalArgumentException
  {
    if (listener == null) 
      throw new IllegalArgumentException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_NULL_ERROR_HANDLER, null)); //"Null error handler");
    m_errorListener = listener;
  }


  // =================================================

  /** The TrAX URI Resolver for resolving URIs from the document(...)
   *  function to source tree nodes.  */
  private URIResolver m_uriResolver;

  /**
   * Get the URIResolver associated with this execution context.
   *
   * @return a URI resolver, which may be null.
   */
  public final URIResolver getURIResolver()
  {
    return m_uriResolver;
  }

  /**
   * Set the URIResolver associated with this execution context.
   *
   * @param resolver the URIResolver to be associated with this 
   *        execution context, may be null to clear an already set resolver.
   */
  public void setURIResolver(URIResolver resolver)
  {
    m_uriResolver = resolver;
  }

  // =================================================

  /** The reader of the primary source tree.    */
  public XMLReader m_primaryReader;

  /**
   * Get primary XMLReader associated with this execution context.
   *
   * @return The reader of the primary source tree.
   */
  public final XMLReader getPrimaryReader()
  {
    return m_primaryReader;
  }

  /**
   * Set primary XMLReader associated with this execution context.
   *
   * @param reader The reader of the primary source tree.
   */
  public void setPrimaryReader(XMLReader reader)
  {
    m_primaryReader = reader;
  }

  // =================================================


  /** Misnamed string manager for XPath messages.  */
  private static XSLMessages m_XSLMessages = new XSLMessages();

  /**
   * Tell the user of an assertion error, and probably throw an
   * exception.
   *
   * @param b  If false, a TransformerException will be thrown.
   * @param msg The assertion message, which should be informative.
   * 
   * @throws javax.xml.transform.TransformerException if b is false.
   */
  private void assertion(boolean b, String msg) throws javax.xml.transform.TransformerException
  {

    ErrorListener errorHandler = getErrorListener();

    if (errorHandler != null)
    {
      errorHandler.fatalError(
        new TransformerException(
          m_XSLMessages.createMessage(
            XPATHErrorResources.ER_INCORRECT_PROGRAMMER_ASSERTION,
            new Object[]{ msg }), (SAXSourceLocator)this.getSAXLocator()));
    }
  }

  //==========================================================
  // SECTION: Execution context state tracking
  //==========================================================
  
  /**
   * The current context node list.
   */
  private Stack m_contextNodeLists = new Stack();
  
  public Stack getContextNodeListsStack() { return m_contextNodeLists; }
  public void setContextNodeListsStack(Stack s) { m_contextNodeLists = s; }

  /**
   * Get the current context node list.
   *
   * @return  the <a href="http://www.w3.org/TR/xslt#dt-current-node-list">current node list</a>,
   * also refered to here as a <term>context node list</term>.
   */
  public final DTMIterator getContextNodeList()
  {

    if (m_contextNodeLists.size() > 0)
      return (DTMIterator) m_contextNodeLists.peek();
    else
      return null;
  }

  /**
   * <meta name="usage" content="internal"/>
   * Set the current context node list.
   *
   * @param nl the <a href="http://www.w3.org/TR/xslt#dt-current-node-list">current node list</a>,
   * also refered to here as a <term>context node list</term>.
   */
  public final void pushContextNodeList(DTMIterator nl)
  {
    m_contextNodeLists.push(nl);
  }

  /**
   * <meta name="usage" content="internal"/>
   * Pop the current context node list.
   */
  public final void popContextNodeList()
  {
  	if(m_contextNodeLists.isEmpty())
  	  System.err.println("Warning: popContextNodeList when stack is empty!");
  	else
      m_contextNodeLists.pop();
  }

  /**
   * The ammount to use for stacks that record information during the 
   * recursive execution.
   */
  public static final int RECURSIONLIMIT = (1024*4);

  /** The stack of <a href="http://www.w3.org/TR/xslt#dt-current-node">current node</a> objects.
   *  Not to be confused with the current node list.  %REVIEW% Note that there 
   *  are no bounds check and resize for this stack, so if it is blown, it's all 
   *  over.  */
  private IntStack m_currentNodes = new IntStack(RECURSIONLIMIT);
   
//  private NodeVector m_currentNodes = new NodeVector();
  
  public IntStack getCurrentNodeStack() {return m_currentNodes; }
  public void setCurrentNodeStack(IntStack nv) { m_currentNodes = nv; }

  /**
   * Get the current context node.
   *
   * @return the <a href="http://www.w3.org/TR/xslt#dt-current-node">current node</a>.
   */
  public final int getCurrentNode()
  {
    return m_currentNodes.peek();
  }
  
  /**
   * Set the current context node and expression node.
   *
   * @param cn the <a href="http://www.w3.org/TR/xslt#dt-current-node">current node</a>.
   * @param en the sub-expression context node.
   */
  public final void pushCurrentNodeAndExpression(int cn, int en)
  {
    m_currentNodes.push(cn);
    m_currentExpressionNodes.push(cn);
  }

  /**
   * Set the current context node.
   */
  public final void popCurrentNodeAndExpression()
  {
    m_currentNodes.quickPop(1);
    m_currentExpressionNodes.quickPop(1);
  }
  
  /**
   * Push the current context node, expression node, and prefix resolver.
   *
   * @param cn the <a href="http://www.w3.org/TR/xslt#dt-current-node">current node</a>.
   * @param en the sub-expression context node.
   * @param nc the namespace context (prefix resolver.
   */
  public final void pushExpressionState(int cn, int en, PrefixResolver nc)
  {
    m_currentNodes.push(cn);
    m_currentExpressionNodes.push(cn);
    m_prefixResolvers.push(nc);
  }
  
  /**
   * Pop the current context node, expression node, and prefix resolver.
   */
  public final void popExpressionState()
  {
    m_currentNodes.quickPop(1);
    m_currentExpressionNodes.quickPop(1);
    m_prefixResolvers.pop();
  }



  /**
   * Set the current context node.
   *
   * @param n the <a href="http://www.w3.org/TR/xslt#dt-current-node">current node</a>.
   */
  public final void pushCurrentNode(int n)
  {
    m_currentNodes.push(n);
  }
  
  /**
   * Pop the current context node.
   */
  public final void popCurrentNode()
  {
    m_currentNodes.quickPop(1);
  }
  
  /**
   * Set the current predicate root.
   */
  public final void pushPredicateRoot(int n)
  {
    m_predicateRoots.push(n);
  }

  /**
   * Pop the current predicate root.
   */
  public final void popPredicateRoot()
  {
    m_predicateRoots.popQuick();
  }

  /**
   * Get the current predicate root.
   */
  public final int getPredicateRoot()
  {
    return m_predicateRoots.peepOrNull();
  }
  
  /**
   * Set the current location path iterator root.
   */
  public final void pushIteratorRoot(int n)
  {
    m_iteratorRoots.push(n);
  }

  /**
   * Pop the current location path iterator root.
   */
  public final void popIteratorRoot()
  {
    m_iteratorRoots.popQuick();
  }

  /**
   * Get the current location path iterator root.
   */
  public final int getIteratorRoot()
  {
    return m_iteratorRoots.peepOrNull();
  }
  
  /** A stack of the current sub-expression nodes.  */
  private NodeVector m_iteratorRoots = new NodeVector();

  /** A stack of the current sub-expression nodes.  */
  private NodeVector m_predicateRoots = new NodeVector();

  /** A stack of the current sub-expression nodes.  */
  private IntStack m_currentExpressionNodes = new IntStack(RECURSIONLIMIT);
  
     
  public IntStack getCurrentExpressionNodeStack() { return m_currentExpressionNodes; }
  public void setCurrentExpressionNodeStack(IntStack nv) { m_currentExpressionNodes = nv; }
  
  private IntStack m_predicatePos = new IntStack();
  
  public final int getPredicatePos()
  {
    return m_predicatePos.peek();
  }

  public final void pushPredicatePos(int n)
  {
    m_predicatePos.push(n);
  }

  public final void popPredicatePos()
  {
    m_predicatePos.pop();
  }

  /**
   * Get the current node that is the expression's context (i.e. for current() support).
   *
   * @return The current sub-expression node.
   */
  public final int getCurrentExpressionNode()
  {
    return m_currentExpressionNodes.peek();
  }

  /**
   * Set the current node that is the expression's context (i.e. for current() support).
   *
   * @param n The sub-expression node to be current.
   */
  public final void pushCurrentExpressionNode(int n)
  {
    m_currentExpressionNodes.push(n);
  }

  /**
   * Pop the current node that is the expression's context 
   * (i.e. for current() support).
   */
  public final void popCurrentExpressionNode()
  {
    m_currentExpressionNodes.quickPop(1);
  }
  
  private ObjectStack m_prefixResolvers 
                                   = new ObjectStack(RECURSIONLIMIT);

  /**
   * Get the current namespace context for the xpath.
   *
   * @return the current prefix resolver for resolving prefixes to 
   *         namespace URLs.
   */
  public final PrefixResolver getNamespaceContext()
  {
    return (PrefixResolver) m_prefixResolvers.peek();
  }

  /**
   * Get the current namespace context for the xpath.
   *
   * @param pr the prefix resolver to be used for resolving prefixes to 
   *         namespace URLs.
   */
  public final void setNamespaceContext(PrefixResolver pr)
  {
    m_prefixResolvers.setTop(pr);
  }

  /**
   * Push a current namespace context for the xpath.
   *
   * @param pr the prefix resolver to be used for resolving prefixes to 
   *         namespace URLs.
   */
  public final void pushNamespaceContext(PrefixResolver pr)
  {
    m_prefixResolvers.push(pr);
  }
  
  /**
   * Just increment the namespace contest stack, so that setNamespaceContext
   * can be used on the slot.
   */
  public final void pushNamespaceContextNull()
  {
    m_prefixResolvers.push(null);
  }

  /**
   * Pop the current namespace context for the xpath.
   */
  public final void popNamespaceContext()
  {
    m_prefixResolvers.pop();
  }

  //==========================================================
  // SECTION: Current TreeWalker contexts (for internal use)
  //==========================================================

  /**
   * Stack of AxesIterators.
   */
  private Stack m_axesIteratorStack = new Stack();
  
  public Stack getAxesIteratorStackStacks() { return m_axesIteratorStack; }
  public void setAxesIteratorStackStacks(Stack s) { m_axesIteratorStack = s; }

  /**
   * <meta name="usage" content="internal"/>
   * Push a TreeWalker on the stack.
   *
   * @param iter A sub-context AxesWalker.
   */
  public final void pushSubContextList(SubContextList iter)
  {
    m_axesIteratorStack.push(iter);
  }

  /**
   * <meta name="usage" content="internal"/>
   * Pop the last pushed axes iterator.
   */
  public final void popSubContextList()
  {
    m_axesIteratorStack.pop();
  }

  /**
   * <meta name="usage" content="internal"/>
   * Get the current axes iterator, or return null if none.
   *
   * @return the sub-context node list.
   */
  public SubContextList getSubContextList()
  {
    return m_axesIteratorStack.isEmpty()
           ? null : (SubContextList) m_axesIteratorStack.peek();
  }
  
  /**
   * <meta name="usage" content="internal"/>
   * Get the <a href="http://www.w3.org/TR/xslt#dt-current-node-list">current node list</a> 
   * as defined by the XSLT spec.
   *
   * @return the <a href="http://www.w3.org/TR/xslt#dt-current-node-list">current node list</a>.
   */
  public org.apache.xpath.axes.LocPathIterator getCurrentNodeList()
  {
    for (int i = m_axesIteratorStack.size()-1; i >= 0; i--) 
    {
      org.apache.xpath.axes.PredicatedNodeTest iter 
       = (org.apache.xpath.axes.PredicatedNodeTest)m_axesIteratorStack.elementAt(i);
      org.apache.xpath.axes.LocPathIterator lpi = iter.getLocPathIterator();
      if(lpi.getIsTopLevel())
        return lpi;
    }
    return null;
  }


  //==========================================================
  // SECTION: Implementation of ExpressionContext interface
  //==========================================================

  /**
   * Get the current context node.
   * @return The current context node.
   */
  public final int getContextNode()
  {
    return this.getCurrentNode();
  }

  /**
   * Get the current context node list.
   * @return An iterator for the current context list, as
   * defined in XSLT.
   */
  public final DTMIterator getContextNodes()
  {

    try
    {
      DTMIterator cnl = getContextNodeList();

      if (null != cnl)
        return cnl.cloneWithReset();
      else
        return null;  // for now... this might ought to be an empty iterator.
    }
    catch (CloneNotSupportedException cnse)
    {
      return null;  // error reporting?
    }
  }
  
  XPathExpressionContext expressionContext = new XPathExpressionContext();
  
  /**
   * The the expression context for extensions for this context.
   * 
   * @return An object that implements the ExpressionContext.
   */
  public ExpressionContext getExpressionContext()
  {
    return expressionContext;
  }
  
  public class XPathExpressionContext implements ExpressionContext
  {
    /**
     * Return the XPathContext associated with this XPathExpressionContext.
     * Extensions should use this judiciously and only when special processing
     * requirements cannot be met another way.  Consider requesting an enhancement
     * to the ExpressionContext interface to avoid having to call this method.
     * @return the XPathContext associated with this XPathExpressionContext.
     */
     public XPathContext getXPathContext()
     {
       return XPathContext.this;
     }

    /**
     * Return the DTMManager object.  Though XPathContext context extends 
     * the DTMManager, it really is a proxy for the real DTMManager.  If a 
     * caller needs to make a lot of calls to the DTMManager, it is faster 
     * if it gets the real one from this function.
     */
     public DTMManager getDTMManager()
     {
       return m_dtmManager;
     }
    
    /**
     * Get the current context node.
     * @return The current context node.
     */
    public org.w3c.dom.Node getContextNode()
    {
      int context = getCurrentNode();
      
      return getDTM(context).getNode(context);
    }
  
    /**
     * Get the current context node list.
     * @return An iterator for the current context list, as
     * defined in XSLT.
     */
    public org.w3c.dom.traversal.NodeIterator getContextNodes()
    {
      return new org.apache.xml.dtm.ref.DTMNodeIterator(getContextNodeList());
    }

    /**
     * Get the error listener.
     * @return The registered error listener.
     */
    public ErrorListener getErrorListener()
    {
      return XPathContext.this.getErrorListener();
    }
  
    /**
     * Get the value of a node as a number.
     * @param n Node to be converted to a number.  May be null.
     * @return value of n as a number.
     */
    public double toNumber(org.w3c.dom.Node n)
    {
      // %REVIEW% You can't get much uglier than this...
      int nodeHandle = getDTMHandleFromNode(n);
      DTM dtm = getDTM(nodeHandle);
      XString xobj = (XString)dtm.getStringValue(nodeHandle);
      return xobj.num();
    }
  
    /**
     * Get the value of a node as a string.
     * @param n Node to be converted to a string.  May be null.
     * @return value of n as a string, or an empty string if n is null.
     */
    public String toString(org.w3c.dom.Node n)
    {
      // %REVIEW% You can't get much uglier than this...
      int nodeHandle = getDTMHandleFromNode(n);
      DTM dtm = getDTM(nodeHandle);
      XMLString strVal = dtm.getStringValue(nodeHandle);
      return strVal.toString();
    }

    /**
     * Get a variable based on it's qualified name.
     * @param qname The qualified name of the variable.
     * @return The evaluated value of the variable.
     * @throws javax.xml.transform.TransformerException
     */

    public final XObject getVariableOrParam(org.apache.xml.utils.QName qname)
              throws javax.xml.transform.TransformerException
    {
      return m_variableStacks.getVariableOrParam(XPathContext.this, qname);
    }

  }

 /**
   * Get a DTM to be used as a container for a global Result Tree
   * Fragment. This will always be an instance of (derived from? equivalent to?) 
   * SAX2DTM, since each RTF is constructed by temporarily redirecting our SAX 
   * output to it. It may be a single DTM containing for multiple fragments, 
   * if the implementation supports that.
   * 
   * Note: The distinction between this method and getRTFDTM() is that the latter
   * allocates space from the dynamic variable stack (m_rtfdtm_stack), which may
   * be pruned away again as the templates which defined those variables are exited.
   * Global variables may be bound late (see XUnresolvedVariable), and never want to
   * be discarded, hence we need to allocate them separately and don't actually need
   * a stack to track them.
   * 
   * @return a non-null DTM reference.
   */
  public DTM getGlobalRTFDTM()
  {
  	// We probably should _NOT_ be applying whitespace filtering at this stage!
  	//
  	// Some magic has been applied in DTMManagerDefault to recognize this set of options
  	// and generate an instance of DTM which can contain multiple documents
  	// (SAX2RTFDTM). Perhaps not the optimal way of achieving that result, but
  	// I didn't want to change the manager API at this time, or expose 
  	// too many dependencies on its internals. (Ideally, I'd like to move
  	// isTreeIncomplete all the way up to DTM, so we wouldn't need to explicitly
  	// specify the subclass here.)

	// If it doesn't exist, or if the one already existing is in the middle of
	// being constructed, we need to obtain a new DTM to write into. I'm not sure
	// the latter will ever arise, but I'd rather be just a bit paranoid..
	if( m_global_rtfdtm==null || m_global_rtfdtm.isTreeIncomplete() )
	{
  		m_global_rtfdtm=(SAX2RTFDTM)m_dtmManager.getDTM(null,true,null,false,false);
	}
    return m_global_rtfdtm;
  }
  



  /**
   * Get a DTM to be used as a container for a dynamic Result Tree
   * Fragment. This will always be an instance of (derived from? equivalent to?) 
   * SAX2DTM, since each RTF is constructed by temporarily redirecting our SAX 
   * output to it. It may be a single DTM containing for multiple fragments, 
   * if the implementation supports that.
   * 
   * @return a non-null DTM reference.
   */
  public DTM getRTFDTM()
  {
  	SAX2RTFDTM rtfdtm;

  	// We probably should _NOT_ be applying whitespace filtering at this stage!
  	//
  	// Some magic has been applied in DTMManagerDefault to recognize this set of options
  	// and generate an instance of DTM which can contain multiple documents
  	// (SAX2RTFDTM). Perhaps not the optimal way of achieving that result, but
  	// I didn't want to change the manager API at this time, or expose 
  	// too many dependencies on its internals. (Ideally, I'd like to move
  	// isTreeIncomplete all the way up to DTM, so we wouldn't need to explicitly
  	// specify the subclass here.)

	if(m_rtfdtm_stack==null)
	{
		m_rtfdtm_stack=new Vector();
  		rtfdtm=(SAX2RTFDTM)m_dtmManager.getDTM(null,true,null,false,false);
    m_rtfdtm_stack.addElement(rtfdtm);
		++m_which_rtfdtm;
	}
	else if(m_which_rtfdtm<0)
	{
		rtfdtm=(SAX2RTFDTM)m_rtfdtm_stack.elementAt(++m_which_rtfdtm);
	}
	else
	{
		rtfdtm=(SAX2RTFDTM)m_rtfdtm_stack.elementAt(m_which_rtfdtm);
  		
	  	// It might already be under construction -- the classic example would be
 	 	// an xsl:variable which uses xsl:call-template as part of its value. To
  		// handle this recursion, we have to start a new RTF DTM, pushing the old
  		// one onto a stack so we can return to it. This is not as uncommon a case
  		// as we might wish, unfortunately, as some folks insist on coding XSLT
  		// as if it were a procedural language...
  		if(rtfdtm.isTreeIncomplete())
	  	{
	  		if(++m_which_rtfdtm < m_rtfdtm_stack.size())
				rtfdtm=(SAX2RTFDTM)m_rtfdtm_stack.elementAt(m_which_rtfdtm);
	  		else
	  		{
		  		rtfdtm=(SAX2RTFDTM)m_dtmManager.getDTM(null,true,null,false,false);
          m_rtfdtm_stack.addElement(rtfdtm); 	
	  		}
 	 	}
	}
		
    return rtfdtm;
  }
  
  /** Push the RTFDTM's context mark, to allows discarding RTFs added after this
   * point. (If it doesn't exist we don't push, since we might still be able to 
   * get away with not creating it. That requires that excessive pops be harmless.)
   * */
  public void pushRTFContext()
  {
  	m_last_pushed_rtfdtm.push(m_which_rtfdtm);
  	if(null!=m_rtfdtm_stack)
	  	((SAX2RTFDTM)(getRTFDTM())).pushRewindMark();
  }
  
  /** Pop the RTFDTM's context mark. This discards any RTFs added after the last
   * mark was set. 
   * 
   * If there is no RTF DTM, there's nothing to pop so this
   * becomes a no-op. If pushes were issued before this was called, we count on
   * the fact that popRewindMark is defined such that overpopping just resets
   * to empty.
   * 
   * Complicating factor: We need to handle the case of popping back to a previous
   * RTF DTM, if one of the weird produce-an-RTF-to-build-an-RTF cases arose.
   * Basically: If pop says this DTM is now empty, then return to the previous
   * if one exists, in whatever state we left it in. UGLY, but hopefully the
   * situation which forces us to consider this will arise exceedingly rarely.
   * */
  public void popRTFContext()
  {
  	int previous=m_last_pushed_rtfdtm.pop();
  	if(null==m_rtfdtm_stack)
  		return;
  
  	if(m_which_rtfdtm==previous)
  	{
  		if(previous>=0) // guard against none-active
  		{
	  		boolean isEmpty=((SAX2RTFDTM)(m_rtfdtm_stack.elementAt(previous))).popRewindMark();
  		}
  	}
  	else while(m_which_rtfdtm!=previous)
  	{
  		// Empty each DTM before popping, so it's ready for reuse
  		// _DON'T_ pop the previous, since it's still open (which is why we
  		// stacked up more of these) and did not receive a mark.
  		boolean isEmpty=((SAX2RTFDTM)(m_rtfdtm_stack.elementAt(m_which_rtfdtm))).popRewindMark();
  		--m_which_rtfdtm; 
  	}
  }
}
