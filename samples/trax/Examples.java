import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

// Needed java classes
import java.io.InputStream;
import java.io.Reader;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;

import java.util.Properties;

// Needed SAX classes
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.Parser;
import org.xml.sax.helpers.ParserAdapter;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.XMLReader;
import org.xml.sax.XMLFilter;
import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.ext.DeclHandler;

// Needed DOM classes
import org.w3c.dom.Node;

// Needed JAXP classes
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.BufferedInputStream;    // dml

/**
 * Some examples to show how the Simple API for Transformations
 * could be used.
 *
 * @author <a href="mailto:scott_boag@lotus.com">Scott Boag</a>
 */
public class Examples
{

  /**
   * Method main
   */
  public static void main(String argv[])
          throws TransformerException, TransformerConfigurationException, IOException, SAXException,
                 ParserConfigurationException, FileNotFoundException
  {
    System.out.println("\n\n==== exampleSimple ====");
    exampleSimple1("xml/foo.xml", "xsl/foo.xsl");
    System.out.println("\n\n==== exampleFromStream ====");
    exampleFromStream("xml/foo.xml", "xsl/foo.xsl");
    System.out.println("\n\n==== exampleFromReader ====");
    exampleFromReader("xml/foo.xml", "xsl/foo.xsl");
    System.out.println("\n\n==== exampleUseTemplatesObj ====");
    exampleUseTemplatesObj("xml/foo.xml", "xml/baz.xml", "xsl/foo.xsl");
    System.out.println("\n\n==== exampleContentHandlerToContentHandler ====");
    exampleContentHandlerToContentHandler("xml/foo.xml", "xsl/foo.xsl");
    System.out.println("\n\n==== exampleXMLReader ====");
    exampleXMLReader("xml/foo.xml", "xsl/foo.xsl");
    System.out.println("\n\n==== exampleXMLFilter ====");
    exampleXMLFilter("xml/foo.xml", "xsl/foo.xsl");
    System.out.println("\n\n==== exampleXMLFilterChain ====");
    exampleXMLFilterChain("xml/foo.xml", "xsl/foo.xsl", "xsl/foo2.xsl", "xsl/foo3.xsl");
    System.out.println("\n\n==== exampleDOM2DOM ====");
    exampleDOM2DOM("xml/foo.xml", "xsl/foo.xsl");
    System.out.println("\n\n==== exampleParam ====");
    exampleParam("xml/foo.xml", "xsl/foo.xsl");
    System.out.println("\n\n==== exampleOutputProperties ====");
    exampleOutputProperties("xml/foo.xml", "xsl/foo.xsl");
    System.out.println("\n\n==== exampleUseAssociated ====");
    exampleUseAssociated("xml/foo.xml");
    System.out.println("\n\n==== exampleContentHandler2DOM ====");
    exampleContentHandler2DOM("xml/foo.xml", "xsl/foo.xsl");
    System.out.println("\n\n==== exampleAsSerializer ====");
    exampleAsSerializer("xml/foo.xml", "xsl/foo.xsl");
    System.out.println("\n\n==== exampleContentHandler2DOM ====");
    exampleContentHandler2DOM("xml/foo.xml", "xsl/foo.xsl");
    
    System.out.println("\n==== done! ====");
  }
  
  /**
   * Show the simplest possible transformation from system id 
   * to output stream.
   */
  public static void exampleSimple1(String sourceID, String xslID)
    throws TransformerException, TransformerConfigurationException
  {
    // Create a transform factory instance.
    TransformerFactory tfactory = TransformerFactory.newInstance();
    
    // Create a transformer for the stylesheet.
    Transformer transformer 
      = tfactory.newTransformer(new StreamSource(xslID));
    
    // Transform the source XML to System.out.
    transformer.transform( new StreamSource(sourceID),
                           new StreamResult(System.out));
  }
  
  /**
   * Show simple transformation from input stream to output stream.
   */
  public static void exampleFromStream(String sourceID, String xslID)
    throws TransformerException, TransformerConfigurationException,
           FileNotFoundException
  {
    // Create a transform factory instance.
    TransformerFactory tfactory = TransformerFactory.newInstance();

    InputStream xslIS = new BufferedInputStream(new FileInputStream(xslID));
    StreamSource xslSource = new StreamSource(xslIS);
    // Note that if we don't do this, relative URLs can not be resolved correctly!
    xslSource.setSystemId(xslID);

    // Create a transformer for the stylesheet.
    Transformer transformer = tfactory.newTransformer(xslSource);
    
    InputStream xmlIS = new BufferedInputStream(new FileInputStream(sourceID));
    StreamSource xmlSource = new StreamSource(xmlIS);
    // Note that if we don't do this, relative URLs can not be resolved correctly!
    xmlSource.setSystemId(sourceID);
    
    // Transform the source XML to System.out.
    transformer.transform( xmlSource, new StreamResult(System.out));
  }
  
  /**
   * Show simple transformation from reader to output stream.  In general 
   * this use case is discouraged, since the XML encoding can not be 
   * processed.
   */
  public static void exampleFromReader(String sourceID, String xslID)
    throws TransformerException, TransformerConfigurationException,
           FileNotFoundException
  {
    // Create a transform factory instance.
    TransformerFactory tfactory = TransformerFactory.newInstance();

    // Note that in this case the XML encoding can not be processed!
    Reader xslReader = new BufferedReader(new FileReader(xslID));
    StreamSource xslSource = new StreamSource(xslReader);
    // Note that if we don't do this, relative URLs can not be resolved correctly!
    xslSource.setSystemId(xslID);

    // Create a transformer for the stylesheet.
    Transformer transformer = tfactory.newTransformer(xslSource);
    
    // Note that in this case the XML encoding can not be processed!
    Reader xmlReader = new BufferedReader(new FileReader(sourceID));
    StreamSource xmlSource = new StreamSource(xmlReader);
    // Note that if we don't do this, relative URLs can not be resolved correctly!
    xmlSource.setSystemId(sourceID);
    
    // Transform the source XML to System.out.
    transformer.transform( xmlSource, new StreamResult(System.out));
  }


 
  /**
   * Show the simplest possible transformation from system id to output stream.
   */
  public static void exampleUseTemplatesObj(String sourceID1, 
                                    String sourceID2, 
                                    String xslID)
          throws TransformerException, TransformerConfigurationException
  {

    TransformerFactory tfactory = TransformerFactory.newInstance();
    
    // Create a templates object, which is the processed, 
    // thread-safe representation of the stylesheet.
    Templates templates = tfactory.newTemplates(new StreamSource(xslID));

    // Illustrate the fact that you can make multiple transformers 
    // from the same template.
    Transformer transformer1 = templates.newTransformer();
    Transformer transformer2 = templates.newTransformer();
    
    System.out.println("\n\n----- transform of "+sourceID1+" -----");
    
    transformer1.transform(new StreamSource(sourceID1),
                          new StreamResult(System.out));
    
    System.out.println("\n\n----- transform of "+sourceID2+" -----");
    
    transformer2.transform(new StreamSource(sourceID2),
                          new StreamResult(System.out));
  }
  


  /**
   * Show the Transformer using SAX events in and SAX events out.
   */
  public static void exampleContentHandlerToContentHandler(String sourceID, 
                                                           String xslID)
          throws TransformerException, 
                 TransformerConfigurationException, 
                 SAXException, IOException
  {
    TransformerFactory tfactory = TransformerFactory.newInstance();

    // Does this factory support SAX features?
    if (tfactory.getFeature(Features.SAX))
    {
      // If so, we can safely cast.
      SAXTransformerFactory stfactory = ((SAXTransformerFactory) tfactory);
      
      // A TransformerHandler is a ContentHandler that will listen for 
      // SAX events, and transform them to the result.
      TransformerHandler handler 
        = stfactory.newTransformerHandler(new StreamSource(xslID));

      // Set the result handling to be a serialization to System.out.
      Result result = new SAXResult(new ExampleContentHandler());
      handler.setResult(result);
      
      // Create a reader, and set it's content handler to be the TransformerHandler.
      XMLReader reader = XMLReaderFactory.createXMLReader();
      reader.setContentHandler(handler);
      
      // It's a good idea for the parser to send lexical events.
      // The TransformerHandler is also a LexicalHandler.
      reader.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
      
      // Parse the source XML, and send the parse events to the TransformerHandler.
      reader.parse(sourceID);
    }
    else
    {
      System.out.println(
        "Can't do exampleContentHandlerToContentHandler because tfactory is not a SAXTransformerFactory");
    }
  }
  
  /**
   * Show the Transformer as a SAX2 XMLReader.  An XMLFilter obtained 
   * from newXMLFilter should act as a transforming XMLReader if setParent is not
   * called.  Internally, an XMLReader is created as the parent for the XMLFilter.
   */
  public static void exampleXMLReader(String sourceID, String xslID)
          throws TransformerException, TransformerConfigurationException, SAXException, IOException    // , ParserConfigurationException
  {
    TransformerFactory tfactory = TransformerFactory.newInstance();
    if(tfactory.getFeature(Features.SAX))
    {
      XMLReader reader 
        = ((SAXTransformerFactory) tfactory).newXMLFilter(new StreamSource(xslID));
      
      reader.setContentHandler(new ExampleContentHandler());

      reader.parse(new InputSource(sourceID));
    }
    else
      System.out.println("tfactory does not support SAX features!");
  }

  /**
   * Show the Transformer as a simple XMLFilter.  This is pretty similar
   * to exampleXMLReader, except that here the parent XMLReader is created 
   * by the caller, instead of automatically within the XMLFilter.  This 
   * gives the caller more direct control over the parent reader.
   */
  public static void exampleXMLFilter(String sourceID, String xslID)
          throws TransformerException, TransformerConfigurationException, SAXException, IOException    // , ParserConfigurationException
  {
    TransformerFactory tfactory = TransformerFactory.newInstance();

    // The transformer will use a SAX parser as it's reader.    
    XMLReader reader = XMLReaderFactory.createXMLReader();
    reader.setContentHandler(new ExampleContentHandler());
    try
    {
      reader.setFeature("http://xml.org/sax/features/namespace-prefixes",
                        true);
      reader.setFeature("http://apache.org/xml/features/validation/dynamic",
                        true);
    }
    catch (SAXException se)
    {

      // What can we do?
      // TODO: User diagnostics.
    }

    XMLFilter filter 
      = ((SAXTransformerFactory) tfactory).newXMLFilter(new StreamSource(xslID));

    filter.setParent(reader);

    // Now, when you call transformer.parse, it will set itself as 
    // the content handler for the parser object (it's "parent"), and 
    // will then call the parse method on the parser.
    filter.parse(new InputSource(sourceID));
  }

  /**
   * This example shows how to chain events from one Transformer
   * to another transformer, using the Transformer as a
   * SAX2 XMLFilter/XMLReader.
   */
  public static void exampleXMLFilterChain(
                                           String sourceID, String xslID_1, 
                                           String xslID_2, String xslID_3)
    throws TransformerException, TransformerConfigurationException, SAXException, IOException
  {
    TransformerFactory tfactory = TransformerFactory.newInstance();
    
    Templates stylesheet1 = tfactory.newTemplates(new StreamSource(xslID_1));
    Transformer transformer1 = stylesheet1.newTransformer();
    
     // If one success, assume all will succeed.
    if (tfactory.getFeature(Features.SAX))
    {
      SAXTransformerFactory stf = (SAXTransformerFactory)tfactory;
      XMLReader reader = XMLReaderFactory.createXMLReader();

      XMLFilter filter1 = stf.newXMLFilter(new StreamSource(xslID_1));
      XMLFilter filter2 = stf.newXMLFilter(new StreamSource(xslID_2));
      XMLFilter filter3 = stf.newXMLFilter(new StreamSource(xslID_3));

      if (null != filter1) // If one success, assume all were success.
      {
        // transformer1 will use a SAX parser as it's reader.    
        filter1.setParent(reader);

        // transformer2 will use transformer1 as it's reader.
        filter2.setParent(filter1);

        // transform3 will use transform2 as it's reader.
        filter3.setParent(filter2);

        filter3.setContentHandler(new ExampleContentHandler());
        // filter3.setContentHandler(new org.xml.sax.helpers.DefaultHandler());

        // Now, when you call transformer3 to parse, it will set  
        // itself as the ContentHandler for transform2, and 
        // call transform2.parse, which will set itself as the 
        // content handler for transform1, and call transform1.parse, 
        // which will set itself as the content listener for the 
        // SAX parser, and call parser.parse(new InputSource("xml/foo.xml")).
        filter3.parse(new InputSource(sourceID));
      }
      else
      {
        System.out.println(
                           "Can't do exampleXMLFilter because "+
                           "tfactory doesn't support asXMLFilter()");
      }
    }
    else
    {
      System.out.println(
                         "Can't do exampleXMLFilter because "+
                         "tfactory is not a SAXTransformerFactory");
    }
  }

  /**
   * Show how to transform a DOM tree into another DOM tree.
   * This uses the javax.xml.parsers to parse an XML file into a
   * DOM, and create an output DOM.
   */
  public static Node exampleDOM2DOM(String sourceID, String xslID)
    throws TransformerException, TransformerConfigurationException, SAXException, IOException,
    ParserConfigurationException
  {
    TransformerFactory tfactory = TransformerFactory.newInstance();

    if (tfactory.getFeature(Features.DOM))
    {
      Templates templates;

      {
        DocumentBuilderFactory dfactory =
          DocumentBuilderFactory.newInstance();
        dfactory.setNamespaceAware(true);
        DocumentBuilder docBuilder = dfactory.newDocumentBuilder();
        org.w3c.dom.Document outNode = docBuilder.newDocument();
        Node doc = docBuilder.parse(new InputSource(xslID));
 
        DOMSource dsource = new DOMSource(doc);
        // If we don't do this, the transformer won't know how to 
        // resolve relative URLs in the stylesheet.
        dsource.setBaseID(xslID);

        templates = tfactory.newTemplates(dsource);
      }

      Transformer transformer = templates.newTransformer();
      DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = dfactory.newDocumentBuilder();
      org.w3c.dom.Document outNode = docBuilder.newDocument();
      Node doc = docBuilder.parse(new InputSource(sourceID));

      transformer.transform(new DOMSource(doc), new DOMResult(outNode));
      
      Transformer serializer = tfactory.newTransformer();
      serializer.transform(new DOMSource(outNode), new StreamResult(System.out));

      return outNode;
    }
    else
    {
      throw new org.xml.sax
        .SAXNotSupportedException("DOM node processing not supported!");
    }
  } 

  /**
   * This shows how to set a parameter for use by the templates.
   */
  public static void exampleParam(String sourceID, 
                                  String xslID)
    throws TransformerException, TransformerConfigurationException
  {
    TransformerFactory tfactory = TransformerFactory.newInstance();
    Templates templates = tfactory.newTemplates(new StreamSource(xslID));
    Transformer transformer1 = templates.newTransformer();
    Transformer transformer2 = templates.newTransformer();

    transformer1.setParameter("a-param",
                              "hello to you!");
    transformer1.transform(new StreamSource(sourceID),
                           new StreamResult(System.out));
    
    System.out.println("\n=========");
    
    transformer2.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer2.transform(new StreamSource(sourceID),
                           new StreamResult(System.out));
  }

  /**
   * Show how to override output properties.
   */
  public static void exampleOutputProperties(String sourceID, String xslID)
    throws TransformerException, TransformerConfigurationException
  {

    TransformerFactory tfactory = TransformerFactory.newInstance();
    Templates templates = tfactory.newTemplates(new StreamSource(xslID));
    Properties oprops = templates.getOutputProperties();

    oprops.put(OutputKeys.INDENT, "yes");

    Transformer transformer = templates.newTransformer();

    transformer.setOutputProperties(oprops);
    transformer.transform(new StreamSource(sourceID),
                          new StreamResult(System.out));
  }

  /**
   * Show how to get stylesheets that are associated with a given
   * xml document via the xml-stylesheet PI (see http://www.w3.org/TR/xml-stylesheet/).
   */
  public static void exampleUseAssociated(String sourceID)
    throws TransformerException, TransformerConfigurationException
  {
    TransformerFactory tfactory = TransformerFactory.newInstance();

    // The DOM tfactory will have it's own way, based on DOM2, 
    // of getting associated stylesheets.
    if (tfactory instanceof SAXTransformerFactory)
    {
      SAXTransformerFactory stf = ((SAXTransformerFactory) tfactory);
      Source sources =
        stf.getAssociatedStylesheet(new StreamSource(sourceID),
          null, null, null);

      if(null != sources)
      {
        Transformer transformer = tfactory.newTransformer(sources);

        transformer.transform(new StreamSource(sourceID),
                              new StreamResult(System.out));
      }
      else
      {
        System.out.println("Can't find the associated stylesheet!");
      }
    }
  }
  
  /**
   * Show the Transformer using SAX events in and DOM nodes out.
   */
  public static void exampleContentHandler2DOM(String sourceID, String xslID)
          throws TransformerException, TransformerConfigurationException, SAXException, IOException, ParserConfigurationException
  {
    TransformerFactory tfactory = TransformerFactory.newInstance();

    // Make sure the transformer factory we obtained supports both
    // DOM and SAX.
    if (tfactory.getFeature(Features.SAX)
        && tfactory.getFeature(Features.DOM))
    {
      // We can now safely cast to a SAXTransformerFactory.
      SAXTransformerFactory sfactory = (SAXTransformerFactory) tfactory;
      
      // Create an Document node as the root for the output.
      DocumentBuilderFactory dfactory 
        = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = dfactory.newDocumentBuilder();
      org.w3c.dom.Document outNode = docBuilder.newDocument();
      
      // Create a ContentHandler that can liston to SAX events 
      // and transform the output to DOM nodes.
      TransformerHandler handler 
        = sfactory.newTransformerHandler(new StreamSource(xslID));
      handler.setResult(new DOMResult(outNode));
      
      // Create a reader and set it's ContentHandler to be the 
      // transformer.
      XMLReader reader = XMLReaderFactory.createXMLReader();
      reader.setContentHandler(handler);
      reader.setProperty("http://xml.org/sax/properties/lexical-handler",
                         handler);
      
      // Send the SAX events from the parser to the transformer,
      // and thus to the DOM tree.
      reader.parse(sourceID);
      
      // Serialize the node for diagnosis.
      exampleSerializeNode(outNode);
    }
    else
    {
      System.out.println(
        "Can't do exampleContentHandlerToContentHandler because tfactory is not a SAXTransformerFactory");
    }
  }
  
  /**
   * Serialize a node to System.out.
   */
  public static void exampleSerializeNode(Node node)
    throws TransformerException, TransformerConfigurationException, SAXException, IOException,
    ParserConfigurationException
  {
    TransformerFactory tfactory = TransformerFactory.newInstance(); 
    
    // This creates a transformer that does a simple identity transform, 
    // and thus can be used for all intents and purposes as a serializer.
    Transformer serializer = tfactory.newTransformer();
    
    serializer.setOutputProperty(OutputKeys.INDENT, "yes");
    serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    serializer.transform(new DOMSource(node), 
                         new StreamResult(System.out));
  }  
  
  /**
   * A fuller example showing how the TrAX interface can be used 
   * to serialize a DOM tree.
   */
  public static void exampleAsSerializer(String sourceID, String xslID)
    throws TransformerException, TransformerConfigurationException, SAXException, IOException,
    ParserConfigurationException
  {
    DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = dfactory.newDocumentBuilder();
    org.w3c.dom.Document outNode = docBuilder.newDocument();
    Node doc = docBuilder.parse(new InputSource(sourceID));

    TransformerFactory tfactory = TransformerFactory.newInstance(); 
    
    // This creates a transformer that does a simple identity transform, 
    // and thus can be used for all intents and purposes as a serializer.
    Transformer serializer = tfactory.newTransformer();
    
    Properties oprops = new Properties();
    oprops.put("method", "html");
    oprops.put("indent-amount", "2");
    serializer.setOutputProperties(oprops);
    serializer.transform(new DOMSource(doc), 
                         new StreamResult(System.out));
  }
  


}
