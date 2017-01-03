package org.seamcat.presentation.batch;

import org.seamcat.batch.BatchJobList;
import org.seamcat.commands.CalculateInterferenceCommand;
import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.eventbus.UIEventHandler;
import org.seamcat.events.ICECriterionChanged;
import org.seamcat.events.ICEProbabilityResultEvent;
import org.seamcat.events.ICESignalTypeChanged;
import org.seamcat.model.Workspace;
import org.seamcat.model.engines.ICEConfiguration;
import org.seamcat.model.simulation.result.SimulationResult;
import org.seamcat.model.types.result.SingleValueTypes;
import org.seamcat.presentation.builder.AsActionListener;
import org.seamcat.presentation.builder.PanelBuilder;
import org.seamcat.presentation.components.BorderPanel;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

public class BatchResultsPanel extends JPanel {

    private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", Locale.ENGLISH);
    private final JComboBox criterionBox;
    private GenericTableModel dm;
    private CdmaTableModel cdmaDm;
    private OfdmaTableModel ofdmaDm;

    private BatchJobList jobs;

    public BatchResultsPanel(BatchJobList jobs) {
        this.jobs = jobs;
        criterionBox = new JComboBox();
        for ( int i=1; i<5; i++ ) {
            criterionBox.addItem(new CriterionItem(i));
        }
        //render();
        EventBusFactory.getEventBus().subscribe(this);
    }

    public void render() {
        removeAll();
        dm = new GenericTableModel();
        cdmaDm = new CdmaTableModel();
        ofdmaDm = new OfdmaTableModel();

        List<Workspace> generic = new ArrayList<>();
        List<Workspace> cdma    = new ArrayList<>();
        List<Workspace> ofdma   = new ArrayList<>();
        for (Workspace item : jobs.getBatchJobs()) {
            if ( item.getVictimSystemLink().isCDMASystem() ) {
                cdma.add( item );
            } else if ( item.getVictimSystemLink().isOFDMASystem() ){
                ofdma.add(item);
            } else {
                generic.add(item);
            }
        }
        List<BorderPanel> panels = new ArrayList<BorderPanel>();
        int types = 0;
        if ( !generic.isEmpty() ) {
            panels.add(layoutGeneric(dm));
            types++;
            for (Workspace item : generic) {
                dm.addRow(item);
            }
        }
        if ( !cdma.isEmpty() ) {
            types++;
            panels.add( layoutDma( cdmaDm, "CDMA Victim"));
            for (Workspace item : cdma) {
                cdmaDm.addRow( item );
            }
        }
        if ( !ofdma.isEmpty() ) {
            types++;
            panels.add( layoutDma(ofdmaDm, "OFDMA Victim"));
            for (Workspace item : ofdma) {
                ofdmaDm.addRow( item );
            }
        }

        setLayout(new GridLayout(types, 1));
        for (BorderPanel panel : panels) {
            add( panel);
        }

    }

    private BorderPanel layoutDma( ResultsTableModel dm, String name ) {
        JTable table = new JTable(dm);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return new BorderPanel( new JScrollPane(table), name);
    }


    private BorderPanel layoutGeneric( final GenericTableModel dm ) {
        JTable table = new JTable(dm);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        createHeader(table, 2, "Unwanted", dm);
        createHeader(table, 3, "Blocking", dm);
        createHeader(table, 4, "Overloading", dm);
        createHeader(table, 5, "Intermodulation", dm);

        TableColumn column = table.getColumnModel().getColumn(1);
        column.setCellEditor(new DefaultCellEditor(criterionBox));
        JPanel generic = new JPanel(new BorderLayout());
        generic.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel alignLeft = new JPanel(new BorderLayout());
        JPanel jPanel = new JPanel(new FlowLayout());
        jPanel.add(PanelBuilder.buildButton(STRINGLIST.getString("BATCH_ANALYSE"), this, "analyse"));
        alignLeft.add(jPanel, BorderLayout.WEST);
        generic.add(alignLeft, BorderLayout.SOUTH);
        return new BorderPanel( generic, "Generic Victim");
    }

    private void createHeader(JTable table, final int index, String name, final GenericTableModel dm ) {
        final JCheckBox box = new JCheckBox(name+"    all/none");
        box.setHorizontalTextPosition(SwingConstants.LEFT);
        box.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean selected = box.isSelected();
                for (int i = 0; i < dm.getRowCount(); i++) {
                    dm.setValueAt(selected, i, index);
                }
                dm.fireTableDataChanged();
            }
        });
        table.getColumnModel().getColumn(index).setHeaderRenderer(new CriteriaHeaderRenderer(box));
    }

    @AsActionListener("analyse")
    private void analyse() {
        dm.analyse();
    }

    public void destroy() {
        EventBusFactory.getEventBus().unsubscribe( this );
    }

    @UIEventHandler
    public void handle( ICECriterionChanged event ) {
        if ( event.getOrigin() == this ) return;
        dm.updateRow( event.getIce() );
    }

    @UIEventHandler
    public void handle( ICESignalTypeChanged event ) {
        if ( event.getOrigin() == this ) return;
        dm.updateRow( event.getIce());
    }

    @UIEventHandler
    public void handle( ICEProbabilityResultEvent event ) {
        dm.updateRow( event.getIce() );
    }

    class CdmaTableModel extends ResultsTableModel {
        private String cdma_column_names[]    = { "Name", "Ref. cell capacity loss", "System capacity loss", "Optimum number of UE", "#Events" };

        @Override
        public String[] getColumnNames() {
            return cdma_column_names;
        }

        @Override
        public void addRow(Workspace item) {
            String name = item.getName();
            List<SingleValueTypes<?>> values = item.getSimulationResults().getSeamcatResult(SimulationResult.CDMA_RESULTS).getResultTypes().getSingleValueTypes();
            double refCellCapLoss = (Double) values.get(0).getValue();
            double systemCapLoss = (Double) values.get(1).getValue();
            int optimum = ((Double) values.get(2).getValue()).intValue();
            int events = item.getSimulationControl().numberOfEvents();
            Object[] objects = { name, refCellCapLoss, systemCapLoss, optimum, events};
            getRows().add( objects );
        }
    }

    class OfdmaTableModel extends ResultsTableModel {

        private String ofdma_column_names[]   = { "Name", "Ref. cell average bit rate loss", "System average bit rate loss", "#Events" };

        @Override
        public String[] getColumnNames() {
            return ofdma_column_names;
        }

        @Override
        public void addRow(Workspace item) {
            String name = item.getName();
            List<SingleValueTypes<?>> values = item.getSimulationResults().getSeamcatResult(SimulationResult.OFDMA_RESULTS).getResultTypes().getSingleValueTypes();
            double refCellBitRateLoss = (Double) values.get(0).getValue();
            double systemBitRateLoss = (Double) values.get(1).getValue();
            int events = item.getSimulationControl().numberOfEvents();
            Object[] objects = { name, refCellBitRateLoss, systemBitRateLoss, events };
            getRows().add( objects );
        }
    }

    class GenericTableModel extends ResultsTableModel {

        private String generic_column_names[] = {"Name", "Criterion", "Unwanted", "Blocking", "Overloading", "Intermodulation", "Probability", "#Events"};
        private Map<Object[], ICEConfiguration> map = new HashMap<Object[], ICEConfiguration>();

        @Override
        public String[] getColumnNames() {
            return generic_column_names;
        }

        public void updateRow( ICEConfiguration updatedICE ) {
            for (Map.Entry<Object[], ICEConfiguration> entry : map.entrySet()) {
                if ( entry.getValue() == updatedICE ) {
                    update(entry.getKey(), updatedICE );
                    int row = getRows().indexOf(entry.getKey());
                    fireTableRowsUpdated(row, row);
                }
            }
        }

        private void update( Object[] row, ICEConfiguration ice ) {
            DecimalFormat format = new DecimalFormat("#00.00%");
            String prob = format.format(ice.getPropabilityResult());
            String probability = ice.getHasBeenCalculated() ? ""+prob : "Not yet calculated";
            row[1] = new CriterionItem(ice.getInterferenceCriterionType());
            row[2] = ice.isUnwanted();
            row[3] = ice.isBlocking();
            row[4] = ice.isOverloading();
            row[5] = ice.isIntermodulation();
            row[6] = probability;
        }

        @Override
        public void addRow(Workspace item) {
            if ( item.getIceConfigurations() != null &&
                    item.getIceConfigurations().size() > 0)  {
                ICEConfiguration ice = item.getIceConfigurations().get(0);
                Object[] objects = { item.getName(), null, null, null, null, null, null,item.getSimulationControl().numberOfEvents() };
                update(objects, ice);
                getRows().add(objects);
                map.put( objects, ice);
            }
        }

        public void clear() {
            getRows().clear();
            map.clear();
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            Object[] objects = getRows().get(rowIndex);
            objects[columnIndex] = aValue;
            ICEConfiguration ice = map.get(objects);
            if ( columnIndex == 1 ) {
                ice.setInterferenceCriterionType(((CriterionItem) aValue).getCriterion());
                EventBusFactory.getEventBus().publish( new ICECriterionChanged(ice,this));
            } else if ( columnIndex == 2 ) {
                ice.setUnwanted((Boolean) aValue);
                EventBusFactory.getEventBus().publish( new ICESignalTypeChanged(ice,this));
            } else if ( columnIndex == 3 ) {
                ice.setBlocking((Boolean) aValue);
                EventBusFactory.getEventBus().publish( new ICESignalTypeChanged(ice,this));
            } else if ( columnIndex == 4) {
                if ( ice.isAllowingOverloading() ) {
                    ice.setOverloading((Boolean) aValue);
                }
                EventBusFactory.getEventBus().publish( new ICESignalTypeChanged(ice,this));
            } else if ( columnIndex == 5 ) {
                if ( ice.allowIntermodulation()) {
                    ice.setIntermodulation((Boolean) aValue);
                }
                EventBusFactory.getEventBus().publish( new ICESignalTypeChanged(ice,this));
            }
            fireTableCellUpdated( rowIndex, columnIndex );
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if ( columnIndex > 1 && columnIndex < 6) return Boolean.class;
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex > 0 && columnIndex < 6;
        }

        public void analyse() {
            for (ICEConfiguration configuration : map.values()) {
                // maybe a problem to calculate in parallel
                EventBusFactory.getEventBus().publish( new CalculateInterferenceCommand( configuration ));
            }
        }
    }


    abstract class ResultsTableModel extends AbstractTableModel {

        abstract public String[] getColumnNames();
        abstract public void addRow(Workspace item);

        private List<Object[]> rows = new ArrayList<Object[]>();

        protected List<Object[]> getRows() {
            return rows;
        }

        @Override
        public int getRowCount() {
            return getRows().size();
        }

        @Override
        public int getColumnCount() {
            return getColumnNames().length;
        }

        @Override
        public String getColumnName(int column) {
            return getColumnNames()[column];
        }

        @Override
        public Object getValueAt(int row, int col) {
            return getRows().get(row)[col];
        }

    }

}