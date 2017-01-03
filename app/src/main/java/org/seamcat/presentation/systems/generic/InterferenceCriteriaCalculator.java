package org.seamcat.presentation.systems.generic;

import org.seamcat.model.generic.InterferenceCriteria;

import static org.seamcat.model.mathematics.Mathematics.linear2dB;

public class InterferenceCriteriaCalculator {

    /**
     * STG-40 contribution of calculation of maximum 5 consistent sets
     * based on the current input values. The calculated results
     * are cases 5-9 of the document attached to ticket #995
     *
     * @param c_i protection ratio
     * @param c_n_i extended protection ratio
     * @param n_i_n noise augmentation
     * @param i_n interference to noise ratio
     *
     * @return array containing 5 calculated consistent sets
     */
    public static Double[][] calculate( double c_i, double c_n_i, double n_i_n, double i_n) {
        Double[] c_i_results   = new Double[5];
        Double[] c_n_i_results = new Double[5];
        Double[] n_i_n_results = new Double[5];
        Double[] i_n_results   = new Double[5];

        // case 5:
        // c_i, c_n_i given, precondition: c_i > c_n_i
        if ( c_i > c_n_i ) {
            double factor = 0.1* (c_i - c_n_i);
            c_i_results[0]   = c_i;
            c_n_i_results[0] = c_n_i;
            n_i_n_results[0] = twoDecimals(linear2dB(1 + 1 / (Math.pow(10, factor) - 1)));
            i_n_results[0]   = twoDecimals(-linear2dB(Math.pow(10, factor) - 1));
        }

        // case 6:
        // c_i, i_n given, no restrictions
        {
            double factor = 0.1 * i_n;
            c_i_results[1]   = c_i;
            i_n_results[1]   = i_n;
            c_n_i_results[1] = twoDecimals(c_i + linear2dB(1 / (1 + Math.pow(10, -factor))));
            n_i_n_results[1] = twoDecimals(linear2dB(1 + Math.pow(10, factor)));
        }

        // case 7:
        // c_i, n_i_n given, precondition: n_i_n > 0
        if ( n_i_n > 0 ) {
            c_i_results[2]   = c_i;
            n_i_n_results[2] = n_i_n;
            double i_n_value = linear2dB(Math.pow(10, 0.1 * n_i_n) - 1);
            i_n_results[2]   = twoDecimals(i_n_value);
            c_n_i_results[2] = twoDecimals(c_i + linear2dB(1 / (1 + Math.pow(10, -0.1 * i_n_value))));
        }

        // case 8:
        // c_n_i, i_n given, no restrictions
        {
            double factor = 0.1 * i_n;
            c_n_i_results[3] = c_n_i;
            i_n_results[3]   = i_n;
            c_i_results[3]   = twoDecimals(c_n_i - linear2dB(1 / (1 + Math.pow(10, -factor))));
            n_i_n_results[3] = twoDecimals(linear2dB(1 + Math.pow(10, factor)));
        }

        // case 9:
        // c_n_i, n_i_n given, no restrictions
        {
            c_n_i_results[4] = c_n_i;
            n_i_n_results[4] = n_i_n;
            double i_n_value = linear2dB(Math.pow(10, 0.1 * n_i_n) - 1);
            i_n_results[4]   = twoDecimals(i_n_value);
            c_i_results[4]   = twoDecimals(c_n_i - linear2dB(1 / (1 + Math.pow(10, -0.1 * i_n_value))));
        }

        Double[][] result = new Double[4][5];
        result[0] = c_i_results;
        result[1] = c_n_i_results;
        result[2] = n_i_n_results;
        result[3] = i_n_results;

        return result;
    }

    public static Double[][] calculate( boolean wsConsistent, double target, InterferenceCriteria criteria) {
        Double[][] result = calculate(criteria.protection_ratio(), criteria.extended_protection_ratio(), criteria.noise_augmentation(), criteria.interference_to_noise_ratio());

        if ( wsConsistent ) {
            for (int i = 0; i < result[1].length; i++) {
                Double c_n_i_result = result[1][i];
                if (!doubleEquals(c_n_i_result, target)) {
                    clear(result, i);
                }
            }
        }

        ifEqualsRemove(result, 0, 1);
        ifEqualsRemove(result, 0, 2);
        ifEqualsRemove(result, 0, 3);
        ifEqualsRemove(result, 0, 4);
        ifEqualsRemove(result, 1, 2);
        ifEqualsRemove(result, 1, 3);
        ifEqualsRemove(result, 1, 4);
        ifEqualsRemove(result, 2, 3);
        ifEqualsRemove(result, 2, 4);
        ifEqualsRemove(result, 3, 4);

        // remove the ones similar to the inputs
        for ( int i=0; i<5; i++) {
            if ( equals(result, criteria.protection_ratio(), criteria.extended_protection_ratio(), criteria.noise_augmentation(), criteria.interference_to_noise_ratio(), i)) {
                clear(result, i);
            }
        }
        // remove inconsistent sets:
        for ( int i=0; i<5; i++) {
            if ( result[0][i] == null ) continue;
            if ( !isConsistent(result[0][i], result[1][i], result[2][i], result[3][i])) {
                clear(result, i);
            }
        }

        return result;
    }

    public static boolean isConsistent( double c_i, double c_n_i, double n_i_n, double i_n ) {
        Double[][] result = calculate(c_i, c_n_i, n_i_n, i_n);

        // see if input is among solutions
        for ( int i=0; i<5; i++) {
            if ( equals(result, c_i, c_n_i, n_i_n, i_n, i)) {
                return true;
            }
        }
        return false;
    }

    private static void clear(Double[][] result, int row ){
        result[0][row] = null;
        result[1][row] = null;
        result[2][row] = null;
        result[3][row] = null;
    }

    private static void ifEqualsRemove(Double[][] result, int result1, int result2) {
        if ( equals(result, result[0][result1], result[1][result1], result[2][result1], result[3][result1], result2) ) {
            clear(result, result2);
        }
    }

    private static boolean equals(Double[][] result, Double c_i, Double c_n_i, Double n_i_n, Double i_n, int row2) {
        return  doubleEquals( c_i, result[0][row2]) &&
                doubleEquals( c_n_i, result[1][row2]) &&
                doubleEquals( n_i_n, result[2][row2]) &&
                doubleEquals( i_n, result[3][row2]);
    }

    private static double twoDecimals(double value) {
        return Math.round( value * 100 ) / 100.0;
    }

    public static boolean doubleEquals(Double a, Double b) {
        if ( a == null || b == null ) return false;
        return Math.abs( a - b) < 0.0001;
    }

}
