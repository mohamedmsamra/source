package org.seamcat.function;

import junit.framework.Assert;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.seamcat.function.BinarySearch.search;

public class BinarySearchTest {


    @Test
    public void corner_cases() {
        int i = search(null, null);
        Assert.assertEquals( 0, i );

        i = search(asList(1, 2, 3), new BinarySearch.Filter<Integer>() {
            public boolean evaluate(Integer integer, int index) {
                return true;
            }
        });
        // predicate true for all elements so expect max
        Assert.assertEquals( 2, i );

        i = search( asList(1,2,3,4,5,6,7,8,9,10,11,12,13), new BinarySearch.Filter<Integer>() {
            public boolean evaluate(Integer integer, int index) {
                return true;
            }
        });
        Assert.assertEquals( 12, i );

        i = search( asList(1.1,2.2,3.3,3.5), new BinarySearch.Filter<Double>() {
            public boolean evaluate(Double d, int index) {
                return d * d > 100;
            }
        });
        // predicate false for all elements
        Assert.assertEquals( 0, i);

        i = search( asList(1.1,2.2,3.3,3.5,3.6,3.7,3.8,3.9,4.0,4.1,4.2,4.3,4.5,4.6,4.7), new BinarySearch.Filter<Double>() {
            public boolean evaluate(Double d, int index) {
                return d * d > 100;
            }
        });
        // predicate false for all elements
        Assert.assertEquals( 0, i);

    }

    @Test
    public void doubleList() {
        int i = search( asList(1.1, 2.2, 3.3, 3.5, 4.4, 5.0, 6.1, 7.7, 9.1, 10.2, 11.3, 20.1), new BinarySearch.Filter<Double>() {
            public boolean evaluate(Double d, int index) {
                return d * d  < 100;
            }
        });
        Assert.assertEquals( 9, i);
    }

}
