package org.seamcat.presentation;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.seamcat.presentation.resources.ImageLoader;


public class LogoSplash extends JPanel {

	public LogoSplash() {
		super( new BorderLayout());
		
		ImageIcon icon = new ImageIcon( ImageLoader.class.getResource( "cept_logo_tan_transparent_background.png" ) );
		
		JLabel welcome = new JLabel("", icon, JLabel.CENTER );
		add( welcome, BorderLayout.CENTER );
	}
	
}
