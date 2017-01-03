package org.seamcat.presentation.components;

import org.seamcat.help.SeamcatHelpResolver;
import org.seamcat.presentation.SeamcatIcons;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;

/** Decorate a panel with a border with title and possibly other
 * title related widgets.
 */
public class BorderPanel extends JPanel {

    JComponent innerPanel;
    JLabel titleLabel;
    JLabel informationIconLabel;
    JLabel helpButton;

    public BorderPanel() {
        setLayout(new BorderPanelLayout());
        titleLabel = new JLabel();
        titleLabel.setFont(UIManager.getFont("TitledBorder.font"));
        titleLabel.setForeground(UIManager.getColor("TitledBorder.titleColor"));
        add(titleLabel, BorderPanelLayout.TITLE_WIDGET);
        helpButton = new JLabel(SeamcatIcons.getImageIcon("SEAMCAT_ICON_HELP", SeamcatIcons.IMAGE_SIZE_TOOLBAR));
        add(helpButton, BorderPanelLayout.TITLE_WIDGET);
        helpButton.setVisible(false);
        informationIconLabel = new JLabel(SeamcatIcons.getImageIcon("SEAMCAT_ICON_INFORMATION", SeamcatIcons.IMAGE_SIZE_TOOLBAR));
        add(informationIconLabel, BorderPanelLayout.TITLE_WIDGET);
        informationIconLabel.setVisible(false);
    }

    public BorderPanel(JComponent innerPanel, String title) {
        this();
        setInnerPanel(innerPanel);
        setTitle(title);
    }

    public BorderPanel(JComponent innerPanel, String title, String infoText) {
        this();
        setInnerPanel(innerPanel);
        setTitle(title);
        setInformationText(infoText);
    }

    public BorderPanel(JComponent innerPanel, String title, String helpText, String helpLink) {
        this();
        setInnerPanel(innerPanel);
        setTitle(title);
        setHelpTextAndLink( helpText, helpLink );
    }

    public BorderPanel(JComponent innerPanel, String title, String helpText, String helpLink, String infoText ) {
        this();
        setInnerPanel( innerPanel);
        setTitle(title);
        setHelpTextAndLink(helpText, helpLink);
        setInformationText(infoText);
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void setInnerPanel(JComponent innerPanel) {
        this.innerPanel = innerPanel;
        innerPanel.setBorder(new TitledBorder((String) null));
        add(innerPanel, BorderPanelLayout.MAIN);
    }

    public JComponent getInnerPanel() {
        return innerPanel;
    }

    private void setInformationText(String text) {
        if (text != null) {
            informationIconLabel.setToolTipText(text);
            informationIconLabel.setVisible(true);
        }
        else {
            informationIconLabel.setVisible(false);
        }
    }

    private void setHelpTextAndLink( String helpText, final String helpLink ) {
        helpButton.setToolTipText(helpText);
        helpButton.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                SeamcatHelpResolver.showHelp(helpLink);
            }
        });
        helpButton.setVisible(true);
    }

}
