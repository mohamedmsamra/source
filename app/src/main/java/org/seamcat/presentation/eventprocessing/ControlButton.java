package org.seamcat.presentation.eventprocessing;

import org.apache.log4j.Logger;
import org.seamcat.model.plugin.eventprocessing.CustomUI;
import org.seamcat.model.simulation.result.SimulationResult;
import org.seamcat.model.types.result.ResultTypes;
import org.seamcat.presentation.SimulationView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;

public class ControlButton extends JButton {

    private static final Logger LOG = Logger.getLogger(ControlButton.class);

    private ResultTypes resultTypes;
    private SimulationResult simulationResult;

    public ControlButton( final SimulationView view, String name, final Method method, final CustomUI customUI,
                          final Object[] arguments, final int indexOfResultTypes, final int indexOfSimulationResults) {
        super(name);
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    if ( indexOfResultTypes != -1 ) {
                        arguments[indexOfResultTypes] = resultTypes;
                    }
                    if ( indexOfSimulationResults != -1 ) {
                        arguments[indexOfSimulationResults] = simulationResult;
                    }
                    method.invoke( customUI, arguments );
                } catch (Exception e) {
                    LOG.error("", e);
                }
                view.updateResults();
            }
        });
        setEnabled( false );
    }


    public void setResultTypes( ResultTypes resultTypes ) {
        this.resultTypes = resultTypes;
        setEnabled( true );
    }

    public void setSimulationResult( SimulationResult simulationResult ) {
        this.simulationResult = simulationResult;
    }

}
