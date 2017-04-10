package com.gros;

import Jama.Matrix;
import org.json.JSONException;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * Created by gros on 08.04.17.
 *
 */
class AhpTreeGraphic extends JPanel {
    protected DefaultMutableTreeNode rootNode;
    protected AhpTreeModel treeModel;
    protected JTree tree;
    private Toolkit toolkit = Toolkit.getDefaultToolkit();
    public PriorityVectorMethod method;
    double req;

    public AhpTreeGraphic() {
        super(new GridLayout(1, 0));
        setMethod("eigenvector");
        this.req = 0.1;

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

    public AhpNodeGraphic getCriterionsRoot() {
        DefaultMutableTreeNode criterions = (DefaultMutableTreeNode)rootNode.getChildAt(0);
        return (AhpNodeGraphic)criterions.getUserObject();
    }

    public AhpNodeGraphic getAlternativesRoot() {
        if(rootNode.getChildCount() < 2)
            return null;
        DefaultMutableTreeNode alternatives = (DefaultMutableTreeNode)rootNode.getChildAt(1);
        return (AhpNodeGraphic)alternatives.getUserObject();
    }

    private ArrayList<AhpNode> getLeafs(AhpNode root) {
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

    public ArrayList<AhpNode> getLeafs() {
        ArrayList<AhpNodeGraphic> leafs = new ArrayList<>();
        DefaultMutableTreeNode goal = (DefaultMutableTreeNode)rootNode.getChildAt(0);
        AhpNodeGraphic ahpGoal = (AhpNodeGraphic)goal.getUserObject();
        return getLeafs(ahpGoal);
    }

    private void updateLeafs() {
        int size = getAlternativesRoot().getChilds().size();
        for(AhpNode nodeTmp : getLeafs()) {
            nodeTmp.matrix = new Matrix(size, size, 1);
            nodeTmp.updateEigen();
        }
    }

    /** Save XML **/
    public void save(String path) {
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
            AhpNode goal = (AhpNode)getCriterionsRoot();
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
    public void load(String path) {
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
                addObject(alternatives, new AhpNodeGraphic(alt, true), true, false);
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("Error loading XML");
        }
    }

    private void loadSubtree(DefaultMutableTreeNode parent, AhpNode root) {
        AhpNodeGraphic newNode = new AhpNodeGraphic(root);
        DefaultMutableTreeNode newNodeTree = addObject(parent, newNode, true, false);
        for(AhpNode child : root.list) {
            loadSubtree(newNodeTree, child);
        }
    }

    /** Parse **/
    public void parse() {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                AhpParse parseFrame = new AhpParse(getCriterionsRoot(), method);
            }
        });
    }

    /** Edit node **/
    public void edit(JFrame mainFrame) {
        TreePath currentSelection = tree.getSelectionPath();
        if (currentSelection != null) {
            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) (currentSelection.getLastPathComponent());
            AhpNodeGraphic ahpCurrentNode = (AhpNodeGraphic)currentNode.getUserObject();
            if(!ahpCurrentNode.isAlternative) {
                this.getParent().setEnabled(false);
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        AhpEditNodeGraphic editFrame = new AhpEditNodeGraphic(ahpCurrentNode, mainFrame);
                    }
                });
            }
            else
                System.out.println("can't edit it");
            return;
        }
        toolkit.beep();
    }

    /** Remove all nodes except the root nodes. */
    public void clear() {
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
    public void changeMethod() {
        String[] choices = { "eigenvector", "geometric mean" };
        String methodNew = (String) JOptionPane.showInputDialog(null, "Methods:",
                "How to compute weight vectors", JOptionPane.QUESTION_MESSAGE, null,
                choices,
                choices[0]);
        setMethod(methodNew);
    }

    private void setMethod(String method) {
        if("eigenvector".equals(method))
            this.method = new EigenvectorMethod();
        else
            this.method = new GeometricMeanMethod();
    }

    public void changeReq() {
        String reqString = (String) JOptionPane.showInputDialog(null, "Requirement",
                "Minimum consistency requirement:", JOptionPane.QUESTION_MESSAGE, null, null, this.req);
        double tmp = 0.1;
        try {
            tmp = Double.parseDouble(reqString);
        } catch (Exception w) {}
        this.req = tmp;
    }

    /** Remove the currently selected node. */
    public void removeCurrentNode() {
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
    public DefaultMutableTreeNode addObject(Object child) {
        DefaultMutableTreeNode parentNode = null;
        TreePath parentPath = tree.getSelectionPath();

        if (parentPath == null) {
            parentNode = rootNode;
        } else {
            parentNode = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
        }

        return addObject(parentNode, child, true, true);
    }

    public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
                                            Object child) {
        return addObject(parent, child, true, true);
    }

    public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
                                            Object child, boolean shouldBeVisible, boolean setMatrix) {
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

        // Make sure the user can see the lovely new node.
        if (shouldBeVisible) {
            tree.scrollPathToVisible(new TreePath(childNode.getPath()));
        }
        return childNode;
    }
}