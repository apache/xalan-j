/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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


package org.apache.xalan.xsltc.trax;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TemplatesHandler;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.XMLFilter;

/**
 * Implementation of a transformer factory that uses an XSLTC 
 * transformer factory for the creation of Templates objects
 * and uses the Xalan processor transformer factory for the
 * creation of Transformer objects.  
 * @author G. Todd Miller 
 */
public class SmartTransformerFactoryImpl extends SAXTransformerFactory 
{

    private SAXTransformerFactory _xsltcFactory = null;
    private SAXTransformerFactory _xalanFactory = null;
    private SAXTransformerFactory _currFactory = null;
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
	_xsltcFactory = new TransformerFactoryImpl();
	_currFactory = _xsltcFactory;
    }

    private void createXalanTransformerFactory() {
 	final String xalanMessage =
	    "org.apache.xalan.xsltc.trax.SmartTransformerFactoryImpl "+
	    "could not create an "+
	    "org.apache.xalan.processor.TransformerFactoryImpl.";
	// try to create instance of Xalan factory...	
	try {
            Class xalanFactClass = ObjectFactory.findProviderClass(
                "org.apache.xalan.processor.TransformerFactoryImpl",
                ObjectFactory.findClassLoader(), true);
	    _xalanFactory = (SAXTransformerFactory)
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
	return _xsltcFactory.newTemplatesHandler();
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
	return _xalanFactory.newTransformerHandler(); 
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
	return _xalanFactory.newTransformerHandler(src); 
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
        return _xsltcFactory.newTransformerHandler(templates);
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
