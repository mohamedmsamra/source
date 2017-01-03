package org.seamcat.model.cellular;

import org.seamcat.model.RadioSystem;
import org.seamcat.model.cellular.cdma.CDMASettings;
import org.seamcat.model.cellular.ofdma.OFDMASettings;

public interface CellularSystem extends RadioSystem {

    String REQUESTED_SUBCARRIERS = "Requested subcarriers";
    String TOTAL_INTERFERENCE    = "Total interference";
    String SUB_CARRIER_RATIO     = "Sub carrier ratio";
    String FREQUENCY             = "Frequency";
    String THERMAL_NOISE         = "Thermal noise";
    String BIT_RATE_ACHIEVED     = "Bit rate achieved";
    String RECEIVED_POWER        = "Received power";
    String SINR_ACHIEVED         = "SINR achieved";
    String CURRENT_TRANSMIT_POWER= "Current transmit power";
    String INTERFERENCE_POWER    = "Interference power";
    String POWER_CONTROL_PL      = "Power control PL";
    String POWER_CONTROL_PLILX   = "Power control PLilx";
    String INTER_SYSTEM_INTERFERENCE = "Inter system interference";
    String EXTERNAL_INTERFERENCE = "External interference";
    String EXTERNAL_INTER_BLOC   = "External interference blocking";
    String EXTERNAL_INTER_UNW    = "External interference unwanted";
    String BASE_STATION_BIT_RATE = "Base station bit rate achieved";
    String PATH_LOSS = "Path loss";
    String EFFECTIVE_PATH_LOSS = "Effective path loss";

    CellularReceiver getReceiver();

    CellularLink getLink();

    boolean isUpLink();

    CellularLayout getLayout();

    CDMASettings getCDMASettings();

    OFDMASettings getOFDMASettings();

    /**
     * Receiver specific noise figure, in dB
     */
    double getReceiverNoiseFigure();

    double getHandoverMargin();

    double getBandwidth();

    double getMinimumCouplingLoss();

    int getUsersPerCell();

    String getDescription();
}
