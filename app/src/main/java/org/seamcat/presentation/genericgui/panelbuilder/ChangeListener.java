package org.seamcat.presentation.genericgui.panelbuilder;

import org.seamcat.presentation.genericgui.item.AbstractItem;

import java.util.List;

public interface ChangeListener<T> {

    void handle(T model, List<AbstractItem> items, AbstractItem changedItem);
}
