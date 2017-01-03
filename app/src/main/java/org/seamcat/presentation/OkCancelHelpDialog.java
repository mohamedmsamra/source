package org.seamcat.presentation;

import org.seamcat.help.SeamcatHelpResolver;
import org.seamcat.presentation.components.NavigateButtonPanel;

import javax.swing.*;
import java.awt.*;

public class OkCancelHelpDialog extends EscapeDialog {

	public OkCancelHelpDialog( JDialog parent, Component panel, final Object helpContext, String title ) {
		super(parent, true);
		setTitle( title );
		getContentPane().setLayout( new BorderLayout());
		getContentPane().add(panel, BorderLayout.CENTER);
		getContentPane().add(new NavigateButtonPanel(this) {
            public void btnHelpActionPerformed() {
                SeamcatHelpResolver.showHelp(helpContext);
            }
        }, BorderLayout.SOUTH);
		setSize(new Dimension(500,600));
		setLocationRelativeTo( parent );
	}

	public boolean display() {
		setAccept( false );
		setVisible( true );
		return isAccept();
	}
}
