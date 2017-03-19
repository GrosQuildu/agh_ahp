package com.gros;

import Jama.Matrix;
import jdk.internal.org.xml.sax.SAXException;
import org.json.JSONException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by gros on 19.03.17.
 */
public class AhpTree {
    AhpNode goal;
    ArrayList<String> alternatives;

    AhpTree(AhpNode root, ArrayList<String> alternatives) {
        this.goal = root;
        this.alternatives = alternatives;
    }

    static AhpTree fromXml(String path) throws Exception {
        File inputFile = new File(path);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputFile);
        doc.getDocumentElement().normalize();

        Element goalElement = doc.getDocumentElement();
        ArrayList<String> alternativesList = new ArrayList<String>();
        Node childNode = goalElement.getFirstChild();

        while(childNode.getNextSibling() != null) {
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) childNode;
                if("alternatives".equals(childElement.getNodeName())) {
                    NodeList alternatives = childElement.getChildNodes();
                    for(int i=0; i < alternatives.getLength(); i++)
                        if (alternatives.item(i).getNodeType() == Node.ELEMENT_NODE)
                            alternativesList.add(alternatives.item(i).getTextContent());
                    break;
                }
            }
            childNode = childNode.getNextSibling();
        }
        return new AhpTree(parseXml(goalElement), alternativesList);
    }

    private static AhpNode parseXml(Element root) throws Exception {
        AhpNode ahpRoot = null;
        ArrayList<AhpNode> ahpList = new ArrayList<AhpNode>();
        Node childNode = root.getFirstChild();

        while(childNode.getNextSibling() != null){
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) childNode;
                if("matrix".equals(childElement.getNodeName())) {
                    ahpRoot = new AhpNode.Builder(root.getAttribute("name"), childElement.getTextContent()).build();
                } else if("attribute".equals(childElement.getNodeName())) {
                    ahpList.add(parseXml(childElement));
                }
            }
            childNode = childNode.getNextSibling();
        }
        if(ahpRoot == null)
            throw new Exception("Goal matrix not found");

        ahpRoot.addChild(ahpList);
        return ahpRoot;
    }

    private static Map<String, Double> sortByValue(Map<String, Double> unsortMap) {

        List<Map.Entry<String, Double>> list =
                new LinkedList<Map.Entry<String, Double>>(unsortMap.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            public int compare(Map.Entry<String, Double> o1,
                               Map.Entry<String, Double> o2) {
                return -(o1.getValue()).compareTo(o2.getValue());
            }
        });

        Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, Double> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    public void printResult() throws Exception {
        Matrix resultWeights = this.goal.getWeightsVector();
        if(resultWeights.getRowDimension() != this.alternatives.size())
            throw new Exception("Number of alternatives not equals weights vector dimension");

        Map<String, Double> result = new HashMap<String, Double>();
        for(int i=0; i < this.alternatives.size(); i++)
            result.put(alternatives.get(i), resultWeights.get(i, 0));
        result = sortByValue(result);

        System.out.println("Result:");
        for (Map.Entry<String, Double> entry : result.entrySet())
            System.out.printf("%s: %.4f\n", entry.getKey(), entry.getValue());
        System.out.println("");
    }


    public String toXml() throws JSONException {
        String xmlCriterions = this.goal.toXml();
        String[] xmlCriterionsSplited = xmlCriterions.split("\n", 2);
        String result = xmlCriterionsSplited[0];
        result += "\n<alternatives>\n";
        for(String alternative : this.alternatives)
            result += "<alternative>" + alternative + "</alternative>\n";
        result += "</alternatives>\n";
        result += xmlCriterionsSplited[1];
        return result;
    }

    public String toString() {
        String result = "Alternatives:\n";
        for(String alternative : this.alternatives)
            result += alternative + ", ";
        result += "\n";
        return result + this.goal.toString();
    }
}
