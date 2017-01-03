package org.seamcat.commands;

public class ShowToolBarCommand {

    private boolean visible;

    public ShowToolBarCommand(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }
}
