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
package org.apache.xpath.objects;

import org.w3c.dom.*;

/**
 * <meta name="usage" content="advanced"/>
 * This class represents an XPath boolean object, and is capable of
 * converting the boolean to other types, such as a string.
 */
public class XBoolean extends XObject
{

  /**
   * <meta name="usage" content="internal"/>
   * A true boolean object so we don't have to keep creating them.
   */
  public static XBoolean S_TRUE = new XBooleanStatic(true);

  /**
   * <meta name="usage" content="internal"/>
   * A true boolean object so we don't have to keep creating them.
   */
  public static XBoolean S_FALSE = new XBooleanStatic(false);

  /** NEEDSDOC Field m_val          */
  boolean m_val;

  /**
   * Construct a XNodeSet object.
   *
   * NEEDSDOC @param b
   */
  public XBoolean(boolean b)
  {

    super();

    m_val = b;
  }

  /**
   * Tell that this is a CLASS_BOOLEAN.
   *
   * NEEDSDOC ($objectName$) @return
   */
  public int getType()
  {
    return CLASS_BOOLEAN;
  }

  /**
   * Given a request type, return the equivalent string.
   * For diagnostic purposes.
   *
   * NEEDSDOC ($objectName$) @return
   */
  public String getTypeString()
  {
    return "#BOOLEAN";
  }

  /**
   * Cast result object to a number.
   *
   * NEEDSDOC ($objectName$) @return
   */
  public double num()
  {
    return m_val ? 1.0 : 0.0;
  }

  /**
   * Cast result object to a boolean.
   *
   * NEEDSDOC ($objectName$) @return
   */
  public boolean bool()
  {
    return m_val;
  }

  /**
   * Cast result object to a string.
   *
   * NEEDSDOC ($objectName$) @return
   */
  public String str()
  {
    return m_val ? "true" : "false";
  }

  /**
   * Return a java object that's closes to the represenation
   * that should be handed to an extension.
   *
   * NEEDSDOC ($objectName$) @return
   */
  public Object object()
  {
    return new Boolean(m_val);
  }

  /**
   * Tell if two objects are functionally equal.
   *
   * NEEDSDOC @param obj2
   *
   * NEEDSDOC ($objectName$) @return
   *
   * @throws org.xml.sax.SAXException
   */
  public boolean equals(XObject obj2) throws org.xml.sax.SAXException
  {

    // In order to handle the 'all' semantics of 
    // nodeset comparisons, we always call the 
    // nodeset function.
    if (obj2.getType() == XObject.CLASS_NODESET)
      return obj2.equals(this);

    return m_val == obj2.bool();
  }

}
