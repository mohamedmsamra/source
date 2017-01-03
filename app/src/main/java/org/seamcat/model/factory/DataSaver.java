package org.seamcat.model.factory;

import org.seamcat.presentation.DialogHelper;
import org.seamcat.tabulardataio.TabularDataFactory;
import org.seamcat.tabulardataio.TabularDataSaver;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Since plugins are not allowed to write to the file system,
 * all writes must be handled by the SEAMCAT data saver.
 *
 * Only new files can be saved to - otherwise plugins could overwrite
 * existing files.
 */
public class DataSaver implements Runnable {

    private static final List<DataFileImpl> currentSaves = new LinkedList<DataFileImpl>();

    public static void add( DataFileImpl dataFile ) {
        synchronized ( currentSaves ) {
            currentSaves.add( dataFile );
        }
    }

    private static boolean running = true;
    private static boolean stopped = false;

    @Override
    public void run() {
        // while SEAMCAT runs this is processing
        Map<DataFileImpl, TabularDataSaver> savers = new HashMap<DataFileImpl, TabularDataSaver>();
        while (running) {
            try {
                synchronized (currentSaves) {
                    List<DataFileImpl> removed = new ArrayList<DataFileImpl>();
                    for (DataFileImpl currentSave : currentSaves) {
                        if ( !currentSave.isWritingStarted() )  {
                            File file = currentSave.getFile();
                            if ( file.exists() ) {
                                DialogHelper.cannotOverwriteError(file.getName());
                                removed.add(currentSave);
                                continue;
                            } else {
                                try {
                                    savers.put(currentSave, TabularDataFactory.newSaverForFile(file));
                                } catch (RuntimeException e) {
                                    DialogHelper.fileExtensionNotRecognized(file.getName());
                                    removed.add(currentSave);
                                    continue;
                                }
                            }
                        }

                        TabularDataSaver saver = savers.get(currentSave);
                        ConcurrentLinkedQueue queue = currentSave.getQueue();
                        while (!queue.isEmpty()) {
                            Object value = queue.poll();
                            if ( value instanceof Boolean ) {
                                // end reached
                                saver.close();
                                savers.remove(currentSave);
                                removed.add( currentSave );
                            } else {
                                saver.addRow( (Object[]) value);
                            }
                        }
                    }
                    for (DataFileImpl dataFile : removed) {
                        currentSaves.remove( dataFile );
                    }

                }
                try {
                    Thread.sleep( 1000 );
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (RuntimeException e ) {
                e.printStackTrace();
            }


        }

        stopped = true;
    }

    public static void stop() {
        running = false;

        while (!stopped) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                //
            }
        }
    }

}
