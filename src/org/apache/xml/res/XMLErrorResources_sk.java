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
package org.apache.xml.res;


import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Set up error messages.
 * We build a two dimensional array of message keys and
 * message strings. In order to add a new message here,
 * you need to first add a String constant. And you need
 * to enter key, value pair as part of the contents
 * array. You also need to update MAX_CODE for error strings
 * and MAX_WARNING for warnings ( Needed for only information
 * purpose )
 */
public class XMLErrorResources_sk extends ListResourceBundle
{

/*
 * This file contains error and warning messages related to Xalan Error
 * Handling.
 *
 *  General notes to translators:
 *
 *  1) Xalan (or more properly, Xalan-interpretive) and XSLTC are names of
 *     components.
 *     XSLT is an acronym for "XML Stylesheet Language: Transformations".
 *     XSLTC is an acronym for XSLT Compiler.
 *
 *  2) A stylesheet is a description of how to transform an input XML document
 *     into a resultant XML document (or HTML document or text).  The
 *     stylesheet itself is described in the form of an XML document.
 *
 *  3) A template is a component of a stylesheet that is used to match a
 *     particular portion of an input document and specifies the form of the
 *     corresponding portion of the output document.
 *
 *  4) An element is a mark-up tag in an XML document; an attribute is a
 *     modifier on the tag.  For example, in <elem attr='val' attr2='val2'>
 *     "elem" is an element name, "attr" and "attr2" are attribute names with
 *     the values "val" and "val2", respectively.
 *
 *  5) A namespace declaration is a special attribute that is used to associate
 *     a prefix with a URI (the namespace).  The meanings of element names and
 *     attribute names that use that prefix are defined with respect to that
 *     namespace.
 *
 *  6) "Translet" is an invented term that describes the class file that
 *     results from compiling an XML stylesheet into a Java class.
 *
 *  7) XPath is a specification that describes a notation for identifying
 *     nodes in a tree-structured representation of an XML document.  An
 *     instance of that notation is referred to as an XPath expression.
 *
 */

  /*
   * Message keys
   */
  public static final String ER_FUNCTION_NOT_SUPPORTED = "ER_FUNCTION_NOT_SUPPORTED";
  public static final String ER_CANNOT_OVERWRITE_CAUSE = "ER_CANNOT_OVERWRITE_CAUSE";
  public static final String ER_NO_DEFAULT_IMPL = "ER_NO_DEFAULT_IMPL";
  public static final String ER_CHUNKEDINTARRAY_NOT_SUPPORTED = "ER_CHUNKEDINTARRAY_NOT_SUPPORTED";
  public static final String ER_OFFSET_BIGGER_THAN_SLOT = "ER_OFFSET_BIGGER_THAN_SLOT";
  public static final String ER_COROUTINE_NOT_AVAIL = "ER_COROUTINE_NOT_AVAIL";
  public static final String ER_COROUTINE_CO_EXIT = "ER_COROUTINE_CO_EXIT";
  public static final String ER_COJOINROUTINESET_FAILED = "ER_COJOINROUTINESET_FAILED";
  public static final String ER_COROUTINE_PARAM = "ER_COROUTINE_PARAM";
  public static final String ER_PARSER_DOTERMINATE_ANSWERS = "ER_PARSER_DOTERMINATE_ANSWERS";
  public static final String ER_NO_PARSE_CALL_WHILE_PARSING = "ER_NO_PARSE_CALL_WHILE_PARSING";
  public static final String ER_TYPED_ITERATOR_AXIS_NOT_IMPLEMENTED = "ER_TYPED_ITERATOR_AXIS_NOT_IMPLEMENTED";
  public static final String ER_ITERATOR_AXIS_NOT_IMPLEMENTED = "ER_ITERATOR_AXIS_NOT_IMPLEMENTED";
  public static final String ER_ITERATOR_CLONE_NOT_SUPPORTED = "ER_ITERATOR_CLONE_NOT_SUPPORTED";
  public static final String ER_UNKNOWN_AXIS_TYPE = "ER_UNKNOWN_AXIS_TYPE";
  public static final String ER_AXIS_NOT_SUPPORTED = "ER_AXIS_NOT_SUPPORTED";
  public static final String ER_NO_DTMIDS_AVAIL = "ER_NO_DTMIDS_AVAIL";
  public static final String ER_NOT_SUPPORTED = "ER_NOT_SUPPORTED";
  public static final String ER_NODE_NON_NULL = "ER_NODE_NON_NULL";
  public static final String ER_COULD_NOT_RESOLVE_NODE = "ER_COULD_NOT_RESOLVE_NODE";
  public static final String ER_STARTPARSE_WHILE_PARSING = "ER_STARTPARSE_WHILE_PARSING";
  public static final String ER_STARTPARSE_NEEDS_SAXPARSER = "ER_STARTPARSE_NEEDS_SAXPARSER";
  public static final String ER_COULD_NOT_INIT_PARSER = "ER_COULD_NOT_INIT_PARSER";
  public static final String ER_EXCEPTION_CREATING_POOL = "ER_EXCEPTION_CREATING_POOL";
  public static final String ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE = "ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE";
  public static final String ER_SCHEME_REQUIRED = "ER_SCHEME_REQUIRED";
  public static final String ER_NO_SCHEME_IN_URI = "ER_NO_SCHEME_IN_URI";
  public static final String ER_NO_SCHEME_INURI = "ER_NO_SCHEME_INURI";
  public static final String ER_PATH_INVALID_CHAR = "ER_PATH_INVALID_CHAR";
  public static final String ER_SCHEME_FROM_NULL_STRING = "ER_SCHEME_FROM_NULL_STRING";
  public static final String ER_SCHEME_NOT_CONFORMANT = "ER_SCHEME_NOT_CONFORMANT";
  public static final String ER_HOST_ADDRESS_NOT_WELLFORMED = "ER_HOST_ADDRESS_NOT_WELLFORMED";
  public static final String ER_PORT_WHEN_HOST_NULL = "ER_PORT_WHEN_HOST_NULL";
  public static final String ER_INVALID_PORT = "ER_INVALID_PORT";
  public static final String ER_FRAG_FOR_GENERIC_URI ="ER_FRAG_FOR_GENERIC_URI";
  public static final String ER_FRAG_WHEN_PATH_NULL = "ER_FRAG_WHEN_PATH_NULL";
  public static final String ER_FRAG_INVALID_CHAR = "ER_FRAG_INVALID_CHAR";
  public static final String ER_PARSER_IN_USE = "ER_PARSER_IN_USE";
  public static final String ER_CANNOT_CHANGE_WHILE_PARSING = "ER_CANNOT_CHANGE_WHILE_PARSING";
  public static final String ER_SELF_CAUSATION_NOT_PERMITTED = "ER_SELF_CAUSATION_NOT_PERMITTED";
  public static final String ER_NO_USERINFO_IF_NO_HOST = "ER_NO_USERINFO_IF_NO_HOST";
  public static final String ER_NO_PORT_IF_NO_HOST = "ER_NO_PORT_IF_NO_HOST";
  public static final String ER_NO_QUERY_STRING_IN_PATH = "ER_NO_QUERY_STRING_IN_PATH";
  public static final String ER_NO_FRAGMENT_STRING_IN_PATH = "ER_NO_FRAGMENT_STRING_IN_PATH";
  public static final String ER_CANNOT_INIT_URI_EMPTY_PARMS = "ER_CANNOT_INIT_URI_EMPTY_PARMS";
  public static final String ER_METHOD_NOT_SUPPORTED ="ER_METHOD_NOT_SUPPORTED";
  public static final String ER_INCRSAXSRCFILTER_NOT_RESTARTABLE = "ER_INCRSAXSRCFILTER_NOT_RESTARTABLE";
  public static final String ER_XMLRDR_NOT_BEFORE_STARTPARSE = "ER_XMLRDR_NOT_BEFORE_STARTPARSE";
  public static final String ER_AXIS_TRAVERSER_NOT_SUPPORTED = "ER_AXIS_TRAVERSER_NOT_SUPPORTED";
  public static final String ER_ERRORHANDLER_CREATED_WITH_NULL_PRINTWRITER = "ER_ERRORHANDLER_CREATED_WITH_NULL_PRINTWRITER";
  public static final String ER_SYSTEMID_UNKNOWN = "ER_SYSTEMID_UNKNOWN";
  public static final String ER_LOCATION_UNKNOWN = "ER_LOCATION_UNKNOWN";
  public static final String ER_PREFIX_MUST_RESOLVE = "ER_PREFIX_MUST_RESOLVE";
  public static final String ER_CREATEDOCUMENT_NOT_SUPPORTED = "ER_CREATEDOCUMENT_NOT_SUPPORTED";
  public static final String ER_CHILD_HAS_NO_OWNER_DOCUMENT = "ER_CHILD_HAS_NO_OWNER_DOCUMENT";
  public static final String ER_CHILD_HAS_NO_OWNER_DOCUMENT_ELEMENT = "ER_CHILD_HAS_NO_OWNER_DOCUMENT_ELEMENT";
  public static final String ER_CANT_OUTPUT_TEXT_BEFORE_DOC = "ER_CANT_OUTPUT_TEXT_BEFORE_DOC";
  public static final String ER_CANT_HAVE_MORE_THAN_ONE_ROOT = "ER_CANT_HAVE_MORE_THAN_ONE_ROOT";
  public static final String ER_ARG_LOCALNAME_NULL = "ER_ARG_LOCALNAME_NULL";
  public static final String ER_ARG_LOCALNAME_INVALID = "ER_ARG_LOCALNAME_INVALID";
  public static final String ER_ARG_PREFIX_INVALID = "ER_ARG_PREFIX_INVALID";
  public static final String ER_NAME_CANT_START_WITH_COLON = "ER_NAME_CANT_START_WITH_COLON";

  /*
   * Now fill in the message text.
   * Then fill in the message text for that message code in the
   * array. Use the new error code as the index into the array.
   */

  // Error messages...

  /**
   * Get the lookup table for error messages
   *
   * @return The association list.
   */
  public Object[][] getContents()
  {
    return new Object[][] {

  /** Error message ID that has a null message, but takes in a single object.    */
    {"ER0000" , "{0}" },

    { ER_FUNCTION_NOT_SUPPORTED,
      "Funkcia nie je podporovan\u00e1!"},

    { ER_CANNOT_OVERWRITE_CAUSE,
      "Nie je mo\u017en\u00e9 prep\u00edsa\u0165 pr\u00ed\u010dinu"},

    { ER_NO_DEFAULT_IMPL,
      "Nebola n\u00e1jden\u00e1 \u017eiadna predvolen\u00e1 implement\u00e1cia "},

    { ER_CHUNKEDINTARRAY_NOT_SUPPORTED,
      "ChunkedIntArray({0}) nie je moment\u00e1lne podporovan\u00fd"},

    { ER_OFFSET_BIGGER_THAN_SLOT,
      "Offset v\u00e4\u010d\u0161\u00ed, ne\u017e z\u00e1suvka"},

    { ER_COROUTINE_NOT_AVAIL,
      "Ko-rutina nie je dostupn\u00e1, id={0}"},

    { ER_COROUTINE_CO_EXIT,
      "CoroutineManager obdr\u017eal po\u017eiadavku co_exit()"},

    { ER_COJOINROUTINESET_FAILED,
      "zlyhal co_joinCoroutineSet()"},

    { ER_COROUTINE_PARAM,
      "Chyba parametra korutiny ({0})"},

    { ER_PARSER_DOTERMINATE_ANSWERS,
      "\nNEO\u010cAK\u00c1VAN\u00c9: Analyz\u00e1tor doTerminate odpoved\u00e1 {0}"},

    { ER_NO_PARSE_CALL_WHILE_PARSING,
      "syntaktick\u00fd analyz\u00e1tor nem\u00f4\u017ee by\u0165 volan\u00fd po\u010das vykon\u00e1vania anal\u00fdzy"},

    { ER_TYPED_ITERATOR_AXIS_NOT_IMPLEMENTED,
      "Chyba: nap\u00edsan\u00fd iter\u00e1tor pre os {0} nie je implementovan\u00fd"},

    { ER_ITERATOR_AXIS_NOT_IMPLEMENTED,
      "Chyba: iter\u00e1tor pre os {0} nie je implementovan\u00fd "},

    { ER_ITERATOR_CLONE_NOT_SUPPORTED,
      "Klon iter\u00e1tora nie je podporovan\u00fd"},

    { ER_UNKNOWN_AXIS_TYPE,
      "Nezn\u00e1my typ pret\u00ednania os\u00ed: {0}"},

    { ER_AXIS_NOT_SUPPORTED,
      "Pret\u00ednanie os\u00ed nie je podporovan\u00e9: {0}"},

    { ER_NO_DTMIDS_AVAIL,
      "\u017diadne \u010fal\u0161ie DTM ID nie s\u00fa dostupn\u00e9"},

    { ER_NOT_SUPPORTED,
      "Nie je podporovan\u00e9: {0}"},

    { ER_NODE_NON_NULL,
      "Pre getDTMHandleFromNode mus\u00ed by\u0165 uzol nenulov\u00fd"},

    { ER_COULD_NOT_RESOLVE_NODE,
      "Nebolo mo\u017en\u00e9 ur\u010di\u0165 uzol na spracovanie"},

    { ER_STARTPARSE_WHILE_PARSING,
       "startParse nem\u00f4\u017ee by\u0165 volan\u00fd po\u010das vykon\u00e1vania anal\u00fdzy"},

    { ER_STARTPARSE_NEEDS_SAXPARSER,
       "startParse potrebuje nenulov\u00fd SAXParser"},

    { ER_COULD_NOT_INIT_PARSER,
       "Nebolo mo\u017en\u00e9 inicializova\u0165 syntaktick\u00fd analyz\u00e1tor pomocou"},

    { ER_EXCEPTION_CREATING_POOL,
       "v\u00fdnimka vytv\u00e1rania novej in\u0161tancie oblasti"},

    { ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE,
       "Cesta obsahuje neplatn\u00fa \u00fanikov\u00fa sekvenciu"},

    { ER_SCHEME_REQUIRED,
       "Je po\u017eadovan\u00e1 sch\u00e9ma!"},

    { ER_NO_SCHEME_IN_URI,
       "V URI sa nena\u0161la \u017eiadna sch\u00e9ma: {0}"},

    { ER_NO_SCHEME_INURI,
       "V URI nebola n\u00e1jden\u00e1 \u017eiadna sch\u00e9ma"},

    { ER_PATH_INVALID_CHAR,
       "Cesta obsahuje neplatn\u00fd znak: {0}"},

    { ER_SCHEME_FROM_NULL_STRING,
       "Nie je mo\u017en\u00e9 stanovi\u0165 sch\u00e9mu z nulov\u00e9ho re\u0165azca"},

    { ER_SCHEME_NOT_CONFORMANT,
       "Nezhodn\u00e1 sch\u00e9ma."},

    { ER_HOST_ADDRESS_NOT_WELLFORMED,
       "Hostite\u013e nie je spr\u00e1vne form\u00e1tovan\u00e1 adresa"},

    { ER_PORT_WHEN_HOST_NULL,
       "Nem\u00f4\u017ee by\u0165 stanoven\u00fd port, ak je hostite\u013e nulov\u00fd"},

    { ER_INVALID_PORT,
       "Neplatn\u00e9 \u010d\u00edslo portu"},

    { ER_FRAG_FOR_GENERIC_URI,
       "Fragment m\u00f4\u017ee by\u0165 stanoven\u00fd len pre v\u0161eobecn\u00e9 URI"},

    { ER_FRAG_WHEN_PATH_NULL,
       "Ak je cesta nulov\u00e1, nem\u00f4\u017ee by\u0165 stanoven\u00fd fragment"},

    { ER_FRAG_INVALID_CHAR,
       "Fragment obsahuje neplatn\u00fd znak"},

    { ER_PARSER_IN_USE,
      "Syntaktick\u00fd analyz\u00e1tor je u\u017e pou\u017e\u00edvan\u00fd"},

    { ER_CANNOT_CHANGE_WHILE_PARSING,
      "Nie je mo\u017en\u00e9 zmeni\u0165 {0} {1} po\u010das vykon\u00e1vania anal\u00fdzy"},

    { ER_SELF_CAUSATION_NOT_PERMITTED,
      "Samozapr\u00ed\u010dinenie nie je povolen\u00e9"},

    { ER_NO_USERINFO_IF_NO_HOST,
      "Ak nebol zadan\u00fd hostite\u013e, mo\u017eno nebolo zadan\u00e9 userinfo"},

    { ER_NO_PORT_IF_NO_HOST,
      "Ak nebol zadan\u00fd hostite\u013e, mo\u017eno nebol zadan\u00fd port"},

    { ER_NO_QUERY_STRING_IN_PATH,
      "Re\u0165azec dotazu nem\u00f4\u017ee by\u0165 zadan\u00fd v ceste a re\u0165azci dotazu"},

    { ER_NO_FRAGMENT_STRING_IN_PATH,
      "Fragment nem\u00f4\u017ee by\u0165 zadan\u00fd v ceste, ani vo fragmente"},

    { ER_CANNOT_INIT_URI_EMPTY_PARMS,
      "Nie je mo\u017en\u00e9 inicializova\u0165 URI s pr\u00e1zdnymi parametrami"},

    { ER_METHOD_NOT_SUPPORTED,
      "Met\u00f3da e\u0161te nie je podporovan\u00e1 "},

    { ER_INCRSAXSRCFILTER_NOT_RESTARTABLE,
      "IncrementalSAXSource_Filter nie je moment\u00e1lne re\u0161tartovate\u013en\u00fd"},

    { ER_XMLRDR_NOT_BEFORE_STARTPARSE,
      "XMLReader nepredch\u00e1dza po\u017eiadavke na startParse"},

    { ER_AXIS_TRAVERSER_NOT_SUPPORTED,
      "Pret\u00ednanie os\u00ed nie je podporovan\u00e9: {0}"},

    { ER_ERRORHANDLER_CREATED_WITH_NULL_PRINTWRITER,
      "ListingErrorHandler vytvoren\u00fd s nulov\u00fdm PrintWriter!"},

    { ER_SYSTEMID_UNKNOWN,
      "Nezn\u00e1me SystemId"},

    { ER_LOCATION_UNKNOWN,
      "Nezn\u00e1me miesto v\u00fdskytu chyby"},

    { ER_PREFIX_MUST_RESOLVE,
      "Predpona sa mus\u00ed rozl\u00ed\u0161i\u0165 do n\u00e1zvov\u00e9ho priestoru: {0}"},

    { ER_CREATEDOCUMENT_NOT_SUPPORTED,
      "createDocument() nie je podporovan\u00e9 XPathContext!"},

    { ER_CHILD_HAS_NO_OWNER_DOCUMENT,
      "Potomok atrib\u00fatu nem\u00e1 dokument vlastn\u00edka!"},

    { ER_CHILD_HAS_NO_OWNER_DOCUMENT_ELEMENT,
      "Potomok atrib\u00fatu nem\u00e1 s\u00fa\u010das\u0165 dokumentu vlastn\u00edka!"},

    { ER_CANT_OUTPUT_TEXT_BEFORE_DOC,
      "Upozornenie: nemo\u017eno vypusti\u0165 text pred elementom dokumentu!  Ignorovanie..."},

    { ER_CANT_HAVE_MORE_THAN_ONE_ROOT,
      "Nie je mo\u017en\u00e9 ma\u0165 viac, ne\u017e jeden kore\u0148 DOM!"},

    { ER_ARG_LOCALNAME_NULL,
       "Argument 'localName' je nulov\u00fd"},

    // Note to translators:  A QNAME has the syntactic form [NCName:]NCName
    // The localname is the portion after the optional colon; the message indicates
    // that there is a problem with that part of the QNAME.
    { ER_ARG_LOCALNAME_INVALID,
       "Lok\u00e1lny n\u00e1zov v QNAME by mal by\u0165 platn\u00fdm NCName"},

    // Note to translators:  A QNAME has the syntactic form [NCName:]NCName
    // The prefix is the portion before the optional colon; the message indicates
    // that there is a problem with that part of the QNAME.
    { ER_ARG_PREFIX_INVALID,
       "Predpona v QNAME by mala by\u0165 platn\u00fdm NCName"},

    { ER_NAME_CANT_START_WITH_COLON,
      "N\u00e1zov sa nem\u00f4\u017ee za\u010d\u00edna\u0165 dvojbodkou."},

    { "BAD_CODE", "Parameter na createMessage bol mimo ohrani\u010denia"},
    { "FORMAT_FAILED", "V\u00fdnimka po\u010das volania messageFormat"},
    { "line", "Riadok #"},
    { "column","St\u013apec #"}


  };
  }

  /**
   *   Return a named ResourceBundle for a particular locale.  This method mimics the behavior
   *   of ResourceBundle.getBundle().
   *
   *   @param className the name of the class that implements the resource bundle.
   *   @return the ResourceBundle
   *   @throws MissingResourceException
   */
  public static final XMLErrorResources loadResourceBundle(String className)
          throws MissingResourceException
  {

    Locale locale = Locale.getDefault();
    String suffix = getResourceSuffix(locale);

    try
    {

      // first try with the given locale
      return (XMLErrorResources) ResourceBundle.getBundle(className
              + suffix, locale);
    }
    catch (MissingResourceException e)
    {
      try  // try to fall back to en_US if we can't load
      {

        // Since we can't find the localized property file,
        // fall back to en_US.
        return (XMLErrorResources) ResourceBundle.getBundle(className,
                new Locale("en", "US"));
      }
      catch (MissingResourceException e2)
      {

        // Now we are really in trouble.
        // very bad, definitely very bad...not going to get very far
        throw new MissingResourceException(
          "Could not load any resource bundles.", className, "");
      }
    }
  }

  /**
   * Return the resource file suffic for the indicated locale
   * For most locales, this will be based the language code.  However
   * for Chinese, we do distinguish between Taiwan and PRC
   *
   * @param locale the locale
   * @return an String suffix which canbe appended to a resource name
   */
  private static final String getResourceSuffix(Locale locale)
  {

    String suffix = "_" + locale.getLanguage();
    String country = locale.getCountry();

    if (country.equals("TW"))
      suffix += "_" + country;

    return suffix;
  }

}
