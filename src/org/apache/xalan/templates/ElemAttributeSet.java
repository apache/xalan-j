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

import org.w3c.dom.Node;
import org.w3c.dom.DOMException;

import javax.xml.transform.TransformerException;

import org.apache.xml.utils.QName;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.res.XSLMessages;

import java.util.Stack;

import org.apache.xalan.transformer.TransformerImpl;

/**
 * <meta name="usage" content="advanced"/>
 * Implement xsl:attribute-set.
 * <pre>
 * &amp;!ELEMENT xsl:attribute-set (xsl:attribute)*>
 * &amp;!ATTLIST xsl:attribute-set
 *   name %qname; #REQUIRED
 *   use-attribute-sets %qnames; #IMPLIED
 * &amp;
 * </pre>
 * @see <a href="http://www.w3.org/TR/xslt#attribute-sets">attribute-sets in XSLT Specification</a>
 */
public class ElemAttributeSet extends ElemUse
{

  /**
   * The name attribute specifies the name of the attribute set.
   * @serial
   */
  public QName m_qname = null;

  /**
   * Set the "name" attribute.
   * The name attribute specifies the name of the attribute set.
   *
   * @param name Name attribute to set
   */
  public void setName(QName name)
  {
    m_qname = name;
  }

  /**
   * Get the "name" attribute.
   * The name attribute specifies the name of the attribute set.
   *
   * @return The name attribute of the attribute set
   */
  public QName getName()
  {
    return m_qname;
  }

  /**
   * Get an int constant identifying the type of element.
   * @see org.apache.xalan.templates.Constants
   *
   * @return Token ID of the element 
   */
  public int getXSLToken()
  {
    return Constants.ELEMNAME_DEFINEATTRIBUTESET;
  }

  /**
   * Return the node name.
   *
   * @return The name of this element
   */
  public String getNodeName()
  {
    return Constants.ELEMNAME_ATTRIBUTESET_STRING;
  }

  /**
   * Apply a set of attributes to the element.
   *
   * @param transformer non-null reference to the the current transform-time state.
   * @param sourceNode non-null reference to the <a href="http://www.w3.org/TR/xslt#dt-current-node">current source node</a>.
   * @param mode reference, which may be null, to the <a href="http://www.w3.org/TR/xslt#modes">current mode</a>.
   *
   * @throws TransformerException
   */
  public void execute(
          TransformerImpl transformer)
            throws TransformerException
  {

    if (transformer.isRecursiveAttrSet(this))
    {
      throw new TransformerException(
        XSLMessages.createMessage(
          XSLTErrorResources.ER_XSLATTRSET_USED_ITSELF,
          new Object[]{ m_qname.getLocalPart() }));  //"xsl:attribute-set '"+m_qname.m_localpart+
    }

    transformer.pushElemAttributeSet(this);
    super.execute(transformer);

    ElemAttribute attr = (ElemAttribute) getFirstChildElem();

    while (null != attr)
    {
      attr.execute(transformer);

      attr = (ElemAttribute) attr.getNextSiblingElem();
    }

    transformer.popElemAttributeSet();
  }

  /**
   * Add a child to the child list.
   * <!ELEMENT xsl:attribute-set (xsl:attribute)*>
   * <!ATTLIST xsl:attribute-set
   *   name %qname; #REQUIRED
   *   use-attribute-sets %qnames; #IMPLIED
   * >
   *
   * @param newChild Child to be added to this node's list of children
   *
   * @return The child that was just added to the list of children
   *
   * @throws DOMException
   */
  public ElemTemplateElement appendChildElem(ElemTemplateElement newChild)
  {

    int type = ((ElemTemplateElement) newChild).getXSLToken();

    switch (type)
    {
    case Constants.ELEMNAME_ATTRIBUTE :
      break;
    default :
      error(XSLTErrorResources.ER_CANNOT_ADD,
            new Object[]{ newChild.getNodeName(),
                          this.getNodeName() });  //"Can not add " +((ElemTemplateElement)newChild).m_elemName +

    //" to " + this.m_elemName);
    }

    return super.appendChild(newChild);
  }

  /**
   * This function is called during recomposition to
   * control how this element is composed.
   * @param root The root stylesheet for this transformation.
   */
  public void recompose(StylesheetRoot root)
  {
    root.recomposeAttributeSets(this);
  }

}
