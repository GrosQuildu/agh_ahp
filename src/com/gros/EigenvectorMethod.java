package com.gros;

import Jama.Matrix;

/**
 * Created by gros on 19.03.17.
 */
public class EigenvectorMethod implements PriorityVectorMethod {
    public Matrix getPriorityVector(AhpNode node) {
        Matrix maxEigenvector = node.getMaxEigenvector();
        double sum = 0;
        for(double x : maxEigenvector.getArray()[0])
            sum += x;
        return maxEigenvector.times(1./sum).transpose();
    }
}
