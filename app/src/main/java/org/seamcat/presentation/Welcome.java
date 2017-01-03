package org.seamcat.presentation;

import org.seamcat.Seamcat;
import org.seamcat.commands.*;
import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.eventbus.UIEventHandler;
import org.seamcat.events.RecentlyUsedChangedEvent;
import org.seamcat.model.factory.Model;
import org.seamcat.presentation.resources.ImageLoader;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;

import static org.seamcat.eventbus.EventBusFactory.getEventBus;

public class Welcome extends EscapeDialog  {

	private JCheckBox show;
	private JEditorPane recentlyUsed;
	private JLabel showWelcomeText;

	public Welcome(Frame parent, String title, boolean modal ) {
		super( parent, title, modal );
		JScrollPane scroll = new JScrollPane();
		JPanel panel = new JPanel( new BorderLayout());
		panel.setBackground( Color.WHITE );
		panel.setBorder( BorderFactory.createEmptyBorder(5,5,5,5));
		show = new JCheckBox("Show this welcome screen at startup");
		show.setSelected(true);
		show.addActionListener( showAction );
		show.setBackground( Color.WHITE );

		JPanel bottom = new JPanel( new BorderLayout());
		bottom.setBackground( Color.WHITE );
		bottom.add( show, BorderLayout.WEST );

		bottom.add( new HtmlPanel( HtmlPanel.link("http://www.cept.org/eco", "<b>www.cept.org/eco</b>"), null, "white" ), BorderLayout.EAST );

		JPanel middle = new JPanel( new BorderLayout());
		showWelcomeText = new JLabel("");
		middle.setBackground( Color.WHITE );
		middle.add( showWelcomeText, BorderLayout.WEST );

		JPanel buttons = new JPanel( new GridLayout(2,3));
		buttons.setBorder( BorderFactory.createEmptyBorder(10,10,0,10));
		buttons.setBackground(Color.WHITE);
		recentlyUsed = new HtmlPanel( "", "5px 0px 0px 5px", "white" );
		recentlyUsed.addHyperlinkListener(recentlyUsedListener);

		buttons.add(linkable("New", ImageLoader.class.getResource("new_workspace_32x32.gif").toString()));
		buttons.add( recentlyUsed );
		buttons.add( linkable( "Open", ImageLoader.class.getResource( "folder_open_32x32.png" ).toString()));
		buttons.add( linkable( "New Batch", ImageLoader.class.getResource( "batch_add_32x32.png" ).toString() ) );
		buttons.add( linkable( "Calc", ImageLoader.class.getResource( "calculator_32x32.png" ).toString() ));


		panel.add(middle, BorderLayout.NORTH);
		panel.add( buttons, BorderLayout.CENTER );
		panel.add(bottom, BorderLayout.SOUTH);

		scroll.setViewportView( panel );
		add( scroll );
		setLocation(30, 130);
		setSize(new Dimension(460, 320));
		EventBusFactory.getEventBus().subscribe(this);
	}

	@UIEventHandler
	public void handleDisplayWelcome( DisplayWelcomeCommand event ) {
		setVisible(true);
	}

	@UIEventHandler
	public void handleRecentlyUsedChanged( RecentlyUsedChangedEvent event ) {
		StringBuilder sb = new StringBuilder("<center><font size=3>");
		if(event.getHistory().size() != 0){
			for (String item : event.getHistory()) {
				String name = item;
				if ( item.length() > 18 ) {
					name = "..." + item.substring( item.length()-17);
				}
				sb.append( HtmlPanel.link( "#"+item, name )).append("<br>");
			}
			if(event.getHistory().size() == 1){
				sb.append("<br><br>");
			}
			if(event.getHistory().size() == 2){
				sb.append("<br>");
			}
		}else{
			sb.append("no file <br>in history<br><br>");
		}
		sb.append( "</font>").append("<b>Recently Used</b></center>");
		recentlyUsed.setText( sb.toString() );
	}

	private ActionListener showAction = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			Preferences pref = Preferences.userNodeForPackage(Seamcat.class);
			pref.put( Seamcat.SHOW_WELCOME, Boolean.toString( show.isSelected() ) );
			Model.getInstance().setShowWelcome( show.isSelected() );
		}
	};

	private JEditorPane linkable( String name, String image ) {
		StringBuilder sb = new StringBuilder("");
		sb.append("<center><a href='#").append( name).append("'><img src=\"");
		sb.append( image ).append("\"></a><br><b>").append( name );
		sb.append("</b></center>");

		HtmlPanel pane = new HtmlPanel( sb.toString(), "7px 0px 0px 15px", "white" );
		pane.setLinkColor( "white");
		pane.addHyperlinkListener(hyperlinkListener);
		return pane;
	}

	private HyperlinkListener hyperlinkListener = new HyperlinkListener() {

		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (e.getEventType().equals(HyperlinkEvent.EventType.ENTERED)) {
				if (e.getDescription().equals("#New")) {
					setWelcomeText("Creates a new workspace based on default values");
				}else if (e.getDescription().equals("#Open")) {
					setWelcomeText("Opens an existing workspace on your local machine");
				} else if (e.getDescription().equals("#Batch")) {
					setWelcomeText("Allows to process several workspace at ones ");
				} else if (e.getDescription().equals("#Help")) {
					setWelcomeText("Redirect you to the on-line manual (internet connection required) ");
				} else if (e.getDescription().equals("#Calc")) {
					setWelcomeText("Opens the SEAMCAT pocket calculator");
				}
			}else if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
				if (e.getDescription().equals("#New")) {
					setVisible(false);
					getEventBus().publish(new NewWorkspaceCommand());
				} else if (e.getDescription().equals("#Open")) {
					setVisible(false);
					getEventBus().publish(new OpenWorkspaceCommand());
				} else if (e.getDescription().equals("#New Batch")) {
					setVisible(false);
					getEventBus().publish(new NewBatchCommand());
				} else if (e.getDescription().equals("#Calc")) {
					setVisible(false);
					getEventBus().publish( new DisplayTestCalculatorCommand());
				}
			}else if (e.getEventType().equals(HyperlinkEvent.EventType.EXITED)) {
				setWelcomeText("Click on icon to begin ...");
			}
		}
	};

	private HyperlinkListener recentlyUsedListener = new HyperlinkListener() {
		@Override
		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (e.getEventType().equals(HyperlinkEvent.EventType.ENTERED)) {
				String filename = e.getDescription().substring(1);
				String shortDirectoryName=filename.substring(0,3);

				String shortFileName="";
				if ( filename.length() > 38 ) {
					shortFileName = "..." + filename.substring( filename.length()-37);
					filename = shortDirectoryName + shortFileName;
				}
				setWelcomeText("Opens "+ filename);
			}else if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
				setVisible( false );
				String filename = e.getDescription().substring(1);

				EventBusFactory.getEventBus().publish( new OpenWorkspaceFileCommand(new File(filename)));
			}else if (e.getEventType().equals(HyperlinkEvent.EventType.EXITED)) {
				setWelcomeText("Click on icon to begin ...");
			}
		}
	};

	public void setWelcomeText(String text){
		showWelcomeText.setText( text );
	}

	public void setVisible( boolean visible ) {
		setWelcomeText( "Click on icon to begin ..." );
		super.setVisible( visible );
	}
}