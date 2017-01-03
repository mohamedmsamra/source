package org.seamcat.plugin;

import org.seamcat.exception.SimulationInvalidException;
import org.seamcat.model.PluginJarFiles;
import org.seamcat.model.factory.SeamcatFactory;
import org.seamcat.model.generic.ProxyHelper;
import org.seamcat.model.plugin.antenna.AntennaGainPlugin;
import org.seamcat.model.simulation.result.AntennaResult;
import org.seamcat.model.simulation.result.LinkResult;
import org.seamcat.model.types.AntennaGain;
import org.seamcat.model.types.Configuration;

import java.util.HashMap;
import java.util.Map;

public class AntennaGainConfiguration<T> extends PluginConfiguration<AntennaGainPlugin<T>, T> implements AntennaGain<T> {

    private final static Map<Class, Double> peakGains = new HashMap<>();
    public static void addPeakGain( Class<?> clazz, double peakGain ) {
        peakGains.put( clazz, peakGain );
    }

    @Override
    public AntennaGainConfiguration<T> instance(T t) {
        return SeamcatFactory.antennaGain().getByClass(getPluginClass()).setModel(t);
    }

    private double peakGain;

    AntennaGainConfiguration(Class<? extends AntennaGainPlugin<T>> clazz, T model) {
        super(PluginJarFiles.findLocation(clazz), ProxyHelper.classInstance(clazz), model);
        if ( !peakGains.containsKey( getModelClass() ) ) {
            peakGain = 0.0;
        } else {
            peakGain = peakGains.get( getModelClass() );
        }
    }

    @Override
    public AntennaGainConfiguration<T> deepClone() {
        AntennaGainConfiguration<T> clone = SeamcatFactory.antennaGain().getByClass(getPluginClass(), getModel(), peakGain());
        clone.setDescription( description() );
        clone.setNotes(getNotes());
        return clone;
    }

    @Override
    public Class<? extends Configuration> getTypeClass() {
        return AntennaGain.class;
    }

    @Override
    public double evaluate(LinkResult linkResult, AntennaResult directionResult) {
        try {
            return getPlugin().evaluate(linkResult, directionResult, peakGain(), getModel());
        } catch (RuntimeException e ) {
            throw new SimulationInvalidException("Antenna gain "+getPlugin().description().name()+" failed.", e);
        }
    }


    @Override
    public double peakGain() {
        return peakGain;
    }

    public AntennaGainConfiguration<T> setPeakGain( double peakGain ) {
        this.peakGain = peakGain;
        return this;
    }

    @Override
    public AntennaGainConfiguration<T> setModel(T t) {
        super.setModel(t);
        return this;
    }
}
