package org.seamcat.model.factory;

import org.seamcat.model.functions.DataFile;

import java.io.File;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DataFileImpl implements DataFile {

    private File file;
    private ConcurrentLinkedQueue queue;
    private boolean writingStarted;

    public DataFileImpl(File file) {
        this.file = file;
        queue = new ConcurrentLinkedQueue();
        writingStarted = false;
    }

    @Override
    public void addRow(String... data) {
        queue.add(data);
    }

    @Override
    public void addRow(Number... data) {
        queue.add(data);
    }

    @Override
    public void close() {
        queue.add(false);
        // wait until queue empty
        while (!queue.isEmpty()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }


    public File getFile() {
        return file;
    }

    public ConcurrentLinkedQueue getQueue() {
        return queue;
    }

    public boolean isWritingStarted() {
        return writingStarted;
    }

    public void setWritingStarted() {
        writingStarted = true;
    }
}
