/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights 
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

import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMFilter;
import org.apache.xpath.Expression;
import org.apache.xpath.compiler.Compiler;

/**
 * <meta name="usage" content="advanced"/>
 * This class implements a general iterator for
 * those LocationSteps with only one step, and perhaps a predicate, 
 * that only go forward (i.e. it can not be used with ancestors, 
 * preceding, etc.)
 * @see org.apache.xpath.axes.WalkerFactory#newLocPathIterator
 */
public class OneStepIteratorForward extends ChildTestIterator
{
  /** The traversal axis from where the nodes will be filtered. */
  protected int m_axis = -1;

  /**
   * Create a OneStepIterator object.
   *
   * @param compiler A reference to the Compiler that contains the op map.
   * @param opPos The position within the op map, which contains the
   * location path expression for this itterator.
   *
   * @throws javax.xml.transform.TransformerException
   */
  OneStepIteratorForward(Compiler compiler, int opPos, int analysis)
          throws javax.xml.transform.TransformerException
  {
    super(compiler, opPos, analysis);
    int firstStepPos = compiler.getFirstChildPos(opPos);
    
    m_axis = WalkerFactory.getAxisFromStep(compiler, firstStepPos);
    
  }
    
  /**
   * Create a OneStepIterator object that will just traverse the self axes.
   * 
   * @param axis One of the org.apache.xml.dtm.Axis integers.
   *
   * @throws javax.xml.transform.TransformerException
   */
  public OneStepIteratorForward(int axis)
  {
    super(null);
    
    m_axis = axis;
    int whatToShow = DTMFilter.SHOW_ALL;
    initNodeTest(whatToShow);
  }

  

  
  /**
   * Initialize the context values for this expression
   * after it is cloned.
   *
   * @param execContext The XPath runtime context for this
   * transformation.
   */
  public void setRoot(int context, Object environment)
  {
    super.setRoot(context, environment);
    m_traverser = m_cdtm.getAxisTraverser(m_axis);
    
  }
  
//  /**
//   * Return the first node out of the nodeset, if this expression is 
//   * a nodeset expression.  This is the default implementation for 
//   * nodesets.
//   * <p>WARNING: Do not mutate this class from this function!</p>
//   * @param xctxt The XPath runtime context.
//   * @return the first node out of the nodeset, or DTM.NULL.
//   */
//  public int asNode(XPathContext xctxt)
//    throws javax.xml.transform.TransformerException
//  {
//    if(getPredicateCount() > 0)
//      return super.asNode(xctxt);
//      
//    int current = xctxt.getCurrentNode();
//    
//    DTM dtm = xctxt.getDTM(current);
//    DTMAxisTraverser traverser = dtm.getAxisTraverser(m_axis);
//    
//    String localName = getLocalName();
//    String namespace = getNamespace();
//    int what = m_whatToShow;
//    
//    // System.out.println("what: ");
//    // NodeTest.debugWhatToShow(what);
//    if(DTMFilter.SHOW_ALL == what
//       || ((DTMFilter.SHOW_ELEMENT & what) == 0)
//       || localName == NodeTest.WILD
//       || namespace == NodeTest.WILD)
//    {
//      return traverser.first(current);
//    }
//    else
//    {
//      int type = getNodeTypeTest(what);
//      int extendedType = dtm.getExpandedTypeID(namespace, localName, type);
//      return traverser.first(current, extendedType);
//    }
//  }
  
  /**
   * Get the next node via getFirstAttribute && getNextAttribute.
   */
  protected int getNextNode()
  {
    m_lastFetched = (DTM.NULL == m_lastFetched)
                     ? m_traverser.first(m_context)
                     : m_traverser.next(m_context, m_lastFetched);
    return m_lastFetched;
  }
  
  /**
   * Returns the axis being iterated, if it is known.
   * 
   * @return Axis.CHILD, etc., or -1 if the axis is not known or is of multiple 
   * types.
   */
  public int getAxis()
  {
    return m_axis;
  }

  /**
   * @see Expression#deepEquals(Expression)
   */
  public boolean deepEquals(Expression expr)
  {
  	if(!super.deepEquals(expr))
  		return false;
  		
  	if(m_axis != ((OneStepIteratorForward)expr).m_axis)
  		return false;
  		
  	return true;
  }

  
}