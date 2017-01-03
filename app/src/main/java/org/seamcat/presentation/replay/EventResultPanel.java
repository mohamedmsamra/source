package org.seamcat.presentation.replay;

import org.seamcat.model.Scenario;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.simulation.result.*;
import org.seamcat.model.types.InterferenceLink;
import org.seamcat.presentation.LabeledPairLayout;

import javax.swing.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EventResultPanel extends JPanel {

    private static NumberFormat nf = new DecimalFormat("0.0000");

    public EventResultPanel( EventResult eventResult, Scenario scenario ) {
        super(new LabeledPairLayout());
        JPanel jPanel = this;
        addRow(jPanel, "Event Result", "");
        for (Map.Entry<String, Double> entry : eventResult.getValues().entrySet()) {
            addRow(jPanel, entry.getKey(), nf.format(entry.getValue()));
        }
        List<? extends LinkResult> vLinks = eventResult.getVictimSystemLinks();
        space(jPanel);
        printLinks("Victim system link", jPanel, vLinks);

        for (InterferenceLink link : scenario.getInterferenceLinks()) {
            InterferenceLinkResults iResults = eventResult.getInterferenceLinkResult(link);
            List<? extends LinkResult> iLinks = iResults.getInterferingSystemLinks();
            space(jPanel);
            printLinks("Interfering System link", jPanel, iLinks);
            List<? extends InterferenceLinkResult> interferenceLinkResults = iResults.getInterferenceLinkResults();
            for (InterferenceLinkResult iLink : interferenceLinkResults) {
                addRow(jPanel, "*** Interference link result ***", "");
                linkResult(jPanel, iLink);
                addRow(jPanel, "iRSS unwanted", nf.format(iLink.getRiRSSUnwantedValue()));
                addRow(jPanel, "iRSS blocking", nf.format(iLink.getRiRSSBlockingValue()));
                LinkResult sensingLinkResult = iLink.getSensingLinkResult();
                if (sensingLinkResult != null) {
                    addRow(jPanel, "Sensing link", "");
                    linkResult(jPanel, sensingLinkResult);
                }
            }
        }
    }

    private void space( JPanel jPanel ) {
        addRow(jPanel, "", "");
    }

    private void printLinks(String single, JPanel jPanel, List<? extends LinkResult> list) {
        if ( list.size() == 0 ) return;
        if ( list.size() > 1 ) {
            addRow(jPanel, single+"s", "");
        } else {
            addRow(jPanel, single, "");
        }
        for (int i = 0; i < list.size(); i++) {
            LinkResult link = list.get(i);
            linkResult(jPanel, link);
            if ( i+1 < list.size() ) {
                addRow(jPanel, "---", "---");
            }
        }
    }

    private void addRow( JPanel panel, String label, String value) {
        panel.add(new JLabel("<html><b>"+label+"</b></html>"), LabeledPairLayout.LABEL);
        panel.add(new JLabel(value), LabeledPairLayout.FIELD);
    }

    private void linkResult( JPanel panel, LinkResult linkResult) {
        addRow(panel, "Receiver antenna", "");
        antenna(panel, linkResult.rxAntenna());
        addRow(panel, "Transmitter antenna", "");
        antenna(panel, linkResult.txAntenna());

        addRow(panel, "Link results", "");
        addRow(panel, "frequency", nf.format(linkResult.getFrequency()));
        addRow(panel, "tx rx angle", nf.format(linkResult.getTxRxAngle()));
        addRow(panel, "blocking attenuation", nf.format(linkResult.getBlockingAttenuation()));
        addRow(panel, "rx noise floor", nf.format(linkResult.getRxNoiseFloor()));
        addRow(panel, "tx power", nf.format(linkResult.getTxPower()));
        addRow(panel, "rx tx distance", nf.format(linkResult.getTxRxDistance()));

        LinkedHashMap<String, Double> values = linkResult.getValues();
        for (Map.Entry<String, Double> entry : values.entrySet()) {
            addRow(panel, entry.getKey(), nf.format(entry.getValue()));
        }

    }

    private void antenna( JPanel panel, AntennaResult antennaResult ) {
        point(panel, "position", antennaResult.getPosition());
        addRow(panel, "height", ""+antennaResult.getHeight());
        addRow(panel, "elevation", ""+antennaResult.getElevation());
        addRow(panel, "azimuth", ""+antennaResult.getAzimuth());
        addRow(panel, "elevation compensation", ""+antennaResult.getElevationCompensation());
        addRow(panel, "tilt", ""+antennaResult.getTilt());
        localEnv(panel, "local environment", antennaResult.getLocalEnvironment());
        addRow(panel, "gain", nf.format(antennaResult.getGain()));
    }

    private void point(JPanel panel, String name, Point2D point) {
        addRow(panel, name, "("+ nf.format(point.getX())+", "+nf.format(point.getY())+")");
    }

    private void localEnv(JPanel panel, String name, LocalEnvironmentResult le ){
        addRow(panel, name, "("+le.getEnvironment()+" wallLoss="+nf.format(le.getWallLoss())+" std.dev="+nf.format(le.getWallLossStdDev())+")");
    }
}
