package org.seamcat.presentation.components;


import org.seamcat.model.factory.Factory;
import org.seamcat.model.systems.generic.LocalEnvironments;
import org.seamcat.model.types.LocalEnvironment;
import org.seamcat.presentation.DisposableJPanel;
import org.seamcat.presentation.genericgui.panelbuilder.PanelModelEditor;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ResourceBundle;

public class LocalEnvironmentsTxRxPanel extends DisposableJPanel implements PanelModelEditor<LocalEnvironments> {

    private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);

    private final LocalEnvironmentsPanel receiverEnv;
    private final LocalEnvironmentsPanel transmitterEnv;
    private final String name;

    public LocalEnvironmentsTxRxPanel(LocalEnvironmentsTxRxModel model, String name) {
        super(new BorderLayout());
        this.name = name;
        JPanel panel = new JPanel(new GridLayout(2,1));

        receiverEnv = new LocalEnvironmentsPanel("Receiver");
        panel.add(receiverEnv);
        transmitterEnv = new LocalEnvironmentsPanel("Transmitter");
        panel.add(transmitterEnv);

        add(panel, BorderLayout.CENTER);
        setModel(model.getReceiverEnvs(), model.getTransmitterEnvs());
    }

    @Override
    public void dispose() {
        receiverEnv.dispose();
        transmitterEnv.dispose();
    }


    public void setModel( List<LocalEnvironment> receiver, List<LocalEnvironment> transmitter) {
        receiverEnv.setModel(receiver);
        transmitterEnv.setModel(transmitter);
    }

    @Override
    public JPanel getPanel() {
        if ( STRINGLIST.containsKey(LocalEnvironments.class.getName()) ) {
            return new BorderPanel(new JScrollPane(this), name, "See SEAMCAT manual", STRINGLIST.getString(LocalEnvironments.class.getName()));
        } else {
            return new BorderPanel(new JScrollPane(this), name);
        }
    }

    @Override
    public LocalEnvironments getModel() {
        receiverEnv.updateModel();
        transmitterEnv.updateModel();
        LocalEnvironments prototype = Factory.prototype(LocalEnvironments.class);
        Factory.when( prototype.receiverEnvironments()).thenReturn(receiverEnv.getModel() );
        Factory.when( prototype.transmitterEnvironments() ).thenReturn(transmitterEnv.getModel());
        return Factory.build(prototype);
    }
}
