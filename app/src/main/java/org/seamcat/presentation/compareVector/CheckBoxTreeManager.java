package org.seamcat.presentation.compareVector;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

interface OnSelectionChangedListener {

	/**
	 * Called when a selected has happened in the tree.
	 * 
	 * @param selectedPaths
	 */
	void onSelectionChanged(List<TreePath> selectedPaths);
}

public class CheckBoxTreeManager extends MouseAdapter implements
      TreeSelectionListener {

	private CheckBoxTreeModel selectionModel;
	private JTree tree = new JTree();
	int hotspot = new JCheckBox().getPreferredSize().width;
	private OnSelectionChangedListener onSelectionChangedListener;

	public CheckBoxTreeManager(JTree tree) {
		this.tree = tree;
		selectionModel = new CheckBoxTreeModel(tree.getModel());
		tree.setCellRenderer(new CheckBoxTreeCellRenderer(
		      new DefaultTreeCellRenderer(), selectionModel)); 
		tree.addMouseListener(this);
		selectionModel.addTreeSelectionListener(this);
	}

	public void setOnSelectionChangedListener(
	      OnSelectionChangedListener onSelectionChangedListener) {
		this.onSelectionChangedListener = onSelectionChangedListener;
	}

	public void mouseClicked(MouseEvent me) {
		TreePath path = tree.getPathForLocation(me.getX(), me.getY());
		if (path == null)
			return;
		if (me.getX() > tree.getPathBounds(path).x + hotspot)
			return;

		if(!((Node) path.getLastPathComponent()).isEnabled()){
			return;
		}
		boolean selected = selectionModel.isPathSelected(path, true);
		selectionModel.removeTreeSelectionListener(this);

		try {
			if (selected)
				selectionModel.removeSelectionPath(path);
			else
				selectionModel.addSelectionPath(path);
		} finally {
			selectionModel.addTreeSelectionListener(this);
			tree.treeDidChange();
			notifyOnSelectionChanged();

		}
	}

	public CheckBoxTreeModel getSelectionModel() {
		return selectionModel;
	}

	public void valueChanged(TreeSelectionEvent e) {
		tree.treeDidChange();
	}

	private void notifyOnSelectionChanged() {
		if (onSelectionChangedListener != null) {
			onSelectionChangedListener.onSelectionChanged(getSelectionModel()
			      .getAllCheckedPaths(this, tree));
		}
	}
	
}