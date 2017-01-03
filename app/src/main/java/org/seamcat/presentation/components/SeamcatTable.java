package org.seamcat.presentation.components;

import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.EventObject;

public class SeamcatTable extends JTable {

	private interface SeamcatTableLastCellAction {
		void tabActionPerformedOnLastCell();
	}

	private SeamcatTableLastCellAction lastCellAction;

	public SeamcatTable(TableModel dm) {
		super(dm);
		init();
		if (dm instanceof DiscreteFunctionTableModelAdapter) {
			addSeamcatTableLastCellAction(new SeamcatTableLastCellAction() {

				public void tabActionPerformedOnLastCell() {
					((DiscreteFunctionTableModelAdapter) getModel()).addRow();
				}
			});
		} else if (dm instanceof DiscreteFunction2TableModelAdapter) {
			addSeamcatTableLastCellAction(new SeamcatTableLastCellAction() {

				public void tabActionPerformedOnLastCell() {
					((DiscreteFunction2TableModelAdapter) getModel()).addRow();
				}
			});
		} else if (dm instanceof StairDistributionTableModelAdapter) {
			addSeamcatTableLastCellAction(new SeamcatTableLastCellAction() {

				public void tabActionPerformedOnLastCell() {
					((StairDistributionTableModelAdapter) getModel()).addRow();
				}
			});
		}

	}

	public void addSeamcatTableLastCellAction(SeamcatTableLastCellAction stca) {
		lastCellAction = stca;
	}

	@Override
	public boolean editCellAt(int row, int column, EventObject e) {
		boolean res = super.editCellAt(row, column, e);
		Component c = getEditorComponent();
		if (c instanceof JTextComponent) {
			((JTextComponent) c).selectAll();
			if (getColumnClass(column) == Double.class
			      && c instanceof JFormattedTextField) {
				((JFormattedTextField) c)
				      .setFocusLostBehavior(JFormattedTextField.COMMIT);
			}
		}
		return res;
	}

	private void init() {
		String actionName = "selectNextColumnCell";
		final Action tabAction = getActionMap().get(actionName);
		Action myAction = new AbstractAction() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				if (isLastCell()) {
					try {
						lastCellAction.tabActionPerformedOnLastCell();
					} catch (Exception ex) {
					}
				}
				tabAction.actionPerformed(e);
			}
		};

		getActionMap().put(actionName, myAction);
		setRowSelectionAllowed(true);
		setColumnSelectionAllowed(false);
		setCellSelectionEnabled(false);
	}

	public boolean isLastCell() {
		int rows = getRowCount();
		int cols = getColumnCount();
		int selectedRow = getSelectedRow();
		int selectedCol = getSelectedColumn();
		return rows == selectedRow + 1 && cols == selectedCol + 1;
	}

}