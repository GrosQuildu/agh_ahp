package com.gros.console;

import Jama.Matrix;

import java.util.ArrayList;

import com.gros.consistency_indices.*;
import com.gros.methods.*;
import org.json.*;


/***
 * Created by gros on 10.03.17.
 **/
public class AhpNode {
    public String name;
    public Matrix matrix;
    public ArrayList<AhpNode> list;

    public AhpNode() {
        this.name = "New Node";
        this.list = new ArrayList<>();
    }

    public AhpNode(String name) {
        this.name = name;
        this.list = new ArrayList<>();
    }

    public AhpNode(String name, String matrix) throws JSONException {
        JSONArray matrixJson = new JSONArray(matrix);
        double[][] matrixArray = new double[matrixJson.length()][matrixJson.length()];
        for(int i = 0; i < matrixArray.length; i++)
            for (int j = 0; j < matrixArray[i].length; j++)
                matrixArray[i][j] = matrixJson.getJSONArray(i).getDouble(j);
        this.matrix = new Matrix(matrixArray);
        this.name = name;
        this.list = new ArrayList<>();
    }

    public AhpNode(String name, Matrix matrix) {
        this.matrix = matrix;
        this.name = name;
        this.list = new ArrayList<>();
    }


    public Matrix getMatrix() { return this.matrix; }

    /*** change hierarchy tree ***/
    public void addChild(AhpNode node) {
        this.list.add(node);
    }
    public void addChild(ArrayList<AhpNode> list) {
        this.list.addAll(list);
    }

    public void removeChild(AhpNode node) { this.list.remove(node); }
    public void clear() { this.list.clear(); }
    

    /** Compute priority/weight vectors **/
    public Matrix getWeightsVector(Matrix matrix, PriorityVector method) {
        Matrix weights = method.getPriorityVector(this);
        if(this.list.size() == 0)
            return weights;

        Matrix stmp = this.list.get(0).getWeightsVector(method);
        Matrix finalWeights = stmp.times(weights.get(0, 0));
        for(int i = 1; i < this.list.size(); i++) {
            Matrix tmp = this.list.get(i).getWeightsVector(method).times(weights.get(i, 0));
            finalWeights.plusEquals(tmp);  // W += w'_i * w_j
        }
        return finalWeights;
    }
    public Matrix getWeightsVector(PriorityVector method) {return getWeightsVector(this.matrix, method);}


    /** Consistency indexes **/
    public double consistencyIndex() {return Index.compute(this.matrix);}
    public double consistencyRatio() {return Ratio.compute(this.matrix);}
    public double consistencyIndexOfDeterminants() {return IndexOfDeterminants.compute(this.matrix);}
    public double consistencyGeometricIndex() {return GeometricIndex.compute(this.matrix, this, new GeometricMean());}
    public double consistencyHarmonicIndex() {return HarmonicIndex.compute(this.matrix);}


    /** convert to printable **/
    String getResult(String before) {
        String result = before + this.name + "\n";

        if(this.matrix != null) {
            result += before + "consistency: ";
            result += String.format("Index: %.4f | ", consistencyIndex());
            result += String.format("Ratio: %.4f | ", consistencyRatio());
            result += String.format("IndexOfDeterminants: %.4f | ", consistencyIndexOfDeterminants());
            result += String.format("GeometricIndex: %.4f | ", consistencyGeometricIndex());
            result += String.format("HarmonicIndex: %.4f | ", consistencyHarmonicIndex());
            result += "\n";

            for (int i = 0; i < this.matrix.getRowDimension(); i++) {
                result += before;
                for (int j = 0; j < this.matrix.getColumnDimension(); j++)
                    result += String.format("%8.3f", this.matrix.get(i, j)) + " ";
                result += "\n";
            }
        }

        result += before + "--------------------" + "\n";
        for(AhpNode node : this.list)
            result += node.getResult(before + "  ");
        return result;
    }

    public String toString() {
        return this.getResult("");
    }

    private String toXml(String before) throws JSONException {
        String result = before + "<attribute name=\"" + this.name + "\">\n";
        result += before + before + "<matrix>" + new JSONArray(this.matrix.getArray()) + "</matrix>\n";
        for(AhpNode node : this.list)
            result += node.toXml(before + "  ");
        result += before + "</attribute>\n";
        return result;
    }

    public String toXml() throws JSONException {
        return this.toXml("");
    }
}
