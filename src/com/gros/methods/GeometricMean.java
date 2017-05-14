package com.gros.methods;

import Jama.Matrix;
import com.gros.console.AhpNode;

/**
 * Created by gros on 19.03.17.
 */
public class GeometricMean implements PriorityVector {
    public Matrix getPriorityVector(AhpNode node) {
        Matrix matrix = node.getMatrix();
        int n = matrix.getRowDimension();
        double[] weights = new double[n];
        double[] powers = new double[n];

        double normalizationTerm = 0;
        for(int i=0; i<n; i++) {
            powers[i] = 1;
            for (int j = 0; j < n; j++)
                powers[i] *= matrix.get(i,j);
            powers[i] = Math.pow(powers[i], 1./n);
            normalizationTerm += powers[i];
        }

        for(int i=0; i<n; i++)
            weights[i] = powers[i] / normalizationTerm;
        return new Matrix(weights, 1).transpose();
    }

    public String toString() {return "geometric mean";}
}
