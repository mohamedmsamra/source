package org.seamcat.presentation.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import org.seamcat.commands.OpenWorkspaceFileCommand;
import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.eventbus.UIEventHandler;
import org.seamcat.events.RecentlyUsedChangedEvent;

public class HistoryItemsMenu {
	private JMenu menu;
	private List<JMenuItem> items;
	private boolean inserted = false;
	private int position;
	private JSeparator separator = new JSeparator();
	private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);

	public HistoryItemsMenu( JMenu menu ) {
		this.menu = menu;
		position = menu.getItemCount();
		items = new ArrayList<JMenuItem>(5);
		for ( int i=0; i<5; i++) {
			JMenuItem item = new JMenuItem();
			item.addActionListener( itemHandler );
			items.add(item);
		}
		EventBusFactory.getEventBus().subscribe(this);
	}

	@UIEventHandler
	public void handleRecentlyUsedChanged( RecentlyUsedChangedEvent event ) {
		updateHistory(event.getHistory());
	}

	private void updateHistory( List<String> history ) {
		for ( int i=0; i<history.size(); i++) {
			updateHistoryItem( items.get(i), history.get(i), i+1  );
		}
		if ( history.size() <5 ) {
			for ( int i=history.size(); i<5; i++) {
				menu.remove( items.get(i) );
			}
		}
		if ( history.size() == 0 ) {
			menu.remove( separator );
			inserted = false;
		}
	}

	private void updateHistoryItem( JMenuItem item, String name, int number ) {
		// build the item
		item.setText( fixName( name, number ) );
		item.setToolTipText( name );

		if ( !contains(item)) {
			menu.add(item, position+number-1);
			if ( !inserted ) {
				inserted = true;
				menu.add(separator, position+number );
			}
		}
	}

	private String fixName(String name, int number) {
		String shortDirectoryName=name.substring(0,3);
		if ( name.length() > 22 ) {
			return number + " " + shortDirectoryName + "..." + name.substring( name.length() - 21 );
		} else {
			return number + " " + shortDirectoryName + " ..." + name;
		}
	}

	private boolean contains( JMenuItem item ) {
		for ( int i=0; i<menu.getItemCount(); i++ ) {
			if ( menu.getItem( i) == item ) {
				return true;
			}
		}
		return false;
	}

	private ActionListener itemHandler = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			JMenuItem item = (JMenuItem) e.getSource();
			String filename = item.getToolTipText();
			EventBusFactory.getEventBus().publish( new OpenWorkspaceFileCommand(new File( filename )));
		}
	};
}
