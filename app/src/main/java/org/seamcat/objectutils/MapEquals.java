package org.seamcat.objectutils;

import java.util.Map;

public class MapEquals {

    private DeepEquals deepEquals;
    private Map<?,?> m1;
    private Map<?,?> m2;

    protected MapEquals(DeepEquals deepEquals, Map<?, ?> m1, Map<?, ?> m2) {
        this.deepEquals = deepEquals;
        this.m1 = m1;
        this.m2 = m2;
    }
    
    protected boolean mapEquals() {
        for (Map.Entry<?, ?> entry : m1.entrySet()) {
            if ( !m2.containsKey( entry.getKey() )) {
                return false;
            }
            if ( !deepEquals.deepCompare(entry.getValue(), m2.get(entry.getKey())) ) {
                return false;
            }
        }
        return m1.keySet().size() == m2.keySet().size();
    }

}
