package org.seamcat.presentation;

import org.seamcat.eventbus.UIEventHandler;
import org.seamcat.events.InfoMessageEvent;

import javax.swing.*;
import java.awt.*;

public class StatusBar extends JPanel {

    private JLabel left;
    private JEditorPane middle;
    private JLabel right;
    
    public StatusBar() {
        setLayout( new GridLayout(1,3 ));
        left = new JLabel();
        left.setBorder( BorderFactory.createEtchedBorder() );
        middle = new HtmlPanel("");
        middle.setBorder( BorderFactory.createEtchedBorder() );
        right = new JLabel();
        right.setBorder( BorderFactory.createEtchedBorder() );

        add( left );
        add( middle );
        add( right );
    }
    
    public void setLeft( String text ) {
        left.setText( text );
        left.setToolTipText( text );
    }
    
    public void setMiddle( String text, String textTooltip ) {
   	 middle.setToolTipText(textTooltip);
   	 middle.setText( "<html><center>"+text+"</center></html>" );
    }

    public void setRight( String text ) {
        right.setText( text );
        right.setToolTipText( text );
    }


    @UIEventHandler
    public void handleInfoMessageEvent( InfoMessageEvent event ) {
        setRight( event.getMessage() );
    }

}
