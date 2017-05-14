package com.gros.gui;

import Jama.Matrix;

import com.gros.console.AhpNode;
import com.gros.console.AhpTree;
import com.gros.methods.Eigenvector;
import com.gros.methods.PriorityVector;

import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import org.json.JSONException;

import java.awt.GridLayout;
import java.awt.Toolkit;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * Created by gros on 08.04.17.
 *
 */
class AhpTreeGraphic extends JPanel {
    private DefaultMutableTreeNode rootNode;
    private AhpTreeModel treeModel;
    private JTree tree;
    private Toolkit toolkit = Toolkit.getDefaultToolkit();
    private PriorityVector method;
    double requirement;

    AhpTreeGraphic() {
        super(new GridLayout(1, 0));
        try {
            this.method = AhpTree.createMethod("eigenvector");
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        this.requirement = 0.1;

        rootNode = new DefaultMutableTreeNode(new AhpNodeGraphic("AHP"));
        treeModel = new AhpTreeModel(rootNode);

        tree = new JTree(treeModel);
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);
        tree.setEditable(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(tree);
        add(scrollPane);
    }

    AhpNodeGraphic getCriterionsRoot() {
        DefaultMutableTreeNode criterions = (DefaultMutableTreeNode)rootNode.getChildAt(0);
        return (AhpNodeGraphic)criterions.getUserObject();
    }

    AhpNodeGraphic getAlternativesRoot() {
        if(rootNode.getChildCount() < 2)
            return null;
        DefaultMutableTreeNode alternatives = (DefaultMutableTreeNode)rootNode.getChildAt(1);
        return (AhpNodeGraphic)alternatives.getUserObject();
    }

    ArrayList<AhpNode> getLeafs(AhpNode root) {
        ArrayList<AhpNode> leafs = new ArrayList<>();
        if(root.list.size() == 0) {
            leafs.add(root);
            return leafs;
        }
        for(AhpNode child : root.list) {
            leafs.addAll(getLeafs(child));
        }
        return leafs;
    }

    ArrayList<AhpNode> getLeafs() {
        DefaultMutableTreeNode goal = (DefaultMutableTreeNode)rootNode.getChildAt(0);
        AhpNodeGraphic ahpGoal = (AhpNodeGraphic)goal.getUserObject();
        return getLeafs(ahpGoal);
    }

    DefaultMutableTreeNode getRootNode() { return rootNode; }

    PriorityVector getMethod() { return method; }

    private void updateLeafs() {
        int size = getAlternativesRoot().getChilds().size();
        for(AhpNode nodeTmp : getLeafs()) {
            nodeTmp.matrix = new Matrix(size, size, 1);
            nodeTmp.updateEigen();
        }
    }

    /** Save XML **/
    void save(String path) {
        String error = null;
        if(!path.endsWith(".xml"))
            path += ".xml";
        try {
            PrintWriter writer = new PrintWriter(path, "UTF-8");
            ArrayList<String> alternatives = new ArrayList<>();
            AhpNodeGraphic alternativeRoot = getAlternativesRoot();
            for(AhpNode alt : alternativeRoot.getChilds()) {
                alternatives.add(alt.name);
            }
            AhpNode goal = getCriterionsRoot();
            AhpTree ahpTree = new AhpTree(goal, alternatives);
            writer.print(ahpTree.toXml());
            writer.close();
        } catch (FileNotFoundException e) {
            error = "Can't save";
        } catch (UnsupportedEncodingException e) {
            error = "Encoding not supported";
        } catch (JSONException e) {
            error = "JSON exception";
        }
        if(error != null) {
            JOptionPane.showMessageDialog(null,
                    ""+error,
                    "Error saving XML",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Load XML **/
    void load(String path) {
        try {
            AhpTree parsedTree = AhpTree.fromXml(path, "eigenvalue");
            clear();

            AhpNodeGraphic goal = getCriterionsRoot();
            goal.matrix = parsedTree.goal.matrix;
            goal.updateEigen();

            for(AhpNode child : parsedTree.goal.list)
                loadSubtree((DefaultMutableTreeNode)rootNode.getChildAt(0), child);

            DefaultMutableTreeNode alternatives = (DefaultMutableTreeNode)rootNode.getChildAt(1);
            for(String alt : parsedTree.alternatives) {
                addObject(alternatives, new AhpNodeGraphic(alt, true), false);
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("Error loading XML");
        }
    }

    private void loadSubtree(DefaultMutableTreeNode parent, AhpNode root) {
        AhpNodeGraphic newNode = new AhpNodeGraphic(root);
        DefaultMutableTreeNode newNodeTree = addObject(parent, newNode, false);
        for(AhpNode child : root.list) {
            loadSubtree(newNodeTree, child);
        }
    }

    /** Parse **/
    void parse() {
        javax.swing.SwingUtilities.invokeLater(() -> new AhpParse(getCriterionsRoot(), method));
    }

    /** Edit node **/
    void edit(JFrame mainFrame) {
        TreePath currentSelection = tree.getSelectionPath();
        if (currentSelection != null) {
            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) (currentSelection.getLastPathComponent());
            AhpNodeGraphic ahpCurrentNode = (AhpNodeGraphic)currentNode.getUserObject();
            if(!ahpCurrentNode.isAlternative) {
                mainFrame.setEnabled(false);
                javax.swing.SwingUtilities.invokeLater(() -> new AhpEditNodeGraphic(ahpCurrentNode, mainFrame));
            }
            else
                System.out.println("can't edit it");
            return;
        }
        toolkit.beep();
    }

    /** Remove all nodes except the root nodes. */
    void clear() {
        Enumeration childs = rootNode.children();
        while(childs.hasMoreElements()) {
            DefaultMutableTreeNode subrootNode = (DefaultMutableTreeNode)childs.nextElement();
            AhpNodeGraphic ahpSubrootNode = (AhpNodeGraphic)subrootNode.getUserObject();
            ahpSubrootNode.clear();
            subrootNode.removeAllChildren();
        }
        treeModel.reload();
    }

    /** Change method for computing weights vectors **/
    void changeMethod() {
        String[] choices = AhpTree.implementedMethods.toArray(new String[AhpTree.implementedMethods.size()]);
        int currentPosition = AhpTree.implementedMethods.indexOf(this.method.toString());
        if(currentPosition == -1)
            currentPosition = 0;
        String methodNew = (String) JOptionPane.showInputDialog(null, "Methods:",
                "How to compute weight vectors", JOptionPane.QUESTION_MESSAGE, null,
                choices,
                choices[currentPosition]);
        try {
            this.method = AhpTree.createMethod(methodNew);
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
            this.method = new Eigenvector();
        }
    }

    void changeRequirement() {
        String reqString = (String) JOptionPane.showInputDialog(null, "Requirement",
                "Minimum consistency requirement:", JOptionPane.QUESTION_MESSAGE, null, null, this.requirement);
        if(reqString == null)
            return;
        try {
            double tmp;
            tmp = Double.parseDouble(reqString);
            if(tmp <= 0 || tmp > 1)
                throw new ValueException("");
            this.requirement = tmp;
        } catch (NumberFormatException | ValueException e) {
            JOptionPane.showMessageDialog(null,
                    "Wrong consistency requirement, must be in (0,1]",
                    "Requirement error",
                    JOptionPane.ERROR_MESSAGE);
            changeRequirement();
        }
    }

    /** Remove the currently selected node. */
    void removeCurrentNode() {
        TreePath currentSelection = tree.getSelectionPath();
        if (currentSelection != null) {
            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) (currentSelection.getLastPathComponent());
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) (currentNode.getParent());
            if (parent != null) {
                AhpNodeGraphic ahpCurrentNode = (AhpNodeGraphic)currentNode.getUserObject();

                //do not delete roots
                if(ahpCurrentNode.isRoot)
                    return;

                //update leafs if deleted alternative
                if(ahpCurrentNode.isAlternative)
                    updateLeafs();

                AhpNodeGraphic ahpParentNode = (AhpNodeGraphic)parent.getUserObject();
                ahpParentNode.removeChild(ahpCurrentNode);
                treeModel.removeNodeFromParent(currentNode);
                return;
            }
        }

        // Either there was no selection, or the root was selected.
        toolkit.beep();
    }

    /** Add child to the currently selected node. */
    DefaultMutableTreeNode addObject(Object child) {
        DefaultMutableTreeNode parentNode;
        TreePath parentPath = tree.getSelectionPath();

        if (parentPath == null) {
            parentNode = rootNode;
        } else {
            parentNode = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
        }

        return addObject(parentNode, child, true);
    }

    DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
                                            Object child) {
        return addObject(parent, child, true);
    }

    DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
                                            Object child, boolean setMatrix) {
        DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);

        if (parent == null)
            parent = rootNode;

        //only alternatives and criterions are roots
        if(parent == rootNode && parent.getChildCount() >= 2)
            return null;

        //one level of alternatives
        if(parent.getParent() != null) {
            DefaultMutableTreeNode grandpa = (DefaultMutableTreeNode)parent.getParent();
            AhpNodeGraphic ahpGrandpa = (AhpNodeGraphic)grandpa.getUserObject();
            if(ahpGrandpa.isRoot && "Alternatives".equals(ahpGrandpa.name))
                return null;
        }

        treeModel.insertNodeInto(childNode, parent, parent.getChildCount());

        AhpNodeGraphic ahpParent = (AhpNodeGraphic)parent.getUserObject();
        AhpNodeGraphic ahpChild = (AhpNodeGraphic)child;
        ahpChild.tree = this;
        ahpChild.parent = ahpParent;
        ahpParent.addChild(ahpChild);

        if(setMatrix) {
            if (ahpParent.isAlternative) {
                ahpChild.isAlternative = true;
                updateLeafs();
            } else {
                ahpParent.setMatrix();
                ahpChild.setMatrix();
            }
        }


        tree.scrollPathToVisible(new TreePath(childNode.getPath()));
        return childNode;
    }
}