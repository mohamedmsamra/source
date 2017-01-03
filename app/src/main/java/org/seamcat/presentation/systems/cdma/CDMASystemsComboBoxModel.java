package org.seamcat.presentation.systems.cdma;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CDMASystemsComboBoxModel implements ComboBoxModel {

	private List<CDMAPlotModel> listModel;
    private CDMAPlotModel selected;
	private Map<String, CDMAPlotModel> model;

	public CDMASystemsComboBoxModel(Map<String, CDMAPlotModel> model) {
		this.model = model;
        listModel = new ArrayList<>();
        listModel.addAll( model.values());
        if ( listModel.size() > 0 ) {
            selected = listModel.get(0);
        }
	}

	public void refresh() {
	}

	public void addListDataListener(ListDataListener arg0) {
	}

	public Object getElementAt(int arg0) {
		return listModel.get(arg0);
	}

	public Object getSelectedItem() {
		return selected;
	}

	public int getSize() {
		return listModel.size();
	}

	public void removeListDataListener(ListDataListener arg0) {
	}

	public void setSelectedItem(Object _selected) {
		 selected = (CDMAPlotModel) _selected;
	}

	public void close() {
		model = null;
	}
}
