package org.apache.xml.dtm.dom2dtm;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import java.io.StringReader;

import org.apache.xml.dtm.*;

/**
 * Simple unit test for DOM2DTM.
 */
public class UnitTest
{

  public UnitTest()
  {
  }
  
  static String s_doc1String = 
              "<?xml version=\"1.0\"?>" +
              "<far-north>" +
              " <north>" +
              "  <near-north>" +
              "   <far-west/>" +
              "   <west/>" +
              "   <near-west near-west-attr1='near-west-attr1-value'/>" +
              "   <center>" +
              "    <near-south>" +
              "     <south south-attr1='south-attr1-value' south-attr2='south-attr2-value'>" +
              "      <far-south/>" +
              "     </south>" +
              "    </near-south>" +
              "   </center>" +
              "   <near-east/>" +
              "   <east/>" +
              "   <far-east/>" +
              "  </near-north>" +
              " </north>" +
              "</far-north>";
  
  protected int run(String[] args)
    throws Exception
  {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    DocumentBuilder db = dbf.newDocumentBuilder();
    StringReader sr = new StringReader(s_doc1String);
    
    Document doc = db.parse(new InputSource(sr));
    
    DTMManager dtmMgr = DTMManager.newInstance();
    DTM dtm = dtmMgr.getDTM(new DOMSource(doc), true);
    
    int docHandle = dtm.getDocument();
    outputChildren(docHandle, dtm, 0);

//    TransformerFactory tf = TransformerFactory.newInstance();
//    Transformer t = tf.newTransformer();
//    t.setOutputProperty(OutputKeys.INDENT, "yes");
//    t.transform(new DOMSource(doc), new StreamResult(System.out));
    
    System.out.println("DOM2DTM Unit test done!");
    
    return 0;
  }
  
  protected void outputChildren(int handle, DTM dtm, int indentAmount)
  {
    for (handle = dtm.getFirstChild(handle); handle != DTM.NULL; 
         handle = dtm.getNextSibling(handle)) 
    {
      for (int i = 0; i < indentAmount; i++) 
      {
        System.out.print(' ');
      }
      
      System.out.println("node name: "+dtm.getNodeName(handle));
      outputChildren(handle, dtm, 0 /* indentAmount+2 */);
    }
  }
  
  public static void main(String[] args)
    throws Exception
  {
    UnitTest unitTest1 = new UnitTest();
    unitTest1.run(args);
  }
}