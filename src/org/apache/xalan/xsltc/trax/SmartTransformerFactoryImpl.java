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
 * @author G. Todd Miller 
 *
 */


package org.apache.xalan.xsltc.trax;

import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.xml.sax.XMLFilter;
import org.xml.sax.InputSource;
import org.apache.xalan.xsltc.compiler.XSLTC;
import org.apache.xalan.xsltc.compiler.SourceLoader;

/**
 * Implementation of a transformer factory that uses an XSLTC 
 * transformer factory for the creation of Templates objects
 * and uses the Xalan processor transformer factory for the
 * creation of Transformer objects.  
 */
public class SmartTransformerFactoryImpl extends SAXTransformerFactory 
{

    private TransformerFactory _xsltcFactory = null;
    private TransformerFactory _xalanFactory = null;
    private TransformerFactory _currFactory = null;
    private ErrorListener      _errorlistener = null;
    private URIResolver        _uriresolver = null;

    /**
     * implementation of the SmartTransformerFactory. This factory
     * uses org.apache.xalan.xsltc.trax.TransformerFactory
     * to return Templates objects; and uses 
     * org.apache.xalan.processor.TransformerFactory
     * to return Transformer objects.  
     */
    public SmartTransformerFactoryImpl() { }

    private void createXSLTCTransformerFactory() {
	// set up error messages from each factory...
 	final String xsltcMessage =
	    "org.apache.xalan.xsltc.trax.SmartTransformerFactoryImpl "+
            "could not create an "+
            "org.apache.xalan.xsltc.trax.TransformerFactoryImpl.";
	
	// try to create instance of XSLTC factory...	
	try {
	    Class xsltcFactClass = Class.forName(
		"org.apache.xalan.xsltc.trax.TransformerFactoryImpl");
	    _xsltcFactory = (org.apache.xalan.xsltc.trax.TransformerFactoryImpl)
		xsltcFactClass.newInstance();
	} 
	catch (ClassNotFoundException e) {
	    System.err.println(xsltcMessage);
	} 
 	catch (InstantiationException e) {
	    System.err.println(xsltcMessage);
	}
 	catch (IllegalAccessException e) {
	    System.err.println(xsltcMessage);
	}
	_currFactory = _xsltcFactory;
    }

    private void createXalanTransformerFactory() {
 	final String xalanMessage =
	    "org.apache.xalan.xsltc.trax.SmartTransformerFactoryImpl "+
	    "could not create an "+
	    "org.apache.xalan.processor.TransformerFactoryImpl.";
	// try to create instance of Xalan factory...	
	try {
	    Class xalanFactClass = Class.forName(
		"org.apache.xalan.processor.TransformerFactoryImpl");
	    _xalanFactory = (org.apache.xalan.processor.TransformerFactoryImpl)
		xalanFactClass.newInstance();
	} 
	catch (ClassNotFoundException e) {
	    System.err.println(xalanMessage);
        }
 	catch (InstantiationException e) {
	    System.err.println(xalanMessage);
	}
 	catch (IllegalAccessException e) {
	    System.err.println(xalanMessage);
	}
	_currFactory = _xalanFactory;
    }

    public void setErrorListener(ErrorListener listener) 
	throws IllegalArgumentException 
    {
	_errorlistener = listener;
    }

    public ErrorListener getErrorListener() { 
	return _errorlistener;
    }

    public Object getAttribute(String name) 
	throws IllegalArgumentException 
    {
	// GTM: NB: 'debug' should change to something more unique... 
	if ((name.equals("translet-name")) || (name.equals("debug"))) { 
	    if (_xsltcFactory == null) {
                createXSLTCTransformerFactory();
            }
            return _xsltcFactory.getAttribute(name); 
        }
        else {
	    if (_xalanFactory == null) {
	        createXalanTransformerFactory();
	    } 
	    return _xalanFactory.getAttribute(name);
        }
    }

    public void setAttribute(String name, Object value) 
	throws IllegalArgumentException { 
	// GTM: NB: 'debug' should change to something more unique... 
	if ((name.equals("translet-name")) || (name.equals("debug"))) { 
	    if (_xsltcFactory == null) {
                createXSLTCTransformerFactory();
            }
            _xsltcFactory.setAttribute(name, value); 
        }
        else {
	    if (_xalanFactory == null) {
	        createXalanTransformerFactory();
	    } 
	    _xalanFactory.setAttribute(name, value);
        }
    }

    /**
     * javax.xml.transform.sax.TransformerFactory implementation.
     * Look up the value of a feature (to see if it is supported).
     * This method must be updated as the various methods and features of this
     * class are implemented.
     *
     * @param name The feature name
     * @return 'true' if feature is supported, 'false' if not
     */
    public boolean getFeature(String name) { 
	// All supported features should be listed here
        String[] features = {
            DOMSource.FEATURE,
            DOMResult.FEATURE,
            SAXSource.FEATURE,
            SAXResult.FEATURE,
            StreamSource.FEATURE,
            StreamResult.FEATURE
        };

        // Inefficient, but it really does not matter in a function like this
        for (int i=0; i<features.length; i++) {
            if (name.equals(features[i])) return true;
	}

        // Feature not supported
        return false;
    }

    public URIResolver getURIResolver() {
	return _uriresolver; 
    } 

    public void setURIResolver(URIResolver resolver) {
	_uriresolver = resolver;
    }

    public Source getAssociatedStylesheet(Source source, String media,
					  String title, String charset)
	throws TransformerConfigurationException 
    {
	if (_currFactory == null) {
            createXSLTCTransformerFactory();
        }
	return _currFactory.getAssociatedStylesheet(source, media,
		title, charset);
    }

    /**
     * Create a Transformer object that copies the input document to the
     * result. Uses the org.apache.xalan.processor.TransformerFactory.
     * @return A Transformer object.
     */
    public Transformer newTransformer()
	throws TransformerConfigurationException 
    {
	if (_xalanFactory == null) {
            createXalanTransformerFactory();
        }
	if (_errorlistener != null) {
	    _xalanFactory.setErrorListener(_errorlistener);	    
	}
	if (_uriresolver != null) {
	    _xalanFactory.setURIResolver(_uriresolver);
	}
 	_currFactory = _xalanFactory;	 
	return _currFactory.newTransformer(); 
    }

    /**
     * Create a Transformer object that from the input stylesheet 
     * Uses the org.apache.xalan.processor.TransformerFactory.
     * @param source the stylesheet.
     * @return A Transformer object.
     */
    public Transformer newTransformer(Source source) throws
	TransformerConfigurationException 
    {
        if (_xalanFactory == null) {
            createXalanTransformerFactory();
        }
	if (_errorlistener != null) {
	    _xalanFactory.setErrorListener(_errorlistener);	    
	}
	if (_uriresolver != null) {
	    _xalanFactory.setURIResolver(_uriresolver);
	}
 	_currFactory = _xalanFactory;	 
	return _currFactory.newTransformer(source); 
    }

    /**
     * Create a Templates object that from the input stylesheet 
     * Uses the org.apache.xalan.xsltc.trax.TransformerFactory.
     * @param source the stylesheet.
     * @return A Templates object.
     */
    public Templates newTemplates(Source source)
	throws TransformerConfigurationException 
    {
        if (_xsltcFactory == null) {
            createXSLTCTransformerFactory();
        }
	if (_errorlistener != null) {
	    _xsltcFactory.setErrorListener(_errorlistener);	    
	}
	if (_uriresolver != null) {
	    _xsltcFactory.setURIResolver(_uriresolver);
	}
 	_currFactory = _xsltcFactory;	 
	return _currFactory.newTemplates(source); 
    }

    /**
     * Get a TemplatesHandler object that can process SAX ContentHandler
     * events into a Templates object. Uses the
     * org.apache.xalan.xsltc.trax.TransformerFactory.
     */
    public TemplatesHandler newTemplatesHandler() 
	throws TransformerConfigurationException 
    {
        if (_xsltcFactory == null) {
            createXSLTCTransformerFactory();
        }
	if (_errorlistener != null) {
	    _xsltcFactory.setErrorListener(_errorlistener);	    
	}
	if (_uriresolver != null) {
	    _xsltcFactory.setURIResolver(_uriresolver);
	}
	return ((SAXTransformerFactory)_xsltcFactory).newTemplatesHandler();
    }

    /**
     * Get a TransformerHandler object that can process SAX ContentHandler
     * events based on a copy transformer. 
     * Uses org.apache.xalan.processor.TransformerFactory. 
     */
    public TransformerHandler newTransformerHandler() 
	throws TransformerConfigurationException 
    {
        if (_xalanFactory == null) {
            createXalanTransformerFactory();
        }
	if (_errorlistener != null) {
	    _xalanFactory.setErrorListener(_errorlistener);	    
	}
	if (_uriresolver != null) {
	    _xalanFactory.setURIResolver(_uriresolver);
	}
	return ((SAXTransformerFactory)_xalanFactory).newTransformerHandler(); 
    }

    /**
     * Get a TransformerHandler object that can process SAX ContentHandler
     * events based on a transformer specified by the stylesheet Source. 
     * Uses org.apache.xalan.processor.TransformerFactory. 
     */
    public TransformerHandler newTransformerHandler(Source src) 
	throws TransformerConfigurationException 
    {
        if (_xalanFactory == null) {
            createXalanTransformerFactory();
        }
	if (_errorlistener != null) {
	    _xalanFactory.setErrorListener(_errorlistener);	    
	}
	if (_uriresolver != null) {
	    _xalanFactory.setURIResolver(_uriresolver);
	}
	return 
            ((SAXTransformerFactory)_xalanFactory).newTransformerHandler(src); 
    }


    /**
     * Get a TransformerHandler object that can process SAX ContentHandler
     * events based on a transformer specified by the stylesheet Source. 
     * Uses org.apache.xalan.xsltc.trax.TransformerFactory. 
     */
    public TransformerHandler newTransformerHandler(Templates templates) 
	throws TransformerConfigurationException  
    {
        if (_xsltcFactory == null) {
            createXSLTCTransformerFactory();
        }
	if (_errorlistener != null) {
	    _xsltcFactory.setErrorListener(_errorlistener);	    
	}
	if (_uriresolver != null) {
	    _xsltcFactory.setURIResolver(_uriresolver);
	}
        return 
        ((SAXTransformerFactory)_xsltcFactory).newTransformerHandler(templates);
    }


    /**
     * Create an XMLFilter that uses the given source as the
     * transformation instructions. Uses
     * org.apache.xalan.xsltc.trax.TransformerFactory.
     */
    public XMLFilter newXMLFilter(Source src) 
	throws TransformerConfigurationException {
        if (_xsltcFactory == null) {
            createXSLTCTransformerFactory();
        }
	if (_errorlistener != null) {
	    _xsltcFactory.setErrorListener(_errorlistener);	    
	}
	if (_uriresolver != null) {
	    _xsltcFactory.setURIResolver(_uriresolver);
	}
	Templates templates = _xsltcFactory.newTemplates(src);
	if (templates == null ) return null;
	return newXMLFilter(templates); 
    }

    /*
     * Create an XMLFilter that uses the given source as the
     * transformation instructions. Uses
     * org.apache.xalan.xsltc.trax.TransformerFactory.
     */
    public XMLFilter newXMLFilter(Templates templates) 
	throws TransformerConfigurationException {
	try {
            return new org.apache.xalan.xsltc.trax.TrAXFilter(templates);
        }
        catch(TransformerConfigurationException e1) {
            if (_xsltcFactory == null) {
                createXSLTCTransformerFactory();
            }
	    ErrorListener errorListener = _xsltcFactory.getErrorListener();
            if(errorListener != null) {
                try {
                    errorListener.fatalError(e1);
                    return null;
                }
                catch( TransformerException e2) {
                    new TransformerConfigurationException(e2);
                }
            }
            throw e1;
        }
    }
}
