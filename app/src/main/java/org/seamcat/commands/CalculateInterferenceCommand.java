package org.seamcat.commands;

import org.seamcat.model.engines.ICEConfiguration;

public class CalculateInterferenceCommand {

    private ICEConfiguration ice;

    public CalculateInterferenceCommand( ICEConfiguration ice ) {
        this.ice = ice;
    }

    public ICEConfiguration getIce() {
        return ice;
    }
}
