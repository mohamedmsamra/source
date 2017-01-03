package org.seamcat.function;

import org.seamcat.model.functions.BlockingMask;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.types.result.DescriptionImpl;

import java.util.List;

public class BlockingMaskImpl extends DiscreteFunction implements BlockingMask {

    public BlockingMaskImpl() {
        setDescription(new DescriptionImpl("Blocking Mask", ""));
    }

    public BlockingMaskImpl(double constant) {
        super(constant);
        setDescription(new DescriptionImpl("Blocking Mask", ""));
    }

    public BlockingMaskImpl(List<Point2D> points)  {
        super(points);
        setDescription(new DescriptionImpl("Blocking Mask", ""));
    }

    public BlockingMaskImpl offset(double value) {
        if ( isConstant() ) {
            return new BlockingMaskImpl(getConstant() + value );
        } else {
            DiscreteFunction offset = super.offset(value);
            return new BlockingMaskImpl(offset.points());
        }
    }

    @Override
    public String toString() {
        return description().name();
    }

}
