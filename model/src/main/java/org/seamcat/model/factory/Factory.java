package org.seamcat.model.factory;

import org.seamcat.model.distributions.DistributionFactory;
import org.seamcat.model.functions.DataExporter;
import org.seamcat.model.functions.FunctionFactory;
import org.seamcat.model.plugin.antenna.AntennaGainFactory;
import org.seamcat.model.plugin.builder.Builder;
import org.seamcat.model.plugin.builder.Returner;
import org.seamcat.model.plugin.propagation.PropagationModelFactory;

/**
 * This class can be used by all plugins to get factories and builders for the different types of SEAMCAT
 */
public class Factory {

    private static DistributionFactory distributionFactory;
    private static PropagationModelFactory propagationModelFactory;
    private static Builders builders;
    private static AntennaGainFactory antennaGainFactory;
    private static FunctionFactory functionFactory;
    private static DataExporter dataExporter;

    static void initialize(DistributionFactory distributionFactory, PropagationModelFactory propagationModelFactory,
                                  Builders builders, AntennaGainFactory antennaGainFactory, FunctionFactory functionFactory,
                                  DataExporter dataExporter) {
        Factory.distributionFactory = distributionFactory;
        Factory.propagationModelFactory = propagationModelFactory;
        Factory.builders = builders;
        Factory.antennaGainFactory = antennaGainFactory;
        Factory.functionFactory = functionFactory;
        Factory.dataExporter = dataExporter;
    }


    /**
     * Get the factory handling the creation of distributions
     * @return Distribution Factory instance
     */
    public static DistributionFactory distributionFactory() {
        return distributionFactory;
    }

    /**
     * Get the factory handling the creation or copy of
     * propagation models
     * @return
     */
    public static PropagationModelFactory propagationModelFactory() {
        return propagationModelFactory;
    }

    /**
     * @deprecated use prototype(), when(), and build() instead
     */
    @Deprecated
    public static <T> Builder<T> createBuilder(Class<T> clazz){
        return builders.createBuilder(clazz);
    }

    /**
     * @deprecated use prototype(), when(), and build() instead
     */
    @Deprecated
    public static <T> Builder<T> createBuilder(Class<T> clazz, T t){
        return builders.createBuilder(clazz, t);
    }

    /**
     * This method returns the prototype object to be build. The prototype is used in the <code>when</code> method
     * to bind return value for methods, e.g. <code>when( prototype.temperature()).thenReturn( 37.5);</code>
     * @return prototype instance of type T
     */
    public static <T> T prototype(Class<T> clazz ) {
        return builders.prototype(clazz);
    }

    /**
     * This method returns the prototype object to be build with values according to the argument t. The prototype is used in the <code>when</code> method
     * to bind return value for methods, e.g. <code>when( prototype.temperature()).thenReturn( 37.5);</code>
     * @return prototype instance of type T
     */
    public static <T> T prototype(Class<T> clazz, T t ) {
        return builders.prototype(clazz,t);
    }

    /**
     * Used in conjunction with <code>prototype()</code>. <br>
     *     Specifies what an instance should return by using the prototype.
     *
     * @param value method invoked on the prototype. Value only used to generate <code>Returner</code> accepting
     *              the correct return value
     * @return Returner object to be passed the actual value to return
     */
    public static <V> Returner<V> when( V value ) {
        return builders.when(value);
    }

    /**
     * method that returns an immutable instance of <code>T</code>.
     * @param prototype a prototype instance with recorded return values to be
     *                  set in the final immutable instance
     * @return immutable instance of T
     */
    public static <T> T build(T prototype) {
        return builders.build(prototype);
    }

    public static <T> T defaultInstance(Class<T> clazz ) {
        return build(prototype(clazz));
    }

    public static <T> T fromInstance(Class<T> clazz, T t ) {
        return build(prototype(clazz, t));
    }

    public static AntennaGainFactory antennaGainFactory() {
        return antennaGainFactory;
    }

    public static FunctionFactory functionFactory() {
        return functionFactory;
    }

    /**
     * @deprecated Data exporter only to be used in CustomUI. Use @Service annotation on field of a custom
     * UI to get an instance injected.
     */
    @Deprecated
    public static DataExporter dataExporter() {
        return dataExporter;
    }

    protected interface Builders {
        <T> T prototype(Class<T> clazz);

        <T> T prototype(Class<T> clazz, T t);

        <V> Returner<V> when( V value );

        <T> T build( T prototype );

        <T> Builder<T> createBuilder(Class<T> clazz);
        <T> Builder<T> createBuilder(Class<T> clazz, T t);
    }

}
