package org.seamcat.util;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;


public class XmlUtils {
	
	public static Document parse(File file) {
		DocumentBuilder db;
      try {
	      db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	      return db.parse(file);
      } catch (Exception e) {
      	throw new RuntimeException(e);
      }
	}

	public static Document parse(InputStream inputStream) {
		DocumentBuilder db;
      try {
	      db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	      return db.parse(inputStream);
      } catch (Exception e) {
      	throw new RuntimeException(e);
      }
	}

	public static void write(Document document, File file) {
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(file);
			write(document, outputStream);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			IOUtils.closeQuietly(outputStream);
		}
   }

	public static void write(Document document, OutputStream outputStream) {
		try {
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(new BufferedOutputStream(outputStream));
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transformer = transFactory.newTransformer();
			transformer.transform(source, result);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Document createDocument() {
		try {
	      return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      } catch (ParserConfigurationException e) {
      	throw new RuntimeException(e);
      }
	}

	public static void renameElement( Element orig, Document document, String newName ) {
		Element elm = document.createElement(newName);

        NamedNodeMap map = orig.getAttributes();
        for ( int i=0; i<map.getLength(); i++) {
            Node node = map.item(i);
            elm.setAttribute(node.getNodeName(), node.getNodeValue());
        }

        for ( int i=0; i<orig.getChildNodes().getLength(); i++ ) {
			elm.appendChild(orig.getChildNodes().item(i));
		}

		Node parentNode = orig.getParentNode();
		parentNode.removeChild( orig );
		parentNode.appendChild( elm );
	}

}
