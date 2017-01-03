package org.seamcat.migration;

import junit.framework.Assert;
import org.apache.commons.jxpath.JXPathContext;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.seamcat.migration.workspace.RemoveUnusedAttributesWorkspaceMigration;
import org.seamcat.model.MigrationIssue;
import org.seamcat.testutil.XPathAssert;
import org.seamcat.util.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class RemoveUnusedAttributesMigrationTest extends AbstractScenarioHelper {

	RemoveUnusedAttributesWorkspaceMigration migration;
    Set<String> attributesRemoved;

	@Before
	public void setUp() {
		migration = new RemoveUnusedAttributesWorkspaceMigration();
        attributesRemoved = new HashSet<String>();
        attributesRemoved.add("rxTxVerTol");
        attributesRemoved.add("txRxVerTol");
        attributesRemoved.add("rxTxHorTol");
        attributesRemoved.add("txRxHorTol");
        attributesRemoved.add("refAntennaHeight");

        attributesRemoved.add("refAntennaHeight");
        attributesRemoved.add("checkPcMax");
        attributesRemoved.add("criterionCalcMode");
        attributesRemoved.add("vrTrialFrequency");
        attributesRemoved.add("pcMax");

        attributesRemoved.add("user_density");
        attributesRemoved.add("fixed_coverage_radius");
        attributesRemoved.add("frequency_re_use");

        attributesRemoved.add("itTrialFrequency");
	}

    @Ignore("FIX THIS!!!")
	@Test
	public void migrate() {
        String path = new File("").getAbsolutePath();
		File originalFile = IOUtils.copyResourceToTempFile(path + "/app/src/test/resources/migration/removeUnusedAttributes.sws");
		File migratedFile = IOUtils.createTempFile();
		migration.migrate( originalFile, migratedFile, new ArrayList<MigrationIssue>());
		
		findScenarioAndResults(migratedFile);
	}

	@Override
	void migrateScenarioDocument(Document document) {
		XPathAssert xpathAssert = new XPathAssert(document);
		xpathAssert.nodeValueEquals( "10", "Workspace/@workspace_format_version");

        // check that none of the attributes are there
        JXPathContext context = JXPathContext.newContext(document);
        assertNotFound(context.selectNodes("//systemLink"));

        assertNotFound(context.selectNodes("//transceiver"));

        assertNotFound(context.selectNodes("//VictimReceiver"));

        assertNotFound(context.selectNodes("//transmitter"));

        assertNotFound(context.selectNodes("//InterferingTransmitter"));

	}

    private void assertNotFound( List elements ) {
        for (Object o : elements) {
            Element element = (Element) o;

            for (String attribute : attributesRemoved) {
                Assert.assertFalse( "Attribute "+attribute+" must be removed", element.hasAttribute( attribute ));
            }
        }
    }

	@Override
	void migrateResultsDocument(Document document) {
	}
}
