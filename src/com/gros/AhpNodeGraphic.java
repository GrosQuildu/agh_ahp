package com.gros;

import Jama.Matrix;

import javax.swing.tree.MutableTreeNode;
import java.util.ArrayList;

/**
 * Created by gros on 08.04.17.
 * Extends AhpNode
 */
public class AhpNodeGraphic extends AhpNode {
    public boolean isRoot = false;
    public boolean isAlternative = false;
    public AhpTreeGraphic tree;
    public AhpNodeGraphic parent;

    AhpNodeGraphic(String name) {
        super(name);
    }

    AhpNodeGraphic(String name, boolean isAlternative) {
        super(name);
        this.isAlternative = isAlternative;
    }

    AhpNodeGraphic(AhpNode node) {
        this.name = node.name;
        this.matrix = node.matrix;
        this.list = new ArrayList<>();
        this.updateEigen();
    }

    @Override
    public String toString() {
        return this.name;
    }

    public ArrayList<AhpNode> getChilds() {
        if(this.list.size() == 0)
            return tree.getAlternativesRoot().list;
        return this.list;
    }

    public void setMatrix() {
        int size = this.list.size();
        if(size == 0 && this.tree != null && tree.getAlternativesRoot() != null)
            size = tree.getAlternativesRoot().list.size();
        if(size == 0)
            return;
        if(this.matrix == null || this.matrix.getRowDimension() != size) {
            this.matrix = new Matrix(size,size,1);
        }
        this.updateEigen();
    }
}
