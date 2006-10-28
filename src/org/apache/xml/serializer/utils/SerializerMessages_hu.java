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

package org.apache.xml.serializer.utils;

import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * An instance of this class is a ListResourceBundle that
 * has the required getContents() method that returns
 * an array of message-key/message associations.
 * <p>
 * The message keys are defined in {@link MsgKey}. The
 * messages that those keys map to are defined here.
 * <p>
 * The messages in the English version are intended to be
 * translated.
 *
 * This class is not a public API, it is only public because it is
 * used in org.apache.xml.serializer.
 *
 * @xsl.usage internal
 */
public class SerializerMessages_hu extends ListResourceBundle {

    /*
     * This file contains error and warning messages related to
     * Serializer Error Handling.
     *
     *  General notes to translators:

     *  1) A stylesheet is a description of how to transform an input XML document
     *     into a resultant XML document (or HTML document or text).  The
     *     stylesheet itself is described in the form of an XML document.

     *
     *  2) An element is a mark-up tag in an XML document; an attribute is a
     *     modifier on the tag.  For example, in <elem attr='val' attr2='val2'>
     *     "elem" is an element name, "attr" and "attr2" are attribute names with
     *     the values "val" and "val2", respectively.
     *
     *  3) A namespace declaration is a special attribute that is used to associate
     *     a prefix with a URI (the namespace).  The meanings of element names and
     *     attribute names that use that prefix are defined with respect to that
     *     namespace.
     *
     *
     */

    /** The lookup table for error messages.   */
    public Object[][] getContents() {
        Object[][] contents = new Object[][] {
            {   MsgKey.BAD_MSGKEY,
                "A(z) ''{0}'' \u00fczenetkulcs nem tal\u00e1lhat\u00f3 a(z) ''{1}'' \u00fczenetoszt\u00e1lyban." },

            {   MsgKey.BAD_MSGFORMAT,
                "A(z) ''{1}'' \u00fczenetoszt\u00e1ly ''{0}'' \u00fczenet\u00e9nek form\u00e1tuma hib\u00e1s." },

            {   MsgKey.ER_SERIALIZER_NOT_CONTENTHANDLER,
                "A(z) ''{0}'' p\u00e9ld\u00e1nyos\u00edt\u00f3 oszt\u00e1ly nem val\u00f3s\u00edtja meg az org.xml.sax.ContentHandler f\u00fcggv\u00e9nyt." },

            {   MsgKey.ER_RESOURCE_COULD_NOT_FIND,
                    "A(z) [ {0} ] er\u0151forr\u00e1s nem tal\u00e1lhat\u00f3.\n {1}" },

            {   MsgKey.ER_RESOURCE_COULD_NOT_LOAD,
                    "A(z) [ {0} ] er\u0151forr\u00e1st nem lehet bet\u00f6lteni: {1} \n {2} \t {3}" },

            {   MsgKey.ER_BUFFER_SIZE_LESSTHAN_ZERO,
                    "Pufferm\u00e9ret <= 0" },

            {   MsgKey.ER_INVALID_UTF16_SURROGATE,
                    "\u00c9rv\u00e9nytelen UTF-16 helyettes\u00edt\u00e9s: {0} ?" },

            {   MsgKey.ER_OIERROR,
                "IO hiba" },

            {   MsgKey.ER_ILLEGAL_ATTRIBUTE_POSITION,
                "Nem lehet {0} attrib\u00fatumot hozz\u00e1adni ut\u00f3d csom\u00f3pontok ut\u00e1n vagy egy elem el\u0151\u00e1ll\u00edt\u00e1sa el\u0151tt.  Az attrib\u00fatum figyelmen k\u00edv\u00fcl marad." },

            /*
             * Note to translators:  The stylesheet contained a reference to a
             * namespace prefix that was undefined.  The value of the substitution
             * text is the name of the prefix.
             */
            {   MsgKey.ER_NAMESPACE_PREFIX,
                "A(z) ''{0}'' el\u0151tag n\u00e9vtere nincs deklar\u00e1lva." },

            /*
             * Note to translators:  This message is reported if the stylesheet
             * being processed attempted to construct an XML document with an
             * attribute in a place other than on an element.  The substitution text
             * specifies the name of the attribute.
             */
            {   MsgKey.ER_STRAY_ATTRIBUTE,
                "A(z) ''{0}'' attrib\u00fatum k\u00edv\u00fcl esik az elemen." },

            /*
             * Note to translators:  As with the preceding message, a namespace
             * declaration has the form of an attribute and is only permitted to
             * appear on an element.  The substitution text {0} is the namespace
             * prefix and {1} is the URI that was being used in the erroneous
             * namespace declaration.
             */
            {   MsgKey.ER_STRAY_NAMESPACE,
                "A(z) ''{0}''=''{1}'' n\u00e9vt\u00e9rdeklar\u00e1ci\u00f3 k\u00edv\u00fcl esik az elemen." },

            {   MsgKey.ER_COULD_NOT_LOAD_RESOURCE,
                "Nem lehet bet\u00f6lteni ''{0}'' er\u0151forr\u00e1st (ellen\u0151rizze a CLASSPATH be\u00e1ll\u00edt\u00e1st), a rendszer az alap\u00e9rtelmez\u00e9seket haszn\u00e1lja." },

            {   MsgKey.ER_ILLEGAL_CHARACTER,
                "K\u00eds\u00e9rletet tett {0} \u00e9rt\u00e9k\u00e9nek karakteres ki\u00edr\u00e1s\u00e1ra, de nem jelen\u00edthet\u0151 meg a megadott {1} kimeneti k\u00f3dol\u00e1ssal." },

            {   MsgKey.ER_COULD_NOT_LOAD_METHOD_PROPERTY,
                "Nem lehet bet\u00f6lteni a(z) ''{0}'' tulajdons\u00e1gf\u00e1jlt a(z) ''{1}'' met\u00f3dushoz (ellen\u0151rizze a CLASSPATH be\u00e1ll\u00edt\u00e1st)" },

            {   MsgKey.ER_INVALID_PORT,
                "\u00c9rv\u00e9nytelen portsz\u00e1m" },

            {   MsgKey.ER_PORT_WHEN_HOST_NULL,
                "A portot nem \u00e1ll\u00edthatja be, ha a hoszt null" },

            {   MsgKey.ER_HOST_ADDRESS_NOT_WELLFORMED,
                "A hoszt nem j\u00f3l form\u00e1zott c\u00edm" },

            {   MsgKey.ER_SCHEME_NOT_CONFORMANT,
                "A s\u00e9ma nem megfelel\u0151." },

            {   MsgKey.ER_SCHEME_FROM_NULL_STRING,
                "Nem lehet be\u00e1ll\u00edtani a s\u00e9m\u00e1t null karaktersorozatb\u00f3l" },

            {   MsgKey.ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE,
                "Az el\u00e9r\u00e9si \u00fat \u00e9rv\u00e9nytelen vez\u00e9rl\u0151 jelsorozatot tartalmaz" },

            {   MsgKey.ER_PATH_INVALID_CHAR,
                "Az el\u00e9r\u00e9si \u00fat \u00e9rv\u00e9nytelen karaktert tartalmaz: {0}" },

            {   MsgKey.ER_FRAG_INVALID_CHAR,
                "A t\u00f6red\u00e9k \u00e9rv\u00e9nytelen karaktert tartalmaz" },

            {   MsgKey.ER_FRAG_WHEN_PATH_NULL,
                "A t\u00f6red\u00e9ket nem \u00e1ll\u00edthatja be, ha az el\u00e9r\u00e9si \u00fat null" },

            {   MsgKey.ER_FRAG_FOR_GENERIC_URI,
                "Csak \u00e1ltal\u00e1nos URI-hoz \u00e1ll\u00edthat be t\u00f6red\u00e9ket" },

            {   MsgKey.ER_NO_SCHEME_IN_URI,
                "Nem tal\u00e1lhat\u00f3 s\u00e9ma az URI-ban" },

            {   MsgKey.ER_CANNOT_INIT_URI_EMPTY_PARMS,
                "Az URI nem inicializ\u00e1lhat\u00f3 \u00fcres param\u00e9terekkel" },

            {   MsgKey.ER_NO_FRAGMENT_STRING_IN_PATH,
                "Nem adhat meg t\u00f6red\u00e9ket az el\u00e9r\u00e9si \u00fatban \u00e9s a t\u00f6red\u00e9kben is" },

            {   MsgKey.ER_NO_QUERY_STRING_IN_PATH,
                "Nem adhat meg lek\u00e9rdez\u00e9si karaktersorozatot az el\u00e9r\u00e9si \u00fatban \u00e9s a lek\u00e9rdez\u00e9si karaktersorozatban" },

            {   MsgKey.ER_NO_PORT_IF_NO_HOST,
                "Nem adhatja meg a portot, ha nincs megadva hoszt" },

            {   MsgKey.ER_NO_USERINFO_IF_NO_HOST,
                "Nem adhatja meg a felhaszn\u00e1l\u00f3i inform\u00e1ci\u00f3kat, ha nincs megadva hoszt" },
            {   MsgKey.ER_XML_VERSION_NOT_SUPPORTED,
                "Figyelmeztet\u00e9s: A kimeneti dokumentum k\u00e9rt verzi\u00f3ja ''{0}''.  Az XML ezen verzi\u00f3ja nem t\u00e1mogatott.  A kimeneti dokumentum verzi\u00f3ja ''1.0'' lesz." },

            {   MsgKey.ER_SCHEME_REQUIRED,
                "S\u00e9m\u00e1ra van sz\u00fcks\u00e9g!" },

            /*
             * Note to translators:  The words 'Properties' and
             * 'SerializerFactory' in this message are Java class names
             * and should not be translated.
             */
            {   MsgKey.ER_FACTORY_PROPERTY_MISSING,
                "A SerializerFactory oszt\u00e1lynak \u00e1tadott Properties objektumnak nincs ''{0}'' tulajdons\u00e1ga." },

            {   MsgKey.ER_ENCODING_NOT_SUPPORTED,
                "Figyelmeztet\u00e9s: A(z) ''{0}'' k\u00f3dol\u00e1st nem t\u00e1mogatja a Java fut\u00e1si k\u00f6rnyezet." },


        };

        return contents;
    }
}
