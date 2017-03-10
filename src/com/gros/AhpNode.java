package com.gros;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

import java.util.ArrayList;
import java.util.stream.DoubleStream;

import org.json.*;


/**
 * Created by gros on 10.03.17.
 */
public class AhpNode {
    private String name;
    private Matrix matrix;
    private ArrayList<AhpNode> list;

    AhpNode(String name, String matrix) throws JSONException {
        JSONArray matrixJson = new JSONArray(matrix);
        this.matrix = new Matrix(matrixJson.length(), matrixJson.length());
        double[][] matrixArray = this.matrix.getArray();
        for(int i = 0; i < matrixArray.length; i++)
            for (int j = 0; j < matrixArray[i].length; j++)
                matrixArray[i][j] = matrixJson.getJSONArray(i).getDouble(j);

        this.name = name;
        this.list = new ArrayList<AhpNode>();
    }

    AhpNode(String name, Matrix matrix) {
        this(name, matrix, new ArrayList<AhpNode>());
    }

    AhpNode(String name, Matrix matrix, ArrayList<AhpNode> list) {
        this.name = name;
        this.matrix = matrix;
        this.list = list;
    }

    void addChild(AhpNode node) {
        this.list.add(node);
    }

    void addChild(ArrayList<AhpNode> list) {
        this.list.addAll(list);
    }

    Matrix getWeightsVector() {
        Matrix weights = matrixToVectorEigenvalues(this.matrix).copy();
        if(this.list.size() == 0)
            return weights;

        Matrix finallWeights = this.list.get(0).getWeightsVector().times(weights.get(0, 0));
        for(int i = 1; i < this.list.size(); i++) {
            Matrix tmp = this.list.get(i).getWeightsVector().times(weights.get(i, 0));
            finallWeights.plusEquals(tmp);  // W += w'_i * w_j
        }
        return finallWeights;
    }

    private static Matrix matrixToVectorEigenvalues(Matrix matrix) {
        EigenvalueDecomposition eigen = matrix.eig();
        Matrix eigenvalues = eigen.getD();
        Matrix eigenvectors = eigen.getV();

        int nonZeroEigenvalueIndex = 0;
        for(; nonZeroEigenvalueIndex < eigenvalues.getRowDimension(); nonZeroEigenvalueIndex++)
            if(eigenvalues.get(nonZeroEigenvalueIndex, nonZeroEigenvalueIndex) != 0)
                break;

//        eigen.getD().print(2, 8);
//        eigen.getV().print(2, 8);

        Matrix goodEigenvector = eigenvectors.getMatrix(0, matrix.getColumnDimension()-1, nonZeroEigenvalueIndex, nonZeroEigenvalueIndex).transpose();
        double[] goodEigenvectorAsArray = goodEigenvector.getArray()[0];
        double sum = DoubleStream.of(goodEigenvectorAsArray).sum();

        for(int i = 0; i < goodEigenvectorAsArray.length; i++)
            goodEigenvectorAsArray[i] /= sum;
        return goodEigenvector.transpose();
    }

    private String toString(String before) {
        String result = before + this.name + "\n";
        for(int i = 0; i<this.matrix.getRowDimension(); i++) {
            result += before;
            for (int j = 0; j < this.matrix.getColumnDimension(); j++)
                result += String.format("%8.3f", this.matrix.get(i, j)) + " ";
            result += "\n";
        }
        result += before + "--------------------" + "\n";
        for(AhpNode node : this.list)
            result += node.toString(before + "  ");
        return result;
    }

    public String toString() {
        return this.toString("");
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
