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
package org.apache.xalan.extensions;

import java.util.Hashtable;
import java.util.Vector;

import java.io.IOException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xalan.templates.Stylesheet;
import org.apache.xml.utils.QName;

import javax.xml.transform.TransformerException;

// Temp??
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.XPathProcessorException;
import org.apache.xml.utils.StringVector;

import java.lang.reflect.Method;

/**
 * <meta name="usage" content="internal"/>
 * Class handling an extension namespace for XPath. Provides functions
 * to test a function's existence and call a function
 *
 * @author Sanjiva Weerawarana (sanjiva@watson.ibm.com)
 */
public class ExtensionHandlerGeneral extends ExtensionHandler
{

  /** NEEDSDOC Field m_scriptSrc          */
  private String m_scriptSrc;  // script source to run (if any)

  /** NEEDSDOC Field m_scriptSrcURL          */
  private String m_scriptSrcURL;  // URL of source of script (if any)

  /** NEEDSDOC Field m_functions          */
  private Hashtable m_functions = new Hashtable();  // functions of namespace

  /** NEEDSDOC Field m_elements          */
  private Hashtable m_elements = new Hashtable();  // elements of namespace

  // BSF objects used to invoke BSF by reflection.  Do not import the BSF classes
  // since we don't want a compile dependency on BSF.

  /** NEEDSDOC Field m_mgr          */
  private Object m_mgr;  // BSF manager used to run scripts

  /** NEEDSDOC Field m_engine          */
  private Object m_engine;  // BSF engine used to run scripts

  // static fields

  /** NEEDSDOC Field BSF_MANAGER          */
  private static final String BSF_MANAGER = "com.ibm.bsf.BSFManager";

  /** NEEDSDOC Field managerClass          */
  private static Class managerClass;

  /** NEEDSDOC Field mgrLoadScriptingEngine          */
  private static Method mgrLoadScriptingEngine;

  /** NEEDSDOC Field BSF_ENGINE          */
  private static final String BSF_ENGINE = "com.ibm.bsf.BSFEngine";

  /** NEEDSDOC Field engineExec          */
  private static Method engineExec;  // Engine call to "compile" scripts

  /** NEEDSDOC Field engineCall          */
  private static Method engineCall;  // Engine call to invoke scripts

  /** NEEDSDOC Field NEG1INT          */
  private static final Integer NEG1INT = new Integer(-1);

  static
  {
    try
    {
      managerClass = Class.forName(BSF_MANAGER);
      mgrLoadScriptingEngine = managerClass.getMethod("loadScriptingEngine",
              new Class[]{ String.class });

      Class engineClass = Class.forName(BSF_ENGINE);

      engineExec = engineClass.getMethod("exec", new Class[]{ String.class,
                                                              Integer.TYPE,
                                                              Integer.TYPE,
                                                              Object.class });
      engineCall = engineClass.getMethod("call", new Class[]{ Object.class,
                                                              String.class,
                                                              Class.forName(
                                                                "[Ljava.lang.Object;") });
    }
    catch (Exception e)
    {
      managerClass = null;
      mgrLoadScriptingEngine = null;
      engineExec = null;
      engineCall = null;

      e.printStackTrace();
    }
  }

  /**
   * Construct a new extension namespace handler given all the information
   * needed.
   *
   * @param namespaceUri the extension namespace URI that I'm implementing
   * NEEDSDOC @param elemNames
   * @param funcNames    string containing list of functions of extension NS
   * @param lang         language of code implementing the extension
   * @param srcURL       value of src attribute (if any) - treated as a URL
   *                     or a classname depending on the value of lang. If
   *                     srcURL is not null, then scriptSrc is ignored.
   * NEEDSDOC @param scriptLang
   * NEEDSDOC @param scriptSrcURL
   * @param scriptSrc    the actual script code (if any)
   *
   * @throws TransformerException
   */
  public ExtensionHandlerGeneral(
          String namespaceUri, StringVector elemNames, StringVector funcNames, String scriptLang, String scriptSrcURL, String scriptSrc)
            throws TransformerException
  {

    super(namespaceUri, scriptLang);

    if (elemNames != null)
    {
      Object junk = new Object();
      int n = elemNames.size();

      for (int i = 0; i < n; i++)
      {
        String tok = elemNames.elementAt(i);

        m_elements.put(tok, junk);  // just stick it in there basically
      }
    }

    if (funcNames != null)
    {
      Object junk = new Object();
      int n = funcNames.size();

      for (int i = 0; i < n; i++)
      {
        String tok = funcNames.elementAt(i);

        m_functions.put(tok, junk);  // just stick it in there basically
      }
    }

    m_scriptSrcURL = scriptSrcURL;
    m_scriptSrc = scriptSrc;

    if (m_scriptSrcURL != null)
    {
      throw new TransformerException("src attribute not yet supported for "
                             + scriptLang);
    }

    if (null == managerClass)
      throw new TransformerException("Could not initialize BSF manager");

    try
    {
      m_mgr = managerClass.newInstance();
      m_engine = mgrLoadScriptingEngine.invoke(m_mgr,
                                               new Object[]{ scriptLang });

      // "Compile" the program
      engineExec.invoke(m_engine, new Object[]{ "XalanScript", NEG1INT,
                                                NEG1INT, m_scriptSrc });
    }
    catch (Exception e)
    {
      e.printStackTrace();

      throw new TransformerException("Could not compile extension", e);
    }
  }

  /**
   * Tests whether a certain function name is known within this namespace.
   * @param function name of the function being tested
   * @return true if its known, false if not.
   */
  public boolean isFunctionAvailable(String function)
  {
    return (m_functions.get(function) != null);
  }

  /**
   * Tests whether a certain element name is known within this namespace.
   * @param function name of the function being tested
   *
   * NEEDSDOC @param element
   * @return true if its known, false if not.
   */
  public boolean isElementAvailable(String element)
  {
    return (m_elements.get(element) != null);
  }

  /**
   * Process a call to a function.
   *
   * @param funcName Function name.
   * @param args     The arguments of the function call.
   * NEEDSDOC @param methodKey
   * NEEDSDOC @param exprContext
   *
   * @return the return value of the function evaluation.
   *
   * @exception XSLProcessorException thrown if something goes wrong
   *            while running the extension handler.
   * @exception MalformedURLException if loading trouble
   * @exception FileNotFoundException if loading trouble
   * @exception IOException           if loading trouble
   * @exception TransformerException          if parsing trouble
   */
  public Object callFunction(
          String funcName, Vector args, Object methodKey, ExpressionContext exprContext)
            throws TransformerException
  {

    Object[] argArray;

    try
    {
      argArray = new Object[args.size()];

      for (int i = 0; i < argArray.length; i++)
      {
        Object o = args.elementAt(i);

        argArray[i] = (o instanceof XObject) ? ((XObject) o).object() : o;
      }

      return engineCall.invoke(m_engine, new Object[]{ null, funcName,
                                                       argArray });
    }
    catch (Exception e)
    {
      e.printStackTrace();

      String msg = e.getMessage();

      if (null != msg)
      {
        if (msg.startsWith("Stopping after fatal error:"))
        {
          msg = msg.substring("Stopping after fatal error:".length());
        }

        // System.out.println("Call to extension function failed: "+msg);
        throw new TransformerException(e);
      }
      else
      {

        // Should probably make a TRaX Extension Exception.
        throw new TransformerException("Could not create extension: " + funcName
                               + " because of: " + e);
      }
    }
  }

  /**
   * Process a call to this extension namespace via an element. As a side
   * effect, the results are sent to the TransformerImpl's result tree.
   *
   * @param localPart      Element name's local part.
   * @param element        The extension element being processed.
   * @param transformer      Handle to TransformerImpl.
   * @param stylesheetTree The compiled stylesheet tree.
   * @param mode           The current mode.
   * @param sourceTree     The root of the source tree (but don't assume
   *                       it's a Document).
   * @param sourceNode     The current context node.
   * NEEDSDOC @param methodKey
   *
   * @exception XSLProcessorException thrown if something goes wrong
   *            while running the extension handler.
   * @exception MalformedURLException if loading trouble
   * @exception FileNotFoundException if loading trouble
   * @exception IOException           if loading trouble
   * @exception TransformerException          if parsing trouble
   */
  public void processElement(
          String localPart, Element element, TransformerImpl transformer, Stylesheet stylesheetTree, Node sourceTree, Node sourceNode, QName mode, Object methodKey)
            throws TransformerException, IOException
  {

    Object result = null;
    XSLProcessorContext xpc = new XSLProcessorContext(transformer,
                                stylesheetTree, sourceTree, sourceNode, mode);

    try
    {
      Vector argv = new Vector(2);

      argv.addElement(xpc);
      argv.addElement(element);

      result = callFunction(localPart, argv, methodKey,
                            transformer.getXPathContext());
    }
    catch (XPathProcessorException e)
    {

      // e.printStackTrace ();
      throw new TransformerException(e.getMessage(), e);
    }

    if (result != null)
    {
      xpc.outputToResultTree(stylesheetTree, result);
    }
  }
}
