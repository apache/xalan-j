/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id$
 */

package org.apache.xalan.lib.sql;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.xml.transform.SourceLocator;

import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMAxisIterator;
import org.apache.xml.dtm.DTMAxisTraverser;
import org.apache.xml.dtm.DTMManager;
import org.apache.xml.dtm.ref.DTMDefaultBaseIterators;
import org.apache.xml.utils.FastStringBuffer;
import org.apache.xml.utils.StringBufferPool;
import org.apache.xml.utils.SuballocatedIntVector;
import org.apache.xml.utils.XMLString;

import org.w3c.dom.Node;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;

/**
 * The SQL Document is the main controlling class the executesa SQL Query
 */
public class DTMDocument extends DTMDefaultBaseIterators
{

  /**
   */
  public interface CharacterNodeHandler
  {
    /**
     * @param node
     *
     * @throws org.xml.sax.SAXException
     */
    public void characters( Node node )throws org.xml.sax.SAXException ;
  }

  /**
   */
  private boolean DEBUG = false;

  /**
   */
  protected static final String S_NAMESPACE = "http://xml.apache.org/xalan/SQLExtension";

  /**
   */
  protected static final String S_ATTRIB_NOT_SUPPORTED = "Not Supported";
  /**
   */
  protected static final String S_ISTRUE = "true";
  /**
   */
  protected static final String S_ISFALSE = "false";

  /**
   */
  protected static final String S_DOCUMENT = "#root";
  /**
   */
  protected static final String S_TEXT_NODE = "#text";
  /**
   */
  protected static final String S_ELEMENT_NODE = "#element";

  /**
   */
  protected int m_Document_TypeID = 0;
  /**
   */
  protected int m_TextNode_TypeID = 0;


  /**
   * Store the SQL Data in this growable array
   */
  protected ObjectArray m_ObjectArray = new ObjectArray();

  /**
   * For each element node, there can be zero or more attributes. If Attributes
   * are assigned, the first attribute for that element will be use here.
   * Subsequent elements will use the m_nextsib, m_prevsib array. The sibling
   * arrays are not meeant to hold indexes to attribute information but as
   * long as there is not direct connection back into the main DTM tree
   * we should be OK.
   */
  protected SuballocatedIntVector m_attribute;

  /**
   * The Document Index will most likely be 0, but we will reference it
   * by variable in case that paradigm falls through.
   */
  protected int m_DocumentIdx;


  /**
   * @param mgr
   * @param ident
   */
  public DTMDocument( DTMManager mgr, int ident )
  {
    super(mgr, null, ident,
      null, mgr.getXMLStringFactory(), true);

    m_attribute = new SuballocatedIntVector(DEFAULT_BLOCKSIZE);
  }

  /**
   * A common routine that allocates an Object from the Object Array.
   * One of the common bugs in this code was to allocate an Object and
   * not incerment m_size, using this method will assure that function.
   * @param o
   *
   */
  private int allocateNodeObject( Object o )
  {
    // Need to keep this counter going even if we don't use it.
    m_size++;
    return m_ObjectArray.append(o);
  }

  /**
   * @param o
   * @param level
   * @param extendedType
   * @param parent
   * @param prevsib
   *
   */
  protected int addElementWithData( Object o, int level, int extendedType, int parent, int prevsib )
  {
    int elementIdx = addElement(level,extendedType,parent,prevsib);

    int data = allocateNodeObject(o);
    m_firstch.setElementAt(data,elementIdx);

    m_exptype.setElementAt(m_TextNode_TypeID, data);
    // m_level.setElementAt((byte)(level), data);
    m_parent.setElementAt(elementIdx, data);

    m_prevsib.setElementAt(DTM.NULL, data);
    m_nextsib.setElementAt(DTM.NULL, data);
    m_attribute.setElementAt(DTM.NULL, data);
    m_firstch.setElementAt(DTM.NULL, data);

    return elementIdx;
  }

  /**
   * @param level
   * @param extendedType
   * @param parent
   * @param prevsib
   *
   */
  protected int addElement( int level, int extendedType, int parent, int prevsib )
  {
    int node = DTM.NULL;

    try
    {
      // Add the Node and adjust its Extended Type
      node = allocateNodeObject(S_ELEMENT_NODE);

      m_exptype.setElementAt(extendedType, node);
      m_nextsib.setElementAt(DTM.NULL, node);
      m_prevsib.setElementAt(prevsib, node);

      m_parent.setElementAt(parent, node);
      m_firstch.setElementAt(DTM.NULL, node);
      // m_level.setElementAt((byte)level, node);
      m_attribute.setElementAt(DTM.NULL, node);

      if (prevsib != DTM.NULL)
      {
        // If the previous sibling is already assigned, then we are
        // inserting a value into the chain.
        if (m_nextsib.elementAt(prevsib) != DTM.NULL)
          m_nextsib.setElementAt(m_nextsib.elementAt(prevsib), node);

        // Tell the proevious sibling that they have a new bother/sister.
        m_nextsib.setElementAt(node, prevsib);
      }

       // So if we have a valid parent and the new node ended up being first
      // in the list, i.e. no prevsib, then set the new node up as the
      // first child of the parent. Since we chained the node in the list,
      // there should be no reason to worry about the current first child
      // of the parent node.
      if ((parent != DTM.NULL) && (m_prevsib.elementAt(node) == DTM.NULL))
      {
        m_firstch.setElementAt(node, parent);
      }
    }
    catch(Exception e)
    {
      error("Error in addElement: "+e.getMessage());
    }

    return node;
  }

  /**
   * Link an attribute to a node, if the node already has one or more
   * attributes assigned, then just link this one to the attribute list.
   * The first attribute is attached to the Parent Node (pnode) through the
   * m_attribute array, subsequent attributes are linked through the
   * m_prevsib, m_nextsib arrays.
   * @param o
   * @param extendedType
   * @param pnode
   *
   */
  protected int addAttributeToNode( Object o, int extendedType, int pnode )
  {
    int attrib = DTM.NULL;
    //int prevsib = DTM.NULL;
    int lastattrib = DTM.NULL;
    // int value = DTM.NULL;

    try
    {
      // Add the Node and adjust its Extended Type
      attrib = allocateNodeObject(o);

      m_attribute.setElementAt(DTM.NULL, attrib);
      m_exptype.setElementAt(extendedType, attrib);
      // m_level.setElementAt((byte)0, attrib);

      // Clear the sibling references
      m_nextsib.setElementAt(DTM.NULL, attrib);
      m_prevsib.setElementAt(DTM.NULL,attrib);
      // Set the parent, although the was we are using attributes
      // in the SQL extension this reference will more than likly
      // be wrong
      m_parent.setElementAt(pnode, attrib);
      m_firstch.setElementAt(DTM.NULL, attrib);

      if (m_attribute.elementAt(pnode) != DTM.NULL)
      {
        // OK, we already have an attribute assigned to this
        // Node, Insert us into the head of the list.
        lastattrib = m_attribute.elementAt(pnode);
        m_nextsib.setElementAt(lastattrib, attrib);
        m_prevsib.setElementAt(attrib, lastattrib);
      }
      // Okay set the new attribute up as the first attribute
      // for the node.
      m_attribute.setElementAt(attrib, pnode);
    }
    catch(Exception e)
    {
      error("Error in addAttributeToNode: "+e.getMessage());
    }

    return attrib;
  }

  /**
   * Allow two nodes to share the same set of attributes. There may be some
   * problems because the parent of any attribute will be the original node
   * they were assigned to. Need to see how the attribute walker works, then
   * we should be able to fake it out.
   * @param toNode
   * @param fromNode
   *
   */
  protected void cloneAttributeFromNode( int toNode, int fromNode )
  {
   try
    {
      if (m_attribute.elementAt(toNode) != DTM.NULL)
      {
        error("Cloneing Attributes, where from Node already had addtibures assigned");
      }

      m_attribute.setElementAt(m_attribute.elementAt(fromNode), toNode);
    }
    catch(Exception e)
    {
      error("Cloning attributes");
    }
  }


  /**
   * @param parm1
   *
   */
  public int getFirstAttribute( int parm1 )
  {
    if (DEBUG) System.out.println("getFirstAttribute("+ parm1+")");
    int nodeIdx = makeNodeIdentity(parm1);
    if (nodeIdx != DTM.NULL)
    {
      int attribIdx =  m_attribute.elementAt(nodeIdx);
      return makeNodeHandle(attribIdx);
    }
    else return DTM.NULL;
  }

 /**
   * @param parm1
   *
   */
  public String getNodeValue( int parm1 )
  {
    if (DEBUG) System.out.println("getNodeValue(" + parm1 + ")");
    try
    {
      Object o = m_ObjectArray.getAt(makeNodeIdentity(parm1));
      if (o != null && o != S_ELEMENT_NODE)
      {
        return o.toString();
      }
      else
      {
        return "";
      }
    }
    catch(Exception e)
    {
      error("Getting String Value");
      return null;
    }
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
    int nodeIdx = makeNodeIdentity(nodeHandle);
    if (DEBUG) System.out.println("getStringValue(" + nodeIdx + ")");

      Object o = m_ObjectArray.getAt(nodeIdx);
    if ( o == S_ELEMENT_NODE )
      {
        FastStringBuffer buf = StringBufferPool.get();
        String s;

        try
        {
          getNodeData(nodeIdx, buf);

          s = (buf.length() > 0) ? buf.toString() : "";
        }
        finally
        {
          StringBufferPool.free(buf);
        }

        return m_xstrf.newstr( s );
      }
      else if( o != null )
      {
        return m_xstrf.newstr(o.toString());
    }
    else
      return(m_xstrf.emptystr());
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
   * @param nodeIdx Index of node whose subtree is to be walked, gathering the
   * contents of all Text or CDATASection nodes.
   * @param buf FastStringBuffer into which the contents of the text
   * nodes are to be concatenated.
   */
  protected void getNodeData(int nodeIdx, FastStringBuffer buf)
  {
    for ( int child = _firstch(nodeIdx) ; child != DTM.NULL ; child = _nextsib(child) )
    {
      Object o = m_ObjectArray.getAt(child);
      if ( o == S_ELEMENT_NODE )
        getNodeData(child, buf);
      else if ( o != null )
        buf.append(o.toString());
    }
  }




  /**
   * @param parm1
   *
   */
  public int getNextAttribute( int parm1 )
  {
    int nodeIdx = makeNodeIdentity(parm1);
    if (DEBUG) System.out.println("getNextAttribute(" + nodeIdx + ")");
    if (nodeIdx != DTM.NULL) return makeNodeHandle(m_nextsib.elementAt(nodeIdx));
    else return DTM.NULL;
  }


  /**
   *
   */
  protected int getNumberOfNodes( )
  {
    if (DEBUG) System.out.println("getNumberOfNodes()");
    return m_size;
  }

  /**
   *
   */
  protected boolean nextNode( )
  {
    if (DEBUG) System.out.println("nextNode()");
    return false;
  }


  /**
   * The Expanded Name table holds all of our Node names. The Base class
   * will add the common element types, need to call this function from
   * the derived class.
   *
   */
  protected void createExpandedNameTable( )
  {
    m_Document_TypeID =
      m_expandedNameTable.getExpandedTypeID(S_NAMESPACE, S_DOCUMENT, DTM.DOCUMENT_NODE);

    m_TextNode_TypeID =
      m_expandedNameTable.getExpandedTypeID(S_NAMESPACE, S_TEXT_NODE, DTM.TEXT_NODE);
  }


  /**
   *
   */
  public void dumpDTM( )
  {
    try
    {
//      File f = new File("DTMDump"+((Object)this).hashCode()+".txt");
      File f = new File("DTMDump.txt");
      System.err.println("Dumping... "+f.getAbsolutePath());
      PrintStream ps = new PrintStream(new FileOutputStream(f));

      while (nextNode()){}

      int nRecords = m_size;

      ps.println("Total nodes: " + nRecords);

      for (int i = 0; i < nRecords; i++)
      {
        ps.println("=========== " + i + " ===========");
        ps.println("NodeName: " + getNodeName(makeNodeHandle(i)));
        ps.println("NodeNameX: " + getNodeNameX(makeNodeHandle(i)));
        ps.println("LocalName: " + getLocalName(makeNodeHandle(i)));
        ps.println("NamespaceURI: " + getNamespaceURI(makeNodeHandle(i)));
        ps.println("Prefix: " + getPrefix(makeNodeHandle(i)));

        int exTypeID = getExpandedTypeID(makeNodeHandle(i));

        ps.println("Expanded Type ID: "
                           + Integer.toHexString(exTypeID));

        int type = getNodeType(makeNodeHandle(i));
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

        ps.println("Type: " + typestring);

        int firstChild = _firstch(i);

        if (DTM.NULL == firstChild)
          ps.println("First child: DTM.NULL");
        else if (NOTPROCESSED == firstChild)
          ps.println("First child: NOTPROCESSED");
        else
          ps.println("First child: " + firstChild);

        int prevSibling = _prevsib(i);

        if (DTM.NULL == prevSibling)
          ps.println("Prev sibling: DTM.NULL");
        else if (NOTPROCESSED == prevSibling)
          ps.println("Prev sibling: NOTPROCESSED");
        else
          ps.println("Prev sibling: " + prevSibling);

        int nextSibling = _nextsib(i);

        if (DTM.NULL == nextSibling)
          ps.println("Next sibling: DTM.NULL");
        else if (NOTPROCESSED == nextSibling)
          ps.println("Next sibling: NOTPROCESSED");
        else
          ps.println("Next sibling: " + nextSibling);

        int parent = _parent(i);

        if (DTM.NULL == parent)
          ps.println("Parent: DTM.NULL");
        else if (NOTPROCESSED == parent)
          ps.println("Parent: NOTPROCESSED");
        else
          ps.println("Parent: " + parent);

        int level = _level(i);

        ps.println("Level: " + level);
        ps.println("Node Value: " + getNodeValue(i));
        ps.println("String Value: " + getStringValue(i));

        ps.println("First Attribute Node: " + m_attribute.elementAt(i));
      }

    }
    catch(IOException ioe)
    {
      ioe.printStackTrace(System.err);
      throw new RuntimeException(ioe.getMessage());
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
   * @param node Node whose subtree is to be walked, gathering the
   * contents of all Text or CDATASection nodes.
   * @param ch
   * @param depth
   *
   * @throws org.xml.sax.SAXException
   */
  protected static void dispatchNodeData( Node node, ContentHandler ch, int depth )throws org.xml.sax.SAXException
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

  /*********************************************************************/
  /*********************************************************************/
  /******************* End of Functions we Wrote ***********************/
  /*********************************************************************/
  /*********************************************************************/


  /**
   * For the moment all the run time properties are ignored by this
   * class.
   * @param property a <code>String</code> value
   * @param value an <code>Object</code> value
   *
   */
  public void setProperty( String property, Object value )
  {
  }

  /**
   * No source information is available for DOM2DTM, so return
   * <code>null</code> here.
   * @param node an <code>int</code> value
   * @return null
   */
  public SourceLocator getSourceLocatorFor( int node )
  {
    return null;
  }

  /**
   * @param parm1
   *
   */
  protected int getNextNodeIdentity( int parm1 )
  {
    if (DEBUG) System.out.println("getNextNodeIdenty(" + parm1 + ")");
    return DTM.NULL;
  }

  /**
   * @param parm1
   * @param parm2
   * @param parm3
   *
   */
  public int getAttributeNode( int parm1, String parm2, String parm3 )
  {
    if (DEBUG)
    {
      System.out.println(
        "getAttributeNode(" +
        parm1 + "," +
        parm2 + "," +
        parm3 + ")");
    }
    return DTM.NULL;
  }

  /**
   * @param parm1
   *
   */
  public String getLocalName( int parm1 )
  {
//    int exID = this.getExpandedTypeID( makeNodeIdentity(parm1) );
      int exID = getExpandedTypeID(parm1);

    if (DEBUG)
    {
      DEBUG = false;
      System.out.print("getLocalName(" + parm1 + ") -> ");
      System.out.println("..." + getLocalNameFromExpandedNameID(exID) );
      DEBUG = true;
    }

    return getLocalNameFromExpandedNameID(exID);
  }

  /**
   * @param parm1
   *
   */
  public String getNodeName( int parm1 )
  {
//    int exID = getExpandedTypeID( makeNodeIdentity(parm1) );
    int exID = getExpandedTypeID( parm1 );
    if (DEBUG)
    {
      DEBUG = false;
      System.out.print("getLocalName(" + parm1 + ") -> ");
      System.out.println("..." + getLocalNameFromExpandedNameID(exID) );
      DEBUG = true;
    }
    return getLocalNameFromExpandedNameID(exID);
  }

   /**
   * @param parm1
   *
   */
  public boolean isAttributeSpecified( int parm1 )
  {
    if (DEBUG) System.out.println("isAttributeSpecified(" + parm1 + ")");
    return false;
  }

  /**
   * @param parm1
   *
   */
  public String getUnparsedEntityURI( String parm1 )
  {
    if (DEBUG) System.out.println("getUnparsedEntityURI(" + parm1 + ")");
    return "";
  }

  /**
   *
   */
  public DTDHandler getDTDHandler( )
  {
    if (DEBUG) System.out.println("getDTDHandler()");
    return null;
  }

  /**
   * @param parm1
   *
   */
  public String getPrefix( int parm1 )
  {
    if (DEBUG) System.out.println("getPrefix(" + parm1  + ")");
    return "";
  }

  /**
   *
   */
  public EntityResolver getEntityResolver( )
  {
    if (DEBUG) System.out.println("getEntityResolver()");
    return null;
  }

  /**
   *
   */
  public String getDocumentTypeDeclarationPublicIdentifier( )
  {
    if (DEBUG) System.out.println("get_DTD_PubId()");
    return "";
  }

  /**
   *
   */
  public LexicalHandler getLexicalHandler( )
  {
    if (DEBUG) System.out.println("getLexicalHandler()");
    return null;
  }
  /**
   *
   */
  public boolean needsTwoThreads( )
  {
    if (DEBUG) System.out.println("needsTwoThreads()");
    return false;
  }

  /**
   *
   */
  public ContentHandler getContentHandler( )
  {
    if (DEBUG) System.out.println("getContentHandler()");
    return null;
  }

  /**
   * @param parm1
   * @param parm2
   *
   * @throws org.xml.sax.SAXException
   */
  public void dispatchToEvents( int parm1, ContentHandler parm2 )throws org.xml.sax.SAXException
  {
    if (DEBUG)
    {
      System.out.println(
      "dispathcToEvents(" +
      parm1 + "," +
      parm2 + ")");
    }
    return;
  }

  /**
   * @param parm1
   *
   */
  public String getNamespaceURI( int parm1 )
  {
    if (DEBUG) System.out.println("getNamespaceURI(" +parm1+")");
    return "";
  }

  /**
   * @param nodeHandle
   * @param ch
   * @param normalize
   *
   * @throws org.xml.sax.SAXException
   */
  public void dispatchCharactersEvents( int nodeHandle, ContentHandler ch, boolean normalize )throws org.xml.sax.SAXException
  {
    if (DEBUG)
    {
      System.out.println("dispatchCharacterEvents(" +
      nodeHandle + "," +
      ch + "," +
      normalize + ")");
    }

    if(normalize)
    {
      XMLString str = getStringValue(nodeHandle);
      str = str.fixWhiteSpace(true, true, false);
      str.dispatchCharactersEvents(ch);
    }
    else
    {
      Node node = getNode(nodeHandle);
      dispatchNodeData(node, ch, 0);
    }
  }

  /**
   * Event overriding for Debug
   *
   */
  public boolean supportsPreStripping( )
  {
    if (DEBUG) System.out.println("supportsPreStripping()");
    return super.supportsPreStripping();
  }

  /**
   * @param parm1
   *
   */
  protected int _exptype( int parm1 )
  {
    if (DEBUG) System.out.println("_exptype(" + parm1 + ")");
    return super._exptype( parm1);
  }

  /**
   * @param parm1
   *
   */
  protected SuballocatedIntVector findNamespaceContext( int parm1 )
  {
    if (DEBUG) System.out.println("SuballocatedIntVector(" + parm1 + ")");
    return super.findNamespaceContext( parm1);
  }

  /**
   * @param parm1
   *
   */
  protected int _prevsib( int parm1 )
  {
    if (DEBUG) System.out.println("_prevsib(" + parm1+ ")");
    return super._prevsib( parm1);
  }


  /**
   * @param parm1
   *
   */
  protected short _type( int parm1 )
  {
    if (DEBUG) System.out.println("_type(" + parm1 + ")");
    return super._type( parm1);
  }

  /**
   * @param parm1
   *
   */
  public Node getNode( int parm1 )
  {
    if (DEBUG) System.out.println("getNode(" + parm1 + ")");
    return super.getNode( parm1);
  }

  /**
   * @param parm1
   *
   */
  public int getPreviousSibling( int parm1 )
  {
    if (DEBUG) System.out.println("getPrevSib(" + parm1 + ")");
    return super.getPreviousSibling( parm1);
  }

  /**
   * @param parm1
   *
   */
  public String getDocumentStandalone( int parm1 )
  {
    if (DEBUG) System.out.println("getDOcStandAlone(" + parm1 + ")");
    return super.getDocumentStandalone( parm1);
  }

  /**
   * @param parm1
   *
   */
  public String getNodeNameX( int parm1 )
  {
    if (DEBUG) System.out.println("getNodeNameX(" + parm1 + ")");
    //return super.getNodeNameX( parm1);
    return getNodeName(parm1);

  }

  /**
   * @param parm1
   * @param parm2
   *
   */
  public void setFeature( String parm1, boolean parm2 )
  {
    if (DEBUG)
    {
      System.out.println(
        "setFeature(" +
        parm1 + "," +
        parm2 + ")");
    }
    super.setFeature( parm1,  parm2);
  }

  /**
   * @param parm1
   *
   */
  protected int _parent( int parm1 )
  {
    if (DEBUG) System.out.println("_parent(" + parm1 + ")");
    return super._parent( parm1);
  }

  /**
   * @param parm1
   * @param parm2
   *
   */
  protected void indexNode( int parm1, int parm2 )
  {
    if (DEBUG) System.out.println("indexNode("+parm1+","+parm2+")");
    super.indexNode( parm1,  parm2);
  }

  /**
   *
   */
  protected boolean getShouldStripWhitespace( )
  {
    if (DEBUG) System.out.println("getShouldStripWS()");
    return super.getShouldStripWhitespace();
  }

  /**
   *
   */
  protected void popShouldStripWhitespace( )
  {
    if (DEBUG) System.out.println("popShouldStripWS()");
    super.popShouldStripWhitespace();
  }

  /**
   * @param parm1
   * @param parm2
   *
   */
  public boolean isNodeAfter( int parm1, int parm2 )
  {
    if (DEBUG) System.out.println("isNodeAfter(" + parm1 + "," + parm2 + ")");
    return super.isNodeAfter( parm1,  parm2);
  }

  /**
   * @param parm1
   *
   */
  public int getNamespaceType( int parm1 )
  {
    if (DEBUG) System.out.println("getNamespaceType(" + parm1 + ")");
    return super.getNamespaceType( parm1);
  }

  /**
   * @param parm1
   *
   */
  protected int _level( int parm1 )
  {
    if (DEBUG) System.out.println("_level(" + parm1 + ")");
    return super._level( parm1);
  }


  /**
   * @param parm1
   *
   */
  protected void pushShouldStripWhitespace( boolean parm1 )
  {
    if (DEBUG) System.out.println("push_ShouldStripWS(" + parm1 + ")");
    super.pushShouldStripWhitespace( parm1);
  }

  /**
   * @param parm1
   *
   */
  public String getDocumentVersion( int parm1 )
  {
    if (DEBUG) System.out.println("getDocVer("+parm1+")");
    return super.getDocumentVersion( parm1);
  }

  /**
   * @param parm1
   * @param parm2
   *
   */
  public boolean isSupported( String parm1, String parm2 )
  {
    if (DEBUG) System.out.println("isSupported("+parm1+","+parm2+")");
    return super.isSupported( parm1,  parm2);
  }


  /**
   * @param parm1
   *
   */
  protected void setShouldStripWhitespace( boolean parm1 )
  {
    if (DEBUG) System.out.println("set_ShouldStripWS("+parm1+")");
    super.setShouldStripWhitespace( parm1);
  }


  /**
   * @param parm1
   * @param parm2
   *
   */
  protected void ensureSizeOfIndex( int parm1, int parm2 )
  {
    if (DEBUG) System.out.println("ensureSizeOfIndex("+parm1+","+parm2+")");
    super.ensureSizeOfIndex( parm1,  parm2);
  }

  /**
   * @param parm1
   *
   */
  protected void ensureSize( int parm1 )
  {
    if (DEBUG) System.out.println("ensureSize("+parm1+")");

    // IntVectors in DTMDefaultBase are now self-sizing, and ensureSize()
    // is being dropped.
    //super.ensureSize( parm1);
  }

  /**
   * @param parm1
   *
   */
  public String getDocumentEncoding( int parm1 )
  {
    if (DEBUG) System.out.println("getDocumentEncoding("+parm1+")");
    return super.getDocumentEncoding( parm1);
  }

  /**
   * @param parm1
   * @param parm2
   * @param parm3
   *
   */
  public void appendChild( int parm1, boolean parm2, boolean parm3 )
  {
    if (DEBUG)
    {
      System.out.println(
        "appendChild(" +
        parm1 + "," +
        parm2 + "," +
        parm3 + ")");
    }
    super.appendChild( parm1,  parm2,  parm3);
  }

  /**
   * @param parm1
   *
   */
  public short getLevel( int parm1 )
  {
    if (DEBUG) System.out.println("getLevel("+parm1+")");
    return super.getLevel( parm1);
  }

  /**
   *
   */
  public String getDocumentBaseURI( )
  {
    if (DEBUG) System.out.println("getDocBaseURI()");
    return super.getDocumentBaseURI();
  }

  /**
   * @param parm1
   * @param parm2
   * @param parm3
   *
   */
  public int getNextNamespaceNode( int parm1, int parm2, boolean parm3 )
  {
    if (DEBUG)
    {
      System.out.println(
      "getNextNamesapceNode(" +
      parm1 + "," +
      parm2 + "," +
      parm3 + ")");
    }
    return super.getNextNamespaceNode( parm1,  parm2,  parm3);
  }

  /**
   * @param parm1
   *
   */
  public void appendTextChild( String parm1 )
  {
    if (DEBUG) System.out.println("appendTextChild(" + parm1 + ")");
    super.appendTextChild( parm1);
  }

  /**
   * @param parm1
   * @param parm2
   * @param parm3
   * @param parm4
   *
   */
  protected int findGTE( int[] parm1, int parm2, int parm3, int parm4 )
  {
    if (DEBUG)
    {
      System.out.println(
      "findGTE("+
      parm1 + "," +
      parm2 + "," +
      parm3 + ")");
    }
    return super.findGTE( parm1,  parm2,  parm3,  parm4);
  }

  /**
   * @param parm1
   * @param parm2
   *
   */
  public int getFirstNamespaceNode( int parm1, boolean parm2 )
  {
    if (DEBUG) System.out.println("getFirstNamespaceNode()");
    return super.getFirstNamespaceNode( parm1,  parm2);
  }

  /**
   * @param parm1
   *
   */
  public int getStringValueChunkCount( int parm1 )
  {
    if (DEBUG) System.out.println("getStringChunkCount(" + parm1 + ")");
    return super.getStringValueChunkCount( parm1);
  }

  /**
   * @param parm1
   *
   */
  public int getLastChild( int parm1 )
  {
    if (DEBUG) System.out.println("getLastChild(" + parm1 + ")");
    return super.getLastChild( parm1);
  }

  /**
   * @param parm1
   *
   */
  public boolean hasChildNodes( int parm1 )
  {
    if (DEBUG) System.out.println("hasChildNodes(" + parm1 + ")");
    return super.hasChildNodes( parm1);
  }

  /**
   * @param parm1
   *
   */
  public short getNodeType( int parm1 )
  {
    if (DEBUG)
    {
      DEBUG=false;
      System.out.print("getNodeType(" + parm1 + ") ");
      int exID = getExpandedTypeID(parm1);
      String name = getLocalNameFromExpandedNameID(exID);
      System.out.println(
        ".. Node name [" + name + "]" +
        "[" + getNodeType( parm1) + "]");

      DEBUG=true;
    }

    return super.getNodeType( parm1);
  }

  /**
   * @param parm1
   *
   */
  public boolean isCharacterElementContentWhitespace( int parm1 )
  {
    if (DEBUG) System.out.println("isCharacterElementContentWhitespace(" + parm1 +")");
    return super.isCharacterElementContentWhitespace( parm1);
  }

  /**
   * @param parm1
   *
   */
  public int getFirstChild( int parm1 )
  {
    if (DEBUG) System.out.println("getFirstChild(" + parm1 + ")");
    return super.getFirstChild( parm1);
  }

  /**
   * @param parm1
   *
   */
  public String getDocumentSystemIdentifier( int parm1 )
  {
    if (DEBUG) System.out.println("getDocSysID(" + parm1 + ")");
    return super.getDocumentSystemIdentifier( parm1);
  }

  /**
   * @param parm1
   * @param parm2
   *
   */
  protected void declareNamespaceInContext( int parm1, int parm2 )
  {
    if (DEBUG) System.out.println("declareNamespaceContext("+parm1+","+parm2+")");
    super.declareNamespaceInContext( parm1,  parm2);
  }

  /**
   * @param parm1
   *
   */
  public String getNamespaceFromExpandedNameID( int parm1 )
  {
    if (DEBUG)
    {
      DEBUG = false;
      System.out.print("getNamespaceFromExpandedNameID("+parm1+")");
      System.out.println("..." + super.getNamespaceFromExpandedNameID( parm1) );
      DEBUG = true;
    }
    return super.getNamespaceFromExpandedNameID( parm1);
  }

  /**
   * @param parm1
   *
   */
  public String getLocalNameFromExpandedNameID( int parm1 )
  {
    if (DEBUG)
    {
      DEBUG = false;
      System.out.print("getLocalNameFromExpandedNameID("+parm1+")");
      System.out.println("..." + super.getLocalNameFromExpandedNameID( parm1));
      DEBUG = true;
    }
    return super.getLocalNameFromExpandedNameID( parm1);
  }

  /**
   * @param parm1
   *
   */
  public int getExpandedTypeID( int parm1 )
  {
    if (DEBUG) System.out.println("getExpandedTypeID("+parm1+")");
    return super.getExpandedTypeID( parm1);
  }

  /**
   *
   */
  public int getDocument( )
  {
    if (DEBUG) System.out.println("getDocument()");
    return super.getDocument();
  }


  /**
   * @param parm1
   * @param parm2
   *
   */
  protected int findInSortedSuballocatedIntVector( SuballocatedIntVector parm1, int parm2 )
  {
    if (DEBUG)
    {
      System.out.println(
      "findInSortedSubAlloctedVector(" +
      parm1 + "," +
      parm2 + ")");
    }
    return super.findInSortedSuballocatedIntVector( parm1,  parm2);
  }

  /**
   * @param parm1
   *
   */
  public boolean isDocumentAllDeclarationsProcessed( int parm1 )
  {
    if (DEBUG) System.out.println("isDocumentAllDeclProc("+parm1+")");
    return super.isDocumentAllDeclarationsProcessed( parm1);
  }

  /**
   * @param parm1
   *
   */
  protected void error( String parm1 )
  {
    if (DEBUG) System.out.println("error("+parm1+")");
    super.error( parm1);
  }


  /**
   * @param parm1
   *
   */
  protected int _firstch( int parm1 )
  {
    if (DEBUG) System.out.println("_firstch("+parm1+")");
    return super._firstch( parm1);
  }

  /**
   * @param parm1
   *
   */
  public int getOwnerDocument( int parm1 )
  {
    if (DEBUG) System.out.println("getOwnerDoc("+parm1+")");
    return super.getOwnerDocument( parm1);
  }

  /**
   * @param parm1
   *
   */
  protected int _nextsib( int parm1 )
  {
    if (DEBUG) System.out.println("_nextSib("+parm1+")");
    return super._nextsib( parm1);
  }

  /**
   * @param parm1
   *
   */
  public int getNextSibling( int parm1 )
  {
    if (DEBUG) System.out.println("getNextSibling("+parm1+")");
    return super.getNextSibling( parm1);
  }


  /**
   *
   */
  public boolean getDocumentAllDeclarationsProcessed( )
  {
    if (DEBUG) System.out.println("getDocAllDeclProc()");
    return super.getDocumentAllDeclarationsProcessed();
  }

  /**
   * @param parm1
   *
   */
  public int getParent( int parm1 )
  {
    if (DEBUG) System.out.println("getParent("+parm1+")");
    return super.getParent( parm1);
  }

  /**
   * @param parm1
   * @param parm2
   * @param parm3
   *
   */
  public int getExpandedTypeID( String parm1, String parm2, int parm3 )
  {
    if (DEBUG) System.out.println("getExpandedTypeID()");
    return super.getExpandedTypeID( parm1,  parm2,  parm3);
  }

  /**
   * @param parm1
   *
   */
  public void setDocumentBaseURI( String parm1 )
  {
    if (DEBUG) System.out.println("setDocBaseURI()");
    super.setDocumentBaseURI( parm1);
  }

  /**
   * @param parm1
   * @param parm2
   * @param parm3
   *
   */
  public char[] getStringValueChunk( int parm1, int parm2, int[] parm3 )
  {
    if (DEBUG)
    {
      System.out.println("getStringChunkValue(" +
      parm1 + "," +
      parm2 + ")");
    }
    return super.getStringValueChunk( parm1,  parm2,  parm3);
  }

  /**
   * @param parm1
   *
   */
  public DTMAxisTraverser getAxisTraverser( int parm1 )
  {
    if (DEBUG) System.out.println("getAxixTraverser("+parm1+")");
    return super.getAxisTraverser( parm1);
  }

  /**
   * @param parm1
   * @param parm2
   *
   */
  public DTMAxisIterator getTypedAxisIterator( int parm1, int parm2 )
  {
    if (DEBUG) System.out.println("getTypedAxisIterator("+parm1+","+parm2+")");
    return super.getTypedAxisIterator( parm1,  parm2);
  }

  /**
   * @param parm1
   *
   */
  public DTMAxisIterator getAxisIterator( int parm1 )
  {
    if (DEBUG) System.out.println("getAxisIterator("+parm1+")");
    return super.getAxisIterator( parm1);
  }
  /**
   * @param parm1
   *
   */
  public int getElementById( String parm1 )
  {
    if (DEBUG) System.out.println("getElementByID("+parm1+")");
    return DTM.NULL;
  }

  /**
   *
   */
  public DeclHandler getDeclHandler( )
  {
    if (DEBUG) System.out.println("getDeclHandler()");
    return null;
  }

  /**
   *
   */
  public ErrorHandler getErrorHandler( )
  {
    if (DEBUG) System.out.println("getErrorHandler()");
    return null;
  }

  /**
   *
   */
  public String getDocumentTypeDeclarationSystemIdentifier( )
  {
    if (DEBUG) System.out.println("get_DTD-SID()");
    return null;
  }


}
