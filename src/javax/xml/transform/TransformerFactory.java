/*
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
 * 4. The name "Apache Software Foundation" must not be used to endorse or
 *    promote products derived from this software without prior written
 *    permission. For written permission, please contact apache@apache.org.
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
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package javax.xml.transform;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.util.Properties;
import java.util.Enumeration;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * A TransformerFactory instance can be used to create
 * {@link javax.xml.transform.Transformer} and
 * {@link javax.xml.transform.Templates} objects.
 *
 * <p>The system property that determines which Factory implementation to
 * create is named <code>"javax.xml.transform.TransformerFactory"</code>.
 * This property names a concrete subclass of the
 * <code>TransformerFactory</code> abstract class. If the property is not
 * defined, a platform default is be used.</p>
 *
 * An implementation of the <code>TransformerFactory</code> class is
 * <em>NOT</em> guaranteed to be thread safe. It is up to the user application 
 * to make sure about the use of the <code>TransformerFactory</code> from 
 * more than one thread. Alternatively the application can have one instance 
 * of the <code>TransformerFactory</code> per thread.
 * An application can use the same instance of the factory to obtain one or 
 * more instances of a <code>Transformer</code> or <code>Templates</code> 
 * provided the instance of the factory isn't being used in more than one 
 * thread at a time.
 */
public abstract class TransformerFactory {
    /**
     * Default constructor is protected on purpose.
     */
    protected TransformerFactory() {}

    /**
     * Obtain a new instance of a <code>TransformerFactory</code>.
     * This static method creates a new factory instance
     * This method uses the following ordered lookup procedure to determine
     * the <code>TransformerFactory</code> implementation class to
     * load:
     * <ul>
     * <li>
     * Use the <code>javax.xml.transform.TransformerFactory</code> system
     * property.
     * </li>
     * <li>
     * Use the properties file "lib/jaxp.properties" in the JRE directory.
     * This configuration file is in standard <code>java.util.Properties
     * </code> format and contains the fully qualified name of the
     * implementation class with the key being the system property defined
     * above.
     * </li>
     * <li>
     * Use the Services API (as detailed in the JAR specification), if
     * available, to determine the classname. The Services API will look
     * for a classname in the file
     * <code>META-INF/services/javax.xml.transform.TransformerFactory</code>
     * in jars available to the runtime.
     * </li>
     * <li>
     * Platform default <code>TransformerFactory</code> instance.
     * </li>
     * </ul>
     *
     * Once an application has obtained a reference to a <code>
     * TransformerFactory</code> it can use the factory to configure
     * and obtain parser instances.
     *
     * @return new TransformerFactory instance, never null.
     *
     * @throws TransformerFactoryConfigurationError
     * if the implmentation is not available or cannot be instantiated.
     */
    public static TransformerFactory newInstance()
        throws TransformerFactoryConfigurationError
    {
        try {
            return (TransformerFactory) FactoryFinder.find(
                /* The default property name according to the JAXP spec */
                "javax.xml.transform.TransformerFactory",
                /* The fallback implementation class name */
                "org.apache.xalan.processor.TransformerFactoryImpl");
        } catch (FactoryFinder.ConfigurationError e) {
            throw new TransformerFactoryConfigurationError(e.getException(),
                                                           e.getMessage());
        }
    }

    /**
     * Process the Source into a Transformer object.  Care must
     * be given not to use this object in multiple threads running concurrently.
     * Different TransformerFactories can be used concurrently by different
     * threads.
     *
     * @param source An object that holds a URI, input stream, etc.
     *
     * @return A Transformer object that may be used to perform a transformation
     * in a single thread, never null.
     *
     * @exception TransformerConfigurationException May throw this during the parse
     *            when it is constructing the Templates object and fails.
     */
    public abstract Transformer newTransformer(Source source)
        throws TransformerConfigurationException;

    /**
     * Create a new Transformer object that performs a copy
     * of the source to the result.
     *
     * @param source An object that holds a URI, input stream, etc.
     *
     * @return A Transformer object that may be used to perform a transformation
     * in a single thread, never null.
     *
     * @exception TransformerConfigurationException May throw this during
     *            the parse when it is constructing the
     *            Templates object and fails.
     */
    public abstract Transformer newTransformer()
        throws TransformerConfigurationException;

    /**
     * Process the Source into a Templates object, which is a
     * a compiled representation of the source. This Templates object
     * may then be used concurrently across multiple threads.  Creating
     * a Templates object allows the TransformerFactory to do detailed
     * performance optimization of transformation instructions, without
     * penalizing runtime transformation.
     *
     * @param source An object that holds a URL, input stream, etc.
     *
     * @return A Templates object capable of being used for transformation purposes,
     * never null.
     *
     * @exception TransformerConfigurationException May throw this during the parse when it
     *            is constructing the Templates object and fails.
     */
    public abstract Templates newTemplates(Source source)
        throws TransformerConfigurationException;

    /**
     * Get the stylesheet specification(s) associated
     * via the xml-stylesheet processing instruction (see
     * http://www.w3.org/TR/xml-stylesheet/) with the document
     * document specified in the source parameter, and that match
     * the given criteria.  Note that it is possible to return several
     * stylesheets, in which case they are applied as if they were
     * a list of imports or cascades in a single stylesheet.
     *
     * @param source The XML source document.
     * @param media The media attribute to be matched.  May be null, in which
     *              case the prefered templates will be used (i.e. alternate = no).
     * @param title The value of the title attribute to match.  May be null.
     * @param charset The value of the charset attribute to match.  May be null.
     *
     * @return A Source object suitable for passing to the TransformerFactory.
     *
     * @throws TransformerConfigurationException.
     */
    public abstract Source getAssociatedStylesheet(
        Source source, String media, String title, String charset)
            throws TransformerConfigurationException;

    /**
     * Set an object that is used by default during the transformation
     * to resolve URIs used in xsl:import, or xsl:include.
     *
     * @param resolver An object that implements the URIResolver interface,
     * or null.
     */
    public abstract void setURIResolver(URIResolver resolver);

    /**
     * Get the object that is used by default during the transformation
     * to resolve URIs used in document(), xsl:import, or xsl:include.
     *
     * @return The URIResolver that was set with setURIResolver.
     */
    public abstract URIResolver getURIResolver();

    //======= CONFIGURATION METHODS =======

    /**
     * Look up the value of a feature.
     *
     * <p>The feature name is any absolute URI.</p>
     * @param name The feature name, which is an absolute URI.
     * @return The current state of the feature (true or false).
     */
    public abstract boolean getFeature(String name);

    /**
     * Allows the user to set specific attributes on the underlying
     * implementation.  An attribute in this context is defined to
     * be an option that the implementation provides.
     *
     * @param name The name of the attribute.
     * @param value The value of the attribute.
     * @throws IllegalArgumentException thrown if the underlying
     * implementation doesn't recognize the attribute.
     */
    public abstract void setAttribute(String name, Object value)
        throws IllegalArgumentException;

    /**
     * Allows the user to retrieve specific attributes on the underlying
     * implementation.
     * @param name The name of the attribute.
     * @return value The value of the attribute.
     * @throws IllegalArgumentException thrown if the underlying
     * implementation doesn't recognize the attribute.
     */
    public abstract Object getAttribute(String name)
        throws IllegalArgumentException;

    /**
     * Set the error event listener for the TransformerFactory, which
     * is used for the processing of transformation instructions,
     * and not for the transformation itself.
     *
     * @param listener The new error listener.
     * @throws IllegalArgumentException if listener is null.
     */
    public abstract void setErrorListener(ErrorListener listener)
        throws IllegalArgumentException;

    /**
     * Get the error event handler for the TransformerFactory.
     *
     * @return The current error handler, which should never be null.
     */
    public abstract ErrorListener getErrorListener();
}
