/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights 
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
 * originally based on software copyright (c) 2002, Sun Microsystems,
 * Inc., http://www.sun.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.xalan.res;


/**
 * Set up error messages.
 * We build a two dimensional array of message keys and
 * message strings. In order to add a new message here,
 * you need to first add a String constant. And
 *  you need to enter key , value pair as part of contents
 * Array. You also need to update MAX_CODE for error strings
 * and MAX_WARNING for warnings ( Needed for only information
 * purpose )
 */
public class XSLTErrorResources_ko extends XSLTErrorResources
{

  /** Maximum error messages, this is needed to keep track of the number of messages.    */
  public static final int MAX_CODE = 201;

  /** Maximum warnings, this is needed to keep track of the number of warnings.          */
  public static final int MAX_WARNING = 29;

  /** Maximum misc strings.   */
  public static final int MAX_OTHERS = 55;

  /** Maximum total warnings and error messages.          */
  public static final int MAX_MESSAGES = MAX_CODE + MAX_WARNING + 1;

  /** The lookup table for error messages.   */

  public static final Object[][] contents = {

  /** Error message ID that has a null message, but takes in a single object.    */
  //public static final int ERROR0000 = 0;


  {
    "ERROR0000", "{0}"},


  /** ER_NO_CURLYBRACE          */
  //public static final int ER_NO_CURLYBRACE = 1;


  {
    ER_NO_CURLYBRACE,
      "\uc624\ub958: \ud45c\ud604\uc2dd \uc548\uc5d0 '{'\ub97c \uc0ac\uc6a9\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},


  /** ER_ILLEGAL_ATTRIBUTE          */
  //public static final int ER_ILLEGAL_ATTRIBUTE = 2;


  {
    ER_ILLEGAL_ATTRIBUTE, "{0}\uc5d0 \uc798\ubabb\ub41c \uc18d\uc131 {1}\uc774(\uac00) \uc788\uc2b5\ub2c8\ub2e4."},


  /** ER_NULL_SOURCENODE_APPLYIMPORTS          */
  //public static final int ER_NULL_SOURCENODE_APPLYIMPORTS = 3;


  {
    ER_NULL_SOURCENODE_APPLYIMPORTS,
      "sourceNode\ub294 xsl:apply-imports\uc5d0\uc11c \ub110\uc785\ub2c8\ub2e4!"},


  /** ER_CANNOT_ADD          */
  //public static final int ER_CANNOT_ADD = 4;


  {
    ER_CANNOT_ADD, "{0}\uc744(\ub97c) {1}\uc5d0 \ucd94\uac00\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4. "},


  /** ER_NULL_SOURCENODE_HANDLEAPPLYTEMPLATES          */
  //public static final int ER_NULL_SOURCENODE_HANDLEAPPLYTEMPLATES = 5;


  {
    ER_NULL_SOURCENODE_HANDLEAPPLYTEMPLATES,
      "sourceNode\ub294 handleApplyTemplatesInstruction\uc5d0\uc11c \ub110\uc785\ub2c8\ub2e4!"},


  /** ER_NO_NAME_ATTRIB          */
  //public static final int ER_NO_NAME_ATTRIB = 6;


  {
    ER_NO_NAME_ATTRIB, "{0}\uc5d0 \uc774\ub984 \uc18d\uc131\uc774 \uc788\uc5b4\uc57c \ud569\ub2c8\ub2e4."},


  /** ER_TEMPLATE_NOT_FOUND          */
  //public static final int ER_TEMPLATE_NOT_FOUND = 7;


  {
    ER_TEMPLATE_NOT_FOUND, "\ud15c\ud50c\ub9ac\ud2b8 {0}\uc744(\ub97c) \ucc3e\uc744 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},


  /** ER_CANT_RESOLVE_NAME_AVT          */
  //public static final int ER_CANT_RESOLVE_NAME_AVT = 8;


  {
    ER_CANT_RESOLVE_NAME_AVT,
      "xsl:call-template\uc5d0\uc11c \uc774\ub984 AVT\ub97c \uacb0\uc815\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},


  /** ER_REQUIRES_ATTRIB          */
  //public static final int ER_REQUIRES_ATTRIB = 9;


  {
    ER_REQUIRES_ATTRIB, "{0}\uc5d0 \uc18d\uc131 {1}\uc774(\uac00) \ud544\uc694\ud569\ub2c8\ub2e4."},


  /** ER_MUST_HAVE_TEST_ATTRIB          */
  //public static final int ER_MUST_HAVE_TEST_ATTRIB = 10;


  {
    ER_MUST_HAVE_TEST_ATTRIB,
      "{0}\uc5d0 ''test'' \uc18d\uc131\uc774 \uc788\uc5b4\uc57c \ud569\ub2c8\ub2e4."},


  /** ER_BAD_VAL_ON_LEVEL_ATTRIB          */
  //public static final int ER_BAD_VAL_ON_LEVEL_ATTRIB = 11;


  {
    ER_BAD_VAL_ON_LEVEL_ATTRIB,
      "\ub808\ubca8 \uc18d\uc131 {0}\uc5d0 \uc798\ubabb\ub41c \uac12\uc774 \uc788\uc2b5\ub2c8\ub2e4."},


  /** ER_PROCESSINGINSTRUCTION_NAME_CANT_BE_XML          */
  //public static final int ER_PROCESSINGINSTRUCTION_NAME_CANT_BE_XML = 12;


  {
    ER_PROCESSINGINSTRUCTION_NAME_CANT_BE_XML,
      "processing-instruction \uc774\ub984\uc774 'xml'\uc774\uc5b4\uc11c\ub294 \uc548\ub429\ub2c8\ub2e4."},


  /** ER_PROCESSINGINSTRUCTION_NOTVALID_NCNAME          */
  //public static final int ER_PROCESSINGINSTRUCTION_NOTVALID_NCNAME = 13;


  {
    ER_PROCESSINGINSTRUCTION_NOTVALID_NCNAME,
      "processing-instruction \uc774\ub984\uc740 \uc62c\ubc14\ub978 NCName {0}\uc774\uc5b4\uc57c \ud569\ub2c8\ub2e4."},


  /** ER_NEED_MATCH_ATTRIB          */
  //public static final int ER_NEED_MATCH_ATTRIB = 14;


  {
    ER_NEED_MATCH_ATTRIB,
      "{0}\uc774(\uac00) \ubaa8\ub4dc\ub97c \uac00\uc9c0\uace0 \uc788\ub294 \uacbd\uc6b0 \uc77c\uce58 \uc18d\uc131\uc774 \uc788\uc5b4\uc57c \ud569\ub2c8\ub2e4."},


  /** ER_NEED_NAME_OR_MATCH_ATTRIB          */
  //public static final int ER_NEED_NAME_OR_MATCH_ATTRIB = 15;


  {
    ER_NEED_NAME_OR_MATCH_ATTRIB,
      "{0}\uc5d0 \uc774\ub984 \ub610\ub294 \uc77c\uce58 \uc18d\uc131 \uc911 \ud558\ub098\uac00 \ud544\uc694\ud569\ub2c8\ub2e4."},


  /** ER_CANT_RESOLVE_NSPREFIX          */
  //public static final int ER_CANT_RESOLVE_NSPREFIX = 16;


  {
    ER_CANT_RESOLVE_NSPREFIX,
      "\uc774\ub984 \uacf5\uac04 \uc811\ub450\uc5b4 {0}\uc744(\ub97c) \uacb0\uc815\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},


  /** ER_ILLEGAL_VALUE          */
  //public static final int ER_ILLEGAL_VALUE = 17;


  {
    ER_ILLEGAL_VALUE, "xml:space\uc5d0 \uc798\ubabb\ub41c \uac12 {0}\uc774(\uac00) \uc788\uc2b5\ub2c8\ub2e4."},


  /** ER_NO_OWNERDOC          */
  //public static final int ER_NO_OWNERDOC = 18;


  {
    ER_NO_OWNERDOC,
      "\uc790\uc2dd \ub178\ub4dc\uc5d0 \uc18c\uc720\uc790 \ubb38\uc11c\uac00 \uc5c6\uc2b5\ub2c8\ub2e4!"},


  /** ER_ELEMTEMPLATEELEM_ERR          */
  //public static final int ER_ELEMTEMPLATEELEM_ERR = 19;


  {
    ER_ELEMTEMPLATEELEM_ERR, "ElemTemplateElement \uc624\ub958: {0}"},


  /** ER_NULL_CHILD          */
  //public static final int ER_NULL_CHILD = 20;


  {
    ER_NULL_CHILD, "\ub110 \uc790\uc2dd\uc744 \ucd94\uac00\ud558\ub824\uace0 \uc2dc\ub3c4\ud558\ub294 \uc911\uc785\ub2c8\ub2e4!"},


  /** ER_NEED_SELECT_ATTRIB          */
  //public static final int ER_NEED_SELECT_ATTRIB = 21;


  {
    ER_NEED_SELECT_ATTRIB, "{0}\uc5d0 \uc120\ud0dd \uc18d\uc131\uc774 \ud544\uc694\ud569\ub2c8\ub2e4."},


  /** ER_NEED_TEST_ATTRIB          */
  //public static final int ER_NEED_TEST_ATTRIB = 22;


  {
    ER_NEED_TEST_ATTRIB,
      "xsl:when\uc5d0 'test' \uc18d\uc131\uc774 \uc788\uc5b4\uc57c \ud569\ub2c8\ub2e4."},


  /** ER_NEED_NAME_ATTRIB          */
  //public static final int ER_NEED_NAME_ATTRIB = 23;


  {
    ER_NEED_NAME_ATTRIB,
      "xsl:with-param\uc5d0 'name' \uc18d\uc131\uc774 \uc788\uc5b4\uc57c \ud569\ub2c8\ub2e4."},


  /** ER_NO_CONTEXT_OWNERDOC          */
  //public static final int ER_NO_CONTEXT_OWNERDOC = 24;


  {
    ER_NO_CONTEXT_OWNERDOC,
      "\ucee8\ud14d\uc2a4\ud2b8\uc5d0 \uc18c\uc720\uc790 \ubb38\uc11c\uac00 \uc5c6\uc2b5\ub2c8\ub2e4."},


  /** ER_COULD_NOT_CREATE_XML_PROC_LIAISON          */
  //public static final int ER_COULD_NOT_CREATE_XML_PROC_LIAISON = 25;


  {
    ER_COULD_NOT_CREATE_XML_PROC_LIAISON,
      "XML TransformerFactory Liaison {0}\uc744(\ub97c) \uc791\uc131\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},


  /** ER_PROCESS_NOT_SUCCESSFUL          */
  //public static final int ER_PROCESS_NOT_SUCCESSFUL = 26;


  {
    ER_PROCESS_NOT_SUCCESSFUL,
      "Xalan: \ud504\ub85c\uc138\uc2a4\uc5d0 \uc131\uacf5\ud558\uc9c0 \ubabb\ud588\uc2b5\ub2c8\ub2e4."},


  /** ER_NOT_SUCCESSFUL          */
  //public static final int ER_NOT_SUCCESSFUL = 27;


  {
    ER_NOT_SUCCESSFUL, "Xalan:\uc5d0 \uc131\uacf5\ud558\uc9c0 \ubabb\ud588\uc2b5\ub2c8\ub2e4."},


  /** ER_ENCODING_NOT_SUPPORTED          */
  //public static final int ER_ENCODING_NOT_SUPPORTED = 28;


  {
    ER_ENCODING_NOT_SUPPORTED, "\ucf54\ub4dc\ud654\uac00 \uc9c0\uc6d0\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4: {0}"},


  /** ER_COULD_NOT_CREATE_TRACELISTENER          */
  //public static final int ER_COULD_NOT_CREATE_TRACELISTENER = 29;


  {
    ER_COULD_NOT_CREATE_TRACELISTENER,
      "TraceListener {0}\uc744(\ub97c) \uc791\uc131\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},


  /** ER_KEY_REQUIRES_NAME_ATTRIB          */
  //public static final int ER_KEY_REQUIRES_NAME_ATTRIB = 30;


  {
    ER_KEY_REQUIRES_NAME_ATTRIB,
      "xsl:key\uc5d0 'name' \uc18d\uc131\uc774 \ud544\uc694\ud569\ub2c8\ub2e4!"},


  /** ER_KEY_REQUIRES_MATCH_ATTRIB          */
  //public static final int ER_KEY_REQUIRES_MATCH_ATTRIB = 31;


  {
    ER_KEY_REQUIRES_MATCH_ATTRIB,
      "xsl:key\uc5d0 'match' \uc18d\uc131\uc774 \ud544\uc694\ud569\ub2c8\ub2e4!"},


  /** ER_KEY_REQUIRES_USE_ATTRIB          */
  //public static final int ER_KEY_REQUIRES_USE_ATTRIB = 32;


  {
    ER_KEY_REQUIRES_USE_ATTRIB,
      "xsl:key\uc5d0 'use' \uc18d\uc131\uc774 \ud544\uc694\ud569\ub2c8\ub2e4!"},


  /** ER_REQUIRES_ELEMENTS_ATTRIB          */
  //public static final int ER_REQUIRES_ELEMENTS_ATTRIB = 33;


  {
    ER_REQUIRES_ELEMENTS_ATTRIB,
      "(StylesheetHandler) {0}\uc5d0 ''elements'' \uc18d\uc131\uc774 \ud544\uc694\ud569\ub2c8\ub2e4!"},


  /** ER_MISSING_PREFIX_ATTRIB          */
  //public static final int ER_MISSING_PREFIX_ATTRIB = 34;


  {
    ER_MISSING_PREFIX_ATTRIB,
      "(StylesheetHandler) {0} \uc18d\uc131 ''prefix''\uac00 \ube60\uc84c\uc2b5\ub2c8\ub2e4."},


  /** ER_BAD_STYLESHEET_URL          */
  //public static final int ER_BAD_STYLESHEET_URL = 35;


  {
    ER_BAD_STYLESHEET_URL, "Stylesheet URL\uc774 \uc798\ubabb\ub418\uc5c8\uc2b5\ub2c8\ub2e4: {0}"},


  /** ER_FILE_NOT_FOUND          */
  //public static final int ER_FILE_NOT_FOUND = 36;


  {
    ER_FILE_NOT_FOUND, "Stylesheet \ud30c\uc77c\uc744 \ucc3e\uc744 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4: {0}"},


  /** ER_IOEXCEPTION          */
  //public static final int ER_IOEXCEPTION = 37;


  {
    ER_IOEXCEPTION,
      "\uc2a4\ud0c0\uc77c \uc2dc\ud2b8 \ud30c\uc77c\ub85c IO \uc608\uc678\uac00 \ubc1c\uc0dd\ud558\uc600\uc2b5\ub2c8\ub2e4: {0}"},


  /** ER_NO_HREF_ATTRIB          */
  //public static final int ER_NO_HREF_ATTRIB = 38;


  {
    ER_NO_HREF_ATTRIB,
      "(StylesheetHandler) {0}\uc5d0 \ub300\ud55c href \uc18d\uc131\uc744 \ucc3e\uc744 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},


  /** ER_STYLESHEET_INCLUDES_ITSELF          */
  //public static final int ER_STYLESHEET_INCLUDES_ITSELF = 39;


  {
    ER_STYLESHEET_INCLUDES_ITSELF,
      "(StylesheetHandler) {0}\uc774(\uac00) \uc9c1\uc811 \ub610\ub294 \uac04\uc811\uc801\uc73c\ub85c \uc790\uc2e0\uc744 \ud3ec\ud568\ud558\uace0 \uc788\uc2b5\ub2c8\ub2e4."},


  /** ER_PROCESSINCLUDE_ERROR          */
  //public static final int ER_PROCESSINCLUDE_ERROR = 40;


  {
    ER_PROCESSINCLUDE_ERROR,
      "StylesheetHandler.processInclude \uc624\ub958, {0}"},


  /** ER_MISSING_LANG_ATTRIB          */
  //public static final int ER_MISSING_LANG_ATTRIB = 41;


  {
    ER_MISSING_LANG_ATTRIB,
      "(StylesheetHandler) {0} \uc18d\uc131 ''lang''\uc774 \ube60\uc84c\uc2b5\ub2c8\ub2e4."},


  /** ER_MISSING_CONTAINER_ELEMENT_COMPONENT          */
  //public static final int ER_MISSING_CONTAINER_ELEMENT_COMPONENT = 42;


  {
    ER_MISSING_CONTAINER_ELEMENT_COMPONENT,
      "(StylesheetHandler) {0} \uc694\uc18c\uc758 \uc704\uce58\uac00 \uc798\ubabb\ub418\uc5c8\uc2b5\ub2c8\ub2e4?? container \uc694\uc18c ''component''\uac00 \ube60\uc84c\uc2b5\ub2c8\ub2e4."},


  /** ER_CAN_ONLY_OUTPUT_TO_ELEMENT          */
  //public static final int ER_CAN_ONLY_OUTPUT_TO_ELEMENT = 43;


  {
    ER_CAN_ONLY_OUTPUT_TO_ELEMENT,
      "Element, DocumentFragment, Document \ub610\ub294 PrintWriter\ub85c\ub9cc \ucd9c\ub825\ud560 \uc218 \uc788\uc2b5\ub2c8\ub2e4."},


  /** ER_PROCESS_ERROR          */
  //public static final int ER_PROCESS_ERROR = 44;


  {
    ER_PROCESS_ERROR, "StylesheetRoot.process \uc624\ub958"},


  /** ER_UNIMPLNODE_ERROR          */
  //public static final int ER_UNIMPLNODE_ERROR = 45;


  {
    ER_UNIMPLNODE_ERROR, "UnImplNode \uc624\ub958: {0}"},


  /** ER_NO_SELECT_EXPRESSION          */
  //public static final int ER_NO_SELECT_EXPRESSION = 46;


  {
    ER_NO_SELECT_EXPRESSION,
      "\uc624\ub958! xpath \uc120\ud0dd \ud45c\ud604\uc2dd(-select)\uc744 \ucc3e\uc744 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4"},


  /** ER_CANNOT_SERIALIZE_XSLPROCESSOR          */
  //public static final int ER_CANNOT_SERIALIZE_XSLPROCESSOR = 47;


  {
    ER_CANNOT_SERIALIZE_XSLPROCESSOR,
      "XSLProcessor\ub97c \uc77c\ub828\ud654\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4!"},


  /** ER_NO_INPUT_STYLESHEET          */
  //public static final int ER_NO_INPUT_STYLESHEET = 48;


  {
    ER_NO_INPUT_STYLESHEET,
      "Stylesheet \uc785\ub825\uc744 \uc9c0\uc815\ud558\uc9c0 \uc54a\uc558\uc2b5\ub2c8\ub2e4!"},


  /** ER_FAILED_PROCESS_STYLESHEET          */
  //public static final int ER_FAILED_PROCESS_STYLESHEET = 49;


  {
    ER_FAILED_PROCESS_STYLESHEET,
      "stylesheet \ucc98\ub9ac\uc5d0 \uc2e4\ud328\ud588\uc2b5\ub2c8\ub2e4!"},


  /** ER_COULDNT_PARSE_DOC          */
  //public static final int ER_COULDNT_PARSE_DOC = 50;


  {
    ER_COULDNT_PARSE_DOC, "{0} \ubb38\uc11c\ub97c \uad6c\ubb38 \ubd84\uc11d\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4!"},


  /** ER_COULDNT_FIND_FRAGMENT          */
  //public static final int ER_COULDNT_FIND_FRAGMENT = 51;


  {
    ER_COULDNT_FIND_FRAGMENT, "\ub2e8\ud3b8 {0}\uc744(\ub97c) \ucc3e\uc744 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},


  /** ER_NODE_NOT_ELEMENT          */
  //public static final int ER_NODE_NOT_ELEMENT = 52;


  {
    ER_NODE_NOT_ELEMENT,
      "\ub2e8\ud3b8 \uc2dd\ubcc4\uc790\uc5d0 \uc758\ud574 \uc9c0\uc815\ub41c \ub178\ub4dc\ub294 \uc694\uc18c\uac00 \uc544\ub2d9\ub2c8\ub2e4: {0}"},


  /** ER_FOREACH_NEED_MATCH_OR_NAME_ATTRIB          */
  //public static final int ER_FOREACH_NEED_MATCH_OR_NAME_ATTRIB = 53;


  {
    ER_FOREACH_NEED_MATCH_OR_NAME_ATTRIB,
      "for-each\uc5d0 \uc77c\uce58 \ub610\ub294 \uc774\ub984 \uc18d\uc131 \uc911 \ud558\ub098\uac00 \uc788\uc5b4\uc57c \ud569\ub2c8\ub2e4."},


  /** ER_TEMPLATES_NEED_MATCH_OR_NAME_ATTRIB          */
  //public static final int ER_TEMPLATES_NEED_MATCH_OR_NAME_ATTRIB = 54;


  {
    ER_TEMPLATES_NEED_MATCH_OR_NAME_ATTRIB,
      "\ud15c\ud50c\ub9ac\ud2b8\uc5d0 \uc77c\uce58 \ub610\ub294 \uc774\ub984 \uc18d\uc131 \uc911 \ud558\ub098\uac00 \uc788\uc5b4\uc57c \ud569\ub2c8\ub2e4."},


  /** ER_NO_CLONE_OF_DOCUMENT_FRAG          */
  //public static final int ER_NO_CLONE_OF_DOCUMENT_FRAG = 55;


  {
    ER_NO_CLONE_OF_DOCUMENT_FRAG,
      "\ubb38\uc11c \ub2e8\ud3b8 \ubcf5\uc81c\uac00 \uc5c6\uc2b5\ub2c8\ub2e4!"},


  /** ER_CANT_CREATE_ITEM          */
  //public static final int ER_CANT_CREATE_ITEM = 56;


  {
    ER_CANT_CREATE_ITEM,
      "\uacb0\uacfc \ud2b8\ub9ac {0}\uc5d0 \ud56d\ubaa9\uc744 \uc791\uc131\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},


  /** ER_XMLSPACE_ILLEGAL_VALUE          */
  //public static final int ER_XMLSPACE_ILLEGAL_VALUE = 57;


  {
    ER_XMLSPACE_ILLEGAL_VALUE,
      "\uc18c\uc2a4 XML\uc758 xml:space\uc5d0 \uc798\ubabb\ub41c \uac12 {0}\uc774(\uac00) \uc788\uc2b5\ub2c8\ub2e4."},


  /** ER_NO_XSLKEY_DECLARATION          */
  //public static final int ER_NO_XSLKEY_DECLARATION = 58;


  {
    ER_NO_XSLKEY_DECLARATION,
      "{0}\uc5d0 \ub300\ud558\uc5ec xsl:key \uc120\uc5b8\uc774 \uc5c6\uc2b5\ub2c8\ub2e4!"},


  /** ER_CANT_CREATE_URL          */
  //public static final int ER_CANT_CREATE_URL = 59;


  {
    ER_CANT_CREATE_URL, "\uc624\ub958! {0}\uc5d0 \ub300\ud558\uc5ec url\uc744 \uc791\uc131\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},


  /** ER_XSLFUNCTIONS_UNSUPPORTED          */
  //public static final int ER_XSLFUNCTIONS_UNSUPPORTED = 60;


  {
    ER_XSLFUNCTIONS_UNSUPPORTED, "xsl:functions\uac00 \uc9c0\uc6d0\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4."},


  /** ER_PROCESSOR_ERROR          */
  //public static final int ER_PROCESSOR_ERROR = 61;


  {
    ER_PROCESSOR_ERROR, "XSLT TransformerFactory \uc624\ub958"},


  /** ER_NOT_ALLOWED_INSIDE_STYLESHEET          */
  //public static final int ER_NOT_ALLOWED_INSIDE_STYLESHEET = 62;


  {
    ER_NOT_ALLOWED_INSIDE_STYLESHEET,
      "(StylesheetHandler) {0}\uc740(\ub294) \uc2a4\ud0c0\uc77c \uc2dc\ud2b8 \ub0b4\ubd80\uc5d0 \ud5c8\uc6a9\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4!"},


  /** ER_RESULTNS_NOT_SUPPORTED          */
  //public static final int ER_RESULTNS_NOT_SUPPORTED = 63;


  {
    ER_RESULTNS_NOT_SUPPORTED,
      "result-ns\ub294 \ub354 \uc774\uc0c1 \uc9c0\uc6d0\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4! \ub300\uc2e0 xsl:output\uc744 \uc0ac\uc6a9\ud558\uc2ed\uc2dc\uc624."},


  /** ER_DEFAULTSPACE_NOT_SUPPORTED          */
  //public static final int ER_DEFAULTSPACE_NOT_SUPPORTED = 64;


  {
    ER_DEFAULTSPACE_NOT_SUPPORTED,
      "default-space\ub294 \ub354 \uc774\uc0c1 \uc9c0\uc6d0\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4! \ub300\uc2e0 xsl:preserve-space\ub97c \uc0ac\uc6a9\ud558\uc2ed\uc2dc\uc624."},


  /** ER_INDENTRESULT_NOT_SUPPORTED          */
  //public static final int ER_INDENTRESULT_NOT_SUPPORTED = 65;


  {
    ER_INDENTRESULT_NOT_SUPPORTED,
      "indent-result\ub294 \ub354 \uc774\uc0c1 \uc9c0\uc6d0\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4! \ub300\uc2e0 xsl:output\uc744 \uc0ac\uc6a9\ud558\uc2ed\uc2dc\uc624."},


  /** ER_ILLEGAL_ATTRIB          */
  //public static final int ER_ILLEGAL_ATTRIB = 66;


  {
    ER_ILLEGAL_ATTRIB,
      "(StylesheetHandler) {0}\uc5d0 \uc798\ubabb\ub41c \uc18d\uc131 {1}\uc774(\uac00) \uc788\uc2b5\ub2c8\ub2e4."},


  /** ER_UNKNOWN_XSL_ELEM          */
  //public static final int ER_UNKNOWN_XSL_ELEM = 67;


  {
    ER_UNKNOWN_XSL_ELEM, "\uc54c \uc218 \uc5c6\ub294 XSL \uc694\uc18c {0}"},


  /** ER_BAD_XSLSORT_USE          */
  //public static final int ER_BAD_XSLSORT_USE = 68;


  {
    ER_BAD_XSLSORT_USE,
      "(StylesheetHandler) xsl:sort\ub294 xsl:apply-templates \ub610\ub294 xsl:for-each\uc640\ub9cc \uc0ac\uc6a9\ud560 \uc218 \uc788\uc2b5\ub2c8\ub2e4."},


  /** ER_MISPLACED_XSLWHEN          */
  //public static final int ER_MISPLACED_XSLWHEN = 69;


  {
    ER_MISPLACED_XSLWHEN,
      "(StylesheetHandler) xsl:when\uc758 \uc704\uce58\uac00 \uc798\ubabb\ub418\uc5c8\uc2b5\ub2c8\ub2e4!"},


  /** ER_XSLWHEN_NOT_PARENTED_BY_XSLCHOOSE          */
  //public static final int ER_XSLWHEN_NOT_PARENTED_BY_XSLCHOOSE = 70;


  {
    ER_XSLWHEN_NOT_PARENTED_BY_XSLCHOOSE,
      "(StylesheetHandler) xsl:when\uc758 \ubd80\ubaa8\ub294 xsl:choose\uac00 \uc544\ub2d9\ub2c8\ub2e4!"},


  /** ER_MISPLACED_XSLOTHERWISE          */
  //public static final int ER_MISPLACED_XSLOTHERWISE = 71;


  {
    ER_MISPLACED_XSLOTHERWISE,
      "(StylesheetHandler) xsl:otherwise\uc758 \uc704\uce58\uac00 \uc798\ubabb\ub418\uc5c8\uc2b5\ub2c8\ub2e4!"},


  /** ER_XSLOTHERWISE_NOT_PARENTED_BY_XSLCHOOSE          */
  //public static final int ER_XSLOTHERWISE_NOT_PARENTED_BY_XSLCHOOSE = 72;


  {
    ER_XSLOTHERWISE_NOT_PARENTED_BY_XSLCHOOSE,
      "(StylesheetHandler) xsl:otherwise\uc758 \ubd80\ubaa8\ub294 xsl:choose\uac00 \uc544\ub2d9\ub2c8\ub2e4!"},


  /** ER_NOT_ALLOWED_INSIDE_TEMPLATE          */
  //public static final int ER_NOT_ALLOWED_INSIDE_TEMPLATE = 73;


  {
    ER_NOT_ALLOWED_INSIDE_TEMPLATE,
      "(StylesheetHandler) {0}\uc740(\ub294) \ud15c\ud50c\ub9ac\ud2b8 \ub0b4\ubd80\uc5d0 \ud5c8\uc6a9\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4!"},


  /** ER_UNKNOWN_EXT_NS_PREFIX          */
  //public static final int ER_UNKNOWN_EXT_NS_PREFIX = 74;


  {
    ER_UNKNOWN_EXT_NS_PREFIX,
      "(StylesheetHandler) {0} \ud655\uc7a5 \uc774\ub984 \uacf5\uac04 \uc811\ub450\uc5b4 {1}\uc744(\ub97c) \uc54c \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},


  /** ER_IMPORTS_AS_FIRST_ELEM          */
  //public static final int ER_IMPORTS_AS_FIRST_ELEM = 75;


  {
    ER_IMPORTS_AS_FIRST_ELEM,
      "(StylesheetHandler) \uac00\uc838\uc624\uae30\ub294 \uc2a4\ud0c0\uc77c \uc2dc\ud2b8\uc758 \uccab \ubc88\uc9f8 \uc694\uc18c\ub85c\ub9cc \ubc1c\uc0dd\ud560 \uc218 \uc788\uc2b5\ub2c8\ub2e4!"},


  /** ER_IMPORTING_ITSELF          */
  //public static final int ER_IMPORTING_ITSELF = 76;


  {
    ER_IMPORTING_ITSELF,
      "(StylesheetHandler) {0}\uc774(\uac00) \uc9c1\uc811 \ub610\ub294 \uac04\uc811\uc801\uc73c\ub85c \uc790\uc2e0\uc744 \uac00\uc838\uc624\uace0 \uc788\uc2b5\ub2c8\ub2e4!"},


  /** ER_XMLSPACE_ILLEGAL_VAL          */
  //public static final int ER_XMLSPACE_ILLEGAL_VAL = 77;


  {
    ER_XMLSPACE_ILLEGAL_VAL,
      "(StylesheetHandler) " + "xml:space\uc5d0 \uc798\ubabb\ub41c \uac12 {0}\uc774(\uac00) \uc788\uc2b5\ub2c8\ub2e4."},


  /** ER_PROCESSSTYLESHEET_NOT_SUCCESSFUL          */
  //public static final int ER_PROCESSSTYLESHEET_NOT_SUCCESSFUL = 78;


  {
    ER_PROCESSSTYLESHEET_NOT_SUCCESSFUL,
      "processStylesheet\uac00 \uc131\uacf5\ud558\uc9c0 \ubabb\ud588\uc2b5\ub2c8\ub2e4!"},


  /** ER_SAX_EXCEPTION          */
  //public static final int ER_SAX_EXCEPTION = 79;


  {
    ER_SAX_EXCEPTION, "SAX \uc608\uc678"},


  /** ER_XSLT_ERROR          */
  //public static final int ER_XSLT_ERROR = 81;


  {
    ER_XSLT_ERROR, "XSLT \uc624\ub958"},


  /** ER_CURRENCY_SIGN_ILLEGAL          */
  //public static final int ER_CURRENCY_SIGN_ILLEGAL = 82;


  {
    ER_CURRENCY_SIGN_ILLEGAL,
      "\ud615\uc2dd \ud328\ud134 \ubb38\uc790\uc5f4\uc5d0 \ud1b5\ud654 \ubd80\ud638\uac00 \ud5c8\uc6a9\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4."},


  /** ER_DOCUMENT_FUNCTION_INVALID_IN_STYLESHEET_DOM          */
  //public static final int ER_DOCUMENT_FUNCTION_INVALID_IN_STYLESHEET_DOM = 83;


  {
    ER_DOCUMENT_FUNCTION_INVALID_IN_STYLESHEET_DOM,
      "Stylesheet DOM\uc5d0\uc11c \ubb38\uc11c \uae30\ub2a5\uc774 \uc9c0\uc6d0\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4!"},


  /** ER_CANT_RESOLVE_PREFIX_OF_NON_PREFIX_RESOLVER          */
  //public static final int ER_CANT_RESOLVE_PREFIX_OF_NON_PREFIX_RESOLVER = 84;


  {
    ER_CANT_RESOLVE_PREFIX_OF_NON_PREFIX_RESOLVER,
      "\uc811\ub450\uc5b4\uac00 \uc5c6\ub294 \uacb0\uc815\uc790\uc758 \uc811\ub450\uc5b4\ub97c \uacb0\uc815\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4!"},


  /** ER_REDIRECT_COULDNT_GET_FILENAME          */
  //public static final int ER_REDIRECT_COULDNT_GET_FILENAME = 85;


  {
    ER_REDIRECT_COULDNT_GET_FILENAME,
      "Redirect \ud655\uc7a5: \ud30c\uc77c \uc774\ub984\uc744 \uac00\uc838\uc62c \uc218 \uc5c6\uc2b5\ub2c8\ub2e4 - \ud30c\uc77c \ub610\ub294 \uc120\ud0dd \uc18d\uc131\uc774 \uc62c\ubc14\ub978 \ubb38\uc790\uc5f4\uc744 \ubc18\ud658\ud574\uc57c \ud569\ub2c8\ub2e4."},


  /** ER_CANNOT_BUILD_FORMATTERLISTENER_IN_REDIRECT          */
  //public static final int ER_CANNOT_BUILD_FORMATTERLISTENER_IN_REDIRECT = 86;


  {
    ER_CANNOT_BUILD_FORMATTERLISTENER_IN_REDIRECT,
      "Redirect \ud655\uc7a5\uc5d0 FormatterListener\ub97c \uad6c\ucd95\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4!"},


  /** ER_INVALID_PREFIX_IN_EXCLUDERESULTPREFIX          */
  //public static final int ER_INVALID_PREFIX_IN_EXCLUDERESULTPREFIX = 87;


  {
    ER_INVALID_PREFIX_IN_EXCLUDERESULTPREFIX,
      "exclude-result-prefixes\uc758 \uc811\ub450\uc5b4\uac00 \uc62c\ubc14\ub974\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4: {0}"},


  /** ER_MISSING_NS_URI          */
  //public static final int ER_MISSING_NS_URI = 88;


  {
    ER_MISSING_NS_URI,
      "\uc9c0\uc815\ub41c \uc811\ub450\uc5b4\uc5d0 \ub300\ud55c \uc774\ub984 \uacf5\uac04 URI\uac00 \ube60\uc84c\uc2b5\ub2c8\ub2e4."},


  /** ER_MISSING_ARG_FOR_OPTION          */
  //public static final int ER_MISSING_ARG_FOR_OPTION = 89;


  {
    ER_MISSING_ARG_FOR_OPTION,
      "\uc635\uc158 {0}\uc5d0 \ub300\ud55c \uc778\uc790\uac00 \ube60\uc84c\uc2b5\ub2c8\ub2e4."},


  /** ER_INVALID_OPTION          */
  //public static final int ER_INVALID_OPTION = 90;


  {
    ER_INVALID_OPTION, "\uc798\ubabb\ub41c \uc635\uc158: {0}"},


  /** ER_MALFORMED_FORMAT_STRING          */
  //public static final int ER_MALFORMED_FORMAT_STRING = 91;


  {
    ER_MALFORMED_FORMAT_STRING, "\uc798\ubabb\ub41c \ud615\uc2dd \ubb38\uc790\uc5f4: {0}"},


  /** ER_STYLESHEET_REQUIRES_VERSION_ATTRIB          */
  //public static final int ER_STYLESHEET_REQUIRES_VERSION_ATTRIB = 92;


  {
    ER_STYLESHEET_REQUIRES_VERSION_ATTRIB,
      "xsl:stylesheet\uc5d0 'version' \uc18d\uc131\uc774 \ud544\uc694\ud569\ub2c8\ub2e4!"},


  /** ER_ILLEGAL_ATTRIBUTE_VALUE          */
  //public static final int ER_ILLEGAL_ATTRIBUTE_VALUE = 93;


  {
    ER_ILLEGAL_ATTRIBUTE_VALUE,
      "\uc18d\uc131: {0}\uc5d0 \uc798\ubabb\ub41c \uac12 {1}\uc774(\uac00) \uc788\uc2b5\ub2c8\ub2e4."},


  /** ER_CHOOSE_REQUIRES_WHEN          */
  //public static final int ER_CHOOSE_REQUIRES_WHEN = 94;


  {
    ER_CHOOSE_REQUIRES_WHEN, "xsl:choose\uc5d0 xsl:when\uc774 \ud544\uc694\ud569\ub2c8\ub2e4.   "},


  /** ER_NO_APPLY_IMPORT_IN_FOR_EACH          */
  //public static final int ER_NO_APPLY_IMPORT_IN_FOR_EACH = 95;


  {
    ER_NO_APPLY_IMPORT_IN_FOR_EACH,
      "xsl:apply-imports\ub294 xsl:for-each\uc5d0 \ud5c8\uc6a9\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4."},


  /** ER_CANT_USE_DTM_FOR_OUTPUT          */
  //public static final int ER_CANT_USE_DTM_FOR_OUTPUT = 96;


  {
    ER_CANT_USE_DTM_FOR_OUTPUT,
      "\ucd9c\ub825 DOM \ub178\ub4dc\ub85c DTMLiaison\uc744 \uc0ac\uc6a9\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4... \ub300\uc2e0 org.apache.xpath.DOM2Helper\ub97c \uc804\ub2ec\ud558\uc2ed\uc2dc\uc624!"},


  /** ER_CANT_USE_DTM_FOR_INPUT          */
  //public static final int ER_CANT_USE_DTM_FOR_INPUT = 97;


  {
    ER_CANT_USE_DTM_FOR_INPUT,
      "\uc785\ub825 DOM \ub178\ub4dc\ub85c DTMLiaison\uc744 \uc0ac\uc6a9\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4... \ub300\uc2e0 org.apache.xpath.DOM2Helper\ub97c \uc804\ub2ec\ud558\uc2ed\uc2dc\uc624!"},


  /** ER_CALL_TO_EXT_FAILED          */
  //public static final int ER_CALL_TO_EXT_FAILED = 98;


  {
    ER_CALL_TO_EXT_FAILED,
      "\ud655\uc7a5 \uc694\uc18c\uc5d0 \ub300\ud55c \ud638\ucd9c\uc774 \uc2e4\ud328\ud588\uc2b5\ub2c8\ub2e4: {0}"},


  /** ER_PREFIX_MUST_RESOLVE          */
  //public static final int ER_PREFIX_MUST_RESOLVE = 99;


  {
    ER_PREFIX_MUST_RESOLVE,
      "\uc811\ub450\uc5b4\uac00 \uc774\ub984 \uacf5\uac04 {0}\uc73c\ub85c(\ub85c) \uacb0\uc815\ub418\uc5b4\uc57c \ud569\ub2c8\ub2e4."},


  /** ER_INVALID_UTF16_SURROGATE          */
  //public static final int ER_INVALID_UTF16_SURROGATE = 100;


  {
    ER_INVALID_UTF16_SURROGATE,
      "\uc798\ubabb\ub41c UTF-16 \ub300\ub9ac\uac00 \uac10\uc9c0\ub418\uc5c8\uc2b5\ub2c8\ub2e4: {0} ?"},


  /** ER_XSLATTRSET_USED_ITSELF          */
  //public static final int ER_XSLATTRSET_USED_ITSELF = 101;


  {
    ER_XSLATTRSET_USED_ITSELF,
      "xsl:attribute-set {0}\uc774(\uac00) \uc790\uc2e0\uc744 \uc0ac\uc6a9\ud558\uc600\uba70, \uc774\ub85c \uc778\ud574 \ubb34\ud55c \ub8e8\ud504\uac00 \ubc1c\uc0dd\ud569\ub2c8\ub2e4."},


  /** ER_CANNOT_MIX_XERCESDOM          */
  //public static final int ER_CANNOT_MIX_XERCESDOM = 102;


  {
    ER_CANNOT_MIX_XERCESDOM,
      "Xerces-DOM\uc774 \uc544\ub2cc \uc785\ub825\uacfc Xerces-DOM \ucd9c\ub825\uc744 \ud568\uaed8 \uc0ac\uc6a9\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4!"},


  /** ER_TOO_MANY_LISTENERS          */
  //public static final int ER_TOO_MANY_LISTENERS = 103;


  {
    ER_TOO_MANY_LISTENERS,
      "addTraceListenersToStylesheet - TooManyListenersException"},


  /** ER_IN_ELEMTEMPLATEELEM_READOBJECT          */
  //public static final int ER_IN_ELEMTEMPLATEELEM_READOBJECT = 104;


  {
    ER_IN_ELEMTEMPLATEELEM_READOBJECT,
      "ElemTemplateElement.readObject\uc5d0: {0}"},


  /** ER_DUPLICATE_NAMED_TEMPLATE          */
  //public static final int ER_DUPLICATE_NAMED_TEMPLATE = 105;


  {
    ER_DUPLICATE_NAMED_TEMPLATE,
      "\uac19\uc740 \uc774\ub984 {0}\uc744(\ub97c) \uac00\uc9c4 \ub458 \uc774\uc0c1\uc758 \ud15c\ud50c\ub9ac\ud2b8\ub97c \ucc3e\uc558\uc2b5\ub2c8\ub2e4."},


  /** ER_INVALID_KEY_CALL          */
  //public static final int ER_INVALID_KEY_CALL = 106;


  {
    ER_INVALID_KEY_CALL,
      "\uc798\ubabb\ub41c \ud568\uc218 \ud638\ucd9c: \uc21c\ud658\uc801 key() \ud638\ucd9c\uc740 \ud5c8\uc6a9\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4."},

  
  /** Variable is referencing itself          */
  //public static final int ER_REFERENCING_ITSELF = 107;


  {
    ER_REFERENCING_ITSELF,
      "{0} \ubcc0\uc218\uac00 \uc9c1\uc811 \ub610\ub294 \uac04\uc811\uc801\uc73c\ub85c \uc790\uc2e0\uc744 \ucc38\uc870\ud558\uace0 \uc788\uc2b5\ub2c8\ub2e4!"},

  
  /** Illegal DOMSource input          */
  //public static final int ER_ILLEGAL_DOMSOURCE_INPUT = 108;


  {
    ER_ILLEGAL_DOMSOURCE_INPUT,
      "newTemplates\uc758 DOMSource\uc5d0\uc11c \uc785\ub825 \ub178\ub4dc\uac00 \ub110\uc774 \ub420 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4!"},

	
	/** Class not found for option         */
  //public static final int ER_CLASS_NOT_FOUND_FOR_OPTION = 109;


  {
    ER_CLASS_NOT_FOUND_FOR_OPTION,
			"\uc635\uc158 {0}\uc758 \ud074\ub798\uc2a4 \ud30c\uc77c\uc744 \ucc3e\uc744 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4.    "},

	
	/** Required Element not found         */
  //public static final int ER_REQUIRED_ELEM_NOT_FOUND = 110;


  {
    ER_REQUIRED_ELEM_NOT_FOUND,
			"\ud544\uc218 \uc694\uc18c\ub97c \ucc3e\uc744 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4: {0}"},

  
  /** InputStream cannot be null         */
  //public static final int ER_INPUT_CANNOT_BE_NULL = 111;


  {
    ER_INPUT_CANNOT_BE_NULL,
			"InputStream\uc740 \ub110\uc774 \ub420 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4"},

  
  /** URI cannot be null         */
  //public static final int ER_URI_CANNOT_BE_NULL = 112;


  {
    ER_URI_CANNOT_BE_NULL,
			"URI\ub294 \ub110\uc774 \ub420 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4"},

  
  /** File cannot be null         */
  //public static final int ER_FILE_CANNOT_BE_NULL = 113;


  {
    ER_FILE_CANNOT_BE_NULL,
			"\ud30c\uc77c\uc740 \ub110\uc774 \ub420 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4"},

  
   /** InputSource cannot be null         */
  //public static final int ER_SOURCE_CANNOT_BE_NULL = 114;


  {
    ER_SOURCE_CANNOT_BE_NULL,
			"InputSource\ub294 \ub110\uc774 \ub420 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4"},

  
  /** Could not initialize BSF Manager        */
  //public static final int ER_CANNOT_INIT_BSFMGR = 116;


  {
    ER_CANNOT_INIT_BSFMGR,
			"BSF \uad00\ub9ac\uc790\ub97c \ucd08\uae30\ud654\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4"},

  
  /** Could not compile extension       */
  //public static final int ER_CANNOT_CMPL_EXTENSN = 117;


  {
    ER_CANNOT_CMPL_EXTENSN,
			"\ud655\uc7a5\uc790\ub97c \ucef4\ud30c\uc77c\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4"},

  
  /** Could not create extension       */
  //public static final int ER_CANNOT_CREATE_EXTENSN = 118;


  {
    ER_CANNOT_CREATE_EXTENSN,
      "{1}\ub85c \uc778\ud574 \ud655\uc7a5\uc790 {0}\uc744(\ub97c) \uc791\uc131\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},

  
  /** Instance method call to method {0} requires an Object instance as first argument       */
  //public static final int ER_INSTANCE_MTHD_CALL_REQUIRES = 119;


  {
    ER_INSTANCE_MTHD_CALL_REQUIRES,
      "\uba54\uc18c\ub4dc {0}\uc5d0 \ub300\ud55c \uc778\uc2a4\ud134\uc2a4 \uba54\uc18c\ub4dc \ud638\ucd9c\uc5d0\uc11c \uac1d\uccb4 \uc778\uc2a4\ud134\uc2a4\uac00 \uccab \ubc88\uc9f8 \uc778\uc790\uac00 \ub418\uc5b4\uc57c \ud569\ub2c8\ub2e4."},

  
  /** Invalid element name specified       */
  //public static final int ER_INVALID_ELEMENT_NAME = 120;


  {
    ER_INVALID_ELEMENT_NAME,
      "\uc798\ubabb\ub41c \uc774\ub984 \uc694\uc18c {0}\uc774(\uac00) \uc9c0\uc815\ub418\uc5c8\uc2b5\ub2c8\ub2e4.   "},

  
   /** Element name method must be static      */
  //public static final int ER_ELEMENT_NAME_METHOD_STATIC = 121;


  {
    ER_ELEMENT_NAME_METHOD_STATIC,
      "\uc774\ub984 \uc694\uc18c \uba54\uc18c\ub4dc\ub294 \uc815\uc801 {0}\uc774\uc5b4\uc57c \ud569\ub2c8\ub2e4."},

  
   /** Extension function {0} : {1} is unknown      */
  //public static final int ER_EXTENSION_FUNC_UNKNOWN = 122;


  {
    ER_EXTENSION_FUNC_UNKNOWN,
             "\ud655\uc7a5 \uae30\ub2a5 {0} : {1}\uc744(\ub97c) \uc54c \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},

  
   /** More than one best match for constructor for       */
  //public static final int ER_MORE_MATCH_CONSTRUCTOR = 123;


  {
    ER_MORE_MATCH_CONSTRUCTOR,
             "{0}\uc758 \uad6c\uc131\uc790\uc640 \uc77c\uce58\ud558\ub294 \uac83\uc774 \ub450 \uac1c \uc774\uc0c1\uc785\ub2c8\ub2e4.   "},

  
   /** More than one best match for method      */
  //public static final int ER_MORE_MATCH_METHOD = 124;


  {
    ER_MORE_MATCH_METHOD,
             "{0} \uba54\uc18c\ub4dc\uc640 \uc77c\uce58\ud558\ub294 \uac83\uc774 \ub450 \uac1c \uc774\uc0c1\uc785\ub2c8\ub2e4"},

  
   /** More than one best match for element method      */
  //public static final int ER_MORE_MATCH_ELEMENT = 125;


  {
    ER_MORE_MATCH_ELEMENT,
             "{0} \uc694\uc18c \uba54\uc18c\ub4dc\uc640 \uc77c\uce58\ud558\ub294 \uac83\uc774 \ub450 \uac1c \uc774\uc0c1\uc785\ub2c8\ub2e4"},

  
   /** Invalid context passed to evaluate       */
  //public static final int ER_INVALID_CONTEXT_PASSED = 126;


  {
    ER_INVALID_CONTEXT_PASSED,
             "{0} \ud3c9\uac00\ub97c \uc704\ud574 \uc804\ub2ec\ub41c \ucee8\ud14d\uc2a4\ud2b8\uac00 \uc798\ubabb\ub418\uc5c8\uc2b5\ub2c8\ub2e4."},

  
   /** Pool already exists       */
  //public static final int ER_POOL_EXISTS = 127;


  {
    ER_POOL_EXISTS,
             "\ud480\uc774 \uc774\ubbf8 \uc788\uc2b5\ub2c8\ub2e4"},

  
   /** No driver Name specified      */
  //public static final int ER_NO_DRIVER_NAME = 128;


  {
    ER_NO_DRIVER_NAME,
             "\uc9c0\uc815\ub41c \ub4dc\ub77c\uc774\ubc84 \uc774\ub984\uc774 \uc5c6\uc2b5\ub2c8\ub2e4"},

  
   /** No URL specified     */
  //public static final int ER_NO_URL = 129;


  {
    ER_NO_URL,
             "\uc9c0\uc815\ub41c URL\uc774 \uc5c6\uc2b5\ub2c8\ub2e4"},

  
   /** Pool size is less than one    */
  //public static final int ER_POOL_SIZE_LESSTHAN_ONE = 130;


  {
    ER_POOL_SIZE_LESSTHAN_ONE,
             "\ud480 \ud06c\uae30\uac00 1\ubcf4\ub2e4 \uc791\uc2b5\ub2c8\ub2e4!"},

  
   /** Invalid driver name specified    */
  //public static final int ER_INVALID_DRIVER = 131;


  {
    ER_INVALID_DRIVER,
             "\uc9c0\uc815\ub41c \ub4dc\ub77c\uc774\ubc84 \uc774\ub984\uc774 \uc798\ubabb\ub418\uc5c8\uc2b5\ub2c8\ub2e4!"},

  
   /** Did not find the stylesheet root    */
  //public static final int ER_NO_STYLESHEETROOT = 132;


  {
    ER_NO_STYLESHEETROOT,
             "\uc2a4\ud0c0\uc77c \uc2dc\ud2b8 \ub8e8\ud2b8\ub97c \ucc3e\uc744 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4!"},

  
   /** Illegal value for xml:space     */
  //public static final int ER_ILLEGAL_XMLSPACE_VALUE = 133;


  {
    ER_ILLEGAL_XMLSPACE_VALUE,
         "xml:space\uc5d0 \ub300\ud55c \uac12\uc774 \uc798\ubabb\ub418\uc5c8\uc2b5\ub2c8\ub2e4"},

  
   /** processFromNode failed     */
  //public static final int ER_PROCESSFROMNODE_FAILED = 134;


  {
    ER_PROCESSFROMNODE_FAILED,
         "processFromNode\uac00 \uc2e4\ud328\ud588\uc2b5\ub2c8\ub2e4"},

  
   /** The resource [] could not load:     */
  //public static final int ER_RESOURCE_COULD_NOT_LOAD = 135;


  {
    ER_RESOURCE_COULD_NOT_LOAD,
        "[ {0} ] \uc790\uc6d0\uc774 \ub2e4\uc74c\uc744 \ub85c\ub4dc\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4: {1} \n {2} \t {3}"},

   
  
   /** Buffer size <=0     */
  //public static final int ER_BUFFER_SIZE_LESSTHAN_ZERO = 136;


  {
    ER_BUFFER_SIZE_LESSTHAN_ZERO,
        "\ubc84\ud37c \ud06c\uae30 <=0"},

  
   /** Unknown error when calling extension    */
  //public static final int ER_UNKNOWN_ERROR_CALLING_EXTENSION = 137;


  {
    ER_UNKNOWN_ERROR_CALLING_EXTENSION,
        "\ud655\uc7a5\uc790 \ud638\ucd9c \uc911 \uc54c \uc218 \uc5c6\ub294 \uc624\ub958\uac00 \ubc1c\uc0dd\ud588\uc2b5\ub2c8\ub2e4"},

  
   /** Prefix {0} does not have a corresponding namespace declaration    */
  //public static final int ER_NO_NAMESPACE_DECL = 138;


  {
    ER_NO_NAMESPACE_DECL,
        "{0} \uc811\ub450\uc5b4\uc5d0 \uad00\ub828 \uc774\ub984 \uacf5\uac04 \uc120\uc5b8\uc774 \uc5c6\uc2b5\ub2c8\ub2e4."},

  
   /** Element content not allowed for lang=javaclass   */
  //public static final int ER_ELEM_CONTENT_NOT_ALLOWED = 139;


  {
    ER_ELEM_CONTENT_NOT_ALLOWED,
        "lang=javaclass {0}\uc5d0 \uc694\uc18c \ucee8\ud150\ud2b8\uac00 \ud5c8\uc6a9\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4."},
     
  
   /** Stylesheet directed termination   */
  //public static final int ER_STYLESHEET_DIRECTED_TERMINATION = 140;


  {
    ER_STYLESHEET_DIRECTED_TERMINATION,
        "\uc2a4\ud0c0\uc77c \uc2dc\ud2b8\uac00 \uc885\ub8cc\ub85c \uc9c0\uc815\ub418\uc5c8\uc2b5\ub2c8\ub2e4"},

  
   /** 1 or 2   */
  //public static final int ER_ONE_OR_TWO = 141;


  {
    ER_ONE_OR_TWO,
        "1 \ub610\ub294 2"},

  
   /** 2 or 3   */
  //public static final int ER_TWO_OR_THREE = 142;


  {
    ER_TWO_OR_THREE,
        "2 or 3"},

  
   /** Could not load {0} (check CLASSPATH), now using just the defaults   */
  //public static final int ER_COULD_NOT_LOAD_RESOURCE = 143;


  {
    ER_COULD_NOT_LOAD_RESOURCE,
        "\uae30\ubcf8\uac12\ub9cc\uc744 \uc0ac\uc6a9\ud558\uc5ec {0}\uc744(\ub97c) \ub85c\ub4dc\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4(CLASSPATH\ub97c \ud655\uc778\ud558\uc2ed\uc2dc\uc624)."},

  
   /** Cannot initialize default templates   */
  //public static final int ER_CANNOT_INIT_DEFAULT_TEMPLATES = 144;


  {
    ER_CANNOT_INIT_DEFAULT_TEMPLATES,
        "\uae30\ubcf8 \ud15c\ud50c\ub9ac\ud2b8\ub97c \ucd08\uae30\ud654\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4"},

  
   /** Result should not be null   */
  //public static final int ER_RESULT_NULL = 145;


  {
    ER_RESULT_NULL,
        "\uacb0\uacfc\ub294 \ub110\uc774 \ub420 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4"},

    
   /** Result could not be set   */
  //public static final int ER_RESULT_COULD_NOT_BE_SET = 146;


  {
    ER_RESULT_COULD_NOT_BE_SET,
        "\uacb0\uacfc\ub97c \uc124\uc815\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4"},

  
   /** No output specified   */
  //public static final int ER_NO_OUTPUT_SPECIFIED = 147;


  {
    ER_NO_OUTPUT_SPECIFIED,
        "\uc9c0\uc815\ub41c \ucd9c\ub825\uc774 \uc5c6\uc2b5\ub2c8\ub2e4"},

  
   /** Can't transform to a Result of type   */
  //public static final int ER_CANNOT_TRANSFORM_TO_RESULT_TYPE = 148;


  {
    ER_CANNOT_TRANSFORM_TO_RESULT_TYPE,
        "\uc720\ud615\uc774 {0}\uc778 \uacb0\uacfc\ub85c \ubcc0\ud658\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},

  
   /** Can't transform to a Source of type   */
  //public static final int ER_CANNOT_TRANSFORM_SOURCE_TYPE = 149;


  {
    ER_CANNOT_TRANSFORM_SOURCE_TYPE,
        "\uc720\ud615\uc774 {0}\uc778 \uc18c\uc2a4\ub85c \ubcc0\ud658\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4. "},

  
   /** Null content handler  */
  //public static final int ER_NULL_CONTENT_HANDLER = 150;


  {
    ER_NULL_CONTENT_HANDLER,
        "\ucee8\ud150\ud2b8 \ucc98\ub9ac\uae30\uac00 \ub110\uc785\ub2c8\ub2e4"},

  
   /** Null error handler  */
  //public static final int ER_NULL_ERROR_HANDLER = 151;


  {
    ER_NULL_ERROR_HANDLER,
        "\uc624\ub958 \ucc98\ub9ac\uae30\uac00 \ub110\uc785\ub2c8\ub2e4"},

  
   /** parse can not be called if the ContentHandler has not been set */
  //public static final int ER_CANNOT_CALL_PARSE = 152;


  {
    ER_CANNOT_CALL_PARSE,
        "ContentHandler\ub97c \uc124\uc815\ud558\uc9c0 \uc54a\uc73c\uba74 \uad6c\ubb38 \ubd84\uc11d\uc774 \ud638\ucd9c\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4."},

  
   /**  No parent for filter */
  //public static final int ER_NO_PARENT_FOR_FILTER = 153;


  {
    ER_NO_PARENT_FOR_FILTER,
        "\ud544\ud130\uc5d0 \ub300\ud55c \ubd80\ubaa8\uac00 \uc5c6\uc2b5\ub2c8\ub2e4"},

  
  
   /**  No stylesheet found in: {0}, media */
  //public static final int ER_NO_STYLESHEET_IN_MEDIA = 154;


  {
    ER_NO_STYLESHEET_IN_MEDIA,
         "{0}\uc5d0 \uc2a4\ud0c0\uc77c \uc2dc\ud2b8\uac00 \uc5c6\uc2b5\ub2c8\ub2e4. \ub9e4\uccb4= {1}"},

  
   /**  No xml-stylesheet PI found in */
  //public static final int ER_NO_STYLESHEET_PI = 155;


  {
    ER_NO_STYLESHEET_PI,
         "{0}\uc5d0 xml-stylesheet PI\uac00 \uc5c6\uc2b5\ub2c8\ub2e4. "},

  
   /**  Not supported  */
  //public static final int ER_NOT_SUPPORTED = 171;


  {
    ER_NOT_SUPPORTED,
       "\uc9c0\uc6d0\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4: {0}"},

  
   /**  Value for property {0} should be a Boolean instance  */
  //public static final int ER_PROPERTY_VALUE_BOOLEAN = 177;


  {
    ER_PROPERTY_VALUE_BOOLEAN,
       "{0} \ud2b9\uc131\uc5d0 \ub300\ud55c \uac12\uc774 \ubd80\uc6b8 \uc778\uc2a4\ud134\uc2a4\uc5ec\uc57c \ud569\ub2c8\ub2e4."},

  
    /* This key/message changed ,NEED ER_COULD_NOT_FIND_EXTERN_SCRIPT: Pending,Ramesh */

   /** src attribute not yet supported for  */
  //public static final int ER_SRC_ATTRIB_NOT_SUPPORTED = 195;


  {
    "ER_SRC_ATTRIB_NOT_SUPPORTED",
       "{0}\uc5d0\uc11c \uc678\ubd80 \uc2a4\ud06c\ub9bd\ud2b8\ub97c \uc5bb\uc744 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4"},

  
  /** The resource [] could not be found     */
  //public static final int ER_RESOURCE_COULD_NOT_FIND = 196;


  {
    ER_RESOURCE_COULD_NOT_FIND,
        "[ {0} ] \uc790\uc6d0\uc744 \ucc3e\uc744 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4.\n {1}"},

  
   /** output property not recognized:  */
  //public static final int ER_OUTPUT_PROPERTY_NOT_RECOGNIZED = 197;


  {
    ER_OUTPUT_PROPERTY_NOT_RECOGNIZED,
        "\ucd9c\ub825 \ud2b9\uc131\uc744 \uc778\uc2dd\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4: {0}"},

  
   /** Failed creating ElemLiteralResult instance   */
  //public static final int ER_FAILED_CREATING_ELEMLITRSLT = 203;


  {
    ER_FAILED_CREATING_ELEMLITRSLT,
        "ElemLiteralResult \uc778\uc2a4\ud134\uc2a4 \uc791\uc131\uc5d0 \uc2e4\ud328\ud588\uc2b5\ub2c8\ub2e4"},
    
  
  // Earlier (JDK 1.4 XALAN 2.2-D11) at key code '204' the key name was ER_PRIORITY_NOT_PARSABLE
  // In latest Xalan code base key name is  ER_VALUE_SHOULD_BE_NUMBER. This should also be taken care
  //in locale specific files like XSLTErrorResources_de.java, XSLTErrorResources_fr.java etc.
  //NOTE: Not only the key name but message has also been changed. - nb.

   /** Priority value does not contain a parsable number   */
  //public static final int ER_VALUE_SHOULD_BE_NUMBER = 204;


  {
    ER_VALUE_SHOULD_BE_NUMBER,
        "{0}\uc5d0 \ub300\ud55c \uac12\uc740 \uad6c\ubd84 \ubd84\uc11d\ud560 \uc218 \uc788\ub294 \uc22b\uc790\ub97c \ud3ec\ud568\ud574\uc57c \ud569\ub2c8\ub2e4."},

  
   /**  Value for {0} should equal 'yes' or 'no'   */
  //public static final int ER_VALUE_SHOULD_EQUAL = 205;


  {
    ER_VALUE_SHOULD_EQUAL,
        "{0}\uc5d0 \ub300\ud55c \uac12\uc740 \uc608 \ub610\ub294 \uc544\ub2c8\uc624\uc774\uc5b4\uc57c \ud569\ub2c8\ub2e4."},

 
   /**  Failed calling {0} method   */
  //public static final int ER_FAILED_CALLING_METHOD = 206;


  {
    ER_FAILED_CALLING_METHOD,
        "{0} \uba54\uc18c\ub4dc \ud638\ucd9c\uc5d0 \uc2e4\ud328\ud588\uc2b5\ub2c8\ub2e4"},

  
   /** Failed creating ElemLiteralResult instance   */
  //public static final int ER_FAILED_CREATING_ELEMTMPL = 207;


  {
    ER_FAILED_CREATING_ELEMTMPL,
        "ElemTemplateElement \uc778\uc2a4\ud134\uc2a4 \uc791\uc131\uc5d0 \uc2e4\ud328\ud588\uc2b5\ub2c8\ub2e4"},

  
   /**  Characters are not allowed at this point in the document   */
  //public static final int ER_CHARS_NOT_ALLOWED = 208;


  {
    ER_CHARS_NOT_ALLOWED,
        "\uc774 \ubb38\uc11c\uc5d0\uc11c \ubb38\uc790\uac00 \ud5c8\uc6a9\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4"},

  
  /**  attribute is not allowed on the element   */
  //public static final int ER_ATTR_NOT_ALLOWED = 209;


  {
    ER_ATTR_NOT_ALLOWED,
        "\"{0}\" \uc18d\uc131\uc774 {1} \uc694\uc18c\uc5d0 \ud5c8\uc6a9\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4!"},

 
  /**  Bad value    */
  //public static final int ER_BAD_VALUE = 211;


  {
    ER_BAD_VALUE,
     "{0} \uc798\ubabb\ub41c \uac12 {1} "},

  
  /**  attribute value not found   */
  //public static final int ER_ATTRIB_VALUE_NOT_FOUND = 212;


  {
    ER_ATTRIB_VALUE_NOT_FOUND,
     "{0} \uc18d\uc131 \uac12\uc744 \ucc3e\uc744 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4 "},

  
  /**  attribute value not recognized    */
  //public static final int ER_ATTRIB_VALUE_NOT_RECOGNIZED = 213;


  {
    ER_ATTRIB_VALUE_NOT_RECOGNIZED,
     "{0} \uc18d\uc131 \uac12\uc744 \uc778\uc2dd\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4 "},

  
  /** Attempting to generate a namespace prefix with a null URI   */
  //public static final int ER_NULL_URI_NAMESPACE = 216;


  {
    ER_NULL_URI_NAMESPACE,
     "\ub110 URI\ub85c \uc774\ub984 \uacf5\uac04 \uc811\ub450\ubd80\ub97c \uc0dd\uc131\ud558\ub824\uace0 \uc2dc\ub3c4 \uc911\uc785\ub2c8\ub2e4"},
      
  
    // Following are the new ERROR keys added in XALAN code base after Jdk 1.4 (Xalan 2.2-D11)

  /** Attempting to generate a namespace prefix with a null URI   */
  //public static final int ER_NUMBER_TOO_BIG = 217;


  {
    ER_NUMBER_TOO_BIG,
     "\uac00\uc7a5 \ud070 Long \uc815\uc218\ubcf4\ub2e4 \ud070 \uc22b\uc790 \uc11c\uc2dd\uc744 \uc9c0\uc815\ud558\ub824\uace0 \uc2dc\ub3c4 \uc911\uc785\ub2c8\ub2e4."},


//ER_CANNOT_FIND_SAX1_DRIVER

  //public static final int  ER_CANNOT_FIND_SAX1_DRIVER = 218;


  {
    ER_CANNOT_FIND_SAX1_DRIVER,
     "SAX1 \ub4dc\ub77c\uc774\ubc84 \ud074\ub798\uc2a4 {0}\uc744(\ub97c) \ucc3e\uc744 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},


//ER_SAX1_DRIVER_NOT_LOADED
  //public static final int  ER_SAX1_DRIVER_NOT_LOADED = 219;


  {
    ER_SAX1_DRIVER_NOT_LOADED,
     "SAX1 \ub4dc\ub77c\uc774\ubc84 \ud074\ub798\uc2a4 {0}\uc744(\ub97c) \ucc3e\uc558\uc9c0\ub9cc \ub85c\ub4dc\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},


//ER_SAX1_DRIVER_NOT_INSTANTIATED
  //public static final int  ER_SAX1_DRIVER_NOT_INSTANTIATED = 220 ;


  {
    ER_SAX1_DRIVER_NOT_INSTANTIATED,
     "SAX1 \ub4dc\ub77c\uc774\ubc84 \ud074\ub798\uc2a4 {0}\uc744(\ub97c) \ub85c\ub4dc\ud588\uc9c0\ub9cc \uc778\uc2a4\ud134\uc2a4\ud654\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},



// ER_SAX1_DRIVER_NOT_IMPLEMENT_PARSER
  //public static final int ER_SAX1_DRIVER_NOT_IMPLEMENT_PARSER = 221;


  {
    ER_SAX1_DRIVER_NOT_IMPLEMENT_PARSER,
     "SAX1 \ub4dc\ub77c\uc774\ubc84 \ud074\ub798\uc2a4 {0}\uc740(\ub294) org.xml.sax.Parser\ub97c \uad6c\ud604\ud558\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4."},


// ER_PARSER_PROPERTY_NOT_SPECIFIED
  //public static final int  ER_PARSER_PROPERTY_NOT_SPECIFIED = 222;


  {
    ER_PARSER_PROPERTY_NOT_SPECIFIED,
     "org.xml.sax.parser \uc2dc\uc2a4\ud15c \ud2b9\uc131\uc774 \uc9c0\uc815\ub418\uc5b4 \uc788\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4."},


//ER_PARSER_ARG_CANNOT_BE_NULL
  //public static final int  ER_PARSER_ARG_CANNOT_BE_NULL = 223 ;


  {
    ER_PARSER_ARG_CANNOT_BE_NULL,
     "Parser \uc778\uc790\ub294 \ub110\uc774 \uc544\ub2c8\uc5b4\uc57c \ud569\ub2c8\ub2e4."},



// ER_FEATURE
  //public static final int  ER_FEATURE = 224;


  {
    ER_FEATURE,
     "\uae30\ub2a5: {0}"},



// ER_PROPERTY
  //public static final int ER_PROPERTY = 225 ;


  {
    ER_PROPERTY,
     "\ud2b9\uc131: {0}"},


// ER_NULL_ENTITY_RESOLVER
  //public static final int ER_NULL_ENTITY_RESOLVER  = 226;


  {
    ER_NULL_ENTITY_RESOLVER,
     "\ub110 \uc5d4\ud2f0\ud2f0 \uacb0\uc815\uc790"},


// ER_NULL_DTD_HANDLER
  //public static final int  ER_NULL_DTD_HANDLER = 227 ;


  {
    ER_NULL_DTD_HANDLER,
     "\ub110 DTD \ucc98\ub9ac\uae30"},


// No Driver Name Specified!
  //public static final int ER_NO_DRIVER_NAME_SPECIFIED = 228;

  {
    ER_NO_DRIVER_NAME_SPECIFIED,
     "\ub4dc\ub77c\uc774\ubc84 \uc774\ub984\uc774 \uc9c0\uc815\ub418\uc9c0 \uc54a\uc74c!"},



// No URL Specified!
  //public static final int ER_NO_URL_SPECIFIED = 229;

  {
    ER_NO_URL_SPECIFIED,
     "URL\uc774 \uc9c0\uc815\ub418\uc9c0 \uc54a\uc74c!"},



// Pool size is less than 1!
  //public static final int ER_POOLSIZE_LESS_THAN_ONE = 230;

  {
    ER_POOLSIZE_LESS_THAN_ONE,
     "\ud480 \ud06c\uae30\uac00 1\ubcf4\ub2e4 \uc791\uc74c!"},



// Invalid Driver Name Specified!
  //public static final int ER_INVALID_DRIVER_NAME = 231;

  {
    ER_INVALID_DRIVER_NAME,
     "\uc798\ubabb\ub41c \ub4dc\ub77c\uc774\ube0c \uc774\ub984\uc774 \uc9c0\uc815\ub428!"},




// ErrorListener
  //public static final int ER_ERRORLISTENER = 232;

  {
    ER_ERRORLISTENER,
     "\uc624\ub958 \uc218\uc2e0\uae30"},



// Programmer's error! expr has no ElemTemplateElement parent!
  //public static final int ER_ASSERT_NO_TEMPLATE_PARENT = 233;

  {
    ER_ASSERT_NO_TEMPLATE_PARENT,
     "\ud504\ub85c\uadf8\ub798\uba38 \uc624\ub958! \uc2dd\uc5d0 ElemTemplateElement \ubd80\ubaa8\uac00 \uc5c6\uc74c!"},



// Programmer's assertion in RundundentExprEliminator: {0}
  //public static final int ER_ASSERT_REDUNDENT_EXPR_ELIMINATOR = 234;

  {
    ER_ASSERT_REDUNDENT_EXPR_ELIMINATOR,
     "RundundentExprEliminator\uc758 \ud504\ub85c\uadf8\ub798\uba38 \uba85\uc81c: {0}"},


  // {0}is not allowed in this position in the stylesheet!
  //public static final int ER_NOT_ALLOWED_IN_POSITION = 237;

  {
    ER_NOT_ALLOWED_IN_POSITION,
     "\uc2a4\ud0c0\uc77c \uc2dc\ud2b8\uc758 \uc774 \uc704\uce58\uc5d0\ub294 {0}\uc744(\ub97c) \uc0ac\uc6a9\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4!"},


  // Non-whitespace text is not allowed in this position in the stylesheet!
  //public static final int ER_NONWHITESPACE_NOT_ALLOWED_IN_POSITION = 238;

  {
    ER_NONWHITESPACE_NOT_ALLOWED_IN_POSITION,
     "\uc2a4\ud0c0\uc77c \uc2dc\ud2b8\uc758 \uc774 \uc704\uce58\uc5d0\ub294 \uacf5\ubc31\uc774 \uc544\ub2cc \ud14d\uc2a4\ud2b8\ub97c \uc0ac\uc6a9\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4!"},


  // This code is shared with warning codes.
  // Illegal value: {1} used for CHAR attribute: {0}.  An attribute of type CHAR must be only 1 character!
  //public static final int INVALID_TCHAR = 239;
  // SystemId Unknown

  {
    INVALID_TCHAR,
     "\uc798\ubabb\ub41c \uac12:  CHAR \uc18d\uc131 {0}\uc5d0 \uc0ac\uc6a9\ub41c {1}.  CHAR \uc720\ud615\uc758 \uc18d\uc131\uc740 1\uc790\uc5ec\uc57c \ud569\ub2c8\ub2e4!"},


    // Note to translators:  The following message is used if the value of
    // an attribute in a stylesheet is invalid.  "QNAME" is the XML data-type of
    // the attribute, and should not be translated.  The substitution text {1} is
    // the attribute value and {0} is the attribute name.
    // INVALID_QNAME

  //The following codes are shared with the warning codes...
  // Illegal value: {1} used for QNAME attribute: {0}
  //public static final int INVALID_QNAME = 242;

  {
    INVALID_QNAME,
     "\uc798\ubabb\ub41c \uac12: QNAME \uc18d\uc131 {0}\uc5d0 \uc0ac\uc6a9\ub41c {1}"},


    // Note to translators:  The following message is used if the value of
    // an attribute in a stylesheet is invalid.  "ENUM" is the XML data-type of
    // the attribute, and should not be translated.  The substitution text {1} is
    // the attribute value, {0} is the attribute name, and {2} is a list of valid
    // values.
    // INVALID_ENUM

  // Illegal value:a {1} used for ENUM attribute:a {0}.  Valid values are:a {2}.
  //public static final int INVALID_ENUM = 243;

  {
    INVALID_ENUM,
     "\uc798\ubabb\ub41c \uac12: ENUM \uc18d\uc131 {0}\uc5d0 \uc0ac\uc6a9\ub41c {1}.  \uc720\ud6a8\ud55c \uac12: {2}."},


// Note to translators:  The following message is used if the value of
// an attribute in a stylesheet is invalid.  "NMTOKEN" is the XML data-type
// of the attribute, and should not be translated.  The substitution text {1} is
// the attribute value and {0} is the attribute name.
// INVALID_NMTOKEN

  // Illegal value:a {1} used for NMTOKEN attribute:a {0}.
  //public static final int INVALID_NMTOKEN = 244;

  {
    INVALID_NMTOKEN,
     "\uc798\ubabb\ub41c \uac12: NMTOKEN \uc18d\uc131 {0}\uc5d0 \uc0ac\uc6a9\ub41c {1} "},


// Note to translators:  The following message is used if the value of
// an attribute in a stylesheet is invalid.  "NCNAME" is the XML data-type
// of the attribute, and should not be translated.  The substitution text {1} is
// the attribute value and {0} is the attribute name.
// INVALID_NCNAME

  // Illegal value:a {1} used for NCNAME attribute:a {0}.
  //public static final int INVALID_NCNAME = 245;

  {
    INVALID_NCNAME,
     "\uc798\ubabb\ub41c \uac12: NCNAME \uc18d\uc131 {0}\uc5d0 \uc0ac\uc6a9\ub41c {1} "},


// Note to translators:  The following message is used if the value of
// an attribute in a stylesheet is invalid.  "boolean" is the XSLT data-type
// of the attribute, and should not be translated.  The substitution text {1} is
// the attribute value and {0} is the attribute name.
// INVALID_BOOLEAN

  // Illegal value:a {1} used for boolean attribute:a {0}.
  //public static final int INVALID_BOOLEAN = 246;


  {
    INVALID_BOOLEAN,
     "\uc798\ubabb\ub41c \uac12: boolean \uc18d\uc131 {0}\uc5d0 \uc0ac\uc6a9\ub41c {1} "},


// Note to translators:  The following message is used if the value of
// an attribute in a stylesheet is invalid.  "number" is the XSLT data-type
// of the attribute, and should not be translated.  The substitution text {1} is
// the attribute value and {0} is the attribute name.
// INVALID_NUMBER

  // Illegal value:a {1} used for number attribute:a {0}.
  //public static final int INVALID_NUMBER = 247;

  {
    INVALID_NUMBER,
     "\uc798\ubabb\ub41c \uac12: number \uc18d\uc131 {0}\uc5d0 \uc0ac\uc6a9\ub41c {1} "},



  // End of shared codes...

// Note to translators:  A "match pattern" is a special form of XPath expression
// that is used for matching patterns.  The substitution text is the name of
// a function.  The message indicates that when this function is referenced in
// a match pattern, its argument must be a string literal (or constant.)
// ER_ARG_LITERAL - new error message for bugzilla //5202

  // Argument to {0} in match pattern must be a literal.
  //public static final int ER_ARG_LITERAL             = 248;

  {
    ER_ARG_LITERAL,
     "\uc77c\uce58 \ud328\ud134\uc5d0\uc11c {0}\uc5d0 \ub300\ud55c \uc778\uc790\ub294 \ub9ac\ud130\ub7f4\uc774\uc5b4\uc57c \ud569\ub2c8\ub2e4."},


// Note to translators:  The following message indicates that two definitions of
// a variable.  A "global variable" is a variable that is accessible everywher
// in the stylesheet.
// ER_DUPLICATE_GLOBAL_VAR - new error message for bugzilla #790

  // Duplicate global variable declaration.
  //public static final int ER_DUPLICATE_GLOBAL_VAR    = 249;

  {
    ER_DUPLICATE_GLOBAL_VAR,
     "\uc911\ubcf5\ub418\ub294 \uc804\uc5ed \ubcc0\uc218 \uc120\uc5b8"},



// Note to translators:  The following message indicates that two definitions of
// a variable were encountered.
// ER_DUPLICATE_VAR - new error message for bugzilla #790

  // Duplicate variable declaration.
  //public static final int ER_DUPLICATE_VAR           = 250;

  {
    ER_DUPLICATE_VAR,
     "\uc911\ubcf5\ub418\ub294 \ubcc0\uc218 \uc120\uc5b8"},


    // Note to translators:  "xsl:template, "name" and "match" are XSLT keywords
    // which must not be translated.
    // ER_TEMPLATE_NAME_MATCH - new error message for bugzilla #789

  // xsl:template must have a name or match attribute (or both)
  //public static final int ER_TEMPLATE_NAME_MATCH     = 251;

  {
    ER_TEMPLATE_NAME_MATCH,
     "xsl:template\uc740 name \ub610\ub294 match \uc18d\uc131(\ub610\ub294 \ubaa8\ub450)\uc744 \uac00\uc838\uc57c \ud569\ub2c8\ub2e4."},


    // Note to translators:  "exclude-result-prefixes" is an XSLT keyword which
    // should not be translated.  The message indicates that a namespace prefix
    // encountered as part of the value of the exclude-result-prefixes attribute
    // was in error.
    // ER_INVALID_PREFIX - new error message for bugzilla #788

  // Prefix in exclude-result-prefixes is not valid:a {0}
  //public static final int ER_INVALID_PREFIX          = 252;

  {
    ER_INVALID_PREFIX,
     "exclude-result-prefixes\uc758 \uc811\ub450\uc5b4\uac00 \uc798\ubabb\ub418\uc5c8\uc2b5\ub2c8\ub2e4: {0}"},


    // Note to translators:  An "attribute set" is a set of attributes that can be
    // added to an element in the output document as a group.  The message indicates
    // that there was a reference to an attribute set named {0} that was never
    // defined.
    // ER_NO_ATTRIB_SET - new error message for bugzilla #782

  // attribute-set named {0} does not exist
  //public static final int ER_NO_ATTRIB_SET           = 253;

  {
    ER_NO_ATTRIB_SET,
     "\uc774\ub984\uc774 {0}\uc778 \uc18d\uc131 \uc9d1\ud569\uc774 \uc5c6\uc2b5\ub2c8\ub2e4."},

  
  // Warnings...

  /** WG_FOUND_CURLYBRACE          */
  //public static final int WG_FOUND_CURLYBRACE = 1;


  {
    WG_FOUND_CURLYBRACE,
      "'}'\ub97c \ucc3e\uc558\uc73c\ub098 \uc18d\uc131 \ud15c\ud50c\ub9ac\ud2b8\ub97c \uc5f4 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4!"},


  /** WG_COUNT_ATTRIB_MATCHES_NO_ANCESTOR          */
  //public static final int WG_COUNT_ATTRIB_MATCHES_NO_ANCESTOR = 2;


  {
    WG_COUNT_ATTRIB_MATCHES_NO_ANCESTOR,
      "\uacbd\uace0: \uce74\uc6b4\ud2b8 \uc18d\uc131\uc774 xsl:number\uc758 \uc0c1\uc704\uc640 \uc77c\uce58\ud558\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4! \ub300\uc0c1 = {0}"},


  /** WG_EXPR_ATTRIB_CHANGED_TO_SELECT          */
  //public static final int WG_EXPR_ATTRIB_CHANGED_TO_SELECT = 3;


  {
    WG_EXPR_ATTRIB_CHANGED_TO_SELECT,
      "\uc774\uc804 \uad6c\ubb38: 'expr' \uc18d\uc131\uc758 \uc774\ub984\uc774 'select'\ub85c \ubcc0\uacbd\ub418\uc5c8\uc2b5\ub2c8\ub2e4."},


  /** WG_NO_LOCALE_IN_FORMATNUMBER          */
  //public static final int WG_NO_LOCALE_IN_FORMATNUMBER = 4;


  {
    WG_NO_LOCALE_IN_FORMATNUMBER,
      "Xalan\uc774 format-number \uae30\ub2a5\uc5d0\uc11c \ub85c\ucf08 \uc774\ub984\uc744 \uc544\uc9c1 \ucc98\ub9ac\ud558\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4."},


  /** WG_LOCALE_NOT_FOUND          */
  //public static final int WG_LOCALE_NOT_FOUND = 5;


  {
    WG_LOCALE_NOT_FOUND,
      "\uacbd\uace0: xml:lang={0}\uc5d0 \ub300\ud55c \ub85c\ucf08\uc744 \ucc3e\uc744 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},


  /** WG_CANNOT_MAKE_URL_FROM          */
  //public static final int WG_CANNOT_MAKE_URL_FROM = 6;


  {
    WG_CANNOT_MAKE_URL_FROM,
      "{0}\uc5d0\uc11c URL\uc744 \uc791\uc131\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},


  /** WG_CANNOT_LOAD_REQUESTED_DOC          */
  //public static final int WG_CANNOT_LOAD_REQUESTED_DOC = 7;


  {
    WG_CANNOT_LOAD_REQUESTED_DOC,
      "\uc694\uccad\ud55c \ubb38\uc11c {0}\uc744(\ub97c) \ub85c\ub4dc\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},


  /** WG_CANNOT_FIND_COLLATOR          */
  //public static final int WG_CANNOT_FIND_COLLATOR = 8;


  {
    WG_CANNOT_FIND_COLLATOR,
      "<sort xml:lang={0}\uc5d0 \ub300\ud55c \uc870\ud569\uae30\ub97c \ucc3e\uc744 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},


  /** WG_FUNCTIONS_SHOULD_USE_URL          */
  //public static final int WG_FUNCTIONS_SHOULD_USE_URL = 9;


  {
    WG_FUNCTIONS_SHOULD_USE_URL,
      "\uc774\uc804 \uad6c\ubb38: \ud568\uc218 \uc9c0\uc2dc\uc0ac\ud56d\uc740 {0} url\uc744 \uc0ac\uc6a9\ud574\uc57c \ud569\ub2c8\ub2e4."},


  /** WG_ENCODING_NOT_SUPPORTED_USING_UTF8          */
  //public static final int WG_ENCODING_NOT_SUPPORTED_USING_UTF8 = 10;


  {
    WG_ENCODING_NOT_SUPPORTED_USING_UTF8,
      "UTF-8\uc744 \uc0ac\uc6a9\ud558\uc5ec \ucf54\ub4dc\ud654\uac00 \uc9c0\uc6d0\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4: {0}"},


  /** WG_ENCODING_NOT_SUPPORTED_USING_JAVA          */
  //public static final int WG_ENCODING_NOT_SUPPORTED_USING_JAVA = 11;


  {
    WG_ENCODING_NOT_SUPPORTED_USING_JAVA,
      "Java {1}\uc744(\ub97c) \uc0ac\uc6a9\ud558\uc5ec \ucf54\ub4dc\ud654\uac00 \uc9c0\uc6d0\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4: {0} "},


  /** WG_SPECIFICITY_CONFLICTS          */
  //public static final int WG_SPECIFICITY_CONFLICTS = 12;


  {
    WG_SPECIFICITY_CONFLICTS,
      "\ud2b9\uc774\uc131 \ucda9\ub3cc\uc774 \ubc1c\uacac\ub418\uc5c8\uc2b5\ub2c8\ub2e4: \uc2a4\ud0c0\uc77c \uc2dc\ud2b8\uc5d0\uc11c \ub9c8\uc9c0\ub9c9\uc73c\ub85c \ubc1c\uacac\ub41c {0}\uc774(\uac00) \uc0ac\uc6a9\ub429\ub2c8\ub2e4."},


  /** WG_PARSING_AND_PREPARING          */
  //public static final int WG_PARSING_AND_PREPARING = 13;


  {
    WG_PARSING_AND_PREPARING,
      "========= {0}\uc744(\ub97c) \uad6c\ubb38 \ubd84\uc11d \ubc0f \uc900\ube44 \uc911 =========="},


  /** WG_ATTR_TEMPLATE          */
  //public static final int WG_ATTR_TEMPLATE = 14;


  {
    WG_ATTR_TEMPLATE, "Attr \ud15c\ud50c\ub9ac\ud2b8, {0}"},


  /** WG_CONFLICT_BETWEEN_XSLSTRIPSPACE_AND_XSLPRESERVESPACE          */
  //public static final int WG_CONFLICT_BETWEEN_XSLSTRIPSPACE_AND_XSLPRESERVESPACE = 15;


  {
    WG_CONFLICT_BETWEEN_XSLSTRIPSPACE_AND_XSLPRESERVESPACE,
      "xsl:strip-space \ubc0f xsl:preserve-space \uac04\uc758 \uc77c\uce58 \ucda9\ub3cc"},


  /** WG_ATTRIB_NOT_HANDLED          */
  //public static final int WG_ATTRIB_NOT_HANDLED = 16;


  {
    WG_ATTRIB_NOT_HANDLED,
      "Xalan\uc774 \uc544\uc9c1 {0} \uc18d\uc131\uc744 \ucc98\ub9ac\ud558\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4!"},


  /** WG_NO_DECIMALFORMAT_DECLARATION          */
  //public static final int WG_NO_DECIMALFORMAT_DECLARATION = 17;


  {
    WG_NO_DECIMALFORMAT_DECLARATION,
      "\uc2ed\uc9c4\uc218 \ud615\uc2dd {0}\uc5d0 \ub300\ud55c \uc120\uc5b8\uc744 \ucc3e\uc744 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},


  /** WG_OLD_XSLT_NS          */
  //public static final int WG_OLD_XSLT_NS = 18;


  {
    WG_OLD_XSLT_NS, "XSLT \uc774\ub984 \uacf5\uac04\uc774 \ube60\uc84c\uac70\ub098 \uc798\ubabb\ub418\uc5c8\uc2b5\ub2c8\ub2e4. "},


  /** WG_ONE_DEFAULT_XSLDECIMALFORMAT_ALLOWED          */
  //public static final int WG_ONE_DEFAULT_XSLDECIMALFORMAT_ALLOWED = 19;


  {
    WG_ONE_DEFAULT_XSLDECIMALFORMAT_ALLOWED,
      "\ud558\ub098\uc758 \uae30\ubcf8 xsl:decimal-format \uc120\uc5b8\ub9cc \ud5c8\uc6a9\ub429\ub2c8\ub2e4."},


  /** WG_XSLDECIMALFORMAT_NAMES_MUST_BE_UNIQUE          */
  //public static final int WG_XSLDECIMALFORMAT_NAMES_MUST_BE_UNIQUE = 20;


  {
    WG_XSLDECIMALFORMAT_NAMES_MUST_BE_UNIQUE,
      "xsl:decimal-format \uc774\ub984\uc740 \uace0\uc720\ud574\uc57c \ud569\ub2c8\ub2e4. \"{0}\"\uc740(\ub294) \uc911\ubcf5\ub429\ub2c8\ub2e4."},


  /** WG_ILLEGAL_ATTRIBUTE          */
  //public static final int WG_ILLEGAL_ATTRIBUTE = 21;


  {
    WG_ILLEGAL_ATTRIBUTE,
      "{0}\uc5d0 \uc798\ubabb\ub41c \uc18d\uc131 {1}\uc774(\uac00) \uc788\uc2b5\ub2c8\ub2e4."},


  /** WG_COULD_NOT_RESOLVE_PREFIX          */
  //public static final int WG_COULD_NOT_RESOLVE_PREFIX = 22;


  {
    WG_COULD_NOT_RESOLVE_PREFIX,
      "\uc774\ub984 \uacf5\uac04 \uc811\ub450\uc5b4 {0}\uc744(\ub97c) \uacb0\uc815\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4. \uc774 \ub178\ub4dc\ub294 \ubb34\uc2dc\ub429\ub2c8\ub2e4."},


  /** WG_STYLESHEET_REQUIRES_VERSION_ATTRIB          */
  //public static final int WG_STYLESHEET_REQUIRES_VERSION_ATTRIB = 23;


  {
    WG_STYLESHEET_REQUIRES_VERSION_ATTRIB,
      "xsl:stylesheet\uc5d0 'version' \uc18d\uc131\uc774 \ud544\uc694\ud569\ub2c8\ub2e4!"},


  /** WG_ILLEGAL_ATTRIBUTE_NAME          */
  //public static final int WG_ILLEGAL_ATTRIBUTE_NAME = 24;


  {
    WG_ILLEGAL_ATTRIBUTE_NAME,
      "\uc798\ubabb\ub41c \uc18d\uc131 \uc774\ub984: {0}"},


  /** WG_ILLEGAL_ATTRIBUTE_VALUE          */
  //public static final int WG_ILLEGAL_ATTRIBUTE_VALUE = 25;


  {
    WG_ILLEGAL_ATTRIBUTE_VALUE,
      "{0} \uc18d\uc131\uc5d0 \uc798\ubabb\ub41c \uac12 {1}\uc774(\uac00) \uc0ac\uc6a9\ub418\uc5c8\uc2b5\ub2c8\ub2e4"},


  /** WG_EMPTY_SECOND_ARG          */
  //public static final int WG_EMPTY_SECOND_ARG = 26;


  {
    WG_EMPTY_SECOND_ARG,
      "\ubb38\uc11c \uae30\ub2a5\uc758 \ub450 \ubc88\uc9f8 \uc778\uc790\uc758 \uacb0\uacfc\ub85c \ubc1c\uc0dd\ud55c \ub178\ub4dc \uc138\ud2b8\uac00 \ube44\uc5b4 \uc788\uc2b5\ub2c8\ub2e4. \uccab \ubc88\uc9f8 \uc778\uc790\uac00 \uc0ac\uc6a9\ub429\ub2c8\ub2e4."},


  // Following are the new WARNING keys added in XALAN code base after Jdk 1.4 (Xalan 2.2-D11)

    // Note to translators:  "name" and "xsl:processing-instruction" are keywords
    // and must not be translated.
    // WG_PROCESSINGINSTRUCTION_NAME_CANT_BE_XML


  /** WG_PROCESSINGINSTRUCTION_NAME_CANT_BE_XML          */
  //public static final int WG_PROCESSINGINSTRUCTION_NAME_CANT_BE_XML = 27;

  {
     WG_PROCESSINGINSTRUCTION_NAME_CANT_BE_XML,
      "xsl:processing-instruction \uc774\ub984\uc758 'name' \uc18d\uc131 \uac12\uc740 'xml'\uc774\uc5b4\uc11c\ub294 \uc548\ub429\ub2c8\ub2e4."},


    // Note to translators:  "name" and "xsl:processing-instruction" are keywords
    // and must not be translated.  "NCName" is an XML data-type and must not be
    // translated.
    // WG_PROCESSINGINSTRUCTION_NOTVALID_NCNAME

  /** WG_PROCESSINGINSTRUCTION_NOTVALID_NCNAME          */
  //public static final int WG_PROCESSINGINSTRUCTION_NOTVALID_NCNAME = 28;

  {
     WG_PROCESSINGINSTRUCTION_NOTVALID_NCNAME,
      "xsl:processing-instruction\uc758 'name' \uc18d\uc131 \uac12\uc740 \uc720\ud6a8\ud55c NCName\uc774\uc5b4\uc57c \ud569\ub2c8\ub2e4: {0}"},


    // Note to translators:  This message is reported if the stylesheet that is
    // being processed attempted to construct an XML document with an attribute in a
    // place other than on an element.  The substitution text specifies the name of
    // the attribute.
    // WG_ILLEGAL_ATTRIBUTE_POSITION

  /** WG_ILLEGAL_ATTRIBUTE_POSITION         */
  //public static final int WG_ILLEGAL_ATTRIBUTE_POSITION = 29;

  {
    WG_ILLEGAL_ATTRIBUTE_POSITION,
      "\ud558\uc704 \ub178\ub4dc \ub2e4\uc74c\uc774\ub098 \ub610\ub294 \uc694\uc18c\uac00 \ub9cc\ub4e4\uc5b4\uc9c0\uae30 \uc804\uc5d0 \uc18d\uc131 {0}\uc744(\ub97c) \ucd94\uac00\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4.  \uc18d\uc131\uc774 \ubb34\uc2dc\ub429\ub2c8\ub2e4."},


    // WHY THERE IS A GAP B/W NUMBERS in the XSLTErrorResources properties file?

  // Other miscellaneous text used inside the code...
  { "ui_language", "ko"},
  { "help_language", "ko"},
  { "language", "ko"},
    { "BAD_CODE",
      "createMessage\uc758 \ub9e4\uac1c\ubcc0\uc218\uac00 \ubc14\uc6b4\ub4dc\ub97c \ubc97\uc5b4\ub0ac\uc2b5\ub2c8\ub2e4."},
    { "FORMAT_FAILED",
      "messageFormat \ud638\ucd9c \uc2dc \uc608\uc678 \ubc1c\uc0dd"},
    { "version", ">>>>>>> Xalan \ubc84\uc804 "},
    { "version2", "<<<<<<<"},
    { "yes", "\uc608"},
    { "line", "\ud589 #"},
    { "column", "\uc5f4 #"},
    { "xsldone", "XSLProcessor: \uc644\ub8cc"},
    { "xslProc_option",
    "Xalan-J \uba85\ub839\uc904 \ud504\ub85c\uc138\uc2a4 \ud074\ub798\uc2a4 \uc635\uc158:"},
    { "optionIN", "    -IN inputXMLURL"},
    { "optionXSL", "   [-XSL XSLTransformationURL]"},
    { "optionOUT", "   [-OUT outputFileName]"},
    { "optionLXCIN",
      "   [-LXCIN compiledStylesheetFileNameIn]"},
    { "optionLXCOUT",
      "   [-LXCOUT compiledStylesheetFileNameOutOut]"},
    { "optionPARSER",
      "   [-PARSER \uad6c\ubb38 \ubd84\uc11d\uae30 liaison\uc758 \uc644\uc804\ud55c \ud074\ub798\uc2a4 \uc774\ub984]"},
    { "optionE",
    "   [-E (\uc5d4\ud2f0\ud2f0 refs\ub97c \ud655\uc7a5\ud558\uc9c0 \ub9c8\uc2ed\uc2dc\uc624.)]"},
    { "optionV",
    "   [-E (\uc5d4\ud2f0\ud2f0 refs\ub97c \ud655\uc7a5\ud558\uc9c0 \ub9c8\uc2ed\uc2dc\uc624.)]"},
    { "optionQC",
      "   [-QC (\ubb34\uc74c \ud328\ud134 \ucda9\ub3cc \uacbd\uace0)]"},
    { "optionQ", "   [-Q  (\ubb34\uc74c \ubaa8\ub4dc)]"},
    { "optionLF",
      "   [-LF (\ucd9c\ub825\uc5d0\uc11c\ub9cc \uc904 \ubc14\uafc8\uc744 \uc0ac\uc6a9\ud558\uc2ed\uc2dc\uc624. {\uae30\ubcf8\uac12\uc740 CR/LF\uc785\ub2c8\ub2e4.})]"},
    { "optionCR",
      "   [-CR (\ucd9c\ub825\uc5d0\uc11c\ub9cc \uce90\ub9ac\uc9c0 \ub9ac\ud134\uc744 \uc0ac\uc6a9\ud558\uc2ed\uc2dc\uc624. {\uae30\ubcf8\uac12\uc740 CR/LF\uc785\ub2c8\ub2e4.})]"},
    { "optionESCAPE",
      "   [-ESCAPE (\uc81c\uc5b4\ud560 \ubb38\uc790 {\uae30\ubcf8\uac12\uc740 <>&\"\'\\r\\n}]"},
    { "optionINDENT",
      "   [-INDENT (\ub4e4\uc5ec\uc4f0\uae30\ud560 \uacf5\ubc31 \uc218\ub97c \uc81c\uc5b4\ud569\ub2c8\ub2e4. {\uae30\ubcf8\uac12\uc740 0\uc785\ub2c8\ub2e4.})]"},
    { "optionTT",
      "   [-TT (\ud638\ucd9c\ub420 \ub54c \ud15c\ud50c\ub9ac\ud2b8\ub97c \ucd94\uc801\ud569\ub2c8\ub2e4.)]"},
    { "optionTG",
      "   [-TG (\uac01 \uc0dd\uc131 \uc774\ubca4\ud2b8\ub97c \ucd94\uc801\ud569\ub2c8\ub2e4.)]"},
    { "optionTS",
    "   [-TS (\uac01 \uc120\ud0dd \uc774\ubca4\ud2b8\ub97c \ucd94\uc801\ud569\ub2c8\ub2e4.)]"},
    { "optionTTC",
      "   [-TTC (\ucc98\ub9ac\ub420 \ub54c \ud15c\ud50c\ub9ac\ud2b8 \uc790\uc2dd\uc744 \ucd94\uc801\ud569\ub2c8\ub2e4.)]"},
    { "optionTCLASS",
      "   [-TCLASS (\ucd94\uc801 \ud655\uc7a5\uc5d0 \ub300\ud55c TraceListener \ud074\ub798\uc2a4\uc785\ub2c8\ub2e4.)]"},
    { "optionVALIDATE",
      "   [-VALIDATE (\uac80\uc99d \uc5ec\ubd80\ub97c \uc124\uc815\ud569\ub2c8\ub2e4. \uae30\ubcf8\uac12\uc740 \uac80\uc99d\uc774 \ubc1c\uc0dd\ud558\uc9c0 \uc54a\ub294 \uac83\uc785\ub2c8\ub2e4.)]"},
    { "optionEDUMP",
      "   [-EDUMP {\uc120\ud0dd\uc801 \ud30c\uc77c \uc774\ub984} (\uc624\ub958\uac00 \ubc1c\uc0dd\ud558\uba74 \uc2a4\ud0dd\ub364\ud504 \ud558\uc2ed\uc2dc\uc624.)]"},
    { "optionXML",
      "   [-XML (XML \ud3ec\ub9e4\ud130\ub97c \uc0ac\uc6a9\ud558\uc5ec XML \ud5e4\ub354\ub97c \ucd94\uac00\ud558\uc2ed\uc2dc\uc624.)]"},
    { "optionTEXT",
      "   [-TEXT (\ub2e8\uc21c \ud14d\uc2a4\ud2b8 \ud3ec\ub9e4\ud130\ub97c \uc0ac\uc6a9\ud558\uc2ed\uc2dc\uc624.)]"},
    { "optionHTML",
    "   [-HTML (HTML \ud3ec\ub9e4\ud130\ub97c \uc0ac\uc6a9\ud558\uc2ed\uc2dc\uc624.)]"},
    { "optionPARAM",
      "   [-PARAM \uc774\ub984 \ud45c\ud604\uc2dd (\uc2a4\ud0c0\uc77c \uc2dc\ud2b8 \ub9e4\uac1c\ubcc0\uc218\ub97c \uc124\uc815\ud558\uc2ed\uc2dc\uc624.)]"},
    { "noParsermsg1",
    "XSL \ud504\ub85c\uc138\uc2a4\uac00 \uc131\uacf5\ud558\uc9c0 \ubabb\ud588\uc2b5\ub2c8\ub2e4."},
    { "noParsermsg2",
    "** \uad6c\ubb38 \ubd84\uc11d\uae30\ub97c \ucc3e\uc744 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4 **"},
    { "noParsermsg3",
    "\ud074\ub798\uc2a4 \uacbd\ub85c\ub97c \ud655\uc778\ud558\uc2ed\uc2dc\uc624."},
    { "noParsermsg4",
      "Java\uc6a9 IBM XML \uad6c\ubb38 \ubd84\uc11d\uae30\uac00 \uc5c6\ub294 \uacbd\uc6b0 \ub2e4\uc74c\uc5d0\uc11c \ub2e4\uc6b4\ub85c\ub4dc\ud560 \uc218 \uc788\uc2b5\ub2c8\ub2e4."},
    { "noParsermsg5",
      "IBM AlphaWorks: http://www.alphaworks.ibm.com/formula/xml"},
    { "optionURIRESOLVER",
    "   [-URIRESOLVER \uc804\uccb4 \ud074\ub798\uc2a4 \uc774\ub984(URI\ub97c \uacb0\uc815\ud558\ub294 \ub370 \uc0ac\uc6a9\ub418\ub294 URIResolver)]"},
    { "optionENTITYRESOLVER",
    "   [-ENTITYRESOLVER \uc804\uccb4 \ud074\ub798\uc2a4 \uc774\ub984(\uc5d4\ud2f0\ud2f0\ub97c \uacb0\uc815\ud558\ub294 \ub370 \uc0ac\uc6a9\ub418\ub294 EntityResolver)]"},
    {  "optionCONTENTHANDLER",
    "   [-CONTENTHANDLER \uc804\uccb4 \ud074\ub798\uc2a4 \uc774\ub984(\ucd9c\ub825\uc744 \uc77c\ub828\ud654\ud558\ub294 \ub370 \uc0ac\uc6a9\ub418\ub294 ContentHandler)]"},
    { "optionLINENUMBERS",
    "   [-L \uc18c\uc2a4 \ubb38\uc11c\uc758 \uc904 \ubc88\ud638 \uc0ac\uc6a9]"},
		
    // Following are the new options added in XSLTErrorResources.properties files after Jdk 1.4 (Xalan 2.2-D11)


    { "optionMEDIA",
    " [-MEDIA mediaType(media \uc18d\uc131\uc744 \uc0ac\uc6a9\ud558\uc5ec \ubb38\uc11c\uc640 \uad00\ub828\ub41c \uc2a4\ud0c0\uc77c \uc2dc\ud2b8\ub97c \ucc3e\uc2b5\ub2c8\ub2e4.)]"},
    { "optionFLAVOR",
    " [-FLAVOR flavorName(\ubcc0\ud658\ud558\ub824\uba74 \uba85\uc2dc\uc801\uc73c\ub85c s2s=SAX \ub610\ub294 d2d=DOM\uc744 \uc0ac\uc6a9\ud558\uc2ed\uc2dc\uc624.)] "}, // Added by sboag/scurcuru; experimental
    { "optionDIAG",
    " [-DIAG(\ubcc0\ud658\uc5d0 \uac78\ub9b0 \uc804\uccb4 \uc2dc\uac04(\ubc00\ub9ac\ucd08)\ub97c \uc778\uc1c4\ud569\ub2c8\ub2e4.)]"},
    { "optionINCREMENTAL",
    " [-INCREMENTAL (http://xml.apache.org/xalan/features/incremental\uc744 true\ub85c \uc124\uc815\ud558\uc5ec \uc99d\ubd84 DTP \uc0dd\uc131\uc744 \uc694\uccad\ud569\ub2c8\ub2e4.)]"},
    { "optionNOOPTIMIMIZE",
    " [-NOOPTIMIMIZE (http://xml.apache.org/xalan/features/optimize\ub97c false\ub85c \uc124\uc815\ud558\uc5ec \uc2a4\ud0c0\uc77c \uc2dc\ud2b8 \ucd5c\uc801\ud654 \ucc98\ub9ac\ub97c \uc694\uccad\ud558\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4.)]"},
    { "optionRL",
    " [-RL recursionlimit(\uc2a4\ud0c0\uc77c \uc2dc\ud2b8 \uc7ac\uadc0 \uae4a\uc774\uc5d0 \ub300\ud574 \uc22b\uc790 \uc81c\ud55c\uc744 \uc9c0\uc815\ud569\ub2c8\ub2e4.)]"},
    { "optionXO",
    " [-XO [transletName] (\uc0dd\uc131\ub41c translet\uc5d0 \uc774\ub984\uc744 \ud560\ub2f9\ud569\ub2c8\ub2e4.)]"},
    { "optionXD",
    " [-XD destinationDirectory(translet\uc5d0 \ub300\ud55c \ub300\uc0c1 \ub514\ub809\ud1a0\ub9ac\ub97c \uc9c0\uc815\ud569\ub2c8\ub2e4.)]"},
    { "optionXJ",
    " [-XJ jarfile(translet \ud074\ub798\uc2a4\ub97c \uc774\ub984\uc774 <jarfile>\uc778 jar \ud30c\uc77c\ub85c \ud328\ud0a4\uc9c0\ud654\ud569\ub2c8\ub2e4.)]"},
    { "optionXP",
    " [-XP package(\uc0dd\uc131\ub41c \ubaa8\ub4e0 translet \ud074\ub798\uc2a4\uc5d0 \ub300\ud574 \ud328\ud0a4\uc9c0 \uc774\ub984 \uc811\ub450\uc5b4\ub97c \uc9c0\uc815\ud569\ub2c8\ub2e4.)]"}


  };

  // ================= INFRASTRUCTURE ======================

  /** String for use when a bad error code was encountered.    */
  public static final String BAD_CODE = "BAD_CODE";

  /** String for use when formatting of the error string failed.   */
  public static final String FORMAT_FAILED = "FORMAT_FAILED";

  /** General error string.   */
  public static final String ERROR_STRING = "#error";

  /** String to prepend to error messages.  */
  public static final String ERROR_HEADER = "\uc624\ub958: ";

  /** String to prepend to warning messages.    */
  public static final String WARNING_HEADER = "\uacbd\uace0: ";

  /** String to specify the XSLT module.  */
  public static final String XSL_HEADER = "XSLT ";

  /** String to specify the XML parser module.  */
  public static final String XML_HEADER = "XML ";

  /** I don't think this is used any more.
   * @deprecated  */
  public static final String QUERY_HEADER = "PATTERN ";

  /**
   * Get the lookup table. 
   *
   * @return The int to message lookup table.
   */
  public Object[][] getContents()
  {
    return contents;
  }
}
