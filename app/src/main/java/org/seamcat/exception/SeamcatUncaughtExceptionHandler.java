package org.seamcat.exception;

import java.awt.EventQueue;
import java.lang.Thread.UncaughtExceptionHandler;

import org.apache.log4j.Logger;
import org.seamcat.presentation.DialogHelper;

public class SeamcatUncaughtExceptionHandler implements UncaughtExceptionHandler {

    private static final Logger logger = Logger.getLogger(SeamcatUncaughtExceptionHandler.class); 

    @Override
    public void uncaughtException(Thread thread, final Throwable exception) {
        try {
            logger.error("Uncaught exception in thread " + thread.getName(), exception);
            //showErrorDialogLater(exception);
        }
        catch (Throwable t) {
            System.err.println("**** Exception while handling uncaught exception in thread ****");
        }
    }

    private void showErrorDialogLater(final Throwable exception) {
        // This code may run on any thread, so we need to invoke the error dialog via EventQueue.invokeLater().
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    DialogHelper.generalSeamcatError(exception);                                    
                }
                catch (Throwable t) {
                    System.err.println("**** Exception while trying to show error dialog, while handling uncaught exception in thread ****");                        
                }
            }                
        });
    }
}
