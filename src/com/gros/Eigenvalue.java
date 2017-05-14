package com.gros;

import Jama.Matrix;

/**
 * Created by gros on 14.05.17.
 */
public class Eigenvalue {
    static public int getMaxEigenvalueIndex(Matrix matrix) {
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

    static public double getMaxEigenvalue(Matrix matrix) {
        return matrix.eig().getRealEigenvalues()[getMaxEigenvalueIndex(matrix)];
    }

    static public Matrix getMaxEigenvector(Matrix matrix) {
        int maxEigenvalueIndex = getMaxEigenvalueIndex(matrix);
        return matrix.eig().getV().getMatrix(0, matrix.getColumnDimension()-1, maxEigenvalueIndex, maxEigenvalueIndex).transpose();
    }
}
