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
package org.apache.xalan.processor;

import javax.xml.transform.TransformerException;
import org.xml.sax.Attributes;
import org.apache.xalan.templates.Stylesheet;
import org.apache.xalan.templates.WhiteSpaceInfo;
import org.apache.xpath.XPath;

import java.util.Vector;

/**
 * TransformerFactory for xsl:preserve-space markup.
 * <pre>
 * <!ELEMENT xsl:preserve-space EMPTY>
 * <!ATTLIST xsl:preserve-space elements CDATA #REQUIRED>
 * </pre>
 */
class ProcessorPreserveSpace extends XSLTElementProcessor
{

  /**
   * Bean property to allow setPropertiesFromAttributes to
   * get the elements attribute.
   */
  private Vector m_elements;

  /**
   * Set from the elements attribute.  This is a list of 
   * whitespace delimited element qualified names that specify
   * preservation of whitespace.
   *
   * @param elems Should be a non-null reference to a list 
   *              of {@link org.apache.xpath.XPath} objects.
   */
  public void setElements(Vector elems)
  {
    m_elements = elems;
  }

  /**
   * Get the property set by setElements().  This is a list of 
   * whitespace delimited element qualified names that specify
   * preservation of whitespace.
   *
   * @return A reference to a list of {@link org.apache.xpath.XPath} objects, 
   *         or null.
   */
  Vector getElements()
  {
    return m_elements;
  }

  /**
   * Receive notification of the start of an preserve-space element.
   *
   * @param handler The calling StylesheetHandler/TemplatesBuilder.
   * @param uri The Namespace URI, or the empty string if the
   *        element has no Namespace URI or if Namespace
   *        processing is not being performed.
   * @param localName The local name (without prefix), or the
   *        empty string if Namespace processing is not being
   *        performed.
   * @param rawName The raw XML 1.0 name (with prefix), or the
   *        empty string if raw names are not available.
   * @param attributes The attributes attached to the element.  If
   *        there are no attributes, it shall be an empty
   *        Attributes object.
   */
  public void startElement(
          StylesheetHandler handler, String uri, String localName, String rawName, Attributes attributes)
            throws org.xml.sax.SAXException
  {

    setPropertiesFromAttributes(handler, rawName, attributes, this);

    Stylesheet thisSheet = handler.getStylesheet();
    Vector xpaths = getElements();

    for (int i = 0; i < xpaths.size(); i++)
    {
      WhiteSpaceInfo wsi = new WhiteSpaceInfo((XPath) xpaths.elementAt(i), false, thisSheet);
      wsi.setUid(handler.nextUid());

      thisSheet.setPreserveSpaces(wsi);
    }
  }
}
