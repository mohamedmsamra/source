package org.seamcat.presentation;

import java.awt.Color;
import java.awt.Desktop;

import javax.swing.JEditorPane;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

public class HtmlPanel extends JEditorPane {

    public HtmlPanel( String message ) {
        setContentType("text/html");
        setText(message);
        setColor( defaultColor(), null );
        setOpaque(false);
        setBorder(null);
        setEditable(false);
        setFocusable(false);
        addDefaultLinkListener();
    }

    public HtmlPanel( String message, String border ) {
        setContentType("text/html");
        setText(message);
        setColor( defaultColor(), border );
        setOpaque(false);
        setBorder(null);
        setEditable(false);
        setFocusable(false);
        addDefaultLinkListener();
    }
    
    public HtmlPanel( String message, String border, String backgroundColor ) {
        setContentType("text/html");
        setText(message);
        setColor( backgroundColor, border );
        setOpaque(false);
        setBorder(null);
        setEditable(false);
        setFocusable(false);
        addDefaultLinkListener();
    }

    private String defaultColor() {
        Color color = UIManager.getColor("Panel.background");
        return String.format("rgb(%d,%d,%d)", color.getRed(), color.getGreen(), color.getBlue());
    }
    
    private void setColor( String backgroundColor, String border ) {
        String bodyRule;
        if ( border == null ) {
            bodyRule = "body { background-color: "+backgroundColor+"; } ";
        } else {
            bodyRule = "body { background-color: "+backgroundColor+"; margin: "+border+";} ";
        }
        StyleSheet sheet = ((HTMLDocument) getDocument()).getStyleSheet();
        sheet.addRule( bodyRule );
    }

    public void setLinkColor( String linkColor ) {
        String anchorRule = "a {color: "+linkColor+"}";
        StyleSheet sheet = ((HTMLDocument) getDocument()).getStyleSheet();
        sheet.addRule( anchorRule);
    }

    private void addDefaultLinkListener() {
        addHyperlinkListener( new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                    if ( !e.getDescription().startsWith("#")) {
                        // Open in browser
                        try {
                            Desktop.getDesktop().browse( e.getURL().toURI() );
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    public static String image( String name, String url ) {
        return "<a href='#"+name+"'><img src=\""+ url+ "\"></a>";
    }

    public static String link( String url ) {
        return "<a href=\""+url+"\">"+url+"</a>";
    }

    public static String link( String url, String name ) {
        return "<a href=\""+url+"\">"+name+"</a>";
    }
}
