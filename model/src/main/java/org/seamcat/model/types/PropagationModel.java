package org.seamcat.model.types;

import org.seamcat.model.simulation.result.LinkResult;

public interface PropagationModel<T> extends Configuration<T>, LibraryItem {

    /**
     * Evaluates the path loss where the caller decides
     * if variation is selected
     */
    double evaluate(LinkResult linkResult, boolean variation);


    /**
     * Evaluates the path loss where variation is
     * as set in the workspace
     */
    double evaluate(LinkResult linkResult);

}
