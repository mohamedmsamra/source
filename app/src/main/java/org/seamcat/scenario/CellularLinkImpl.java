package org.seamcat.scenario;

import org.seamcat.model.cellular.BaseStation;
import org.seamcat.model.cellular.CellularLink;
import org.seamcat.model.cellular.MobileStation;
import org.seamcat.model.types.PropagationModel;

public class CellularLinkImpl implements CellularLink {

    private PropagationModel propagationModel;
    private MobileStationImpl mobileStation;
    private BaseStationImpl baseStation;

    public CellularLinkImpl( PropagationModel propagationModel, MobileStationImpl mobileStation, BaseStationImpl baseStation) {
        this.propagationModel = propagationModel;
        this.mobileStation = mobileStation;
        this.baseStation = baseStation;
    }

    @Override
    public BaseStationImpl getBaseStation() {
        return baseStation;
    }

    @Override
    public MobileStationImpl getMobileStation() {
        return mobileStation;
    }

    @Override
    public PropagationModel getPropagationModel() {
        return propagationModel;
    }

    public void setPropagationModel( PropagationModel propagationModel ) {
        this.propagationModel = propagationModel;
    }

    public void setBaseStation( BaseStationImpl baseStation ) {
        this.baseStation = baseStation;
    }

    public void setMobileStation( MobileStationImpl mobileStation ) {
        this.mobileStation = mobileStation;
    }
}
