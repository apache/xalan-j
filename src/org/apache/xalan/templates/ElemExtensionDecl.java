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
package org.apache.xalan.templates;

import java.util.Vector;

import org.apache.xml.utils.QName;
import org.apache.xml.utils.NameSpace;
import org.apache.xml.utils.StringToStringTable;
import org.apache.xml.utils.StringVector;
import org.apache.xalan.extensions.ExtensionHandler;
import org.apache.xalan.extensions.ExtensionHandlerGeneral;
import org.apache.xalan.extensions.ExtensionsTable;
import org.apache.xalan.transformer.TransformerImpl;

import javax.xml.transform.TransformerException;

import org.apache.xpath.XPathContext;
import org.apache.xalan.res.XSLTErrorResources;

/**
 * <meta name="usage" content="internal"/>
 * NEEDSDOC Class ElemExtensionDecl <needs-comment/>
 */
public class ElemExtensionDecl extends ElemTemplateElement
{

  /**
   * Constructor ElemExtensionDecl
   *
   */
  public ElemExtensionDecl()
  {

    // System.out.println("ElemExtensionDecl ctor");
  }

  /** NEEDSDOC Field m_prefix          */
  private String m_prefix = null;

  /**
   * NEEDSDOC Method setPrefix 
   *
   *
   * NEEDSDOC @param v
   */
  public void setPrefix(String v)
  {
    m_prefix = v;
  }

  /**
   * NEEDSDOC Method getPrefix 
   *
   *
   * NEEDSDOC (getPrefix) @return
   */
  public String getPrefix()
  {
    return m_prefix;
  }

  /** NEEDSDOC Field m_functions          */
  private StringVector m_functions = new StringVector();

  /**
   * NEEDSDOC Method setFunctions 
   *
   *
   * NEEDSDOC @param v
   */
  public void setFunctions(StringVector v)
  {
    m_functions = v;
  }

  /**
   * NEEDSDOC Method getFunctions 
   *
   *
   * NEEDSDOC (getFunctions) @return
   */
  public StringVector getFunctions()
  {
    return m_functions;
  }

  /**
   * NEEDSDOC Method getFunction 
   *
   *
   * NEEDSDOC @param i
   *
   * NEEDSDOC (getFunction) @return
   *
   * @throws ArrayIndexOutOfBoundsException
   */
  public String getFunction(int i) throws ArrayIndexOutOfBoundsException
  {

    if (null == m_functions)
      throw new ArrayIndexOutOfBoundsException();

    return (String) m_functions.elementAt(i);
  }

  /**
   * NEEDSDOC Method getFunctionCount 
   *
   *
   * NEEDSDOC (getFunctionCount) @return
   */
  public int getFunctionCount()
  {
    return (null != m_functions) ? m_functions.size() : 0;
  }

  /** NEEDSDOC Field m_elements          */
  private StringVector m_elements = null;

  /**
   * NEEDSDOC Method setElements 
   *
   *
   * NEEDSDOC @param v
   */
  public void setElements(StringVector v)
  {
    m_elements = v;
  }

  /**
   * NEEDSDOC Method getElements 
   *
   *
   * NEEDSDOC (getElements) @return
   */
  public StringVector getElements()
  {
    return m_elements;
  }

  /**
   * NEEDSDOC Method getElement 
   *
   *
   * NEEDSDOC @param i
   *
   * NEEDSDOC (getElement) @return
   *
   * @throws ArrayIndexOutOfBoundsException
   */
  public String getElement(int i) throws ArrayIndexOutOfBoundsException
  {

    if (null == m_elements)
      throw new ArrayIndexOutOfBoundsException();

    return (String) m_elements.elementAt(i);
  }

  /**
   * NEEDSDOC Method getElementCount 
   *
   *
   * NEEDSDOC (getElementCount) @return
   */
  public int getElementCount()
  {
    return (null != m_elements) ? m_elements.size() : 0;
  }

  /**
   * Get an int constant identifying the type of element.
   * @see org.apache.xalan.templates.Constants
   *
   * NEEDSDOC ($objectName$) @return
   */
  public int getXSLToken()
  {
    return Constants.ELEMNAME_EXTENSIONDECL;
  }

  /**
   * This function will be called on top-level elements
   * only, just before the transform begins.
   *
   * @param transformer The XSLT TransformerFactory.
   *
   * @throws TransformerException
   */
  public void runtimeInit(TransformerImpl transformer) throws TransformerException
  {

    String lang = null;
    String srcURL = null;
    String scriptSrc = null;
    String prefix = getPrefix();
    String declNamespace = getNamespaceForPrefix(prefix);

    if (null == declNamespace)
      throw new TransformerException("Prefix " + prefix
                             + " does not have a corresponding "
                             + "namespace declaration");

    for (ElemTemplateElement child = getFirstChildElem(); child != null;
            child = child.getNextSiblingElem())
    {
      if (Constants.ELEMNAME_EXTENSIONSCRIPT == child.getXSLToken())
      {
        ElemExtensionScript sdecl = (ElemExtensionScript) child;

        lang = sdecl.getLang();
        srcURL = sdecl.getSrc();

        ElemTemplateElement childOfSDecl = sdecl.getFirstChildElem();

        if (null != childOfSDecl)
        {
          if (Constants.ELEMNAME_TEXTLITERALRESULT
                  == childOfSDecl.getXSLToken())
          {
            ElemTextLiteral tl = (ElemTextLiteral) childOfSDecl;
            char[] chars = tl.getChars();

            scriptSrc = new String(chars);

            if (scriptSrc.trim().length() == 0)
              scriptSrc = null;
          }
        }
      }
    }

    if (null == lang)
      lang = "javaclass";

    if (lang.equals("javaclass") && (scriptSrc != null))
      throw new TransformerException("Element content not allowed for lang=javaclass "
                             + scriptSrc);

    XPathContext liaison = ((XPathContext) transformer.getXPathContext());
    ExtensionsTable etable = liaison.getExtensionsTable();
    ExtensionHandler nsh = etable.get(declNamespace);

    // If we have no prior ExtensionHandler for this namespace, we need to
    // create one.
    // If the script element is for javaclass, this is our special compiled java.
    // Element content is not supported for this so we throw an exception if
    // it is provided.  Otherwise, we look up the srcURL to see if we already have
    // an ExtensionHandler.
    if (null == nsh)
    {
      if (lang.equals("javaclass"))
      {
        if (null == srcURL)
        {
          nsh = etable.makeJavaNamespace(declNamespace);
        }
        else
        {
          nsh = etable.get(srcURL);

          if (null == nsh)
          {
            nsh = etable.makeJavaNamespace(srcURL);
          }
        }
      }
      else  // not java
      {
        nsh = new ExtensionHandlerGeneral(declNamespace, this.m_elements,
                                          this.m_functions, lang, srcURL,
                                          scriptSrc);

        // System.out.println("Adding NS Handler: declNamespace = "+
        //                   declNamespace+", lang = "+lang+", srcURL = "+
        //                   srcURL+", scriptSrc="+scriptSrc);
      }

      etable.addExtensionNamespace(declNamespace, nsh);
    }
  }
}
