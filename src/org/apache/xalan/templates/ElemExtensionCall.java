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

import java.io.*;

import java.util.*;

//import org.w3c.dom.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import java.util.StringTokenizer;

import org.apache.xml.utils.QName;
import org.apache.xml.utils.NameSpace;
import org.apache.xml.utils.StringToStringTable;
import org.apache.xpath.XPathContext;
import org.apache.xml.utils.MutableAttrListImpl;
import org.apache.xalan.res.XSLMessages;
import org.apache.xpath.XPathContext;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.extensions.ExtensionHandler;
import org.apache.xalan.extensions.ExtensionsTable;
import org.apache.xalan.transformer.TransformerImpl;

import javax.xml.transform.TransformerException;

import org.apache.xml.dtm.DTM;

/**
 * <meta name="usage" content="advanced"/>
 * Implement an extension element.
 * @see <a href="http://www.w3.org/TR/xslt#extension-element">extension-element in XSLT Specification</a>
 */
public class ElemExtensionCall extends ElemLiteralResult
{

  // ExtensionNSHandler nsh;

  /** The Namespace URI for this extension call element.
   *  @serial          */
  String m_extns;

  /** Language used by extension.
   *  @serial          */
  String m_lang;

  /** URL pointing to extension.
   *  @serial          */
  String m_srcURL;

  /** Source for script.
   *  @serial          */
  String m_scriptSrc;

  /** Declaration for Extension element. 
   *  @serial          */
  ElemExtensionDecl m_decl = null;

  /**
   * Get an int constant identifying the type of element.
   * @see org.apache.xalan.templates.Constants
   *
   *@return The token ID for this element
   */
  public int getXSLToken()
  {
    return Constants.ELEMNAME_EXTENSIONCALL;
  }

  /**
   * Return the node name.
   *
   * @return The element's name
   */

  // public String getNodeName()
  // {
  // TODO: Need prefix.
  // return localPart;
  // }

  /**
   * This function is called after everything else has been
   * recomposed, and allows the template to set remaining
   * values that may be based on some other property that
   * depends on recomposition.
   */
  public void compose(StylesheetRoot sroot) throws TransformerException
  {

    super.compose(sroot);
    m_extns = this.getNamespace();

    StylesheetRoot stylesheet = this.getStylesheetRoot();

    m_decl = getElemExtensionDecl(stylesheet, m_extns);

    if (null != m_decl)
    {
      for (ElemTemplateElement child = m_decl.getFirstChildElem();
              child != null; child = child.getNextSiblingElem())
      {
        if (Constants.ELEMNAME_EXTENSIONSCRIPT == child.getXSLToken())
        {
          ElemExtensionScript sdecl = (ElemExtensionScript) child;

          m_lang = sdecl.getLang();
          m_srcURL = sdecl.getSrc();

          ElemTemplateElement childOfSDecl = sdecl.getFirstChildElem();

          if (null != childOfSDecl)
          {
            if (Constants.ELEMNAME_TEXTLITERALRESULT
                    == childOfSDecl.getXSLToken())
            {
              ElemTextLiteral tl = (ElemTextLiteral) childOfSDecl;
              char[] chars = tl.getChars();

              m_scriptSrc = new String(chars);
            }
          }

          break;
        }
      }
    }
    else
    {

      // stylesheet.error(xxx);
    }
  }

  /**
   * Return the ElemExtensionDecl for this extension element 
   *
   *
   * @param stylesheet Stylesheet root associated with this extension element
   * @param namespace Namespace associated with this extension element
   *
   * @return the ElemExtensionDecl for this extension element. 
   */
  private ElemExtensionDecl getElemExtensionDecl(StylesheetRoot stylesheet,
          String namespace)
  {

    ElemExtensionDecl decl = null;
    int n = stylesheet.getGlobalImportCount();

    for (int i = 0; i < n; i++)
    {
      Stylesheet imported = stylesheet.getGlobalImport(i);

      for (ElemTemplateElement child = imported.getFirstChildElem();
              child != null; child = child.getNextSiblingElem())
      {
        if (Constants.ELEMNAME_EXTENSIONDECL == child.getXSLToken())
        {
          decl = (ElemExtensionDecl) child;

          String prefix = decl.getPrefix();
          String declNamespace = child.getNamespaceForPrefix(prefix);

          if (namespace.equals(declNamespace))
          {
            return decl;
          }
        }
      }
    }

    return decl;
  }
  
  /**
   * Execute the fallbacks when an extension is not available.
   *
   * @param transformer non-null reference to the the current transform-time state.
   * @param sourceNode non-null reference to the <a href="http://www.w3.org/TR/xslt#dt-current-node">current source node</a>.
   * @param mode reference, which may be null, to the <a href="http://www.w3.org/TR/xslt#modes">current mode</a>.
   *
   * @throws TransformerException
   */
  public void executeFallbacks(
          TransformerImpl transformer)
            throws TransformerException
  {
    for (ElemTemplateElement child = m_firstChild; child != null;
             child = child.m_nextSibling)
    {
      if (child.getXSLToken() == Constants.ELEMNAME_FALLBACK)
      {
        try
        {
          transformer.pushElemTemplateElement(child);
          ((ElemFallback) child).executeFallback(transformer);
        }
        finally
        {
          transformer.popElemTemplateElement();
        }
      }
    }

  }


  /**
   * Execute an extension.
   *
   * @param transformer non-null reference to the the current transform-time state.
   * @param sourceNode non-null reference to the <a href="http://www.w3.org/TR/xslt#dt-current-node">current source node</a>.
   * @param mode reference, which may be null, to the <a href="http://www.w3.org/TR/xslt#modes">current mode</a>.
   *
   * @throws TransformerException
   */
  public void execute(
          TransformerImpl transformer)
            throws TransformerException
  {

    try
    {
      transformer.getResultTreeHandler().flushPending();

      XPathContext liaison = ((XPathContext) transformer.getXPathContext());
      ExtensionsTable etable = liaison.getExtensionsTable();
      ExtensionHandler nsh = etable.get(m_extns);

      // We're seeing this extension namespace used for the first time.  Try to
      // autodeclare it as a java namespace.

      if (null == nsh)
      {
        nsh = etable.makeJavaNamespace(m_extns);

        if(null != nsh)
          etable.addExtensionNamespace(m_extns, nsh);
        else
        {
          executeFallbacks(transformer);
          return;
        }

      }

      try
      {
        nsh.processElement(this.getLocalName(), this, transformer,
                           getStylesheet(), this);
      }
      catch (Exception e)
      {

        // System.out.println(e);
        // e.printzStackTrace();
        String msg = e.getMessage();
        
        TransformerException te;
        if(e instanceof TransformerException)
        {
          te = (TransformerException)e;
        }
        else
        {
          if(null != msg)
            te = new TransformerException(e);
          else
            te = new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_UNKNOWN_ERROR_CALLING_EXTENSION, null), e); //"Unknown error when calling extension!", e);
        }
        if(null == te.getLocator())
          te.setLocator(this);

        if (null != msg)
        {
          if (msg.indexOf("fatal") >= 0)
          {
            transformer.getErrorListener().fatalError(te);
          }
          else if(e instanceof RuntimeException)      
            transformer.getErrorListener().error(te); // ??
          else
            transformer.getErrorListener().warning(te);

        }
        else      
          transformer.getErrorListener().error(te); // ??

        executeFallbacks(
          transformer);
      }
    }
    catch(org.xml.sax.SAXException se)
    {
      transformer.getErrorListener().fatalError(new TransformerException(se));
    }
  }

  /**
   * Return the raw value of the attribute.
   *
   * @param rawName Raw name of the attribute to get
   *
   * @return the raw value of the attribute or null if not found
   */
  public String getAttribute(String rawName)
  {

    AVT avt = getLiteralResultAttribute(rawName);

    if ((null != avt) && avt.getRawName().equals(rawName))
    {
      return avt.getSimpleString();
    }

    return null;
  }

  /**
   * Return the value of the attribute interpreted as an Attribute
   * Value Template (in other words, you can use curly expressions
   * such as href="http://{website}".
   *
   * @param rawName Raw name of the attribute to get
   * @param sourceNode non-null reference to the <a href="http://www.w3.org/TR/xslt#dt-current-node">current source node</a>.
   * @param transformer non-null reference to the the current transform-time state.
   *
   * @return the value of the attribute
   *
   * @throws TransformerException
   */
  public String getAttribute(
          String rawName, org.w3c.dom.Node sourceNode, TransformerImpl transformer)
            throws TransformerException
  {

    AVT avt = getLiteralResultAttribute(rawName);

    if ((null != avt) && avt.getRawName().equals(rawName))
    {
      XPathContext xctxt = transformer.getXPathContext();

      return avt.evaluate(xctxt, 
            xctxt.getDTMHandleFromNode(sourceNode), 
            this);
    }

    return null;
  }
  
}
