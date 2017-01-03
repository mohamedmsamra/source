package org.seamcat.dmasystems;

import org.seamcat.model.Scenario;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.simulation.result.InterferenceLinkResult;
import org.seamcat.model.simulation.result.Interferer;
import org.seamcat.model.types.AntennaGain;
import org.seamcat.model.types.InterferenceLink;
import org.seamcat.plugin.AntennaGainConfiguration;
import org.seamcat.scenario.CellularSystemImpl;
import org.seamcat.simulation.result.MutableAntennaResult;
import org.seamcat.simulation.result.MutableInterferenceLinkResult;
import org.seamcat.simulation.result.MutableLinkResult;

public abstract class ActiveInterferer implements Interferer {

    private Scenario scenario;
    private InterferenceLink link;
    private MutableLinkResult result;
	private AntennaGain antennaGain;

	private double fixedGain;
	private String name;
	private boolean usingFixedGain = false;
	private boolean translateAngles = false;

	private double antAzimuth;
	private double rInterferingLinkAngle;
	private boolean isDMASystemInterferer;

    public ActiveInterferer(Scenario scenario, InterferenceLink link, MutableLinkResult result) {
        // generic interferer
        this.scenario = scenario;
        this.link = link;
        this.isDMASystemInterferer = false;
        this.result = result;
        this.antennaGain = link.getInterferingSystem().getTransmitter().getAntennaGain();
        antAzimuth = result.txAntenna().getAzimuth();
        rInterferingLinkAngle = result.getTxRxAngle();
    }

	public ActiveInterferer(Scenario scenario, InterferenceLink link, Point2D p, double power,
                            double freq, double antHeight, double antTilt,
                            AntennaGain _antenna,
                            String _name, double _antAzimuth, double _rILangle) {
		this.scenario = scenario;
        this.link = link;
		antAzimuth = _antAzimuth;
		rInterferingLinkAngle = _rILangle;

        antennaGain = _antenna;
        name = _name;

        // Determine if the interfering tranmitter antenna is CDMA/OFDMA TriSector Antenna
        this.isDMASystemInterferer = true;
        this.translateAngles = true;
        setupLink(p, antTilt, power, freq, antHeight);
	}

	public ActiveInterferer(Scenario scenario, InterferenceLink link, Point2D p, double power,
                            double freq, double antHeight, double antTilt,
                            double _antennaGain, double _antAzimuth, double _rILangle) {
		this.scenario = scenario;
        this.link = link;
		antAzimuth = _antAzimuth;
		fixedGain = _antennaGain;
		usingFixedGain = true;
		rInterferingLinkAngle = _rILangle;
		isDMASystemInterferer = true;
        CellularSystemImpl system = (CellularSystemImpl) link.getInterferingSystem();
        if ( system.isUpLink() ) {
            AntennaGainConfiguration gain = (AntennaGainConfiguration) system.getTransmitter().getAntennaGain();
            gain.setPeakGain(fixedGain);
        } else {
            AntennaGainConfiguration gain = (AntennaGainConfiguration) system.getReceiver().getAntennaGain();
            gain.setPeakGain(fixedGain);
        }
        setupLink(p, antTilt, power, freq, antHeight);
	}

    private void setupLink(Point2D p, double txTilt, double power, double freq, double antHeight) {
        result = new MutableLinkResult();
        result.setTxPower(power);
        result.setFrequency(freq);
        result.txAntenna().setHeight(antHeight);
        result.txAntenna().setTilt(txTilt);
        result.txAntenna().setPosition(p);
    }

    @Override
	public AntennaGain getAntennaGain() {
		return antennaGain;
	}

	@Override
	public double getAntennaHeight() {
		return result.txAntenna().getHeight();
	}

	@Override
	public double getFrequency() {
		return result.getFrequency();
	}

	public boolean isUsingFixedGain() {
		return usingFixedGain;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "Interferer at " + result.txAntenna().getPosition()+" : Transmit Power = " + result.getTxPower() + " dBm; Frequency = "
		+ result.getFrequency() + "MHz; Antenna Height = " + result.txAntenna().getHeight() + " m";
	}

	public boolean isPathlossCorrelated() {
		return link.getPathLossCorrelation().isUsingPathLossCorrelation();
	}

	public double getCorrelationFactor() {
		return link.getPathLossCorrelation().getCorrelationFactor();
	}

	public double getPathlossVariance() {
        return link.getPathLossCorrelation().getPathLossVariance();
	}

    public double getMinimumCouplingLoss() {
		return link.getInterferingLinkRelativePosition().getMinimumCouplingLoss().trial();
	}

    public InterferenceLink getInterferenceLink() {
        return link;
    }

    @Override
    public MutableLinkResult getLinkResult() {
        return result;
    }

    @Override
    public Point2D getPoint() {
        return result.txAntenna().getPosition();
    }

    @Override
    public Scenario getScenario() {
        return scenario;
    }

    @Override
    public double getFixedGain() {
        return fixedGain;
    }

    /**
     * get the horizontal angle of the interferer
     * @param p
     * @return horizontal angle in degree
     */
    @Override
    public double getHorizontalAngle( Point2D p ) {
        double horiAngle = Mathematics.calculateKartesianAngle(getPoint(), p);

        if(!isDMASystemInterferer){ // if the interferer is traditional reuse the same method as for IT-VR (trad-trad)
            horiAngle = LinkCalculator.calculateItVictimAzimuth(rInterferingLinkAngle, antAzimuth, Mathematics.calculateKartesianAngle(result.txAntenna().getPosition(), p), "ILT -> VLR");
            horiAngle = LinkCalculator.convertAngleToConfineToHorizontalDefinedRange(horiAngle);
        }
        if (translateAngles) {
            horiAngle -= 180;
        }
        return horiAngle;
    }

    /**
     * get the vertical angle of the interferer
     * @param height
     * @param p
     * @return vertical angle in degree
     */
    @Override
    public double getElevation(double height, Point2D p) {
        if(!isDMASystemInterferer){
            return LinkCalculator.calculateElevationWithTilt(getPoint(), height, p, result.txAntenna().getHeight(), result.txAntenna().getTilt(), antAzimuth, "Interferer is traditional");
        }else{
            double vertiAngle = Mathematics.calculateElevation(getPoint(), height, p, result.txAntenna().getHeight());
            return vertiAngle - result.txAntenna().getTilt();
        }
    }

    public abstract void applyInterferenceLinkCalculations(MutableInterferenceLinkResult link);

    @Override
    public void calculateLosses(Point2D victimPos, double victimHeight, InterferenceLinkResult result) {
        MutableInterferenceLinkResult linkResult = (MutableInterferenceLinkResult) result;
        if (isUsingFixedGain()) {
            linkResult.txAntenna().setGain(getFixedGain());
        } else {
            double horiAngle = getHorizontalAngle(victimPos);
            double elevation = getElevation(victimHeight, victimPos);
            MutableAntennaResult direction = new MutableAntennaResult();
            direction.setAzimuth( horiAngle );
            direction.setElevation( elevation );
            linkResult.txAntenna().setGain(getAntennaGain().evaluate(linkResult, direction));
        }

        LinkCalculator.itVrPropagationLoss(linkResult, getMinimumCouplingLoss() );
    }
}
