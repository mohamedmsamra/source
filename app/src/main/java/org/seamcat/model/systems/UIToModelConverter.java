package org.seamcat.model.systems;

import org.seamcat.cdma.CDMADownlinkSystem;
import org.seamcat.cdma.CDMAUplinkSystem;
import org.seamcat.function.DiscreteFunction;
import org.seamcat.function.EmissionMaskImpl;
import org.seamcat.model.Receiver;
import org.seamcat.model.Transmitter;
import org.seamcat.model.TransmitterToReceiverPath;
import org.seamcat.model.cellular.CellularLayout;
import org.seamcat.model.cellular.CellularReceiverImpl;
import org.seamcat.model.cellular.CellularTransmitterImpl;
import org.seamcat.model.core.InterferenceLink;
import org.seamcat.model.core.SystemSimulationModel;
import org.seamcat.model.distributions.AbstractDistribution;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.distributions.StairDistributionImpl;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.functions.*;
import org.seamcat.model.generic.Defaults;
import org.seamcat.model.generic.InterfererDensity;
import org.seamcat.model.generic.InterferersDensityUI;
import org.seamcat.model.generic.Train_Defaults;
import org.seamcat.model.plugin.OptionalDoubleValue;
import org.seamcat.model.systems.cdma.*;
import org.seamcat.model.systems.generic.ReceiverModel;
import org.seamcat.model.systems.generic.SystemModelGeneric;
import org.seamcat.model.systems.generic.T_ReceiverModel;
import org.seamcat.model.systems.generic.TransmitterModel;
import org.seamcat.model.systems.generic.TransmitterReceiverPathModel;
import org.seamcat.model.systems.ofdma.*;
import org.seamcat.model.types.LocalEnvironment;
import org.seamcat.model.workspace.InterferenceLinkUI;
import org.seamcat.ofdma.DownlinkOfdmaSystem;
import org.seamcat.ofdma.UplinkOfdmaSystem;
import org.seamcat.plugin.AntennaGainConfiguration;
import org.seamcat.plugin.CoverageRadiusConfiguration;
import org.seamcat.plugin.PropagationModelConfiguration;
import org.seamcat.presentation.systems.CellularPosition;
import org.seamcat.scenario.*;
import org.seamcat.simulation.calculator.InterferenceCalculator;

import java.util.Arrays;
import java.util.List;

import static org.seamcat.model.factory.SeamcatFactory.antennaGain;

public class UIToModelConverter {

    public static InterferenceLink convert( SystemSimulationModel victim, SystemSimulationModel interferer, InterferenceLinkUI uiLink ) {
        InterferenceLink link = new InterferenceLink(victim, interferer);

        link.setRelativeLocation(uiLink.path().relativeLocation());
        link.setPathLossCorrelation(uiLink.path().pathLossCorrelation());
        link.setPropagationModel((PropagationModelConfiguration) uiLink.path().propagationModel());

        return link;
    }

    public static SystemSimulationModel convert(SystemModel systemModel ) {
        if ( systemModel instanceof SystemModelOFDMAUpLink ) {
            SystemModelOFDMAUpLink ofdmaUpLink = (SystemModelOFDMAUpLink) systemModel;
            OFDMAUpLinkGeneralTab general = ofdmaUpLink.generalSettings();
            UplinkOfdmaSystem uplinkOfdmaSystem = new UplinkOfdmaSystem(new CDMADownlinkSystem());

            CellularSystemImpl radioSystem = uplinkOfdmaSystem.getSystemSettings();
            radioSystem.setName(systemModel.description().name());
            radioSystem.setOFDMASettings(convert(general));
            radioSystem.setReceiverNoiseFigure(general.generalSettings().receiverNoiseFigure());
            radioSystem.setBandwidth(general.generalSettings().bandwidth());
            radioSystem.setFrequency(ofdmaUpLink.general().frequency());
            radioSystem.setHandoverMargin(general.generalSettings().handoverMargin());
            radioSystem.setMinimumCouplingLoss(general.generalSettings().minimumCouplingLoss());
            radioSystem.setUsersPerCell(general.ofdmaCapacity().usersPerBS());

            double rxBandwidth = general.generalSettings().bandwidthResourceBlock() * general.generalSettings().maxSubcarriersBs();
            radioSystem.setReceiver(getDmaReceiver(general.receiverSettings(), general.generalSettings().receiverNoiseFigure(),
                    general.receiverSettings().blockingMask(),
                    rxBandwidth,
                    general.localEnvironments().receiverEnvironments(),
                    (AntennaGainConfiguration) ofdmaUpLink.positioning().baseStation().antennaGain(),
                    ofdmaUpLink.positioning().baseStation().antennaHeight()
            ));

            double txBandwidth  = general.generalSettings().bandwidthResourceBlock() * general.generalSettings().maxSubcarriersMs();
            radioSystem.setTransmitter(getDmaTransmitter(
                    ofdmaUpLink.positioning().mobile().antennaHeight(),
                    general.transmitterSettings().emissionMask(),
                    convert(general.transmitterSettings().emissionFloor().getMaskFunction()),
                    general.transmitterSettings().emissionFloor().isRelevant(),
                    txBandwidth,
                    new Bounds(txBandwidth, txBandwidth, true),
                    general.localEnvironments().transmitterEnvironments(),
                    antennaGain().getPeakGainAntenna()
            ));

            handlePosition(radioSystem, ofdmaUpLink.positioning().position());

            radioSystem.getLink().setMobileStation(convert(ofdmaUpLink.positioning().mobile()));
            radioSystem.getLink().setBaseStation(convert(ofdmaUpLink.positioning().baseStation()));
            radioSystem.getLink().setPropagationModel(general.propagationModel());

            SystemSimulationModel result = new SystemSimulationModel(radioSystem, ofdmaUpLink.description().name());
            result.setDMASystem(uplinkOfdmaSystem);
            return result;
        } else if ( systemModel instanceof SystemModelOFDMADownLink ) {
            SystemModelOFDMADownLink ofdmaD = (SystemModelOFDMADownLink) systemModel;
            DownlinkOfdmaSystem downLinkOfdmaSystem = new DownlinkOfdmaSystem(new CDMADownlinkSystem());

            OFDMADownLinkGeneralTab general = ofdmaD.generalSettings();

            CellularSystemImpl radioSystem = downLinkOfdmaSystem.getSystemSettings();
            radioSystem.setName(systemModel.description().name());
            radioSystem.setOFDMASettings(convert(general));
            radioSystem.setReceiverNoiseFigure(general.generalSettings().receiverNoiseFigure());
            radioSystem.setBandwidth(general.generalSettings().bandwidth());
            radioSystem.setFrequency(ofdmaD.general().frequency());
            radioSystem.setHandoverMargin(general.generalSettings().handoverMargin());
            radioSystem.setMinimumCouplingLoss(general.generalSettings().minimumCouplingLoss());
            radioSystem.setUsersPerCell(general.ofdmaCapacity().usersPerBS());

            double rxBandwidth = general.generalSettings().bandwidthResourceBlock() * general.generalSettings().maxSubcarriersMs();
            radioSystem.setReceiver(getDmaReceiver(general.receiverSettings(),general.generalSettings().receiverNoiseFigure(),
                    general.receiverSettings().blockingMask(),
                    rxBandwidth,
                    general.localEnvironments().receiverEnvironments(),
                    antennaGain().getPeakGainAntenna(),
                    ofdmaD.positioning().mobile().antennaHeight()
            ));

            double txBandwidth  = general.generalSettings().bandwidthResourceBlock() * general.generalSettings().maxSubcarriersBs();
            radioSystem.setTransmitter(getDmaTransmitter(
                    ofdmaD.positioning().baseStation().antennaHeight(),
                    general.transmitterSettings().emissionMask(),
                    convert(general.transmitterSettings().emissionFloor().getMaskFunction()),
                    general.transmitterSettings().emissionFloor().isRelevant(),
                    txBandwidth,
                    new Bounds(txBandwidth, txBandwidth, true),
                    general.localEnvironments().transmitterEnvironments(),
                    (AntennaGainConfiguration) ofdmaD.positioning().baseStation().antennaGain()
            ));

            handlePosition(radioSystem, ofdmaD.positioning().position());

            radioSystem.getLink().setMobileStation(convert(ofdmaD.positioning().mobile()));
            radioSystem.getLink().setBaseStation(convert(ofdmaD.positioning().baseStation()));
            radioSystem.getLink().setPropagationModel(general.propagationModel());

            SystemSimulationModel result = new SystemSimulationModel(radioSystem, ofdmaD.description().name());
            result.setDMASystem( downLinkOfdmaSystem );
            return result;

        } else if ( systemModel instanceof SystemModelCDMAUpLink) {
            SystemModelCDMAUpLink cdmaUpLink = (SystemModelCDMAUpLink) systemModel;
            CDMAUplinkSystem cdmaUplinkSystem = new CDMAUplinkSystem(new CDMADownlinkSystem());

            CDMAUpLinkGeneralTab general = cdmaUpLink.generalSettings();

            CellularSystemImpl radioSystem = cdmaUplinkSystem.getSystemSettings();
            radioSystem.setName(systemModel.description().name());
            radioSystem.setCDMASettings(convert(general));
            radioSystem.setReceiverNoiseFigure(general.generalSettings().receiverNoiseFigure());
            radioSystem.setBandwidth(general.generalSettings().bandwidth());
            radioSystem.setFrequency(cdmaUpLink.general().frequency());
            radioSystem.setHandoverMargin(general.generalSettings().handoverMargin());
            radioSystem.setMinimumCouplingLoss(general.generalSettings().minimumCouplingLoss());
            radioSystem.setUsersPerCell(general.cdmaCapacity().initUsersPerCell());
            radioSystem.setReceiver(getDmaReceiver(general.receiverSettings(),general.generalSettings().receiverNoiseFigure(),
                    general.receiverSettings().blockingMask(),
                    general.generalSettings().bandwidth(),
                    general.localEnvironments().receiverEnvironments(),
                    (AntennaGainConfiguration) cdmaUpLink.positioning().baseStation().antennaGain(),
                    cdmaUpLink.positioning().baseStation().antennaHeight()
            ));
            double bandwidth = general.generalSettings().bandwidth();
            radioSystem.setTransmitter(getDmaTransmitter(
                    cdmaUpLink.positioning().mobile().antennaHeight(),
                    general.transmitterSettings().emissionMask(),
                    convert(general.transmitterSettings().emissionFloor().getMaskFunction()),
                    general.transmitterSettings().emissionFloor().isRelevant(),
                    bandwidth,
                    new Bounds(bandwidth, bandwidth, true),
                    general.localEnvironments().transmitterEnvironments(),
                    antennaGain().getPeakGainAntenna()
            ));

            handlePosition(radioSystem, cdmaUpLink.positioning().position());

            radioSystem.getLink().setMobileStation(convert(cdmaUpLink.positioning().mobile()));
            radioSystem.getLink().setBaseStation(convert(cdmaUpLink.positioning().baseStation()));
            radioSystem.getLink().setPropagationModel(general.propagationModel());

            SystemSimulationModel result = new SystemSimulationModel(radioSystem, cdmaUpLink.description().name());
            result.setDMASystem( cdmaUplinkSystem );
            return result;

        } else if ( systemModel instanceof SystemModelCDMADownLink) {
            SystemModelCDMADownLink downLink = (SystemModelCDMADownLink) systemModel;
            CDMADownlinkSystem cdmaUplinkSystem = new CDMADownlinkSystem();

            CDMADownLinkGeneralTab general = downLink.generalSettings();

            CellularSystemImpl radioSystem = cdmaUplinkSystem.getSystemSettings();
            radioSystem.setName(systemModel.description().name());
            radioSystem.setCDMASettings(convert(general));
            radioSystem.setReceiverNoiseFigure(general.generalSettings().receiverNoiseFigure());
            radioSystem.setBandwidth(general.generalSettings().bandwidth());
            radioSystem.setFrequency(downLink.general().frequency());
            radioSystem.setHandoverMargin(general.generalSettings().handoverMargin());
            radioSystem.setMinimumCouplingLoss(general.generalSettings().minimumCouplingLoss());
            radioSystem.setUsersPerCell(general.cdmaCapacity().initUsersPerCell());
            radioSystem.setReceiver(getDmaReceiver(general.receiverSettings(), general.generalSettings().receiverNoiseFigure(),
                    general.receiverSettings().blockingMask(),
                    general.generalSettings().bandwidth(),
                    general.localEnvironments().receiverEnvironments(),
                    antennaGain().getPeakGainAntenna(),
                    downLink.positioning().baseStation().antennaHeight()
            ));
            double bandwidth = general.generalSettings().bandwidth();
            radioSystem.setTransmitter(getDmaTransmitter(
                    downLink.positioning().baseStation().antennaHeight(),
                    general.transmitterSettings().emissionMask(),
                    convert(general.transmitterSettings().emissionFloor().getMaskFunction()),
                    general.transmitterSettings().emissionFloor().isRelevant(),
                    bandwidth,
                    new Bounds(bandwidth, bandwidth, true),
                    general.localEnvironments().transmitterEnvironments(),
                    (AntennaGainConfiguration) downLink.positioning().baseStation().antennaGain()
            ));

            handlePosition(radioSystem, downLink.positioning().position());

            radioSystem.getLink().setMobileStation(convert(downLink.positioning().mobile()));
            radioSystem.getLink().setBaseStation(convert(downLink.positioning().baseStation()));
            radioSystem.getLink().setPropagationModel(general.propagationModel());

            SystemSimulationModel result = new SystemSimulationModel(radioSystem, downLink.description().name());
            result.setDMASystem( cdmaUplinkSystem );
            return result;

        } 
        
        else /*generic*/{
            SystemModelGeneric generic = (SystemModelGeneric) systemModel;
            Transmitter transmitter = convert(generic.transmitter());
            transmitter.setLocalEnvironments(generic.path().localEnvironments().transmitterEnvironments());
            Receiver receiver = convert(generic.receiver());
        //    Receiver t_receiver = convert(generic.t_receiver());
             
            receiver.setHeight(Factory.distributionFactory().getConstantDistribution(5));
             
             receiver.setLocalEnvironments(generic.path().localEnvironments().receiverEnvironments());
            // receiver.setHeight(Factory.distributionFactory().getConstantDistribution(5));
        //    t_receiver.setLocalEnvironments(generic.path().localEnvironments().receiverEnvironments());
          
      
           
            	GenericSystemImpl system = new GenericSystemImpl(generic.description().name(),
                        transmitter,
                        receiver,
                        convert(generic.path()),
                        generic.general().frequency(),
                        getDensity(generic));
            	return new SystemSimulationModel(system, generic.description().name());
            
            
        }
		
    }

    private static InterfererDensity getDensity(SystemModelGeneric generic) {
        final InterferersDensityUI ui = generic.path().density();

        return new InterfererDensity() {
            @Override
            public double getDensityTx() {
                return ui.densityTx();
            }

            @Override
            public double getProbabilityOfTransmission() {
                return ui.probabilityOfTransmission();
            }

            @Override
            public Function getActivity() {
                return ui.activity();
            }

            @Override
            public double getHourOfDay() {
                return ui.hourOfDay();
            }
        };
    }

 
    // THIS IS HORRIBLE CHANGE SOON
    private static EmissionMask convert( MaskFunction function ) {
        EmissionMaskImpl result = new EmissionMaskImpl();

        for (Point2D point : function.getPoints()) {
            result.addPoint( point, function.getMask(point));
        }
        return result;
    }

    private static MobileStationImpl convert(OFDMAMobile mobile) {
        return new MobileStationImpl((AbstractDistribution)mobile.antennaHeight(), (AbstractDistribution)mobile.antennaGain(),
                new StairDistributionImpl(
                        new DiscreteFunction( Arrays.asList(new Point2D[]{new Point2D(0, 0.25), new Point2D(3, 0.50),
                                new Point2D(30, 0.75), new Point2D(100, 1)}))));
    }

    private static void handlePosition(CellularSystemImpl systemSettings, CellularPosition position) {
        systemSettings.getLayout().setCellRadius(position.cellRadius());
        systemSettings.getLayout().setGenerateWrapAround(position.generateWrapAround());
        systemSettings.getLayout().setIndexOfReferenceCell(position.referenceCellId());
        systemSettings.getLayout().setReferenceSector(position.referenceSector());
        systemSettings.getLayout().setMeasureInterferenceFromEntireCluster(position.measureFromEntireCluster());
        systemSettings.getLayout().setSectorSetup( position.sectorType() );
        int tiers = position.tiers();
        if( tiers == 0 ) {
            systemSettings.getLayout().setTierSetup(CellularLayout.TierSetup.SingleCell);
        } else if ( tiers == 1 ) {
            systemSettings.getLayout().setTierSetup(CellularLayout.TierSetup.OneTier);
        } else {
            systemSettings.getLayout().setTierSetup(CellularLayout.TierSetup.TwoTiers);
        }
        systemSettings.getLayout().setSystemLayout( position.layout() );
    }

    private static OFDMASettingsImpl convert(OFDMAUpLinkGeneralTab general) {
        OFDMASettingsImpl result = new OFDMASettingsImpl(general.generalSettings().bitRateMapping());
        result.setBandwidthOfResourceBlock(general.generalSettings().bandwidthResourceBlock());
        result.setMaxSubCarriersPerBaseStation( general.generalSettings().maxSubcarriersBs() );
        result.setNumberOfSubCarriersPerMobileStation( general.generalSettings().maxSubcarriersMs());

        PathLossCorrelationImpl pl = new PathLossCorrelationImpl();
        pl.setCorrelationFactor( general.pathLossCorrelation().correlationFactor());
        pl.setPathLossVariance(general.pathLossCorrelation().pathLossVariance());
        pl.setUsingPathLossCorrelation(general.pathLossCorrelation().usePathLossCorrelation());
        result.setPathLossCorrelation(pl);

        OFDMAUpLinkImpl uplink = new OFDMAUpLinkImpl();
        uplink.setBalancingFactor(general.ofdmaUpLink().getBalancingFactor());
        uplink.setMaximumAllowedTransmitPowerOfMS(general.ofdmaUpLink().getMaximumAllowedTransmitPowerOfMS());
        uplink.setMinimumTransmitPowerOfMS(general.ofdmaUpLink().getMinimumTransmitPowerOfMS());
        uplink.setPowerScalingThreshold(general.ofdmaUpLink().getPowerScalingThreshold());
        result.setUpLinkSettings(uplink);
        return result;
    }

    private static OFDMASettingsImpl convert(OFDMADownLinkGeneralTab general) {
        OFDMASettingsImpl result = new OFDMASettingsImpl(general.generalSettings().bitRateMapping());
        result.setBandwidthOfResourceBlock(general.generalSettings().bandwidthResourceBlock());
        result.setMaxSubCarriersPerBaseStation( general.generalSettings().maxSubcarriersBs() );
        result.setNumberOfSubCarriersPerMobileStation( general.generalSettings().maxSubcarriersMs());

        PathLossCorrelationImpl pl = new PathLossCorrelationImpl();
        pl.setCorrelationFactor( general.pathLossCorrelation().correlationFactor());
        pl.setPathLossVariance( general.pathLossCorrelation().pathLossVariance());
        pl.setUsingPathLossCorrelation( general.pathLossCorrelation().usePathLossCorrelation());
        result.setPathLossCorrelation(pl);

        OFDMADownLinkImpl downlink = new OFDMADownLinkImpl();
        downlink.setBSMaximumTransmitPower(general.ofdmaDownLink().bsMaximumTransmitPower());
        result.setDownLinkSettings(downlink);
        return result;
    }

    private static CellularReceiverImpl getDmaReceiver(ReceiverSettings ui, double receiverNoiseFigure, BlockingMask mask,
                                                       double bandwidth, List<LocalEnvironment> environments,
                                                       AntennaGainConfiguration gain, Distribution height) {
        return new CellularReceiverImpl(ui, receiverNoiseFigure, mask, bandwidth, environments, gain, height);
    }

    private static CellularTransmitterImpl getDmaTransmitter(Distribution height, EmissionMask emissionMask, EmissionMask emissionFloor,
                                                             boolean usingFloor, double bandwidth,
                                                             Bounds bandwidthBounds, List<LocalEnvironment> environments,
                                                             AntennaGainConfiguration gain) {
        return new CellularTransmitterImpl(emissionMask, emissionFloor,
                usingFloor,bandwidth,
                bandwidthBounds, environments, gain, height);

    }

    private static BaseStationImpl convert(CellularBastStation bs) {
        return new BaseStationImpl((AbstractDistribution)bs.antennaHeight(), (AbstractDistribution)bs.antennaTilt(), (AntennaGainConfiguration) bs.antennaGain());
    }

    private static MobileStationImpl convert(CDMAMobile mobile) {
        MobileStationImpl ms = new MobileStationImpl((AbstractDistribution)mobile.antennaHeight(),
                (AbstractDistribution)mobile.antennaGain(), (AbstractDistribution)mobile.mobility());
        return ms;
    }

    private static CDMASettingsImpl convert(CDMAUpLinkGeneralTab ui) {
        CDMASettingsImpl result = new CDMASettingsImpl();
        result.setUpLinkSettings( convert( ui.cdmaUplink()));
        result.setCallDropThreshold( ui.generalSettings().callDropThreshold());
        result.setDeltaUsersPerCell(ui.cdmaCapacity().deltaUsersPerCell());
        result.setNumberOfTrials( ui.cdmaCapacity().numberOfTrials());
        result.setSimulateNonInterferedCapacity( ui.cdmaCapacity().simulateNonInterferedCapacity());
        result.setTargetNoiseRisePrecision( ui.cdmaCapacity().targetNoiseRisePrecision());
        result.setVoiceBitRate(ui.generalSettings().voiceBitRate());
        result.setVoiceActivityFactor( ui.generalSettings().voiceActivityFactor() );
        result.setLld( ui.generalSettings().lld());
        return result;
    }

    private static CDMASettingsImpl convert(CDMADownLinkGeneralTab ui) {
        CDMASettingsImpl result = new CDMASettingsImpl();
        result.setDownLinkSettings(convert(ui.cdmaDownLink()));
        result.setCallDropThreshold( ui.generalSettings().callDropThreshold());
        result.setDeltaUsersPerCell(ui.cdmaCapacity().deltaUsersPerCell());
        result.setNumberOfTrials( ui.cdmaCapacity().numberOfTrials());
        result.setSimulateNonInterferedCapacity( ui.cdmaCapacity().simulateNonInterferedCapacity());
        result.setToleranceOfInitialOutage(ui.cdmaCapacity().toleranceOfInitialOutage());
        result.setVoiceBitRate(ui.generalSettings().voiceBitRate());
        result.setVoiceActivityFactor( ui.generalSettings().voiceActivityFactor() );
        result.setLld( ui.generalSettings().lld() );
        return result;
    }

    public static CDMAUpLinkImpl convert(CDMAUpLink ui) {
        CDMAUpLinkImpl link = new CDMAUpLinkImpl();
        OptionalDoubleValue nr = ui.targetCellNoiseRise();
        link.setCellNoiseRise(nr.isRelevant());
        link.setTargetCellNoiseRise(nr.getValue());
        link.setMSConvergencePrecision( ui.pcConvergencePrecision());
        link.setMSMaximumTransmitPower( ui.msMaximumTransmitPower() );
        link.setMSPowerControlRange( ui.msPowerControlRange());
        link.setTargetNetworkNoiseRise(ui.targetNetworkNoiseRise());
        return link;
    }

    public static CDMADownLinkImpl convert(CDMADownLink ui) {
        CDMADownLinkImpl link = new CDMADownLinkImpl();
        link.setMaximumBroadcastChannel( ui.maxBroadcastPower() );
        link.setMaximumTrafficChannelFraction( ui.maxTrafficChannelFraction());
        link.setOverheadChannelFraction( ui.overheadChannelFraction());
        link.setPilotChannelFraction( ui.pilotChannelFraction());
        link.setSuccessThreshold( ui.successThreshold() );
        return link;
    }

    //////////////////////////////////////////////////////
    public static Receiver convert( ReceiverModel receiverModel ) {
        Receiver receiver = new Receiver();
       // receiver.setHeight( receiverModel.antennaPointing().antennaHeight());
        receiver.setAntennaGain((AntennaGainConfiguration) receiverModel.antennaGain());
        receiver.setAntennaPointing(receiverModel.antennaPointing());
        receiver.setReceptionCharacteristics(receiverModel.receptionCharacteristics());
      //receiver.setHeight(Factory.distributionFactory().getConstantDistribution(5));
        receiver.setInterferenceCriteria( receiverModel.interferenceCriteria());
        return receiver;
    }
    
    public static Receiver convert( T_ReceiverModel receiverModel ) {
        Receiver receiver = new Receiver();
        
        receiver.setHeight( receiverModel.antennaPointing().antennaHeight());
        receiver.setAntennaGain((AntennaGainConfiguration) receiverModel.antennaGain());
        receiver.setAntennaPointing(receiverModel.antennaPointing());
        receiver.setReceptionCharacteristics(receiverModel.receptionCharacteristics());
        receiver.setInterferenceCriteria( receiverModel.interferenceCriteria());
        return receiver;
    }

    public static Transmitter convert( TransmitterModel transmitterModel ) {
        Bounds bounds = InterferenceCalculator.calculateBounds(transmitterModel.emissionCharacteristics().emissionMask());
        double bandwidth = Math.rint((bounds.getMax() - bounds.getMin())*1000)/1000;// rounded to 1 kHz
        Transmitter transmitter = new Transmitter(transmitterModel.emissionCharacteristics(), bandwidth, bounds);
        transmitter.setHeight(transmitterModel.antennaPointing().antennaHeight());
        transmitter.setAntennaPointing(transmitterModel.antennaPointing());
        transmitter.setAntennaGain((AntennaGainConfiguration) transmitterModel.antennaGain());
        return transmitter;
    }

    public static TransmitterToReceiverPath convert( TransmitterReceiverPathModel model ){
        TransmitterToReceiverPath path = new TransmitterToReceiverPath();
        path.setPropagationModel((PropagationModelConfiguration) model.propagationModel());
        path.setRelativeLocationUI( model.relativeLocation());
        path.setCoverageRadius((CoverageRadiusConfiguration) model.coverageRadius());
        return path;
    }

}
