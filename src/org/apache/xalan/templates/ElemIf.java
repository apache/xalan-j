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

import org.xml.sax.*;

import org.apache.xpath.*;
import org.apache.xpath.objects.XObject;
import org.apache.xalan.trace.SelectionEvent;
import org.apache.xalan.utils.QName;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;

/**
 * <meta name="usage" content="advanced"/>
 * Implement xsl:if.
 * <pre>
 * <!ELEMENT xsl:if %template;>
 * <!ATTLIST xsl:if
 *   test %expr; #REQUIRED
 *   %space-att;
 * >
 * </pre>
 * @see <a href="http://www.w3.org/TR/xslt#section-Conditional-Processing-with-xsl:if">XXX in XSLT Specification</a>
 */
public class ElemIf extends ElemTemplateElement
{

  /**
   * The xsl:if element must have a test attribute, which specifies an expression.
   */
  private XPath m_test = null;

  /**
   * Set the "test" attribute.
   * The xsl:if element must have a test attribute, which specifies an expression.
   *
   * NEEDSDOC @param v
   */
  public void setTest(XPath v)
  {
    m_test = v;
  }

  /**
   * Get the "test" attribute.
   * The xsl:if element must have a test attribute, which specifies an expression.
   *
   * NEEDSDOC ($objectName$) @return
   */
  public XPath getTest()
  {
    return m_test;
  }

  /**
   * Get an int constant identifying the type of element.
   * @see org.apache.xalan.templates.Constants
   *
   * NEEDSDOC ($objectName$) @return
   */
  public int getXSLToken()
  {
    return Constants.ELEMNAME_IF;
  }

  /**
   * Return the node name.
   *
   * NEEDSDOC ($objectName$) @return
   */
  public String getNodeName()
  {
    return Constants.ELEMNAME_IF_STRING;
  }

  /**
   * Conditionally execute a sub-template.
   * The expression is evaluated and the resulting object is converted
   * to a boolean as if by a call to the boolean function. If the result
   * is true, then the content template is instantiated; otherwise, nothing
   * is created.
   *
   * NEEDSDOC @param transformer
   * NEEDSDOC @param sourceNode
   * NEEDSDOC @param mode
   *
   * @throws SAXException
   */
  public void execute(
          TransformerImpl transformer, Node sourceNode, QName mode)
            throws SAXException
  {

    if (TransformerImpl.S_DEBUG)
      transformer.getTraceManager().fireTraceEvent(sourceNode, mode, this);

    XPathContext xctxt = transformer.getXPathContext();
    XObject test = m_test.execute(xctxt, sourceNode, this);

    if (TransformerImpl.S_DEBUG)
      transformer.getTraceManager().fireSelectedEvent(sourceNode, this,
              "test", m_test, test);

    if (test.bool())
    {
      transformer.executeChildTemplates(this, sourceNode, mode);
    }
  }
}
