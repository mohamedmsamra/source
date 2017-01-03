package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

public class RemoveUnusedAttributesWorkspaceMigration extends AbstractScenarioMigration {


	@Override
	void migrateScenarioDocument(Document document) {
		updateVersion(document);

        JXPathContext context = JXPathContext.newContext(document);
        List systemLinks = context.selectNodes("//systemLink");

        for (Object o : systemLinks) {
            Element link = (Element) o;
            link.removeAttribute("rxTxVerTol");
            link.removeAttribute("txRxVerTol");
            link.removeAttribute("rxTxHorTol");
            link.removeAttribute("txRxHorTol");
        }

        List transceivers = context.selectNodes("//transceiver");

        for (Object o : transceivers) {
            Element transceiver = (Element) o;
            transceiver.removeAttribute("refAntennaHeight");
        }

        List vrs = context.selectNodes("//VictimReceiver");
        for (Object o : vrs) {
            Element vr = (Element) o;
            vr.removeAttribute("refAntennaHeight");
            vr.removeAttribute("checkPcMax");
            vr.removeAttribute("criterionCalcMode");
            vr.removeAttribute("vrTrialFrequency");
            vr.removeAttribute("pcMax");
        }


        List txs = context.selectNodes("//transmitter");
        for (Object o : txs) {
            Element tx = (Element) o;
            tx.removeAttribute("user_density");
            tx.removeAttribute("fixed_coverage_radius");
            tx.removeAttribute("frequency_re_use");
        }

        List itxs = context.selectNodes("//InterferingTransmitter");
        for (Object o : itxs) {
            Element itx = (Element) o;
            itx.removeAttribute("itTrialFrequency");
        }
    }

    @Override
    void migrateResultsDocument(Document document) {
    }

    private void updateVersion(Document document) {
	   JXPathContext context = JXPathContext.newContext(document);
	   context.createPathAndSetValue("Workspace/@workspace_format_version", getOriginalVersion().nextVersion().getNumber());
   }

	@Override
   public FormatVersion getOriginalVersion() {
	   return new FormatVersion(9);
   }


}
