package com.gros.consistency_indices;

import Jama.Matrix;
import com.gros.console.AhpNode;

/**
 * Created by gros on 14.05.17.
 */
public class IndexOfDeterminants extends Consistency{
    static public double compute(Matrix matrix) {
        int n = matrix.getRowDimension();
        double result = 0;

        if(n < 2)
            return Float.NaN;
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
}
