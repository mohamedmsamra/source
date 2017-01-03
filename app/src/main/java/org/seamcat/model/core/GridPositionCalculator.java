package org.seamcat.model.core;

import org.seamcat.function.Exp;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.mathematics.Mathematics;

import static org.seamcat.mathematics.Constants.SQRT3;

/**
 * method for the wrap around calculation
 */
public class GridPositionCalculator {

    private static final Exp[][] locationTransformer = new Exp[19][];

    static {
        locationTransformer[0] = new Exp[2];
        locationTransformer[1] = new Exp[2];
        locationTransformer[2] = new Exp[2];
        locationTransformer[3] = new Exp[2];
        locationTransformer[4] = new Exp[2];
        locationTransformer[5] = new Exp[2];
        locationTransformer[6] = new Exp[2];
        locationTransformer[7] = new Exp[2];
        locationTransformer[8] = new Exp[2];
        locationTransformer[9] = new Exp[2];
        locationTransformer[10]= new Exp[2];
        locationTransformer[11]= new Exp[2];
        locationTransformer[12]= new Exp[2];
        locationTransformer[13]= new Exp[2];
        locationTransformer[14]= new Exp[2];
        locationTransformer[15]= new Exp[2];
        locationTransformer[16]= new Exp[2];
        locationTransformer[17]= new Exp[2];
        locationTransformer[18]= new Exp[2];

        locationTransformer[0][0] = new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p;
            }
        };
        locationTransformer[1][0] = new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p.transform(add,-d, 0);
            }
        };
        locationTransformer[2][0] = new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p.transform(add,-d/2, -d * (SQRT3/2));
            }
        };
        locationTransformer[3][0] = new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p.transform(add, d/2, -d * (SQRT3/2));
            }
        };
        locationTransformer[4][0] = new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p.transform(add, d, 0);
            }
        };
        locationTransformer[5][0] = new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p.transform(add, d/2, d * (SQRT3/2));
            }
        };
        locationTransformer[6][0] = new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p.transform(add, -d/2, d * (SQRT3/2));
            }
        };
        locationTransformer[7][0] = new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p.transform(add, -2 * d, 0);
            }
        };
        locationTransformer[8][0] = new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p.transform(add, -3 * d/2,-d*(SQRT3/2));
            }
        };
        locationTransformer[9][0] = new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p.transform(add, -d,-d*SQRT3);
            }
        };
        locationTransformer[10][0]= new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p.transform(add,0, - SQRT3 *d );
            }
        };
        locationTransformer[11][0]= new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p.transform(add,d, -d * SQRT3 );
            }
        };
        locationTransformer[12][0]= new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p.transform(add,3 * d/2,-SQRT3/2 * d );
            }
        };
        locationTransformer[13][0]= new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p.transform(add,2 * d, 0 );
            }
        };
        locationTransformer[14][0]= new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p.transform(add,3 * d / 2,SQRT3/2 * d );
            }
        };
        locationTransformer[15][0]= new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p.transform(add,d,SQRT3 * d );
            }
        };
        locationTransformer[16][0]= new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p.transform(add,0,SQRT3 * d );
            }
        };
        locationTransformer[17][0]= new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p.transform(add,-d, SQRT3 * d );
            }
        };
        locationTransformer[18][0]= new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p.transform(add,-3 * d/2,SQRT3/2*d);
            }
        };

        locationTransformer[0][1] = new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p;
            }
        };
        locationTransformer[1][1] = new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p.transform(add,-1.5 * d / SQRT3,-d/2);
            }
        };
        locationTransformer[2][1] = new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p.transform(add,0,-d);
            }
        };
        locationTransformer[3][1] = new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p.transform(add,1.5 * d / SQRT3,-d/2);
            }
        };
        locationTransformer[4][1] = new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p.transform(add,1.5 * d / SQRT3,d / 2);
            }
        };
        locationTransformer[5][1] = new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p.transform(add,0,d);
            }
        };
        locationTransformer[6][1] = new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p.transform(add,-1.5 * d / SQRT3,d/2);
            }
        };
        locationTransformer[7][1] = new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p.transform(add,-3 * d / SQRT3,0);
            }
        };
        locationTransformer[8][1] = new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p.transform(add,-3 * d / SQRT3,-d);
            }
        };
        locationTransformer[9][1] = new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p.transform(add,-1.5*d/ SQRT3,-1.5 * d);
            }
        };
        locationTransformer[10][1] = new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p.transform(add,0,-2 * d);
            }
        };
        locationTransformer[11][1] = new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p.transform(add,1.5*d/SQRT3,-1.5*d);
            }
        };
        locationTransformer[12][1] = new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p.transform(add,3 * d/ SQRT3,-d);
            }
        };
        locationTransformer[13][1] = new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p.transform(add,3 * d/ SQRT3,0);
            }
        };
        locationTransformer[14][1] = new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p.transform(add,3 * d/ SQRT3,d);
            }
        };
        locationTransformer[15][1] = new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p.transform(add,1.5*d/SQRT3,1.5 * d);
            }
        };
        locationTransformer[16][1] = new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p.transform(add,0,2 * d);
            }
        };
        locationTransformer[17][1] = new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p.transform(add,-1.5*d/SQRT3,1.5 * d);
            }
        };
        locationTransformer[18][1] = new Exp() {
            public Point2D evaluate(boolean add, Point2D p, double d) {
                return p.transform(add,-3 * d / SQRT3,d);
            }
        };
    }

    public static Point2D standard(boolean add, int referenceCell, Point2D p, double d ) {
        return locationTransformer[referenceCell][0].evaluate(add, p, d);
    }

    public static Point2D ppg2(boolean add, int referenceCell, Point2D p, double d ) {
        return locationTransformer[referenceCell][1].evaluate(add, p, d);
    }

    /**
     * method that check if the UE is within the hexagonal cell or not
     *
     * @param p
     * @param shiftX
     * @param shiftY
     * @param hexagon
     */
    public static boolean isInside(Point2D p, double shiftX, double shiftY, Point2D[] hexagon ){
        for (int i = 0; i < hexagon.length; i++) {
            int j = (i + 1) % hexagon.length;
            if ((hexagon[i].getX() + shiftX) * ((hexagon[j].getY() + shiftY) - p.getY()) + (hexagon[j].getX() + shiftX) * (p.getY() - (hexagon[i].getY() + shiftY)) + p.getX() * ((hexagon[i].getY() + shiftY) - (hexagon[j].getY() + shiftY)) < 0.0) {
                return false;
            }
        }
        return true;
    }

    /**
     * method that calculates the points (coordinates) of the hexagon
     * @param position
     * @param cellRadius
     * @param hexagon
     */
    public static void calculateHexagon( Point2D position, double cellRadius, Point2D[] hexagon ) {
        for (int k = 0; k < hexagon.length; k++) {
            hexagon[k] = position.transform((Mathematics.cosD((k * 60)) * cellRadius), (Mathematics.sinD((k * 60)) * cellRadius));
        }
    }
}
