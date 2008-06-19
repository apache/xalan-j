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

import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.xml.utils.SystemIDResolver;
import org.apache.bcel.generic.ANEWARRAY;
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.ISTORE;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.NEWARRAY;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.TargetLostException;
import org.apache.bcel.util.InstructionFinder;
import org.apache.xalan.xsltc.compiler.util.ClassGenerator;
import org.apache.xalan.xsltc.compiler.util.ErrorMsg;
import org.apache.xalan.xsltc.compiler.util.MethodGenerator;
import org.apache.xalan.xsltc.compiler.util.Type;
import org.apache.xalan.xsltc.compiler.util.TypeCheckError;
import org.apache.xalan.xsltc.compiler.util.Util;
import org.apache.xalan.xsltc.runtime.AbstractTranslet;
import org.apache.xml.dtm.DTM;

public final class Stylesheet extends SyntaxTreeNode {

    /**
     * XSLT version defined in the stylesheet.
     */
    private String _version;
    
    /**
     * Internal name of this stylesheet used as a key into the symbol table.
     */
    private QName _name;
    
    /**
     * A URI that represents the system ID for this stylesheet.
     */
    private String _systemId;
    
    /**
     * A reference to the parent stylesheet or null if topmost.
     */
    private Stylesheet _parentStylesheet;
	
    /**
     * Contains global variables and parameters defined in the stylesheet.
     */
    private Vector _globals = new Vector();

    /**
     * Used to cache the result returned by <code>hasLocalParams()</code>.
     */
    private Boolean _hasLocalParams = null;

    /**
     * The name of the class being generated.
     */
    private String _className;
    
    /**
      * Contains all templates defined in this stylesheet
      */
    private final Vector _templates = new Vector();
    
    /**
     * Used to cache result of <code>getAllValidTemplates()</code>. Only
     * set in top-level stylesheets that include/import other stylesheets.
     */
    private Vector _allValidTemplates = null;

    private Vector _elementsWithNamespacesUsedDynamically = null;

    /**
     * Counter to generate unique mode suffixes.
     */
    private int _nextModeSerial = 1;
    
    /**
     * Mapping between mode names and Mode instances.
     */
    private final Hashtable _modes = new Hashtable();
    
    /**
     * A reference to the default Mode object.
     */
    private Mode _defaultMode;

    /**
     * Mapping between extension URIs and their prefixes.
     */
    private final Hashtable _extensions = new Hashtable();

    /**
     * Reference to the stylesheet from which this stylesheet was
     * imported (if any).
     */
    public Stylesheet _importedFrom = null;
    
    /**
     * Reference to the stylesheet from which this stylesheet was
     * included (if any).
     */
    public Stylesheet _includedFrom = null;
    
    /** 
     * Array of all the stylesheets imported or included from this one.
     */
    private Vector _includedStylesheets = null;
    
    /**
     * Import precendence for this stylesheet.
     */
    private int _importPrecedence = 1;

    /**
     * Minimum precendence of any descendant stylesheet by inclusion or
     * importation.
     */
    private int _minimumDescendantPrecedence = -1;

    /**
     * Mapping between key names and Key objects (needed by Key/IdPattern).
     */
    private Hashtable _keys = new Hashtable();

    /** 
     * A reference to the SourceLoader set by the user (a URIResolver
     * if the JAXP API is being used).
     */
    private SourceLoader _loader = null;

    /**
     * Flag indicating if format-number() is called.
     */
    private boolean _numberFormattingUsed = false;

    /**
     * Flag indicating if this is a simplified stylesheets. A template
     * matching on "/" must be added in this case.
     */
    private boolean _simplified = false;

    /**
     * Flag indicating if multi-document support is needed.
     */
    private boolean _multiDocument = false;
    
    /**
     * Flag indicating if nodset() is called.
     */
    private boolean _callsNodeset = false;

    /**
     * Flag indicating if id() is called.
     */
    private boolean _hasIdCall = false;
    
    /**
     * Set to true to enable template inlining optimization.
     * @see XSLTC#_templateInlining
     */
    private boolean _templateInlining = false;

    /**
     * A reference to the last xsl:output object found in the styleshet.
     */
    private Output  _lastOutputElement = null;
    
    /**
     * Output properties for this stylesheet.
     */
    private Properties _outputProperties = null;
    
    /**
     * Output method for this stylesheet (must be set to one of
     * the constants defined below).
     */ 
    private int _outputMethod = UNKNOWN_OUTPUT;

    // Output method constants
    public static final int UNKNOWN_OUTPUT = 0;
    public static final int XML_OUTPUT     = 1;
    public static final int HTML_OUTPUT    = 2;
    public static final int TEXT_OUTPUT    = 3;
    
    /**
     * Return the output method
     */
    public int getOutputMethod() {
    	return _outputMethod;
    }
    
    /**
     * Check and set the output method
     */
    private void checkOutputMethod() {
	if (_lastOutputElement != null) {
	    String method = _lastOutputElement.getOutputMethod();
	    if (method != null) {
	        if (method.equals("xml"))
	            _outputMethod = XML_OUTPUT;
	        else if (method.equals("html"))
	            _outputMethod = HTML_OUTPUT;
	        else if (method.equals("text"))
	            _outputMethod = TEXT_OUTPUT;
	    }
	}
    }

    public boolean getTemplateInlining() {
	return _templateInlining;
    }

    public void setTemplateInlining(boolean flag) {
	_templateInlining = flag;
    }

    public boolean isSimplified() {
	return(_simplified);
    }

    public void setSimplified() {
	_simplified = true;
    }
    
    public void setHasIdCall(boolean flag) {
        _hasIdCall = flag;
    }

    public void setOutputProperty(String key, String value) {
	if (_outputProperties == null) {
	    _outputProperties = new Properties();
	}
	_outputProperties.setProperty(key, value);
    }

    public void setOutputProperties(Properties props) {
	_outputProperties = props;
    }

    public Properties getOutputProperties() {
	return _outputProperties;
    }

    public Output getLastOutputElement() {
    	return _lastOutputElement;
    }
    
    public void setMultiDocument(boolean flag) {	
	_multiDocument = flag;
    }

    public boolean isMultiDocument() {
	return _multiDocument;
    }

    public void setCallsNodeset(boolean flag) {
	if (flag) setMultiDocument(flag);
	_callsNodeset = flag;
    }

    public boolean callsNodeset() {
	return _callsNodeset;
    }

    public void numberFormattingUsed() {
	_numberFormattingUsed = true;
        /*
         * Fix for bug 23046, if the stylesheet is included, set the 
         * numberFormattingUsed flag to the parent stylesheet too.
         * AbstractTranslet.addDecimalFormat() will be inlined once for the
         * outer most stylesheet. 
         */ 
        Stylesheet parent = getParentStylesheet();
        if (null != parent) parent.numberFormattingUsed();        
    }

    public void setImportPrecedence(final int precedence) {
	// Set import precedence for this stylesheet
	_importPrecedence = precedence;

	// Set import precedence for all included stylesheets
	final Enumeration elements = elements();
	while (elements.hasMoreElements()) {
	    SyntaxTreeNode child = (SyntaxTreeNode)elements.nextElement();
	    if (child instanceof Include) {
		Stylesheet included = ((Include)child).getIncludedStylesheet();
		if (included != null && included._includedFrom == this) {
		    included.setImportPrecedence(precedence);
		}
	    }
	}

	// Set import precedence for the stylesheet that imported this one
	if (_importedFrom != null) {
	    if (_importedFrom.getImportPrecedence() < precedence) {
		final Parser parser = getParser();
		final int nextPrecedence = parser.getNextImportPrecedence();
		_importedFrom.setImportPrecedence(nextPrecedence);
	    }
	}
	// Set import precedence for the stylesheet that included this one
	else if (_includedFrom != null) {
	    if (_includedFrom.getImportPrecedence() != precedence)
		_includedFrom.setImportPrecedence(precedence);
	}
    }
    
    public int getImportPrecedence() {
	return _importPrecedence;
    }

    /**
     * Get the minimum of the precedence of this stylesheet, any stylesheet
     * imported by this stylesheet and any include/import descendant of this
     * stylesheet.
     */
    public int getMinimumDescendantPrecedence() {
        if (_minimumDescendantPrecedence == -1) {
            // Start with precedence of current stylesheet as a basis.
            int min = getImportPrecedence();

            // Recursively examine all imported/included stylesheets.
            final int inclImpCount = (_includedStylesheets != null)
                                          ? _includedStylesheets.size()
                                          : 0;

            for (int i = 0; i < inclImpCount; i++) {
                int prec = ((Stylesheet)_includedStylesheets.elementAt(i))
                                              .getMinimumDescendantPrecedence();

                if (prec < min) {
                    min = prec;
                }
            }

            _minimumDescendantPrecedence = min;
        }
        return _minimumDescendantPrecedence;
    }

    public boolean checkForLoop(String systemId) {
	// Return true if this stylesheet includes/imports itself
	if (_systemId != null && _systemId.equals(systemId)) {
	    return true;
	}
	// Then check with any stylesheets that included/imported this one
	if (_parentStylesheet != null) 
	    return _parentStylesheet.checkForLoop(systemId);
	// Otherwise OK
	return false;
    }
    
    public void setParser(Parser parser) {
	super.setParser(parser);
	_name = makeStylesheetName("__stylesheet_");
    }
    
    public void setParentStylesheet(Stylesheet parent) {
	_parentStylesheet = parent;
    }
    
    public Stylesheet getParentStylesheet() {
	return _parentStylesheet;
    }

    public void setImportingStylesheet(Stylesheet parent) {
	_importedFrom = parent;
	parent.addIncludedStylesheet(this);
    }

    public void setIncludingStylesheet(Stylesheet parent) {
	_includedFrom = parent;
	parent.addIncludedStylesheet(this);
    }

    public void addIncludedStylesheet(Stylesheet child) {
    	if (_includedStylesheets == null) {
    	    _includedStylesheets = new Vector();
    	}
    	_includedStylesheets.addElement(child);
    }

    public void setSystemId(String systemId) {
        if (systemId != null) {
            _systemId = SystemIDResolver.getAbsoluteURI(systemId);
        }
    }
    
    public String getSystemId() {
	return _systemId;
    }

    public void setSourceLoader(SourceLoader loader) {
	_loader = loader;
    }
    
    public SourceLoader getSourceLoader() {
	return _loader;
    }

    private QName makeStylesheetName(String prefix) {
	return getParser().getQName(prefix+getXSLTC().nextStylesheetSerial());
    }

    /**
     * Returns true if this stylesheet has global vars or params.
     */
    public boolean hasGlobals() {
	return _globals.size() > 0;
    }

    /**
     * Returns true if at least one template in the stylesheet has params
     * defined. Uses the variable <code>_hasLocalParams</code> to cache the
     * result.
     */
    public boolean hasLocalParams() {
	if (_hasLocalParams == null) {
	    Vector templates = getAllValidTemplates();
	    final int n = templates.size();
	    for (int i = 0; i < n; i++) {
		final Template template = (Template)templates.elementAt(i);
		if (template.hasParams()) {
		    _hasLocalParams = Boolean.TRUE;
		    return true;
		}
	    }
	    _hasLocalParams = Boolean.FALSE;
	    return false;
	}
	else {
	    return _hasLocalParams.booleanValue();
	}
    }

    /**
     * Adds a single prefix mapping to this syntax tree node.
     * @param prefix Namespace prefix.
     * @param uri Namespace URI.
     */
    protected void addPrefixMapping(String prefix, String uri) {
	if (prefix.equals(EMPTYSTRING) && uri.equals(XHTML_URI)) return;
	super.addPrefixMapping(prefix, uri);
    }

    /**
     * Store extension URIs
     */
    private void extensionURI(String prefixes, SymbolTable stable) {
	if (prefixes != null) {
	    StringTokenizer tokens = new StringTokenizer(prefixes);
	    while (tokens.hasMoreTokens()) {
		final String prefix = tokens.nextToken();
		final String uri = lookupNamespace(prefix);
		if (uri != null) {
		    _extensions.put(uri, prefix);
		}
	    }
	}
    }

    public boolean isExtension(String uri) {
	return (_extensions.get(uri) != null);
    }

    public void declareExtensionPrefixes(Parser parser) {
	final SymbolTable stable = parser.getSymbolTable();
	final String extensionPrefixes = getAttribute("extension-element-prefixes");
	extensionURI(extensionPrefixes, stable);
    }

    /**
     * Parse the version and uri fields of the stylesheet and add an
     * entry to the symbol table mapping the name <tt>__stylesheet_</tt>
     * to an instance of this class.
     */
    public void parseContents(Parser parser) {
	final SymbolTable stable = parser.getSymbolTable();

	/*
	// Make sure the XSL version set in this stylesheet
	if ((_version == null) || (_version.equals(EMPTYSTRING))) {
	    reportError(this, parser, ErrorMsg.REQUIRED_ATTR_ERR,"version");
	}
	// Verify that the version is 1.0 and nothing else
	else if (!_version.equals("1.0")) {
	    reportError(this, parser, ErrorMsg.XSL_VERSION_ERR, _version);
	}
	*/

	// Add the implicit mapping of 'xml' to the XML namespace URI
	addPrefixMapping("xml", "http://www.w3.org/XML/1998/namespace");

	// Report and error if more than one stylesheet defined
	final Stylesheet sheet = stable.addStylesheet(_name, this);
	if (sheet != null) {
	    // Error: more that one stylesheet defined
	    ErrorMsg err = new ErrorMsg(ErrorMsg.MULTIPLE_STYLESHEET_ERR,this);
	    parser.reportError(Constants.ERROR, err);
	}

	// If this is a simplified stylesheet we must create a template that
	// grabs the root node of the input doc ( <xsl:template match="/"/> ).
	// This template needs the current element (the one passed to this
	// method) as its only child, so the Template class has a special
	// method that handles this (parseSimplified()).
	if (_simplified) {
	    stable.excludeURI(XSLT_URI);
	    Template template = new Template();
	    template.parseSimplified(this, parser);
	}
	// Parse the children of this node
	else {
	    parseOwnChildren(parser);
	}
    }

    /**
     * Parse all direct children of the <xsl:stylesheet/> element.
     */
    public final void parseOwnChildren(Parser parser) {
        final SymbolTable stable = parser.getSymbolTable();
        final String excludePrefixes = getAttribute("exclude-result-prefixes");
        final String extensionPrefixes = getAttribute("extension-element-prefixes");
        
        // Exclude XSLT uri 
        stable.pushExcludedNamespacesContext();
        stable.excludeURI(Constants.XSLT_URI);
        stable.excludeNamespaces(excludePrefixes);
        stable.excludeNamespaces(extensionPrefixes);

	final Vector contents = getContents();
	final int count = contents.size();

	// We have to scan the stylesheet element's top-level elements for
	// variables and/or parameters before we parse the other elements
	for (int i = 0; i < count; i++) {
	    SyntaxTreeNode child = (SyntaxTreeNode)contents.elementAt(i);
	    if ((child instanceof VariableBase) ||
		(child instanceof NamespaceAlias)) {
		parser.getSymbolTable().setCurrentNode(child);
		child.parseContents(parser);
	    }
	}

	// Now go through all the other top-level elements...
	for (int i = 0; i < count; i++) {
	    SyntaxTreeNode child = (SyntaxTreeNode)contents.elementAt(i);
	    if (!(child instanceof VariableBase) && 
		!(child instanceof NamespaceAlias)) {
		parser.getSymbolTable().setCurrentNode(child);
		child.parseContents(parser);
	    }

	    // All template code should be compiled as methods if the
	    // <xsl:apply-imports/> element was ever used in this stylesheet
	    if (!_templateInlining && (child instanceof Template)) {
		Template template = (Template)child;
		String name = "template$dot$" + template.getPosition();
		template.setName(parser.getQName(name));
	    }
	}

	stable.popExcludedNamespacesContext();
    }

    public void processModes() {
	if (_defaultMode == null)
	    _defaultMode = new Mode(null, this, Constants.EMPTYSTRING);
	_defaultMode.processPatterns(_keys);
	final Enumeration modes = _modes.elements();
	while (modes.hasMoreElements()) {
	    final Mode mode = (Mode)modes.nextElement();
	    mode.processPatterns(_keys);
	}
    }
	
    private void compileModes(ClassGenerator classGen) {
	_defaultMode.compileApplyTemplates(classGen);
	final Enumeration modes = _modes.elements();
	while (modes.hasMoreElements()) {
	    final Mode mode = (Mode)modes.nextElement();
	    mode.compileApplyTemplates(classGen);
	}
    }

    public Mode getMode(QName modeName) {
	if (modeName == null) {
	    if (_defaultMode == null) {
		_defaultMode = new Mode(null, this, Constants.EMPTYSTRING);
	    }
	    return _defaultMode;
	}
	else {
	    Mode mode = (Mode)_modes.get(modeName);
	    if (mode == null) {
		final String suffix = Integer.toString(_nextModeSerial++);
		_modes.put(modeName, mode = new Mode(modeName, this, suffix));
	    }
	    return mode;
	}
    }

    /**
     * Type check all the children of this node.
     */
    public Type typeCheck(SymbolTable stable) throws TypeCheckError {
	final int count = _globals.size();
	for (int i = 0; i < count; i++) {
	    final VariableBase var = (VariableBase)_globals.elementAt(i);
	    var.typeCheck(stable);
	}
	return typeCheckContents(stable);
    }

    /**
     * Translate the stylesheet into JVM bytecodes. 
     */
    public void translate(ClassGenerator classGen, MethodGenerator methodGen) {
	translate();
    }

    private void addDOMField(ClassGenerator classGen) {
	final FieldGen fgen = new FieldGen(ACC_PUBLIC,
					   Util.getJCRefType(DOM_INTF_SIG),
					   DOM_FIELD,
					   classGen.getConstantPool());
	classGen.addField(fgen.getField());
    }
    
    /**
     * Add a static field
     */
    private void addStaticField(ClassGenerator classGen, String type,
                                String name)
    {
        final FieldGen fgen = new FieldGen(ACC_PROTECTED|ACC_STATIC,
                                           Util.getJCRefType(type),
                                           name,
                                           classGen.getConstantPool());
        classGen.addField(fgen.getField());

    }

    /**
     * Translate the stylesheet into JVM bytecodes. 
     */
    public void translate() {
	_className = getXSLTC().getClassName();

	// Define a new class by extending TRANSLET_CLASS
	final ClassGenerator classGen =
	    new ClassGenerator(_className,
			       TRANSLET_CLASS,
			       Constants.EMPTYSTRING,
			       ACC_PUBLIC | ACC_SUPER,
			       null, this);
	
	addDOMField(classGen);

	// Compile transform() to initialize parameters, globals & output
	// and run the transformation
	compileTransform(classGen);

	// Translate all non-template elements and filter out all templates
	final Enumeration elements = elements();
	while (elements.hasMoreElements()) {
	    Object element = elements.nextElement();
	    // xsl:template
	    if (element instanceof Template) {
		// Separate templates by modes
		final Template template = (Template)element;
		//_templates.addElement(template);
		getMode(template.getModeName()).addTemplate(template);
	    }
	    // xsl:attribute-set
	    else if (element instanceof AttributeSet) {
		((AttributeSet)element).translate(classGen, null);
	    }
	    else if (element instanceof Output) {
		// save the element for later to pass to compileConstructor 
		Output output = (Output)element;
		if (output.enabled()) _lastOutputElement = output;
	    }
	    else {
		// Global variables and parameters are handled elsewhere.
		// Other top-level non-template elements are ignored. Literal
		// elements outside of templates will never be output.
	    }
	}

	checkOutputMethod();
	processModes();
	compileModes(classGen);
        compileStaticInitializer(classGen);
	compileConstructor(classGen, _lastOutputElement);

	if (!getParser().errorsFound()) {
	    getXSLTC().dumpClass(classGen.getJavaClass());
	}
    }

    /**
     * <p>Compile the namesArray, urisArray, typesArray, namespaceArray,
     * namespaceAncestorsArray, prefixURIsIdxArray and prefixURIPairsArray into
     * the static initializer. They are read-only from the
     * translet. All translet instances can share a single
     * copy of this informtion.</p>
     * <p>The <code>namespaceAncestorsArray</code>,
     * <code>prefixURIsIdxArray</code> and <code>prefixURIPairsArray</code>
     * contain namespace information accessible from the stylesheet:
     * <dl>
     * <dt><code>namespaceAncestorsArray</code></dt>
     * <dd>Array indexed by integer stylesheet node IDs containing node IDs of
     * the nearest ancestor node in the stylesheet with namespace
     * declarations or <code>-1</code> if there is no such ancestor.  There
     * can be more than one disjoint tree of nodes - one for each stylesheet
     * module</dd>
     * <dt><code>prefixURIsIdxArray</code></dt>
     * <dd>Array indexed by integer stylesheet node IDs containing the index
     * into <code>prefixURIPairsArray</code> of the first namespace prefix
     * declared for the node.  The values are stored in ascending order, so
     * the next value in this array (if any) can be used to find the last such
     * prefix-URI pair</dd>
     * <dt>prefixURIPairsArray</dt>
     * <dd>Array of pairs of namespace prefixes and URIs.  A zero-length
     * string represents the default namespace if it appears as a prefix and
     * a namespace undeclaration if it appears as a URI.</dd>
     * </dl>
     * </p>
     * <p>For this stylesheet
     * <pre><code>
     * &lt;xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"&gt;
     *   &lt;xsl:template match="/"&gt;
     *     &lt;xsl:for-each select="*" xmlns:foo="foouri"&gt;
     *       &lt;xsl:element name="{n}" xmlns:foo="baruri"&gt;
     *     &lt;/xsl:for-each&gt;
     *     &lt;out xmlns="lumpit"/&gt;
     *     &lt;xsl:element name="{n}" xmlns="foouri"/&gt;
     *     &lt;xsl:element name="{n}" namespace="{ns}" xmlns="limpit"/gt;
     *   &lt;/xsl:template&gt;
     * &lt;/xsl:stylesheet&gt;
     * </code></pre>
     * there will be four stylesheet nodes whose namespace information is
     * needed, and
     * <ul>
     * <li><code>namespaceAncestorsArray</code> will have the value
     * <code>[-1,0,1,0]</code>;</li>
     * <li><code>prefixURIsIdxArray</code> will have the value
     * <code>[0,4,6,8]</code>; and</li>
     * <li><code>prefixURIPairsArray</code> will have the value
     * <code>["xml","http://www.w3.org/XML/1998/namespace",
     *        "xsl","http://www.w3.org/1999/XSL/Transform"
     *        "foo","foouri","foo","baruri","","foouri"].</code></li>
     * </ul>
     * </p>
     */
    private void compileStaticInitializer(ClassGenerator classGen) {
	final ConstantPoolGen cpg = classGen.getConstantPool();
	final InstructionList il = new InstructionList();

	final MethodGenerator staticConst =
	    new MethodGenerator(ACC_PUBLIC|ACC_STATIC,
				org.apache.bcel.generic.Type.VOID, 
				null, null, "<clinit>", 
				_className, il, cpg);

	addStaticField(classGen, "[" + STRING_SIG, STATIC_NAMES_ARRAY_FIELD);
	addStaticField(classGen, "[" + STRING_SIG, STATIC_URIS_ARRAY_FIELD);
	addStaticField(classGen, "[I", STATIC_TYPES_ARRAY_FIELD);
	addStaticField(classGen, "[" + STRING_SIG, STATIC_NAMESPACE_ARRAY_FIELD);
        // Create fields of type char[] that will contain literal text from
        // the stylesheet.
        final int charDataFieldCount = getXSLTC().getCharacterDataCount();
        for (int i = 0; i < charDataFieldCount; i++) {
            addStaticField(classGen, STATIC_CHAR_DATA_FIELD_SIG,
                           STATIC_CHAR_DATA_FIELD+i);
        }

	// Put the names array into the translet - used for dom/translet mapping
	final Vector namesIndex = getXSLTC().getNamesIndex();
	int size = namesIndex.size();
	String[] namesArray = new String[size];
	String[] urisArray = new String[size];
	int[] typesArray = new int[size];
	
	int index;
	for (int i = 0; i < size; i++) {
	    String encodedName = (String)namesIndex.elementAt(i);
	    if ((index = encodedName.lastIndexOf(':')) > -1) {
	        urisArray[i] = encodedName.substring(0, index);
	    }
	    
	    index = index + 1;
	    if (encodedName.charAt(index) == '@') {
	    	typesArray[i] = DTM.ATTRIBUTE_NODE;
	    	index++;
	    } else if (encodedName.charAt(index) == '?') {
	    	typesArray[i] = DTM.NAMESPACE_NODE;
	    	index++;
	    } else {
	        typesArray[i] = DTM.ELEMENT_NODE;
	    }
	    
	    if (index == 0) {
	        namesArray[i] = encodedName;
	    }
	    else {
	        namesArray[i] = encodedName.substring(index);
	    }	    
	}

        staticConst.markChunkStart();
	il.append(new PUSH(cpg, size));
	il.append(new ANEWARRAY(cpg.addClass(STRING)));		
        int namesArrayRef = cpg.addFieldref(_className,
				            STATIC_NAMES_ARRAY_FIELD,
					    NAMES_INDEX_SIG);
	il.append(new PUTSTATIC(namesArrayRef));
        staticConst.markChunkEnd();

	for (int i = 0; i < size; i++) {
	    final String name = namesArray[i];
            staticConst.markChunkStart();
	    il.append(new GETSTATIC(namesArrayRef));
	    il.append(new PUSH(cpg, i));
	    il.append(new PUSH(cpg, name));
	    il.append(AASTORE);
            staticConst.markChunkEnd();
	}

        staticConst.markChunkStart();
	il.append(new PUSH(cpg, size));
	il.append(new ANEWARRAY(cpg.addClass(STRING)));		
        int urisArrayRef = cpg.addFieldref(_className,
					   STATIC_URIS_ARRAY_FIELD,
					   URIS_INDEX_SIG);
	il.append(new PUTSTATIC(urisArrayRef));
        staticConst.markChunkEnd();

	for (int i = 0; i < size; i++) {
	    final String uri = urisArray[i];
            staticConst.markChunkStart();
	    il.append(new GETSTATIC(urisArrayRef));
	    il.append(new PUSH(cpg, i));
	    il.append(new PUSH(cpg, uri));
	    il.append(AASTORE);
            staticConst.markChunkEnd();
	}

        staticConst.markChunkStart();
	il.append(new PUSH(cpg, size));
	il.append(new NEWARRAY(BasicType.INT));		
        int typesArrayRef = cpg.addFieldref(_className,
					    STATIC_TYPES_ARRAY_FIELD,
					    TYPES_INDEX_SIG);
	il.append(new PUTSTATIC(typesArrayRef));
        staticConst.markChunkEnd();

	for (int i = 0; i < size; i++) {
	    final int nodeType = typesArray[i];
            staticConst.markChunkStart();
	    il.append(new GETSTATIC(typesArrayRef));
	    il.append(new PUSH(cpg, i));
	    il.append(new PUSH(cpg, nodeType));
	    il.append(IASTORE);
            staticConst.markChunkEnd();
	}

	// Put the namespace names array into the translet
	final Vector namespaces = getXSLTC().getNamespaceIndex();
        staticConst.markChunkStart();
	il.append(new PUSH(cpg, namespaces.size()));
	il.append(new ANEWARRAY(cpg.addClass(STRING)));		
        int namespaceArrayRef = cpg.addFieldref(_className,
					        STATIC_NAMESPACE_ARRAY_FIELD,
					        NAMESPACE_INDEX_SIG);
	il.append(new PUTSTATIC(namespaceArrayRef));
        staticConst.markChunkEnd();

	for (int i = 0; i < namespaces.size(); i++) {
	    final String ns = (String)namespaces.elementAt(i);
            staticConst.markChunkStart();
	    il.append(new GETSTATIC(namespaceArrayRef));
	    il.append(new PUSH(cpg, i));
	    il.append(new PUSH(cpg, ns));
	    il.append(AASTORE);
            staticConst.markChunkEnd();
	}

        // Put the tree of stylesheet namespace declarations into the translet
        final Vector namespaceAncestors = getXSLTC().getNSAncestorPointers();
        if (namespaceAncestors != null && namespaceAncestors.size() != 0) {
            addStaticField(classGen, NS_ANCESTORS_INDEX_SIG,
                           STATIC_NS_ANCESTORS_ARRAY_FIELD);
            staticConst.markChunkStart();
            il.append(new PUSH(cpg, namespaceAncestors.size()));
            il.append(new NEWARRAY(BasicType.INT));
            int namespaceAncestorsArrayRef =
                    cpg.addFieldref(_className, STATIC_NS_ANCESTORS_ARRAY_FIELD,
                                    NS_ANCESTORS_INDEX_SIG);
            il.append(new PUTSTATIC(namespaceAncestorsArrayRef));
            staticConst.markChunkEnd();
            for (int i = 0; i < namespaceAncestors.size(); i++) {
                int ancestor = ((Integer) namespaceAncestors.get(i)).intValue();
                staticConst.markChunkStart();
                il.append(new GETSTATIC(namespaceAncestorsArrayRef));
                il.append(new PUSH(cpg, i));
                il.append(new PUSH(cpg, ancestor));
                il.append(IASTORE);
                staticConst.markChunkEnd();
            }
        }
        // Put the array of indices into the namespace prefix/URI pairs array
        // into the translet
        final Vector prefixURIPairsIdx = getXSLTC().getPrefixURIPairsIdx();
        if (prefixURIPairsIdx != null && prefixURIPairsIdx.size() != 0) {
            addStaticField(classGen, PREFIX_URIS_IDX_SIG,
                           STATIC_PREFIX_URIS_IDX_ARRAY_FIELD);
            staticConst.markChunkStart();
            il.append(new PUSH(cpg, prefixURIPairsIdx.size()));
            il.append(new NEWARRAY(BasicType.INT));
            int prefixURIPairsIdxArrayRef = 
                        cpg.addFieldref(_className,
                                        STATIC_PREFIX_URIS_IDX_ARRAY_FIELD,
                                        PREFIX_URIS_IDX_SIG);
            il.append(new PUTSTATIC(prefixURIPairsIdxArrayRef));
            staticConst.markChunkEnd();
            for (int i = 0; i < prefixURIPairsIdx.size(); i++) {
                int idx = ((Integer) prefixURIPairsIdx.get(i)).intValue();
                staticConst.markChunkStart();
                il.append(new GETSTATIC(prefixURIPairsIdxArrayRef));
                il.append(new PUSH(cpg, i));
                il.append(new PUSH(cpg, idx));
                il.append(IASTORE);
                staticConst.markChunkEnd();
            }
        }

        // Put the array of pairs of namespace prefixes and URIs into the
        // translet
        final Vector prefixURIPairs = getXSLTC().getPrefixURIPairs();
        if (prefixURIPairs != null && prefixURIPairs.size() != 0) {
            addStaticField(classGen, PREFIX_URIS_ARRAY_SIG,
                    STATIC_PREFIX_URIS_ARRAY_FIELD);

            staticConst.markChunkStart();
            il.append(new PUSH(cpg, prefixURIPairs.size()));
            il.append(new ANEWARRAY(cpg.addClass(STRING)));
            int prefixURIPairsRef = 
                        cpg.addFieldref(_className,
                                        STATIC_PREFIX_URIS_ARRAY_FIELD,
                                        PREFIX_URIS_ARRAY_SIG);
            il.append(new PUTSTATIC(prefixURIPairsRef));
            staticConst.markChunkEnd();
            for (int i = 0; i < prefixURIPairs.size(); i++) {
                String prefixOrURI = (String) prefixURIPairs.get(i);
                staticConst.markChunkStart();
                il.append(new GETSTATIC(prefixURIPairsRef));
                il.append(new PUSH(cpg, i));
                il.append(new PUSH(cpg, prefixOrURI));
                il.append(AASTORE);
                staticConst.markChunkEnd();
            }
        }

        // Grab all the literal text in the stylesheet and put it in a char[]
        final int charDataCount = getXSLTC().getCharacterDataCount();
        final int toCharArray = cpg.addMethodref(STRING, "toCharArray", "()[C");
        for (int i = 0; i < charDataCount; i++) {
            staticConst.markChunkStart();
            il.append(new PUSH(cpg, getXSLTC().getCharacterData(i)));
            il.append(new INVOKEVIRTUAL(toCharArray));
            il.append(new PUTSTATIC(cpg.addFieldref(_className,
                                               STATIC_CHAR_DATA_FIELD+i,
                                               STATIC_CHAR_DATA_FIELD_SIG)));
            staticConst.markChunkEnd();
        }

	il.append(RETURN);

	classGen.addMethod(staticConst);
    	
    }

    /**
     * Compile the translet's constructor
     */
    private void compileConstructor(ClassGenerator classGen, Output output) {

	final ConstantPoolGen cpg = classGen.getConstantPool();
	final InstructionList il = new InstructionList();

	final MethodGenerator constructor =
	    new MethodGenerator(ACC_PUBLIC,
				org.apache.bcel.generic.Type.VOID, 
				null, null, "<init>", 
				_className, il, cpg);

	// Call the constructor in the AbstractTranslet superclass
	il.append(classGen.loadTranslet());
	il.append(new INVOKESPECIAL(cpg.addMethodref(TRANSLET_CLASS,
						     "<init>", "()V")));
	
        constructor.markChunkStart();
	il.append(classGen.loadTranslet());
	il.append(new GETSTATIC(cpg.addFieldref(_className,
	                                        STATIC_NAMES_ARRAY_FIELD,
	                                        NAMES_INDEX_SIG)));
	il.append(new PUTFIELD(cpg.addFieldref(TRANSLET_CLASS,
	                                       NAMES_INDEX,
	                                       NAMES_INDEX_SIG)));
        constructor.markChunkEnd();
	
        constructor.markChunkStart();
	il.append(classGen.loadTranslet());
	il.append(new GETSTATIC(cpg.addFieldref(_className,
	                                        STATIC_URIS_ARRAY_FIELD,
	                                        URIS_INDEX_SIG)));
	il.append(new PUTFIELD(cpg.addFieldref(TRANSLET_CLASS,
	                                       URIS_INDEX,
	                                       URIS_INDEX_SIG)));
        constructor.markChunkEnd();

        constructor.markChunkStart();
	il.append(classGen.loadTranslet());
	il.append(new GETSTATIC(cpg.addFieldref(_className,
	                                        STATIC_TYPES_ARRAY_FIELD,
	                                        TYPES_INDEX_SIG)));
	il.append(new PUTFIELD(cpg.addFieldref(TRANSLET_CLASS,
	                                       TYPES_INDEX,
	                                       TYPES_INDEX_SIG)));
        constructor.markChunkEnd();

        constructor.markChunkStart();
	il.append(classGen.loadTranslet());
	il.append(new GETSTATIC(cpg.addFieldref(_className,
	                                        STATIC_NAMESPACE_ARRAY_FIELD,
	                                        NAMESPACE_INDEX_SIG)));
	il.append(new PUTFIELD(cpg.addFieldref(TRANSLET_CLASS,
	                                       NAMESPACE_INDEX,
	                                       NAMESPACE_INDEX_SIG)));
        constructor.markChunkEnd();

        constructor.markChunkStart();
	il.append(classGen.loadTranslet());
        il.append(new PUSH(cpg, AbstractTranslet.CURRENT_TRANSLET_VERSION));
	il.append(new PUTFIELD(cpg.addFieldref(TRANSLET_CLASS,
	                                       TRANSLET_VERSION_INDEX,
	                                       TRANSLET_VERSION_INDEX_SIG)));
        constructor.markChunkEnd();
	
	if (_hasIdCall) {
            constructor.markChunkStart();
	    il.append(classGen.loadTranslet());
	    il.append(new PUSH(cpg, Boolean.TRUE));
	    il.append(new PUTFIELD(cpg.addFieldref(TRANSLET_CLASS,
					           HASIDCALL_INDEX,
					           HASIDCALL_INDEX_SIG)));
            constructor.markChunkEnd();
	}
	
        // Compile in code to set the output configuration from <xsl:output>
	if (output != null) {
	    // Set all the output settings files in the translet
            constructor.markChunkStart();
	    output.translate(classGen, constructor);
            constructor.markChunkEnd();
	}

	// Compile default decimal formatting symbols.
	// This is an implicit, nameless xsl:decimal-format top-level element.
	if (_numberFormattingUsed) {
            constructor.markChunkStart();
	    DecimalFormatting.translateDefaultDFS(classGen, constructor);
            constructor.markChunkEnd();
        }

	il.append(RETURN);

	classGen.addMethod(constructor);
    }

    /**
     * Compile a topLevel() method into the output class. This method is 
     * called from transform() to handle all non-template top-level elements.
     * Returns the signature of the topLevel() method.
     *
     * Global variables/params and keys are first sorted to resolve 
     * dependencies between them. The XSLT 1.0 spec does not allow a key 
     * to depend on a variable. However, for compatibility with Xalan
     * interpretive, that type of dependency is allowed. Note also that
     * the buildKeys() method is still generated as it is used by the 
     * LoadDocument class, but it no longer called from transform().
     */
    private String compileTopLevel(ClassGenerator classGen) {
	final ConstantPoolGen cpg = classGen.getConstantPool();

	final org.apache.bcel.generic.Type[] argTypes = {
	    Util.getJCRefType(DOM_INTF_SIG),
	    Util.getJCRefType(NODE_ITERATOR_SIG),
	    Util.getJCRefType(TRANSLET_OUTPUT_SIG)
	};

	final String[] argNames = {
	    DOCUMENT_PNAME, ITERATOR_PNAME, TRANSLET_OUTPUT_PNAME
	};

	final InstructionList il = new InstructionList();

	final MethodGenerator toplevel =
	    new MethodGenerator(ACC_PUBLIC,
				org.apache.bcel.generic.Type.VOID,
				argTypes, argNames,
				"topLevel", _className, il,
				classGen.getConstantPool());

	toplevel.addException("org.apache.xalan.xsltc.TransletException");

	// Define and initialize 'current' variable with the root node
	final LocalVariableGen current = 
	    toplevel.addLocalVariable("current",
				      org.apache.bcel.generic.Type.INT,
				      null, null);

	final int setFilter = cpg.addInterfaceMethodref(DOM_INTF,
			       "setFilter",
			       "(Lorg/apache/xalan/xsltc/StripFilter;)V");

	final int gitr = cpg.addInterfaceMethodref(DOM_INTF,
							"getIterator",
							"()"+NODE_ITERATOR_SIG);
	il.append(toplevel.loadDOM());
	il.append(new INVOKEINTERFACE(gitr, 1));
        il.append(toplevel.nextNode());
	current.setStart(il.append(new ISTORE(current.getIndex())));

    // Create a new list containing variables/params + keys
    Vector varDepElements = new Vector(_globals);        
    Enumeration elements = elements();
    while (elements.hasMoreElements()) {
        final Object element = elements.nextElement();
        if (element instanceof Key) {
            varDepElements.add(element);
        }
    }
            
    // Determine a partial order for the variables/params and keys
    varDepElements = resolveDependencies(varDepElements);
    
    // Translate vars/params and keys in the right order
    final int count = varDepElements.size();
    for (int i = 0; i < count; i++) {
        final TopLevelElement tle = (TopLevelElement) varDepElements.elementAt(i);            
        tle.translate(classGen, toplevel);            
        if (tle instanceof Key) {
            final Key key = (Key) tle;
            _keys.put(key.getName(), key);
        }
    }

    // Compile code for other top-level elements
    Vector whitespaceRules = new Vector();
    elements = elements();
	while (elements.hasMoreElements()) {
	    final Object element = elements.nextElement();
	    // xsl:decimal-format
	    if (element instanceof DecimalFormatting) {
		((DecimalFormatting)element).translate(classGen,toplevel);
	    }
	    // xsl:strip/preserve-space
	    else if (element instanceof Whitespace) {
		whitespaceRules.addAll(((Whitespace)element).getRules());
	    }
	}

	// Translate all whitespace strip/preserve rules
	if (whitespaceRules.size() > 0) {
	    Whitespace.translateRules(whitespaceRules,classGen);
	}

	if (classGen.containsMethod(STRIP_SPACE, STRIP_SPACE_PARAMS) != null) {
	    il.append(toplevel.loadDOM());
	    il.append(classGen.loadTranslet());
	    il.append(new INVOKEINTERFACE(setFilter, 2));
	}

	il.append(RETURN);

	// Compute max locals + stack and add method to class
	classGen.addMethod(toplevel);
	
	return("("+DOM_INTF_SIG+NODE_ITERATOR_SIG+TRANSLET_OUTPUT_SIG+")V");
    }
    
    /**
     * This method returns a vector with variables/params and keys in the 
     * order in which they are to be compiled for initialization. The order
     * is determined by analyzing the dependencies between them. The XSLT 1.0 
     * spec does not allow a key to depend on a variable. However, for 
     * compatibility with Xalan interpretive, that type of dependency is 
     * allowed and, therefore, consider to determine the partial order.
     */
    private Vector resolveDependencies(Vector input) {
	/* DEBUG CODE - INGORE 
	for (int i = 0; i < input.size(); i++) {
	    final TopLevelElement e = (TopLevelElement) input.elementAt(i);
	    System.out.println("e = " + e + " depends on:");
            Vector dep = e.getDependencies();
            for (int j = 0; j < (dep != null ? dep.size() : 0); j++) {
                System.out.println("\t" + dep.elementAt(j));
            }
	}
	System.out.println("=================================");	
        */

	Vector result = new Vector();
	while (input.size() > 0) {
	    boolean changed = false;
	    for (int i = 0; i < input.size(); ) {
		final TopLevelElement vde = (TopLevelElement) input.elementAt(i);
		final Vector dep = vde.getDependencies();
		if (dep == null || result.containsAll(dep)) {
		    result.addElement(vde);
		    input.remove(i);
		    changed = true;
		}
		else {
		    i++;
		}
	    }

	    // If nothing was changed in this pass then we have a circular ref
	    if (!changed) {
		ErrorMsg err = new ErrorMsg(ErrorMsg.CIRCULAR_VARIABLE_ERR,
					    input.toString(), this);
		getParser().reportError(Constants.ERROR, err);
		return(result);
	    }
	}

	/* DEBUG CODE - INGORE 
	System.out.println("=================================");
	for (int i = 0; i < result.size(); i++) {
	    final TopLevelElement e = (TopLevelElement) result.elementAt(i);
	    System.out.println("e = " + e);
	}
        */

	return result;
    }

    /**
     * Compile a buildKeys() method into the output class. Note that keys 
     * for the input document are created in topLevel(), not in this method. 
     * However, we still need this method to create keys for documents loaded
     * via the XPath document() function. 
     */
    private String compileBuildKeys(ClassGenerator classGen) {
	final ConstantPoolGen cpg = classGen.getConstantPool();

	final org.apache.bcel.generic.Type[] argTypes = {
	    Util.getJCRefType(DOM_INTF_SIG),
	    Util.getJCRefType(NODE_ITERATOR_SIG),
	    Util.getJCRefType(TRANSLET_OUTPUT_SIG),
	    org.apache.bcel.generic.Type.INT
	};

	final String[] argNames = {
	    DOCUMENT_PNAME, ITERATOR_PNAME, TRANSLET_OUTPUT_PNAME, "current"
	};

	final InstructionList il = new InstructionList();

	final MethodGenerator buildKeys =
	    new MethodGenerator(ACC_PUBLIC,
				org.apache.bcel.generic.Type.VOID,
				argTypes, argNames,
				"buildKeys", _className, il,
				classGen.getConstantPool());

	buildKeys.addException("org.apache.xalan.xsltc.TransletException");
	
	final Enumeration elements = elements();
	while (elements.hasMoreElements()) {
	    // xsl:key
	    final Object element = elements.nextElement();
	    if (element instanceof Key) {
		final Key key = (Key)element;
		key.translate(classGen, buildKeys);
		_keys.put(key.getName(),key);
	    }
	}
	
	il.append(RETURN);
	
	// Add method to class
        classGen.addMethod(buildKeys);
	
	return("("+DOM_INTF_SIG+NODE_ITERATOR_SIG+TRANSLET_OUTPUT_SIG+"I)V");
    }

    /**
     * Compile transform() into the output class. This method is used to 
     * initialize global variables and global parameters. The current node
     * is set to be the document's root node.
     */
    private void compileTransform(ClassGenerator classGen) {
	final ConstantPoolGen cpg = classGen.getConstantPool();

	/* 
	 * Define the the method transform with the following signature:
	 * void transform(DOM, NodeIterator, HandlerBase)
	 */
	final org.apache.bcel.generic.Type[] argTypes = 
	    new org.apache.bcel.generic.Type[3];
	argTypes[0] = Util.getJCRefType(DOM_INTF_SIG);
	argTypes[1] = Util.getJCRefType(NODE_ITERATOR_SIG);
	argTypes[2] = Util.getJCRefType(TRANSLET_OUTPUT_SIG);

	final String[] argNames = new String[3];
	argNames[0] = DOCUMENT_PNAME;
	argNames[1] = ITERATOR_PNAME;
	argNames[2] = TRANSLET_OUTPUT_PNAME;

	final InstructionList il = new InstructionList();
	final MethodGenerator transf =
	    new MethodGenerator(ACC_PUBLIC,
				org.apache.bcel.generic.Type.VOID,
				argTypes, argNames,
				"transform",
				_className,
				il,
				classGen.getConstantPool());
	transf.addException("org.apache.xalan.xsltc.TransletException");

	// Define and initialize current with the root node
	final LocalVariableGen current = 
	    transf.addLocalVariable("current",
				    org.apache.bcel.generic.Type.INT,
				    null, null);
	final String applyTemplatesSig = classGen.getApplyTemplatesSig();
	final int applyTemplates = cpg.addMethodref(getClassName(),
						    "applyTemplates",
						    applyTemplatesSig);
	final int domField = cpg.addFieldref(getClassName(),
					     DOM_FIELD,
					     DOM_INTF_SIG);

	// push translet for PUTFIELD
	il.append(classGen.loadTranslet());
	// prepare appropriate DOM implementation
	
	if (isMultiDocument()) {
	    il.append(new NEW(cpg.addClass(MULTI_DOM_CLASS)));
	    il.append(DUP);
	}
	
	il.append(classGen.loadTranslet());
	il.append(transf.loadDOM());
	il.append(new INVOKEVIRTUAL(cpg.addMethodref(TRANSLET_CLASS,
						     "makeDOMAdapter",
						     "("+DOM_INTF_SIG+")"+
						     DOM_ADAPTER_SIG)));
	// DOMAdapter is on the stack

	if (isMultiDocument()) {
	    final int init = cpg.addMethodref(MULTI_DOM_CLASS,
					      "<init>",
					      "("+DOM_INTF_SIG+")V");
	    il.append(new INVOKESPECIAL(init));
	    // MultiDOM is on the stack
	}
	
	//store to _dom variable
	il.append(new PUTFIELD(domField));

	// continue with globals initialization
	final int gitr = cpg.addInterfaceMethodref(DOM_INTF,
							"getIterator",
							"()"+NODE_ITERATOR_SIG);
	il.append(transf.loadDOM());
	il.append(new INVOKEINTERFACE(gitr, 1));
        il.append(transf.nextNode());
	current.setStart(il.append(new ISTORE(current.getIndex())));

	// Transfer the output settings to the output post-processor
	il.append(classGen.loadTranslet());
	il.append(transf.loadHandler());
	final int index = cpg.addMethodref(TRANSLET_CLASS,
					   "transferOutputSettings",
					   "("+OUTPUT_HANDLER_SIG+")V");
	il.append(new INVOKEVIRTUAL(index));

        /*
         * Compile buildKeys() method. Note that this method is not 
         * invoked here as keys for the input document are now created
         * in topLevel(). However, this method is still needed by the
         * LoadDocument class.
         */        
        final String keySig = compileBuildKeys(classGen);
        final int keyIdx = cpg.addMethodref(getClassName(),
                                               "buildKeys", keySig);
                
        // Look for top-level elements that need handling
	final Enumeration toplevel = elements();
	if (_globals.size() > 0 || toplevel.hasMoreElements()) {
	    // Compile method for handling top-level elements
	    final String topLevelSig = compileTopLevel(classGen);
	    // Get a reference to that method
	    final int topLevelIdx = cpg.addMethodref(getClassName(),
						     "topLevel",
						     topLevelSig);
	    // Push all parameters on the stack and call topLevel()
	    il.append(classGen.loadTranslet()); // The 'this' pointer
	    il.append(classGen.loadTranslet());
	    il.append(new GETFIELD(domField));  // The DOM reference
	    il.append(transf.loadIterator());
	    il.append(transf.loadHandler());    // The output handler
	    il.append(new INVOKEVIRTUAL(topLevelIdx));
	}	

	// start document
	il.append(transf.loadHandler());
	il.append(transf.startDocument());

	// push first arg for applyTemplates
	il.append(classGen.loadTranslet());
	// push translet for GETFIELD to get DOM arg
	il.append(classGen.loadTranslet());
	il.append(new GETFIELD(domField));
	// push remaining 2 args
	il.append(transf.loadIterator());
	il.append(transf.loadHandler());
	il.append(new INVOKEVIRTUAL(applyTemplates));
	// endDocument
	il.append(transf.loadHandler());
	il.append(transf.endDocument());

	il.append(RETURN);

	// Compute max locals + stack and add method to class
	classGen.addMethod(transf);
    }

    /**
     * Peephole optimization: Remove sequences of [ALOAD, POP].
     */
    private void peepHoleOptimization(MethodGenerator methodGen) {
	final String pattern = "`aload'`pop'`instruction'";
	final InstructionList il = methodGen.getInstructionList();
	final InstructionFinder find = new InstructionFinder(il);
	for(Iterator iter=find.search(pattern); iter.hasNext(); ) {
	    InstructionHandle[] match = (InstructionHandle[])iter.next();
	    try {
		il.delete(match[0], match[1]);
	    } 
	    catch (TargetLostException e) {
            	// TODO: move target down into the list
            }
	}
    }

    public int addParam(Param param) {
	_globals.addElement(param);
	return _globals.size() - 1;
    }

    public int addVariable(Variable global) {
	_globals.addElement(global);
	return _globals.size() - 1;
    }

    public void display(int indent) {
	indent(indent);
	Util.println("Stylesheet");
	displayContents(indent + IndentIncrement);
    }

    // do we need this wrapper ?????
    public String getNamespace(String prefix) {
	return lookupNamespace(prefix);
    }

    public String getClassName() {
	return _className;
    }

    public Vector getTemplates() {
	return _templates;
    }

    public Vector getAllValidTemplates() {
        // Return templates if no imported/included stylesheets
        if (_includedStylesheets == null) {
            return _templates;
        }
        
        // Is returned value cached?
        if (_allValidTemplates == null) {
           Vector templates = new Vector();
            int size = _includedStylesheets.size();
            for (int i = 0; i < size; i++) {
                Stylesheet included =(Stylesheet)_includedStylesheets.elementAt(i);
                templates.addAll(included.getAllValidTemplates());
            }
            templates.addAll(_templates);

            // Cache results in top-level stylesheet only
            if (_parentStylesheet != null) {
                return templates;
            }
            _allValidTemplates = templates;
         }
        
        return _allValidTemplates;
    }
    
    protected void addTemplate(Template template) {
        _templates.addElement(template);
    }
}
