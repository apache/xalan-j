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
package org.apache.xpath.compiler;

import java.util.Vector;

import org.apache.xml.utils.QName;
import org.apache.xpath.patterns.NodeTest;

/**
 * This class represents the data structure basics of the XPath
 * object.
 */
public class OpMap
{

  /**
   * The current pattern string, for diagnostics purposes
   */
  protected String m_currentPattern;

  /**
   * Return the expression as a string for diagnostics.
   *
   * @return The expression string.
   */
  public String toString()
  {
    return m_currentPattern;
  }

  /**
   * Return the expression as a string for diagnostics.
   *
   * @return The expression string.
   */
  public String getPatternString()
  {
    return m_currentPattern;
  }

  /**
   * The max size that the token queue can grow to.
   */
  static final int MAXTOKENQUEUESIZE = 500;

  /**
   *  TokenStack is the queue of used tokens. The current token is the token at the
   * end of the m_tokenQueue. The idea is that the queue can be marked and a sequence
   * of tokens can be reused.
   */
  public Object[] m_tokenQueue = new Object[MAXTOKENQUEUESIZE];

  /**
   * Get the XPath as a list of tokens.
   *
   * @return an array of string tokens.
   */
  public Object[] getTokenQueue()
  {
    return m_tokenQueue;
  }

  /**
   * Get the XPath as a list of tokens.
   *
   * @param pos index into token queue.
   *
   * @return The token, normally a string.
   */
  public Object getToken(int pos)
  {
    return m_tokenQueue[pos];
  }

  /**
   * The current size of the token queue.
   */
  public int m_tokenQueueSize = 0;

  /**
    * Get size of the token queue.
   *
   * @return The size of the token queue.
   */
  public int getTokenQueueSize()
  {
    return m_tokenQueueSize;
  }

  /**
   * An operations map is used instead of a proper parse tree.  It contains
   * operations codes and indexes into the m_tokenQueue.
   * I use an array instead of a full parse tree in order to cut down
   * on the number of objects created.
   */
  public int m_opMap[] = null;

  /**
    * Get the opcode list that describes the XPath operations.  It contains
   * operations codes and indexes into the m_tokenQueue.
   * I use an array instead of a full parse tree in order to cut down
   * on the number of objects created.
   *
   * @return An array of integers that is the opcode list that describes the XPath operations.
   */
  public int[] getOpMap()
  {
    return m_opMap;
  }

  // Position indexes

  /**
   * The length is always the opcode position + 1.
   * Length is always expressed as the opcode+length bytes,
   * so it is always 2 or greater.
   */
  public static final int MAPINDEX_LENGTH = 1;

  /**
   * Replace the large arrays
   * with a small array.
   */
  void shrink()
  {

    int map[] = m_opMap;
    int n = m_opMap[MAPINDEX_LENGTH];
    ;

    m_opMap = new int[n + 4];

    int i;

    for (i = 0; i < n; i++)
    {
      m_opMap[i] = map[i];
    }

    m_opMap[i] = 0;
    m_opMap[i + 1] = 0;
    m_opMap[i + 2] = 0;

    Object[] tokens = m_tokenQueue;

    n = m_tokenQueueSize;
    m_tokenQueue = new Object[n + 4];

    for (i = 0; i < n; i++)
    {
      m_tokenQueue[i] = tokens[i];
    }

    m_tokenQueue[i] = null;
    m_tokenQueue[i + 1] = null;
    m_tokenQueue[i + 2] = null;
  }

  /**
  * Given an operation position, return the current op.
   *
   * @param opPos index into op map.
   * @return the op that corresponds to the opPos argument.
   */
  public int getOp(int opPos)
  {
    return m_opMap[opPos];
  }

  /**
   * Given an operation position, return the end position, i.e. the
   * beginning of the next operation.
   *
   * @param opPos An op position of an operation for which there is a size 
   *              entry following.
   * @return position of next operation in m_opMap.
   */
  public int getNextOpPos(int opPos)
  {
    return opPos + m_opMap[opPos + 1];
  }

  /**
   * Given a location step position, return the end position, i.e. the
   * beginning of the next step.
   *
   * @param opPos the position of a location step.
   * @return the position of the next location step.
   */
  public int getNextStepPos(int opPos)
  {

    int stepType = getOp(opPos);

    if ((stepType >= OpCodes.AXES_START_TYPES)
            && (stepType <= OpCodes.AXES_END_TYPES))
    {
      return getNextOpPos(opPos);
    }
    else if ((stepType >= OpCodes.FIRST_NODESET_OP)
             && (stepType <= OpCodes.LAST_NODESET_OP))
    {
      int newOpPos = getNextOpPos(opPos);

      while (OpCodes.OP_PREDICATE == getOp(newOpPos))
      {
        newOpPos = getNextOpPos(newOpPos);
      }

      stepType = getOp(newOpPos);

      if (!((stepType >= OpCodes.AXES_START_TYPES)
            && (stepType <= OpCodes.AXES_END_TYPES)))
      {
        return OpCodes.ENDOP;
      }

      return newOpPos;
    }
    else
    {
      throw new RuntimeException(
        "Programmer's assertion in getNextStepPos: unknown stepType: "
        + stepType);
    }
  }

  /**
   * Given an operation position, return the end position, i.e. the
   * beginning of the next operation.
   *
   * @param opMap The operations map.
   * @param opPos index to operation, for which there is a size entry following.
   * @return position of next operation in m_opMap.
   */
  public static int getNextOpPos(int[] opMap, int opPos)
  {
    return opPos + opMap[opPos + 1];
  }

  /**
   * Given an FROM_stepType position, return the position of the
   * first predicate, if there is one, or else this will point
   * to the end of the FROM_stepType.
   * Example:
   *  int posOfPredicate = xpath.getNextOpPos(stepPos);
   *  boolean hasPredicates =
   *            OpCodes.OP_PREDICATE == xpath.getOp(posOfPredicate);
   *
   * @param opPos position of FROM_stepType op. 
   * @return position of predicate in FROM_stepType structure.
   */
  public int getFirstPredicateOpPos(int opPos)
     throws javax.xml.transform.TransformerException
  {

    int stepType = m_opMap[opPos];

    if ((stepType >= OpCodes.AXES_START_TYPES)
            && (stepType <= OpCodes.AXES_END_TYPES))
    {
      return opPos + m_opMap[opPos + 2];
    }
    else if ((stepType >= OpCodes.FIRST_NODESET_OP)
             && (stepType <= OpCodes.LAST_NODESET_OP))
    {
      return opPos + m_opMap[opPos + 1];
    }
    else if(-2 == stepType)
    {
      return -2;
    }
    else
    {
      error(org.apache.xpath.res.XPATHErrorResources.ER_UNKNOWN_OPCODE,
            new Object[]{ String.valueOf(stepType) });  //"ERROR! Unknown op code: "+m_opMap[opPos]);
      return -1;
    }
  }
  
  /**
   * Tell the user of an error, and probably throw an
   * exception.
   *
   * @param msg An error number that corresponds to one of the numbers found 
   *            in {@link org.apache.xpath.res.XPATHErrorResources}, which is 
   *            a key for a format string.
   * @param args An array of arguments represented in the format string, which 
   *             may be null.
   *
   * @throws TransformerException if the current ErrorListoner determines to 
   *                              throw an exception.
   */
  public void error(int msg, Object[] args) throws javax.xml.transform.TransformerException
  {

    java.lang.String fmsg = org.apache.xalan.res.XSLMessages.createXPATHMessage(msg, args);
    

    throw new javax.xml.transform.TransformerException(fmsg);
  }


  /**
   * Go to the first child of a given operation.
   *
   * @param opPos position of operation.
   *
   * @return The position of the first child of the operation.
   */
  public static int getFirstChildPos(int opPos)
  {
    return opPos + 2;
  }

  /**
   * Get the length of an operation.
   *
   * @param opPos The position of the operation in the op map.
   *
   * @return The size of the operation.
   */
  public int getArgLength(int opPos)
  {
    return m_opMap[opPos + MAPINDEX_LENGTH];
  }

  /**
   * Given a location step, get the length of that step.
   *
   * @param opPos Position of location step in op map.
   *
   * @return The length of the step.
   */
  public int getArgLengthOfStep(int opPos)
  {
    return m_opMap[opPos + MAPINDEX_LENGTH + 1] - 3;
  }

  /**
   * Get the first child position of a given location step.
   *
   * @param opPos Position of location step in the location map.
   *
   * @return The first child position of the step.
   */
  public static int getFirstChildPosOfStep(int opPos)
  {
    return opPos + 3;
  }

  /**
   * Get the test type of the step, i.e. NODETYPE_XXX value.
   * 
   * @param opPosOfStep The position of the FROM_XXX step.
   *
   * @return NODETYPE_XXX value.
   */
  public int getStepTestType(int opPosOfStep)
  {
    return m_opMap[opPosOfStep + 3];  // skip past op, len, len without predicates
  }

  /**
   * Get the namespace of the step.
   * 
   * @param opPosOfStep The position of the FROM_XXX step.
   *
   * @return The step's namespace, NodeTest.WILD, or null for null namespace.
   */
  public String getStepNS(int opPosOfStep)
  {

    int argLenOfStep = getArgLengthOfStep(opPosOfStep);

    // System.out.println("getStepNS.argLenOfStep: "+argLenOfStep);
    if (argLenOfStep == 3)
    {
      int index = m_opMap[opPosOfStep + 4];

      if (index >= 0)
        return (String) m_tokenQueue[index];
      else if (OpCodes.ELEMWILDCARD == index)
        return NodeTest.WILD;
      else
        return null;
    }
    else
      return null;
  }

  /**
   * Get the local name of the step.
   * @param opPosOfStep The position of the FROM_XXX step.
   *
   * @return OpCodes.EMPTY, OpCodes.ELEMWILDCARD, or the local name.
   */
  public String getStepLocalName(int opPosOfStep)
  {

    int argLenOfStep = getArgLengthOfStep(opPosOfStep);

    // System.out.println("getStepLocalName.argLenOfStep: "+argLenOfStep);
    int index;

    switch (argLenOfStep)
    {
    case 0 :
      index = OpCodes.EMPTY;
      break;
    case 1 :
      index = OpCodes.ELEMWILDCARD;
      break;
    case 2 :
      index = m_opMap[opPosOfStep + 4];
      break;
    case 3 :
      index = m_opMap[opPosOfStep + 5];
      break;
    default :
      index = OpCodes.EMPTY;
      break;  // Should assert error
    }

    // int index = (argLenOfStep == 3) ? m_opMap[opPosOfStep+5] 
    //                                  : ((argLenOfStep == 1) ? -3 : -2);
    if (index >= 0)
      return (String) m_tokenQueue[index].toString();
    else if (OpCodes.ELEMWILDCARD == index)
      return NodeTest.WILD;
    else
      return null;
  }

}
