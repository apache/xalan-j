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
package org.apache.xpath.res;

/**
 * <meta name="usage" content="advanced"/>
 * Set up error messages.
 * We build a two dimensional array of message keys and
 * message strings. In order to add a new message here,
 * you need to first add a Static string constant for the
 * Key and update the contents array with Key, Value pair
  * Also you need to  update the count of messages(MAX_CODE)or
 * the count of warnings(MAX_WARNING) [ Information purpose only]
 */
public class XPATHErrorResources_ko extends XPATHErrorResources
{


  /** Field MAX_CODE          */
  public static final int MAX_CODE = 108;  // this is needed to keep track of the number of messages          

  /** Field MAX_WARNING          */
  public static final int MAX_WARNING = 11;  // this is needed to keep track of the number of warnings

  /** Field MAX_OTHERS          */
  public static final int MAX_OTHERS = 20;

  /** Field MAX_MESSAGES          */
  public static final int MAX_MESSAGES = MAX_CODE + MAX_WARNING + 1;


  // Error messages...
  public static final Object[][] contents = {

  /** Field ERROR0000          */
  //public static final int ERROR0000 = 0;

 
  {
    "ERROR0000", "{0}"},
 

  /** Field ER_CURRENT_NOT_ALLOWED_IN_MATCH          */
  //public static final int ER_CURRENT_NOT_ALLOWED_IN_MATCH = 1;

 
  {
    ER_CURRENT_NOT_ALLOWED_IN_MATCH,
      "current() \ud568\uc218\ub294 \uc77c\uce58 \ud328\ud134\uc5d0 \ud5c8\uc6a9\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4."},
 

  /** Field ER_CURRENT_TAKES_NO_ARGS          */
  //public static final int ER_CURRENT_TAKES_NO_ARGS = 2;

 
  {
    ER_CURRENT_TAKES_NO_ARGS,
      "current() \ud568\uc218\uc5d0\ub294 \uc778\uc790\uac00 \uc5c6\uc2b5\ub2c8\ub2e4!"},
 

  /** Field ER_DOCUMENT_REPLACED          */
  //public static final int ER_DOCUMENT_REPLACED = 3;

 
  {
    ER_DOCUMENT_REPLACED,
      "document() \ud568\uc218 \uad6c\ud604\uc740 org.apache.xalan.xslt.FuncDocument\ub85c \ub300\uccb4\ub418\uc5c8\uc2b5\ub2c8\ub2e4!"},
 

  /** Field ER_CONTEXT_HAS_NO_OWNERDOC          */
  //public static final int ER_CONTEXT_HAS_NO_OWNERDOC = 4;

 
  {
    ER_CONTEXT_HAS_NO_OWNERDOC,
      "\ucee8\ud14d\uc2a4\ud2b8\uc5d0 \uc18c\uc720\uc790 \ubb38\uc11c\uac00 \uc5c6\uc2b5\ub2c8\ub2e4."},
 

  /** Field ER_LOCALNAME_HAS_TOO_MANY_ARGS          */
  //public static final int ER_LOCALNAME_HAS_TOO_MANY_ARGS = 5;

 
  {
    ER_LOCALNAME_HAS_TOO_MANY_ARGS,
      "local-name()\uc758 \uc778\uc790\uac00 \ub108\ubb34 \ub9ce\uc2b5\ub2c8\ub2e4."},
 

  /** Field ER_NAMESPACEURI_HAS_TOO_MANY_ARGS          */
  //public static final int ER_NAMESPACEURI_HAS_TOO_MANY_ARGS = 6;

 
  {
    ER_NAMESPACEURI_HAS_TOO_MANY_ARGS,
      "namespace-uri()\uc758 \uc778\uc790\uac00 \ub108\ubb34 \ub9ce\uc2b5\ub2c8\ub2e4."},
 

  /** Field ER_NORMALIZESPACE_HAS_TOO_MANY_ARGS          */
  //public static final int ER_NORMALIZESPACE_HAS_TOO_MANY_ARGS = 7;

 
  {
    ER_NORMALIZESPACE_HAS_TOO_MANY_ARGS,
      "normalize-space()\uc758 \uc778\uc790\uac00 \ub108\ubb34 \ub9ce\uc2b5\ub2c8\ub2e4."},
 

  /** Field ER_NUMBER_HAS_TOO_MANY_ARGS          */
  //public static final int ER_NUMBER_HAS_TOO_MANY_ARGS = 8;

 
  {
    ER_NUMBER_HAS_TOO_MANY_ARGS,
      "number()\uc758 \uc778\uc790\uac00 \ub108\ubb34 \ub9ce\uc2b5\ub2c8\ub2e4."},
 

  /** Field ER_NAME_HAS_TOO_MANY_ARGS          */
  //public static final int ER_NAME_HAS_TOO_MANY_ARGS = 9;

 
  {
    ER_NAME_HAS_TOO_MANY_ARGS, "name()\uc758 \uc778\uc790\uac00 \ub108\ubb34 \ub9ce\uc2b5\ub2c8\ub2e4."},
 

  /** Field ER_STRING_HAS_TOO_MANY_ARGS          */
  //public static final int ER_STRING_HAS_TOO_MANY_ARGS = 10;

 
  {
    ER_STRING_HAS_TOO_MANY_ARGS,
      "string()\uc758 \uc778\uc790\uac00 \ub108\ubb34 \ub9ce\uc2b5\ub2c8\ub2e4."},
 

  /** Field ER_STRINGLENGTH_HAS_TOO_MANY_ARGS          */
  //public static final int ER_STRINGLENGTH_HAS_TOO_MANY_ARGS = 11;

 
  {
    ER_STRINGLENGTH_HAS_TOO_MANY_ARGS,
      "string-length()\uc758 \uc778\uc790\uac00 \ub108\ubb34 \ub9ce\uc2b5\ub2c8\ub2e4."},
 

  /** Field ER_TRANSLATE_TAKES_3_ARGS          */
  //public static final int ER_TRANSLATE_TAKES_3_ARGS = 12;

 
  {
    ER_TRANSLATE_TAKES_3_ARGS,
      "translate() \ud568\uc218\uc5d0\ub294 \uc138 \uac1c\uc758 \uc778\uc790\ub97c \uc0ac\uc6a9\ud569\ub2c8\ub2e4!"},
 

  /** Field ER_UNPARSEDENTITYURI_TAKES_1_ARG          */
  //public static final int ER_UNPARSEDENTITYURI_TAKES_1_ARG = 13;

 
  {
    ER_UNPARSEDENTITYURI_TAKES_1_ARG,
      "unparsed-entity-uri \ud568\uc218\ub294 \ud558\ub098\uc758 \uc778\uc790\ub9cc\uc744 \uc0ac\uc6a9\ud569\ub2c8\ub2e4!"},
 

  /** Field ER_NAMESPACEAXIS_NOT_IMPLEMENTED          */
  //public static final int ER_NAMESPACEAXIS_NOT_IMPLEMENTED = 14;

 
  {
    ER_NAMESPACEAXIS_NOT_IMPLEMENTED,
      "\uc774\ub984 \uacf5\uac04 \ucd95\uc774 \uc544\uc9c1 \uad6c\ud604\ub418\uc9c0 \uc54a\uc558\uc2b5\ub2c8\ub2e4!"},
 

  /** Field ER_UNKNOWN_AXIS          */
  //public static final int ER_UNKNOWN_AXIS = 15;

 
  {
    ER_UNKNOWN_AXIS, "{0}\uc740(\ub294) \uc54c \uc218 \uc5c6\ub294 \ucd95\uc785\ub2c8\ub2e4."},
 

  /** Field ER_UNKNOWN_MATCH_OPERATION          */
  //public static final int ER_UNKNOWN_MATCH_OPERATION = 16;

 
  {
    ER_UNKNOWN_MATCH_OPERATION, "\uc54c \uc218 \uc5c6\ub294 \uc77c\uce58 \uc5f0\uc0b0\uc785\ub2c8\ub2e4!"},
 

  /** Field ER_INCORRECT_ARG_LENGTH          */
  //public static final int ER_INCORRECT_ARG_LENGTH = 17;

 
  {
    ER_INCORRECT_ARG_LENGTH,
      "processing-instruction() \ub178\ub4dc \uac80\uc0ac\uc758 \uc778\uc790 \uae38\uc774\uac00 \uc62c\ubc14\ub974\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4!"},
 

  /** Field ER_CANT_CONVERT_TO_NUMBER          */
  //public static final int ER_CANT_CONVERT_TO_NUMBER = 18;

 
  {
    ER_CANT_CONVERT_TO_NUMBER,
      "{0}\uc744(\ub97c) \uc22b\uc790\ub85c \ubcc0\ud658\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},
 

  /** Field ER_CANT_CONVERT_TO_NODELIST          */
  //public static final int ER_CANT_CONVERT_TO_NODELIST = 19;

 
  {
    ER_CANT_CONVERT_TO_NODELIST,
      "{0}\uc744(\ub97c) NodeList\ub85c \ubcc0\ud658\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4!"},
 

  /** Field ER_CANT_CONVERT_TO_MUTABLENODELIST          */
  //public static final int ER_CANT_CONVERT_TO_MUTABLENODELIST = 20;

 
  {
    ER_CANT_CONVERT_TO_MUTABLENODELIST,
      "{0}\uc744(\ub97c) NodeSetDTM\uc73c\ub85c \ubcc0\ud658\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4!"},
 

  /** Field ER_CANT_CONVERT_TO_TYPE          */
  //public static final int ER_CANT_CONVERT_TO_TYPE = 21;

 
  {
    ER_CANT_CONVERT_TO_TYPE,
      "{0}\uc744(\ub97c) type//{1}(\uc73c)\ub85c \ubcc0\ud658\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4. "},
 

  /** Field ER_EXPECTED_MATCH_PATTERN          */
  //public static final int ER_EXPECTED_MATCH_PATTERN = 22;

 
  {
    ER_EXPECTED_MATCH_PATTERN,
      "getMatchScore\uc5d0 \uc77c\uce58 \ud328\ud134\uc774 \uc788\uc5b4\uc57c \ud569\ub2c8\ub2e4!"},
 

  /** Field ER_COULDNOT_GET_VAR_NAMED          */
  //public static final int ER_COULDNOT_GET_VAR_NAMED = 23;

 
  {
    ER_COULDNOT_GET_VAR_NAMED,
      "{0} \ubcc0\uc218\ub97c \uac00\uc838\uc62c \uc218 \uc5c6\uc2b5\ub2c8\ub2e4. "},
 

  /** Field ER_UNKNOWN_OPCODE          */
  //public static final int ER_UNKNOWN_OPCODE = 24;

 
  {
    ER_UNKNOWN_OPCODE, "\uc624\ub958! \uc54c \uc218 \uc5c6\ub294 \uc5f0\uc0b0 \ucf54\ub4dc: {0}"},
 

  /** Field ER_EXTRA_ILLEGAL_TOKENS          */
  //public static final int ER_EXTRA_ILLEGAL_TOKENS = 25;

 
  {
    ER_EXTRA_ILLEGAL_TOKENS, "\uc798\ubabb\ub41c \ud1a0\ud070: {0}"},
 

  /** Field ER_EXPECTED_DOUBLE_QUOTE          */
  //public static final int ER_EXPECTED_DOUBLE_QUOTE = 26;

 
  {
    ER_EXPECTED_DOUBLE_QUOTE,
      "\ub9ac\ud130\ub7f4\uc758 \uc778\uc6a9\ubd80\ud638\uac00 \uc798\ubabb\ub418\uc5c8\uc2b5\ub2c8\ub2e4... \ud070\ub530\uc634\ud45c\uac00 \ub098\uc640\uc57c \ud569\ub2c8\ub2e4!"},
 

  /** Field ER_EXPECTED_SINGLE_QUOTE          */
  //public static final int ER_EXPECTED_SINGLE_QUOTE = 27;

 
  {
    ER_EXPECTED_SINGLE_QUOTE,
      "\ub9ac\ud130\ub7f4\uc758 \uc778\uc6a9\ubd80\ud638\uac00 \uc798\ubabb\ub418\uc5c8\uc2b5\ub2c8\ub2e4... \ub2e8\uc77c \uc778\uc6a9\ubd80\ud638\uac00 \ub098\uc640\uc57c \ud569\ub2c8\ub2e4!"},
 

  /** Field ER_EMPTY_EXPRESSION          */
  //public static final int ER_EMPTY_EXPRESSION = 28;

 
  {
    ER_EMPTY_EXPRESSION, "\ud45c\ud604\uc2dd\uc774 \ube44\uc5b4 \uc788\uc2b5\ub2c8\ub2e4!"},
 

  /** Field ER_EXPECTED_BUT_FOUND          */
  //public static final int ER_EXPECTED_BUT_FOUND = 29;

 
  {
    ER_EXPECTED_BUT_FOUND, "{0}\uc744(\ub97c) \uc608\uc0c1\ud588\uc9c0\ub9cc {1}\uc744(\ub97c) \ucc3e\uc558\uc2b5\ub2c8\ub2e4."},
 

  /** Field ER_INCORRECT_PROGRAMMER_ASSERTION          */
  //public static final int ER_INCORRECT_PROGRAMMER_ASSERTION = 30;

 
  {
    ER_INCORRECT_PROGRAMMER_ASSERTION,
      "\ud504\ub85c\uadf8\ub798\uba38 \uba85\uc81c\uac00 \uc62c\ubc14\ub974\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4! - {0}"},
 

  /** Field ER_BOOLEAN_ARG_NO_LONGER_OPTIONAL          */
  //public static final int ER_BOOLEAN_ARG_NO_LONGER_OPTIONAL = 31;

 
  {
    ER_BOOLEAN_ARG_NO_LONGER_OPTIONAL,
      "\ubd80\uc6b8(...) \uc778\uc790\ub294 19990709 XPath \ub4dc\ub798\ud504\ud2b8\uc640 \ud568\uaed8 \ub354 \uc774\uc0c1 \uc120\ud0dd \uc778\uc790\uac00 \uc544\ub2d9\ub2c8\ub2e4."},
 

  /** Field ER_FOUND_COMMA_BUT_NO_PRECEDING_ARG          */
  //public static final int ER_FOUND_COMMA_BUT_NO_PRECEDING_ARG = 32;

 
  {
    ER_FOUND_COMMA_BUT_NO_PRECEDING_ARG,
      "','\ub97c \ucc3e\uc558\uc73c\ub098 \uc120\ud589 \uc778\uc790\uac00 \uc544\ub2d9\ub2c8\ub2e4!"},
 

  /** Field ER_FOUND_COMMA_BUT_NO_FOLLOWING_ARG          */
  //public static final int ER_FOUND_COMMA_BUT_NO_FOLLOWING_ARG = 33;

 
  {
    ER_FOUND_COMMA_BUT_NO_FOLLOWING_ARG,
      "','\ub97c \ucc3e\uc558\uc73c\ub098 \ud6c4\ubbf8 \uc778\uc790\uac00 \uc544\ub2d9\ub2c8\ub2e4!"},
 

  /** Field ER_PREDICATE_ILLEGAL_SYNTAX          */
  //public static final int ER_PREDICATE_ILLEGAL_SYNTAX = 34;

 
  {
    ER_PREDICATE_ILLEGAL_SYNTAX,
      "'..[predicate]' \ub610\ub294 '.[predicate]'\ub294 \uc798\ubabb\ub41c \uad6c\ubb38\uc785\ub2c8\ub2e4. \ub300\uc2e0 'self::node()[predicate]'\uc744 \uc0ac\uc6a9\ud558\uc2ed\uc2dc\uc624. "},
 

  /** Field ER_ILLEGAL_AXIS_NAME          */
  //public static final int ER_ILLEGAL_AXIS_NAME = 35;

 
  {
    ER_ILLEGAL_AXIS_NAME, "\uc798\ubabb\ub41c \ucd95 \uc774\ub984: {0}"},
 

  /** Field ER_UNKNOWN_NODETYPE          */
  //public static final int ER_UNKNOWN_NODETYPE = 36;

 
  {
    ER_UNKNOWN_NODETYPE, "\uc54c \uc218 \uc5c6\ub294 \ub178\ub4dc \uc720\ud615: {0}"},
 

  /** Field ER_PATTERN_LITERAL_NEEDS_BE_QUOTED          */
  //public static final int ER_PATTERN_LITERAL_NEEDS_BE_QUOTED = 37;

 
  {
    ER_PATTERN_LITERAL_NEEDS_BE_QUOTED,
      "\ud328\ud134 \ub9ac\ud130\ub7f4({0})\uc5d0 \uc778\uc6a9\ubd80\ud638\uac00 \uc788\uc5b4\uc57c \ud569\ub2c8\ub2e4!"},
 

  /** Field ER_COULDNOT_BE_FORMATTED_TO_NUMBER          */
  //public static final int ER_COULDNOT_BE_FORMATTED_TO_NUMBER = 38;

 
  {
    ER_COULDNOT_BE_FORMATTED_TO_NUMBER,
      "{0}\uc744(\ub97c) \uc22b\uc790\ub85c \ud3ec\ub9f7\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4!"},
 

  /** Field ER_COULDNOT_CREATE_XMLPROCESSORLIAISON          */
  //public static final int ER_COULDNOT_CREATE_XMLPROCESSORLIAISON = 39;

 
  {
    ER_COULDNOT_CREATE_XMLPROCESSORLIAISON,
      "XML TransformerFactory Liaison {0}\uc744(\ub97c) \uc791\uc131\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},
 

  /** Field ER_DIDNOT_FIND_XPATH_SELECT_EXP          */
  //public static final int ER_DIDNOT_FIND_XPATH_SELECT_EXP = 40;

 
  {
    ER_DIDNOT_FIND_XPATH_SELECT_EXP,
      "\uc624\ub958! xpath \uc120\ud0dd \ud45c\ud604\uc2dd(-select)\uc744 \ucc3e\uc744 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},
 

  /** Field ER_COULDNOT_FIND_ENDOP_AFTER_OPLOCATIONPATH          */
  //public static final int ER_COULDNOT_FIND_ENDOP_AFTER_OPLOCATIONPATH = 41;

 
  {
    ER_COULDNOT_FIND_ENDOP_AFTER_OPLOCATIONPATH,
      "\uc624\ub958! OP_LOCATIONPATH \ub2e4\uc74c\uc5d0 ENDOP\ub97c \ucc3e\uc744 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4.   "},
 

  /** Field ER_ERROR_OCCURED          */
  //public static final int ER_ERROR_OCCURED = 42;

 
  {
    ER_ERROR_OCCURED, "\uc624\ub958\uac00 \ubc1c\uc0dd\ud588\uc2b5\ub2c8\ub2e4!"},
 

  /** Field ER_ILLEGAL_VARIABLE_REFERENCE          */
  //public static final int ER_ILLEGAL_VARIABLE_REFERENCE = 43;

 
  {
    ER_ILLEGAL_VARIABLE_REFERENCE,
      "VariableReference\uac00 \ucee8\ud14d\uc2a4\ud2b8\ub97c \ubc97\uc5b4\ub0ac\uac70\ub098 \uc815\uc758\ub418\uc9c0 \uc54a\uc740 \ubcc0\uc218\uc5d0 \uc9c0\uc815\ub418\uc5c8\uc2b5\ub2c8\ub2e4! \uc774\ub984 = {0}"},
 

  /** Field ER_AXES_NOT_ALLOWED          */
  //public static final int ER_AXES_NOT_ALLOWED = 44;

 
  {
    ER_AXES_NOT_ALLOWED,
      "\uc77c\uce58 \ud328\ud134\uc5d0\uc11c\ub294 \ud558\ub098\uc758 child:: \ubc0f attribute:: \ucd95\uc774 \ud5c8\uc6a9\ub429\ub2c8\ub2e4. \uc704\ubc18 \ucd95 = {0}"},
 

  /** Field ER_KEY_HAS_TOO_MANY_ARGS          */
  //public static final int ER_KEY_HAS_TOO_MANY_ARGS = 45;

 
  {
    ER_KEY_HAS_TOO_MANY_ARGS,
      "key()\uc758 \uc778\uc790 \uc218\uac00 \uc798\ubabb\ub418\uc5c8\uc2b5\ub2c8\ub2e4."},
 

  /** Field ER_COUNT_TAKES_1_ARG          */
  //public static final int ER_COUNT_TAKES_1_ARG = 46;

 
  {
    ER_COUNT_TAKES_1_ARG,
      "\uce74\uc6b4\ud2b8 \ud568\uc218\ub294 \ud558\ub098\uc758 \uc778\uc790\ub9cc\uc744 \uc0ac\uc6a9\ud569\ub2c8\ub2e4!"},
 

  /** Field ER_COULDNOT_FIND_FUNCTION          */
  //public static final int ER_COULDNOT_FIND_FUNCTION = 47;

 
  {
    ER_COULDNOT_FIND_FUNCTION, "\ud568\uc218 {0}\uc744(\ub97c) \ucc3e\uc744 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},
 

  /** Field ER_UNSUPPORTED_ENCODING          */
  //public static final int ER_UNSUPPORTED_ENCODING = 48;

 
  {
    ER_UNSUPPORTED_ENCODING, "\uc9c0\uc6d0\ub418\uc9c0 \uc54a\ub294 \ucf54\ub4dc\ud654: {0}"},
 

  /** Field ER_PROBLEM_IN_DTM_NEXTSIBLING          */
  //public static final int ER_PROBLEM_IN_DTM_NEXTSIBLING = 49;

 
  {
    ER_PROBLEM_IN_DTM_NEXTSIBLING,
      "getNextSibling\uc758 DTM\uc5d0 \ubb38\uc81c\uac00 \ubc1c\uc0dd\ud588\uc2b5\ub2c8\ub2e4... \ubcf5\uad6c\ub97c \uc2dc\ub3c4 \uc911\uc785\ub2c8\ub2e4."},
 

  /** Field ER_CANNOT_WRITE_TO_EMPTYNODELISTIMPL          */
  //public static final int ER_CANNOT_WRITE_TO_EMPTYNODELISTIMPL = 50;

 
  {
    ER_CANNOT_WRITE_TO_EMPTYNODELISTIMPL,
      "\ud504\ub85c\uadf8\ub798\uba38 \uc624\ub958: EmptyNodeList\uc5d0\ub294 \uc4f8 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},
 

  /** Field ER_SETDOMFACTORY_NOT_SUPPORTED          */
  //public static final int ER_SETDOMFACTORY_NOT_SUPPORTED = 51;

 
  {
    ER_SETDOMFACTORY_NOT_SUPPORTED,
      "setDOMFactory\ub294 XPathContext\uc5d0\uc11c \uc9c0\uc6d0\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4!"},
 

  /** Field ER_PREFIX_MUST_RESOLVE          */
  //public static final int ER_PREFIX_MUST_RESOLVE = 52;

 
  {
    ER_PREFIX_MUST_RESOLVE,
      "\uc811\ub450\uc5b4\uac00 \uc774\ub984 \uacf5\uac04 {0}\uc73c\ub85c(\ub85c) \uacb0\uc815\ub418\uc5b4\uc57c \ud569\ub2c8\ub2e4."},
 

  /** Field ER_PARSE_NOT_SUPPORTED          */
  //public static final int ER_PARSE_NOT_SUPPORTED = 53;

 
  {
    ER_PARSE_NOT_SUPPORTED,
      "\uad6c\ubb38 \ubd84\uc11d(InputSource \uc18c\uc2a4)\uc740 XPathContext\uc5d0\uc11c \uc9c0\uc6d0\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4! {0}\uc744(\ub97c) \uc5f4 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4.  "},
 

  /** Field ER_SAX_API_NOT_HANDLED          */
  //public static final int ER_SAX_API_NOT_HANDLED = 57;

 
  {
    ER_SAX_API_NOT_HANDLED,
      "SAX API \ubb38\uc790(char ch[]...\ub294 DTM\uc5d0 \uc758\ud574 \ucc98\ub9ac\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4!"},
 

  /** Field ER_IGNORABLE_WHITESPACE_NOT_HANDLED          */
  //public static final int ER_IGNORABLE_WHITESPACE_NOT_HANDLED = 58;

 
  {
    ER_IGNORABLE_WHITESPACE_NOT_HANDLED,
      "ignorableWhitespace(char ch[]...\ub294 DTM\uc5d0 \uc758\ud574 \ucc98\ub9ac\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4!"},
 

  /** Field ER_DTM_CANNOT_HANDLE_NODES          */
  //public static final int ER_DTM_CANNOT_HANDLE_NODES = 59;

 
  {
    ER_DTM_CANNOT_HANDLE_NODES,
      "DTMLiaison\uc740 {0} \uc720\ud615\uc758 \ub178\ub4dc\ub97c \ucc98\ub9ac\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4. "},
 

  /** Field ER_XERCES_CANNOT_HANDLE_NODES          */
  //public static final int ER_XERCES_CANNOT_HANDLE_NODES = 60;

 
  {
    ER_XERCES_CANNOT_HANDLE_NODES,
      "DOM2Helper\ub294 {0} \uc720\ud615\uc758 \ub178\ub4dc\ub97c \ucc98\ub9ac\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4.   "},
 

  /** Field ER_XERCES_PARSE_ERROR_DETAILS          */
  //public static final int ER_XERCES_PARSE_ERROR_DETAILS = 61;

 
  {
    ER_XERCES_PARSE_ERROR_DETAILS,
      "DOM2Helper.parse \uc624\ub958: SystemID - {0} \ud589 - {1}"},
 

  /** Field ER_XERCES_PARSE_ERROR          */
  //public static final int ER_XERCES_PARSE_ERROR = 62;

 
  {
    ER_XERCES_PARSE_ERROR, "DOM2Helper.parse \uc624\ub958"},
 

  /** Field ER_INVALID_UTF16_SURROGATE          */
  //public static final int ER_INVALID_UTF16_SURROGATE = 65;

 
  {
    ER_INVALID_UTF16_SURROGATE,
      "\uc798\ubabb\ub41c UTF-16 \ub300\ub9ac\uac00 \uac10\uc9c0\ub418\uc5c8\uc2b5\ub2c8\ub2e4: {0} ?"},
 

  /** Field ER_OIERROR          */
  //public static final int ER_OIERROR = 66;

 
  {
    ER_OIERROR, "IO \uc624\ub958"},
 

  /** Field ER_CANNOT_CREATE_URL          */
  //public static final int ER_CANNOT_CREATE_URL = 67;

 
  {
    ER_CANNOT_CREATE_URL, "{0}\uc5d0 \ub300\ud55c url\uc744 \uc791\uc131\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4.     "},
 

  /** Field ER_XPATH_READOBJECT          */
  //public static final int ER_XPATH_READOBJECT = 68;

 
  {
    ER_XPATH_READOBJECT, "XPath.readObject\uc5d0: {0}"},
 
  
  /** Field ER_XPATH_READOBJECT         */
  //public static final int ER_FUNCTION_TOKEN_NOT_FOUND = 69;

 
  {
    ER_FUNCTION_TOKEN_NOT_FOUND,
      "\uae30\ub2a5 \ud1a0\ud070\uc744 \ucc3e\uc744 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},
 
  
   /**  Can not deal with XPath type:   */
  //public static final int ER_CANNOT_DEAL_XPATH_TYPE = 71;

 
  {
    ER_CANNOT_DEAL_XPATH_TYPE,
       "XPath \uc720\ud615\uc744 \ucc98\ub9ac\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4: {0}    "},
 
  
   /**  This NodeSet is not mutable  */
  //public static final int ER_NODESET_NOT_MUTABLE = 72;

 
  {
    ER_NODESET_NOT_MUTABLE,
       "NodeSet\uc740 \ubcc0\uacbd\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4"},
 
  
   /**  This NodeSetDTM is not mutable  */
  //public static final int ER_NODESETDTM_NOT_MUTABLE = 73;

 
  {
    ER_NODESETDTM_NOT_MUTABLE,
       "NodeSetDTM\uc740 \ubcc0\uacbd\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4"},
 
  
   /**  Variable not resolvable:   */
  //public static final int ER_VAR_NOT_RESOLVABLE = 74;

 
  {
    ER_VAR_NOT_RESOLVABLE,
        "\ubcc0\uc218\ub97c \uacb0\uc815\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4: {0}"},
 
  
   /** Null error handler  */
  //public static final int ER_NULL_ERROR_HANDLER = 75;

 
  {
    ER_NULL_ERROR_HANDLER,
        "\uc624\ub958 \ucc98\ub9ac\uae30\uac00 \ub110\uc785\ub2c8\ub2e4"},
 
  
   /**  Programmer's assertion: unknown opcode  */
  //public static final int ER_PROG_ASSERT_UNKNOWN_OPCODE = 76;

 
  {
    ER_PROG_ASSERT_UNKNOWN_OPCODE,
       "\ud504\ub85c\uadf8\ub798\uba38 \uba85\uc81c: \uc54c \uc218 \uc5c6\ub294 opcode: {0}"},
 
  
   /**  0 or 1   */
  //public static final int ER_ZERO_OR_ONE = 77;

 
  {
    ER_ZERO_OR_ONE,
       "0 \ub610\ub294 1"},
 
  
 
   /**  rtf() not supported by XRTreeFragSelectWrapper   */
  //public static final int ER_RTF_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER = 78;

 
  {
    ER_RTF_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
       "XRTreeFragSelectWrapper\uac00 rtf()\ub97c \uc9c0\uc6d0\ud558\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4"},
 
  
   /**  asNodeIterator() not supported by XRTreeFragSelectWrapper   */
  //public static final int ER_ASNODEITERATOR_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER = 79;

 
  {
    ER_ASNODEITERATOR_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
       "XRTreeFragSelectWrapper\uac00 asNodeIterator()\ub97c \uc9c0\uc6d0\ud558\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4"},
 
  
   /**  fsb() not supported for XStringForChars   */
  //public static final int ER_FSB_NOT_SUPPORTED_XSTRINGFORCHARS = 80;

 
  {
    ER_FSB_NOT_SUPPORTED_XSTRINGFORCHARS,
       "fsb()\uac00 XStringForChars\uc5d0 \ub300\ud574 \uc9c0\uc6d0\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4"},
 
  
   /**  Could not find variable with the name of   */
  //public static final int ER_COULD_NOT_FIND_VAR = 81;

 
  {
    ER_COULD_NOT_FIND_VAR,
      "\uc774\ub984\uc774 {0}\uc778 \ubcc0\uc218\ub97c \ucc3e\uc744 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4.   "},
 
  
   /**  XStringForChars can not take a string for an argument   */
  //public static final int ER_XSTRINGFORCHARS_CANNOT_TAKE_STRING = 82;

 
  {
    ER_XSTRINGFORCHARS_CANNOT_TAKE_STRING,
      "XStringForChars\uac00 \uc778\uc790\uc5d0 \ub300\ud55c \ubb38\uc790\uc5f4\uc744 \uac00\uc838\uc62c \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},
 
  
   /**  The FastStringBuffer argument can not be null   */
  //public static final int ER_FASTSTRINGBUFFER_CANNOT_BE_NULL = 83;

 
  {
    ER_FASTSTRINGBUFFER_CANNOT_BE_NULL,
      "FastStringBuffer \uc778\uc790\ub294 \ub110\uc774 \ub420 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},
    
  /* MANTIS_XALAN CHANGE: BEGIN */ 
   /**  2 or 3   */
  //public static final int ER_TWO_OR_THREE = 84;

 
  {
    ER_TWO_OR_THREE,
       "2 \ub610\ub294 3"},
 

   /** Variable accessed before it is bound! */
  //public static final int ER_VARIABLE_ACCESSED_BEFORE_BIND = 85;

 
  {
    ER_VARIABLE_ACCESSED_BEFORE_BIND,
       "\ubcc0\uc218\uac00 \ubc14\uc6b4\ub529\ub418\uae30 \uc804\uc5d0 \uc561\uc138\uc2a4\ub418\uc5c8\uc2b5\ub2c8\ub2e4."},
 

   /** XStringForFSB can not take a string for an argument! */
  //public static final int ER_FSB_CANNOT_TAKE_STRING = 86;

 
  {
    ER_FSB_CANNOT_TAKE_STRING,
       "XStringForFSB\uac00 \uc778\uc790\uc5d0 \ub300\ud55c \ubb38\uc790\uc5f4\uc744 \uac00\uc838\uc62c \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},
 

   /** Error! Setting the root of a walker to null! */
  //public static final int ER_SETTING_WALKER_ROOT_TO_NULL = 87;

 
  {
    ER_SETTING_WALKER_ROOT_TO_NULL,
       "\n !!!! \uc624\ub958! walker\uc758 \ub8e8\ud2b8\ub97c \ub110\ub85c \uc124\uc815\ud588\uc2b5\ub2c8\ub2e4!!!"},
 

   /** This NodeSetDTM can not iterate to a previous node! */
  //public static final int ER_NODESETDTM_CANNOT_ITERATE = 88;

 
  {
    ER_NODESETDTM_CANNOT_ITERATE,
       "\uc774 NodeSetDTM\uc774 \uc774\uc804 \ub178\ub4dc\ub85c \ubc18\ubcf5\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4!"},
 

  /** This NodeSet can not iterate to a previous node! */
  //public static final int ER_NODESET_CANNOT_ITERATE = 89;

 
  {
    ER_NODESET_CANNOT_ITERATE,
       "\uc774 NodeSet\uc774 \uc774\uc804 \ub178\ub4dc\ub85c \ubc18\ubcf5\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4!"},
 

  /** This NodeSetDTM can not do indexing or counting functions! */
  //public static final int ER_NODESETDTM_CANNOT_INDEX = 90;

 
  {
    ER_NODESETDTM_CANNOT_INDEX,
       "\uc774 NodeSetDTM\uc774 \uc778\ub371\uc2a4 \ub610\ub294 \uce74\uc6b4\ud305 \uae30\ub2a5\uc744 \uc218\ud589\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4!"},
 

  /** This NodeSet can not do indexing or counting functions! */
  //public static final int ER_NODESET_CANNOT_INDEX = 91;

 
  {
    ER_NODESET_CANNOT_INDEX,
       "\uc774 NodeSet\uc774 \uc778\ub371\uc2a4 \ub610\ub294 \uce74\uc6b4\ud305 \uae30\ub2a5\uc744 \uc218\ud589\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4!"},
 

  /** Can not call setShouldCacheNodes after nextNode has been called! */
  //public static final int ER_CANNOT_CALL_SETSHOULDCACHENODE = 92;

 
  {
    ER_CANNOT_CALL_SETSHOULDCACHENODE,
       "nextNode\uac00 \ud638\ucd9c\ub41c \ub2e4\uc74c\uc5d0 setShouldCacheNodeshas\ub97c \ud638\ucd9c\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4!"},
 

  /** {0} only allows {1} arguments */
  //public static final int ER_ONLY_ALLOWS = 93;

 
  {
    ER_ONLY_ALLOWS,
       "{0}\uc740(\ub294) {1} \uc778\uc790\ub9cc \ud5c8\uc6a9\ud569\ub2c8\ub2e4."},
 

  /** Programmer's assertion in getNextStepPos: unknown stepType: {0} */
  //public static final int ER_UNKNOWN_STEP = 94;

 
  {
    ER_UNKNOWN_STEP,
       "getNextStepPos\uc5d0\uc11c \ud504\ub85c\uadf8\ub798\uba38 \uba85\uc81c: \uc54c \uc218 \uc5c6\ub294 stepType: {0}"},
 

  //Note to translators:  A relative location path is a form of XPath expression.
  // The message indicates that such an expression was expected following the
  // characters '/' or '//', but was not found.

  /** Problem with RelativeLocationPath */
  //public static final int ER_EXPECTED_REL_LOC_PATH = 95;

 
  {
    ER_EXPECTED_REL_LOC_PATH,
       "\uc0c1\ub300 \uc704\uce58 \uacbd\ub85c\ub294 '/' \ub610\ub294 '//' \ub4a4\uc5d0 \ub098\uc640\uc57c \ud569\ub2c8\ub2e4."},
 

  // Note to translators:  A location path is a form of XPath expression.
  // The message indicates that syntactically such an expression was expected,but
  // the characters specified by the substitution text were encountered instead.

  /** Problem with LocationPath */
  //public static final int ER_EXPECTED_LOC_PATH = 96;

 
  {
    ER_EXPECTED_LOC_PATH,
       "\uc0c1\ub300 \uc704\uce58 \uacbd\ub85c\uac00 \uc640\uc57c \ud558\uc9c0\ub9cc \ub300\uc2e0 \ub2e4\uc74c \ud1a0\ud070\uc774 \ubc1c\uacac\ub418\uc5c8\uc2b5\ub2c8\ub2e4.\u003a  {0}"},
 

  // Note to translators:  A location step is part of an XPath expression.
  // The message indicates that syntactically such an expression was expected
  // following the specified characters.

  /** Problem with Step */
  //public static final int ER_EXPECTED_LOC_STEP = 97;

 
  {
    ER_EXPECTED_LOC_STEP,
       "\uc704\uce58 \ub2e8\uacc4\ub294 '/' \ub610\ub294 '//' \ub2e4\uc74c\uc5d0 \ub098\uc640\uc57c \ud569\ub2c8\ub2e4."},
 

  // Note to translators:  A node test is part of an XPath expression that is
  // used to test for particular kinds of nodes.  In this case, a node test that
  // consists of an NCName followed by a colon and an asterisk or that consists
  // of a QName was expected, but was not found.

  /** Problem with NodeTest */
  //public static final int ER_EXPECTED_NODE_TEST = 98;

 
  {
    ER_EXPECTED_NODE_TEST,
       "NCName:* \ub610\ub294 QName\uacfc \uc77c\uce58\ud558\ub294 \ub178\ub4dc \ud14c\uc2a4\ud2b8\uac00 \uc640\uc57c \ud569\ub2c8\ub2e4."},
 

  // Note to translators:  A step pattern is part of an XPath expression.
  // The message indicates that syntactically such an expression was expected,
  // but the specified character was found in the expression instead.

  /** Expected step pattern */
  //public static final int ER_EXPECTED_STEP_PATTERN = 99;

 
  {
    ER_EXPECTED_STEP_PATTERN,
       "\ub2e8\uacc4 \ud328\ud134\uc774 \uc640\uc57c \ud558\uc9c0\ub9cc \ub300\uc2e0 '/'\ub97c \ubc1c\uacac\ud588\uc2b5\ub2c8\ub2e4."},
 

  // Note to translators: A relative path pattern is part of an XPath expression.
  // The message indicates that syntactically such an expression was expected,
  // but was not found.
 
  /** Expected relative path pattern */
  //public static final int ER_EXPECTED_REL_PATH_PATTERN = 100;

 
  {
    ER_EXPECTED_REL_PATH_PATTERN,
       "\uc0c1\ub300 \uacbd\ub85c \ud328\ud134\uc774 \uc640\uc57c \ud569\ub2c8\ub2e4."},
 

  // Note to translators:  The substitution text is the name of a data type.  The
  // message indicates that a value of a particular type could not be converted
  // to a value of type string.

  /** Field ER_CANT_CONVERT_TO_BOOLEAN          */
  //public static final int ER_CANT_CONVERT_TO_BOOLEAN = 103;

 
  {
    ER_CANT_CONVERT_TO_BOOLEAN,
       "{0}\uc744(\ub97c) \ubd80\uc6b8\ub85c \ubcc0\ud658\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},
 

  // Note to translators: Do not translate ANY_UNORDERED_NODE_TYPE and 
  // FIRST_ORDERED_NODE_TYPE.

  /** Field ER_CANT_CONVERT_TO_SINGLENODE       */
  //public static final int ER_CANT_CONVERT_TO_SINGLENODE = 104;

 
  {
    ER_CANT_CONVERT_TO_SINGLENODE,
       "{0}\uc744(\ub97c) \ub2e8\uc77c \ub178\ub4dc\ub85c \ubcc0\ud658\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4. \uc774 getter\ub294 ANY_UNORDERED_NODE_TYPE \uc720\ud615\uacfc FIRST_ORDERED_NODE_TYPE \uc720\ud615\uc5d0 \uc801\uc6a9\ub429\ub2c8\ub2e4."},
 

  // Note to translators: Do not translate UNORDERED_NODE_SNAPSHOT_TYPE and
  // ORDERED_NODE_SNAPSHOT_TYPE.

  /** Field ER_CANT_GET_SNAPSHOT_LENGTH         */
  //public static final int ER_CANT_GET_SNAPSHOT_LENGTH = 105;

 
  {
    ER_CANT_GET_SNAPSHOT_LENGTH,
       "\uc720\ud615 {0}\uc5d0\uc11c \uc2a4\ub0c5\uc0f7 \uae38\uc774\ub97c \uac00\uc838\uc62c \uc218 \uc5c6\uc2b5\ub2c8\ub2e4. \uc774 getter\ub294 UNORDERED_NODE_SNAPSHOT_TYPE \uc720\ud615\uacfc ORDERED_NODE_SNAPSHOT_TYPE \uc720\ud615\uc5d0 \uc801\uc6a9\ub429\ub2c8\ub2e4."},
 

  /** Field ER_NON_ITERATOR_TYPE                */
  //public static final int ER_NON_ITERATOR_TYPE        = 106;

 
  {
    ER_NON_ITERATOR_TYPE,
       "\ube44\ubc18\ubcf5 \uc720\ud615 {0}\uc5d0 \ub300\ud574 \ubc18\ubcf5\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},
 

  // Note to translators: This message indicates that the document being operated
  // upon changed, so the iterator object that was being used to traverse the
  // document has now become invalid.

  /** Field ER_DOC_MUTATED                      */
  //public static final int ER_DOC_MUTATED              = 107;

 
  {
    ER_DOC_MUTATED,
       "\uacb0\uacfc \uc774\ud6c4\ub85c \ubcc0\uacbd\ub41c \ubb38\uc11c\uac00 \ubc18\ud658\ub418\uc5c8\uc2b5\ub2c8\ub2e4. \ubc18\ubcf5\uc790\uac00 \uc798\ubabb\ub418\uc5c8\uc2b5\ub2c8\ub2e4."},
 

  /** Field ER_INVALID_XPATH_TYPE               */
  //public static final int ER_INVALID_XPATH_TYPE       = 108;

 
  {
    ER_INVALID_XPATH_TYPE,
       "\uc798\ubabb\ub41c XPath \uc720\ud615 \uc778\uc790: {0}"},
 

  /** Field ER_EMPTY_XPATH_RESULT                */
  //public static final int ER_EMPTY_XPATH_RESULT       = 109;

 
  {
    ER_EMPTY_XPATH_RESULT,
       "\ube48 XPath \uacb0\uacfc \uac1d\uccb4"},
 

  /** Field ER_INCOMPATIBLE_TYPES                */
  //public static final int ER_INCOMPATIBLE_TYPES       = 110;

 
  {
    ER_INCOMPATIBLE_TYPES,
       "\ubc18\ud658\ub41c \uc720\ud615: {0}\uc744(\ub97c) \uc9c0\uc815\ud55c \uc720\ud615\uc73c\ub85c \uac15\uc81c \ubcc0\ud658\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4: {1}"},
 

  /** Field ER_NULL_RESOLVER                     */
  //public static final int ER_NULL_RESOLVER            = 111;

 
  {
    ER_NULL_RESOLVER,
       "\ub110 \uc811\ub450\uc5b4 \ud574\uacb0\uc790\ub85c \uc811\ub450\uc5b4\ub97c \ud574\uacb0\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},
 

  // Note to translators:  The substitution text is the name of a data type.  The
  // message indicates that a value of a particular type could not be converted
  // to a value of type string.

  /** Field ER_CANT_CONVERT_TO_STRING            */
  //public static final int ER_CANT_CONVERT_TO_STRING   = 112;

 
  {
    ER_CANT_CONVERT_TO_STRING,
       "{0}\uc744(\ub97c) \ubb38\uc790\uc5f4\ub85c \ubcc0\ud658\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},
 

  // Note to translators: Do not translate snapshotItem,
  // UNORDERED_NODE_SNAPSHOT_TYPE and ORDERED_NODE_SNAPSHOT_TYPE.

  /** Field ER_NON_SNAPSHOT_TYPE                 */
  //public static final int ER_NON_SNAPSHOT_TYPE       = 113;

 
  {
    ER_NON_SNAPSHOT_TYPE,
       "\uc720\ud615 {0}\uc5d0\uc11c snapshotItem\uc744 \ud638\ucd9c\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4. \uc774 \uba54\uc18c\ub4dc\ub294 UNORDERED_NODE_SNAPSHOT_TYPE \uc720\ud615\uacfc ORDERED_NODE_SNAPSHOT_TYPE \uc720\ud615\uc5d0 \uc801\uc6a9\ub429\ub2c8\ub2e4."},
 

  // Note to translators:  XPathEvaluator is a Java interface name.  An
  // XPathEvaluator is created with respect to a particular XML document, and in
  // this case the expression represented by this object was being evaluated with
  // respect to a context node from a different document.

  /** Field ER_WRONG_DOCUMENT                    */
  //public static final int ER_WRONG_DOCUMENT          = 114;

 
  {
    ER_WRONG_DOCUMENT,
       "\ucee8\ud14d\uc2a4\ud2b8 \ub178\ub4dc\uac00 \uc774 XPathEvaluator\uc5d0 \ubc14\uc6b4\ub529\ub41c \ubb38\uc11c\uc5d0 \uc18d\ud558\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4."},
 

  // Note to translators:  The XPath expression cannot be evaluated with respect
  // to this type of node.
  /** Field ER_WRONG_NODETYPE                    */
  //public static final int ER_WRONG_NODETYPE          = 115;

 
  {
    ER_WRONG_NODETYPE ,
       "\ucee8\ud14d\uc2a4\ud2b8 \ub178\ub4dc \uc720\ud615\uc774 \uc9c0\uc6d0\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4."},
 

  /** Field ER_XPATH_ERROR                       */
  //public static final int ER_XPATH_ERROR             = 116;

 
  {
    ER_XPATH_ERROR ,
       "XPath\uc5d0 \uc54c \uc218 \uc5c6\ub294 \uc624\ub958\uac00 \ubc1c\uc0dd\ud588\uc2b5\ub2c8\ub2e4."},
 
 

  // Warnings...

  /** Field WG_LOCALE_NAME_NOT_HANDLED          */
  //public static final int WG_LOCALE_NAME_NOT_HANDLED = 1;

 
  {
    WG_LOCALE_NAME_NOT_HANDLED,
      "format-number \uae30\ub2a5\uc758 \ub85c\ucf00\uc77c \uc774\ub984\uc774 \uc544\uc9c1 \ucc98\ub9ac\ub418\uc9c0 \uc54a\uc558\uc2b5\ub2c8\ub2e4."},
 

  /** Field WG_PROPERTY_NOT_SUPPORTED          */
  //public static final int WG_PROPERTY_NOT_SUPPORTED = 2;

 
  {
    WG_PROPERTY_NOT_SUPPORTED,
      "XSL \ud2b9\uc131\uc774 \uc9c0\uc6d0\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4: {0}"},
 

  /** Field WG_DONT_DO_ANYTHING_WITH_NS          */
  //public static final int WG_DONT_DO_ANYTHING_WITH_NS = 3;

 
  {
    WG_DONT_DO_ANYTHING_WITH_NS,
      "\ud2b9\uc131 {1}\uc758 \uc774\ub984 \uacf5\uac04 {0}\uc5d0 \uc544\ubb34 \uac83\ub3c4 \uc218\ud589\ud558\uc9c0 \ub9c8\uc2ed\uc2dc\uc624."},
 

  /** Field WG_SECURITY_EXCEPTION          */
  //public static final int WG_SECURITY_EXCEPTION = 4;

 
  {
    WG_SECURITY_EXCEPTION,
      "XSL \uc2dc\uc2a4\ud15c \ud2b9\uc131 {0}\uc5d0 \uc561\uc138\uc2a4\ud558\ub824\uace0 \ud560 \ub54c SecurityException\uc774 \ubc1c\uc0dd\ud588\uc2b5\ub2c8\ub2e4. "},
 

  /** Field WG_QUO_NO_LONGER_DEFINED          */
  //public static final int WG_QUO_NO_LONGER_DEFINED = 5;

 
  {
    WG_QUO_NO_LONGER_DEFINED,
      "\uc774\uc804 \uad6c\ubb38: quo(...)\ub294 \ub354 \uc774\uc0c1 XPath\uc5d0\uc11c \uc815\uc758\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4."},
 

  /** Field WG_NEED_DERIVED_OBJECT_TO_IMPLEMENT_NODETEST          */
  //public static final int WG_NEED_DERIVED_OBJECT_TO_IMPLEMENT_NODETEST = 6;

 
  {
    WG_NEED_DERIVED_OBJECT_TO_IMPLEMENT_NODETEST,
      "XPath\ub294 nodeTest \uad6c\ud604\uc744 \uc704\ud574 \ud30c\uc0dd\ub41c \uac1d\uccb4\uac00 \ud544\uc694\ud569\ub2c8\ub2e4!"},
 

  /** Field WG_FUNCTION_TOKEN_NOT_FOUND          */
  //public static final int WG_FUNCTION_TOKEN_NOT_FOUND = 7;

 
  {
    WG_FUNCTION_TOKEN_NOT_FOUND,
      "\uae30\ub2a5 \ud1a0\ud070\uc744 \ucc3e\uc744 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},
 

  /** Field WG_COULDNOT_FIND_FUNCTION          */
  //public static final int WG_COULDNOT_FIND_FUNCTION = 8;

 
  {
    WG_COULDNOT_FIND_FUNCTION,
      "\ud568\uc218 {0}\uc744(\ub97c) \ucc3e\uc744 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},
 

  /** Field WG_CANNOT_MAKE_URL_FROM          */
  //public static final int WG_CANNOT_MAKE_URL_FROM = 9;

 
  {
    WG_CANNOT_MAKE_URL_FROM,
      "{0}\uc5d0\uc11c URL\uc744 \uc791\uc131\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},
 

  /** Field WG_EXPAND_ENTITIES_NOT_SUPPORTED          */
  //public static final int WG_EXPAND_ENTITIES_NOT_SUPPORTED = 10;

 
  {
    WG_EXPAND_ENTITIES_NOT_SUPPORTED,
      "-E \uc635\uc158\uc740 DTM \uad6c\ubb38 \ubd84\uc11d\uae30\uc5d0 \ub300\ud574 \uc9c0\uc6d0\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4."},
 

  /** Field WG_ILLEGAL_VARIABLE_REFERENCE          */
  //public static final int WG_ILLEGAL_VARIABLE_REFERENCE = 11;

 
  {
    WG_ILLEGAL_VARIABLE_REFERENCE,
      "VariableReference\uac00 \ucee8\ud14d\uc2a4\ud2b8\ub97c \ubc97\uc5b4\ub0ac\uac70\ub098 \uc815\uc758\ub418\uc9c0 \uc54a\uc740 \ubcc0\uc218\uc5d0 \uc9c0\uc815\ub418\uc5c8\uc2b5\ub2c8\ub2e4! \uc774\ub984 = {0}"},
 

  /** Field WG_UNSUPPORTED_ENCODING          */
  //public static final int WG_UNSUPPORTED_ENCODING = 12;

 
  {
    WG_UNSUPPORTED_ENCODING, "\uc9c0\uc6d0\ub418\uc9c0 \uc54a\ub294 \ucf54\ub4dc\ud654: {0}"},
 

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
    { "line", "\ud589 //"},
    { "column", "\uc5f4 //"},
    { "xsldone", "XSLProcessor: \uc644\ub8cc"},
    { "xpath_option", "xpath \uc635\uc158: "},
    { "optionIN", "   [-in inputXMLURL]"},
    { "optionSelect", "   [-select xpath expression]"},
    { "optionMatch", 
      "   [-match \uc77c\uce58 \ud328\ud134 (\uc77c\uce58 \uc9c4\ub2e8\uc5d0 \ub300\ud55c)]"},
    { "optionAnyExpr",
      "\ub610\ub294 xpath \ud45c\ud604\uc2dd\uc774 \uc9c4\ub2e8 \ub364\ud504\ub97c \uc218\ud589\ud569\ub2c8\ub2e4."},
    { "noParsermsg1",
    "XSL \ud504\ub85c\uc138\uc2a4\uac00 \uc131\uacf5\ud558\uc9c0 \ubabb\ud588\uc2b5\ub2c8\ub2e4."},
    { "noParsermsg2",
    "** \uad6c\ubb38 \ubd84\uc11d\uae30\ub97c \ucc3e\uc744 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4 **"},
    { "noParsermsg3",
    "\ud074\ub798\uc2a4 \uacbd\ub85c\ub97c \ud655\uc778\ud558\uc2ed\uc2dc\uc624."},
    { "noParsermsg4",
      "Java\uc6a9 IBM XML \uad6c\ubb38 \ubd84\uc11d\uae30\uac00 \uc5c6\ub294 \uacbd\uc6b0 \ub2e4\uc74c\uc5d0\uc11c \ub2e4\uc6b4\ub85c\ub4dc\ud560 \uc218 \uc788\uc2b5\ub2c8\ub2e4."},
    { "noParsermsg5",
      "IBM AlphaWorks: http://www.alphaworks.ibm.com/formula/xml"}
  };

  // ================= INFRASTRUCTURE ======================

  /** Field BAD_CODE          */
  public static final String BAD_CODE = "BAD_CODE";

  /** Field FORMAT_FAILED          */
  public static final String FORMAT_FAILED = "FORMAT_FAILED";

  /** Field ERROR_RESOURCES          */
  public static final String ERROR_RESOURCES =
    "org.apache.xpath.res.XPATHErrorResources";

  /** Field ERROR_STRING          */
  public static final String ERROR_STRING = "//error";

  /** Field ERROR_HEADER          */
  public static final String ERROR_HEADER = "\uc624\ub958: ";

  /** Field WARNING_HEADER          */
  public static final String WARNING_HEADER = "\uacbd\uace0: ";

  /** Field XSL_HEADER          */
  public static final String XSL_HEADER = "XSL ";

  /** Field XML_HEADER          */
  public static final String XML_HEADER = "XML ";

  /** Field QUERY_HEADER          */
  public static final String QUERY_HEADER = "PATTERN ";

  /**
   * Get the association list.
   *
   * @return The association list.
   */
  public Object[][] getContents()
  {
    return contents;
  }
}

