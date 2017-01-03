package org.seamcat.batch;

import javax.swing.JOptionPane;

import org.seamcat.model.engines.ICEConfiguration;
import org.seamcat.presentation.MainWindow;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class BatchItemControl {

	private ICEConfiguration iceConf;
	private boolean processed;

	public BatchItemControl() {
		iceConf = new ICEConfiguration();
	}

	public BatchItemControl(Element element) {
		processed = Boolean.valueOf(element.getAttribute("processed"));
		try {
			iceConf = new ICEConfiguration((Element) element.getElementsByTagName(
			      "ICEConfiguration").item(0));
		} catch (Exception e) {
			JOptionPane.showMessageDialog(MainWindow.getInstance(),
			      "Ignoring ICE Results!\nCause: " + e.getMessage(),
			      "Ignoring stored results", JOptionPane.WARNING_MESSAGE);
		}
	}

	public ICEConfiguration getIceConfiguration() {
		return iceConf;
	}

	public int getInterenceCriterionType() {
		return iceConf.getInterferenceCriterionType();
	}

	public boolean isProcessed() {
		return processed;
	}

	public void setProcessed(boolean processed) {
		this.processed = processed;
	}

	public Element toElement(Document doc) {
		// return null;
		Element element = doc.createElement("batch-item-control");
		element.setAttribute("processed", Boolean.toString(processed));
		element.appendChild(iceConf.toElement(doc));
		return element;
	}

}
