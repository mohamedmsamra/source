package org.seamcat.model;

import org.seamcat.function.BinarySearch;
import org.seamcat.function.DiscreteFunction;
import org.seamcat.model.functions.Point2D;

import java.util.ArrayList;
import java.util.List;


public class AntennaPatternConverter {

	public static void convertFrom0360To180180( DiscreteFunction function ) {
		List<Point2D> points = function.points();
		if ( points == null || points.size() == 0 ) return;
        Point2D first = points.get( 0 );
		Point2D last = points.get( points.size() -1 );
		if ( first.getX() == 0.0 && last.getX() == 360.0 ) {
            ensure180(function);
            points.remove( last );
            for (int i = 0; i < points.size(); i++) {
                Point2D point = points.get(i);
                if ( point == last ) continue;
                if (point.getX() > 180) {
                    points.set( i, new Point2D(point.getX() - 360, point.getY()));
                }
            }
			function.sortPoints();
		}
	}

    private static void ensure180( DiscreteFunction function ) {
        List<Point2D> points = function.points();
        final int[] lessIndex = new int[1];
        BinarySearch.search( points, new BinarySearch.Filter<Point2D>() {
            @Override
            public boolean evaluate(Point2D point, int index) {
                lessIndex[0] = index;
                return point.getX() <= 180;
            }
        });
        Point2D less = points.get(lessIndex[0]-1);
        if ( less.getX() == 180 ) {
            points.add( new Point2D( -180, less.getY()));
        }
        Point2D more = points.get(lessIndex[0]);
        double y = (less.getY() + more.getY()) / 2.0;
        points.add( new Point2D(180, y) );
        points.add( new Point2D(-180,y) );
        function.sortPoints();
        //return new Point2D(180.0, (less.getY() + more.getY())/2.0);
    }

    public static DiscreteFunction convertFrom0360To180180new( DiscreteFunction function ) {
        if ( function == null ) return new DiscreteFunction();
        List<Point2D> points = function.points();
        List<Point2D> converted = new ArrayList<Point2D>();
        ensure180( function );
        for (Point2D p : points) {
            if ( p.getX() > 359.999 ) continue;

            if ( p.getX() > 180 ) {
                converted.add( new Point2D( p.getX() - 360, p.getY()));
            } else {
                converted.add( p );
            }
            if ( Math.abs(p.getX() - 180) < 0.001 ) {
                converted.add( new Point2D( -180, p.getY()));
            }
        }
        DiscreteFunction result = new DiscreteFunction(converted);
        result.sortPoints();
        return result;
    }

}
