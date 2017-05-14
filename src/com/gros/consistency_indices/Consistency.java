package com.gros.consistency_indices;

import Jama.Matrix;
import com.gros.console.AhpNode;
import com.gros.methods.PriorityVector;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by gros on 14.05.17.
 */
abstract class Consistency {
    static double compute(Matrix matrix, AhpNode node, PriorityVector method) throws NotImplementedException{
        throw new NotImplementedException();
    }
    static double compute(Matrix matrix) throws NotImplementedException {
        throw new NotImplementedException();
    }
}
