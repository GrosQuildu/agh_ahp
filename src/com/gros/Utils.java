package com.gros;

import Jama.Matrix;

import java.util.Random;

/**
 * Created by gros on 19.03.17.
 */
public class Utils {
    static AhpNode randomMatrices(int alternatives, int criterionsDeep, int maxCriterions, int inconsistency) {
        if(alternatives < 2)
            return null;
        Random rand = new Random();
        if(criterionsDeep == 0) {
            return new AhpNode("C"+criterionsDeep, randomMatrix(alternatives, inconsistency));
        } else {
            int criterions = rand.nextInt(maxCriterions)+2;
            AhpNode root = new AhpNode("C"+criterionsDeep, randomMatrix(criterions, inconsistency));
            for (int i = 0; i < criterions; i++)
                root.addChild(randomMatrices(alternatives, criterionsDeep - 1, maxCriterions, inconsistency));
            return root;
        }
    }

    static Matrix randomMatrix(int size, int inconsistency) {
        Matrix weights = Matrix.random(size, 1);
        double sum = 0;
        for(double x : weights.getArray()[0])
            sum += x;
        weights.timesEquals(1./sum);

        Matrix matrix = new Matrix(size, size);
        double[][] tmp = matrix.getArray();
        Random rand = new Random();
        for(int i = 0; i<size; i++)
            for(int j = 0; j<size; j++)
                if(inconsistency == 0)
                    tmp[i][j] = weights.get(i, 0) / weights.get(j, 0);
                else
                    tmp[i][j] = ((rand.nextDouble()/inconsistency)+1) * (weights.get(i, 0) / weights.get(j, 0));
        return matrix;
    }
}
