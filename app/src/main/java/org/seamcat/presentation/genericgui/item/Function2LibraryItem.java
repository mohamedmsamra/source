package org.seamcat.presentation.genericgui.item;

import com.rits.cloning.Cloner;
import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.events.InfoMessageEvent;
import org.seamcat.function.DiscreteFunction;
import org.seamcat.function.EmissionMaskImpl;
import org.seamcat.model.Library;
import org.seamcat.model.Workspace;
import org.seamcat.model.factory.Model;
import org.seamcat.model.functions.EmissionMask;
import org.seamcat.model.systems.SystemModel;
import org.seamcat.model.systems.cdma.SystemModelCDMADownLink;
import org.seamcat.model.systems.cdma.SystemModelCDMAUpLink;
import org.seamcat.model.systems.ofdma.SystemModelOFDMADownLink;
import org.seamcat.model.systems.ofdma.SystemModelOFDMAUpLink;
import org.seamcat.presentation.DialogHelper;
import org.seamcat.presentation.DialogLibraryFunction2Define;
import org.seamcat.presentation.ImportSpectrumEmissionMaskDetailPanel;
import org.seamcat.presentation.components.GenericListDetailDialog;
import org.seamcat.presentation.genericgui.WidgetAndKind;
import org.seamcat.presentation.genericgui.WidgetKind;
import org.seamcat.presentation.model.VictimCharacteristics;
import org.seamcat.presentation.resources.ImageLoader;
import org.seamcat.presentation.valuepreview.ButtonWithValuePreviewTip;
import org.seamcat.presentation.valuepreview.LabelWithValuePreviewTip;
import org.seamcat.presentation.valuepreview.ValuePreviewTextUtil;
import org.seamcat.presentation.valuepreview.ValuePreviewableFunction2Adapter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class Function2LibraryItem extends AbstractItem<EmissionMaskImpl, Object> {

    private SystemModel owner;
    private Workspace workspace;
	private EmissionMaskImpl function;
	private JFrame parent;
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

	public Function2LibraryItem(JFrame parent, SystemModel owner, Workspace workspace) {
        this.parent = parent;
        this.owner = owner;
        this.workspace = workspace;
    }

    private VictimCharacteristics getVictimCharacteristics() {
        if ( workspace == null || owner == null) return null;

        Double interfererBand = getBandwidth(owner);
        if ( interfererBand == null ) return null;

        SystemModel system = workspace.getVictimSystem();
        Double victimBand = getBandwidth( system );
        if ( victimBand == null ) return null;

        double offset = Library.getFrequency( owner ).trial() - Library.getFrequency(  system).trial();

        return new VictimCharacteristics(victimBand, offset, true, interfererBand);

    }

    private Double getBandwidth( SystemModel model ) {
        if ( model instanceof SystemModelCDMAUpLink ) {
            return ((SystemModelCDMAUpLink) model).generalSettings().generalSettings().bandwidth();
        } else if ( model instanceof SystemModelCDMADownLink ) {
            return ((SystemModelCDMADownLink) model).generalSettings().generalSettings().bandwidth();
        } else if ( model instanceof SystemModelOFDMAUpLink ) {
            return ((SystemModelOFDMAUpLink) model).generalSettings().generalSettings().bandwidth();
        } else if ( model instanceof SystemModelOFDMADownLink ) {
            return ((SystemModelOFDMADownLink) model).generalSettings().generalSettings().bandwidth();
        }

        return null;
    }

	public Function2LibraryItem label(String label) {
		super.label(label);
		return this;
	}

	public Function2LibraryItem unit(String unit) {
		super.unit(unit);
		return this;
	}

	public Function2LibraryItem axisNames(String xAxisName, String yAxisName) {
		this.xAxisName = xAxisName;
		this.yAxisName = yAxisName;
		return this;
	}

	@Override
	public EmissionMaskImpl getValue() {
		return function;
	}

	@Override
	public void setValue(EmissionMaskImpl value) {
		function = value;
		updateValuePreview(function);
	}

	@Override
	public List<WidgetAndKind> createWidgets() {
		List<WidgetAndKind> widgets = super.createWidgets();

		functionButton = new ButtonWithValuePreviewTip("Edit");
		functionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showFunctionDialog();
			}
		});
		importButton = new JButton(importIcon);
		importButton.setToolTipText("Import");
		importButton.addActionListener(new ActionListener() {
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
		buttons = new JPanel(new GridLayout(1, 3));

		buttons.add(functionButton);
		buttons.add(importButton);
		buttons.add(exportButton);
		widgets.add(new WidgetAndKind(buttons, WidgetKind.VALUE));
		widgets.add(new WidgetAndKind(unitLabel, WidgetKind.UNIT));

		valuePreviewLabel = new LabelWithValuePreviewTip();
		widgets.add(new WidgetAndKind(valuePreviewLabel, WidgetKind.VALUE_PREVIEW));

		return widgets;
	}

	private void showFunctionDialog() {
		DialogLibraryFunction2Define dialog = new DialogLibraryFunction2Define(parent, true);
        String title = "Spectrum Emission Mask [Offset (MHz) ; Mask Value (dBc) ; Ref. BW (kHz)]";

		boolean okFromDialog;
        VictimCharacteristics vc = getVictimCharacteristics();
		if (vc != null) {
			okFromDialog = dialog.show(function, title, vc.getVictimBandwidth(), vc.getFrequencyOffset(), vc.isShowACLR(), vc.getInterfererBandwidth(), xAxisName, yAxisName);
		} else {
			okFromDialog = dialog.show(function, title, xAxisName, yAxisName);
		}

		if (okFromDialog) {
			dialog.updateModel();
			function = dialog.getModel();
			updateValuePreview(function);
			fireItemChanged();
		}
	}

	@Override
	public void setRelevant(boolean relevant) {
		super.setRelevant(relevant);
		for (int i = 0; i < buttons.getComponentCount(); i++) {
			buttons.getComponent(i).setEnabled(relevant);
		}
	}

	private void updateValuePreview(EmissionMask function) {
		valuePreviewLabel.setText(ValuePreviewTextUtil.previewLabelText(DiscreteFunction.pretty(function)));
		ValuePreviewableFunction2Adapter previewable = new ValuePreviewableFunction2Adapter(function).axisNames(xAxisName, yAxisName);
		previewable.setVictimCharacteristics(getVictimCharacteristics());
		valuePreviewLabel.setPreviewable(previewable);
		functionButton.setPreviewable(previewable);
	}

	private void importPressed() {
		List<EmissionMaskImpl> masks = Model.getInstance().getLibrary().getSpectrumEmissionMasks();
        GenericListDetailDialog<EmissionMaskImpl> dialog = new GenericListDetailDialog<EmissionMaskImpl>(parent, "Import Spectrum Emission Mask", masks) {
            public void selectedElement(EmissionMaskImpl fun) {
                setDetail( new ImportSpectrumEmissionMaskDetailPanel(fun));
            }
        };
        if (dialog.display() ) {
            setValue( dialog.getSelectedValue() );
        }

	}

	private void exportPressed() {
        EmissionMaskImpl clone = new Cloner().deepClone(getValue());
		Library lib = Model.getInstance().getLibrary();
		if (lib.hasLibraryFunction(clone)) {
			if (DialogHelper.overrideInLibrary(parent, clone.description().name())) {
				lib.overrideLibraryFunction(clone);
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
