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

import java.util.Hashtable;

/**
 * <meta name="usage" content="internal"/>
 * This class is in support of FormatterToHTML, and acts as a sort 
 * of element representative for HTML elements.
 */
class ElemDesc
{
  int m_flags;
  Hashtable m_attrs = null;
  
  static final int EMPTY = (1 << 1);
  static final int FLOW = (1 << 2);
  static final int BLOCK = (1 << 3);
  static final int BLOCKFORM = (1 << 4);
  static final int BLOCKFORMFIELDSET = (1 << 5);
  static final int CDATA = (1 << 6);
  static final int PCDATA = (1 << 7);
  static final int RAW = (1 << 8);
  static final int INLINE = (1 << 9);
  static final int INLINEA = (1 << 10);
  static final int INLINELABEL = (1 << 11);
  static final int FONTSTYLE = (1 << 12);
  static final int PHRASE = (1 << 13);
  static final int FORMCTRL = (1 << 14);
  static final int SPECIAL = (1 << 15);
  static final int ASPECIAL = (1 << 16);
  static final int HEADMISC = (1 << 17);
  static final int HEAD = (1 << 18);
  static final int LIST = (1 << 19);
  static final int PREFORMATTED = (1 << 20);
  static final int WHITESPACESENSITIVE = (1 << 21);

  static final int ATTRURL = (1 << 1);
  static final int ATTREMPTY = (1 << 2);

  ElemDesc(int flags)
  {
    m_flags = flags;
  }
  
  boolean is(int flags)
  {
    // int which = (m_flags & flags);
    return (m_flags & flags) != 0;
  }
  
  void setAttr(String name, int flags)
  {
    if(null == m_attrs)
      m_attrs = new Hashtable();
    
    m_attrs.put(name, new Integer(flags));
  }
  
  boolean isAttrFlagSet(String name, int flags)
  {
    if(null != m_attrs)
    {
      Integer _flags = (Integer)m_attrs.get(name);
      if(null != _flags)
      {
        return (_flags.intValue() & flags) != 0;
      }
    }
    return false;
  }
  
  
}
