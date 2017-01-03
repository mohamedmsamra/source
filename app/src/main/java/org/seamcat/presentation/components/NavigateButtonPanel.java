package org.seamcat.presentation.components;

import org.seamcat.help.SeamcatHelpResolver;
import org.seamcat.presentation.EscapeDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

public class NavigateButtonPanel extends JPanel {

    protected static final ResourceBundle stringlist = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);

    protected final JButton btnCancel;
    protected final JButton btnHelp;
    protected final JButton btnOk;
    private EscapeDialog owner;

    public NavigateButtonPanel( EscapeDialog owner ) {
        this( owner, true );
    }

    public NavigateButtonPanel(EscapeDialog owner, boolean displayHelp ) {
        this(owner, displayHelp, true, stringlist.getString("BTN_CAPTION_OK"));
    }

    public NavigateButtonPanel(EscapeDialog owner, boolean displayHelp, boolean displayCancel ) {
        this(owner, displayHelp, displayCancel, stringlist.getString("BTN_CAPTION_OK"));
    }

    public NavigateButtonPanel( EscapeDialog owner, boolean displayHelp, String okText  ) {
        this(owner, displayHelp, true, okText );
    }

    public NavigateButtonPanel( EscapeDialog owner, boolean displayHelp, boolean displayCancel, String okText  ) {
        this.owner = owner;
        btnOk = new JButton(okText);
        btnCancel = new JButton(stringlist.getString("BTN_CAPTION_CANCEL"));
        btnHelp = new JButton(stringlist.getString("BTN_CAPTION_HELP"));

        btnOk.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                btnOkActionPerformed();
            }
        });
        btnCancel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                btnCancelActionPerformed();
            }
        });
        btnHelp.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                btnHelpActionPerformed();
            }
        });

        add(btnOk);
        if ( displayCancel ) {
            add(btnCancel);
        }
        if ( displayHelp ) {
            add(btnHelp);
        }
    }

    public void btnCancelActionPerformed() {
        owner.setAccept( false );
        owner.setVisible( false );
    }

    public void btnHelpActionPerformed() {
        SeamcatHelpResolver.showHelp(owner);
    }

    public void btnOkActionPerformed() {
        owner.setAccept( true );
        owner.setVisible( false );
    }
}
