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
package org.apache.xalan.templates;

import org.w3c.dom.*;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.*;
import org.apache.xpath.*;
import org.apache.xpath.objects.XObject;
import org.apache.xalan.trace.SelectionEvent;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.utils.QName;
import org.apache.xalan.transformer.TreeWalker2Result;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xalan.transformer.ResultTreeHandler;

/**
 * <meta name="usage" content="advanced"/>
 * Implement xsl:copy-of.
 * <pre>
 * <!ELEMENT xsl:copy-of EMPTY>
 * <!ATTLIST xsl:copy-of select %expr; #REQUIRED>
 * </pre>
 * @see <a href="http://www.w3.org/TR/xslt#copy-of">copy-of in XSLT Specification</a>
 */
public class ElemCopyOf extends ElemTemplateElement
{
  /**
   * The required select attribute contains an expression. 
   */
  public XPath m_selectExpression = null;
  
  /**
   * Set the "select" attribute. 
   * The required select attribute contains an expression. 
   */
  public void setSelect(XPath expr)
  {
    m_selectExpression = expr;
  }

  /**
   * Get the "use-attribute-sets" attribute.
   * The required select attribute contains an expression. 
   */
  public XPath getSelect()
  {
    return m_selectExpression;
  }

  /**
   * Get an int constant identifying the type of element.
   * @see org.apache.xalan.templates.Constants
   */
  public int getXSLToken()
  {
    return Constants.ELEMNAME_COPY_OF;
  }
  
  /** 
   * Return the node name.
   */
  public String getNodeName()
  {
    return Constants.ELEMNAME_COPY_OF_STRING;
  }

  /**
   * The xsl:copy-of element can be used to insert a result tree 
   * fragment into the result tree, without first converting it to 
   * a string as xsl:value-of does (see [7.6.1 Generating Text with 
   * xsl:value-of]). 
   */
  public void execute(TransformerImpl transformer, 
                     Node sourceNode,
                     QName mode)
    throws SAXException
  {    
    if(TransformerImpl.S_DEBUG)
      transformer.getTraceManager().fireTraceEvent(sourceNode, mode, this);

    XPathContext xctxt = transformer.getXPathContext();
    XObject value = m_selectExpression.execute(xctxt, sourceNode, this);
    
    if(TransformerImpl.S_DEBUG)
      transformer.getTraceManager().fireSelectedEvent(sourceNode,
                                    this, "select", m_selectExpression, value);
    
    ResultTreeHandler handler = transformer.getResultTreeHandler();
    
    if(null != value)
    {
      int type = value.getType();
      String s;
      switch(type)
      {
      case XObject.CLASS_BOOLEAN:
      case XObject.CLASS_NUMBER:
      case XObject.CLASS_STRING:
        s = value.str();
        handler.characters(s.toCharArray(), 0, s.length());
        break;
        
      case XObject.CLASS_NODESET:
        // System.out.println(value);
        NodeIterator nl = value.nodeset();
        
        // Copy the tree.
        org.apache.xalan.utils.TreeWalker tw 
          = new TreeWalker2Result(transformer, handler, this);
        Node pos;
        while(null != (pos = nl.nextNode()))
        {
          int t = pos.getNodeType();
          // If we just copy the whole document, a startDoc and endDoc get 
          // generated, so we need to only walk the child nodes.
          if(t == Node.DOCUMENT_NODE)
          {
            for(Node child = pos.getFirstChild(); child != null; child = child.getNextSibling())
            {
              tw.traverse(child);
            }
          }
          else if(t == Node.ATTRIBUTE_NODE)
          {
            handler.addAttribute((Attr)pos);
          }
          else
          {
            tw.traverse(pos);
          }
        }
        break;
        
      case XObject.CLASS_RTREEFRAG:
        handler.outputResultTreeFragment(value, transformer.getXPathContext());
        break;
        
      default:
        s = value.str();
        handler.characters(s.toCharArray(), 0, s.length());
        break;
      }
    }
  }
  
  /**
   * Add a child to the child list.
   */
  public Node               appendChild(Node newChild)
    throws DOMException
  {
    error(XSLTErrorResources.ER_CANNOT_ADD, new Object[] {newChild.getNodeName(), this.getNodeName()}); //"Can not add " +((ElemTemplateElement)newChild).m_elemName +
          //" to " + this.m_elemName);
    return null;
  }

}
