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
 *     the documentation and/or other materials provided with the
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
import java.util.Hashtable;

import org.apache.xpath.functions.Function;
import org.apache.xpath.functions.Function2Args;
import org.apache.xpath.XPath;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XNodeSet;
import org.apache.xpath.DOMHelper;
import org.apache.xpath.XPathContext;
import org.apache.xpath.axes.LocPathIterator;
import org.apache.xpath.axes.UnionPathIterator;
import org.apache.xalan.utils.QName;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xalan.transformer.KeyManager;

import org.apache.xpath.res.XPATHErrorResources;
import org.apache.xpath.XPathContext;

import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;


/**
 * <meta name="usage" content="advanced"/>
 * Execute the Key() function.
 */
public class FuncKey extends Function2Args
{
  static private Boolean ISTRUE = new Boolean(true);
  
  /**
   * Execute the function.  The function must return 
   * a valid object.
   * @param xctxt The current execution context.
   * @return A valid XObject.
   */
  public XObject execute(XPathContext xctxt) 
    throws org.xml.sax.SAXException
  {    
    // TransformerImpl transformer = (TransformerImpl)xctxt;
    TransformerImpl transformer = (TransformerImpl)xctxt.getOwnerObject();
    XNodeSet nodes = null;
    Node context = xctxt.getCurrentNode();
    Document docContext = (Node.DOCUMENT_NODE == context.getNodeType()) 
                          ? (Document)context : context.getOwnerDocument();
    if(null == docContext)
    {
      // path.error(context, XPATHErrorResources.ER_CONTEXT_HAS_NO_OWNERDOC); //"context does not have an owner document!");
    }
    String xkeyname = getArg0().execute(xctxt).str();
    QName keyname = new QName(xkeyname, xctxt.getNamespaceContext());
    XObject arg = getArg1().execute(xctxt);
    boolean argIsNodeSet = (XObject.CLASS_NODESET == arg.getType());
    
    KeyManager kmgr = transformer.getKeyManager();
    if(argIsNodeSet)
    {
      Hashtable usedrefs = null;
      NodeIterator ni = arg.nodeset();
      Node pos;
      UnionPathIterator upi = new UnionPathIterator();
      while(null != (pos = ni.nextNode()))
      {
        String ref = DOMHelper.getNodeData(pos);
        if(null == ref)
          continue;
        if(null == usedrefs)
          usedrefs = new Hashtable();

        if(usedrefs.get(ref) != null)
        {
          continue; // We already have 'em.
        }
        else
        {
          // ISTRUE being used as a dummy value.
          usedrefs.put(ref, ISTRUE);
        }
        
        LocPathIterator nl = kmgr.getNodeSetByKey(xctxt, docContext, 
                                           keyname, ref, 
                                           xctxt.getNamespaceContext());
        upi.addIterator(nl);
        //mnodeset.addNodesInDocOrder(nl, xctxt); needed??
      }
      upi.initContext(xctxt);
      nodes = new XNodeSet(upi);
    }
    else
    {
      String ref = arg.str();
      LocPathIterator nl = kmgr.getNodeSetByKey(xctxt, docContext, 
                                         keyname, ref, 
                                         xctxt.getNamespaceContext());
      nodes = new XNodeSet(nl);
    }
    return nodes;
  }
}
