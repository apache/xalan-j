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
 * Bare-bones, unsafe, fast string buffer. No thread-safety, no
 * parameter range checking, exposed fields. Note that in typical
 * applications, thread-safety of a StringBuffer is a somewhat
 * dubious concept in any case.
 * <p>
 * Note that Stree is using a single FastStringBuffer as a string pool,
 * by recording start and length indices within a single buffer. This
 * minimizes heap overhead, but of course requires more work when retrieving
 * the data.
 * <p>
 * This has been recoded to operate as a "chunked buffer". Doing so
 * reduces the need to recopy existing information when an append
 * exceeds the space available; we just allocate another chunk and
 * flow across to it. (The array of chunks may need to grow,
 * admittedly, but that's a much smaller object.) Some excess
 * recopying may arise when we extract Strings which cross chunk
 * boundaries; larger chunks make that less frequent.
 * <p>
 * The size values are parameterized, to allow tuning this code. In
 * theory, RTFs might want to be tuned differently from the main
 * document's text.
 * <p>
 */
public class FastStringBuffer
{
  // If nonzero, forces the inial chunk size.
  /**/static final int DEBUG_FORCE_INIT_BITS=0;

  /**
   * Field m_chunkBits sets our chunking strategy, by saying how many
   * bits of index can be used within a single chunk before flowing over
   * to the next chunk. For example, if m_chunkbits is set to 15, each
   * chunk can contain up to 2^15 (32K) characters  
   */
  int m_chunkBits = 15;

  /**
   * Field m_maxChunkBits affects our chunk-growth strategy, by saying what
   * the largest permissible chunk size is in this particular FastStringBuffer
   * hierarchy. 
   */
  int m_maxChunkBits = 15;

  /**
   * Field m_rechunkBits affects our chunk-growth strategy, by saying how
   * many chunks should be allocated at one size before we encapsulate them
   * into the first chunk of the next size up. For example, if m_rechunkBits
   * is set to 3, then after 8 chunks at a given size we will rebundle
   * them as the first element of a FastStringBuffer using a chunk size
   * 8 times larger (chunkBits shifted left three bits).
   */
  int m_rebundleBits = 2;

  /**
   * Field m_chunkSize establishes the maximum size of one chunk of the array
   * as 2**chunkbits characters.
   * (Which may also be the minimum size if we aren't tuning for storage) 
   */
  int m_chunkSize;  // =1<<(m_chunkBits-1);

  /**
   * Field m_chunkMask is m_chunkSize-1 -- in other words, m_chunkBits
   * worth of low-order '1' bits, useful for shift-and-mask addressing
   * within the chunks. 
   */
  int m_chunkMask;  // =m_chunkSize-1;

  /**
   * Field m_array holds the string buffer's text contents, using an
   * array-of-arrays. Note that this array, and the arrays it contains, may be
   * reallocated when necessary in order to allow the buffer to grow;
   * references to them should be considered to be invalidated after any
   * append. However, the only time these arrays are directly exposed
   * is in the sendSAXcharacters call.
   */
  char[][] m_array;

  /**
   * Field m_lastChunk is an index into m_array[], pointing to the last
   * chunk of the Chunked Array currently in use. Note that additional
   * chunks may actually be allocated, eg if the FastStringBuffer had
   * previously been truncated or if someone issued an ensureSpace request.
   * <p>
   * The insertion point for append operations is addressed by the combination
   * of m_lastChunk and m_firstFree.
   */
  int m_lastChunk = 0;

  /**
   * Field m_firstFree is an index into m_array[m_lastChunk][], pointing to
   * the first character in the Chunked Array which is not part of the
   * FastStringBuffer's current content. Since m_array[][] is zero-based,
   * the length of that content can be calculated as
   * (m_lastChunk<<m_chunkBits) + m_firstFree 
   */
  int m_firstFree = 0;

  /**
   * Field m_innerFSB, when non-null, is a FastStringBuffer whose total
   * length equals m_chunkSize, and which replaces m_array[0]. This allows
   * building a hierarchy of FastStringBuffers, where early appends use
   * a smaller chunkSize (for less wasted memory overhead) but later
   * ones use a larger chunkSize (for less heap activity overhead).
   */
  FastStringBuffer m_innerFSB = null;

  /**
   * Construct a FastStringBuffer, with allocation policy as per parameters.
   * <p>
   * For coding convenience, I've expressed both allocation sizes in terms of
   * a number of bits. That's needed for the final size of a chunk,
   * to permit fast and efficient shift-and-mask addressing. It's less critical
   * for the inital size, and may be reconsidered.
   * <p>
   * An alternative would be to accept integer sizes and round to powers of two;
   * that really doesn't seem to buy us much, if anything.
   *
   * @param initChunkBits Length in characters of the initial allocation
   * of a chunk, expressed in log-base-2. (That is, 10 means allocate 1024
   * characters.) Later chunks will use larger allocation units, to trade off
   * allocation speed of large document against storage efficiency of small
   * ones.
   * @param maxChunkBits Number of character-offset bits that should be used for
   * addressing within a chunk. Maximum length of a chunk is 2^chunkBits
   * characters.
   * @param rebundleBits Number of character-offset bits that addressing should
   * advance before we attempt to take a step from initChunkBits to maxChunkBits
   */
  public FastStringBuffer(int initChunkBits, int maxChunkBits,
                          int rebundleBits)
  {
    if(DEBUG_FORCE_INIT_BITS!=0) initChunkBits=DEBUG_FORCE_INIT_BITS;

    m_array = new char[16][];

    // Don't bite off more than we're prepared to swallow!
    if (initChunkBits > maxChunkBits)
      initChunkBits = maxChunkBits;

    m_chunkBits = initChunkBits;
    m_maxChunkBits = maxChunkBits;
    m_rebundleBits = rebundleBits;
    m_chunkSize = 1 << (initChunkBits);
    m_chunkMask = m_chunkSize - 1;
    m_array[0] = new char[m_chunkSize];
  }

  /**
   * Construct a FastStringBuffer, using a default rebundleBits value.
   *
   * NEEDSDOC @param initChunkBits
   * NEEDSDOC @param maxChunkBits
   */
  public FastStringBuffer(int initChunkBits, int maxChunkBits)
  {
    this(initChunkBits, maxChunkBits, 2);
  }

  /**
   * Construct a FastStringBuffer, using default maxChunkBits and
   * rebundleBits values.
   * <p>
   * ISSUE: Should this call assert initial size, or fixed size?
   * Now configured as initial, with a default for fixed.
   *
   * @param
   *
   * NEEDSDOC @param initChunkBits
   */
  public FastStringBuffer(int initChunkBits)
  {
    this(initChunkBits, 15, 2);
  }

  /**
   * Construct a FastStringBuffer, using a default allocation policy.
   */
  public FastStringBuffer()
  {

    // 10 bits is 1K. 15 bits is 32K. Remember that these are character
    // counts, so actual memory allocation unit is doubled for UTF-16 chars.
    //
    // For reference: In the original FastStringBuffer, we simply
    // overallocated by blocksize (default 1KB) on each buffer-growth.
    this(10, 15, 2);
  }

  /**
   * Get the length of the list. Synonym for length().
   *
   * @return the number of characters in the FastStringBuffer's content.
   */
  public final int size()
  {
    return (m_lastChunk << m_chunkBits) + m_firstFree;
  }

  /**
   * Get the length of the list. Synonym for size().
   *
   * @return the number of characters in the FastStringBuffer's content.
   */
  public final int length()
  {
    return (m_lastChunk << m_chunkBits) + m_firstFree;
  }

  /**
   * Discard the content of the FastStringBuffer, and most of the memory
   * that was allocated by it, restoring the initial state. Note that this
   * may eventually be different from setLength(0), which see.
   */
  public final void reset()
  {

    m_lastChunk = 0;
    m_firstFree = 0;

    // Recover the original chunk size
    FastStringBuffer innermost = this;

    while (innermost.m_innerFSB != null)
    {
      innermost = innermost.m_innerFSB;
    }

    m_chunkBits = innermost.m_chunkBits;
    m_chunkSize = innermost.m_chunkSize;
    m_chunkMask = innermost.m_chunkMask;

    // Discard the hierarchy
    m_innerFSB = null;
    m_array = new char[16][0];
    m_array[0] = new char[m_chunkSize];
  }

  /**
   * Directly set how much of the FastStringBuffer's storage is to be
   * considered part of its content. This is a fast but hazardous
   * operation. It is not protected against negative values, or values
   * greater than the amount of storage currently available... and even
   * if additional storage does exist, its contents are unpredictable.
   * The only safe use for our setLength() is to truncate the FastStringBuffer
   * to a shorter string.
   * <p>
   * TODO: %REVEIW% Current setLength code is probably not the best solution.
   * It releases memory that in theory we shouldn retain and
   * reuse. Holding onto that would require recursive truncation of
   * the inner FSB, and extending the append operations to recurse
   * into the inner FSB when space exists within them. Could be done,
   * but nontrivial change and adds some overhead to the append
   * operation. Consider alternatives. 
   *
   * @param l New length. If l<0 or l>=getLength(), this operation will
   * not report an error but future operations will almost certainly fail.
   */
  public final void setLength(int l)
  {

    m_lastChunk = l >>> m_chunkBits;

    if (m_lastChunk == 0 && m_innerFSB != null)
    {
      m_innerFSB.setLength(l, this);
    }
    else
    {
      m_firstFree = l & m_chunkMask;
    }
  }

  /**
   * Subroutine for the public setLength() method. Deals with the fact
   * that truncation may require restoring one of the innerFSBs
   *
   * NEEDSDOC @param l
   * NEEDSDOC @param rootFSB
   */
  private final void setLength(int l, FastStringBuffer rootFSB)
  {

    m_lastChunk = l >>> m_chunkBits;

    if (m_lastChunk == 0 && m_innerFSB != null)
    {
      m_innerFSB.setLength(l, rootFSB);
    }
    else
    {

      // Undo encapsulation -- pop the innerFSB data back up to root.
      rootFSB.m_chunkBits = m_chunkBits;
      rootFSB.m_maxChunkBits = m_maxChunkBits;
      rootFSB.m_rebundleBits = m_rebundleBits;
      rootFSB.m_chunkSize = m_chunkSize;
      rootFSB.m_chunkMask = m_chunkMask;
      rootFSB.m_array = m_array;
      rootFSB.m_innerFSB = m_innerFSB;
      rootFSB.m_lastChunk = m_lastChunk;

      // Finally, truncate this sucker.
      rootFSB.m_firstFree = l & m_chunkMask;
    }
  }

  /**
   * Note that this operation has been somewhat deoptimized by the shift to a
   * chunked array, as there is no factory method to produce a String object
   * directly from an array of arrays and hence a double copy is needed.
   * By using ensureCapacity we hope to minimize the heap overhead of building
   * the intermediate StringBuffer.
   * <p>
   * (It really is a pity that Java didn't design String as a final subclass
   * of MutableString, rather than having StringBuffer be a separate hierarchy.
   * We'd avoid a <strong>lot</strong> of double-buffering.)
   *
   * @return the contents of the FastStringBuffer as a standard Java string.
   */
  public final String toString()
  {

    int length = (m_lastChunk << m_chunkBits) + m_firstFree;

    return getString(new StringBuffer(length), 0, 0, length).toString();
  }

  /**
   * Append a single character onto the FastStringBuffer, growing the
   * storage if necessary.
   * <p>
   * NOTE THAT after calling append(), previously obtained
   * references to m_array[][] may no longer be valid....
   * though in fact they should be in this instance.
   *
   * @param value character to be appended.
   */
  public final void append(char value)
  {

    char[] chunk;

    // We may have preallocated chunks. If so, all but last should
    // be at full size.
    boolean lastchunk = (m_lastChunk + 1 == m_array.length);

    if (m_firstFree < m_chunkSize)  // Simplified test single-character-fits
      chunk = m_array[m_lastChunk];
    else
    {

      // Extend array?
      int i = m_array.length;

      if (m_lastChunk + 1 == i)
      {
        char[][] newarray = new char[i + 16][];

        System.arraycopy(m_array, 0, newarray, 0, i);

        m_array = newarray;
      }

      // Advance one chunk
      chunk = m_array[++m_lastChunk];

      if (chunk == null)
      {

        // Hierarchical encapsulation
        if (m_lastChunk == 1 << m_rebundleBits
                && m_chunkBits < m_maxChunkBits)
        {

          // Should do all the work of both encapsulating
          // existing data and establishing new sizes/offsets
          m_innerFSB = new FastStringBuffer(this);
        }

        // Add a chunk.
        chunk = m_array[m_lastChunk] = new char[m_chunkSize];
      }

      m_firstFree = 0;
    }

    // Space exists in the chunk. Append the character.
    chunk[m_firstFree++] = value;
  }

  /**
   * Append the contents of a String onto the FastStringBuffer,
   * growing the storage if necessary.
   * <p>
   * NOTE THAT after calling append(), previously obtained
   * references to m_array[] may no longer be valid.
   *
   * @param value String whose contents are to be appended.
   */
  public final void append(String value)
  {

    int strlen = value.length();

    if (0 == strlen)
      return;

    int copyfrom = 0;
    char[] chunk = m_array[m_lastChunk];
    int available = m_chunkSize - m_firstFree;

    // Repeat while data remains to be copied
    while (strlen > 0)
    {

      // Copy what fits
      if (available > strlen)
        available = strlen;

      value.getChars(copyfrom, copyfrom + available, m_array[m_lastChunk],
                     m_firstFree);

      strlen -= available;
      copyfrom += available;

      // If there's more left, allocate another chunk and continue
      if (strlen > 0)
      {

        // Extend array?
        int i = m_array.length;

        if (m_lastChunk + 1 == i)
        {
          char[][] newarray = new char[i + 16][];

          System.arraycopy(m_array, 0, newarray, 0, i);

          m_array = newarray;
        }

        // Advance one chunk
        chunk = m_array[++m_lastChunk];

        if (chunk == null)
        {

          // Hierarchical encapsulation
          if (m_lastChunk == 1 << m_rebundleBits
                  && m_chunkBits < m_maxChunkBits)
          {

            // Should do all the work of both encapsulating
            // existing data and establishing new sizes/offsets
            m_innerFSB = new FastStringBuffer(this);
          }

          // Add a chunk. 
          chunk = m_array[m_lastChunk] = new char[m_chunkSize];
        }

        available = m_chunkSize;
        m_firstFree = 0;
      }
    }

    // Adjust the insert point in the last chunk, when we've reached it.
    m_firstFree += available;
  }

  /**
   * Append the contents of a StringBuffer onto the FastStringBuffer,
   * growing the storage if necessary.
   * <p>
   * NOTE THAT after calling append(), previously obtained
   * references to m_array[] may no longer be valid.
   *
   * @param value StringBuffer whose contents are to be appended.
   */
  public final void append(StringBuffer value)
  {

    int strlen = value.length();

    if (0 == strlen)
      return;

    int copyfrom = 0;
    char[] chunk = m_array[m_lastChunk];
    int available = m_chunkSize - m_firstFree;

    // Repeat while data remains to be copied
    while (strlen > 0)
    {

      // Copy what fits
      if (available > strlen)
        available = strlen;

      value.getChars(copyfrom, copyfrom + available, m_array[m_lastChunk],
                     m_firstFree);

      strlen -= available;
      copyfrom += available;

      // If there's more left, allocate another chunk and continue
      if (strlen > 0)
      {

        // Extend array?
        int i = m_array.length;

        if (m_lastChunk + 1 == i)
        {
          char[][] newarray = new char[i + 16][];

          System.arraycopy(m_array, 0, newarray, 0, i);

          m_array = newarray;
        }

        // Advance one chunk
        chunk = m_array[++m_lastChunk];

        if (chunk == null)
        {

          // Hierarchical encapsulation
          if (m_lastChunk == 1 << m_rebundleBits
                  && m_chunkBits < m_maxChunkBits)
          {

            // Should do all the work of both encapsulating
            // existing data and establishing new sizes/offsets
            m_innerFSB = new FastStringBuffer(this);
          }

          // Add a chunk.
          chunk = m_array[m_lastChunk] = new char[m_chunkSize];
        }

        available = m_chunkSize;
        m_firstFree = 0;
      }
    }

    // Adjust the insert point in the last chunk, when we've reached it.
    m_firstFree += available;
  }

  /**
   * Append part of the contents of a Character Array onto the
   * FastStringBuffer,  growing the storage if necessary.
   * <p>
   * NOTE THAT after calling append(), previously obtained
   * references to m_array[] may no longer be valid.
   *
   * @param chars character array from which data is to be copied
   * @param start offset in chars of first character to be copied,
   * zero-based.
   * @param length number of characters to be copied
   */
  public final void append(char[] chars, int start, int length)
  {

    int strlen = length;

    if (0 == strlen)
      return;

    int copyfrom = start;
    char[] chunk = m_array[m_lastChunk];
    int available = m_chunkSize - m_firstFree;

    // Repeat while data remains to be copied
    while (strlen > 0)
    {

      // Copy what fits
      if (available > strlen)
        available = strlen;

      System.arraycopy(chars, copyfrom, m_array[m_lastChunk], m_firstFree,
                       available);

      strlen -= available;
      copyfrom += available;

      // If there's more left, allocate another chunk and continue
      if (strlen > 0)
      {

        // Extend array?
        int i = m_array.length;

        if (m_lastChunk + 1 == i)
        {
          char[][] newarray = new char[i + 16][];

          System.arraycopy(m_array, 0, newarray, 0, i);

          m_array = newarray;
        }

        // Advance one chunk
        chunk = m_array[++m_lastChunk];

        if (chunk == null)
        {

          // Hierarchical encapsulation
          if (m_lastChunk == 1 << m_rebundleBits
                  && m_chunkBits < m_maxChunkBits)
          {

            // Should do all the work of both encapsulating
            // existing data and establishing new sizes/offsets
            m_innerFSB = new FastStringBuffer(this);
          }

          // Add a chunk.
          chunk = m_array[m_lastChunk] = new char[m_chunkSize];
        }

        available = m_chunkSize;
        m_firstFree = 0;
      }
    }

    // Adjust the insert point in the last chunk, when we've reached it.
    m_firstFree += available;
  }

  /**
   * Append the contents of another FastStringBuffer onto
   * this FastStringBuffer, growing the storage if necessary.
   * <p>
   * NOTE THAT after calling append(), previously obtained
   * references to m_array[] may no longer be valid.
   *
   * @param value FastStringBuffer whose contents are
   * to be appended.
   */
  public final void append(FastStringBuffer value)
  {

    // Complicating factor here is that the two buffers may use
    // different chunk sizes, and even if they're the same we're
    // probably on a different alignment due to previously appended
    // data. We have to work through the source in bite-sized chunks.
    int strlen = value.length();

    if (0 == strlen)
      return;

    int copyfrom = 0;
    char[] chunk = m_array[m_lastChunk];
    int available = m_chunkSize - m_firstFree;

    // Repeat while data remains to be copied
    while (strlen > 0)
    {

      // Copy what fits
      if (available > strlen)
        available = strlen;

      int sourcechunk = (copyfrom + value.m_chunkSize - 1)
                        >>> value.m_chunkBits;
      int sourcecolumn = copyfrom & value.m_chunkMask;
      int runlength = value.m_chunkSize - sourcecolumn;

      if (runlength > available)
        runlength = available;

      System.arraycopy(value.m_array[sourcechunk], sourcecolumn,
                       m_array[m_lastChunk], m_firstFree, runlength);

      if (runlength != available)
        System.arraycopy(value.m_array[sourcechunk + 1], 0,
                         m_array[m_lastChunk], m_firstFree + runlength,
                         available - runlength);

      strlen -= available;
      copyfrom += available;

      // If there's more left, allocate another chunk and continue
      if (strlen > 0)
      {

        // Extend array?
        int i = m_array.length;

        if (m_lastChunk + 1 == i)
        {
          char[][] newarray = new char[i + 16][];

          System.arraycopy(m_array, 0, newarray, 0, i);

          m_array = newarray;
        }

        // Advance one chunk
        chunk = m_array[++m_lastChunk];

        if (chunk == null)
        {

          // Hierarchical encapsulation
          if (m_lastChunk == 1 << m_rebundleBits
                  && m_chunkBits < m_maxChunkBits)
          {

            // Should do all the work of both encapsulating
            // existing data and establishing new sizes/offsets
            m_innerFSB = new FastStringBuffer(this);
          }

          // Add a chunk. 
          chunk = m_array[m_lastChunk] = new char[m_chunkSize];
        }

        available = m_chunkSize;
        m_firstFree = 0;
      }
    }

    // Adjust the insert point in the last chunk, when we've reached it.
    m_firstFree += available;
  }

  /**
   * @return true if the specified range of characters are all whitespace,
   * as defined by XMLCharacterRecognizer.
   * <p>
   * CURRENTLY DOES NOT CHECK FOR OUT-OF-RANGE.
   *
   * @param start Offset of first character in the range.
   * @param length Number of characters to send.
   */
  public boolean isWhitespace(int start, int length)
  {

    int sourcechunk = start >>> m_chunkBits;
    int sourcecolumn = start & m_chunkMask;
    int available = m_chunkSize - sourcecolumn;
    boolean chunkOK;

    while (length > 0)
    {
      int runlength = (length <= available) ? length : available;

      if (sourcechunk == 0 && m_innerFSB != null)
        chunkOK = m_innerFSB.isWhitespace(sourcecolumn, runlength);
      else
        chunkOK = org.apache.xml.utils.XMLCharacterRecognizer.isWhiteSpace(
          m_array[sourcechunk], sourcecolumn, runlength);

      if (!chunkOK)
        return false;

      length -= runlength;

      ++sourcechunk;

      sourcecolumn = 0;
      available = m_chunkSize;
    }

    return true;
  }

  /**
   * @param start Offset of first character in the range.
   * @param length Number of characters to send.
   * @return a new String object initialized from the specified range of
   * characters.
   */
  public String getString(int start, int length)
  {
    return getString(new StringBuffer(length), start >>> m_chunkBits,
                     start & m_chunkMask, length).toString();
  }

  /**
   * @param sb StringBuffer to be appended to
   * @param start Offset of first character in the range.
   * @param length Number of characters to send.
   * @return sb with the requested text appended to it
   */
  StringBuffer getString(StringBuffer sb, int start, int length)
  {
    return getString(sb, start >>> m_chunkBits, start & m_chunkMask, length);
  }

  /**
   * Internal support for toString() and getString().
   * PLEASE NOTE SIGNATURE CHANGE from earlier versions; it now appends into
   * and returns a StringBuffer supplied by the caller. This simplifies
   * m_innerFSB support.
   * <p>
   * Note that this operation has been somewhat deoptimized by the shift to a
   * chunked array, as there is no factory method to produce a String object
   * directly from an array of arrays and hence a double copy is needed.
   * By presetting length we hope to minimize the heap overhead of building
   * the intermediate StringBuffer.
   * <p>
   * (It really is a pity that Java didn't design String as a final subclass
   * of MutableString, rather than having StringBuffer be a separate hierarchy.
   * We'd avoid a <strong>lot</strong> of double-buffering.)
   *
   *
   * @param sb
   * @param startChunk
   * @param startColumn
   * @param length
   * 
   * @return the contents of the FastStringBuffer as a standard Java string.
   */
  StringBuffer getString(StringBuffer sb, int startChunk, int startColumn,
                         int length)
  {

    int stop = (startChunk << m_chunkBits) + startColumn + length;
    int stopChunk = stop >>> m_chunkBits;
    int stopColumn = stop & m_chunkMask;

    // Factored out
    //StringBuffer sb=new StringBuffer(length);
    for (int i = startChunk; i < stopChunk; ++i)
    {
      if (i == 0 && m_innerFSB != null)
        m_innerFSB.getString(sb, startColumn, m_chunkSize - startColumn);
      else
        sb.append(m_array[i], startColumn, m_chunkSize - startColumn);

      startColumn = 0;  // after first chunk
    }

    if (stopChunk == 0 && m_innerFSB != null)
      m_innerFSB.getString(sb, startColumn, stopColumn - startColumn);
    else if (stopColumn > startColumn)
      sb.append(m_array[stopChunk], startColumn, stopColumn - startColumn);

    return sb;
  }

  /**
   * Get a single character from the string buffer.
   *
   *
   * @param pos character position requested.
   * @return A character from the requested position.
   */
  public char charAt(int pos)
  {
    int startChunk = pos >>> m_chunkBits;

    if (startChunk == 0 && m_innerFSB != null)
      return m_innerFSB.charAt(pos & m_chunkMask);
    else
      return m_array[startChunk][pos & m_chunkMask];
  }

  /**
   * Sends the specified range of characters as one or more SAX characters()
   * events.
   * Note that the buffer reference passed to the ContentHandler may be
   * invalidated if the FastStringBuffer is edited; it's the user's
   * responsibility to manage access to the FastStringBuffer to prevent this
   * problem from arising.
   * <p>
   * Note too that there is no promise that the output will be sent as a
   * single call. As is always true in SAX, one logical string may be split
   * across multiple blocks of memory and hence delivered as several
   * successive events.
   *
   * @param ch SAX ContentHandler object to receive the event.
   * @param start Offset of first character in the range.
   * @param length Number of characters to send.
   * @exception org.xml.sax.SAXException may be thrown by handler's
   * characters() method.
   */
  public void sendSAXcharacters(
          org.xml.sax.ContentHandler ch, int start, int length)
            throws org.xml.sax.SAXException
  {

    int stop = start + length;
    int startChunk = start >>> m_chunkBits;
    int startColumn = start & m_chunkMask;
    int stopChunk = stop >>> m_chunkBits;
    int stopColumn = stop & m_chunkMask;

    for (int i = startChunk; i < stopChunk; ++i)
    {
      if (i == 0 && m_innerFSB != null)
        m_innerFSB.sendSAXcharacters(ch, startColumn,
                                     m_chunkSize - startColumn);
      else
        ch.characters(m_array[i], startColumn, m_chunkSize - startColumn);

      startColumn = 0;  // after first chunk
    }

    // Last, or only, chunk
    if (stopChunk == 0 && m_innerFSB != null)
      m_innerFSB.sendSAXcharacters(ch, startColumn, stopColumn - startColumn);
    else if (stopColumn > startColumn)
      ch.characters(m_array[stopChunk], startColumn,
                    stopColumn - startColumn);
  }
  
  /**
   * Sends the specified range of characters as one or more SAX characters()
   * events, normalizing the characters according to XSLT rules.
   *
   * @param ch SAX ContentHandler object to receive the event.
   * @param start Offset of first character in the range.
   * @param length Number of characters to send.
   * @exception org.xml.sax.SAXException may be thrown by handler's
   * characters() method.
   */
  public void sendNormalizedSAXcharacters(
          org.xml.sax.ContentHandler ch, int start, int length)
            throws org.xml.sax.SAXException
  {

    int stop = start + length;
    int startChunk = start >>> m_chunkBits;
    int startColumn = start & m_chunkMask;
    int stopChunk = stop >>> m_chunkBits;
    int stopColumn = stop & m_chunkMask;

    for (int i = startChunk; i < stopChunk; ++i)
    {
      if (i == 0 && m_innerFSB != null)
        m_innerFSB.sendNormalizedSAXcharacters(ch, startColumn,
                                     m_chunkSize - startColumn);
      else
        sendNormalizedSAXcharacters(m_array[i], startColumn, 
                                    m_chunkSize - startColumn, ch);

      startColumn = 0;  // after first chunk
    }

    // Last, or only, chunk
    if (stopChunk == 0 && m_innerFSB != null)
      m_innerFSB.sendNormalizedSAXcharacters(ch, startColumn, stopColumn - startColumn);
    else if (stopColumn > startColumn)
    {
      sendNormalizedSAXcharacters(m_array[stopChunk], startColumn,
                    stopColumn - startColumn, ch);
    }
  }
  
  static char[] m_oneChar = {' '};
  
  /**
   * Directly normalize and dispatch the character array.
   *
   * @param ch The characters from the XML document.
   * @param start The start position in the array.
   * @param length The number of characters to read from the array.
   * 
   * @exception org.xml.sax.SAXException Any SAX exception, possibly
   *            wrapping another exception.
   */
  public static void sendNormalizedSAXcharacters(char ch[], 
             int start, int length, 
             org.xml.sax.ContentHandler handler)
          throws org.xml.sax.SAXException
  {
    int end = length + start;
    int s;
    for (s = start; s < end; s++)
    {
      char c = ch[s];
      if(!XMLCharacterRecognizer.isWhiteSpace(c))
        break;
    }

    boolean whiteSpaceFound = false;
    boolean needToFlushSpace = false;
    int d = s;
    for (; s < end; s++)
    {
      char c = ch[s];

      if (XMLCharacterRecognizer.isWhiteSpace(c))
      {
        if (!whiteSpaceFound)
        {
          whiteSpaceFound = true;
          if(c != ' ')
          {
            int len = (s-d);
            if( len > 0)
            {
              if(needToFlushSpace)
                handler.characters(m_oneChar, 0, 1);
                
              handler.characters(ch, d, len);
              needToFlushSpace = true;
              // handler.characters(m_oneChar, 0, 1);
            }
            d = s+1;
          }
        }
        else
        {
          int z;
          for (z = s+1; z < end; z++)
          {
            c = ch[z];
            if(!XMLCharacterRecognizer.isWhiteSpace(c))
              break;
          }

          int len = (s-d);

          if(z == end)
          {
            end = s;
            break; // Let the flush at the end handle it.
          }
          if(len > 0)
          {
            if(needToFlushSpace)
            {
              handler.characters(m_oneChar, 0, 1);
              needToFlushSpace = false;
            }
              
            handler.characters(ch, d, len);
          }

          whiteSpaceFound = false;
          d = s = z;
        }
      }
      else
      {
        whiteSpaceFound = false;
      }
    }

    if (whiteSpaceFound)
      s--;
    
    int len = (s-d);
    
    if(len > 0)
    {
      if(needToFlushSpace)
        handler.characters(m_oneChar, 0, 1);
      handler.characters(ch, d, len);
    }
  }
  
  /**
   * Sends the specified range of characters as sax Comment.
   * <p>
   * Note that, unlike sendSAXcharacters, this has to be done as a single 
   * call to LexicalHandler#comment.
   *
   * @param ch SAX LexicalHandler object to receive the event.
   * @param start Offset of first character in the range.
   * @param length Number of characters to send.
   * @exception org.xml.sax.SAXException may be thrown by handler's
   * characters() method.
   */
  public void sendSAXComment(
          org.xml.sax.ext.LexicalHandler ch, int start, int length)
            throws org.xml.sax.SAXException
  {

    // %OPT% Do it this way for now...
    String comment = getString(start, length);
    ch.comment(comment.toCharArray(), 0, length);
  }

  /**
   * Copies characters from this string into the destination character
   * array.
   *
   * @param      srcBegin   index of the first character in the string
   *                        to copy.
   * @param      srcEnd     index after the last character in the string
   *                        to copy.
   * @param      dst        the destination array.
   * @param      dstBegin   the start offset in the destination array.
   * @exception IndexOutOfBoundsException If any of the following
   *            is true:
   *            <ul><li><code>srcBegin</code> is negative.
   *            <li><code>srcBegin</code> is greater than <code>srcEnd</code>
   *            <li><code>srcEnd</code> is greater than the length of this
   *                string
   *            <li><code>dstBegin</code> is negative
   *            <li><code>dstBegin+(srcEnd-srcBegin)</code> is larger than
   *                <code>dst.length</code></ul>
   * @exception NullPointerException if <code>dst</code> is <code>null</code>
   */
  private void getChars(int srcBegin, int srcEnd, char dst[], int dstBegin)
  {
    // %TBD% Joe needs to write this function.  Make public when implemented.
  }

  /**
   * Encapsulation c'tor. After this is called, the source FastStringBuffer
   * will be reset to use the new object as its m_innerFSB, and will have
   * had its chunk size reset appropriately. IT SHOULD NEVER BE CALLED
   * EXCEPT WHEN source.length()==1<<(source.m_chunkBits+source.m_rebundleBits)
   *
   * NEEDSDOC @param source
   */
  private FastStringBuffer(FastStringBuffer source)
  {

    // Copy existing information into new encapsulation
    m_chunkBits = source.m_chunkBits;
    m_maxChunkBits = source.m_maxChunkBits;
    m_rebundleBits = source.m_rebundleBits;
    m_chunkSize = source.m_chunkSize;
    m_chunkMask = source.m_chunkMask;
    m_array = source.m_array;
    m_innerFSB = source.m_innerFSB;

    // These have to be adjusted because we're calling just at the time
    // when we would be about to allocate another chunk
    m_lastChunk = source.m_lastChunk - 1;
    m_firstFree = source.m_chunkSize;

    // Establish capsule as the Inner FSB, reset chunk sizes/addressing
    source.m_array = new char[16][];
    source.m_innerFSB = this;

    // Since we encapsulated just as we were about to append another
    // chunk, return ready to create the chunk after the innerFSB
    // -- 1, not 0.
    source.m_lastChunk = 1;
    source.m_firstFree = 0;
    source.m_chunkBits += m_rebundleBits;
    source.m_chunkSize = 1 << (source.m_chunkBits);
    source.m_chunkMask = source.m_chunkSize - 1;
  }
}
