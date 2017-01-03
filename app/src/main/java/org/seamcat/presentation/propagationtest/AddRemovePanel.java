package org.seamcat.presentation.propagationtest;

import org.seamcat.presentation.menu.ToolBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

public class AddRemovePanel extends JPanel implements
      ActionListener {

	public interface AddRemoveListener {

		void add();

		void remove();

        void help();
	}
	private JToolBar toolBar;
	private JLabel label;
	private JButton add, remove, help;
	private AddRemoveListener addRemoveListener;	
	private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);


	public AddRemovePanel() {
		setLayout(new BorderLayout());

		toolBar = new JToolBar();
		toolBar.setFocusable(false);
		toolBar.setRollover(true);
		toolBar.setFloatable(false);
		setupToolbar();
		label = new JLabel(STRINGLIST.getString("LBL_ADD_REMOVE"));
		add(label, BorderLayout.NORTH);
		add(toolBar, BorderLayout.CENTER);
		add.addActionListener(this);
		remove.addActionListener(this);
		help.addActionListener(this);

		add.setToolTipText(STRINGLIST.getString("BTN_ADD_TOOL_TIP"));
		remove.setToolTipText(STRINGLIST.getString("BTN_REMOVE_TOOL_TIP"));
	}
	
	
	private void setupToolbar() {
		add = ToolBar.button("SEAMCAT_ICON_ADD",
		      "BTN_ADD_TOOL_TIP",
		      null);
		remove = ToolBar.button("SEAMCAT_ICON_DELETE_TRASH",
		      "BTN_REMOVE_TOOL_TIP",
		      null);

        help = ToolBar.button("SEAMCAT_ICON_HELP", null, null);
		toolBar.add(add);
		toolBar.add(remove);
        toolBar.add(Box.createHorizontalGlue());
        toolBar.addSeparator();
		toolBar.add(help);
	}

	public void setAddToolTip(String toolTip) {
		add.setToolTipText(toolTip);
	}
	
	public void setLabelText(String text) {
		label.setText(text);
	}
	public void addAddRemoveListener(AddRemoveListener addRemoveListener) {
		this.addRemoveListener = addRemoveListener;
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		if (addRemoveListener != null) {
			Object src = evt.getSource();
			if (src == add) {
				addRemoveListener.add();
			} else if (src == remove) {
				addRemoveListener.remove();
			} else if ( src == help ) {
                addRemoveListener.help();
            }
		}
	}
	
	public void enableRemove(boolean enable) {
		remove.setEnabled(enable);
	}

}
