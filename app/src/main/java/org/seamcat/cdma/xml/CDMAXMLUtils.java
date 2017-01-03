package org.seamcat.cdma.xml;

import org.apache.log4j.Logger;
import org.seamcat.cdma.CDMALinkLevelData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CDMAXMLUtils {

	private static final Logger LOG = Logger.getLogger(CDMAXMLUtils.class);

	public static Element createCDMALibraryElement(Document doc,
	      Collection<CDMALinkLevelData> data) {
		Element element = doc.createElement("cdmalld");
		for (CDMALinkLevelData lld : data) {
			element.appendChild(lld.toElement(doc));
		}
		return element;
	}

	public static List<CDMALinkLevelData> getDataFromLibrary(Element e) {

		List<CDMALinkLevelData> datas = new ArrayList<CDMALinkLevelData>();
		NodeList lldNodes = e.getElementsByTagName("cdmalld");
		Element lld = null;
		// If cdmalld is root element it will only match the local name test
		if (lldNodes.getLength() > 0) {
			lld = (Element) lldNodes.item(0);
		} else if (e.getLocalName().equals("cdmalld")) {
			lld = e;
		}
		if (lld != null) {
			NodeList links = lld.getElementsByTagName("CDMA-Link-level-data");

			for (int i = 0, stop = links.getLength(); i < stop; i++) {
				datas.add(new CDMALinkLevelData((Element) links.item(i)));
			}
		}

		return datas;
	}
}
