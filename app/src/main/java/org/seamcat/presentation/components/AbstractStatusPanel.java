package org.seamcat.presentation.components;

import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.events.MemoryStatusUpdatedEvent;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractStatusPanel extends JPanel {

    private static final int MEMORY_FACTOR = 1024;

    static {
        new Thread("static memory monitor") {
            @Override
            public void run() {
                while (true) {
                    try {
                        int memoryMax = (int) (Runtime.getRuntime().totalMemory() / MEMORY_FACTOR);
                        int memoryUsageValue = memoryMax
                                - (int) (Runtime.getRuntime().freeMemory() / MEMORY_FACTOR);
                        String memoryStatusLabel = "Use of allocated memory (max: "
                                + (int) (Runtime.getRuntime().maxMemory() / MEMORY_FACTOR)
                                + " kb / allocated: "
                                + (int) (Runtime.getRuntime().totalMemory() / MEMORY_FACTOR)
                                + " kb ("
                                + (int) (Runtime.getRuntime().totalMemory() / MEMORY_FACTOR)
                                / ((int) (Runtime.getRuntime().maxMemory() / MEMORY_FACTOR) / 100)
                                + " %)):";
                        EventBusFactory.getEventBus().publish(new MemoryStatusUpdatedEvent(memoryMax, memoryStatusLabel, memoryUsageValue));
                        Thread.sleep(1000);
                    } catch (Exception e) {
                    }
                }
            }
        }.start();
    }

    protected JProgressBar currentProcess = new JProgressBar(0, 100);

    protected JLabel currentProcessLabel = new JLabel("Current process:");

    protected JCheckBox debug = new JCheckBox("Debug");
    protected LayoutManager layout = new GridBagLayout();

    protected JProgressBar memoryUsage = new JProgressBar(0, 100);
    protected JLabel memoryUsageLabel = new JLabel("Memory usage:");

    protected AbstractStatusPanel() {
        this.setLayout(layout);

        currentProcess.setStringPainted(true);
        memoryUsage.setStringPainted(true);

        memoryUsageLabel.setText("Use of allocated memory (max: "
                + (int) (Runtime.getRuntime().maxMemory() / MEMORY_FACTOR)
                + " kb / allocated: "
                + (int) (Runtime.getRuntime().totalMemory() / MEMORY_FACTOR)
                + " kb):");

        GridBagConstraints con = new GridBagConstraints();
        con.gridheight = 1;
        con.fill = GridBagConstraints.BOTH;

        con.anchor = GridBagConstraints.WEST;
        con.gridx = 0;
        con.weightx = 1;
        con.gridy++;
        con.gridwidth = 3;
        this.add(currentProcessLabel, con);
        con.gridx = 0;
        con.weightx = 1;
        con.gridy++;
        con.gridwidth = 3;
        this.add(currentProcess, con);

        con.gridx = 0;
        con.weightx = 1;
        con.gridy++;
        con.gridwidth = 3;
        this.add(memoryUsageLabel, con);
        con.gridy++;
        con.gridwidth = 3;
        this.add(memoryUsage, con);

        con.gridy++;
        con.gridx = 0;
        con.weightx = 0.5;
        con.gridwidth = 1;
        this.add(Box.createHorizontalGlue(), con);
        con.gridx++;
        con.weightx = 1;
        this.add(Box.createHorizontalGlue(), con);
        con.gridx++;
        this.add(Box.createHorizontalGlue(), con);

        EventBusFactory.getEventBus().subscribe(this);
    }

    protected void initialize(int maxCount ) {
        currentProcess.setMaximum(maxCount);
        currentProcess.setValue(0);
    }

    protected void increment() {
        currentProcess.setValue(currentProcess.getValue() + 1);
    }

    protected void finished() {
        currentProcess.setValue(currentProcess.getMaximum());
    }
}
