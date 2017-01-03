package org.seamcat.presentation.components;

import org.seamcat.presentation.EscapeDialog;
import org.seamcat.presentation.MainWindow;
import org.seamcat.presentation.genericgui.panelbuilder.GenericPanelEditor;

import javax.swing.*;
import java.awt.*;

public class LocalEnvironmentDialog extends EscapeDialog {

    private final GenericPanelEditor<LocalEnvironmentUI> editor;

    public LocalEnvironmentDialog(final LocalEnvironmentUI ui ) {
        super(MainWindow.getInstance(), true);
        getContentPane().setLayout(new BorderLayout());
        editor = new GenericPanelEditor<>(MainWindow.getInstance(), LocalEnvironmentUI.class, ui);
        setTitle("Edit Local Environment");
        getContentPane().add(editor, BorderLayout.CENTER);
        getContentPane().add(new NavigateButtonPanel(this, false) {
            @Override
            public void btnOkActionPerformed() {
                // validate percentage
                LocalEnvironmentUI model = editor.getModel();
                if ( model.probability() > 100 || model.probability() < 0 ) {
                    JOptionPane.showMessageDialog(this, "Probability must be between 0 and 100", "Illegal Setting", JOptionPane.ERROR_MESSAGE );
                } else {
                    super.btnOkActionPerformed();
                }
            }
        }, BorderLayout.SOUTH);
        setSize( new Dimension(450,450));
    }

    public LocalEnvironmentUI getModel() {
        return editor.getModel();
    }
}
