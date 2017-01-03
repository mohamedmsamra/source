package org.seamcat.model.systems;

import org.junit.Test;
import org.seamcat.cdma.CDMADownlinkSystem;
import org.seamcat.model.core.SystemSimulationModel;
import org.seamcat.model.factory.RandomAccessor;
import org.seamcat.model.factory.TestFactory;
import org.seamcat.model.generic.ProxyHelper;
import org.seamcat.model.systems.cdma.SystemModelCDMADownLink;
import org.seamcat.simulation.cdma.CDMADownLinkSystemPlugin;
import org.seamcat.simulation.generic.GenericSystemPlugin;
import org.seamcat.simulation.result.MutableEventResult;
import org.seamcat.simulation.result.PreSimulationResultsImpl;

public class CorrelationModesTest {


    @Test
    public void allModes() {
        GenericSystemPlugin generic = new GenericSystemPlugin();
        CDMADownLinkSystemPlugin cellular = new CDMADownLinkSystemPlugin();

        CorrelationModeResolver resolver = new CorrelationModeResolver();
        resolver.resolveCorrelationModes(generic, generic);
        System.out.println("---------------");
        resolver.resolveCorrelationModes(cellular, cellular);
        System.out.println("---------------");
        resolver.resolveCorrelationModes(generic, cellular);
        System.out.println("---------------");
        resolver.resolveCorrelationModes(cellular, generic);

    }

    @Test
    public void testCapacityFinding() throws InterruptedException {
        long start = System.currentTimeMillis();

        TestFactory.initialize();
        RandomAccessor.fixSeed(10);

        CDMADownLinkSystemPlugin plugin = new CDMADownLinkSystemPlugin();

        SystemModelCDMADownLink sys = ProxyHelper.newComposite(SystemModelCDMADownLink.class);

        plugin.convert(sys);

        //plugin.simulationInstance().preSimulate();

        System.out.println("time: "+(System.currentTimeMillis() - start));
    }


    @Test
    public void testCapacityFindingOld() throws InterruptedException {
        long start = System.currentTimeMillis();
        TestFactory.initialize();

        RandomAccessor.fixSeed(10);
        SystemModelCDMADownLink sys = ProxyHelper.newComposite(SystemModelCDMADownLink.class);

        PreSimulationResultsImpl pre = new PreSimulationResultsImpl();
        SystemSimulationModel model = UIToModelConverter.convert(sys);
        CDMADownlinkSystem system = (CDMADownlinkSystem) model.getDMASystem();
        system.setResults( pre );
        system.initialize(new MutableEventResult(-1));
        system.performPreSimulationTasks(900);
        //system.findNonInterferedCapacity( pre, this );

        pre.getPreSimulationResults();

        System.out.println("time: "+(System.currentTimeMillis() - start));
    }


}
