package org.seamcat.presentation.components;

import org.seamcat.function.WithPoints;
import org.seamcat.model.functions.Function;
import org.seamcat.model.functions.Point2D;

public interface DiscreteFunctionTableModelAdapterInterface {
    void addRow();

    void clear();

    void deleteRow(int row);

    void setFunction(Function function);

    Function getFunction();
}
