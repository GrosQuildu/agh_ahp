package com.gros.consistency_indices;

import Jama.Matrix;

import java.util.Random;

/**
 * Created by gros on 14.05.17.
 */
public class Ratio extends Consistency {
    static public double compute(Matrix matrix) {
        return Index.compute(matrix) / getRandomIndex(matrix.getRowDimension());
    }

    static private double getRandomIndex(int n) {
        //CONSISTENCY IN THE ANALYTIC HIERARCHY PROCESS: A NEW APPROACH JOSÉ ANTONIO ALONSO
        if(n > 2 && n < 16) {
            double[] uppuluri = {1, 1, 1, 0.5799, 0.8921, 1.1159, 1.2358, 1.3322, 1.3952, 1.4537, 1.4882, 1.5117, 1.5356, 1.5571, 1.5714, 1.5831};
            return uppuluri[n];
        } else
            return generateRandomIndex(n);
    }

    static private double generateRandomIndex(int n) {
        Random rand = new Random();
        int tries = 1000;
        double result = 0;

        for(int x=0; x < tries; x++) {
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
            result += Index.compute(tmp);
        }
        return result / tries;
    }
}
