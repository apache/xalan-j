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

import java.util.Hashtable;
//import java.util.Stack;
import java.util.Vector;

import org.apache.xml.dtm.ChunkedIntArray;
import org.apache.xml.utils.FastStringBuffer;

import org.xml.sax.Attributes;

/**
 * This is the implementation of the DTM document interface.  It receives
 * requests from an XML content handler similar to that of an XML DOM or SAX parser
 * to store information from the xml document in an array based
 * dtm table structure.  This informtion is used later for document navigation,
 * query, and SAX event dispatch functions. The DTM can also be used directly as a
 * document composition model for an application.  The requests received are:
 * <ul>
 * <li>initiating DTM to set the doc handle</li>
 * <li>resetting DTM for data structure reuse</li>
 * <li>hinting the end of document to adjust the end of data structure pointers</li>
 * <li>createnodes (element, comment, text, attribute, ....)</li>
 * <li>hinting the end of an element to patch parent and siblings<li>
 * <li>setting application provided symbol name stringpool data structures</li>
 * </ul>
 * <p>State: In progress!!</p>
 *
 * <p>Origin: the implemention is a composite logic based on the DTM of XalanJ1 and
 *     DocImpl, DocumentImpl, ElementImpl, TextImpl, etc. of XalanJ2</p>
 */
public class DTMDocumentImpl implements DTM {

	// Number of lower bits used to represent node index.
	protected static final byte DOCHANDLE_SHIFT = 20;
	// Masks the lower order of node handle.
	// Same as {@link DTMConstructor.IDENT_NODE_DEFAULT}
	protected static final int NODEHANDLE_MASK = (1 << (DOCHANDLE_SHIFT + 1)) - 1; 
	// Masks the higher order Document handle
	// Same as {@link DTMConstructor.IDENT_DOC_DEFAULT}
	protected static final int DOCHANDLE_MASK = -1 - NODEHANDLE_MASK;

	int m_docHandle = NULL;		 // masked document handle for this dtm document
	int m_docElement = NULL;	 // nodeHandle to the root of the actual dtm doc content

	// Context for parse-and-append operations
	int currentParent = 0;			// current parent - default is document root
	int previousSibling = 0;		// previous sibling - no previous sibling
	protected int m_currentNode = -1;		// current node

        // The tree under construction can itself be used as
        // the element stack, so m_elemStack isn't needed.
	//protected Stack m_elemStack = new Stack();	 // element stack

	private boolean previousSiblingWasParent = false;
	// Local cache for record-at-a-time fetch
	int gotslot[] = new int[4];

	// endDocument recieved?
	private boolean done = false;
	boolean m_isError = false;

	private final boolean DEBUG = false;

	// ========= DTM data structure declarations. ==============

	// nodes array: integer array blocks to hold the first level reference of the nodes,
	// each reference slot is addressed by a nodeHandle index value.
	// Assumes indices are not larger than {@link NODEHANDLE_MASK}
        // ({@link DOCHANDLE_SHIFT} bits).
	ChunkedIntArray nodes = new ChunkedIntArray(4);

	// text/comment table: string buffer to hold the text string values of the document,
	// each of which is addressed by the absolute offset and length in the buffer
	private FastStringBuffer m_char = new FastStringBuffer();

	// node name table, name space table, attribute name table, and prefix name table
	// are can be defined as DTMStringPool(s).  But may be substituted with better data
	// structure that can support the DTMStringPool interface in the future.

	private DTMStringPool m_elementNames = new DTMStringPool();
	private DTMStringPool m_nsNames = new DTMStringPool();
	private DTMStringPool m_attributeNames = new DTMStringPool();
	private DTMStringPool m_prefixNames = new DTMStringPool();

	// ###jjk m_expandedNames is not needed, as far as I can tell,
	// since expanded name indices are currently defined as L bits
	// of localname index, N bits of namespace index, and
	// (possibly) T bits of node type (current proposal is L=14
	// N=14 T=4).  In that setup, it's probably best to index into
	// the localname and namespace pools and reconstruct the
	// string form if and only if it is actually called for --
	// which will be _extremely_ rare -- rather than storing it in
	// Yet Another String Pool.
	   // private DTMStringPool m_expandedNames = new DTMStringPool(); //###zaj

	/**
	 * Construct a DTM.
	 *
	 * %REVIEW% Do we really want to support a no-arguments constructor
	 * defaulting to document number 0? Or do we want to insist the
	 * document ID number always be supplied, and let the caller pass 0
	 * if that's really what they intend? The latter seems safer.
	 */
	public DTMDocumentImpl(){
		initDocument(0);		 // clear nodes and document handle
	}

	/**
	 * Wrapper for ChunkedIntArray.append, to automatically update the
	 * previous sibling's "next" reference (if necessary) and periodically
	 * wake a reader who may have encountered incomplete data and entered
	 * a wait state.
	 * @param w0 int As in ChunkedIntArray.append
	 * @param w1 int As in ChunkedIntArray.append
	 * @param w2 int As in ChunkedIntArray.append
	 * @param w3 int As in ChunkedIntArray.append
	 * @return int As in ChunkedIntArray.append
	 * @see ChunkedIntArray.append
	 */
	private final int appendNode(int w0, int w1, int w2, int w3)
	{
		// A decent compiler will probably inline this.
	        // %REVIEW% jjk Do we want to rely on "a decent JIT compiler"?
		int slotnumber = nodes.appendSlot(w0, w1, w2, w3);

		if (DEBUG) System.out.println(slotnumber+": "+w0+" "+w1+" "+w2+" "+w3);

		if (previousSiblingWasParent)
			nodes.writeEntry(previousSibling,2,slotnumber);

		previousSiblingWasParent = false;	// Set the default; endElement overrides

		return slotnumber;
	}

	// ========= DTM Implementation Control Functions. ==============

	/**
	 * Set a suggested parse block size for the parser.
	 *
	 * @param blockSizeSuggestion Suggested size of the parse blocks, in bytes.
	 */
	public void setParseBlockSize(int blockSizeSuggestion) {};

	/**
	 * Set an implementation dependent feature.
	 * <p>
	 * %REVIEW% Do we really expect to set features on DTMs?
	 *
	 * @param featureId A feature URL.
	 * @param state true if this feature should be on, false otherwise.
	 */
	public void setFeature(String featureId, boolean state) {};

	/**
	 * Set a reference pointer to the element name symbol table.
	 * %REVIEW% Should this really be Public? Changing it while
	 * DTM is in use would be a disaster.
	 *
	 * @param poolRef DTMStringPool reference to an instance of table.
	 */
	public void setElementNameTable(DTMStringPool poolRef) {
		m_elementNames = poolRef;
	}

        /**
	 * Get a reference pointer to the element name symbol table.
	 *
	 * @return DTMStringPool reference to an instance of table.
	 */
        public DTMStringPool getElementNameTable() {
                 return m_elementNames;
         }

	/**
	 * Set a reference pointer to the namespace URI symbol table.
	 * %REVIEW% Should this really be Public? Changing it while
	 * DTM is in use would be a disaster.
	 *
	 * @param poolRef DTMStringPool reference to an instance of table.
	 */
	public void setNsNameTable(DTMStringPool poolRef) {
		m_nsNames = poolRef;
	}

        /**
	 * Get a reference pointer to the namespace URI symbol table.
	 *
	 * @return DTMStringPool reference to an instance of table.
	 */
        public DTMStringPool getNsNameTable() {
                 return m_nsNames;
         }

	/**
	 * Set a reference pointer to the attribute name symbol table.
	 * %REVIEW% Should this really be Public? Changing it while
	 * DTM is in use would be a disaster.
	 *
	 * @param poolRef DTMStringPool reference to an instance of table.
	 */
	public void setAttributeNameTable(DTMStringPool poolRef) {
		m_attributeNames = poolRef;
	}

        /**
	 * Get a reference pointer to the attribute name symbol table.
	 *
	 * @return DTMStringPool reference to an instance of table.
	 */
        public DTMStringPool getAttributeNameTable() {
                 return m_attributeNames;
         }

	/**
	 * Set a reference pointer to the prefix name symbol table.
	 * %REVIEW% Should this really be Public? Changing it while
	 * DTM is in use would be a disaster.
	 *
	 * @param poolRef DTMStringPool reference to an instance of table.
	 */
	public void setPrefixNameTable(DTMStringPool poolRef) {
		m_prefixNames = poolRef;
	}

	/**
	 * Get a reference pointer to the prefix name symbol table.
	 *
	 * @return DTMStringPool reference to an instance of table.
	 */
	public DTMStringPool getPrefixNameTable() {
		return m_prefixNames;
	}

	/**
	 * Set a reference pointer to the expanded name symbol table.
	 *
	 * @param poolRef DTMStringPool reference to an instance of table.
	 */
	//###zaj
	//### jjk see earlier discussion; this appears superfluous.
	  //   	public void setExpandedNameTable(DTMStringPool poolRef) {
	  //		m_expandedNames = poolRef;
	  //	}


         /**
          * Set a reference pointer to the content-text repository
          *
          * @param bufferRef FastStringBuffer reference to an instance of
          * buffer
          */
         void setContentBuffer(FastStringBuffer buffer) {
                 m_char = buffer;
         }
 
         /**
          * Get a reference pointer to the content-text repository
          *
          * @return FastStringBuffer reference to an instance of buffer
          */
         void getContentBuffer() {
                 return m_char;
         }





	// ========= Document Handler Functions =========
        // %TBD% jjk -- DocumentHandler is SAX Level 1, and should
        // be phased out in favor of ContentHandler/LexicalHandler

	/**
	 * Receive notification of the beginning of a dtm document.
	 *
	 * The DTMManager will invoke this method when the dtm is created.
	 *
	 * %REVIEW% Given the way getDocument() is currently coded,
	 * the docHandle parameter is apparently supposed to be the
	 * document number pre-shifted up into the high bits. Do we
	 * really want to require that, or should we accept the
	 * document number instead and shift it for them?
	 *
	 * @param docHandle int the handle for the DTM document.
	 */
	final void initDocument(int docHandle)
	{
		// save masked DTM document handle
		m_docHandle = docHandle;
		// Initialize the doc -- no parent, no next-sib
		nodes.writeSlot(0,DOCUMENT_NODE,-1,-1,0);
		// wait for the first startElement to create the doc root node
		done = false;
	}

	/**
	 * Receive hint of the end of a document.
	 *
	 * <p>The content handler will invoke this method only once, and it will
	 * be the last method invoked during the parse.  The handler shall not
	 * not invoke this method until it has either abandoned parsing
	 * (because of an unrecoverable error) or reached the end of
	 * input.</p>
	 */
	public void documentEnd()
	{
		done = true;
		// %TBD% may need to notice the last slot number and slot count to avoid
		// residual data from provious use of this DTM
	}

	/**
	 * Receive notification of the beginning of a document.
	 *
	 * <p>The SAX parser will invoke this method only once, before any
	 * other methods in this interface.</p>
	 */
	public void reset()
	{

		// %TBD% reset slot 0 to indicate ChunkedIntArray reuse or wait for
		//       the next initDocument().
		m_docElement = NULL;	 // reset nodeHandle to the root of the actual dtm doc content
		initDocument(0);
	}

	/**
	 * Factory method; creates an Element node in this document.
	 *
	 * The node created will be chained according to its natural order of request
	 * received.  %TBD% It can be rechained later via the optional DTM writable interface.
	 *
	 * <p>The XML content handler will invoke endElement() method after all
	 * of the element's content are processed in order to give DTM the indication
	 * to prepare and patch up parent and sibling node pointers.</p>
	 *
	 * <p>The following interface for createElement will use an index value corresponds
	 * to the symbol entry in the DTMDStringPool based symbol tables.</p>
	 *
	 * @param nsIndex The namespace of the node
	 * @param nameIndex The element name.
	 * @see #endElement
	 * @see org.xml.sax.Attributes
	 * @return nodeHandle int of the element created
	 */
	public int createElement(int nsIndex, int nameIndex, Attributes atts)
	{
		// do document root node creation here on the first element, create nodes for
		// this element and its attributes, store the element, namespace, and attritute
		// name indexes to the nodes array, keep track of the current node and parent
		// element used

		// W0  High:  Namespace  Low:  Node Type
		int w0 = (nsIndex << 16) | ELEMENT_NODE;
		// W1: Parent
		int w1 = currentParent;
		// W2: Next  (initialized as 0)
		int w2 = 0;
		// W3: Tagname
		int w3 = nameIndex;
		//int ourslot = nodes.appendSlot(w0, w1, w2, w3);
		int ourslot = appendNode(w0, w1, w2, w3);
		currentParent = ourslot;
		previousSibling = 0;
		setAttributes(atts);

		// set the root element pointer when creating the first element node
		if (m_docElement == NULL)
			m_docElement = ourslot;
		return (m_docHandle | ourslot);
	}

	// Factory method to create an Element node not associated with a given name space
	// using String value parameters passed in from a content handler or application
	/**
	 * Factory method; creates an Element node not associated with a given name space in this document.
	 *
	 * The node created will be chained according to its natural order of request
	 * received.  %TBD% It can be rechained later via the optional DTM writable interface.
	 *
	 * <p>The XML content handler or application will invoke endElement() method after all
	 * of the element's content are processed in order to give DTM the indication
	 * to prepare and patch up parent and sibling node pointers.</p>
	 *
	 * <p>The following parameters for createElement contains raw string values for name
	 * symbols used in an Element node.</p>
	 *
	 * @param name String the element name, including the prefix if any.
	 * @param atts The attributes attached to the element, if any.
	 * @see #endElement
	 * @see org.xml.sax.Attributes
	 */
	public int createElement(String name, Attributes atts)
	{
		// This method wraps around the index valued interface of the createElement interface.
		// The raw string values are stored into the current DTM name symbol tables.  The method
		// method will then use the index values returned to invoke the other createElement()
		// onverted to index values modified to match a
		// method.
		int nsIndex = NULL;
		int nameIndex = m_elementNames.stringToIndex(name);
		// note - there should be no prefix separator in the name because it is not associated
		// with a name space

		return createElement(nsIndex, nameIndex, atts);
	}

	// Factory method to create an Element node associated with a given name space
	// using String value parameters passed in from a content handler or application
	/**
	 * Factory method; creates an Element node associated with a given name space in this document.
	 *
	 * The node created will be chained according to its natural order of request
	 * received.  %TBD% It can be rechained later via the optional DTM writable interface.
	 *
	 * <p>The XML content handler or application will invoke endElement() method after all
	 * of the element's content are processed in order to give DTM the indication
	 * to prepare and patch up parent and sibling node pointers.</p>
	 *
	 * <p>The following parameters for createElementNS contains raw string values for name
	 * symbols used in an Element node.</p>
	 *
	 * @param ns String the namespace of the node
	 * @param name String the element name, including the prefix if any.
	 * @param atts The attributes attached to the element, if any.
	 * @see #endElement
	 * @see org.xml.sax.Attributes
	 */
	public int createElementNS(String ns, String name, Attributes atts)
	{
		// This method wraps around the index valued interface of the createElement interface.
		// The raw string values are stored into the current DTM name symbol tables.  The method
		// method will then use the index values returned to invoke the other createElement()
		// onverted to index values modified to match a
		// method.
		int nsIndex = m_nsNames.stringToIndex(ns);
		int nameIndex = m_elementNames.stringToIndex(name);
		// The prefixIndex is not needed by the indexed interface of the createElement method
		int prefixSep = name.indexOf(":");
		int prefixIndex = m_prefixNames.stringToIndex(name.substring(0, prefixSep));
		return createElement(nsIndex, nameIndex, atts);
	}

	/**
	 * Receive an indication for the end of an element.
	 *
	 * <p>The XML content handler will invoke this method at the end of every
	 * element in the XML document to give hint its time to pop up the current
	 * element and parent and patch up parent and sibling pointers if necessary
	 *
	 * <p>%tbd% The following interface may need to be modified to match a
	 * coordinated access to the DTMDStringPool based symbol tables.</p>
		 *
	 * @param ns the namespace of the element
	 * @param localName The local part of the qualified name of the element
	 * @param name The element name
	 */
	public void endElement(String ns, String name)
	{ 
		// pop up the stacks

		// 
		if (previousSiblingWasParent)
			nodes.writeEntry(previousSibling, 2, NULL);

		// Pop parentage 
		previousSibling = currentParent;
		nodes.readSlot(currentParent, gotslot);
		currentParent = gotslot[1] & 0xFFFF;

		// The element just being finished will be
		// the previous sibling for the next operation
		previousSiblingWasParent = true;

		// Pop a level of namespace table
		// namespaceTable.removeLastElem();
	}

	/**
	 * Creates attributes for the current node.
	 *
	 * @param atts Attributes to be created.
	 */
	void setAttributes(Attributes atts) {
		int atLength = (null == atts) ? 0 : atts.getLength();
		for (int i=0; i < atLength; i++) {
			String qname = atts.getQName(i);
			createAttribute(atts.getQName(i), atts.getValue(i));
		}
	}

	/**
	 * Appends an attribute to the document.
	 * @param qname Qualified Name of the attribute
	 * @param value Value of the attribute
	 * @return Handle of node
	 */
	public int createAttribute(String qname, String value) {
		int colonpos = qname.indexOf(":");
		String attName = qname.substring(colonpos+1);
		int w0 = 0;
		if (colonpos > 0) {
			String prefix = qname.substring(0, colonpos);
			if (prefix.equals("xml")) {
				//w0 = ATTRIBUTE_NODE | 
				//	(org.apache.xalan.templates.Constants.S_XMLNAMESPACEURI << 16);
			} else {
				//w0 = ATTRIBUTE_NODE | 
			}
		} else {
			w0 = ATTRIBUTE_NODE;
		}
		// W1:  Parent
		int w1 = currentParent;
		// W2:  Next (not yet resolved)
		int w2 = 0;
		// W3:  Tag name
		int w3 = m_attributeNames.stringToIndex(attName);
		// Add node
		int ourslot = appendNode(w0, w1, w2, w3);
		previousSibling = ourslot;	// Should attributes be previous siblings

		// W0: Node Type
		w0 = TEXT_NODE;
		// W1: Parent
		w1 = ourslot;
		// W2: Start Position within buffer
		w2 = m_char.length();
		m_char.append(value);
		// W3: Length
		w3 = m_char.length() - w2;
		appendNode(w0, w1, w2, w3);
		charStringStart=m_char.length();
		charStringLength = 0;
		//previousSibling = ourslot;
		// Attrs are Parents
		previousSiblingWasParent = true;
		return (m_docHandle | ourslot);
	}

	/**
	 * Factory method; creates a Text node in this document.
	 *
	 * The node created will be chained according to its natural order of request
	 * received.  %TBD% It can be rechained later via the optional DTM writable interface.
	 *
	 * @param text String The characters text string from the XML document.
	 * @return int DTM node-number of the text node created
	 */
	public int createTextNode(String text)
	throws DTMException
	{ 
		// wraps around the index value based createTextNode method
		return createTextNode(text.toCharArray(), 0, text.length());
	}

	/**
	 * Factory method; creates a Text node in this document.
	 *
	 * The node created will be chained according to its natural order of request
	 * received.  %TBD% It can be rechained later via the optional DTM writable interface.
	 *
	 * %REVIEW% for text normalization issues, unless we are willing to
	 * insist that all adjacent text must be merged before this method
	 * is called.
	 *
	 * @param ch The characters from the XML document.
	 * @param start The start position in the array.
	 * @param length The number of characters to read from the array.
	 */
	public int createTextNode(char ch[], int start, int length)
	throws DTMException
	{
		m_char.append(ch, start, length);		// store the chunk to the text/comment string table

		// create a Text Node
		// %TBD% may be possible to combine with appendNode()to replace the next chunk of code
		int w0 = TEXT_NODE;
		// W1: Parent
		int w1 = currentParent;
		// W2: Start position within m_char
		int w2 = charStringStart;
		// W3: Length of the full string
		int w3 = length;
		int ourslot = appendNode(w0, w1, w2, w3);
		previousSibling = ourslot;

		charStringStart=m_char.length();
		charStringLength = 0;
		return (m_docHandle | ourslot);
	}

	/**
	 * Factory method; creates a Comment node in this document.
	 *
	 * The node created will be chained according to its natural order of request
	 * received.  %TBD% It can be rechained later via the optional DTM writable interface.
	 *
	 * @param text String The characters text string from the XML document.
	 * @return int DTM node-number of the text node created
	 */
	public int createComment(String text)
	throws DTMException
	{
		// wraps around the index value based createTextNode method
		return createComment(text.toCharArray(), 0, text.length());
	}

	/**
	 * Factory method; creates a Comment node in this document.
	 *
	 * The node created will be chained according to its natural order of request
	 * received.  %TBD% It can be rechained later via the optional DTM writable interface.
	 *
	 * @param ch An array holding the characters in the comment.
	 * @param start The starting position in the array.
	 * @param length The number of characters to use from the array.
	 * @see DTMException
	 */
	public int createComment(char ch[], int start, int length)
	throws DTMException
	{
		m_char.append(ch, start, length);		// store the comment string to the text/comment string table

		// create a Comment Node
		// %TBD% may be possible to combine with appendNode()to replace the next chunk of code
		int w0 = COMMENT_NODE;
		// W1: Parent
		int w1 = currentParent;
		// W2: Start position within m_char
		int w2 = charStringStart;
		// W3: Length of the full string
		int w3 = length;
		int ourslot = appendNode(w0, w1, w2, w3);
		previousSibling = ourslot;

		charStringStart=m_char.length();
		charStringLength = 0;
		return (m_docHandle | ourslot);
	}

	// Counters to keep track of the current text string being accumulated with respect
	// to the text/comment string table: charStringStart should point to the starting
	// offset of the string in the table and charStringLength the acccumulated length when
	// appendAccumulatedText starts, and reset to the end of the table and 0 at the end
	// of appendAccumulatedText for the next set of characters receives
	int charStringStart=0,charStringLength=0;

	// ========= Document Navigation Functions =========

	/** Given a node handle, test if it has child nodes.
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
	public boolean hasChildNodes(int nodeHandle) {
		return(getFirstChild(nodeHandle) != NULL);
	}

	/**
	 * Given a node handle, get the handle of the node's first child.
	 * If not yet resolved, waits for more nodes to be added to the document and
	 * tries again.
	 *
	 * @param nodeHandle int Handle of the node.
	 * @return int DTM node-number of first child, or DTM.NULL to indicate none exists.
	 */
	public int getFirstChild(int nodeHandle) {
		// ###shs worry about tracing/debug later
		nodeHandle &= NODEHANDLE_MASK;
		// Read node into variable
		nodes.readSlot(nodeHandle, gotslot);

		// type is the last half of first slot
		short type = (short) (gotslot[0] & 0xFFFF);

		// Check to see if Element or Document node
		if ((type == ELEMENT_NODE) || (type == DOCUMENT_NODE) || 
				(type == ENTITY_REFERENCE_NODE)) {
			// In case when Document root is given
			if (nodeHandle == 0) nodeHandle = 1;
			int kid = nodeHandle + 1;
			nodes.readSlot(kid, gotslot);
			while (ATTRIBUTE_NODE == (gotslot[0] & 0xFFFF)) {
				// points to next sibling
				kid = gotslot[2];
				// Return NULL if node has only attributes
				if (kid == NULL) return NULL; 
				nodes.readSlot(kid, gotslot);
			}
			// If parent slot matches given parent, return kid
			if (gotslot[1] == nodeHandle)	return kid | m_docHandle;
		}
		// No child found
		return NULL;
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
	public int getLastChild(int nodeHandle) {
		// ###shs put trace/debug later
		nodeHandle &= NODEHANDLE_MASK;
		// do not need to test node type since getFirstChild does that
		int lastChild = NULL;
		for (int nextkid = getFirstChild(nodeHandle); nextkid != NULL;
				nextkid = getNextSibling(nextkid)) {
			lastChild = nextkid;
		}
		return lastChild | m_docHandle;		
	}

	/**
	 * Retrieves an attribute node by by qualified name and namespace URI.
	 *
	 * @param nodeHandle int Handle of the node upon which to look up this attribute.
	 * @param namespaceURI The namespace URI of the attribute to
	 *   retrieve, or null.
	 * @param name The local name of the attribute to
	 *   retrieve.
	 * @return The attribute node handle with the specified name (
	 *   <code>nodeName</code>) or <code>DTM.NULL</code> if there is no such
	 *   attribute.
	 */
	public int getAttributeNode(int nodeHandle, String namespaceURI, String name) {
		int nsIndex = m_nsNames.stringToIndex(namespaceURI),
									nameIndex = m_attributeNames.stringToIndex(name);
		nodeHandle &= NODEHANDLE_MASK;
		nodes.readSlot(nodeHandle, gotslot);
		short type = (short) (gotslot[0] & 0xFFFF);
		// If nodeHandle points to element next slot would be first attribute
		if (type == ELEMENT_NODE)
			nodeHandle++;
		// Iterate through Attribute Nodes
		while (type == ATTRIBUTE_NODE) {
			if ((nsIndex == (gotslot[0] << 16)) && (gotslot[3] == nameIndex))
				return nodeHandle | m_docHandle;
			// Goto next sibling
			nodeHandle = gotslot[2];
			nodes.readSlot(nodeHandle, gotslot);
		}
		return NULL;
	}

	/**
	 * Given a node handle, get the index of the node's first attribute.
	 *
	 * @param nodeHandle int Handle of the Element node.
	 * @return Handle of first attribute, or DTM.NULL to indicate none exists.
	 */
	public int getFirstAttribute(int nodeHandle) {
		nodeHandle &= NODEHANDLE_MASK;

		// %REVIEW% jjk: Just a quick observation: If you're going to
		// call readEntry repeatedly on the same node, it may be
		// more efficiently to do a readSlot to get the data locally,
		// reducing the addressing and call-and-return overhead.

		// Should we check if handle is element (do we want sanity checks?)
		if (ELEMENT_NODE != (nodes.readEntry(nodeHandle, 0) & 0xFFFF))
			return NULL;
		// First Attribute (if any) should be at next position in table
		nodeHandle++;
		return(ATTRIBUTE_NODE == (nodes.readEntry(nodeHandle, 0) & 0xFFFF)) ? 
		nodeHandle | m_docHandle : NULL;
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
	public int getFirstNamespaceNode(int nodeHandle, boolean inScope) {

		return NULL;
	}

	/**
	 * Given a node handle, advance to its next sibling.
	 * %TBD% Remove - If not yet resolved, waits for more nodes to be added to the document and
	 * tries again.
	 * @param nodeHandle int Handle of the node.
	 * @return int Node-number of next sibling,
	 * or DTM.NULL to indicate none exists.
	 */
	public int getNextSibling(int nodeHandle) {
		nodeHandle &= NODEHANDLE_MASK;
		// Document root has no next sibling
		if (nodeHandle == 0)
			return NULL;

		short type = (short) (nodes.readEntry(nodeHandle, 0) & 0xFFFF);
		if ((type == ELEMENT_NODE) || (type == ATTRIBUTE_NODE) ||
				(type == ENTITY_REFERENCE_NODE)) {
			int nextSib = nodes.readEntry(nodeHandle, 2);
			if (nextSib == NULL)
				return NULL;
			if (nextSib != 0)
				return (m_docHandle | nextSib);
			// ###shs should cycle/wait if nextSib is 0? Working on threading next
		}
		// Next Sibling is in the next position if it shares the same parent
		int thisParent = nodes.readEntry(nodeHandle, 1);
		
		// %REVIEW% jjk: Old code was reading from nodehandle+1.
		// That would be ++nodeHandle, not nodeHandle++. Check this!
		if (nodes.readEntry(nodeHandle++, 1) == thisParent)
			return (m_docHandle | nodeHandle);

		return NULL;
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
	public int getPreviousSibling(int nodeHandle) {
		nodeHandle &= NODEHANDLE_MASK;
		// Document root has no previous sibling
		if (nodeHandle == 0)
			return NULL;

		int parent = nodes.readEntry(nodeHandle, 1);
		int kid = NULL;
		for (int nextkid = getFirstChild(parent); nextkid != nodeHandle;
				nextkid = getNextSibling(nextkid)) {
			kid = nextkid;
		}
		return kid | m_docHandle;
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
	public int getNextAttribute(int nodeHandle) {
		nodeHandle &= NODEHANDLE_MASK;
		nodes.readSlot(nodeHandle, gotslot);

		//%REVIEW% Why are we using short here? There's no storage
		//reduction for an automatic variable, especially one used
		//so briefly, and it typically costs more cycles to process
		//than an int would.
		short type = (short) (gotslot[0] & 0xFFFF);

		if (type == ELEMENT_NODE) {
			return getFirstAttribute(nodeHandle);
		} else if (type == ATTRIBUTE_NODE) {
			if (gotslot[2] != NULL)
				return (m_docHandle | gotslot[2]);
		}
		return NULL;
	}

	/**
	 * Given a namespace handle, advance to the next namespace.
	 *
	 * %TBD% THIS METHOD DOES NOT MATCH THE CURRENT SIGNATURE IN
	 * THE DTM INTERFACE.  FIX IT, OR JUSTIFY CHANGING THE DTM
	 * API.
	 *
	 * @param namespaceHandle handle to node which must be of type NAMESPACE_NODE.
	 * @return handle of next namespace, or DTM.NULL to indicate none exists.
	 */
	public int getNextNamespaceNode(int namespaceHandle, boolean inScope) {
		// ###shs need to work on namespace
		return NULL;
	}

	/**
	 * Given a node handle, advance to its next descendant.
	 * If not yet resolved, waits for more nodes to be added to the document and
	 * tries again.
	 *
	 * @param subtreeRootNodeHandle
	 * @param nodeHandle int Handle of the node.
	 * @return handle of next descendant,
	 * or DTM.NULL to indicate none exists.
	 */
	public int getNextDescendant(int subtreeRootHandle, int nodeHandle) {
		subtreeRootHandle &= NODEHANDLE_MASK;
		nodeHandle &= NODEHANDLE_MASK;
		// Document root [Document Node? -- jjk] - no next-sib
		if (nodeHandle == 0)
			return NULL;
		while (!m_isError) {
			// Document done and node out of bounds
			if (done && (nodeHandle > nodes.slotsUsed()))
				break;
			if (nodeHandle > subtreeRootHandle) {
				nodes.readSlot(nodeHandle+1, gotslot);
				if (gotslot[2] != 0) {
					short type = (short) (gotslot[0] & 0xFFFF);
					if (type == ATTRIBUTE_NODE) {
						nodeHandle +=2;
					} else {
						int nextParentPos = gotslot[1];
						if (nextParentPos >= subtreeRootHandle)
							return (m_docHandle | (nodeHandle+1));
						else
							break;
					}
				} else if (!done) {
					// Add wait logic here
				} else
					break;
			} else {
				nodeHandle++;
			}
		}
		// Probably should throw error here like original instead of returning
		return NULL;
	}

	/**
	 * Given a node handle, advance to the next node on the following axis.
	 *
	 * @param axisContextHandle the start of the axis that is being traversed.
	 * @param nodeHandle
	 * @return handle of next sibling,
	 * or DTM.NULL to indicate none exists.
	 */
	public int getNextFollowing(int axisContextHandle, int nodeHandle) {
		//###shs still working on
		return NULL;
	}

	/**
	 * Given a node handle, advance to the next node on the preceding axis.
	 *
	 * @param axisContextHandle the start of the axis that is being traversed.
	 * @param nodeHandle the id of the node.
	 * @return int Node-number of preceding sibling,
	 * or DTM.NULL to indicate none exists.
	 */
	public int getNextPreceding(int axisContextHandle, int nodeHandle) {
		// ###shs copied from Xalan 1, what is this suppose to do?
		nodeHandle &= NODEHANDLE_MASK;
		while (nodeHandle > 1) {
			nodeHandle--;
			if (ATTRIBUTE_NODE == (nodes.readEntry(nodeHandle, 0) & 0xFFFF))
				continue;
			return (m_docHandle | nodes.specialFind(axisContextHandle, nodeHandle));
		}
		return NULL;
	}

	/**
	 * Given a node handle, find its parent node.
	 *
	 * @param nodeHandle the id of the node.
	 * @return int Node-number of parent,
	 * or DTM.NULL to indicate none exists.
	 */
	public int getParent(int nodeHandle) {
		// Should check to see within range?

		// Document Root should not have to be handled differently
		return (m_docHandle | nodes.readEntry(nodeHandle, 1));
	}

	/**
	 * Returns the root element of the document.
	 * @return nodeHandle to the Document Root.
	 */
	public int getDocumentRoot() {
		return (m_docHandle | m_docElement);
	}

	/**
		* Given a node handle, find the owning document node.
		*
		* @param nodeHandle the id of the node.
		* @return int Node handle of document, which should always be valid.
		*/
	public int getDocument() {
		return m_docHandle;
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
	 * @return int Node handle of owning document, or NULL if the nodeHandle is
	 *             a document.
	 */
	public int getOwnerDocument(int nodeHandle) {
		// Assumption that Document Node is always in 0 slot
		if ((nodeHandle & NODEHANDLE_MASK) == 0)
			return NULL;
		return (nodeHandle & DOCHANDLE_MASK);
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
	public String getStringValue(int nodeHandle) {
	// ###zaj - researching 
	nodes.readSlot(nodeHandle, gotslot);
	int nodetype=gotslot[0] & 0xFF;		
	String value=null;

	switch (nodetype) {			
	case TEXT_NODE:   
	case COMMENT_NODE:
	case CDATA_SECTION_NODE: 
		value=m_char.getString(gotslot[2], gotslot[3]);		
		break;
	case PROCESSING_INSTRUCTION_NODE:
	case ATTRIBUTE_NODE:	
	case ELEMENT_NODE:
	case ENTITY_REFERENCE_NODE:
	default:
		break;
	}
	return value; 
	       
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
	//###zaj - tbd
	public int getStringValueChunkCount(int nodeHandle)
	{
		//###zaj    return value 
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
	//###zaj - tbd 
	public char[] getStringValueChunk(int nodeHandle, int chunkIndex,
																		int[] startAndLen) {return new char[0];}

	/**
	 * Given a node handle, return an ID that represents the node's expanded name.
	 *
	 * @param nodeHandle The handle to the node in question.
	 *
	 * @return the expanded-name id of the node.
	 */
	public int getExpandedNameID(int nodeHandle) {
	   nodes.readSlot(nodeHandle, gotslot);

           String qName = m_elementNames.indexToString(gotslot[3]); 
           // Remove prefix from localName
	   int colonpos = qName.indexOf(":");
	   String localName = qName.substring(colonpos+1);
	   // Get NS
           String namespace = m_nsNames.indexToString(gotslot[0] << 16); 
	   // Create expanded name
	   String expandedName = namespace + ":" + localName;
	   int expandedNameID = m_nsNames.stringToIndex(expandedName);

        return expandedNameID;
        }


	/**
	 * Given an expanded name, return an ID.  If the expanded-name does not
	 * exist in the internal tables, the entry will be created, and the ID will
	 * be returned.  Any additional nodes that are created that have this
	 * expanded name will use this ID.
	 *
	 * @param nodeHandle The handle to the node in question.
	 *
	 * @return the expanded-name id of the node.
	 */
	public int getExpandedNameID(String namespace, String localName) {
          
	   // Create expanded name
	   String expandedName = namespace + ":" + localName;
	   int expandedNameID = m_nsNames.stringToIndex(expandedName);

        return expandedNameID;
	}



	/**
	 * Given an expanded-name ID, return the local name part.
	 *
	 * @param ExpandedNameID an ID that represents an expanded-name.
	 * @return String Local name of this node.
	 */
	public String getLocalNameFromExpandedNameID(int ExpandedNameID) {

	   // Get expanded name
	   String expandedName = m_elementNames.indexToString(ExpandedNameID); 
	   // Remove prefix from expanded name
	   int colonpos = expandedName.indexOf(":");
	   String localName = expandedName.substring(colonpos+1);

        return localName;
	}


	/**
	 * Given an expanded-name ID, return the namespace URI part.
	 *
	 * @param ExpandedNameID an ID that represents an expanded-name.
	 * @return String URI value of this node's namespace, or null if no
	 * namespace was resolved.
	*/
	public String getNamespaceFromExpandedNameID(int ExpandedNameID) {

	   String expandedName = m_elementNames.indexToString(ExpandedNameID); 
	   // Remove local name from expanded name
	   int colonpos = expandedName.indexOf(":");
	   String nsName = expandedName.substring(0, colonpos);

	return nsName;
	}


	/**
	 * fixednames 
	*/ 
	static final String[] fixednames=
	{
		null,null,							// nothing, Element
		null,"#text",						// Attr, Text
		"#cdata_section",null,	// CDATA, EntityReference
		null,null,							// Entity, PI
		"#comment","#document",	// Comment, Document
		null,"#document-fragment", // Doctype, DocumentFragment
		null};									// Notation

	/**
	 * Given a node handle, return its DOM-style node name. This will
	 * include names such as #text or #document.
	 *
	 * @param nodeHandle the id of the node.
	 * @return String Name of this node, which may be an empty string.
	 * %REVIEW% Document when empty string is possible...
	 */
	public String getNodeName(int nodeHandle) {
		nodes.readSlot(nodeHandle, gotslot);
		short type = (short) (gotslot[0] & 0xFFFF);
		String name = fixednames[type];
		if (null == name) { 
			if (type == ELEMENT_NODE) 
				name = m_elementNames.indexToString(gotslot[3]);
			else if (type == ATTRIBUTE_NODE)
				name = m_attributeNames.indexToString(gotslot[3]);
		}
		return name;
	}

	/**
	 * Given a node handle, return the XPath node name.  This should be
	 * the name as described by the XPath data model, NOT the DOM-style
	 * name.
	 *
	 * @param nodeHandle the id of the node.
	 * @return String Name of this node.
	 */
	public String getNodeNameX(int nodeHandle) {return null;}

	/**
	 * Given a node handle, return its DOM-style localname.
	 * (As defined in Namespaces, this is the portion of the name after any
	 * colon character)
	 *
	 * @param nodeHandle the id of the node.
	 * @return String Local name of this node.
	 */
	public String getLocalName(int nodeHandle) {
		String name = getNodeName(nodeHandle);
		if (null != name) {
			int colonpos = name.indexOf(":");
			return (colonpos < 0) ? name : name.substring(colonpos+1);
		}
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
	public String getPrefix(int nodeHandle) {
		String name = getNodeName(nodeHandle);
		int colonpos = name.indexOf(":");
		return (colonpos < 0) ? "" : name.substring(0, colonpos);
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
	public String getNamespaceURI(int nodeHandle) {return null;}

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
		nodes.readSlot(nodeHandle, gotslot);
		int nodetype=gotslot[0] & 0xFF;		// ###zaj use mask to get node type
		String value=null;

		switch (nodetype) {			// ###zaj todo - document nodetypes
		case ATTRIBUTE_NODE:
			nodes.readSlot(nodeHandle+1, gotslot);
		case TEXT_NODE:   
		case COMMENT_NODE:
		case CDATA_SECTION_NODE: 
			value=m_char.getString(gotslot[2], gotslot[3]);		//###zaj
			break;
		case PROCESSING_INSTRUCTION_NODE:
		case ELEMENT_NODE:
		case ENTITY_REFERENCE_NODE:
		default:
			break;
		}
		return value; 
	}

	/**
	 * Given a node handle, return its DOM-style node type.
	 * <p>
	 * %REVIEW% Generally, returning short is false economy. Return int?
	 *
	 * @param nodeHandle The node id.
	 * @return int Node type, as per the DOM's Node._NODE constants.
	 */
	public short getNodeType(int nodeHandle) {
		return(short) (nodes.readEntry(nodeHandle, 0) & 0xFFFF);
	}

	/**
	 * <meta name="usage" content="internal"/>
	 * Get the depth level of this node in the tree (equals 1 for
	 * a parentless node).
	 *
	 * @param nodeHandle The node id.
	 * @return the number of ancestors, plus one
	 */
	public short getLevel(int nodeHandle) {
		short count = 0;
		while (nodeHandle != 0) {
			count++;
			nodeHandle = nodes.readEntry(nodeHandle, 1);
		}
		return count;
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
	 * @return Returns <code>true</code> if the specified feature is
	 *   supported on this node, <code>false</code> otherwise.
	 */
	public boolean isSupported(String feature,
														 String version) {return false;}

	/**
	 * Return the base URI of the document entity. If it is not known
	 * (because the document was parsed from a socket connection or from
	 * standard input, for example), the value of this property is unknown.
	 *
	 * @param nodeHandle The node id, which can be any valid node handle.
	 * @return the document base URI String object or null if unknown.
	 */
	public String getDocumentBaseURI(int nodeHandle) {return null;}

	/**
	 * Return the system identifier of the document entity. If
	 * it is not known, the value of this property is unknown.
	 *
	 * @param nodeHandle The node id, which can be any valid node handle.
	 * @return the system identifier String object or null if unknown.
	 */
	public String getDocumentSystemIdentifier(int nodeHandle) {return null;}

	/**
	 * Return the name of the character encoding scheme
	 *        in which the document entity is expressed.
	 *
	 * @param nodeHandle The node id, which can be any valid node handle.
	 * @return the document encoding String object.
	 */
	public String getDocumentEncoding(int nodeHandle) {return null;}

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
	public String getDocumentStandalone(int nodeHandle) {return null;}

	/**
	 * Return a string representing the XML version of the document. This
	 * property is derived from the XML declaration optionally present at the
	 * beginning of the document entity, and has no value if there is no XML
	 * declaration.
	 *
	 * @param the document handle
	 *
	 * @return the document version String object
	 */
	public String getDocumentVersion(int documentHandle) {return null;}

	/**
	 * Return an indication of
	 * whether the processor has read the complete DTD. Its value is a
	 * boolean. If it is false, then certain properties (indicated in their
	 * descriptions below) may be unknown. If it is true, those properties
	 * are never unknown.
	 *
	 * @return <code>true</code> if all declarations were processed {};
	 *         <code>false</code> otherwise.
	 */
	public boolean getDocumentAllDeclarationsProcessed() {return false;}

	/**
	 *   A document type declaration information item has the following properties:
	 *
	 *     1. [system identifier] The system identifier of the external subset, if
	 *        it exists. Otherwise this property has no value.
	 *
	 * @return the system identifier String object, or null if there is none.
	 */
	public String getDocumentTypeDeclarationSystemIdentifier() {return null;}

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
	public int getDocumentTypeDeclarationPublicIdentifier() {return 0;}

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
	public int getElementById(String elementId) {return 0;}

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
	public String getUnparsedEntityURI(String name) {return null;}


	// ============== Boolean methods ================

	/**
	 * Return true if the xsl:strip-space or xsl:preserve-space was processed
	 * during construction of the DTM document.
	 *
	 * <p>%REVEIW% Presumes a 1:1 mapping from DTM to Document, since
	 * we aren't saying which Document to query...?</p>
	 */
	public boolean supportsPreStripping() {return false;}

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
	 * @return false if node2 comes before node1, otherwise return true.
	 * You can think of this as
	 * <code>(node1.documentOrderPosition &lt;= node2.documentOrderPosition)</code>.
	 */
	public boolean isNodeAfter(int nodeHandle1, int nodeHandle2) {return false;}

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
	public boolean isCharacterElementContentWhitespace(int nodeHandle) {return false;}

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
	public boolean isDocumentAllDeclarationsProcessed(int documentHandle) {return false;}

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
	public boolean isAttributeSpecified(int attributeHandle) {return false;}

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
	throws org.xml.sax.SAXException {}

	/**
	 * Directly create SAX parser events from a subtree.
	 *
	 * @param nodeHandle The node ID.
	 * @param ch A non-null reference to a ContentHandler.
	 *
	 * @throws org.xml.sax.SAXException
	 */

	public void dispatchToEvents(int nodeHandle, org.xml.sax.ContentHandler ch)
	throws org.xml.sax.SAXException {}

	// ==== Construction methods (may not be supported by some implementations!) =====
	// %REVIEW% jjk: These probably aren't the right API. At the very least
	// they need to deal with current-insertion-location and end-element
	// issues.

	/**
	 * Append a child to the end of the child list of the current node. Please note that the node
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
	public void appendChild(int newChild, boolean clone, boolean cloneDepth) {
		boolean sameDoc = ((newChild & DOCHANDLE_MASK) == m_docHandle);
		if (clone || !sameDoc) {

		} else {

		}
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
	public void appendTextChild(String str) {
		// ###shs Think more about how this differs from createTextNode
		createTextNode(str);
	}


  // ==== BUILDER methods ====
  // %TBD% jjk: These are API sketches based on the assumption that the SAX
  // ContentHandler adapter code lives in the DTMBuilder object and
  // invokes these to actually construct the DTM nodes. An alternative
  // would be to move that code directly into this class and have those
  // methods construct the DTM directly. NOTE that it is assumed that the
  // Builder code and the DTM instance have already negotiated to share the
  // string pools/buffers, and that the Builder will accept full responsibility
  // for populating those -- including normalizing across consecutive blocks
  // of characters().

  /** Append a text child at the current insertion point. Assumes that the
   * actual content of the text has previously been appended to the m_char
   * buffer (shared with the builder).
   *
   * @param contentStart int Starting offset of node's content in m_char.
   * @param contentLength int Length of node's content in m_char.
   * */
  void appendTextChild(int contentStart,int contentLength)
  {
    // %TBD%
  }
  
  /** Append a comment child at the current insertion point. Assumes that the
   * actual content of the comment has previously been appended to the m_char
   * buffer (shared with the builder).
   *
   * @param contentStart int Starting offset of node's content in m_char.
   * @param contentLength int Length of node's content in m_char.
   * */
  void appendComment(int contentStart,int contentLength)
  {
    // %TBD%
  }
  
  
  /** Append an Element child at the current insertion point. This
   * Element then _becomes_ the insertion point; subsequent appends
   * become its lastChild until an appendEndElement() call is made.
   * 
   * Assumes that the symbols (local name, namespace URI and prefix)
   * have already been added to the pools
   *
   * @param namespaceIndex: Index within the namespaceURI string pool
   * @param localNameIndex Index within the local name string pool
   * @param prefixIndex: Index within the prefix string pool
   * */
  void startElement(int namespaceIndex,int localNameIndex, int prefixIndex)
  {
    // %TBD%
  }
  
  /** Append a Namespace Declaration child at the current insertion point.
   * Assumes that the symbols (namespace URI and prefix) have already been
   * added to the pools
   *
   * @param prefixIndex: Index within the prefix string pool
   * @param namespaceIndex: Index within the namespaceURI string pool
   * @param isID: If someone really insists on writing a bad DTD, it is
   * theoretically possible for a namespace declaration to also be declared
   * as being a node ID. I don't really want to support that stupidity,
   * but I'm not sure we can refuse to accept it.
   * */
  void appendNSDeclaration(int prefixIndex, int namespaceIndex,
                           boolean isID)
  {
    // %TBD%
  }

  /** Append a Namespace Declaration child at the current insertion
   * point.  Assumes that the symbols (namespace URI, local name, and
   * prefix) have already been added to the pools, and that the content has
   * already been appended to m_char. Note that the attribute's content has
   * been flattened into a single string; DTM does _NOT_ attempt to model
   * the details of entity references within attribute values.
   *
   * @param namespaceIndex int Index within the namespaceURI string pool
   * @param localNameIndex int Index within the local name string pool
   * @param prefixIndex int Index within the prefix string pool
   * @param isID boolean True if this attribute was declared as an ID
   * (for use in supporting getElementByID).
   * @param contentStart int Starting offset of node's content in m_char.
   * @param contentLength int Length of node's content in m_char.
   * */
  void appendAttribute(int namespaceIndex, int localNameIndex, int prefixIndex,
                       boolean isID,
                       int contentStart, int contentLength)
  {
    // %TBD%
  }
  


  /** Terminate the element currently acting as an insertion point. Subsequent
   * insertions will occur as the last child of this element's parent.
   * */
  void appendEndElement()
  {
    // %TBD%
  }
  
  /**  All appends to this document have finished; do whatever final
   * cleanup is needed. I expect this will actually be a no-op.
   * */
  void appendEndDocument()
  {
    // %TBD%
  }

}
