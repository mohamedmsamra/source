package org.seamcat.model.systems;

import java.util.List;

import static org.seamcat.model.systems.SystemPlugin.CorrelationMode.CORRELATED;

public class CorrelationModeResolver {

    public void resolveCorrelationModes( SystemPlugin victimSystem, SystemPlugin interferingSystem ) {
        List<String> vPoints = victimSystem.getCorrelationPointNames();

        List<String> iPoints = interferingSystem.getCorrelationPointNames();
        List<SystemPlugin.CorrelationMode> iModes = interferingSystem.getCorrelationModes();

        for (SystemPlugin.CorrelationMode mode : iModes) {
            if ( mode == CORRELATED ) {
                // only mode that is inter point related
                for (String vPoint : vPoints) {
                    for (String iPoint : iPoints) {
                        System.out.println(mode + " (victim "+vPoint+" -> interferer "+iPoint+")");
                    }
                }
            } else {
                for (String vPoint : vPoints) {
                    System.out.println( mode + " victim " + vPoint );
                }
            }
        }

    }
}
