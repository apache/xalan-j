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
package org.apache.xalan.res;

import java.util.ListResourceBundle;

import org.apache.xpath.res.XPATHMessages;

/**
 * <meta name="usage" content="internal"/>
 * Sets things up for issuing error messages.  This class is misnamed, and
 * should be called XalanMessages, or some such.
 */
public class XSLMessages extends XPATHMessages
{

  /** The language specific resource object for Xalan messages.  */
  private static ListResourceBundle XSLTBundle = null;

  /** The class name of the Xalan error message string table.    */
  private static final String XSLT_ERROR_RESOURCES =
    "org.apache.xalan.res.XSLTErrorResources";

  /**
   * Creates a message from the specified key and replacement
   * arguments, localized to the given locale.
   *
   * @param errorCode The key for the message text.
   * @param args      The arguments to be used as replacement text
   *                  in the message created.
   *
   * @return The formatted message string.
   */
  public static final String createMessage(String msgKey, Object args[])  //throws Exception
  {
    if (XSLTBundle == null)
      XSLTBundle = loadResourceBundle(XSLT_ERROR_RESOURCES);
    
    if (XSLTBundle != null)
    {
      return createMsg(XSLTBundle, msgKey, args);
    }
    else
      return "Could not load any resource bundles.";
  }
  
  /**
   * Creates a message from the specified key and replacement
   * arguments, localized to the given locale.
   *
   * @param msgKey    The key for the message text.
   * @param args      The arguments to be used as replacement text
   *                  in the message created.
   *
   * @return The formatted warning string.
   */
  public static final String createWarning(String msgKey, Object args[])  //throws Exception
  {
    if (XSLTBundle == null)
      XSLTBundle = loadResourceBundle(XSLT_ERROR_RESOURCES);

    if (XSLTBundle != null)
    {
      return createMsg(XSLTBundle, msgKey, args);
    }
    else
      return "Could not load any resource bundles.";
  }
}
