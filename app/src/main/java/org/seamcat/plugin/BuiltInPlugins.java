package org.seamcat.plugin;

import org.seamcat.eventprocessing.*;
import org.seamcat.model.antenna.*;
import org.seamcat.model.propagation.*;
import org.seamcat.model.propagation.p528.P528PropagationModel;
import org.seamcat.simulation.coverageradius.NoiseLimitedCoverageRadius;
import org.seamcat.simulation.coverageradius.TrafficLimitedNetworkCoverageRadius;
import org.seamcat.simulation.coverageradius.UserDefinedCoverageRadius;
import org.seamcat.simulation.generic.CognitiveRadio;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Method that captures all the plugins that are added (i.e. built-in) into SEAMCAT
 */
public class BuiltInPlugins extends JarConfigurationModel {

    private static Map<String, Class> builtInPluginClasses = new LinkedHashMap<String, Class>();

    private static void put( Class clazz ) {
        builtInPluginClasses.put( clazz.getName(), clazz );
    }
    static {
        put(DemoEPP_1_collectIntermediaryResults.class);
        put(DemoEPP_2_developNewAlgorithm.class);
        put(DemoEPP_3_developNewAlgorithm_checkTxPower.class);
        put(DemoEPP_4_generate_CoverI_results.class);
        put(DemoEPP_5_Tx_Power.class);
        put(DemoEPP_6_CellularInternals.class);
        put(DemoEPP_7_Frequency.class);
        put(DemoEPP_8_Distance.class);
        put(DemoEPP_9_Intermod.class);
        put(DemoEPP_10_OFDMA_Internals.class);
        put(HataSE21PropagationModel.class);
        put(HataSE24PropagationModel.class);
        put(SDPropagationModel.class);
        put(P452ver14PropagationModel.class);
        put(FreeSpacePropagationModel.class);
        put(P1546ver4PropagationModel.class);
        put(P1546ver1PropagationModel.class);
        put(P1411LowAntennaHeight.class);
        put(P528PropagationModel.class);
        put(LongleyRice_mod.class);
        put(Model_C_IEEE_802_11_rev3.class);
        put(JTG56PropagationModel.class);
        put(UserDefinedCoverageRadius.class);
        put(NoiseLimitedCoverageRadius.class);
        put(TrafficLimitedNetworkCoverageRadius.class);
        put(PeakGainAntenna.class);
        put(HorizontalVerticalAntenna.class);
        put(SphericalAntenna.class);
        put(CognitiveRadio.class);
    }

    public static Map<String,Class> getBuiltInPluginClasses() {
        return builtInPluginClasses;
    }

    public BuiltInPlugins() {
        setHash(PluginLocation.BUILTIN);
    }

    @Override
    public PluginClass getPluginClass(String classname) {
        Class aClass = builtInPluginClasses.get(classname);
        return new PluginClass(this, aClass);
    }

    @Override
    public Class<?> loadClass(String classname) {
        try {
            return Class.forName( classname );
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not load class: " + classname, e);
        }
    }

    @Override
    public String toString() {
        return PluginLocation.BUILTIN;
    }
}
