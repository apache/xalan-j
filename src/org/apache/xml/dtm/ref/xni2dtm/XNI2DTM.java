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
package org.apache.xml.dtm.ref.xni2dtm;

import javax.xml.transform.Source;

import org.apache.xerces.impl.xs.AttributePSVImpl;
import org.apache.xerces.impl.xs.ElementPSVImpl;
import org.apache.xerces.impl.xs.psvi.XSSimpleTypeDefinition;
import org.apache.xerces.impl.xs.psvi.XSTypeDefinition;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLDTDHandler;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLParseException;
import org.apache.xerces.xni.parser.XMLPullParserConfiguration;
import org.apache.xerces.xni.psvi.ItemPSVI;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMManager;
import org.apache.xml.dtm.DTMSequence;
import org.apache.xml.dtm.DTMWSFilter;
import org.apache.xml.utils.SparseVector;
import org.apache.xml.utils.WrappedRuntimeException;
import org.apache.xpath.objects.XSequence;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This class implements a DTM that is constructed via an XNI data stream.
 * 
 * Please note that it this is a PROTOTYPE, since the Xerces post-schema 
 * validation infoset (PSVI) APIs it is using are themselves prototypes and
 * subject to change without warning. The most recent such change was from
 * "lightweight" to "heavyweight" schema models -- the former are no longer
 * supported.
 * 
 * This version is derived from SAX2DTM for ease of implementation and
 * support, but deliberately blocks external access to
 * the SAX-listener calls to make our testing a bit more rigorous.
 * 
 * Note to developers: Both XNI and Xalan have classes called XMLString.
 * Don't confuse them!
 * 
 * %REVIEW% Should we re-unify this with SAX2(RTF)DTM?
 * 	(Since we'll want to create typed temporary trees, definitely an issue...)
 *  To actually _USE_ as a temporary tree, we'd need to accept SAX
 *     rather than blocking it, and/or rewrite Xalan core to be XNI-based,
 *     and/or do some bypassing to add types to SAX stream.
 *  Test confirms we can successfully derive from either.
 */
public class XNI2DTM 
  extends org.apache.xml.dtm.ref.sax2dtm.SAX2DTM
  implements XMLDocumentHandler, XMLErrorHandler, XMLDTDHandler
{
  /** DEBUGGING FLAG: Set true to monitor XNI events and similar diagnostic info. */
  private static final boolean DEBUG = false;  
  
  /** %OPT% %REVIEW% PROTOTYPE: Schema Type information, datatype as instantiated.
   * See discussion in addNode */
  protected SparseVector m_schemaTypeOverride=new SparseVector();
  
  /**
   * If we're building the model incrementally on demand, we need to
   * be able to tell the source when to send us more data.
   *
   * Note that if this has not been set, and you attempt to read ahead
   * of the current build point, we'll probably throw a null-pointer
   * exception. We could try to wait-and-retry instead, as a very poor
   * fallback, but that has all the known problems with multithreading
   * on multiprocessors and we Don't Want to Go There.
   *
   * @see setIncrementalXNISource
   */
  private XMLPullParserConfiguration m_incrementalXNISource = null;

  /** The XNI Document locator 
   * %REVIEW% Should we be storing a SAX locator instead? */
  transient private LocatorWrapper m_locator_wrapper = new LocatorWrapper();
  
  /** Manefest constant: Schema namespace string */
  private static final String SCHEMANS="http://www.w3.org/2001/XMLSchema";
  
  /** Manefest constant: Message for SAX-disablement stubs */
  static final String UNEXPECTED="XNI2DTM UNEXPECTED SAX ";

  /**
   * Construct a XNI2DTM object ready to be constructed from XNI
   * ContentHandler events.
   *
   * @param mgr The DTMManager who owns this DTM.
   * @param source the JAXP 1.1 Source object for this DTM.
   * @param dtmIdentity The DTM identity ID for this DTM.
   * @param whiteSpaceFilter The white space filter for this DTM, which may
   *                         be null.
   * @param xstringfactory XMLString factory for creating character content.
   * @param doIndexing true if the caller considers it worth it to use 
   *                   indexing schemes.
   */
  public XNI2DTM(DTMManager mgr, Source source, int dtmIdentity,
                 DTMWSFilter whiteSpaceFilter,
                 org.apache.xml.utils.XMLStringFactory xstringfactory,
                 boolean doIndexing)
  {
    super(mgr, source, dtmIdentity, whiteSpaceFilter, 
          xstringfactory, doIndexing);
  }

  
  /** ADDED for XNI, SUPPLEMENTS non-schema-typed addNode:
   * 
   * Construct the node map from the node. EXTENDED to carry PSVI type data
   * delivered via XNI. This is currently using non-published Xerces APIs, which
   * are subject to change as their PSVI support becomes more official.
   *
   * @param type raw type ID, one of DTM.XXX_NODE.
   * @param expandedTypeID The expended type ID.
   * @param parentIndex The current parent index.
   * @param previousSibling The previous sibling index.
   * @param dataOrPrefix index into m_data table, or string handle.
   * @param canHaveFirstChild true if the node can have a first child, false
   *                          if it is atomic.
   * @param actualType Schema type object as resolved in actual instance document
   *
   * @return The index identity of the node that was added.
   */
  protected int addNode(int type, int expandedTypeID,
                        int parentIndex, int previousSibling,
                        int dataOrPrefix, boolean canHaveFirstChild,
                        XPath2Type actualType)
  {
    int identity=super.addNode(type,expandedTypeID,
                               parentIndex,previousSibling,
                               dataOrPrefix,canHaveFirstChild);

    // The goal is to not consume storage for types unless they actualy exist,
    // and to minimize per-node overhead.
    //
    // NOTE: Record first-seen as default even if it is null, because
    // otherwise late changes of type will bash previously recorded
    // nodes. This is NOT necessarily maximally efficient, but to really
    // optimize we would have to rewrite data to make the default the most
    // common -- and since Scott insists that overrides will be uncommon,
    // I don't want to go there.
    //
    // NOTE: Element schema-types aren't fully resolved until endElement, and
    // need to be dealt with there.
    if(type!=ELEMENT_NODE)
    {
      // Try to record as default for this nodetype
      if(!m_expandedNameTable.setSchemaType(m_exptype.elementAt(identity),
                                            actualType)
         )
      {
        m_schemaTypeOverride.setElementAt(actualType,identity);
      }
    }

    return identity;
  }

  /** ADDED FOR XPATH2: Query schema type name of a given node.
   * 
   * %REVIEW% Is this actually needed?
   * 
   * @param nodeHandle DTM Node Handle of Node to be queried
   * @return null if no type known, else returns the expanded-QName (namespace URI
   *    rather than prefix) of the type actually
   *    resolved in the instance document. Note that this may be derived from,
   *    rather than identical to, the type declared in the schema.
   */
  public String getSchemaTypeName(int nodeHandle)
  {
    int identity=makeNodeIdentity(nodeHandle);
        
    if(identity!=DTM.NULL)
    {
      XPath2Type actualType=(XPath2Type)m_schemaTypeOverride.elementAt(identity);
      if(actualType==null)
        actualType=(XPath2Type)m_expandedNameTable.getSchemaType(m_exptype.elementAt(identity));

      if(actualType!=null)
      {
        String nsuri=actualType.getTargetNamespace();
        String local=actualType.getTypeName();
        return (nsuri==null)
          ? local
          : nsuri+":"+local;
      }
    }
        
    return null;
  }
        
  /** ADDED FOR XPATH2: Query schema type namespace of a given node.
   * 
   * %REVIEW% Is this actually needed?
   * 
   * @param nodeHandle DTM Node Handle of Node to be queried
   * @return null if no type known, else returns the namespace URI
   *    of the type actually resolved in the instance document. This may
   *    be null if the default/unspecified namespace was used.
   *    Note that this may be derived from,
   *    rather than identical to, the type declared in the schema.
   */
  public String getSchemaTypeNamespace(int nodeHandle)
  {
    int identity=makeNodeIdentity(nodeHandle);
        
    if(identity!=DTM.NULL)
    {
      XPath2Type actualType=(XPath2Type)m_schemaTypeOverride.elementAt(identity);
      if(actualType==null)
        actualType=(XPath2Type)m_expandedNameTable.getSchemaType(m_exptype.elementAt(identity));
      if(actualType!=null)
      {
        return actualType.getTargetNamespace();
      }
    }
        
    return null;
  }

  /** ADDED FOR XPATH2: Query schema type localname of a given node.
   * 
   * %REVIEW% Is this actually needed?
   * 
   * @param nodeHandle DTM Node Handle of Node to be queried
   * @return null if no type known, else returns the localname of the type
   *    resolved in the instance document. Note that this may be derived from,
   *    rather than identical to, the type declared in the schema.
   */
  public String getSchemaTypeLocalName(int nodeHandle)
  {
    int identity=makeNodeIdentity(nodeHandle);
        
    if(identity!=DTM.NULL)
    {
      XPath2Type actualType=(XPath2Type)m_schemaTypeOverride.elementAt(identity);
      if(actualType==null)
        actualType=(XPath2Type)m_expandedNameTable.getSchemaType(m_exptype.elementAt(identity));
      if(actualType!=null)
      {
        return actualType.getTypeName();
      }
    }
        
    return null;
  }

  /** ADDED FOR XPATH2: Query whether node's type is derived from a specific type
   * 
   * @param nodeHandle DTM Node Handle of Node to be queried
   * @param namespace String containing URI of namespace for the type we're intersted in
   * @param localname String containing local name for the type we're intersted in
   * @return true if node has a Schema Type which equals or is derived from 
   *    the specified type. False if the node has no type or that type is not
   *    derived from the specified type.
   */
  public boolean isNodeSchemaType(int nodeHandle, String namespace, String localname)
  {
    int identity=makeNodeIdentity(nodeHandle);
        
    if(identity!=DTM.NULL)
    {
      XPath2Type actualType=(XPath2Type)m_schemaTypeOverride.elementAt(identity);
      if(actualType==null)
        actualType=(XPath2Type)m_expandedNameTable.getSchemaType(m_exptype.elementAt(identity));
      if(actualType!=null)
        return actualType.derivedFrom(namespace,localname);
    }
        
    return false;
  }
  
  /** ADDED FOR XPATH2: Retrieve the typed value(s), based on the schema type
   * 
   * %REVIEW% That may not be 
   * */
  public DTMSequence getTypedValue(int nodeHandle)
  {
    // Determine whether instance of built-in type, or list thereof
    // If so, map to corresponding Java type
    // Retrieve string content (as always, for element this spans children
    // If type was xs:string (or untyped?) just return that in a collection.
    // Else parse into collection object, return that
         
    int identity=makeNodeIdentity(nodeHandle);
    if(identity==DTM.NULL)
      return DTMSequence.EMPTY;
                
    XPath2Type actualType=(XPath2Type)m_schemaTypeOverride.elementAt(identity);
    if(actualType==null)
      actualType=(XPath2Type)m_expandedNameTable.getSchemaType(m_exptype.elementAt(identity));

    if(actualType==null)
      return DTMSequence.EMPTY;
                
        /* %REVIEW% Efficiency issues; value may be in FSB or scattered,
        	 in which case generating a Java String may arguably be wasteful. 
        	 And are we handling list types at all reasonably?
        	 */
        //GONK lists;
        //GONK efficiency;
        
    // Gathers all text. Is that right? Should we not do if type is not known?
    String textvalue=getStringValue(nodeHandle).toString();
    
    // DTM node should provide the namespace context.
    // Do we have an existing encapulation for that concept?
        
    return actualType.typedValue(textvalue, 
    	new org.apache.xml.dtm.ref.xni2dtm.NamespaceSupportAtDTMNode(this,nodeHandle));
  }
  
  
  //===========================================================================

  /** OVERRIDDEN FOR XNI:
   * 
   * Ask the CoRoutine parser to terminate and clear the reference. If
   * the parser has already been cleared, this will have no effect.
   *
   * @param callDoTerminate true if parsing should be terminated
   */
  public void clearCoRoutine(boolean callDoTerminate)
  {

    if (null != m_incrementalXNISource)
    {
      if (callDoTerminate)
        m_incrementalXNISource.cleanup();

      m_incrementalXNISource = null;
    }
  }

  /** ADDED FOR XNI, REPLACES setIncrementalSAXSource:
   * 
   * Bind a IncrementalXNISource to this DTM. If we discover we need nodes
   * that have not yet been built, we will ask this object to send us more
   * events, and it will manage interactions with its data sources.
   *
   * Note that we do not actually build the IncrementalXNISource, since we don't
   * know what source it's reading from, what thread that source will run in,
   * or when it will run.
   *
   * @param incrementalXNISource The parser that we want to recieve events from
   * on demand.
   * @param appCoRID The CoRoutine ID for the application.
   */
  public void setIncrementalXNISource(XMLPullParserConfiguration incrementalXNISource)
  {

    // Establish coroutine link so we can request more data
    //
    // Note: It's possible that some versions of IncrementalXNISource may
    // not actually use a CoroutineManager, and hence may not require
    // that we obtain an Application Coroutine ID. (This relies on the
    // coroutine transaction details having been encapsulated in the
    // IncrementalXNISource.do...() methods.)
    m_incrementalXNISource = incrementalXNISource;

    // Establish XNI-stream link so we can receive the requested data
    incrementalXNISource.setDocumentHandler(this);

    // Are the following really needed? incrementalXNISource doesn't yet
    // support them, and they're mostly no-ops here...
    incrementalXNISource.setErrorHandler(this);
    incrementalXNISource.setDTDHandler(this);
  }

  /** DISABLED FOR XNI:
   * 
   * getContentHandler returns "our SAX builder" -- the thing that
   * someone else should send SAX events to in order to extend this
   * DTM model.
   * 
   * %REVIEW% We _could_ leave SAX support in place and have a single model
   * that handles both kinds of event streams. I've chosen to block it
   * during development for better isolation of XNI support.
   *
   * @return null since this model doesn't respond to SAX events
   */
  public org.xml.sax.ContentHandler getContentHandler()
  {
    return null;
  }

  /**
   * Return this DTM's lexical handler.
   *
   * %REVIEW% Should this return null if constrution already done/begun?
   * 
   * %REVIEW% We _could_ leave SAX support in place and have a single model
   * that handles both kinds of event streams. I've chosen to block it
   * during development for better isolation of XNI support.
   *
   * @return null since this model doesn't respond to SAX events
   */
  public org.xml.sax.ext.LexicalHandler getLexicalHandler()
  {
    return null;
  }

  /**
   * Return this DTM's DTDHandler.
   * 
   * %REVIEW% We _could_ leave SAX support in place and have a single model
   * that handles both kinds of event streams. I've chosen to block it
   * during development for better isolation of XNI support.
   *
   * @return null since this model doesn't respond to SAX events
   */
  public org.xml.sax.DTDHandler getDTDHandler()
  {
    return null;
  }

  /**
   * Return this DTM's ErrorHandler.
   *
   * @return null if this model doesn't respond to SAX error events.
   */
  public org.xml.sax.ErrorHandler getErrorHandler()
  {
    //return this;
    return null;
  }

  /**
   * Return this DTM's DeclHandler.
   *
   * @return null if this model doesn't respond to SAX Decl events.
   */
  public org.xml.sax.ext.DeclHandler getDeclHandler()
  {
    //return this;
    return null;
  }

  /** OVERRIDDEN FOR XNI:
   * 
   * @return true iff we're building this model incrementally (eg
   * we're partnered with a IncrementalXNISource) and thus require that the
   * transformation and the parse run simultaneously. Guidance to the
   * DTMManager.
   */
  public boolean needsTwoThreads()
  {
    return null != m_incrementalXNISource;
  }

  /** OVERRIDDEN FOR XNI:
   * 
   * This method should try and build one or more nodes in the table.
   * 
   * %REVIEW% Is it worth factoring out the actual request for more events
   * into a subroutine, isolating it better? Consider if/when we re-merge
   * with SAX2DTM.
   *
   * @return The true if a next node is found or false if
   *         there are no more nodes.
   */
  protected boolean nextNode()
  {

    if (null == m_incrementalXNISource)
      return false;

    if (m_endDocumentOccured)
    {
      clearCoRoutine();

      return false;
    }

    try
    {
      boolean gotMore = m_incrementalXNISource.parse(false);
      if (!gotMore)
      {
        // EOF reached without satisfying the request
        clearCoRoutine();  // Drop connection, stop trying
        // %TBD% deregister as its listener?
      }
      return gotMore;
    }
    catch(RuntimeException e)
    {
      throw e;
    }
    catch(Exception e)
    {
      throw new WrappedRuntimeException(e);
    }

    // %REVIEW% dead code
    //clearCoRoutine();
    //return false;
  }
  
  /** %REVIEW% XNI should let us support this better...
   * 
   * Return the public identifier of the external subset,
   * normalized as described in 4.2.2 External Entities [XML]. If there is
   * no external subset or if it has no public identifier, this property
   * has no value.
   *
   * @param the document type declaration handle
   *
   * @return the public identifier String object, or null if there is none.
   */
  public String getDocumentTypeDeclarationPublicIdentifier()
  {
    return super.getDocumentTypeDeclarationPublicIdentifier();
  }



  ////////////////////////////////////////////////////////////////////
  // Implementation of XNI XMLDocumentHandler interface.
  //
  // %REVIEW% Hand off the SAX2DTM SAX event code, or copy/adapt method
  // bodies? Performance may favor latter approach, but former is no worse
  // than what's been happening on the parser's side of the fence and
  // simplifies maintainance.
  ////////////////////////////////////////////////////////////////////

  /**
   * Receive notification of a notation declaration.
   *
   * <p>By default, do nothing.  Application writers may override this
   * method in a subclass if they wish to keep track of the notations
   * declared in a document.</p>
   *
   * @param name The notation name.
   * @param publicId The notation public identifier, or null if not
   *                 available.
   * @param systemId The notation system identifier.
   * @throws XNIException Any XNI exception, possibly
   *            wrapping another exception.
   * @see org.xml.sax.DTDHandler#notationDecl
   *
   * @throws XNIException
   */
  public void notationDecl(String name, String publicId, String systemId)
    throws XNIException
  {
    // no op
  }

  /**
   * Receive notification of an unparsed entity declaration.
   *
   * <p>By default, do nothing.  Application writers may override this
   * method in a subclass to keep track of the unparsed entities
   * declared in a document.</p>
   *
   * @param name The entity name.
   * @param publicId The entity public identifier, or null if not
   *                 available.
   * @param systemId The entity system identifier.
   * @param notationName The name of the associated notation.
   * @throws XNIException Any XNI exception, possibly
   *            wrapping another exception.
   * @see org.xml.sax.DTDHandler#unparsedEntityDecl
   *
   * @throws XNIException
   */
  public void unparsedEntityDecl(
                                 String name, String publicId, String systemId, String notationName)
    throws XNIException
  {
    try
    {
      super.unparsedEntityDecl(name,publicId,systemId,notationName);
    } 
    catch(SAXException e)
    {
      throw new XNIException(e);
    }
  }

  /**
   * Receive a Locator object for document events.
   *
   * <p>By default, do nothing.  Application writers may override this
   * method in a subclass if they wish to store the locator for use
   * with other document events.</p>
   *
   * @param locator A locator for all XNI document events.
   * @see org.xml.sax.ContentHandler#setDocumentLocator
   * @see org.xml.sax.Locator
   */
  public void setDocumentLocator(XMLLocator locator)
  {
    m_locator_wrapper.setLocator(locator);
    super.setDocumentLocator(m_locator_wrapper);
  }

  /**
   * Receive notification of the beginning of the document.
   *
   * @throws XNIException Any XNI exception, possibly
   *            wrapping another exception.
   * @see org.xml.sax.ContentHandler#startDocument
   */
  public void startDocument(XMLLocator locator,String encoding,Augmentations augs)
    throws XNIException
  {
    if (DEBUG)
      System.out.println("startDocument");
 
    try
    {    
      super.startDocument();
    } 
    catch(SAXException e)
    {
      throw new XNIException(e);
    }
  }

  /**
   * Receive notification of the end of the document.
   *
   * @throws XNIException Any XNI exception, possibly
   *            wrapping another exception.
   * @see org.xml.sax.ContentHandler#endDocument
   */
  public void endDocument(Augmentations augs) throws XNIException
  {
    if (DEBUG)
      System.out.println("endDocument");
      
    try
    {
      super.endDocument();
    } 
    catch(SAXException e)
    {
      throw new XNIException(e);
    }
  }

  /**
   * Receive notification of the start of a Namespace mapping.
   *
   * <p>By default, do nothing.  Application writers may override this
   * method in a subclass to take specific actions at the start of
   * each Namespace prefix scope (such as storing the prefix mapping).</p>
   *
   * @param prefix The Namespace prefix being declared.
   * @param uri The Namespace URI mapped to the prefix.
   * @throws XNIException Any XNI exception, possibly
   *            wrapping another exception.
   * @see org.xml.sax.ContentHandler#startPrefixMapping
   */
  public void startPrefixMapping(String prefix,String uri,Augmentations augs)
    throws XNIException
  {
    if (DEBUG)
      System.out.println("startPrefixMapping: prefix: " + prefix + ", uri: "
                         + uri);
    try
    {                        
      if(augs!=null &&
         null!=augs.getItem(DTM2XNI.DTM2XNI_ADDED_STRUCTURE))
      {
        if (DEBUG)
          System.out.println("\t***** Added by DTM2XNI; ignored here");
                  
        return; // Ignore it!
      }
                
                  
      super.startPrefixMapping(prefix,uri);
    } 
    catch(SAXException e)
    {
      throw new XNIException(e);
    }
  }

  /**
   * Receive notification of the end of a Namespace mapping.
   *
   * <p>By default, do nothing.  Application writers may override this
   * method in a subclass to take specific actions at the end of
   * each prefix mapping.</p>
   *
   * @param prefix The Namespace prefix being declared.
   * @throws XNIException Any XNI exception, possibly
   *            wrapping another exception.
   * @see org.xml.sax.ContentHandler#endPrefixMapping
   */
  public void endPrefixMapping(String prefix, Augmentations augs) 
    throws XNIException
  {
    if (DEBUG)
      System.out.println("endPrefixMapping: prefix: " + prefix);

    try
    {
      if(augs!=null &&
         null!=augs.getItem(DTM2XNI.DTM2XNI_ADDED_STRUCTURE))
      {
        if (DEBUG)
          System.out.println("\t***** Added by DTM2XNI; ignored here");
                  
        return; // Ignore it!
      }
                
      super.endPrefixMapping(prefix);
    } 
    catch(SAXException e)
    {
      throw new XNIException(e);
    }
  }

  /**
   * Receive notification of the start of an element.
   *
   * <p>By default, do nothing.  Application writers may override this
   * method in a subclass to take specific actions at the start of
   * each element (such as allocating a new tree node or writing
   * output to a file).</p>
   *
   * @param name The element type name.
   *
   * @param uri The Namespace URI, or the empty string if the
   *        element has no Namespace URI or if Namespace
   *        processing is not being performed.
   * @param localName The local name (without prefix), or the
   *        empty string if Namespace processing is not being
   *        performed.
   * @param qName The qualified name (with prefix), or the
   *        empty string if qualified names are not available.
   * @param attributes The specified or defaulted attributes.
   * @throws XNIException Any XNI exception, possibly
   *            wrapping another exception.
   * @see org.xml.sax.ContentHandler#startElement
   */
  public void startElement(QName element, XMLAttributes attributes,
                           Augmentations augs)
    //String uri, String localName, String qName, Attributes attributes)
    throws XNIException
  {
    // %REVIEW% I've copied this verbatim and altered it for XNI.
    // Is that overkill? Can we hand any of it back to the SAX layer?
    // (I suspect not, darn it....)
    
    if (DEBUG)
    {
      System.out.println("startElement: uri: " + element.uri + 
                         ", localname: " + element.localpart + 
                         ", qname: "+element.rawname+", atts: " + attributes);    
    }
      
    boolean syntheticElement=false;
        
    // Augs might be null if schema support not turned on in parser. 
    // Shouldn't arise in final operation (?); may arise during debugging
    ElementPSVImpl elemPSVI=null;
    XSTypeDefinition actualType =null;
    XPath2Type xp2type=null;
    if(augs!=null)
    {
      // Node added by DTM2XNI?
      syntheticElement = null!=augs.getItem(DTM2XNI.DTM2XNI_ADDED_STRUCTURE);
        
      // Extract Experimental Xerces PSVI data          
      elemPSVI=(ElementPSVImpl)augs.getItem(org.apache.xerces.impl.Constants.ELEMENT_PSVI);
      xp2type=new XPath2Type(elemPSVI,false);
      actualType =
        (elemPSVI==null) ? null : elemPSVI.getTypeDefinition();
      if (DEBUG)
      {
        String actualExpandedQName=xp2type.getTargetNamespace()+":"+xp2type.getTypeName();

        System.out.println("\ttypeDefinition (actual): "+ actualType +
                           "\n\t\ttype expanded-qname: " + actualExpandedQName +
//                           "\n\tDerived from builtin string: " + actualType.derivedFrom(SCHEMANS,"string") +
                           "\n\tSynthesized by DTM2XNI: "+syntheticElement
                           );
      } //DEBUG
    }// augs
    
    if(DEBUG && attributes!=null)  
    {
      int n = attributes.getLength();
      if(n==0)
        System.out.println("\tempty attribute list");
      else for (int i = 0; i < n; i++)
      {
        System.out.println("\t attr: uri: " + attributes.getURI(i) +
                           ", localname: " + attributes.getLocalName(i) +
                           ", qname: " + attributes.getQName(i) +
                           ", type: " + attributes.getType(i) +
                           ", value: " + attributes.getValue(i)                                                                                                          
                           );
        // Experimental Xerces PSVI data
        Augmentations attrAugs=attributes.getAugmentations(i);
        AttributePSVImpl attrPSVI=(AttributePSVImpl)attrAugs.getItem(org.apache.xerces.impl.Constants.ATTRIBUTE_PSVI);
        XPath2Type xp2attrtype=new XPath2Type(attrPSVI,true);
        XSTypeDefinition actualAttrType=(attrPSVI==null) ? null : attrPSVI.getTypeDefinition();
        String actualExpandedQName=(actualAttrType==null) ? null : actualAttrType.getNamespace()+":"+actualAttrType.getName();
        // Node added by DTM2XNI?
        boolean syntheticAttribute = null!=attrAugs.getItem(DTM2XNI.DTM2XNI_ADDED_STRUCTURE);

        actualExpandedQName=xp2attrtype.getTargetNamespace()+
        	":"+ xp2attrtype.getTypeName();
        
        System.out.println("\t\ttypeDefinition (actual): "+ actualAttrType +
                           "\n\t\t\ttype expanded-qname: " + actualExpandedQName
                           );
        if(actualAttrType!=null)                                                
          System.out.println(
                             "\n\t\tSynthesized by DTM2XNI: "+syntheticAttribute
                             );
      } // dump all attrs
    } // DEBUG

    
    if(syntheticElement)
    {
      if (DEBUG)
        System.out.println("\t***** Added by DTM2XNI; ignored here");
      
      return; // Ignore it!
    }
    
    charactersFlush();


    int exName = m_expandedNameTable.getExpandedTypeID(element.uri, element.localpart, DTM.ELEMENT_NODE);
    String prefix = getPrefix(element.rawname, element.uri);
    int prefixIndex = (null != element.prefix)
      ? m_valuesOrPrefixes.stringToIndex(element.rawname) : 0;
    int elemNode = addNode(DTM.ELEMENT_NODE, exName,
                           m_parents.peek(), m_previous, prefixIndex, true,
                           xp2type);

    if(m_indexing)
      indexNode(exName, elemNode);
    
    m_parents.push(elemNode);

    int startDecls = m_contextIndexes.peek();
    int nDecls = m_prefixMappings.size();
    int prev = DTM.NULL;

    if(!m_pastFirstElement)
    {
      // SPECIAL CASE: Implied declaration at root element
      prefix="xml";
      String declURL = "http://www.w3.org/XML/1998/namespace";
      exName = m_expandedNameTable.getExpandedTypeID(null, prefix, DTM.NAMESPACE_NODE);
      int val = m_valuesOrPrefixes.stringToIndex(declURL);
      // %REVIEW% I don't _think_ we need datatype on namespaces...?
      prev = addNode(DTM.NAMESPACE_NODE, exName, elemNode,
                     prev, val, false);
      m_pastFirstElement=true;
    }
                        
    for (int i = startDecls; i < nDecls; i += 2)
    {
      prefix = (String) m_prefixMappings.elementAt(i);

      if (prefix == null)
        continue;

      String declURL = (String) m_prefixMappings.elementAt(i + 1);

      exName = m_expandedNameTable.getExpandedTypeID(null, prefix, DTM.NAMESPACE_NODE);

      int val = m_valuesOrPrefixes.stringToIndex(declURL);

      // %REVIEW% I don't _think_ we need datatype on namespaces...?
      prev = addNode(DTM.NAMESPACE_NODE, exName, elemNode,
                     prev, val, false);
    }

    int n = attributes.getLength();

    for (int i = 0; i < n; i++)
    {
      String attrUri = attributes.getURI(i);
      
      
      
      String attrQName = attributes.getQName(i);
      String valString = attributes.getValue(i);

      prefix = getPrefix(attrQName, attrUri);

      int nodeType;

      if ((null != attrQName)
          && (attrQName.equals("xmlns")
              || attrQName.startsWith("xmlns:")))
      {
        if (declAlreadyDeclared(prefix))
          continue;  // go to the next attribute.

        nodeType = DTM.NAMESPACE_NODE;
      } // NS special-handling
      else
      {
        nodeType = DTM.ATTRIBUTE_NODE;

        if (attributes.getType(i).equalsIgnoreCase("ID"))
          setIDAttribute(valString, elemNode);
      } // Attr/ID special handling
      
      
      // Bit of a hack... if somehow valString is null, stringToIndex will 
      // return -1, which will make things very unhappy.
      if(null == valString)
        valString = "";

      int val = m_valuesOrPrefixes.stringToIndex(valString);
      String attrLocalName = attributes.getLocalName(i);

      if (null != prefix)
      {
        
        prefixIndex = m_valuesOrPrefixes.stringToIndex(attrQName);

        int dataIndex = m_data.size();

        m_data.addElement(prefixIndex);
        m_data.addElement(val);

        val = -dataIndex;
      } // Prefix handling

      exName = m_expandedNameTable.getExpandedTypeID(attrUri, attrLocalName, nodeType);

      // Experimental Xerces PSVI data
      Augmentations attrAugs=attributes.getAugmentations(i);
      boolean syntheticAttribute= null != attrAugs.getItem(DTM2XNI.DTM2XNI_ADDED_STRUCTURE);
      if(syntheticAttribute)
      {
        if (DEBUG)
          System.out.println("\t***** Attr {"+attrUri+"}"+attrLocalName+" added by DTM2XNI; ignored here");
        return; // Ignore it!
      } // Synthetic suppression
      
      AttributePSVImpl attrPSVI=(AttributePSVImpl)attrAugs.getItem(org.apache.xerces.impl.Constants.ATTRIBUTE_PSVI);
      XPath2Type xp2attrtype=new XPath2Type(attrPSVI,true);
      
      prev = addNode(nodeType, exName, elemNode, prev, val,
                     false, xp2attrtype);
    } // Attribute list loop
    
    
    if(DTM2XNI.SUPPRESS_XSI_ATTRIBUTES)
    {
     	// Were any XNI attributes passed _around_ the validator,
     	// using element augmentations? If so, magic them back
     	// into being attributes.
     	//
     	// See comments in DTM2XNI. Is this trip REALLY necessary?)
      	java.util.Vector v=(java.util.Vector)augs.getItem(DTM2XNI.SUPPRESSED_XSI);
     	if(v!=null)
     	{
     		for(int i=v.size()-1;i>=0;--i)
     		{
     			Object[] ary=(Object[])v.elementAt(i); 	// {QName,"CDATA",value}
     			QName aqq=(QName) ary[0];
     			String valString=(String) ary[2]; 
				if(null == valString)
					valString = "";
				int val = m_valuesOrPrefixes.stringToIndex(valString);
		        exName = m_expandedNameTable.getExpandedTypeID(
		        	aqq.uri,aqq.localpart, DTM.ATTRIBUTE_NODE);

     			prev=addNode(DTM.ATTRIBUTE_NODE,exName,elemNode,prev,val,
     						false,null);
     		}
     	}
     	
    }

    if (DTM.NULL != prev)
      m_nextsib.setElementAt(DTM.NULL,prev);

    if (null != m_wsfilter)
    {
      short wsv = m_wsfilter.getShouldStripSpace(makeNodeHandle(elemNode), this);
      boolean shouldStrip = (DTMWSFilter.INHERIT == wsv)
        ? getShouldStripWhitespace()
        : (DTMWSFilter.STRIP == wsv);

      pushShouldStripWhitespace(shouldStrip);
    }

    m_previous = DTM.NULL;

    m_contextIndexes.push(m_prefixMappings.size());  // for the children.
  }

  /**
   * Receive notification of the end of an element.
   *
   * @param name The element type name.
   * @param attributes The specified or defaulted attributes.
   *
   * @param uri The Namespace URI, or the empty string if the
   *        element has no Namespace URI or if Namespace
   *        processing is not being performed.
   * @param localName The local name (without prefix), or the
   *        empty string if Namespace processing is not being
   *        performed.
   * @param qName The qualified XML 1.0 name (with prefix), or the
   *        empty string if qualified names are not available.
   * @throws XNIException Any XNI exception, possibly
   *            wrapping another exception.
   * @see org.xml.sax.ContentHandler#endElement
   */
  public void endElement(QName element,Augmentations augs) throws XNIException
  {
    if (DEBUG)
      System.out.println("endElement: uri: " + element.uri + 
                         ", localname: " + element.localpart + ", qname: "+element.rawname);
                         
    if(null!=augs && null!=augs.getItem(DTM2XNI.DTM2XNI_ADDED_STRUCTURE))
    {
      if (DEBUG)
        System.out.println("\t***** Added by DTM2XNI; ignored here");
      
      return; // Ignore it!
    }

    try
    {
      super.endElement(element.uri,element.localpart,element.rawname);
    } 
    catch(SAXException e)
    {
      throw new XNIException(e);
    }

	// Complication: Type information is obtained at startElement.
	// VALIDITY isn't known 'till the endElement. XPath2 type is actually
	// a combination of information from both. NOTE that this largely defeats
	// attempts to implement XNI2DTM as a standard demand-mode DTM; we don't 
	// know the node type until we _exit_ the node; the nextNode() logic
	// would have to be rewritten to deal with that, and very little streaming
	// would actually result.

   	{
  	   // After super.endElement, m_previous is the element we're ending.	
   	  // Try to record as default for this nodetype, 
   	  // using our type wrapper which records XPath2 resolved NS/localname
      
	  ItemPSVI elemPSVI=null;
	  XPath2Type actualType =null;
      if(augs!=null)
      {
        elemPSVI=(ItemPSVI) augs.getItem(org.apache.xerces.impl.Constants.ELEMENT_PSVI);
        
	  /** %BUG% Work around Xerces' bad habit of not telling us what the type was
	   * at the time endElement() is called, and (obviously) not telling us
 	   * validity at the time startElement() is called. I hope to convince them
	   * to change the former.
	   * */
		XSSimpleTypeDefinition member=elemPSVI.getMemberTypeDefinition();
		XSTypeDefinition type=elemPSVI.getTypeDefinition();
        
		actualType =new XPath2Type(elemPSVI,member,type,false);
      }

      if(!m_expandedNameTable.setSchemaType(m_exptype.elementAt(m_previous),
                                            actualType)
         )
      {
        m_schemaTypeOverride.setElementAt(actualType,m_previous);
      }
    }
    
  }

  /** An empty element.
   * 
   * @param element - The name of the element.
   * @param attributes - The element attributes.
   * @param augs - Additional information that may include infoset augmentations
   * @throws XNIException - Thrown by handler to signal an error.
   * */
  public void emptyElement(QName element,
                           XMLAttributes attributes,
                           Augmentations augs)
    throws XNIException
  {
    // %OPT% We could skip the pushes and pops and save some cycles...
      startElement(element,attributes,augs);
      endElement(element,augs);
  }

  /**
   * Receive notification of character data inside an element.
   *
   * <p>By default, do nothing.  Application writers may override this
   * method to take specific actions for each chunk of character data
   * (such as adding the data to a node or buffer, or printing it to
   * a file).</p>
   *
   * @param ch The characters.
   * @param start The start position in the character array.
   * @param length The number of characters to use from the
   *               character array.
   * @throws XNIException Any XNI exception, possibly
   *            wrapping another exception.
   * @see org.xml.sax.ContentHandler#characters
   */
  public void characters(org.apache.xerces.xni.XMLString text,Augmentations augs) throws XNIException
  {
    try
    {
      super.characters(text.ch, text.offset, text.length);
    } 
    catch(SAXException e)
    {
      throw new XNIException(e);
    }
  }

  /**
   * Receive notification of ignorable whitespace in element content.
   *
   * <p>By default, do nothing.  Application writers may override this
   * method to take specific actions for each chunk of ignorable
   * whitespace (such as adding data to a node or buffer, or printing
   * it to a file).</p>
   *
   * @param ch The whitespace characters.
   * @param start The start position in the character array.
   * @param length The number of characters to use from the
   *               character array.
   * @throws XNIException Any XNI exception, possibly
   *            wrapping another exception.
   * @see org.xml.sax.ContentHandler#ignorableWhitespace
   */
  public void ignorableWhitespace(org.apache.xerces.xni.XMLString text, Augmentations augs)
    throws XNIException
  {
    // %OPT% We can probably take advantage of the fact that we know this 
    // is whitespace... or has that been dealt with at the source?
    try
    {
      super.characters(text.ch, text.offset, text.length);
    } 
    catch(SAXException e)
    {
      throw new XNIException(e);
    }
  }

  /**
   * Receive notification of a processing instruction.
   *
   * <p>By default, do nothing.  Application writers may override this
   * method in a subclass to take specific actions for each
   * processing instruction, such as setting status variables or
   * invoking other methods.</p>
   *
   * @param target The processing instruction target.
   * @param data The processing instruction data, or null if
   *             none is supplied.
   * @throws XNIException Any XNI exception, possibly
   *            wrapping another exception.
   * @see org.xml.sax.ContentHandler#processingInstruction
   */
  public void processingInstruction(String target,org.apache.xerces.xni.XMLString data,Augmentations augs)
    throws XNIException
  {
    if (DEBUG)
      System.out.println("processingInstruction: target: " + target +", data: "+data);

    try
    {
      // %REVIEW% Can we avoid toString? I don't think so, given our 
      // current data structures, but that bears reconsideration
      super.processingInstruction(target,data.toString());
    } 
    catch(SAXException e)
    {
      throw new XNIException(e);
    }
  }

  /**
   * Report the beginning of an entity in content.
   *
   * <p><strong>NOTE:</entity> entity references in attribute
   * values -- and the start and end of the document entity --
   * are never reported.</p>
   *
   * <p>The start and end of the external DTD subset are reported
   * using the pseudo-name "[dtd]".  All other events must be
   * properly nested within start/end entity events.</p>
   *
   * <p>Note that skipped entities will be reported through the
   * {@link org.xml.sax.ContentHandler#skippedEntity skippedEntity}
   * event, which is part of the ContentHandler interface.</p>
   *
   * @param name The name of the entity.  If it is a parameter
   *        entity, the name will begin with '%'.
   * @throws SAXException The application may raise an exception.
   * @see #endEntity
   * @see org.xml.sax.ext.DeclHandler#internalEntityDecl
   * @see org.xml.sax.ext.DeclHandler#externalEntityDecl
   */
  public void startGeneralEntity(String name,XMLResourceIdentifier identifier,
                                 String encoding,Augmentations augs) 
    throws XNIException
  {

    // no op
  }

  /**
   * Report the end of an entity.
   *
   * @param name The name of the entity that is ending.
   * @throws SAXException The application may raise an exception.
   * @see #startEntity
   */
  public void endGeneralEntity(String name,Augmentations augs) throws XNIException
  {

    // no op
  }
  
  /** Notifies of the presence of an XMLDecl line in the document. 
   * If present, this method will be called immediately following 
   * the startDocument call.
   * @param version - The XML version.
   * @param encoding - The IANA encoding name of the document, or null if not
   *    specified.
   * @param standalone - The standalone value, or null if not specified.
   * @param augs - Additional information that may include infoset augmentations
   * @throws XNIException - Thrown by handler to signal an error.
   * */
  public void xmlDecl(String version,String encoding,String standalone,
                      Augmentations augs)
    throws XNIException
  {

    // no op
  }
             
        
  /** Notifies of the presence of a TextDecl line in an entity. If present, this
   * method will be called immediately following the startEntity call. 
   * 
   * Note: This method will never be called for the document entity; it is only
   * called for external general entities referenced in document content. 
   * 
   * Note: This method is not called for entity references appearing as part of
   * attribute values.
   * @param version - The XML version, or null if not specified.
   * @param encoding - The IANA encoding name of the entity.
   * @param augs - Additional information that may include infoset augmentations
   * @throws XNIException - Thrown by handler to signal an error.
   * */
  public void textDecl(String version,String encoding,Augmentations augs)
    throws XNIException
  {

    // no op
  }
              

  /**
   * Report the start of a CDATA section.
   *
   * <p>The contents of the CDATA section will be reported through
   * the regular {@link org.xml.sax.ContentHandler#characters
   * characters} event.</p>
   *
   * @throws SAXException The application may raise an exception.
   * @see #endCDATA
   */
  public void startCDATA(Augmentations augs) throws XNIException
  {
    try
    {
      super.startCDATA();
    } 
    catch(SAXException e)
    {
      throw new XNIException(e);
    }
        
  }

  /**
   * Report the end of a CDATA section.
   *
   * @throws SAXException The application may raise an exception.
   * @see #startCDATA
   */
  public void endCDATA(Augmentations augs) throws XNIException
  {
    try
    {
      super.endCDATA();
    } 
    catch(SAXException e)
    {
      throw new XNIException(e);
    }
  }

  /**
   * Report an XML comment anywhere in the document.
   *
   * <p>This callback will be used for comments inside or outside the
   * document element, including comments in the external DTD
   * subset (if read).</p>
   *
   * @param ch An array holding the characters in the comment.
   * @param start The starting position in the array.
   * @param length The number of characters to use from the array.
   * @throws SAXException The application may raise an exception.
   */
  public void comment(org.apache.xerces.xni.XMLString text,Augmentations augs) throws XNIException
  {
    try
    {
      super.comment(text.ch,text.offset,text.length);
    } 
    catch(SAXException e)
    {
      throw new XNIException(e);
    }
  }


  /** Helper class: Present XNI Locator info as SAX Locator
   * */
  private class LocatorWrapper 
    implements org.xml.sax.Locator
  {
    XMLLocator locator;
    String publicId=null;
    String systemId=null;
        
    public void setLocator(XMLLocator locator)
    { this.locator=locator; }
        
    public void setPublicId(String publicId)
    { this.publicId=publicId; }
        
    public void setSystemId(String systemId)
    { this.systemId=systemId; }
        
    public int getColumnNumber() 
    {
      return locator.getColumnNumber();
    }
    public int getLineNumber() 
    {
      return locator.getLineNumber();
    }
    public String getPublicId() 
    {
      return publicId;
    }
    public String getSystemId()         
    {
      return systemId;
    }
  }
  
  /** Notifies of the presence of the DOCTYPE line in the document.
         *  @param rootElement - The name of the root element.
         * @param publicId - The public identifier if an external DTD or null if the
         *        external DTD is specified using SYSTEM.
         * @param systemId - The system identifier if an external DTD, null otherwise.
         * @param augs - Additional information that may include infoset augmentations
         * @throws XNIException - Thrown by handler to signal an error.
         * */
  public void doctypeDecl(String rootElement,String publicId,String systemId,
                          Augmentations augs)
    throws XNIException
  {
    // no op
  }
    
  ////////////////////////////////////////////////////////////////////
  // XNI error handler
  
  public void warning(java.lang.String domain,
                      java.lang.String key,
                      XMLParseException exception)
    throws XNIException
  {
    // %REVIEW% Should be routed to the JAXP error listener, presumably.
    // What's the easiest way to get that from here?
    // It's available from the xctxt, but I'm not sure how to get that
    // from the DTM structures; the connections mostly go the other way.
    System.err.println(exception);
  }
  public void error(java.lang.String domain,
                    java.lang.String key,
                    XMLParseException exception)
    throws XNIException
  {
    // %REVIEW% Should be routed to the JAXP error listener, presumably.
    // see warning()
     System.err.println(exception);
  }
  public void fatalError(java.lang.String domain,
                         java.lang.String key,
                         XMLParseException exception)
    throws XNIException
  {
    // %REVIEW% Should be routed to the JAXP error listener, presumably.
    // see warning()
    throw exception;
  }
  
  ////////////////////////////////////////////////////////////////////
  // XNI DTD handler. Needed for unparsed entities, but to get that
  // have to accept the other calls... which means we need to know the start
  // and end of the DTD to prevent DTD comments from being taken as
  // part of the main document.
  
  public void startDTD(XMLLocator locator,
                       Augmentations augmentations)
    throws XNIException
  {
    try
    {
      super.startDTD("unknownDocumentTypeName",locator.getPublicId(),locator.getLiteralSystemId());
    } 
    catch(SAXException e)
    {
      throw new XNIException(e);
    }
  }
  
  public void startParameterEntity(java.lang.String name,
                                   XMLResourceIdentifier identifier,
                                   java.lang.String encoding,
                                   Augmentations augmentations)
    throws XNIException
  {
    // no op
  }
  
  // textDecl already handled
  
  public void endParameterEntity(java.lang.String name,
                                 Augmentations augmentations)
    throws XNIException  
  {
    // no op
  }
  public void startExternalSubset(XMLResourceIdentifier resource,Augmentations augmentations)
    throws XNIException  
  {
    // no op
  }
  public void endExternalSubset(Augmentations augmentations)
    throws XNIException  
  {
    // no op
  }  
  
  // comment already handled, including suppression during the DTD
  // processing instruction already handled. NOT currently suppressed during DTD?
  
  public void elementDecl(java.lang.String name,
                          java.lang.String contentModel,
                          Augmentations augmentations)
    throws XNIException
  {
    //no op
  }
  public void startAttlist(java.lang.String elementName,
                           Augmentations augmentations)
    throws XNIException
  {
    //no op
  }
  
  public void attributeDecl(String elementName,String attributeName, 
                            java.lang.String type, java.lang.String[] enumeration, 
                            String defaultType, XMLString defaultValue,
                            XMLString nonNormalizedDefaultValue, Augmentations augmentations)
    throws XNIException
  { 
    // no op
  }
  
  public void endAttlist(Augmentations augmentations)
    throws XNIException  
  {
    //no op
  }
  public void internalEntityDecl(String name, XMLString text, 
                                 XMLString nonNormalizedText,
                                 Augmentations augmentations) 
    throws XNIException
  {
    //no op
  }
  public void externalEntityDecl(java.lang.String name,
                                 XMLResourceIdentifier identifier,
                                 Augmentations augmentations)
    throws XNIException
  {
    //no op
  }
  
  public void unparsedEntityDecl(java.lang.String name,
                                 XMLResourceIdentifier identifier,
                                 java.lang.String notation,
                                 Augmentations augmentations)
    throws XNIException
  {
    // This one's the wnole point of the exercise...
    try
    {
      super.unparsedEntityDecl(name,identifier.getPublicId(),identifier.getLiteralSystemId(),notation);
    } 
    catch(SAXException e)
    {
      throw new XNIException(e);
    }
  }     
 
  public void notationDecl(java.lang.String name,
                           XMLResourceIdentifier identifier,
                           Augmentations augmentations)
    throws XNIException
  {
    // no op
  }
  public void startConditional(short type,
                               Augmentations augmentations)
    throws XNIException
  {
    // no op
  }
  
  public void ignoredCharacters(XMLString text, Augmentations augmentations)
    throws XNIException
  {
    // no op
  }
  public void endConditional(Augmentations augmentations)
    throws XNIException
  {
    // no op
  }
  
  public void endDTD(Augmentations augmentations)
    throws XNIException
  {
    try
    {
      super.endDTD();
    } 
    catch(SAXException e)
    {
      throw new XNIException(e);
    }
  }     
 
  ////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////
  // %REVIEW% SAX APIs are currently _blocked_ for debugging purposes. We can
  // re-enable them if/when we fold the XNI support back into the main SAX2DTM
  ////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////
  
 
  // Implementation of DTDHandler interface.

  // SAME AS XNI? OR DID WE RETAIN THEM ABOVE FOR OTHER REASONS?
  //public void notationDecl(String name, String publicId, String systemId)
  //        throws SAXException
  // public void unparsedEntityDecl(
  //        String name, String publicId, String systemId, String notationName)
  //        throws SAXException

  // Implementation of ContentHandler interface.

  public void setDocumentLocator(org.xml.sax.Locator locator)
  {
    throw new RuntimeException(UNEXPECTED+"setDocumentLocator");
  }
  public void startDocument() throws SAXException
  {
    throw new SAXException(UNEXPECTED+"startDocument");
  }
  public void endDocument() throws SAXException
  {
    throw new SAXException(UNEXPECTED+"endDocument");
  }
  public void startPrefixMapping(String prefix, String uri)
    throws SAXException
  {
    throw new SAXException(UNEXPECTED+"startPrefixMapping");
  }
  public void endPrefixMapping(String prefix) throws SAXException
  {
    throw new SAXException(UNEXPECTED+"endPrefixMapping");
  }
  public void startElement(
                           String uri, String localName, String qName, org.xml.sax.Attributes attributes)
    throws SAXException
  {
    throw new SAXException(UNEXPECTED+"startElement");
  }
  public void endElement(String uri, String localName, String qName)
    throws SAXException
  {
    throw new SAXException(UNEXPECTED+"endElement");
  }
  public void characters(char ch[], int start, int length) throws SAXException
  {
    throw new SAXException(UNEXPECTED+"characters");
  }
  public void ignorableWhitespace(char ch[], int start, int length)
    throws SAXException
  {
    throw new SAXException(UNEXPECTED+"ignorableWhitespace");
  }
  public void processingInstruction(String target, String data)
    throws SAXException
  {
    throw new SAXException(UNEXPECTED+"processingInstruction");
  }
  public void skippedEntity(String name) throws SAXException
  {
    throw new SAXException(UNEXPECTED+"skippedEntity");
  }
  
  // Implementation of SAX error handler

  public void warning(SAXParseException e) throws SAXException
  {
    // %REVIEW% Is there anyway to get the JAXP error listener here?
    throw new SAXException(UNEXPECTED+"Warning: "+e.getMessage());
  }
  public void error(SAXParseException e) throws SAXException
  {
    throw new SAXException(UNEXPECTED+"Error: "+e.getMessage());
  }
  public void fatalError(SAXParseException e) throws SAXException
  {
    throw new SAXException(UNEXPECTED+"Fatal Error: "+e.getMessage());
  }

  // Implementation of SAX DeclHandler interface.
  
  public void elementDecl(String name, String model) throws SAXException
  {
    throw new SAXException(UNEXPECTED+"elementDecl "+name);
  }
  public void attributeDecl(
                            String eName, String aName, String type, String valueDefault, String value)
    throws SAXException
  {
    throw new SAXException(UNEXPECTED+"attributeDecl "+aName);
  }
  public void internalEntityDecl(String name, String value)
    throws SAXException
  {
    throw new SAXException(UNEXPECTED+"internalEntityDecl "+name);
  }
  public void externalEntityDecl(
                                 String name, String publicId, String systemId) throws SAXException
  {
    throw new SAXException(UNEXPECTED+"externalEntityDecl "+name);
  }

  // Implementation of the LexicalHandler interface.
  public void startDTD(String name, String publicId, String systemId)
    throws SAXException
  {
    throw new SAXException(UNEXPECTED+"startDTD");
  }
  public void endDTD() throws SAXException
  {
    throw new SAXException(UNEXPECTED+"endDTD");
  }

} // XNI2DTM
