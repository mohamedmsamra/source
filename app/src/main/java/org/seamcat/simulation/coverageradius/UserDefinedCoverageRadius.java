package org.seamcat.simulation.coverageradius;

import org.seamcat.model.Scenario;
import org.seamcat.model.generic.GenericSystem;
import org.seamcat.model.plugin.Config;
import org.seamcat.model.plugin.Validator;
import org.seamcat.model.plugin.coverageradius.CoverageRadiusPlugin;
import org.seamcat.model.types.Description;
import org.seamcat.model.types.result.DescriptionImpl;

import java.util.List;

public class UserDefinedCoverageRadius implements CoverageRadiusPlugin<UserDefinedCoverageRadius.Input> {

    @Override
    public void consistencyCheck(Scenario scenario, List<Object> path, Input input, Validator<Input> validator) {
    }

    @Override
    public double evaluate(GenericSystem genericSystem, Input input ) {
        return input.coverageRadius();
    }

    @Override
    public Description description() {
        return new DescriptionImpl("User-defined radius","User-defined radius");
    }

    public interface Input {
        @Config(order = 1, name = "Coverage Radius", unit = "km")
        double coverageRadius();

        double coverageRadius = 0.1;
    }
}
