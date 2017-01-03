package org.seamcat;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class SplashScreen extends JWindow {

	private static final ResourceBundle stringlist = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);
    private static final boolean BETAVERSION = false;
    private static final boolean BETAIMAGE = false;

	public SplashScreen(Frame parent) {
		super(parent);
        JProgressBar statusLabel = new JProgressBar();
        statusLabel.setString(stringlist.getString("SPLASH_SCREEN_TEXT") + " " + stringlist.getString("APPLICATION_TITLE"));
		
		// CHP 20/12-2005: 350 is the current count of log events that occur
		// during startup.
		// Each log event will increment progress bar by 1.
		statusLabel.setMaximum(350);
		statusLabel.setStringPainted(true);
		statusLabel.setAlignmentX(JProgressBar.LEFT_ALIGNMENT);
		
		String imageFileName;
		if(BETAVERSION){
            if ( BETAIMAGE) {
                imageFileName = stringlist.getString("SPLASH_BETA_SCREEN_IMAGE");
            } else {
                imageFileName = stringlist.getString("SPLASH_ALPHA_SCREEN_IMAGE");
            }
		}else{
			imageFileName = stringlist.getString("SPLASH_SCREEN_IMAGE");	
		}
		
		JLabel l = new JLabel(new ImageIcon(getClass().getResource(imageFileName))) {

         private static final long serialVersionUID = 1L;

			@Override
         protected void paintComponent(Graphics g) {
	         super.paintComponent(g);
	         
	         if (BETAVERSION) {
		         g.setColor(new Color(245,187,0)); //SEAMCAT Yellow...
		         g.drawString("Loading version: " + stringlist.getString("APPLICATION_TITLE"), 10, 20);
	         }
         }
		};
		
		getContentPane().add(l, BorderLayout.CENTER);
		getContentPane().add(statusLabel, BorderLayout.SOUTH);
		getContentPane().setBackground(Color.WHITE);

		pack();
		setLocationRelativeTo(parent);
		setVisible(true);
	}
}
