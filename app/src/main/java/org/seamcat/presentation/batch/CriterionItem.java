package org.seamcat.presentation.batch;

public class CriterionItem {

    private int criterion;

    public CriterionItem(int criterion ) {
        this.criterion = criterion;
    }

    public int getCriterion() {
        return criterion;
    }

    @Override
    public String toString() {
        if ( criterion == 1 ) return "C/I";
        else if (criterion == 2) return "C/(I + N)";
        else if (criterion == 3) return "(N + I)/N";
        else return "I/N";
    }

    @Override
    public boolean equals(Object obj) {
        if ( obj == null || !(obj instanceof CriterionItem))return false;
        CriterionItem other = (CriterionItem)obj;
        return other.criterion  == criterion;
    }
}
