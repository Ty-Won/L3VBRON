package guigeneration;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException; 
import gui.MainWindow;

import java.io.File;
import java.io.IOException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class XMLReader {
	
	public static EV3GeneratedPanel createEV3Panel(String file, int port, MainWindow mw){
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		Document doc;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		try {
			File xmlLayout = new File(file);
			if (!xmlLayout.exists()) {
				System.out.println("Error: XML layout file " + xmlLayout.getCanonicalPath() + " does not exist.");
				System.exit(-9);
			}
			else if (!xmlLayout.canRead()) {
				System.out.println("Error: XML layout file " + xmlLayout.getCanonicalPath() + " is not readable.");
				System.exit(-10);
			}
			doc = db.parse(xmlLayout);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		EV3GeneratedPanel EV3Panel = new EV3GeneratedPanel(mw, doc, port);
		return EV3Panel;
	}

	//TODO use in createEV3Panel method to clean code.
	public static Document getContentsDoc(String file){
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		Document doc;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		try {
			doc = db.parse(new File(file));
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return doc;
	}
	
	public static Node getNextNonTextNode(Node node, boolean returnText){
		Node temp1, temp2;
		temp1 = node;
		while (temp1.getNodeName() == "#text"){
			temp2 = node.getNextSibling();
			if (temp2 != null){
				temp1 = temp2;
			} else {
				if (returnText){
					return temp1;
				} else {
					return null;
				}
			}
		}
		return temp1;
	}
}
