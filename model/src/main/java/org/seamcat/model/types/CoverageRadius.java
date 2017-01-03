package org.seamcat.model.types;

import org.seamcat.model.generic.GenericSystem;

public interface CoverageRadius<T> extends Configuration<T>, LibraryItem {

    double evaluate( GenericSystem system );
}
