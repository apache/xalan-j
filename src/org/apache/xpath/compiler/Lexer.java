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
 *     the documentation and/or other materials provided with the
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

import org.apache.xalan.utils.PrefixResolver;
import java.util.Vector;
import org.apache.xpath.res.XPATHErrorResources;
import org.apache.xpath.XPath;
import org.apache.xpath.compiler.Compiler;
import org.apache.xpath.compiler.OpCodes;
import org.apache.xpath.compiler.XPathParser;

/**
 * This class is in charge of lexical processing of the XPath 
 * expression into tokens.
 */
class Lexer
{
  /**
   * The target XPath.
   */
  private Compiler m_compiler;
  
  /**
   * The prefix resolver to map prefixes to namespaces in the XPath.
   */
  PrefixResolver m_namespaceContext;
  
  /**
   * The XPath processor object.
   */
  XPathParser m_processor;
  
  /**
   * This value is added to each element name in the TARGETEXTRA
   * that is a 'target' (right-most top-level element name).
   */
  static final int TARGETEXTRA = 10000;
  
  /**
   * Ignore this, it is going away.
   * This holds a map to the m_tokenQueue that tells where the top-level elements are.
   * It is used for pattern matching so the m_tokenQueue can be walked backwards.
   * Each element that is a 'target', (right-most top level element name) has 
   * TARGETEXTRA added to it.
   * 
   */
  private int m_patternMap[] = new int[100];
  
  /**
   * Ignore this, it is going away.
   * The number of elements that m_patternMap maps;
   */
  private int m_patternMapSize;


  /**
   * Create a Lexer object.
   */
  Lexer(Compiler compiler, PrefixResolver resolver, XPathParser xpathProcessor)
  {
    m_compiler = compiler;
    m_namespaceContext = resolver;
    m_processor = xpathProcessor;
  }

  /**
   * Walk through the expression and build a token queue, and a map of the top-level
   * elements.
   * @param pat XSLT Expression.
   */
  void tokenize(String pat)
    throws org.xml.sax.SAXException
  {
    tokenize(pat, null);
  }

  /**
   * Walk through the expression and build a token queue, and a map of the top-level
   * elements.
   * @param pat XSLT Expression.
   * @param targetStrings Vector to hold Strings, may be null.
   */
  void tokenize(String pat, Vector targetStrings)
    throws org.xml.sax.SAXException
  {
    m_compiler.m_tokenQueueSize = 0;
    m_compiler.m_currentPattern = pat;
    m_patternMapSize = 0;
    m_compiler.m_opMap = new int[OpMap.MAXTOKENQUEUESIZE*5];
    int nChars = pat.length();
    int startSubstring = -1;
    int posOfNSSep = -1;
    boolean isStartOfPat = true;
    boolean isAttrName = false;
    boolean isNum = false;

    // Nesting of '[' so we can know if the given element should be
    // counted inside the m_patternMap.
    int nesting = 0;

    // char[] chars = pat.toCharArray();
    for(int i = 0; i < nChars; i++)
    {
      char c = pat.charAt(i);
      switch(c)
      {
      case '\"':
        {
          if(startSubstring != -1)
          {
            isNum = false;
            isStartOfPat = mapPatternElemPos(nesting, isStartOfPat, isAttrName);
            isAttrName = false;
            if(-1 != posOfNSSep)
            {
              posOfNSSep = mapNSTokens(pat, startSubstring, posOfNSSep, i);
            }
            else
            {
              addToTokenQueue(pat.substring(startSubstring, i));
            }
          }
          startSubstring = i;
          for(i++; (i < nChars) && ((c = pat.charAt(i)) != '\"'); i++);
          if(c == '\"')
          {
            addToTokenQueue(pat.substring(startSubstring, i+1));
            startSubstring = -1;
          }
          else
          {
            m_processor.error(XPATHErrorResources.ER_EXPECTED_DOUBLE_QUOTE, null); //"misquoted literal... expected double quote!");
          }
        }
        break;

      case '\'':
        if(startSubstring != -1)
        {
          isNum = false;
          isStartOfPat = mapPatternElemPos(nesting, isStartOfPat, isAttrName);
          isAttrName = false;
          if(-1 != posOfNSSep)
          {
            posOfNSSep = mapNSTokens(pat, startSubstring, posOfNSSep, i);
          }
          else
          {
            addToTokenQueue(pat.substring(startSubstring, i));
          }
        }
        startSubstring = i;
        for(i++; (i < nChars) && ((c = pat.charAt(i)) != '\''); i++);
        if(c == '\'')
        {
          addToTokenQueue(pat.substring(startSubstring, i+1));
          startSubstring = -1;
        }
        else
        {
          m_processor.error(XPATHErrorResources.ER_EXPECTED_SINGLE_QUOTE, null); //"misquoted literal... expected single quote!");
        }
        break;

      case 0x0A:
      case 0x0D:
      case ' ':
      case '\t':
        if(startSubstring != -1)
        {
          isNum = false;
          isStartOfPat = mapPatternElemPos(nesting, isStartOfPat, isAttrName);
          isAttrName = false;
          if(-1 != posOfNSSep)
          {
            posOfNSSep = mapNSTokens(pat, startSubstring, posOfNSSep, i);
          }
          else
          {
            addToTokenQueue(pat.substring(startSubstring, i));
          }
          startSubstring = -1;
        }
        break;

      case '@':
        isAttrName = true;
        // fall-through on purpose

      case '-':
        if('-' == c)
        {
          if(!(isNum || (startSubstring == -1)))
          {
            break;
          }
          isNum = false;
        }
        // fall-through on purpose

      case '(':
      case '[':
      case ')':
      case ']':
      case '|':
      case '/':
      case '*':
      case '+':
      case '=':
      case ',':
      case '\\': // Unused at the moment
      case '^': // Unused at the moment
      case '!': // Unused at the moment
      case '$':
      case '<':
      case '>':
        if(startSubstring != -1)
        {
          isNum = false;
          isStartOfPat = mapPatternElemPos(nesting, isStartOfPat, isAttrName);
          isAttrName = false;
          if(-1 != posOfNSSep)
          {
            posOfNSSep = mapNSTokens(pat, startSubstring, posOfNSSep, i);
          }
          else
          {
            addToTokenQueue(pat.substring(startSubstring, i));
          }
          startSubstring = -1;
        }
        else if(('/' == c) && isStartOfPat)
        {
          isStartOfPat = mapPatternElemPos(nesting, isStartOfPat, isAttrName);
        }
        else if('*' == c)
        {
          isStartOfPat = mapPatternElemPos(nesting, isStartOfPat, isAttrName);
          isAttrName = false;
        }

        if(0 == nesting)
        {
          if('|' == c)
          {
            if(null != targetStrings)
            {
              recordTokenString(targetStrings);
            }
            isStartOfPat = true;
          }
        }
        if((')' == c) || (']' == c))
        {
          nesting--;
        }
        else if(('(' == c) || ('[' == c))
        {
          nesting++;
        }
        addToTokenQueue(pat.substring(i, i+1));

       break;

      case ':':
       if(posOfNSSep == (i-1))
       {
         if(startSubstring != -1)
         {
           if(startSubstring < (i-1))
             addToTokenQueue(pat.substring(startSubstring, i-1));
         }
         isNum = false;
         isAttrName = false;
         startSubstring = -1;
         posOfNSSep = -1;

         addToTokenQueue(pat.substring(i-1, i+1));
         break;
       }
       else
       {
         posOfNSSep = i;
       }
        // fall through on purpose

      default:
        if(-1 == startSubstring)
        {
          startSubstring = i;
          isNum = Character.isDigit(c);
        }
        else if(isNum)
        {
          isNum = Character.isDigit(c);
        }
      }
    }
    if(startSubstring != -1)
    {
      isNum = false;
      isStartOfPat = mapPatternElemPos(nesting, isStartOfPat, isAttrName);
      if(-1 != posOfNSSep)
      {
        posOfNSSep = mapNSTokens(pat, startSubstring, posOfNSSep, nChars);
      }
      else
      {
        addToTokenQueue(pat.substring(startSubstring, nChars));
      }
    }

    if(0 == m_compiler.m_tokenQueueSize)
    {
      m_processor.error(XPATHErrorResources.ER_EMPTY_EXPRESSION, null); //"Empty expression!");
    }
    else if(null != targetStrings)
    {
      recordTokenString(targetStrings);
    }
    m_processor.m_queueMark = 0;
  }

  /**
   * Record the current position on the token queue as long as
   * this is a top-level element.  Must be called before the
   * next token is added to the m_tokenQueue.
   */
  private boolean mapPatternElemPos(int nesting, boolean isStart, boolean isAttrName)
  {
    if(0 == nesting)
    {
      if(!isStart)
      {
        m_patternMap[m_patternMapSize-1] -= TARGETEXTRA;
      }
      m_patternMap[m_patternMapSize]
        = (m_compiler.m_tokenQueueSize - (isAttrName ? 1 : 0)) + TARGETEXTRA;
      m_patternMapSize++;
      isStart = false;
    }
    return isStart;
  }
  
  /**
   * Given a map pos, return the corresponding token queue pos.
   */
  private int getTokenQueuePosFromMap(int i)
  {
    int pos = m_patternMap[i];
    return (pos >= TARGETEXTRA) ? (pos - TARGETEXTRA) : pos;
  }
  
  /**
   * Reset token queue mark and m_token to a
   * given position.
   * @param mark The new position.
   */
  private final void resetTokenMark(int mark)
  {
    int qsz = m_compiler.m_tokenQueueSize;
    m_processor.m_queueMark = (mark > 0) ? ((mark <= qsz) ? mark -1 : mark) : 0;
    if( m_processor.m_queueMark < qsz )
    {
      m_processor.m_token = (String)m_compiler.m_tokenQueue[m_processor.m_queueMark++];
      m_processor.m_tokenChar = m_processor.m_token.charAt(0);
    }
    else
    {
      m_processor.m_token = null;
      m_processor.m_tokenChar = 0;
    }
  }
  
  /**
   * Given a string, return the corresponding keyword token.
   */
  final int getKeywordToken(String key)
  {
    int tok;
    try
    {
      Integer itok = (Integer)Keywords.m_keywords.get(key);
      tok = (null != itok) ? itok.intValue() : 0;
    }
    catch(NullPointerException npe)
    {
      tok = 0;
    }
    catch(ClassCastException cce)
    {
      tok = 0;
    }
    return tok;
  }

  /**
   * Record the correct token string in the passed vector.
   */
  private void recordTokenString(Vector targetStrings)
  {
    int tokPos = getTokenQueuePosFromMap(m_patternMapSize-1);
    resetTokenMark(tokPos+1);

    if(m_processor.lookahead('(', 1))
    {
      int tok = getKeywordToken(m_processor.m_token);
      switch(tok)
      {
      case OpCodes.NODETYPE_COMMENT:
        targetStrings.addElement(PsuedoNames.PSEUDONAME_COMMENT);
        break;
      case OpCodes.NODETYPE_TEXT:
        targetStrings.addElement(PsuedoNames.PSEUDONAME_TEXT);
        break;
      case OpCodes.NODETYPE_NODE:
        targetStrings.addElement(PsuedoNames.PSEUDONAME_ANY);
        break;
      case OpCodes.NODETYPE_ROOT:
        targetStrings.addElement(PsuedoNames.PSEUDONAME_ROOT);
        break;
      case OpCodes.NODETYPE_ANYELEMENT:
        targetStrings.addElement(PsuedoNames.PSEUDONAME_ANY);
        break;
      case OpCodes.NODETYPE_PI:
        targetStrings.addElement(PsuedoNames.PSEUDONAME_ANY);
        break;
      default:
        targetStrings.addElement(PsuedoNames.PSEUDONAME_ANY);
      }
    }
    else
    {
      if(m_processor.tokenIs('@'))
      {
        tokPos++;
        resetTokenMark(tokPos+1);
      }
      if(m_processor.lookahead(':', 1))
      {
        tokPos += 2;
      }
      targetStrings.addElement(m_compiler.m_tokenQueue[tokPos]);
    }
  }

  private final void addToTokenQueue(String s)
  {
    m_compiler.m_tokenQueue[m_compiler.m_tokenQueueSize++] = s;
  }
  
  /**
   * When a seperator token is found, see if there's a element name or
   * the like to map.
   */
  private int mapNSTokens(String pat, int startSubstring, int posOfNSSep, int posOfScan)
  {
    String prefix = pat.substring(startSubstring, posOfNSSep);
    String uName;
    if((null != m_namespaceContext) && !prefix.equals("*") && !prefix.equals("xmlns"))
    {
      try
      {
        if(prefix.length() > 0)
          uName = ((PrefixResolver)m_namespaceContext).getNamespaceForPrefix(prefix);
        else
        {
          // Assume last was wildcard. This is not legal according
          // to the draft. Set the below to true to make namespace
          // wildcards work.
          if(false)
          {
            addToTokenQueue(":");
            String s = pat.substring(posOfNSSep+1, posOfScan);
            if(s.length() > 0)
              addToTokenQueue(s);
            return -1;
          }
          else
          {
            uName = ((PrefixResolver)m_namespaceContext).getNamespaceForPrefix(prefix);
          }
        }
      }
      catch(ClassCastException cce)
      {
        uName = m_namespaceContext.getNamespaceForPrefix(prefix);
      }
    }
    else
    {
      uName = prefix;
    }
    if((null != uName) && (uName.length() > 0))
    {
      addToTokenQueue(uName);
      addToTokenQueue(":");
      String s = pat.substring(posOfNSSep+1, posOfScan);
      if(s.length() > 0)
        addToTokenQueue(s);
    }
    else
    {
      // error("Could not locate namespace for prefix: "+prefix);
      addToTokenQueue(prefix);
      addToTokenQueue(":");
      String s = pat.substring(posOfNSSep+1, posOfScan);
      if(s.length() > 0)
        addToTokenQueue(s);
    }
    return -1;
  }


}
