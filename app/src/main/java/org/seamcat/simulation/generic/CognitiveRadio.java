package org.seamcat.simulation.generic;

import org.apache.log4j.Logger;
import org.seamcat.dmasystems.LinkCalculator;
import org.seamcat.function.DiscreteFunction;
import org.seamcat.model.Scenario;
import org.seamcat.model.distributions.*;
import org.seamcat.model.engines.EIRPFrequencyValue;
import org.seamcat.model.functions.Bounds;
import org.seamcat.model.functions.EmissionMask;
import org.seamcat.model.functions.Function;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.generic.GenericSystem;
import org.seamcat.model.generic.GenericTransmitter;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.plugin.Validator;
import org.seamcat.model.plugin.eventprocessing.EventProcessingPlugin;
import org.seamcat.model.simulation.result.EventResult;
import org.seamcat.model.simulation.result.LinkResult;
import org.seamcat.model.types.*;
import org.seamcat.model.types.result.*;
import org.seamcat.simulation.result.MutableEventResult;
import org.seamcat.simulation.result.MutableInterferenceLinkResult;
import org.seamcat.simulation.result.MutableSensingLinkResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.log10;
import static java.lang.String.format;
import static org.seamcat.model.simulation.result.SimulationResult.DRSSVector;
import static org.seamcat.simulation.LocalEnvironmentSelector.pickLocalEnvironment;

/**
 * Method that implements the Cognitive Radio algorithm
 */
public class CognitiveRadio implements EventProcessingPlugin<CognitiveRadio.Input>{

    private static Logger LOG = Logger.getLogger(CognitiveRadio.class);
    private GenericSystem victimSystem;
    private double[] rsRSSFrequency;
    private boolean[] rsRSSavailableChannel;
    private double[] rsRSSValue;

    public static String COGNITIVE  = "Cognitive radio";

    private static String CRV_SRSS   = "sRSS";
    private static String CRV_WSD_F  = "WSD frequency";
    private static String CRV_WSD_EIRP = "WSD EIRP";
    private static String CRV_VIC_FREQ = "Victim frequency";
    private static String CRV_AVG_EIRP = "Average EIRP per event x active WSDs (for each frequency)";
    private static String CRV_AVG_ACT  = "Average Active WSD per event (for each frequency)";

    public interface Input {}

    @Override
    public ResultTypes evaluate(Scenario scenario, Iterable<EventResult> results, Input input) {

        victimSystem = (GenericSystem) scenario.getVictimSystem();
        initChannels();
        Distribution random = new UniformDistributionImpl(0,1);
        ResultTypes crResults = new ResultTypes();

        int size = crCount(scenario);
        List<List<Double>> srssValues = init( size );
        List<List<Double>> wsdfValues = init( size );
        List<List<Double>> wsdeValues = init( size );
        List<List<Double>> vFreqValues = init( size );

        crResults.getBarChartResultTypes().add( new BarChartResultType(CRV_AVG_EIRP, "Frequency (MHz)", "dBm"));
        crResults.getBarChartResultTypes().add(new BarChartResultType(CRV_AVG_ACT, "Frequency (MHz)", "Number of active WSDs"));

        for (EventResult er : results) {
            MutableEventResult result = (MutableEventResult) er;
            int crCount = 0;
            List<InterferenceLink<GenericSystem>> crLinks = getCRLinks(scenario);
            for (InterferenceLink<GenericSystem> iLink : crLinks ) {
                for (MutableInterferenceLinkResult linkResult : result.getInterferenceLinkResult(iLink).getInterferenceLinkResults()) {

                    EIRPFrequencyValue selectedWSD;
                    GenericTransmitter gt = iLink.getInterferingSystem().getTransmitter();
                    SensingLink sensingLink = gt.getSensingLink();

                    linkResult.setSensingLinkResult( new MutableSensingLinkResult() );
                    linkResult.getSensingLinkResult().txAntenna().setLocalEnvironment(pickLocalEnvironment(gt.getLocalEnvironments()));
                    linkResult.getSensingLinkResult().rxAntenna().setLocalEnvironment(pickLocalEnvironment(iLink.getVictimSystem().getTransmitter().getLocalEnvironments()));
                    sensingTrial(linkResult);
                    Function detectionThreshold = sensingLink.getDetectionThreshold();
                    int availableChannels = rsRSSFrequency.length;
                    LinkResult victimLink = linkResult.getVictimSystemLink();

                    LOG.debug(format("There are %d potential WSD channels", rsRSSFrequency.length));

                    double vrFreq = victimLink.getFrequency();

                    boolean modePropagationVariation = true;
                    double pathLoss = 0;
                    double itFreq = 0;


                    // Calculate sRSS and determine available channels
                    for (int x = 0; x < rsRSSFrequency.length; x++) {
                        itFreq = rsRSSFrequency[x];
                        if (modePropagationVariation) {
                            // modification of the code to solve the issue of
                            // the STD varying through the various frequency channel
                            pathLoss = calculatePathLoss(linkResult, gt, itFreq);
                        }
                        double sRSS = getSRSS(linkResult.getSensingLinkResult(), iLink,
                                scenario.getPreSimulationResults(this.victimSystem).getNormalizedEmissionsMask(), victimLink,
                                itFreq, pathLoss);

                        double dFreq = itFreq - vrFreq;
                        double detectionThresholdEval = detectionThreshold.evaluate(dFreq);
                        rsRSSavailableChannel[x] = sRSS < detectionThresholdEval;

                        if (!rsRSSavailableChannel[x]) {
                            availableChannels--;
                        }

                        if (LOG.isDebugEnabled()) {
                            LOG.debug(format("Evaluating sRSS<detectionThreshold for frequency %f MHz = (%f<%f) ", itFreq, sRSS, detectionThresholdEval));
                            LOG.debug(String.format("sRSS < detectionThreshold = %s", Boolean.toString(rsRSSavailableChannel[x]).toUpperCase()));
                        }

                        rsRSSValue[x] = sRSS;
                        modePropagationVariation = false;
                    }

                    LOG.debug(format("There are %d available WSD channels", availableChannels));

                    // Failure
                    boolean sensingFailure = (random.trial() * 100) < sensingLink.getProbabilityOfFailure();

                    if (LOG.isDebugEnabled()) {
                        double failureProb = sensingLink.getProbabilityOfFailure();
                        LOG.debug(format("Sensing failure feature is %s", failureProb > 0 ? "ENABLED" : "DISABLED"));
                        if (failureProb > 0) {
                            LOG.debug(format("Sensing failure probability is %f pct", failureProb));
                        }
                    }

                    boolean isActive = false;

                    if (sensingFailure) {
                        // select the first frequency of the potential
                        // channel to enable the calculation of the pathloss
                        itFreq = rsRSSFrequency[0];
                        pathLoss = calculatePathLoss(linkResult, gt, itFreq);

                        selectedWSD = new EIRPFrequencyValue(vrFreq, linkResult.getInterferingSystemLink().getTxPower() + iLink.getInterferingSystem().getTransmitter().getAntennaGain().peakGain(),
                                getSRSS(linkResult.getSensingLinkResult(), iLink, scenario.getPreSimulationResults(this.victimSystem).getNormalizedEmissionsMask(), victimLink, vrFreq, pathLoss));

                        isActive = true;
                        LOG.debug("Sensing failure detected. Using max power and frequency of victim");
                    } else if (availableChannels == rsRSSFrequency.length) {
                        // All channels are available
                        // Selecting random
                        int selectedFreqIndex = (int) (random.trial() * rsRSSFrequency.length);
                        selectedWSD = new EIRPFrequencyValue(rsRSSFrequency[selectedFreqIndex], linkResult.getInterferingSystemLink().getTxPower() + iLink.getInterferingSystem().getTransmitter().getAntennaGain().peakGain(),
                                rsRSSValue[selectedFreqIndex]);
                        isActive = true;
                    } else if (availableChannels > 0) {
                        // Some channels, but not all, are blocked
                        EIRPFrequencyValue[] availableChannelEIRP = new EIRPFrequencyValue[availableChannels];
                        EmissionMask eIRPInBlock = scenario.getPreSimulationResults(iLink.getInterferingSystem()).getNormalizedEIRPInBlockMask();

                        // X = index of channellist (available)
                        for (int x = 0, availableChannelIndex = 0; x < rsRSSFrequency.length; x++) {
                            if (rsRSSavailableChannel[x]) {
                                availableChannelEIRP[availableChannelIndex] = new EIRPFrequencyValue(rsRSSFrequency[x], Double.MAX_VALUE, rsRSSValue[x]);
                                // Y = index of channellist (nonavailable)
                                for (int y = 0; y < rsRSSFrequency.length; y++) {
                                    if (!rsRSSavailableChannel[y]) {
                                        double freqOffset = rsRSSFrequency[x] - rsRSSFrequency[y];
                                        double eirp = eIRPInBlock.evaluate(freqOffset);
                                        eirp += 10 * log10(this.victimSystem.getReceiver().getBandwidth()); // divide by 1000
                                        availableChannelEIRP[availableChannelIndex].setEirp(Math.min(eirp, availableChannelEIRP[availableChannelIndex].getEirp()));
                                    }
                                }

                                if (LOG.isDebugEnabled()) {
                                    LOG.debug(format("Min EIRP for %f MHz is %f dBm", availableChannelEIRP[availableChannelIndex].getFrequency(), availableChannelEIRP[availableChannelIndex].getEirp()));
                                }

                                availableChannelIndex++;
                            }
                        }

                        EIRPFrequencyValue[] bestEIRPFreq = Mathematics.sortBest(availableChannelEIRP);
                        // Choose one of the best frequencies
                        selectedWSD = bestEIRPFreq.length > 1 ? bestEIRPFreq[(int) (random.trial() * bestEIRPFreq.length)] : bestEIRPFreq[0];

                        // Ensure that selected EIRP is capped at max TX
                        // power
                        selectedWSD.setEirp(Math.min(selectedWSD.getEirp(), linkResult.getInterferingSystemLink().getTxPower() + iLink.getInterferingSystem().getTransmitter().getAntennaGain().peakGain()));

                        isActive = true;
                    }
                    // All channels are blocked (WSD will be inactive)
                    else {
                        // Find channel for victim freq
                        double sRSS = Double.NaN;
                        double victimBW = iLink.getVictimSystem().getReceiver().getBandwidth() / 2;
                        SEARCHCHANNEL: {
                            for (int x = 0; x < rsRSSFrequency.length; x++) {
                                double freq = rsRSSFrequency[x];
                                if (vrFreq > (freq - victimBW) && vrFreq < (freq + victimBW)) {
                                    sRSS = rsRSSValue[x];
                                    break SEARCHCHANNEL;
                                }
                            }
                            throw new RuntimeException("Could not find WSD channel for victim frequency");
                        }

                        selectedWSD = new EIRPFrequencyValue(vrFreq, -1000d, sRSS);
                    }


                    if (LOG.isDebugEnabled()) {
                        LOG.debug(format("Selected EIRP is %f dBm and freq is %f", selectedWSD.getEirp(), selectedWSD.getFrequency()));
                    }



                    double sensitivity = this.victimSystem.getReceiver().getSensitivity();
                    if ( result.getValue(DRSSVector) > sensitivity) {
                        if ( isActive ) {
                            crResults.getBarChartResultTypes().get(0).getChartPoints().add(new BarChartValue(""+selectedWSD.getFrequency(), selectedWSD.getEirp()));
                            for (int i=0; i<rsRSSFrequency.length; i++) {
                                Double freq = rsRSSFrequency[i];
                                crResults.getBarChartResultTypes().get(1).getChartPoints().add(new BarChartValue(""+freq, freq.equals(selectedWSD.getFrequency()) ? 1d : 0d));
                            }
                        }
                    }

                    get(crCount, srssValues).add(selectedWSD.getsRSS());
                    get(crCount, wsdfValues).add(selectedWSD.getFrequency());
                    get(crCount, wsdeValues).add(selectedWSD.getEirp());
                    get(crCount, vFreqValues).add(vrFreq);

                    linkResult.getSensingLinkResult().setTxRxPathLoss( pathLoss );
                    linkResult.getInterferingSystemLink().setTxPower(selectedWSD.getEirp() - gt.getAntennaGain().peakGain());
                    linkResult.getInterferingSystemLink().setFrequency(selectedWSD.getFrequency());
                    crCount++;


                }
            }
        }

        VectorGroupResultType srss = new VectorGroupResultType(CRV_SRSS, "dBm");
        for (int i = 0; i < srssValues.size(); i++) {
            srss.addVector(new NamedVectorResult("WSD " + i, new VectorResult(srssValues.get(i))));
        }
        crResults.getVectorGroupResultTypes().add(srss);

        VectorGroupResultType wsdf = new VectorGroupResultType(CRV_WSD_F, "MHz");
        for (int i = 0; i < wsdfValues.size(); i++) {
            wsdf.addVector(new NamedVectorResult("WSD " + i, new VectorResult(wsdfValues.get(i))));
        }
        crResults.getVectorGroupResultTypes().add(wsdf);

        VectorGroupResultType wsde = new VectorGroupResultType(CRV_WSD_EIRP, "dBm");
        for (int i = 0; i < wsdeValues.size(); i++) {
            wsde.addVector(new NamedVectorResult("WSD " + i, new VectorResult(wsdeValues.get(i))));
        }
        crResults.getVectorGroupResultTypes().add(wsde);

        VectorGroupResultType vFreq = new VectorGroupResultType(CRV_VIC_FREQ, "MHz");
        for (int i = 0; i < vFreqValues.size(); i++) {
            vFreq.addVector(new NamedVectorResult("WSD " + i, new VectorResult(vFreqValues.get(i))));
        }
        crResults.getVectorGroupResultTypes().add(vFreq);


        return crResults;
    }

    private List<InterferenceLink<GenericSystem>> getCRLinks(Scenario scenario) {
        List<InterferenceLink<GenericSystem>> result = new ArrayList<>();
        for (InterferenceLink link : scenario.getInterferenceLinks()) {
            if (link.getInterferingSystem() instanceof GenericSystem) {
                GenericSystem sys = (GenericSystem) link.getInterferingSystem();
                if ( sys.getTransmitter().isInterfererCognitiveRadio() ) {
                    result.add(link);
                }
            }
        }

        return result;
    }

    private List<List<Double>> init( int size ) {
        List<List<Double>> vectors = new ArrayList<>();
        for (int i=0; i<size; i++) {
            vectors.add( new ArrayList<Double>());
        }
        return vectors;
    }

    private List<Double> get( int index, List<List<Double>> values ) {
        List<Double> result;
        if ( values.size() == index ) {
            result = new ArrayList<>();
            values.add( result );
        } else {
            result = values.get(index);
        }
        return result;
    }

    private int crCount(Scenario scenario) {
        int crCount = 0;
        for (InterferenceLink link : scenario.getInterferenceLinks()) {
            Transmitter transmitter = link.getInterferingSystem().getTransmitter();
            if ( transmitter instanceof GenericTransmitter && ((GenericTransmitter)transmitter).isInterfererCognitiveRadio() ) {
                crCount++;
            }
        }
        return crCount;
    }


    @Override
    public void consistencyCheck(Scenario scenario, List<Object> path, Input input, Validator<Input> validator) {
        // not used. Internal plugin
    }

    @Override
    public Description description() {
        return new DescriptionImpl( COGNITIVE, "Internal plugin. Should not be exposed");
    }

    private void initChannels() {
        Distribution frequencyDist = victimSystem.getFrequency();

        List<BigDecimal> frequencies = new ArrayList<BigDecimal>();

        if (frequencyDist instanceof UserDefinedDistributionImpl || frequencyDist instanceof UniformDistributionImpl || frequencyDist instanceof ConstantDistributionImpl || frequencyDist instanceof DiscreteUniformDistributionImpl) {
            Bounds bounds = frequencyDist.getBounds();
            BigDecimal frequency = new BigDecimal(bounds.getMin());
            double max = bounds.getMax();

            BigDecimal bandwidth;
            if (frequencyDist instanceof DiscreteUniformDistributionImpl) {
                bandwidth = new BigDecimal(((DiscreteUniformDistributionImpl)frequencyDist).getStep());
                frequency = frequency.add(bandwidth.divide(new BigDecimal(2)));
            } else {
                bandwidth = new BigDecimal(victimSystem.getReceiver().getBandwidth());
            }

            do {
                frequencies.add(frequency);
            } while ((frequency = frequency.add(bandwidth)).doubleValue() <= max);
        } else if (frequencyDist instanceof StairDistributionImpl) {
            DiscreteFunction cdf = (DiscreteFunction) ((StairDistributionImpl) frequencyDist).getCdf();
            for (Point2D p : cdf.points()) {
                frequencies.add(new BigDecimal(p.getX()));
            }
        } else {
            throw new IllegalStateException("Unsupported frequency distribution in victimlink (for cognitive radio interferers): " + (frequencyDist != null ? frequencyDist.getClass().getName() : "null"));
        }

        rsRSSFrequency = new double[frequencies.size()];
        for (int x = 0, stop = frequencies.size(); x < stop; x++) {
            rsRSSFrequency[x] = frequencies.get(x).doubleValue();
        }
        rsRSSValue = new double[rsRSSFrequency.length];
        rsRSSavailableChannel = new boolean[rsRSSFrequency.length];
        //usingRsRSSFrequency = true;
    }


    /**
     * Calculates the path loss between the interfering transmitter and the wanted transmitter for the sRSS algorithm.
     *
     * @param result mutable interference link result
     * @param rFreq frequency of the interferer
     * @return path loss in dB
     */
    private double calculatePathLoss( MutableInterferenceLinkResult result, GenericTransmitter gt, double rFreq) {
        PropagationModel propagationModel = gt.getSensingLink().getPropagationModel();
        result.getSensingLinkResult().setFrequency(rFreq);
        result.getSensingLinkResult().trialTxRxInSameBuilding();
        return propagationModel.evaluate(result.getSensingLinkResult());
    }

    /**
     * Calculate the sRSS value
     *<p>
     * the equation is as follow
     *  <p>
     *      <code>sRSS = Vt_Power + Tx_Gain - pathLoss + Rx_Gain();</code>
     *
     * @param sensingResult link result
     * @param il interference link
     * @param wtUnwantedEmission unwanted emission mask of the victim link transmitter
     * @param link link result
     * @param wsdFreq frequency of the white space device (the interfering device)
     * @param pathLoss pathloss between the ILT and the ILR
     * @return the sRSS value in dBm
     */
    private double getSRSS(LinkResult sensingResult, InterferenceLink<GenericSystem> il, EmissionMask wtUnwantedEmission, LinkResult link, double wsdFreq, double pathLoss) {
        double wtPower = powerCalculation(il, wtUnwantedEmission, link, wsdFreq);
        double sRSS = wtPower + sensingResult.txAntenna().getGain() - pathLoss + sensingResult.rxAntenna().getGain();
        // note that the path loss is not frequency dependent in the budget link.

        if (LOG.isDebugEnabled()) {
            LOG.debug(format("Calculating sRSS %f + %f -%f + %f = %f", wtPower, sensingResult.txAntenna().getGain(), pathLoss, sensingResult.rxAntenna().getGain(), sRSS));
        }
        return sRSS;
    }

    /**
     *  Calculate the power of the victim link transmitter related to the sRSS calculation
     *
     * @param il interference link
     * @param wtUnwantedEmission unwanted emission mask of the victim link transmitter
     * @param link link result
     * @param wsdFreq frequency of the white space device (the interfering device)
     * @return power of the victim link transmitter
     */
    private double powerCalculation(InterferenceLink<GenericSystem> il, EmissionMask wtUnwantedEmission, LinkResult link, double wsdFreq) {
        // Power of the Wanted transmitter
        double rWtPower = link.getTxPower();

        // Relative unwanted emission in dBc
        double rWtUnwantedEmissionRel = wtUnwantedEmissions(wtUnwantedEmission, link, il, wsdFreq);

        // Calculation of unwanted emission
        double wtUnwantedEmissionAbs = rWtPower + rWtUnwantedEmissionRel;
        if (LOG.isDebugEnabled()) {
            LOG.debug(format("Victim Link Transmitter Tx Power trial = %f", rWtPower));
            LOG.debug(format("Absolute Unwanted Emission at WT = wtPower + RelUnwanted + wtPower Gain = %f", wtUnwantedEmissionAbs));
        }

        // Optional thresholding with unwanted emission floor
        if (il.getVictimSystem().getTransmitter().isUsingEmissionsFloor()) {
            double rWtUnwantedReference = wtUnwantedReference(link, il, wsdFreq);
            wtUnwantedEmissionAbs = Math.max(wtUnwantedEmissionAbs, rWtUnwantedReference);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Using unwanted emission floor at the Wt");
                LOG.debug(format("Wt Absolute Unwanted Emission = MAX(Abs Unwanted, Ref Unwanted) = %f", wtUnwantedEmissionAbs));
            }
        }
        return wtUnwantedEmissionAbs;
    }

    /**
     * Set the emission floor at the victim link transmitter for the sRSS calculation
     *<p>
     * the main fundamental equation of this method is as follow:
     * <p>
     *     <code>rResult = unWantedReference.integrate(It_freq - Vt_freq, It_Bandwidth);</code>
     *
     * @param link link result
     * @param il interference link
     * @param rFreqIt frequency of the interfering system
     * @return return the emission floor at the victim link transmitter
     */
    private double wtUnwantedReference(LinkResult link, InterferenceLink<GenericSystem> il, double rFreqIt) {
        double rFreqWt = link.getFrequency();
        GenericTransmitter transmitter = il.getInterferingSystem().getTransmitter();
        double rItBandwidth = transmitter.getSensingLink().getBandwidth() / 1000;
        EmissionMask unWantedReference = il.getVictimSystem().getTransmitter().getEmissionsFloor();

        double rResult = unWantedReference.integrate(rFreqIt - rFreqWt, rItBandwidth);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Victim Link Transmitter Frequency = " + rFreqWt);
            LOG.debug("It Receiver Frequency = " + rFreqIt);
            LOG.debug("It Receiver Bandwith = " + rItBandwidth);
            LOG.debug(format("Reference Unwanted Emission = .integrate((ItFreq - WtFreq), ItBandwith) = %f dBc", rResult));
        }

        return rResult;
    }

    /**
     * Set the unwanted emission mask for the victim link transmitter
     *<p>
     * the equation is as follow
     * <p>
     *     <code>rResult = wtUnwantedEmission.integrate(It_freq - Vt_freq, It_Bandwidth);</code>
     *
     * @param wtUnwantedEmission emission mask of the victim link transmitter
     * @param link link result
     * @param il interference link
     * @param rFreqIt frequency of the interfering system
     * @return the unwanted emission mask of the victim link transmitter
     */
    private double wtUnwantedEmissions(EmissionMask wtUnwantedEmission, LinkResult link, InterferenceLink<GenericSystem> il, double rFreqIt) {
        double rFreqWt = link.getFrequency();
        GenericTransmitter transmitter = il.getInterferingSystem().getTransmitter();
        double rItBandwidth = transmitter.getSensingLink().getBandwidth() / 1000;

        double rResult = wtUnwantedEmission.integrate(rFreqIt - rFreqWt, rItBandwidth);
        if (LOG.isDebugEnabled()) {
            LOG.debug(format("Victim Link Transmitter Frequency = %f", rFreqWt));
            LOG.debug(format("It Receiver Frequency = %f", rFreqIt));
            LOG.debug(format("It Receiver Bandwith = %f", rItBandwidth));
            LOG.debug(format("Relative Unwanted Emission = .integrate((ItFreq - WtFreq), ItBandwith) = %f dBc", rResult));
        }

        return rResult;
    }


    /**
     * set the antenna gain for the interfering link transmitter and the victim link transmitter as part of the sRSS <br>
     *     calculation
     * @param result mutable interference link result
     */
    private static void wtItPathAntGains(MutableInterferenceLinkResult result) {
        Transmitter it = result.getInterferenceLink().getInterferingSystem().getTransmitter();
        double itWtAntennaGain = it.getAntennaGain().evaluate(result.getSensingLinkResult(), result.getSensingLinkResult().rxAntenna() );

        Transmitter vt = result.getInterferenceLink().getVictimSystem().getTransmitter();
        double wtItAntennaGain = vt.getAntennaGain().evaluate(result.getSensingLinkResult(), result.getSensingLinkResult().txAntenna());

        result.getSensingLinkResult().rxAntenna().setGain(itWtAntennaGain);
        result.getSensingLinkResult().txAntenna().setGain(wtItAntennaGain);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Sensing Link ILT->VLT Antenna Gain = " + itWtAntennaGain);
            LOG.debug("Sensing Link VLT->ILT Antenna Gain = " + wtItAntennaGain);
        }
    }

    /**
     * set the azimuth and elevation for the interfering link transmitter and the victim link transmitter path as part <br>
     *     of the sRSS calculation
     * @param result mutable interference link result
     */
    private void sLPathAntAziElev(MutableInterferenceLinkResult result) {
        double itAntHeight = result.getInterferingSystemLink().txAntenna().getHeight();
        result.getSensingLinkResult().rxAntenna().setHeight( itAntHeight );
        double itAntTilt = result.getInterferingSystemLink().txAntenna().getTilt();
        double wtAntHeight = result.getVictimSystemLink().txAntenna().getHeight();
        result.getSensingLinkResult().txAntenna().setHeight( wtAntHeight );
        double wtAntTilt = result.getVictimSystemLink().txAntenna().getTilt();

        double rILangle = result.getInterferingSystemLink().getTxRxAngle() - 180;
        double rILAzi = result.getInterferingSystemLink().txAntenna().getAzimuth();
        double rVLangle = result.getVictimSystemLink().getTxRxAngle();
        double rVLAzi = result.getVictimSystemLink().txAntenna().getAzimuth();
        double rItWtangle = Mathematics.calculateKartesianAngle(result.getVictimSystemLink().txAntenna().getPosition(),result.getInterferingSystemLink().txAntenna().getPosition());

        double itWtAzimuth = LinkCalculator.calculateItVictimAzimuth(rILangle, rILAzi, rItWtangle, "ILT -> VLT");
        itWtAzimuth = LinkCalculator.convertAngleToConfineToHorizontalDefinedRange(itWtAzimuth);
        double itWtElevation = LinkCalculator.calculateElevationWithTilt(result.getVictimSystemLink().txAntenna().getPosition(), wtAntHeight,result.getInterferingSystemLink().txAntenna().getPosition(), itAntHeight, itAntTilt, itWtAzimuth, "VLR ->VLT");
        itWtElevation = LinkCalculator.convertAngleToConfineToVerticalDefinedRange(itWtElevation);

        double wtItAzimuth = LinkCalculator.calculateItVictimAzimuth(rVLangle, rVLAzi, rItWtangle, "VLT -> ILT");
        wtItAzimuth = LinkCalculator.convertAngleToConfineToHorizontalDefinedRange(wtItAzimuth);
        double wtItElevation = LinkCalculator.calculateElevationWithTilt(result.getInterferingSystemLink().txAntenna().getPosition(), itAntHeight, result.getVictimSystemLink().txAntenna().getPosition(), wtAntHeight, wtAntTilt, wtItAzimuth,"VLR ->VLT");
        wtItElevation = LinkCalculator.convertAngleToConfineToVerticalDefinedRange(wtItElevation);


        result.getSensingLinkResult().rxAntenna().setAzimuth(itWtAzimuth);
        result.getSensingLinkResult().rxAntenna().setElevation(itWtElevation);
        result.getSensingLinkResult().txAntenna().setAzimuth(wtItAzimuth);
        result.getSensingLinkResult().txAntenna().setElevation(wtItElevation);
        result.getSensingLinkResult().rxAntenna().setElevationCompensation(result.getInterferingSystemLink().txAntenna().getElevationCompensation());
        result.getSensingLinkResult().txAntenna().setElevationCompensation(result.getVictimSystemLink().txAntenna().getElevationCompensation());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Sensing Link ILT->VLT azimuth = " + itWtAzimuth);
            LOG.debug("Sensing Link ILT->VLT elevation = " + itWtElevation);
            LOG.debug("Sensing Link VLT->ILT azimuth = " + wtItAzimuth);
            LOG.debug("Sensing Link VLT->ILT elevation = " + wtItElevation);
        }
    }

    /**
     * set the distance between the interfering link transmitter and the victim link transmitter
     *
     * @param result mutable interference link result
     */
    private void wt2itPath(MutableInterferenceLinkResult result ) {
        double wtItDistance = Mathematics.distance(result.getVictimSystemLink().txAntenna().getPosition(),
                result.getInterferingSystemLink().txAntenna().getPosition());

        result.getSensingLinkResult().setTxRxDistance( wtItDistance );
        if (LOG.isDebugEnabled()) {
            LOG.debug("Sensing Link ILT->VLT distance = " + wtItDistance);
        }
    }

    /**
     * trial the sensing variables (i.e. distance, azimuth, elevation and antenna gains
     *
     * @param result mutable interference link result
     */
    private void sensingTrial( MutableInterferenceLinkResult result) {
        wt2itPath(result);
        sLPathAntAziElev(result);
        wtItPathAntGains(result);
    }


}
