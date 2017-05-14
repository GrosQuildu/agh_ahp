package com.gros.methods;

import Jama.Matrix;
import com.gros.Eigenvalue;
import com.gros.console.AhpNode;

/**
 * Created by gros on 19.03.17.
 */
public class Eigenvector implements PriorityVector {
    public Matrix getPriorityVector(AhpNode node) {
        Matrix maxEigenvector = Eigenvalue.getMaxEigenvector(node.getMatrix());
        double sum = 0;
        for(double x : maxEigenvector.getArray()[0])
            sum += x;
        return maxEigenvector.times(1./sum).transpose();
    }

    public String toString() {return "eigenvector";}
}
