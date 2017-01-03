package org.seamcat.presentation.components;

import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.eventbus.UIEventHandler;
import org.seamcat.function.DiscreteFunction;
import org.seamcat.model.types.result.AntennaResultImpl;
import org.seamcat.model.types.result.LinkResultImpl;
import org.seamcat.plugin.AntennaGainConfiguration;
import org.seamcat.presentation.DialogHelper;
import org.seamcat.presentation.EscapeDialog;
import org.seamcat.presentation.MainWindow;
import org.seamcat.presentation.displaysignal.ControlButtonPanel;
import org.seamcat.presentation.genericgui.GenericPanel;
import org.seamcat.presentation.genericgui.ItemChangedEvent;
import org.seamcat.presentation.genericgui.item.DoubleItem;
import org.seamcat.tabulardataio.FileDataIO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GainPlotDialog extends EscapeDialog {

    private DiscreteFunctionGraph graph;
    private DoubleItem frequency;
    private DoubleItem granularity;
    private DiscreteFunctionTableModelAdapter hor;
    private DiscreteFunctionTableModelAdapter ver;
    private DiscreteFunctionTableModelAdapter sph;
    private DiscreteFunction horizontal = new DiscreteFunction();
    private DiscreteFunction vertical = new DiscreteFunction();
    private DiscreteFunction spherical = new DiscreteFunction();
    private AntennaGainConfiguration configuration;


    public GainPlotDialog(AntennaGainConfiguration configuration) {
        super(MainWindow.getInstance(), "Antenna Gain Plot", true);
        this.configuration = configuration;

        GenericPanel panel = new GenericPanel();
        frequency = new DoubleItem();
        panel.addItem( frequency.label("Frequency").unit("MHz"));
        granularity = new DoubleItem();
        panel.addItem( granularity.label("Granularity"));
        panel.initializeWidgets();
        frequency.setValue(900.0);
        granularity.setValue(1.0);

        gainPlot();
        hor = new DiscreteFunctionTableModelAdapter(horizontal);
        ver = new DiscreteFunctionTableModelAdapter(vertical);
        sph = new DiscreteFunctionTableModelAdapter(spherical);

        graph = new DiscreteFunctionGraph(hor, ver, sph);
        JPanel right = new JPanel(new BorderLayout());
        right.add( panel, BorderLayout.NORTH );
        right.add( createControls(), BorderLayout.CENTER );

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new BorderPanel(graph, "Gain plot of "+configuration.description().name()), BorderLayout.CENTER);
        getContentPane().add(right, BorderLayout.EAST);
        pack();
        EventBusFactory.getEventBus().subscribe(this);
    }

    @Override
    public boolean display() {
        boolean display = super.display();
        EventBusFactory.getEventBus().unsubscribe(this);
        return display;
    }

    @UIEventHandler
    public void handle(ItemChangedEvent event ) {
        if ( event.getItem() == frequency || event.getItem() == granularity ) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            new SwingWorker<Void, Void>() {

                @Override
                protected Void doInBackground() throws Exception {
                    gainPlot();
                    return null;
                }

                @Override
                protected void done() {
                    setCursor(Cursor.getDefaultCursor());
                    try {
                        get();
                    } catch (Exception e) {
                        DialogHelper.gainCalculationError(e);
                        // show error dialog
                        horizontal = new DiscreteFunction();
                        vertical = new DiscreteFunction();
                        spherical = new DiscreteFunction();
                    }
                    hor.setDiscreteFunction( horizontal );
                    ver.setDiscreteFunction( vertical );
                    sph.setDiscreteFunction( spherical );
                }
            }.execute();

        }
    }

    private ControlButtonPanel createControls() {
        return new ControlButtonPanel(this,
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        FileDataIO fileIO = SaveFileChooser.chooseFile(GainPlotDialog.this.getParent());
                        if (fileIO != null) {
                            DiscreteFunctionTableModelAdapter dataSet = graph.getDataSet();
                            fileIO.savePoints(dataSet.getFunction().points());
                        }
                    }
                },
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        graph.saveImage();
                    }
                });
    }

    private void gainPlot() {
        LinkResultImpl result = new LinkResultImpl();
        result.setFrequency( frequency.getValue() );
        AntennaResultImpl x = new AntennaResultImpl();
        result.setRxAntenna( x );
        result.setTxAntenna( x );

        // all integer angles
        horizontal = new DiscreteFunction();
        vertical = new DiscreteFunction();
        spherical = new DiscreteFunction();

        x.setElevation(0);
        for (double azi = 0; azi <= 360; azi += granularity.getValue()) {
            x.setAzimuth(azi);
            double value = configuration.evaluate(result, x);
            double v = Math.rint(azi * 10) / 10;
            horizontal.addPoint(v, value);
            if ( azi <= 180 ) {
                spherical.addPoint(v, value);
            }
        }

        x.setAzimuth(0);
        for (double ele = -90; ele <= 90; ele += granularity.getValue()) {
            x.setElevation(ele);
            vertical.addPoint(Math.rint(ele * 10)/10, configuration.evaluate(result, x));
        }
    }

}
