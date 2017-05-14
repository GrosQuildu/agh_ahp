package com.gros.console;

import Jama.Matrix;

import java.util.ArrayList;
import java.util.Random;

import com.gros.methods.GeometricMean;
import com.gros.methods.PriorityVector;
import org.json.*;


/***
 * Created by gros on 10.03.17.
 **/
public class AhpNode {
    public String name;
    public Matrix matrix;
    public ArrayList<AhpNode> list;

    private int maxEigenvalueIndex;
    private double maxEigenvalue;
    private Matrix maxEigenvector;

    public AhpNode() {
        this.name = "New Node";
        this.list = new ArrayList<AhpNode>();
    }

    public AhpNode(String name) {
        this.name = name;
        this.list = new ArrayList<AhpNode>();
    }

    public AhpNode(String name, String matrix) throws JSONException {
        JSONArray matrixJson = new JSONArray(matrix);
        double[][] matrixArray = new double[matrixJson.length()][matrixJson.length()];
        for(int i = 0; i < matrixArray.length; i++)
            for (int j = 0; j < matrixArray[i].length; j++)
                matrixArray[i][j] = matrixJson.getJSONArray(i).getDouble(j);
        this.matrix = new Matrix(matrixArray);
        this.name = name;
        AhpNodeInit();
    }

    public AhpNode(String name, Matrix matrix) {
        this.matrix = matrix;
        this.name = name;
        AhpNodeInit();
    }

    public AhpNode list(ArrayList<AhpNode> list) {
        this.list = list;
        return this;
    }

    private void AhpNodeInit() {
        this.updateEigen();
        this.list = new ArrayList<AhpNode>();
    }

    public void updateEigen() {
        int maxEigenvalueIndex = getMaxEigenvalueIndex(this.matrix);
        double maxEigenvalue = this.matrix.eig().getRealEigenvalues()[maxEigenvalueIndex];
        Matrix maxEigenvector = this.matrix.eig().getV().getMatrix(0, this.matrix.getColumnDimension()-1, maxEigenvalueIndex, maxEigenvalueIndex).transpose();
        this.maxEigenvalue = maxEigenvalue;
        this.maxEigenvector = maxEigenvector;
        this.maxEigenvalueIndex = maxEigenvalueIndex;
    }


    /** getters **/
    public Matrix getMatrix() { return this.matrix; }
    public int getMaxEigenvalueIndex() { return this.maxEigenvalueIndex; }
    public double getMaxEigenvalue() { return this.maxEigenvalue; }
    public Matrix getMaxEigenvector() { return this.maxEigenvector; }


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
        Matrix finallWeights = stmp.times(weights.get(0, 0));
        for(int i = 1; i < this.list.size(); i++) {
            Matrix tmp = this.list.get(i).getWeightsVector(method).times(weights.get(i, 0));
            finallWeights.plusEquals(tmp);  // W += w'_i * w_j
        }
        return finallWeights;
    }
    public Matrix getWeightsVector(PriorityVector method) {return getWeightsVector(this.matrix, method);}


    /** Consistency indexes **/
    public double consistencyIndex() {return consistencyIndex(this.matrix, this.maxEigenvalue);}
    public double consistencyRatio() {return consistencyRatio(this.matrix, this.maxEigenvalue);}
    public double consistencyIndexOfDeterminants() {return consistencyIndexOfDeterminants(this.matrix);}
    public double consistencyGeometricIndex() {return consistencyGeometricIndex(this.matrix, new GeometricMean().getPriorityVector(this));}
    public double consistencyHarmonicIndex() {return consistencyHarmonicIndex(this.matrix);}
    public double consistencyAmbiguityIndex() {return consistencyAmbiguityIndex(this.matrix);}


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
//        result += String.format("AmbiguityIndex: %.4f | ", consistencyAmbiguityIndex());
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


    /** STATIC **/
    /** Random Index **/
    static double getRandomIndex(int n) {
        //CONSISTENCY IN THE ANALYTIC HIERARCHY PROCESS: A NEW APPROACH JOSÉ ANTONIO ALONSO
        if(n > 2 && n < 16) {
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

    /** Consistency Index/Ratio **/
    static double consistencyRatio(Matrix matrix, double maxEigenvalue) {
        return consistencyIndex(matrix, maxEigenvalue) / getRandomIndex(matrix.getRowDimension());
    }

    static double consistencyIndex(Matrix matrix, double maxEigenvalue) {
        int n = matrix.getRowDimension();
        double tmp = (maxEigenvalue - n) / (n - 1);
        if(tmp < 0)
            return -tmp;
        return tmp;
    }

    public static int factorial(int n) {
        int fact = 1; // this  will be the result
        for (int i = 1; i <= n; i++) {
            fact *= i;
        }
        return fact;
    }

    static double consistencyIndexOfDeterminants(Matrix matrix) {
        int n = matrix.getRowDimension();
        double result = 0;
        int denom = n*(n-1)*(n-2)/6;

        for(int i=0; i<n-2; i++)
            for(int j=i+1; j<n-1; j++)
                for(int k=j+1; k<n; k++) {
                    double tmp = ((matrix.get(i, k) / (matrix.get(i, j) * matrix.get(j, k)))
                            + ((matrix.get(i, j) * matrix.get(j, k)) / matrix.get(i, k))
                            - 2);
                    result += tmp;
                }
        return result / denom;
    }

    static double consistencyGeometricIndex(Matrix matrix, Matrix weights) {
        int n = matrix.getRowDimension();
        double result = 0;
        for(int i=0; i<n-1; i++)
            for(int j=i+1; j<n; j++)
                result += Math.pow(Math.log(matrix.get(i,j) * (weights.get(j,0)/weights.get(i,0))), 2);
        return (2./((n-1)*(n-2))) * result;
    }

    static double consistencyHarmonicIndex(Matrix matrix) {
        int n = matrix.getRowDimension();
        double sum;

        ArrayList<Double> s = new ArrayList<Double>();
        for(int j=0; j<n; j++) {
            sum = 0;
            for (int i = 0; i < n; i++)
                sum += matrix.get(i,j);
            s.add(sum);
        }

        sum = 0;
        for(int j=0; j<n; j++)
            sum += 1./s.get(j);
        double hm = n/sum;

        return ((hm - n)*(n+1)) / (n*(n-1));
    }

    static double consistencyAmbiguityIndex(Matrix matrix) {
        int n = matrix.getRowDimension();
        return 0;
    }


    /** Eigenvalues **/
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
