package org.seamcat.presentation.genericgui.item;

import com.rits.cloning.Cloner;
import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.events.InfoMessageEvent;
import org.seamcat.function.BlockingMaskImpl;
import org.seamcat.function.DiscreteFunction;
import org.seamcat.model.Library;
import org.seamcat.model.factory.Model;
import org.seamcat.model.functions.Function;
import org.seamcat.presentation.DialogHelper;
import org.seamcat.presentation.DialogLibraryFunctionDefine;
import org.seamcat.presentation.ImportReceiverBlockingMaskDetailPanel;
import org.seamcat.presentation.components.GenericListDetailDialog;
import org.seamcat.presentation.genericgui.WidgetAndKind;
import org.seamcat.presentation.genericgui.WidgetKind;
import org.seamcat.presentation.resources.ImageLoader;
import org.seamcat.presentation.valuepreview.ButtonWithValuePreviewTip;
import org.seamcat.presentation.valuepreview.LabelWithValuePreviewTip;
import org.seamcat.presentation.valuepreview.ValuePreviewTextUtil;
import org.seamcat.presentation.valuepreview.ValuePreviewableFunctionAdapter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;


public class FunctionLibraryItem extends AbstractItem<BlockingMaskImpl, Object> {

    private String functionDialogTitle = "Receiver Blocking Mask/ACS,   X(MHz)/Y(dB)";
    private BlockingMaskImpl function;
    private JFrame parentDialog;
    private String xAxisName;
    private String yAxisName;
    private LabelWithValuePreviewTip valuePreviewLabel;
    private ButtonWithValuePreviewTip functionButton;
    private JButton importButton;
    private JButton exportButton;
    private JLabel unitLabel = new JLabel();

    ImageIcon importIcon = new ImageIcon(ImageLoader.class.getResource("import_16x16.png"));
    ImageIcon exportIcon = new ImageIcon(ImageLoader.class.getResource("export_16x16.png"));
    private JPanel buttons;

    public FunctionLibraryItem(JFrame parent) {
        this.parentDialog = parent;
    }

    public FunctionLibraryItem label(String label) {
        super.label(label);
        return this;
    }

    public FunctionLibraryItem unit(String unit) {
        super.unit(unit);
        return this;
    }

    public FunctionLibraryItem functionDialogTitle(String dialogTitle) {
        this.functionDialogTitle = dialogTitle;
        return this;
    }

    public FunctionLibraryItem axisNames(String xAxisName, String yAxisName) {
        this.xAxisName = xAxisName;
        this.yAxisName = yAxisName;
        return this;
    }

    public FunctionLibraryItem setUnit(String unit) {
        unitLabel.setText( unit);
        return this;
    }

    @Override
    public BlockingMaskImpl getValue() {
        return function;
    }

    @Override
    public void setValue(BlockingMaskImpl value) {
        function = value;
        updateValuePreview(function);
    }

    @Override
    public List<WidgetAndKind> createWidgets() {
        List<WidgetAndKind> widgets = super.createWidgets();


        functionButton = new ButtonWithValuePreviewTip( "Edit" );
        functionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showFunctionDialog();
            }
        });
        importButton = new JButton(importIcon);
        importButton.setToolTipText("Import");
        importButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                importPressed();
            }
        });
        exportButton = new JButton(exportIcon);
        exportButton.setToolTipText("Export");
        exportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                exportPressed();
            }
        });
        buttons = new JPanel( new GridLayout(1,3));

        buttons.add(functionButton);
        buttons.add(importButton);
        buttons.add( exportButton );
        widgets.add(new WidgetAndKind(buttons, WidgetKind.VALUE));
        widgets.add(new WidgetAndKind(unitLabel, WidgetKind.UNIT));

        valuePreviewLabel = new LabelWithValuePreviewTip();
        widgets.add(new WidgetAndKind(valuePreviewLabel, WidgetKind.VALUE_PREVIEW));

        return widgets;
    }

    private void showFunctionDialog() {
        DialogLibraryFunctionDefine dialog = new DialogLibraryFunctionDefine(parentDialog, true );
        if (dialog.show(function, functionDialogTitle, xAxisName, yAxisName )) {
            dialog.updateModel();
            function = dialog.getModel();
            updateValuePreview(function);
            fireItemChanged();
        }
    }

    @Override
    public void setRelevant(boolean relevant) {
        super.setRelevant(relevant);
        for ( int i =0; i<buttons.getComponentCount(); i++) {
            buttons.getComponent(i).setEnabled( relevant );
        }
    }

    private void updateValuePreview(Function function) {
        valuePreviewLabel.setText(ValuePreviewTextUtil.previewLabelText(DiscreteFunction.pretty(function)));
        ValuePreviewableFunctionAdapter previewable = new ValuePreviewableFunctionAdapter(function).axisNames(xAxisName, yAxisName);
        valuePreviewLabel.setPreviewable(previewable);
        functionButton.setPreviewable(previewable);
    }

    private void importPressed() {
        List<BlockingMaskImpl> masks = Model.getInstance().getLibrary().getReceiverBlockingMasks();
        GenericListDetailDialog<BlockingMaskImpl> dialog = new GenericListDetailDialog<BlockingMaskImpl>(parentDialog, "Import Receiver Blocking Mask", masks) {
            public void selectedElement(BlockingMaskImpl fun) {
                setDetail( new ImportReceiverBlockingMaskDetailPanel(fun, xAxisName, yAxisName));
            }
        };
        if (dialog.display() ) {
            setValue( dialog.getSelectedValue() );
        }
    }

    private void exportPressed() {
        BlockingMaskImpl clone = new Cloner().deepClone( getValue() );
        Library lib = Model.getInstance().getLibrary();
        if ( lib.hasLibraryFunction(clone) ) {
            if ( DialogHelper.overrideInLibrary(parentDialog, clone.description().name()) ) {
                lib.overrideLibraryFunction( clone );
                EventBusFactory.getEventBus().publish(new InfoMessageEvent(String.format("'%s' overridden in function library", clone.description().name())));
                Model.getInstance().persist();
            }
        } else {
            lib.addLibraryFunction(clone);
            EventBusFactory.getEventBus().publish(new InfoMessageEvent(String.format("'%s' added to function library", clone.description().name())));
            Model.getInstance().persist();
        }
    }
}
