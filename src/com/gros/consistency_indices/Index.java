package com.gros.consistency_indices;

import Jama.Matrix;
import com.gros.Eigenvalue;

/**
 * Created by gros on 14.05.17.
 */
public class Index extends Consistency {
    static public double compute(Matrix matrix) {
        int n = matrix.getRowDimension();
        double tmp = (Eigenvalue.getMaxEigenvalue(matrix) - n) / (n - 1);
        if(tmp < 0)
            return -tmp;
        return tmp;
    }
}
