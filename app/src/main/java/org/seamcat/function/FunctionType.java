package org.seamcat.function;

public class FunctionType {

    private boolean horizontal;
    private boolean vertical;
    private boolean spherical;
    private boolean none;

    private FunctionType(boolean horizontal, boolean vertical, boolean spherical, boolean none ) {
        this.horizontal = horizontal;
        this.vertical = vertical;
        this.spherical = spherical;
        this.none = none;
    }

    public static FunctionType horizontal() {
        return new FunctionType(true, false, false, false);
    }

    public static FunctionType vertical() {
        return new FunctionType(false, true, false, false);
    }

    public static FunctionType spherical() {
        return new FunctionType(false, false, true, false);
    }

    public static FunctionType none() {
        return new FunctionType(false, false, false, true);
    }

    public boolean isHorizontal() {
        return horizontal;
    }

    public boolean isVertical() {
        return vertical;
    }

    public boolean isSpherical() {
        return spherical;
    }

    public boolean isNone() {
        return none;
    }

    public String getTitle() {
        if ( isHorizontal()) {
            return "Horizontal Antenna";
        } else if ( isVertical() ) {
            return "Vertical Antenna";
        } else if ( isSpherical() ) {
            return "Spherical Antenna";
        }
        return "Function";
    }
}
