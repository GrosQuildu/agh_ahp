package com.gros.console;

import Jama.Matrix;

import com.gros.methods.Eigenvector;
import com.gros.methods.PriorityVector;
import org.json.JSONException;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;

/**
 * Created by gros on 19.03.17.
 */
public class AhpTree {
    public AhpNode goal;
    public ArrayList<String> alternatives;
    private PriorityVector method;
    public static final List<String> implementedMethods = Arrays.asList("eigenvector", "geometric mean");

    public AhpTree(AhpNode root, ArrayList<String> alternatives, String method) {
        this(root, alternatives);
        setMethod(method);
    }

    public AhpTree(AhpNode root, ArrayList<String> alternatives) {
        this.goal = root;
        this.alternatives = alternatives;
    }

    private static String makeClassName(String method) {
        String[] arr = method.split(" ");
        StringBuilder sb = new StringBuilder();

        for (String anArr : arr) {
            sb.append(Character.toUpperCase(anArr.charAt(0)))
                    .append(anArr.substring(1)).append(" ");
        }
        return sb.toString().trim().replaceAll("\\s+", "");
    }

    void setMethod(String method) {
        try {
            this.method = AhpTree.createMethod(method);
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
            System.out.println("Setting eigenvector method");
            this.method = new Eigenvector();
        }
    }

    static public PriorityVector createMethod(String method) throws ClassNotFoundException {
        if(implementedMethods.contains(method)) {
            String methodClass = "com.gros.methods." + makeClassName(method);
            try {
                Object newMethod = Class.forName(methodClass).newInstance();
                return (PriorityVector)newMethod;
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                throw new ClassNotFoundException("Method " + methodClass + " not implemented");
            }
        } else {
            throw new ClassNotFoundException("Method " + method + " not implemented");
        }
    }

    public static AhpTree fromXml(String path, String method) throws Exception {
        File inputFile = new File(path);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputFile);
        doc.getDocumentElement().normalize();

        Element goalElement = doc.getDocumentElement();
        ArrayList<String> alternativesList = new ArrayList<>();
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
        return new AhpTree(parseXml(goalElement), alternativesList, method);
    }

    private static AhpNode parseXml(Element root) throws Exception {
        AhpNode ahpRoot = null;
        ArrayList<AhpNode> ahpList = new ArrayList<>();
        Node childNode = root.getFirstChild();

        while(childNode.getNextSibling() != null){
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) childNode;
                if("matrix".equals(childElement.getNodeName())) {
                    ahpRoot = new AhpNode(root.getAttribute("name"), childElement.getTextContent());
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

    public static Map<String, Double> sortByValue(Map<String, Double> unsortMap) {

        List<Map.Entry<String, Double>> list =
                new LinkedList<>(unsortMap.entrySet());

        list.sort((o1, o2) -> -(o1.getValue()).compareTo(o2.getValue()));

        Map<String, Double> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    void printResult() throws Exception {
        Matrix resultWeights = this.goal.getWeightsVector(this.method);
        if(resultWeights.getRowDimension() != this.alternatives.size())
            throw new Exception("Number of alternatives not equals weights vector dimension");

        Map<String, Double> result = new HashMap<>();
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
