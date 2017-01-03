package org.seamcat.plugin;

import org.seamcat.model.antenna.HorizontalVerticalAntenna;
import org.seamcat.model.antenna.PeakGainAntenna;
import org.seamcat.model.antenna.SphericalAntenna;
import org.seamcat.model.plugin.VoidInput;
import org.seamcat.model.plugin.antenna.AntennaGainFactory;
import org.seamcat.model.plugin.antenna.AntennaGainPlugin;
import org.seamcat.model.plugin.antenna.HorizontalVerticalInput;
import org.seamcat.model.plugin.antenna.SphericalInput;

public class AntennaGainFactoryImpl implements AntennaGainFactory {

    @Override
    public AntennaGainConfiguration<VoidInput> getPeakGainAntenna() {
        return getByClass( PeakGainAntenna.class );
    }

    @Override
    public AntennaGainConfiguration<VoidInput> getPeakGainAntenna(double peakGain) {
        return getByClass( PeakGainAntenna.class, new VoidInput() {}, peakGain);
    }

    @Override
    public AntennaGainConfiguration<HorizontalVerticalInput> getHorizontalVerticalAntenna() {
        return getByClass( HorizontalVerticalAntenna.class );
    }

    @Override
    public AntennaGainConfiguration<HorizontalVerticalInput> getHorizontalVerticalAntenna(HorizontalVerticalInput input, double peakGain) {
        return getByClass( HorizontalVerticalAntenna.class, input, peakGain);
    }

    @Override
    public AntennaGainConfiguration<SphericalInput> getSphericalAntenna() {
        return getByClass( SphericalAntenna.class );
    }

    @Override
    public AntennaGainConfiguration<SphericalInput> getSphericalAntenna(SphericalInput input, double peakGain) {
        return getByClass( SphericalAntenna.class, input, peakGain );
    }

    @Override
    public <T> AntennaGainConfiguration<T> getByClass(Class<? extends AntennaGainPlugin<T>> clazz) {
        return new AntennaGainConfiguration<T>(clazz, null);
    }

    @Override
    public <T> AntennaGainConfiguration<T> getByClass(Class<? extends AntennaGainPlugin<T>> clazz, T input, double peakGain) {
        return getByClass(clazz).setModel(input).setPeakGain(peakGain);
    }

}
