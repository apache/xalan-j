/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights reserved.
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
 * originally based on software copyright (c) 2003, International Business
 * Machines, Inc., http://www.ibm.com.  For more information on the Apache
 * Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.xml.serializer;

import javax.xml.transform.SourceLocator;

import org.xml.sax.SAXException;

/**
 * This interface describes extensions to the SAX ContentHandler interface.
 * It is intended to be used by a serializer. The methods on this interface will
 * implement SAX- like behavior. This allows the gradual collection of
 * information rather than having it all up front. For example the call
 * <pre>
 * startElement(namespaceURI,localName,qName,atts)
 * </pre>
 * could be replaced with the calls
 * <pre>
 * startElement(namespaceURI,localName,qName)
 * addAttributes(atts)
 * </pre>
 * If there are no attributes the second call can be dropped. If attributes are
 * to be added one at a time with calls to
 * <pre>
 * addAttribute(namespaceURI, localName, qName, type, value)
 * </pre>
 */
public interface ExtendedContentHandler extends org.xml.sax.ContentHandler
{
    /**
     * Add at attribute to the current element
     * @param uri the namespace URI of the attribute name
     * @param localName the local name of the attribute (without prefix)
     * @param rawName the qualified name of the attribute
     * @param type the attribute type typically character data (CDATA)
     * @param value the value of the attribute
     * @throws SAXException
     */
    public void addAttribute(
        String uri,
        String localName,
        String rawName,
        String type,
        String value)
        throws SAXException;
    /**
     * Add attributes to the current element
     * @param atts the attributes to add.
     * @throws SAXException
     */
    public void addAttributes(org.xml.sax.Attributes atts)
        throws org.xml.sax.SAXException;
    /**
     * Add an attribute to the current element. The namespace URI of the
     * attribute will be calculated from the prefix of qName. The local name
     * will be derived from qName and the type will be assumed to be "CDATA".
     * @param qName
     * @param value
     */
    public void addAttribute(String qName, String value);
    /**
     * This method is used to notify of a character event, but passing the data
     * as a character String rather than the standard character array.
     * @param chars the character data
     * @throws SAXException
     */
    public void characters(String chars) throws SAXException;
    /**
     * This method is used to notify that an element has ended. Unlike the
     * standard SAX method
     * <pre>
     * endElement(namespaceURI,localName,qName)
     * </pre>
     * only the last parameter is passed. If needed the serializer can derive
     * the localName from the qualified name and derive the namespaceURI from
     * its implementation.
     * @param elemName the fully qualified element name.
     * @throws SAXException
     */
    public void endElement(String elemName) throws SAXException;

    /**
     * This method is used to notify that an element is starting.
     * This method is just like the standard SAX method
     * <pre>
     * startElement(uri,localName,qname,atts)
     * </pre>
     * but without the attributes.
     * @param uri the namespace URI of the element
     * @param localName the local name (without prefix) of the element
     * @param qName the qualified name of the element
     * 
     * @throws SAXException
     */
    public void startElement(String uri, String localName, String qName)
        throws org.xml.sax.SAXException;

    /**
     * This method is used to notify of the start of an element
     * @param qName the fully qualified name of the element
     * @throws SAXException
     */
    public void startElement(String qName) throws SAXException;
    /**
     * This method is used to notify that a prefix mapping is to start, but
     * after an element is started. The SAX method call
     * <pre>
     * startPrefixMapping(prefix,uri)
     * </pre>
     * is used just before an element starts and applies to the element to come,
     * not to the current element.  This method applies to the current element.
     * For example one could make the calls in this order:
     * <pre>
     * startElement("prfx8:elem9")
     * namespaceAfterStartElement("http://namespace8","prfx8")
     * </pre>
     * 
     * @param uri the namespace URI being declared
     * @param prefix the prefix that maps to the given namespace
     * @throws SAXException
     */
    public void namespaceAfterStartElement(String uri, String prefix)
        throws SAXException;

    /**
     * This method is used to notify that a prefix maping is to start, which can
     * be for the current element, or for the one to come.
     * @param prefix the prefix that maps to the given URI
     * @param uri the namespace URI of the given prefix
     * @param shouldFlush if true this call is like the SAX
     * startPrefixMapping(prefix,uri) call and the mapping applies to the
     * element to come.  If false the mapping applies to the current element.
     * @return boolean false if the prefix mapping was already in effect (in
     * other words we are just re-declaring), true if this is a new, never
     * before seen mapping for the element.
     * @throws SAXException
     */
    public boolean startPrefixMapping(
        String prefix,
        String uri,
        boolean shouldFlush)
        throws SAXException;
    /**
     * Notify of an entity reference.
     * @param entityName the name of the entity
     * @throws SAXException
     */
    public void entityReference(String entityName) throws SAXException;

    /**
     * This method returns an object that has the current namespace mappings in
     * effect.
     * 
     * @return NamespaceMappings an object that has the current namespace
     * mappings in effect.
     */
    public NamespaceMappings getNamespaceMappings();
    /**
     * This method returns the prefix that currently maps to the given namespace
     * URI.
     * @param uri the namespace URI
     * @return String the prefix that currently maps to the given URI.
     */
    public String getPrefix(String uri);
    /**
     * This method gets the prefix associated with a current element or
     * attribute name.
     * @param name the qualified name of an element, or attribute
     * @param isElement true if it is an element name, false if it is an
     * atttribute name
     * @return String the namespace URI associated with the element or
     * attribute.
     */
    public String getNamespaceURI(String name, boolean isElement);
    /**
     * This method returns the namespace URI currently associated with the
     * prefix.
     * @param prefix a prefix of an element or attribute.
     * @return String the namespace URI currently associated with the prefix.
     */
    public String getNamespaceURIFromPrefix(String prefix);

    /**
     * This method is used to set the source locator, which might be used to
     * generated an error message.
     * @param locator the source locator
     */
    public void setSourceLocator(SourceLocator locator);

}
