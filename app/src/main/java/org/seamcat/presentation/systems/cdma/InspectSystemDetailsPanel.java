package org.seamcat.presentation.systems.cdma;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.apache.log4j.Logger;
import org.seamcat.dmasystems.AbstractDmaBaseStation;
import org.seamcat.dmasystems.AbstractDmaLink;
import org.seamcat.dmasystems.AbstractDmaMobile;
import org.seamcat.dmasystems.ActiveInterferer;
import org.seamcat.model.cellular.CellularLayout;
import org.seamcat.model.cellular.CellularSystem;
import org.seamcat.model.simulation.result.Interferer;
import org.seamcat.model.simulation.result.SimulationElement;
import org.seamcat.ofdma.DownlinkOfdmaMobile;
import org.seamcat.ofdma.OfdmaExternalInterferer;
import org.seamcat.presentation.DialogDisplaySignal;
import org.seamcat.presentation.MainWindow;
import org.seamcat.presentation.components.BorderPanel;
import org.seamcat.presentation.components.ScrollingBorderPanel;
import org.seamcat.presentation.systems.cdma.tablemodels.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.*;
import java.util.List;
import java.util.ResourceBundle;

public class InspectSystemDetailsPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {

    private static final Logger LOG = Logger.getLogger(InspectSystemDetailsPanel.class);
    private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);

    protected DetailedSystemPlot plot;
    protected CDMAPlotModel model;

    protected double clickRadius = 0.11; // 110 meters
    protected int offsetX = 0;
    protected int offsetY = 0;
    protected int selectedSector = 0;

    private final CDMAInterfererTableModel interfererTableModel = new CDMAInterfererTableModel();
    private final CDMALinkInfoTableModel linkTableModel = new CDMALinkInfoTableModel();
    private final CDMASystemTableModel systemTableModel = new CDMASystemTableModel();
    private final CDMACellTableModel cellTableModel = new CDMACellTableModel();
    private final CDMAUserInfoTableModel userTableModel = new CDMAUserInfoTableModel();

    private final ExternalInteferenceLinkTableModel externalInteferenceLinkTableModel = new ExternalInteferenceLinkTableModel();

    protected SnapshotVectorsListModel vectorModel;
    private boolean showVectorDetails = true;
    private int eventNumber;

    public InspectSystemDetailsPanel(int eventNumber) {
        this.eventNumber = eventNumber;
        initComponents();
    }

    public void addSystemSelectionListener(ActionListener act) {
        systemSelector.addActionListener(act);
    }

    public void reset() {
        updateSummaryLabels(0, 0, 0, 0);
        setDetailedSystemPlot(null);
    }

    public void updateSummaryLabels(int active, int dropped, int conn, int notConn) {
        totalUsersLabel.setText(Integer.toString(active));
        droppedUsersLabel.setText(Integer.toString(dropped));
        connectedUsersLabel.setText((conn + notConn) + " [" + conn + " / " + notConn + "]");
    }

    public void setSystemSelectionModel(CDMASystemsComboBoxModel model) {
        systemSelector.setModel(model);
    }

    public void setDetailedSystemPlot(DetailedSystemPlot _plot) {
        plot = _plot;
        if (plot == null) {
            model = null;
            userTableModel.setUserTerminal(null);
            cellTableModel.setSelectedCell(null);
            interfererTableModel.setSelectedInterferer(null);
        } else {
            model = plot.getModel();
            vectorList.setSelectedValue(null,false);
            vectorModel = new SnapshotVectorsListModel(model);
            if (vectorModel.getSize() > 0) {
                vectorList.setModel(vectorModel);
                showVectorDetails = true;
                vectorList.setToolTipText("Double click a vector to see the details");
            } else {
                //System has no active users and therefore there are no vectors to show
                showVectorDetails = false;
                vectorList.setToolTipText("Simulated system has no active connections");
            }

            CellularSystem system = model.getCellularSystem();
            int active = model.getActiveUsers().size();
            int inActive = model.getInactiveUsers().size();
            int dropped = model.getDroppedUsers().size();
            updateSummaryLabels(active + inActive + dropped,
                    dropped,
                    active,
                    inActive);

            systemTableModel.setDmaSystem(model);
            userTableModel.setUserTerminal(null);
            cellTableModel.setSelectedCell(null);
            interfererTableModel.setSelectedInterferer(null);

            plot.setSelectedCell(null);
            plot.setSelectedUser(null);
            plot.setSelectedInterferer(null);
            plot.addMouseListener(this);
            plot.addMouseMotionListener(this);
            plot.addMouseWheelListener(this);

            boolean enable = system.getLayout().getSectorSetup() != CellularLayout.SectorSetup.SingleSector;
            sector1.setEnabled(enable);
            sector2.setEnabled(enable);
            sector3.setEnabled(enable);
            sectorLabel.setEnabled(enable);
        }
    }

    private void zoomFactorSliderStateChanged(ChangeEvent e) {
        zoomFactorLabel.setText("Zoom Factor: " + zoomFactorSlider.getValue() + "%");
        plot.setZoomFactor(zoomFactorSlider.getValue() / 100.0);
        plot.repaint();
    }

    private void clickRadiusSliderStateChanged(ChangeEvent e) {
//		clickRadiusLabel.setText("Click Radius: " + clickRadiusSlider.getValue() + " meters");
    }

    private void vectorListMouseClicked(MouseEvent e) {
        if (vectorList.getSelectedValue() != null) {
            if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1 && showVectorDetails ) {
                String title = (String) vectorList.getSelectedValue();
                String unit = vectorModel.getUnit(title);
                String label = vectorModel.getLabel(title);
                double[] data  = vectorModel.getVectorData(title);


                DialogDisplaySignal dialogDisplaySignal = new DialogDisplaySignal(MainWindow.getInstance(),
                        STRINGLIST.getString("VECTOR_GRAPH_AXIX_TITLE_X"), STRINGLIST.getString("VECTOR_GRAPH_AXIX_TITLE_Y"));
                dialogDisplaySignal.displayDataSelectionPanel(false);
                dialogDisplaySignal.show(data, title, unit, label);
            }
        }
    }

    private void sector1ActionPerformed() {
        if (sector1.isSelected()) {
            setSelectedSector(0);
        }
    }

    private void sector2ActionPerformed() {
        if (sector2.isSelected()) {
            setSelectedSector(1);
        }
    }

    private void sector3ActionPerformed() {
        if (sector3.isSelected()) {
            setSelectedSector(2);
        }
    }

    private void initComponents() {
        ResourceBundle bundle = ResourceBundle.getBundle("org.seamcat.presentation.localization");
        xCollapsiblePane1 = new JPanel();
        totalUserTextLabel = new JLabel();
        totalUsersLabel = new JLabel();
        connectedUsersTextLabel = new JLabel();
        connectedUsersLabel = new JLabel();
        droppedUsersTextLabel = new JLabel();
        droppedUsersLabel = new JLabel();
        label3 = new JLabel();
        systemSelector = new JComboBox();
        sectorLabel = new JLabel();
        sector1 = new JRadioButton();
        sector2 = new JRadioButton();
        sector3 = new JRadioButton();
        selectedLabel = new JLabel();
        tabbedPane1 = new JTabbedPane();
        DetailsTablePanel = new JPanel();
        scrollPane2 = new JScrollPane();
        detailsTable = new JTable();
        vectorListPanel = new JPanel();
        scrollPane1 = new JScrollPane();
        vectorList = new JList();
        plotSettingsPanel = new JPanel();
        label5 = new JLabel();
        clickRadiusSlider = new JSlider();
        zoomFactorLabel = new JLabel();
        zoomFactorSlider = new JSlider();
        CellConstraints cc = new CellConstraints();

        //======== this ========
        setLayout(new FormLayout(
                "left:121dlu, $lcgap, 49dlu:grow",
                "top:144dlu, $lgap, top:14dlu, $lgap, top:default:grow"));

        //======== xCollapsiblePane1 ========
        {
            xCollapsiblePane1.setLayout(new FormLayout(
                    "left:55dlu, $lcgap, left:57dlu, $lcgap, 54dlu:grow",
                    "8*(default, $lgap), default"));

            //---- totalUserTextLabel ----
            totalUserTextLabel.setText(bundle.getString("CdmaDetailsPanel.totalUserTextLabel.text"));
            xCollapsiblePane1.add(totalUserTextLabel, cc.xywh(1, 1, 3, 1));

            //---- totalUsersLabel ----
            totalUsersLabel.setText(bundle.getString("CdmaDetailsPanel.totalUsersLabel.text"));
            xCollapsiblePane1.add(totalUsersLabel, cc.xy(5, 1));

            //---- connectedUsersTextLabel ----
            connectedUsersTextLabel.setText(bundle.getString("CdmaDetailsPanel.connectedUsersTextLabel.text"));
            xCollapsiblePane1.add(connectedUsersTextLabel, cc.xywh(1, 3, 4, 1));

            //---- connectedUsersLabel ----
            connectedUsersLabel.setText(bundle.getString("CdmaDetailsPanel.connectedUsersLabel.text"));
            xCollapsiblePane1.add(connectedUsersLabel, cc.xy(5, 3));

            //---- droppedUsersTextLabel ----
            droppedUsersTextLabel.setText(bundle.getString("CdmaDetailsPanel.droppedUsersTextLabel.text"));
            xCollapsiblePane1.add(droppedUsersTextLabel, cc.xywh(1, 5, 3, 1));

            //---- droppedUsersLabel ----
            droppedUsersLabel.setText(bundle.getString("CdmaDetailsPanel.droppedUsersLabel.text"));
            xCollapsiblePane1.add(droppedUsersLabel, cc.xy(5, 5));

            //---- label3 ----
            label3.setText(bundle.getString("CdmaDetailsPanel.label3.text"));
            xCollapsiblePane1.add(label3, cc.xywh(1, 7, 2, 1));
            xCollapsiblePane1.add(systemSelector, cc.xywh(3, 7, 3, 1));

            //---- sectorLabel ----
            sectorLabel.setText(bundle.getString("CdmaDetailsPanel.sectorLabel.text"));
            xCollapsiblePane1.add(sectorLabel, cc.xy(1, 9));

            //---- sector1 ----
            sector1.setText(bundle.getString("CdmaDetailsPanel.sector1.text"));
            sector1.setSelected(true);
            sector1.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    sector1ActionPerformed();
                }
            });
            xCollapsiblePane1.add(sector1, cc.xywh(3, 9, 3, 1));

            //---- sector2 ----
            sector2.setText(bundle.getString("CdmaDetailsPanel.sector2.text"));
            sector2.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    sector2ActionPerformed();
                }
            });
            xCollapsiblePane1.add(sector2, cc.xywh(3, 11, 3, 1));

            //---- sector3 ----
            sector3.setText(bundle.getString("CdmaDetailsPanel.sector3.text"));
            sector3.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    sector3ActionPerformed();
                }
            });
            xCollapsiblePane1.add(sector3, cc.xywh(3, 13, 3, 1));
        }
        add(new ScrollingBorderPanel(xCollapsiblePane1,bundle.getString("CdmaDetailsPanel_SUMMARY") + " " + eventNumber), cc.xywh(1, 1, 3, 4));
        add(selectedLabel, cc.xy(1, 3));

        //======== tabbedPane1 ========
        {
            //======== DetailsTablePanel ========
            {
                DetailsTablePanel.setLayout(new FormLayout(
                        "default",
                        "default"));

                //======== scrollPane2 ========
                {
                    scrollPane2.setViewportView(detailsTable);
                }
                DetailsTablePanel.add(scrollPane2, cc.xy(1, 1));
            }
            tabbedPane1.addTab(bundle.getString("CdmaDetailsPanel.DetailsTablePanel.tab.title"), DetailsTablePanel);


            //======== vectorListPanel ========
            {
                vectorListPanel.setLayout(new FormLayout(
                        "106dlu:grow",
                        "fill:default:grow"));

                //======== scrollPane1 ========
                {

                    //---- vectorList ----
                    //vectorList.setFilterEnabled(true);
                    vectorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    vectorList.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            vectorListMouseClicked(e);
                        }
                    });
                    scrollPane1.setViewportView(vectorList);
                }
                vectorListPanel.add(scrollPane1, cc.xy(1, 1));
            }
            tabbedPane1.addTab(bundle.getString("CdmaDetailsPanel.vectorListPanel.tab.title"), vectorListPanel);


            //======== plotSettingsPanel ========
            {
                plotSettingsPanel.setLayout(new FormLayout(
                        "default, $lcgap, default",
                        "2*(default, $lgap), default"));

                //---- label5 ----
                label5.setText(bundle.getString("CdmaDetailsPanel.label5.text"));
                plotSettingsPanel.add(label5, cc.xy(1, 1));

                //---- clickRadiusSlider ----
                clickRadiusSlider.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        clickRadiusSliderStateChanged(e);
                    }
                });
                plotSettingsPanel.add(clickRadiusSlider, cc.xy(3, 1));

                //---- zoomFactorLabel ----
                zoomFactorLabel.setText(bundle.getString("CdmaDetailsPanel.zoomFactorLabel.text"));
                plotSettingsPanel.add(zoomFactorLabel, cc.xy(1, 3));

                //---- zoomFactorSlider ----
                zoomFactorSlider.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        zoomFactorSliderStateChanged(e);
                    }
                });
                plotSettingsPanel.add(zoomFactorSlider, cc.xy(3, 3));
            }
            tabbedPane1.addTab(bundle.getString("CdmaDetailsPanel.plotSettingsPanel.tab.title"), plotSettingsPanel);

        }
        add(new BorderPanel(tabbedPane1,bundle.getString("CdmaDetailsPanel.tabbedPane1.border")), cc.xywh(1, 5, 3, 1));

        //---- buttonGroup1 ----
        ButtonGroup buttonGroup1 = new ButtonGroup();
        buttonGroup1.add(sector1);
        buttonGroup1.add(sector2);
        buttonGroup1.add(sector3);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel xCollapsiblePane1;
    private JLabel totalUserTextLabel;
    private JLabel totalUsersLabel;
    private JLabel connectedUsersTextLabel;
    private JLabel connectedUsersLabel;
    private JLabel droppedUsersTextLabel;
    private JLabel droppedUsersLabel;
    private JLabel label3;
    private JComboBox systemSelector;
    private JLabel sectorLabel;
    private JRadioButton sector1;
    private JRadioButton sector2;
    private JRadioButton sector3;
    private JLabel selectedLabel;
    private JTabbedPane tabbedPane1;
    private JPanel DetailsTablePanel;
    private JScrollPane scrollPane2;
    private JTable detailsTable;
    private JPanel vectorListPanel;
    private JScrollPane scrollPane1;
    private JList vectorList;
    private JPanel plotSettingsPanel;
    private JLabel label5;
    private JSlider clickRadiusSlider;
    private JLabel zoomFactorLabel;
    private JSlider zoomFactorSlider;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    private boolean isClickHit(double x, double y, SimulationElement u) {
        return isClickHit(x,y,u.getPosition().getX(), u.getPosition().getY());
    }


    private boolean isClickHit(double x1, double y1, double x2, double y2) {
        return Math.abs(x2 - x1) < clickRadius
                && Math.abs(y2 - y1) < clickRadius;
    }

    public void mouseClicked(MouseEvent e) {
        // First check if this event was a double right click
        if (e.getButton() == MouseEvent.BUTTON3) {
            if (e.getClickCount() == 2) {
                zoomFactorSlider.setValue(100);
                zoomFactorLabel.setText("Zoom Factor: 100%");
                plot.resetView();
            }
            return;
        }
        // Check if system has been set
        if (model == null) {
            return;
        }
        // Resolve click by finding any element at click position
        clickRadius = clickRadiusSlider.getValue() / 1000.0;
        List<? extends AbstractDmaMobile> users = model.getActiveUsers();
        double x = (e.getX() - plot.getTranslateX() - plot.getFocusShiftX())
                / plot.getScaleFactor();
        double y = -((e.getY() - plot.getTranslateY() - plot.getFocusShiftY()) / plot
                .getScaleFactor());

        // Check if link is selected
        if (e.isControlDown()) {
            if (plot.getSelectedUser() != null) {
                AbstractDmaMobile user = plot.getSelectedUser();
                if (plot.isPlotExternalInterferers() && user instanceof DownlinkOfdmaMobile) {
                    DownlinkOfdmaMobile duser = (DownlinkOfdmaMobile) user;
                    for (OfdmaExternalInterferer ext : duser.getExternalInterferers()) {
                        if (isClickHit(x, y, ext.getInterferer().getPoint().getX(), ext.getInterferer().getPoint().getY())) {
                            externalInteferenceLinkTableModel.setLink(ext);
                            detailsTable.setModel(externalInteferenceLinkTableModel);
                            selectedLabel.setText("External Interference Link");

                            plot.setSelectedInterferer(ext.getInterferer());

                            plot.setSelectedCell(null);
                            plot.setSelectedInterferer(null);
                            plot.repaint();
                            return;
                        }
                    }
                }


                for (AbstractDmaLink link : user.getActiveList()) {
                    if (isClickHit(x, y, link.getBaseStation())) {
                        linkTableModel.setCDMALink(link);
                        detailsTable.setModel(linkTableModel);
                        selectedLabel.setText("Active Link");
                        plot.setSelectedLink(link);
                        plot.setSelectedUser(link.getUserTerminal());
                        plot.setSelectedCell(null);
                        plot.setSelectedInterferer(null);
                        plot.repaint();
                        return;
                    }
                }
                for (AbstractDmaLink link :  user.getAllLinks()) {
                    if (!user.getActiveList().contains(link)) {
                        if (isClickHit(x, y, link.getBaseStation())) {
                            linkTableModel.setCDMALink(link);
                            detailsTable.setModel(linkTableModel);
                            selectedLabel.setText("Inactive Link");
                            plot.setSelectedLink(link);
                            plot.setSelectedUser(link.getUserTerminal());
                            plot.setSelectedCell(null);
                            plot.setSelectedInterferer(null);
                            plot.repaint();
                            return;
                        }
                    }
                }
            }
            if (plot.getSelectedCell() != null) {
                AbstractDmaBaseStation cell = plot.getSelectedCell();
                for (AbstractDmaLink link : cell.getOldTypeActiveConnections()) {
                    if (isClickHit(x, y, link.getUserTerminal())) {
                        linkTableModel.setCDMALink( link);
                        detailsTable.setModel(linkTableModel);
                        selectedLabel.setText("Active Link");
                        plot.setSelectedLink(link);
                        plot.setSelectedUser(link.getUserTerminal());
                        plot.setSelectedInterferer(null);
                        plot.repaint();
                        return;
                    }
                }
                for (AbstractDmaLink link : cell.getDroppedUsers()) {
                    if (isClickHit(x, y, link.getBaseStation())) {
                        linkTableModel.setCDMALink( link);
                        detailsTable.setModel(linkTableModel);
                        selectedLabel.setText("Link to dropped user");
                        plot.setSelectedLink( link);
                        plot.setSelectedUser(link.getUserTerminal());
                        plot.setSelectedInterferer(null);
                        plot.repaint();
                        return;
                    }
                }
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Clicked button " + e.getButton() + ": (" + x + ", " + y + ")");
        }
        // Search active connected users:
        if (plot.isPlotUsers()) {
            for (int i = 0, stop = users.size(); i < stop; i++) {
                AbstractDmaMobile u = users.get(i);
                if (isClickHit(x, y, u)) {
                    userTableModel.setUserTerminal(u);
                    detailsTable.setModel(userTableModel);
                    selectedLabel.setText("Connected - Voice Active User");
                    plot.setSelectedUser(u);
                    plot.setSelectedCell(null);
                    plot.setSelectedInterferer(null);
                    plot.setSelectedLink(null);
                    plot.repaint();
                    return;
                }
            }
            // Search inactive users:
            users = model.getInactiveUsers();
            for (int i = 0, stop = users.size(); i < stop; i++) {
                AbstractDmaMobile u = users.get(i);
                if (isClickHit(x, y, u)) {
                    userTableModel.setUserTerminal(u);
                    detailsTable.setModel(userTableModel);
                    selectedLabel.setText("Connected - Inactive User");
                    plot.setSelectedUser(u);
                    plot.setSelectedCell(null);
                    plot.setSelectedInterferer(null);
                    plot.setSelectedLink(null);
                    plot.repaint();
                    return;
                }
            }

        }
        // Search dropped users:
        if (plot.isPlotDroppedUsers()) {
            for (AbstractDmaMobile u : model.getDroppedUsers()) {
                if (isClickHit(x, y, u)) {
                    userTableModel.setUserTerminal(u);
                    detailsTable.setModel(userTableModel);
                    selectedLabel.setText("Dropped User");
                    plot.setSelectedUser(u);
                    plot.setSelectedCell(null);
                    plot.setSelectedInterferer(null);
                    plot.setSelectedLink(null);
                    plot.repaint();
                    return;
                }
            }
        }
        if (plot.isPlotCellCenter()) {
            AbstractDmaBaseStation[][] cells = model.getBaseStations();
            for (int i = 0, stop = cells.length; i < stop; i++) {
                AbstractDmaBaseStation c = cells[i][selectedSector];
                if (Math.abs(c.getPosition().getX() - x) < clickRadius && Math.abs(c.getPosition().getY() - y) < clickRadius) {
                    cellTableModel.setSelectedCell(c);
                    detailsTable.setModel(cellTableModel);
                    selectedLabel.setText("BaseStation #" + c.getCellid());
                    plot.setSelectedCell(c);
                    plot.setSelectedUser(null);
                    plot.setSelectedInterferer(null);
                    plot.setSelectedLink(null);
                    plot.repaint();
                    return;
                }
            }
        }
        if (plot.isPlotExternalInterferers()) {
            for ( Interferer interferer : model.getExternalInterferers() ) {
                ActiveInterferer ei = (ActiveInterferer) interferer;
                if (Math.abs(ei.getPoint().getX() - x) < clickRadius
                        && Math.abs(ei.getPoint().getY() - y) < clickRadius) {
                    interfererTableModel.setSelectedInterferer(ei);
                    detailsTable.setModel(interfererTableModel);
                    selectedLabel.setText("CDMA Interferer");
                    plot.setSelectedCell(null);
                    plot.setSelectedUser(null);
                    plot.setSelectedInterferer(ei);
                    plot.setSelectedLink(null);
                    plot.repaint();
                    return;
                }
            }
        }
        if (plot.getSelectedCell() != null) { // Cell is selected and no clicked
            // element was found
            List<AbstractDmaLink> cellUsers = plot.getSelectedCell().getOldTypeActiveConnections();
            for (AbstractDmaLink link : cellUsers) {
                AbstractDmaMobile u = link.getUserTerminal();
                if (isClickHit(x, y, u)) {
                    userTableModel.setUserTerminal(u);
                    detailsTable.setModel(userTableModel);
                    selectedLabel.setText("Connected - Voice Active User");
                    plot.setSelectedUser(u);
                    // plot.setSelectedCell(null);
                    plot.setSelectedInterferer(null);
                    plot.setSelectedLink(null);
                    plot.repaint();
                    return;
                }
            }
            // Search dropped users:
            cellUsers = plot.getSelectedCell().getDroppedUsers();
            for (AbstractDmaLink link : cellUsers) {
                AbstractDmaMobile u = link.getUserTerminal();
                if (isClickHit(x, y, u)) {
                    userTableModel.setUserTerminal(u);
                    detailsTable.setModel(userTableModel);
                    selectedLabel.setText("Dropped User");
                    plot.setSelectedUser(u);
                    // plot.setSelectedCell(null);
                    plot.setSelectedInterferer(null);
                    plot.setSelectedLink(null);
                    plot.repaint();
                    return;
                }
            }
        }
        selectedLabel.setText("Click on system element to view details");
        plot.setSelectedCell(null);
        plot.setSelectedUser(null);
        plot.setSelectedInterferer(null);
        plot.setSelectedLink(null);
        detailsTable.setModel(systemTableModel);
        plot.repaint();
        if (detailsTable.getModel() == cellTableModel) {
            cellTableModel.setSelectedCell(null);
        } else if (detailsTable.getModel() == userTableModel) {
            userTableModel.setUserTerminal(null);
        } else if (detailsTable.getModel() == interfererTableModel) {
            interfererTableModel.setSelectedInterferer(null);
        }
    }

    public void mouseDragged(MouseEvent e) {
        int x = e.getX() - offsetX;
        int y = e.getY() - offsetY;
        if (plot != null) {
            plot.adjustFocusShiftX(x);
            plot.adjustFocusShiftY(y);
            plot.repaint();
            offsetX = e.getX();
            offsetY = e.getY();
        }
    }

    public void mouseEntered(MouseEvent e) {
        // Do nothing
    }

    public void mouseExited(MouseEvent e) {
        // Do nothing
    }

    public void mouseMoved(MouseEvent e) { }

    public void mousePressed(MouseEvent e) {
        offsetX = e.getX();
        offsetY = e.getY();
    }

    public void mouseReleased(MouseEvent e) {
        // Do nothing
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            plot.adjustZoom(e.getUnitsToScroll());
            zoomFactorSlider.setValue((int) (plot.getZoomFactor() * 100));
            zoomFactorLabel.setText("Zoom Factor: " + plot.getZoomFactor() * 100
                    + "%");
            plot.repaint();
        }
    }

    public void setSelectedSector(int selectedSector) {
        this.selectedSector = selectedSector;

        if (plot.getSelectedCell() != null) {
            AbstractDmaBaseStation c = model.getBaseStations()[cellTableModel.getSelectedCell().getCellLocationId()][selectedSector];
            cellTableModel.setSelectedCell(c);
            detailsTable.setModel(cellTableModel);
            selectedLabel.setText("BaseStation #" + c.getCellid());
            plot.setSelectedCell(c);
            plot.setSelectedUser(null);
            plot.setSelectedInterferer(null);
            plot.repaint();
        }
    }


}
