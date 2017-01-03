package org.seamcat.presentation.report;

import org.apache.log4j.Logger;
import org.seamcat.batch.BatchJobList;
import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.events.InfoMessageEvent;
import org.seamcat.model.factory.Model;
import org.seamcat.model.Workspace;
import org.seamcat.presentation.EscapeDialog;
import org.seamcat.presentation.MainWindow;
import org.seamcat.tabulardataio.TabularDataFactory;
import org.seamcat.tabulardataio.TabularDataSaver;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class ReportDialog extends EscapeDialog {

    private static final Logger LOG = Logger.getLogger(ReportDialog.class);
    private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", Locale.ENGLISH);

    private JCheckBox scenario = new JCheckBox(STRINGLIST.getString("REPORT_OPTION_SCENARIOS"), true);
    private JCheckBox preResults    = new JCheckBox(STRINGLIST.getString("REPORT_OPTION_PRERESULTS"), true);
    private JCheckBox results       = new JCheckBox(STRINGLIST.getString("REPORT_OPTION_RESULTS"), true);
    private JCheckBox expandResults = new JCheckBox(STRINGLIST.getString("REPORT_OPTION_EXPAND_TAB"), false);
    private JRadioButton xMLFile = new JRadioButton("XML File", true);
    private JRadioButton hTMLFile = new JRadioButton("HTML File", false);
    private JRadioButton xlsFile = new JRadioButton("XLS File", false);
    private JRadioButton xlsxFile = new JRadioButton("XLSX File", false);

    private Workspace workspace;
    private BatchJobList batchJobList;
    private String name;

    public ReportDialog() {
        super(MainWindow.getInstance(), "SEAMCAT report generator", true);

        ButtonGroup buttonGroupOutputOptions = new ButtonGroup();
        buttonGroupOutputOptions.add(xMLFile);
        buttonGroupOutputOptions.add(hTMLFile);
        buttonGroupOutputOptions.add(xlsFile);
        buttonGroupOutputOptions.add(xlsxFile);

        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.add( scenario );
        optionsPanel.add( new JSeparator());
        optionsPanel.add( preResults );
        optionsPanel.add( results );
        optionsPanel.add( expandResults );
        optionsPanel.setBorder(new TitledBorder(STRINGLIST.getString("REPORT_OPTION_CONTENT")));

        JPanel outputOptionsPanel = new JPanel();
        outputOptionsPanel.setLayout(new BoxLayout(outputOptionsPanel, BoxLayout.Y_AXIS));
        outputOptionsPanel.setFocusable(false);
        outputOptionsPanel.setBorder(new TitledBorder(STRINGLIST.getString("REPORT_OPTION_FORMATS")));
        outputOptionsPanel.add(xMLFile);
        outputOptionsPanel.add(hTMLFile);
        outputOptionsPanel.add(xlsFile);
        outputOptionsPanel.add(xlsxFile);

        JPanel main = new JPanel(new GridLayout());
        main.add( optionsPanel );
        main.add( outputOptionsPanel );
        JPanel container = new JPanel(new BorderLayout());
        container.add( main, BorderLayout.CENTER);

        JPanel control = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton generate = new JButton("Generate");
        control.add(generate);
        JButton close = new JButton("Close");
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ReportDialog.this.setVisible(false);
            }
        });
        control.add(close);
        generate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                File file = null;
                if ( hTMLFile.isSelected() ) {
                    file = new File(Model.getReportDirectory().getAbsolutePath() + File.separator + name + ".html");
                    try {
                        HTMLReportGenerator.generate(new FileWriter(file), convertSource());
                    } catch (IOException e) {
                        LOG.error("Error generating html report", e);
                        EventBusFactory.getEventBus().publish(new InfoMessageEvent("Error generating report. See log for details"));
                    }
                } else {
                    if ( xMLFile.isSelected() ) {
                        file = new File(Model.getReportDirectory().getAbsolutePath() + File.separator + name + ".xml");
                        try {
                            XMLReportGenerator.generate(new FileWriter(file), convertSource());
                        } catch (IOException e) {
                            LOG.error("Error generating xtml report", e);
                            EventBusFactory.getEventBus().publish(new InfoMessageEvent("Error generating report. See log for details"));
                        }
                    } else if ( xlsFile.isSelected()) {
                        file = new File(Model.getReportDirectory().getAbsolutePath() + File.separator + name + ".xls");
                        TabularDataSaver saver = TabularDataFactory.newSaverForFile(file);
                        ExcelReportGenerator.generate( saver, convertSource() );
                        saver.close();
                    } else if ( xlsxFile.isSelected() ) {
                        file = new File(Model.getReportDirectory().getAbsolutePath() + File.separator + name + ".xlsx");
                        TabularDataSaver saver = TabularDataFactory.newSaverForFile(file);
                        ExcelReportGenerator.generate( saver, convertSource() );
                        saver.close();
                    }
                }
                if ( file != null ) {
                    EventBusFactory.getEventBus().publish(new InfoMessageEvent("Generated report: " + file.getAbsolutePath()));
                }
                ReportDialog.this.setVisible(false);
            }
        });
        container.add(control, BorderLayout.SOUTH);


        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(container, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(owner);
    }

    public void setReportSource( Workspace workspace ) {
        this.workspace = workspace;
        if ( workspace.isHasBeenCalculated() ) {
            name = workspace.getName() + " Simulation Report";
        } else {
            name = workspace.getName() + " Report";
        }

        if ( !workspace.isHasBeenCalculated() ) {
            preResults.setEnabled(false);
            results.setEnabled(false);
            expandResults.setEnabled(false);
        }
    }

    public void setReportSource( BatchJobList batchJobList ) {
        this.batchJobList = batchJobList;
        if ( batchJobList.hasBeenCalculated() ) {
            name = batchJobList.getDescription().name() + " Simulation Report";
        } else {
            name = batchJobList.getDescription().name() + " Report";
        }



        if ( !batchJobList.hasBeenCalculated() ) {
            preResults.setEnabled(false);
            results.setEnabled(false);
            expandResults.setEnabled(false);
        }
    }


    private boolean s( JCheckBox check ) {
        return check.isEnabled() && check.isSelected();
    }

    private List<ReportSection> convertSource() {
        if ( workspace != null ) {
            return ReportGenerator.generate(s(scenario), s(preResults), s(results), s(expandResults), workspace);
        } else if ( batchJobList != null ) {
            LinkedList<ReportSection> batch = new LinkedList<>();

            for (Workspace ws : batchJobList.getBatchJobs()) {
                batch.addAll(ReportGenerator.generate(s(scenario), s(preResults), s(results), s(expandResults), ws));
            }

            ReportSection main = new ReportSection("Batch Report [" + batchJobList.getDescription() + "]");
            batch.addFirst( main );

            return batch;
        }

        throw new RuntimeException("No known report souce found!");
    }
}
