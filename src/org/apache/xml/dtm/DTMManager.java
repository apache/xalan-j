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

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.util.Properties;
import java.util.Enumeration;

import org.apache.xml.utils.PrefixResolver;
import org.apache.xml.utils.XMLString;
import org.apache.xml.utils.XMLStringFactory;

import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;

/**
 * A DTMManager instance can be used to create DTM and
 * DTMIterator objects, and manage the DTM objects in the system.
 *
 * <p>The system property that determines which Factory implementation
 * to create is named "org.apache.xml.utils.DTMFactory". This
 * property names a concrete subclass of the DTMFactory abstract
 *  class. If the property is not defined, a platform default is be used.</p>
 *
 * <p>An instance of this class <emph>must</emph> be safe to use across
 * thread instances.  It is expected that a client will create a single instance
 * of a DTMManager to use across multiple threads.  This will allow sharing
 * of DTMs across multiple processes.</p>
 *
 * <p>Note: this class is incomplete right now.  It will be pretty much
 * modeled after javax.xml.transform.TransformerFactory in terms of its
 * factory support.</p>
 *
 * <p>State: In progress!!</p>
 */
public abstract class DTMManager
{

  /** The default property name to load the manager. */
  private static final String defaultPropName =
    "org.apache.xml.dtm.DTMManager";

  /**
   * Factory for creating XMLString objects.
   *  %TBD% Make this set by the caller.
   */
  protected XMLStringFactory m_xsf = null;

  /**
   * Default constructor is protected on purpose.
   */
  protected DTMManager(){}

  /**
   * Get the XMLStringFactory used for the DTMs.
   *
   *
   * @return a valid XMLStringFactory object, or null if it hasn't been set yet.
   */
  public XMLStringFactory getXMLStringFactory()
  {
    return m_xsf;
  }

  /**
   * Set the XMLStringFactory used for the DTMs.
   *
   *
   * @param xsf a valid XMLStringFactory object, should not be null.
   */
  public void setXMLStringFactory(XMLStringFactory xsf)
  {
    m_xsf = xsf;
  }

  /**
   * Obtain a new instance of a <code>DTMManager</code>.
   * This static method creates a new factory instance
   * This method uses the following ordered lookup procedure to determine
   * the <code>DTMManager</code> implementation class to
   * load:
   * <ul>
   * <li>
   * Use the <code>org.apache.xml.dtm.DTMManager</code> system
   * property.
   * </li>
   * <li>
   * Use the JAVA_HOME(the parent directory where jdk is
   * installed)/lib/jaxp.properties for a property file that contains the
   * name of the implementation class keyed on the same value as the
   * system property defined above.
   * </li>
   * <li>
   * Use the Services API (as detailed in the JAR specification), if
   * available, to determine the classname. The Services API will look
   * for a classname in the file
   * <code>META-INF/services/javax.xml.parsers.DTMManager</code>
   * in jars available to the runtime.
   * </li>
   * <li>
   * Use the default <code>DTMManager</code> classname, which is
   * <code>org.apache.xml.dtm.ref.DTMManagerDefault</code>.
   * </li>
   * </ul>
   *
   * Once an application has obtained a reference to a <code>
   * DTMManager</code> it can use the factory to configure
   * and obtain parser instances.
   *
   * @return new DTMManager instance, never null.
   *
   * @throws DTMConfigurationException
   * if the implementation is not available or cannot be instantiated.
   */
  public static DTMManager newInstance(XMLStringFactory xsf) 
           throws DTMConfigurationException
  {

    String classname = findFactory(defaultPropName,
                                   "org.apache.xml.dtm.ref.DTMManagerDefault");

    if (classname == null)
    {
      throw new DTMConfigurationException(XSLMessages.createMessage(XSLTErrorResources.ER_NO_DEFAULT_IMPL, null)); //"No default implementation found");
    }

    DTMManager factoryImpl;

    try
    {
      Class clazz = Class.forName(classname);

      factoryImpl = (DTMManager) clazz.newInstance();
    }
    catch (ClassNotFoundException cnfe)
    {
      throw new DTMConfigurationException(cnfe);
    }
    catch (IllegalAccessException iae)
    {
      throw new DTMConfigurationException(iae);
    }
    catch (InstantiationException ie)
    {
      throw new DTMConfigurationException(ie);
    }
    factoryImpl.setXMLStringFactory(xsf);

    return factoryImpl;
  }

  /**
   * Get an instance of a DTM, loaded with the content from the
   * specified source.  If the unique flag is true, a new instance will
   * always be returned.  Otherwise it is up to the DTMManager to return a
   * new instance or an instance that it already created and may be being used
   * by someone else.
   * (I think more parameters will need to be added for error handling, and entity
   * resolution).
   *
   * @param source the specification of the source object, which may be null,
   *               in which case it is assumed that node construction will take
   *               by some other means.
   * @param unique true if the returned DTM must be unique, probably because it
   * is going to be mutated.
   * @param whiteSpaceFilter Enables filtering of whitespace nodes, and may
   *                         be null.
   * @param incremental true if the DTM should be built incrementally, if
   *                    possible.
   * @param doIndexing true if the caller considers it worth it to use 
   *                   indexing schemes.
   *
   * @return a non-null DTM reference.
   */
  public abstract DTM getDTM(javax.xml.transform.Source source,
                             boolean unique, DTMWSFilter whiteSpaceFilter,
                             boolean incremental, boolean doIndexing);

  /**
   * Get the instance of DTM that "owns" a node handle.
   *
   * @param nodeHandle the nodeHandle.
   *
   * @return a non-null DTM reference.
   */
  public abstract DTM getDTM(int nodeHandle);

  /**
   * Given a W3C DOM node, try and return a DTM handle.
   * Note: calling this may be non-optimal.
   *
   * @param node Non-null reference to a DOM node.
   *
   * @return a valid DTM handle.
   */
  public abstract int getDTMHandleFromNode(org.w3c.dom.Node node);

  /**
   * Creates a DTM representing an empty <code>DocumentFragment</code> object.
   * @return a non-null DTM reference.
   */
  public abstract DTM createDocumentFragment();

  /**
   * Release a DTM either to a lru pool, or completely remove reference.
   * DTMs without system IDs are always hard deleted.
   * State: experimental.
   *
   * @param dtm The DTM to be released.
   * @param shouldHardDelete True if the DTM should be removed no matter what.
   * @return true if the DTM was removed, false if it was put back in a lru pool.
   */
  public abstract boolean release(DTM dtm, boolean shouldHardDelete);

  /**
   * Create a new <code>DTMIterator</code> based on an XPath
   * <a href="http://www.w3.org/TR/xpath#NT-LocationPath>LocationPath</a> or
   * a <a href="http://www.w3.org/TR/xpath#NT-UnionExpr">UnionExpr</a>.
   *
   * @param xpathCompiler ??? Somehow we need to pass in a subpart of the
   * expression.  I hate to do this with strings, since the larger expression
   * has already been parsed.
   *
   * @param pos The position in the expression.
   * @return The newly created <code>DTMIterator</code>.
   */
  public abstract DTMIterator createDTMIterator(Object xpathCompiler,
          int pos);

  /**
   * Create a new <code>DTMIterator</code> based on an XPath
   * <a href="http://www.w3.org/TR/xpath#NT-LocationPath>LocationPath</a> or
   * a <a href="http://www.w3.org/TR/xpath#NT-UnionExpr">UnionExpr</a>.
   *
   * @param xpathString Must be a valid string expressing a
   * <a href="http://www.w3.org/TR/xpath#NT-LocationPath>LocationPath</a> or
   * a <a href="http://www.w3.org/TR/xpath#NT-UnionExpr">UnionExpr</a>.
   *
   * @param presolver An object that can resolve prefixes to namespace URLs.
   *
   * @return The newly created <code>DTMIterator</code>.
   */
  public abstract DTMIterator createDTMIterator(String xpathString,
          PrefixResolver presolver);

  /**
   * Create a new <code>DTMIterator</code> based only on a whatToShow
   * and a DTMFilter.  The traversal semantics are defined as the
   * descendant access.
   * <p>
   * Note that DTMIterators may not be an exact match to DOM
   * NodeIterators. They are initialized and used in much the same way
   * as a NodeIterator, but their response to document mutation is not
   * currently defined.
   *
   * @param whatToShow This flag specifies which node types may appear in
   *   the logical view of the tree presented by the iterator. See the
   *   description of <code>NodeFilter</code> for the set of possible
   *   <code>SHOW_</code> values.These flags can be combined using
   *   <code>OR</code>.
   * @param filter The <code>NodeFilter</code> to be used with this
   *   <code>DTMFilter</code>, or <code>null</code> to indicate no filter.
   * @param entityReferenceExpansion The value of this flag determines
   *   whether entity reference nodes are expanded.
   *
   * @return The newly created <code>DTMIterator</code>.
   */
  public abstract DTMIterator createDTMIterator(int whatToShow,
          DTMFilter filter, boolean entityReferenceExpansion);

  /**
   * Create a new <code>DTMIterator</code> that holds exactly one node.
   *
   * @param node The node handle that the DTMIterator will iterate to.
   *
   * @return The newly created <code>DTMIterator</code>.
   */
  public abstract DTMIterator createDTMIterator(int node);
  
  /* Flag indicating whether an incremental transform is desired */
  public static boolean m_incremental = false;  
  
  /**
   * Set a flag indicating whether an incremental transform is desired 
   * @param incremental boolean to use to set m_incremental.
   *
   */
  public synchronized static boolean getIncremental()
  {
    return m_incremental;  
  }
  
  /**
   * Set a flag indicating whether an incremental transform is desired 
   * @param incremental boolean to use to set m_incremental.
   *
   */
  public synchronized static void setIncremental(boolean incremental)
  {
    m_incremental = incremental;  
  }
  
  

  // -------------------- private methods --------------------

  /**
   * Avoid reading all the files when the findFactory
   * method is called the second time (cache the result of
   * finding the default impl).
   */
  private static String foundFactory = null;

  /**
   * Temp debug code - this will be removed after we test everything
   */
  private static boolean debug;

  static
  {
    try
    {
      debug = System.getProperty("dtm.debug") != null;
    }
    catch (SecurityException ex){}
  }

  /**
   * Private implementation method - will find the implementation
   * class in the specified order.
   *
   * @param factoryId   Name of the factory interface.
   * @param xmlProperties Name of the properties file based on JAVA/lib.
   * @param defaultFactory Default implementation, if nothing else is found.
   *
   * @return The factory class name.
   */
  private static String findFactory(String factoryId, String defaultFactory)
  {

    // Use the system property first
    try
    {
      String systemProp = null;

      try
      {
        systemProp = System.getProperty(factoryId);
      }
      catch (SecurityException se){}

      if (systemProp != null)
      {
        if (debug)
        {
          System.err.println("DTM: found system property" + systemProp);
        }

        return systemProp;
      }
    }
    catch (SecurityException se){}

    if (foundFactory != null)
    {
      return foundFactory;
    }

    // try to read from $java.home/lib/jaxp.properties
    try
    {
      String javah = System.getProperty("java.home");
      String configFile = javah + File.separator + "lib" + File.separator
                          + "jaxp.properties";
      File f = new File(configFile);

      if (f.exists())
      {
        Properties props = new Properties();

        props.load(new FileInputStream(f));

        foundFactory = props.getProperty(factoryId);

        if (debug)
        {
          System.err.println("DTM: found java.home property " + foundFactory);
        }

        if (foundFactory != null)
        {
          return foundFactory;
        }
      }
    }
    catch (Exception ex)
    {
      if (debug)
      {
        ex.printStackTrace();
      }
    }

    String serviceId = "META-INF/services/" + factoryId;

    // try to find services in CLASSPATH
    try
    {
      ClassLoader cl = DTMManager.class.getClassLoader();
      InputStream is = null;

      if (cl == null)
      {
        is = ClassLoader.getSystemResourceAsStream(serviceId);
      }
      else
      {
        is = cl.getResourceAsStream(serviceId);
      }

      if (is != null)
      {
        if (debug)
        {
          System.err.println("DTM: found  " + serviceId);
        }

        BufferedReader rd = new BufferedReader(new InputStreamReader(is));

        foundFactory = rd.readLine();

        rd.close();

        if (debug)
        {
          System.err.println("DTM: loaded from services: " + foundFactory);
        }

        if ((foundFactory != null) &&!"".equals(foundFactory))
        {
          return foundFactory;
        }
      }
    }
    catch (Exception ex)
    {
      if (debug)
      {
        ex.printStackTrace();
      }
    }

    return defaultFactory;
  }


  /** This value, set at compile time, controls how many bits of the
   * DTM node identifier numbers are used to identify a node within a
   * document, and thus sets the maximum number of nodes per
   * document. The remaining bits are used to identify the DTM
   * document which contains this node.
   *
   * If you change IDENT_DTM_NODE_BITS, be sure to rebuild _ALL_ the
   * files which use it... including the IDKey testcases.
   *
   * (FuncGenerateKey currently uses the node identifier directly and
   * thus is sensitive to its format. The IDKEY results will still be
   * _correct_ (presuming no other breakage), but simple equality
   * comparison against the previous "golden" files will probably
   * complain.)  */
  public static final int IDENT_DTM_NODE_BITS = 22;
    

  /** When this bitmask is ANDed with a DTM node handle number, the result
   * is the node's index number within that DTM.
   */
  public static final int IDENT_NODE_DEFAULT = (1<<IDENT_DTM_NODE_BITS)-1;


  /** When this bitmask is ANDed with a DTM node handle number, the result
   * is the DTM's document identity number.
   */
  public static final int IDENT_DTM_DEFAULT = ~IDENT_NODE_DEFAULT;

  /** This is the maximum number of DTMs available.  The highest DTM is
    * one less than this.
   */
  public static final int IDENT_MAX_DTMS = (IDENT_DTM_DEFAULT >>> IDENT_DTM_NODE_BITS) + 1;


  /**
   * %TBD% Doc
   *
   * NEEDSDOC @param dtm
   *
   * NEEDSDOC ($objectName$) @return
   */
  public abstract int getDTMIdentity(DTM dtm);

  /**
   * %TBD% Doc
   *
   * NEEDSDOC ($objectName$) @return
   */
  public int getDTMIdentityMask()
  {
    return IDENT_DTM_DEFAULT;
  }

  /**
   * %TBD% Doc
   *
   * NEEDSDOC ($objectName$) @return
   */
  public int getNodeIdentityMask()
  {
    return IDENT_NODE_DEFAULT;
  }

}
