// ###yst - 4/20/2001, working on interfacing change to DTMDocumentImpl
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
import java.util.Hashtable;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.util.Properties;
import java.util.Enumeration;

import org.apache.xml.utils.PrefixResolver;

/**
 * <code>DTMConstructor</code> is an instance of DTMManager that can
 * be used to create DTM and DTMIterator objects, and manage the DTM
 * objects in the system.
 *
 * <p>The system property that determines which Factory implementation
 * to create is named "org.apache.xml.utils.DTMFactory". This
 * property names a concrete subclass of the DTMFactory abstract
 * class. The name needs to be set to this class to cause it be loaded as
 * the factory implementation for DTMManager.</p>
 *
 * <p>An instance of this class <emph>must</emph> be safe to use across
 * thread instances.  It is expected that a client will create a single instance
 * of a DTMManager to use across multiple threads.  This will allow sharing
 * of DTMs across multiple processes.</p>
 *
 * <p>Note: this class is incomplete right now.  It will be pretty much
 * modeled after org.apache.xml.dtm.DTMManager in terms of its
 * factory support.</p>
 *
 * <p>State: In progress!!</p>
 */
public class DTMConstructor extends DTMManager
{
  /**
   * An application will obtain a reference to this DTMManager factory instance
   * from a DTMManager constructor call that returns the instance.  Once the
   * application has obtained the reference it can use it to configure the DTM
   * object directory and obtain DTM objects.
   */

  // A DTMManager manages up to 4K (12 bits)DTM objects each of which can have
  // up to 1M (20 bits) nodes.  The data structures used for the DTMManager
  // include a DTM object symbol lookup table, a DTM object allocation table, a
  // reused DTM object table, and a DTM allocation map.
  //
  // note - the allocation map is not used initially for a quick demo of the
  // reference implementation.  The DTM object is allocated new or from the
  // reused table and stored with an index assigned in the symbol hash table.
  Hashtable dtmSymbolTable = new Hashtable();
  Vector dtmAllocList = new Vector();
  Vector dtmReuseList = new Vector();
  Vector dtmIndexReuseList= new Vector();

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
   *
   * @return a non-null DTM reference.
   */
  public DTM getDTM(javax.xml.transform.Source source,
                             boolean unique, DTMWSFilter whiteSpaceFilter){
      DTM dtmImpl;
      Integer iobj = null;

      // allocate a unique DTM instance and insert at the first available slot
      // or add to the end of the DTM allocation table
      if (unique || dtmReuseList.isEmpty()){
          dtmImpl = new DTMDocumentImpl(0);
          if (!dtmIndexReuseList.isEmpty()){
              iobj = (Integer)dtmIndexReuseList.lastElement();
              dtmIndexReuseList.removeElementAt(dtmIndexReuseList.size());
              dtmAllocList.insertElementAt(dtmImpl, iobj.intValue());
          }
          else if (dtmAllocList.size() < 4096){
                  dtmAllocList.addElement(dtmImpl);
                  iobj = new Integer(dtmAllocList.size());
               }
               // let this dtm loose to be unmanaged, iobj remains to be null
      }
      // allocate a reused DTM instance and insert at the first available slot
      // of the DTM allocation table
      // The DTM instance has been reset from a previous release operation
      else {
          dtmImpl = (DTM)dtmReuseList.lastElement();
          dtmReuseList.removeElementAt(dtmReuseList.size());
          iobj = (Integer)dtmIndexReuseList.lastElement();
          dtmIndexReuseList.removeElementAt(dtmIndexReuseList.size());
          dtmAllocList.insertElementAt(dtmImpl, iobj.intValue());
      }

                        // ### shs:  startDocument and initDocument not declared in DTM interface
                        /*
      if (iobj==null) dtmImpl.startDocument(0);
      else {
          dtmSymbolTable.put(dtmImpl,iobj);
          // ###yst: begin modification
          dtmImpl.initDocument(iobj.intValue() << 20);
          // ###yst: end modification
      }
      // %TBD%  note - need to determine how to load the source content, perhaps by
      //               passing the source object in the startDocument method
                        */
      return dtmImpl;
  }

  /**
   * Get the instance of DTM that "owns" a node handle.
   *
   * @param nodeHandle the nodeHandle.
   *
   * @return a non-null DTM reference.
   */
  public DTM getDTM(int nodeHandle){

      int dtmIndex = nodeHandle >>> 20;
      // ###yst: begin comment modification
      // can not find an owning DTM, may be a lost node,  or from an unmanaged DTM,
      // try allocating a DTM
      // %TBD% not sure we should allow this case if doc handle is zero, should direct
      // application to use the DTM constructor and request it be managed later through a
      // DTMManager register method call
      // ###yst: end comment modification
      if (dtmIndex == 0) 
                                return getDTM(null, false, null);
      // managed DTM
      return (DTM)dtmAllocList.elementAt(dtmIndex);
  }

  /**
   * Creates a DTM representing an empty <code>DocumentFragment</code> object.
   * @return a non-null DTM reference.
   */
  public DTM createDocumentFragment() {
                //  NEEDS to be worked ON!
                return null;
        }

  /**
   * Release a DTM either to a lru pool, or completely remove reference.
   * DTMs without system IDs are always hard deleted.
   * State: experimental.
   *
   * @param dtm The DTM to be released.
   * @param shouldHardDelete True if the DTM should be removed no matter what.
   * @return true if the DTM was removed, false if it was put back in a lru pool.
   */
  public boolean release(DTM dtm, boolean shouldHardDelete){

      Integer iobj = (Integer)dtmSymbolTable.remove(dtm);

      // unmanaged DTM -> hard delete, let it be garbage collected naturally
      if (iobj == null)return true;
      // release the dtm to the resue pool or remove completely
      else {
          dtmIndexReuseList.addElement(iobj);
          dtmAllocList.setElementAt(null, iobj.intValue());
          // remove completely, let it be garbage collected naturally
          if (shouldHardDelete) return true;
          else {
                                                // ###shs  Reset not declared in DTM interface
              //dtm.reset();
              dtmReuseList.addElement(dtm);
              return false;
          }
      }
  }

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
  public DTMIterator createDTMIterator(Object xpathCompiler, int pos) {
                // NEEDS TO BE WORKED ON!
                return null;
        }

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
  public DTMIterator createDTMIterator(String xpathString, PrefixResolver presolver) {
                // NEEDS TO BE WORKED ON!
                return null;
        }

  /**
   * Create a new <code>DTMIterator</code> based only on a whatToShow and
   * a DTMFilter.  The traversal semantics are defined as the descendant
   * access.
   *
   * @param whatToShow This flag specifies which node types may appear in
   *   the logical view of the tree presented by the iterator. See the
   *   description of <code>NodeFilter</code> for the set of possible
   *   <code>SHOW_</code> values.These flags can be combined using
   *   <code>OR</code>.
   * @param filter The <code>NodeFilter</code> to be used with this
   *   <code>TreeWalker</code>, or <code>null</code> to indicate no filter.
   * @param entityReferenceExpansion The value of this flag determines
   *   whether entity reference nodes are expanded.
   *
   * @return The newly created <code>DTMIterator</code>.
   */
  public DTMIterator createDTMIterator(int whatToShow, DTMFilter filter, 
                                                                                                                                                         boolean entityReferenceExpansion) {
                // NEEDS TO BE WORKED ON!
                return null;
        }

  /**
   * Create a new <code>DTMIterator</code> that holds exactly one node.
   *
   * @param node The node handle that the DTMIterator will iterate to.
   *
   * @return The newly created <code>DTMIterator</code>.
   */
  public DTMIterator createDTMIterator(int node) {
                // NEEDS TO BE WORKED ON!
                return null;
        }

    // -------------------- private methods --------------------

    /**
     * Temp debug code - this will be removed after we test everything
     */
    private static boolean debug;
    static {
        try {
            debug = System.getProperty("dtm.debug") != null;
        } catch( SecurityException ex ) {}
    }

    /** %TBD% Doc */
    static final int IDENT_DTM_DEFAULT = 0xFFF00000;

    /** %TBD% Doc */
    static final int IDENT_NODE_DEFAULT = 0x000FFFFF;

    /**
     * %TBD% Doc
     */
    public int getDTMIdentity(DTM dtm){
                        // NEEDS TO BE WORKED ON!
                        return -1;
                }

    /**
     * %TBD% Doc
     */
    public int getDTMIdentityMask()
    {
      return IDENT_DTM_DEFAULT;
    }

    /**
     * %TBD% Doc
     */
    public int getNodeIdentityMask()
    {
      return IDENT_NODE_DEFAULT;
    }

}
