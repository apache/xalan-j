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
package org.apache.xalan.stree;

import org.w3c.dom.Node;
import org.w3c.dom.Text;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.apache.xalan.utils.FastStringBuffer;

/**
 * <meta name="usage" content="internal"/>
 * NEEDSDOC Class TextImpl <needs-comment/>
 */
public class TextImpl extends Child implements Text, SaxEventDispatch
{

  /** NEEDSDOC Field m_data          */
  protected String m_data;

  /** NEEDSDOC Field m_start          */
  protected int m_start;

  /** NEEDSDOC Field m_length          */
  protected int m_length;

  /**
   * Constructor TextImpl
   *
   *
   * NEEDSDOC @param doc
   * NEEDSDOC @param data
   */
  public TextImpl(DocumentImpl doc, String data)
  {

    super(doc);

    m_data = data;
    m_length = data.length();
    m_start = -1;
  }

  /**
   * Constructor TextImpl
   *
   *
   * NEEDSDOC @param doc
   * NEEDSDOC @param ch
   * NEEDSDOC @param start
   * NEEDSDOC @param length
   */
  public TextImpl(DocumentImpl doc, char ch[], int start, int length)
  {

    super(doc);

    // m_data = new String(ch, start, start+length);
    FastStringBuffer fsb = doc.m_chars;

    m_start = fsb.m_firstFree;
    m_length = length;

    fsb.append(ch, start, length);
  }

  /**
   * NEEDSDOC Method dispatchSaxEvent 
   *
   *
   * NEEDSDOC @param ch
   *
   * @throws SAXException
   */
  public void dispatchSaxEvent(ContentHandler ch) throws SAXException
  {

    if (-1 == m_start)
      ch.characters(m_data.toCharArray(), 0, m_data.length());
    else
      ch.characters(m_doc.m_chars.m_map, m_start, m_length);
  }

  /**
   * A short integer indicating what type of node this is. The named
   * constants for this value are defined in the org.w3c.dom.Node interface.
   *
   * NEEDSDOC ($objectName$) @return
   */
  public short getNodeType()
  {
    return Node.TEXT_NODE;
  }

  /**
   * Returns the node name. 
   *
   * NEEDSDOC ($objectName$) @return
   */
  public String getNodeName()
  {
    return "#text";
  }

  /**
   * Returns the local part of the qualified name of this node.
   * <br>For nodes created with a DOM Level 1 method, such as
   * <code>createElement</code> from the <code>Document</code> interface,
   * it is <code>null</code>.
   * @since DOM Level 2
   *
   * NEEDSDOC ($objectName$) @return
   */
  public String getLocalName()
  {
    return "#text";
  }

  /**
   * Retrieve character data currently stored in this node.
   *
   *
   * NEEDSDOC ($objectName$) @return
   * @throws DOMExcpetion(DOMSTRING_SIZE_ERR) In some implementations,
   * the stored data may exceed the permitted length of strings. If so,
   * getData() will throw this DOMException advising the user to
   * instead retrieve the data in chunks via the substring() operation.
   */
  public String getData()
  {

    if (null == m_data)
      m_data = new String(m_doc.m_chars.m_map, m_start, m_length);

    return m_data;
  }

  /**
   * Report number of characters currently stored in this node's
   * data. It may be 0, meaning that the value is an empty string.
   *
   * NEEDSDOC ($objectName$) @return
   */
  public int getLength()
  {
    return m_length;
  }

  /**
   * NEEDSDOC Method getNodeValue 
   *
   *
   * NEEDSDOC (getNodeValue) @return
   */
  public String getNodeValue()
  {

    if (null == m_data)
      m_data = new String(m_doc.m_chars.m_map, m_start, m_length);

    return m_data;
  }

  /**
   * NEEDSDOC Method supports 
   *
   *
   * NEEDSDOC @param feature
   * NEEDSDOC @param version
   *
   * NEEDSDOC (supports) @return
   */
  public boolean supports(String feature, String version)
  {

    if (feature == SaxEventDispatch.SUPPORTSINTERFACE)
      return true;
    else
      return false;

    // else if(feature.equals(SaxEventDispatch.SUPPORTSINTERFACE))
    //  return true;
    // else
    //  return super.supports(feature, version);
  }
}
