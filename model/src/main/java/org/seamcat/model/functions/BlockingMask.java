package org.seamcat.model.functions;

import org.seamcat.model.types.LibraryItem;

public interface BlockingMask extends Function, LibraryItem {

    BlockingMask offset(double value);
}
