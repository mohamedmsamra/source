package org.seamcat.model.factory;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.seamcat.function.DiscreteFunction;
import org.seamcat.model.functions.Function;
import org.seamcat.model.plugin.Config;
import org.seamcat.model.plugin.builder.Builder;

import static org.seamcat.model.factory.Factory.when;

public class TestBuilder {

    private interface TST {
        @Config(order = 1)
        double temperature();

        @Config(order = 2)
        Function mask();

        @Config(order = 3)
        Integer size();
    }

    @Before
    public void setup() {
        TestFactory.initialize();
    }

    @Test
    public void testBuild() {
        TST prototype = Factory.prototype(TST.class);

        when(prototype.temperature()).thenReturn( 4.4 );
        when(prototype.size()).thenReturn( 3 );
        when(prototype.mask()).thenReturn(new DiscreteFunction(6));

        TST instance = Factory.build(prototype);

        Assert.assertEquals( 4.4, instance.temperature() );
        Assert.assertEquals( (Integer)3, instance.size() );

        Function mask = instance.mask();
        Assert.assertTrue( mask.isConstant() );
        Assert.assertEquals(6.0, mask.getConstant());
    }

    @Test
    public void testDefaults() {
        TST instance = Factory.defaultInstance(TST.class);

        Assert.assertEquals(0.0, instance.temperature() );

    }

    @Test
    public void testDeprecatedBuild() {
        Builder<TST> builder = Factory.createBuilder(TST.class);

        builder.returnValue(4.4).temperature();
        builder.returnValue(3).size();
        builder.returnValue(new DiscreteFunction(6)).mask();

        TST instance = builder.build();

        Assert.assertEquals( 4.4, instance.temperature() );
        Assert.assertEquals( (Integer)3, instance.size() );

        Function mask = instance.mask();
        Assert.assertTrue( mask.isConstant() );
        Assert.assertEquals( 6.0, mask.getConstant() );
    }

}
