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

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.apache.xpath.NodeSet;
import org.apache.xpath.XPathContext;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.utils.UnImplNode;

import org.apache.xalan.stree.DocumentFragmentImpl;

/**
 * <meta name="usage" content="internal"/>
 * Container of a result tree fragment.
 */
public class ResultTreeFrag extends DocumentFragmentImpl implements NodeList
{
  Document m_docFactory;
  XPathContext m_xsupport;

  public ResultTreeFrag(Document docFactory, XPathContext support)
  {
    m_xsupport = support;
    m_docFactory = docFactory;
  }

  public ResultTreeFrag(Document docFactory, NodeSet children,
                        XPathContext support)
  {
    m_xsupport = support;
    m_docFactory = docFactory;
  }
  
  public NodeList           getChildNodes()
  {
    return this;
  }
  
  /**
   *  Returns the <code>index</code> th item in the collection. If 
   * <code>index</code> is greater than or equal to the number of nodes in 
   * the list, this returns <code>null</code> .
   * @param index  Index into the collection.
   * @return  The node at the <code>index</code> th position in the 
   *   <code>NodeList</code> , or <code>null</code> if that is not a valid 
   *   index.
   */
  public Node item(int index)
  {
    return getChild(index);
  }

  /**
   *  The number of nodes in the list. The range of valid child node indices 
   * is 0 to <code>length-1</code> inclusive. 
   */
  public int getLength()
  {
    return getChildCount();
  }
  
  /**
   * The <code>Document</code> object associated with this node. This is 
   * also the <code>Document</code> object used to create new nodes. When 
   * this node is a <code>Document</code> or a <code>DocumentType</code> 
   * which is not used with any <code>Document</code> yet, this is 
   * <code>null</code>.
   * @version DOM Level 2
   */
  public Document     getOwnerDocument()
  {
    return m_docFactory;
  }

}
