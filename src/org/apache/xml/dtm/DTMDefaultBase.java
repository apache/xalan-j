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

import org.apache.xml.dtm.*;
import org.apache.xml.utils.IntVector;
import org.apache.xml.utils.IntStack;
import org.apache.xml.utils.BoolStack;
import org.apache.xml.utils.StringBufferPool;
import org.apache.xml.utils.FastStringBuffer;
import org.apache.xml.utils.TreeWalker;
import org.apache.xml.utils.QName;
import org.apache.xml.utils.XMLCharacterRecognizer;

import java.util.Vector;

import org.xml.sax.ContentHandler;

import org.apache.xml.utils.NodeVector;

import javax.xml.transform.Source;

import org.apache.xml.utils.XMLString;
import org.apache.xml.utils.XMLStringFactory;

/**
 * The <code>DTMDefaultBase</code> class serves as a helper base for DTMs.
 * It sets up structures for navigation and type, while leaving data
 * management and construction to the derived classes.
 */
public abstract class DTMDefaultBase implements DTM
{

  /**
   * The number of nodes, which is also used to determine the next
   *  node index.
   */
  protected int m_size = 0;

  /** The expanded names, one array element for each node. */
  protected int[] m_exptype;

  /** levels deep, one array element for each node. */
  protected byte[] m_level;

  /** First child values, one array element for each node. */
  protected int[] m_firstch;

  /** Next sibling values, one array element for each node. */
  protected int[] m_nextsib;

  /** Previous sibling values, one array element for each node. */
  protected short[] m_prevsib;

  /** Previous sibling values, one array element for each node. */
  protected short[] m_parent;

  /** The default initial block size of the node arrays */
  protected int m_initialblocksize = 512;  // favor small docs.

  /** Size of blocks to allocate */
  protected int m_blocksize = 2 * 1024;

  /**
   * The value to use when the information has not been built yet.
   */
  protected static final int NOTPROCESSED = DTM.NULL - 1;

  /** Not sure if this is used? */
  protected static final int NODEIDENTITYBITS = 0x000FFFFF;

  /**
   * The DTM manager who "owns" this DTM.
   */
  protected DTMManager m_mgr;

  /** The document identity, which is OR'd with node indexes to make handles. */
  protected int m_dtmIdent;

  /** The mask for the identity.  %REVIEW% static constant? */
  protected int m_mask;

  /** The base URI for this document. */
  protected String m_documentBaseURI;

  /**
   * The whitespace filter that enables elements to strip whitespace or not.
   */
  protected DTMWSFilter m_wsfilter;

  /** Flag indicating whether to strip whitespace nodes */
  protected boolean m_shouldStripWS = false;

  /** Stack of flags indicating whether to strip whitespace nodes */
  protected BoolStack m_shouldStripWhitespaceStack;

  /** The XMLString factory for creating XMLStrings. */
  protected XMLStringFactory m_xstrf;

  /** The identity of the root node. */
  public static final int ROOTNODE = 0;

  /**
   * Construct a DTMDefaultBase object from a DOM node.
   *
   * @param mgr The DTMManager who owns this DTM.
   * @param domSource the DOM source that this DTM will wrap.
   * @param source The object that is used to specify the construction source.
   * @param dtmIdentity The DTM identity ID for this DTM.
   * @param whiteSpaceFilter The white space filter for this DTM, which may
   *                         be null.
   * @param xstringfactory The factory to use for creating XMLStrings.
   */
  public DTMDefaultBase(DTMManager mgr, Source source, int dtmIdentity,
                        DTMWSFilter whiteSpaceFilter,
                        XMLStringFactory xstringfactory)
  {

    m_exptype = new int[m_initialblocksize];
    m_level = new byte[m_initialblocksize];
    m_firstch = new int[m_initialblocksize];
    m_nextsib = new int[m_initialblocksize];
    m_prevsib = new short[m_initialblocksize];
    m_parent = new short[m_initialblocksize];
    m_mgr = mgr;
    m_documentBaseURI = (null != source) ? source.getSystemId() : null;
    m_dtmIdent = dtmIdentity;
    m_mask = mgr.getNodeIdentityMask();
    m_wsfilter = whiteSpaceFilter;
    m_xstrf = xstringfactory;

    if (null != whiteSpaceFilter)
    {
      m_shouldStripWhitespaceStack = new BoolStack();

      pushShouldStripWhitespace(false);
    }
  }

  /**
   * Get the next node identity value in the list, and call the iterator
   * if it hasn't been added yet.
   *
   * @param identity The node identity (index).
   * @return identity+1, or DTM.NULL.
   */
  protected abstract int getNextNodeIdentity(int identity);

  /**
   * This method should try and build one or more nodes in the table.
   *
   * @return The true if a next node is found or false if
   *         there are no more nodes.
   */
  protected abstract boolean nextNode();

  /**
   * Get the number of nodes that have been added.
   *
   * @return the number of nodes that have been mapped.
   */
  protected abstract int getNumberOfNodes();

  /** Stateless axis traversers, lazely built. */
  protected DTMAxisTraverser[] m_traversers;

  /**
   * Ensure that the size of the information arrays can hold another entry
   * at the given index.
   *
   * @param on exit from this function, the information arrays sizes must be
   * at least index+1.
   *
   * NEEDSDOC @param index
   */
  protected void ensureSize(int index)
  {

    int capacity = m_exptype.length;

    if (capacity <= index)
    {
      int newcapacity = capacity + m_blocksize;

      // %OPT% Compilers might be happier if we operated on one array
      // at a time, though the parallel code might be a trifle less
      // obvious.
      
      int[] exptype = m_exptype;
      byte[] level = m_level;
      int[] firstch = m_firstch;
      int[] nextsib = m_nextsib;
      short[] prevsib = m_prevsib;
      short[] parent = m_parent;

      m_exptype = new int[newcapacity];
      m_level = new byte[newcapacity];
      m_firstch = new int[newcapacity];
      m_nextsib = new int[newcapacity];
      m_prevsib = new short[newcapacity];
      m_parent = new short[newcapacity];

      System.arraycopy(exptype, 0, m_exptype, 0, capacity);
      System.arraycopy(level, 0, m_level, 0, capacity);
      System.arraycopy(firstch, 0, m_firstch, 0, capacity);
      System.arraycopy(nextsib, 0, m_nextsib, 0, capacity);
      System.arraycopy(prevsib, 0, m_prevsib, 0, capacity);
      System.arraycopy(parent, 0, m_parent, 0, capacity);

      // %REVIEW%
      m_blocksize = m_blocksize + m_blocksize;
    }
  }

  /**
   * Get the simple type ID for the given node identity.
   *
   * @param identity The node identity.
   *
   * @return The simple type ID, or DTM.NULL.
   */
  protected int _type(int identity)
  {

    int info = getExpandedTypeID(identity);

    if (NULL != info)
      return ExpandedNameTable.getType(info);
    else
      return NULL;
  }

  /**
   * Get the expanded type ID for the given node identity.
   *
   * @param identity The node identity.
   *
   * @return The expanded type ID, or DTM.NULL.
   */
  protected int _exptype(int identity)
  {

    if (identity < m_size)
      return m_exptype[identity];

    // Check to see if the information requested has been processed, and, 
    // if not, advance the iterator until we the information has been 
    // processed.
    while (true)
    {
      boolean isMore = nextNode();

      if (!isMore)
        return NULL;
      else if (identity < m_size)
        return m_exptype[identity];
    }
  }

  /**
   * Get the level in the tree for the given node identity.
   *
   * @param identity The node identity.
   *
   * @return The tree level, or DTM.NULL.
   */
  protected int _level(int identity)
  {

    if (identity < m_size)
      return m_level[identity];

    // Check to see if the information requested has been processed, and, 
    // if not, advance the iterator until we the information has been 
    // processed.
    while (true)
    {
      boolean isMore = nextNode();

      if (!isMore)
        return NULL;
      else if (identity < m_size)
        return m_level[identity];
    }
  }

  /**
   * Get the first child for the given node identity.
   *
   * @param identity The node identity.
   *
   * @return The first child identity, or DTM.NULL.
   */
  protected int _firstch(int identity)
  {

    // Boiler-plate code for each of the _xxx functions, except for the array.
    int info = (identity >= m_size) ? NOTPROCESSED : m_firstch[identity];

    // Check to see if the information requested has been processed, and, 
    // if not, advance the iterator until we the information has been 
    // processed.
    while (info == NOTPROCESSED)
    {
      boolean isMore = nextNode();

      if (identity >= m_size &&!isMore)
        return NULL;
      else
        info = m_firstch[identity];
    }

    return info;
  }

  /**
   * Get the next sibling for the given node identity.
   *
   * @param identity The node identity.
   *
   * @return The next sibling identity, or DTM.NULL.
   */
  protected int _nextsib(int identity)
  {

    // Boiler-plate code for each of the _xxx functions, except for the array.
    int info = (identity >= m_size) ? NOTPROCESSED : m_nextsib[identity];

    // Check to see if the information requested has been processed, and, 
    // if not, advance the iterator until we the information has been 
    // processed.
    while (info == NOTPROCESSED)
    {
      boolean isMore = nextNode();

      if (identity >= m_size &&!isMore)
        return NULL;
      else
        info = m_nextsib[identity];
    }

    return info;
  }

  /**
   * Get the previous sibling for the given node identity.
   *
   * @param identity The node identity.
   *
   * @return The previous sibling identity, or DTM.NULL.
   */
  protected int _prevsib(int identity)
  {

    if (identity < m_size)
      return m_prevsib[identity];

    // Check to see if the information requested has been processed, and, 
    // if not, advance the iterator until we the information has been 
    // processed.
    while (true)
    {
      boolean isMore = nextNode();

      if (!isMore)
        return NULL;
      else if (identity < m_size)
        return m_prevsib[identity];
    }
  }

  /**
   * Get the parent for the given node identity.
   *
   * @param identity The node identity.
   *
   * @return The parent identity, or DTM.NULL.
   */
  protected int _parent(int identity)
  {

    if (identity < m_size)
      return m_parent[identity];

    // Check to see if the information requested has been processed, and, 
    // if not, advance the iterator until we the information has been 
    // processed.
    while (true)
    {
      boolean isMore = nextNode();

      if (!isMore)
        return NULL;
      else if (identity < m_size)
        return m_parent[identity];
    }
  }

  /**
   * Diagnostics function to dump the DTM.
   */
  public void dumpDTM()
  {

    while (nextNode()){}

    int nRecords = m_size;

    System.out.println("Total nodes: " + nRecords);

    for (int i = 0; i < nRecords; i++)
    {
      System.out.println("=========== " + i + " ===========");
      System.out.println("NodeName: " + getNodeName(i));
      System.out.println("NodeNameX: " + getNodeNameX(i));
      System.out.println("LocalName: " + getLocalName(i));
      System.out.println("NamespaceURI: " + getNamespaceURI(i));
      System.out.println("Prefix: " + getPrefix(i));

      int exTypeID = getExpandedTypeID(i);

      System.out.println("Expanded Type ID: "
                         + Integer.toHexString(exTypeID));

      int type = getNodeType(i);
      String typestring;

      switch (type)
      {
      case DTM.ATTRIBUTE_NODE :
        typestring = "ATTRIBUTE_NODE";
        break;
      case DTM.CDATA_SECTION_NODE :
        typestring = "CDATA_SECTION_NODE";
        break;
      case DTM.COMMENT_NODE :
        typestring = "COMMENT_NODE";
        break;
      case DTM.DOCUMENT_FRAGMENT_NODE :
        typestring = "DOCUMENT_FRAGMENT_NODE";
        break;
      case DTM.DOCUMENT_NODE :
        typestring = "DOCUMENT_NODE";
        break;
      case DTM.DOCUMENT_TYPE_NODE :
        typestring = "DOCUMENT_NODE";
        break;
      case DTM.ELEMENT_NODE :
        typestring = "ELEMENT_NODE";
        break;
      case DTM.ENTITY_NODE :
        typestring = "ENTITY_NODE";
        break;
      case DTM.ENTITY_REFERENCE_NODE :
        typestring = "ENTITY_REFERENCE_NODE";
        break;
      case DTM.NAMESPACE_NODE :
        typestring = "NAMESPACE_NODE";
        break;
      case DTM.NOTATION_NODE :
        typestring = "NOTATION_NODE";
        break;
      case DTM.NULL :
        typestring = "NULL";
        break;
      case DTM.PROCESSING_INSTRUCTION_NODE :
        typestring = "PROCESSING_INSTRUCTION_NODE";
        break;
      case DTM.TEXT_NODE :
        typestring = "TEXT_NODE";
        break;
      default :
        typestring = "Unknown!";
        break;
      }

      System.out.println("Type: " + typestring);

      int firstChild = _firstch(i);

      if (DTM.NULL == firstChild)
        System.out.println("First child: DTM.NULL");
      else if (NOTPROCESSED == firstChild)
        System.out.println("First child: NOTPROCESSED");
      else
        System.out.println("First child: " + firstChild);

      int prevSibling = _prevsib(i);

      if (DTM.NULL == prevSibling)
        System.out.println("Prev sibling: DTM.NULL");
      else if (NOTPROCESSED == prevSibling)
        System.out.println("Prev sibling: NOTPROCESSED");
      else
        System.out.println("Prev sibling: " + prevSibling);

      int nextSibling = _nextsib(i);

      if (DTM.NULL == nextSibling)
        System.out.println("Next sibling: DTM.NULL");
      else if (NOTPROCESSED == nextSibling)
        System.out.println("Next sibling: NOTPROCESSED");
      else
        System.out.println("Next sibling: " + nextSibling);

      int parent = _parent(i);

      if (DTM.NULL == parent)
        System.out.println("Parent: DTM.NULL");
      else if (NOTPROCESSED == parent)
        System.out.println("Parent: NOTPROCESSED");
      else
        System.out.println("Parent: " + parent);

      int level = _level(i);

      System.out.println("Level: " + level);
      System.out.println("Node Value: " + getNodeValue(i));
      System.out.println("String Value: " + getStringValue(i));
    }
  }

  // ========= DTM Implementation Control Functions. ==============

  /**
   * Set an implementation dependent feature.
   * <p>
   * %REVIEW% Do we really expect to set features on DTMs?
   *
   * @param featureId A feature URL.
   * @param state true if this feature should be on, false otherwise.
   */
  public void setFeature(String featureId, boolean state){}

  // ========= Document Navigation Functions =========

  /**
   * Given a node handle, test if it has child nodes.
   * <p> %REVIEW% This is obviously useful at the DOM layer, where it
   * would permit testing this without having to create a proxy
   * node. It's less useful in the DTM API, where
   * (dtm.getFirstChild(nodeHandle)!=DTM.NULL) is just as fast and
   * almost as self-evident. But it's a convenience, and eases porting
   * of DOM code to DTM.  </p>
   *
   * @param nodeHandle int Handle of the node.
   * @return int true if the given node has child nodes.
   */
  public boolean hasChildNodes(int nodeHandle)
  {

    int identity = nodeHandle & m_mask;
    int firstChild = _firstch(identity);

    return firstChild != DTM.NULL;
  }

  /**
   * Given a node handle, get the handle of the node's first child.
   * If not yet resolved, waits for more nodes to be added to the document and
   * tries again.
   *
   * @param nodeHandle int Handle of the node.
   * @return int DTM node-number of first child, or DTM.NULL to indicate none exists.
   */
  public int getFirstChild(int nodeHandle)
  {

    int identity = nodeHandle & m_mask;
    int firstChild = _firstch(identity);

    return firstChild | m_dtmIdent;
  }

  /**
   * Given a node handle, advance to its last child.
   * If not yet resolved, waits for more nodes to be added to the document and
   * tries again.
   *
   * @param nodeHandle int Handle of the node.
   * @return int Node-number of last child,
   * or DTM.NULL to indicate none exists.
   */
  public int getLastChild(int nodeHandle)
  {

    int identity = nodeHandle & m_mask;
    int child = _firstch(identity);
    int lastChild = DTM.NULL;

    while (child != DTM.NULL)
    {
      lastChild = child;
      child = _nextsib(child);
    }

    return lastChild | m_dtmIdent;
  }

  /**
   * Retrieves an attribute node by by qualified name and namespace URI.
   *
   * @param nodeHandle int Handle of the node upon which to look up this attribute..
   * @param namespaceURI The namespace URI of the attribute to
   *   retrieve, or null.
   * @param name The local name of the attribute to
   *   retrieve.
   * @return The attribute node handle with the specified name (
   *   <code>nodeName</code>) or <code>DTM.NULL</code> if there is no such
   *   attribute.
   */
  public abstract int getAttributeNode(int nodeHandle, String namespaceURI,
                                       String name);

  /**
   * Given a node handle, get the index of the node's first attribute.
   *
   * @param nodeHandle int Handle of the node.
   * @return Handle of first attribute, or DTM.NULL to indicate none exists.
   */
  public int getFirstAttribute(int nodeHandle)
  {

    int type = getNodeType(nodeHandle);

    if (DTM.ELEMENT_NODE == type)
    {

      // Assume that attributes and namespaces immediately follow the element.
      int identity = nodeHandle & m_mask;

      while (DTM.NULL != (identity = getNextNodeIdentity(identity)))
      {

        // Assume this can not be null.
        type = getNodeType(identity);

        if (type == DTM.ATTRIBUTE_NODE)
        {
          return identity | m_dtmIdent;
        }
        else if (DTM.NAMESPACE_NODE != type)
        {
          break;
        }
      }
    }

    return DTM.NULL;
  }

  /**
   * Given a node handle, advance to its next sibling.
   * If not yet resolved, waits for more nodes to be added to the document and
   * tries again.
   * @param nodeHandle int Handle of the node.
   * @return int Node-number of next sibling,
   * or DTM.NULL to indicate none exists.
   */
  public int getNextSibling(int nodeHandle)
  {

    int identity = nodeHandle & m_mask;
    int nextSibling = _nextsib(identity);

    return nextSibling | m_dtmIdent;
  }

  /**
   * Given a node handle, find its preceeding sibling.
   * WARNING: DTM is asymmetric; this operation is resolved by search, and is
   * relatively expensive.
   *
   * @param nodeHandle the id of the node.
   * @return int Node-number of the previous sib,
   * or DTM.NULL to indicate none exists.
   */
  public int getPreviousSibling(int nodeHandle)
  {
    return _prevsib(nodeHandle & m_mask) | m_dtmIdent;
  }

  /**
   * Given a node handle, advance to the next attribute. If an
   * element, we advance to its first attribute; if an attr, we advance to
   * the next attr on the same node.
   *
   * @param nodeHandle int Handle of the node.
   * @return int DTM node-number of the resolved attr,
   * or DTM.NULL to indicate none exists.
   */
  public int getNextAttribute(int nodeHandle)
  {

    int type = getNodeType(nodeHandle);

    if (DTM.ATTRIBUTE_NODE == type)
    {

      // Assume that attributes and namespace nodes immediately follow the element.
      int identity = nodeHandle & m_mask;

      while (DTM.NULL != (identity = getNextNodeIdentity(identity)))
      {
        type = getNodeType(identity);

        if (type == DTM.ATTRIBUTE_NODE)
        {
          return identity | m_dtmIdent;
        }
        else if (type != DTM.NAMESPACE_NODE)
        {
          break;
        }
      }
    }

    return DTM.NULL;
  }

  /** Lazily created namespace lists. */
  private Vector m_namespaceLists = null;  // on demand

  /**
   * Get a full list of namespace handles for the given element.  In other words
   * get all the namespace nodes in context for the given element.
   *
   *
   * @param baseHandle The base element handle.
   *
   * @return A list of namespace handles for this element.
   */
  protected NodeVector getNamespaceList(int baseHandle)
  {

    if (null == m_namespaceLists)
      m_namespaceLists = new Vector();
    else
    {
      int n = m_namespaceLists.size();

      for (int i = (n - 1); i >= 0; i--)
      {
        NodeVector ivec = (NodeVector) m_namespaceLists.elementAt(i);

        if (ivec.elementAt(0) == baseHandle)
          return ivec;
      }
    }

    NodeVector ivec = buildNamespaceList(baseHandle);

    m_namespaceLists.addElement(ivec);

    return ivec;
  }

  /**
   * Build a list of all namespace nodes in context for this element.
   *
   *
   * @param baseHandle The base element handle.
   *
   * @return a NodeVector that contains a list of all namespace nodes in
   * context for this element.
   */
  private NodeVector buildNamespaceList(int baseHandle)
  {

    NodeVector ivec = new NodeVector(7);

    ivec.addElement(-1);  // for base handle.

    int nodeHandle = baseHandle;
    int type = getNodeType(baseHandle);
    int namespaceHandle = DTM.NULL;

    if (DTM.ELEMENT_NODE == type)
    {

      // We have to return in document order, so we actually want to find the 
      // first namespace decl of the last element that has a namespace decl.
      // Assume that attributes and namespaces immediately follow the element.
      int identity = nodeHandle & m_mask;

      while (DTM.NULL != identity)
      {
        identity = getNextNodeIdentity(identity);
        type = (DTM.NULL == identity) ? -1 : getNodeType(identity);

        if (type == DTM.NAMESPACE_NODE)
        {
          namespaceHandle = identity | m_dtmIdent;

          ivec.insertInOrder(namespaceHandle);
        }
        else if (DTM.ATTRIBUTE_NODE != type)
        {
          if (identity > 0)
          {
            nodeHandle = getParent(nodeHandle);

            if (nodeHandle == DTM.NULL)
              break;

            identity = nodeHandle & m_mask;

            if (identity == 0)
              break;
          }
          else
            break;
        }
      }
    }

    ivec.setElementAt(baseHandle, 0);

    return ivec;
  }

  /**
   * Given a node handle, get the index of the node's first child.
   * If not yet resolved, waits for more nodes to be added to the document and
   * tries again
   *
   * @param nodeHandle handle to node, which should probably be an element
   *                   node, but need not be.
   *
   * @param inScope    true if all namespaces in scope should be returned,
   *                   false if only the namespace declarations should be
   *                   returned.
   * @return handle of first namespace, or DTM.NULL to indicate none exists.
   */
  public int getFirstNamespaceNode(int nodeHandle, boolean inScope)
  {

    int type = getNodeType(nodeHandle);

    if (DTM.ELEMENT_NODE == type)
    {
      if (inScope)
      {
        NodeVector namespaces = getNamespaceList(nodeHandle);
        int n = namespaces.size();

        if (n > 1)
          return namespaces.elementAt(1);
      }
      else
      {

        // Assume that attributes and namespaces immediately follow the element.
        int identity = nodeHandle & m_mask;

        while (DTM.NULL != (identity = getNextNodeIdentity(identity)))
        {

          // Assume this can not be null.
          type = getNodeType(identity);

          if (type == DTM.NAMESPACE_NODE)
          {
            return identity | m_dtmIdent;
          }
          else if (DTM.ATTRIBUTE_NODE != type)
          {
            break;
          }
        }
      }
    }

    return DTM.NULL;
  }

  /**
   * Given a namespace handle, advance to the next namespace.
   *
   * @param baseHandle handle to original node from where the first namespace
   * was relative to (needed to return nodes in document order).
   * @param namespaceHandle handle to node which must be of type
   * NAMESPACE_NODE.
   * @param nodeHandle A namespace handle for which we will find the next node.
   * @param inScope true if all namespaces that are in scope should be processed,
   * otherwise just process the nodes in the given element handle.
   * @return handle of next namespace, or DTM.NULL to indicate none exists.
   */
  public int getNextNamespaceNode(int baseHandle, int nodeHandle,
                                  boolean inScope)
  {

    int type = getNodeType(nodeHandle);

    if (DTM.NAMESPACE_NODE == type)
    {
      if (inScope)
      {
        NodeVector namespaces = getNamespaceList(baseHandle);
        int n = namespaces.size();

        for (int i = 1; i < n; i++)  // start from 1 on purpose 
        {
          if (nodeHandle == namespaces.elementAt(i))
          {
            if (i + 1 < n)
              return namespaces.elementAt(i + 1);
          }
        }
      }
      else
      {

        // Assume that attributes and namespace nodes immediately follow the element.
        int identity = nodeHandle & m_mask;

        while (DTM.NULL != (identity = getNextNodeIdentity(identity)))
        {
          type = getNodeType(identity);

          if (type == DTM.NAMESPACE_NODE)
          {
            return identity | m_dtmIdent;
          }
          else if (type != DTM.ATTRIBUTE_NODE)
          {
            break;
          }
        }
      }
    }

    return DTM.NULL;
  }

  /**
   * Given a node handle, find its parent node.
   *
   * @param nodeHandle the id of the node.
   * @return int Node-number of parent,
   * or DTM.NULL to indicate none exists.
   */
  public int getParent(int nodeHandle)
  {

    int identity = nodeHandle & m_mask;

    if (identity > 0)
      return _parent(identity) | m_dtmIdent;
    else
      return DTM.NULL;
  }

  /**
   *  Given a node handle, find the owning document node.
   *
   *  @param nodeHandle the id of the node.
   *  @return int Node handle of document, which should always be valid.
   */
  public int getDocument()
  {
    return m_dtmIdent;
  }

  /**
   * Given a node handle, find the owning document node.  This has the exact
   * same semantics as the DOM Document method of the same name, in that if
   * the nodeHandle is a document node, it will return NULL.
   *
   * <p>%REVIEW% Since this is DOM-specific, it may belong at the DOM
   * binding layer. Included here as a convenience function and to
   * aid porting of DOM code to DTM.</p>
   *
   * @param nodeHandle the id of the node.
   * @return int Node handle of owning document, or -1 if the nodeHandle is
   *             a document.
   */
  public int getOwnerDocument(int nodeHandle)
  {

    int type = getNodeType(nodeHandle);

    if (DTM.DOCUMENT_NODE == type)
    {
      return DTM.NULL;
    }

    return getDocument();
  }

  /**
   * Get the string-value of a node as a String object
   * (see http://www.w3.org/TR/xpath#data-model
   * for the definition of a node's string-value).
   *
   * @param nodeHandle The node ID.
   *
   * @return A string object that represents the string-value of the given node.
   */
  public abstract XMLString getStringValue(int nodeHandle);

  /**
   * Get number of character array chunks in
   * the string-value of a node.
   * (see http://www.w3.org/TR/xpath#data-model
   * for the definition of a node's string-value).
   * Note that a single text node may have multiple text chunks.
   *
   * @param nodeHandle The node ID.
   *
   * @return number of character array chunks in
   *         the string-value of a node.
   */
  public int getStringValueChunkCount(int nodeHandle)
  {

    // %TBD%
    error("getStringValueChunkCount not yet supported!");

    return 0;
  }

  /**
   * Get a character array chunk in the string-value of a node.
   * (see http://www.w3.org/TR/xpath#data-model
   * for the definition of a node's string-value).
   * Note that a single text node may have multiple text chunks.
   *
   * @param nodeHandle The node ID.
   * @param chunkIndex Which chunk to get.
   * @param startAndLen An array of 2 where the start position and length of
   *                    the chunk will be returned.
   *
   * @return The character array reference where the chunk occurs.
   */
  public char[] getStringValueChunk(int nodeHandle, int chunkIndex,
                                    int[] startAndLen)
  {

    // %TBD%
    error("getStringValueChunk not yet supported!");

    return null;
  }

  /**
   * Given a node handle, return an ID that represents the node's expanded name.
   *
   * @param nodeHandle The handle to the node in question.
   *
   * @return the expanded-name id of the node.
   */
  public int getExpandedTypeID(int nodeHandle)
  {

    int identity = nodeHandle & m_mask;
    int expandedNameID = _exptype(identity);

    return expandedNameID;
  }

  /**
   * Given an expanded name, return an ID.  If the expanded-name does not
   * exist in the internal tables, the entry will be created, and the ID will
   * be returned.  Any additional nodes that are created that have this
   * expanded name will use this ID.
   *
   * @param nodeHandle The handle to the node in question.
   * @param type The simple type, i.e. one of ELEMENT, ATTRIBUTE, etc.
   *
   * @param namespace The namespace URI, which may be null, may be an empty
   *                  string (which will be the same as null), or may be a
   *                  namespace URI.
   * @param localName The local name string, which must be a valid
   *                  <a href="http://www.w3.org/TR/REC-xml-names/">NCName</a>.
   *
   * @return the expanded-name id of the node.
   */
  public int getExpandedTypeID(String namespace, String localName, int type)
  {

    ExpandedNameTable ent = m_mgr.getExpandedNameTable(this);

    return ent.getExpandedTypeID(namespace, localName, type);
  }

  /**
   * Given an expanded-name ID, return the local name part.
   *
   * @param ExpandedNameID an ID that represents an expanded-name.
   * @return String Local name of this node.
   */
  public String getLocalNameFromExpandedNameID(int ExpandedNameID)
  {

    ExpandedNameTable ent = m_mgr.getExpandedNameTable(this);

    return ent.getLocalName(ExpandedNameID);
  }

  /**
   * Given an expanded-name ID, return the namespace URI part.
   *
   * @param ExpandedNameID an ID that represents an expanded-name.
   * @return String URI value of this node's namespace, or null if no
   * namespace was resolved.
   */
  public String getNamespaceFromExpandedNameID(int ExpandedNameID)
  {

    ExpandedNameTable ent = m_mgr.getExpandedNameTable(this);

    return ent.getNamespace(ExpandedNameID);
  }

  /**
   * Returns the namespace type of a specific node
   * @param nodeHandle the id of the node.
   * @return the ID of the namespace.
   */
  public int getNamespaceType(final int nodeHandle)
  {

    int identity = nodeHandle & m_mask;
    int expandedNameID = _exptype(identity);

    return ExpandedNameTable.getNamespaceID(expandedNameID);
  }

  /**
   * Given a node handle, return its DOM-style node name. This will
   * include names such as #text or #document.
   *
   * @param nodeHandle the id of the node.
   * @return String Name of this node, which may be an empty string.
   * %REVIEW% Document when empty string is possible...
   * %REVIEW-COMMENT% It should never be empty, should it?
   */
  public abstract String getNodeName(int nodeHandle);

  /**
   * Given a node handle, return the XPath node name.  This should be
   * the name as described by the XPath data model, NOT the DOM-style
   * name.
   *
   * @param nodeHandle the id of the node.
   * @return String Name of this node, which may be an empty string.
   */
  public String getNodeNameX(int nodeHandle)
  {

    /** @todo: implement this org.apache.xml.dtm.DTMDefaultBase abstract method */
    error("Not yet supported!");

    return null;
  }

  /**
   * Given a node handle, return its XPath-style localname.
   * (As defined in Namespaces, this is the portion of the name after any
   * colon character).
   *
   * @param nodeHandle the id of the node.
   * @return String Local name of this node.
   */
  public abstract String getLocalName(int nodeHandle);

  /**
   * Given a namespace handle, return the prefix that the namespace decl is
   * mapping.
   * Given a node handle, return the prefix used to map to the namespace.
   *
   * <p> %REVIEW% Are you sure you want "" for no prefix?  </p>
   * <p> %REVIEW-COMMENT% I think so... not totally sure. -sb  </p>
   *
   * @param nodeHandle the id of the node.
   * @return String prefix of this node's name, or "" if no explicit
   * namespace prefix was given.
   */
  public abstract String getPrefix(int nodeHandle);

  /**
   * Given a node handle, return its DOM-style namespace URI
   * (As defined in Namespaces, this is the declared URI which this node's
   * prefix -- or default in lieu thereof -- was mapped to.)
   *
   * <p>%REVIEW% Null or ""? -sb</p>
   *
   * @param nodeHandle the id of the node.
   * @return String URI value of this node's namespace, or null if no
   * namespace was resolved.
   */
  public abstract String getNamespaceURI(int nodeHandle);

  /**
   * Given a node handle, return its node value. This is mostly
   * as defined by the DOM, but may ignore some conveniences.
   * <p>
   *
   * @param nodeHandle The node id.
   * @return String Value of this node, or null if not
   * meaningful for this node type.
   */
  public abstract String getNodeValue(int nodeHandle);

  /**
   * Given a node handle, return its DOM-style node type.
   * <p>
   * %REVIEW% Generally, returning short is false economy. Return int?
   *
   * @param nodeHandle The node id.
   * @return int Node type, as per the DOM's Node._NODE constants.
   */
  public short getNodeType(int nodeHandle)
  {

    int identity = nodeHandle & m_mask;
    short type = (short) _type(identity);

    return type;
  }

  /**
   * <meta name="usage" content="internal"/>
   * Get the depth level of this node in the tree (equals 1 for
   * a parentless node).
   *
   * @param nodeHandle The node id.
   * @return the number of ancestors, plus one
   */
  public short getLevel(int nodeHandle)
  {

    int identity = nodeHandle & m_mask;

    // Apparently, the axis walker stuff requires levels to count from 1.
    return (short) (_level(identity) + 1);
  }

  // ============== Document query functions ============== 

  /**
   * Tests whether DTM DOM implementation implements a specific feature and
   * that feature is supported by this node.
   *
   * @param feature The name of the feature to test.
   * @param versionThis is the version number of the feature to test.
   *   If the version is not
   *   specified, supporting any version of the feature will cause the
   *   method to return <code>true</code>.
   * @param version The version string of the feature requested, may be null.
   * @return Returns <code>true</code> if the specified feature is
   *   supported on this node, <code>false</code> otherwise.
   */
  public boolean isSupported(String feature, String version)
  {

    // %TBD%
    return false;
  }

  /**
   * Return the base URI of the document entity. If it is not known
   * (because the document was parsed from a socket connection or from
   * standard input, for example), the value of this property is unknown.
   *
   * @return the document base URI String object or null if unknown.
   */
  public String getDocumentBaseURI()
  {
    return m_documentBaseURI;
  }

  /**
   * Set the base URI of the document entity.
   *
   * @param baseURI the document base URI String object or null if unknown.
   */
  public void setDocumentBaseURI(String baseURI)
  {
    m_documentBaseURI = baseURI;
  }

  /**
   * Return the system identifier of the document entity. If
   * it is not known, the value of this property is unknown.
   *
   * @param nodeHandle The node id, which can be any valid node handle.
   * @return the system identifier String object or null if unknown.
   */
  public String getDocumentSystemIdentifier(int nodeHandle)
  {

    // %REVIEW%  OK? -sb
    return m_documentBaseURI;
  }

  /**
   * Return the name of the character encoding scheme
   *        in which the document entity is expressed.
   *
   * @param nodeHandle The node id, which can be any valid node handle.
   * @return the document encoding String object.
   */
  public String getDocumentEncoding(int nodeHandle)
  {

    // %REVIEW%  OK??  -sb
    return "UTF-8";
  }

  /**
   * Return an indication of the standalone status of the document,
   *        either "yes" or "no". This property is derived from the optional
   *        standalone document declaration in the XML declaration at the
   *        beginning of the document entity, and has no value if there is no
   *        standalone document declaration.
   *
   * @param nodeHandle The node id, which can be any valid node handle.
   * @return the document standalone String object, either "yes", "no", or null.
   */
  public String getDocumentStandalone(int nodeHandle)
  {
    return null;
  }

  /**
   * Return a string representing the XML version of the document. This
   * property is derived from the XML declaration optionally present at the
   * beginning of the document entity, and has no value if there is no XML
   * declaration.
   *
   * @param documentHandle The document handle
   *
   * @return the document version String object.
   */
  public String getDocumentVersion(int documentHandle)
  {
    return null;
  }

  /**
   * Return an indication of
   * whether the processor has read the complete DTD. Its value is a
   * boolean. If it is false, then certain properties (indicated in their
   * descriptions below) may be unknown. If it is true, those properties
   * are never unknown.
   *
   * @return <code>true</code> if all declarations were processed;
   *         <code>false</code> otherwise.
   */
  public boolean getDocumentAllDeclarationsProcessed()
  {

    // %REVIEW% OK?
    return true;
  }

  /**
   *   A document type declaration information item has the following properties:
   *
   *     1. [system identifier] The system identifier of the external subset, if
   *        it exists. Otherwise this property has no value.
   *
   * @return the system identifier String object, or null if there is none.
   */
  public abstract String getDocumentTypeDeclarationSystemIdentifier();

  /**
   * Return the public identifier of the external subset,
   * normalized as described in 4.2.2 External Entities [XML]. If there is
   * no external subset or if it has no public identifier, this property
   * has no value.
   *
   * @param the document type declaration handle
   *
   * @return the public identifier String object, or null if there is none.
   */
  public abstract String getDocumentTypeDeclarationPublicIdentifier();

  /**
   * Returns the <code>Element</code> whose <code>ID</code> is given by
   * <code>elementId</code>. If no such element exists, returns
   * <code>DTM.NULL</code>. Behavior is not defined if more than one element
   * has this <code>ID</code>. Attributes (including those
   * with the name "ID") are not of type ID unless so defined by DTD/Schema
   * information available to the DTM implementation.
   * Implementations that do not know whether attributes are of type ID or
   * not are expected to return <code>DTM.NULL</code>.
   *
   * <p>%REVIEW% Presumably IDs are still scoped to a single document,
   * and this operation searches only within a single document, right?
   * Wouldn't want collisions between DTMs in the same process.</p>
   *
   * @param elementId The unique <code>id</code> value for an element.
   * @return The handle of the matching element.
   */
  public abstract int getElementById(String elementId);

  /**
   * The getUnparsedEntityURI function returns the URI of the unparsed
   * entity with the specified name in the same document as the context
   * node (see [3.3 Unparsed Entities]). It returns the empty string if
   * there is no such entity.
   * <p>
   * XML processors may choose to use the System Identifier (if one
   * is provided) to resolve the entity, rather than the URI in the
   * Public Identifier. The details are dependent on the processor, and
   * we would have to support some form of plug-in resolver to handle
   * this properly. Currently, we simply return the System Identifier if
   * present, and hope that it a usable URI or that our caller can
   * map it to one.
   * TODO: Resolve Public Identifiers... or consider changing function name.
   * <p>
   * If we find a relative URI
   * reference, XML expects it to be resolved in terms of the base URI
   * of the document. The DOM doesn't do that for us, and it isn't
   * entirely clear whether that should be done here; currently that's
   * pushed up to a higher level of our application. (Note that DOM Level
   * 1 didn't store the document's base URI.)
   * TODO: Consider resolving Relative URIs.
   * <p>
   * (The DOM's statement that "An XML processor may choose to
   * completely expand entities before the structure model is passed
   * to the DOM" refers only to parsed entities, not unparsed, and hence
   * doesn't affect this function.)
   *
   * @param name A string containing the Entity Name of the unparsed
   * entity.
   *
   * @return String containing the URI of the Unparsed Entity, or an
   * empty string if no such entity exists.
   */
  public abstract String getUnparsedEntityURI(String name);

  // ============== Boolean methods ================

  /**
   * Return true if the xsl:strip-space or xsl:preserve-space was processed
   * during construction of the DTM document.
   *
   * @return true if this DTM supports prestripping.
   */
  public boolean supportsPreStripping()
  {
    return true;
  }

  /**
   * Figure out whether nodeHandle2 should be considered as being later
   * in the document than nodeHandle1, in Document Order as defined
   * by the XPath model. This may not agree with the ordering defined
   * by other XML applications.
   * <p>
   * There are some cases where ordering isn't defined, and neither are
   * the results of this function -- though we'll generally return true.
   *
   * TODO: Make sure this does the right thing with attribute nodes!!!
   *
   * @param nodeHandle1 Node handle to perform position comparison on.
   * @param nodeHandle2 Second Node handle to perform position comparison on .
   *
   * @return false if node2 comes before node1, otherwise return true.
   * You can think of this as
   * <code>(node1.documentOrderPosition &lt;= node2.documentOrderPosition)</code>.
   */
  public boolean isNodeAfter(int nodeHandle1, int nodeHandle2)
  {

    int index1 = nodeHandle1 & m_mask;
    int index2 = nodeHandle2 & m_mask;

    return index1 <= index2;
  }

  /**
   *     2. [element content whitespace] A boolean indicating whether the
   *        character is white space appearing within element content (see [XML],
   *        2.10 "White Space Handling"). Note that validating XML processors are
   *        required by XML 1.0 to provide this information. If there is no
   *        declaration for the containing element, this property has no value for
   *        white space characters. If no declaration has been read, but the [all
   *        declarations processed] property of the document information item is
   *        false (so there may be an unread declaration), then the value of this
   *        property is unknown for white space characters. It is always false for
   *        characters that are not white space.
   *
   * @param nodeHandle the node ID.
   * @return <code>true</code> if the character data is whitespace;
   *         <code>false</code> otherwise.
   */
  public boolean isCharacterElementContentWhitespace(int nodeHandle)
  {

    // %TBD%
    return false;
  }

  /**
   *    10. [all declarations processed] This property is not strictly speaking
   *        part of the infoset of the document. Rather it is an indication of
   *        whether the processor has read the complete DTD. Its value is a
   *        boolean. If it is false, then certain properties (indicated in their
   *        descriptions below) may be unknown. If it is true, those properties
   *        are never unknown.
   *
   * @param the document handle
   *
   * @param documentHandle A node handle that must identify a document.
   * @return <code>true</code> if all declarations were processed;
   *         <code>false</code> otherwise.
   */
  public boolean isDocumentAllDeclarationsProcessed(int documentHandle)
  {
    return true;
  }

  /**
   *     5. [specified] A flag indicating whether this attribute was actually
   *        specified in the start-tag of its element, or was defaulted from the
   *        DTD.
   *
   * @param attributeHandle The attribute handle in question.
   *
   * @return <code>true</code> if the attribute was specified;
   *         <code>false</code> if it was defaulted.
   */
  public abstract boolean isAttributeSpecified(int attributeHandle);

  // ========== Direct SAX Dispatch, for optimization purposes ========

  /**
   * Directly call the
   * characters method on the passed ContentHandler for the
   * string-value of the given node (see http://www.w3.org/TR/xpath#data-model
   * for the definition of a node's string-value). Multiple calls to the
   * ContentHandler's characters methods may well occur for a single call to
   * this method.
   *
   * @param nodeHandle The node ID.
   * @param ch A non-null reference to a ContentHandler.
   *
   * @throws org.xml.sax.SAXException
   */
  public abstract void dispatchCharactersEvents(
    int nodeHandle, org.xml.sax.ContentHandler ch)
      throws org.xml.sax.SAXException;

  /**
   * Directly create SAX parser events from a subtree.
   *
   * @param nodeHandle The node ID.
   * @param ch A non-null reference to a ContentHandler.
   *
   * @throws org.xml.sax.SAXException
   */
  public abstract void dispatchToEvents(
    int nodeHandle, org.xml.sax.ContentHandler ch)
      throws org.xml.sax.SAXException;

  /**
   * Return an DOM node for the given node.
   *
   * @param nodeHandle The node ID.
   *
   * @return A node representation of the DTM node.
   */
  public org.w3c.dom.Node getNode(int nodeHandle)
  {
    return new DTMNodeProxy(this, nodeHandle);
  }

  // ==== Construction methods (may not be supported by some implementations!) =====

  /**
   * Append a child to the end of the document. Please note that the node
   * is always cloned if it is owned by another document.
   *
   * <p>%REVIEW% "End of the document" needs to be defined more clearly.
   * Does it become the last child of the Document? Of the root element?</p>
   *
   * @param newChild Must be a valid new node handle.
   * @param clone true if the child should be cloned into the document.
   * @param cloneDepth if the clone argument is true, specifies that the
   *                   clone should include all it's children.
   */
  public void appendChild(int newChild, boolean clone, boolean cloneDepth)
  {
    error("appendChild not yet supported!");
  }

  /**
   * Append a text node child that will be constructed from a string,
   * to the end of the document.
   *
   * <p>%REVIEW% "End of the document" needs to be defined more clearly.
   * Does it become the last child of the Document? Of the root element?</p>
   *
   * @param str Non-null reverence to a string.
   */
  public void appendTextChild(String str)
  {
    error("appendTextChild not yet supported!");
  }

  /**
   * Simple error for asserts and the like.
   *
   * @param msg Error message to report.
   */
  protected void error(String msg)
  {
    throw new DTMException(msg);
  }

  /**
   * Find out whether or not to strip whispace nodes.
   *
   *
   * @return whether or not to strip whispace nodes.
   */
  protected boolean getShouldStripWhitespace()
  {
    return m_shouldStripWS;
  }

  /**
   * Set whether to strip whitespaces and push in current value of
   * m_shouldStripWS in m_shouldStripWhitespaceStack.
   *
   * @param shouldStrip Flag indicating whether to strip whitespace nodes
   */
  protected void pushShouldStripWhitespace(boolean shouldStrip)
  {

    m_shouldStripWS = shouldStrip;

    if (null != m_shouldStripWhitespaceStack)
      m_shouldStripWhitespaceStack.push(shouldStrip);
  }

  /**
   * Set whether to strip whitespaces at this point by popping out
   * m_shouldStripWhitespaceStack.
   *
   */
  protected void popShouldStripWhitespace()
  {
    if (null != m_shouldStripWhitespaceStack)
      m_shouldStripWS = m_shouldStripWhitespaceStack.popAndTop();
  }

  /**
   * Set whether to strip whitespaces and set the top of the stack to
   * the current value of m_shouldStripWS.
   *
   *
   * @param shouldStrip Flag indicating whether to strip whitespace nodes
   */
  protected void setShouldStripWhitespace(boolean shouldStrip)
  {

    m_shouldStripWS = shouldStrip;

    if (null != m_shouldStripWhitespaceStack)
      m_shouldStripWhitespaceStack.setTop(shouldStrip);
  }

  /**
   * This returns a stateless "traverser", that can navigate over an
   * XPath axis, though perhaps not in document order.
   *
   * @param axis One of Axes.ANCESTORORSELF, etc.
   *
   * @return A DTMAxisIterator, or null if the givin axis isn't supported.
   */
  public DTMAxisTraverser getAxisTraverser(final int axis)
  {

    DTMAxisTraverser traverser;

    if (null == m_traversers)
    {
      m_traversers = new DTMAxisTraverser[Axis.names.length];
      traverser = null;
    }
    else
    {
      traverser = m_traversers[axis];

      if (traverser != null)
        return traverser;
    }

    switch (axis)
    {
    case Axis.ANCESTOR :
      traverser = new AncestorTraverser();
      break;
    case Axis.ANCESTORORSELF :
      traverser = new AncestorOrSelfTraverser();
      break;
    case Axis.ATTRIBUTE :
      traverser = new AttributeTraverser();
      break;
    case Axis.CHILD :
      traverser = new ChildTraverser();
      break;
    case Axis.DESCENDANT :
      traverser = new DescendantTraverser();
      break;
    case Axis.DESCENDANTORSELF :
      traverser = new DescendantTraverser();
      break;
    case Axis.FOLLOWING :
      traverser = new FollowingTraverser();
      break;
    case Axis.FOLLOWINGSIBLING :
      traverser = new FollowingSiblingTraverser();
      break;
    case Axis.NAMESPACE :
      traverser = new NamespaceTraverser();
      break;
    case Axis.NAMESPACEDECLS :
      traverser = new NamespaceDeclsTraverser();
      break;
    case Axis.PARENT :
      traverser = new ParentTraverser();
      break;
    case Axis.PRECEDING :
      traverser = new PrecedingTraverser();
      break;
    case Axis.PRECEDINGSIBLING :
      traverser = new PrecedingSiblingTraverser();
      break;
    case Axis.SELF :
      traverser = new SelfTraverser();
      break;
    case Axis.SUBTREE :
      traverser = new SubtreeTraverser();
      break;
    default :
      throw new DTMException("Unknown axis traversal type");
    }

    if (null == traverser)
      throw new DTMException("Axis traverser not supported: "
                             + Axis.names[axis]);

    m_traversers[axis] = traverser;

    return traverser;
  }

  /**
   * Get an iterator that can navigate over an XPath Axis, predicated by
   * the extended type ID.
   *
   * @param axis One of Axes.ANCESTORORSELF, etc.
   * @param type An extended type ID.
   *
   * @return A DTMAxisIterator, or null if the givin axis isn't supported.
   */
  public DTMAxisIterator getTypedAxisIterator(int axis, int type)
  {

    DTMAxisIterator iterator = null;

    /* This causes an error when using patterns for elements that
       do not exist in the DOM (translet types which do not correspond
       to a DOM type are mapped to the DOM.ELEMENT type).
    */

    //        if (type == NO_TYPE) {
    //            return(EMPTYITERATOR);
    //        }
    //        else if (type == ELEMENT) {
    //            iterator = new FilterIterator(getAxisIterator(axis),
    //                                          getElementFilter());
    //        }
    //        else 
    {
      switch (axis)
      {
      case Axis.SELF :
        iterator = new TypedSingletonIterator(type);
        break;
      case Axis.CHILD :
        iterator = new TypedChildrenIterator(type);
        break;
      case Axis.PARENT :
        return (new ParentIterator().setNodeType(type));
      case Axis.ANCESTOR :
        return (new TypedAncestorIterator(type));
      case Axis.ANCESTORORSELF :
        return ((new TypedAncestorIterator(type)).includeSelf());
      case Axis.ATTRIBUTE :
        return (new TypedAttributeIterator(type));
      case Axis.DESCENDANT :
        iterator = new TypedDescendantIterator(type);
        break;
      case Axis.DESCENDANTORSELF :
        iterator = (new TypedDescendantIterator(type)).includeSelf();
        break;
      case Axis.FOLLOWING :
        iterator = new TypedFollowingIterator(type);
        break;
      case Axis.PRECEDING :
        iterator = new TypedPrecedingIterator(type);
        break;
      case Axis.FOLLOWINGSIBLING :
        iterator = new TypedFollowingSiblingIterator(type);
        break;
      case Axis.PRECEDINGSIBLING :
        iterator = new TypedPrecedingSiblingIterator(type);
        break;
      default :
        throw new DTMException("Error: typed iterator for axis "
                               + Axis.names[axis] + "not implemented");
      }
    }

    return (iterator);
  }

  /**
   * This is a shortcut to the iterators that implement the
   * XPath axes.
   * Returns a bare-bones iterator that must be initialized
   * with a start node (using iterator.setStartNode()).
   *
   * @param axis One of Axes.ANCESTORORSELF, etc.
   *
   * @return A DTMAxisIterator, or null if the givin axis isn't supported.
   */
  public DTMAxisIterator getAxisIterator(final int axis)
  {

    DTMAxisIterator iterator = null;

    switch (axis)
    {
    case Axis.SELF :
      iterator = new SingletonIterator();
      break;
    case Axis.CHILD :
      iterator = new ChildrenIterator();
      break;
    case Axis.PARENT :
      return (new ParentIterator());
    case Axis.ANCESTOR :
      return (new AncestorIterator());
    case Axis.ANCESTORORSELF :
      return ((new AncestorIterator()).includeSelf());
    case Axis.ATTRIBUTE :
      return (new AttributeIterator());
    case Axis.DESCENDANT :
      iterator = new DescendantIterator();
      break;
    case Axis.DESCENDANTORSELF :
      iterator = (new DescendantIterator()).includeSelf();
      break;
    case Axis.FOLLOWING :
      iterator = new FollowingIterator();
      break;
    case Axis.PRECEDING :
      iterator = new PrecedingIterator();
      break;
    case Axis.FOLLOWINGSIBLING :
      iterator = new FollowingSiblingIterator();
      break;
    case Axis.PRECEDINGSIBLING :
      iterator = new PrecedingSiblingIterator();
      break;
    case Axis.NAMESPACE :

    // return(new N()); %TBD%
    default :
      throw new DTMException("Error: iterator for axis '" + Axis.names[axis]
                             + "' not implemented");
    }

    return (iterator);
  }

  /**
   * Iterator that returns all children of a given node
   */
  private final class ChildrenIterator extends DTMAxisIteratorBase
  {

    /** child to return next. */
    private int _currentChild;

    /**
     * Set start to END should 'close' the iterator,
     * i.e. subsequent call to next() should return END.
     *
     * @param node Sets the root of the iteration.
     *
     * @return A DTMAxisIterator set to the start of the iteration.
     */
    public DTMAxisIterator setStartNode(final int node)
    {

      if (_isRestartable)
      {
        _startNode = node;
        _currentChild = NOTPROCESSED;

        return resetPosition();
      }

      return this;
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {

      _currentChild = (NOTPROCESSED == _currentChild)
                      ? getFirstChild(_startNode)
                      : getNextSibling(_currentChild);

      return returnNode(_currentChild);
    }

    /**
     * Remembers the current node for the next call to gotoMark().
     */
    public void setMark()
    {
      _markedNode = _currentChild;
    }

    /**
     * Restores the current node remembered by setMark().
     */
    public void gotoMark()
    {
      _currentChild = _markedNode;
    }
  }  // end of ChildrenIterator

  /**
   * Iterator that returns the parent of a given node
   */
  private final class ParentIterator extends DTMAxisIteratorBase
  {

    /** candidate parent node. */
    private int _node;

    /** The extended type ID that was requested. */
    private int _nodeType = -1;

    /**
     * Set start to END should 'close' the iterator,
     * i.e. subsequent call to next() should return END.
     *
     * @param node Sets the root of the iteration.
     *
     * @return A DTMAxisIterator set to the start of the iteration.
     */
    public DTMAxisIterator setStartNode(int node)
    {

      if (_isRestartable)
      {
        _startNode = node;
        _node = getParent(node);

        return resetPosition();
      }

      return this;
    }

    /**
     * Set the node type of the parent that we're looking for.
     *
     *
     * @param type extended type ID.
     *
     * @return The node type.
     */
    public DTMAxisIterator setNodeType(final int type)
    {

      _nodeType = type;

      return this;
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {

      int result = _node;

      if ((_nodeType != -1) && (getExpandedTypeID(_node) != _nodeType))
        result = END;
      else
        result = _node;

      _node = END;

      return returnNode(result);
    }

    /**
     * Remembers the current node for the next call to gotoMark().
     */
    public void setMark()
    {
      _markedNode = _node;
    }

    /**
     * Restores the current node remembered by setMark().
     */
    public void gotoMark()
    {
      _node = _markedNode;
    }
  }  // end of ParentIterator

  /**
   * Iterator that returns children of a given type for a given node.
   * The functionality chould be achieved by putting a filter on top
   * of a basic child iterator, but a specialised iterator is used
   * for efficiency (both speed and size of translet).
   */
  private final class TypedChildrenIterator extends DTMAxisIteratorBase
  {

    /** The extended type ID that was requested. */
    private final int _nodeType;

    /** node to consider next. */
    private int _currentChild;

    /**
     * Constructor TypedChildrenIterator
     *
     *
     * @param nodeType The extended type ID being requested.
     */
    public TypedChildrenIterator(int nodeType)
    {
      _nodeType = nodeType;
    }

    /**
     * Set start to END should 'close' the iterator,
     * i.e. subsequent call to next() should return END.
     *
     * @param node Sets the root of the iteration.
     *
     * @return A DTMAxisIterator set to the start of the iteration.
     */
    public DTMAxisIterator setStartNode(int node)
    {

      if (_isRestartable)
      {
        _startNode = node;
        _currentChild = NOTPROCESSED;

        return resetPosition();
      }

      return this;
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {

      for (int node = (NOTPROCESSED == _currentChild)
                      ? getFirstChild(_startNode)
                      : getNextSibling(_currentChild); node
                        != END; node = getNextSibling(node))
      {
        if (getExpandedTypeID(node) == _nodeType)
        {
          _currentChild = node;

          return returnNode(node);
        }
      }

      return END;
    }

    /**
     * Remembers the current node for the next call to gotoMark().
     */
    public void setMark()
    {
      _markedNode = _currentChild;
    }

    /**
     * Restores the current node remembered by setMark().
     */
    public void gotoMark()
    {
      _currentChild = _markedNode;
    }
  }  // end of TypedChildrenIterator

  /**
   * Iterator that returns children within a given namespace for a
   * given node. The functionality chould be achieved by putting a
   * filter on top of a basic child iterator, but a specialised
   * iterator is used for efficiency (both speed and size of translet).
   */
  private final class NamespaceChildrenIterator extends DTMAxisIteratorBase
  {

    /** The extended type ID being requested. */
    private final int _nsType;

    /** The current child. */
    private int _currentChild;

    /**
     * Constructor NamespaceChildrenIterator
     *
     *
     * @param type The extended type ID being requested.
     */
    public NamespaceChildrenIterator(final int type)
    {
      _nsType = type;
    }

    /**
     * Set start to END should 'close' the iterator,
     * i.e. subsequent call to next() should return END.
     *
     * @param node Sets the root of the iteration.
     *
     * @return A DTMAxisIterator set to the start of the iteration.
     */
    public DTMAxisIterator setStartNode(int node)
    {

      if (_isRestartable)
      {
        _startNode = node;
        _currentChild = NOTPROCESSED;

        return resetPosition();
      }

      return this;
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {

      for (int node = (NOTPROCESSED == _currentChild)
                      ? getFirstChild(_startNode)
                      : getNextSibling(_currentChild); node
                        != END; node = getNextSibling(node))
      {
        if (getNamespaceType(node) == _nsType)
        {
          _currentChild = node;

          return returnNode(node);
        }
      }

      return END;
    }

    /**
     * Remembers the current node for the next call to gotoMark().
     */
    public void setMark()
    {
      _markedNode = _currentChild;
    }

    /**
     * Restores the current node remembered by setMark().
     */
    public void gotoMark()
    {
      _currentChild = _markedNode;
    }
  }  // end of TypedChildrenIterator

  /**
   * Iterator that returns attributes within a given namespace for a node.
   */
  private final class NamespaceAttributeIterator extends DTMAxisIteratorBase
  {

    /** The extended type ID being requested. */
    private final int _nsType;

    /** The current attribute. */
    private int _attribute;

    /**
     * Constructor NamespaceAttributeIterator
     *
     *
     * @param nsType The extended type ID being requested.
     */
    public NamespaceAttributeIterator(int nsType)
    {

      super();

      _nsType = nsType;
    }

    /**
     * Set start to END should 'close' the iterator,
     * i.e. subsequent call to next() should return END.
     *
     * @param node Sets the root of the iteration.
     *
     * @return A DTMAxisIterator set to the start of the iteration.
     */
    public DTMAxisIterator setStartNode(int node)
    {

      if (_isRestartable)
      {
        _startNode = node;
        _attribute = getFirstNamespaceNode(node, false);

        return resetPosition();
      }

      return this;
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {

      int node = _attribute;

      if (DTM.NULL != node)
        _attribute = getNextNamespaceNode(_startNode, node, false);

      return returnNode(node);
    }

    /**
     * Remembers the current node for the next call to gotoMark().
     */
    public void setMark()
    {
      _markedNode = _attribute;
    }

    /**
     * Restores the current node remembered by setMark().
     */
    public void gotoMark()
    {
      _attribute = _markedNode;
    }
  }  // end of TypedChildrenIterator

  /**
   * Iterator that returns all siblings of a given node.
   */
  private class FollowingSiblingIterator extends DTMAxisIteratorBase
  {

    /** The current node. */
    private int _node;

    /**
     * Set start to END should 'close' the iterator,
     * i.e. subsequent call to next() should return END.
     *
     * @param node Sets the root of the iteration.
     *
     * @return A DTMAxisIterator set to the start of the iteration.
     */
    public DTMAxisIterator setStartNode(int node)
    {

      if (_isRestartable)
      {
        _node = _startNode = node;

        return resetPosition();
      }

      return this;
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {
      return returnNode(_node = getNextSibling(_node));
    }

    /**
     * Remembers the current node for the next call to gotoMark().
     */
    public void setMark()
    {
      _markedNode = _node;
    }

    /**
     * Restores the current node remembered by setMark().
     */
    public void gotoMark()
    {
      _node = _markedNode;
    }
  }  // end of FollowingSiblingIterator

  /**
   * Iterator that returns all following siblings of a given node.
   */
  private final class TypedFollowingSiblingIterator
          extends FollowingSiblingIterator
  {

    /** The extended type ID that was requested. */
    private final int _nodeType;

    /**
     * Constructor TypedFollowingSiblingIterator
     *
     *
     * @param type The extended type ID being requested.
     */
    public TypedFollowingSiblingIterator(int type)
    {
      _nodeType = type;
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {

      int node;

      while ((node = super.next()) != NULL
             && getExpandedTypeID(node) != _nodeType){}

      return node;
    }
  }  // end of TypedFollowingSiblingIterator

  /**
   * Iterator that returns attribute nodes (of what nodes?)
   */
  private final class AttributeIterator extends DTMAxisIteratorBase
  {

    /** The current attribute. */
    private int _attribute;

    // assumes caller will pass element nodes

    /**
     * Set start to END should 'close' the iterator,
     * i.e. subsequent call to next() should return END.
     *
     * @param node Sets the root of the iteration.
     *
     * @return A DTMAxisIterator set to the start of the iteration.
     */
    public DTMAxisIterator setStartNode(int node)
    {

      if (_isRestartable)
      {
        _startNode = node;
        _attribute = getFirstAttribute(node);

        return resetPosition();
      }

      return this;
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {

      final int node = _attribute;

      _attribute = getNextAttribute(node);

      return returnNode(node);
    }

    /**
     * Remembers the current node for the next call to gotoMark().
     */
    public void setMark()
    {
      _markedNode = _attribute;
    }

    /**
     * Restores the current node remembered by setMark().
     */
    public void gotoMark()
    {
      _attribute = _markedNode;
    }
  }  // end of AttributeIterator

  /**
   * Iterator that returns attribute nodes of a given type
   */
  private final class TypedAttributeIterator extends DTMAxisIteratorBase
  {

    /** The extended type ID that was requested. */
    private final int _nodeType;

    /** The current attribute. */
    private int _attribute;

    /**
     * Constructor TypedAttributeIterator
     *
     *
     * @param nodeType The extended type ID that is requested.
     */
    public TypedAttributeIterator(int nodeType)
    {
      _nodeType = nodeType;
    }

    // assumes caller will pass element nodes

    /**
     * Set start to END should 'close' the iterator,
     * i.e. subsequent call to next() should return END.
     *
     * @param node Sets the root of the iteration.
     *
     * @return A DTMAxisIterator set to the start of the iteration.
     */
    public DTMAxisIterator setStartNode(int node)
    {

      if (_isRestartable)
      {
        _startNode = node;

        for (node = getFirstAttribute(node); node != END;
                node = getNextAttribute(node))
        {
          if (getExpandedTypeID(node) == _nodeType)
            break;
        }

        _attribute = node;

        return resetPosition();
      }

      return this;
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {

      final int node = _attribute;

      // singleton iterator, since there can only be one attribute of 
      // a given type.
      _attribute = NULL;

      return returnNode(node);
    }

    /**
     * Remembers the current node for the next call to gotoMark().
     */
    public void setMark()
    {
      _markedNode = _attribute;
    }

    /**
     * Restores the current node remembered by setMark().
     */
    public void gotoMark()
    {
      _attribute = _markedNode;
    }
  }  // end of TypedAttributeIterator

  /**
   * Iterator that returns preceding siblings of a given node
   */
  private class PrecedingSiblingIterator extends DTMAxisIteratorBase
  {

    /** The start node (...on the left of the graph, I think. -sb) */
    private int _start;

    /** The current node. */
    private int _node;

    /**
     * True if this iterator has a reversed axis.
     *
     * @return true.
     */
    public boolean isReverse()
    {
      return true;
    }

    /**
     * Set start to END should 'close' the iterator,
     * i.e. subsequent call to next() should return END.
     *
     * @param node Sets the root of the iteration.
     *
     * @return A DTMAxisIterator set to the start of the iteration.
     */
    public DTMAxisIterator setStartNode(int node)
    {

      if (_isRestartable)
      {
        _startNode = node;
        _node = getFirstChild(getParent(node));

        return resetPosition();
      }

      return this;
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {

      if (_node == _start)
      {
        return NULL;
      }
      else
      {
        final int node = _node;

        _node = getNextSibling(node);

        return returnNode(node);
      }
    }

    /**
     * Remembers the current node for the next call to gotoMark().
     */
    public void setMark()
    {
      _markedNode = _node;
    }

    /**
     * Restores the current node remembered by setMark().
     */
    public void gotoMark()
    {
      _node = _markedNode;
    }
  }  // end of PrecedingSiblingIterator

  /**
   * Iterator that returns preceding siblings of a given type for
   * a given node
   */
  private final class TypedPrecedingSiblingIterator
          extends PrecedingSiblingIterator
  {

    /** The extended type ID that was requested. */
    private final int _nodeType;

    /**
     * Constructor TypedPrecedingSiblingIterator
     *
     *
     * @param type The extended type ID being requested.
     */
    public TypedPrecedingSiblingIterator(int type)
    {
      _nodeType = type;
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {

      int node;

      while ((node = super.next()) != NULL
             && getExpandedTypeID(node) != _nodeType){}

      return node;
    }
  }  // end of PrecedingSiblingIterator

  /**
   * Iterator that returns preceding nodes of a given node.
   * This includes the node set {root+1, start-1}, but excludes
   * all ancestors.
   */
  private class PrecedingIterator extends DTMAxisIteratorBase
  {

    /** The max ancestors, but it can grow... */
    private final int _maxAncestors = 8;

    /**
     * The stack of start node + ancestors up to ROOTNODE,
     *  which we must avoid.
     */
    private int[] _stack = new int[_maxAncestors];

    /** (not sure yet... -sb) */
    private int _sp, _oldsp;

    /** _node precedes candidates.  This is the identity, not the handle! */
    private int _node;

    /**
     * True if this iterator has a reversed axis.
     *
     * @return true since this iterator is a reversed axis.
     */
    public boolean isReverse()
    {
      return true;
    }

    /**
     * Returns a deep copy of this iterator.
     *
     * @return a deep copy of this iterator.
     */
    public DTMAxisIterator cloneIterator()
    {

      _isRestartable = false;

      final int[] stackCopy = new int[_maxAncestors];

      try
      {
        final PrecedingIterator clone = (PrecedingIterator) super.clone();

        System.arraycopy(_stack, 0, stackCopy, 0, _stack.length);

        clone._stack = stackCopy;

        return clone.reset();
      }
      catch (CloneNotSupportedException e)
      {
        throw new DTMException("Iterator clone not supported.");
      }
    }

    /**
     * Set start to END should 'close' the iterator,
     * i.e. subsequent call to next() should return END.
     *
     * @param node Sets the root of the iteration.
     *
     * @return A DTMAxisIterator set to the start of the iteration.
     */
    public DTMAxisIterator setStartNode(int node)
    {

      if (_isRestartable)
      {

        // iterator is not a clone
        int parent, index;

        _startNode = node;
        _node = ROOTNODE;  // Remember it's the identity, not the full handle.
        _stack[index = 0] = node;

        if (node > ROOTNODE)
        {
          while ((parent = _parent(node)) != ROOTNODE)
          {
            if (++index == _stack.length)
            {
              final int[] stack = new int[index + 4];

              System.arraycopy(_stack, 0, stack, 0, index);

              _stack = stack;
            }

            _stack[index] = node = parent;
          }
        }

        _oldsp = _sp = index;

        return resetPosition();
      }

      return this;
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {

      final int node = _node + 1;

      if ((_sp >= 0) && (node < _stack[_sp]))
      {
        return returnNode((_node = node) | m_dtmIdent);
      }
      else
      {
        _node = node;  // skip ancestor

        return --_sp >= 0 ? next() : NULL;
      }
    }

    // redefine DTMAxisIteratorBase's reset

    /**
     * Resets the iterator to the last start node.
     *
     * @return A DTMAxisIterator, which may or may not be the same as this
     *         iterator.
     */
    public DTMAxisIterator reset()
    {

      _sp = _oldsp;

      return resetPosition();
    }

    /**
     * Remembers the current node for the next call to gotoMark().
     */
    public void setMark()
    {
      _markedNode = _node;
    }

    /**
     * Restores the current node remembered by setMark().
     */
    public void gotoMark()
    {
      _node = _markedNode;
    }
  }  // end of PrecedingIterator

  /**
   * Iterator that returns preceding nodes of agiven type for a
   * given node. This includes the node set {root+1, start-1}, but
   * excludes all ancestors.
   */
  private final class TypedPrecedingIterator extends PrecedingIterator
  {

    /** The extended type ID that was requested. */
    private final int _nodeType;

    /**
     * Constructor TypedPrecedingIterator
     *
     *
     * @param type The extended type ID being requested.
     */
    public TypedPrecedingIterator(int type)
    {
      _nodeType = type;
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {

      int node;

      while ((node = super.next()) != NULL
             && getExpandedTypeID(node) != _nodeType){}

      return node;
    }
  }  // end of TypedPrecedingIterator

  /**
   * Iterator that returns following nodes of for a given node.
   */
  private class FollowingIterator extends DTMAxisIteratorBase
  {

    /** _node precedes search for next. */
    protected int _node;

    /**
     * Set start to END should 'close' the iterator,
     * i.e. subsequent call to next() should return END.
     *
     * @param node Sets the root of the iteration.
     *
     * @return A DTMAxisIterator set to the start of the iteration.
     */
    public DTMAxisIterator setStartNode(int node)
    {

      if (_isRestartable)
      {
        _startNode = node;

        // ?? -sb
        // find rightmost descendant (or self)
        // int current;
        // while ((node = getLastChild(current = node)) != NULL){}
        // _node = current;
        _node = node;

        // _node precedes possible following(node) nodes
        return resetPosition();
      }

      return this;
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {

      int node = _node;

      while (END != node)
      {
        node++;

        int type = _type(node);

        if (DTM.NAMESPACE_NODE != type && DTM.ATTRIBUTE_NODE != type)
        {
          _node = node;

          return returnNode(_node | m_dtmIdent);
        }
      }

      return returnNode(_node = END);
    }

    /**
     * Remembers the current node for the next call to gotoMark().
     */
    public void setMark()
    {
      _markedNode = _node;
    }

    /**
     * Restores the current node remembered by setMark().
     */
    public void gotoMark()
    {
      _node = _markedNode;
    }
  }  // end of FollowingIterator

  /**
   * Iterator that returns following nodes of a given type for a given node.
   */
  private final class TypedFollowingIterator extends FollowingIterator
  {

    /** The extended type ID that was requested. */
    private final int _nodeType;

    /**
     * Constructor TypedFollowingIterator
     *
     *
     * @param type The extended type ID being requested.
     */
    public TypedFollowingIterator(int type)
    {
      _nodeType = type;
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {

      int node;

      while ((node = super.next()) != NULL
             && getExpandedTypeID(node) != _nodeType){}

      return returnNode(node);
    }
  }  // end of TypedFollowingIterator

  /**
   * Iterator that returns the ancestors of a given node in document
   * order.  (NOTE!  This was changed from the XSLTC code!)
   */
  private class AncestorIterator extends DTMAxisIteratorBase
  {

    /** The current ancestor index. */
    protected int _index;

    /**
     * True if this iterator has a reversed axis.
     *
     * @return true since this iterator is a reversed axis.
     */
    public final boolean isReverse()
    {
      return true;
    }

    /**
     * Returns the last element in this interation.
     *
     * @return the last element in this interation.
     */
    public int getLast()
    {
      return (_startNode);
    }

    /**
     * Returns a deep copy of this iterator.
     *
     * @return a deep copy of this iterator.
     */
    public DTMAxisIterator cloneIterator()
    {

      _isRestartable = false;  // must set to false for any clone

      try
      {
        final AncestorIterator clone = (AncestorIterator) super.clone();

        clone._startNode = _startNode;

        return clone.reset();
      }
      catch (CloneNotSupportedException e)
      {
        throw new DTMException("Iterator clone not supported.");
      }
    }

    /**
     * Set start to END should 'close' the iterator,
     * i.e. subsequent call to next() should return END.
     *
     * @param node Sets the root of the iteration.
     *
     * @return A DTMAxisIterator set to the start of the iteration.
     */
    public DTMAxisIterator setStartNode(int node)
    {

      if (_isRestartable)
      {
        if (_includeSelf)
          _startNode = node;
        else
          _startNode = getParent(node);

        _index = getDocument();

        return resetPosition();
      }

      return this;
    }

    /**
     * Resets the iterator to the last start node.
     *
     * @return A DTMAxisIterator, which may or may not be the same as this
     *         iterator.
     */
    public DTMAxisIterator reset()
    {

      _index = _startNode;

      return resetPosition();
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {

      int next = _index;

      // The alternative to this is to just allocate a stack in setStartNode.
      // Given often next() is only called once, I'm not sure that would 
      // be optimal.  -sb
      int node = _startNode;

      while (node != END && node != _index)
      {
        _index = node;
        node = getParent(node);
      }

      return (next);
    }

    /**
     * Remembers the current node for the next call to gotoMark().
     */
    public void setMark()
    {
      _markedNode = _index;
    }

    /**
     * Restores the current node remembered by setMark().
     */
    public void gotoMark()
    {
      _index = _markedNode;
    }
  }  // end of AncestorIterator

  /**
   * Typed iterator that returns the ancestors of a given node.
   */
  private final class TypedAncestorIterator extends AncestorIterator
  {

    /** The extended type ID that was requested. */
    private final int _nodeType;

    /**
     * Constructor TypedAncestorIterator
     *
     *
     * @param type The extended type ID being requested.
     */
    public TypedAncestorIterator(int type)
    {
      _nodeType = type;
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {

      int node;

      while ((node = super.next()) != NULL)
      {
        if (getExpandedTypeID(node) == _nodeType)
          return returnNode(node);
      }

      return (NULL);
    }

    /**
     * Returns the last element in this interation.
     *
     * @return the last element in this interation.
     */
    public int getLast()
    {

      int last = NULL;
      int curr = _startNode;

      while (curr != NULL)
      {
        if (getExpandedTypeID(curr) == _nodeType)
          last = curr;

        curr = getParent(curr);
      }

      return (last);
    }
  }  // end of TypedAncestorIterator

  /**
   * Iterator that returns the descendants of a given node.
   */
  private class DescendantIterator extends DTMAxisIteratorBase
  {

    /** _node precedes search for next */
    protected int _node;  // Identity, not handle!

    /**
     * Set start to END should 'close' the iterator,
     * i.e. subsequent call to next() should return END.
     *
     * @param node Sets the root of the iteration.
     *
     * @return A DTMAxisIterator set to the start of the iteration.
     */
    public DTMAxisIterator setStartNode(int node)
    {

      if (_isRestartable)
      {
        node = node & m_mask;
        _startNode = node;

        if (_includeSelf)
          node--;

        _node = node;

        return resetPosition();
      }

      return this;
    }

    /**
     * Tell if this node identity is a descendant.  Assumes that
     * the node info for the element has already been obtained.
     * @param identity The index number of the node in question.
     * @return true if the index is a descendant of _startNode.
     */
    protected boolean isDescendant(int identity)
    {
      return _parent(identity) >= _startNode;
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {

      while (true)
      {
        int node = ++_node;
        int type = _type(node);

        if (NULL == type ||!isDescendant(node))
          return END;

        if (ATTRIBUTE_NODE == type || NAMESPACE_NODE == type)
          continue;

        return returnNode(node | m_dtmIdent);  // make handle.
      }
    }

    /**
     * Remembers the current node for the next call to gotoMark().
     */
    public void setMark()
    {
      _markedNode = _node;
    }

    /**
     * Restores the current node remembered by setMark().
     */
    public void gotoMark()
    {
      _node = _markedNode;
    }
  }  // end of DescendantIterator

  /**
   * Typed iterator that returns the descendants of a given node.
   */
  private final class TypedDescendantIterator extends DescendantIterator
  {

    /** The extended type ID that was requested. */
    private final int _nodeType;

    /**
     * Constructor TypedDescendantIterator
     *
     *
     * @param nodeType Extended type ID being requested.
     */
    public TypedDescendantIterator(int nodeType)
    {
      _nodeType = nodeType;
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {

      int node;

      while ((node = super.next()) != NULL
             && getExpandedTypeID(node) != _nodeType){}

      return node;
    }
  }  // end of TypedDescendantIterator

  /**
   * Iterator that returns the descendants of a given node.
   * I'm not exactly clear about this one... -sb
   */
  private class NthDescendantIterator extends DescendantIterator
  {

    /** The current nth position. */
    int _pos;

    /**
     * Constructor NthDescendantIterator
     *
     *
     * @param pos The nth position being requested.
     */
    public NthDescendantIterator(int pos)
    {
      _pos = pos;
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {

      // I'm not exactly clear yet what this is doing... -sb
      int node;

      while ((node = super.next()) != END)
      {
        node = (node & m_mask);

        int parent = _parent(node);
        int child = _firstch(parent);
        int pos = 0;

        do
        {
          int type = _type(child);

          if (ELEMENT_NODE == type)
            pos++;
        }
        while ((pos < _pos) && (child = _nextsib(child)) != END);

        if (node == child)
          return node;
      }

      return (END);
    }
  }  // end of NthDescendantIterator

  /**
   * Class SingletonIterator.
   */
  private class SingletonIterator extends DTMAxisIteratorBase
  {

    /** The current node. */
    private int _node;

    /** (not sure yet what this is.  -sb) */
    private final boolean _isConstant;

    /**
     * Constructor SingletonIterator
     *
     */
    public SingletonIterator()
    {
      this(Integer.MIN_VALUE, false);
    }

    /**
     * Constructor SingletonIterator
     *
     *
     * @param node The node handle to return.
     */
    public SingletonIterator(int node)
    {
      this(node, false);
    }

    /**
     * Constructor SingletonIterator
     *
     *
     * @param node the node handle to return.
     * @param constant (Not sure what this is yet.  -sb)
     */
    public SingletonIterator(int node, boolean constant)
    {
      _node = _startNode = node;
      _isConstant = constant;
    }

    /**
     * Set start to END should 'close' the iterator,
     * i.e. subsequent call to next() should return END.
     *
     * @param node Sets the root of the iteration.
     *
     * @return A DTMAxisIterator set to the start of the iteration.
     */
    public DTMAxisIterator setStartNode(int node)
    {

      if (_isConstant)
      {
        _node = _startNode;

        return resetPosition();
      }
      else if (_isRestartable)
      {
        if (_node == Integer.MIN_VALUE)
        {
          _node = _startNode = node;
        }

        return resetPosition();
      }

      return this;
    }

    /**
     * Resets the iterator to the last start node.
     *
     * @return A DTMAxisIterator, which may or may not be the same as this
     *         iterator.
     */
    public DTMAxisIterator reset()
    {

      if (_isConstant)
      {
        _node = _startNode;

        return resetPosition();
      }
      else
      {
        final boolean temp = _isRestartable;

        _isRestartable = true;

        setStartNode(_startNode);

        _isRestartable = temp;
      }

      return this;
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {

      final int result = _node;

      _node = END;

      return returnNode(result);
    }

    /**
     * Remembers the current node for the next call to gotoMark().
     */
    public void setMark()
    {
      _markedNode = _node;
    }

    /**
     * Restores the current node remembered by setMark().
     */
    public void gotoMark()
    {
      _node = _markedNode;
    }
  }

  /**
   * Iterator that returns a given node only if it is of a given type.
   */
  private final class TypedSingletonIterator extends SingletonIterator
  {

    /** The extended type ID that was requested. */
    private final int _nodeType;

    /**
     * Constructor TypedSingletonIterator
     *
     *
     * @param nodeType The extended type ID being requested.
     */
    public TypedSingletonIterator(int nodeType)
    {
      _nodeType = nodeType;
    }

    /**
     * Get the next node in the iteration.
     *
     * @return The next node handle in the iteration, or END.
     */
    public int next()
    {

      final int result = super.next();

      return getExpandedTypeID(result) == _nodeType ? result : NULL;
    }
  }  // end of TypedSingletonIterator

  /**
   * Implements traversal of the Ancestor access, in reverse document order.
   */
  private class AncestorTraverser extends DTMAxisTraverser
  {

    /**
     * Traverse to the next node after the current node.
     *
     * @param context The context node if this iteration.
     * @param current The current node of the iteration.
     *
     * @return the next node in the iteration, or DTM.NULL.
     */
    public int next(int context, int current)
    {
      return m_parent[current & m_mask] | m_dtmIdent;
    }

    /**
     * Traverse to the next node after the current node that is matched
     * by the extended type ID.
     *
     * @param context The context node if this iteration.
     * @param current The current node of the iteration.
     * @param extendedTypeID The extended type ID that must match.
     *
     * @return the next node in the iteration, or DTM.NULL.
     */
    public int next(int context, int current, int extendedTypeID)
    {

      current = current & m_mask;

      while (DTM.NULL != (current = m_parent[current]))
      {
        if (m_exptype[current] == extendedTypeID)
          return current | m_dtmIdent;
      }

      return NULL;
    }
  }

  /**
   * Implements traversal of the Ancestor access, in reverse document order.
   */
  private class AncestorOrSelfTraverser extends AncestorTraverser
  {

    /**
     * By the nature of the stateless traversal, the context node can not be
     * returned or the iteration will go into an infinate loop.  To see if
     * the self node should be processed, use this function.
     *
     * @param context The context node if this traversal.
     *
     * @return the first node in the traversal.
     */
    public int first(int context)
    {
      return context;
    }

    /**
     * By the nature of the stateless traversal, the context node can not be
     * returned or the iteration will go into an infinate loop.  To see if
     * the self node should be processed, use this function.  If the context
     * node does not match the extended type ID, this function will return
     * false.
     *
     * @param context The context node if this traversal.
     * @param extendedTypeID The extended type ID that must match.
     *
     * @return the first node in the traversal.
     */
    public int first(int context, int extendedTypeID)
    {
      return (m_exptype[context & m_mask] == extendedTypeID)
             ? context : next(context, context, extendedTypeID);
    }
  }

  /**
   * Implements traversal of the Ancestor access, in reverse document order.
   */
  private class AttributeTraverser extends DTMAxisTraverser
  {

    /**
     * Traverse to the next node after the current node.
     *
     * @param context The context node if this iteration.
     * @param current The current node of the iteration.
     *
     * @return the next node in the iteration, or DTM.NULL.
     */
    public int next(int context, int current)
    {
      return (context == current)
             ? getFirstAttribute(context) : getNextAttribute(current);
    }

    /**
     * Traverse to the next node after the current node that is matched
     * by the extended type ID.
     *
     * @param context The context node if this iteration.
     * @param current The current node of the iteration.
     * @param extendedTypeID The extended type ID that must match.
     *
     * @return the next node in the iteration, or DTM.NULL.
     */
    public int next(int context, int current, int extendedTypeID)
    {

      current = (context == current)
                ? getFirstAttribute(context) : getNextAttribute(current);

      do
      {
        if (m_exptype[current] == extendedTypeID)
          return current;
      }
      while (DTM.NULL != (current = getNextAttribute(current)));

      return NULL;
    }
  }

  /**
   * Implements traversal of the Ancestor access, in reverse document order.
   */
  private class ChildTraverser extends DTMAxisTraverser
  {

    /**
     * Traverse to the next node after the current node.
     *
     * @param context The context node if this iteration.
     * @param current The current node of the iteration.
     *
     * @return the next node in the iteration, or DTM.NULL.
     */
    public int next(int context, int current)
    {

      // %OPT%
      return (context == current)
             ? getFirstChild(context) : getNextSibling(current);
    }

    /**
     * Traverse to the next node after the current node that is matched
     * by the extended type ID.
     *
     * @param context The context node if this iteration.
     * @param current The current node of the iteration.
     * @param extendedTypeID The extended type ID that must match.
     *
     * @return the next node in the iteration, or DTM.NULL.
     */
    public int next(int context, int current, int extendedTypeID)
    {

      current = (context == current)
                ? getFirstChild(context) : getNextSibling(current);

      do
      {
        if (m_exptype[current & m_mask] == extendedTypeID)
          return current;
      }
      while (DTM.NULL != (current = getNextSibling(current)));

      return NULL;
    }
  }

  /**
   * Implements traversal of the Ancestor access, in reverse document order.
   */
  private class DescendantTraverser extends DTMAxisTraverser
  {

    /**
     * Tell if this node identity is a descendant.  Assumes that
     * the node info for the element has already been obtained.
     *
     * NEEDSDOC @param subtreeRootIdentity
     * @param identity The index number of the node in question.
     * @return true if the index is a descendant of _startNode.
     */
    protected boolean isDescendant(int subtreeRootIdentity, int identity)
    {
      return _parent(identity) >= subtreeRootIdentity;
    }

    /**
     * Traverse to the next node after the current node.
     *
     * @param context The context node if this iteration.
     * @param current The current node of the iteration.
     *
     * @return the next node in the iteration, or DTM.NULL.
     */
    public int next(int context, int current)
    {

      int subtreeRootIdent = context & m_mask;

      for (current = (current & m_mask) + 1; ; current++)
      {
        int type = _type(current);  // may call nextNode()

        if (!isDescendant(subtreeRootIdent, current))
          return NULL;

        if (ATTRIBUTE_NODE == type || NAMESPACE_NODE == type)
          continue;

        return (current | m_dtmIdent);  // make handle.
      }
    }

    /**
     * Traverse to the next node after the current node that is matched
     * by the extended type ID.
     *
     * @param context The context node if this iteration.
     * @param current The current node of the iteration.
     * @param extendedTypeID The extended type ID that must match.
     *
     * @return the next node in the iteration, or DTM.NULL.
     */
    public int next(int context, int current, int extendedTypeID)
    {

      int subtreeRootIdent = context & m_mask;

      for (current = (current & m_mask) + 1; ; current++)
      {
        int exptype = _exptype(current);  // may call nextNode()

        if (!isDescendant(subtreeRootIdent, current))
          return NULL;

        if (exptype != extendedTypeID)
          continue;

        return (current | m_dtmIdent);  // make handle.
      }
    }
  }

  /**
   * Implements traversal of the Ancestor access, in reverse document order.
   */
  private class DescendantOrSelfTraverser extends DescendantTraverser
  {

    /**
     * By the nature of the stateless traversal, the context node can not be
     * returned or the iteration will go into an infinate loop.  To see if
     * the self node should be processed, use this function.
     *
     * @param context The context node if this traversal.
     *
     * @return the first node in the traversal.
     */
    public int first(int context)
    {
      return context;
    }

    /**
     * By the nature of the stateless traversal, the context node can not be
     * returned or the iteration will go into an infinate loop.  To see if
     * the self node should be processed, use this function.  If the context
     * node does not match the extended type ID, this function will return
     * false.
     *
     * @param context The context node if this traversal.
     * @param extendedTypeID The extended type ID that must match.
     *
     * @return the first node in the traversal.
     */
    public int first(int context, int extendedTypeID)
    {
      return (m_exptype[context & m_mask] == extendedTypeID)
             ? context : next(context, context, extendedTypeID);
    }
  }

  /**
   * Implements traversal of the entire subtree, including the root node.
   */
  private class SubtreeTraverser extends DescendantOrSelfTraverser
  {

    /**
     * Traverse to the next node after the current node.
     *
     * @param context The context node if this iteration.
     * @param current The current node of the iteration.
     *
     * @return the next node in the iteration, or DTM.NULL.
     */
    public int next(int context, int current)
    {

      int subtreeRootIdent = context & m_mask;

      for (current = (current & m_mask) + 1; ; current++)
      {
        _exptype(current);  // make sure it's here.

        if (!isDescendant(subtreeRootIdent, current))
          return NULL;

        return (current | m_dtmIdent);  // make handle.
      }
    }
  }

  /**
   * Implements traversal of the following access, in document order.
   */
  private class FollowingTraverser extends DescendantTraverser
  {

    /**
     * Traverse to the next node after the current node.
     *
     * @param context The context node if this iteration.
     * @param current The current node of the iteration.
     *
     * @return the next node in the iteration, or DTM.NULL.
     */
    public int next(int context, int current)
    {

      int subtreeRootIdent = context & m_mask;

      if (context == current)
        current = getNextSibling(context) & m_mask;
      else
        current = (current & m_mask) + 1;

      for (; ; current++)
      {
        int type = _type(current);  // may call nextNode()

        if (NULL == type)
          return NULL;

        if (ATTRIBUTE_NODE == type || NAMESPACE_NODE == type)
          continue;

        return (current | m_dtmIdent);  // make handle.
      }
    }

    /**
     * Traverse to the next node after the current node that is matched
     * by the extended type ID.
     *
     * @param context The context node if this iteration.
     * @param current The current node of the iteration.
     * @param extendedTypeID The extended type ID that must match.
     *
     * @return the next node in the iteration, or DTM.NULL.
     */
    public int next(int context, int current, int extendedTypeID)
    {

      int subtreeRootIdent = context & m_mask;

      if (context == current)
        current = getNextSibling(context) & m_mask;
      else
        current = (current & m_mask) + 1;

      for (; ; current++)
      {
        int exptype = _exptype(current);  // may call nextNode()

        if (NULL == exptype)
          return NULL;

        if (exptype != extendedTypeID)
          continue;

        return (current | m_dtmIdent);  // make handle.
      }
    }
  }

  /**
   * Implements traversal of the Ancestor access, in reverse document order.
   */
  private class FollowingSiblingTraverser extends DTMAxisTraverser
  {

    /**
     * Traverse to the next node after the current node.
     *
     * @param context The context node if this iteration.
     * @param current The current node of the iteration.
     *
     * @return the next node in the iteration, or DTM.NULL.
     */
    public int next(int context, int current)
    {
      return getNextSibling(current);
    }

    /**
     * Traverse to the next node after the current node that is matched
     * by the extended type ID.
     *
     * @param context The context node if this iteration.
     * @param current The current node of the iteration.
     * @param extendedTypeID The extended type ID that must match.
     *
     * @return the next node in the iteration, or DTM.NULL.
     */
    public int next(int context, int current, int extendedTypeID)
    {

      while (DTM.NULL != (current = getNextSibling(current)))
      {
        if (m_exptype[current & m_mask] == extendedTypeID)
          return current;
      }

      return NULL;
    }
  }

  /**
   * Implements traversal of the Ancestor access, in reverse document order.
   */
  private class NamespaceDeclsTraverser extends DTMAxisTraverser
  {

    /**
     * Traverse to the next node after the current node.
     *
     * @param context The context node if this iteration.
     * @param current The current node of the iteration.
     *
     * @return the next node in the iteration, or DTM.NULL.
     */
    public int next(int context, int current)
    {

      return (context == current)
             ? getFirstNamespaceNode(context, false)
             : getNextNamespaceNode(context, current, false);
    }

    /**
     * Traverse to the next node after the current node that is matched
     * by the extended type ID.
     *
     * @param context The context node if this iteration.
     * @param current The current node of the iteration.
     * @param extendedTypeID The extended type ID that must match.
     *
     * @return the next node in the iteration, or DTM.NULL.
     */
    public int next(int context, int current, int extendedTypeID)
    {

      current = (context == current)
                ? getFirstNamespaceNode(context, false)
                : getNextNamespaceNode(context, current, false);

      do
      {
        if (m_exptype[current] == extendedTypeID)
          return current;
      }
      while (DTM.NULL
             != (current = getNextNamespaceNode(context, current, false)));

      return NULL;
    }
  }

  /**
   * Implements traversal of the Ancestor access, in reverse document order.
   */
  private class NamespaceTraverser extends DTMAxisTraverser
  {

    /**
     * Traverse to the next node after the current node.
     *
     * @param context The context node if this iteration.
     * @param current The current node of the iteration.
     *
     * @return the next node in the iteration, or DTM.NULL.
     */
    public int next(int context, int current)
    {

      return (context == current)
             ? getFirstNamespaceNode(context, true)
             : getNextNamespaceNode(context, current, true);
    }

    /**
     * Traverse to the next node after the current node that is matched
     * by the extended type ID.
     *
     * @param context The context node if this iteration.
     * @param current The current node of the iteration.
     * @param extendedTypeID The extended type ID that must match.
     *
     * @return the next node in the iteration, or DTM.NULL.
     */
    public int next(int context, int current, int extendedTypeID)
    {

      current = (context == current)
                ? getFirstNamespaceNode(context, true)
                : getNextNamespaceNode(context, current, true);

      do
      {
        if (m_exptype[current] == extendedTypeID)
          return current;
      }
      while (DTM.NULL
             != (current = getNextNamespaceNode(context, current, true)));

      return NULL;
    }
  }

  /**
   * Implements traversal of the Ancestor access, in reverse document order.
   */
  private class ParentTraverser extends DTMAxisTraverser
  {

    /**
     * Traverse to the next node after the current node.
     *
     * @param context The context node if this iteration.
     * @param current The current node of the iteration.
     *
     * @return the next node in the iteration, or DTM.NULL.
     */
    public int next(int context, int current)
    {

      if (context == current)
        return m_parent[current & m_mask] | m_dtmIdent;
      else
        return NULL;
    }

    /**
     * Traverse to the next node after the current node that is matched
     * by the extended type ID.
     *
     * @param context The context node if this iteration.
     * @param current The current node of the iteration.
     * @param extendedTypeID The extended type ID that must match.
     *
     * @return the next node in the iteration, or DTM.NULL.
     */
    public int next(int context, int current, int extendedTypeID)
    {

      if (context != current)
        return NULL;

      current = current & m_mask;

      while (NULL != (current = m_parent[current]))
      {
        if (m_exptype[current] == extendedTypeID)
          return (current | m_dtmIdent);
      }

      return NULL;
    }
  }

  /**
   * Implements traversal of the Ancestor access, in reverse document order.
   */
  private class PrecedingTraverser extends DTMAxisTraverser
  {

    /**
     * Tell if the current identity is an ancestor of the context identity.
     * This is an expensive operation, made worse by the stateless traversal.
     * But the preceding axis is used fairly infrequently.
     *
     * @param contextIdent The context node of the axis traversal.
     * @param currentIdent The node in question.
     * @return true if the currentIdent node is an ancestor of contextIdent.
     */
    protected boolean isAncestor(int contextIdent, int currentIdent)
    {

      for (contextIdent = m_parent[contextIdent]; DTM.NULL != contextIdent;
              contextIdent = m_parent[contextIdent])
      {
        if (contextIdent == currentIdent)
          return true;
      }

      return false;
    }

    /**
     * Traverse to the next node after the current node.
     *
     * @param context The context node if this iteration.
     * @param current The current node of the iteration.
     *
     * @return the next node in the iteration, or DTM.NULL.
     */
    public int next(int context, int current)
    {

      int subtreeRootIdent = context & m_mask;

      for (current = (current & m_mask) - 1; current >= 0; current--)
      {
        int exptype = m_exptype[current];
        int type = ExpandedNameTable.getType(exptype);

        if (ATTRIBUTE_NODE == type || NAMESPACE_NODE == type
                || isAncestor(subtreeRootIdent, current))
          continue;

        return (current | m_dtmIdent);  // make handle.
      }

      return NULL;
    }

    /**
     * Traverse to the next node after the current node that is matched
     * by the extended type ID.
     *
     * @param context The context node if this iteration.
     * @param current The current node of the iteration.
     * @param extendedTypeID The extended type ID that must match.
     *
     * @return the next node in the iteration, or DTM.NULL.
     */
    public int next(int context, int current, int extendedTypeID)
    {

      int subtreeRootIdent = context & m_mask;

      for (current = (current & m_mask) - 1; current >= 0; current--)
      {
        int exptype = m_exptype[current];
        int type = ExpandedNameTable.getType(exptype);

        if (exptype != extendedTypeID
                || isAncestor(subtreeRootIdent, current))
          continue;

        return (current | m_dtmIdent);  // make handle.
      }

      return NULL;
    }
  }

  /**
   * Implements traversal of the Ancestor access, in reverse document order.
   */
  private class PrecedingSiblingTraverser extends DTMAxisTraverser
  {

    /**
     * Traverse to the next node after the current node.
     *
     * @param context The context node if this iteration.
     * @param current The current node of the iteration.
     *
     * @return the next node in the iteration, or DTM.NULL.
     */
    public int next(int context, int current)
    {
      return getPreviousSibling(current);
    }

    /**
     * Traverse to the next node after the current node that is matched
     * by the extended type ID.
     *
     * @param context The context node if this iteration.
     * @param current The current node of the iteration.
     * @param extendedTypeID The extended type ID that must match.
     *
     * @return the next node in the iteration, or DTM.NULL.
     */
    public int next(int context, int current, int extendedTypeID)
    {

      while (DTM.NULL != (current = getPreviousSibling(current)))
      {
        if (m_exptype[current & m_mask] == extendedTypeID)
          return current;
      }

      return NULL;
    }
  }

  /**
   * Implements traversal of the Self axis.
   */
  private class SelfTraverser extends DTMAxisTraverser
  {

    /**
     * By the nature of the stateless traversal, the context node can not be
     * returned or the iteration will go into an infinate loop.  To see if
     * the self node should be processed, use this function.
     *
     * @param context The context node if this traversal.
     *
     * @return the first node in the traversal.
     */
    public int first(int context)
    {
      return context;
    }

    /**
     * By the nature of the stateless traversal, the context node can not be
     * returned or the iteration will go into an infinate loop.  To see if
     * the self node should be processed, use this function.  If the context
     * node does not match the extended type ID, this function will return
     * false.
     *
     * @param context The context node if this traversal.
     * @param extendedTypeID The extended type ID that must match.
     *
     * @return the first node in the traversal.
     */
    public int first(int context, int extendedTypeID)
    {
      return (m_exptype[context & m_mask] == extendedTypeID) ? context : NULL;
    }

    /**
     * Traverse to the next node after the current node.
     *
     * @param context The context node if this iteration.
     * @param current The current node of the iteration.
     *
     * @return Always return NULL for this axis.
     */
    public int next(int context, int current)
    {
      return NULL;
    }

    /**
     * Traverse to the next node after the current node that is matched
     * by the extended type ID.
     *
     * @param context The context node if this iteration.
     * @param current The current node of the iteration.
     * @param extendedTypeID The extended type ID that must match.
     *
     * @return the next node in the iteration, or DTM.NULL.
     */
    public int next(int context, int current, int extendedTypeID)
    {
      return NULL;
    }
  }
}
