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
                 *    <B/>
                 *   </A>
                 *   <C/>
                 *  </top> */


                DTMDocumentImpl doc = new DTMDocumentImpl();
                doc.createElement("top",  null);
                doc.createElement( "A", null);
                AttributesImpl atts = new AttributesImpl();
                /*atts.addAttribute("", "", "hat", "CDATA", "new");
                atts.addAttribute("", "", "car", "CDATA", "Honda");
                atts.addAttribute("", "", "dog", "CDATA", "Boxer");*/
                doc.createElement("B", atts);
                doc.elementEnd("", "B");
                doc.elementEnd("", "A");
                doc.createElement("C", null);
                doc.elementEnd("", "C");
                doc.elementEnd("", "top");
                doc.documentEnd();
                TestDTMNodes.printNodeTable(doc);
        }
}
