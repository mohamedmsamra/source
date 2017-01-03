package org.seamcat.presentation.displaysignal;

import org.seamcat.help.SeamcatHelpResolver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.ResourceBundle;

public class ControlButtonPanel extends JPanel {

    private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", Locale.ENGLISH);

    public ControlButtonPanel(final Component owner, ActionListener saveAction, ActionListener saveImageAc) {
        super(new GridBagLayout());

        JButton close = new JButton(STRINGLIST.getString("BTN_CAPTION_CLOSE"));
        close.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (owner != null && owner instanceof Window) {
                    ((Window) owner).dispose();
                }
            }
        });

        JButton save  = new JButton(STRINGLIST.getString("BTN_CAPTION_SAVE"));
        save.addActionListener( saveAction );

        JButton saveImage = new JButton("Save image");
        saveImage.addActionListener(saveImageAc);

        JButton help  = new JButton(STRINGLIST.getString("BTN_CAPTION_HELP"));
        help.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                SeamcatHelpResolver.showHelp(this);
            }
        });

        GridBagConstraints gc = new GridBagConstraints(0,
                GridBagConstraints.RELATIVE, 1, 1, 1, 50,
                GridBagConstraints.SOUTHEAST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 1, 1);
        add(Box.createVerticalGlue(), gc);
        gc.weighty = 1;
        add(save, gc);
        add(saveImage, gc);
        add(close, gc);
        add(help, gc);
    }

}
