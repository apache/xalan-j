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
package org.apache.xalan.lib;

import java.util.*;
import java.io.*;
import java.net.URL;

import org.xml.sax.ContentHandler;

import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xalan.templates.StylesheetRoot;
import org.apache.xalan.templates.ElemExtensionCall;
import org.apache.xalan.templates.OutputProperties;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.XPath;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;

import org.w3c.dom.*;

/**
 * Implements three extension elements to allow an XSLT transformation to
 * redirect its output to multiple output files.
 * You must declare the Xalan namespace (xmlns:lxslt="http://xml.apache.org/xslt"),
 * a namespace for the extension prefix (such as xmlns:redirect="org.apache.xalan.lib.Redirect"),
 * and declare the extension namespace as an extension (extension-element-prefixes="redirect").
 * You can either just use redirect:write, in which case the file will be
 * opened and immediately closed after the write, or you can bracket the
 * write calls by redirect:open and redirect:close, in which case the
 * file will be kept open for multiple writes until the close call is
 * encountered.  Calls can be nested.  Calls can take a 'file' attribute
 * and/or a 'select' attribute in order to get the filename.  If a select
 * attribute is encountered, it will evaluate that expression for a string
 * that indicates the filename.  If the string evaluates to empty, it will
 * attempt to use the 'file' attribute as a default.  Filenames can be relative
 * or absolute.  If they are relative, the base directory will be the same as
 * the base directory for the output document.  This is obtained by calling
 * getOutputTarget() on the TransformerImpl.  You can set this base directory
 * by calling TransformerImpl.setOutputTarget() or it is automatically set
 * when using the two argument form of transform() or transformNode().
 *
 * <p>Example:</p>
 * <PRE>
 * &lt;?xml version="1.0"?>
 * &lt;xsl:stylesheet xmlns:xsl="http://www.w3.org/XSL/Transform/1.0"
 *                 xmlns:lxslt="http://xml.apache.org/xslt"
 *                 xmlns:redirect="org.apache.xalan.lib.Redirect"
 *                 extension-element-prefixes="redirect">
 *
 *   &lt;xsl:template match="/">
 *     &lt;out>
 *       default output.
 *     &lt;/out>
 *     &lt;redirect:open file="doc3.out"/>
 *     &lt;redirect:write file="doc3.out">
 *       &lt;out>
 *         &lt;redirect:write file="doc1.out">
 *           &lt;out>
 *             doc1 output.
 *             &lt;redirect:write file="doc3.out">
 *               Some text to doc3
 *             &lt;/redirect:write>
 *           &lt;/out>
 *         &lt;/redirect:write>
 *         &lt;redirect:write file="doc2.out">
 *           &lt;out>
 *             doc2 output.
 *             &lt;redirect:write file="doc3.out">
 *               Some more text to doc3
 *               &lt;redirect:write select="doc/foo">
 *                 text for doc4
 *               &lt;/redirect:write>
 *             &lt;/redirect:write>
 *           &lt;/out>
 *         &lt;/redirect:write>
 *       &lt;/out>
 *     &lt;/redirect:write>
 *     &lt;redirect:close file="doc3.out"/>
 *   &lt;/xsl:template>
 *
 * &lt;/xsl:stylesheet>
 * </PRE>
 *
 * @author Scott Boag
 * @version 1.0
 * @see <a href="../../../../../../extensions.html#ex-redirect" target="_top">Example with Redirect extension</a>
 */
public class Redirect
{
  /**
   * List of formatter listeners indexed by filename.
   */
  protected Hashtable m_formatterListeners = new Hashtable ();

  /**
   * List of output streams indexed by filename.
   */
  protected Hashtable m_outputStreams = new Hashtable ();

  /**
   * Open the given file and put it in the XML, HTML, or Text formatter listener's table.
   */
  public void open(XSLProcessorContext context, ElemExtensionCall elem)
    throws java.net.MalformedURLException,
           java.io.FileNotFoundException,
           java.io.IOException,
           javax.xml.transform.TransformerException
  {
    String fileName = getFilename(context, elem);
    Object flistener = m_formatterListeners.get(fileName);
    if(null == flistener)
    {
      String mkdirsExpr 
        = elem.getAttribute ("mkdirs", context.getContextNode(), 
                                                  context.getTransformer());
      boolean mkdirs = (mkdirsExpr != null)
                       ? (mkdirsExpr.equals("true") || mkdirsExpr.equals("yes")) : true;
          // ContentHandler fl = 
          makeFormatterListener(context, elem, fileName, true, mkdirs);
          // fl.startDocument();
    }
  }
  
  /**
   * Write the evalutation of the element children to the given file. Then close the file
   * unless it was opened with the open extension element and is in the formatter listener's table.
   */
  public void write(XSLProcessorContext context, ElemExtensionCall elem)
    throws java.net.MalformedURLException,
           java.io.FileNotFoundException,
           java.io.IOException,
           javax.xml.transform.TransformerException
  {
    String fileName = getFilename(context, elem);
    Object flObject = m_formatterListeners.get(fileName);
    ContentHandler formatter;
    boolean inTable = false;
    if(null == flObject)
    {
      String mkdirsExpr 
        = ((ElemExtensionCall)elem).getAttribute ("mkdirs", 
                                                  context.getContextNode(), 
                                                  context.getTransformer());
      boolean mkdirs = (mkdirsExpr != null)
                       ? (mkdirsExpr.equals("true") || mkdirsExpr.equals("yes")) : true;
      formatter = makeFormatterListener(context, elem, fileName, true, mkdirs);
    }
    else
    {
      inTable = true;
      formatter = (ContentHandler)flObject;
    }
    
    TransformerImpl transf = context.getTransformer();
    
    transf.executeChildTemplates(elem,
                                 context.getContextNode(),
                                 context.getMode(), formatter);
    
    if(!inTable)
    {
      OutputStream ostream = (OutputStream)m_outputStreams.get(fileName);
      if(null != ostream)
      {
        try
        {
          formatter.endDocument();
        }
        catch(org.xml.sax.SAXException se)
        {
          throw new TransformerException(se);
        }
        ostream.close();
        m_outputStreams.remove(fileName);
        m_formatterListeners.remove(fileName);
      }
    }
  }


  /**
   * Close the given file and remove it from the formatter listener's table.
   */
  public void close(XSLProcessorContext context, ElemExtensionCall elem)
    throws java.net.MalformedURLException,
    java.io.FileNotFoundException,
    java.io.IOException,
    javax.xml.transform.TransformerException
  {
    String fileName = getFilename(context, elem);
    Object formatterObj = m_formatterListeners.get(fileName);
    if(null != formatterObj)
    {
      ContentHandler fl = (ContentHandler)formatterObj;
      try
      {
        fl.endDocument();
      }
      catch(org.xml.sax.SAXException se)
      {
        throw new TransformerException(se);
      }
      OutputStream ostream = (OutputStream)m_outputStreams.get(fileName);
      if(null != ostream)
      {
        ostream.close();
        m_outputStreams.remove(fileName);
      }
      m_formatterListeners.remove(fileName);
    }
  }

  /**
   * Get the filename from the 'select' or the 'file' attribute.
   */
  private String getFilename(XSLProcessorContext context, ElemExtensionCall elem)
    throws java.net.MalformedURLException,
    java.io.FileNotFoundException,
    java.io.IOException,
    javax.xml.transform.TransformerException
  {
    String fileName;
    String fileNameExpr 
      = ((ElemExtensionCall)elem).getAttribute ("select", 
                                                context.getContextNode(), 
                                                context.getTransformer());
    if(null != fileNameExpr)
    {
      org.apache.xpath.XPathContext xctxt 
        = context.getTransformer().getXPathContext();
      XPath myxpath = new XPath(fileNameExpr, elem, xctxt.getNamespaceContext(), XPath.SELECT);
      XObject xobj = myxpath.execute(xctxt, context.getContextNode(), elem);
      fileName = xobj.str();
      if((null == fileName) || (fileName.length() == 0))
      {
        fileName = elem.getAttribute ("file", 
                                      context.getContextNode(), 
                                      context.getTransformer());
      }
    }
    else
    {
      fileName = elem.getAttribute ("file", context.getContextNode(), 
                                                               context.getTransformer());
    }
    if(null == fileName)
    {
      context.getTransformer().getMsgMgr().error(elem, elem, 
                                     context.getContextNode(), 
                                     XSLTErrorResources.ER_REDIRECT_COULDNT_GET_FILENAME);
                              //"Redirect extension: Could not get filename - file or select attribute must return vald string.");
    }
    return fileName;
  }
  
  // yuck.
  private String urlToFileName(String base)
  {
    if(null != base)
    {
      if(base.startsWith("file:////"))
      {
        base = base.substring(7);
      }
      else if(base.startsWith("file:///"))
      {
        base = base.substring(6);
      }
      else if(base.startsWith("file://"))
      {
        base = base.substring(5); // absolute?
      }
      else if(base.startsWith("file:/"))
      {
        base = base.substring(5);
      }
      else if(base.startsWith("file:"))
      {
        base = base.substring(4);
      }
    }
    return base;
  }

  /**
   * Create a new ContentHandler, based on attributes of the current ContentHandler.
   */
  private ContentHandler makeFormatterListener(XSLProcessorContext context,
                                               ElemExtensionCall elem,
                                               String fileName,
                                               boolean shouldPutInTable,
                                               boolean mkdirs)
    throws java.net.MalformedURLException,
    java.io.FileNotFoundException,
    java.io.IOException,
    javax.xml.transform.TransformerException
  {
    File file = new File(fileName);
    TransformerImpl transformer = context.getTransformer();
    String base;          // Base URI to use for relative paths

    if(!file.isAbsolute())
    {
      // This code is attributed to Jon Grov <jon@linpro.no>.  A relative file name
      // is relative to the Result used to kick off the transform.  If no such
      // Result was supplied, the filename is relative to the source document.
      // When transforming with a SAXResult or DOMResult, call
      // TransformerImpl.setOutputTarget() to set the desired Result base.
  //      String base = urlToFileName(elem.getStylesheet().getSystemId());

      Result outputTarget = transformer.getOutputTarget();
      if ( (null != outputTarget) && ((base = outputTarget.getSystemId()) != null) ) {
        base = urlToFileName(base);
      }
      else
      {
        base = urlToFileName(transformer.getBaseURLOfSource());
      }

      if(null != base)
      {
        File baseFile = new File(base);
        file = new File(baseFile.getParent(), fileName);
      }
      // System.out.println("file is: "+file.toString());
    }

    if(mkdirs)
    {
      String dirStr = file.getParent();
      if((null != dirStr) && (dirStr.length() > 0))
      {
        File dir = new File(dirStr);
        dir.mkdirs();
      }
    }

    // This should be worked on so that the output format can be 
    // defined by a first child of the redirect element.
    OutputProperties format = transformer.getOutputFormat();

    FileOutputStream ostream = new FileOutputStream(file);
    
    try
    {
      ContentHandler flistener 
        = transformer.createResultContentHandler(new StreamResult(ostream), format);
      try
      {
        flistener.startDocument();
      }
      catch(org.xml.sax.SAXException se)
      {
        throw new TransformerException(se);
      }
      if(shouldPutInTable)
      {
        m_outputStreams.put(fileName, ostream);
        m_formatterListeners.put(fileName, flistener);
      }
      return flistener;
    }
    catch(TransformerException te)
    {
      throw new javax.xml.transform.TransformerException(te);
    }
    
  }
}
