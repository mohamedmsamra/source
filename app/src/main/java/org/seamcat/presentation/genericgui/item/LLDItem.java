package org.seamcat.presentation.genericgui.item;

import com.rits.cloning.Cloner;
import org.seamcat.cdma.CDMALinkLevelData;
import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.events.InfoMessageEvent;
import org.seamcat.model.factory.Model;
import org.seamcat.presentation.DialogHelper;
import org.seamcat.presentation.components.BorderPanel;
import org.seamcat.presentation.components.GenericListDetailDialog;
import org.seamcat.presentation.genericgui.WidgetAndKind;
import org.seamcat.presentation.genericgui.WidgetKind;
import org.seamcat.presentation.resources.ImageLoader;
import org.seamcat.presentation.systems.cdma.CDMAEditModel;
import org.seamcat.presentation.systems.cdma.CDMALinkLevelDataEditorDialog;
import org.seamcat.presentation.systems.cdma.LLDGraphPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import static org.seamcat.cdma.CDMALinkLevelData.LinkType.DOWNLINK;
import static org.seamcat.cdma.CDMALinkLevelData.LinkType.UPLINK;
import static org.seamcat.presentation.genericgui.WidgetKind.LABEL;


public class LLDItem extends AbstractItem<CDMALinkLevelData, Object> {

    private final JPanel buttons;
    private ImageIcon importIcon = new ImageIcon(ImageLoader.class.getResource("import_16x16.png"));
    private ImageIcon exportIcon = new ImageIcon(ImageLoader.class.getResource("export_16x16.png"));

    private CDMALinkLevelDataEditorDialog lldEditor = new CDMALinkLevelDataEditorDialog();
    private JLabel workspaceLinkData;
    private CDMALinkLevelData model;


    public LLDItem(final JFrame owner, final boolean downlink) {
        buttons = new JPanel( new GridLayout(1,3));

        JButton edit = new JButton("Edit");
        buttons.add(edit);
        edit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editLinkLevelDataButtonActionPerformed(e);
            }
        });

        JButton anImport = new JButton(importIcon);
        anImport.setToolTipText("Import");
        buttons.add(anImport);
        anImport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                List<CDMALinkLevelData> all = Model.getInstance().getLibrary().getCDMALinkLevelData();
                List<CDMALinkLevelData> filtered = new ArrayList<CDMALinkLevelData>();
                // filter based on the type of the system
                for (CDMALinkLevelData link : all) {
                    if (link.getLinkType() == DOWNLINK && downlink) filtered.add(link);
                    if (link.getLinkType() == UPLINK && !downlink) filtered.add(link);
                }
                GenericListDetailDialog<CDMALinkLevelData> dialog = new GenericListDetailDialog<CDMALinkLevelData>(owner, "Import CDMA LLD", filtered) {
                    public void selectedElement(CDMALinkLevelData data) {
                        LLDGraphPanel panel = new LLDGraphPanel();
                        panel.setGraph((CDMAEditModel) data.getTableModel(1));
                        if ( data.getLinkType() == DOWNLINK ) {
                            panel.setRangeLabel(data.getTargetERpct(), data.getTargetERType().toString());
                        }
                        setDetail(new BorderPanel(panel, "Preview"));
                    }
                };
                if (dialog.display()) {
                    model = dialog.getSelectedValue();
                    updateSelectedLinkDataValuePreview();
                }
            }
        });

        JButton export = new JButton(exportIcon);
        export.setToolTipText("Export");
        buttons.add(export);
        export.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                CDMALinkLevelData clone = new Cloner().deepClone(model);
                if (!Model.getInstance().getLibrary().addCDMALinkLevelData(clone)) {
                    if (DialogHelper.overrideInLibrary(owner, clone.toString())) {
                        Model.getInstance().getLibrary().removeCDMALinkLevelData(clone);
                        Model.getInstance().getLibrary().addCDMALinkLevelData(clone);
                        EventBusFactory.getEventBus().publish(new InfoMessageEvent(String.format("'%s' overridden in CDMA link level data library", model.description().name())));
                    }
                } else {
                    EventBusFactory.getEventBus().publish(new InfoMessageEvent(String.format("'%s' added to CDMA link level data library", model.shortText())));
                }
            }
        });

        workspaceLinkData = new JLabel();

    }

    private void editLinkLevelDataButtonActionPerformed(ActionEvent e) {
        if (lldEditor.show(model)) {
            updateSelectedLinkDataValuePreview();
        }
    }

    public LLDItem label(String label) {
        super.label(label);
        return this;
    }

    public LLDItem unit(String unit) {
        super.unit(unit);
        return this;
    }


    private void updateSelectedLinkDataValuePreview() {
        if (model == null) {
            workspaceLinkData.setText("");
            workspaceLinkData.setToolTipText("");
        }
        else {
            workspaceLinkData.setText(model.shortText());
            workspaceLinkData.setToolTipText(model.fullText());
        }
    }

    @Override
    public List<WidgetAndKind> createWidgets() {
        List<WidgetAndKind> widgets = super.createWidgets();

        widgets.add(new WidgetAndKind(workspaceLinkData, LABEL));
        widgets.add(new WidgetAndKind(buttons, WidgetKind.VALUE));

        return widgets;
    }

    @Override
    public CDMALinkLevelData getValue() {
        return model;
    }

    @Override
    public void setValue(CDMALinkLevelData model) {
        this.model = model;
        updateSelectedLinkDataValuePreview();
    }

    public void dispose() {
        super.dispose();
    }
}
