package org.seamcat.model.plugin.antenna;

import org.seamcat.model.factory.Factory;
import org.seamcat.model.plugin.VoidInput;
import org.seamcat.model.types.AntennaGain;

/**
 * <p>
 *     Factory for instantiating antenna gain plugins.
 * </p>
 * <p>
 *     Get an instance of this factory by calling <code>Factory.antennaGainFactory()</code><br>
 *     @see Factory Factory
 * </p>
 */
public interface AntennaGainFactory {

    /**
     * Peak gain antenna
     *
     * Regardless of the direction this always returns the peak gain value specified
     *
     */
    AntennaGain<VoidInput> getPeakGainAntenna();

    /**
     * Peak gain antenna
     *
     * Regardless of the direction this always returns the peak gain value specified
     *
     */
    AntennaGain<VoidInput> getPeakGainAntenna(double peakGain);

    /**
     * HorizontalVerticalAntenna -using default configuration, i.e. only peak gain
     * <ol>
     *    <li>horizontal pattern only selected: direct evaluation of the azimuth gain</li>
     *    <li>vertical pattern only selected: direct evaluation of the elevation angle</li>
     *    <li>if both horizontal and vertical pattern selected, then compute the combined gain</li>
     * </ol>
     *<code>
     * if (Math.abs(horiGain - vertiGain) < 3){<br>
     *     &nbsp gain = G_max * sqrt(((G_horiz^2) + (G_vert^2))/2)<br>
     * }else{<br>
     *     &nbsp gain = G_max * min(G_horiz,G_vert)<br>
     * }<br>
     *</code>
     */
    AntennaGain<HorizontalVerticalInput> getHorizontalVerticalAntenna();

    /**
     * HorizontalVerticalAntenna
     * <ol>
     *    <li>horizontal pattern only selected: direct evaluation of the azimuth gain</li>
     *    <li>vertical pattern only selected: direct evaluation of the elevation angle</li>
     *    <li>if both horizontal and vertical pattern selected, then compute the combined gain</li>
     * </ol>
     *<code>
     * if (Math.abs(horiGain - vertiGain) < 3){<br>
     *     &nbsp gain = G_max * sqrt(((G_horiz^2) + (G_vert^2))/2)<br>
     * }else{<br>
     *     &nbsp gain = G_max * min(G_horiz,G_vert)<br>
     * }<br>
     *</code>
     */
    AntennaGain<HorizontalVerticalInput> getHorizontalVerticalAntenna(HorizontalVerticalInput input, double peakGain);

    /**
     * Spherical Antenna - using default values
     *
     * The antenna gain is calculated using a spherical antenna pattern
     *
     */
    AntennaGain<SphericalInput> getSphericalAntenna();

    /**
     * Spherical Antenna
     *
     * The antenna gain is calculated using a spherical antenna pattern
     *
     */
    AntennaGain<SphericalInput> getSphericalAntenna(SphericalInput input, double peakGain);

    <T> AntennaGain<T> getByClass(Class<? extends AntennaGainPlugin<T>> clazz);
    <T> AntennaGain<T> getByClass(Class<? extends AntennaGainPlugin<T>> clazz, T input, double peakGain);
}
