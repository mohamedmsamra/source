package org.seamcat.dmasystems;

import org.apache.log4j.Logger;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.simulation.result.MutableMobileStationResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractDmaMobile extends MutableMobileStationResult {

    protected static final Logger LOG = Logger.getLogger(AbstractDmaMobile.class);

    protected List<AbstractDmaLink> activeList;
    protected AbstractDmaLink[] links;
    protected AbstractDmaLink servingLink;

    private AbstractDmaSystem system;
    public AbstractDmaSystem getSystem() {
        return system;
    }

    @Override
    public double getFrequency() {
        return system.getFrequency();
    }

    @Override
    public double getReferenceBandwidth() {
        return system.getSystemSettings().getBandwidth();
    }

    public AbstractDmaMobile(Point2D point, AbstractDmaSystem _system, int _userid, double antGain, double antHeight) {
        setPosition( point );
        setAntennaHeight( antHeight );
        setAntennaTilt( 0 );
        setUserId(_userid);
        setAntennaGain( antGain );
        this.system = _system;
        this.links = generateLinksArray();
        this.activeList = new ArrayList<AbstractDmaLink>(2);
    }

    public void addToActiveList(AbstractDmaLink link) {
        activeList.add(link);
    }

    /**
     * calculate the total interference in dBm
     *<p></p>
     * <code>fromWatt2dBm(fromdBm2Watt(getExternalInterference()) + getThermalNoise())</code>
     *
     * @return calculate the total interference in dBm
     */
    public double calculateTotalInterference_dBm() {
        setTotalInterference(Mathematics.fromWatt2dBm(Mathematics.fromdBm2Watt(getExternalInterference()) + system.getResults().getThermalNoise()));
        return getTotalInterference();
    }

    public double calculateTotalInterference_Watt() {
        return Mathematics.fromdBm2Watt(calculateTotalInterference_dBm());
    }

    public abstract boolean connect();

    protected abstract AbstractDmaLink[] generateLinksArray();

    public abstract void generateLinksToBaseStations();

    public List<AbstractDmaLink> getActiveList() {
        return activeList;
    }

    public AbstractDmaLink[] getAllLinks() {
        return links;
    }

    public AbstractDmaLink getServingLink() {
        return servingLink;
    }

    public boolean isInSofterHandover() {
        if (isInSoftHandover()) {
            if (activeList.get(0).getBaseStation().getCellLocationId() == activeList.get(1).getBaseStation().getCellLocationId()) {
                return true;
            }
        }
        return false;
    }

    public boolean isInSoftHandover() {
        return activeList.size() > 1;
    }

    public abstract void selectActiveList(double handoverMargin);

    public void setCurrentTransmitPower_dBm(double currentTransmitPower) {
        setCurrentTransmitPowerIndBm( currentTransmitPower );
        setCurrentTransmitPower(Mathematics.fromdBm2Watt(currentTransmitPower));
    }

    public void setServingLink(AbstractDmaLink servingLink) {
        this.servingLink = servingLink;
    }

    public void sortLinks() {
        Arrays.sort(links, AbstractDmaLink.CDMALinkPathlossComparator);
    }

    public void translate( Point2D translate ) {
        setPosition(getPosition().add(translate));
    }
}
