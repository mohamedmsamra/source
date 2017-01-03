package org.seamcat.objectutils;

import java.lang.reflect.Array;

public class ArrayEquals {

    private boolean result;
    private DeepEquals deepEquals;
    private int length;
    private Object a;
    private Object b;

    protected ArrayEquals(DeepEquals deepEquals, Object a, Object b) {
        this.deepEquals = deepEquals;
        this.a = a;
        this.b = b;
        length = Array.getLength(a);
        result = length == Array.getLength(b) && iterablesEquals();
    }
    
    private boolean iterablesEquals() {
        for ( int i=0; i<length; i++) {
            if ( !deepEquals.deepCompare(Array.get(a, i), Array.get(b, i)) ) {
                return false;
            }
        }
        return true;
    }

    protected boolean equals() {
        return result;
    }
}
