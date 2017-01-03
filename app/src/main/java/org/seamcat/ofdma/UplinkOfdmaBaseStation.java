package org.seamcat.ofdma;

import org.seamcat.dmasystems.AbstractDmaLink;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.mathematics.Mathematics;

import java.util.ArrayList;
import java.util.List;

public class UplinkOfdmaBaseStation<UserType extends UplinkOfdmaMobile> extends OfdmaBaseStation implements OfdmaVictim {

    public UplinkOfdmaBaseStation(Point2D position, OfdmaSystem<UserType> _system, int _cellid,
                                  double antHeight, double antennaTilt, int sectorId) {
        super(position, _system, _cellid, antHeight, antennaTilt, sectorId);
        externalInterferers = new ArrayList<OfdmaExternalInterferer>();
    }

    protected List<OfdmaExternalInterferer> externalInterferers;

    @Override
    public void addExternalInterferer(OfdmaExternalInterferer interferer) {
        externalInterferers.add(interferer);
    }

    @Override
    public void resetBaseStation() {
        super.resetBaseStation();
        externalInterferers.clear();
    }

    /**
     * Compute the sum of the blocking external interference for all the links.
     *<p></p>
     * it is used in collecting data for the "External Interference, Blocking (ref cell)" snapshot vector
     *
     * @return the blocking value in dBm
     */
    @Override
    public double getExternalBlocking_dBm() {
        double temp = 0;

        for (AbstractDmaLink l : getOldTypeActiveConnections()) {
            OfdmaUplink link = (OfdmaUplink) l;
            temp += Math.pow(10, (link.calculateExternalInterferenceBlocking_dBm() - 30) / 10);
        }

        return Mathematics.fromWatt2dBm(temp);
    }

    /**
     * Compute the sum of the unwanted external interference for all the links.
     *<p></p>
     * it is used in collecting data for the "External Interference, Unwanted (ref cell)" snapshot vector
     *
     * @return the unwanted value in dBm
     */
    @Override
    public double getExternalUnwanted_dBm() {
        double temp = 0;

        for (AbstractDmaLink l : getOldTypeActiveConnections()) {
            OfdmaUplink link = (OfdmaUplink) l;
            temp += Math.pow(10, (link.calculateExternalInterference_dBm() - 30) / 10);
        }

        return Mathematics.fromWatt2dBm(temp);
    }

    @Override
    public List<OfdmaExternalInterferer> getExternalInterferers() {
        return externalInterferers;
    }
}
