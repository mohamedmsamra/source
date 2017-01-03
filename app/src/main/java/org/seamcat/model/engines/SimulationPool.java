package org.seamcat.model.engines;

import org.apache.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimulationPool {

    private static final Logger LOG = Logger.getLogger(SimulationPool.class);
    private final ExecutorService pool;
    private final int poolSize;

    public SimulationPool( int poolSize ) {
        this.poolSize = poolSize;
        LOG.info("Initializing simulation engine with " + poolSize + " threads");
        pool = Executors.newFixedThreadPool( poolSize );
    }

    public SimulationPool() {
        this( Runtime.getRuntime().availableProcessors() );
    }

    public int getPoolSize() {
        return poolSize;
    }

    public ExecutorService getPool() {
        return pool;
    }

    public void destroy() {
        pool.shutdown();
    }

}
