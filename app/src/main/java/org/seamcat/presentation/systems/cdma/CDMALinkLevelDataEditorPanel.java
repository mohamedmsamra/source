package org.seamcat.presentation.systems.cdma;

import org.seamcat.cdma.CDMALinkLevelData;
import org.seamcat.cdma.CDMALinkLevelDataPoint;
import org.seamcat.presentation.SeamcatDoubleCellEditor;
import org.seamcat.presentation.components.SpringUtilities;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class CDMALinkLevelDataEditorPanel extends JPanel {

	private JFrame owner;

	private class CenterPanel extends JPanel implements TableModelListener {

		private class PointTablePanel extends JPanel {

			private CDMAEditModel path1Model;
			private CDMAEditModel path2Model;
			private final JTable table = new JTable();
			private final TableModelListener tableModelListener;

			PointTablePanel(TableModelListener tableModelListener) {
				super(new GridLayout());
				this.tableModelListener = tableModelListener;

				table.setRowSelectionAllowed(false);
				table.setColumnSelectionAllowed(false);
				table.setCellSelectionEnabled(true);
				table.setDefaultRenderer(Double.class, new LinkLevelCellRenderer());
				table.setDefaultEditor(Double.class, new SeamcatDoubleCellEditor(-100000, 100000));

				add(new JScrollPane(table));
				setPreferredSize(new Dimension(500, 200));
			}

			int addTableRow() {
				CDMAEditModel model = (CDMAEditModel) table.getModel();
				model.addRow();
				table.tableChanged(new TableModelEvent(model));
				return model.getRowCount() - 1;
			}

			CDMAEditModel getPathModel(int path) {
				if ( path == 1 ) return path1Model;
                if ( path == 2 ) return path2Model;
                throw new IllegalArgumentException("Can only get path 1 or 2 (1-based)");
			}

			void removeTableRow() {
				int row = table.getSelectedRow();
				if (row >= 0) {
					CDMAEditModel model = (CDMAEditModel) table.getModel();
					model.deleteRow(row);
					table.tableChanged(new TableModelEvent(model));
				}
			}

			void setModels(CDMAEditModel path1Model, CDMAEditModel path2Model) {
				if (this.path1Model != null && this.path2Model != null) {
					this.path1Model.removeTableModelListener(tableModelListener);
					this.path2Model.removeTableModelListener(tableModelListener);
				}
				if (path1Model != null && path2Model != null) {
					path1Model.addTableModelListener(tableModelListener);
					path2Model.addTableModelListener(tableModelListener);
					this.path1Model = path1Model;
					this.path2Model = path2Model;
				}
			}

			void setSelectedPathModel(int path) {
				if ( path == 1 ) table.setModel( path1Model);
                else if ( path == 2 ) table.setModel( path2Model );
                else throw new IllegalArgumentException("Can only set path to 1 or 2 (1-based)");
			}
		}

		private final LLDGraphPanel graphPanel = new LLDGraphPanel();
		private final PointTablePanel pointTablePanel;

		CenterPanel() {
			super(new SpringLayout());

			pointTablePanel = new PointTablePanel(this);

			add(pointTablePanel);
			add(Box.createVerticalGlue());
			add(graphPanel);

			SpringUtilities.makeCompactGrid(this, 3, 1, 0, 0, 0, 0);
		}

		int addTableRow() {
			return pointTablePanel.addTableRow();
		}

		CDMAEditModel getPathModel(int path) {
			return pointTablePanel.getPathModel(path);
		}

		void removeTableRow() {
			pointTablePanel.removeTableRow();
		}

		void setModels() {
            if ( data == null ) return;
            CDMAEditModel path1Model = (CDMAEditModel) data.getTableModel(1);
            CDMAEditModel path2Model = (CDMAEditModel) data.getTableModel(2);
            pointTablePanel.setModels(path1Model, path2Model);
			if (path1Model != null && path2Model != null) {
				pointTablePanel.setSelectedPathModel(1);
				if ( data.getLinkType() == CDMALinkLevelData.LinkType.DOWNLINK ) {
                    graphPanel.setRangeLabel(data.getTargetERpct(), data.getTargetERType().toString());
                }
			}
		}

		void setSelectedPathModel(int path) {
			pointTablePanel.setSelectedPathModel(path);
			graphPanel.setGraph(getPathModel(path));
		}

		public void tableChanged(TableModelEvent e) {
			graphPanel.updateGraph();

			if ( e.getType() == TableModelEvent.DELETE ) {
				// refresh everything
			} else if ( e.getType() == TableModelEvent.UPDATE ) {
				// because it is hard to navigate the points in the data structure
				// update everything

				List<CDMALinkLevelDataPoint> values = new ArrayList<CDMALinkLevelDataPoint>();
				CDMAEditModel model = (CDMAEditModel) e.getSource();
				for ( int row = 0; row < model.getRowCount(); row++ ) {
					double geometry = 0;
					for ( int column = 0; column < model.getColumnCount(); column++ ) {
						Double value = (Double) model.getValueAt(row, column);
						if ( column == 0 ) {
							geometry = value;
						} else if ( value != null ) {
							double speed = CDMALinkLevelData.SPEED_VALUES[ column -1 ];
							values.add( new CDMALinkLevelDataPoint(data.getFrequency(), data.getCurrentPath(), geometry, speed, value) );
						}
					}
				}
				data.updatePath( data.getCurrentPath(), values );
			}


		}
	}

	private static class LinkLevelCellRenderer extends JLabel implements TableCellRenderer {

		private static final Color ILLEGAL_BGCOLOR = new Color(255, 120, 120);

		public LinkLevelCellRenderer() {
			super("", SwingConstants.RIGHT);
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
		      int row, int column) {
			if (value != null) {
				setText(value.toString());
			} else {
				setText("");
			}
			if (isSelected) {
				setBackground(table.getSelectionBackground());
				setForeground(table.getSelectionForeground());
			} else {
				setBackground(table.getBackground());
				setForeground(table.getForeground());
			}
			if (table.getModel().getValueAt(row, 0) == null && !isSelected) {
				setBackground(ILLEGAL_BGCOLOR);
			}
			setEnabled(table.isEnabled());
			setFont(table.getFont());
			setOpaque(true);
			return this;
		}
	}

	private class SideButtonPanel extends JPanel {

		private class DataPointAddDeletePanel extends JPanel {

			DataPointAddDeletePanel() {
				super(new GridLayout(2, 1));

				JButton btnAdd = new JButton(STRINGLIST.getString("BTN_CAPTION_ADD"));
				btnAdd.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						selectCell(addTableRow(), 0);
					}
				});

				JButton btnDelete = new JButton(STRINGLIST.getString("BTN_CAPTION_DELETE"));
				btnDelete.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						removeTableRow();
					}
				});

				setBorder(new TitledBorder(STRINGLIST.getString("LIBRARY_CDMA_LLD_DETAILS_DATAPOINTS")));

				add(btnAdd);
				add(btnDelete);
			}
		}

		private class PathSelectorPanel extends JPanel {

			private final JRadioButton rb1Path = new JRadioButton(STRINGLIST.getString("LIBRARY_CDMA_LLD_DETAILS_1PATH"));
			private final JRadioButton rb2Path = new JRadioButton(STRINGLIST.getString("LIBRARY_CDMA_LLD_DETAILS_2PATH"));

			PathSelectorPanel() {
				super(new GridLayout(2, 1));

				ButtonGroup bgPath = new ButtonGroup();
				bgPath.add(rb1Path);
				bgPath.add(rb2Path);

				rb1Path.setSelected(true);

				rb1Path.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						selectPath(1);
					}
				});
				rb2Path.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						selectPath(2);
					}
				});

				setBorder(new TitledBorder(STRINGLIST.getString("LIBRARY_CDMA_LLD_DETAILS_PATHS")));
				add(rb1Path);
				add(rb2Path);
			}

			void selectPath(int path) {
				CDMALinkLevelDataEditorPanel.this.setSelectedPathModel(path);
				data.setCurrentPath( path );
			}
		}

		private final CDMALinkLevelDataEditBasicsDialog basicsDialog;

		private final DataPointAddDeletePanel dataPointAddDeletePanel = new DataPointAddDeletePanel();

		private final PathSelectorPanel pathSelectorPanel = new PathSelectorPanel();

		SideButtonPanel() {
			basicsDialog = new CDMALinkLevelDataEditBasicsDialog(owner);

			JButton btnBasics = new JButton(STRINGLIST.getString("LIBRARY_CDMA_LLD_DETAILS_LINKBASICS"));
			btnBasics.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					btnBasicsActionPerformed();
				}
			});

			JButton btnAdvanced = new JButton("Advanced");
			btnAdvanced.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {

					double geo = 7;
					// for (;geo <= 100;geo++) {
					CDMALinkLevelDataPoint point = new CDMALinkLevelDataPoint(data.getFrequency(), 1, geo, 0, 0);
					data.getLinkLevelDataPoint(point);
					// }
					setModel(data);
				}

			});

			JPanel btnBasicsPanel = new JPanel(new GridLayout());
			btnBasicsPanel.setBorder(new TitledBorder(STRINGLIST.getString("LIBRARY_CDMA_LLD_BASICS_BTN_BORDER")));
			btnBasicsPanel.add(btnBasics);
			// btnBasicsPanel.add(btnAdvanced);

			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			add(pathSelectorPanel);
			add(dataPointAddDeletePanel);
			add(btnBasicsPanel);
			add(Box.createVerticalStrut(1000));

		}

		void btnBasicsActionPerformed() {
			if (basicsDialog.showDialog(data)) {
				setModel(data);
			}
		}
	}

	private static class TitlePanel extends JPanel {

		private final JLabel lbTitle = new JLabel("title undefined");

		TitlePanel() {
			super(new FlowLayout(FlowLayout.CENTER));

			lbTitle.setFont(getFont().deriveFont(Font.BOLD));

			add(lbTitle);
		}

		void setPanelTitle(String title) {
			lbTitle.setText(title);
		}
	}

	private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);
	private final CenterPanel centerPanel = new CenterPanel();
	private CDMALinkLevelData data;

    private TitlePanel titlePanel = new TitlePanel();

	public CDMALinkLevelDataEditorPanel( JFrame owner) {
		super( new BorderLayout() );
		this.owner = owner;

		add(titlePanel, BorderLayout.NORTH);
        add(new SideButtonPanel(), BorderLayout.EAST);
		add(centerPanel, BorderLayout.CENTER);
		add(Box.createHorizontalStrut(50), BorderLayout.WEST);
	}

	private int addTableRow() {
		return centerPanel.addTableRow();
	}

	private void removeTableRow() {
		centerPanel.removeTableRow();
	}

	private void selectCell(int row, int col) {
		centerPanel.pointTablePanel.table.changeSelection(row, col, true, false);
		centerPanel.pointTablePanel.table.editCellAt(row, col);
		centerPanel.pointTablePanel.table.grabFocus();
	}

	public void setModel( CDMALinkLevelData data ) {
		this.data = data;
        centerPanel.setModels();
        if (data != null) {
			setSelectedPathModel(1);
		}
	}

	private void setSelectedPathModel(int path) {
		centerPanel.setSelectedPathModel(path);
		titlePanel.setPanelTitle(data.getPathDescription(path));
	}
}