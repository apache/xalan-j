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
package org.apache.xml.dtm.dom2dtm;

import org.apache.xml.dtm.*;
import org.apache.xml.utils.IntVector;
import org.apache.xml.utils.IntStack;

import org.w3c.dom.*;

import java.util.Vector;

/**
 * The <code>DOM2DTM</code> class serves up a DOM via a DTM API.
 */
public class DOM2DTM implements DTM
{

  /**
   * The node objects.  The instance part of the handle indexes directly
   * into this vector.  Each DTM node may actually be composed of several
   * DOM nodes.
   */
  protected Vector m_nodes = new Vector();

  /**
   * This is extra information about the node objects.  Each information
   * block is composed of several array members.  The size of this will always
   * be (m_nodes.size() * NODEINFOBLOCKSIZE).
   */
  protected IntVector m_info = new IntVector();

  // Offsets into each set of integers in the <code>m_info</code> table.

  /** %TBD% Doc */
  static final int OFFSET_TYPE = 0;

  /** %TBD% Doc */
  static final int OFFSET_LEVEL = 1;

  /** %TBD% Doc */
  static final int OFFSET_FIRSTCHILD = 2;

  /** %TBD% Doc */
  static final int OFFSET_NEXTSIBLING = 3;

  /** %TBD% Doc */
  static final int OFFSET_PREVSIBLING = 4;

  /** %TBD% Doc */
  static final int OFFSET_PARENT = 5;

  /**
   * This represents the number of integers per node in the
   * <code>m_info</code> member variable.
   */
  static final int NODEINFOBLOCKSIZE = 5;

  /**
   * The value to use when the information has not been built yet.
   */
  static final int NOTPROCESSED = DTM.NULL - 1;

  /** NEEDSDOC Field NODEIDENTITYBITS          */
  static final int NODEIDENTITYBITS = 0x000FFFFF;

  /**
   * The DTM manager who "owns" this DTM.
   */
  protected DTMManager m_mgr;

  /** %TBD% Doc */
  protected int m_dtmIdent;

  /** %TBD% Doc */
  protected int m_mask;

  /**
   * Construct a DOM2DTM object from a DOM node.
   *
   * NEEDSDOC @param mgr
   * NEEDSDOC @param node
   */
  public DOM2DTM(DTMManager mgr, Node node)
  {

    m_mgr = mgr;
    m_root = node;
    m_pos = null;
    m_nodesAreProcessed = false;
    m_dtmIdent = m_mgr.getDTMIdentity(this);
    m_mask = m_mgr.getNodeIdentityMask();

    addNode(node, 0, DTM.NULL, DTM.NULL);
  }

  /**
   * Construct the node map from the node.
   *
   * NEEDSDOC @param node
   * NEEDSDOC @param level
   * NEEDSDOC @param parentIndex
   * NEEDSDOC @param previousSibling
   *
   * NEEDSDOC ($objectName$) @return
   */
  protected int addNode(Node node, int level, int parentIndex,
                        int previousSibling)
  {

    int nodeIndex = m_nodes.size();

    m_nodes.addElement(node);

    int startInfo = nodeIndex * NODEINFOBLOCKSIZE;

    m_info.addElements(NODEINFOBLOCKSIZE);
    m_info.setElementAt(level, startInfo + OFFSET_LEVEL);
    int type = node.getNodeType();
    if(Node.ATTRIBUTE_NODE == type)
    {
      String name = node.getNodeName();
      if(name.startsWith("xmlns:") || name.equals("xmlns"))
      {
        type = DTM.NAMESPACE_NODE;
      }
    }
    m_info.setElementAt(type, startInfo + OFFSET_TYPE);
    m_info.setElementAt(NOTPROCESSED, startInfo + OFFSET_FIRSTCHILD);
    m_info.setElementAt(NOTPROCESSED, startInfo + OFFSET_NEXTSIBLING);
    m_info.setElementAt(previousSibling, startInfo + OFFSET_PREVSIBLING);
    m_info.setElementAt(parentIndex, startInfo + OFFSET_PARENT);

    if (DTM.NULL != previousSibling)
    {
      m_info.setElementAt(nodeIndex,
                          (previousSibling * NODEINFOBLOCKSIZE)
                          + OFFSET_NEXTSIBLING);
    }

    return nodeIndex;
  }

  /** The top of the subtree, may not be the same as m_context if "//foo" pattern. */
  transient private Node m_root;

  /** The current position in the tree. */
  transient private Node m_pos;

  /** true if all the nodes have been processed. */
  transient private boolean m_nodesAreProcessed;

  /**
   * %TBD% Needs doc... how to explain?
   * [0] index of parent.
   * [1] index of previous sibling.
   */
  transient private IntStack m_levelInfo = new IntStack();

  /**
   * %TBD% Doc
   */
  transient private NamedNodeMap m_attrs;

  /**
   * %TBD% Doc
   */
  transient private int m_attrsPos;

  /** NEEDSDOC Field LEVELINFO_PARENT          */
  static final int LEVELINFO_PARENT = 1;

  /** NEEDSDOC Field LEVELINFO_PREVSIB          */
  static final int LEVELINFO_PREVSIB = 0;

  /** NEEDSDOC Field LEVELINFO_NPERLEVEL          */
  static final int LEVELINFO_NPERLEVEL = 2;

  /**
   * This method iterates to the next node that will be added to the table.
   * Each call to this method adds a new node to the table, unless the end
   * is reached, in which case it returns null.
   *
   * NEEDSDOC ($objectName$) @return
   */
  protected Node nextNode()
  {

    if (m_nodesAreProcessed)
      return null;

    Node top = m_root;  // tells us when to stop.
    Node pos = (null == m_pos) ? m_root : m_pos;

    // non-recursive depth-first traversal.
    // while (null != pos)
    {

      // %TBD% Process attributes!
      Node nextNode;
      int type = pos.getNodeType();

      if (Node.ELEMENT_NODE == type)
      {
        m_attrs = pos.getAttributes();
        m_attrsPos = 0;

        if (null != m_attrs)
        {
          if (m_attrsPos < m_attrs.getLength())
            nextNode = m_attrs.item(m_attrsPos);
          else
            nextNode = null;
        }
        else
          nextNode = null;
      }
      else if (Node.ATTRIBUTE_NODE == type)
      {
        m_attrsPos++;

        if (m_attrsPos < m_attrs.getLength())
          nextNode = m_attrs.item(m_attrsPos);
        else
        {
          nextNode = null;

          m_levelInfo.quickPop(LEVELINFO_NPERLEVEL);
        }
      }
      else
        nextNode = null;

      if (null == nextNode)
        nextNode = pos.getFirstChild();

      if (null != nextNode)
      {
        int currentIndexHandle = m_nodes.size();

        m_levelInfo.push(currentIndexHandle);
        m_levelInfo.push(DTM.NULL);
      }

      while (null == nextNode)
      {
        if (top.equals(pos))
          break;

        nextNode = pos.getNextSibling();

        if (null == nextNode)
        {
          pos = pos.getParentNode();

          if ((null == pos) || (top.equals(pos)))
          {
            nextNode = null;

            break;
          }

          m_levelInfo.quickPop(LEVELINFO_NPERLEVEL);
        }
      }

      pos = nextNode;

      if (null != pos)
      {
        int level = m_levelInfo.size() / 2;

        addNode(pos, level, m_levelInfo.peek(LEVELINFO_PARENT),
                m_levelInfo.peek(LEVELINFO_PREVSIB));

        m_pos = pos;

        int currentIndexHandle = m_nodes.size();
        int sz = m_levelInfo.size();

        m_levelInfo.setElementAt(currentIndexHandle,
                                 sz - (1 + LEVELINFO_PREVSIB));

        return pos;
      }
    }

    m_nodesAreProcessed = true;

    return null;
  }

  /**
   * Get a Node from a handle.
   *
   * NEEDSDOC @param nodeHandle
   *
   * NEEDSDOC ($objectName$) @return
   */
  protected Node getNode(int nodeHandle)
  {

    int identity = nodeHandle & m_mask;

    return (Node) m_nodes.elementAt(identity);
  }

  /**
   * Get a Node from an identity index.
   *
   * NEEDSDOC @param nodeIdentity
   *
   * NEEDSDOC ($objectName$) @return
   */
  protected Node lookupNode(int nodeIdentity)
  {
    return (Node) m_nodes.elementAt(nodeIdentity);
  }

  /**
   * Get the next node identity value in the list, and call the iterator
   * if it hasn't been added yet.
   *
   * @param identity The node identity (index).
   * @return identity+1, or DTM.NULL.
   */
  protected int getNextNodeIdentity(int identity)
  {

    identity += 1;

    if (identity >= m_nodes.size())
    {
      Node node = nextNode();

      if (null == node)
        identity = DTM.NULL;
    }

    return identity;
  }

  /**
   * Get a node handle that is relative to the given node.
   *
   * @param identity The node identity.
   * @param offsetValue One of OFFSET_XXX values.
   * @return The relative node handle.
   */
  protected int getNodeInfo(int identity, int offsetValue)
  {

    int base = (identity * NODEINFOBLOCKSIZE);
    int info = m_info.elementAt(base + offsetValue);

    // Check to see if the information requested has been processed, and, 
    // if not, advance the iterator until we the information has been 
    // processed.
    while (info == NOTPROCESSED)
    {
      Node node = nextNode();

      info = m_info.elementAt(base + offsetValue);
    }

    return info;
  }

  // ========= DTM Implementation Control Functions. ==============

  /**
   * Set a suggested parse block size for the parser.
   *
   * @param blockSizeSuggestion Suggested size of the parse blocks, in bytes.
   */
  public void setParseBlockSize(int blockSizeSuggestion){}

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

    // %REVIEW% This may not be OK if there is an entity child?
    Node node = getNode(nodeHandle);

    return node.hasChildNodes();
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
    int firstChild = getNodeInfo(identity, OFFSET_FIRSTCHILD);

    return nodeHandle | m_dtmIdent;
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
    int child = getNodeInfo(identity, OFFSET_FIRSTCHILD);
    int lastChild = DTM.NULL;

    while (child != DTM.NULL)
    {
      lastChild = child;
      child = getNodeInfo(identity, OFFSET_NEXTSIBLING);
    }

    return lastChild | m_dtmIdent;
  }

  /**
   * Retrieves an attribute node by by qualified name and namespace URI.
   *
   * @param nodeHandle int Handle of the node.
   * @param namespaceURI The namespace URI of the attribute to
   *   retrieve, or null.
   * @param name The local name of the attribute to
   *   retrieve.
   * @return The attribute node handle with the specified name (
   *   <code>nodeName</code>) or <code>DTM.NULL</code> if there is no such
   *   attribute.
   */
  public int getAttributeNode(int nodeHandle, String namespaceURI,
                              String name)
  {

    // %OPT% This is probably slower than it needs to be.
    if (null == namespaceURI)
      namespaceURI = "";

    int type = getNodeType(nodeHandle);

    if (DTM.ELEMENT_NODE == type)
    {

      // Assume that attributes immediately follow the element.
      int identity = nodeHandle & m_mask;

      while (DTM.NULL != (identity = getNextNodeIdentity(identity)))
      {
        Node node = lookupNode(identity);

        // Assume this can not be null.
        type = node.getNodeType();
        if (type == DTM.ATTRIBUTE_NODE)
        {
          String nodeuri = node.getNamespaceURI();

          if (null == nodeuri)
            nodeuri = "";

          String nodelocalname = node.getLocalName();

          if (nodeuri.equals(namespaceURI) && name.equals(nodelocalname))
            return identity | m_dtmIdent;
        }
        else if(DTM.NAMESPACE_NODE != type)
        {
          break;  // should be no more attribute nodes.
        }
      }
    }

    return DTM.NULL;
  }

  /**
   * Given a node handle, get the index of the node's first attribute.
   *
   * @param nodeHandle int Handle of the node.
   * @return Handle of first attribute, or DTM.NULL to indicate none exists.
   */
  public int getFirstAttribute(int nodeHandle)
  {

    int type = getNodeType(nodeHandle);

    while (DTM.ELEMENT_NODE == type)
    {
     // Assume that attributes and namespaces immediately follow the element.
      int identity = nodeHandle & m_mask;

      if(DTM.NULL != (identity = getNextNodeIdentity(identity)))
      {
        Node node = lookupNode(identity);

        // Assume this can not be null.
        type = node.getNodeType();
        if (node.getNodeType() == DTM.ATTRIBUTE_NODE)
        {
          return identity | m_dtmIdent;
        }
        else if(DTM.NAMESPACE_NODE != type)
        {
          break;
        }
      }
    }
    return DTM.NULL;
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

    // %TBD%
    return 0;
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

    // %TBD%
    return 0;
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

    // %TBD%
    return 0;
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

      while(DTM.NULL != (identity = getNextNodeIdentity(identity)))
      {
        Node node = lookupNode(identity);

        // Assume this can not be null.
        type = node.getNodeType();
        if (type == DTM.ATTRIBUTE_NODE)
        {
          return identity | m_dtmIdent;
        }
        else if(type != DTM.NAMESPACE_NODE)
        {
          break;
        }
        
      }
    }
    return DTM.NULL;
  }

  /**
   * Given a namespace handle, advance to the next namespace.
   *
   * @param namespaceHandle handle to node which must be of type NAMESPACE_NODE.
   * NEEDSDOC @param inScope
   * @return handle of next namespace, or DTM.NULL to indicate none exists.
   */
  public int getNextNamespaceNode(int namespaceHandle, boolean inScope)
  {

    // %TBD%
    return 0;
  }

  /**
   * Given a node handle, advance to its next descendant.
   * If not yet resolved, waits for more nodes to be added to the document and
   * tries again.
   *
   * @param subtreeRootNodeHandle
   *
   * NEEDSDOC @param subtreeRootHandle
   * @param nodeHandle int Handle of the node.
   * @return handle of next descendant,
   * or DTM.NULL to indicate none exists.
   */
  public int getNextDescendant(int subtreeRootHandle, int nodeHandle)
  {

    // %TBD%
    return 0;
  }

  /**
   * Given a node handle, advance to the next node on the following axis.
   *
   * @param axisContextHandle the start of the axis that is being traversed.
   * @param nodeHandle
   * @return handle of next sibling,
   * or DTM.NULL to indicate none exists.
   */
  public int getNextFollowing(int axisContextHandle, int nodeHandle)
  {

    // %TBD%
    return 0;
  }

  /**
   * Given a node handle, advance to the next node on the preceding axis.
   *
   * @param axisContextHandle the start of the axis that is being traversed.
   * @param nodeHandle the id of the node.
   * @return int Node-number of preceding sibling,
   * or DTM.NULL to indicate none exists.
   */
  public int getNextPreceding(int axisContextHandle, int nodeHandle)
  {

    // %TBD%
    return 0;
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

    // %TBD%
    return 0;
  }

  /**
   *  Given a node handle, find the owning document node.
   *
   *  @param nodeHandle the id of the node.
   *  @return int Node handle of document, which should always be valid.
   */
  public int getDocument()
  {

    // %TBD%
    return 0;
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

    // %TBD%
    return 0;
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
  public String getStringValue(int nodeHandle)
  {

    // %TBD%
    return null;
  }

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
    return null;
  }

  /**
   * Given a node handle, return an ID that represents the node's expanded name.
   *
   * @param nodeHandle The handle to the node in question.
   *
   * @return the expanded-name id of the node.
   */
  public int getExpandedNameID(int nodeHandle)
  {

    // %TBD%
    return 0;
  }

  /**
   * Given an expanded name, return an ID.  If the expanded-name does not
   * exist in the internal tables, the entry will be created, and the ID will
   * be returned.  Any additional nodes that are created that have this
   * expanded name will use this ID.
   *
   * @param nodeHandle The handle to the node in question.
   *
   * NEEDSDOC @param namespace
   * NEEDSDOC @param localName
   *
   * @return the expanded-name id of the node.
   */
  public int getExpandedNameID(String namespace, String localName)
  {

    // %TBD%
    return 0;
  }

  /**
   * Given an expanded-name ID, return the local name part.
   *
   * @param ExpandedNameID an ID that represents an expanded-name.
   * @return String Local name of this node.
   */
  public String getLocalNameFromExpandedNameID(int ExpandedNameID)
  {

    // %TBD%
    return null;
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

    // %TBD%
    return null;
  }

  /**
   * Given a node handle, return its DOM-style node name. This will
   * include names such as #text or #document.
   *
   * @param nodeHandle the id of the node.
   * @return String Name of this node, which may be an empty string.
   * %REVIEW% Document when empty string is possible...
   */
  public String getNodeName(int nodeHandle)
  {

    // %TBD%
    return null;
  }

  /**
   * Given a node handle, return the XPath node name.  This should be
   * the name as described by the XPath data model, NOT the DOM-style
   * name.
   *
   * @param nodeHandle the id of the node.
   * @return String Name of this node.
   */
  public String getNodeNameX(int nodeHandle)
  {

    // %TBD%
    return null;
  }

  /**
   * Given a node handle, return its DOM-style localname.
   * (As defined in Namespaces, this is the portion of the name after any
   * colon character)
   *
   * @param nodeHandle the id of the node.
   * @return String Local name of this node.
   */
  public String getLocalName(int nodeHandle)
  {

    // %TBD%
    return null;
  }

  /**
   * Given a namespace handle, return the prefix that the namespace decl is
   * mapping.
   * Given a node handle, return the prefix used to map to the namespace.
   *
   * <p> %REVIEW% Are you sure you want "" for no prefix?  </p>
   *
   * @param nodeHandle the id of the node.
   * @return String prefix of this node's name, or "" if no explicit
   * namespace prefix was given.
   */
  public String getPrefix(int nodeHandle)
  {

    // %TBD%
    return null;
  }

  /**
   * Given a node handle, return its DOM-style namespace URI
   * (As defined in Namespaces, this is the declared URI which this node's
   * prefix -- or default in lieu thereof -- was mapped to.)
   *
   * @param nodeHandle the id of the node.
   * @return String URI value of this node's namespace, or null if no
   * namespace was resolved.
   */
  public String getNamespaceURI(int nodeHandle)
  {

    // %TBD%
    return null;
  }

  /**
   * Given a node handle, return its node value. This is mostly
   * as defined by the DOM, but may ignore some conveniences.
   * <p>
   *
   * @param nodeHandle The node id.
   * @return String Value of this node, or null if not
   * meaningful for this node type.
   */
  public String getNodeValue(int nodeHandle)
  {

    // %TBD%
    return null;
  }

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
    short type = (short)getNodeInfo(identity, OFFSET_TYPE);

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

    return (short) getNodeInfo(identity, OFFSET_LEVEL);
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
   * NEEDSDOC @param version
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
   * @param nodeHandle The node id, which can be any valid node handle.
   * @return the document base URI String object or null if unknown.
   */
  public String getDocumentBaseURI(int nodeHandle)
  {

    // %TBD%
    return null;
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

    // %TBD%
    return null;
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

    // %TBD%
    return null;
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

    // %TBD%
    return null;
  }

  /**
   * Return a string representing the XML version of the document. This
   * property is derived from the XML declaration optionally present at the
   * beginning of the document entity, and has no value if there is no XML
   * declaration.
   *
   * @param the document handle
   *
   * NEEDSDOC @param documentHandle
   *
   * @return the document version String object
   */
  public String getDocumentVersion(int documentHandle)
  {

    // %TBD%
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

    // %TBD%
    return false;
  }

  /**
   *   A document type declaration information item has the following properties:
   *
   *     1. [system identifier] The system identifier of the external subset, if
   *        it exists. Otherwise this property has no value.
   *
   * @return the system identifier String object, or null if there is none.
   */
  public String getDocumentTypeDeclarationSystemIdentifier()
  {

    // %TBD%
    return null;
  }

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
  public int getDocumentTypeDeclarationPublicIdentifier()
  {

    // %TBD%
    return 0;
  }

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
  public int getElementById(String elementId)
  {

    // %TBD%
    return 0;
  }

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
  public String getUnparsedEntityURI(String name)
  {

    // %TBD%
    return null;
  }

  // ============== Boolean methods ================

  /**
   * Return true if the xsl:strip-space or xsl:preserve-space was processed
   * during construction of the DTM document.
   *
   * <p>%REVEIW% Presumes a 1:1 mapping from DTM to Document, since
   * we aren't saying which Document to query...?</p>
   *
   * NEEDSDOC ($objectName$) @return
   */
  public boolean supportsPreStripping()
  {

    // %TBD%
    return false;
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
   * @param node1 DOM Node to perform position comparison on.
   * @param node2 DOM Node to perform position comparison on .
   *
   * NEEDSDOC @param nodeHandle1
   * NEEDSDOC @param nodeHandle2
   *
   * @return false if node2 comes before node1, otherwise return true.
   * You can think of this as
   * <code>(node1.documentOrderPosition &lt;= node2.documentOrderPosition)</code>.
   */
  public boolean isNodeAfter(int nodeHandle1, int nodeHandle2)
  {

    // %TBD%
    return false;
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

    // %TBD%
    return false;
  }

  /**
   *     5. [specified] A flag indicating whether this attribute was actually
   *        specified in the start-tag of its element, or was defaulted from the
   *        DTD.
   *
   * @param the attribute handle
   *
   * NEEDSDOC @param attributeHandle
   * @return <code>true</code> if the attribute was specified;
   *         <code>false</code> if it was defaulted.
   */
  public boolean isAttributeSpecified(int attributeHandle)
  {

    // %TBD%
    return false;
  }

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
  public void dispatchCharactersEvents(
          int nodeHandle, org.xml.sax.ContentHandler ch)
            throws org.xml.sax.SAXException{}

  /**
   * Directly create SAX parser events from a subtree.
   *
   * @param nodeHandle The node ID.
   * @param ch A non-null reference to a ContentHandler.
   *
   * @throws org.xml.sax.SAXException
   */
  public void dispatchToEvents(int nodeHandle, org.xml.sax.ContentHandler ch)
          throws org.xml.sax.SAXException{}

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
  public void appendChild(int newChild, boolean clone, boolean cloneDepth){}

  /**
   * Append a text node child that will be constructed from a string,
   * to the end of the document.
   *
   * <p>%REVIEW% "End of the document" needs to be defined more clearly.
   * Does it become the last child of the Document? Of the root element?</p>
   *
   * @param str Non-null reverence to a string.
   */
  public void appendTextChild(String str){}
}
