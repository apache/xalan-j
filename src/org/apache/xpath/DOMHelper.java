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
package org.apache.xpath;

import java.util.Hashtable;
import java.util.Vector;

import org.w3c.dom.*;

import javax.xml.transform.TransformerException;

import org.apache.xml.utils.NSInfo;
import org.apache.xml.utils.QName;
import org.apache.xml.utils.StringBufferPool;
import org.apache.xml.utils.FastStringBuffer;
import org.apache.xalan.res.XSLMessages;
import org.apache.xpath.res.XPATHErrorResources;

// Imported JAVA API for XML Parsing 1.0 classes
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * <meta name="usage" content="internal"/>
 * This class provides a front-end to DOM implementations, providing
 * a number of utility functions that either aren't yet standardized
 * by the DOM spec or that are defined in optional DOM modules and
 * hence may not be present in all DOMs.
 */
public class DOMHelper
{

  /**
   * DOM Level 1 did not have a standard mechanism for creating a new
   * Document object. This function provides a DOM-implementation-independent
   * abstraction for that for that concept. It's typically used when 
   * outputting a new DOM as the result of an operation.
   * <p>
   * TODO: This isn't directly compatable with DOM Level 2. 
   * The Level 2 createDocument call also creates the root 
   * element, and thus requires that you know what that element will be
   * before creating the Document. We should think about whether we want
   * to change this code, and the callers, so we can use the DOM's own 
   * method. (It's also possible that DOM Level 3 may relax this
   * sequence, but you may give up some intelligence in the DOM by
   * doing so; the intent was that knowing the document type and root
   * element might let the DOM automatically switch to a specialized
   * subclass for particular kinds of documents.)
   *
   * @return The newly created DOM Document object, with no children, or
   * null if we can't find a DOM implementation that permits creating
   * new empty Documents.
   */
  public Document createDocument()
  {

    try
    {

      // Use an implementation of the JAVA API for XML Parsing 1.0 to
      // create a DOM Document node to contain the result.
      DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();

      dfactory.setNamespaceAware(true);
      dfactory.setValidating(true);

      DocumentBuilder docBuilder = dfactory.newDocumentBuilder();
      Document outNode = docBuilder.newDocument();

      return outNode;
    }
    catch (ParserConfigurationException pce)
    {
      throw new RuntimeException(
        XSLMessages.createXPATHMessage(
          XPATHErrorResources.ER_CREATEDOCUMENT_NOT_SUPPORTED, null));  //"createDocument() not supported in XPathContext!");

      // return null;
    }
  }

  /**
   * <meta name="usage" content="advanced"/>
   * Tells, through the combination of the default-space attribute
   * on xsl:stylesheet, xsl:strip-space, xsl:preserve-space, and the
   * xml:space attribute, whether or not extra whitespace should be stripped
   * from the node.  Literal elements from template elements should
   * <em>not</em> be tested with this function.
   * @param textNode A text node from the source tree.
   * @return true if the text node should be stripped of extra whitespace.
   *
   * @throws javax.xml.transform.TransformerException
   */
  public boolean shouldStripSourceNode(Node textNode)
          throws javax.xml.transform.TransformerException
  {

    // return (null == m_envSupport) ? false : m_envSupport.shouldStripSourceNode(textNode);
    return false;
  }

  /**
   * Supports the XPath function GenerateID by returning a unique
   * identifier string for any given DOM Node.
   * <p>
   * Warning: The base implementation uses the Node object's hashCode(),
   * which is NOT guaranteed to be unique. If that method hasn't been
   * overridden in this DOM ipmlementation, most Java implementions will
   * derive it from the object's address and should be OK... but if
   * your DOM uses a different definition of hashCode (eg hashing the
   * contents of the subtree), or if your DOM may have multiple objects
   * that represent a single Node in the data structure (eg via proxying),
   * you may need to find another way to assign a unique identifier.
   * <p>
   * Also, be aware that if nodes are destroyed and recreated, there is
   * an open issue regarding whether an ID may be reused. Currently
   * we're assuming that the input document is stable for the duration
   * of the XPath/XSLT operation, so this shouldn't arise in this context.
   * <p>
   * (DOM Level 3 is investigating providing a unique node "key", but
   * that won't help Level 1 and Level 2 implementations.)
   *
   * @param node whose identifier you want to obtain
   *
   * @return a string which should be different for every Node object.
   */
  public String getUniqueID(Node node)
  {
    return "N" + Integer.toHexString(node.hashCode());
  }

  /**
   * Figure out whether node2 should be considered as being later
   * in the document than node1, in Document Order as defined
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
   * @return false if node2 comes before node1, otherwise return true.
   * You can think of this as 
   * <code>(node1.documentOrderPosition &lt;= node2.documentOrderPosition)</code>.
   */
  public boolean isNodeAfter(Node node1, Node node2)
  {
    if (node1 == node2)
      return true;

	// Default return value, if there is no defined ordering
    boolean isNodeAfter = true;
	
    Node parent1 = getParentOfNode(node1);
    Node parent2 = getParentOfNode(node2);

    // Optimize for most common case
    if (parent1 == parent2)  // then we know they are siblings
    {
      if (null != parent1)
        isNodeAfter = isNodeAfterSibling(parent1, node1, node2);
      else
      {
		  // If both parents are null, ordering is not defined.
		  // We're returning a value in lieu of throwing an exception.
		  // Not a case we expect to arise in XPath, but beware if you
		  // try to reuse this method.
		  
		  // We can just fall through in this case, which allows us
		  // to hit the debugging code at the end of the function.
          //return isNodeAfter;
      }
    }
    else
    {

      // General strategy: Figure out the lengths of the two 
      // ancestor chains, reconcile the lengths, and look for
	  // the lowest common ancestor. If that ancestor is one of
	  // the nodes being compared, it comes before the other.
      // Otherwise perform a sibling compare. 
		//
		// NOTE: If no common ancestor is found, ordering is undefined
		// and we return the default value of isNodeAfter.
		
      // Count parents in each ancestor chain
      int nParents1 = 2, nParents2 = 2;  // include node & parent obtained above

      while (parent1 != null)
      {
        nParents1++;

        parent1 = getParentOfNode(parent1);
      }

      while (parent2 != null)
      {
        nParents2++;

        parent2 = getParentOfNode(parent2);
      }

	  // Initially assume scan for common ancestor starts with
	  // the input nodes.
      Node startNode1 = node1, startNode2 = node2;

      // If one ancestor chain is longer, adjust its start point
	  // so we're comparing at the same depths
      if (nParents1 < nParents2)
      {
        // Adjust startNode2 to depth of startNode1
        int adjust = nParents2 - nParents1;

        for (int i = 0; i < adjust; i++)
        {
          startNode2 = getParentOfNode(startNode2);
        }
      }
      else if (nParents1 > nParents2)
      {
        // adjust startNode1 to depth of startNode2
        int adjust = nParents1 - nParents2;

        for (int i = 0; i < adjust; i++)
        {
          startNode1 = getParentOfNode(startNode1);
        }
      }

      Node prevChild1 = null, prevChild2 = null;  // so we can "back up"

      // Loop up the ancestor chain looking for common parent
      while (null != startNode1)
      {
        if (startNode1 == startNode2)  // common parent?
        {
          if (null == prevChild1)  // first time in loop?
          {

            // Edge condition: one is the ancestor of the other.
            isNodeAfter = (nParents1 < nParents2) ? true : false;

            break;  // from while loop
          }
          else 
          {
			// Compare ancestors below lowest-common as siblings
            isNodeAfter = isNodeAfterSibling(startNode1, prevChild1,
                                             prevChild2);

            break;  // from while loop
          }
        }  // end if(startNode1 == startNode2)

		// Move up one level and try again
        prevChild1 = startNode1;
        startNode1 = getParentOfNode(startNode1);
        prevChild2 = startNode2;
        startNode2 = getParentOfNode(startNode2);
      }  // end while(parents exist to examine)
    }  // end big else (not immediate siblings)
	
	// WARNING: The following diagnostic won't report the early
	// "same node" case. Fix if/when needed.
	
    /* -- please do not remove... very useful for diagnostics --
    System.out.println("node1 = "+node1.getNodeName()+"("+node1.getNodeType()+")"+
    ", node2 = "+node2.getNodeName()
    +"("+node2.getNodeType()+")"+
    ", isNodeAfter = "+isNodeAfter); */
    return isNodeAfter;
  }  // end isNodeAfter(Node node1, Node node2)

  /**
   * Figure out if child2 is after child1 in document order.
   * @param parent Must be the parent of both child1 and child2.
   * @param child1 Must be the child of parent and not equal to child2.
   * @param child2 Must be the child of parent and not equal to child1.
   * @returns true if child 2 is after child1 in document order.
   *
   * NEEDSDOC ($objectName$) @return
   */
  private static boolean isNodeAfterSibling(Node parent, Node child1,
                                            Node child2)
  {

    boolean isNodeAfterSibling = false;
    short child1type = child1.getNodeType();
    short child2type = child2.getNodeType();

    if ((Node.ATTRIBUTE_NODE != child1type)
            && (Node.ATTRIBUTE_NODE == child2type))
    {

      // always sort attributes before non-attributes.
      isNodeAfterSibling = false;
    }
    else if ((Node.ATTRIBUTE_NODE == child1type)
             && (Node.ATTRIBUTE_NODE != child2type))
    {

      // always sort attributes before non-attributes.
      isNodeAfterSibling = true;
    }
    else if (Node.ATTRIBUTE_NODE == child1type)
    {
      NamedNodeMap children = parent.getAttributes();
      int nNodes = children.getLength();
      boolean found1 = false, found2 = false;

      for (int i = 0; i < nNodes; i++)
      {
        Node child = children.item(i);

        if (child1 == child)
        {
          if (found2)
          {
            isNodeAfterSibling = false;

            break;
          }

          found1 = true;
        }
        else if (child2 == child)
        {
          if (found1)
          {
            isNodeAfterSibling = true;

            break;
          }

          found2 = true;
        }
      }
    }
    else
    {

      // NodeList children = parent.getChildNodes();
      // int nNodes = children.getLength();
      Node child = parent.getFirstChild();
      boolean found1 = false, found2 = false;

      while (null != child)
      {

        // Node child = children.item(i);
        if (child1 == child)
        {
          if (found2)
          {
            isNodeAfterSibling = false;

            break;
          }

          found1 = true;
        }
        else if (child2 == child)
        {
          if (found1)
          {
            isNodeAfterSibling = true;

            break;
          }

          found2 = true;
        }

        child = child.getNextSibling();
      }
    }

    return isNodeAfterSibling;
  }  // end isNodeAfterSibling(Node parent, Node child1, Node child2)

  //==========================================================
  // SECTION: Namespace resolution
  //==========================================================

  /**
   * <meta name="usage" content="internal"/>
   * Get the depth level of this node in the tree (count from 1).
   *
   * NEEDSDOC @param n
   *
   * NEEDSDOC ($objectName$) @return
   */
  public short getLevel(Node n)
  {

    short level = 1;

    while (null != (n = getParentOfNode(n)))
    {
      level++;
    }

    return level;
  }

  /**
   * Given a prefix and a namespace context, return the expanded namespace.
   * Default handling:
   *
   * NEEDSDOC @param prefix
   * NEEDSDOC @param namespaceContext
   *
   * NEEDSDOC ($objectName$) @return
   */
  public String getNamespaceForPrefix(String prefix, Element namespaceContext)
  {

    int type;
    Node parent = namespaceContext;
    String namespace = null;

    if (prefix.equals("xml"))
    {
      namespace = QName.S_XMLNAMESPACEURI;
    }
    else
    {
      while ((null != parent) && (null == namespace)
             && (((type = parent.getNodeType()) == Node.ELEMENT_NODE)
                 || (type == Node.ENTITY_REFERENCE_NODE)))
      {
        if (type == Node.ELEMENT_NODE)
        {
          NamedNodeMap nnm = parent.getAttributes();

          for (int i = 0; i < nnm.getLength(); i++)
          {
            Node attr = nnm.item(i);
            String aname = attr.getNodeName();
            boolean isPrefix = aname.startsWith("xmlns:");

            if (isPrefix || aname.equals("xmlns"))
            {
              int index = aname.indexOf(':');
              String p = isPrefix ? aname.substring(index + 1) : "";

              if (p.equals(prefix))
              {
                namespace = attr.getNodeValue();

                break;
              }
            }
          }
        }

        parent = getParentOfNode(parent);
      }
    }

    return namespace;
  }

  /**
   * An experiment for the moment.
   */
  Hashtable m_NSInfos = new Hashtable();

  /** NEEDSDOC Field m_NSInfoUnProcWithXMLNS          */
  protected static final NSInfo m_NSInfoUnProcWithXMLNS = new NSInfo(false,
                                                            true);

  /** NEEDSDOC Field m_NSInfoUnProcWithoutXMLNS          */
  protected static final NSInfo m_NSInfoUnProcWithoutXMLNS = new NSInfo(false,
                                                               false);

  /** NEEDSDOC Field m_NSInfoUnProcNoAncestorXMLNS          */
  protected static final NSInfo m_NSInfoUnProcNoAncestorXMLNS =
    new NSInfo(false, false, NSInfo.ANCESTORNOXMLNS);

  /** NEEDSDOC Field m_NSInfoNullWithXMLNS          */
  protected static final NSInfo m_NSInfoNullWithXMLNS = new NSInfo(true,
                                                          true);

  /** NEEDSDOC Field m_NSInfoNullWithoutXMLNS          */
  protected static final NSInfo m_NSInfoNullWithoutXMLNS = new NSInfo(true,
                                                             false);

  /** NEEDSDOC Field m_NSInfoNullNoAncestorXMLNS          */
  protected static final NSInfo m_NSInfoNullNoAncestorXMLNS =
    new NSInfo(true, false, NSInfo.ANCESTORNOXMLNS);

  /** NEEDSDOC Field m_candidateNoAncestorXMLNS          */
  protected Vector m_candidateNoAncestorXMLNS = new Vector();

  /**
   * Returns the namespace of the given node.
   *
   * NEEDSDOC @param n
   *
   * NEEDSDOC ($objectName$) @return
   */
  public String getNamespaceOfNode(Node n)
  {

    String namespaceOfPrefix;
    boolean hasProcessedNS;
    NSInfo nsInfo;
    short ntype = n.getNodeType();

    if (Node.ATTRIBUTE_NODE != ntype)
    {
      Object nsObj = m_NSInfos.get(n);  // return value

      nsInfo = (nsObj == null) ? null : (NSInfo) nsObj;
      hasProcessedNS = (nsInfo == null) ? false : nsInfo.m_hasProcessedNS;
    }
    else
    {
      hasProcessedNS = false;
      nsInfo = null;
    }

    if (hasProcessedNS)
    {
      namespaceOfPrefix = nsInfo.m_namespace;
    }
    else
    {
      namespaceOfPrefix = null;

      String nodeName = n.getNodeName();
      int indexOfNSSep = nodeName.indexOf(':');
      String prefix;

      if (Node.ATTRIBUTE_NODE == ntype)
      {
        if (indexOfNSSep > 0)
        {
          prefix = nodeName.substring(0, indexOfNSSep);
        }
        else
        {

          // Attributes don't use the default namespace, so if 
          // there isn't a prefix, we're done.
          return namespaceOfPrefix;
        }
      }
      else
      {
        prefix = (indexOfNSSep >= 0)
                 ? nodeName.substring(0, indexOfNSSep) : "";
      }

      boolean ancestorsHaveXMLNS = false;
      boolean nHasXMLNS = false;

      if (prefix.equals("xml"))
      {
        namespaceOfPrefix = QName.S_XMLNAMESPACEURI;
      }
      else
      {
        int parentType;
        Node parent = n;

        while ((null != parent) && (null == namespaceOfPrefix))
        {
          if ((null != nsInfo)
                  && (nsInfo.m_ancestorHasXMLNSAttrs
                      == nsInfo.ANCESTORNOXMLNS))
          {
            break;
          }

          parentType = parent.getNodeType();

          if ((null == nsInfo) || nsInfo.m_hasXMLNSAttrs)
          {
            boolean elementHasXMLNS = false;

            if (parentType == Node.ELEMENT_NODE)
            {
              NamedNodeMap nnm = parent.getAttributes();

              for (int i = 0; i < nnm.getLength(); i++)
              {
                Node attr = nnm.item(i);
                String aname = attr.getNodeName();

                if (aname.charAt(0) == 'x')
                {
                  boolean isPrefix = aname.startsWith("xmlns:");

                  if (aname.equals("xmlns") || isPrefix)
                  {
                    if (n == parent)
                      nHasXMLNS = true;

                    elementHasXMLNS = true;
                    ancestorsHaveXMLNS = true;

                    String p = isPrefix ? aname.substring(6) : "";

                    if (p.equals(prefix))
                    {
                      namespaceOfPrefix = attr.getNodeValue();

                      break;
                    }
                  }
                }
              }
            }

            if ((Node.ATTRIBUTE_NODE != parentType) && (null == nsInfo)
                    && (n != parent))
            {
              nsInfo = elementHasXMLNS
                       ? m_NSInfoUnProcWithXMLNS : m_NSInfoUnProcWithoutXMLNS;

              m_NSInfos.put(parent, nsInfo);
            }
          }

          if (Node.ATTRIBUTE_NODE == parentType)
          {
            parent = getParentOfNode(parent);
          }
          else
          {
            m_candidateNoAncestorXMLNS.addElement(parent);
            m_candidateNoAncestorXMLNS.addElement(nsInfo);

            parent = parent.getParentNode();
          }

          if (null != parent)
          {
            Object nsObj = m_NSInfos.get(parent);  // return value

            nsInfo = (nsObj == null) ? null : (NSInfo) nsObj;
          }
        }

        int nCandidates = m_candidateNoAncestorXMLNS.size();

        if (nCandidates > 0)
        {
          if ((false == ancestorsHaveXMLNS) && (null == parent))
          {
            for (int i = 0; i < nCandidates; i += 2)
            {
              Object candidateInfo = m_candidateNoAncestorXMLNS.elementAt(i
                                       + 1);

              if (candidateInfo == m_NSInfoUnProcWithoutXMLNS)
              {
                m_NSInfos.put(m_candidateNoAncestorXMLNS.elementAt(i),
                              m_NSInfoUnProcNoAncestorXMLNS);
              }
              else if (candidateInfo == m_NSInfoNullWithoutXMLNS)
              {
                m_NSInfos.put(m_candidateNoAncestorXMLNS.elementAt(i),
                              m_NSInfoNullNoAncestorXMLNS);
              }
            }
          }

          m_candidateNoAncestorXMLNS.removeAllElements();
        }
      }

      if (Node.ATTRIBUTE_NODE != ntype)
      {
        if (null == namespaceOfPrefix)
        {
          if (ancestorsHaveXMLNS)
          {
            if (nHasXMLNS)
              m_NSInfos.put(n, m_NSInfoNullWithXMLNS);
            else
              m_NSInfos.put(n, m_NSInfoNullWithoutXMLNS);
          }
          else
          {
            m_NSInfos.put(n, m_NSInfoNullNoAncestorXMLNS);
          }
        }
        else
        {
          m_NSInfos.put(n, new NSInfo(namespaceOfPrefix, nHasXMLNS));
        }
      }
    }

    return namespaceOfPrefix;
  }

  /**
   * Returns the local name of the given node.
   *
   * NEEDSDOC @param n
   *
   * NEEDSDOC ($objectName$) @return
   */
  public String getLocalNameOfNode(Node n)
  {

    String qname = n.getNodeName();
    int index = qname.indexOf(':');

    return (index < 0) ? qname : qname.substring(index + 1);
  }

  /**
   * Returns the element name with the namespace expanded.
   *
   * NEEDSDOC @param elem
   *
   * NEEDSDOC ($objectName$) @return
   */
  public String getExpandedElementName(Element elem)
  {

    String namespace = getNamespaceOfNode(elem);

    return (null != namespace)
           ? namespace + ":" + getLocalNameOfNode(elem)
           : getLocalNameOfNode(elem);
  }

  /**
   * Returns the attribute name with the namespace expanded.
   *
   * NEEDSDOC @param attr
   *
   * NEEDSDOC ($objectName$) @return
   */
  public String getExpandedAttributeName(Attr attr)
  {

    String namespace = getNamespaceOfNode(attr);

    return (null != namespace)
           ? namespace + ":" + getLocalNameOfNode(attr)
           : getLocalNameOfNode(attr);
  }

  //==========================================================
  // SECTION: DOM Helper Functions
  //==========================================================

  /**
   * Tell if the node is ignorable whitespace.
   * @deprecated
   *
   * NEEDSDOC @param node
   *
   * NEEDSDOC ($objectName$) @return
   */
  public boolean isIgnorableWhitespace(Text node)
  {

    boolean isIgnorable = false;  // return value

    // TODO: I can probably do something to figure out if this 
    // space is ignorable from just the information in
    // the DOM tree.
    return isIgnorable;
  }

  /**
   * Get the first unparented node in the ancestor chain.
   * @deprecated
   *
   * NEEDSDOC @param node
   *
   * NEEDSDOC ($objectName$) @return
   */
  public Node getRoot(Node node)
  {

    Node root = null;

    while (node != null)
    {
      root = node;
      node = getParentOfNode(node);
    }

    return root;
  }

  /**
   * Get the root node of the document tree, regardless of
   * whether or not the node passed in is a document node.
   *
   * NEEDSDOC @param n
   *
   * NEEDSDOC ($objectName$) @return
   */
  public Node getRootNode(Node n)
  {
    int nt = n.getNodeType();
    return ( (Node.DOCUMENT_NODE == nt) || (Node.DOCUMENT_FRAGMENT_NODE == nt) ) 
           ? n : n.getOwnerDocument();
  }

  /**
   * Tell if the given node is a namespace decl node.
   *
   * NEEDSDOC @param n
   *
   * NEEDSDOC ($objectName$) @return
   */
  public boolean isNamespaceNode(Node n)
  {

    if (Node.ATTRIBUTE_NODE == n.getNodeType())
    {
      String attrName = n.getNodeName();

      return (attrName.startsWith("xmlns:") || attrName.equals("xmlns"));
    }

    return false;
  }

  /**
   * I have to write this silly, and expensive function,
   * because the DOM WG decided that attributes don't
   * have parents.  If Xalan is used with a DOM implementation
   * that reuses attribute nodes, this will not work correctly.
   *
   * NEEDSDOC @param node
   *
   * NEEDSDOC ($objectName$) @return
   *
   * @throws RuntimeException
   */
  public Node getParentOfNode(Node node) throws RuntimeException
  {

    Node parent;
    short nodeType = node.getNodeType();

    if (Node.ATTRIBUTE_NODE == nodeType)
    {
      Document doc = node.getOwnerDocument();

      /*
      TBD:
      if(null == doc)
      {
        throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_CHILD_HAS_NO_OWNER_DOCUMENT, null));//"Attribute child does not have an owner document!");
      }
      */
      Element rootElem = doc.getDocumentElement();

      if (null == rootElem)
      {
        throw new RuntimeException(
          XSLMessages.createXPATHMessage(
            XPATHErrorResources.ER_CHILD_HAS_NO_OWNER_DOCUMENT_ELEMENT,
            null));  //"Attribute child does not have an owner document element!");
      }

      parent = locateAttrParent(rootElem, node);
    }
    else
    {
      parent = node.getParentNode();

      // if((Node.DOCUMENT_NODE != nodeType) && (null == parent))
      // {
      //   throw new RuntimeException("Child does not have parent!");
      // }
    }

    return parent;
  }

  /**
   * Given an ID, return the element.
   *
   * NEEDSDOC @param id
   * NEEDSDOC @param doc
   *
   * NEEDSDOC ($objectName$) @return
   */
  public Element getElementByID(String id, Document doc)
  {
    return null;
  }

  /**
   * The getUnparsedEntityURI function returns the URI of the unparsed
   * entity with the specified name in the same document as the context
   * node (see [3.3 Unparsed Entities]). It returns the empty string if
   * there is no such entity.
   * Since it states in the DOM draft: "An XML processor may choose to
   * completely expand entities before the structure model is passed
   * to the DOM; in this case, there will be no EntityReferences in the DOM tree."
   * So I'm not sure how well this is going to work.
   *
   * NEEDSDOC @param name
   * NEEDSDOC @param doc
   *
   * NEEDSDOC ($objectName$) @return
   */
  public String getUnparsedEntityURI(String name, Document doc)
  {

    String url = "";
    DocumentType doctype = doc.getDoctype();

    if (null != doctype)
    {
      NamedNodeMap entities = doctype.getEntities();
      Entity entity = (Entity) entities.getNamedItem(name);
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

    return url;
  }

  /**
   * Support for getParentOfNode.
   *
   * NEEDSDOC @param elem
   * NEEDSDOC @param attr
   *
   * NEEDSDOC ($objectName$) @return
   */
  private Node locateAttrParent(Element elem, Node attr)
  {

    Node parent = null;
    NamedNodeMap attrs = elem.getAttributes();

    if (null != attrs)
    {
      int nAttrs = attrs.getLength();

      for (int i = 0; i < nAttrs; i++)
      {
        if (attr == attrs.item(i))
        {
          parent = elem;

          break;
        }
      }
    }

    if (null == parent)
    {
      for (Node node = elem.getFirstChild(); null != node;
              node = node.getNextSibling())
      {
        if (Node.ELEMENT_NODE == node.getNodeType())
        {
          parent = locateAttrParent((Element) node, attr);

          if (null != parent)
            break;
        }
      }
    }

    return parent;
  }

  /**
   * The factory object used for creating nodes
   * in the result tree.
   */
  protected Document m_DOMFactory = null;

  /**
   * Get the factory object required to create DOM nodes
   * in the result tree.
   *
   * NEEDSDOC @param domFactory
   */
  public void setDOMFactory(Document domFactory)
  {
    this.m_DOMFactory = domFactory;
  }

  /**
   * Get the factory object required to create DOM nodes
   * in the result tree.
   *
   * NEEDSDOC ($objectName$) @return
   */
  public Document getDOMFactory()
  {

    if (null == this.m_DOMFactory)
    {
      this.m_DOMFactory = createDocument();
    }

    return this.m_DOMFactory;
  }

  /**
   * Get the textual contents of the node. If the node
   * is an element, apply whitespace stripping rules,
   * though I'm not sure if this is right (I'll fix
   * or declare victory when I review the entire
   * whitespace handling).
   *
   * NEEDSDOC @param node
   *
   * NEEDSDOC ($objectName$) @return
   */
  public static String getNodeData(Node node)
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

    return s;
  }

  /**
   * Get the textual contents of the node. If the node
   * is an element, apply whitespace stripping rules,
   * though I'm not sure if this is right (I'll fix
   * or declare victory when I review the entire
   * whitespace handling).
   *
   * NEEDSDOC @param node
   * NEEDSDOC @param buf
   */
  public static void getNodeData(Node node, FastStringBuffer buf)
  {

    // String data = null;
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
      buf.append(node.getNodeValue());
      break;
    case Node.ATTRIBUTE_NODE :
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
}
