package org.seamcat.function;

import java.util.Arrays;
import java.util.List;

/**
 * For doing binary searches through sorted lists using filter function
 */
public class BinarySearch<T> {

    private List<T> sortedList;
    private Filter<T> filter;
    private BinarySearch( List<T> sortedList, Filter<T> filter) {
        this.sortedList = sortedList;
        this.filter = filter;
    }

    /**
     * Find the lowest index where filter is false.
     * <p></p>
     * If true for all elements return highest element
     */
    public static <T> int search( List<T> sortedList, Filter<T> filter) {
        if ( filter == null || sortedList == null || sortedList.isEmpty() ) return 0;
        int highestIndex = sortedList.size()-1;
        if ( sortedList.size() < 10 ) {
            // such small lists is faster searched linear
            int i;
            for (i = 0; i<sortedList.size() && filter.evaluate( sortedList.get(i), i); i++) {}
            return Math.min( i, highestIndex );
        }
        return Math.min( new BinarySearch<T>( sortedList, filter).search( 0, highestIndex ), highestIndex );
    }

    public static <T> int searchArray( T[] sortedArray, Filter<T> filter) {
        if ( sortedArray == null || sortedArray.length == 0 ) return 0;
        return search( Arrays.asList(sortedArray), filter);
    }

    private <T> int search( int min, int max ) {
        if ( !filter.evaluate( sortedList.get(min ), min)) return min;
        if ( min == max ) {
            return min+1;
        }
        if ( min+1 == max ) {
            if ( filter.evaluate( sortedList.get(max), max)) return max+1;
            return min+1;
        }
        int middle = (min + max) / 2;
        if ( filter.evaluate( sortedList.get( middle), middle ) ) {
            return search( middle, max );
        } else {
            return search( min, middle );
        }
    }

    public interface Filter<T> {
        boolean evaluate(T t, int index);
    }
}
