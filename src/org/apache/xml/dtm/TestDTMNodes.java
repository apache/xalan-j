package org.apache.xml.dtm;

import org.apache.xml.dtm.ChunkedIntArray;

public class TestDTMNodes {

	public static void printNodeTable(DTMDocumentImpl doc) {
		int length = doc.nodes.slotsUsed(), slot[] = new int[4];
		for (int i=0; i <= length; i++) {
			doc.nodes.readSlot(i, slot);
			short high = (short) (slot[0] >> 16), low = (short) (slot[0] & 0xFFFF);
			System.out.println(i + ": (" + high + ") (" + low + ") " + slot[1] + 
												 " " + slot[2] + " " +slot[3] + " Node Name: " + 
												 doc.getNodeName(i) + " Node Value: " + doc.getNodeValue(i)); 
		}

	}
}
