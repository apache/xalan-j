/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES{} LOSS OF
 * USE, DATA, OR PROFITS{} OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
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
 * @author Santiago Pericas-Geertsen
 * @author G. Todd Miller 
 *
 */
package org.apache.xml.serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.BitSet;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;

import org.apache.xml.res.XMLErrorResources;
import org.apache.xml.res.XMLMessages;
import org.apache.xml.utils.BoolStack;
import org.apache.xml.utils.FastStringBuffer;
import org.apache.xml.utils.QName;
import org.apache.xml.utils.SystemIDResolver;
import org.apache.xml.utils.TreeWalker;
import org.apache.xml.utils.WrappedRuntimeException;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

//import com.sun.media.sound.IESecurity;

/**
 * This abstract class is a base class for other serializers (xml, html, text
 * ...) that write output to a stream.
 * @author minchau
 *
 */

abstract public class ToStream extends SerializerBase
{

    private static final String COMMENT_BEGIN = "<!--";
    private static final String COMMENT_END = "-->";

    /** Stack to keep track of disabling output escaping. */
    protected BoolStack m_disableOutputEscapingStates = new BoolStack();

    /**
     * Boolean that tells if we already tried to get the converter.
     */
    boolean m_triedToGetConverter = false;
    /**
     * Method reference to the sun.io.CharToByteConverter#canConvert method 
     * for this encoding.  Invalid if m_charToByteConverter is null.
     */
    java.lang.reflect.Method m_canConvertMeth;

    /**
     * Opaque reference to the sun.io.CharToByteConverter for this 
     * encoding.
     */
    Object m_charToByteConverter = null;

    /**
     * Stack to keep track of whether or not we need to
     * preserve whitespace.
     */
    protected BoolStack m_preserves = new BoolStack();

    /**
     * State flag to tell if preservation of whitespace
     * is important.
     */
    protected boolean m_ispreserve = false;

    /**
     * State flag that tells if the previous node processed
     * was text, so we can tell if we should preserve whitespace.
     */
    protected boolean m_isprevtext = false;

    /**
     * A stack of Boolean objects that tell if the given element
     * has children.
     */
    // protected BoolStack m_elemStack = new BoolStack();

    /**
     * Map that tells which XML characters should have special treatment, and it
     *  provides character to entity name lookup.
     */
    private static CharInfo m_xmlcharInfo =
        //      new CharInfo(CharInfo.XML_ENTITIES_RESOURCE);
    CharInfo.getCharInfo(CharInfo.XML_ENTITIES_RESOURCE);

    /**
     * The maximum character size before we have to resort
     * to escaping.
     */
    protected int m_maxCharacter = Encodings.getLastPrintable();

    /**
     * Use the system line seperator to write line breaks.
     */
    protected final char[] m_lineSep =
        System.getProperty("line.separator").toCharArray();

    /**
     * The length of the line seperator, since the write is done
     * one character at a time.
     */
    protected final int m_lineSepLen = m_lineSep.length;

    /**
     * Map that tells which characters should have special treatment, and it
     *  provides character to entity name lookup.
     */
    protected CharInfo m_charInfo;

    /** Table of user-specified char infos. */
    private static Hashtable m_charInfos = null;

    /** True if we control the buffer, and we should flush the output on endDocument. */
    boolean m_shouldFlush = true;

    //    protected OutputBuffer _buffer = null;

    /**
     * Add space before '/>' for XHTML.
     */
    protected boolean m_spaceBeforeClose = false;

    /**
     * Flag to signal that a newline should be added.
     */
    boolean m_startNewLine;

    /**
     * Tells if we're in an internal document type subset.
     */
    protected boolean m_inDoctype = false;

    /**
       * Flag to quickly tell if the encoding is UTF8.
       */
    boolean m_isUTF8 = false;

    /** The xsl:output properties. */
    protected Properties m_format;

    /**
     * remembers if we are in between the startCDATA() and endCDATA() callbacks
     */
    protected boolean m_cdataStartCalled = false;

    /**
     * Default constructor
     */
    public ToStream()
    {
    }

    /**
     * This helper method to writes out "]]>" when closing a CDATA section.
     *
     * @throws org.xml.sax.SAXException
     */
    protected void closeCDATA() throws org.xml.sax.SAXException
    {
        try
        {
            m_writer.write(CDATA_DELIMITER_CLOSE);
            // write out a CDATA section closing "]]>"
            m_cdataTagOpen = false; // Remember that we have done so.
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }
    }

    /**
     * Serializes the DOM node. Throws an exception only if an I/O
     * exception occured while serializing.
     *
     * @param elem The element to serialize
     *
     * @param node Node to serialize.
     * @throws IOException An I/O exception occured while serializing
     */
    public void serialize(Node node) throws IOException
    {

        try
        {
            TreeWalker walker =
                new TreeWalker(this, new org.apache.xml.utils.DOM2Helper());

            walker.traverse(node);
        }
        catch (org.xml.sax.SAXException se)
        {
            throw new WrappedRuntimeException(se);
        }
    }

    /**
     * Return true if the character is the high member of a surrogate pair.
     *
     * NEEDSDOC @param c
     *
     * NEEDSDOC ($objectName$) @return
     */
    static final boolean isUTF16Surrogate(char c)
    {
        return (c & 0xFC00) == 0xD800;
    }

    /**
     * Taken from XSLTC 
     */
    private boolean m_escaping = true;

    /**
     * Flush the formatter's result stream.
     *
     * @throws org.xml.sax.SAXException
     */
    protected final void flushWriter() throws org.xml.sax.SAXException
    {

        if (null != m_writer)
        {
            try
            {
                if (m_writer instanceof WriterToUTF8Buffered)
                {
                    if (m_shouldFlush)
                         ((WriterToUTF8Buffered) m_writer).flush();
                    else
                         ((WriterToUTF8Buffered) m_writer).flushBuffer();
                }
                if (m_writer instanceof WriterToUTF8)
                {
                    if (m_shouldFlush)
                        m_writer.flush();
                }
                else if (m_writer instanceof WriterToASCI)
                {
                    if (m_shouldFlush)
                        m_writer.flush();
                }
                else
                {
                    // Flush always. 
                    // Not a great thing if the writer was created 
                    // by this class, but don't have a choice.
                    m_writer.flush();
                }
            }
            catch (IOException ioe)
            {
                throw new org.xml.sax.SAXException(ioe);
            }
        }
    }

    /**
     * Get the output stream where the events will be serialized to.
     *
     * @return reference to the result stream, or null of only a writer was
     * set.
     */
    public OutputStream getOutputStream()
    {

        if (m_writer instanceof WriterToUTF8Buffered)
            return ((WriterToUTF8Buffered) m_writer).getOutputStream();
        if (m_writer instanceof WriterToUTF8)
            return ((WriterToUTF8) m_writer).getOutputStream();
        else if (m_writer instanceof WriterToASCI)
            return ((WriterToASCI) m_writer).getOutputStream();
        else
            return null;
    }

    // Implement DeclHandler

    /**
     *   Report an element type declaration.
     *  
     *   <p>The content model will consist of the string "EMPTY", the
     *   string "ANY", or a parenthesised group, optionally followed
     *   by an occurrence indicator.  The model will be normalized so
     *   that all whitespace is removed,and will include the enclosing
     *   parentheses.</p>
     *  
     *   @param name The element type name.
     *   @param model The content model as a normalized string.
     *   @exception SAXException The application may raise an exception.
     */
    public void elementDecl(String name, String model) throws SAXException
    {
        // Do not inline external DTD
        if (m_inExternalDTD)
            return;
        try
        {

            if (m_inDoctype)
            {
                m_writer.write(" [");
                m_writer.write(m_lineSep, 0, m_lineSepLen);

                m_inDoctype = false;
            }

            m_writer.write("<!ELEMENT ");
            m_writer.write(name);
            m_writer.write(' ');
            m_writer.write(model);
            m_writer.write('>');
            m_writer.write(m_lineSep, 0, m_lineSepLen);
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }

    }

    /**
     * Report an internal entity declaration.
     *
     * <p>Only the effective (first) declaration for each entity
     * will be reported.</p>
     *
     * @param name The name of the entity.  If it is a parameter
     *        entity, the name will begin with '%'.
     * @param value The replacement text of the entity.
     * @exception SAXException The application may raise an exception.
     * @see #externalEntityDecl
     * @see org.xml.sax.DTDHandler#unparsedEntityDecl
     */
    public void internalEntityDecl(String name, String value)
        throws SAXException
    {
        // Do not inline external DTD
        if (m_inExternalDTD)
            return;
        try
        {
            if (m_inDoctype)
            {
                m_writer.write(" [");
                m_writer.write(m_lineSep, 0, m_lineSepLen);

                m_inDoctype = false;
            }

            outputEntityDecl(name, value);
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }

    }

    /**
     * Output the doc type declaration.
     *
     * @param name non-null reference to document type name.
     * NEEDSDOC @param value
     *
     * @throws org.xml.sax.SAXException
     */
    void outputEntityDecl(String name, String value) throws IOException
    {

        m_writer.write("<!ENTITY ");
        m_writer.write(name);
        m_writer.write(" \"");
        m_writer.write(value);
        m_writer.write("\">");
        m_writer.write(m_lineSep, 0, m_lineSepLen);
    }

    /**
     * Output a system-dependent line break.
     *
     * @throws org.xml.sax.SAXException
     */
    protected final void outputLineSep() throws IOException
    {

        m_writer.write(m_lineSep, 0, m_lineSepLen);
    }

    /**
     * Specifies an output format for this serializer. It the
     * serializer has already been associated with an output format,
     * it will switch to the new format. This method should not be
     * called while the serializer is in the process of serializing
     * a document.
     *
     * @param format The output format to use
     */
    public void setOutputFormat(Properties format)
    {

        boolean shouldFlush = m_shouldFlush;

        init(m_writer, format, false, false);

        m_shouldFlush = shouldFlush;
    }

    /**
     * Initialize the serializer with the specified writer and output format.
     * Must be called before calling any of the serialize methods.
     *
     * @param writer The writer to use
     * @param format The output format
     * @param shouldFlush True if the writer should be flushed at EndDocument.
     */
    private synchronized void init(
        Writer writer,
        Properties format,
        boolean defaultProperties,
        boolean shouldFlush)
    {

        m_shouldFlush = shouldFlush;

        
        // if we are tracing events we need to trace what
        // characters are written to the output writer.
        if (m_tracer != null
         && !(writer instanceof SerializerTraceWriter)  )
            m_writer = new SerializerTraceWriter(writer, m_tracer);
        else
            m_writer = writer;        
        

        m_format = format;
        //        m_cdataSectionNames =
        //            OutputProperties.getQNameProperties(
        //                OutputKeys.CDATA_SECTION_ELEMENTS,
        //                format);
        setCdataSectionElements(OutputKeys.CDATA_SECTION_ELEMENTS, format);

        setIndentAmount(
            OutputPropertyUtils.getIntProperty(
                OutputPropertiesFactory.S_KEY_INDENT_AMOUNT,
                format));
        setIndent(
            OutputPropertyUtils.getBooleanProperty(OutputKeys.INDENT, format));

        boolean shouldNotWriteXMLHeader =
            OutputPropertyUtils.getBooleanProperty(
                OutputKeys.OMIT_XML_DECLARATION,
                format);
        setOmitXMLDeclaration(shouldNotWriteXMLHeader);
        setDoctypeSystem(format.getProperty(OutputKeys.DOCTYPE_SYSTEM));
        String doctypePublic = format.getProperty(OutputKeys.DOCTYPE_PUBLIC);
        setDoctypePublic(doctypePublic);

        // if standalone was explicitly specified
        if (format.get(OutputKeys.STANDALONE) != null)
        {
            String val = format.getProperty(OutputKeys.STANDALONE);
            if (defaultProperties)
                setStandaloneInternal(val);
            else
                setStandalone(val);
        }

        setMediaType(format.getProperty(OutputKeys.MEDIA_TYPE));

        if (null != doctypePublic)
        {
            if (doctypePublic.startsWith("-//W3C//DTD XHTML"))
                m_spaceBeforeClose = true;
        }

        // initCharsMap();
        String encoding = getEncoding();
        if (null == encoding)
        {
            encoding =
                Encodings.getMimeEncoding(
                    format.getProperty(OutputKeys.ENCODING));
            setEncoding(encoding);
        }

        m_isUTF8 = encoding.equals(Encodings.DEFAULT_MIME_ENCODING);
        m_maxCharacter = Encodings.getLastPrintable(encoding);

        // Access this only from the Hashtable level... we don't want to 
        // get default properties.
        String entitiesFileName =
            (String) format.get(OutputPropertiesFactory.S_KEY_ENTITIES);

        if (null != entitiesFileName)
        {
            m_charInfo = CharInfo.getCharInfo(entitiesFileName);
        }

    }

    /**
     * Initialize the serializer with the specified writer and output format.
     * Must be called before calling any of the serialize methods.
     *
     * @param writer The writer to use
     * @param format The output format
     */
    private synchronized void init(Writer writer, Properties format)
    {
        init(writer, format, false, false);
    }
    /**
     * Initialize the serializer with the specified output stream and output
     * format. Must be called before calling any of the serialize methods.
     *
     * @param output The output stream to use
     * @param format The output format
     * @param defaultProperties true if the properties are the default
     * properties
     * 
     * @throws UnsupportedEncodingException The encoding specified   in the
     * output format is not supported
     */
    protected synchronized void init(
        OutputStream output,
        Properties format,
        boolean defaultProperties)
        throws UnsupportedEncodingException
    {

        String encoding = getEncoding();
        if (encoding == null)
        {
            // if not already set then get it from the properties
            encoding =
                Encodings.getMimeEncoding(
                    format.getProperty(OutputKeys.ENCODING));
            setEncoding(encoding);
        }

        if (encoding.equalsIgnoreCase("UTF-8"))
        {
            m_isUTF8 = true;
            //            if (output instanceof java.io.BufferedOutputStream)
            //            {
            //                init(new WriterToUTF8(output), format, defaultProperties, true);
            //            }
            //            else if (output instanceof java.io.FileOutputStream)
            //            {
            //                init(new WriterToUTF8Buffered(output), format, defaultProperties, true);
            //            }
            //            else
            //            {
            //                // Not sure what to do in this case.  I'm going to be conservative 
            //                // and not buffer.
            //                init(new WriterToUTF8(output), format, defaultProperties, true);
            //            }
            if (output instanceof java.io.BufferedOutputStream)
            {
                init(new WriterToUTF8(output), format, defaultProperties, true);
            }
            else
            {
                init(
                    new WriterToUTF8Buffered(output),
                    format,
                    defaultProperties,
                    true);
            }

        }
        else if (
            encoding.equals("WINDOWS-1250")
                || encoding.equals("US-ASCII")
                || encoding.equals("ASCII"))
        {
            init(new WriterToASCI(output), format, defaultProperties, true);
        }
        else
        {
            Writer osw;

            try
            {
                osw = Encodings.getWriter(output, encoding);
            }
            catch (UnsupportedEncodingException uee)
            {
                System.out.println(
                    "Warning: encoding \""
                        + encoding
                        + "\" not supported"
                        + ", using "
                        + Encodings.DEFAULT_MIME_ENCODING);

                encoding = Encodings.DEFAULT_MIME_ENCODING;
                setEncoding(encoding);
                osw = Encodings.getWriter(output, encoding);
            }

            m_maxCharacter = Encodings.getLastPrintable(encoding);

            init(osw, format, defaultProperties, true);
        }

    }

    /**
     * Returns the output format for this serializer.
     *
     * @return The output format in use
     */
    public Properties getOutputFormat()
    {
        return m_format;
    }

    /**
     * Specifies a writer to which the document should be serialized.
     * This method should not be called while the serializer is in
     * the process of serializing a document.
     *
     * @param writer The output writer stream
     */
    public void setWriter(Writer writer)
    {        
        // if we are tracing events we need to trace what 
        // characters are written to the output writer.
        if (m_tracer != null
         && !(writer instanceof SerializerTraceWriter)  )
            m_writer = new SerializerTraceWriter(writer, m_tracer);
        else
            m_writer = writer;
    }

    /**
     * Specifies an output stream to which the document should be
     * serialized. This method should not be called while the
     * serializer is in the process of serializing a document.
     * <p>
     * The encoding specified in the output properties is used, or
     * if no encoding was specified, the default for the selected
     * output method.
     *
     * @param output The output stream
     */
    public void setOutputStream(OutputStream output)
    {

        try
        {
            Properties format;
            if (null == m_format)
                format =
                    OutputPropertiesFactory.getDefaultMethodProperties(
                        Method.XML);
            else
                format = m_format;
            init(output, format, true);
        }
        catch (UnsupportedEncodingException uee)
        {

            // Should have been warned in init, I guess...
        }
    }

    /**
     * @see org.apache.xml.serializer.SerializationHandler#setEscaping(boolean)
     */
    public boolean setEscaping(boolean escape)
    {
        final boolean temp = m_escaping;
        m_escaping = escape;
        return temp;

    }

    /**
     * Resets the serializer. If this method returns true, the
     * serializer may be used for subsequent serialization of new
     * documents. It is possible to change the output format and
     * output stream prior to serializing, or to use the existing
     * output format and output stream.
     *
     * @return True if serializer has been reset and can be reused
     */
    public boolean reset()
    {
        m_needToCallStartDocument = true;
        
        return false;
    }

    /**
     * Might print a newline character and the indentation amount
     * of the current element.
     *
     * @throws org.xml.sax.SAXException if an error occurs during writing.
     */
    protected void indent() throws IOException
    {

        if (m_startNewLine)
            outputLineSep();
        /* For m_indentAmount > 0 this extra test might be slower
         * but Xalan's default value is 0, so this extra test
         * will run faster in that situation.
         */
        if (m_indentAmount > 0)
            printSpace(m_currentElemDepth * m_indentAmount);

    }
    /**
     * Prints <var>n</var> spaces.
     * @param pw        The character output stream to use.
     * @param n         Number of spaces to print.
     *
     * @throws org.xml.sax.SAXException if an error occurs when writing.
     */
    private void printSpace(int n) throws IOException
    {

        for (int i = 0; i < n; i++)
        {
            m_writer.write(' ');
        }

    }

    /**
     * Report an attribute type declaration.
     *
     * <p>Only the effective (first) declaration for an attribute will
     * be reported.  The type will be one of the strings "CDATA",
     * "ID", "IDREF", "IDREFS", "NMTOKEN", "NMTOKENS", "ENTITY",
     * "ENTITIES", or "NOTATION", or a parenthesized token group with
     * the separator "|" and all whitespace removed.</p>
     *
     * @param eName The name of the associated element.
     * @param aName The name of the attribute.
     * @param type A string representing the attribute type.
     * @param valueDefault A string representing the attribute default
     *        ("#IMPLIED", "#REQUIRED", or "#FIXED") or null if
     *        none of these applies.
     * @param value A string representing the attribute's default value,
     *        or null if there is none.
     * @exception SAXException The application may raise an exception.
     */
    public void attributeDecl(
        String eName,
        String aName,
        String type,
        String valueDefault,
        String value)
        throws SAXException
    {
        // Do not inline external DTD
        if (m_inExternalDTD)
            return;
        try
        {
            if (m_inDoctype)
            {
                m_writer.write(" [");
                m_writer.write(m_lineSep, 0, m_lineSepLen);

                m_inDoctype = false;
            }

            m_writer.write("<!ATTLIST ");
            m_writer.write(eName);
            m_writer.write(" ");

            m_writer.write(aName);
            m_writer.write(" ");
            m_writer.write(type);
            if (valueDefault != null)
            {
                m_writer.write(" ");
                m_writer.write(valueDefault);
            }

            //m_writer.write(" ");
            //m_writer.write(value);
            m_writer.write(">");
            m_writer.write(m_lineSep, 0, m_lineSepLen);
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }
    }

    /**
     * Get the character stream where the events will be serialized to.
     *
     * @return Reference to the result Writer, or null.
     */
    public Writer getWriter()
    {
        return m_writer;
    }

    /**
     * Report a parsed external entity declaration.
     *
     * <p>Only the effective (first) declaration for each entity
     * will be reported.</p>
     *
     * @param name The name of the entity.  If it is a parameter
     *        entity, the name will begin with '%'.
     * @param publicId The declared public identifier of the entity, or
     *        null if none was declared.
     * @param systemId The declared system identifier of the entity.
     * @exception SAXException The application may raise an exception.
     * @see #internalEntityDecl
     * @see org.xml.sax.DTDHandler#unparsedEntityDecl
     */
    public void externalEntityDecl(
        String name,
        String publicId,
        String systemId)
        throws SAXException
    {
    }

    /**
     * Tell if this character can be written without escaping.
     */
    protected boolean escapingNotNeeded(char ch)
    {
        if (ch < 127)
        {
            if (ch >= 0x20 || (0x0A == ch || 0x0D == ch || 0x09 == ch))
                return true;
            else
                return false;
        }

        if (null == m_charToByteConverter && false == m_triedToGetConverter)
        {
            m_triedToGetConverter = true;
            try
            {
                m_charToByteConverter =
                    Encodings.getCharToByteConverter(getEncoding());
                if (null != m_charToByteConverter)
                {
                    Class argsTypes[] = new Class[1];
                    argsTypes[0] = Character.TYPE;
                    Class convClass = m_charToByteConverter.getClass();
                    m_canConvertMeth =
                        convClass.getMethod("canConvert", argsTypes);
                }
            }
            catch (Exception e)
            {
                // This is just an assert: no action at the moment.
                System.err.println("Warning: " + e.getMessage());
            }
        }
        if (null != m_charToByteConverter)
        {
            try
            {
                Object args[] = new Object[1];
                args[0] = new Character(ch);
                Boolean bool =
                    (Boolean) m_canConvertMeth.invoke(
                        m_charToByteConverter,
                        args);
                return bool.booleanValue()
                    ? !Character.isISOControl(ch)
                    : false;
            }
            catch (java.lang.reflect.InvocationTargetException ite)
            {
                // This is just an assert: no action at the moment.
                System.err.println(
                    "Warning: InvocationTargetException in canConvert!");
            }
            catch (java.lang.IllegalAccessException iae)
            {
                // This is just an assert: no action at the moment.
                System.err.println(
                    "Warning: IllegalAccessException in canConvert!");
            }
        }
        // fallback!
        return (ch <= m_maxCharacter);
    }

    /**
     * Once a surrogate has been detected, write the pair as a single
     * character reference.
     *
     * @param c the first part of the surrogate.
     * @param ch Character array.
     * @param i position Where the surrogate was detected.
     * @param end The end index of the significant characters.
     * @return i+1.
     * @throws IOException
     * @throws org.xml.sax.SAXException if invalid UTF-16 surrogate detected.
     */
    protected int writeUTF16Surrogate(char c, char ch[], int i, int end)
        throws IOException
    {

        // UTF-16 surrogate
        int surrogateValue = getURF16SurrogateValue(c, ch, i, end);

        i++;

        m_writer.write('&');
        m_writer.write('#');

        // m_writer.write('x');
        m_writer.write(Integer.toString(surrogateValue));
        m_writer.write(';');

        return i;
    }

    /**
     * Once a surrogate has been detected, get the pair as a single integer
     * value.
     *
     * @param c the first part of the surrogate.
     * @param ch Character array.
     * @param i position Where the surrogate was detected.
     * @param end The end index of the significant characters.
     * @return i+1.
     * @throws org.xml.sax.SAXException if invalid UTF-16 surrogate detected.
     */
    int getURF16SurrogateValue(char c, char ch[], int i, int end)
        throws IOException
    {

        int next;

        if (i + 1 >= end)
        {
            throw new IOException(
                XMLMessages.createXMLMessage(
                    XMLErrorResources.ER_INVALID_UTF16_SURROGATE,
                    new Object[] { Integer.toHexString((int) c)}));
            //"Invalid UTF-16 surrogate detected: "

            //+Integer.toHexString((int)c)+ " ?");
        }
        else
        {
            next = ch[++i];

            if (!(0xdc00 <= next && next < 0xe000))
                throw new IOException(
                    XMLMessages.createXMLMessage(
                        XMLErrorResources.ER_INVALID_UTF16_SURROGATE,
                        new Object[] {
                            Integer.toHexString((int) c)
                                + " "
                                + Integer.toHexString(next)}));
            //"Invalid UTF-16 surrogate detected: "

            //+Integer.toHexString((int)c)+" "+Integer.toHexString(next));
            next = ((c - 0xd800) << 10) + next - 0xdc00 + 0x00010000;
        }

        return next;
    }

    /**
     * Handle one of the default entities, return false if it
     * is not a default entity.
     *
     * @param ch character to be escaped.
     * @param i index into character array.
     * @param chars non-null reference to character array.
     * @param len length of chars.
     * @param escLF true if the linefeed should be escaped.
     *
     * @return i+1 if the character was written, else i.
     *
     * @throws java.io.IOException
     */
    protected int accumDefaultEntity(
        java.io.Writer writer,
        char ch,
        int i,
        char[] chars,
        int len,
        boolean escLF)
        throws IOException
    {

        if (!escLF && CharInfo.S_LINEFEED == ch)
        {
            writer.write(m_lineSep, 0, m_lineSepLen);
        }
        else
        {
            if (m_charInfo.isSpecial(ch))
            {
                String entityRef = m_charInfo.getEntityNameForChar(ch);

                if (null != entityRef)
                {
                    writer.write('&');
                    writer.write(entityRef);
                    writer.write(';');
                }
                else
                    return i;
            }
            else
                return i;
        }

        return i + 1;

    }
    /**
     * Normalize the characters, but don't escape.
     *
     * @param ch The characters from the XML document.
     * @param start The start position in the array.
     * @param length The number of characters to read from the array.
     * @param isCData true if a CDATA block should be built around the characters.
     *
     * @throws IOException
     * @throws org.xml.sax.SAXException
     */
    void writeNormalizedChars(
        char ch[],
        int start,
        int length,
        boolean isCData)
        throws IOException, org.xml.sax.SAXException
    {

        int end = start + length;

        for (int i = start; i < end; i++)
        {
            char c = ch[i];

            if (CharInfo.S_LINEFEED == c)
            {
                m_writer.write(m_lineSep, 0, m_lineSepLen);
            }
            else if (isCData && (!escapingNotNeeded(c)))
            {
                //                if (i != 0)
                if (m_cdataTagOpen)
                    closeCDATA();

                // This needs to go into a function... 
                if (isUTF16Surrogate(c))
                {
                    i = writeUTF16Surrogate(c, ch, i, end);
                }
                else
                {
                    m_writer.write("&#");

                    String intStr = Integer.toString((int) c);

                    m_writer.write(intStr);
                    m_writer.write(';');
                }

                //                if ((i != 0) && (i < (end - 1)))
                //                if (!m_cdataTagOpen && (i < (end - 1)))
                //                {
                //                    m_writer.write(CDATA_DELIMITER_OPEN);
                //                    m_cdataTagOpen = true;
                //                }
            }
            else if (
                isCData
                    && ((i < (end - 2))
                        && (']' == c)
                        && (']' == ch[i + 1])
                        && ('>' == ch[i + 2])))
            {
                m_writer.write(CDATA_CONTINUE);

                i += 2;
            }
            else
            {
                if (escapingNotNeeded(c))
                {
                    if (isCData && !m_cdataTagOpen)
                    {
                        m_writer.write(CDATA_DELIMITER_OPEN);
                        m_cdataTagOpen = true;
                    }
                    m_writer.write(c);
                }

                // This needs to go into a function... 
                else if (isUTF16Surrogate(c))
                {
                    if (m_cdataTagOpen)
                        closeCDATA();
                    i = writeUTF16Surrogate(c, ch, i, end);
                }
                else
                {
                    if (m_cdataTagOpen)
                        closeCDATA();
                    m_writer.write("&#");

                    String intStr = Integer.toString((int) c);

                    m_writer.write(intStr);
                    m_writer.write(';');
                }
            }
        }

    }

    /**
     * Ends an un-escaping section.
     *
     * @see #startNonEscaping
     *
     * @throws org.xml.sax.SAXException
     */
    public void endNonEscaping() throws org.xml.sax.SAXException
    {
        m_disableOutputEscapingStates.pop();
    }

    /**
     * Starts an un-escaping section. All characters printed within an un-
     * escaping section are printed as is, without escaping special characters
     * into entity references. Only XML and HTML serializers need to support
     * this method.
     * <p> The contents of the un-escaping section will be delivered through the
     * regular <tt>characters</tt> event.
     *
     * @throws org.xml.sax.SAXException
     */
    public void startNonEscaping() throws org.xml.sax.SAXException
    {
        m_disableOutputEscapingStates.push(true);
    }

    /**
     * Receive notification of cdata.
     *
     * <p>The Parser will call this method to report each chunk of
     * character data.  SAX parsers may return all contiguous character
     * data in a single chunk, or they may split it into several
     * chunks; however, all of the characters in any single event
     * must come from the same external entity, so that the Locator
     * provides useful information.</p>
     *
     * <p>The application must not attempt to read from the array
     * outside of the specified range.</p>
     *
     * <p>Note that some parsers will report whitespace using the
     * ignorableWhitespace() method rather than this one (validating
     * parsers must do so).</p>
     *
     * @param ch The characters from the XML document.
     * @param start The start position in the array.
     * @param length The number of characters to read from the array.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see #ignorableWhitespace
     * @see org.xml.sax.Locator
     *
     * @throws org.xml.sax.SAXException
     */
    protected void cdata(char ch[], int start, final int length)
        throws org.xml.sax.SAXException
    {

        try
        {
            final int old_start = start;
            if (m_startTagOpen)
            {
                closeStartTag();
                m_startTagOpen = false;
            }
            m_ispreserve = true;

            if (shouldIndent())
                indent();

            boolean writeCDataBrackets =
                (((length >= 1) && escapingNotNeeded(ch[start])));

            /* Write out the CDATA opening delimiter only if
             * we are supposed to, and if we are not already in
             * the middle of a CDATA section  
             */
            if (writeCDataBrackets && !m_cdataTagOpen)
            {
                m_writer.write(CDATA_DELIMITER_OPEN);
                m_cdataTagOpen = true;
            }

            // m_writer.write(ch, start, length);
            if (isEscapingDisabled())
            {
                charactersRaw(ch, start, length);
            }
            else
                writeNormalizedChars(ch, start, length, true);

            /* used to always write out CDATA closing delimiter here,
             * but now we delay, so that we can merge CDATA sections on output.    
             * need to write closing delimiter later
             */
            if (writeCDataBrackets)
            {
                /* if the CDATA section ends with ] don't leave it open
                 * as there is a chance that an adjacent CDATA sections
                 * starts with ]>.  
                 * We don't want to merge ]] with > , or ] with ]> 
                 */
                if (ch[start + length - 1] == ']')
                    closeCDATA();
            }

            // time to fire off CDATA event
            super.fireCDATAEvent(ch, old_start, length);
        }
        catch (IOException ioe)
        {
            throw new org.xml.sax.SAXException(
                XMLMessages.createXMLMessage(
                    XMLErrorResources.ER_OIERROR,
                    null),
                ioe);
            //"IO error", ioe);
        }
    }

    /**
     * Tell if the character escaping should be disabled for the current state.
     *
     * @return true if the character escaping should be disabled.
     */
    private boolean isEscapingDisabled()
    {
        return m_disableOutputEscapingStates.peekOrFalse();
    }

    /**
     * If available, when the disable-output-escaping attribute is used,
     * output raw text without escaping.
     *
     * @param ch The characters from the XML document.
     * @param start The start position in the array.
     * @param length The number of characters to read from the array.
     *
     * @throws org.xml.sax.SAXException
     */
    protected void charactersRaw(char ch[], int start, int length)
        throws org.xml.sax.SAXException
    {

        if (m_inEntityRef)
            return;
        try
        {
            if (m_startTagOpen)
            {
                closeStartTag();
                m_startTagOpen = false;
            }

            m_ispreserve = true;

            m_writer.write(ch, start, length);
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }

    }

    /**
     * Receive notification of character data.
     *
     * <p>The Parser will call this method to report each chunk of
     * character data.  SAX parsers may return all contiguous character
     * data in a single chunk, or they may split it into several
     * chunks; however, all of the characters in any single event
     * must come from the same external entity, so that the Locator
     * provides useful information.</p>
     *
     * <p>The application must not attempt to read from the array
     * outside of the specified range.</p>
     *
     * <p>Note that some parsers will report whitespace using the
     * ignorableWhitespace() method rather than this one (validating
     * parsers must do so).</p>
     *
     * @param chars The characters from the XML document.
     * @param start The start position in the array.
     * @param length The number of characters to read from the array.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see #ignorableWhitespace
     * @see org.xml.sax.Locator
     *
     * @throws org.xml.sax.SAXException
     */
    public void characters(char chars[], int start, int length)
        throws org.xml.sax.SAXException
    {
        if (0 == length)
        {
            // Even though the character string is empty, but it is still a character event
            // time to fire off characters generation event
            super.fireCharEvent(chars, start, length);
            return;
        }

        if (m_startTagOpen)
        {
            closeStartTag();
            m_startTagOpen = false;
        }
        else if (m_needToCallStartDocument)
        {
            startDocumentInternal();
        }

        if (m_cdataStartCalled || m_cdataSectionStates.peekOrFalse())
        {
            /* either due to startCDATA() being called or due to 
             * cdata-section-elements atribute, we need this as cdata
             */
            cdata(chars, start, length);

            return;
        }

        if (m_cdataTagOpen)
            closeCDATA();
        // the check with _escaping is a bit of a hack for XLSTC

        if (m_disableOutputEscapingStates.peekOrFalse() || (!m_escaping))
        {
            charactersRaw(chars, start, length);

            // time to fire off characters generation event
            super.fireCharEvent(chars, start, length);

            return;
        }

        if (m_startTagOpen)
            closeStartTag();

        int startClean = start;
        int lengthClean = 0;

        // int pos = 0;
        int end = start + length;
        boolean checkWhite = true;
        final int maxCharacter = m_maxCharacter;
        final BitSet specialsMap = m_charInfo.m_specialsMap;
        try
        {
            for (int i = start; i < end; i++)
            {
                char ch = chars[i];

                if (checkWhite
                    && ((ch > 0x20)
                        || !((ch == 0x20)
                            || (ch == 0x09)
                            || (ch == 0xD)
                            || (ch == 0xA))))
                {
                    m_ispreserve = true;
                    checkWhite = false;
                }

                // The first if(...) has the most common part of escapingNotNeeded()
                // inlined to save the call.  If the expression is false it will
                // fall back to the next else if(...) which does the real thing
                // with esacapingNotNeeded()
                if ((((ch < 127)
                    && (0x20 <= ch || (0x0A == ch || 0x0D == ch || 0x09 == ch)))
                    && (!specialsMap.get(ch)))
                    || ('"' == ch))
                {
                    lengthClean++;
                }
                else if (
                    (escapingNotNeeded(ch) && (!specialsMap.get(ch)))
                        || ('"' == ch))
                {
                    lengthClean++;
                }
                else
                {
                    if (lengthClean > 0)
                    {
                        m_writer.write(chars, startClean, lengthClean);

                        lengthClean = 0;
                    }

                    if (CharInfo.S_LINEFEED == ch)
                    {
                        m_writer.write(m_lineSep, 0, m_lineSepLen);

                        startClean = i + 1;
                    }
                    else
                    {
                        startClean =
                            accumDefaultEscape(
                                m_writer,
                                ch,
                                i,
                                chars,
                                end,
                                false);
                        i = startClean - 1;
                    }
                }
            }

            if (lengthClean > 0)
            {
                m_writer.write(chars, startClean, lengthClean);
            }

            m_isprevtext = true;
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }

        // time to fire off characters generation event
        super.fireCharEvent(chars, start, length);
    }

    /**
     * Receive notification of character data.
     *
     * @param s The string of characters to process.
     *
     * @throws org.xml.sax.SAXException
     */
    public void characters(String s) throws org.xml.sax.SAXException
    {
        characters(s.toCharArray(), 0, s.length());
    }

    /**
     * Escape and m_writer.write a character.
     *
     * @param ch character to be escaped.
     * @param i index into character array.
     * @param chars non-null reference to character array.
     * @param len length of chars.
     * @param escLF true if the linefeed should be escaped.
     *
     * @return i+1 if the character was written, else i.
     *
     * @throws org.xml.sax.SAXException
     */
    protected int accumDefaultEscape(
        Writer writer,
        char ch,
        int i,
        char[] chars,
        int len,
        boolean escLF)
        throws IOException
    {

        int pos = accumDefaultEntity(writer, ch, i, chars, len, escLF);

        if (i == pos)
        {
            pos++;

            if (0xd800 <= ch && ch < 0xdc00)
            {

                // UTF-16 surrogate
                int next;

                if (i + 1 >= len)
                {
                    throw new IOException(
                        XMLMessages.createXMLMessage(
                            XMLErrorResources.ER_INVALID_UTF16_SURROGATE,
                            new Object[] { Integer.toHexString(ch)}));
                    //"Invalid UTF-16 surrogate detected: "

                    //+Integer.toHexString(ch)+ " ?");
                }
                else
                {
                    next = chars[++i];

                    if (!(0xdc00 <= next && next < 0xe000))
                        throw new IOException(
                            XMLMessages.createXMLMessage(
                                XMLErrorResources
                                    .ER_INVALID_UTF16_SURROGATE,
                                new Object[] {
                                    Integer.toHexString(ch)
                                        + " "
                                        + Integer.toHexString(next)}));
                    //"Invalid UTF-16 surrogate detected: "

                    //+Integer.toHexString(ch)+" "+Integer.toHexString(next));
                    next = ((ch - 0xd800) << 10) + next - 0xdc00 + 0x00010000;
                }

                writer.write("&#");
                writer.write(Integer.toString(next));
                writer.write(";");

                /*} else if (null != ctbc && !ctbc.canConvert(ch)) {
                sb.append("&#x");
                sb.append(Integer.toString((int)ch, 16));
                sb.append(";");*/
            }
            else
            {
                if (!escapingNotNeeded(ch) || (m_charInfo.isSpecial(ch)))
                {
                    writer.write("&#");
                    writer.write(Integer.toString(ch));
                    writer.write(";");
                }
                else
                {
                    writer.write(ch);
                }
            }

        }
        return pos;
    }

    /**
     * Receive notification of the beginning of an element, although this is a
     * SAX method additional namespace or attribute information can occur before
     * or after this call, that is associated with this element.
     *
     *
     * @param namespaceURI The Namespace URI, or the empty string if the
     *        element has no Namespace URI or if Namespace
     *        processing is not being performed.
     * @param localName The local name (without prefix), or the
     *        empty string if Namespace processing is not being
     *        performed.
     * @param name The element type name.
     * @param atts The attributes attached to the element, if any.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.ContentHandler#startElement
     * @see org.xml.sax.ContentHandler#endElement
     * @see org.xml.sax.AttributeList
     *
     * @throws org.xml.sax.SAXException
     */
    public void startElement(
        String namespaceURI,
        String localName,
        String name,
        Attributes atts)
        throws org.xml.sax.SAXException
    {
        if (m_inEntityRef)
            return;

        if (m_needToCallStartDocument)
        {
            startDocumentInternal();
            m_needToCallStartDocument = false;
        }
        else if (m_cdataTagOpen)
            closeCDATA();
        try
        {
            if ((true == m_needToOutputDocTypeDecl)
                && (null != getDoctypeSystem()))
            {
                outputDocTypeDecl(name, true);
            }

            m_needToOutputDocTypeDecl = false;

            /* before we over-write the current elementLocalName etc.
             * lets close out the old one (if we still need to)
             */
            if (m_startTagOpen)
            {
                closeStartTag();
                m_startTagOpen = false;
            }

            if (namespaceURI != null)
                ensurePrefixIsDeclared(namespaceURI, name);

            // remember for later
            m_elementLocalName = localName;
            m_elementURI = namespaceURI;
            m_elementName = name;

            m_ispreserve = false;

            if (shouldIndent() && m_startNewLine)
            {
                indent();
            }

            m_startNewLine = true;

            m_writer.write('<');
            m_writer.write(name);
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }

        // process the attributes now, because after this SAX call they might be gone
        if (atts != null)
            addAttributes(atts);

        // mark that the closing '>' of the starting tag is not yet written out
        m_startTagOpen = true;
        m_currentElemDepth++; // current element is one element deeper
        m_isprevtext = false;
    }

    /**
      * Receive notification of the beginning of an element, additional
      * namespace or attribute information can occur before or after this call,
      * that is associated with this element.
      *
      *
      * @param namespaceURI The Namespace URI, or the empty string if the
      *        element has no Namespace URI or if Namespace
      *        processing is not being performed.
      * @param localName The local name (without prefix), or the
      *        empty string if Namespace processing is not being
      *        performed.
      * @param name The element type name.
      * @throws org.xml.sax.SAXException Any SAX exception, possibly
      *            wrapping another exception.
      * @see org.xml.sax.ContentHandler#startElement
      * @see org.xml.sax.ContentHandler#endElement
      * @see org.xml.sax.AttributeList
      *
      * @throws org.xml.sax.SAXException
      */
    public void startElement(
        String elementNamespaceURI,
        String elementLocalName,
        String elementName)
        throws SAXException
    {
        startElement(elementNamespaceURI, elementLocalName, elementName, null);
    }

    public void startElement(String elementName) throws SAXException
    {
        startElement(null, null, elementName, null);
    }

    /**
     * Output the doc type declaration.
     *
     * @param name non-null reference to document type name.
     * NEEDSDOC @param closeDecl
     *
     * @throws java.io.IOException
     */
    void outputDocTypeDecl(String name, boolean closeDecl) throws SAXException
    {
        if (m_cdataTagOpen)
            closeCDATA();
        try
        {
            m_writer.write("<!DOCTYPE ");
            m_writer.write(name);

            String doctypePublic = getDoctypePublic();
            if (null != doctypePublic)
            {
                m_writer.write(" PUBLIC \"");
                m_writer.write(doctypePublic);
                m_writer.write('\"');
            }

            String doctypeSystem = getDoctypeSystem();
            if (null != doctypeSystem)
            {
                if (null == doctypePublic)
                    m_writer.write(" SYSTEM \"");
                else
                    m_writer.write(" \"");

                m_writer.write(doctypeSystem);

                if (closeDecl)
                {
                    m_writer.write("\">");
                    m_writer.write(m_lineSep, 0, m_lineSepLen);
                    closeDecl = false; // done closing
                }
                else
                    m_writer.write('\"');
            }
            boolean dothis = false;
            if (dothis)
            {
                // at one point this code seemed right,
                // but not anymore - bjm
                if (closeDecl)
                {
                    m_writer.write(">");
                    m_writer.write(m_lineSep, 0, m_lineSepLen);
                }
            }
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }
    }

    /**
     * Process the colleced attributes from SAX- like calls for an element from
     * calls to addattibute(String name, String value)
     * 
     *
     * @throws org.xml.sax.SAXException
     */
    public void processAttributes() throws IOException, SAXException
    {

        // finish processing attributes, time to fire off the start element event
        super.fireStartElem(m_elementName);

        int nAttrs = 0;
        // if passed real SAX attributes, then process only them

        if ((nAttrs = m_attributes.getLength()) > 0)
        {
            /* real SAX attributes are not passed in, so process the 
             * attributes that were collected after the startElement call.
             * _attribVector is a "cheap" list for Stream serializer output
             * accumulated over a series of calls to attribute(name,value)
             */

            String encoding = getEncoding();
            for (int i = 0; i < nAttrs; i++)
            {
                // elementAt is JDK 1.1.8
                final String name = m_attributes.getQName(i);
                final String value = m_attributes.getValue(i);
                m_writer.write(' ');
                m_writer.write(name);
                m_writer.write("=\"");
                writeAttrString(m_writer, value, encoding);
                m_writer.write('\"');
            }

            /* The attributes are now processed so clear them out
             * so that they don't accumulate from element to element.
             * .removeAllElements() is used as it is from JDK 1.1.8
             */
            m_attributes.clear();

        }
    }

    /**
     * Returns the specified <var>string</var> after substituting <VAR>specials</VAR>,
     * and UTF-16 surrogates for chracter references <CODE>&amp;#xnn</CODE>.
     *
     * @param   string      String to convert to XML format.
     * @param   encoding    CURRENTLY NOT IMPLEMENTED.
     *
     * @throws java.io.IOException
     */
    public void writeAttrString(
        Writer writer,
        String string,
        String encoding)
        throws IOException
    {

        final char[] stringChars = string.toCharArray();
        final int len = stringChars.length;
        for (int i = 0; i < len; i++)
        {
            char ch = stringChars[i];
            if (escapingNotNeeded(ch) && (!m_charInfo.isSpecial(ch)))
            {
                writer.write(ch);
            }
            else
            { // I guess the parser doesn't normalize cr/lf in attributes. -sb
                if ((CharInfo.S_CARRIAGERETURN == ch)
                    && ((i + 1) < len)
                    && (CharInfo.S_LINEFEED == stringChars[i + 1]))
                {
                    i++;
                    ch = CharInfo.S_LINEFEED;
                }

                accumDefaultEscape(writer, ch, i, stringChars, len, true);
            }
        }

    }

    /**
     * Receive notification of the end of an element.
     *
     *
     * @param namespaceURI The Namespace URI, or the empty string if the
     *        element has no Namespace URI or if Namespace
     *        processing is not being performed.
     * @param localName The local name (without prefix), or the
     *        empty string if Namespace processing is not being
     *        performed.
     * @param name The element type name
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     *
     * @throws org.xml.sax.SAXException
     */
    public void endElement(String namespaceURI, String localName, String name)
        throws org.xml.sax.SAXException
    {
        if (m_inEntityRef)
            return;

        // namespaces declared at the current depth are no longer valid
        // so get rid of them    
        m_prefixMap.popNamespaces(m_currentElemDepth);

        // this element is done, so the new current element is one element less deep    
        m_currentElemDepth--;

        try
        {
            if (m_startTagOpen)
            {
                /* The start tag is still open and we have hit
                 * endElement, so close it down
                 */
                processAttributes();
                if (m_spaceBeforeClose)
                    m_writer.write(" />");
                else
                    m_writer.write("/>");
                /* don't need to pop cdataSectionState because
                 * this element ended so quickly that we didn't get
                 * to push the state.
                 */

            }
            else
            {
                if (m_cdataTagOpen)
                    closeCDATA();

                if (shouldIndent())
                    indent();
                m_writer.write('<');
                m_writer.write('/');
                m_writer.write(name);
                m_writer.write('>');
                m_cdataSectionStates.pop();
            }
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }

        if (!m_startTagOpen)
        {
            m_ispreserve = m_preserves.isEmpty() ? false : m_preserves.pop();
        }

        m_isprevtext = false;
        m_startTagOpen = false;

        // m_disableOutputEscapingStates.pop();

        // fire off the end element event
        super.fireEndElem(name);
    }

    /**
     * Receive notification of the end of an element.
     * @param name The element type name
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *     wrapping another exception.
     */
    public void endElement(String name) throws org.xml.sax.SAXException
    {
        endElement(null, null, name);
    }

    /**
     * Begin the scope of a prefix-URI Namespace mapping
     * just before another element is about to start.
     * This call will close any open tags so that the prefix mapping
     * will not apply to the current element, but the up comming child.
     * 
     * @see org.xml.sax.ContentHandler#startPrefixMapping
     * 
     * @param prefix The Namespace prefix being declared.
     * @param uri The Namespace URI the prefix is mapped to.
     * 
     * @throws org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * 
     */
    public void startPrefixMapping(String prefix, String uri)
        throws org.xml.sax.SAXException
    {
        // the "true" causes the flush of any open tags
        startPrefixMapping(prefix, uri, true);
    }

    /**
     * Handle a prefix/uri mapping, which is associated with a startElement()
     * that is soon to follow. Need to close any open start tag to make
     * sure than any name space attributes due to this event are associated wih
     * the up comming element, not the current one.
     * @see org.xml.sax.ExtendedContentHandler#startPrefixMapping
     *
     * @param prefix The Namespace prefix being declared.
     * @param uri The Namespace URI the prefix is mapped to.
     * @param shouldFlush true if any open tags need to be closed first, this
     * will impact which element the mapping applies to (open parent, or its up
     * comming child)
     * @return returns true if the call made a change to the current 
     * namespace information, false if it did not change anything, e.g. if the
     * prefix/namespace mapping was already in scope from before.
     * 
     * @throws org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     *
     *
     */
    public boolean startPrefixMapping(
        String prefix,
        String uri,
        boolean shouldFlush)
        throws org.xml.sax.SAXException
    {

        /* Remember the mapping, and at what depth it was declared
         * This is one greater than the current depth because these
         * mappings will apply to the next depth. This is in
         * consideration that startElement() will soon be called
         */

        boolean pushed;
        int pushDepth;
        if (shouldFlush)
        {
            flushPending();
            // the prefix mapping applies to the child element (one deeper)
            pushDepth = m_currentElemDepth + 1;
        }
        else
        {
            // the prefix mapping applies to the current element
            pushDepth = m_currentElemDepth;
        }
        pushed = m_prefixMap.pushNamespace(prefix, uri, pushDepth);

        if (pushed)
        {
            /* bjm: don't know if we really needto do this. The
             * callers of this object should have injected both
             * startPrefixMapping and the attributes.  We are 
             * just covering our butt here.
             */
            String name;
            if (EMPTYSTRING.equals(prefix))
            {
                name = "xmlns";
                addAttributeAlways(XMLNS_URI, prefix, name, "CDATA", uri);
            }
            else
            {
                if (!EMPTYSTRING.equals(uri))
                    // hack for XSLTC attribset16 test
                { // that maps ns1 prefix to "" URI
                    name = "xmlns:" + prefix;

                    /* for something like xmlns:abc="w3.pretend.org"
                     *  the      uri is the value, that is why we pass it in the
                     * value, or 5th slot of addAttributeAlways()
                     */
                    addAttributeAlways(XMLNS_URI, prefix, name, "CDATA", uri);
                }
            }
        }
        return pushed;
    }

    /**
     * Receive notification of an XML comment anywhere in the document. This
     * callback will be used for comments inside or outside the document
     * element, including comments in the external DTD subset (if read).
     * @param ch An array holding the characters in the comment.
     * @param start The starting position in the array.
     * @param length The number of characters to use from the array.
     * @throws org.xml.sax.SAXException The application may raise an exception.
     */
    public void comment(char ch[], int start, int length)
        throws org.xml.sax.SAXException
    {

        int start_old = start;
        if (m_inEntityRef)
            return;
        if (m_startTagOpen)
        {
            closeStartTag();
            m_startTagOpen = false;
        }
        else if (m_needToCallStartDocument)
        {
            startDocumentInternal();
            m_needToCallStartDocument = false;
        }

        try
        {
            if (shouldIndent())
                indent();

            final int limit = start + length;
            boolean wasDash = false;
            if (m_cdataTagOpen)
                closeCDATA();
            m_writer.write(COMMENT_BEGIN);
            // Detect occurrences of two consecutive dashes, handle as necessary.
            for (int i = start; i < limit; i++)
            {
                if (wasDash && ch[i] == '-')
                {
                    m_writer.write(ch, start, i - start);
                    m_writer.write(" -");
                    start = i + 1;
                }
                wasDash = (ch[i] == '-');
            }

            // if we have some chars in the comment
            if (length > 0)
            {
                // Output the remaining characters (if any)
                final int remainingChars = (limit - start);
                if (remainingChars > 0)
                    m_writer.write(ch, start, remainingChars);
                // Protect comment end from a single trailing dash
                if (ch[limit - 1] == '-')
                    m_writer.write(' ');
            }
            m_writer.write(COMMENT_END);
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }

        m_startNewLine = true;
        // time to generate comment event
        super.fireCommentEvent(ch, start_old,length);
    }

    /**
     * Report the end of a CDATA section.
     * @throws org.xml.sax.SAXException The application may raise an exception.
     *
     *  @see  #startCDATA
     */
    public void endCDATA() throws org.xml.sax.SAXException
    {
        if (m_cdataTagOpen)
            closeCDATA();
        m_cdataStartCalled = false;
    }

    /**
     * Report the end of DTD declarations.
     * @throws org.xml.sax.SAXException The application may raise an exception.
     * @see #startDTD
     */
    public void endDTD() throws org.xml.sax.SAXException
    {
        try
        {
            if (m_needToOutputDocTypeDecl)
            {
                outputDocTypeDecl(m_elementName, false);
                m_needToOutputDocTypeDecl = false;
            }
            if (!m_inDoctype)
                m_writer.write("]>");
            else
            {
                m_writer.write('>');
            }

            m_writer.write(m_lineSep, 0, m_lineSepLen);
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }

    }

    /**
     * End the scope of a prefix-URI Namespace mapping.
     * @see org.xml.sax.ContentHandler#endPrefixMapping
     * 
     * @param prefix The prefix that was being mapping.
     * @throws org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     */
    public void endPrefixMapping(String prefix) throws org.xml.sax.SAXException
    { // do nothing
    }

    /**
     * Receive notification of ignorable whitespace in element content.
     * 
     * Not sure how to get this invoked quite yet.
     * 
     * @param ch The characters from the XML document.
     * @param start The start position in the array.
     * @param length The number of characters to read from the array.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see #characters
     * 
     * @throws org.xml.sax.SAXException
     */
    public void ignorableWhitespace(char ch[], int start, int length)
        throws org.xml.sax.SAXException
    {

        if (0 == length)
            return;
        characters(ch, start, length);
    }

    /**
     * Receive notification of a skipped entity.
     * @see org.xml.sax.ContentHandler#skippedEntity
     * 
     * @param name The name of the skipped entity.  If it is a
     *       parameter                   entity, the name will begin with '%',
     * and if it is the external DTD subset, it will be the string
     * "[dtd]".
     * @throws org.xml.sax.SAXException Any SAX exception, possibly wrapping
     * another exception.
     */
    public void skippedEntity(String name) throws org.xml.sax.SAXException
    { // TODO: Should handle
    }

    /**
     * Report the start of a CDATA section.
     * 
     * @throws org.xml.sax.SAXException The application may raise an exception.
     * @see #endCDATA
     */
    public void startCDATA() throws org.xml.sax.SAXException
    {
        m_cdataStartCalled = true;
    }

    /**
     * Report the beginning of an entity.
     * 
     * The start and end of the document entity are not reported.
     * The start and end of the external DTD subset are reported
     * using the pseudo-name "[dtd]".  All other events must be
     * properly nested within start/end entity events.
     * 
     * @param name The name of the entity.  If it is a parameter
     *        entity, the name will begin with '%'.
     * @throws org.xml.sax.SAXException The application may raise an exception.
     * @see #endEntity
     * @see org.xml.sax.ext.DeclHandler#internalEntityDecl
     * @see org.xml.sax.ext.DeclHandler#externalEntityDecl
     */
    public void startEntity(String name) throws org.xml.sax.SAXException
    {
        if (name.equals("[dtd]"))
            m_inExternalDTD = true;
        m_inEntityRef = true;
    }

    /**
     * For the enclosing elements starting tag write out
     * out any attributes followed by ">"
     *
     * @throws org.xml.sax.SAXException
     */
    protected void closeStartTag() throws SAXException
    {

        if (m_startTagOpen)
        {

            try
            {
                processAttributes();
                m_writer.write('>');
            }
            catch (IOException e)
            {
                throw new SAXException(e);
            }

            /* whether Xalan or XSLTC, we have the prefix mappings now, so
             * lets determine if the current element is specified in the cdata-
             * section-elements list.
             */
            pushCdataSectionState();

            m_isprevtext = false;
            m_preserves.push(m_ispreserve);
            m_startTagOpen = false;

        }

    }

    /**
     * Report the start of DTD declarations, if any.
     *
     * Any declarations are assumed to be in the internal subset unless
     * otherwise indicated.
     * 
     * @param name The document type name.
     * @param publicId The declared public identifier for the
     *        external DTD subset, or null if none was declared.
     * @param systemId The declared system identifier for the
     *        external DTD subset, or null if none was declared.
     * @throws org.xml.sax.SAXException The application may raise an
     *            exception.
     * @see #endDTD
     * @see #startEntity
     */
    public void startDTD(String name, String publicId, String systemId)
        throws org.xml.sax.SAXException
    {
        setDoctypeSystem(systemId);
        setDoctypePublic(publicId);

        m_elementName = name;
        m_inDoctype = true;
    }

    /**
     * Returns the m_indentAmount.
     * @return int
     */
    public int getIndentAmount()
    {
        return m_indentAmount;
    }

    /**
     * Sets the m_indentAmount.
     * 
     * @param m_indentAmount The m_indentAmount to set
     */
    public void setIndentAmount(int m_indentAmount)
    {
        this.m_indentAmount = m_indentAmount;
    }

    /**
     * Tell if, based on space preservation constraints and the doIndent property,
     * if an indent should occur.
     *
     * @return True if an indent should occur.
     */
    protected boolean shouldIndent()
    {
        return m_doIndent && (!m_ispreserve && !m_isprevtext);
    }

    /**
     * Searches for the list of qname properties with the specified key in the
     * property list. If the key is not found in this property list, the default
     * property list, and its defaults, recursively, are then checked. The
     * method returns <code>null</code> if the property is not found.
     *
     * @param   key   the property key.
     * @param props the list of properties to search in.
     * 
     * Sets the vector of local-name/URI pairs of the cdata section elements
     * specified in the cdata-section-elements property.
     * 
     * This method is essentially a copy of getQNameProperties() from
     * OutputProperties. Eventually this method should go away and a call
     * to setCdataSectionElements(Vector v) should be made directly.
     */
    private void setCdataSectionElements(String key, Properties props)
    {

        String s = props.getProperty(key);

        if (null != s)
        {
            // Vector of URI/LocalName pairs
            Vector v = new Vector();
            int l = s.length();
            boolean inCurly = false;
            FastStringBuffer buf = new FastStringBuffer();

            // parse through string, breaking on whitespaces.  I do this instead
            // of a tokenizer so I can track whitespace inside of curly brackets,
            // which theoretically shouldn't happen if they contain legal URLs.
            for (int i = 0; i < l; i++)
            {
                char c = s.charAt(i);

                if (Character.isWhitespace(c))
                {
                    if (!inCurly)
                    {
                        if (buf.length() > 0)
                        {
                            addCdataSectionElement(buf.toString(), v);
                            buf.reset();
                        }
                        continue;
                    }
                }
                else if ('{' == c)
                    inCurly = true;
                else if ('}' == c)
                    inCurly = false;

                buf.append(c);
            }

            if (buf.length() > 0)
            {
                addCdataSectionElement(buf.toString(), v);
                buf.reset();
            }
            // call the official, public method to set the collected names
            setCdataSectionElements(v);
        }

    }

    /**
     * Adds a URI/LocalName pair of strings to the list.
     *
     * @param name String of the form "{uri}local" or "local" 
     * 
     * @return a QName object
     */
    private void addCdataSectionElement(String URI_and_localName, Vector v)
    {

        StringTokenizer tokenizer =
            new StringTokenizer(URI_and_localName, "{}", false);
        QName qname;
        String s1 = tokenizer.nextToken();
        String s2 = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : null;

        if (null == s2)
        {
            // add null URI and the local name
            v.addElement(null);
            v.addElement(s1);
        }
        else
        {
            // add URI, then local name
            v.addElement(s1);
            v.addElement(s2);
        }
    }

    /**
     * Remembers the cdata sections specified in the cdata-section-elements.
     * The "official way to set URI and localName pairs. 
     * This method should be used by both Xalan and XSLTC.
     * 
     * @param Vector URI_and_localNames a vector of pairs of Strings (URI/local)
     */
    public void setCdataSectionElements(Vector URI_and_localNames)
    {
        m_cdataSectionElements = URI_and_localNames;
    }

    /**
     * Makes sure that the namespace URI for the given qualified attribute name
     * is declared.
     * @param ns the namespace URI
     * @param rawName the qualified name 
     * @return returns null if no action is taken, otherwise it returns the
     * prefix used in declaring the namespace. 
     * @throws SAXException
     */
    protected String ensureAttributesNamespaceIsDeclared(
        String ns,
        String localName,
        String rawName)
        throws org.xml.sax.SAXException
    {

        if (ns != null && ns.length() > 0)
        {

            // extract the prefix in front of the raw name
            int index = 0;
            String prefixFromRawName =
                (index = rawName.indexOf(":")) < 0
                    ? ""
                    : rawName.substring(0, index);

            if (index > 0)
            {
                // we have a prefix, lets see if it maps to a namespace 
                String uri = m_prefixMap.lookupNamespace(prefixFromRawName);
                if (uri != null && uri.equals(ns))
                {
                    // the prefix in the raw name is already maps to the given namespace uri
                    // so we don't need to do anything
                    return null;
                }
                else
                {
                    // The uri does not map to the prefix in the raw name,
                    // so lets make the mapping.
                    this.startPrefixMapping(prefixFromRawName, ns, false);
                    this.addAttribute(
                        "http://www.w3.org/2000/xmlns/",
                        prefixFromRawName,
                        "xmlns:" + prefixFromRawName,
                        "CDATA",
                        ns);
                    return prefixFromRawName;
                }
            }
            else
            {
                // we don't have a prefix in the raw name.
                // Does the URI map to a prefix already?
                String prefix = m_prefixMap.lookupPrefix(ns);
                if (prefix == null)
                {
                    // uri is not associated with a prefix,
                    // so lets generate a new prefix to use
                    prefix = m_prefixMap.generateNextPrefix();
                    this.startPrefixMapping(prefix, ns, false);
                    this.addAttribute(
                        "http://www.w3.org/2000/xmlns/",
                        prefix,
                        "xmlns:" + prefix,
                        "CDATA",
                        ns);
                }

                return prefix;

            }
        }
        return null;
    }

    private void ensurePrefixIsDeclared(String ns, String rawName)
        throws org.xml.sax.SAXException
    {

        if (ns != null && ns.length() > 0)
        {
            int index;
            String prefix =
                (index = rawName.indexOf(":")) < 0
                    ? ""
                    : rawName.substring(0, index);

            if (null != prefix)
            {
                String foundURI = m_prefixMap.lookupNamespace(prefix);

                if ((null == foundURI) || !foundURI.equals(ns))
                {
                    this.startPrefixMapping(prefix, ns);

                    // Bugzilla1133: Generate attribute as well as namespace event.
                    // SAX does expect both.

                    this.addAttributeAlways(
                        "http://www.w3.org/2000/xmlns/",
                        prefix,
                        "xmlns" + (prefix.length() == 0 ? "" : ":") + prefix,
                        "CDATA",
                        ns);
                }

            }
        }
    }

    /**
     * This method flushes any pending events, which can be startDocument()
     * closing the opening tag of an element, or closing an open CDATA section.
     */
    public void flushPending()
    {
        try
        {

            if (m_needToCallStartDocument)
            {
                startDocumentInternal();
                m_needToCallStartDocument = false;
            }
            if (m_startTagOpen)
            {
                closeStartTag();
                m_startTagOpen = false;
            }

            if (m_cdataTagOpen)
            {
                closeCDATA();
                m_cdataTagOpen = false;
            }
        }
        catch (SAXException e)
        { // can we do anything useful here,
            // or should this method throw a SAXException?
        }

    }

    public void setContentHandler(ContentHandler ch)
    {
        // this method is really only useful in the ToSAXHandler classes but it is
        // in the interface.  If the method defined here is ever called
        // we are probably in trouble.
    }

    /**
     * Adds the given attribute to the set of attributes, even if there is
     * nocurrently open element. This is useful if a SAX startPrefixMapping()
     * should need to add an attribute before the element name is seen.
     * 
     * This method is a copy of its super classes method, except that some
     * tracing of events is done.  This is so the tracing is only done for
     * stream serializers, not for SAX ones.
     *
     * @param uri the URI of the attribute
     * @param localName the local name of the attribute
     * @param rawName   the qualified name of the attribute
     * @param type the type of the attribute (probably CDATA)
     * @param value the value of the attribute
     */
    public void addAttributeAlways(
        String uri,
        String localName,
        String rawName,
        String type,
        String value)
    {

        int index;
        index = m_attributes.getIndex(rawName);
        if (index >= 0)
        {
            String old_value = null;
            if (m_tracer != null)
            {
                old_value = m_attributes.getValue(index);
                if (value.equals(old_value))
                    old_value = null;
            }

            /* We've seen the attribute before.
             * We may have a null uri or localName, but all we really
             * want to re-set is the value anyway.
             */
            m_attributes.setValue(index, value);
            if (old_value != null)
                firePseudoAttributes();

        }
        else
        {
            // the attribute doesn't exist yet, create it
            m_attributes.addAttribute(uri, localName, rawName, type, value);
            if (m_tracer != null)
                firePseudoAttributes();
        }

    }

    /**
     * To fire off the pseudo characters of attributes, as they currently
     * exist. This method should be called everytime an attribute is added,
     * or when an attribute value is changed.
     */

    protected void firePseudoAttributes()
    {

        int nAttrs;

        if (m_tracer != null && (nAttrs = m_attributes.getLength()) > 0)
        {
            String encoding = getEncoding();
            // make a StringBuffer to write the name="value" pairs to.
            StringBuffer sb = new StringBuffer();

            // make a writer that internally appends to the same
            // StringBuffer
            java.io.Writer writer = new ToStream.WritertoStringBuffer(sb);

            try
            {
                for (int i = 0; i < nAttrs; i++)
                {

                    // for each attribute append the name="value"
                    // to the StringBuffer
                    final String name = m_attributes.getQName(i);
                    final String value = m_attributes.getValue(i);
                    sb.append(' ');
                    sb.append(name);
                    sb.append("=\"");
                    writeAttrString(writer, value, encoding);
                    sb.append('\"');
                }
            }
            catch (IOException ioe)
            {
            }

            // convert the StringBuffer to a char array and
            // emit the trace event that these characters "might"
            // be written
            char ch[] = sb.toString().toCharArray();
            m_tracer.fireGenerateEvent(
                SerializerTrace.EVENTTYPE_OUTPUT_PSEUDO_CHARACTERS,
                ch,
                0,
                ch.length);
        }
    }

    /**
     * This inner class is used only to collect attribute values
     * written by the method writeAttrString() into a string buffer.
     * In this manner trace events, and the real writing of attributes will use
     * the same code.
     * 
     * @author minchau
     *
     */
    private class WritertoStringBuffer extends java.io.Writer
    {
        final private StringBuffer m_stringbuf;
        /**
         * @see java.io.Writer#write(char[], int, int)
         */
        WritertoStringBuffer(StringBuffer sb)
        {
            m_stringbuf = sb;
        }

        public void write(char[] arg0, int arg1, int arg2) throws IOException
        {
            m_stringbuf.append(arg0, arg1, arg2);
        }
        /**
         * @see java.io.Writer#flush()
         */
        public void flush() throws IOException
        {
        }
        /**
         * @see java.io.Writer#close()
         */
        public void close() throws IOException
        {
        }

        public void write(int i)
        {
            m_stringbuf.append((char) i);
        }
        
        public void write(String s)
        {
            m_stringbuf.append(s);
        }
    }

    /**
     * @see org.apache.xml.serializer.SerializationHandler#setTransformer(Transformer)
     */
    public void setTransformer(Transformer transformer) {
        super.setTransformer(transformer);
        if (m_tracer != null
         && !(m_writer instanceof SerializerTraceWriter)  )
            m_writer = new SerializerTraceWriter(m_writer, m_tracer);        
        
        
    }
}
