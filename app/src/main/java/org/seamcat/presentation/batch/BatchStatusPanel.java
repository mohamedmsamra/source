package org.seamcat.presentation.batch;

import javax.swing.*;
import java.awt.*;

public class BatchStatusPanel extends JPanel {

    private JLabel status = new JLabel();
    private JProgressBar batchStatus;

    public BatchStatusPanel() {
        super(new BorderLayout());
        batchStatus = new JProgressBar(0, 10);
        add( status, BorderLayout.NORTH );
        add( batchStatus, BorderLayout.SOUTH );
    }

    public void initialize( final int steps ) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                batchStatus.setMaximum( steps );
                batchStatus.setValue(0);
            }
        });
    }

    public void updateStatus( final String text ) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                status.setText( text );
                batchStatus.setValue(batchStatus.getValue() + 1);
            }
        });
    }

    public void complete() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                status.setText( "Batch simulation completed");
                batchStatus.setValue( batchStatus.getMaximum() );
            }
        });
    }

}
