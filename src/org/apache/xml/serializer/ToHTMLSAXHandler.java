/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES{} LOSS OF
 * USE, DATA, OR PROFITS{} OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 2001, Sun
 * Microsystems., http://www.sun.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * @author Santiago Pericas-Geertsen
 * @author G. Todd Miller 
 *
 */

package org.apache.xml.serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Properties;

import javax.xml.transform.Result;

import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * This class accepts SAX-like calls, then sends true SAX calls to a
 * wrapped SAX handler.  There is optimization done knowing that the ultimate
 * output is HTML.
 */
public class ToHTMLSAXHandler extends ToSAXHandler
{

    /**
     * Keeps track of whether output escaping is currently enabled
     */
    protected boolean m_escapeSetting = false;

    /**
     * Returns null.
     * @return null
     * @see org.apache.xml.serializer.Serializer#getOutputFormat()
     */
    public Properties getOutputFormat()
    {
        return null;
    }

    /**
     * Reurns null
     * @return null
     * @see org.apache.xml.serializer.Serializer#getOutputStream()
     */
    public OutputStream getOutputStream()
    {
        return null;
    }

    /**
     * Returns null
     * @return null
     * @see org.apache.xml.serializer.Serializer#getWriter()
     */
    public Writer getWriter()
    {
        return null;
    }

    /**
     * Does nothing.
     * @see org.apache.xml.serializer.SerializationHandler#indent(int)
     */
    public void indent(int n) throws SAXException
    {
    }

    /**
     * Returns false
     * @return false
     * @see org.apache.xml.serializer.Serializer#reset()
     */
    public boolean reset()
    {
        return false;
    }

    /**
     * Does nothing.
     * @see org.apache.xml.serializer.DOMSerializer#serialize(Node)
     */
    public void serialize(Node node) throws IOException
    {
        return;
    }

    /**
     * Turns special character escaping on/off.
     *
     *
     * @param excape true if escaping is to be set on.
     *
     * @see org.apache.xml.serializer.SerializationHandler#setEscaping(boolean)
     */
    public boolean setEscaping(boolean escape) throws SAXException
    {
        boolean oldEscapeSetting = m_escapeSetting;
        m_escapeSetting = escape;

        if (escape) {
            processingInstruction(Result.PI_ENABLE_OUTPUT_ESCAPING, "");
        } else {
            processingInstruction(Result.PI_DISABLE_OUTPUT_ESCAPING, "");
        }

        return oldEscapeSetting;
    }

    /**
     * Does nothing
     * @param indent the number of spaces to indent per indentation level
     * (ignored)
     * @see org.apache.xml.serializer.SerializationHandler#setIndent(boolean)
     */
    public void setIndent(boolean indent)
    {
    }

    /**
     * Does nothing.
     * @param format this parameter is not used
     * @see org.apache.xml.serializer.Serializer#setOutputFormat(Properties)
     */
    public void setOutputFormat(Properties format)
    {
    }

    /**
     * Does nothing.
     * @param output this parameter is ignored
     * @see org.apache.xml.serializer.Serializer#setOutputStream(OutputStream)
     */
    public void setOutputStream(OutputStream output)
    {
    }


    /**
     * Does nothing.
     * @param writer this parameter is ignored.
     * @see org.apache.xml.serializer.Serializer#setWriter(Writer)
     */
    public void setWriter(Writer writer)
    {
    }

    /**
     * @see org.xml.sax.ext.DeclHandler#attributeDecl(String, String, String, String, String)
     */
    /**
     * Does nothing.
     *
     * @param eName this parameter is ignored
     * @param aName this parameter is ignored
     * @param type this parameter is ignored
     * @param valueDefault this parameter is ignored
     * @param value this parameter is ignored
     * @see org.xml.sax.ext.DeclHandler#attributeDecl(String, String, String,String,String)
     */    
    public void attributeDecl(
        String eName,
        String aName,
        String type,
        String valueDefault,
        String value)
        throws SAXException
    {
    }


    /**
     * Does nothing.
     * @see org.xml.sax.ext.DeclHandler#elementDecl(String, String)
     */    
    public void elementDecl(String name, String model) throws SAXException
    {
        return;
    }

    /**
     * @see org.xml.sax.ext.DeclHandler#externalEntityDecl(String, String, String)
     */
    public void externalEntityDecl(String arg0, String arg1, String arg2)
        throws SAXException
    {
    }

    /**
     * Does nothing.
     *
     * @see org.xml.sax.DTDHandler#unparsedEntityDecl
     */
    public void internalEntityDecl(String name, String value)
        throws SAXException
    {
    }

    /**
     * Receive notification of the end of an element.
     *
     * <p>The SAX parser will invoke this method at the end of every
     * element in the XML document; there will be a corresponding
     * startElement() event for every endElement() event (even when the
     * element is empty).</p>
     *
     * <p>If the element name has a namespace prefix, the prefix will
     * still be attached to the name.</p>
     *
     *
     * @param namespaceURI The Namespace URI, or the empty string if the
     *        element has no Namespace URI or if Namespace
     *        processing is not being performed.
     * @param localName The local name (without prefix), or the
     *        empty string if Namespace processing is not being
     *        performed.
     * @param name The qualified name (with prefix), or the
     *        empty string if qualified names are not available.
     * @param name The element type name
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.ContentHandler#endElement(String, String, String)
     */
    public void endElement(String uri, String localName, String qName)
        throws SAXException
    {
        flushPending();
        m_saxHandler.endElement(uri, localName, qName);
        
        // time to fire off endElement event
        super.fireEndElem(qName);
    }

    /**
     * Does nothing.
     */
    public void endPrefixMapping(String prefix) throws SAXException
    {
    }

    /**
     * Does nothing.
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    public void ignorableWhitespace(char[] ch, int start, int length)
        throws SAXException
    {
    }
    
    /**
     * Receive notification of a processing instruction.
     *
     * <p>The Parser will invoke this method once for each processing
     * instruction found: note that processing instructions may occur
     * before or after the main document element.</p>
     *
     * <p>A SAX parser should never report an XML declaration (XML 1.0,
     * section 2.8) or a text declaration (XML 1.0, section 4.3.1)
     * using this method.</p>
     *
     * @param target The processing instruction target.
     * @param data The processing instruction data, or null if
     *        none was supplied.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     *
     * @throws org.xml.sax.SAXException
     * @see org.xml.sax.ContentHandler#processingInstruction(String, String)
     */
    public void processingInstruction(String arg0, String arg1)
        throws SAXException
    {
        flushPending();
        m_saxHandler.processingInstruction(arg0,arg1);

		// time to fire off processing instruction event
		super.fireEscapingEvent(arg0,arg1);        
    }

    /**
     * Does nothing.  
     * @see org.xml.sax.ContentHandler#setDocumentLocator(Locator)
     */
    public void setDocumentLocator(Locator arg0)
    {
        // do nothing
    }

    /**
     * Does nothing.
     * @see org.xml.sax.ContentHandler#skippedEntity(String)
     */
    public void skippedEntity(String arg0) throws SAXException
    {
    }

    /**
     * Receive notification of the beginning of an element, although this is a
     * SAX method additional namespace or attribute information can occur before
     * or after this call, that is associated with this element.
     *
     *
     * @param namespaceURI The Namespace URI, or the empty string if the
     *        element has no Namespace URI or if Namespace
     *        processing is not being performed.
     * @param localName The local name (without prefix), or the
     *        empty string if Namespace processing is not being
     *        performed.
     * @param qName The elements name.
     * @param atts The attributes attached to the element, if any.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.ContentHandler#startElement
     * @see org.xml.sax.ContentHandler#endElement
     * @see org.xml.sax.AttributeList
     *
     * @throws org.xml.sax.SAXException
     *
     * @see org.xml.sax.ContentHandler#startElement(String, String, String, Attributes)
     */
    public void startElement(
        String namespaceURI,
        String localName,
        String qName,
        Attributes atts)
        throws SAXException
    {
        flushPending();
        super.startElement(namespaceURI, localName, qName, atts);
        m_saxHandler.startElement(namespaceURI, localName, qName, atts);
        m_startTagOpen = false;
    }

    /**
     * Receive notification of a comment anywhere in the document. This callback
     * will be used for comments inside or outside the document element.
     * @param ch An array holding the characters in the comment.
     * @param start The starting position in the array.
     * @param length The number of characters to use from the array.
     * @throws org.xml.sax.SAXException The application may raise an exception.
     *
     * @see org.xml.sax.ext.LexicalHandler#comment(char[], int, int)
     */
    public void comment(char[] ch, int start, int length) throws SAXException
    {
        flushPending();
        m_lexHandler.comment(ch, start, length);

        // time to fire off comment event
        super.fireCommentEvent(ch, start, length);
        return;
    }

    /**
     * Does nothing.
     * @see org.xml.sax.ext.LexicalHandler#endCDATA()
     */
    public void endCDATA() throws SAXException
    {
        return;
    }

    /**
     * Does nothing.
     * @see org.xml.sax.ext.LexicalHandler#endDTD()
     */
    public void endDTD() throws SAXException
    {
    }

    /**
     * Does nothing.
     * @see org.xml.sax.ext.LexicalHandler#startCDATA()
     */
    public void startCDATA() throws SAXException
    {
    }

    /**
     * Does nothing.
     * @see org.xml.sax.ext.LexicalHandler#startEntity(String)
     */
    public void startEntity(String arg0) throws SAXException
    {
    }

    /**
     * Receive notification of the end of a document.
     *
     * <p>The SAX parser will invoke this method only once, and it will
     * be the last method invoked during the parse.  The parser shall
     * not invoke this method until it has either abandoned parsing
     * (because of an unrecoverable error) or reached the end of
     * input.</p>
     *
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     *
     * @throws org.xml.sax.SAXException
     *
     * @see org.xml.sax.ContentHander#endDocument()
     */
    public void endDocument() throws SAXException
    {
        flushPending();

        // Close output document
        m_saxHandler.endDocument();

        super.fireEndDoc();        
    }

    /**
     * This method is called when all the data needed for a call to the
     * SAX handler's startElement() method has been gathered.
     */
    protected void closeStartTag() throws SAXException
    {

        m_startTagOpen = false;

        // Now is time to send the startElement event
        m_saxHandler.startElement(
            EMPTYSTRING,
            m_elementName,
            m_elementName,
            m_attributes);
        m_attributes.clear();       

    }

    /**
     * Do nothing.
     * @see org.apache.xml.serializer.SerializationHandler#close()
     */
    public void close()
    {
        return;
    }

    /**
     * Receive notification of character data.
     *
     * @param chars The string of characters to process.
     *
     * @throws org.xml.sax.SAXException
     *
     * @see org.apache.xml.serializer.ExtendedContentHandler#characters(String)
     */
    public void characters(String chars) throws SAXException
    {
        this.characters(chars.toCharArray(), 0, chars.length());
        return;
    }


    /**
     * A constructor
     * @param handler the wrapped SAX content handler
     * @param encoding the encoding of the output HTML document
     */
    public ToHTMLSAXHandler(ContentHandler handler, String encoding)
    {
        super(handler,encoding);
    }
    /**
     * A constructor.
     * @param handler the wrapped SAX content handler
     * @param lex the wrapped lexical handler
     * @param encoding the encoding of the output HTML document
     */
    public ToHTMLSAXHandler(
        ContentHandler handler,
        LexicalHandler lex,
        String encoding)
    {
        super(handler,lex,encoding);
    }

    /**
     * An element starts, but attributes are not fully known yet.
     *
     * @param elementNamespaceURI the URI of the namespace of the element
     * (optional)
     * @param elementLocalName the element name, but without prefix
     * (optional)
     * @param elementName the element name, with prefix, if any (required)
     *
     * @see org.apache.xml.serializer.ExtendedContentHandler#startElement(String)
     */
    public void startElement(
        String elementNamespaceURI,
        String elementLocalName,
        String elementName) throws SAXException
    {

        super.startElement(elementNamespaceURI, elementLocalName, elementName);

        flushPending();

        // Handle document type declaration (for first element only)
        if (m_lexHandler != null)
        {
            String doctypeSystem = getDoctypeSystem();
            String doctypePublic = getDoctypePublic();
            if ((doctypeSystem != null) || (doctypePublic != null))
                m_lexHandler.startDTD(
                    elementName,
                    doctypePublic,
                    doctypeSystem);
            m_lexHandler = null;
        }

        m_currentElemDepth++;
        m_elementName = elementName;         // required
        m_elementLocalName = elementLocalName; // possibly null (optional)
        m_elementURI = elementNamespaceURI;  // possibly null (optional)
//        m_attributes.clear();
        m_startTagOpen = true;

    }
    /**
     * An element starts, but attributes are not fully known yet.
     *
     * @param elementName the element name, with prefix, if any
     *
     * @see org.apache.xml.serializer.ExtendedContentHandler#startElement(String)
     */
    public void startElement(String elementName) throws SAXException
    {
        this.startElement(null,null, elementName);
    }
    
    /**
     * Receive notification of the end of an element.
     * @param elementName The element type name
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *     wrapping another exception.
     *
     * @see org.apache.xml.serializer.ExtendedContentHandler#endElement(String)
     */
    public void endElement(String elementName) throws SAXException
    {
        flushPending();
        m_saxHandler.endElement(EMPTYSTRING, elementName, elementName);

        // time to fire off endElement event
        super.fireEndElem(elementName);        
    }

    /**
     * Receive notification of character data.
     *
     * <p>The Parser will call this method to report each chunk of
     * character data.  SAX parsers may return all contiguous character
     * data in a single chunk, or they may split it into several
     * chunks; however, all of the characters in any single event
     * must come from the same external entity, so that the Locator
     * provides useful information.</p>
     *
     * <p>The application must not attempt to read from the array
     * outside of the specified range.</p>
     *
     * <p>Note that some parsers will report whitespace using the
     * ignorableWhitespace() method rather than this one (validating
     * parsers must do so).</p>
     *
     * @param ch The characters from the XML document.
     * @param off The start position in the array.
     * @param len The number of characters to read from the array.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see #ignorableWhitespace
     * @see org.xml.sax.Locator
     *
     * @throws org.xml.sax.SAXException
     *
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int off, int len) throws SAXException
    {

        flushPending();
        m_saxHandler.characters(ch, off, len);

        // time to fire off characters event
        super.fireCharEvent(ch, off, len);        
    }

    /**
     * This method flushes any pending events, which can be startDocument()
     * closing the opening tag of an element, or closing an open CDATA section.
     */
    public void flushPending()
    {
		if (m_needToCallStartDocument)
		{
			try {startDocumentInternal();
			m_needToCallStartDocument = false;
			} catch (SAXException e) {}
		}       	
        // Close any open element
        if (m_startTagOpen)
        {
            try
            {
                closeStartTag();
            }
            catch(SAXException se)
            {
                // do something ??
            }
            m_startTagOpen = false;
        }
    }
    /**
     * Handle a prefix/uri mapping, which is associated with a startElement()
     * that is soon to follow. Need to close any open start tag to make
     * sure than any name space attributes due to this event are associated wih
     * the up comming element, not the current one.
     * @see org.xml.sax.ExtendedContentHandler#startPrefixMapping
     *
     * @param prefix The Namespace prefix being declared.
     * @param uri The Namespace URI the prefix is mapped to.
     * @param shouldFlush true if any open tags need to be closed first, this
     * will impact which element the mapping applies to (open parent, or its up
     * comming child)
     * @return returns true if the call made a change to the current
     * namespace information, false if it did not change anything, e.g. if the
     * prefix/namespace mapping was already in scope from before.
     *
     * @throws org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     */    
    public boolean startPrefixMapping(
        String prefix,
        String uri,
        boolean shouldFlush)
        throws SAXException
    {
        // no namespace support for HTML
        if (shouldFlush) 
            flushPending();   
        m_saxHandler.startPrefixMapping(prefix,uri);
        return false;
    }

    /**
     * Begin the scope of a prefix-URI Namespace mapping
     * just before another element is about to start.
     * This call will close any open tags so that the prefix mapping
     * will not apply to the current element, but the up comming child.
     *
     * @see org.xml.sax.ContentHandler#startPrefixMapping
     *
     * @param prefix The Namespace prefix being declared.
     * @param uri The Namespace URI the prefix is mapped to.
     *
     * @throws org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     *
     */
    public void startPrefixMapping(String prefix, String uri)
        throws org.xml.sax.SAXException
    {
        startPrefixMapping(prefix,uri,true);        
    }

    /**
     * This method is used when a prefix/uri namespace mapping
     * is indicated after the element was started with a
     * startElement() and before and endElement().
     * startPrefixMapping(prefix,uri) would be used before the
     * startElement() call.
     * @param prefix the prefix associated with the given URI.
     * @param uri the URI of the namespace
     *
     * @see org.apache.xml.serializer.ExtendedContentHandler#namespaceAfterStartElement(String, String)
     */
    public void namespaceAfterStartElement(
        final String prefix,
        final String uri)
        throws SAXException
    {
        // hack for XSLTC with finding URI for default namespace
        if (m_elementURI == null)
        {
            String prefix1 = getPrefixPart(m_elementName);
            if (prefix1 == null && EMPTYSTRING.equals(prefix))
            {
                // the elements URI is not known yet, and it
                // doesn't have a prefix, and we are currently
                // setting the uri for prefix "", so we have
                // the uri for the element... lets remember it
                m_elementURI = uri;
            }
        }       
        startPrefixMapping(prefix,uri,false);
    }
}
