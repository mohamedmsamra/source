package org.seamcat.presentation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class SimulationLogDialog extends EscapeDialog {

    public SimulationLogDialog( File logfile) {
        super(MainWindow.getInstance(), true);
        JPanel panel = new JPanel(new BorderLayout());

        JTextArea area = new JTextArea(30, 80);
        JScrollPane pane = new JScrollPane( area );
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader( new FileInputStream(logfile) ));
            String line;
            area.append( logfile.getAbsolutePath()+":\n");
            while ( null != (line = reader.readLine())) {
                area.append( line + "\n");
            }
        } catch ( Exception e ) {
            area.append( "Error reading file" + e );
        }
        area.setCaretPosition(0);
        area.setEditable(false);
        panel.add( pane, BorderLayout.CENTER );

        setTitle("Output from debug run");
        getContentPane().setLayout( new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);
        JButton close = new JButton("Close");
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                SimulationLogDialog.this.setVisible(false);
            }
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.add( close );
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
    }
}
