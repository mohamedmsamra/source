package org.seamcat.objectutils;

import java.util.Iterator;

public class IterablesEquals {

    private Iterator<?> i;
    private Iterator<?> j;
    private boolean equalSize;
    private boolean result;
    private DeepEquals deepEquals;

    protected IterablesEquals(DeepEquals deepEquals, Iterator<?> i, Iterator<?> j) {
        this.deepEquals = deepEquals;
        this.i =i;
        this.j = j;
        equalSize = true;
        result = iterablesEquals();
    }
    
    private boolean iterablesEquals() {
        while ( hasNext() ) {
            if ( !nextEquals() ) {
                return false;
            }
        }
        return equalSize;
    }

    private boolean hasNext() {
        boolean hasNext = i.hasNext() && j.hasNext();
        if ( !hasNext ) {
            equalSize = !(i.hasNext() || j.hasNext());
        }
        return hasNext;
    }
    
    private boolean nextEquals() {
        return deepEquals.deepCompare(i.next(), j.next());
    }

    protected boolean equals() {
        return result;
    }
}
