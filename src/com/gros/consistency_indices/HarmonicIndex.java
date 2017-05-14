package com.gros.consistency_indices;

import Jama.Matrix;
import java.util.ArrayList;

/**
 * Created by gros on 14.05.17.
 */
public class HarmonicIndex extends Consistency{
    static public double compute(Matrix matrix) {
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
}
