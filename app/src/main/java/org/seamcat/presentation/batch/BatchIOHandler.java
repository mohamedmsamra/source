package org.seamcat.presentation.batch;

import org.apache.log4j.Logger;
import org.seamcat.batch.BatchJobList;
import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.events.FileOpenedEvent;
import org.seamcat.events.InfoMessageEvent;
import org.seamcat.events.WorkspaceSavedEvent;
import org.seamcat.loadsave.BatchLoader;
import org.seamcat.loadsave.BatchSaver;
import org.seamcat.migration.batch.BatchMigrator;
import org.seamcat.model.MigrationIssue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class BatchIOHandler {
	private static final Logger LOG = Logger.getLogger(BatchIOHandler.class);

    public static void save(BatchJobList batchJobList ) {
        try {
            BatchSaver saver = new BatchSaver(batchJobList);
            saver.saveToFile( new File(batchJobList.getAbsoluteLocation()) );
            batchJobList.setAbsoluteLocation(batchJobList.getAbsoluteLocation());
            EventBusFactory.getEventBus().publish( new InfoMessageEvent("Saved batch to '"+batchJobList.getAbsoluteLocation()+"'"));
            EventBusFactory.getEventBus().publish(new WorkspaceSavedEvent(batchJobList.getAbsoluteLocation()));
        } catch (Exception ex) {
            LOG.error("An Error occured", ex);
        }
    }

	public static BatchJobList load(File file) {
		BatchJobList bj = null;
		try {
            File migrate = new BatchMigrator().migrate(file, new ArrayList<MigrationIssue>());
            BatchLoader loader = new BatchLoader();
            bj = loader.loadFromFile(migrate);
            if (loader.isCancelled()) return null;
            if (bj != null) {
                bj.setAbsoluteLocation(file.getAbsolutePath());
                EventBusFactory.getEventBus().publish(new FileOpenedEvent(file.getAbsolutePath()));
            }
        } catch (IOException e) {
            LOG.error("Error loading batch", e);
        }

		return bj;
	}

}
