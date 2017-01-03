package org.seamcat.presentation.replay;

import org.seamcat.presentation.SeamcatIcons;
import org.seamcat.presentation.components.BorderPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;

public class SingleEventPanel extends JPanel {

    private final static int maxLines = 1000;

    public SingleEventPanel(SingleEventSimulationResult result, final JTabbedPane pane) {
        super(new BorderLayout());
        JPanel close = new JPanel(new FlowLayout(FlowLayout.LEFT));
        close.add(new JLabel("Close replay tab"));
        JButton bClose = new JButton(SeamcatIcons.getImageIcon("SEAMCAT_ICON_WORKSPACE_CLOSE"));
        bClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                pane.remove( pane.getSelectedComponent() );
            }
        });
        close.add(bClose, BorderLayout.NORTH);
        add(close, BorderLayout.NORTH);
        EventResultPanel panel = new EventResultPanel(result.getEventResult(), result.getScenario());
        JSplitPane jPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        jPanel.setDividerLocation(700);
        jPanel.add(new BorderPanel(new JScrollPane(panel), "Event Result"));

        JTextArea textArea = new JTextArea( 20, 30);
        JPanel debugTracePanel = new JPanel(new BorderLayout());
        debugTracePanel.add( new JLabel("<html>Showing log trace of file<br><b>"+result.getLogFile().getAbsolutePath()+"</b></html>"), BorderLayout.NORTH );
        debugTracePanel.add( new JScrollPane(textArea), BorderLayout.CENTER );
        jPanel.add(new BorderPanel(debugTracePanel, "Log Trace"));
        add(jPanel, BorderLayout.CENTER );

        try {
            BufferedReader reader = new BufferedReader(new FileReader(result.getLogFile()));
            String line;
            int i =0;
            while (null != (line = reader.readLine()) && i < maxLines) {
                textArea.append( line + "\n");
                i++;
            }
            if ( line != null ) {
                textArea.append("...\n");
                textArea.append("See log file for full trace");
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

