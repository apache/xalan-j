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
package org.apache.xml.dtm;

/**
 * This class serves as a default base for implementations of DTMAxisIterators.
 */
public abstract class DTMAxisIteratorBase implements DTMAxisIterator
{

  /** The position of the last node in the iteration, as defined by XPath. */
  private int _last = -1;

  /** The position of the current node in the iteration, as defined by XPath. */
  private int _position = 0;

  /** The position of the marked node in the iteration. */
  protected int _markedNode;

  /** The handle to the start, or root of the iteration. */
  protected int _startNode = DTMAxisIterator.END;

  /** Flag to tell if the start node should be included in the iteration.   */
  protected boolean _includeSelf = false;

  /** Flag to tell if the iterator is restartable.   */
  protected boolean _isRestartable = true;

  /**
   * Resets the iterator to the last start node.
   *
   * @return A DTMAxisIterator, which may or may not be the same as this 
   *         iterator.
   */
  public DTMAxisIterator reset()
  {

    final boolean temp = _isRestartable;

    _isRestartable = true;

    setStartNode(_startNode);

    _isRestartable = temp;

    return this;
  }

  /**
   * Set the flag to include the start node in the iteration. 
   *
   *
   * @return This default method returns just returns itself, after setting the
   *         flag.
   */
  public DTMAxisIterator includeSelf()
  {

    _includeSelf = true;

    return this;
  }

  /**
   * Returns the number of elements in this iterator.  This may be an expensive 
   * operation when called the first time.
   *
   * @return The number of elements in this iterator.
   */
  public int getLast()
  {

    if (_last == -1)
    {
      final int temp = _position;

      setMark();
      reset();

      do
      {
        _last++;
      }
      while (next() != END);

      gotoMark();

      _position = temp;
    }

    return _last;
  }

  /**
   * Returns the position of the current node in the set.
   *
   * @return The position of the current node in the set, as defined by XPath.
   */
  public int getPosition()
  {
    return _position == 0 ? 1 : _position;
  }

  /**
   * True if this iterator has a reversed axis.
   *
   * @return true if this iterator has a reversed axis.
   */
  public boolean isReverse()
  {
    return false;
  }

  /**
   * Returns a deep copy of this iterator.
   *
   * @return a deep copy of this iterator.
   */
  public DTMAxisIterator cloneIterator()
  {

    try
    {
      final DTMAxisIteratorBase clone = (DTMAxisIteratorBase) super.clone();

      clone._isRestartable = false;

      return clone.reset();
    }
    catch (CloneNotSupportedException e)
    {
      throw new org.apache.xml.utils.WrappedRuntimeException(e);
    }
  }

  /**
   * Simply return the node that is passed in, after incrementing the position.
   *
   *
   * @param node Node handle.
   *
   * @return The node handle passed in.
   */
  protected final int returnNode(final int node)
  {

    _position++;

    return node;
  }

  /**
   * Reset the position to zero.
   *
   *
   * @return This instance.
   */
  protected final DTMAxisIterator resetPosition()
  {

    _position = 0;

    return this;
  }
}
