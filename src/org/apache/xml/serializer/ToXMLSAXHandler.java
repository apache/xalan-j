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
 * This class receives notification of SAX-like events, and with gathered
 * information over these calls it will invoke the equivalent SAX methods
 * on a handler, the ultimate output is known to be XML.
 * 
 * @author minchau
 */
public class ToXMLSAXHandler extends ToSAXHandler
{

    /**
     * Keeps track of whether output escaping is currently enabled
     */
    protected boolean m_escapeSetting = false;

    public ToXMLSAXHandler()
    {
        // default constructor (need to set content handler ASAP !)
        m_prefixMap = new NamespaceMappings();
        initCDATA();
    }

    /**
     * @see org.apache.xml.serializer.Serializer#getOutputFormat()
     */
    public Properties getOutputFormat()
    {
        return null;
    }

    /**
     * @see org.apache.xml.serializer.Serializer#getOutputStream()
     */
    public OutputStream getOutputStream()
    {
        return null;
    }

    /**
     * @see org.apache.xml.serializer.Serializer#getWriter()
     */
    public Writer getWriter()
    {
        return null;
    }

    /**
     * @see org.apache.xml.serializer.SerializationHandler#indent(int)
     */
    public void indent(int n) throws SAXException
    {
    }

    /**
     * @see org.apache.xml.serializer.Serializer#reset()
     */
    public boolean reset()
    {
        return false;
    }

    /**
     * @see org.apache.xml.serializer.DOMSerializer#serialize(Node)
     */
    public void serialize(Node node) throws IOException
    {
    }

    /**
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
     * @see org.apache.xml.serializer.Serializer#setOutputFormat(Properties)
     */
    public void setOutputFormat(Properties format)
    {
    }

    /**
     * @see org.apache.xml.serializer.Serializer#setOutputStream(OutputStream)
     */
    public void setOutputStream(OutputStream output)
    {
    }

    /**
     * @see org.apache.xml.serializer.Serializer#setWriter(Writer)
     */
    public void setWriter(Writer writer)
    {
    }

    /**
     * @see org.xml.sax.ext.DeclHandler#attributeDecl(String, String, String, String, String)
     */
    public void attributeDecl(
        String arg0,
        String arg1,
        String arg2,
        String arg3,
        String arg4)
        throws SAXException
    {
    }

    /**
     * @see org.xml.sax.ext.DeclHandler#elementDecl(String, String)
     */
    public void elementDecl(String arg0, String arg1) throws SAXException
    {
    }

    /**
     * @see org.xml.sax.ext.DeclHandler#externalEntityDecl(String, String, String)
     */
    public void externalEntityDecl(String arg0, String arg1, String arg2)
        throws SAXException
    {
    }

    /**
     * @see org.xml.sax.ext.DeclHandler#internalEntityDecl(String, String)
     */
    public void internalEntityDecl(String arg0, String arg1)
        throws SAXException
    {
    }

    /**
     * Receives notification of the end of the document.
     * @see org.xml.sax.ContentHandler#endDocument()
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

        final String localName = getLocalName(m_elementName);
        final String uri = getNamespaceURI(m_elementName, true);

        // Now is time to send the startElement event
        if (m_needToCallStartDocument)
        {
            startDocumentInternal();
        }
        m_saxHandler.startElement(uri, localName, m_elementName, m_attributes);
        // we've sent the official SAX attributes on their way,
        // now we don't need them anymore.
        m_attributes.clear();

        if(m_state != null)
          m_state.setCurrentNode(null);

        pushCdataSectionState();

    }

    /**
     * Closes the open cdata tag
     */
    public void closeCDATA() throws SAXException
    {

        // Output closing bracket - "]]>"
        m_saxHandler.characters(ENDCDATA, 0, ENDCDATA.length);
        m_cdataTagOpen = false;
    }

    /**
     * @see org.xml.sax.ContentHandler#endElement(String, String, String)
     */
    public void endElement(String namespaceURI, String localName, String qName)
        throws SAXException
    {
        // Close any open elements etc.
        flushPending();
        
        if (namespaceURI == null)
        {
            if (m_elementURI != null)
                namespaceURI = m_elementURI;
            else
                namespaceURI = getNamespaceURI(qName, true);
        }
        
        if (localName == null)
        {
            if (m_elementLocalName != null)
                localName = m_elementLocalName;
            else
                localName = getLocalName(qName);
        }

        m_saxHandler.endElement(namespaceURI, localName, qName);

		super.fireEndElem(qName);       

        /* Pop all namespaces at the current element depth.
         * We are not waiting for official endPrefixMapping() calls.
         */
        m_prefixMap.popNamespaces(m_currentElemDepth);
        m_currentElemDepth--;
        m_startTagOpen = false;
        // m_disableOutputEscapingStates.pop();
        m_cdataSectionStates.pop();
    }

    /**
     * @see org.xml.sax.ContentHandler#endPrefixMapping(String)
     */
    public void endPrefixMapping(String prefix) throws SAXException
    {
        /* poping all prefix mappings should have been done
         * in endElement() already
         */
         return;
    }

    /**
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
        throws SAXException
    {
        m_saxHandler.ignorableWhitespace(arg0,arg1,arg2);
    }

    /**
     * @see org.xml.sax.ContentHandler#setDocumentLocator(Locator)
     */
    public void setDocumentLocator(Locator arg0)
    {
        m_saxHandler.setDocumentLocator(arg0);
    }

    /**
     * @see org.xml.sax.ContentHandler#skippedEntity(String)
     */
    public void skippedEntity(String arg0) throws SAXException
    {
        m_saxHandler.skippedEntity(arg0);
    }

    /**
     * @see org.xml.sax.ContentHandler#startPrefixMapping(String, String)
     * @ param prefix The prefix that maps to the URI
     * @ param uri The URI for the namespace
     */
    public void startPrefixMapping(String prefix, String uri)
        throws SAXException
    {
       startPrefixMapping(prefix, uri, true);
    }

    /**
     * Remember the prefix/uri mapping at the current nested element depth.
     *
     * @see org.xml.sax.ContentHandler#startPrefixMapping(String, String)
     * @param prefix The prefix that maps to the URI
     * @param uri The URI for the namespace
     * @param shouldFlush a flag indicating if the mapping applies to the
     * current element or an up coming child (not used).
     */

    public boolean startPrefixMapping(
        String prefix,
        String uri,
        boolean shouldFlush)
        throws org.xml.sax.SAXException
    {

        /* Remember the mapping, and at what depth it was declared
         * This is one greater than the current depth because these
         * mappings will apply to the next depth. This is in
         * consideration that startElement() will soon be called
         */

        boolean pushed;
        int pushDepth;
        if (shouldFlush)
        {
            flushPending();
            // the prefix mapping applies to the child element (one deeper)
            pushDepth = m_currentElemDepth + 1;
        }
        else
        {
            // the prefix mapping applies to the current element
            pushDepth = m_currentElemDepth;
        }
        pushed = m_prefixMap.pushNamespace(prefix, uri, pushDepth);

        if (pushed)
        {
            m_saxHandler.startPrefixMapping(prefix,uri);
            
            /* bjm: don't know if we really needto do this. The
             * callers of this object should have injected both
             * startPrefixMapping and the attributes.  We are
             * just covering our butt here.
             */
            String name;
            if (EMPTYSTRING.equals(prefix))
            {
                name = "xmlns";
                addAttributeAlways(XMLNS_URI, prefix, name,"CDATA",uri);
            }
            else {
                if (!EMPTYSTRING.equals(uri)) // hack for XSLTC attribset16 test
                {                             // that maps ns1 prefix to "" URI 
                    name = "xmlns:" + prefix;

                    /* for something like xmlns:abc="w3.pretend.org"
             	 	 *  the      uri is the value, that is why we pass it in the
             	 	 * value, or 5th slot of addAttributeAlways()
                 	 */
                    addAttributeAlways(XMLNS_URI, prefix, name,"CDATA",uri);
                }
            }
        }
        return pushed;
    }
        

    /**
     * @see org.xml.sax.ext.LexicalHandler#comment(char[], int, int)
     */
    public void comment(char[] arg0, int arg1, int arg2) throws SAXException
    {
        flushPending();
        if (m_lexHandler != null)
            m_lexHandler.comment(arg0, arg1, arg2);
            
        super.fireCommentEvent(arg0, arg1, arg2);
    }

    /**
     * @see org.xml.sax.ext.LexicalHandler#endCDATA()
     */
    public void endCDATA() throws SAXException
    {
        if (m_lexHandler != null)
            m_lexHandler.endCDATA();
    }

    /**
     * @see org.xml.sax.ext.LexicalHandler#endDTD()
     */
    public void endDTD() throws SAXException
    {
        if (m_lexHandler != null)
            m_lexHandler.endDTD();
    }

    /**
     * @see org.xml.sax.ext.LexicalHandler#startEntity(String)
     */
    public void startEntity(String arg0) throws SAXException
    {
        if (m_lexHandler != null)
            m_lexHandler.startEntity(arg0);
    }

    /**
     * @see org.apache.xml.serializer.ExtendedContentHandler#characters(String)
     */
    public void characters(String chars) throws SAXException
    {
        final char[] arr = chars.toCharArray();
        this.characters(arr,0,arr.length);
    }

    /////////////////// from XSLTC //////////////
    public ToXMLSAXHandler(ContentHandler handler, String encoding)
    {
        super(handler, encoding);

        initCDATA();
        // initNamespaces();
        m_prefixMap = new NamespaceMappings();
    }

    public ToXMLSAXHandler(
        ContentHandler handler,
        LexicalHandler lex,
        String encoding)
    {
        super(handler, lex, encoding);

        initCDATA();
        //      initNamespaces();
        m_prefixMap = new NamespaceMappings();
    }

    /**
     * Start an element in the output document. This might be an XML element
     * (<elem>data</elem> type) or a CDATA section.
     */
    public void startElement(
    String elementNamespaceURI,
    String elementLocalName,
    String elementName) throws SAXException
    {
        startElement(
            elementNamespaceURI,elementLocalName,elementName, null);


    }
    public void startElement(String elementName) throws SAXException
    {
        startElement(null, null, elementName, null);
    }


    public void characters(char[] ch, int off, int len) throws SAXException
    {
        // System.out.println("SAXXMLOutput.characters ch = " + new String(ch, off, len));

        flushPending();

        if (m_cdataSectionStates.peekOrFalse())
        {
            startCDATA(ch, off, len);
        }
        else
        {
            m_saxHandler.characters(ch, off, len);
        }

		// time to generate characters event
		fireCharEvent(ch, off, len);
    }
    

    /**
     * @see org.apache.xml.serializer.ExtendedContentHandler#endElement(String)
     */
    public void endElement(String elemName) throws SAXException
    {
        endElement(null, null, elemName);
    }    


    /**
     * Send a namespace declaration in the output document. The namespace
     * declaration will not be include if the namespace is already in scope
     * with the same prefix.
     */
    public void namespaceAfterStartElement(
        final String prefix,
        final String uri)
        throws SAXException
    {
        startPrefixMapping(prefix,uri,false);
    }

    /**
     *
     * @see org.xml.sax.ContentHandler#processingInstruction(String, String)
     * Send a processing instruction to the output document
     */
    public void processingInstruction(String target, String data)
        throws SAXException
    {
        flushPending();

        // Pass the processing instruction to the SAX handler
        m_saxHandler.processingInstruction(target, data);

        // we don't want to leave serializer to fire off this event,
        // so do it here.
        super.fireEscapingEvent(target, data);        
    }

    /**
     * Undeclare the namespace that is currently pointed to by a given
     * prefix. Inform SAX handler if prefix was previously mapped.
     */
    protected boolean popNamespace(String prefix)
    {
        try
        {
            if (m_prefixMap.popNamespace(prefix))
            {
                m_saxHandler.endPrefixMapping(prefix);
                return true;
            }
        }
        catch (SAXException e)
        {
            // falls through
        }
        return false;
    }

    public void startCDATA() throws SAXException
    {

        // Output start bracket - "<![CDATA["
        m_saxHandler.characters(BEGCDATA, 0, BEGCDATA.length);
        m_cdataTagOpen = true;

    }

    /**
     * Utility method - pass a whole charactes as CDATA to SAX handler
     */
    private void startCDATA(char[] ch, int off, int len) throws SAXException
    {
        final int limit = off + len;
        int offset = off;

        // Output start bracket - "<![CDATA["
        m_saxHandler.characters(BEGCDATA, 0, BEGCDATA.length);

        // Detect any occurence of "]]>" in the character array
        for (int i = offset; i < limit - 2; i++)
        {
            if (ch[i] == ']' && ch[i + 1] == ']' && ch[i + 2] == '>')
            {
                m_saxHandler.characters(ch, offset, i - offset);
                m_saxHandler.characters(CNTCDATA, 0, CNTCDATA.length);
                offset = i + 3;
                i += 2; // Skip next chars ']' and '>'
            }
        }

        // Output the remaining characters
        if (offset < limit)
        {
            m_saxHandler.characters(ch, offset, limit - offset);
        }
        m_cdataTagOpen = true;
    }

    /**
    * @see org.xml.sax.ContentHandler#startElement(String, String, String, Attributes)
    */
    public void startElement(
    String namespaceURI,
    String localName,
    String name,
    Attributes atts)
        throws SAXException
    {
        flushPending();
        super.startElement(namespaceURI, localName, name, atts);

        // Handle document type declaration (for first element only)
         if (m_needToOutputDocTypeDecl)
         {
             String doctypeSystem = getDoctypeSystem();
             if (doctypeSystem != null && m_lexHandler != null)
             {
                 String doctypePublic = getDoctypePublic();
                 if (doctypeSystem != null)
                     m_lexHandler.startDTD(
                         name,
                         doctypePublic,
                         doctypeSystem);
             }
             m_needToOutputDocTypeDecl = false;
         }

        m_currentElemDepth++; // current element is one element deeper

        // ensurePrefixIsDeclared depends on the current depth, so
        // the previous increment is necessary where it is.
        if (namespaceURI != null)
            ensurePrefixIsDeclared(namespaceURI, name);

        // remember for later
        m_elementLocalName = localName;  // null if not known yet
        m_elementURI = namespaceURI;     // null if not known yet
        m_elementName = name;            // required 

        // add the attributes to the collected ones
        if (atts != null)
            addAttributes(atts);

        m_startTagOpen = true;
         
        // do we really need this CDATA section state?
        pushCdataSectionState();
   
    }
 
    private void ensurePrefixIsDeclared(String ns, String rawName)
        throws org.xml.sax.SAXException
    {

        if (ns != null && ns.length() > 0)
        {
            int index;
            String prefix =
                (index = rawName.indexOf(":")) < 0
                    ? ""
                    : rawName.substring(0, index);

            if (null != prefix)
            {
                String foundURI = m_prefixMap.lookupNamespace(prefix);

                if ((null == foundURI) || !foundURI.equals(ns))
                {
                    this.startPrefixMapping(prefix, ns, false);

                    // Bugzilla1133: Generate attribute as well as namespace event.
                    // SAX does expect both.
                    this.addAttributeAlways(
                        "http://www.w3.org/2000/xmlns/",
                        prefix,
                        "xmlns" + (prefix.length() == 0 ? "" : ":") + prefix,
                        "CDATA",
                        ns);
                }

            }
        }
    }
    /**
     * Adds the given attribute to the set of attributes, and also makes sure
     * that the needed prefix/uri mapping is declared, but only if there is a
     * currently open element.
     * 
     * @param uri the URI of the attribute
     * @param localName the local name of the attribute
     * @param rawName    the qualified name of the attribute
     * @param type the type of the attribute (probably CDATA)
     * @param value the value of the attribute
     * @see org.apache.xml.serializer.ExtendedContentHandler#addAttribute(String, String, String, String, String)
     */
    public void addAttribute(
        String uri,
        String localName,
        String rawName,
        String type,
        String value)
        throws SAXException
    {      
        if (m_startTagOpen)
        {
            ensurePrefixIsDeclared(uri, rawName);
            addAttributeAlways(uri, localName, rawName, type, value);
        }

    }    

}
