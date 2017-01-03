package org.seamcat.presentation.displaysignal;

import org.seamcat.presentation.propagationtest.PropagationHolder;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Vector;

public class GroupSelectorPanel extends JPanel {

	private JScrollPane jScrollPane;
	private MyJlist chartGroup;
	private TitledBorder border;

	public GroupSelectorPanel() {
		setLayout(new BorderLayout());
		chartGroup = new MyJlist();
		jScrollPane = new JScrollPane(chartGroup);
		border = new TitledBorder("Generated signals");
		this.add(jScrollPane, BorderLayout.CENTER);
		setBorder(border);
		setPreferredSize(new Dimension(getPreferredSize().width, 130));
	}

	public void addListSelectionListener(ListSelectionListener listSelectionListener) {
		chartGroup.addListSelectionListener(listSelectionListener);
	}
    public void removeListSelectionListener(ListSelectionListener listener ) {
        chartGroup.removeListSelectionListener(listener);
    }

	public void setBorderTitle(String title) {
		border.setTitle(title);
	}

	public void setListData(List<PropagationHolder> propagations) {
        //Vector elements = new Vector();
        DefaultListModel model = new DefaultListModel();
        for (PropagationHolder propagation : propagations) {
            model.addElement(new GroupListItem(propagation));
        }
        chartGroup.setModel(model);
        chartGroup.setSelectionInterval(0, chartGroup.getModel().getSize() - 1);
    }

    public void updateListData(List<PropagationHolder> propagationHolders ) {
        for (int i=0; i<propagationHolders.size(); i++ ) {
            if ( chartGroup.getModel().getSize() == i ) {
                DefaultListModel model = (DefaultListModel) chartGroup.getModel();
                model.addElement(new GroupListItem(propagationHolders.get(i)));
                chartGroup.setSelectionInterval(0, chartGroup.getModel().getSize() - 1);
            }
            GroupListItem item = (GroupListItem) chartGroup.getModel().getElementAt(i);
            item.propagation = propagationHolders.get(i);
        }
    }

	public void reset() {
		chartGroup.setListData(new Vector());
	}

	private class MyJlist extends JList {

		public MyJlist() {
            ToolTipManager.sharedInstance().unregisterComponent(this);
		}

		@Override
		public void setListData(Object[] listData) {
			super.setListData(listData);
			toogleToolTip(listData.length > 0);

		}

		@Override
		public void setListData(Vector listData) {
			super.setListData(listData);
			toogleToolTip(listData.size() > 0);
		}

		
		private void toogleToolTip(boolean enable) {
			// TODO potential threading issue here
            if(enable) {
                ToolTipManager.sharedInstance().registerComponent(this);
			} else {
				ToolTipManager.sharedInstance().unregisterComponent(this);
			}
		}
		@Override
		public String getToolTipText(MouseEvent event) {
			int index = chartGroup.locationToIndex(event.getPoint());
			if (index != -1) {
				return chartGroup.getModel().getElementAt(index).toString();
			}
			return null;
		}
	}

    public class GroupListItem {
        private PropagationHolder propagation;
        private String name;
        GroupListItem(PropagationHolder propagation ) {
            this.propagation = propagation;
            this.name = propagation.getTitle();
        }

        @Override
        public String toString() {
            return name;
        }

        public PropagationHolder getPropagation() {
            return propagation;
        }
    }


}
