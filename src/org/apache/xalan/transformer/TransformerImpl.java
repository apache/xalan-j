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
package org.apache.xalan.transformer;

// Java imports
import java.util.Stack;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;

import java.io.StringWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

// Xalan imports
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.templates.Constants;
import org.apache.xalan.templates.ElemAttributeSet;
import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xalan.templates.StylesheetComposed;
import org.apache.xalan.templates.ElemForEach;
import org.apache.xalan.templates.ElemApplyTemplates;
import org.apache.xalan.templates.ElemUse;
import org.apache.xalan.templates.StylesheetRoot;
import org.apache.xalan.templates.Stylesheet;
import org.apache.xalan.templates.ElemWithParam;
import org.apache.xalan.templates.ElemSort;
import org.apache.xalan.templates.AVT;
import org.apache.xalan.templates.ElemVariable;
import org.apache.xalan.templates.ElemParam;
import org.apache.xalan.templates.ElemCallTemplate;
import org.apache.xalan.templates.ElemTemplate;
import org.apache.xalan.templates.ElemTextLiteral;
import org.apache.xalan.templates.TemplateList;
import org.apache.xalan.templates.XUnresolvedVariable;
import org.apache.xalan.templates.OutputProperties;
import org.apache.xalan.trace.TraceManager;
import org.apache.xalan.transformer.XalanProperties;
import org.apache.xml.utils.DOMBuilder;
import org.apache.xml.utils.NodeVector;
import org.apache.xml.utils.BoolStack;
import org.apache.xml.utils.QName;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xml.utils.ObjectPool;
import org.apache.xml.utils.SAXSourceLocator;
import org.apache.xpath.XPathContext;
import org.apache.xpath.NodeSetDTM;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XNodeSet;
import org.apache.xpath.XPath;
import org.apache.xpath.objects.XString;
import org.apache.xpath.objects.XRTreeFrag;
import org.apache.xpath.Arg;
import org.apache.xpath.XPathAPI;
import org.apache.xpath.VariableStack;
import org.apache.xpath.SourceTreeManager;
import org.apache.xpath.compiler.XPathParser;
import org.apache.xpath.axes.ContextNodeList;
import org.apache.xpath.Expression;

// Serializer Imports
import org.apache.xalan.serialize.Serializer;
import org.apache.xalan.serialize.SerializerFactory;
import org.apache.xalan.serialize.Method;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.dtm.DTMManager;
import org.apache.xml.dtm.DTMWSFilter;

// We have to figure out what to do about this one.
import org.apache.xml.dtm.ref.ExpandedNameTable;

// SAX2 Imports
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.InputSource;

import javax.xml.transform.TransformerException;

import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.Locator;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import javax.xml.transform.ErrorListener;

// TRaX Imports
import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;

// Imported JAVA API for XML Parsing 1.0 classes
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * <meta name="usage" content="advanced"/>
 * This class implements the
 * {@link javax.xml.transform.Transformer} interface, and is the core
 * representation of the transformation execution.</p>
 */
public class TransformerImpl extends Transformer
        implements Runnable, DTMWSFilter
{

  // Synch object to gaurd against setting values from the TrAX interface 
  // or reentry while the transform is going on.

  /** NEEDSDOC Field m_reentryGuard          */
  private Boolean m_reentryGuard = new Boolean(true);

  /**
   * This is null unless we own the stream.
   */
  private java.io.FileOutputStream m_outputStream = null;

  /**
   * True if the parser events should be on the main thread,
   * false if not.  Experemental.  Can not be set right now.
   */
  private boolean m_parserEventsOnMain = true;

  /** The thread that the transformer is running on. */
  private Thread m_transformThread;

  /** The base URL of the source tree. */
  private String m_urlOfSource = null;

  /** The Result object at the start of the transform, if any. */
  private Result m_outputTarget = null;

  /**
   * The output format object set by the user.  May be null.
   */
  private OutputProperties m_outputFormat;

  /** The output serializer */
  private Serializer m_serializer;

  /**
   * The content handler for the source input tree.
   */
  ContentHandler m_inputContentHandler;

  /**
   * The content handler for the result tree.
   */
  private ContentHandler m_outputContentHandler = null;

  //  /*
  //   * Use member variable to store param variables as they're
  //   * being created, use member variable so we don't
  //   * have to create a new vector every time.
  //   */
  //  private Vector m_newVars = new Vector();

  /** The JAXP Document Builder, mainly to create Result Tree Fragments. */
  DocumentBuilder m_docBuilder = null;

  /**
   * A pool of ResultTreeHandlers, for serialization of a subtree to text.
   *  Please note that each of these also holds onto a Text Serializer.  
   */
  private ObjectPool m_textResultHandlerObjectPool =
    new ObjectPool("org.apache.xalan.transformer.ResultTreeHandler");

  /**
   * Related to m_textResultHandlerObjectPool, this is a pool of
   * StringWriters, which are passed to the Text Serializers.
   * (I'm not sure if this is really needed any more.  -sb)      
   */
  private ObjectPool m_stringWriterObjectPool =
    new ObjectPool("java.io.StringWriter");

  /**
   * A static text format object, which can be used over and
   * over to create the text serializers.    
   */
  private OutputProperties m_textformat = new OutputProperties(Method.Text);

  // Commenteded out in response to problem reported by 
  // Nicola Brown <Nicola.Brown@jacobsrimell.com>
  //  /**
  //   * Flag to let us know if an exception should be reported inside the 
  //   * postExceptionFromThread method.  This is needed if the transform is 
  //   * being generated from SAX events, and thus there is no central place 
  //   * to report the exception from.  (An exception is usually picked up in 
  //   * the main thread from the transform thread in {@link #transform(Source source)} 
  //   * from {@link #getExceptionThrown()}. )
  //   */
  //  private boolean m_reportInPostExceptionFromThread = false;

  /**
   * A node vector used as a stack to track the current
   * ElemTemplateElement.  Needed for the
   * org.apache.xalan.transformer.TransformState interface,
   * so a tool can discover the calling template. Note the use of an array 
   * for this limits the recursion depth to 4K.
   */
  ElemTemplateElement[] m_currentTemplateElements 
      = new ElemTemplateElement[XPathContext.RECURSIONLIMIT];
  
  /** The top of the currentTemplateElements stack. */
  int m_currentTemplateElementsTop = 0;

  /**
   * A node vector used as a stack to track the current
   * ElemTemplate that was matched.
   * Needed for the
   * org.apache.xalan.transformer.TransformState interface,
   * so a tool can discover the matched template
   */
  Stack m_currentMatchTemplates = new Stack();

  /**
   * A node vector used as a stack to track the current
   * node that was matched.
   * Needed for the
   * org.apache.xalan.transformer.TransformState interface,
   * so a tool can discover the matched
   * node. 
   */
  NodeVector m_currentMatchedNodes = new NodeVector();

  /**
   * The root of a linked set of stylesheets.
   */
  private StylesheetRoot m_stylesheetRoot = null;

  /**
   * If this is set to true, do not warn about pattern
   * match conflicts.
   */
  private boolean m_quietConflictWarnings = true;

  /**
   * The liason to the XML parser, so the XSL processor
   * can handle included files, and the like, and do the
   * initial parse of the XSL document.
   */
  private XPathContext m_xcontext;

  /**
   * Object to guard agains infinite recursion when
   * doing queries.
   */
  private StackGuard m_stackGuard = new StackGuard();

  /**
   * Output handler to bottleneck SAX events.
   */
  private ResultTreeHandler m_resultTreeHandler;

  /** The key manager, which manages xsl:keys. */
  private KeyManager m_keyManager = new KeyManager();

  /**
   * Stack for the purposes of flagging infinite recursion with
   * attribute sets.
   */
  Stack m_attrSetStack = null;

  /**
   * The table of counters for xsl:number support.
   * @see ElemNumber
   */
  CountersTable m_countersTable = null;

  /**
   * Is > 0 when we're processing a for-each.
   */
  BoolStack m_currentTemplateRuleIsNull = new BoolStack();

  /**
   * The message manager, which manages error messages, warning
   * messages, and other types of message events.   
   */
  private MsgMgr m_msgMgr;

  /**
   * This is a compile-time flag to turn off calling
   * of trace listeners. Set this to false for optimization purposes.
   */
  public static boolean S_DEBUG = false;

  /**
   * This property specifies whether the transformation phase should
   * keep track of line and column numbers for the input source
   * document. By default is false. */
  protected boolean m_useSourceLocationProperty = false;
  
  /**
   * The SAX error handler, where errors and warnings are sent.
   */
  private ErrorListener m_errorHandler =
    new org.apache.xml.utils.DefaultErrorHandler();

  /**
   * The trace manager.
   */
  private TraceManager m_traceManager = new TraceManager(this);

  /**
   * If the transform thread throws an exception, the exception needs to
   * be stashed away so that the main thread can pass it on to the
   * client. 
   */
  private Exception m_exceptionThrown = null;

  /**
   * The InputSource for the source tree, which is needed if the
   * parse thread is not the main thread, in order for the parse
   * thread's run method to get to the input source.
   * (Delete this if reversing threads is outlawed. -sb)    
   */
  private Source m_xmlSource;

  /**
   * This is needed for support of setSourceTreeDocForThread(Node doc),
   * which must be called in order for the transform thread's run
   * method to obtain the root of the source tree to be transformed.     
   */
  private int m_doc;

  /**
   * If the the transform is on the secondary thread, we
   * need to know when it is done, so we can return.
   */
  private boolean m_isTransformDone = false;

  /** Flag to to tell if the tranformer needs to be reset. */
  private boolean m_hasBeenReset = false;

  /** NEEDSDOC Field m_shouldReset          */
  private boolean m_shouldReset = true;

  /**
   * NEEDSDOC Method setShouldReset 
   *
   *
   * NEEDSDOC @param shouldReset
   */
  public void setShouldReset(boolean shouldReset)
  {
    m_shouldReset = shouldReset;
  }

  /**
   * A stack of current template modes.
   */
  private Stack m_modes = new Stack();

  //==========================================================
  // SECTION: Constructors
  //==========================================================

  /**
   * Construct a TransformerImpl.
   *
   * @param stylesheet The root of the stylesheet tree.
   */
  public TransformerImpl(StylesheetRoot stylesheet)
  {

    setStylesheet(stylesheet);
    setXPathContext(new XPathContext(this));
    getXPathContext().setNamespaceContext(stylesheet);
  }

  /**
   * Reset the state.  This needs to be called after a process() call
   * is invoked, if the processor is to be used again.
   */
  public void reset()
  {

    if (!m_hasBeenReset && m_shouldReset)
    {
      m_hasBeenReset = true;

      if (this.m_outputStream != null)
      {
        try
        {
          m_outputStream.close();
        }
        catch (java.io.IOException ioe){}
      }

      m_outputStream = null;

      // I need to look more carefully at which of these really
      // needs to be reset.
      m_countersTable = null;
      m_stackGuard = new StackGuard();

      m_xcontext.reset();
      m_xcontext.getVarStack().reset();

      int n = m_currentTemplateElements.length;
      for (int i = 0; i < n; i++) 
      {
        m_currentTemplateElements[i] = null;
      }
      m_currentTemplateElementsTop = 0;
      
      m_currentMatchTemplates.removeAllElements();
      m_currentMatchedNodes.removeAllElements();

      m_resultTreeHandler = null;
      m_outputTarget = null;
      m_keyManager = new KeyManager();
      m_attrSetStack = null;
      m_countersTable = null;
      m_currentTemplateRuleIsNull = new BoolStack();
      m_xmlSource = null;
      m_doc = DTM.NULL;
      m_isTransformDone = false;
      m_transformThread = null;

      // m_inputContentHandler = null;
      // For now, reset the document cache each time.
      m_xcontext.getSourceTreeManager().reset();
    }

    //    m_reportInPostExceptionFromThread = false;
  }

  /**
   * <code>getProperty</code> returns the current setting of the
   * property described by the <code>property</code> argument.
   *
   * @param property a <code>String</code> value
   * @return a <code>boolean</code> value
   */
  public boolean getProperty(String property)
  {
    if (property.equals(XalanProperties.SOURCE_LOCATION))
      return m_useSourceLocationProperty;

    return false;
  }

  /**
   * Set a runtime property for this <code>TransformerImpl</code>.
   *
   * @param property a <code>String</code> value
   * @param value an <code>Object</code> value
   */
  public void setProperty(String property, Object value)
  {
    if (property.equals(XalanProperties.SOURCE_LOCATION)) {
      if (!(value instanceof Boolean))
        throw new RuntimeException(XSLMessages.createMessage(XSLTErrorResources.ER_PROPERTY_VALUE_BOOLEAN, new Object[]{XalanProperties.SOURCE_LOCATION})); //"Value for property "
                                   //+ XalanProperties.SOURCE_LOCATION
                                   //+ " should be a Boolean instance");
      m_useSourceLocationProperty = ((Boolean)value).booleanValue();
    }
  }

  // ========= Transformer Interface Implementation ==========

  /**
   * <meta name="usage" content="experimental"/>
   * Get true if the parser events should be on the main thread,
   * false if not.  Experimental.  Can not be set right now.
   *
   * @return true if the parser events should be on the main thread,
   * false if not.
   */
  public boolean isParserEventsOnMain()
  {
    return m_parserEventsOnMain;
  }

  /**
   * <meta name="usage" content="internal"/>
   * Get the thread that the transform process is on.
   *
   * @return The thread that the transform process is on, or null.
   */
  public Thread getTransformThread()
  {
    return m_transformThread;
  }

  /**
   * <meta name="usage" content="internal"/>
   * Get the thread that the transform process is on.
   *
   * @param t The transform thread, may be null.
   */
  public void setTransformThread(Thread t)
  {
    m_transformThread = t;
  }

  /** NEEDSDOC Field m_hasTransformThreadErrorCatcher          */
  private boolean m_hasTransformThreadErrorCatcher = false;

  /**
   * Return true if the transform was initiated from the transform method,
   * otherwise it was probably done from a pure parse events.
   *
   * NEEDSDOC ($objectName$) @return
   */
  public boolean hasTransformThreadErrorCatcher()
  {
    return m_hasTransformThreadErrorCatcher;
  }
        
        /**
   * Process the source tree to SAX parse events.
   * @param source  The input for the source tree.
   *
   * @throws TransformerException
   */
  public void transform(Source source) throws TransformerException
  {
                transform(source, true); 
        }

  /**
   * Process the source tree to SAX parse events.
   * @param source  The input for the source tree.
   * @param shouldRelease  Flag indicating whether to release DTMManager.
   *
   * @throws TransformerException
   */
  public void transform(Source source, boolean shouldRelease) throws TransformerException
  {

    try
    {
      String base = source.getSystemId();
      
      // If no systemID of the source, use the base of the stylesheet.
      if(null == base)
      {
        base = m_stylesheetRoot.getBaseIdentifier();
      }

      // As a last resort, use the current user dir.
      if(null == base)
      {
        String currentDir = System.getProperty("user.dir");
        
        if (currentDir.startsWith(java.io.File.separator))
          base = "file://" + currentDir;
        else
          base = "file:///" + currentDir;
        
        base = base + java.io.File.separatorChar
               + source.getClass().getName();
      }
      setBaseURLOfSource(base);
      DTMManager mgr = m_xcontext.getDTMManager();
      DTM dtm = mgr.getDTM(source, false, this, true, true);
      dtm.setProperty(XalanProperties.SOURCE_LOCATION,
                      new Boolean(m_useSourceLocationProperty));
      
      boolean hardDelete = true;  // %REVIEW% I have to think about this. -sb

      try
      {
        this.transformNode(dtm.getDocument());
      }
      finally
      {
        if (shouldRelease)
          mgr.release(dtm, hardDelete);
      }

      // Kick off the parse.  When the ContentHandler gets 
      // the startDocument event, it will call transformNode( node ).
      // reader.parse( xmlSource );
      // This has to be done to catch exceptions thrown from 
      // the transform thread spawned by the STree handler.
      Exception e = getExceptionThrown();

      if (null != e)
      {
        if (e instanceof javax.xml.transform.TransformerException)
        {
          throw (javax.xml.transform.TransformerException) e;
        }
        else if (e instanceof org.apache.xml.utils.WrappedRuntimeException)
        {
          m_errorHandler.fatalError(
            new javax.xml.transform.TransformerException(
              ((org.apache.xml.utils.WrappedRuntimeException) e).getException()));
        }
        else
        {
          throw new javax.xml.transform.TransformerException(e);
        }
      }
      else if (null != m_resultTreeHandler)
      {
        m_resultTreeHandler.endDocument();
      }
    }
    catch (org.apache.xml.utils.WrappedRuntimeException wre)
    {
      Throwable throwable = wre.getException();

      while (throwable
             instanceof org.apache.xml.utils.WrappedRuntimeException)
      {
        throwable =
          ((org.apache.xml.utils.WrappedRuntimeException) throwable).getException();
      }

      m_errorHandler.fatalError(new TransformerException(wre.getException()));
    }

    // Patch attributed to David Eisenberg <david@catcode.com>
    catch (org.xml.sax.SAXParseException spe)
    {
      String msg = spe.getMessage();
      SAXSourceLocator loc = new SAXSourceLocator(spe);

      //m_errorHandler.fatalError(new TransformerException( msg, loc ));
      m_errorHandler.fatalError(new TransformerException(spe));
    }
    catch (org.xml.sax.SAXException se)
    {
      m_errorHandler.fatalError(new TransformerException(se));
    }
    finally
    {
      m_hasTransformThreadErrorCatcher = false;

      // This looks to be redundent to the one done in TransformNode.
      reset();
    }
  }

  /**
   * Get the base URL of the source.
   *
   * @return The base URL of the source tree, or null.
   */
  public String getBaseURLOfSource()
  {
    return m_urlOfSource;
  }

  /**
   * Get the base URL of the source.
   *
   *
   * NEEDSDOC @param base
   * @return The base URL of the source tree, or null.
   */
  public void setBaseURLOfSource(String base)
  {
    m_urlOfSource = base;
  }

  /**
   * Get the original output target.
   *
   * @return The Result object used to kick of the transform or null.
   */
  public Result getOutputTarget()
  {
    return m_outputTarget;
  }

  /**
   * Set the original output target.  This is useful when using a SAX transform and
   * supplying a ContentHandler or when the URI of the output target should
   * not be the same as the systemID of the original output target.
   *
   *
   * NEEDSDOC @param outputTarget
   */
  public void setOutputTarget(Result outputTarget)
  {
    m_outputTarget = outputTarget;
  }

  /**
   * Get an output property that is in effect for the
   * transformation.  The property specified may be a property
   * that was set with setOutputProperty, or it may be a
   * property specified in the stylesheet.
   *
   * @param name A non-null String that specifies an output
   * property name, which may be namespace qualified.
   *
   * NEEDSDOC @param qnameString
   *
   * @return The string value of the output property, or null
   * if no property was found.
   *
   * @throws IllegalArgumentException If the property is not supported.
   *
   * @see javax.xml.transform.OutputKeys
   */
  public String getOutputProperty(String qnameString)
          throws IllegalArgumentException
  {

    String value = null;
    OutputProperties props = getOutputFormat();

    value = props.getProperty(qnameString);

    if (null == value)
    {
      if (!props.isLegalPropertyKey(qnameString))
        throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_OUTPUT_PROPERTY_NOT_RECOGNIZED, new Object[]{qnameString})); //"output property not recognized: "
                                           //+ qnameString);
    }

    return value;
  }

  /**
   * Get the value of a property, without using the default properties.  This
   * can be used to test if a property has been explicitly set by the stylesheet
   * or user.
   *
   * @param name The property name, which is a fully-qualified URI.
   *
   * NEEDSDOC @param qnameString
   *
   * @return The value of the property, or null if not found.
   *
   * @throws IllegalArgumentException If the property is not supported,
   * and is not namespaced.
   */
  public String getOutputPropertyNoDefault(String qnameString)
          throws IllegalArgumentException
  {

    String value = null;
    OutputProperties props = getOutputFormat();

    value = (String) props.getProperties().get(qnameString);

    if (null == value)
    {
      if (!props.isLegalPropertyKey(qnameString))
        throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_OUTPUT_PROPERTY_NOT_RECOGNIZED, new Object[]{qnameString})); //"output property not recognized: "
                                          // + qnameString);
    }

    return value;
  }

  /**
   * Set the value of a property.  Recognized properties are:
   *
   * <p>"http://xml.apache.org/xslt/sourcebase" - the base URL for the
   * source, which is needed when pure SAX ContentHandler transformation
   * is to be done.</p>
   *
   * @param name The property name, which is a fully-qualified URI.
   * @param value The requested value for the property.
   * @throws IllegalArgumentException if the property name is not legal.
   */
  public void setOutputProperty(String name, String value)
          throws IllegalArgumentException
  {

    synchronized (m_reentryGuard)
    {

      // Get the output format that was set by the user, otherwise get the 
      // output format from the stylesheet.
      if (null == m_outputFormat)
      {
        m_outputFormat =
          (OutputProperties) getStylesheet().getOutputComposed().clone();
      }

      if (!m_outputFormat.isLegalPropertyKey(name))
        throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_OUTPUT_PROPERTY_NOT_RECOGNIZED, new Object[]{name})); //"output property not recognized: "
                                           //+ name);

      m_outputFormat.setProperty(name, value);
    }
  }

  /**
   * Set the output properties for the transformation.  These
   * properties will override properties set in the templates
   * with xsl:output.
   *
   * <p>If argument to this function is null, any properties
   * previously set will be removed.</p>
   *
   * @param oformat A set of output properties that will be
   * used to override any of the same properties in effect
   * for the transformation.
   */
  public void setOutputProperties(Properties oformat)
  {

    synchronized (m_reentryGuard)
    {
      if (null != oformat)
      {

        // See if an *explicit* method was set.
        String method = (String) oformat.get(OutputKeys.METHOD);

        if (null != method)
          m_outputFormat = new OutputProperties(method);
        else
          m_outputFormat = new OutputProperties();
      }

      if (null != oformat)
      {
        m_outputFormat.copyFrom(oformat);
      }

      // copyFrom does not set properties that have been already set, so 
      // this must be called after, which is a bit in the reverse from 
      // what one might think.
      m_outputFormat.copyFrom(m_stylesheetRoot.getOutputProperties());
    }
  }

  /**
   * Get a copy of the output properties for the transformation.  These
   * properties will override properties set in the templates
   * with xsl:output.
   *
   * <p>Note that mutation of the Properties object returned will not
   * effect the properties that the transformation contains.</p>
   *
   * @returns A copy of the set of output properties in effect
   * for the next transformation.
   *
   * NEEDSDOC ($objectName$) @return
   */
  public Properties getOutputProperties()
  {
    return (Properties) getOutputFormat().getProperties().clone();
  }

  /**
   * Create a result ContentHandler from a Result object, based
   * on the current OutputProperties.
   *
   * @param outputTarget Where the transform result should go,
   * should not be null.
   *
   * @return A valid ContentHandler that will create the
   * result tree when it is fed SAX events.
   *
   * @throws TransformerException
   */
  public ContentHandler createResultContentHandler(Result outputTarget)
          throws TransformerException
  {
    return createResultContentHandler(outputTarget, getOutputFormat());
  }

  /**
   * Create a ContentHandler from a Result object and an OutputProperties.
   *
   * @param outputTarget Where the transform result should go,
   * should not be null.
   * @param format The OutputProperties object that will contain
   * instructions on how to serialize the output.
   *
   * @return A valid ContentHandler that will create the
   * result tree when it is fed SAX events.
   *
   * @throws TransformerException
   */
  public ContentHandler createResultContentHandler(
          Result outputTarget, OutputProperties format)
            throws TransformerException
  {

    ContentHandler handler = null;

    // If the Result object contains a Node, then create 
    // a ContentHandler that will add nodes to the input node.
    org.w3c.dom.Node outputNode = null;

    if (outputTarget instanceof DOMResult)
    {
      outputNode = ((DOMResult) outputTarget).getNode();

      org.w3c.dom.Document doc;
      short type;

      if (null != outputNode)
      {
        type = outputNode.getNodeType();
        doc = (org.w3c.dom.Node.DOCUMENT_NODE == type)
              ? (org.w3c.dom.Document) outputNode
              : outputNode.getOwnerDocument();
      }
      else
      {
        doc = org.apache.xpath.DOMHelper.createDocument();
        outputNode = doc;
        type = outputNode.getNodeType();

        ((DOMResult) outputTarget).setNode(outputNode);
      }

      handler =
        (org.w3c.dom.Node.DOCUMENT_FRAGMENT_NODE == type)
        ? new DOMBuilder(doc, (org.w3c.dom.DocumentFragment) outputNode)
        : new DOMBuilder(doc, outputNode);
    }
    else if (outputTarget instanceof SAXResult)
    {
      handler = ((SAXResult) outputTarget).getHandler();

      if (null == handler)
        throw new IllegalArgumentException(
          "handler can not be null for a SAXResult");
    }

    // Otherwise, create a ContentHandler that will serialize the 
    // result tree to either a stream or a writer.
    else if (outputTarget instanceof StreamResult)
    {
      StreamResult sresult = (StreamResult) outputTarget;
      String method = format.getProperty(OutputKeys.METHOD);

      try
      {
        Serializer serializer =
          SerializerFactory.getSerializer(format.getProperties());

        if (null != sresult.getWriter())
          serializer.setWriter(sresult.getWriter());
        else if (null != sresult.getOutputStream())
          serializer.setOutputStream(sresult.getOutputStream());
        else if (null != sresult.getSystemId())
        {
          String fileURL = sresult.getSystemId();

          if (fileURL.startsWith("file:///"))
          {
            if (fileURL.substring(8).indexOf(":") >0)
              fileURL = fileURL.substring(8);
            else 
              fileURL = fileURL.substring(7);
          }

          m_outputStream = new java.io.FileOutputStream(fileURL);

          serializer.setOutputStream(m_outputStream);
        }
        else
          throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_NO_OUTPUT_SPECIFIED, null)); //"No output specified!");

        handler = serializer.asContentHandler();

        this.setSerializer(serializer);
      }
      catch (UnsupportedEncodingException uee)
      {
        throw new TransformerException(uee);
      }
      catch (IOException ioe)
      {
        throw new TransformerException(ioe);
      }
    }
    else
    {
      throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_CANNOT_TRANSFORM_TO_RESULT_TYPE, new Object[]{outputTarget.getClass().getName()})); //"Can't transform to a Result of type "
                                     //+ outputTarget.getClass().getName()
                                     //+ "!");
    }

    return handler;
  }
        
        /**
   * Process the source tree to the output result.
   * @param xmlSource  The input for the source tree.
   * @param outputTarget The output source target.
   *
   * @throws TransformerException
   */
  public void transform(Source xmlSource, Result outputTarget)
          throws TransformerException
  {
                transform(xmlSource, outputTarget, true);
        }

  /**
   * Process the source tree to the output result.
   * @param xmlSource  The input for the source tree.
   * @param outputTarget The output source target.
   * @param shouldRelease  Flag indicating whether to release DTMManager. 
   *
   * @throws TransformerException
   */
  public void transform(Source xmlSource, Result outputTarget, boolean shouldRelease)
          throws TransformerException
  {

    synchronized (m_reentryGuard)
    {
      ContentHandler handler = createResultContentHandler(outputTarget);

      m_outputTarget = outputTarget;

      this.setContentHandler(handler);
      transform(xmlSource, shouldRelease);
    }
  }

  /**
   * Process the source node to the output result, if the
   * processor supports the "http://xml.org/trax/features/dom/input"
   * feature.
   * %REVIEW% Do we need a Node version of this?
   * @param node  The input source node, which can be any valid DTM node.
   * @param outputTarget The output source target.
   *
   * @throws TransformerException
   */
  public void transformNode(int node, Result outputTarget)
          throws TransformerException
  {

    ContentHandler handler = createResultContentHandler(outputTarget);

    m_outputTarget = outputTarget;

    this.setContentHandler(handler);
    transformNode(node);
  }

  /**
   * Process the source node to the output result, if the
   * processor supports the "http://xml.org/trax/features/dom/input"
   * feature.
   * %REVIEW% Do we need a Node version of this?
   * @param node  The input source node, which can be any valid DTM node.
   * @param outputTarget The output source target.
   *
   * @throws TransformerException
   */
  public void transformNode(int node) throws TransformerException
  {

    // Make sure we're not writing to the same output content handler.
    synchronized (m_outputContentHandler)
    {
      m_hasBeenReset = false;
      
      XPathContext xctxt = getXPathContext();
      DTM dtm = xctxt.getDTM(node);

      try
      {
        pushGlobalVars(node);

        // ==========
        // Give the top-level templates a chance to pass information into 
        // the context (this is mainly for setting up tables for extensions).
        StylesheetRoot stylesheet = this.getStylesheet();
        int n = stylesheet.getGlobalImportCount();

        for (int i = 0; i < n; i++)
        {
          StylesheetComposed imported = stylesheet.getGlobalImport(i);
          int includedCount = imported.getIncludeCountComposed();

          for (int j = -1; j < includedCount; j++)
          {
            Stylesheet included = imported.getIncludeComposed(j);

            included.runtimeInit(this);

            for (ElemTemplateElement child = included.getFirstChildElem();
                    child != null; child = child.getNextSiblingElem())
            {
              child.runtimeInit(this);
            }
          }
        }

        // ===========
        // System.out.println("Calling applyTemplateToNode - "+Thread.currentThread().getName());
        DTMIterator dtmIter = new org.apache.xpath.axes.SelfIteratorNoPredicate();
        dtmIter.setRoot(node, xctxt);
        xctxt.pushContextNodeList(dtmIter);
        try
        {
          this.applyTemplateToNode(null, null, node);
        }
        finally
        {
          xctxt.popContextNodeList();
        }
        // m_stylesheetRoot.getStartRule().execute(this);

        // System.out.println("Done with applyTemplateToNode - "+Thread.currentThread().getName());
        if (null != m_resultTreeHandler)
        {
          m_resultTreeHandler.endDocument();
        }
      }
      catch (Exception se)
      {

        // System.out.println(Thread.currentThread().getName()+" threw an exception! "
        //                   +se.getMessage());
        // If an exception was thrown, we need to make sure that any waiting 
        // handlers can terminate, which I guess is best done by sending 
        // an endDocument.
        
        // SAXSourceLocator
        while(se instanceof org.apache.xml.utils.WrappedRuntimeException)
        {
          Exception e = ((org.apache.xml.utils.WrappedRuntimeException)se).getException();
          if(null != e)
            se = e;
        }
        
        if (null != m_resultTreeHandler)
        {
          try
          {
            if(se instanceof org.xml.sax.SAXParseException)
              m_resultTreeHandler.fatalError((org.xml.sax.SAXParseException)se);
            else
              m_resultTreeHandler.fatalError(new org.xml.sax.SAXParseException(se.getMessage(), new SAXSourceLocator(), se));              
          }
          catch (Exception e){}
        }        
        
        if(se instanceof TransformerException)
        {
          m_errorHandler.fatalError((TransformerException)se);
        }
        else if(se instanceof org.xml.sax.SAXParseException)
        {
          m_errorHandler.fatalError(new TransformerException(se.getMessage(), 
                      new SAXSourceLocator((org.xml.sax.SAXParseException)se), 
                      se));
        }
        else
        {
          m_errorHandler.fatalError(new TransformerException(se));
        }
        
      }
      finally
      {
        this.reset();
      }
    }
  }

  /**
   * Get a SAX2 ContentHandler for the input.
   *
   * @return A valid ContentHandler, which should never be null, as
   * long as getFeature("http://xml.org/trax/features/sax/input")
   * returns true.
   */
  public ContentHandler getInputContentHandler()
  {
    return getInputContentHandler(false);
  }

  /**
   * Get a SAX2 ContentHandler for the input.
   *
   * @param doDocFrag true if a DocumentFragment should be created as
   * the root, rather than a Document.
   *
   * @return A valid ContentHandler, which should never be null, as
   * long as getFeature("http://xml.org/trax/features/sax/input")
   * returns true.
   */
  public ContentHandler getInputContentHandler(boolean doDocFrag)
  {

    if (null == m_inputContentHandler)
    {

      //      if(null == m_urlOfSource && null != m_stylesheetRoot)
      //        m_urlOfSource = m_stylesheetRoot.getBaseIdentifier();
      m_inputContentHandler = new TransformerHandlerImpl(this, doDocFrag,
              m_urlOfSource);
    }

    return m_inputContentHandler;
  }

  /**
   * Get a SAX2 DeclHandler for the input.
   * @return A valid DeclHandler, which should never be null, as
   * long as getFeature("http://xml.org/trax/features/sax/input")
   * returns true.
   */
  public DeclHandler getInputDeclHandler()
  {

    if (m_inputContentHandler instanceof DeclHandler)
      return (DeclHandler) m_inputContentHandler;
    else
      return null;
  }

  /**
   * Get a SAX2 LexicalHandler for the input.
   * @return A valid LexicalHandler, which should never be null, as
   * long as getFeature("http://xml.org/trax/features/sax/input")
   * returns true.
   */
  public LexicalHandler getInputLexicalHandler()
  {

    if (m_inputContentHandler instanceof LexicalHandler)
      return (LexicalHandler) m_inputContentHandler;
    else
      return null;
  }

  /**
   * Set the output properties for the transformation.  These
   * properties will override properties set in the templates
   * with xsl:output.
   *
   * @param oformat A valid OutputProperties object (which will
   * not be mutated), or null.
   */
  public void setOutputFormat(OutputProperties oformat)
  {
    m_outputFormat = oformat;
  }

  /**
   * Get the output properties used for the transformation.
   *
   * @return the output format that was set by the user,
   * otherwise the output format from the stylesheet.
   */
  public OutputProperties getOutputFormat()
  {

    // Get the output format that was set by the user, otherwise get the 
    // output format from the stylesheet.
    OutputProperties format = (null == m_outputFormat)
                              ? getStylesheet().getOutputComposed()
                              : m_outputFormat;

    return format;
  }

  /**
   * <meta name="usage" content="internal"/>
   * Get the current serializer in use, which may well not
   * be the main serializer (for instance, this may well be
   * a text serializer for string creation from templates).
   *
   * @return The current serializer, or null if there is none.
   */
  public Serializer getSerializer()
  {
    return m_serializer;
  }

  /**
   * <meta name="usage" content="internal"/>
   * Set the current serializer.
   *
   * @param s The current serializer, or null.
   */
  public void setSerializer(Serializer s)
  {
    m_serializer = s;
  }
  
  /**
   * Set a parameter for the templates.
   * 
   * @param name The name of the parameter.
   * @param namespace The namespace of the parameter.
   * @param value The value object.  This can be any valid Java object
   * -- it's up to the processor to provide the proper
   * coersion to the object, or simply pass it on for use
   * in extensions.
   */
  public void setParameter(String name, String namespace, Object value)
  {

    VariableStack varstack = getXPathContext().getVarStack();
    QName qname = new QName(namespace, name);
    XObject xobject = XObject.create(value, getXPathContext());
    
    StylesheetRoot sroot = m_stylesheetRoot;
    Vector vars = sroot.getVariablesAndParamsComposed();
    int i = vars.size();
    while (--i >= 0)
    {
      ElemVariable variable = (ElemVariable)vars.elementAt(i);
      if(variable.getXSLToken() == Constants.ELEMNAME_PARAMVARIABLE && 
         variable.getName().equals(qname))
      {
          varstack.setGlobalVariable(i, xobject);
      }
    }
  }

  /** NEEDSDOC Field m_userParams          */
  Vector m_userParams;

  /**
   * Set a parameter for the transformation.
   *
   * @param name The name of the parameter,
   *             which may have a namespace URI.
   * @param value The value object.  This can be any valid Java object
   * -- it's up to the processor to provide the proper
   * coersion to the object, or simply pass it on for use
   * in extensions.
   */
  public void setParameter(String name, Object value)
  {

    StringTokenizer tokenizer = new StringTokenizer(name, "{}", false);

    try
    {

      // The first string might be the namespace, or it might be 
      // the local name, if the namespace is null.
      String s1 = tokenizer.nextToken();
      String s2 = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : null;

      if (null == m_userParams)
        m_userParams = new Vector();

      if (null == s2)
      {
        replaceOrPushUserParam(new QName(s1), XObject.create(value, getXPathContext()));
        setParameter(s1, null, value);
      }
      else
      {
        replaceOrPushUserParam(new QName(s1, s2), XObject.create(value, getXPathContext()));
        setParameter(s2, s1, value);
      }
    }
    catch (java.util.NoSuchElementException nsee)
    {

      // Should throw some sort of an error.
    }
  }

  /**
   * NEEDSDOC Method replaceOrPushUserParam 
   *
   *
   * NEEDSDOC @param qname
   * NEEDSDOC @param xval
   */
  private void replaceOrPushUserParam(QName qname, XObject xval)
  {

    int n = m_userParams.size();

    for (int i = n - 1; i >= 0; i--)
    {
      Arg arg = (Arg) m_userParams.elementAt(i);

      if (arg.getQName().equals(qname))
      {
        m_userParams.setElementAt(new Arg(qname, xval, true), i);

        return;
      }
    }

    m_userParams.addElement(new Arg(qname, xval, true));
  }

  /**
   * Get a parameter that was explicitly set with setParameter
   * or setParameters.
   *
   *
   * NEEDSDOC @param name
   * @return A parameter that has been set with setParameter
   * or setParameters,
   * *not* all the xsl:params on the stylesheet (which require
   * a transformation Source to be evaluated).
   */
  public Object getParameter(String name)
  {

    try
    {

      // VariableStack varstack = getXPathContext().getVarStack();
      // The first string might be the namespace, or it might be 
      // the local name, if the namespace is null.
      QName qname = QName.getQNameFromString(name);

      if (null == m_userParams)
        return null;

      int n = m_userParams.size();

      for (int i = n - 1; i >= 0; i--)
      {
        Arg arg = (Arg) m_userParams.elementAt(i);

        if (arg.getQName().equals(qname))
        {
          return arg.getVal().object();
        }
      }

      return null;
    }
    catch (java.util.NoSuchElementException nsee)
    {

      // Should throw some sort of an error.
      return null;
    }
  }

  /**
   * Set a bag of parameters for the transformation. Note that
   * these will not be additive, they will replace the existing
   * set of parameters.
   *
   * @param name The name of the parameter,
   *             which may have a namespace URI.
   * @param value The value object.  This can be any valid Java object
   * -- it's up to the processor to provide the proper
   * coersion to the object, or simply pass it on for use
   * in extensions.
   *
   * NEEDSDOC @param params
   */
  public void setParameters(Properties params)
  {

    clearParameters();

    Enumeration names = params.propertyNames();

    while (names.hasMoreElements())
    {
      String name = params.getProperty((String) names.nextElement());
      StringTokenizer tokenizer = new StringTokenizer(name, "{}", false);

      try
      {

        // The first string might be the namespace, or it might be 
        // the local name, if the namespace is null.
        String s1 = tokenizer.nextToken();
        String s2 = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : null;

        if (null == s2)
          setParameter(s1, null, params.getProperty(name));
        else
          setParameter(s2, s1, params.getProperty(name));
      }
      catch (java.util.NoSuchElementException nsee)
      {

        // Should throw some sort of an error.
      }
    }
  }

  /**
   * Reset the parameters to a null list.
   */
  public void clearParameters()
  {

    synchronized (m_reentryGuard)
    {
      VariableStack varstack = new VariableStack();

      m_xcontext.setVarStack(varstack);

      m_userParams = null;
    }
  }


  /**
   * Internal -- push the global variables from the Stylesheet onto
   * the context's runtime variable stack.
   * <p>If we encounter a variable
   * that is already defined in the variable stack, we ignore it.  This
   * is because the second variable definition will be at a lower import
   * precedence.  Presumably, global variables at the same import precedence
   * with the same name will have been caught during the recompose process.
   * <p>However, if we encounter a parameter that is already defined in the
   * variable stack, we need to see if this is a parameter whose value was
   * supplied by a setParameter call.  If so, we need to "receive" the one
   * already in the stack, ignoring this one.  If it is just an earlier
   * xsl:param or xsl:variable definition, we ignore it using the same
   * reasoning as explained above for the variable.
   *
   * @param contextNode The root of the source tree, can't be null.
   *
   * @throws TransformerException
   */
  protected void pushGlobalVars(int contextNode) throws TransformerException
  {

    XPathContext xctxt = m_xcontext;
    VariableStack vs = xctxt.getVarStack();
    StylesheetRoot sr = getStylesheet();
    Vector vars = sr.getVariablesAndParamsComposed();
    
    int i = vars.size();
    vs.link(i);

    while (--i >= 0)
    {
      ElemVariable v = (ElemVariable) vars.elementAt(i);

      // XObject xobj = v.getValue(this, contextNode);
      XObject xobj = new XUnresolvedVariable(v, contextNode, this,
                                     vs.getStackFrame(), 0, true);
      
      if(null == vs.elementAt(i))                               
        vs.setGlobalVariable(i, xobj);
    }

  }

  /**
   * Set an object that will be used to resolve URIs used in
   * document(), etc.
   * @param resolver An object that implements the URIResolver interface,
   * or null.
   */
  public void setURIResolver(URIResolver resolver)
  {

    synchronized (m_reentryGuard)
    {
      m_xcontext.getSourceTreeManager().setURIResolver(resolver);
    }
  }

  /**
   * Get an object that will be used to resolve URIs used in
   * document(), etc.
   *
   * @return An object that implements the URIResolver interface,
   * or null.
   */
  public URIResolver getURIResolver()
  {
    return m_xcontext.getSourceTreeManager().getURIResolver();
  }

  // ======== End Transformer Implementation ========  

  /**
   * Set the content event handler.
   *
   * @param resolver The new content handler.
   *
   * NEEDSDOC @param handler
   * @throws java.lang.NullPointerException If the handler
   *            is null.
   * @see org.xml.sax.XMLReader#setContentHandler
   */
  public void setContentHandler(ContentHandler handler)
  {

    if (handler == null)
    {
      throw new NullPointerException(XSLMessages.createMessage(XSLTErrorResources.ER_NULL_CONTENT_HANDLER, null)); //"Null content handler");
    }
    else
    {
      m_outputContentHandler = handler;

      if (null == m_resultTreeHandler)
        m_resultTreeHandler = new ResultTreeHandler(this, handler);
      else
        m_resultTreeHandler.setContentHandler(handler);
    }
  }

  /**
   * Get the content event handler.
   *
   * @return The current content handler, or null if none was set.
   * @see org.xml.sax.XMLReader#getContentHandler
   */
  public ContentHandler getContentHandler()
  {
    return m_outputContentHandler;
  }

  /**
   * <meta name="usage" content="advanced"/>
   * Given a stylesheet element, create a result tree fragment from it's
   * contents.
   * @param templateParent The template element that holds the fragment.
   * @param sourceNode The current source context node.
   * @param mode The mode under which the template is operating.
   * @return An object that represents the result tree fragment.
   *
   * @throws TransformerException
   */
  public int transformToRTF(ElemTemplateElement templateParent)
          throws TransformerException
  {

    XPathContext xctxt = m_xcontext;
    DTM dtmFrag = xctxt.getDTM(null, true, this, false, false);
    ContentHandler rtfHandler = dtmFrag.getContentHandler();

    // Create a ResultTreeFrag object.
    int resultFragment = resultFragment = dtmFrag.getDocument();

    // Save the current result tree handler.
    ResultTreeHandler savedRTreeHandler = this.m_resultTreeHandler;

    // And make a new handler for the RTF.
    m_resultTreeHandler = new ResultTreeHandler(this, rtfHandler);

    ResultTreeHandler rth = m_resultTreeHandler;

    try
    {
      rth.startDocument();

      try
      {

        // Do the transformation of the child elements.
        executeChildTemplates(templateParent, true);

        // Make sure everything is flushed!
        rth.flushPending();
      }
      finally
      {
        rth.endDocument();
      }
    }
    catch (org.xml.sax.SAXException se)
    {
      throw new TransformerException(se);
    }
    finally
    {

      // Restore the previous result tree handler.
      this.m_resultTreeHandler = savedRTreeHandler;
    }

    return resultFragment;
  }

  /**
   * <meta name="usage" content="internal"/>
   * Get the StringWriter pool, so that StringWriter
   * objects may be reused.
   *
   * @return The string writer pool, not null.
   */
  public ObjectPool getStringWriterPool()
  {
    return m_stringWriterObjectPool;
  }

  /**
   * <meta name="usage" content="advanced"/>
   * Take the contents of a template element, process it, and
   * convert it to a string.
   *
   * @param elem The parent element whose children will be output
   * as a string.
   * @param transformer The XSLT transformer instance.
   * @param sourceNode The current source node context.
   * @param mode The current xslt mode.
   *
   * @return The stringized result of executing the elements children.
   *
   * @throws TransformerException
   */
  public String transformToString(ElemTemplateElement elem)
          throws TransformerException
  {
    ElemTemplateElement firstChild = elem.getFirstChildElem();
    if(null == firstChild)
      return "";
    if(elem.hasTextLitOnly() && org.apache.xalan.processor.TransformerFactoryImpl.m_optimize)
    {
      return ((ElemTextLiteral)firstChild).getNodeValue();
    }

    // Save the current result tree handler.
    ResultTreeHandler savedRTreeHandler = this.m_resultTreeHandler;

    // Create a Serializer object that will handle the SAX events 
    // and build the ResultTreeFrag nodes.
    StringWriter sw = (StringWriter) m_stringWriterObjectPool.getInstance();

    m_resultTreeHandler =
      (ResultTreeHandler) m_textResultHandlerObjectPool.getInstance();

    Serializer serializer = m_resultTreeHandler.getSerializer();

    try
    {
      if (null == serializer)
      {
        serializer =
          SerializerFactory.getSerializer(m_textformat.getProperties());

        m_resultTreeHandler.setSerializer(serializer);
        serializer.setWriter(sw);

        ContentHandler shandler = serializer.asContentHandler();

        m_resultTreeHandler.init(this, shandler);
      }
      else
      {

        // Leave Commented.  -sb
        // serializer.setWriter(sw);
        // serializer.setOutputFormat(m_textformat);
        // ContentHandler shandler = serializer.asContentHandler();
        // m_resultTreeHandler.setContentHandler(shandler);
      }
    }
    catch (IOException ioe)
    {
      throw new TransformerException(ioe);
    }

    String result;

    try
    {
      this.m_resultTreeHandler.startDocument();

      // Do the transformation of the child elements.
      executeChildTemplates(elem, true);
      this.m_resultTreeHandler.endDocument();

      result = sw.toString();
    }
    catch (org.xml.sax.SAXException se)
    {
      throw new TransformerException(se);
    }
    finally
    {
      sw.getBuffer().setLength(0);

      try
      {
        sw.close();
      }
      catch (Exception ioe){}

      m_stringWriterObjectPool.freeInstance(sw);
      m_textResultHandlerObjectPool.freeInstance(m_resultTreeHandler);
      m_resultTreeHandler.reset();

      // Restore the previous result tree handler.
      m_resultTreeHandler = savedRTreeHandler;
    }

    return result;
  }

  /**
   * <meta name="usage" content="advanced"/>
   * Given an element and mode, find the corresponding
   * template and process the contents.
   *
   * @param xslInstruction The calling element.
   * @param template The template to use if xsl:for-each, or null.
   * @param child The source context node.
   * @param mode The current mode, may be null.
   * @throws TransformerException
   * @return true if applied a template, false if not.
   */
  public boolean applyTemplateToNode(ElemTemplateElement xslInstruction,  // xsl:apply-templates or xsl:for-each
                                     ElemTemplate template, int child)
                                             throws TransformerException
  {

    DTM dtm = m_xcontext.getDTM(child);
    short nodeType = dtm.getNodeType(child);
    boolean isDefaultTextRule = false;

    if (null == template)
    {
      int maxImportLevel;
      boolean isApplyImports = ((xslInstruction == null)
                                ? false
                                : xslInstruction.getXSLToken()
                                  == Constants.ELEMNAME_APPLY_IMPORTS);

      if (isApplyImports)
      {
        maxImportLevel =
          xslInstruction.getStylesheetComposed().getImportCountComposed() - 1;
      }
      else
      {
        maxImportLevel = -1;
      }

      // If we're trying an xsl:apply-imports at the top level (ie there are no
      // imported stylesheets), we need to indicate that there is no matching template.
      // The above logic will calculate a maxImportLevel of -1 which indicates
      // that we should find any template.  This is because a value of -1 for
      // maxImportLevel has a special meaning.  But we don't want that.
      // We want to match -no- templates. See bugzilla bug 1170.
      if (isApplyImports && (maxImportLevel == -1))
      {
        template = null;
      }
      else
      {

        // Find the XSL template that is the best match for the 
        // element.        
        XPathContext xctxt = m_xcontext;

        try
        {
          xctxt.pushNamespaceContext(xslInstruction);

          QName mode = this.getMode();

          template = m_stylesheetRoot.getTemplateComposed(xctxt, child, mode,
                  maxImportLevel, m_quietConflictWarnings, dtm);
        }
        finally
        {
          xctxt.popNamespaceContext();
        }
      }

      // If that didn't locate a node, fall back to a default template rule.
      // See http://www.w3.org/TR/xslt#built-in-rule.
      if (null == template)
      {
        switch (nodeType)
        {
        case DTM.DOCUMENT_FRAGMENT_NODE :
        case DTM.ELEMENT_NODE :
          template = m_stylesheetRoot.getDefaultRule();
          break;
        case DTM.CDATA_SECTION_NODE :
        case DTM.TEXT_NODE :
        case DTM.ATTRIBUTE_NODE :
          template = m_stylesheetRoot.getDefaultTextRule();
          isDefaultTextRule = true;
          break;
        case DTM.DOCUMENT_NODE :
          template = m_stylesheetRoot.getDefaultRootRule();
          break;
        default :

          // No default rules for processing instructions and the like.
          return false;
        }
      }
    }

    // If we are processing the default text rule, then just clone 
    // the value directly to the result tree.
    try
    {
      pushElemTemplateElement(template);
      m_xcontext.pushCurrentNode(child);
      pushPairCurrentMatched(template, child);
      
      // Fix copy copy29 test.
      DTMIterator cnl = new org.apache.xpath.NodeSetDTM(child, m_xcontext.getDTMManager());
      m_xcontext.pushContextNodeList(cnl);

      if (isDefaultTextRule)
      {
        switch (nodeType)
        {
        case DTM.CDATA_SECTION_NODE :
        case DTM.TEXT_NODE :
          ClonerToResultTree.cloneToResultTree(child, nodeType, 
                                        dtm, getResultTreeHandler(), false);
          break;
        case DTM.ATTRIBUTE_NODE :
          dtm.dispatchCharactersEvents(child, getResultTreeHandler(), false);
          break;
        }
      }
      else
      {

        // Fire a trace event for the template.
         
                if (TransformerImpl.S_DEBUG)
                  getTraceManager().fireTraceEvent(template);
        // And execute the child templates.
        // 9/11/00: If template has been compiled, hand off to it
        // since much (most? all?) of the processing has been inlined.
        // (It would be nice if there was a single entry point that
        // worked for both... but the interpretive system works by
        // having the Tranformer execute the children, while the
        // compiled obviously has to run its own code. It's
        // also unclear that "execute" is really the right name for
        // that entry point.)
        m_xcontext.setSAXLocator(template);
        // m_xcontext.getVarStack().link();
        m_xcontext.getVarStack().link(template.m_frameSize);
        executeChildTemplates(template, true);
      }
    }
    catch (org.xml.sax.SAXException se)
    {
      throw new TransformerException(se);
    }
    finally
    {
      m_xcontext.getVarStack().unlink();
      m_xcontext.popCurrentNode();
      m_xcontext.popContextNodeList();
      popCurrentMatched();
      popElemTemplateElement();
    }

    return true;
  }
  
  
  /**
   * <meta name="usage" content="advanced"/>
   * Execute each of the children of a template element.  This method
   * is only for extension use.
   *
   * @param elem The ElemTemplateElement that contains the children
   * that should execute.
   * @param sourceNode The current context node.
   * NEEDSDOC @param context
   * @param mode The current mode.
   * @param handler The ContentHandler to where the result events
   * should be fed.
   *
   * @throws TransformerException
   */
  public void executeChildTemplates(
          ElemTemplateElement elem, org.w3c.dom.Node context, QName mode, ContentHandler handler)
            throws TransformerException
  {

    XPathContext xctxt = m_xcontext;

    try
    {
      if(null != mode)
        pushMode(mode);
      xctxt.pushCurrentNode(xctxt.getDTMHandleFromNode(context));
      executeChildTemplates(elem, handler);
    }
    finally
    {
      xctxt.popCurrentNode();
      
      // I'm not sure where or why this was here.  It is clearly in 
      // error though, without a corresponding pushMode().
      if (null != mode)
        popMode();
    }
  }

  /**
   * <meta name="usage" content="advanced"/>
   * Execute each of the children of a template element.
   *
   * @param elem The ElemTemplateElement that contains the children
   * that should execute.
   * @param handler The ContentHandler to where the result events
   * should be fed.
   *
   * @throws TransformerException
   */
  public void executeChildTemplates(
          ElemTemplateElement elem, ContentHandler handler)
            throws TransformerException
  {

    ResultTreeHandler rth = this.getResultTreeHandler();

    // These may well not be the same!  In this case when calling 
    // the Redirect extension, it has already set the ContentHandler
    // in the Transformer.
    ContentHandler savedRTHHandler = rth.getContentHandler();
    ContentHandler savedHandler = this.getContentHandler();

    try
    {
      getResultTreeHandler().flushPending();
      this.setContentHandler(handler);

      // %REVIEW% Make sure current node is being pushed.
      executeChildTemplates(elem, true);
    }
    catch (org.xml.sax.SAXException se)
    {
      throw new TransformerException(se);
    }
    finally
    {
      this.setContentHandler(savedHandler);

      // This fixes a bug where the ResultTreeHandler's ContentHandler
      // was being reset to the wrong ContentHandler.
      rth.setContentHandler(savedRTHHandler);
    }
  }

  /**
   * <meta name="usage" content="advanced"/>
   * Execute each of the children of a template element.
   *
   * @param transformer The XSLT transformer instance.
   *
   * @param elem The ElemTemplateElement that contains the children
   * that should execute.
   * @param sourceNode The current context node.
   * @param mode The current mode.
   * @param shouldAddAttrs true if xsl:attributes should be executed.
   *
   * @throws TransformerException
   */
  public void executeChildTemplates(
          ElemTemplateElement elem, boolean shouldAddAttrs)
            throws TransformerException
  {

    // Does this element have any children?
    ElemTemplateElement t = elem.getFirstChildElem();

    if (null == t)
      return;      
    
    if(elem.hasTextLitOnly() && org.apache.xalan.processor.TransformerFactoryImpl.m_optimize)
    {      
      char[] chars = ((ElemTextLiteral)t).getChars();
      try
      {
        // Have to push stuff on for tooling...
        this.pushElemTemplateElement(t);
        m_resultTreeHandler.characters(chars, 0, chars.length);
      }
      catch(SAXException se)
      {
        throw new TransformerException(se);
      }
      finally
      {
        this.popElemTemplateElement();
      }
      return;
    }

//    // Check for infinite loops if we have to.
//    boolean check = (m_stackGuard.m_recursionLimit > -1);
//
//    if (check)
//      getStackGuard().push(elem, xctxt.getCurrentNode());

    XPathContext xctxt = m_xcontext;
    xctxt.pushSAXLocatorNull();
    int currentTemplateElementsTop = m_currentTemplateElementsTop;
    m_currentTemplateElementsTop++;

    try
    {
      // Loop through the children of the template, calling execute on 
      // each of them.
      for (; t != null; t = t.getNextSiblingElem())
      {
        if (!shouldAddAttrs
                && t.getXSLToken() == Constants.ELEMNAME_ATTRIBUTE)
          continue;

        xctxt.setSAXLocator(t);
        m_currentTemplateElements[currentTemplateElementsTop] = t;
        t.execute(this);
      }
    }
    finally
    {
      m_currentTemplateElementsTop--;
      xctxt.popSAXLocator();
    }

    // Check for infinite loops if we have to
//    if (check)
//      getStackGuard().pop();
  }

  /**
   * <meta name="usage" content="advanced"/>
   * Get the keys for the xsl:sort elements.
   * Note: Should this go into ElemForEach?
   *
   * @param foreach Valid ElemForEach element, not null.
   * @param sourceNodeContext The current node context in the source tree,
   * needed to evaluate the Attribute Value Templates.
   *
   * @return A Vector of NodeSortKeys, or null.
   *
   * @throws TransformerException
   */
  public Vector processSortKeys(ElemForEach foreach, int sourceNodeContext)
          throws TransformerException
  {

    Vector keys = null;
    XPathContext xctxt = m_xcontext;
    int nElems = foreach.getSortElemCount();

    if (nElems > 0)
      keys = new Vector();

    // March backwards, collecting the sort keys.
    for (int i = 0; i < nElems; i++)
    {
      ElemSort sort = foreach.getSortElem(i);
      String langString =
        (null != sort.getLang())
        ? sort.getLang().evaluate(xctxt, sourceNodeContext, foreach) : null;
      String dataTypeString = sort.getDataType().evaluate(xctxt,
                                sourceNodeContext, foreach);

      if (dataTypeString.indexOf(":") >= 0)
        System.out.println(
          "TODO: Need to write the hooks for QNAME sort data type");
      else if (!(dataTypeString.equalsIgnoreCase(Constants.ATTRVAL_DATATYPE_TEXT))
               &&!(dataTypeString.equalsIgnoreCase(
                 Constants.ATTRVAL_DATATYPE_NUMBER)))
        foreach.error(XSLTErrorResources.ER_ILLEGAL_ATTRIBUTE_VALUE,
                      new Object[]{ Constants.ATTRNAME_DATATYPE,
                                    dataTypeString });

      boolean treatAsNumbers =
        ((null != dataTypeString) && dataTypeString.equals(
        Constants.ATTRVAL_DATATYPE_NUMBER)) ? true : false;
      String orderString = sort.getOrder().evaluate(xctxt, sourceNodeContext,
                             foreach);

      if (!(orderString.equalsIgnoreCase(Constants.ATTRVAL_ORDER_ASCENDING))
              &&!(orderString.equalsIgnoreCase(
                Constants.ATTRVAL_ORDER_DESCENDING)))
        foreach.error(XSLTErrorResources.ER_ILLEGAL_ATTRIBUTE_VALUE,
                      new Object[]{ Constants.ATTRNAME_ORDER,
                                    orderString });

      boolean descending =
        ((null != orderString) && orderString.equals(
        Constants.ATTRVAL_ORDER_DESCENDING)) ? true : false;
      AVT caseOrder = sort.getCaseOrder();
      boolean caseOrderUpper;

      if (null != caseOrder)
      {
        String caseOrderString = caseOrder.evaluate(xctxt, sourceNodeContext,
                                                    foreach);

        if (!(caseOrderString.equalsIgnoreCase(Constants.ATTRVAL_CASEORDER_UPPER))
                &&!(caseOrderString.equalsIgnoreCase(
                  Constants.ATTRVAL_CASEORDER_LOWER)))
          foreach.error(XSLTErrorResources.ER_ILLEGAL_ATTRIBUTE_VALUE,
                        new Object[]{ Constants.ATTRNAME_CASEORDER,
                                      caseOrderString });

        caseOrderUpper =
          ((null != caseOrderString) && caseOrderString.equals(
          Constants.ATTRVAL_CASEORDER_UPPER)) ? true : false;
      }
      else
      {
        caseOrderUpper = false;
      }

      keys.addElement(new NodeSortKey(this, sort.getSelect(), treatAsNumbers,
                                      descending, langString, caseOrderUpper,
                                      foreach));
    }

    return keys;
  }

  //==========================================================
  // SECTION: TransformState implementation
  //==========================================================

  /**
   * Push the current template element.
   *
   * @param elem The current ElemTemplateElement (may be null, and then
   * set via setCurrentElement).
   */
  public void pushElemTemplateElement(ElemTemplateElement elem)
  {
    m_currentTemplateElements[m_currentTemplateElementsTop++] = elem;
  }

  /**
   * Pop the current template element.
   */
  public void popElemTemplateElement()
  {
    m_currentTemplateElementsTop--;
  }

  /**
   * Set the top of the current template elements
   * stack.
   *
   * @param e The current ElemTemplateElement about to
   * be executed.
   */
  public void setCurrentElement(ElemTemplateElement e)
  {
    m_currentTemplateElements[m_currentTemplateElementsTop-1] = e;
  }

  /**
   * Retrieves the current ElemTemplateElement that is
   * being executed.
   *
   * @return The current ElemTemplateElement that is executing,
   * should not normally be null.
   */
  public ElemTemplateElement getCurrentElement()
  {
    return m_currentTemplateElements[m_currentTemplateElementsTop-1];
  }

  /**
   * This method retrieves the current context node
   * in the source tree.
   *
   * @return The current context node (should never be null?).
   */
  public int getCurrentNode()
  {
    return m_xcontext.getCurrentNode();
  }

  /**
   * This method retrieves the xsl:template
   * that is in effect, which may be a matched template
   * or a named template.
   *
   * <p>Please note that the ElemTemplate returned may
   * be a default template, and thus may not have a template
   * defined in the stylesheet.</p>
   *
   * @return The current xsl:template, should not be null.
   */
  public ElemTemplate getCurrentTemplate()
  {

    ElemTemplateElement elem = getCurrentElement();

    while ((null != elem)
           && (elem.getXSLToken() != Constants.ELEMNAME_TEMPLATE))
    {
      elem = elem.getParentElem();
    }

    return (ElemTemplate) elem;
  }

  /**
   * Push both the current xsl:template or xsl:for-each onto the
   * stack, along with the child node that was matched.
   * (Note: should this only be used for xsl:templates?? -sb)
   *
   * @param template xsl:template or xsl:for-each.
   * @param child The child that was matched.
   */
  public void pushPairCurrentMatched(ElemTemplateElement template, int child)
  {
    m_currentMatchTemplates.push(template);
    m_currentMatchedNodes.push(child);
  }

  /**
   * Pop the elements that were pushed via pushPairCurrentMatched.
   */
  public void popCurrentMatched()
  {
    m_currentMatchTemplates.pop();
    m_currentMatchedNodes.pop();
  }

  /**
   * This method retrieves the xsl:template
   * that was matched.  Note that this may not be
   * the same thing as the current template (which
   * may be from getCurrentElement()), since a named
   * template may be in effect.
   *
   * @return The pushed template that was pushed via pushPairCurrentMatched.
   */
  public ElemTemplate getMatchedTemplate()
  {
    return (ElemTemplate) m_currentMatchTemplates.peek();
  }

  /**
   * Retrieves the node in the source tree that matched
   * the template obtained via getMatchedTemplate().
   *
   * @return The matched node that corresponds to the
   * match attribute of the current xsl:template.
   */
  public int getMatchedNode()
  {
    return m_currentMatchedNodes.peepTail();
  }

  /**
   * Get the current context node list.
   *
   * @return A reset clone of the context node list.
   */
  public DTMIterator getContextNodeList()
  {

    try
    {
      DTMIterator cnl = m_xcontext.getContextNodeList();

      return (cnl == null) ? null : (DTMIterator) cnl.cloneWithReset();
    }
    catch (CloneNotSupportedException cnse)
    {

      // should never happen.
      return null;
    }
  }

  /**
   * Get the TrAX Transformer object in effect.
   *
   * @return This object.
   */
  public Transformer getTransformer()
  {
    return this;
  }

  //==========================================================
  // SECTION: Accessor Functions
  //==========================================================

  /**
   * Set the stylesheet for this processor.  If this is set, then the
   * process calls that take only the input .xml will use
   * this instead of looking for a stylesheet PI.  Also,
   * setting the stylesheet is needed if you are going
   * to use the processor as a SAX ContentHandler.
   *
   * @param stylesheetRoot A non-null StylesheetRoot object,
   * or null if you wish to clear the stylesheet reference.
   */
  public void setStylesheet(StylesheetRoot stylesheetRoot)
  {
    m_stylesheetRoot = stylesheetRoot;
  }

  /**
   * Get the current stylesheet for this processor.
   *
   * @return The stylesheet that is associated with this
   * transformer.
   */
  public final StylesheetRoot getStylesheet()
  {
    return m_stylesheetRoot;
  }

  /**
   * Get quietConflictWarnings property. If the quietConflictWarnings
   * property is set to true, warnings about pattern conflicts won't be
   * printed to the diagnostics stream.
   *
   * @return True if this transformer should not report
   * template match conflicts.
   */
  public boolean getQuietConflictWarnings()
  {
    return m_quietConflictWarnings;
  }

  /**
   * If the quietConflictWarnings property is set to
   * true, warnings about pattern conflicts won't be
   * printed to the diagnostics stream.
   * False by default.
   * (Currently setting this property will have no effect.)
   *
   * @param b true if conflict warnings should be suppressed.
   */
  public void setQuietConflictWarnings(boolean b)
  {
    m_quietConflictWarnings = b;
  }

  /**
   * <meta name="usage" content="internal"/>
   * Set the execution context for XPath.
   *
   * @param xcontext A non-null reference to the XPathContext
   * associated with this transformer.
   */
  public void setXPathContext(XPathContext xcontext)
  {
    m_xcontext = xcontext;
  }

  /**
   * Get the XPath context associated with this transformer.
   *
   * @return The XPathContext reference, never null.
   */
  public final XPathContext getXPathContext()
  {
    return m_xcontext;
  }

  /**
   * <meta name="usage" content="internal"/>
   * Get the object used to guard the stack from
   * recursion.
   *
   * @return The StackGuard object, which should never be null.
   */
  public StackGuard getStackGuard()
  {
    return m_stackGuard;
  }

  /**
   * Get the recursion limit.
   * Used for infinite loop check. If the value is -1, do not
   * check for infinite loops. Anyone who wants to enable that
   * check should change the value of this variable to be the
   * level of recursion that they want to check. Be careful setting
   * this variable, if the number is too low, it may report an
   * infinite loop situation, when there is none.
   * Post version 1.0.0, we'll make this a runtime feature.
   *
   * @return The limit on recursion, or -1 if no check is to be made.
   */
  public int getRecursionLimit()
  {
    return m_stackGuard.getRecursionLimit();
  }

  /**
   * Get the recursion limit.
   * Used for infinite loop check. If the value is -1, do not
   * check for infinite loops. Anyone who wants to enable that
   * check should change the value of this variable to be the
   * level of recursion that they want to check. Be careful setting
   * this variable, if the number is too low, it may report an
   * infinite loop situation, when there is none.
   * Post version 1.0.0, we'll make this a runtime feature.
   *
   * @param limit A number that represents the limit of recursion,
   * or -1 if no checking is to be done.
   */
  public void setRecursionLimit(int limit)
  {
    m_stackGuard.setRecursionLimit(limit);
  }

  /**
   * Get the ResultTreeHandler object.
   *
   * @return The current ResultTreeHandler, which may not
   * be the main result tree manager.
   */
  public ResultTreeHandler getResultTreeHandler()
  {
    return m_resultTreeHandler;
  }

  /**
   * Get the KeyManager object.
   *
   * @return A reference to the KeyManager object, which should
   * never be null.
   */
  public KeyManager getKeyManager()
  {
    return m_keyManager;
  }

  /**
   * Check to see if this is a recursive attribute definition.
   *
   * @param attrSet A non-null ElemAttributeSet reference.
   *
   * @return true if the attribute set is recursive.
   */
  public boolean isRecursiveAttrSet(ElemAttributeSet attrSet)
  {

    if (null == m_attrSetStack)
    {
      m_attrSetStack = new Stack();
    }

    if (!m_attrSetStack.empty())
    {
      int loc = m_attrSetStack.search(attrSet);

      if (loc > -1)
      {
        return true;
      }
    }

    return false;
  }

  /**
   * Push an executing attribute set, so we can check for
   * recursive attribute definitions.
   *
   * @param attrSet A non-null ElemAttributeSet reference.
   */
  public void pushElemAttributeSet(ElemAttributeSet attrSet)
  {
    m_attrSetStack.push(attrSet);
  }

  /**
   * Pop the current executing attribute set.
   */
  public void popElemAttributeSet()
  {
    m_attrSetStack.pop();
  }

  /**
   * Get the table of counters, for optimized xsl:number support.
   *
   * @return The CountersTable, never null.
   */
  public CountersTable getCountersTable()
  {

    if (null == m_countersTable)
      m_countersTable = new CountersTable();

    return m_countersTable;
  }

  /**
   * Tell if the current template rule is null, i.e. if we are
   * directly within an apply-templates.  Used for xsl:apply-imports.
   *
   * @return True if the current template rule is null.
   */
  public boolean currentTemplateRuleIsNull()
  {
    return ((!m_currentTemplateRuleIsNull.isEmpty())
            && (m_currentTemplateRuleIsNull.peek() == true));
  }

  /**
   * Push true if the current template rule is null, false
   * otherwise.
   *
   * @param b True if the we are executing an xsl:for-each
   * (or xsl:call-template?).
   */
  public void pushCurrentTemplateRuleIsNull(boolean b)
  {
    m_currentTemplateRuleIsNull.push(b);
  }

  /**
   * Push true if the current template rule is null, false
   * otherwise.
   */
  public void popCurrentTemplateRuleIsNull()
  {
    m_currentTemplateRuleIsNull.pop();
  }

  /**
   * Return the message manager.
   *
   * @return The message manager, never null.
   */
  public MsgMgr getMsgMgr()
  {

    if (null == m_msgMgr)
      m_msgMgr = new MsgMgr(this);

    return m_msgMgr;
  }

  /**
   * Set the error event listener.
   *
   * @param listener The new error listener.
   * @throws IllegalArgumentException if
   */
  public void setErrorListener(ErrorListener listener)
          throws IllegalArgumentException
  {

    synchronized (m_reentryGuard)
    {
      if (listener == null)
        throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_NULL_ERROR_HANDLER, null)); //"Null error handler");

      m_errorHandler = listener;
    }
  }

  /**
   * Get the current error event handler.
   *
   * @return The current error handler, which should never be null.
   */
  public ErrorListener getErrorListener()
  {
    return m_errorHandler;
  }

  /**
   * Get an instance of the trace manager for this transformation.
   * This object can be used to set trace listeners on various
   * events during the transformation.
   *
   * @return A reference to the TraceManager, never null.
   */
  public TraceManager getTraceManager()
  {
    return m_traceManager;
  }

  /**
   * Look up the value of a feature.
   *
   * <p>The feature name is any fully-qualified URI.  It is
   * possible for an TransformerFactory to recognize a feature name but
   * to be unable to return its value; this is especially true
   * in the case of an adapter for a SAX1 Parser, which has
   * no way of knowing whether the underlying parser is
   * validating, for example.</p>
   *
   * <h3>Open issues:</h3>
   * <dl>
   *    <dt><h4>Should getFeature be changed to hasFeature?</h4></dt>
   *    <dd>Keith Visco writes: Should getFeature be changed to hasFeature?
   *        It returns a boolean which indicated whether the "state"
   *        of feature is "true or false". I assume this means whether
   *        or not a feature is supported? I know SAX is using "getFeature",
   *        but to me "hasFeature" is cleaner.</dd>
   * </dl>
   *
   * @param name The feature name, which is a fully-qualified
   *        URI.
   * @return The current state of the feature (true or false).
   * @throws org.xml.sax.SAXNotRecognizedException When the
   *            TransformerFactory does not recognize the feature name.
   * @throws org.xml.sax.SAXNotSupportedException When the
   *            TransformerFactory recognizes the feature name but
   *            cannot determine its value at this time.
   *
   * @throws SAXNotRecognizedException
   * @throws SAXNotSupportedException
   */
  public boolean getFeature(String name)
          throws SAXNotRecognizedException, SAXNotSupportedException
  {

    if ("http://xml.org/trax/features/sax/input".equals(name))
      return true;
    else if ("http://xml.org/trax/features/dom/input".equals(name))
      return true;

    throw new SAXNotRecognizedException(name);
  }

  // %TODO% Doc

  /**
   * NEEDSDOC Method getMode 
   *
   *
   * NEEDSDOC (getMode) @return
   */
  public QName getMode()
  {
    return m_modes.isEmpty() ? null : (QName) m_modes.peek();
  }

  // %TODO% Doc

  /**
   * NEEDSDOC Method pushMode 
   *
   *
   * NEEDSDOC @param mode
   */
  public void pushMode(QName mode)
  {
    m_modes.push(mode);
  }

  // %TODO% Doc

  /**
   * NEEDSDOC Method popMode 
   *
   */
  public void popMode()
  {
    m_modes.pop();
  }

  ////////////////////////
  // Implement Runnable //  
  ////////////////////////

  /**
   * Base thread controler for xalan. Must be overriden with
   * a derived class to support thread pooling.
   *
   * All thread-related stuff is in this class.
   * 
   * <p><em>WARNING!</em>  This class will probably move since the DTM 
   * CoroutineSAXParser depends on it.  This class should move 
   * to the CoroutineSAXParser.  You can use it, but be aware 
   * that your code will have to change when the move occurs.</p>
   */
  public static class ThreadControler
  {

    /**
     * Will get a thread from the pool, execute the task
     *  and return the thread to the pool.
     *
     *  The return value is used only to wait for completion
     *
     *
     * NEEDSDOC @param task
     * @param priority if >0 the task will run with the given priority
     *  ( doesn't seem to be used in xalan, since it's allways the default )
     * @returns The thread that is running the task, can be used
     *          to wait for completion
     *
     * NEEDSDOC ($objectName$) @return
     */
    public Thread run(Runnable task, int priority)
    {

      Thread t = new Thread(task);

      t.start();

      //       if( priority > 0 )
      //      t.setPriority( priority );
      return t;
    }

    /**
     *  Wait until the task is completed on the worker
     *  thread.
     *
     * NEEDSDOC @param worker
     * NEEDSDOC @param task
     *
     * @throws InterruptedException
     */
    public void waitThread(Thread worker, Runnable task)
            throws InterruptedException
    {

      // This should wait until the transformThread is considered not alive.
      worker.join();
    }
  }

  /** NEEDSDOC Field tpool          */
  static ThreadControler tpool = new ThreadControler();

  /**
   * Change the ThreadControler that will be used to
   *  manage the transform threads.
   *
   * NEEDSDOC @param tp
   */
  public static void setThreadControler(ThreadControler tp)
  {
    tpool = tp;
  }

  /**
   * Called by SourceTreeHandler to start the transformation
   *  in a separate thread
   *
   * NEEDSDOC @param priority
   */
  public void runTransformThread(int priority)
  {

    // used in SourceTreeHandler
    Thread t = tpool.run(this, priority);

    this.setTransformThread(t);
  }

  /**
   * Called by this.transform() if isParserEventsOnMain()==false.
   *  Similar with runTransformThread(), but no priority is set
   *  and setTransformThread is not set.
   */
  public void runTransformThread()
  {
    tpool.run(this, -1);
  }
  
  /**
   * Called by CoRoutineSAXParser. Launches the CoroutineSAXParser
   * in a thread, and prepares it to invoke the parser from that thread
   * upon request. 
   *  
   */
  public static void runTransformThread(Runnable runnable)
  {
    tpool.run(runnable, -1);
  }

  /**
   * Used by SourceTreeHandler to wait until the transform
   *   completes
   *
   * @throws SAXException
   */
  public void waitTransformThread() throws SAXException
  {

    // This is called to make sure the task is done.
    // It is possible that the thread has been reused -
    // but for a different transformation. ( what if we 
    // recycle the transformer ? Not a problem since this is
    // still in use. )
    Thread transformThread = this.getTransformThread();

    if (null != transformThread)
    {
      try
      {
        tpool.waitThread(transformThread, this);

        if (!this.hasTransformThreadErrorCatcher())
        {
          Exception e = this.getExceptionThrown();

          if (null != e)
          {
            e.printStackTrace();
            throw new org.xml.sax.SAXException(e);
          }
        }

        this.setTransformThread(null);
      }
      catch (InterruptedException ie){}
    }
  }

  /**
   * Get the exception thrown by the secondary thread (normally
   * the transform thread).
   *
   * @return The thrown exception, or null if no exception was
   * thrown.
   */
  public Exception getExceptionThrown()
  {
    return m_exceptionThrown;
  }

  /**
   * Set the exception thrown by the secondary thread (normally
   * the transform thread).
   *
   * @param e The thrown exception, or null if no exception was
   * thrown.
   */
  public void setExceptionThrown(Exception e)
  {
    m_exceptionThrown = e;
  }

  /**
   * This is just a way to set the document for run().
   *
   * @param doc A non-null reference to the root of the
   * tree to be transformed.
   */
  public void setSourceTreeDocForThread(int doc)
  {
    m_doc = doc;
  }

  /**
   * Set the input source for the source tree, which is needed if the
   * parse thread is not the main thread, in order for the parse
   * thread's run method to get to the input source.
   *
   * @param source The input source for the source tree.
   */
  public void setXMLSource(Source source)
  {
    m_xmlSource = source;
  }

  /**
   * Tell if the transform method is completed.
   *
   * @return True if transformNode has completed, or
   * an exception was thrown.
   */
  public boolean isTransformDone()
  {

    synchronized (this)
    {
      return m_isTransformDone;
    }
  }

  /**
   * Set if the transform method is completed.
   *
   * @param done True if transformNode has completed, or
   * an exception was thrown.
   */
  public void setIsTransformDone(boolean done)
  {

    synchronized (this)
    {
      m_isTransformDone = done;
    }
  }

  /**
   * From a secondary thread, post the exception, so that
   * it can be picked up from the main thread.
   *
   * @param e The exception that was thrown.
   */
  void postExceptionFromThread(Exception e)
  {

    // Commented out in response to problem reported by Nicola Brown <Nicola.Brown@jacobsrimell.com>
    //    if(m_reportInPostExceptionFromThread)
    //    {
    //      // Consider re-throwing the exception if this flag is set.
    //      e.printStackTrace();
    //    }
    // %REVIEW Need DTM equivelent?    
    //    if (m_inputContentHandler instanceof SourceTreeHandler)
    //    {
    //      SourceTreeHandler sth = (SourceTreeHandler) m_inputContentHandler;
    //
    //      sth.setExceptionThrown(e);
    //    }
    ContentHandler ch = getContentHandler();

    //    if(ch instanceof SourceTreeHandler)
    //    {
    //      SourceTreeHandler sth = (SourceTreeHandler) ch;
    //      ((TransformerImpl)(sth.getTransformer())).postExceptionFromThread(e);
    //    }
    m_isTransformDone = true;
    m_exceptionThrown = e;
    ;  // should have already been reported via the error handler?

    synchronized (this)
    {

      // See message from me on 3/27/2001 to Patrick Moore.
      //      String msg = e.getMessage();
      // System.out.println(e.getMessage());
      // Is this really needed?  -sb
      notifyAll();

      //      if (null == msg)
      //      {
      //
      //        // m_throwNewError = false;
      //        e.printStackTrace();
      //      }
      // throw new org.apache.xml.utils.WrappedRuntimeException(e);
    }
  }

  /**
   * Run the transform thread.
   */
  public void run()
  {

    m_hasBeenReset = false;

    try
    {

      // int n = ((SourceTreeHandler)getInputContentHandler()).getDTMRoot();
      // transformNode(n);
      try
      {
        m_isTransformDone = false;
        
        // Should no longer be needed...
//          if(m_inputContentHandler instanceof TransformerHandlerImpl)
//          {
//            TransformerHandlerImpl thi = (TransformerHandlerImpl)m_inputContentHandler;
//            thi.waitForInitialEvents();
//          }

        transformNode(m_doc);
        
      }
      catch (Exception e)
      {
        // e.printStackTrace();

        // Strange that the other catch won't catch this...
        if (null != m_transformThread)
          postExceptionFromThread(e);   // Assume we're on the main thread
        else 
          throw new RuntimeException(e.getMessage());
      }
      finally
      {
        m_isTransformDone = true;

        if (m_inputContentHandler instanceof TransformerHandlerImpl)
        {
          ((TransformerHandlerImpl) m_inputContentHandler).clearCoRoutine();
        }

        //        synchronized (this)
        //        {
        //          notifyAll();
        //        }
      }
    }
    catch (Exception e)
    {

      // e.printStackTrace();
      if (null != m_transformThread)
        postExceptionFromThread(e);
      else 
        throw new RuntimeException(e.getMessage());         // Assume we're on the main thread.
    }
  }

  // Fragment re-execution interfaces for a tool.

  /**
   * This will get a snapshot of the current executing context 
   *
   *
   * @return TransformerSnapshot object, snapshot of executing context
   */
  public TransformSnapshot getSnapshot()
  {
    return new TransformSnapshotImpl(this);
  }

  /**
   * This will execute the following XSLT instructions
   * from the snapshot point, after the stylesheet execution
   * context has been reset from the snapshot point. 
   *
   * @param ts The snapshot of where to start execution
   *
   * @throws TransformerException
   */
  public void executeFromSnapshot(TransformSnapshot ts)
          throws TransformerException
  {

    ElemTemplateElement template = getMatchedTemplate();
    int child = getMatchedNode();

    pushElemTemplateElement(template);  //needed??
    m_xcontext.pushCurrentNode(child);  //needed??
    this.executeChildTemplates(template, true);  // getResultTreeHandler());
  }

  /**
   * This will reset the stylesheet execution context
   * from the snapshot point.
   *
   * @param ts The snapshot of where to start execution
   */
  public void resetToStylesheet(TransformSnapshot ts)
  {
    ((TransformSnapshotImpl) ts).apply(this);
  }

  /**
   * NEEDSDOC Method stopTransformation 
   *
   */
  public void stopTransformation(){}

  /**
   * Test whether whitespace-only text nodes are visible in the logical
   * view of <code>DTM</code>. Normally, this function
   * will be called by the implementation of <code>DTM</code>;
   * it is not normally called directly from
   * user code.
   *
   * @param elementHandle int Handle of the element.
   * @return one of NOTSTRIP, STRIP, or INHERIT.
   */
  public short getShouldStripSpace(int elementHandle, DTM dtm)
  {

    try
    {
      org.apache.xalan.templates.WhiteSpaceInfo info =
        m_stylesheetRoot.getWhiteSpaceInfo(m_xcontext, elementHandle, dtm);

      if (null == info)
      {
        return DTMWSFilter.INHERIT;
      }
      else
      {

        // System.out.println("getShouldStripSpace: "+info.getShouldStripSpace());
        return info.getShouldStripSpace()
               ? DTMWSFilter.STRIP : DTMWSFilter.NOTSTRIP;
      }
    }
    catch (TransformerException se)
    {
      return DTMWSFilter.INHERIT;
    }
  }
}  // end TransformerImpl class

