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
package org.apache.xml.dtm;

import java.util.Vector;

import javax.xml.parsers.*;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.Source;

import org.apache.xml.utils.PrefixResolver;

import org.apache.xml.dtm.dom2dtm.DOM2DTM;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * The default implementation for the DTMManager.
 */
public class DTMManagerDefault extends DTMManager
{
  protected Vector m_dtms = new Vector();

  /**
   * Constructor DTMManagerDefault
   *
   */
  public DTMManagerDefault(){}

  /**
   * Get an instance of a DTM, loaded with the content from the
   * specified source.  If the unique flag is true, a new instance will
   * always be returned.  Otherwise it is up to the DTMManager to return a
   * new instance or an instance that it already created and may be being used
   * by someone else.
   * (I think more parameters will need to be added for error handling, and entity
   * resolution).
   *
   * @param source the specification of the source object.
   * @param unique true if the returned DTM must be unique, probably because it
   * is going to be mutated.
   * @param whiteSpaceFilter Enables filtering of whitespace nodes, and may 
   *                         be null.
   *
   * @return a non-null DTM reference.
   */
  public DTM getDTM(Source source, boolean unique, DTMWSFilter whiteSpaceFilter)
  {

    if(source instanceof DOMSource)
    {
      int documentID = m_dtms.size() << 20;
      DTM dtm = new DOM2DTM(this, (DOMSource)source, documentID, whiteSpaceFilter);
      m_dtms.add(dtm);
      return dtm;
    }
    else
    {
      throw new DTMException("Not supported: "+source);
    }
  }

  /**
   * NEEDSDOC Method getDTM 
   *
   *
   * NEEDSDOC @param nodeHandle
   *
   * NEEDSDOC (getDTM) @return
   */
  public DTM getDTM(int nodeHandle)
  {
    // Performance critical function.
    return (DTM)m_dtms.elementAt(nodeHandle >> 20);
  }
  
  /**
   * NEEDSDOC Method getDTMIdentity 
   *
   *
   * NEEDSDOC @param dtm
   *
   * NEEDSDOC (getDTMIdentity) @return
   */
  public int getDTMIdentity(DTM dtm)
  {

    // A backwards search should normally be the fastest.
    int n = m_dtms.size();
    for (int i = (n-1); i >= 0; i--) 
    {
      DTM tdtm = (DTM)m_dtms.elementAt(i);
      if(tdtm == dtm)
        return i;
    }
    
    return -1;
  }

  /**
   * NEEDSDOC Method release 
   *
   *
   * NEEDSDOC @param dtm
   * NEEDSDOC @param shouldHardDelete
   *
   * NEEDSDOC (release) @return
   */
  public boolean release(DTM dtm, boolean shouldHardDelete)
  {
    int i = getDTMIdentity(dtm);
    // %TBD% Recover space.
    if(i >= 0)
    {
      m_dtms.setElementAt(null, i);
    }

    /** @todo: implement this org.apache.xml.dtm.DTMManager abstract method */
    return true;
  }
  
  /**
   * NEEDSDOC Method createDocumentFragment 
   *
   *
   * NEEDSDOC (createDocumentFragment) @return
   */
  public DTM createDocumentFragment()
  {
    try
    {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.newDocument();
      Node df = doc.createDocumentFragment();
  
      return getDTM(new DOMSource(df), true, null);
    }
    catch(Exception e)
    {
      throw new DTMException(e);
    }
  }
  
  /**
   * NEEDSDOC Method createDTMIterator 
   *
   *
   * NEEDSDOC @param whatToShow
   * NEEDSDOC @param filter
   * NEEDSDOC @param entityReferenceExpansion
   *
   * NEEDSDOC (createDTMIterator) @return
   */
  public DTMIterator createDTMIterator(int whatToShow, DTMFilter filter,
                                       boolean entityReferenceExpansion)
  {

    /** @todo: implement this org.apache.xml.dtm.DTMManager abstract method */
    return null;
  }

  /**
   * NEEDSDOC Method createDTMIterator 
   *
   *
   * NEEDSDOC @param xpathString
   * NEEDSDOC @param presolver
   *
   * NEEDSDOC (createDTMIterator) @return
   */
  public DTMIterator createDTMIterator(String xpathString,
                                       PrefixResolver presolver)
  {

    /** @todo: implement this org.apache.xml.dtm.DTMManager abstract method */
    return null;
  }

  /**
   * NEEDSDOC Method createDTMIterator 
   *
   *
   * NEEDSDOC @param node
   *
   * NEEDSDOC (createDTMIterator) @return
   */
  public DTMIterator createDTMIterator(int node)
  {

    /** @todo: implement this org.apache.xml.dtm.DTMManager abstract method */
    return null;
  }

  /**
   * NEEDSDOC Method createDTMIterator 
   *
   *
   * NEEDSDOC @param xpathCompiler
   * NEEDSDOC @param pos
   *
   * NEEDSDOC (createDTMIterator) @return
   */
  public DTMIterator createDTMIterator(Object xpathCompiler, int pos)
  {

    /** @todo: implement this org.apache.xml.dtm.DTMManager abstract method */
    return null;
  }

}
