package com.gros.gui;

import Jama.Matrix;
import com.gros.console.AhpNode;

import java.util.ArrayList;

/**
 * Created by gros on 08.04.17.
 * Extends AhpNode
 */
public class AhpNodeGraphic extends AhpNode {
    boolean isRoot = false;
    boolean isAlternative = false;
    AhpTreeGraphic tree;
    AhpNodeGraphic parent;

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
    }

    @Override
    public String toString() {
        return this.name;
    }

    ArrayList<AhpNode> getChilds() {
        if(this.list.size() == 0)
            return tree.getAlternativesRoot().list;
        return this.list;
    }

    void setMatrix() {
        int size = this.list.size();
        if(size == 0 && this.tree != null && tree.getAlternativesRoot() != null)
            size = tree.getAlternativesRoot().list.size();
        if(size == 0)
            return;
        if(this.matrix == null || this.matrix.getRowDimension() != size) {
            this.matrix = new Matrix(size,size,1);
        }
    }
}
