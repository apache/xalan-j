package org.apache.xml.dtm;

import org.apache.xml.dtm.DTMDocumentImpl;
import org.apache.xml.dtm.TestDTMNodes;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Tests the DTM by creating 
 *
 */
public class TestDTM {

	public static void main(String argv[]) {

		/*  <?xml version="1.0"?>
		 *  <top>
		 *   <A>
		 *    <B hat="new" car="Honda" dog="Boxer">Life is good</B>
		 *   </A>
		 *   <C>My Anaconda<D/>Words</C>
		 *  </top> */

		DTMDocumentImpl doc = new DTMDocumentImpl(0);
		doc.createElement("top",  null);
		doc.createElement( "A", null);
		AttributesImpl atts = new AttributesImpl();
		atts.addAttribute("", "", "hat", "CDATA", "new");
		atts.addAttribute("", "", "car", "CDATA", "Honda");
		atts.addAttribute("", "", "dog", "CDATA", "Boxer");
		doc.createElement("B", atts);
		doc.createTextNode("Life is good");
		doc.endElement("", "B");
		doc.endElement("", "A");
		doc.createElement("C", null);
		doc.createTextNode("My Anaconda");
		doc.createElement("D", null);
		doc.endElement("", "D");
		doc.createTextNode("Words");
		doc.endElement("", "C");
		doc.endElement("", "top");
		doc.documentEnd();

		boolean BUILDPURCHASEORDER=false;
		if(BUILDPURCHASEORDER)
		  {
		    int root, h, c1, c2, c3, c4, c1_text, c2_text, c3_text, c4_text;

		    root = doc.createElement("PurchaseOrderList", null);
		    // root.createAttribute("version", "1.1"));

		    for (int i = 0; i < 10; i++) {

		      h = doc.createElement("PurchaseOrder", null);

		      c1 = doc.createElement("Item", null);
		      // c1.createAttribute();
		      c1_text = doc.createTextNode("Basketball" + " - " + i);
		      doc.endElement(null, "Item");

		      c2 = doc.createElement("Description", null);
		      // c2.createAttribute();
		      c2_text = doc.createTextNode("Professional Leather Michael Jordan Signatured Basketball");
		      doc.endElement(null, "Description");

		      c3 = doc.createElement("UnitPrice", null);
		      c3_text = doc.createTextNode("$12.99");
		      doc.endElement(null, "UnitPrice");

		      c4 = doc.createElement("Quanity", null);
		      c4_text = doc.createTextNode("50");
		      doc.endElement(null, "Quanity");

		      doc.endElement(null, "PurchaseOrder");
		    }

		    doc.endElement(null, "PurchaseOrderList");
		  } // if(BUILDPURCHASEORDER)
		
		

		TestDTMNodes.printNodeTable(doc);
	}
}
