package org.seamcat.presentation.genericgui.item;

import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.presentation.SeamcatIcons;
import org.seamcat.presentation.genericgui.ItemChangedEvent;
import org.seamcat.presentation.genericgui.WidgetAndKind;
import org.seamcat.presentation.genericgui.WidgetKind;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractItem<ValueType, ModelType> implements Item<ValueType>{

	private List<WidgetAndKind> widgets;
	private String label;
	private String unit;
	private boolean relevant;
	private ModelType model;
	private String tooltip;
    private String information;
    private JLabel jLabel;
    private JLabel jUnit;
    private JLabel info;

    public AbstractItem() {
		this.relevant = true;
	}

    public AbstractItem tooltip(String tooltip ) {
		this.tooltip = tooltip;
		return this;
	}

    public AbstractItem information( String information ) {
        this.information = information;
        return this;
    }

	public AbstractItem label(String label) {
		this.label = label;
		return this;
	}

	public AbstractItem unit(String unit) {
		this.unit = unit;
		return this;
	}

	public void setModel(ModelType model) {
		this.model = model;
	}

	public ModelType getModel() {
		return model;
	}

	@Override
	public List<WidgetAndKind> getWidgets() {
		if (widgets == null) {
			widgets = createWidgets();
		}

		return widgets;
	}

	public void initialize() {
		getWidgets();
	}

	public List<WidgetAndKind> createWidgets() {
		ArrayList<WidgetAndKind> widgets = new ArrayList<WidgetAndKind>();
		if (label != null) {
			jLabel = new JLabel(label);
			widgets.add(new WidgetAndKind(jLabel, WidgetKind.LABEL));
			if ( tooltip != null ) {
				jLabel.setToolTipText( tooltip );
			}
		}
		if (unit == null) {
		    jUnit = new JLabel("");
        } else {
            jUnit = new JLabel(unit);
        }
        widgets.add(new WidgetAndKind(jUnit, WidgetKind.UNIT));
        if ( information != null ) {
            info = new JLabel(SeamcatIcons.getImageIcon("SEAMCAT_ICON_INFORMATION", SeamcatIcons.IMAGE_SIZE_TOOLBAR));
            info.setToolTipText( information );
            widgets.add(new WidgetAndKind(info, WidgetKind.UNIT));
        }
		return widgets;
	}

    protected String getInformation() {
        return information;
    }

    public AbstractItem setLabelText( String text ) {
        if ( jLabel != null ) {
            jLabel.setText( text );
        }
        return this;
    }

    public AbstractItem setUnitText( String text ) {
        if ( jUnit != null ) {
            jUnit.setText( text );
        }
        return this;
    }

	public String getLabel() {
		return label;
	}

	public String getToolTipText() {
		return tooltip;
	}

	public String getUnit() {
		return unit;
	}

    public void setInformation( String information ) {
        info.setVisible(true);
        info.setToolTipText( information );
    }

	@Override
	public void setRelevant(boolean relevant) {
		this.relevant = relevant;
		for (WidgetAndKind wik : widgets) {
			wik.getWidget().setEnabled(relevant);
		}
	}

	@Override
	public boolean isRelevant() {
		return relevant;
	}

	protected void fireItemChanged() {
		EventBusFactory.getEventBus().publish(new ItemChangedEvent(this));
	}

    public void dispose() {
        for (WidgetAndKind widget : widgets) {
            widget.dispose();
        }
    }

    public void updateLabel(String label) {
        this.label = label;
        if ( jLabel != null) {
            jLabel.setText( label );
        }
    }
}
