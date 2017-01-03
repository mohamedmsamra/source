package org.seamcat.presentation.systems;

import org.seamcat.model.types.Description;

import static org.seamcat.model.factory.Factory.*;

public class Helper {

    public static Description changeName( Description current, String name ) {
        Description prototype = prototype(Description.class, current);
        when( prototype.name() ).thenReturn( name );
        return build( prototype );
    }
}
