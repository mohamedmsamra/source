package org.seamcat.presentation.genericgui.panelbuilder;

import com.rits.cloning.Cloner;
import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.events.InfoMessageEvent;
import org.seamcat.model.Library;
import org.seamcat.model.factory.Model;
import org.seamcat.model.types.Description;
import org.seamcat.model.types.LibraryItem;
import org.seamcat.model.types.result.DescriptionImpl;
import org.seamcat.presentation.DialogHelper;
import org.seamcat.presentation.components.GenericLibraryImportExportWidget;
import org.seamcat.presentation.components.GenericSelectionDialog;
import org.seamcat.presentation.genericgui.GenericModelEditorPanel;
import org.seamcat.presentation.genericgui.item.TextItem;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class LibraryItemDescriptionPanel<T extends LibraryItem> extends GenericModelEditorPanel<T> {

    private final TextItem nameItem;
    private final TextItem descriptionItem;

    public LibraryItemDescriptionPanel(final JFrame owner, final Class<T> clazz, final CompositeEditor<T> parent, Description description) {
        // an import export dialog should be added here
        GenericLibraryImportExportWidget importExportWidget = new GenericLibraryImportExportWidget();
        importExportWidget.addImportHandler(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                List<T> group = Model.getInstance().getLibrary().getGroup(clazz);
                GenericSelectionDialog<T> dialog = new GenericSelectionDialog<T>(owner, Library.typeName(clazz) + " Library", group);
                if (dialog.display()) {
                    parent.setModel((T) dialog.getSelectedValue());
                }
            }
        });
        importExportWidget.addExportHandler(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                Library library = Model.getInstance().getLibrary();
                T clone = new Cloner().deepClone(parent.getModel());
                String name = parent.getModel().description().name();
                if (!library.add(clazz, clone)) {
                    if (DialogHelper.overrideInLibrary(owner, name)) {
                        library.removeItem(clazz, name);
                        library.add(clazz, clone);
                        EventBusFactory.getEventBus().publish(new InfoMessageEvent(String.format("'%s' overridden in library", name)));
                    }
                } else {
                    EventBusFactory.getEventBus().publish(new InfoMessageEvent(String.format("'%s' added to library", name)));
                }
            }
        });
        addItem(importExportWidget);
        nameItem = new TextItem().label("Name");
        nameItem.initialize();
        nameItem.setValue( description.name() );
        descriptionItem = new TextItem().label("Description").linesToDisplay(3);
        descriptionItem.initialize();
        descriptionItem.setValue( description.description() );
        addItem(nameItem);
        addItem(descriptionItem);
        initializeWidgets();
    }

    public Description getDescription() {
        return new DescriptionImpl(nameItem.getValue(), descriptionItem.getValue());
    }
}
