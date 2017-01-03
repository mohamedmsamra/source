package org.seamcat.presentation.components;

import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.seamcat.presentation.genericgui.WidgetAndKind;
import org.seamcat.presentation.genericgui.WidgetKind;
import org.seamcat.presentation.genericgui.item.ComponentWrapperItem;
import org.seamcat.presentation.resources.ImageLoader;

public class GenericLibraryImportExportWidget extends ComponentWrapperItem {

	private JButton anImport;
	private JButton export;

	ImageIcon importIcon = new ImageIcon(ImageLoader.class.getResource("import_16x16.png"));
	ImageIcon exportIcon = new ImageIcon(ImageLoader.class.getResource("export_16x16.png"));

	public GenericLibraryImportExportWidget( ) {
		JPanel buttons = new JPanel( new GridLayout(1,2));
		anImport = new JButton(importIcon);
		anImport.setToolTipText("Import");
		buttons.add(anImport);
		export = new JButton(exportIcon);
		export.setToolTipText("Export");
		buttons.add(export);
	   addWidgetAndKind( new WidgetAndKind(new JLabel("Library"), WidgetKind.LABEL) );
		addWidgetAndKind( new WidgetAndKind(buttons, WidgetKind.VALUE));
	}

	public void addExportHandler( ActionListener listener ) {
		export.addActionListener( listener );
	}
	public void addImportHandler( ActionListener listener ) {
		anImport.addActionListener( listener );
	}

    public void dispose() {
        for (ActionListener listener : export.getActionListeners()) {
            export.removeActionListener( listener );
        }

        for (ActionListener listener : anImport.getActionListeners()) {
            anImport.removeActionListener(listener);
        }
    }
}
