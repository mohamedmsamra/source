package org.seamcat.presentation.genericgui;

/** A generic panel with the added ability of interacting with a model object
 * for all of the items.
 */
public class GenericModelEditorPanel<Model> extends GenericPanel {
	
	private Model model;

    public Model getModel() {
		return model;
	}

}
