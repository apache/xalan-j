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
package org.apache.xml.utils;

/**
 * <meta name="usage" content="internal"/>
 * A very simple table that stores a list of int.
 */
public class IntVector
{

  /** NEEDSDOC Field m_blocksize          */
  protected int m_blocksize;

  /** NEEDSDOC Field m_map[]          */
  public int m_map[];  // expose to package for direct access.

  /** NEEDSDOC Field m_firstFree          */
  protected int m_firstFree = 0;

  /** NEEDSDOC Field m_mapSize          */
  protected int m_mapSize;

  /**
   * Default constructor.  Note that the default
   * block size is very small, for small lists.
   */
  public IntVector()
  {

    m_blocksize = 32;
    m_mapSize = m_blocksize;
    m_map = new int[m_blocksize];
  }

  /**
   * Construct a IntVector, using the given block size.
   *
   * NEEDSDOC @param blocksize
   */
  public IntVector(int blocksize)
  {

    m_blocksize = blocksize;
    m_mapSize = blocksize;
    m_map = new int[blocksize];
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
   * Append a int onto the vector.
   *
   * NEEDSDOC @param value
   */
  public final void addElement(int value)
  {

    if ((m_firstFree + 1) >= m_mapSize)
    {
      m_mapSize += m_blocksize;

      int newMap[] = new int[m_mapSize];

      System.arraycopy(m_map, 0, newMap, 0, m_firstFree + 1);

      m_map = newMap;
    }

    m_map[m_firstFree] = value;

    m_firstFree++;
  }

  /**
   * Inserts the specified node in this vector at the specified index.
   * Each component in this vector with an index greater or equal to
   * the specified index is shifted upward to have an index one greater
   * than the value it had previously.
   *
   * NEEDSDOC @param value
   * NEEDSDOC @param at
   */
  public final void insertElementAt(int value, int at)
  {

    if ((m_firstFree + 1) >= m_mapSize)
    {
      m_mapSize += m_blocksize;

      int newMap[] = new int[m_mapSize];

      System.arraycopy(m_map, 0, newMap, 0, m_firstFree + 1);

      m_map = newMap;
    }

    if (at <= (m_firstFree - 1))
    {
      System.arraycopy(m_map, at, m_map, at + 1, m_firstFree - at);
    }

    m_map[at] = value;

    m_firstFree++;
  }

  /**
   * Inserts the specified node in this vector at the specified index.
   * Each component in this vector with an index greater or equal to
   * the specified index is shifted upward to have an index one greater
   * than the value it had previously.
   */
  public final void removeAllElements()
  {

    for (int i = 0; i < m_firstFree; i++)
    {
      m_map[i] = java.lang.Integer.MIN_VALUE;
    }

    m_firstFree = 0;
  }

  /**
   * Removes the first occurrence of the argument from this vector.
   * If the object is found in this vector, each component in the vector
   * with an index greater or equal to the object's index is shifted
   * downward to have an index one smaller than the value it had
   * previously.
   *
   * NEEDSDOC @param s
   *
   * NEEDSDOC ($objectName$) @return
   */
  public final boolean removeElement(int s)
  {

    for (int i = 0; i < m_firstFree; i++)
    {
      if (m_map[i] == s)
      {
        if ((i + 1) < m_firstFree)
          System.arraycopy(m_map, i + 1, m_map, i - 1, m_firstFree - i);
        else
          m_map[i] = java.lang.Integer.MIN_VALUE;

        m_firstFree--;

        return true;
      }
    }

    return false;
  }

  /**
   * Deletes the component at the specified index. Each component in
   * this vector with an index greater or equal to the specified
   * index is shifted downward to have an index one smaller than
   * the value it had previously.
   *
   * NEEDSDOC @param i
   */
  public final void removeElementAt(int i)
  {

    if (i > m_firstFree)
      System.arraycopy(m_map, i + 1, m_map, i, m_firstFree);
    else
      m_map[i] = java.lang.Integer.MIN_VALUE;

    m_firstFree--;
  }

  /**
   * Sets the component at the specified index of this vector to be the
   * specified object. The previous component at that position is discarded.
   *
   * The index must be a value greater than or equal to 0 and less
   * than the current size of the vector.
   *
   * NEEDSDOC @param node
   * NEEDSDOC @param index
   */
  public final void setElementAt(int node, int index)
  {
    m_map[index] = node;
  }

  /**
   * Get the nth element.
   *
   * NEEDSDOC @param i
   *
   * NEEDSDOC ($objectName$) @return
   */
  public final int elementAt(int i)
  {
    return m_map[i];
  }

  /**
   * Tell if the table contains the given node.
   *
   * NEEDSDOC @param s
   *
   * NEEDSDOC ($objectName$) @return
   */
  public final boolean contains(int s)
  {

    for (int i = 0; i < m_firstFree; i++)
    {
      if (m_map[i] == s)
        return true;
    }

    return false;
  }

  /**
   * Searches for the first occurence of the given argument,
   * beginning the search at index, and testing for equality
   * using the equals method.
   *
   * NEEDSDOC @param elem
   * NEEDSDOC @param index
   * @return the index of the first occurrence of the object
   * argument in this vector at position index or later in the
   * vector; returns -1 if the object is not found.
   */
  public final int indexOf(int elem, int index)
  {

    for (int i = index; i < m_firstFree; i++)
    {
      if (m_map[i] == elem)
        return i;
    }

    return java.lang.Integer.MIN_VALUE;
  }

  /**
   * Searches for the first occurence of the given argument,
   * beginning the search at index, and testing for equality
   * using the equals method.
   *
   * NEEDSDOC @param elem
   * @return the index of the first occurrence of the object
   * argument in this vector at position index or later in the
   * vector; returns -1 if the object is not found.
   */
  public final int indexOf(int elem)
  {

    for (int i = 0; i < m_firstFree; i++)
    {
      if (m_map[i] == elem)
        return i;
    }

    return java.lang.Integer.MIN_VALUE;
  }

  /**
   * Searches for the first occurence of the given argument,
   * beginning the search at index, and testing for equality
   * using the equals method.
   *
   * NEEDSDOC @param elem
   * @return the index of the first occurrence of the object
   * argument in this vector at position index or later in the
   * vector; returns -1 if the object is not found.
   */
  public final int lastIndexOf(int elem)
  {

    for (int i = (m_firstFree - 1); i >= 0; i--)
    {
      if (m_map[i] == elem)
        return i;
    }

    return java.lang.Integer.MIN_VALUE;
  }
}
