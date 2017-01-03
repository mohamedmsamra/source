package org.seamcat.presentation.components;

import org.jfree.ui.FilesystemFilter;
import org.seamcat.presentation.SeamcatJFileChooser;
import org.seamcat.tabulardataio.FileDataIO;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ResourceBundle;


public abstract class FunctionButtonPanel extends JPanel {

	protected static final ResourceBundle stringlist = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);

    private FilesystemFilter xlsFilter;
    private FilesystemFilter xlsxFilter;
    private FilesystemFilter txtFilter;

    protected JFileChooser fileChooser = new SeamcatJFileChooser();
	protected FileDataIO fileio = new FileDataIO();
    private DiscreteFunctionTableModelAdapterInterface model;

    public FunctionButtonPanel() {
        this(null);
    }

    public FunctionButtonPanel( DiscreteFunctionTableModelAdapterInterface model ) {
        this.model = model;

        xlsFilter = new FilesystemFilter("xls", "Excel files (*.xls)", true);
        xlsxFilter = new FilesystemFilter("xlsx", "Excel files (*.xlsx)", true);
        txtFilter = new FilesystemFilter("txt", "Text files (*.txt)", true);

        FileFilter first = fileChooser.getFileFilter();
        fileChooser.setFileFilter(txtFilter);
        fileChooser.setFileFilter(xlsFilter);
        fileChooser.setFileFilter(xlsxFilter);
        fileChooser.setFileFilter(first);

        JButton btnLoad = new JButton(stringlist.getString("BTN_CAPTION_LOAD"));
		JButton btnSave = new JButton(stringlist.getString("BTN_CAPTION_SAVE"));
		JButton btnSaveImage = new JButton("Save image");
        JButton btnClear = new JButton(stringlist.getString("BTN_CAPTION_CLEAR"));
		JButton btnAdd = new JButton(stringlist.getString("BTN_CAPTION_ADD"));
		JButton btnDelete = new JButton(stringlist
		      .getString("BTN_CAPTION_DELETE"));
		JButton btnSym = new JButton(stringlist.getString("BTN_CAPTION_SYM"));

		btnLoad.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				btnLoadActionPerformed();
			}
		});
		btnSave.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				btnSaveActionPerformed();
			}
		});
        btnSaveImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveChartImage();
            }
        });
		btnClear.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				btnClearActionPerformed();
			}
		});
		btnAdd.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				btnAddActionPerformed();
			}
		});
		btnDelete.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				btnDeleteActionPerformed();
			}
		});
		btnSym.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				btnSymActionPerformed();
			}
		});

		JPanel stretchPanel = new JPanel(new GridLayout(7, 1));
		stretchPanel.add(btnLoad);
		stretchPanel.add(btnSave);
        stretchPanel.add(btnSaveImage);
		stretchPanel.add(btnClear);
		stretchPanel.add(btnAdd);
		stretchPanel.add(btnDelete);
		stretchPanel.add(btnSym);
		add(stretchPanel);
	}

	public void btnAddActionPerformed() {
        model.addRow();
    }

	public void btnClearActionPerformed() {
        model.clear();
    }

	public abstract void btnDeleteActionPerformed();

    public abstract void saveChartImage();

	public void btnLoadActionPerformed() {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            fileio.setFile(selectedFile);
            model.setFunction( fileio.loadPoints() );
        }
    }

	public void btnSaveActionPerformed() {
        fileChooser.setFileFilter(xlsxFilter);
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = alignSelectedFile();
            if ( okToSave( selectedFile)) {
                fileio.setFile(fileChooser.getSelectedFile());
                fileio.savePoints(model.getFunction().getPoints());
            }
        }
    }

    protected File alignSelectedFile() {
        File selectedFile = fileChooser.getSelectedFile();
        if (fileChooser.getFileFilter() == txtFilter) {
            if (!selectedFile.getName().endsWith(".txt")) {
                fileChooser.setSelectedFile(new File(selectedFile.getAbsolutePath() + ".txt"));
            }
        } else if (fileChooser.getFileFilter() == xlsFilter) {
            if (!selectedFile.getName().endsWith(".xls")) {
                fileChooser.setSelectedFile(new File(selectedFile.getAbsolutePath() + ".xls"));
            }
        } else if (fileChooser.getFileFilter() == xlsxFilter) {
            if (!selectedFile.getName().endsWith(".xlsx")) {
                fileChooser.setSelectedFile(new File(selectedFile.getAbsolutePath() + ".xlsx"));
            }
        }

        return fileChooser.getSelectedFile();
    }

    protected boolean okToSave(File selectedFile) {
        if (selectedFile.exists()) {
            int res = JOptionPane.showConfirmDialog(this, "<html>The file named <b>" + selectedFile.getName() + "</b>" + " already exists.\nDo you wish to override?", "File already exists", JOptionPane.YES_NO_CANCEL_OPTION);
            if (res == JOptionPane.NO_OPTION) {
                return false;
            } else if (res == JOptionPane.CANCEL_OPTION) {
                return false;
            }
        }

        return true;
    }

	public abstract void btnSymActionPerformed();

}
