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
package org.apache.xalan.utils;

/**
 * <meta name="usage" content="internal"/>
 * A very simple table that stores a list of StringToStringTables, optimized
 * for small lists.
 */
public class StringToStringTableVector
{

  /** NEEDSDOC Field m_blocksize          */
  private int m_blocksize;

  /** NEEDSDOC Field m_map[]          */
  private StringToStringTable m_map[];

  /** NEEDSDOC Field m_firstFree          */
  private int m_firstFree = 0;

  /** NEEDSDOC Field m_mapSize          */
  private int m_mapSize;

  /**
   * Default constructor.  Note that the default
   * block size is very small, for small lists.
   */
  public StringToStringTableVector()
  {

    m_blocksize = 8;
    m_mapSize = m_blocksize;
    m_map = new StringToStringTable[m_blocksize];
  }

  /**
   * Construct a StringToStringTableVector, using the given block size.
   *
   * NEEDSDOC @param blocksize
   */
  public StringToStringTableVector(int blocksize)
  {

    m_blocksize = blocksize;
    m_mapSize = blocksize;
    m_map = new StringToStringTable[blocksize];
  }

  /**
   * Get the length of the list.
   *
   * NEEDSDOC ($objectName$) @return
   */
  public final int getLength()
  {
    return m_firstFree;
  }

  /**
   * Get the length of the list.
   *
   * NEEDSDOC ($objectName$) @return
   */
  public final int size()
  {
    return m_firstFree;
  }

  /**
   * Append a string onto the vector.
   *
   * NEEDSDOC @param value
   */
  public final void addElement(StringToStringTable value)
  {

    if ((m_firstFree + 1) >= m_mapSize)
    {
      m_mapSize += m_blocksize;

      StringToStringTable newMap[] = new StringToStringTable[m_mapSize];

      System.arraycopy(m_map, 0, newMap, 0, m_firstFree + 1);

      m_map = newMap;
    }

    m_map[m_firstFree] = value;

    m_firstFree++;
  }

  /**
   * Given a string, find the last added occurance value
   * that matches the key.
   *
   * NEEDSDOC @param key
   *
   * NEEDSDOC ($objectName$) @return
   */
  public final String get(String key)
  {

    for (int i = m_firstFree - 1; i >= 0; --i)
    {
      String nsuri = m_map[i].get(key);

      if (nsuri != null)
        return nsuri;
    }

    return null;
  }

  /**
   * Given a string, find the last added occurance value
   * that matches the key.
   *
   * NEEDSDOC @param key
   *
   * NEEDSDOC ($objectName$) @return
   */
  public final boolean containsKey(String key)
  {

    for (int i = m_firstFree - 1; i >= 0; --i)
    {
      if (m_map[i].get(key) != null)
        return true;
    }

    return false;
  }

  /**
   * Remove the last element.
   */
  public final void removeLastElem()
  {

    if (m_firstFree > 0)
    {
      m_map[m_firstFree] = null;

      m_firstFree--;
    }
  }

  /**
   * Get the nth element.
   *
   * NEEDSDOC @param i
   *
   * NEEDSDOC ($objectName$) @return
   */
  public final StringToStringTable elementAt(int i)
  {
    return m_map[i];
  }

  /**
   * Tell if the table contains the given string.
   *
   * NEEDSDOC @param s
   *
   * NEEDSDOC ($objectName$) @return
   */
  public final boolean contains(StringToStringTable s)
  {

    for (int i = 0; i < m_firstFree; i++)
    {
      if (m_map[i].equals(s))
        return true;
    }

    return false;
  }
}
