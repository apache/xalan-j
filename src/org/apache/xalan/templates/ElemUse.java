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

import java.util.Vector;

import org.w3c.dom.Node;

import javax.xml.transform.TransformerException;

import org.apache.xpath.*;
import org.apache.xalan.utils.QName;

import java.util.StringTokenizer;

import org.apache.xalan.transformer.TransformerImpl;

/**
 * <meta name="usage" content="advanced"/>
 * Implement xsl:use.
 * This acts as a superclass for ElemCopy, ElemAttributeSet,
 * ElemElement, and ElemLiteralResult, on order to implement
 * shared behavior the use-attribute-sets attribute.
 * @see <a href="http://www.w3.org/TR/xslt#attribute-sets">attribute-sets in XSLT Specification</a>
 */
public class ElemUse extends ElemTemplateElement
{

  /**
   * The value of the "use-attribute-sets" attribute.
   */
  private QName m_attributeSetsNames[] = null;

  /**
   * Set the "use-attribute-sets" attribute.
   * Attribute sets are used by specifying a use-attribute-sets
   * attribute on xsl:element, xsl:copy (see [7.5 Copying]) or
   * xsl:attribute-set elements. The value of the use-attribute-sets
   * attribute is a whitespace-separated list of names of attribute
   * sets. Each name is specified as a QName, which is expanded as
   * described in [2.4 Qualified Names].
   *
   * NEEDSDOC @param v
   */
  public void setUseAttributeSets(Vector v)
  {

    int n = v.size();

    m_attributeSetsNames = new QName[n];

    for (int i = 0; i < n; i++)
    {
      m_attributeSetsNames[i] = (QName) v.elementAt(i);
    }
  }

  /**
   * Set the "use-attribute-sets" attribute.
   * Attribute sets are used by specifying a use-attribute-sets
   * attribute on xsl:element, xsl:copy (see [7.5 Copying]) or
   * xsl:attribute-set elements. The value of the use-attribute-sets
   * attribute is a whitespace-separated list of names of attribute
   * sets. Each name is specified as a QName, which is expanded as
   * described in [2.4 Qualified Names].
   *
   * NEEDSDOC @param v
   */
  public void setUseAttributeSets(QName[] v)
  {
    m_attributeSetsNames = v;
  }

  /**
   * Get the "use-attribute-sets" attribute.
   * Attribute sets are used by specifying a use-attribute-sets
   * attribute on xsl:element, xsl:copy (see [7.5 Copying]) or
   * xsl:attribute-set elements, or a xsl:use-attribute-sets attribute on
   * Literal Result Elements.
   * The value of the use-attribute-sets
   * attribute is a whitespace-separated list of names of attribute
   * sets. Each name is specified as a QName, which is expanded as
   * described in [2.4 Qualified Names].
   *
   * NEEDSDOC ($objectName$) @return
   */
  public QName[] getUseAttributeSets()
  {
    return m_attributeSetsNames;
  }

  /**
   * Add the attributes from the named attribute sets to the attribute list.
   * TODO: Error handling for: "It is an error if there are two attribute sets
   * with the same expanded-name and with equal import precedence and that both
   * contain the same attribute unless there is a definition of the attribute
   * set with higher import precedence that also contains the attribute."
   *
   * NEEDSDOC @param transformer
   * NEEDSDOC @param stylesheet
   * NEEDSDOC @param attributeSetsNames
   * NEEDSDOC @param sourceNode
   * NEEDSDOC @param mode
   *
   * @throws TransformerException
   */
  private void applyAttrSets(
          TransformerImpl transformer, StylesheetComposed stylesheet, QName attributeSetsNames[], Node sourceNode, QName mode)
            throws TransformerException
  {

    if (null != attributeSetsNames)
    {
      int nNames = attributeSetsNames.length;

      for (int i = 0; i < nNames; i++)
      {
        QName qname = attributeSetsNames[i];
        Vector attrSets = stylesheet.getAttributeSetComposed(qname);

        if (null != attrSets)
        {
          int nSets = attrSets.size();

          for (int k = 0; k < nSets; k++)
          {
            ElemAttributeSet attrSet =
              (ElemAttributeSet) attrSets.elementAt(k);

            attrSet.execute(transformer, sourceNode, mode);
          }
        }
      }
    }
  }

  /**
   * Copy attributes specified by use-attribute-sets to the result tree.
   * Specifying a use-attribute-sets attribute is equivalent to adding
   * xsl:attribute elements for each of the attributes in each of the
   * named attribute sets to the beginning of the content of the element
   * with the use-attribute-sets attribute, in the same order in which
   * the names of the attribute sets are specified in the use-attribute-sets
   * attribute. It is an error if use of use-attribute-sets attributes
   * on xsl:attribute-set elements causes an attribute set to directly
   * or indirectly use itself.
   *
   * NEEDSDOC @param transformer
   * NEEDSDOC @param sourceNode
   * NEEDSDOC @param mode
   *
   * @throws TransformerException
   */
  public void execute(
          TransformerImpl transformer, Node sourceNode, QName mode)
            throws TransformerException
  {

    if (TransformerImpl.S_DEBUG)
      transformer.getTraceManager().fireTraceEvent(sourceNode, mode, this);

    if (null != m_attributeSetsNames)
    {
      applyAttrSets(transformer, getStylesheetRoot().getStylesheetComposed(),
                    m_attributeSetsNames, sourceNode, mode);
    }
  }
}
