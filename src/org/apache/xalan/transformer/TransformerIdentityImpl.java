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

import java.util.Properties;
import java.util.Hashtable;

import java.io.IOException;

// TRaX Imports
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;
import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.xml.sax.ext.*;

import org.apache.xalan.serialize.*;
import org.apache.xml.utils.DOMBuilder;
import org.apache.xml.utils.TreeWalker;
import org.apache.xalan.templates.OutputProperties;
import org.apache.xalan.serialize.Method;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.res.XSLMessages;

import org.w3c.dom.*;

/**
 * This class implements an identity transformer for
 * {@link javax.xml.transform.sax.SAXTransformerFactory#newTransformerHandler()
 * and {@link javax.xml.transform.TransformerFactory#newTransformer()}.  It
 * simply feeds SAX events directly to a serializer ContentHandler, if the
 * result is a stream.  If the result is a DOM, it will send the events to
 * {@link org.apache.xml.utils.DOMBuilder}.  If the result is another
 * content handler, it will simply pass the events on.
 */
public class TransformerIdentityImpl extends Transformer
        implements TransformerHandler, DeclHandler
{

  /**
   * Constructor TransformerIdentityImpl creates an identity transform.
   *
   */
  public TransformerIdentityImpl()
  {
    m_outputFormat = new OutputProperties(Method.XML);
  }

  /**
   * Enables the user of the TransformerHandler to set the
   * to set the Result for the transformation.
   *
   * @param result A Result instance, should not be null.
   *
   * @throws IllegalArgumentException if result is invalid for some reason.
   */
  public void setResult(Result result) throws IllegalArgumentException
  {
    if(null == result)
      throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_RESULT_NULL, null)); //"Result should not be null");        
    m_result = result;
  }

  /**
   * Set the base ID (URI or system ID) from where relative
   * URLs will be resolved.
   * @param systemID Base URI for the source tree.
   */
  public void setSystemId(String systemID)
  {
    m_systemID = systemID;
  }

  /**
   * Get the base ID (URI or system ID) from where relative
   * URLs will be resolved.
   * @return The systemID that was set with {@link #setSystemId}.
   */
  public String getSystemId()
  {
    return m_systemID;
  }

  /**
   * Get the Transformer associated with this handler, which
   * is needed in order to set parameters and output properties.
   *
   * @return non-null reference to the transformer.
   */
  public Transformer getTransformer()
  {
    return this;
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
  private void createResultContentHandler(Result outputTarget)
          throws TransformerException
  {

    if (outputTarget instanceof SAXResult)
    {
      SAXResult saxResult = (SAXResult) outputTarget;

      m_resultContentHandler = saxResult.getHandler();
      m_resultLexicalHandler = saxResult.getLexicalHandler();

      if (m_resultContentHandler instanceof Serializer)
      {

        // Dubious but needed, I think.
        m_serializer = (Serializer) m_resultContentHandler;
      }
    }
    else if (outputTarget instanceof DOMResult)
    {
      DOMResult domResult = (DOMResult) outputTarget;
      Node outputNode = domResult.getNode();
      Document doc;
      short type;

      if (null != outputNode)
      {
        type = outputNode.getNodeType();
        doc = (Node.DOCUMENT_NODE == type)
              ? (Document) outputNode : outputNode.getOwnerDocument();
      }
      else
      {
        try
        {
          DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

          dbf.setNamespaceAware(true);

          DocumentBuilder db = dbf.newDocumentBuilder();

          doc = db.newDocument();
        }
        catch (ParserConfigurationException pce)
        {
          throw new TransformerException(pce);
        }

        outputNode = doc;
        type = outputNode.getNodeType();

        ((DOMResult) outputTarget).setNode(outputNode);
      }

      m_resultContentHandler =
        (Node.DOCUMENT_FRAGMENT_NODE == type)
        ? new DOMBuilder(doc, (DocumentFragment) outputNode)
        : new DOMBuilder(doc, outputNode);
      m_resultLexicalHandler = (LexicalHandler) m_resultContentHandler;
    }
    else if (outputTarget instanceof StreamResult)
    {
      StreamResult sresult = (StreamResult) outputTarget;
      String method = m_outputFormat.getProperty(OutputKeys.METHOD);

      try
      {
        Serializer serializer =
          SerializerFactory.getSerializer(m_outputFormat.getProperties());

        m_serializer = serializer;

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

        m_resultContentHandler = serializer.asContentHandler();
      }
      catch (IOException ioe)
      {
        throw new TransformerException(ioe);
      }
    }
    else
    {
      throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_CANNOT_TRANSFORM_TO_RESULT_TYPE, new Object[]{outputTarget.getClass().getName()})); //"Can't transform to a Result of type "
                                    // + outputTarget.getClass().getName()
                                    // + "!");
    }

    if (m_resultContentHandler instanceof DTDHandler)
      m_resultDTDHandler = (DTDHandler) m_resultContentHandler;
    
    if (m_resultContentHandler instanceof DeclHandler)
      m_resultDeclHandler = (DeclHandler) m_resultContentHandler;

    if (m_resultContentHandler instanceof LexicalHandler)
      m_resultLexicalHandler = (LexicalHandler) m_resultContentHandler;
  }

  /**
   * Process the source tree to the output result.
   * @param source  The input for the source tree.
   *
   * @param outputTarget The output target.
   *
   * @throws TransformerException If an unrecoverable error occurs
   * during the course of the transformation.
   */
  public void transform(Source source, Result outputTarget)
          throws TransformerException
  {

    createResultContentHandler(outputTarget);

    try
    {
      if (source instanceof DOMSource)
      {
        DOMSource dsource = (DOMSource) source;
  
        m_systemID = dsource.getSystemId();
  
        Node dNode = dsource.getNode();
  
        if (null != dNode)
        {
          try
          {
            if(dNode.getNodeType() != Node.DOCUMENT_NODE)
              this.startDocument();
            try
            {
              if(dNode.getNodeType() == Node.ATTRIBUTE_NODE)
              {
                String data = dNode.getNodeValue();
                char[] chars = data.toCharArray();
                characters(chars, 0, chars.length);
              }
              else
              {
                TreeWalker walker = new TreeWalker(this, new org.apache.xpath.DOM2Helper(), m_systemID);
                walker.traverse(dNode);
              }
            }
            finally
            {
              if(dNode.getNodeType() != Node.DOCUMENT_NODE)
                this.endDocument();
            }
          }
          catch (SAXException se)
          {
            throw new TransformerException(se);
          }
  
          return;
        }
        else
        {
          String messageStr = XSLMessages.createMessage(
            XSLTErrorResources.ER_ILLEGAL_DOMSOURCE_INPUT, null);
  
          throw new IllegalArgumentException(messageStr);
        }
      }
  
      InputSource xmlSource = SAXSource.sourceToInputSource(source);
  
      if (null == xmlSource)
      {
        throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_CANNOT_TRANSFORM_SOURCE_TYPE, new Object[]{source.getClass().getName()})); //"Can't transform a Source of type "
                                       //+ source.getClass().getName() + "!");
      }
  
      if (null != xmlSource.getSystemId())
        m_systemID = xmlSource.getSystemId();
  
      try
      {
        XMLReader reader = null;
  
        if (source instanceof SAXSource)
          reader = ((SAXSource) source).getXMLReader();
          
        boolean isUserReader = (reader != null);
  
        if (null == reader)
        {
  
          // Use JAXP1.1 ( if possible )      
          try
          {
            javax.xml.parsers.SAXParserFactory factory =
              javax.xml.parsers.SAXParserFactory.newInstance();
  
            factory.setNamespaceAware(true);
  
            javax.xml.parsers.SAXParser jaxpParser = factory.newSAXParser();
  
            reader = jaxpParser.getXMLReader();
          }
          catch (javax.xml.parsers.ParserConfigurationException ex)
          {
            throw new org.xml.sax.SAXException(ex);
          }
          catch (javax.xml.parsers.FactoryConfigurationError ex1)
          {
            throw new org.xml.sax.SAXException(ex1.toString());
          }
          catch (NoSuchMethodError ex2){}
          catch (AbstractMethodError ame){}
        }
  
        if (null == reader)
        {
          reader = XMLReaderFactory.createXMLReader();
        }
  
        try
        {
          reader.setFeature("http://xml.org/sax/features/namespace-prefixes",
                            true);
        // Commented out as per discussion with Thomas2.Maesing@bgs-ag.de 
        // about bug 2124.
//          if(!isUserReader)
//            reader.setFeature("http://apache.org/xml/features/validation/dynamic",
//                              true);
        }
        catch (org.xml.sax.SAXException se)
        {
  
          // We don't care.
        }
  
        // Get the input content handler, which will handle the 
        // parse events and create the source tree. 
        ContentHandler inputHandler = this;
  
        reader.setContentHandler(inputHandler);
  
        if (inputHandler instanceof org.xml.sax.DTDHandler)
          reader.setDTDHandler((org.xml.sax.DTDHandler) inputHandler);
  
        try
        {
          if (inputHandler instanceof org.xml.sax.ext.LexicalHandler)
            reader.setProperty("http://xml.org/sax/properties/lexical-handler",
                               inputHandler);
  
          if (inputHandler instanceof org.xml.sax.ext.DeclHandler)
            reader.setProperty(
              "http://xml.org/sax/properties/declaration-handler",
              inputHandler);
        }
        catch (org.xml.sax.SAXException se){}
  
        try
        {
          if (inputHandler instanceof org.xml.sax.ext.LexicalHandler)
            reader.setProperty("http://xml.org/sax/handlers/LexicalHandler",
                               inputHandler);
  
          if (inputHandler instanceof org.xml.sax.ext.DeclHandler)
            reader.setProperty("http://xml.org/sax/handlers/DeclHandler",
                               inputHandler);
        }
        catch (org.xml.sax.SAXNotRecognizedException snre){}
  
        reader.parse(xmlSource);
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
  
        throw new TransformerException(wre.getException());
      }
      catch (org.xml.sax.SAXException se)
      {
        throw new TransformerException(se);
      }
      catch (IOException ioe)
      {
        throw new TransformerException(ioe);
      }
    }
    finally
    {
      if(null != m_outputStream)
      {
        try
        {
          m_outputStream.close();
        }
        catch(IOException ioe){}
        m_outputStream = null;
      }
    }
  }

  /**
   * Add a parameter for the transformation.
   *
   * <p>Pass a qualified name as a two-part string, the namespace URI
   * enclosed in curly braces ({}), followed by the local name. If the
   * name has a null URL, the String only contain the local name. An
   * application can safely check for a non-null URI by testing to see if the first
   * character of the name is a '{' character.</p>
   * <p>For example, if a URI and local name were obtained from an element
   * defined with &lt;xyz:foo xmlns:xyz="http://xyz.foo.com/yada/baz.html"/&gt;,
   * then the qualified name would be "{http://xyz.foo.com/yada/baz.html}foo". Note that
   * no prefix is used.</p>
   *
   * @param name The name of the parameter, which may begin with a namespace URI
   * in curly braces ({}).
   * @param value The value object.  This can be any valid Java object. It is
   * up to the processor to provide the proper object coersion or to simply
   * pass the object on for use in an extension.
   */
  public void setParameter(String name, Object value)
  {

    if (null == m_params)
    {
      m_params = new Hashtable();
    }

    m_params.put(name, value);
  }

  /**
   * Get a parameter that was explicitly set with setParameter
   * or setParameters.
   *
   * <p>This method does not return a default parameter value, which
   * cannot be determined until the node context is evaluated during
   * the transformation process.
   *
   *
   * @param name Name of the parameter.
   * @return A parameter that has been set with setParameter.
   */
  public Object getParameter(String name)
  {

    if (null == m_params)
      return null;

    return m_params.get(name);
  }

  /**
   * Clear all parameters set with setParameter.
   */
  public void clearParameters()
  {

    if (null == m_params)
      return;

    m_params.clear();
  }

  /**
   * Set an object that will be used to resolve URIs used in
   * document().
   *
   * <p>If the resolver argument is null, the URIResolver value will
   * be cleared, and the default behavior will be used.</p>
   *
   * @param resolver An object that implements the URIResolver interface,
   * or null.
   */
  public void setURIResolver(URIResolver resolver)
  {
    m_URIResolver = resolver;
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
    return m_URIResolver;
  }

  /**
   * Set the output properties for the transformation.  These
   * properties will override properties set in the Templates
   * with xsl:output.
   *
   * <p>If argument to this function is null, any properties
   * previously set are removed, and the value will revert to the value
   * defined in the templates object.</p>
   *
   * <p>Pass a qualified property key name as a two-part string, the namespace URI
   * enclosed in curly braces ({}), followed by the local name. If the
   * name has a null URL, the String only contain the local name. An
   * application can safely check for a non-null URI by testing to see if the first
   * character of the name is a '{' character.</p>
   * <p>For example, if a URI and local name were obtained from an element
   * defined with &lt;xyz:foo xmlns:xyz="http://xyz.foo.com/yada/baz.html"/&gt;,
   * then the qualified name would be "{http://xyz.foo.com/yada/baz.html}foo". Note that
   * no prefix is used.</p>
   *
   * @param oformat A set of output properties that will be
   * used to override any of the same properties in affect
   * for the transformation.
   *
   * @see javax.xml.transform.OutputKeys
   * @see java.util.Properties
   *
   * @throws IllegalArgumentException if any of the argument keys are not
   * recognized and are not namespace qualified.
   */
  public void setOutputProperties(Properties oformat)
          throws IllegalArgumentException
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
  }

  /**
   * Get a copy of the output properties for the transformation.
   *
   * <p>The properties returned should contain properties set by the user,
   * and properties set by the stylesheet, and these properties
   * are "defaulted" by default properties specified by <a href="http://www.w3.org/TR/xslt#output">section 16 of the
   * XSL Transformations (XSLT) W3C Recommendation</a>.  The properties that
   * were specifically set by the user or the stylesheet should be in the base
   * Properties list, while the XSLT default properties that were not
   * specifically set should be the default Properties list.  Thus,
   * getOutputProperties().getProperty(String key) will obtain any
   * property in that was set by {@link #setOutputProperty},
   * {@link #setOutputProperties}, in the stylesheet, <em>or</em> the default
   * properties, while
   * getOutputProperties().get(String key) will only retrieve properties
   * that were explicitly set by {@link #setOutputProperty},
   * {@link #setOutputProperties}, or in the stylesheet.</p>
   *
   * <p>Note that mutation of the Properties object returned will not
   * effect the properties that the transformation contains.</p>
   *
   * <p>If any of the argument keys are not recognized and are not
   * namespace qualified, the property will be ignored.  In other words the
   * behaviour is not orthogonal with setOutputProperties.</p>
   *
   * @return A copy of the set of output properties in effect
   * for the next transformation.
   *
   * @see javax.xml.transform.OutputKeys
   * @see java.util.Properties
   */
  public Properties getOutputProperties()
  {
    return (Properties) m_outputFormat.getProperties().clone();
  }

  /**
   * Set an output property that will be in effect for the
   * transformation.
   *
   * <p>Pass a qualified property name as a two-part string, the namespace URI
   * enclosed in curly braces ({}), followed by the local name. If the
   * name has a null URL, the String only contain the local name. An
   * application can safely check for a non-null URI by testing to see if the first
   * character of the name is a '{' character.</p>
   * <p>For example, if a URI and local name were obtained from an element
   * defined with &lt;xyz:foo xmlns:xyz="http://xyz.foo.com/yada/baz.html"/&gt;,
   * then the qualified name would be "{http://xyz.foo.com/yada/baz.html}foo". Note that
   * no prefix is used.</p>
   *
   * <p>The Properties object that was passed to {@link #setOutputProperties} won't
   * be effected by calling this method.</p>
   *
   * @param name A non-null String that specifies an output
   * property name, which may be namespace qualified.
   * @param value The non-null string value of the output property.
   *
   * @throws IllegalArgumentException If the property is not supported, and is
   * not qualified with a namespace.
   *
   * @see javax.xml.transform.OutputKeys
   */
  public void setOutputProperty(String name, String value)
          throws IllegalArgumentException
  {

    if (!m_outputFormat.isLegalPropertyKey(name))
      throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_OUTPUT_PROPERTY_NOT_RECOGNIZED, new Object[]{name})); //"output property not recognized: "
                                         //+ name);

    m_outputFormat.setProperty(name, value);
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
   * @return The string value of the output property, or null
   * if no property was found.
   *
   * @throws IllegalArgumentException If the property is not supported.
   *
   * @see javax.xml.transform.OutputKeys
   */
  public String getOutputProperty(String name) throws IllegalArgumentException
  {

    String value = null;
    OutputProperties props = m_outputFormat;

    value = props.getProperty(name);

    if (null == value)
    {
      if (!props.isLegalPropertyKey(name))
        throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_OUTPUT_PROPERTY_NOT_RECOGNIZED, new Object[]{name})); //"output property not recognized: "
                                          // + name);
    }

    return value;
  }

  /**
   * Set the error event listener in effect for the transformation.
   *
   * @param listener The new error listener.
   * @throws IllegalArgumentException if listener is null.
   */
  public void setErrorListener(ErrorListener listener)
          throws IllegalArgumentException
  {
    m_errorListener = listener;
  }

  /**
   * Get the error event handler in effect for the transformation.
   *
   * @return The current error handler, which should never be null.
   */
  public ErrorListener getErrorListener()
  {
    return m_errorListener;
  }

  ////////////////////////////////////////////////////////////////////
  // Default implementation of DTDHandler interface.
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
   * @throws org.xml.sax.SAXException Any SAX exception, possibly
   *            wrapping another exception.
   * @see org.xml.sax.DTDHandler#notationDecl
   *
   * @throws SAXException
   */
  public void notationDecl(String name, String publicId, String systemId)
          throws SAXException
  {
    if (null != m_resultDTDHandler)
      m_resultDTDHandler.notationDecl(name, publicId, systemId);
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
   * @throws org.xml.sax.SAXException Any SAX exception, possibly
   *            wrapping another exception.
   * @see org.xml.sax.DTDHandler#unparsedEntityDecl
   *
   * @throws SAXException
   */
  public void unparsedEntityDecl(
          String name, String publicId, String systemId, String notationName)
            throws SAXException
  {

    if (null != m_resultDTDHandler)
      m_resultDTDHandler.unparsedEntityDecl(name, publicId, systemId,
                                            notationName);
  }

  ////////////////////////////////////////////////////////////////////
  // Default implementation of ContentHandler interface.
  ////////////////////////////////////////////////////////////////////

  /**
   * Receive a Locator object for document events.
   *
   * <p>By default, do nothing.  Application writers may override this
   * method in a subclass if they wish to store the locator for use
   * with other document events.</p>
   *
   * @param locator A locator for all SAX document events.
   * @see org.xml.sax.ContentHandler#setDocumentLocator
   * @see org.xml.sax.Locator
   */
  public void setDocumentLocator(Locator locator)
  {
    try
    {
      if (null == m_resultContentHandler)
        createResultContentHandler(m_result);
    }
    catch (TransformerException te)
    {
      throw new org.apache.xml.utils.WrappedRuntimeException(te);
    }

    m_resultContentHandler.setDocumentLocator(locator);
  }

  /**
   * Receive notification of the beginning of the document.
   *
   * <p>By default, do nothing.  Application writers may override this
   * method in a subclass to take specific actions at the beginning
   * of a document (such as allocating the root node of a tree or
   * creating an output file).</p>
   *
   * @throws org.xml.sax.SAXException Any SAX exception, possibly
   *            wrapping another exception.
   * @see org.xml.sax.ContentHandler#startDocument
   *
   * @throws SAXException
   */
  public void startDocument() throws SAXException
  {

    try
    {
      if (null == m_resultContentHandler)
        createResultContentHandler(m_result);
    }
    catch (TransformerException te)
    {
      throw new SAXException(te.getMessage(), te);
    }

    // Reset for multiple transforms with this transformer.
    m_flushedStartDoc = false;
    m_foundFirstElement = false;
  }
  
  boolean m_flushedStartDoc = false;
  
  protected final void flushStartDoc()
     throws SAXException
  {
    if(!m_flushedStartDoc)
    {
      m_resultContentHandler.startDocument();
      m_flushedStartDoc = true;
    }
  }

  /**
   * Receive notification of the end of the document.
   *
   * <p>By default, do nothing.  Application writers may override this
   * method in a subclass to take specific actions at the end
   * of a document (such as finalising a tree or closing an output
   * file).</p>
   *
   * @throws org.xml.sax.SAXException Any SAX exception, possibly
   *            wrapping another exception.
   * @see org.xml.sax.ContentHandler#endDocument
   *
   * @throws SAXException
   */
  public void endDocument() throws SAXException
  {
    flushStartDoc();
    m_resultContentHandler.endDocument();
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
   * @throws org.xml.sax.SAXException Any SAX exception, possibly
   *            wrapping another exception.
   * @see org.xml.sax.ContentHandler#startPrefixMapping
   *
   * @throws SAXException
   */
  public void startPrefixMapping(String prefix, String uri)
          throws SAXException
  {
    flushStartDoc();
    m_resultContentHandler.startPrefixMapping(prefix, uri);
  }

  /**
   * Receive notification of the end of a Namespace mapping.
   *
   * <p>By default, do nothing.  Application writers may override this
   * method in a subclass to take specific actions at the end of
   * each prefix mapping.</p>
   *
   * @param prefix The Namespace prefix being declared.
   * @throws org.xml.sax.SAXException Any SAX exception, possibly
   *            wrapping another exception.
   * @see org.xml.sax.ContentHandler#endPrefixMapping
   *
   * @throws SAXException
   */
  public void endPrefixMapping(String prefix) throws SAXException
  {
    flushStartDoc();
    m_resultContentHandler.endPrefixMapping(prefix);
  }

  /**
   * Receive notification of the start of an element.
   *
   * <p>By default, do nothing.  Application writers may override this
   * method in a subclass to take specific actions at the start of
   * each element (such as allocating a new tree node or writing
   * output to a file).</p>
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
   * @throws org.xml.sax.SAXException Any SAX exception, possibly
   *            wrapping another exception.
   * @see org.xml.sax.ContentHandler#startElement
   *
   * @throws SAXException
   */
  public void startElement(
          String uri, String localName, String qName, Attributes attributes)
            throws SAXException
  {

    if (!m_foundFirstElement && null != m_serializer)
    {
      m_foundFirstElement = true;

      Serializer newSerializer;

      try
      {
        newSerializer = SerializerSwitcher.switchSerializerIfHTML(uri,
                localName, m_outputFormat.getProperties(), m_serializer);
      }
      catch (TransformerException te)
      {
        throw new SAXException(te);
      }

      if (newSerializer != m_serializer)
      {
        try
        {
          m_resultContentHandler = newSerializer.asContentHandler();
        }
        catch (IOException ioe)  // why?
        {
          throw new SAXException(ioe);
        }

        if (m_resultContentHandler instanceof DTDHandler)
          m_resultDTDHandler = (DTDHandler) m_resultContentHandler;

        if (m_resultContentHandler instanceof LexicalHandler)
          m_resultLexicalHandler = (LexicalHandler) m_resultContentHandler;

        m_serializer = newSerializer;
      }
    }
    flushStartDoc();
    m_resultContentHandler.startElement(uri, localName, qName, attributes);
  }

  /**
   * Receive notification of the end of an element.
   *
   * <p>By default, do nothing.  Application writers may override this
   * method in a subclass to take specific actions at the end of
   * each element (such as finalising a tree node or writing
   * output to a file).</p>
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
   *
   * @throws org.xml.sax.SAXException Any SAX exception, possibly
   *            wrapping another exception.
   * @see org.xml.sax.ContentHandler#endElement
   *
   * @throws SAXException
   */
  public void endElement(String uri, String localName, String qName)
          throws SAXException
  {
    m_resultContentHandler.endElement(uri, localName, qName);
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
   * @throws org.xml.sax.SAXException Any SAX exception, possibly
   *            wrapping another exception.
   * @see org.xml.sax.ContentHandler#characters
   *
   * @throws SAXException
   */
  public void characters(char ch[], int start, int length) throws SAXException
  {
    flushStartDoc();
    m_resultContentHandler.characters(ch, start, length);
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
   * @throws org.xml.sax.SAXException Any SAX exception, possibly
   *            wrapping another exception.
   * @see org.xml.sax.ContentHandler#ignorableWhitespace
   *
   * @throws SAXException
   */
  public void ignorableWhitespace(char ch[], int start, int length)
          throws SAXException
  {
    m_resultContentHandler.ignorableWhitespace(ch, start, length);
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
   * @throws org.xml.sax.SAXException Any SAX exception, possibly
   *            wrapping another exception.
   * @see org.xml.sax.ContentHandler#processingInstruction
   *
   * @throws SAXException
   */
  public void processingInstruction(String target, String data)
          throws SAXException
  {
    flushStartDoc();
    m_resultContentHandler.processingInstruction(target, data);
  }

  /**
   * Receive notification of a skipped entity.
   *
   * <p>By default, do nothing.  Application writers may override this
   * method in a subclass to take specific actions for each
   * processing instruction, such as setting status variables or
   * invoking other methods.</p>
   *
   * @param name The name of the skipped entity.
   * @throws org.xml.sax.SAXException Any SAX exception, possibly
   *            wrapping another exception.
   * @see org.xml.sax.ContentHandler#processingInstruction
   *
   * @throws SAXException
   */
  public void skippedEntity(String name) throws SAXException
  {
    flushStartDoc();
    m_resultContentHandler.skippedEntity(name);
  }

  /**
   * Report the start of DTD declarations, if any.
   *
   * <p>Any declarations are assumed to be in the internal subset
   * unless otherwise indicated by a {@link #startEntity startEntity}
   * event.</p>
   *
   * <p>Note that the start/endDTD events will appear within
   * the start/endDocument events from ContentHandler and
   * before the first startElement event.</p>
   *
   * @param name The document type name.
   * @param publicId The declared public identifier for the
   *        external DTD subset, or null if none was declared.
   * @param systemId The declared system identifier for the
   *        external DTD subset, or null if none was declared.
   * @throws SAXException The application may raise an
   *            exception.
   * @see #endDTD
   * @see #startEntity
   */
  public void startDTD(String name, String publicId, String systemId)
          throws SAXException
  {
    flushStartDoc();
    if (null != m_resultLexicalHandler)
      m_resultLexicalHandler.startDTD(name, publicId, systemId);
  }

  /**
   * Report the end of DTD declarations.
   *
   * @throws SAXException The application may raise an exception.
   * @see #startDTD
   */
  public void endDTD() throws SAXException
  {
    if (null != m_resultLexicalHandler)
      m_resultLexicalHandler.endDTD();
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
  public void startEntity(String name) throws SAXException
  {
    if (null != m_resultLexicalHandler)
      m_resultLexicalHandler.startEntity(name);
  }

  /**
   * Report the end of an entity.
   *
   * @param name The name of the entity that is ending.
   * @throws SAXException The application may raise an exception.
   * @see #startEntity
   */
  public void endEntity(String name) throws SAXException
  {
    if (null != m_resultLexicalHandler)
      m_resultLexicalHandler.endEntity(name);
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
  public void startCDATA() throws SAXException
  {
    if (null != m_resultLexicalHandler)
      m_resultLexicalHandler.startCDATA();
  }

  /**
   * Report the end of a CDATA section.
   *
   * @throws SAXException The application may raise an exception.
   * @see #startCDATA
   */
  public void endCDATA() throws SAXException
  {
    if (null != m_resultLexicalHandler)
      m_resultLexicalHandler.endCDATA();
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
  public void comment(char ch[], int start, int length) throws SAXException
  {
    flushStartDoc();
    if (null != m_resultLexicalHandler)
      m_resultLexicalHandler.comment(ch, start, length);
  }
  
  // Implement DeclHandler
  
  /**
     * Report an element type declaration.
     *
     * <p>The content model will consist of the string "EMPTY", the
     * string "ANY", or a parenthesised group, optionally followed
     * by an occurrence indicator.  The model will be normalized so
     * that all whitespace is removed,and will include the enclosing
     * parentheses.</p>
     *
     * @param name The element type name.
     * @param model The content model as a normalized string.
     * @exception SAXException The application may raise an exception.
     */
    public void elementDecl (String name, String model)
        throws SAXException
    {
                        if (null != m_resultDeclHandler)
                                m_resultDeclHandler.elementDecl(name, model);
    }


    /**
     * Report an attribute type declaration.
     *
     * <p>Only the effective (first) declaration for an attribute will
     * be reported.  The type will be one of the strings "CDATA",
     * "ID", "IDREF", "IDREFS", "NMTOKEN", "NMTOKENS", "ENTITY",
     * "ENTITIES", or "NOTATION", or a parenthesized token group with 
     * the separator "|" and all whitespace removed.</p>
     *
     * @param eName The name of the associated element.
     * @param aName The name of the attribute.
     * @param type A string representing the attribute type.
     * @param valueDefault A string representing the attribute default
     *        ("#IMPLIED", "#REQUIRED", or "#FIXED") or null if
     *        none of these applies.
     * @param value A string representing the attribute's default value,
     *        or null if there is none.
     * @exception SAXException The application may raise an exception.
     */
    public void attributeDecl (String eName,
                                        String aName,
                                        String type,
                                        String valueDefault,
                                        String value)
        throws SAXException
    {
      if (null != m_resultDeclHandler)
                                m_resultDeclHandler.attributeDecl(eName, aName, type, valueDefault, value);
    }


    /**
     * Report an internal entity declaration.
     *
     * <p>Only the effective (first) declaration for each entity
     * will be reported.</p>
     *
     * @param name The name of the entity.  If it is a parameter
     *        entity, the name will begin with '%'.
     * @param value The replacement text of the entity.
     * @exception SAXException The application may raise an exception.
     * @see #externalEntityDecl
     * @see org.xml.sax.DTDHandler#unparsedEntityDecl
     */
    public void internalEntityDecl (String name, String value)
        throws SAXException
    {
      if (null != m_resultDeclHandler)
                                m_resultDeclHandler.internalEntityDecl(name, value); 
    }


    /**
     * Report a parsed external entity declaration.
     *
     * <p>Only the effective (first) declaration for each entity
     * will be reported.</p>
     *
     * @param name The name of the entity.  If it is a parameter
     *        entity, the name will begin with '%'.
     * @param publicId The declared public identifier of the entity, or
     *        null if none was declared.
     * @param systemId The declared system identifier of the entity.
     * @exception SAXException The application may raise an exception.
     * @see #internalEntityDecl
     * @see org.xml.sax.DTDHandler#unparsedEntityDecl
     */
    public void externalEntityDecl (String name, String publicId,
                                             String systemId)
        throws SAXException
    {
      if (null != m_resultDeclHandler)
                                m_resultDeclHandler.externalEntityDecl(name, publicId, systemId);
    }
  
  /**
   * This is null unless we own the stream.
   */
  private java.io.FileOutputStream m_outputStream = null;

  /** The content handler where result events will be sent. */
  private ContentHandler m_resultContentHandler;

  /** The lexical handler where result events will be sent. */
  private LexicalHandler m_resultLexicalHandler;

  /** The DTD handler where result events will be sent. */
  private DTDHandler m_resultDTDHandler;
  
  /** The Decl handler where result events will be sent. */
  private DeclHandler m_resultDeclHandler;

  /** The Serializer, which may or may not be null. */
  private Serializer m_serializer;

  /** The Result object. */
  private Result m_result;

  /**
   * The system ID, which is unused, but must be returned to fullfill the
   *  TransformerHandler interface.
   */
  private String m_systemID;

  /**
   * The parameters, which is unused, but must be returned to fullfill the
   *  Transformer interface.
   */
  private Hashtable m_params;

  /** The error listener for TrAX errors and warnings. */
  private ErrorListener m_errorListener =
    new org.apache.xml.utils.DefaultErrorHandler();

  /**
   * The URIResolver, which is unused, but must be returned to fullfill the
   *  TransformerHandler interface.
   */
  URIResolver m_URIResolver;

  /** The output properties. */
  private OutputProperties m_outputFormat;

  /** Flag to set if we've found the first element, so we can tell if we have 
   *  to check to see if we should create an HTML serializer.      */
  boolean m_foundFirstElement;
}
