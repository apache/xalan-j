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
package org.apache.xalan.xslt;

import org.w3c.dom.*;
import java.util.*;
import java.net.*;
import java.io.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import trax.ProcessorException;
import org.apache.xml.serialize.*;

/**
 * <meta name="usage" content="general"/> 
 * Binary representation of a stylesheet -- use the {@link org.apache.xalan.xslt.XSLTProcessor} ProcessStylesheet
 * method to create a StylesheetRoot and improve performance for a stylesheet performing multiple transformations.
 * Also required for XSLTProcessor to function as SAX DocumentHandler.
 */
public class StylesheetRoot extends org.apache.xalan.templates.StylesheetRoot
{
  /**
   * Uses an XSL stylesheet document.
   * @param processor  The XSLTProcessor implementation.
   * @param baseIdentifier The file name or URL for the XSL stylesheet.
   * @exception XSLProcessorException thrown if the active ProblemListener and XMLParserLiaison decide
   * the error condition is severe enough to halt processing.
   */
  public StylesheetRoot(XSLTEngineImpl processor,
                        String baseIdentifier)
    throws ProcessorException
  {   
    super();     
  }
  
  /** Get the encoding string that was specified in the stylesheet. */
  public String getOutputEncoding()
  {
    return this.getOutputComposed().getEncoding();
  }

  /** Get the media-type string that was specified in the stylesheet. */
  public String getOutputMediaType() 
  { 
    return this.getOutputComposed().getMediaType(); 
  }
  
  
}
