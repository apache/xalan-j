package org.apache.xalan.transformer;

import java.io.IOException;

import javax.xml.transform.TransformerException;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.XMLFilter;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.DTDHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.ErrorListener;

import org.apache.xalan.stree.SourceTreeHandler;

public class TrAXFilter extends XMLFilterImpl
{
  private Templates m_templates;
  private TransformerImpl m_transformer;
    
  /**
   * Construct an empty XML filter, with no parent.
   *
   * <p>This filter will have no parent: you must assign a parent
   * before you start a parse or do any configuration with
   * setFeature or setProperty.</p>
   *
   * @see org.xml.sax.XMLReader#setFeature
   * @see org.xml.sax.XMLReader#setProperty
   */
  public TrAXFilter (Templates templates)
    throws TransformerConfigurationException
  {
    m_templates = templates;
    m_transformer = (TransformerImpl)templates.newTransformer();
  }
  
  /** Set the parent reader.
   *
   * <p>This is the {@link org.xml.sax.XMLReader XMLReader} from which 
   * this filter will obtain its events and to which it will pass its 
   * configuration requests.  The parent may itself be another filter.</p>
   *
   * <p>If there is no parent reader set, any attempt to parse
   * or to set or get a feature or property will fail.</p>
   *
   * @param parent The parent XML reader.
   * @exception java.lang.NullPointerException If the parent is null.
   */
  public void setParent (XMLReader parent)
  { 
    super.setParent(parent);
    
    if(null != parent.getContentHandler())
      this.setContentHandler(parent.getContentHandler());

    // Not really sure if we should do this here, but 
    // it seems safer in case someone calls parse() on 
    // the parent.
    setupParse ();
  }
  
  /**
   * Parse a document.
   *
   * @param input The input source for the document entity.
   * @exception javax.xml.transform.TransformerException Any SAX exception, possibly
   *            wrapping another exception.
   * @exception java.io.IOException An IO exception from the parser,
   *            possibly from a byte stream or character stream
   *            supplied by the application.
   * @see org.xml.sax.XMLReader#parse(org.xml.sax.InputSource)
   */
  public void parse (InputSource input)
    throws org.xml.sax.SAXException, IOException
  {
    if(null == getParent())
    {
      XMLReader parent = XMLReaderFactory.createXMLReader();
      try
      {
        parent.setFeature("http://xml.org/sax/features/namespace-prefixes",
                          true);
        parent.setFeature("http://apache.org/xml/features/validation/dynamic",
                          true);
      }
      catch (org.xml.sax.SAXException se){}
      // setParent calls setupParse...
      setParent(parent);
    }
    else
    {
      // Make sure everything is set up.
      setupParse ();
    }
    if(null == m_transformer.getContentHandler())
    {
      throw new org.xml.sax.SAXException("parse can not be called if the ContentHandler has not been set!");
    }

    getParent().parse(input);
    Exception e = m_transformer.getExceptionThrown();
    if(null != e)
    {
      if(e instanceof org.xml.sax.SAXException)
        throw (org.xml.sax.SAXException)e;
      else
        throw new org.xml.sax.SAXException(e);
    }
  }
  
  /**
   * Parse a document.
   *
   * @param systemId The system identifier as a fully-qualified URI.
   * @exception javax.xml.transform.TransformerException Any SAX exception, possibly
   *            wrapping another exception.
   * @exception java.io.IOException An IO exception from the parser,
   *            possibly from a byte stream or character stream
   *            supplied by the application.
   * @see org.xml.sax.XMLReader#parse(java.lang.String)
   */
  public void parse (String systemId)
    throws org.xml.sax.SAXException, IOException
  {
    parse(new InputSource(systemId));
  }


  /**
   * Set up before a parse.
   *
   * <p>Before every parse, check whether the parent is
   * non-null, and re-register the filter for all of the 
   * events.</p>
   */
  private void setupParse ()
  {
    XMLReader p = getParent();
    if (p == null) {
      throw new NullPointerException("No parent for filter");
    }
    
    ContentHandler ch = m_transformer.getInputContentHandler();
    if(ch instanceof SourceTreeHandler)
      ((SourceTreeHandler)ch).setUseMultiThreading(true);
    p.setContentHandler(ch);

    if(ch instanceof EntityResolver)
      p.setEntityResolver((EntityResolver)ch);
    else
      p.setEntityResolver(this);
    
    if(ch instanceof DTDHandler)
      p.setDTDHandler((DTDHandler)ch);
    else
      p.setDTDHandler(this);
    
    ErrorListener elistener = m_transformer.getErrorListener();
    if((null != elistener) && (elistener instanceof org.xml.sax.ErrorHandler))
      p.setErrorHandler((org.xml.sax.ErrorHandler)elistener);
    else
      p.setErrorHandler(this);
  }

  /**
   * Set the content event handler.
   *
   * @param resolver The new content handler.
   * @exception java.lang.NullPointerException If the handler
   *            is null.
   * @see org.xml.sax.XMLReader#setContentHandler
   */
  public void setContentHandler (ContentHandler handler)
  {
    m_transformer.setContentHandler(handler);
    // super.setContentHandler(m_transformer.getResultTreeHandler());
  }
  
  public void setErrorListener (ErrorListener handler)
  {
    m_transformer.setErrorListener(handler);
  }

}
