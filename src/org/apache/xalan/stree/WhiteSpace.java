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

/**
 * Right now this is the same as TextImpl.  Not sure what I
 * want to do with this, but it certainly seems like there
 * should be some way to optimize the storage of whitespace
 * nodes.
 */
public class WhiteSpace extends Child
{

  /** White space data          */
  String m_data;

  /**
   * Constructor WhiteSpace
   *
   *
   * @param doc Document object
   * @param ch Array containing whitespace data
   * @param start Start of data in array
   * @param length Length of data in array
   */
  public WhiteSpace(DocumentImpl doc, char ch[], int start, int length)
  {

    super(doc);

    m_data = new String(ch, start, start + length);
  }

  /**
   * A short integer indicating what type of node this is. The named
   * constants for this value are defined in the org.w3c.dom.Node interface.
   *
   * @return TEXT_NODE node type
   */
  public short getNodeType()
  {
    return Node.TEXT_NODE;
  }

  /**
   * Returns the node name. 
   *
   * @return Text node name 
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
   * @return Text node name
   */
  public String getLocalName()
  {
    return "#text";
  }

  /**
   * Retrieve character data currently stored in this node.
   *
   *
   * @return Text associated with this node
   * @throws DOMExcpetion(DOMSTRING_SIZE_ERR) In some implementations,
   * the stored data may exceed the permitted length of strings. If so,
   * getData() will throw this DOMException advising the user to
   * instead retrieve the data in chunks via the substring() operation.
   */
  public String getData()
  {
    return m_data;
  }

  /**
   * Report number of characters currently stored in this node's
   * data. It may be 0, meaning that the value is an empty string.
   *
   * @return Length of text associated with this node
   */
  public int getLength()
  {
    return m_data.length();
  }

  /**
   * Text associated with this node 
   *
   *
   * @return Text associated with this node
   */
  public String getNodeValue()
  {
    return m_data;
  }  // getNodeValue():String
}
