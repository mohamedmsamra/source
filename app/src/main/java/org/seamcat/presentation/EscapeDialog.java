package org.seamcat.presentation;

import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

@SuppressWarnings("serial")
public abstract class EscapeDialog extends JDialog {

	public ActionListener closeHandler() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		};
	}

    private boolean accept;
	protected Window owner;
	
	public EscapeDialog() {
		super();
	}

	public EscapeDialog(Frame _owner) {
		super(_owner);
		owner = _owner;

	}

	public EscapeDialog(Frame _owner, boolean modal) {
		super(_owner, modal);
        owner = _owner;
	}

	public EscapeDialog(Frame _owner, String title) {
		super(_owner, title);
		owner = _owner;
	}

	public EscapeDialog(Frame _owner, String title, boolean modal) {
		super(_owner, title, modal);
		owner = _owner;
	}

	public EscapeDialog(JDialog _owner) {
		super(_owner);
		owner = _owner;
	}

	public EscapeDialog(JDialog _owner, boolean modal) {
		super(_owner, modal);
		owner = _owner;
	}

	public EscapeDialog(JDialog _owner, String title, boolean modal) {
		super(_owner, title, modal);
		owner = _owner;
	}

	@Override
	protected JRootPane createRootPane() {
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		JRootPane rootPane = new JRootPane();
		rootPane.registerKeyboardAction( closeHandler(), stroke,
		      JComponent.WHEN_IN_FOCUSED_WINDOW);
		return rootPane;
	}

    public boolean isAccept() {
        return accept;
    }

    public void setAccept(boolean accept) {
        this.accept = accept;
    }

    public boolean display() {
        setAccept( false );
        setVisible( true );
        return isAccept();
    }
}
