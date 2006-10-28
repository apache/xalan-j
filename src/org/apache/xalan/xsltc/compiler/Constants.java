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

package org.apache.xalan.xsltc.compiler;

import org.apache.bcel.generic.InstructionConstants;
import org.apache.xml.serializer.SerializerBase;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
public interface Constants extends InstructionConstants {

    // Error categories used to report errors to Parser.reportError()

    // Unexpected internal errors, such as null-ptr exceptions, etc.
    // Immediately terminates compilation, no translet produced
    public final int INTERNAL        = 0;
    // XSLT elements that are not implemented and unsupported ext.
    // Immediately terminates compilation, no translet produced
    public final int UNSUPPORTED     = 1;
    // Fatal error in the stylesheet input (parsing or content)
    // Immediately terminates compilation, no translet produced
    public final int FATAL           = 2;
    // Other error in the stylesheet input (parsing or content)
    // Does not terminate compilation, no translet produced
    public final int ERROR           = 3;
    // Other error in the stylesheet input (content errors only)
    // Does not terminate compilation, a translet is produced
    public final int WARNING         = 4;

    public static final String EMPTYSTRING = "";

    public static final String NAMESPACE_FEATURE =
	"http://xml.org/sax/features/namespaces";

    public static final String TRANSLET_INTF
	= "org.apache.xalan.xsltc.Translet";
    public static final String TRANSLET_INTF_SIG        
	= "Lorg/apache/xalan/xsltc/Translet;";
    
    public static final String ATTRIBUTES_SIG 
	= "Lorg/apache/xalan/xsltc/runtime/Attributes;";
    public static final String NODE_ITERATOR_SIG
	= "Lorg/apache/xml/dtm/DTMAxisIterator;";
    public static final String DOM_INTF_SIG
	= "Lorg/apache/xalan/xsltc/DOM;";
    public static final String DOM_IMPL_CLASS
	= "org/apache/xalan/xsltc/DOM"; // xml/dtm/ref/DTMDefaultBaseIterators"; //xalan/xsltc/dom/DOMImpl";
	public static final String SAX_IMPL_CLASS
	= "org/apache/xalan/xsltc/DOM/SAXImpl"; 
    public static final String DOM_IMPL_SIG
	= "Lorg/apache/xalan/xsltc/dom/SAXImpl;"; //xml/dtm/ref/DTMDefaultBaseIterators"; //xalan/xsltc/dom/DOMImpl;";
	public static final String SAX_IMPL_SIG
	= "Lorg/apache/xalan/xsltc/dom/SAXImpl;";
    public static final String DOM_ADAPTER_CLASS
	= "org/apache/xalan/xsltc/dom/DOMAdapter";
    public static final String DOM_ADAPTER_SIG
	= "Lorg/apache/xalan/xsltc/dom/DOMAdapter;";
    public static final String MULTI_DOM_CLASS
	= "org.apache.xalan.xsltc.dom.MultiDOM";
    public static final String MULTI_DOM_SIG
	= "Lorg/apache/xalan/xsltc/dom/MultiDOM;";

    public static final String STRING    
	= "java.lang.String";

    public static final int ACC_PUBLIC    
	= org.apache.bcel.Constants.ACC_PUBLIC;
    public static final int ACC_SUPER     
	= org.apache.bcel.Constants.ACC_SUPER;
    public static final int ACC_FINAL     
	= org.apache.bcel.Constants.ACC_FINAL;
    public static final int ACC_PRIVATE   
	= org.apache.bcel.Constants.ACC_PRIVATE;
    public static final int ACC_PROTECTED 
	= org.apache.bcel.Constants.ACC_PROTECTED;
    public static final int ACC_STATIC
	= org.apache.bcel.Constants.ACC_STATIC;

    public static final String STRING_SIG         
	= "Ljava/lang/String;";
    public static final String STRING_BUFFER_SIG  
	= "Ljava/lang/StringBuffer;";
    public static final String OBJECT_SIG         
	= "Ljava/lang/Object;";
    public static final String DOUBLE_SIG         
	= "Ljava/lang/Double;";
    public static final String INTEGER_SIG        
	= "Ljava/lang/Integer;";
    public static final String COLLATOR_CLASS
        = "java/text/Collator";
    public static final String COLLATOR_SIG
        = "Ljava/text/Collator;";

    public static final String NODE               
	= "int";
    public static final String NODE_ITERATOR      
	= "org.apache.xml.dtm.DTMAxisIterator";
    public static final String NODE_ITERATOR_BASE
	= "org.apache.xml.dtm.ref.DTMAxisIteratorBase";
    public static final String SORT_ITERATOR      
	= "org.apache.xalan.xsltc.dom.SortingIterator";
    public static final String SORT_ITERATOR_SIG     
	= "Lorg.apache.xalan.xsltc.dom.SortingIterator;";
    public static final String NODE_SORT_RECORD 
	= "org.apache.xalan.xsltc.dom.NodeSortRecord";
    public static final String NODE_SORT_FACTORY
	= "org/apache/xalan/xsltc/dom/NodeSortRecordFactory";
    public static final String NODE_SORT_RECORD_SIG 
	= "Lorg/apache/xalan/xsltc/dom/NodeSortRecord;";
    public static final String NODE_SORT_FACTORY_SIG
	= "Lorg/apache/xalan/xsltc/dom/NodeSortRecordFactory;";
    public static final String LOCALE_CLASS
        = "java.util.Locale";
    public static final String LOCALE_SIG 
	= "Ljava/util/Locale;";
    public static final String STRING_VALUE_HANDLER
	= "org.apache.xalan.xsltc.runtime.StringValueHandler";
    public static final String STRING_VALUE_HANDLER_SIG 
	= "Lorg/apache/xalan/xsltc/runtime/StringValueHandler;";
    public static final String OUTPUT_HANDLER
	= SerializerBase.PKG_PATH+"/SerializationHandler";
    public static final String OUTPUT_HANDLER_SIG
	= "L"+SerializerBase.PKG_PATH+"/SerializationHandler;";
    public static final String FILTER_INTERFACE   
	= "org.apache.xalan.xsltc.dom.Filter";
    public static final String FILTER_INTERFACE_SIG   
	= "Lorg/apache/xalan/xsltc/dom/Filter;";
    public static final String UNION_ITERATOR_CLASS
	= "org.apache.xalan.xsltc.dom.UnionIterator";
    public static final String STEP_ITERATOR_CLASS
	= "org.apache.xalan.xsltc.dom.StepIterator";
    public static final String CACHED_NODE_LIST_ITERATOR_CLASS
	= "org.apache.xalan.xsltc.dom.CachedNodeListIterator";	
    public static final String NTH_ITERATOR_CLASS
	= "org.apache.xalan.xsltc.dom.NthIterator";
    public static final String ABSOLUTE_ITERATOR
	= "org.apache.xalan.xsltc.dom.AbsoluteIterator";
    public static final String DUP_FILTERED_ITERATOR
	= "org.apache.xalan.xsltc.dom.DupFilterIterator";
    public static final String CURRENT_NODE_LIST_ITERATOR
	= "org.apache.xalan.xsltc.dom.CurrentNodeListIterator";
    public static final String CURRENT_NODE_LIST_FILTER
	= "org.apache.xalan.xsltc.dom.CurrentNodeListFilter";
    public static final String CURRENT_NODE_LIST_ITERATOR_SIG 
	= "Lorg/apache/xalan/xsltc/dom/CurrentNodeListIterator;";
    public static final String CURRENT_NODE_LIST_FILTER_SIG
	= "Lorg/apache/xalan/xsltc/dom/CurrentNodeListFilter;";
    public static final String FILTER_STEP_ITERATOR 
	= "org.apache.xalan.xsltc.dom.FilteredStepIterator";
    public static final String FILTER_ITERATOR 
	= "org.apache.xalan.xsltc.dom.FilterIterator";
    public static final String SINGLETON_ITERATOR 
	= "org.apache.xalan.xsltc.dom.SingletonIterator";
    public static final String MATCHING_ITERATOR 
	= "org.apache.xalan.xsltc.dom.MatchingIterator";
    public static final String NODE_SIG           
	= "I";
    public static final String GET_PARENT         
	= "getParent";
    public static final String GET_PARENT_SIG     
	= "(" + NODE_SIG + ")" + NODE_SIG;
    public static final String NEXT_SIG           
	= "()" + NODE_SIG;
    public static final String NEXT               
	= "next";
	public static final String NEXTID               
	= "nextNodeID";
    public static final String MAKE_NODE          
	= "makeNode";
    public static final String MAKE_NODE_LIST     
	= "makeNodeList";
    public static final String GET_UNPARSED_ENTITY_URI
        = "getUnparsedEntityURI";
    public static final String STRING_TO_REAL     
	= "stringToReal";
    public static final String STRING_TO_REAL_SIG 
	= "(" + STRING_SIG + ")D";
    public static final String STRING_TO_INT     
	= "stringToInt";
    public static final String STRING_TO_INT_SIG 
	= "(" + STRING_SIG + ")I";

    public static final String XSLT_PACKAGE       
	= "org.apache.xalan.xsltc";
    public static final String COMPILER_PACKAGE   
	= XSLT_PACKAGE + ".compiler";
    public static final String RUNTIME_PACKAGE    
	= XSLT_PACKAGE + ".runtime";
    public static final String TRANSLET_CLASS     
	= RUNTIME_PACKAGE + ".AbstractTranslet";

    public static final String TRANSLET_SIG        
	= "Lorg/apache/xalan/xsltc/runtime/AbstractTranslet;";
    public static final String UNION_ITERATOR_SIG  
	= "Lorg/apache/xalan/xsltc/dom/UnionIterator;";
    public static final String TRANSLET_OUTPUT_SIG    
	= "L"+SerializerBase.PKG_PATH+"/SerializationHandler;";
    public static final String MAKE_NODE_SIG       
	= "(I)Lorg/w3c/dom/Node;";
    public static final String MAKE_NODE_SIG2      
	= "(" + NODE_ITERATOR_SIG + ")Lorg/w3c/dom/Node;";
    public static final String MAKE_NODE_LIST_SIG  
	= "(I)Lorg/w3c/dom/NodeList;";
    public static final String MAKE_NODE_LIST_SIG2 
	= "(" + NODE_ITERATOR_SIG + ")Lorg/w3c/dom/NodeList;";
    
    public static final String STREAM_XML_OUTPUT
    = SerializerBase.PKG_NAME+".ToXMLStream";
    
    public static final String OUTPUT_BASE
    = SerializerBase.PKG_NAME+".SerializerBase";
    
    public static final String LOAD_DOCUMENT_CLASS
	= "org.apache.xalan.xsltc.dom.LoadDocument";

    public static final String KEY_INDEX_CLASS
	= "org/apache/xalan/xsltc/dom/KeyIndex";
    public static final String KEY_INDEX_SIG
	= "Lorg/apache/xalan/xsltc/dom/KeyIndex;";
    public static final String KEY_INDEX_ITERATOR_SIG
	= "Lorg/apache/xalan/xsltc/dom/KeyIndex$KeyIndexIterator;";

    public static final String DOM_INTF
	= "org.apache.xalan.xsltc.DOM";
    public static final String DOM_IMPL
	= "org.apache.xalan.xsltc.dom.SAXImpl";
	public static final String SAX_IMPL
	= "org.apache.xalan.xsltc.dom.SAXImpl";
    public static final String STRING_CLASS 		
	= "java.lang.String";
    public static final String OBJECT_CLASS 		
	= "java.lang.Object";
    public static final String BOOLEAN_CLASS 		
	= "java.lang.Boolean";
    public static final String STRING_BUFFER_CLASS
	= "java.lang.StringBuffer";
    public static final String STRING_WRITER
        = "java.io.StringWriter";
    public static final String WRITER_SIG
        = "Ljava/io/Writer;";

    public static final String TRANSLET_OUTPUT_BASE       
	= "org.apache.xalan.xsltc.TransletOutputBase";
    // output interface
    public static final String TRANSLET_OUTPUT_INTERFACE
	= SerializerBase.PKG_NAME+".SerializationHandler";
    public static final String BASIS_LIBRARY_CLASS 
	= "org.apache.xalan.xsltc.runtime.BasisLibrary";
    public static final String ATTRIBUTE_LIST_IMPL_CLASS 
	= "org.apache.xalan.xsltc.runtime.AttributeListImpl";
    public static final String DOUBLE_CLASS       
	= "java.lang.Double";
    public static final String INTEGER_CLASS      
	= "java.lang.Integer";
    public static final String RUNTIME_NODE_CLASS 
	= "org.apache.xalan.xsltc.runtime.Node";
    public static final String MATH_CLASS         
	= "java.lang.Math";

    public static final String BOOLEAN_VALUE      
	= "booleanValue";
    public static final String BOOLEAN_VALUE_SIG  
	= "()Z";
    public static final String INT_VALUE          
	= "intValue";
    public static final String INT_VALUE_SIG      
	= "()I";
    public static final String DOUBLE_VALUE       
	= "doubleValue";
    public static final String DOUBLE_VALUE_SIG   
	= "()D";
    
    public static final String DOM_PNAME         
  = "dom";
    public static final String NODE_PNAME         
	= "node";
    public static final String TRANSLET_OUTPUT_PNAME 
	= "handler";
    public static final String ITERATOR_PNAME     
	= "iterator";
    public static final String DOCUMENT_PNAME     
	= "document";
    public static final String TRANSLET_PNAME     
	= "translet";

    public static final String INVOKE_METHOD
	= "invokeMethod";
    public static final String GET_NODE_NAME      
	= "getNodeNameX";
    public static final String CHARACTERSW        
	= "characters";
    public static final String GET_CHILDREN       
	= "getChildren";
    public static final String GET_TYPED_CHILDREN 
	= "getTypedChildren";
    public static final String CHARACTERS         
	= "characters";
    public static final String APPLY_TEMPLATES    
	= "applyTemplates";
    public static final String GET_NODE_TYPE      
	= "getNodeType";
    public static final String GET_NODE_VALUE     
	= "getStringValueX";
    public static final String GET_ELEMENT_VALUE  
	= "getElementValue";
    public static final String GET_ATTRIBUTE_VALUE  
	= "getAttributeValue";
    public static final String HAS_ATTRIBUTE      
	= "hasAttribute";
    public static final String ADD_ITERATOR       
	= "addIterator";
    public static final String SET_START_NODE     
	= "setStartNode";
    public static final String RESET     	    
	= "reset";

    public static final String ATTR_SET_SIG
	= "(" + DOM_INTF_SIG  + NODE_ITERATOR_SIG + TRANSLET_OUTPUT_SIG + ")V";

    public static final String GET_NODE_NAME_SIG   
	= "(" + NODE_SIG + ")" + STRING_SIG;
    public static final String CHARACTERSW_SIG     
	= "("  + STRING_SIG + TRANSLET_OUTPUT_SIG + ")V";
    public static final String CHARACTERS_SIG     
	= "(" + NODE_SIG + TRANSLET_OUTPUT_SIG + ")V";
    public static final String GET_CHILDREN_SIG
	= "(" + NODE_SIG +")" + NODE_ITERATOR_SIG;
    public static final String GET_TYPED_CHILDREN_SIG
	= "(I)" + NODE_ITERATOR_SIG;
    public static final String GET_NODE_TYPE_SIG
	= "()S";
    public static final String GET_NODE_VALUE_SIG
	= "(I)" + STRING_SIG;
    public static final String GET_ELEMENT_VALUE_SIG
	= "(I)" + STRING_SIG;
    public static final String GET_ATTRIBUTE_VALUE_SIG
	= "(II)" + STRING_SIG;
    public static final String HAS_ATTRIBUTE_SIG
	= "(II)Z";
    public static final String GET_ITERATOR_SIG
	= "()" + NODE_ITERATOR_SIG;

    public static final String NAMES_INDEX
	= "namesArray";
    public static final String NAMES_INDEX_SIG
	= "[" + STRING_SIG;
    public static final String URIS_INDEX
       = "urisArray";
    public static final String URIS_INDEX_SIG
       = "[" + STRING_SIG;
    public static final String TYPES_INDEX
       = "typesArray";
    public static final String TYPES_INDEX_SIG
       = "[I";
    public static final String NAMESPACE_INDEX
	= "namespaceArray";
    public static final String NAMESPACE_INDEX_SIG
	= "[" + STRING_SIG;
    public static final String NS_ANCESTORS_INDEX_SIG
        = "[I";
    public static final String PREFIX_URIS_IDX_SIG
        = "[I";
    public static final String PREFIX_URIS_ARRAY_SIG
        = "[" + STRING_SIG;
    public static final String HASIDCALL_INDEX
        = "_hasIdCall";
    public static final String HASIDCALL_INDEX_SIG
        = "Z";
    public static final String TRANSLET_VERSION_INDEX
        = "transletVersion";
    public static final String TRANSLET_VERSION_INDEX_SIG
        = "I";
    public static final String LOOKUP_STYLESHEET_QNAME_NS_REF
        = "lookupStylesheetQNameNamespace";
    public static final String LOOKUP_STYLESHEET_QNAME_NS_SIG
        = "(" + STRING_SIG
              + "I"
              + NS_ANCESTORS_INDEX_SIG
              + PREFIX_URIS_IDX_SIG
              + PREFIX_URIS_ARRAY_SIG
              + "Z)" + STRING_SIG;
    public static final String EXPAND_STYLESHEET_QNAME_REF
        = "expandStylesheetQNameRef";
    public static final String EXPAND_STYLESHEET_QNAME_SIG
        = "(" + STRING_SIG
              + "I"
              + NS_ANCESTORS_INDEX_SIG
              + PREFIX_URIS_IDX_SIG
              + PREFIX_URIS_ARRAY_SIG
              + "Z)" + STRING_SIG;

    public static final String DOM_FIELD
	= "_dom";
    public static final String STATIC_NAMES_ARRAY_FIELD
        = "_sNamesArray";
    public static final String STATIC_URIS_ARRAY_FIELD
        = "_sUrisArray";
    public static final String STATIC_TYPES_ARRAY_FIELD
        = "_sTypesArray";
    public static final String STATIC_NAMESPACE_ARRAY_FIELD
        = "_sNamespaceArray";
    public static final String STATIC_NS_ANCESTORS_ARRAY_FIELD
        = "_sNamespaceAncestorsArray";
    public static final String STATIC_PREFIX_URIS_IDX_ARRAY_FIELD
        = "_sPrefixURIsIdxArray";
    public static final String STATIC_PREFIX_URIS_ARRAY_FIELD
        = "_sPrefixURIPairsArray";
    public static final String STATIC_CHAR_DATA_FIELD
        = "_scharData";
    public static final String STATIC_CHAR_DATA_FIELD_SIG
        = "[C";
    public static final String FORMAT_SYMBOLS_FIELD	 
	= "format_symbols";

    public static final String ITERATOR_FIELD_SIG      
	= NODE_ITERATOR_SIG;
    public static final String NODE_FIELD		 
	= "node";
    public static final String NODE_FIELD_SIG		 
	= "I";
	
    public static final String EMPTYATTR_FIELD	     
	= "EmptyAttributes";
    public static final String ATTRIBUTE_LIST_FIELD    
	= "attributeList";
    public static final String CLEAR_ATTRIBUTES        
	= "clear";
    public static final String ADD_ATTRIBUTE           
	= "addAttribute";
    public static final String ATTRIBUTE_LIST_IMPL_SIG 
	= "Lorg/apache/xalan/xsltc/runtime/AttributeListImpl;";
    public static final String CLEAR_ATTRIBUTES_SIG    
	= "()" + ATTRIBUTE_LIST_IMPL_SIG;
    public static final String ADD_ATTRIBUTE_SIG   
	= "(" + STRING_SIG + STRING_SIG + ")" + ATTRIBUTE_LIST_IMPL_SIG;
	
    public static final String ADD_ITERATOR_SIG   
	= "(" + NODE_ITERATOR_SIG +")" + UNION_ITERATOR_SIG;

    public static final String ORDER_ITERATOR
	= "orderNodes";
    public static final String ORDER_ITERATOR_SIG
	= "("+NODE_ITERATOR_SIG+"I)"+NODE_ITERATOR_SIG;
	
    public static final String SET_START_NODE_SIG   
	= "(" + NODE_SIG + ")" + NODE_ITERATOR_SIG;

    public static final String NODE_COUNTER 
	= "org.apache.xalan.xsltc.dom.NodeCounter";
    public static final String NODE_COUNTER_SIG 
	= "Lorg/apache/xalan/xsltc/dom/NodeCounter;";
    public static final String DEFAULT_NODE_COUNTER 
	= "org.apache.xalan.xsltc.dom.DefaultNodeCounter";
    public static final String DEFAULT_NODE_COUNTER_SIG 
	= "Lorg/apache/xalan/xsltc/dom/DefaultNodeCounter;";
    public static final String TRANSLET_FIELD 
	= "translet";
    public static final String TRANSLET_FIELD_SIG 
	= TRANSLET_SIG;

    public static final String RESET_SIG   	       
	= "()" + NODE_ITERATOR_SIG;
    public static final String GET_PARAMETER      
	= "getParameter";
    public static final String ADD_PARAMETER         
	= "addParameter";
    public static final String PUSH_PARAM_FRAME
	= "pushParamFrame";
    public static final String PUSH_PARAM_FRAME_SIG  
	= "()V";
    public static final String POP_PARAM_FRAME       
	= "popParamFrame";
    public static final String POP_PARAM_FRAME_SIG   
	= "()V";
    public static final String GET_PARAMETER_SIG     
	= "(" + STRING_SIG + ")" + OBJECT_SIG;
    public static final String ADD_PARAMETER_SIG
	= "(" + STRING_SIG + OBJECT_SIG + "Z)" + OBJECT_SIG;

    public static final String STRIP_SPACE
	= "stripSpace";
    public static final String STRIP_SPACE_INTF
	= "org/apache/xalan/xsltc/StripFilter";
    public static final String STRIP_SPACE_SIG
	= "Lorg/apache/xalan/xsltc/StripFilter;";
    public static final String STRIP_SPACE_PARAMS
	= "(Lorg/apache/xalan/xsltc/DOM;II)Z";

    public static final String GET_NODE_VALUE_ITERATOR
	= "getNodeValueIterator";
    public static final String GET_NODE_VALUE_ITERATOR_SIG
	= "("+NODE_ITERATOR_SIG+"I"+STRING_SIG+"Z)"+NODE_ITERATOR_SIG;

    public static final String GET_UNPARSED_ENTITY_URI_SIG
        = "("+STRING_SIG+")"+STRING_SIG;

    public static final int POSITION_INDEX = 2;
    public static final int LAST_INDEX     = 3;

    public static final String XMLNS_PREFIX = "xmlns";
    public static final String XMLNS_STRING = "xmlns:";
    public static final String XMLNS_URI 
	= "http://www.w3.org/2000/xmlns/";
    public static final String XSLT_URI
	= "http://www.w3.org/1999/XSL/Transform";
    public static final String XHTML_URI
	= "http://www.w3.org/1999/xhtml";
    public static final String TRANSLET_URI
	= "http://xml.apache.org/xalan/xsltc";
    public static final String REDIRECT_URI
        = "http://xml.apache.org/xalan/redirect";
    public static final String FALLBACK_CLASS
	= "org.apache.xalan.xsltc.compiler.Fallback";

    public static final int RTF_INITIAL_SIZE = 32;
}
