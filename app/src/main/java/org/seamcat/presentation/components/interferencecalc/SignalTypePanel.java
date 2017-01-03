package org.seamcat.presentation.components.interferencecalc;

import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.eventbus.UIEventHandler;
import org.seamcat.events.ICESignalTypeChanged;
import org.seamcat.model.engines.ICEConfiguration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

public class SignalTypePanel extends JPanel {

	private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle(
	      "stringlist", java.util.Locale.ENGLISH);

	private ICEConfiguration iceconf;
	private JCheckBox unwanted = new JCheckBox(STRINGLIST.getString("ICECONFIG_SIGNALTYPE_UNWANTED"));
	private JCheckBox blocking = new JCheckBox(STRINGLIST.getString("ICECONFIG_SIGNALTYPE_BLOCKING"));
	private JCheckBox overloading = new JCheckBox(STRINGLIST.getString("ICECONFIG_SIGNALTYPE_OVERLOADING"));
	private JCheckBox intermodulation = new JCheckBox(STRINGLIST.getString("ICECONFIG_SIGNALTYPE_INTERMODULATION"));

	private final List<ActionListener> selectionActionListeners = new LinkedList<ActionListener>();

	public SignalTypePanel() {
		super();

		unwanted.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
                if (iceconf!= null ) {
					iceconf.setUnwanted(unwanted.isSelected());
					changed();
				}
			}
		});

		blocking.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
                if (iceconf!= null ) {
					iceconf.setBlocking(blocking.isSelected());
					changed();
				}
			}
		});

		overloading.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
                if (iceconf!= null ) {
					iceconf.setOverloading(overloading.isSelected());
					changed();
				}
			}
		});

		intermodulation.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (iceconf!= null ) {
                    iceconf.setIntermodulation(intermodulation.isSelected());
                    changed();
                }
			}
		});

		setLayout(new GridLayout(4, 1));
		add(unwanted);
		add(blocking);
		add(overloading);
		add(intermodulation);
        EventBusFactory.getEventBus().subscribe(this);
	}

    private void changed() {
        EventBusFactory.getEventBus().publish( new ICESignalTypeChanged(iceconf, this));
    }

    @UIEventHandler
    public void handle( ICESignalTypeChanged event ) {
        if ( event.getOrigin() == this ) return;
        if ( event.getIce() == iceconf ) {
            init();
        }
    }

	public void init(ICEConfiguration _iceconf) {
		this.iceconf = _iceconf;
        init();
    }

    private void init() {
        setEnabling(true);
        setUnwantedStatus(iceconf.isUnwanted());
		setBlockingStatus(iceconf.isBlocking());
		setIntermodulationStatus(iceconf.isIntermodulation());

		if (iceconf.isAllowingOverloading()) {
			setOverloadingOptionEnabled(true);
			setOverloadingStatus(iceconf.isOverloading());
		}
		else {
			setOverloadingOptionEnabled(false);
		}

		if (!iceconf.allowIntermodulation()) {
			intermodulation.setEnabled(false);
			intermodulation.setSelected(false);
		} else {
			intermodulation.setEnabled(true);
		}
        if (iceconf.getHasBeenCalculated()) {
            setEnabling(false);
        }
	}

    private void setEnabling( boolean enabling ) {
        unwanted.setEnabled( enabling );
        blocking.setEnabled( enabling );
        overloading.setEnabled( enabling );
        intermodulation.setEnabled(enabling );
    }

	public void setBlockingStatus(boolean selected) {
		blocking.setSelected(selected);
	}

	public void setIntermodulationStatus(boolean selected) {
		intermodulation.setSelected(selected);
	}

	public void setUnwantedStatus(boolean selected) {
		unwanted.setSelected(selected);
	}

	public void setOverloadingStatus(boolean selected) {
		overloading.setSelected(selected);
	}

	public boolean isOverloadingOptionEnabled() {
		return overloading.isEnabled();
	}

	public void setOverloadingOptionEnabled(boolean isOverloadingOptionEnabled) {
		if (!isOverloadingOptionEnabled) {
			overloading.setSelected(false);
		}
		overloading.setEnabled(isOverloadingOptionEnabled);
	}

}
