package com.gros;

import Jama.Matrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.json.*;


/**
 * Created by gros on 10.03.17.
 */
public class AhpNode {
    private final String name;
    private final Matrix matrix;
    private final ArrayList<AhpNode> list;
    public PriorityVectorMethod method;

    private int maxEigenvalueIndex;
    private double maxEigenvalue;
    private Matrix maxEigenvector;

    private AhpNode(Builder builder) {
        this.name = builder.name;
        this.matrix = builder.matrix;
        this.list = builder.list;
        this.method = builder.method;

        this.maxEigenvalueIndex = builder.maxEigenvalueIndex;
        this.maxEigenvalue = builder.maxEigenvalue;
        this.maxEigenvector = builder.maxEigenvector;
    }

    public static class Builder {
        private final String name;
        private final Matrix matrix;

        private PriorityVectorMethod method;
        private ArrayList<AhpNode> list;
        private int maxEigenvalueIndex;
        private double maxEigenvalue;
        private Matrix maxEigenvector;

        public Builder(String name, String matrix) throws JSONException {
            JSONArray matrixJson = new JSONArray(matrix);
            double[][] matrixArray = new double[matrixJson.length()][matrixJson.length()];
            for(int i = 0; i < matrixArray.length; i++)
                for (int j = 0; j < matrixArray[i].length; j++)
                    matrixArray[i][j] = matrixJson.getJSONArray(i).getDouble(j);
            this.matrix = new Matrix(matrixArray);
            this.name = name;
            AhpNodeInit();
        }

        public Builder(String name, Matrix matrix) {
            this.matrix = matrix;
            this.name = name;
            AhpNodeInit();
        }

        private void AhpNodeInit() {
            int maxEigenvalueIndex = getMaxEigenvalueIndex(this.matrix);
            double maxEigenvalue = this.matrix.eig().getRealEigenvalues()[maxEigenvalueIndex];
            Matrix maxEigenvector = this.matrix.eig().getV().getMatrix(0, this.matrix.getColumnDimension()-1, maxEigenvalueIndex, maxEigenvalueIndex).transpose();
            this.maxEigenvalue = maxEigenvalue;
            this.maxEigenvector = maxEigenvector;
            this.maxEigenvalueIndex = maxEigenvalueIndex;
            this.method = new EigenvectorMethod();
            this.list = new ArrayList<AhpNode>();
        }

        public Builder list(ArrayList<AhpNode> list) {
            this.list = list;
            return this;
        }

        public Builder method(String method) {
            if("eigenvector".equals(method))
                this.method = new EigenvectorMethod();
            else
                this.method = new GeometricMeanMethod();
            return this;
        }

        public AhpNode build() {
            return new AhpNode(this);
        }
    }

    Matrix getMatrix() { return this.matrix; }
    int getMaxEigenvalueIndex() { return this.maxEigenvalueIndex; }
    double getMaxEigenvalue() { return this.maxEigenvalue; }
    Matrix getMaxEigenvector() { return this.maxEigenvector; }

    /* expand hierarchy tree */
    void addChild(AhpNode node) {
        this.list.add(node);
    }
    void addChild(ArrayList<AhpNode> list) {
        this.list.addAll(list);
    }

    /* Compute priority/weight vectors */
    Matrix getWeightsVector(Matrix matrix, PriorityVectorMethod method) {
        Matrix weights = method.getPriorityVector(this);
        if(this.list.size() == 0)
            return weights;

        Matrix finallWeights = this.list.get(0).getWeightsVector(matrix, method).times(weights.get(0, 0));
        for(int i = 1; i < this.list.size(); i++) {
            Matrix tmp = this.list.get(i).getWeightsVector(matrix, method).times(weights.get(i, 0));
            finallWeights.plusEquals(tmp);  // W += w'_i * w_j
        }
        return finallWeights;
    }
    Matrix getWeightsVector() {
        return getWeightsVector(this.matrix, new EigenvectorMethod());
    }

    /* Consistency indexes */
    double consistencyIndex() {
        return consistencyIndex(this.matrix, this.maxEigenvalue);
    }
    double consistencyRatio() {
        return consistencyRatio(this.matrix, this.maxEigenvalue);
    }

    /* convert to printable */
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


    /* STATIC */
    /* Random Index */
    static double getRandomIndex(int n) {
        //CONSISTENCY IN THE ANALYTIC HIERARCHY PROCESS: A NEW APPROACH JOSÉ ANTONIO ALONSO
        if(n < 2 && n < 16) {
            double[] uppuluri = {1, 1, 1, 0.5799, 0.8921, 1.1159, 1.2358, 1.3322, 1.3952, 1.4537, 1.4882, 1.5117, 1.5356, 1.5571, 1.5714, 1.5831};
            return uppuluri[n];
        } else
            return generateRandomIndex(n);
    }

    static double generateRandomIndex(int n) {
        Random rand = new Random();
        int tries = 1000;
        double result = 0;

        for(int x=0; x<tries; x++) {
            Matrix tmp = new Matrix(n, n, 1);
            double[][] tmpArray = tmp.getArray();
            for(int i = 0; i<tmpArray.length; i++)
                for(int j = 0; j<tmpArray.length; j++) {
                    int randVal = rand.nextInt(17);  // 1/9, 1/8, 1/7, ..., 1/2, 1, 2 ,...,8, 9
                    if(randVal < 8)
                        tmpArray[i][j] = 1./(randVal+2);
                    else
                        tmpArray[i][j] = (randVal-7);
                }
            result += consistencyIndex(tmp, AhpNode.getMaxEigenvalue(tmp));
        }
        return result / tries;
    }

    /* Consistency Index/Ratio */
    static double consistencyRatio(Matrix matrix, double maxEigenvalue) {
        return consistencyIndex(matrix, maxEigenvalue) / getRandomIndex(matrix.getRowDimension());
    }

    static double consistencyIndex(Matrix matrix, double maxEigenvalue) {
        int n = matrix.getRowDimension();
//        System.out.println(maxEigenvalue);
        return (maxEigenvalue - n) / (n - 1);
    }

    static int getMaxEigenvalueIndex(Matrix matrix) {
        double[] eigenvalues = matrix.eig().getRealEigenvalues();
        int maxEigenvalueIndex = 0;
        double maxEigenvalue = 0;
        for(int i = 0; i < eigenvalues.length; i++) {
            if (eigenvalues[i] != 0 && eigenvalues[i] > maxEigenvalue) {
                maxEigenvalue = eigenvalues[i];
                maxEigenvalueIndex = i;
            }
        }
        return maxEigenvalueIndex;
    }

    static double getMaxEigenvalue(Matrix matrix) {
        return matrix.eig().getRealEigenvalues()[getMaxEigenvalueIndex(matrix)];
    }
}
