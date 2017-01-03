package org.seamcat.presentation.components;

import org.seamcat.presentation.genericgui.WidgetAndKind;
import org.seamcat.presentation.genericgui.WidgetKind;
import org.seamcat.presentation.genericgui.item.ComponentWrapperItem;
import org.seamcat.presentation.resources.ImageLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class GenericLibraryImportExportResetWidget extends ComponentWrapperItem {

	private JButton anImport;
	private JButton export;
	private JButton restore;

	ImageIcon importIcon = new ImageIcon(ImageLoader.class.getResource("import_16x16.png"));
	ImageIcon exportIcon = new ImageIcon(ImageLoader.class.getResource("export_16x16.png"));
	ImageIcon restoreIcon= new ImageIcon(ImageLoader.class.getResource("restore_default_16x16.png"));

	public GenericLibraryImportExportResetWidget() {
		JPanel buttons = new JPanel( new GridLayout(1,3));
		anImport = new JButton(importIcon);
		anImport.setToolTipText("Import");
		buttons.add(anImport);
		export = new JButton(exportIcon);
		export.setToolTipText("Export");
		buttons.add(export);
		restore = new JButton(restoreIcon);
		restore.setToolTipText("Restore default values");
		buttons.add( restore );
	   	addWidgetAndKind(new WidgetAndKind(new JLabel("Library"), WidgetKind.LABEL));
		addWidgetAndKind( new WidgetAndKind(buttons, WidgetKind.VALUE));
	}

	public void addExportHandler( ActionListener listener ) {
		export.addActionListener( listener );
	}
	public void addImportHandler( ActionListener listener ) {
		anImport.addActionListener( listener );
	}
    public void addRestoreHandle( ActionListener listener ) { restore.addActionListener( listener);}

    public void dispose() {
        for (ActionListener listener : export.getActionListeners()) {
            export.removeActionListener( listener );
        }

        for (ActionListener listener : anImport.getActionListeners()) {
            anImport.removeActionListener(listener);
        }
    }

    public void setGlobalRelevance( boolean relevance ) {
        anImport.setEnabled( relevance );
        export.setEnabled( relevance );
        restore.setEnabled( relevance );
    }
}
