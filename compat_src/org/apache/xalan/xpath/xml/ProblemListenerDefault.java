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
package org.apache.xalan.xpath.xml;

import org.w3c.dom.*;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * <meta name="usage" content="general"/>
 * This is the interface that the XSL processor calls when it 
 * has a problem of some kind, either an error or a warning.
 * Users should ass the XSLTEngineImpl class to setProblemListener
 * if they wish an object instance to be called when a problem
 * event occurs.
 */
public class ProblemListenerDefault implements ProblemListener 
{
  ErrorHandler m_errorHandler = null;
  ProblemListener m_problemListener = null;
  
  public ProblemListenerDefault()
  {
    m_errorHandler = new org.apache.xml.utils.DefaultErrorHandler();
  }
  
  public ProblemListenerDefault(ErrorHandler handler, ProblemListener l)
  { 
    m_errorHandler = handler;
    m_problemListener = l;
  }
  
  public void setErrorHandler (ErrorHandler handler)
  {
    m_errorHandler = handler;
  }
  
  public void setProblemListener (ProblemListener l)
  {
    m_problemListener = l;
  }
  
  public ErrorHandler getErrorHandler ()
  {
    return m_errorHandler;
  }
  
  public ProblemListener getProblemListener ()
  {
    return m_problemListener;
  }
  
  /**
   * Function that is called to issue a message.
   * @param   msg               A string message to output.
   */
  public boolean message(String msg)
  {
      
      synchronized (this)
      {  
        new java.io.PrintWriter(  System.err, true ).println( msg );
      } 
   
    return false;                    // we don't know this is an error 
  }
  
  public boolean problem(short where, short classification, 
                       Object styleNode, Node sourceNode,
                       String msg, String id, int lineNo, int charOffset)
  throws org.xml.sax.SAXException   
  {
    m_errorHandler.error(new SAXParseException(msg, null, id, lineNo, charOffset));
    return false;   
  }  
  

}
