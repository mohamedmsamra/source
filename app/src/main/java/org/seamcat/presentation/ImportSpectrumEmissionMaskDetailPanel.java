package org.seamcat.presentation;

import org.seamcat.function.EmissionMaskImpl;
import org.seamcat.presentation.components.BorderPanel;
import org.seamcat.presentation.components.DiscreteFunction2TableModelAdapter;
import org.seamcat.presentation.components.UnwantedEmissionGraph2;

import javax.swing.*;
import java.awt.*;

public class ImportSpectrumEmissionMaskDetailPanel extends JPanel {

    public ImportSpectrumEmissionMaskDetailPanel(EmissionMaskImpl fun) {
        setLayout(new BorderLayout());
        JPanel previewPanel = new JPanel(new BorderLayout());
        add(new BorderPanel(previewPanel, "Preview"), BorderLayout.CENTER);

        DiscreteFunction2TableModelAdapter model = new DiscreteFunction2TableModelAdapter();
        UnwantedEmissionGraph2 emissionGraph2 = new UnwantedEmissionGraph2(model);
        emissionGraph2.setVictimCharacteristics(-1,-1,false);
        model.setFunction( fun );
        previewPanel.add( emissionGraph2 );
    }
}