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
package org.apache.xpath.axes;

import org.apache.xpath.axes.LocPathIterator;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;

/**
 * Walker for the 'attribute' axes.
 * @see <a href="http://www.w3.org/TR/xpath#axes">XPath axes descriptions</a>
 */
public class AttributeWalker extends AxesWalker
{
  /**
   * Construct an AttributeWalker using a LocPathWalker.
   */
  public AttributeWalker(LocPathIterator locPathIterator)
  {
    super(locPathIterator);
  }
  
  /**
   *  The root node of the TreeWalker.
   */
  public void setRoot(Node root)
  {
    m_attrListPos = -1;
    m_attributeList = null;
    m_nAttrs = -2;
    m_nextLevelAmount = 1;
    super.setRoot(root);
  }
  
  /**
   *  Moves the <code>TreeWalker</code> to the first visible child of the 
   * current node, and returns the new node. If the current node has no 
   * visible children, returns <code>null</code> , and retains the current 
   * node.
   * @return  The new node, or <code>null</code> if the current node has no 
   *   visible children in the TreeWalker's logical view.
   */
  public Node firstChild()
  {
    if(m_currentNode.getNodeType() == Node.ELEMENT_NODE)
    {
      m_attrListPos = -1;
      m_attributeList = m_currentNode.getAttributes();
      if(null != m_attributeList)
        m_nAttrs = m_attributeList.getLength();
      else
        m_nAttrs = -2;
    }
    m_nextLevelAmount = 0;
    return nextSibling();
  }
  
  /**
   *  Moves the <code>TreeWalker</code> to the next sibling of the current 
   * node, and returns the new node. If the current node has no visible 
   * next sibling, returns <code>null</code> , and retains the current node.
   * @return  The new node, or <code>null</code> if the current node has no 
   *   next sibling in the TreeWalker's logical view.
   */
  public Node nextSibling()
  {
    // Attributes don't really have siblings, but we'll fake it.
    if(null != m_attributeList)
    {
      m_attrListPos++;
      if(m_attrListPos < m_nAttrs)
      {
        return setCurrentIfNotNull(m_attributeList.item(m_attrListPos));
      }
      else
        m_attributeList = null;
    }
    return null; 
  }
  
  NamedNodeMap m_attributeList;
  int m_attrListPos;
  int m_nAttrs;
    
  /**
   * Tell what's the maximum level this axes can descend to.
   */
  protected int getLevelMax()
  {
    return m_lpi.getDOMHelper().getLevel(m_root);
  }

}
