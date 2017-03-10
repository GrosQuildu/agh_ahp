package com.gros;

import java.util.ArrayList;
import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

/**
 * Created by gros on 10.03.17.
 */
public class Main {
    public static void main(String[] args) {
        String path = "./resources/sample_matrices.xml";
        try {
            AhpNode goal = parseXml(path);
            System.out.println(goal);
            //System.out.println(goal.toXml());

            goal.getWeightsVector().print(5,5);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static AhpNode parseXmlNode(Element root) throws Exception {
        AhpNode ahpRoot = null;
        ArrayList<AhpNode> ahpList = new ArrayList<AhpNode>();
        Node childNode = root.getFirstChild();

        while(childNode.getNextSibling() != null){
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) childNode;
                if("matrix".equals(childElement.getNodeName())) {
                    ahpRoot = new AhpNode(root.getAttribute("name"), childElement.getTextContent());
                } else if("attribute".equals(childElement.getNodeName())) {
                    ahpList.add(parseXmlNode(childElement));
                }
            }
            childNode = childNode.getNextSibling();
        }
        if(ahpRoot == null)
            throw new Exception("Goal matrix not found");

        ahpRoot.addChild(ahpList);
        return ahpRoot;
    }

    private static AhpNode parseXml(String path) throws Exception {
        File inputFile = new File(path);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputFile);
        doc.getDocumentElement().normalize();

        Element goalElement = doc.getDocumentElement();
        return parseXmlNode(goalElement);
    }
}
