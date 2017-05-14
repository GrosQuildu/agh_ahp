package com.gros.consistency_indices;

import Jama.Matrix;
import com.gros.console.AhpNode;
import com.gros.methods.PriorityVector;

/**
 * Created by gros on 14.05.17.
 */
public class GeometricIndex extends Consistency {
    static public double compute(Matrix matrix, AhpNode node, PriorityVector method){
        Matrix weights = method.getPriorityVector(node);
        int n = matrix.getRowDimension();
        if(n < 2)
            return Float.NaN;

        double result = 0;
        for(int i=0; i<n-1; i++)
            for(int j=i+1; j<n; j++)
                result += Math.pow(Math.log(matrix.get(i,j) * (weights.get(j,0)/weights.get(i,0))), 2);
        System.out.println(result);
        return (2./((n-1)*(n-2))) * result;
    }
}
