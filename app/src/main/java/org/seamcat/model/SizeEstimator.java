package org.seamcat.model;

import org.apache.log4j.Logger;
import org.seamcat.model.simulation.SimulationResultGroup;
import org.seamcat.model.types.result.*;

import java.util.List;


public class SizeEstimator {

    private static final long SAVE_MEMORY_USAGE_PER_EVENT = 0; // Conservative - real value is probably close to 366
    private static final long SAVE_MEMORY_USAGE_FIXED_OVERHEAD = 32 * 1024 * 1024; // Conservative
    private static final long LOAD_MEMORY_USAGE_PER_EVENT = 0; // A wild guess. Memory profiler shows that load mem usage is higher than for save. The exact number has not been analyzed (yet)
    private static final long LOAD_MEMORY_USAGE_FIXED_OVERHEAD = 32 * 1024 * 1024; // Conservative
    private static final long FILE_SIZE_PER_DOUBLE = 9;
    private static final long FILE_SIZE_FIXED_OVERHEAD = 5000;

    private final static Logger logger = Logger.getLogger(SizeEstimator.class);

    /** The estimated workspace file size in bytes stemming from event storage
     */
    public static long eventFileSizeEstimate( Workspace workspace ) {
        return FILE_SIZE_PER_DOUBLE*SizeEstimator.countOfDoublesToBeSaved(workspace) + FILE_SIZE_FIXED_OVERHEAD;
    }

    public static long countOfDoublesToBeSaved(Workspace workspace) {
        if ( workspace.getSimulationResults() == null ) return 0;
        return countDoubles( workspace.getSimulationResults().getSeamcatResults() ) + countDoubles(workspace.getSimulationResults().getEventProcessingResults());
    }

    private static int countDoubles(List<SimulationResultGroup> groups ) {
        int doubles = 0;
        for (SimulationResultGroup group : groups) {
            for (BarChartResultType bar : group.getResultTypes().getBarChartResultTypes()) {
                doubles += bar.getChartPoints().size() * 2;
            }
            for (VectorGroupResultType vg : group.getResultTypes().getVectorGroupResultTypes()) {
                for (NamedVectorResult v : vg.getVectorGroup()) {
                    doubles += v.getVector().size();
                }
            }
            for (VectorResultType vector : group.getResultTypes().getVectorResultTypes()) {
                doubles += vector.getValue().size();
            }
            for (ScatterDiagramResultType scatter : group.getResultTypes().getScatterDiagramResultTypes()) {
                doubles += scatter.getScatterPoints().size()*2;
            }
            // don't know how to estimate custom results
        }
        return doubles;
    }

    public static boolean resultsTooLargeForSaveOrLoad(Workspace workspace) {
        long doubles = countOfDoublesToBeSaved(workspace);
        long intermediateMemoryRequiredForSave = doubles * SAVE_MEMORY_USAGE_PER_EVENT + SAVE_MEMORY_USAGE_FIXED_OVERHEAD;
        long intermediateMemoryRequiredForLoad = doubles * LOAD_MEMORY_USAGE_PER_EVENT + LOAD_MEMORY_USAGE_FIXED_OVERHEAD;

        long maxMemory = Runtime.getRuntime().maxMemory();
        long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long availableMemory = maxMemory - usedMemory;

        if (Math.max(intermediateMemoryRequiredForSave, intermediateMemoryRequiredForLoad) > availableMemory) {
            logger.info("Reported memory too low: Collecting garbage and trying again");
            Runtime.getRuntime().gc();
            usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            availableMemory = maxMemory - usedMemory;
        }

        logger.info("Memory reported by runtime - max: " + maxMemory + ", avail: " + availableMemory);
        logger.info("Memory required for save: " + intermediateMemoryRequiredForSave + " (values: " + doubles+ ")");
        logger.info("Memory required for subsequent load: " + intermediateMemoryRequiredForLoad);

        return Math.max(intermediateMemoryRequiredForSave, intermediateMemoryRequiredForLoad) > availableMemory;
    }
}
