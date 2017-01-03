package org.seamcat.model.plugin.propagation;

import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.functions.BlockingMask;
import org.seamcat.model.functions.EmissionMask;
import org.seamcat.model.functions.Function;
import org.seamcat.model.plugin.*;
import org.seamcat.model.types.AntennaGain;
import org.seamcat.model.types.PropagationModel;

public interface InputTestInput {

    @Config(order = 1, name = "Vertical")
    @Vertical
    OptionalFunction vertical();

    @Config(order = 2, name = "Horizontal")
    @Horizontal
    Function horizontal();

    @Config(order = 3, name = "Spherical")
    @Spherical
    OptionalFunction spherical();


    @Config(order = 4, name = "Integer")
    int intValue();

    @Config(order = 5, name = "Distribution")
    Distribution distribution();

    @Config(order = 6, name = "Object Integer")
    Integer IntValue();

    @Config(order = 7, name ="Opt double")
    OptionalDoubleValue maybe();

    @Config(order = 8, name = "A value", unit = "dB")
    double value();

    @Config(order = 9, name ="true/false")
    boolean bool();

    @Config(order = 10, name ="True/False")
    Boolean BOOL();

    @Config(order = 11, name = "Propagation")
    PropagationModel pm();

    @Config(order = 12, name = "Antenna")
    AntennaGain gain();

    @Config(order = 13, name = "Emission")
    EmissionMask emission();

    @Config(order = 14, name = "Blocking mask")
    BlockingMask blockingMask();

    @Config(order = 15, name = "String")
    String string();

    @Config(order = 16, name = "Gender", values = "Male,Female")
    String gender();

    @Config(order = 17, name = "Function")
    Function function();

    @Config(order = 18, name = "Opt Function")
    OptionalFunction optFunction();
}
