package org.seamcat.presentation;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.seamcat.Seamcat;
import org.seamcat.help.SeamcatHelpResolver;
import org.seamcat.model.factory.Model;
import org.seamcat.presentation.components.NavigateButtonPanel;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import static org.seamcat.presentation.LabeledPairLayout.FIELD;
import static org.seamcat.presentation.LabeledPairLayout.LABEL;

/**
 * 
 * @author Christian Petersen CIBER Danmark A/S
 */
public final class DialogOptions extends EscapeDialog {

    private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", Locale.ENGLISH);
    private static final Logger LOG = Logger.getLogger(DialogOptions.class);
	private static final Level[] logLevels = { Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR, Level.FATAL, Level.OFF };

	private JFileChooser fc;
	private JTextField filename = new JTextField(25);
	private JButton filenameButton = new JButton(STRINGLIST.getString("BTN_CAPTION_BROWSE"));
	private JPanel filenamePanel = new JPanel(new BorderLayout());
	private JComboBox formats = new JComboBox();

	private JLabel lblFilename = new JLabel(STRINGLIST.getString("DIALOG_OPTIONS_FILENAME"));
	private JLabel lblFormatString = new JLabel(STRINGLIST.getString("DIALOG_OPTIONS_FORMAT"));
	private JLabel lblLogLevel = new JLabel(STRINGLIST.getString("DIALOG_OPTIONS_LOGLEVEL"));
	private JComboBox loglevel = new JComboBox(logLevels);
	private JComboBox lookAndFeel = new JComboBox();

	private PatternLayout logTestLayout = new PatternLayout();
    private JCheckBox showWelcomeScreen = new JCheckBox(STRINGLIST.getString("DIALOG_OPTIONS_SHOW_WELCOME"));

	public DialogOptions(JFrame owner) {
		super(owner);
		fc = new SeamcatJFileChooser(true);

		filenamePanel.add(filename, BorderLayout.CENTER);
		filenamePanel.add(filenameButton, BorderLayout.EAST);
        filename.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                Model.setSeamcatBaseDir(filename.getText());
            }
        });

		filenameButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				browseFileActionPerformed();
			}
		});

		DefaultComboBoxModel m = new DefaultComboBoxModel();
		for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			if (!info.getName().equals("Windows Classic") && !info.getName().equals("GTK+")) {
				// CP 20-03-2008: Windows Classic Look And Feel is disabled because
				// it throws a null pointer on Java 6
				// (See: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6429812)
				m.addElement(info.getName());
			}
		}

		lookAndFeel.setModel(m);
		lookAndFeel.setSelectedItem(UIManager.getLookAndFeel().getName());
		lookAndFeel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                LookAndFeelInfo selected = null;
                String s = (String) lookAndFeel.getSelectedItem();
                if (s != null) {
                    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                        if (s.equals(info.getName())) {
                            selected = info;
                            break;
                        }
                    }
                }
            }

        });

		getContentPane().setLayout(new BorderLayout());
		JPanel options = new JPanel(new LabeledPairLayout());
		options.add(LABEL, lblFilename);
		options.add(FIELD, filenamePanel);

        options.add( LABEL, new JLabel(""));
        options.add( FIELD, new JLabel("Notice: you need to restart SEAMCAT for a new SEAMCAT home to take effect"));

		options.add(LABEL, lblLogLevel);
		options.add(FIELD, loglevel);

		options.add(LABEL, lblFormatString);
		options.add(FIELD, formats);

		options.add(LABEL, new JLabel("Select the Application Look And Feel"));
		options.add(FIELD, lookAndFeel);
        
        options.add(LABEL, new JLabel(""));
        options.add(FIELD, showWelcomeScreen);

		options.setBorder( BorderFactory.createEmptyBorder(5,5,5,5));

		getContentPane().add(options, BorderLayout.CENTER);
		getContentPane().add(new NavigateButtonPanel(this) {
			@Override
			public void btnOkActionPerformed() {
				okButtonActionPerformed();
			}
		}, BorderLayout.SOUTH);

		setTitle(STRINGLIST.getString("DIALOG_OPTIONS_TITLE"));

		loglevel.addActionListener(new java.awt.event.ActionListener() {

			public void actionPerformed(java.awt.event.ActionEvent evt) {
				logLevelActionPerformed();
			}
		});

		formats.setModel(new DefaultComboBoxModel(Model.getInstance().getLogPatterns()));
		formats.setEditable(true);
		formats.addActionListener(new java.awt.event.ActionListener() {

			public void actionPerformed(java.awt.event.ActionEvent evt) {
				formatStringActionPerformed();
			}
		});

		setSize(800, 500);
		setLocationRelativeTo(owner);
		registerHelp();
	}
	
	private void registerHelp() {
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
		getRootPane().registerKeyboardAction(new ActionListener() {

			@Override
         public void actionPerformed(ActionEvent e) {
				SeamcatHelpResolver.showHelp(this);
         }
			
		}, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
   }

	private void browseFileActionPerformed() {
		fc.setSelectedFile(new File(Model.getSeamcatHomeBaseDir()));
		int returnVal = fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			filename.setText(fc.getSelectedFile().getAbsolutePath());
            Model.setSeamcatBaseDir( fc.getSelectedFile().getAbsolutePath() );
		}
	}

	private void formatStringActionPerformed() {
		logTestLayout.setConversionPattern(formats.getSelectedItem().toString());
	}

	private void logLevelActionPerformed() {
		Logger.getRootLogger().setLevel(Level.INFO);
		Logger.getRootLogger().info("Setting logging level to: " + logLevels[loglevel.getSelectedIndex()]);
		Logger.getRootLogger().setLevel(logLevels[loglevel.getSelectedIndex()]);
	}

	private void okButtonActionPerformed() {
		String pattern = formats.getSelectedItem().toString();
		Model.getInstance().addPattern(pattern);
        Model.getInstance().setShowWelcome(showWelcomeScreen.isSelected());

        Preferences pref = Preferences.userNodeForPackage(Seamcat.class);
        pref.put( Seamcat.SHOW_WELCOME, Boolean.toString( showWelcomeScreen.isSelected()) );

		String s = (String) this.lookAndFeel.getSelectedItem();
		if (s != null) {
			LookAndFeelInfo selected = null;
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if (s.equals(info.getName())) {
					selected = info;
				}
			}

				if (selected != null) {
					
					if (!selected.getClassName().equals(UIManager.getLookAndFeel().getClass().getName())) {
						pref.put(Seamcat.LOOK_AND_FEEL_KEY, selected.getClassName());
						JOptionPane.showMessageDialog(DialogOptions.this, "Changes to SEAMCAT's look and feel will not be activated\nuntil the application is restarted.");
					}
				}
		}
		super.setVisible(false);
	}

	@Override
	public void setVisible(boolean value) {
		filename.setText(Model.getSeamcatHomeBaseDir());
		loglevel.setSelectedItem(Logger.getRootLogger().getLevel());
		formats.setSelectedItem(Model.getInstance().getLogFilePattern().getConversionPattern());
		logTestLayout.setConversionPattern(formats.getSelectedItem().toString());
        showWelcomeScreen.setSelected(Model.getInstance().showWelcomeScreen());
		super.setVisible(value);
	}
}