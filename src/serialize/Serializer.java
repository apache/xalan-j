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
package serialize;


import java.io.Writer;
import java.io.OutputStream;
import java.io.IOException;
import org.xml.sax.DocumentHandler;
import org.xml.sax.ContentHandler;


/**
 * A serializer is used for serializing a document with a given output
 * method. The {@link Serializer} object serves as an anchor point for
 * setting the output stream and output format, for obtaining objects
 * for serializing the document, and for resetting the serializer.
 * <p>
 * Prior to using the serializer, the output format and output stream
 * or writer should be set. The serializer is then used in one of
 * three ways:
 * <ul>
 * <li>To serialize SAX 1 events call {@link #asDocumentHandler}
 * <li>To serialize SAX 2 events call {@link #asContentHandler}
 * <li>To serialize a DOM document call {@link #asDOMSerializer}
 * </ul>
 * <p>
 * The application may call one of these methods to obtain a way to
 * serialize the document. It may not attempt to use two different
 * handlers at the same time, nor should it use the same handler to
 * serialize two documents.
 * <p>
 * The serializer may be recycled and used with a different or the
 * same output format and output stream, by calling the {@link #reset}
 * method after completing serialization.
 * <p>
 * A serializer is not thread safe. Only one thread should call the
 * <tt>asXXX</tt> methods and use the returned handler.
 * <p>
 * Example:
 * <pre>
 * ser = SerializerFactory.getSerializer( Method.XML );
 * emptyDoc( ser, System.out );
 * emptyDoc( ser, System.err );
 * . . . 
 *
 * void emptyDoc( Serializer ser, OutputStream os )
 * {
 *     ser.setOutputStream( os );
 *     ser.asDocumentHandler().startDocument();
 *     ser.asDocumentHandler().startElement( "empty", new AttributeListImpl() );
 *     ser.asDocumentHandler().endElement( "empty" );
 *     ser.asDocumentHandler().endDocument();
 *     ser.reset();
 * }
 * </pre>
 *
 * @version Alpha
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @author <a href="mailto:Scott_Boag/CAM/Lotus@lotus.com">Scott Boag</a>
 */
public interface Serializer
{


    /**
     * Specifies an output stream to which the document should be
     * serialized. This method should not be called while the
     * serializer is in the process of serializing a document.
     * <p>
     * The encoding specified in the {@link OutputFormat} is used, or
     * if no encoding was specified, the default for the selected
     * output method.
     *
     * @param output The output stream
     */
    public void setOutputStream( OutputStream output );
    public OutputStream getOutputStream();


    /**
     * Specifies a writer to which the document should be serialized.
     * This method should not be called while the serializer is in
     * the process of serializing a document.
     * <p>
     * The encoding specified for the {@link OutputFormat} must be
     * identical to the output format used with the writer.
     *
     * @param writer The output writer stream
     */
    public void setWriter( Writer writer );
    public Writer getWriter();

    /**
     * Specifies an output format for this serializer. It the
     * serializer has already been associated with an output format,
     * it will switch to the new format. This method should not be
     * called while the serializer is in the process of serializing
     * a document.
     *
     * @param format The output format to use
     */
    public void setOutputFormat( OutputFormat format );


    /**
     * Returns the output format for this serializer.
     *
     * @return The output format in use
     */
    public OutputFormat getOutputFormat();


    /**
     * Return a {@link DocumentHandler} interface into this serializer.
     * If the serializer does not support the {@link DocumentHandler}
     * interface, it should return null.
     *
     * @return A {@link DocumentHandler} interface into this serializer,
     *  or null if the serializer is not SAX 1 capable
     * @throws IOException An I/O exception occured
     */
    public DocumentHandler asDocumentHandler()
        throws IOException;


    /**
     * Return a {@link ContentHandler} interface into this serializer.
     * If the serializer does not support the {@link ContentHandler}
     * interface, it should return null.
     *
     * @return A {@link ContentHandler} interface into this serializer,
     *  or null if the serializer is not SAX 2 capable
     * @throws IOException An I/O exception occured
     */
    public ContentHandler asContentHandler()
        throws IOException;


    /**
     * Return a {@link DOMSerializer} interface into this serializer.
     * If the serializer does not support the {@link DOMSerializer}
     * interface, it should return null.
     *
     * @return A {@link DOMSerializer} interface into this serializer,
     *  or null if the serializer is not DOM capable
     * @throws IOException An I/O exception occured
     */
    public DOMSerializer asDOMSerializer()
        throws IOException;


    /**
     * Resets the serializer. If this method returns true, the
     * serializer may be used for subsequent serialization of new
     * documents. It is possible to change the output format and
     * output stream prior to serializing, or to use the existing
     * output format and output stream.
     *
     * @return True if serializer has been reset and can be reused
     */
    public boolean reset();


}





