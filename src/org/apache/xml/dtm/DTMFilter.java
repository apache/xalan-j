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

/**
 * Simple filter for doing node tests.  Note the semantics of this are somewhat 
 * different that the DOM's NodeFilter.
 */
public interface DTMFilter
{

  // Constants for whatToShow.  These are used to set the node type that will 
  // be traversed.

  /**
   * Show all <code>Nodes</code>.
   */
  public static final int SHOW_ALL = 0xFFFFFFFF;

  /**
   * Show <code>Element</code> nodes.
   */
  public static final int SHOW_ELEMENT = 0x00000001;

  /**
   * Show <code>Attr</code> nodes. This is meaningful only when creating an
   * iterator or tree-walker with an attribute node as its
   * <code>root</code>; in this case, it means that the attribute node
   * will appear in the first position of the iteration or traversal.
   * Since attributes are never children of other nodes, they do not
   * appear when traversing over the document tree.
   */
  public static final int SHOW_ATTRIBUTE = 0x00000002;

  /**
   * Show <code>Text</code> nodes.
   */
  public static final int SHOW_TEXT = 0x00000004;

  /**
   * Show <code>CDATASection</code> nodes.
   */
  public static final int SHOW_CDATA_SECTION = 0x00000008;

  /**
   * Show <code>EntityReference</code> nodes.
   */
  public static final int SHOW_ENTITY_REFERENCE = 0x00000010;

  /**
   * Show <code>Entity</code> nodes. This is meaningful only when creating
   * an iterator or tree-walker with an<code> Entity</code> node as its
   * <code>root</code>; in this case, it means that the <code>Entity</code>
   *  node will appear in the first position of the traversal. Since
   * entities are not part of the document tree, they do not appear when
   * traversing over the document tree.
   */
  public static final int SHOW_ENTITY = 0x00000020;

  /**
   * Show <code>ProcessingInstruction</code> nodes.
   */
  public static final int SHOW_PROCESSING_INSTRUCTION = 0x00000040;

  /**
   * Show <code>Comment</code> nodes.
   */
  public static final int SHOW_COMMENT = 0x00000080;

  /**
   * Show <code>Document</code> nodes.
   */
  public static final int SHOW_DOCUMENT = 0x00000100;

  /**
   * Show <code>DocumentType</code> nodes.
   */
  public static final int SHOW_DOCUMENT_TYPE = 0x00000200;

  /**
   * Show <code>DocumentFragment</code> nodes.
   */
  public static final int SHOW_DOCUMENT_FRAGMENT = 0x00000400;

  /**
   * Show <code>Notation</code> nodes. This is meaningful only when creating
   * an iterator or tree-walker with a <code>Notation</code> node as its
   * <code>root</code>; in this case, it means that the
   * <code>Notation</code> node will appear in the first position of the
   * traversal. Since notations are not part of the document tree, they do
   * not appear when traversing over the document tree.
   */
  public static final int SHOW_NOTATION = 0x00000800;
  
  /**
   * This bit specifies a namespace, and extends the SHOW_XXX stuff
   *  in {@link org.w3c.dom.traversal.NodeFilter}.
   * Make sure this does not conflict with those values.
   */
  public static final int SHOW_NAMESPACE = 0x00001000;

  /**
   * Special bitmap for match patterns starting with a function.
   * This extends the SHOW_XXX stuff
   *  in {@link org.w3c.dom.traversal.NodeFilter}.
   * Make sure this does not conflict with those values.
   */
  public static final int SHOW_BYFUNCTION = 0x00010000;

  /**
   * Test whether a specified node is visible in the logical view of a
   * <code>DTMIterator</code>. Normally, this function
   * will be called by the implementation of <code>DTMIterator</code>; 
   * it is not normally called directly from
   * user code.
   * 
   * @param nodeHandle int Handle of the node.
   * @param whatToShow one of SHOW_XXX values.
   * @return one of FILTER_ACCEPT, FILTER_REJECT, or FILTER_SKIP.
   */
  public short acceptNode(int nodeHandle, int whatToShow);
  
  /**
   * Test whether a specified node is visible in the logical view of a
   * <code>DTMIterator</code>. Normally, this function
   * will be called by the implementation of <code>DTMIterator</code>; 
   * it is not normally called directly from
   * user code.
   * 
   * <p>%REVIEW% Under what circumstances will this be used? The cases
   * I've considered are just as easy and just about as efficient if
   * the name test is performed in the DTMIterator... -- Joe</p>
   * 
   * <p>%REVIEW% Should that 0xFFFF have a mnemonic assigned to it?
   * Also: This representation is assuming the expanded name is indeed
   * split into high/low 16-bit halfwords. If we ever change the
   * balance between namespace and localname bits (eg because we
   * decide there are many more localnames than namespaces, which is
   * fairly likely), this is going to break. It might be safer to
   * encapsulate the details with a makeExpandedName method and make
   * that responsible for setting up the wildcard version as well.</p>
   * 
   * @param nodeHandle int Handle of the node.
   * @param whatToShow one of SHOW_XXX values.
   * @param expandedName a value defining the exanded name as defined in 
   *                     the DTM interface.  Wild cards will be defined 
   *                     by 0xFFFF in the high word and/or in the low word.
   * @return one of FILTER_ACCEPT, FILTER_REJECT, or FILTER_SKIP.
   */
  public short acceptNode(int nodeHandle, int whatToShow, int expandedName);
 
}
