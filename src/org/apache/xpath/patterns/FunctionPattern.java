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
package org.apache.xpath.patterns;

import org.apache.xpath.XPath;
import org.apache.xpath.DOMHelper;
import org.apache.xpath.Expression;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XNumber;
import org.apache.xpath.objects.XObject;

import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

/**
 * <meta name="usage" content="internal"/>
 * NEEDSDOC Class FunctionPattern <needs-comment/>
 */
public class FunctionPattern extends StepPattern
{

  /**
   * Constructor FunctionPattern
   *
   *
   * NEEDSDOC @param expr
   */
  public FunctionPattern(Expression expr)
  {

    super(0, null, null);

    m_functionExpr = expr;
  }

  /**
   * Static calc of match score.
   */
  protected final void calcScore()
  {

    m_score = SCORE_OTHER;

    if (null == m_targetString)
      calcTargetString();
  }

  /** NEEDSDOC Field m_functionExpr          */
  Expression m_functionExpr;

  /**
   * Test a node to see if it matches the given node test.
   * @param xpath The xpath that is executing.
   * @param context The current source tree context node.
   * @param opPos The current position in the xpath.m_opMap array.
   * @param len The length of the argument.
   * @param len The type of the step.
   * @returns score in an XNumber, one of MATCH_SCORE_NODETEST,
   * MATCH_SCORE_NONE, MATCH_SCORE_OTHER, MATCH_SCORE_QNAME.
   *
   * NEEDSDOC @param xctxt
   *
   * NEEDSDOC ($objectName$) @return
   *
   * @throws org.xml.sax.SAXException
   */
  public XObject execute(XPathContext xctxt) throws org.xml.sax.SAXException
  {

    Node context = xctxt.getCurrentNode();
    XObject obj = m_functionExpr.execute(xctxt);
    NodeIterator nl = obj.nodeset();
    XNumber score = SCORE_NONE;

    if (null != nl)
    {
      Node n;

      while (null != (n = nl.nextNode()))
      {
        score = (n.equals(context)) ? SCORE_OTHER : SCORE_NONE;

        if (score == SCORE_OTHER)
        {
          context = n;

          break;
        }
      }
    }

    return score;
  }
}
