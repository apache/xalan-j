/*
 * @(#)$Id$
 *
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
 * originally based on software copyright (c) 2001, Sun
 * Microsystems., http://www.sun.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author G. Todd Miller
 * @author Morten Jorgensen
 * @author John Howard (johnh@schemasoft.com)
 *
 */

package org.apache.xalan.xsltc.compiler;

import java.io.*;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Date;
import java.util.Map;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.jar.*;

import org.xml.sax.*;

import javax.xml.parsers.*;

import org.apache.xalan.xsltc.compiler.util.*;
import org.apache.xalan.xsltc.cmdline.getopt.*;
import org.apache.xalan.xsltc.DOM;
import org.apache.bcel.classfile.JavaClass;

public final class XSLTC {

    /**
     * These constants define the various translet output methods.
     */
    public static final int FILE_OUTPUT        = 0;
    public static final int JAR_OUTPUT         = 1;
    public static final int BYTEARRAY_OUTPUT   = 2;
    public static final int CLASSLOADER_OUTPUT = 3;
    public static final int BYTEARRAY_AND_FILE_OUTPUT = 4;
    public static final int BYTEARRAY_AND_JAR_OUTPUT  = 5;

    /**
     * A reference to the parser object.
     */
    private Parser _parser;

    /**
     * A reference to the XMLReader (producer of SAX events) from
     * which the stylesheet will be read.
     */
    private XMLReader _reader = null;

    /**
     * The source loader used for xsl:include and xsl:import.
     */
    private SourceLoader _loader = null;

    /**
     * A reference to the stylesheet being compiled.
     */
    private Stylesheet _stylesheet = null;

    /**
     * Class name either set by -o <class-name> or inferred
     * from stylesheet name.
     */
    private String _className = null;

    /**
     * Command-line option -x.
     */
    private boolean _debug = false;

    /**
     * Command-line option -j <jar-file-name>
     */
    private String _jarFileName = null;

    /**
     * Command-line option -p <package-name>
     */
    private String _packageName = null;

    /**
     * Command-line option -d <directory-name>
     */
    private File _destDir = null;

    /**
     * Compiler's output type.
     */
    private int _outputType = FILE_OUTPUT;

    /**
     * Array of classes represented as byte arrays. This array holds
     * all the classes compiled by XSLTC.
     */
    private ArrayList _classes;

    /**
     * Array needed for the generation of jar files (-j option).
     */
    private ArrayList _bcelClasses;

    /**
     * A reference to the (single) instance of the compiler context.
     */
    private CompilerContext _ccontext;

    /**
     * XSLTC compiler constructor
     */
    public XSLTC() {
	_parser = new Parser(this);
	_ccontext = CompilerContext.getInstance(this);
    }

    /**
     * Initializes the compiler to compile a new stylesheet
     */
    public void init() {
	reset();
	_reader = null;
	_classes = new ArrayList();
	_bcelClasses = new ArrayList();
    }

    /**
     * Initializes the compiler to produce a new translet
     */
    private void reset() {
	_parser.init();
	_ccontext.init();

        // IMPORTANT TODO - reset all thread variables here!!!
        // How are we going to do this. Can we get a list
        // of all the thread variables???
    }

    /**
     * Returns a reference to the parser object.
     */
    public Parser getParser() {
        return _parser;
    }

    /**
     * Sets a new parser object (used by TrAX).
     */
    public void setParser(Parser parser) {
        _parser = parser;
    }

    /**
     * Returns a reference to the compiler context (used by TrAX).
     */
    public CompilerContext getCompilerContext() {
        return _ccontext;
    }

    /**
     * Set the output type of the transform (used by TrAX).
     */
    public void setOutputType(int type) {
        _outputType = type;
    }

    /**
     * Turn debugging messages on/off.
     */
    public void setDebug(boolean debug) {
        _debug = debug;
    }

    /**
     * Get current debugging message setting.
     */
    public boolean getDebug() {
        return _debug;
    }

    /**
     * Returns the output properties (used by TrAX).
     */
    public Properties getOutputProperties() {
        return _parser.getOutputProperties();
    }

    /**
     * Defines an external SourceLoader to provide the compiler with
     * documents referenced in xsl:include and xsl:import.
     *
     * @param loader The SourceLoader to use for include/import
     */
    public void setSourceLoader(SourceLoader loader) {
	_loader = loader;
    }

    /**
     * Set the parameters to use to locate the correct <?xml-stylesheet ...?>
     * processing instruction in the case where the input document to the
     * compiler (and parser) is an XML document.
     *
     * @param media The media attribute to be matched. May be null, in which
     * case the prefered templates will be used (i.e. alternate = no).
     * @param title The value of the title attribute to match. May be null.
     * @param charset The value of the charset attribute to match. May be null.
     */
    public void setPIParameters(String media, String title, String charset) {
	_parser.setPIParameters(media, title, charset);
    }

    /**
     * Compiles an XSL stylesheet pointed to by a URL
     *
     * @param url An URL containing the input XSL stylesheet
     */
    public boolean compile(URL url) {
	try {
	    // Open input stream from URL and wrap inside InputSource
	    final InputStream stream = url.openStream();
	    final InputSource input = new InputSource(stream);
	    input.setSystemId(url.toString());
	    return compile(input, _className);
	}
	catch (IOException e) {
	    _parser.reportError(Constants.FATAL, new ErrorMsg(e.getMessage()));
	    return false;
	}
    }

    /**
     * Compiles an XSL stylesheet pointed to by a URL.
     *
     * @param url An URL containing the input XSL stylesheet.
     * @param name The name to assign to the translet class.
     */
    public boolean compile(URL url, String name) {
	try {
	    // Open input stream from URL and wrap inside InputSource
	    final InputStream stream = url.openStream();
	    final InputSource input = new InputSource(stream);
	    input.setSystemId(url.toString());
	    return compile(input, name);
	}
	catch (IOException e) {
	    _parser.reportError(Constants.FATAL,
                                new ErrorMsg(e.getMessage()));
	    return false;
	}
    }

    /**
     * Compiles an XSL stylesheet from an InputStream.
     *
     * @param input An InputStream that will pass in the stylesheet
     *        contents.
     * @param name The name of the translet class to generate.
     * @return 'true' if the compilation was successful.
     */
    public boolean compile(InputStream stream, String name) {
	final InputSource input = new InputSource(stream);
	input.setSystemId(name); // We have nothing else!!!
	return compile(input, name);
    }

    /**
     * Compiles a set of stylesheets from a list of URLs.
     *
     * @param stylesheets A ArrayList containing URLs.
     * @return 'true' if the compilation was successful.
     */
    public boolean compile(ArrayList stylesheets) {
        // Get the number of stylesheets (ie. URLs) in the vector
        final int count = stylesheets.size();

        // Return straight away if the vector is empty
        if (count == 0) return true;

        // Special handling needed if the URL count is one, becuase the
        // _className global must not be reset if it was set explicitly
        if (count == 1) {
            final Object url = stylesheets.get(0);
            if (url instanceof URL) {
                return compile((URL)url);
            }
            return false;
        }
        else {
            // Traverse all elements in the vector and compile
            final Iterator urls = stylesheets.iterator();

            while (urls.hasNext()) {
                _className = null; // reset, so that new name will be computed
                final Object url = urls.next();
                if (url instanceof URL) {
                    if (!compile((URL)url)) return false;
                }
            }
        }
        return true;
    }

    /**
     * Compiles an XSL stylesheet passed in through an InputStream
     * @param input An InputSource that will pass in the stylesheet contents
     * @param name The name of the translet class to generate - can be null
     * @return 'true' if the compilation was successful
     */
    public boolean compile(InputSource input, String name) {
	try {
	    // Reset globals in case we're called by compile(ArrayList v);
	    reset();

	    // The systemId may not be set, so we'll have to check the URL
	    String systemId = null;
	    if (input != null) {
	        systemId = input.getSystemId();
	    }

	    // Set the translet class name if not already set
	    if (_className == null) {
		if (name != null) {
		    setClassName(name);
		}
		else if ((systemId != null) && (!systemId.equals(""))) {
		    setClassName(Util.baseName(systemId));
		}
		else {
		    setClassName("GregorSamsa"); // default translet name
		}
	    }

	    // Get the root node of the abstract syntax tree
	    SyntaxTreeNode element = null;
	    if (_reader == null) {
		element = _parser.parse(input);
	    }
	    else {
		element = _parser.parse(_reader, input);
	    }

	    // Compile the translet - this is where the work is done!
	    if (!_parser.errorsFound() && element != null) {
		// Create a Stylesheet element from the root node
		_stylesheet = _parser.makeStylesheet(element);
		_stylesheet.setSourceLoader(_loader);
		_stylesheet.setSystemId(systemId);
		_stylesheet.setParentStylesheet(null);

                // Set current stylesheet in static context
                StaticContext scontext = StaticContext.getInstance(null);
                scontext.setCurrentStylesheet(_stylesheet);

		// Create, parse and typecheck AST
		_parser.createAST(_stylesheet);
	    }

	    // Generate the bytecodes and output the translet class(es)
	    if (!_parser.errorsFound() && _stylesheet != null) {
		// Class synchronization is needed for BCEL
		synchronized (getClass()) {
		    _stylesheet.translate();
		}
	    }
	}
	catch (Exception e) {
	    if (_debug) e.printStackTrace();
	    _parser.reportError(Constants.FATAL, new ErrorMsg(e.getMessage()));
	}
	catch (Error e) {
	    if (_debug) e.printStackTrace();
	    _parser.reportError(Constants.FATAL, new ErrorMsg(e.getMessage()));
	}
	finally {
	    _reader = null; // reset this here to be sure it is not re-used
	    return !_parser.errorsFound();
	}
    }

    /**
     * Returns an array of bytecode arrays generated by the compilation
     * process.
     *
     * @return JVM bytecodes for translet and helper classes.
     */
    public byte[][] getBytecodes() {
	final int count = _classes.size();
	final byte[][] result = new byte[count][1];
	for (int i = 0; i < count; i++) {
	    result[i] = (byte[])_classes.get(i);
	}
	return result;
    }

    /**
     * Compiles a stylesheet from an InputSource. The result is returned
     * as an array of byte arrays.
     *
     * @param name The name of the translet class to generate.
     * @param input An InputSource that will pass in the stylesheet contents.
     * @param outputType The output type.
     * @return JVM bytecodes that represent translet class definition.
     */
    public byte[][] compile(String name, InputSource input, int outputType) {
	_outputType = outputType;
	if (compile(input, name)) {
	    return getBytecodes();
	}
        return null;
    }

    /**
     * Compiles a stylesheet pointed to by a URL. The result is put in a
     * set of byte arrays. One byte array for each generated class.
     * @param name The name of the translet class to generate
     * @param input An InputSource that will pass in the stylesheet contents
     * @return JVM bytecodes that represent translet class definition
     */
    public byte[][] compile(String name, InputSource input) {
        return compile(name, input, BYTEARRAY_OUTPUT);
    }

    /**
     * Set the XMLReader to use for parsing the next input stylesheet
     * @param reader XMLReader (SAX2 parser) to use
     */
    public void setXMLReader(XMLReader reader) {
	_reader = reader;
    }

    /**
     * Get a ArrayList containing all compile error messages
     * @return A ArrayList containing all compile error messages
     */
    public ArrayList getErrors() {
	return _parser.getErrors();
    }

    /**
     * Get a ArrayList containing all compile warning messages
     * @return A ArrayList containing all compile error messages
     */
    public ArrayList getWarnings() {
	return _parser.getWarnings();
    }

    /**
     * Print all compile error messages to standard output
     */
    public void printErrors() {
	_parser.printErrors();
    }

    /**
     * Print all compile warning messages to standard output
     */
    public void printWarnings() {
	_parser.printWarnings();
    }

    /**
     * Set the class name for the generated translet. This class name is
     * overridden if multiple stylesheets are compiled in one go using the
     * compile(ArrayList urls) method.
     * @param className The name to assign to the translet class
     */
    public void setClassName(String className) {
	final String base  = Util.baseName(className);
	final String noext = Util.noExtName(base);
	String name  = Util.toJavaName(noext);

	if (_packageName == null) {
	    _className = name;
	}
	else {
	    _className = _packageName + '.' + name;
	}
    }

    /**
     * Get the class name for the generated translet.
     */
    public String getClassName() {
	return _className;
    }

    /**
     * Convert for Java class name of local system file name.
     * (Replace '.' with '/' on UNIX and replace '.' by '\' on Windows/DOS.)
     */
    private String classFileName(final String className) {
	return className.replace('.', File.separatorChar) + ".class";
    }

    /**
     * Generate an output File object to send the translet to
     */
    private File getOutputFile(String className) {
	if (_destDir != null) {
	    return new File(_destDir, classFileName(className));
	}
	else {
	    return new File(classFileName(className));
	}
    }

    /**
     * Set the destination directory for the translet.
     * The current working directory will be used by default.
     */
    public boolean setDestDirectory(String dstDirName) {
	final File dir = new File(dstDirName);
	if (dir.exists() || dir.mkdirs()) {
	    _destDir = dir;
	    return true;
	}
	else {
	    _destDir = null;
	    return false;
	}
    }

    /**
     * Set an optional package name for the translet and auxiliary classes
     */
    public void setPackageName(String packageName) {
	_packageName = packageName;
	if (_className != null) setClassName(_className);
    }

    /**
     * Set the name of an optional JAR-file to dump the translet and
     * auxiliary classes to
     */
    public void setJarFileName(String jarFileName) {
	final String JAR_EXT = ".jar";
	if (jarFileName.endsWith(JAR_EXT)) {
	    _jarFileName = jarFileName;
	}
	else {
	    _jarFileName = jarFileName + JAR_EXT;
	}
	_outputType = JAR_OUTPUT;
    }

    public String getJarFileName() {
	return _jarFileName;
    }

    /**
     * Set the top-level stylesheet
     */
    public void setStylesheet(Stylesheet stylesheet) {
	if (_stylesheet == null) _stylesheet = stylesheet;
    }

    /**
     * Returns the top-level stylesheet
     */
    public Stylesheet getStylesheet() {
	return _stylesheet;
    }

    public void dumpClass(JavaClass clazz) {

	if (_outputType == FILE_OUTPUT ||
	    _outputType == BYTEARRAY_AND_FILE_OUTPUT)
	{
	    File outFile = getOutputFile(clazz.getClassName());
	    String parentDir = outFile.getParent();
	    if (parentDir != null) {
	      	File parentFile = new File(parentDir);
	      	if (!parentFile.exists())
	            parentFile.mkdirs();
	    }
	}

	try {
	    switch (_outputType) {
	    case FILE_OUTPUT:
		clazz.dump(
		    new BufferedOutputStream(
			new FileOutputStream(
			    getOutputFile(clazz.getClassName()))));
		break;
	    case JAR_OUTPUT:
		_bcelClasses.add(clazz);
		break;
	    case BYTEARRAY_OUTPUT:
	    case BYTEARRAY_AND_FILE_OUTPUT:
	    case BYTEARRAY_AND_JAR_OUTPUT:
	    case CLASSLOADER_OUTPUT:
		ByteArrayOutputStream out = new ByteArrayOutputStream(2048);
		clazz.dump(out);
		_classes.add(out.toByteArray());

		if (_outputType == BYTEARRAY_AND_FILE_OUTPUT)
		  clazz.dump(new BufferedOutputStream(
			new FileOutputStream(getOutputFile(clazz.getClassName()))));
		else if (_outputType == BYTEARRAY_AND_JAR_OUTPUT)
		  _bcelClasses.add(clazz);

		break;
	    }
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /**
     * File separators are converted to forward slashes for ZIP files.
     */
    private String entryName(File f) throws IOException {
	return f.getName().replace(File.separatorChar, '/');
    }

    /**
     * Generate output JAR-file and packages
     */
    public void outputToJar() throws IOException {
	// create the manifest
	final Manifest manifest = new Manifest();
	final java.util.jar.Attributes atrs = manifest.getMainAttributes();
	atrs.put(java.util.jar.Attributes.Name.MANIFEST_VERSION,"1.2");

	final Map map = manifest.getEntries();
	// create manifest
	Iterator classes = _bcelClasses.iterator();
	final String now = (new Date()).toString();
	final java.util.jar.Attributes.Name dateAttr =
	    new java.util.jar.Attributes.Name("Date");

	while (classes.hasNext()) {
	    final JavaClass clazz = (JavaClass)classes.next();
	    final String className = clazz.getClassName().replace('.','/');
	    final java.util.jar.Attributes attr = new java.util.jar.Attributes();

            attr.put(dateAttr, now);
	    map.put(className+".class", attr);
	}

	final File jarFile = new File(_destDir, _jarFileName);
	final JarOutputStream jos =
	    new JarOutputStream(new FileOutputStream(jarFile), manifest);
	classes = _bcelClasses.iterator();

        while (classes.hasNext()) {
	    final JavaClass clazz = (JavaClass)classes.next();
	    final String className = clazz.getClassName().replace('.','/');
	    jos.putNextEntry(new JarEntry(className+".class"));
	    final ByteArrayOutputStream out = new ByteArrayOutputStream(2048);
	    clazz.dump(out); // dump() closes it's output stream
	    out.writeTo(jos);
	}
	jos.close();
    }
}
