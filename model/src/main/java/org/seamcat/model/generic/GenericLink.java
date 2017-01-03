package org.seamcat.model.generic;

import org.seamcat.model.types.CoverageRadius;
import org.seamcat.model.types.Link;

public interface GenericLink extends Link {

    CoverageRadius getCoverageRadius();

    RelativeLocation getRelativeLocation();

}
