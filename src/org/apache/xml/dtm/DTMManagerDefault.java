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

// JAXP 1.1
import javax.xml.parsers.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.Source;
import javax.xml.transform.SourceLocator;

// Apache XML Utilities
import org.apache.xml.utils.PrefixResolver;
import org.apache.xml.utils.SystemIDResolver;
import org.apache.xml.dtm.dom2dtm.DOM2DTM;
import org.apache.xml.dtm.sax2dtm.SAX2DTM;

// W3C DOM
import org.w3c.dom.Document;
import org.w3c.dom.Node;

// SAX2
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;

/**
 * The default implementation for the DTMManager.
 */
public class DTMManagerDefault extends DTMManager
{

  /** NEEDSDOC Field m_dtms          */
  protected Vector m_dtms = new Vector();

  /**
   * Constructor DTMManagerDefault
   *
   */
  public DTMManagerDefault(){}
  
  private static final boolean DUMPTREE = false;

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
  public DTM getDTM(Source source, boolean unique,
                    DTMWSFilter whiteSpaceFilter)
  {

    int documentID = m_dtms.size() << 20;

    if (source instanceof DOMSource)
    {
      DOM2DTM dtm = new DOM2DTM(this, (DOMSource) source, documentID,
                            whiteSpaceFilter);
      m_dtms.add(dtm);
      
      if(DUMPTREE)
      {
        System.out.println("Dumping DOM2DTM");
        dtm.dumpDTM();
      }

      return dtm;
    }
    else
    {
      boolean isSAXSource = (source instanceof SAXSource);
      boolean isStreamSource = (source instanceof StreamSource);

      if (isSAXSource || isStreamSource)
      {
        XMLReader reader = getXMLReader(source);

        // transformer.setIsTransformDone(false);
        InputSource xmlSource = SAXSource.sourceToInputSource(source);
        String urlOfSource = xmlSource.getSystemId();

        if (null != urlOfSource)
        {
          try
          {
            urlOfSource = SystemIDResolver.getAbsoluteURI(urlOfSource);
          }
          catch (Exception e)
          {

            // %REVIEW% Is there a better way to send a warning?
            System.err.println("Can not absolutize URL: " + urlOfSource);
          }

          xmlSource.setSystemId(urlOfSource);
        }

        try
        {
          reader.setFeature("http://apache.org/xml/features/validation/dynamic",
                            true);
        }
        catch (org.xml.sax.SAXException se) {}

        // Create the basic SAX2DTM.
        SAX2DTM dtm = new SAX2DTM(this, 
                                  source,
                                  documentID, 
                                  whiteSpaceFilter);
                                  
        // Go ahead and add the DTM to the lookup table.  This needs to be 
        // done before any parsing occurs.
        m_dtms.add(dtm);
        
        // Create a CoroutineManager to manage the coordination between the 
        // parser and the transformation.  This will "throttle" between 
        // the parser and the calling application.
        CoroutineManager coroutineManager=new CoroutineManager();
        
        // Create an CoRoutine ID for the transformation.
        int appCoroutine = coroutineManager.co_joinCoroutineSet(-1);
        // System.out.println("appCoroutine (mgr): "+appCoroutine);
        
        // %TBD% Test for a Xerces Parser, and create a 
        // CoroutineSAXParser_Xerces to avoid threading.
                                          
        // Create a CoroutineSAXParser that will run on the secondary thread.
        CoroutineSAXParser coParser=new CoroutineSAXParser(coroutineManager, appCoroutine, reader);
        
        // Have the DTM set itself up as the CoroutineSAXParser's listener.
        dtm.setCoroutineParser(coParser, appCoroutine);
        
        // Get the parser's CoRoutine ID.
        int parserCoroutine = coParser.getParserCoroutineID();
        // System.out.println("parserCoroutine (mgr): "+parserCoroutine);
    
        
        // %TBD%  It's probably OK to have these bypass the CoRoutine stuff??
        // Or maybe not?
        reader.setDTDHandler(dtm);
        reader.setErrorHandler(dtm);
                          
        try
        {
          // This is a strange way to start the parse.
          Object gotMore = coParser.doParse(xmlSource, appCoroutine);
          if (gotMore != Boolean.TRUE)
          {
      
            dtm.clearCoRoutine();
          }
        }
        catch(RuntimeException re)
        {
          // coroutineManager.co_exit(appCoroutine);
          dtm.clearCoRoutine();
          throw re;
        }
        catch(Exception e)
        {
          // coroutineManager.co_exit(appCoroutine);
          dtm.clearCoRoutine();
          throw new org.apache.xml.utils.WrappedRuntimeException(e);
        }
        finally
        {
          // coroutineManager.co_exit(appCoroutine);
        }

        if(DUMPTREE)
        {
          System.out.println("Dumping SAX2DOM");
          dtm.dumpDTM();
        }
        return dtm;
      }
      else
      {

        // It should have been handled by a derived class or the caller 
        // made a mistake.
        throw new DTMException("Not supported: " + source);
      }
    }
  }

  /**
   * This method returns the SAX2 parser to use with the InputSource
   * obtained from this URI.
   * It may return null if any SAX2-conformant XML parser can be used,
   * or if getInputSource() will also return null. The parser must
   * be free for use (i.e.
   * not currently in use for another parse().
   *
   * @param inputSource The value returned from the URIResolver.
   * @returns a SAX2 XMLReader to use to resolve the inputSource argument.
   *
   * @return non-null XMLReader reference ready to parse.
   */
  public XMLReader getXMLReader(Source inputSource)
  {

    try
    {
      XMLReader reader = (inputSource instanceof SAXSource)
                         ? ((SAXSource) inputSource).getXMLReader() : null;

      if (null == reader)
      {
        try
        {
          javax.xml.parsers.SAXParserFactory factory =
            javax.xml.parsers.SAXParserFactory.newInstance();

          factory.setNamespaceAware(true);

          javax.xml.parsers.SAXParser jaxpParser = factory.newSAXParser();

          reader = jaxpParser.getXMLReader();
        }
        catch (javax.xml.parsers.ParserConfigurationException ex)
        {
          throw new org.xml.sax.SAXException(ex);
        }
        catch (javax.xml.parsers.FactoryConfigurationError ex1)
        {
          throw new org.xml.sax.SAXException(ex1.toString());
        }
        catch (NoSuchMethodError ex2){}
        catch (AbstractMethodError ame){}

        if (null == reader)
          reader = XMLReaderFactory.createXMLReader();
      }

      try
      {
        reader.setFeature("http://xml.org/sax/features/namespace-prefixes",
                          true);
        reader.setFeature("http://apache.org/xml/features/validation/dynamic",
                          true);
      }
      catch (org.xml.sax.SAXException se)
      {

        // What can we do?
        // TODO: User diagnostics.
      }

      return reader;
    }
    catch (org.xml.sax.SAXException se)
    {
      throw new DTMException(se.getMessage(), se);
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
    return (DTM) m_dtms.elementAt(nodeHandle >> 20);
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

    for (int i = (n - 1); i >= 0; i--)
    {
      DTM tdtm = (DTM) m_dtms.elementAt(i);

      if (tdtm == dtm)
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
    if(dtm instanceof SAX2DTM)
    {
      ((SAX2DTM)dtm).clearCoRoutine();
    }

    int i = getDTMIdentity(dtm);

    // %TBD% Recover space.
    if (i >= 0)
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
    catch (Exception e)
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
