package org.seamcat.presentation;

import org.apache.log4j.Logger;
import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.eventbus.UIEventHandler;
import org.seamcat.events.ProgressEvent;
import org.seamcat.presentation.layout.VerticalSubPanelLayoutManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class ProgressDialog extends JDialog {

    private final static Logger logger = Logger.getLogger(ProgressDialog.class);

    private static final int MIN_WIDTH = 300;

    private JLabel explanationLabel;
    private JLabel progressMessageLabel;

    public ProgressDialog( Frame owner ) {
        this( owner, null );
    }

    public ProgressDialog(Frame owner, Object cancelCommand ) {
        super(owner);
        setUndecorated(true);
        setModal(true);
        getContentPane().add(initializeMainPanel(cancelCommand));
        autoSetSize();
        setLocationRelativeTo(owner);
        setResizable(false);
        EventBusFactory.getEventBus().subscribe(this);
    }

    private void autoSetSize() {
        pack();
        setSize(Math.max(getWidth(), MIN_WIDTH), getHeight());
    }

    public void showModally() {
        if (logger.isDebugEnabled()) {
            logger.debug("Showing progress dialog: " + getTitle());
        }
        setVisible(true);
    }

    private JPanel initializeMainPanel( final Object cancelCommand ) {
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(new EmptyBorder(10, 10, 5, 10));
        mainPanel.setLayout(new VerticalSubPanelLayoutManager(10));
        explanationLabel = new JLabel();
        mainPanel.add(explanationLabel);
        progressMessageLabel = new JLabel("Foo");
        mainPanel.add(progressMessageLabel);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        mainPanel.add(progressBar);

        if ( cancelCommand != null ) {
            JButton cancel = new JButton("Cancel");
            cancel.addActionListener( new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    EventBusFactory.getEventBus().publish( cancelCommand );
                }
            });
            mainPanel.add(cancel);
        }
        return mainPanel;
    }

    public String getExplanation() {
        return explanationLabel.getText();
    }

    public void setExplanation(String explanation) {
        explanationLabel.setText(explanation);
        autoSetSize();
    }

    public void setProgressMessage(String message) {
        progressMessageLabel.setText(message);
    }

    public void close() {
        if (logger.isDebugEnabled()) {
            logger.debug("Closing progress dialog: " + getTitle());
        }
        setVisible(false);
    }

    @UIEventHandler
    public void handleProgressEvent(ProgressEvent e) {
        setProgressMessage(e.getMessage());
    }
}
