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
package org.apache.xml.dtm.ref.dom2dtm;

import org.apache.xml.dtm.ref.*;
import org.apache.xml.dtm.*;
import org.apache.xml.utils.SuballocatedIntVector;
import org.apache.xml.utils.IntStack;
import org.apache.xml.utils.BoolStack;
import org.apache.xml.utils.StringBufferPool;
import org.apache.xml.utils.FastStringBuffer;
import org.apache.xml.utils.TreeWalker;
import org.apache.xml.utils.QName;
import org.apache.xml.utils.XMLCharacterRecognizer;

import org.w3c.dom.*;

import java.util.Vector;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.SourceLocator;
import org.xml.sax.ContentHandler;

import org.apache.xml.utils.NodeVector;

import org.apache.xml.utils.XMLString;
import org.apache.xml.utils.XMLStringFactory;

/** The <code>DOM2DTM</code> class serves up a DOM's contents via the
 * DTM API.
 *
 * Note that it doesn't necessarily represent a full Document
 * tree. You can wrap a DOM2DTM around a specific node and its subtree
 * and the right things should happen. (I don't _think_ we currently
 * support DocumentFrgment nodes as roots, though that might be worth
 * considering.)
 *
 * Note too that we do not currently attempt to track document
 * mutation. If you alter the DOM after wrapping DOM2DTM around it,
 * all bets are off.
 * */
public class DOM2DTM extends DTMDefaultBaseIterators
{
  static final boolean JJK_DEBUG=false;
  
  /** The current position in the DOM tree. Last node examined for
   * possible copying to DTM. */
  transient private Node m_pos;
  /** The current position in the DTM tree. Who children get appended to. */
  private int m_last_parent=0;
  /** The current position in the DTM tree. Who children reference as their 
   * previous sib. */
  private int m_last_kid=NULL;

  /** The top of the subtree.
   * %REVIEW%: 'may not be the same as m_context if "//foo" pattern.'
   * */
  transient private Node m_root;

  /** true if ALL the nodes in the m_root subtree have been processed;
   * false if our incremental build has not yet finished scanning the
   * DOM tree.  */
  transient private boolean m_nodesAreProcessed;

  /** The node objects.  The instance part of the handle indexes
   * directly into this vector.  Each DTM node may actually be
   * composed of several DOM nodes (for example, if logically-adjacent
   * Text/CDATASection nodes in the DOM have been coalesced into a
   * single DTM Text node); this table points only to the first in
   * that sequence. */
  protected Vector m_nodes = new Vector();

  /**
   * Construct a DOM2DTM object from a DOM node.
   *
   * @param mgr The DTMManager who owns this DTM.
   * @param domSource the DOM source that this DTM will wrap.
   * @param dtmIdentity The DTM identity ID for this DTM.
   * @param whiteSpaceFilter The white space filter for this DTM, which may 
   *                         be null.
   * @param xstringfactory XMLString factory for creating character content.
   * @param doIndexing true if the caller considers it worth it to use 
   *                   indexing schemes.
   */
  public DOM2DTM(DTMManager mgr, DOMSource domSource, 
                 int dtmIdentity, DTMWSFilter whiteSpaceFilter,
                 XMLStringFactory xstringfactory,
                 boolean doIndexing)
  {
    super(mgr, domSource, dtmIdentity, whiteSpaceFilter, 
          xstringfactory, doIndexing);

    // Initialize DOM navigation
    m_pos=m_root = domSource.getNode();
    // Initialize DTM navigation
    m_last_parent=m_last_kid=NULL;
    m_last_kid=addNode(m_root, 0, m_last_parent,m_last_kid, NULL);

    // Apparently the domSource root may not actually be the
    // Document node. If it's an Element node, we need to immediately
    // add its attributes. Adapted from nextNode().
    // %REVIEW% Move this logic into addNode and recurse? Cleaner!
    //
    // (If it's an EntityReference node, we're probably scrod. For now
    // I'm just hoping nobody is ever quite that foolish... %REVIEW%)
    if(ELEMENT_NODE == m_root.getNodeType())
    {
      NamedNodeMap attrs=m_root.getAttributes();
      int attrsize=(attrs==null) ? 0 : attrs.getLength();
      if(attrsize>0)
      {
        int attrIndex=NULL; // start with no previous sib
        for(int i=0;i<attrsize;++i)
        {
          // No need to force nodetype in this case;
          // addNode() will take care of switching it from
          // Attr to Namespace if necessary.
          attrIndex=addNode(attrs.item(i),1,0,attrIndex,NULL);
          m_firstch.setElementAt(DTM.NULL,attrIndex);
        }
        // Terminate list of attrs, and make sure they aren't
        // considered children of the element
        m_nextsib.setElementAt(DTM.NULL,attrIndex);

        // IMPORTANT: This does NOT change m_last_parent or m_last_kid!
      } // if attrs exist
    } //if(ELEMENT_NODE)

    // Initialize DTM-completed status 
    m_nodesAreProcessed = false;
  }

  /**
   * Construct the node map from the node.
   *
   * @param node The node that is to be added to the DTM.
   * @param level The current level in the tree.
   * @param parentIndex The current parent index.
   * @param previousSibling The previous sibling index.
   * @param forceNodeType If not DTM.NULL, overrides the DOM node type.
   *	Used to force nodes to Text rather than CDATASection when their
   *	coalesced value includes ordinary Text nodes (current DTM behavior).
   *
   * @return The index identity of the node that was added.
   */
  protected int addNode(Node node, int level, int parentIndex,
                        int previousSibling, int forceNodeType)
  {
    int nodeIndex = m_nodes.size();
    m_size++;
    ensureSize(nodeIndex);
    
    int type;
    if(NULL==forceNodeType)
        type = node.getNodeType();
    else
        type=forceNodeType;
        
    // %REVIEW% The Namespace Spec currently says that Namespaces are
    // processed in a non-namespace-aware manner, by matching the
    // QName, even though there is in fact a namespace assigned to
    // these nodes in the DOM. If and when that changes, we will have
    // to consider whether we check the namespace-for-namespaces
    // rather than the node name.
    //
    // %TBD% Note that the DOM does not necessarily explicitly declare
    // all the namespaces it uses. DOM Level 3 will introduce a
    // namespace-normalization operation which reconciles that, and we
    // can request that users invoke it or otherwise ensure that the
    // tree is namespace-well-formed before passing the DOM to Xalan.
    // But if they don't, what should we do about it? We probably
    // don't want to alter the source DOM (and may not be able to do
    // so if it's read-only). The best available answer might be to
    // synthesize additional DTM Namespace Nodes that don't correspond
    // to DOM Attr Nodes.
    //
    // %REVIEW% With forceNodeType... Is this trip really necessary?
    if (Node.ATTRIBUTE_NODE == type)
    {
      String name = node.getNodeName();

      if (name.startsWith("xmlns:") || name.equals("xmlns"))
      {
        type = DTM.NAMESPACE_NODE;
      }
    }
    
    m_nodes.addElement(node);
    
    // Do casts here so that if we change the sizes, the changes are localized.
    // %REVIEW% Remember to change this cast if we change
    // m_level's type, or we may truncate values without warning!
    //m_level[nodeIndex] = (byte)level;
    m_level.addElement((byte)level); // setElementAt(level,nodeIndex)?
    
    m_firstch.setElementAt(NOTPROCESSED,nodeIndex);
    m_nextsib.setElementAt(NOTPROCESSED,nodeIndex);
    m_prevsib.setElementAt(previousSibling,nodeIndex);
    m_parent.setElementAt(parentIndex,nodeIndex);
    
    if(DTM.NULL != parentIndex && 
       type != DTM.ATTRIBUTE_NODE && 
       type != DTM.NAMESPACE_NODE)
    {
      // If the DTM parent had no children, this becomes its first child.
      if(NOTPROCESSED == m_firstch.elementAt(parentIndex))
        m_firstch.setElementAt(nodeIndex,parentIndex);
    }
    
    String nsURI = node.getNamespaceURI();

    // Deal with the difference between Namespace spec and XSLT
    // definitions of local name. (The former says PIs don't have
    // localnames; the latter says they do.)
    String localName =  (type == Node.PROCESSING_INSTRUCTION_NODE) ? 
                         node.getNodeName() :
                         node.getLocalName();
                         
    // Hack to make DOM1 sort of work...
    if(((type == Node.ELEMENT_NODE) || (type == Node.ATTRIBUTE_NODE)) 
        && null == localName)
      localName = node.getNodeName(); // -sb
      
    ExpandedNameTable exnt = m_expandedNameTable;

    // %TBD% Nodes created with the old non-namespace-aware DOM
    // calls createElement() and createAttribute() will never have a
    // localname. That will cause their expandedNameID to be just the
    // nodeType... which will keep them from being matched
    // successfully by name. Since the DOM makes no promise that
    // those will participate in namespace processing, this is
    // officially accepted as Not Our Fault. But it might be nice to
    // issue a diagnostic message!
    if(node.getLocalName()==null &&
       (type==Node.ELEMENT_NODE || type==Node.ATTRIBUTE_NODE))
      {
        // warning("DOM 'level 1' node "+node.getNodeName()+" won't be mapped properly in DOM2DTM.");
      }
    
    int expandedNameID = (null != localName) 
       ? exnt.getExpandedTypeID(nsURI, localName, type) :
         exnt.getExpandedTypeID(type);

    m_exptype.setElementAt(expandedNameID,nodeIndex);
    
    indexNode(expandedNameID, nodeIndex);

    if (DTM.NULL != previousSibling)
      m_nextsib.setElementAt(nodeIndex,previousSibling);

    // This should be done after m_exptype has been set, and probably should
    // always be the last thing we do
    if (type == DTM.NAMESPACE_NODE)
        declareNamespaceInContext(parentIndex,nodeIndex);

    return nodeIndex;
  }
  
  /**
   * Get the number of nodes that have been added.
   */
  protected int getNumberOfNodes()
  {
    return m_nodes.size();
  }
  
 /**
   * This method iterates to the next node that will be added to the table.
   * Each call to this method adds a new node to the table, unless the end
   * is reached, in which case it returns null.
   *
   * @return The true if a next node is found or false if 
   *         there are no more nodes.
   */
  protected boolean nextNode()
  {
    // Non-recursive one-fetch-at-a-time depth-first traversal with 
    // attribute/namespace nodes and white-space stripping.
    // Navigating the DOM is simple, navigating the DTM is simple;
    // keeping track of both at once is a trifle baroque but at least
    // we've avoided most of the special cases.
    if (m_nodesAreProcessed)
      return false;
        
    // %REVIEW% Is this local copy Really Useful from a performance
    // point of view?  Or is this a false microoptimization?
    Node pos=m_pos; 
    Node next=null;
    int nexttype=NULL;

    // Navigate DOM tree
    do
      {
        // Look down to first child.
        if (pos.hasChildNodes()) 
          {
            next = pos.getFirstChild();

            // %REVIEW% There's probably a more elegant way to skip
            // the doctype. (Just let it go and Suppress it?
            if(next!=null && DOCUMENT_TYPE_NODE==next.getNodeType())
              next=next.getNextSibling();

            // Push DTM context -- except for children of Entity References, 
            // which have no DTM equivalent and cause no DTM navigation.
            if(ENTITY_REFERENCE_NODE!=pos.getNodeType())
              {
                m_last_parent=m_last_kid;
                m_last_kid=NULL;
                // Whitespace-handler context stacking
                if(null != m_wsfilter)
                {
                  short wsv =
                    m_wsfilter.getShouldStripSpace(m_last_parent|m_dtmIdent,this);
                  boolean shouldStrip = (DTMWSFilter.INHERIT == wsv) 
                    ? getShouldStripWhitespace() 
                    : (DTMWSFilter.STRIP == wsv);
                  pushShouldStripWhitespace(shouldStrip);
                } // if(m_wsfilter)
              }
          }

        // If that fails, look up and right (but not past root!)
        else 
          {
            if(m_last_kid!=NULL)
              {
                // Last node posted at this level had no more children
                // If it has _no_ children, we need to record that.
                if(m_firstch.elementAt(m_last_kid)==NOTPROCESSED)
                  m_firstch.setElementAt(NULL,m_last_kid);
              }
                        
            while(m_last_parent != NULL)
              {
                // %REVIEW% There's probably a more elegant way to
                // skip the doctype. (Just let it go and Suppress it?
                next = pos.getNextSibling();
                if(next!=null && DOCUMENT_TYPE_NODE==next.getNodeType())
                  next=next.getNextSibling();

                if(next!=null)
                  break; // Found it!
                
                // No next-sibling found. Pop the DOM.
                pos=pos.getParentNode();
                if(pos==null)
                  {
                    // %TBD% Should never arise, but I want to be sure of that...
                    if(JJK_DEBUG)
                      {
                        System.out.println("***** DOM2DTM Pop Control Flow problem");
                        for(;;); // Freeze right here!
                      }
                  }
                
                // The only parents in the DTM are Elements.  However,
                // the DOM could contain EntityReferences.  If we
                // encounter one, pop it _without_ popping DTM.
                if(pos!=null && ENTITY_REFERENCE_NODE == pos.getNodeType())
                  {
                    // Nothing needs doing
                    if(JJK_DEBUG)
                      System.out.println("***** DOM2DTM popping EntRef");
                  }
                else
                  {
                    popShouldStripWhitespace();
                    // Fix and pop DTM
                    if(m_last_kid==NULL)
                      m_firstch.setElementAt(NULL,m_last_parent); // Popping from an element
                    else
                      m_nextsib.setElementAt(NULL,m_last_kid); // Popping from anything else
                    m_last_parent=m_parent.elementAt(m_last_kid=m_last_parent);
                  }
              }
            if(m_last_parent==NULL)
              next=null;
          }
                
        if(next!=null)
          nexttype=next.getNodeType();
                
        // If it's an entity ref, advance past it.
        //
        // %REVIEW% Should we let this out the door and just suppress it?
        // More work, but simpler code, more likely to be correct, and
        // it doesn't happen very often. We'd get rid of the loop too.
        if (ENTITY_REFERENCE_NODE == nexttype)
          pos=next;
      }
    while (ENTITY_REFERENCE_NODE == nexttype); 
        
    // Did we run out of the tree?
    if(next==null)
      {
        m_nextsib.setElementAt(NULL,0);
        m_nodesAreProcessed = true;
        m_pos=null;
                
        if(JJK_DEBUG)
          {
            System.out.println("***** DOM2DTM Crosscheck:");
            for(int i=0;i<m_nodes.size();++i)
              System.out.println(i+":\t"+m_firstch.elementAt(i)+"\t"+m_nextsib.elementAt(i));
          }
                
        return false;
      }

    // Text needs some special handling:
    //
    // DTM may skip whitespace. This is handled by the suppressNode flag, which
    // when true will keep the DTM node from being created.
    //
    // DTM only directly records the first DOM node of any logically-contiguous
    // sequence. The lastTextNode value will be set to the last node in the 
    // contiguous sequence, and -- AFTER the DTM addNode -- can be used to 
    // advance next over this whole block. Should be simpler than special-casing
    // the above loop for "Was the logically-preceeding sibling a text node".
    // 
    // Finally, a DTM node should be considered a CDATASection only if all the
    // contiguous text it covers is CDATASections. The first Text should
    // force DTM to Text.
        
    boolean suppressNode=false;
    Node lastTextNode=null;

    nexttype=next.getNodeType();
        
    // nexttype=pos.getNodeType();
    if(TEXT_NODE == nexttype || CDATA_SECTION_NODE == nexttype)
      {
        // If filtering, initially assume we're going to suppress the node
        suppressNode=((null != m_wsfilter) && getShouldStripWhitespace());

        // Scan logically contiguous text (siblings, plus "flattening"
        // of entity reference boundaries).
        Node n=next;
        while(n!=null)
          {
            lastTextNode=n;
            // Any Text node means DTM considers it all Text
            if(TEXT_NODE == n.getNodeType())
              nexttype=TEXT_NODE;
            // Any non-whitespace in this sequence blocks whitespace
            // suppression
            suppressNode &=
              XMLCharacterRecognizer.isWhiteSpace(n.getNodeValue());
                        
            n=logicalNextDOMTextNode(n);
          }
      }
        
    // Special handling for PIs: Some DOMs represent the XML
    // Declaration as a PI. This is officially incorrect, per the DOM
    // spec, but is considered a "wrong but tolerable" temporary
    // workaround pending proper handling of these fields in DOM Level
    // 3. We want to recognize and reject that case.
    else if(PROCESSING_INSTRUCTION_NODE==nexttype)
      {
        suppressNode = (pos.getNodeName().toLowerCase().equals("xml"));
      }
        
        
    if(!suppressNode)
      {
        // Inserting next. NOTE that we force the node type; for
        // coalesced Text, this records CDATASections adjacent to
        // ordinary Text as Text.
        int level=m_level.elementAt(m_last_parent)+1;
        int nextindex=addNode(next,level,m_last_parent,m_last_kid,
                              nexttype);
        m_last_kid=nextindex;

        if(ELEMENT_NODE == nexttype)
          {
            // Process attributes _now_, rather than waiting.
            // Simpler control flow, makes NS cache available immediately.
            NamedNodeMap attrs=next.getAttributes();
            int attrsize=(attrs==null) ? 0 : attrs.getLength();
            if(attrsize>0)
              {
                int attrlevel=level+1;
                int attrIndex=NULL; // start with no previous sib
                for(int i=0;i<attrsize;++i)
                  {
                    // No need to force nodetype in this case;
                    // addNode() will take care of switching it from
                    // Attr to Namespace if necessary.
                    attrIndex=addNode(attrs.item(i),attrlevel,
                                      nextindex,attrIndex,NULL);
                    m_firstch.setElementAt(DTM.NULL,attrIndex);
                  }
                // Terminate list of attrs, and make sure they aren't
                // considered children of the element
                m_nextsib.setElementAt(DTM.NULL,attrIndex);
              } // if attrs exist
          } //if(ELEMENT_NODE)
      } // (if !suppressNode)

    // Text postprocessing: Act on values stored above
    if(TEXT_NODE == nexttype || CDATA_SECTION_NODE == nexttype)
      {
        // %TBD% If nexttype was forced to TEXT, patch the DTM node
                
        next=lastTextNode;      // Advance the DOM cursor over contiguous text
      }
        
    // Remember where we left off.
    m_pos=next;
    return true;
  }  


  /**
   * Return an DOM node for the given node.
   *
   * @param nodeHandle The node ID.
   *
   * @return A node representation of the DTM node.
   */
  public Node getNode(int nodeHandle)
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
      if (!nextNode())
        identity = DTM.NULL;
    }

    return identity;
  }

  /**
   * Get the handle from a Node.
   * <p>%OPT% This will be pretty slow.</p>
   *
   * <p>%OPT% An XPath-like search (walk up DOM to root, tracking path;
   * walk down DTM reconstructing path) might be considerably faster
   * on later nodes in large documents. That might also imply improving
   * this call to handle nodes which would be in this DTM but
   * have not yet been built, which might or might not be a Good Thing.</p>
   * 
   * %REVIEW% This relies on being able to test node-identity via
   * object-identity. DTM2DOM proxying is a great example of a case where
   * that doesn't work. DOM Level 3 will provide the isSameNode() method
   * to fix that, but until then this is going to be flaky.
   *
   * @param node A node, which may be null.
   *
   * @return The node handle or <code>DTM.NULL</code>.
   */
  private int getHandleFromNode(Node node)
  {
    if (null != node)
    {
      int len = m_nodes.size();        
      boolean isMore;
      int i = 0;
      do
      {          
        for (; i < len; i++)
        {
          if (m_nodes.elementAt(i) == node)
            return i | m_dtmIdent;         
        }

        isMore = nextNode();
  
        len = m_nodes.size();
            
      } 
      while(isMore || i < len);
    }
    
    return DTM.NULL;
  }

  /** Get the handle from a Node. This is a more robust version of
   * getHandleFromNode, intended to be usable by the public.
   *
   * <p>%OPT% This will be pretty slow.</p>
   * 
   * %REVIEW% This relies on being able to test node-identity via
   * object-identity. DTM2DOM proxying is a great example of a case where
   * that doesn't work. DOM Level 3 will provide the isSameNode() method
   * to fix that, but until then this is going to be flaky.
   *
   * @param node A node, which may be null.
   *
   * @return The node handle or <code>DTM.NULL</code>.  */
  public int getHandleOfNode(Node node)
  {
    if (null != node)
    {
      // Is Node actually within the same document? If not, don't search!
      // This would be easier if m_root was always the Document node, but
      // we decided to allow wrapping a DTM around a subtree.
      if((m_root==node) ||
         (m_root.getNodeType()==DOCUMENT_NODE &&
          m_root==node.getOwnerDocument()) ||
         (m_root.getNodeType()!=DOCUMENT_NODE &&
          m_root.getOwnerDocument()==node.getOwnerDocument())
         )
        {
          // If node _is_ in m_root's tree, find its handle
          //
          // %OPT% This check may be improved significantly when DOM
          // Level 3 nodeKey and relative-order tests become
          // available!
          for(Node cursor=node;
              cursor!=null;
              cursor=
                (cursor.getNodeType()!=ATTRIBUTE_NODE)
                ? cursor.getParentNode()
                : ((org.w3c.dom.Attr)cursor).getOwnerElement())
            {
              if(cursor==m_root)
                // We know this node; find its handle.
                return getHandleFromNode(node); 
            } // for ancestors of node
        } // if node and m_root in same Document
    } // if node!=null

    return DTM.NULL;
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
        // Assume this can not be null.
        type = getNodeType(identity);

        if (type == DTM.ATTRIBUTE_NODE)
        {
          Node node = lookupNode(identity);
          String nodeuri = node.getNamespaceURI();

          if (null == nodeuri)
            nodeuri = "";

          String nodelocalname = node.getLocalName();

          if (nodeuri.equals(namespaceURI) && name.equals(nodelocalname))
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
   * Get the string-value of a node as a String object
   * (see http://www.w3.org/TR/xpath#data-model
   * for the definition of a node's string-value).
   *
   * @param nodeHandle The node ID.
   *
   * @return A string object that represents the string-value of the given node.
   */
  public XMLString getStringValue(int nodeHandle)
  {

    int type = getNodeType(nodeHandle);
    Node node = getNode(nodeHandle);
    // %TBD% If an element only has one text node, we should just use it 
    // directly.
    if(DTM.ELEMENT_NODE == type || DTM.DOCUMENT_NODE == type 
    || DTM.DOCUMENT_FRAGMENT_NODE == type)
    {
      FastStringBuffer buf = StringBufferPool.get();
      String s;
  
      try
      {
        getNodeData(node, buf);
  
        s = (buf.length() > 0) ? buf.toString() : "";
      }
      finally
      {
        StringBufferPool.free(buf);
      }
  
      return m_xstrf.newstr( s );
    }
    else if(TEXT_NODE == type || CDATA_SECTION_NODE == type)
    {
      // If this is a DTM text node, it may be made of multiple DOM text
      // nodes -- including navigating into Entity References. DOM2DTM
      // records the first node in the sequence and requires that we
      // pick up the others when we retrieve the DTM node's value.
      //
      // %REVIEW% DOM Level 3 is expected to add a "whole text"
      // retrieval method which performs this function for us.
      FastStringBuffer buf = StringBufferPool.get();
      while(node!=null)
      {
        buf.append(node.getNodeValue());
        node=logicalNextDOMTextNode(node);
      }
      String s=(buf.length() > 0) ? buf.toString() : "";
      StringBufferPool.free(buf);
      return m_xstrf.newstr( s );
    }
    else
      return m_xstrf.newstr( node.getNodeValue() );
  }
  
  /**
   * Retrieve the text content of a DOM subtree, appending it into a
   * user-supplied FastStringBuffer object. Note that attributes are
   * not considered part of the content of an element.
   * <p>
   * There are open questions regarding whitespace stripping. 
   * Currently we make no special effort in that regard, since the standard
   * DOM doesn't yet provide DTD-based information to distinguish
   * whitespace-in-element-context from genuine #PCDATA. Note that we
   * should probably also consider xml:space if/when we address this.
   * DOM Level 3 may solve the problem for us.
   * <p>
   * %REVIEW% Actually, since this method operates on the DOM side of the
   * fence rather than the DTM side, it SHOULDN'T do
   * any special handling. The DOM does what the DOM does; if you want
   * DTM-level abstractions, use DTM-level methods.
   *
   * @param node Node whose subtree is to be walked, gathering the
   * contents of all Text or CDATASection nodes.
   * @param buf FastStringBuffer into which the contents of the text
   * nodes are to be concatenated.
   */
  protected static void getNodeData(Node node, FastStringBuffer buf)
  {

    switch (node.getNodeType())
    {
    case Node.DOCUMENT_FRAGMENT_NODE :
    case Node.DOCUMENT_NODE :
    case Node.ELEMENT_NODE :
    {
      for (Node child = node.getFirstChild(); null != child;
              child = child.getNextSibling())
      {
        getNodeData(child, buf);
      }
    }
    break;
    case Node.TEXT_NODE :
    case Node.CDATA_SECTION_NODE :
    case Node.ATTRIBUTE_NODE :	// Never a child but might be our starting node
      buf.append(node.getNodeValue());
      break;
    case Node.PROCESSING_INSTRUCTION_NODE :
      // warning(XPATHErrorResources.WG_PARSING_AND_PREPARING);        
      break;
    default :
      // ignore
      break;
    }
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
  public String getNodeName(int nodeHandle)
  {

    Node node = getNode(nodeHandle);

    // Assume non-null.
    return node.getNodeName();
  }

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

    String name;
    short type = getNodeType(nodeHandle);

    switch (type)
    {
    case DTM.NAMESPACE_NODE :
    {
      Node node = getNode(nodeHandle);

      // assume not null.
      name = node.getNodeName();
      if(name.startsWith("xmlns:"))
      {
        name = QName.getLocalPart(name);
      }
      else if(name.equals("xmlns"))
      {
        name = "";
      }
    }
    break;
    case DTM.ATTRIBUTE_NODE :
    case DTM.ELEMENT_NODE :
    case DTM.ENTITY_REFERENCE_NODE :
    case DTM.PROCESSING_INSTRUCTION_NODE :
    {
      Node node = getNode(nodeHandle);

      // assume not null.
      name = node.getNodeName();
    }
    break;
    default :
      name = "";
    }

    return name;
  }

  /**
   * Given a node handle, return its XPath-style localname.
   * (As defined in Namespaces, this is the portion of the name after any
   * colon character).
   *
   * @param nodeHandle the id of the node.
   * @return String Local name of this node.
   */
  public String getLocalName(int nodeHandle)
  {

    String name;
    short type = getNodeType(nodeHandle);

    switch (type)
    {
    case DTM.ATTRIBUTE_NODE :
    case DTM.ELEMENT_NODE :
    case DTM.ENTITY_REFERENCE_NODE :
    case DTM.NAMESPACE_NODE :
    case DTM.PROCESSING_INSTRUCTION_NODE :
    {
      Node node = getNode(nodeHandle);

      // assume not null.
      name = node.getLocalName();

      if (null == name)
      {
        String qname = node.getNodeName();
        int index = qname.indexOf(':');

        name = (index < 0) ? qname : qname.substring(index + 1);
      }
    }
    break;
    default :
      name = "";
    }

    return name;
  }

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
  public String getPrefix(int nodeHandle)
  {

    String prefix;
    short type = getNodeType(nodeHandle);

    switch (type)
    {
    case DTM.NAMESPACE_NODE :
    {
      Node node = getNode(nodeHandle);

      // assume not null.
      String qname = node.getNodeName();
      int index = qname.indexOf(':');

      prefix = (index < 0) ? "" : qname.substring(index + 1);
    }
    break;
    case DTM.ATTRIBUTE_NODE :
    case DTM.ELEMENT_NODE :
    {
      Node node = getNode(nodeHandle);

      // assume not null.
      String qname = node.getNodeName();
      int index = qname.indexOf(':');

      prefix = (index < 0) ? "" : qname.substring(0, index);
    }
    break;
    default :
      prefix = "";
    }

    return prefix;
  }

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
  public String getNamespaceURI(int nodeHandle)
  {

    String nsuri;
    short type = getNodeType(nodeHandle);

    switch (type)
    {
    case DTM.ATTRIBUTE_NODE :
    case DTM.ELEMENT_NODE :
    case DTM.ENTITY_REFERENCE_NODE :
    case DTM.NAMESPACE_NODE :
    case DTM.PROCESSING_INSTRUCTION_NODE :
    {
      Node node = getNode(nodeHandle);

      // assume not null.
      nsuri = node.getNamespaceURI();

      // %TBD% Handle DOM1?
    }
    break;
    default :
      nsuri = null;
    }

    return nsuri;
  }
  
  /** Utility function: Given a DOM Text node, determine whether it is
   * logically followed by another Text or CDATASection node. This may
   * involve traversing into Entity References.
   * 
   * %REVIEW% DOM Level 3 is expected to add functionality which may 
   * allow us to retire this.
   */
  private Node logicalNextDOMTextNode(Node n)
  {
        Node p=n.getNextSibling();
        if(p==null)
        {
                // Walk out of any EntityReferenceNodes that ended with text
                for(n=n.getParentNode();
                        n!=null && ENTITY_REFERENCE_NODE == n.getNodeType();
                        n=n.getParentNode())
                {
                        p=n.getNextSibling();
                        if(p!=null)
                                break;
                }
        }
        n=p;
        while(n!=null && ENTITY_REFERENCE_NODE == n.getNodeType())
        {
                // Walk into any EntityReferenceNodes that start with text
                if(n.hasChildNodes())
                        n=n.getFirstChild();
                else
                        n=n.getNextSibling();
        }
        if(n!=null)
        {
                // Found a logical next sibling. Is it text?
                int ntype=n.getNodeType();
                if(TEXT_NODE != ntype && CDATA_SECTION_NODE != ntype)
                        n=null;
        }
        return n;
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
    int type=_type(nodeHandle);
    if(TEXT_NODE!=type && CDATA_SECTION_NODE!=type)
      return getNode(nodeHandle).getNodeValue();
    
    // If this is a DTM text node, it may be made of multiple DOM text
    // nodes -- including navigating into Entity References. DOM2DTM
    // records the first node in the sequence and requires that we
    // pick up the others when we retrieve the DTM node's value.
    //
    // %REVIEW% DOM Level 3 is expected to add a "whole text"
    // retrieval method which performs this function for us.
    Node node = getNode(nodeHandle);
    Node n=logicalNextDOMTextNode(node);
    if(n==null)
      return node.getNodeValue();
    
    FastStringBuffer buf = StringBufferPool.get();
        buf.append(node.getNodeValue());
    while(n!=null)
    {
      buf.append(n.getNodeValue());
      n=logicalNextDOMTextNode(n);
    }
    String s = (buf.length() > 0) ? buf.toString() : "";
    StringBufferPool.free(buf);
    return s;
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

    Document doc;

    if (m_root.getNodeType() == Node.DOCUMENT_NODE)
      doc = (Document) m_root;
    else
      doc = m_root.getOwnerDocument();

    if (null != doc)
    {
      DocumentType dtd = doc.getDoctype();

      if (null != dtd)
      {
        return dtd.getSystemId();
      }
    }

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
  public String getDocumentTypeDeclarationPublicIdentifier()
  {

    Document doc;

    if (m_root.getNodeType() == Node.DOCUMENT_NODE)
      doc = (Document) m_root;
    else
      doc = m_root.getOwnerDocument();

    if (null != doc)
    {
      DocumentType dtd = doc.getDoctype();

      if (null != dtd)
      {
        return dtd.getPublicId();
      }
    }

    return null;
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

    Document doc = (m_root.getNodeType() == Node.DOCUMENT_NODE) 
        ? (Document) m_root : m_root.getOwnerDocument();
        
    if(null != doc)
    {
      Node elem = doc.getElementById(elementId);
      if(null != elem)
      {
        int elemHandle = getHandleFromNode(elem);
        
        if(DTM.NULL == elemHandle)
        {
          int identity = m_nodes.size()-1;
          while (DTM.NULL != (identity = getNextNodeIdentity(identity)))
          {
            Node node = getNode(identity);
            if(node == elem)
            {
              elemHandle = getHandleFromNode(elem);
              break;
            }
           }
        }
        
        return elemHandle;
      }
    
    }
    return DTM.NULL;
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

    String url = "";
    Document doc = (m_root.getNodeType() == Node.DOCUMENT_NODE) 
        ? (Document) m_root : m_root.getOwnerDocument();

    if (null != doc)
    {
      DocumentType doctype = doc.getDoctype();
  
      if (null != doctype)
      {
        NamedNodeMap entities = doctype.getEntities();
        if(null == entities)
          return url;
        Entity entity = (Entity) entities.getNamedItem(name);
        if(null == entity)
          return url;
        
        String notationName = entity.getNotationName();
  
        if (null != notationName)  // then it's unparsed
        {
          // The draft says: "The XSLT processor may use the public 
          // identifier to generate a URI for the entity instead of the URI 
          // specified in the system identifier. If the XSLT processor does 
          // not use the public identifier to generate the URI, it must use 
          // the system identifier; if the system identifier is a relative 
          // URI, it must be resolved into an absolute URI using the URI of 
          // the resource containing the entity declaration as the base 
          // URI [RFC2396]."
          // So I'm falling a bit short here.
          url = entity.getSystemId();
  
          if (null == url)
          {
            url = entity.getPublicId();
          }
          else
          {
            // This should be resolved to an absolute URL, but that's hard 
            // to do from here.
          }        
        }
      }
    }

    return url;
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
    int type = getNodeType(attributeHandle);

    if (DTM.ATTRIBUTE_NODE == type)
    {
      Attr attr = (Attr)getNode(attributeHandle);
      return attr.getSpecified();
    }
    return false;
  }

  /** Bind a CoroutineParser to this DTM. NOT RELEVANT for DOM2DTM, since
   * we're wrapped around an existing DOM.
   *
   * @param coroutineParser The parser that we want to recieve events from
   * on demand.
   */
  public void setCoroutineParser(CoroutineParser coroutineParser)
  {
  }
  
  /** getContentHandler returns "our SAX builder" -- the thing that
   * someone else should send SAX events to in order to extend this
   * DTM model.
   *
   * @return null if this model doesn't respond to SAX events,
   * "this" if the DTM object has a built-in SAX ContentHandler,
   * the CoroutineParser if we're bound to one and should receive
   * the SAX stream via it for incremental build purposes...
   * */
  public org.xml.sax.ContentHandler getContentHandler()
  {
      return null;
  }
  
  /**
   * Return this DTM's lexical handler.
   *
   * %REVIEW% Should this return null if constrution already done/begun?
   *
   * @return null if this model doesn't respond to lexical SAX events,
   * "this" if the DTM object has a built-in SAX ContentHandler,
   * the CoroutineParser if we're bound to one and should receive
   * the SAX stream via it for incremental build purposes...
   */
  public org.xml.sax.ext.LexicalHandler getLexicalHandler()
  {

    return null;
  }

  
  /**
   * Return this DTM's EntityResolver.
   *
   * @return null if this model doesn't respond to SAX entity ref events.
   */
  public org.xml.sax.EntityResolver getEntityResolver()
  {

    return null;
  }
  
  /**
   * Return this DTM's DTDHandler.
   *
   * @return null if this model doesn't respond to SAX dtd events.
   */
  public org.xml.sax.DTDHandler getDTDHandler()
  {

    return null;
  }

  /**
   * Return this DTM's ErrorHandler.
   *
   * @return null if this model doesn't respond to SAX error events.
   */
  public org.xml.sax.ErrorHandler getErrorHandler()
  {

    return null;
  }
  
  /**
   * Return this DTM's DeclHandler.
   *
   * @return null if this model doesn't respond to SAX Decl events.
   */
  public org.xml.sax.ext.DeclHandler getDeclHandler()
  {

    return null;
  }  

  /** @return true iff we're building this model incrementally (eg
   * we're partnered with a CoroutineParser) and thus require that the
   * transformation and the parse run simultaneously. Guidance to the
   * DTMManager.
   * */
  public boolean needsTwoThreads()
  {
    return false;
  }

  // ========== Direct SAX Dispatch, for optimization purposes ========
  
  /**
   * Returns whether the specified <var>ch</var> conforms to the XML 1.0 definition
   * of whitespace.  Refer to <A href="http://www.w3.org/TR/1998/REC-xml-19980210#NT-S">
   * the definition of <CODE>S</CODE></A> for details.
   * @param   ch      Character to check as XML whitespace.
   * @return          =true if <var>ch</var> is XML whitespace; otherwise =false.
   */
  private static boolean isSpace(char ch)
  {
    return XMLCharacterRecognizer.isWhiteSpace(ch);  // Take the easy way out for now.
  }

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
          int nodeHandle, org.xml.sax.ContentHandler ch, 
          boolean normalize)
            throws org.xml.sax.SAXException
  {
    if(normalize)
    {
      XMLString str = getStringValue(nodeHandle);
      str = str.fixWhiteSpace(true, true, false);
      str.dispatchCharactersEvents(ch);
    }
    else
    {
      int type = getNodeType(nodeHandle);
      Node node = getNode(nodeHandle);
      dispatchNodeData(node, ch, 0);
          // Text coalition -- a DTM text node may represent multiple
          // DOM nodes.
          if(TEXT_NODE == type || CDATA_SECTION_NODE == type)
          {
                  while( null != (node=logicalNextDOMTextNode(node)) )
                  {
                      dispatchNodeData(node, ch, 0);
                  }
          }
    }
  }
  
  /**
   * Retrieve the text content of a DOM subtree, appending it into a
   * user-supplied FastStringBuffer object. Note that attributes are
   * not considered part of the content of an element.
   * <p>
   * There are open questions regarding whitespace stripping. 
   * Currently we make no special effort in that regard, since the standard
   * DOM doesn't yet provide DTD-based information to distinguish
   * whitespace-in-element-context from genuine #PCDATA. Note that we
   * should probably also consider xml:space if/when we address this.
   * DOM Level 3 may solve the problem for us.
   * <p>
   * %REVIEW% Note that as a DOM-level operation, it can be argued that this
   * routine _shouldn't_ perform any processing beyond what the DOM already
   * does, and that whitespace stripping and so on belong at the DTM level.
   * If you want a stripped DOM view, wrap DTM2DOM around DOM2DTM.
   *
   * @param node Node whose subtree is to be walked, gathering the
   * contents of all Text or CDATASection nodes.
   * @param buf FastStringBuffer into which the contents of the text
   * nodes are to be concatenated.
   */
  protected static void dispatchNodeData(Node node, 
                                         org.xml.sax.ContentHandler ch, 
                                         int depth)
            throws org.xml.sax.SAXException
  {

    switch (node.getNodeType())
    {
    case Node.DOCUMENT_FRAGMENT_NODE :
    case Node.DOCUMENT_NODE :
    case Node.ELEMENT_NODE :
    {
      for (Node child = node.getFirstChild(); null != child;
              child = child.getNextSibling())
      {
        dispatchNodeData(child, ch, depth+1);
      }
    }
    break;
    case Node.PROCESSING_INSTRUCTION_NODE : // %REVIEW%
    case Node.COMMENT_NODE :
      if(0 != depth)
        break;
        // NOTE: Because this operation works in the DOM space, it does _not_ attempt
        // to perform Text Coalition. That should only be done in DTM space. 
    case Node.TEXT_NODE :
    case Node.CDATA_SECTION_NODE :
    case Node.ATTRIBUTE_NODE :
      String str = node.getNodeValue();
      if(ch instanceof CharacterNodeHandler)
      {
        ((CharacterNodeHandler)ch).characters(node);
      }
      else
      {
        ch.characters(str.toCharArray(), 0, str.length());
      }
      break;
//    /* case Node.PROCESSING_INSTRUCTION_NODE :
//      // warning(XPATHErrorResources.WG_PARSING_AND_PREPARING);        
//      break; */
    default :
      // ignore
      break;
    }
  }
  
  TreeWalker m_walker = new TreeWalker(null);
  
  /**
   * Directly create SAX parser events from a subtree.
   *
   * @param nodeHandle The node ID.
   * @param ch A non-null reference to a ContentHandler.
   *
   * @throws org.xml.sax.SAXException
   */
  public void dispatchToEvents(int nodeHandle, org.xml.sax.ContentHandler ch)
          throws org.xml.sax.SAXException
  {
    TreeWalker treeWalker = m_walker;
    ContentHandler prevCH = treeWalker.getContentHandler();
    
    if(null != prevCH)
    {
      treeWalker = new TreeWalker(null);
    }
    treeWalker.setContentHandler(ch);
    
    try
    {
      Node node = getNode(nodeHandle);
      treeWalker.traverse(node);
    }
    finally
    {
      treeWalker.setContentHandler(null);
    }
  }
  
  public interface CharacterNodeHandler
  {
    public void characters(Node node)
            throws org.xml.sax.SAXException;
  }

  /**
   * For the moment all the run time properties are ignored by this
   * class.
   *
   * @param property a <code>String</code> value
   * @param value an <code>Object</code> value
   */
  public void setProperty(String property, Object value)
  {
  }
  
  /**
   * No source information is available for DOM2DTM, so return
   * <code>null</code> here.
   *
   * @param node an <code>int</code> value
   * @return null
   */
  public SourceLocator getSourceLocatorFor(int node)
  {
    return null;
  }
}
