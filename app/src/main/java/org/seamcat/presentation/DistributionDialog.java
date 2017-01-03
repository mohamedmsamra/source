package org.seamcat.presentation;

import org.apache.log4j.Logger;
import org.seamcat.calculator.CalculatorInputField;
import org.seamcat.function.DiscreteFunction;
import org.seamcat.function.WithPoints;
import org.seamcat.help.SeamcatHelpResolver;
import org.seamcat.model.distributions.*;
import org.seamcat.model.functions.Point2D;
import org.seamcat.presentation.components.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.ResourceBundle;

public class DistributionDialog extends EscapeDialog {

    private static final int CONSTANT = 0;
    private static final int DISCRETEUNIFORM = 8;
    private static final int GAUSSIAN = 3;
    private static final String[] LAYOUTS = { "NON_USERDEFINED_LAYOUT", "USERDEFINED_LAYOUT", "USERDEFINED_STAIR_LAYOUT" };

    private static final Logger LOG = Logger.getLogger(DistributionDialog.class);
    private static final int NON_USERDEFINED_LAYOUT = 0;
    private static final int RAYLEIGH = 4;

    private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);
    private static final int UNIFORM = 2;
    private static final int UNIFORMPOLARANGLE = 6;
    private static final int UNIFORMPOLARDISTANCE = 5;
    private static final int USERDEFINED = 1;
    private static final int USERDEFINED_LAYOUT = 1;

    private static final int USERDEFINED_STAIR_LAYOUT = 2;
    private static final int USERDEFINEDSTAIR = 7;

    private DistributionCheck check;
    private final CardLayout contentPanelLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(contentPanelLayout);

    private final DistributionState CONSTANT_STATE = new ConstantDistributionState();
    private final DistributionState DISCRETEUNIFORM_STATE = new DiscreteUniformDistributionState();
    private final DistributionState GAUSSIAN_STATE = new GaussianDistributionState();
    private final DistributionState RAYLEIGH_STATE = new RayleighDistributionState();
    private final DistributionState UNIFORM_STATE = new UniformDistributionState();
    private final DistributionState UNIFORMPOLARANGLE_STATE = new UniformPolarAngleDistributionState();
    private final DistributionState UNIFORMPOLARDISTANCE_STATE = new UniformPolarDistanceDistributionState();
    private final DistributionState USERDEFINED_STATE = new UserDefinedDistributionState();

    private ParametersPanel parametersPanel = new ParametersPanel();

    private StairDistributionPanel stairDistributionPanel = new StairDistributionPanel();

    private DistributionState state;
    private TypePanel typePanel = new TypePanel();

    private final DistributionCheck userDefinedCheck = new UserDefinedDistributionCheck();

    private UserDefinedFunctionPanel userDefinedFunctionPanel = new UserDefinedFunctionPanel("User defined distribution", "Value", "Probability");

    private final DistributionState USERDEFINEDSTAIR_STATE = new UserDefinedStairDistributionState();

    /** Creates new PowerDistribution form */
    public DistributionDialog(Frame parent, boolean modal) {
        super(parent, modal);
        create();
        setLocationRelativeTo(parent);
    }

    /** Creates new PowerDistribution form */
    public DistributionDialog(JDialog parent, boolean modal) {
        super(parent, modal);
        create();
        setLocationRelativeTo(parent);
    }

    private void create( ) {
        setSize( new Dimension(700,450));
        userDefinedFunctionPanel.getModel().addTableModelListener(new FunctionPointsVerifier());
        userDefinedFunctionPanel.setUsePropabilitySymmetrizeFunction(true);

        contentPanel.add(parametersPanel, LAYOUTS[NON_USERDEFINED_LAYOUT]);
        contentPanel.add(userDefinedFunctionPanel, LAYOUTS[USERDEFINED_LAYOUT]);
        contentPanel.add(stairDistributionPanel, LAYOUTS[USERDEFINED_STAIR_LAYOUT]);

        getContentPane().add(typePanel, BorderLayout.WEST);
        getContentPane().add(new NavigateButtonPanel(this) {
            public void btnOkActionPerformed() {
                if (check != null) {
                    if (!check.validate(state.getDistribution())) {
                        JOptionPane.showMessageDialog(DistributionDialog.this, check.getErrorString(), "Distribution not valid",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
                if (state == USERDEFINED_STATE) {
                    userDefinedFunctionPanel.stopEditing();
                }
                else if (state == USERDEFINEDSTAIR_STATE) {
                    StairDistributionImpl stair = (StairDistributionImpl) state.getDistribution();
                    if (!stair.validate()) {
                        JOptionPane.showMessageDialog(DistributionDialog.this, "Cumulative Probability MUST be between 0 and 1. It also need to include the value 1",
                                "CDF Warning", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    stairDistributionPanel.stopEditing();
                }
                super.btnOkActionPerformed();
            }
        }, BorderLayout.SOUTH);
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        registerHelp();
    }

    private void registerHelp() {
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
        getRootPane().registerKeyboardAction(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                SeamcatHelpResolver.showHelp(DistributionDialog.this);
            }

        }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    public AbstractDistribution getDistribution() {
        if (state != null) {
            return state.getDistribution();
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("There's no active distribution state. Cannot return Distribution object");
            }
            throw new IllegalStateException("No distribution entered");
        }
    }

    protected void setState(DistributionState s) {
        if (state != null) {
            state.leaveState();
        }
        state = s;
        if (state != null) {
            state.enterState();
        }
    }

    public boolean showDistributionDialog(AbstractDistribution d, String windowtitle) {
        return showDistributionDialog(d, windowtitle, null);
    }

    public boolean showDistributionDialog(AbstractDistribution d, String windowtitle, DistributionCheck _check) {
        check = _check != null ? _check : userDefinedCheck;

        // Check for null value
        if (d == null) {
            d = new ConstantDistributionImpl(33);
            if (LOG.isDebugEnabled()) {
                LOG.debug("showDistributionDialog() was called with <null>. Defaulting to ConstantDistribution");
            }
        }

        if (d instanceof ConstantDistributionImpl) {
            setState(CONSTANT_STATE);
        } else if (d instanceof UniformDistributionImpl) {
            setState(UNIFORM_STATE);
        } else if (d instanceof GaussianDistributionImpl) {
            setState(GAUSSIAN_STATE);
        } else if (d instanceof RayleighDistributionImpl) {
            setState(RAYLEIGH_STATE);
        } else if (d instanceof UniformPolarAngleDistributionImpl) {
            setState(UNIFORMPOLARANGLE_STATE);
        } else if (d instanceof UniformPolarDistanceDistributionImpl) {
            setState(UNIFORMPOLARDISTANCE_STATE);
        } else if (d instanceof DiscreteUniformDistributionImpl) {
            setState(DISCRETEUNIFORM_STATE);
        } else if (d instanceof UserDefinedDistributionImpl) {
            setState(USERDEFINED_STATE);
        } else if (d instanceof StairDistributionImpl) {
            setState(USERDEFINEDSTAIR_STATE);
        } else {
            throw new IllegalArgumentException("Unknown distribution type <" + d.getClass() + ">");
        }
        state.setDistribution(d);
        setTitle(windowtitle);
        setAccept( false );
        super.setVisible(true);
        check = null;
        return isAccept();
    }

    public boolean showDistributionDialog(String title) {
        return showDistributionDialog((AbstractDistribution) null, title);
    }

    private class ConstantDistributionState extends NonUserDefinedDistributionState {

        @Override
        public AbstractDistribution getDistribution() {
            return new ConstantDistributionImpl(parametersPanel.getComponentValue(ParametersPanel.CONSTANT));
        }

        @Override
        protected void setComponentState(boolean enabled) {
            if (enabled) {
                typePanel.setSelectedButton(CONSTANT);
            }
            parametersPanel.setComponentState(ParametersPanel.CONSTANT, enabled);
        }

        @Override
        public void setDistribution(AbstractDistribution d) throws IllegalArgumentException {
            if (!(d instanceof ConstantDistributionImpl)) {
                throw new IllegalArgumentException("Instance should be of class ConstantDistribution");
            }
            parametersPanel.setComponentValue(ParametersPanel.CONSTANT, d.getConstant());
        }
    }

    private class DiscreteUniformDistributionState extends NonUserDefinedDistributionState {

        @Override
        public AbstractDistribution getDistribution() {
            return new DiscreteUniformDistributionImpl(parametersPanel.getComponentValue(ParametersPanel.MIN),
                    parametersPanel.getComponentValue(ParametersPanel.MAX), parametersPanel.getComponentValue(
                    ParametersPanel.STEP), parametersPanel.getComponentValue(ParametersPanel.STEPSHIFT));
        }

        @Override
        protected void setComponentState(boolean enabled) {
            if (enabled) {
                typePanel.setSelectedButton(DISCRETEUNIFORM);
            }
            parametersPanel.setComponentState(ParametersPanel.MIN, enabled);
            parametersPanel.setComponentState(ParametersPanel.MAX, enabled);
            parametersPanel.setComponentState(ParametersPanel.STEP, enabled);
            parametersPanel.setComponentState(ParametersPanel.STEPSHIFT, enabled);
        }

        @Override
        public void setDistribution(AbstractDistribution d) throws IllegalArgumentException {
            if (!(d instanceof DiscreteUniformDistributionImpl)) {
                throw new IllegalArgumentException("Instance should be of class DiscreteUniformDistribution");
            }
            parametersPanel.setComponentValue(ParametersPanel.MIN, d.getMin());
            parametersPanel.setComponentValue(ParametersPanel.MAX, d.getMax());
            parametersPanel.setComponentValue(ParametersPanel.STEP, d.getStep());
            parametersPanel.setComponentValue(ParametersPanel.STEPSHIFT, d.getStepShift());
            parametersPanel.writeHelp();
        }
    }

    public static interface DistributionCheck {

        public String getErrorString();

        public boolean validate(AbstractDistribution d);
    }

    private abstract class DistributionState {

        public void enterState() {
            setComponentState(true);
            parametersPanel.writeHelp();
        }

        public abstract AbstractDistribution getDistribution();

        public void leaveState() {
            setComponentState(false);
        }

        protected abstract void setComponentState(boolean enabled);

        public abstract void setDistribution(AbstractDistribution d) throws IllegalArgumentException;
    }

    private static class FunctionPointsVerifier implements TableModelListener {

        public void tableChanged(TableModelEvent t) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Change in column <" + t.getColumn() + "> first-row <" + t.getFirstRow() + "> last-row <"
                        + t.getLastRow() + "> type <" + t.getType() + ">");
            }

            switch (t.getType()) {
                case TableModelEvent.DELETE: {
                    break;
                }
                case TableModelEvent.UPDATE: {
                    if (t.getColumn() == 1) {
                        double value = ((Number) ((TableModel) t.getSource()).getValueAt(t.getFirstRow(), t.getColumn()))
                                .doubleValue();
                        if (value > 1 || value < 0) {
                            JOptionPane.showMessageDialog(null, "Cumulated probability P(X < x) must be between 0 and 1",
                                    "Illegal value", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
                case TableModelEvent.INSERT: {
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Illegal TableModelEvent type. Must be INSERT/UPDATE or DELETE");
                }
            }
        }
    }

    private class GaussianDistributionState extends NonUserDefinedDistributionState {

        @Override
        public AbstractDistribution getDistribution() {
            return new GaussianDistributionImpl(parametersPanel.getComponentValue(ParametersPanel.MEAN),
                    parametersPanel.getComponentValue(ParametersPanel.STDDEV));
        }

        @Override
        protected void setComponentState(boolean enabled) {
            if (enabled) {
                typePanel.setSelectedButton(GAUSSIAN);
            }
            parametersPanel.setComponentState(ParametersPanel.MEAN, enabled);
            parametersPanel.setComponentState(ParametersPanel.STDDEV, enabled);
        }

        @Override
        public void setDistribution(AbstractDistribution d) throws IllegalArgumentException {
            if (!(d instanceof GaussianDistributionImpl)) {
                throw new IllegalArgumentException("Instance should be of class GaussianDistribution");
            }
            parametersPanel.setComponentValue(ParametersPanel.MEAN, new Double(d.getMean()));
            parametersPanel.setComponentValue(ParametersPanel.STDDEV, new Double(d.getStdDev()));
        }
    }

    private abstract class NonUserDefinedDistributionState extends DistributionState {

        @Override
        public void enterState() {
            super.enterState();
            contentPanelLayout.show(contentPanel, LAYOUTS[NON_USERDEFINED_LAYOUT]);
        }
    }

    private class ParametersPanel extends JPanel {

        public static final int CONSTANT = 0;
        public static final int MEAN = 1;
        public static final int STDDEV = 2;
        public static final int MIN = 3;
        public static final int MAX = 4;
        public static final int MAXDISTANCE = 5;
        public static final int MAXANGLE = 6;
        public static final int STEP = 7;
        public static final int STEPSHIFT = 8;

        private JLabel lblConstant = new JLabel(STRINGLIST.getString("DISTRIBUTION_CONSTANT"));
        private JLabel lblMax = new JLabel(STRINGLIST.getString("DISTRIBUTION_PARAM_MAX"));
        private JLabel lblMaxAngle = new JLabel(STRINGLIST.getString("DISTRIBUTION_PARAM_MAXANGLE"));
        private JLabel lblMaxDistance = new JLabel(STRINGLIST.getString("DISTRIBUTION_PARAM_MAXDIST"));
        private JLabel lblMean = new JLabel(STRINGLIST.getString("DISTRIBUTION_PARAM_MEAN"));
        private JLabel lblMin = new JLabel(STRINGLIST.getString("DISTRIBUTION_PARAM_MIN"));
        private JLabel lblStdDev = new JLabel(STRINGLIST.getString("DISTRIBUTION_PARAM_STDDEV"));
        private JLabel lblStep = new JLabel(STRINGLIST.getString("DISTRIBUTION_PARAM_STEP"));
        private JLabel lblStepShift = new JLabel(STRINGLIST.getString("DISTRIBUTION_STEP_SHIFT"));
        private JLabel shiftNote = new JLabel();

        private JFormattedTextField tfConstant = new CalculatorInputField(30);
        private JFormattedTextField tfMax = new CalculatorInputField(1);
        private JFormattedTextField tfMaxAngle = new CalculatorInputField(360);
        private JFormattedTextField tfMaxDistance = new CalculatorInputField(1);
        private JFormattedTextField tfMean = new CalculatorInputField(0);
        private JFormattedTextField tfMin = new CalculatorInputField(0);
        private JFormattedTextField tfStdDev = new CalculatorInputField(0);
        private JFormattedTextField tfStep = new CalculatorInputField(0.2d);
        private JFormattedTextField tfStepShift = new CalculatorInputField(0.0d);
        private NumberFormat nf = NumberFormat.getNumberInstance();

        public void writeHelp() {
            boolean enabled = typePanel.btnDiscreteUniform.isSelected();
            if (enabled) {
                // calculate help text:
                double min = parametersPanel.getComponentValue(ParametersPanel.MIN);
                double max = parametersPanel.getComponentValue(ParametersPanel.MAX);
                double step = parametersPanel.getComponentValue(ParametersPanel.STEP);
                double shift = parametersPanel.getComponentValue(ParametersPanel.STEPSHIFT);
                double first = min + shift;
                double second = min + shift + step;
                double last = max - step + shift;
                if ( Math.abs(shift) < 0.000001 ) {
                    parametersPanel.setShiftText("["+nf.format(first)+", "+ nf.format(second)+", ..., "+nf.format(max)+"]");
                } else {
                    parametersPanel.setShiftText("["+nf.format(first)+", "+ nf.format(second)+", ..., "+nf.format(last)+"]");
                }
            } else {
                parametersPanel.setShiftText("");
            }// update step shift
        }

        public ParametersPanel() {
            super(new FlowLayout(FlowLayout.LEFT));

            FocusListener listener = new FocusAdapter() {
                public void focusLost(FocusEvent e) {
                    writeHelp();
                }
            };
            tfMin.addFocusListener(listener);
            tfMax.addFocusListener(listener);
            tfStep.addFocusListener(listener);
            tfStepShift.addFocusListener(listener);

            lblConstant.setLabelFor(tfConstant);
            lblMean.setLabelFor(tfMean);
            lblStdDev.setLabelFor(tfStdDev);
            lblMin.setLabelFor(tfMin);
            lblMax.setLabelFor(tfMax);
            lblMaxDistance.setLabelFor(tfMaxDistance);
            lblMaxAngle.setLabelFor(tfMaxAngle);
            lblStep.setLabelFor(tfMaxAngle);

            tfConstant.setEnabled(false);
            tfMean.setEnabled(false);
            tfStdDev.setEnabled(false);
            tfMin.setEnabled(false);
            tfMax.setEnabled(false);
            tfMaxDistance.setEnabled(false);
            tfMaxAngle.setEnabled(false);
            tfStep.setEnabled(false);
            tfStepShift.setEnabled(false);

            lblConstant.setEnabled(false);
            lblMean.setEnabled(false);
            lblStdDev.setEnabled(false);
            lblMin.setEnabled(false);
            lblMax.setEnabled(false);
            lblMaxDistance.setEnabled(false);
            lblMaxAngle.setEnabled(false);
            lblStep.setEnabled(false);
            lblStepShift.setEnabled(false);
            shiftNote.setEnabled(false);

            int columns = 12;

            tfConstant.setColumns(columns);
            tfMean.setColumns(columns);
            tfStdDev.setColumns(columns);
            tfMin.setColumns(columns);
            tfMax.setColumns(columns);
            tfMaxDistance.setColumns(columns);
            tfMaxAngle.setColumns(columns);
            tfStep.setColumns(columns);

            JPanel innerPanel = new JPanel(new LabeledPairLayout());
            innerPanel.add(lblConstant, LabeledPairLayout.LABEL);
            innerPanel.add(tfConstant, LabeledPairLayout.FIELD);
            innerPanel.add(lblMean, LabeledPairLayout.LABEL);
            innerPanel.add(tfMean, LabeledPairLayout.FIELD);
            innerPanel.add(lblStdDev, LabeledPairLayout.LABEL);
            innerPanel.add(tfStdDev, LabeledPairLayout.FIELD);
            innerPanel.add(lblMin, LabeledPairLayout.LABEL);
            innerPanel.add(tfMin, LabeledPairLayout.FIELD);
            innerPanel.add(lblMax, LabeledPairLayout.LABEL);
            innerPanel.add(tfMax, LabeledPairLayout.FIELD);
            innerPanel.add(lblMaxDistance, LabeledPairLayout.LABEL);
            innerPanel.add(tfMaxDistance, LabeledPairLayout.FIELD);
            innerPanel.add(lblMaxAngle, LabeledPairLayout.LABEL);
            innerPanel.add(tfMaxAngle, LabeledPairLayout.FIELD);
            innerPanel.add(lblStep, LabeledPairLayout.LABEL);
            innerPanel.add(tfStep, LabeledPairLayout.FIELD);
            innerPanel.add(lblStepShift, LabeledPairLayout.LABEL);
            innerPanel.add(tfStepShift, LabeledPairLayout.FIELD);
            innerPanel.add(new JLabel(), LabeledPairLayout.LABEL);
            innerPanel.add(shiftNote, LabeledPairLayout.FIELD);
            add(innerPanel);
            setBorder(new TitledBorder("Parameters"));
        }

        public double getComponentValue(int component) {
            switch (component) {
                case CONSTANT: return ((Number) tfConstant.getValue()).doubleValue();
                case MEAN: return ((Number) tfMean.getValue()).doubleValue();
                case STDDEV: return ((Number) tfStdDev.getValue()).doubleValue();
                case MIN: return ((Number) tfMin.getValue()).doubleValue();
                case MAX: return ((Number) tfMax.getValue()).doubleValue();
                case MAXDISTANCE: return ((Number) tfMaxDistance.getValue()).doubleValue();
                case MAXANGLE: return ((Number) tfMaxAngle.getValue()).doubleValue();
                case STEP: return ((Number) tfStep.getValue()).doubleValue();
                case STEPSHIFT: return ((Number) tfStepShift.getValue()).doubleValue();
                default: throw new IllegalArgumentException("Illegal componentvalue");
            }
        }

        public void setShiftText( String text ) {
            shiftNote.setText(text);
        }

        public void setComponentState(int component, boolean enabled) {
            switch (component) {
                case CONSTANT: {
                    lblConstant.setEnabled(enabled);
                    tfConstant.setEnabled(enabled);
                    break;
                }
                case MEAN: {
                    lblMean.setEnabled(enabled);
                    tfMean.setEnabled(enabled);
                    break;
                }
                case STDDEV: {
                    lblStdDev.setEnabled(enabled);
                    tfStdDev.setEnabled(enabled);
                    break;
                }
                case MIN: {
                    lblMin.setEnabled(enabled);
                    tfMin.setEnabled(enabled);
                    break;
                }
                case MAX: {
                    lblMax.setEnabled(enabled);
                    tfMax.setEnabled(enabled);
                    break;
                }
                case MAXDISTANCE: {
                    lblMaxDistance.setEnabled(enabled);
                    tfMaxDistance.setEnabled(enabled);
                    break;
                }
                case MAXANGLE: {
                    lblMaxAngle.setEnabled(enabled);
                    tfMaxAngle.setEnabled(enabled);
                    break;
                }
                case STEP: {
                    lblStep.setEnabled(enabled);
                    tfStep.setEnabled(enabled);
                    break;
                }
                case STEPSHIFT: {
                    lblStepShift.setEnabled(enabled);
                    tfStepShift.setEnabled(enabled);
                    shiftNote.setEnabled(enabled);
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Illegal componentvalue");
                }
            }
        }

        public void setComponentValue(int component, double value) {
            switch (component) {
                case CONSTANT:
                    tfConstant.setValue(value);
                    break;
                case MEAN:
                    tfMean.setValue(value);
                    break;
                case STDDEV:
                    tfStdDev.setValue(value);
                    break;
                case MIN:
                    tfMin.setValue(value);
                    break;
                case MAX:
                    tfMax.setValue(value);
                    break;
                case MAXDISTANCE:
                    tfMaxDistance.setValue(value);
                    break;
                case MAXANGLE:
                    tfMaxAngle.setValue(value);
                    break;
                case STEP:
                    tfStep.setValue(value);
                    break;
                case STEPSHIFT:
                    tfStepShift.setValue(value);
                    break;
                default: throw new IllegalArgumentException("Illegal componentvalue");
            }
        }
    }

    private class RayleighDistributionState extends NonUserDefinedDistributionState {

        @Override
        public AbstractDistribution getDistribution() {
            return new RayleighDistributionImpl(parametersPanel.getComponentValue(ParametersPanel.MIN),
                    parametersPanel.getComponentValue(ParametersPanel.STDDEV));
        }

        @Override
        protected void setComponentState(boolean enabled) {
            if (enabled) {
                typePanel.setSelectedButton(RAYLEIGH);
            }
            parametersPanel.setComponentState(ParametersPanel.MIN, enabled);
            parametersPanel.setComponentState(ParametersPanel.STDDEV, enabled);
        }

        @Override
        public void setDistribution(AbstractDistribution d) throws IllegalArgumentException {
            if (!(d instanceof RayleighDistributionImpl)) {
                throw new IllegalArgumentException("Instance should be of class RayleighDistribution");
            }
            parametersPanel.setComponentValue(ParametersPanel.MIN, d.getMin());
            parametersPanel.setComponentValue(ParametersPanel.STDDEV, d.getStdDev());
        }
    }

    private static class StairDistributionPanel extends JPanel {

        private class DialogFunctionButtonPanel extends FunctionButtonPanel {
            public DialogFunctionButtonPanel() {}

            @Override
            public void saveChartImage() {
                functionGraph.saveChartImage();
            }

            @Override
            public void btnAddActionPerformed() {
                ((StairDistributionTableModelAdapter) dataTable.getModel()).addRow();
            }

            @Override
            public void btnClearActionPerformed() {
                ((StairDistributionTableModelAdapter) dataTable.getModel()).clear();
            }

            @Override
            public void btnDeleteActionPerformed() {
                model.deleteRow(dataTable.getSelectedRow());
            }

            @SuppressWarnings("unchecked")
            @Override
            public void btnLoadActionPerformed() {
                if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    fileio.setFile(fileChooser.getSelectedFile());
                    model.setPoints(((WithPoints)fileio.loadPoints()).points());
                }
            }

            @Override
            public void btnSaveActionPerformed() {
                if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = alignSelectedFile();
                    if ( okToSave( selectedFile)) {
                        fileio.setFile(fileChooser.getSelectedFile());
                        fileio.savePoints(model.getPointsList());
                    }
                }
            }

            @Override
            public void btnSymActionPerformed() {
                DialogTableToDataSet.symmetrize(model.getPointsList(), 0);
                model.sortPoints();
                model.fireChangeListeners();
            }
        }

        private JTable dataTable;
        private StairDistributionGraph functionGraph;

        private StairDistributionTableModelAdapter model;

        public StairDistributionPanel() {
            model = new StairDistributionTableModelAdapter();

            dataTable = new SeamcatTable(model);

            JScrollPane dataTableScrollPane = new JScrollPane(dataTable);
            functionGraph = new StairDistributionGraph(model);

            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            add(dataTableScrollPane);
            add(new DialogFunctionButtonPanel());
            add(functionGraph);
            setBorder(new TitledBorder("Stair distribution"));
        }

        public List<Point2D> getPoints() {
            return model.getPointsList();
        }

        public void setStairDistribution(StairDistributionImpl d) {
            model.setPoints(d);
        }

        public void stopEditing() {
            if (dataTable.isEditing()) {
                dataTable.getCellEditor().stopCellEditing();
            }
        }
    }

    private class TypePanel extends JPanel {
        private JRadioButton btnConstant;
        private JRadioButton btnDiscreteUniform;
        private JRadioButton btnGaussian;
        private JRadioButton btnRayleigh;
        private JRadioButton btnUniform;
        private JRadioButton btnUniformPolarAngle;
        private JRadioButton btnUniformPolarDist;
        private JRadioButton btnUserDefined;
        private JRadioButton btnUserDefinedStair;

        public TypePanel() {
            super();
            btnConstant = new JRadioButton(STRINGLIST.getString("DISTRIBUTION_CONSTANT"));
            btnDiscreteUniform = new JRadioButton(STRINGLIST.getString("DISTRIBUTION_DISCRETE_UNIFORM"));
            btnGaussian = new JRadioButton(STRINGLIST.getString("DISTRIBUTION_GAUSSIAN"));
            btnRayleigh = new JRadioButton(STRINGLIST.getString("DISTRIBUTION_RAYLEIGH"));
            btnUniform = new JRadioButton(STRINGLIST.getString("DISTRIBUTION_UNIFORM"));
            btnUniformPolarAngle = new JRadioButton(STRINGLIST.getString("DISTRIBUTION_UNIFORM_POLAR_ANGLE"));
            btnUniformPolarDist = new JRadioButton(STRINGLIST.getString("DISTRIBUTION_UNIFORM_POLAR_DIST"));
            btnUserDefined = new JRadioButton(STRINGLIST.getString("DISTRIBUTION_USERDEFINED"));
            btnUserDefinedStair = new JRadioButton(STRINGLIST.getString("DISTRIBUTION_USERDEFINED_STAIR"));

            ButtonGroup buttonGroupType = new ButtonGroup();
            buttonGroupType.add(btnConstant);
            buttonGroupType.add(btnDiscreteUniform);
            buttonGroupType.add(btnGaussian);
            buttonGroupType.add(btnRayleigh);
            buttonGroupType.add(btnUniform);
            buttonGroupType.add(btnUniformPolarAngle);
            buttonGroupType.add(btnUniformPolarDist);
            buttonGroupType.add(btnUserDefined);
            buttonGroupType.add(btnUserDefinedStair);

            btnConstant.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    btnConstantActionPerformed(evt);
                }
            });
            btnUserDefined.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    btnUserDefinedActionPerformed(evt);
                }
            });
            btnUniform.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    btnUniformActionPerformed(evt);
                }
            });
            btnGaussian.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    btnGaussianActionPerformed(evt);
                }
            });
            btnRayleigh.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    btnRayleighActionPerformed(evt);
                }
            });
            btnUniformPolarDist.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    btnUniformPolarDistActionPerformed(evt);
                }
            });
            btnUniformPolarAngle.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    btnUniformPolarAngleActionPerformed(evt);
                }
            });
            btnUserDefinedStair.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    btnUserDefinedStairActionPerformed(evt);
                }
            });
            btnDiscreteUniform.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    btnDiscreteUniformActionPerformed(evt);
                }
            });

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            add(btnConstant);
            add(btnUserDefined);
            add(btnUniform);
            add(btnGaussian);
            add(btnRayleigh);
            add(btnUniformPolarDist);
            add(btnUniformPolarAngle);
            add(btnUserDefinedStair);
            add(btnDiscreteUniform);
            setBorder(new TitledBorder("Type"));
        }

        private void btnConstantActionPerformed(ActionEvent e) {
            setState(CONSTANT_STATE);
        }

        private void btnDiscreteUniformActionPerformed(ActionEvent e) {
            setState(DISCRETEUNIFORM_STATE);
        }

        private void btnGaussianActionPerformed(ActionEvent e) {
            setState(GAUSSIAN_STATE);
        }

        private void btnRayleighActionPerformed(ActionEvent e) {
            setState(RAYLEIGH_STATE);
        }

        private void btnUniformActionPerformed(ActionEvent e) {
            setState(UNIFORM_STATE);
        }

        private void btnUniformPolarAngleActionPerformed(ActionEvent e) {
            setState(UNIFORMPOLARANGLE_STATE);
        }

        private void btnUniformPolarDistActionPerformed(ActionEvent e) {
            setState(UNIFORMPOLARDISTANCE_STATE);
        }

        private void btnUserDefinedActionPerformed(ActionEvent e) {
            setState(USERDEFINED_STATE);
        }

        private void btnUserDefinedStairActionPerformed(ActionEvent e) {
            setState(USERDEFINEDSTAIR_STATE);
        }

        public void setSelectedButton(int button) {
            switch (button) {
                case CONSTANT: {
                    btnConstant.setSelected(true);
                    btnConstant.requestFocus();
                    break;
                }
                case USERDEFINED: {
                    btnUserDefined.setSelected(true);
                    btnUserDefined.requestFocus();
                    break;
                }
                case UNIFORM: {
                    btnUniform.setSelected(true);
                    btnUniform.requestFocus();
                    break;
                }
                case GAUSSIAN: {
                    btnGaussian.setSelected(true);
                    btnGaussian.requestFocus();
                    break;
                }
                case RAYLEIGH: {
                    btnRayleigh.setSelected(true);
                    btnRayleigh.requestFocus();
                    break;
                }
                case UNIFORMPOLARDISTANCE: {
                    btnUniformPolarDist.setSelected(true);
                    btnUniformPolarDist.requestFocus();
                    break;
                }
                case UNIFORMPOLARANGLE: {
                    btnUniformPolarAngle.setSelected(true);
                    btnUniformPolarAngle.requestFocus();
                    break;
                }
                case USERDEFINEDSTAIR: {
                    btnUserDefinedStair.setSelected(true);
                    btnUserDefinedStair.requestFocus();
                    break;
                }
                case DISCRETEUNIFORM: {
                    btnDiscreteUniform.setSelected(true);
                    btnDiscreteUniform.requestFocus();
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Illegal button state");
                }
            }
        }

        /*public void setTypeChangeEnabled(boolean value) {
            btnConstant.setEnabled(value || btnConstant.isSelected());
            btnDiscreteUniform.setEnabled(value || btnDiscreteUniform.isSelected());
            btnGaussian.setEnabled(value || btnGaussian.isSelected());
            btnRayleigh.setEnabled(value || btnRayleigh.isSelected());
            btnUniform.setEnabled(value || btnUniform.isSelected());
            btnUniformPolarAngle.setEnabled(value || btnUniformPolarAngle.isSelected());
            btnUniformPolarDist.setEnabled(value || btnUniformPolarDist.isSelected());
            btnUserDefined.setEnabled(value || btnUserDefined.isSelected());
            btnUserDefinedStair.setEnabled(value || btnUserDefinedStair.isSelected());
        } */
    }

    private class UniformDistributionState extends NonUserDefinedDistributionState {

        @Override
        public AbstractDistribution getDistribution() {
            return new UniformDistributionImpl(parametersPanel.getComponentValue(ParametersPanel.MIN),
                    parametersPanel.getComponentValue(ParametersPanel.MAX));
        }

        @Override
        protected void setComponentState(boolean enabled) {
            if (enabled) {
                typePanel.setSelectedButton(UNIFORM);
            }
            parametersPanel.setComponentState(ParametersPanel.MIN, enabled);
            parametersPanel.setComponentState(ParametersPanel.MAX, enabled);
        }

        @Override
        public void setDistribution(AbstractDistribution d) throws IllegalArgumentException {
            if (!(d instanceof UniformDistributionImpl)) {
                throw new IllegalArgumentException("Instance should be of class UniformDistribution");
            }
            parametersPanel.setComponentValue(ParametersPanel.MIN, d.getMin());
            parametersPanel.setComponentValue(ParametersPanel.MAX, d.getMax());
        }
    }

    private class UniformPolarAngleDistributionState extends NonUserDefinedDistributionState {

        @Override
        public AbstractDistribution getDistribution() {
            return new UniformPolarAngleDistributionImpl(parametersPanel.getComponentValue(ParametersPanel.MAXANGLE));
        }

        @Override
        protected void setComponentState(boolean enabled) {
            if (enabled) {
                typePanel.setSelectedButton(UNIFORMPOLARANGLE);
            }
            parametersPanel.setComponentState(ParametersPanel.MAXANGLE, enabled);
        }

        @Override
        public void setDistribution(AbstractDistribution d) throws IllegalArgumentException {
            if (!(d instanceof UniformPolarAngleDistributionImpl)) {
                throw new IllegalArgumentException("Instance should be of class UniformPolarAngelDistribution (is "
                        + d.getClass() + ")");
            }
            parametersPanel.setComponentValue(ParametersPanel.MAXANGLE, d.getMaxAngle());
        }
    }

    private class UniformPolarDistanceDistributionState extends NonUserDefinedDistributionState {

        @Override
        public AbstractDistribution getDistribution() {
            return new UniformPolarDistanceDistributionImpl(parametersPanel.getComponentValue(ParametersPanel.MAXDISTANCE));
        }

        @Override
        protected void setComponentState(boolean enabled) {
            if (enabled) {
                typePanel.setSelectedButton(UNIFORMPOLARDISTANCE);
            }
            parametersPanel.setComponentState(ParametersPanel.MAXDISTANCE, enabled);
        }

        @Override
        public void setDistribution(AbstractDistribution d) throws IllegalArgumentException {
            if (!(d instanceof UniformPolarDistanceDistributionImpl)) {
                throw new IllegalArgumentException("Instance should be of class UniformPolarDistanceDistribution");
            }
            parametersPanel.setComponentValue(ParametersPanel.MAXDISTANCE, d.getMaxDistance());
        }
    }

    private class UserDefinedDistributionState extends DistributionState {

        @Override
        public void enterState() {
            super.enterState();
            userDefinedFunctionPanel.clear();
            contentPanelLayout.show(contentPanel, LAYOUTS[USERDEFINED_LAYOUT]);
        }

        @Override
        public AbstractDistribution getDistribution() {
            return new UserDefinedDistributionImpl(userDefinedFunctionPanel.getDiscreteFunction());
        }

        @Override
        protected void setComponentState(boolean enabled) {
            if (enabled) {
                typePanel.setSelectedButton(USERDEFINED);
            }
        }

        @Override
        public void setDistribution(AbstractDistribution d) throws IllegalArgumentException {
            userDefinedFunctionPanel.setDiscreteFunction((DiscreteFunction)((UserDefinedDistributionImpl) d).getCdf());
        }
    }

    private class UserDefinedStairDistributionState extends UserDefinedDistributionState {

        @Override
        public void enterState() {
            super.enterState();
            contentPanelLayout.show(contentPanel, LAYOUTS[USERDEFINED_STAIR_LAYOUT]);
        }

        @Override
        public AbstractDistribution getDistribution() {
            return new StairDistributionImpl(new DiscreteFunction(stairDistributionPanel.getPoints()));
        }

        @Override
        protected void setComponentState(boolean enabled) {
            if (enabled) {
                typePanel.setSelectedButton(USERDEFINEDSTAIR);
            }
        }

        @Override
        public void setDistribution(AbstractDistribution d) throws IllegalArgumentException {
            stairDistributionPanel.setStairDistribution((StairDistributionImpl) d);
        }
    }

    private static class UserDefinedDistributionCheck implements DistributionCheck {

        private String errorMsg;

        @Override
        public String getErrorString() {
            return errorMsg;
        }

        @Override
        public boolean validate(AbstractDistribution d) {
            errorMsg = null;
            if (d instanceof UserDefinedDistributionImpl) {
                UserDefinedDistributionImpl dist = (UserDefinedDistributionImpl)d;
                DiscreteFunction f = (DiscreteFunction) dist.getCdf();
                int zeroYCount = 0;
                int oneYCount = 0;
                int illegalValueCount = 0;
                for (Point2D p : f.points()) {
                    double y = p.getY();
                    if (y == 0d) {
                        zeroYCount++;
                    }
                    if (y == 1d) {
                        oneYCount++;
                    }
                    if (y < 0d || y > 1d) {
                        illegalValueCount++;
                    }
                }

                if (zeroYCount == 0) {
                    errorMsg = "User-defined distribution doesn't include 0";
                }
                else if (oneYCount == 0) {
                    errorMsg = "User-defined distribution doesn't include 1";
                }
                else if (zeroYCount > 1) {
                    errorMsg = "User-defined distribution has 0 more than once";
                }
                else if (oneYCount > 1) {
                    errorMsg = "User-defined distribution has 1 more than once";
                }
                else if (illegalValueCount > 0) {
                    errorMsg = "User-defined distribution values outside the allowed range (0.0 - 1.0)";
                }

            }
            return errorMsg == null;
        }
    }
}
