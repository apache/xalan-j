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

import java.util.Stack;

import org.w3c.dom.Element;
import org.apache.xpath.res.XPATHErrorResources;
import org.apache.xalan.res.XSLMessages;

/**
 * <meta name="usage" content="general"/>
 * Class to represent a qualified name: "The name of an internal XSLT object, 
 * specifically a named template (see [7 Named Templates]), a mode (see [6.7 Modes]), 
 * an attribute set (see [8.1.4 Named Attribute Sets]), a key (see [14.2 Keys]), 
 * a locale (see [14.3 Number Formatting]), a variable or a parameter (see 
 * [12 Variables and Parameters]) is specified as a QName. If it has a prefix, 
 * then the prefix is expanded into a URI reference using the namespace declarations 
 * in effect on the attribute in which the name occurs. The expanded name 
 * consisting of the local part of the name and the possibly null URI reference 
 * is used as the name of the object. The default namespace is not used for 
 * unprefixed names."
 */
public class QName extends serialize.QName implements java.io.Serializable
{
  /**
   * The XML namespace.
   */
  public static final String S_XMLNAMESPACEURI = "http://www.w3.org/XML/1998/namespace";
  
  
  /**
   * Get the namespace of the qualified name.
   */
  public String getNamespace()
  {
    return getNamespaceURI();
  }
  
  /**
   * Get the local part of the qualified name.
   */
  public String getLocalPart()
  {
    return getLocalName();
  }

  /**
   * The cached hashcode, which is calculated at construction time.
   */
  private int m_hashCode;
  
  /**
   * Return the cached hashcode of the qualified name.
   */
  public int hashCode()
  {
    return m_hashCode;
  }
    
  /**
   * Override equals and agree that we're equal if 
   * the passed object is a string and it matches 
   * the name of the arg.
   */
  public boolean equals(String ns, String localPart)
  {
    String thisnamespace = getNamespaceURI();
    return getLocalName().equals(localPart) 
           && (((null != thisnamespace) && (null != ns)) 
               ? thisnamespace.equals(ns)
                 : ((null == thisnamespace) && (null == ns)));
  }


  /**
   * Override equals and agree that we're equal if 
   * the passed object is a QName and it matches 
   * the name of the arg.
   */
  public boolean equals(QName qname)
  {
    String thisnamespace = getNamespaceURI();
    String thatnamespace = qname.getNamespaceURI();
    return getLocalName().equals(qname.getLocalName()) 
           && (((null != thisnamespace) && (null != thatnamespace)) 
           ? thisnamespace.equals(thatnamespace)
             : ((null == thisnamespace) && (null == thatnamespace)));
  }
  
  /**
   * Construct a QName from a string, without namespace resolution.  Good 
   * for a few odd cases.
   */
  public QName(String localName)
  {
    super(null, localName);
    m_hashCode = toString().hashCode();
  }
  
  /**
   * Construct a QName from a namespace and a local name.
   */
  public QName(String ns, String localName)
  {
    super(null, localName);
    m_hashCode = toString().hashCode();
  }
  
  /**
   * This function tells if a raw attribute name is a 
   * xmlns attribute.
   */
  public static boolean isXMLNSDecl(String attRawName)
  {
    return (attRawName.startsWith("xmlns") && 
            (attRawName.equals("xmlns") ||
             attRawName.startsWith("xmlns:")));
  }

  /**
   * This function tells if a raw attribute name is a 
   * xmlns attribute.
   */
  public static String getPrefixFromXMLNSDecl(String attRawName)
  {
    int index = attRawName.indexOf(':');
    return (index >= 0) ? attRawName.substring(0, index) : "";
  }

  /**
   * Returns the local name of the given node.
   */
  public static String getLocalPart(String qname)
  {
    int index = qname.indexOf(':');
    return (index < 0) ? qname : qname.substring(index+1);
  }

  /**
   * Returns the local name of the given node.
   */
  public static String getPrefixPart(String qname)
  {
    int index = qname.indexOf(':');
    return (index >= 0) ? qname.substring(0, index) : "";
  }
  
  /**
   * Construct a QName from a string, resolving the prefix 
   * using the given namespace stack. The default namespace is 
   * not resolved.
   */
  public QName(String qname, Stack namespaces)
  {
    String namespace = null;
    String prefix = null;

    int indexOfNSSep = qname.indexOf(':');
    if(indexOfNSSep > 0)
    {
      prefix = qname.substring(0, indexOfNSSep);
      if(prefix.equals("xml"))
      {
        namespace = S_XMLNAMESPACEURI;
      }
      else if(prefix.equals("xmlns"))
      {
        return;
      }
      else
      {
        int depth = namespaces.size();
        for(int i = depth-1; i >= 0; i--)
        {
          NameSpace ns = (NameSpace)namespaces.elementAt(i);
          while(null != ns)
          {
            if((null != ns.m_prefix) && prefix.equals(ns.m_prefix))
            {
              namespace = ns.m_uri;
              i = -1;
              break;
            }
            ns = ns.m_next;
          }
        }
      }  
      if(null == namespace)
      {
        throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_PREFIX_MUST_RESOLVE, new Object[]{prefix})); //"Prefix must resolve to a namespace: "+prefix);
      }
    }
    _localName = (indexOfNSSep < 0) ? qname : qname.substring(indexOfNSSep+1);
    _namespaceURI = namespace;
    _prefix = prefix;
    m_hashCode = toString().hashCode();
  }
  
  /**
   * Construct a QName from a string, resolving the prefix 
   * using the given namespace stack. The default namespace is 
   * not resolved.
   */
  public QName(String qname, Element namespaceContext, PrefixResolver resolver)
  {
    _namespaceURI = null;

    int indexOfNSSep = qname.indexOf(':');
    if(indexOfNSSep > 0)
    {
      if(null != namespaceContext)
      {
        String prefix = qname.substring(0, indexOfNSSep);
        _prefix = prefix;
        if(prefix.equals("xml"))
        {
          _namespaceURI = S_XMLNAMESPACEURI;
        }
        else
        {
          _namespaceURI = resolver.getNamespaceForPrefix(prefix, namespaceContext);
        }  
        if(null == _namespaceURI)
        {
          throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_PREFIX_MUST_RESOLVE, new Object[]{prefix})); //"Prefix must resolve to a namespace: "+prefix);
        }
      }
      else
      {
        // TODO: error or warning...
      }
    }
    
    _localName = (indexOfNSSep < 0) ? qname : qname.substring(indexOfNSSep+1);
    m_hashCode = toString().hashCode();
  }
  
  /**
   * Construct a QName from a string, resolving the prefix 
   * using the given namespace stack. The default namespace is 
   * not resolved.
   */
  public QName(String qname, PrefixResolver resolver)
  {
    _namespaceURI = null;

    int indexOfNSSep = qname.indexOf(':');
    if(indexOfNSSep > 0)
    {
      String prefix = qname.substring(0, indexOfNSSep);
      if(prefix.equals("xml"))
      {
        _namespaceURI = S_XMLNAMESPACEURI;
      }
      else
      {
        _namespaceURI = resolver.getNamespaceForPrefix(prefix);
      }  
      if(null == _namespaceURI)
      {
        throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_PREFIX_MUST_RESOLVE, new Object[]{prefix})); //"Prefix must resolve to a namespace: "+prefix);
      }
    }
    
    _localName = (indexOfNSSep < 0) ? qname : qname.substring(indexOfNSSep+1);
    m_hashCode = toString().hashCode();
  }
  
  /**
   * Return the string representation of the namespace. Performs 
   * string concatenation, so beware of performance issues.
   */
  public String toString()
  {
    return (null != this._namespaceURI) 
           ? (this._namespaceURI + ":" + this._localName) 
             : this._localName;
  }

}
