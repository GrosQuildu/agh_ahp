package com.gros.methods;

import Jama.Matrix;
import com.gros.console.AhpNode;

/**
 * Created by gros on 19.03.17.
 */
public interface PriorityVector {
    Matrix getPriorityVector(AhpNode node);
    String toString();
}
