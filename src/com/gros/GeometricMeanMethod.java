package com.gros;

import Jama.Matrix;

/**
 * Created by gros on 19.03.17.
 */
public class GeometricMeanMethod implements PriorityVectorMethod{
    public Matrix getPriorityVector(AhpNode node) {
        return node.getMatrix();
    }
}
