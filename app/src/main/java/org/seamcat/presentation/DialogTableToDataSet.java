package org.seamcat.presentation;

import org.seamcat.function.DiscreteFunction;
import org.seamcat.function.EmissionMaskImpl;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.functions.Point3D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DialogTableToDataSet {

    private static boolean pointXExistsInList(List<? extends Point2D> data, double x) {
        boolean exists = false;
        for ( Point2D point : data ) {
            if (point.getX() == x) {
                exists = true;
            }
        }
        return exists;
    }

    /**
     * Symmetries points in the given list. New points for Point2D lists will
     * have: x=(x*-1), y=(1-y) and for Point3D lists: x=(x*-1), y=y, z=z
     */
    public static void symmetrize(List<Point2D> data, double symmetryPoint) {
        List<Point2D> newPoints = new ArrayList<Point2D>();
        for ( Point2D point : data ) {
            double x = symmetryPoint + symmetryPoint - point.getX();

            if (!DialogTableToDataSet.pointXExistsInList(data, x)) {
                if (point instanceof Point3D) {
                    newPoints.add(new Point3D(x, point.getY(), ((Point3D) point).getRZ()));
                } else {
                    newPoints.add(new Point2D(x, point.getY()));
                }
            }
        }
        if (newPoints.size() > 0) {
            data.addAll(newPoints);
        }
    }

    public static void symmetrizeFunction(DiscreteFunction function, double symmetryPoint) {
        List<Point2D> points = function.points();
        List<Point2D> symmetryPoints = new ArrayList<Point2D>();
        Map<Point2D, Double> mask = new HashMap<Point2D, Double>();
        for (Point2D point : points) {
            double x = symmetryPoint + symmetryPoint - point.getX();

            if (!DialogTableToDataSet.pointXExistsInList(function.points(), x)) {
                Point2D sym = new Point2D(x, point.getY());
                symmetryPoints.add(sym);
                if (function instanceof EmissionMaskImpl) {
                    mask.put(sym, ((EmissionMaskImpl) function).getMask(point));
                }
            }
        }

        for (Point2D point : symmetryPoints) {
            function.addPoint( point );
            if ( function instanceof EmissionMaskImpl ) {
                ((EmissionMaskImpl) function).setMask( point, mask.get(point));
            }
        }
    }

}
