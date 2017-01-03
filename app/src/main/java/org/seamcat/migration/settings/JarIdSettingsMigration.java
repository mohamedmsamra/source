package org.seamcat.migration.settings;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FileMigration;
import org.seamcat.migration.FormatVersion;
import org.seamcat.model.MigrationIssue;
import org.seamcat.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JarIdSettingsMigration implements FileMigration {

    private static final Set<String> builtIn;

    static {
        builtIn = new HashSet<String>();

        builtIn.add("org.seamcat.simulation.coverageradius.UserDefinedCoverageRadius");
        builtIn.add("org.seamcat.simulation.coverageradius.TrafficLimitedNetworkCoverageRadius");
        builtIn.add("org.seamcat.simulation.coverageradius.NoiseLimitedCoverageRadius");
        builtIn.add("org.seamcat.model.propagation.HataSE21PropagationModel");
        builtIn.add("org.seamcat.model.propagation.HataSE24PropagationModel");
        builtIn.add("org.seamcat.model.propagation.SDPropagationModel");
        builtIn.add("org.seamcat.model.propagation.P452ver14PropagationModel");
        builtIn.add("org.seamcat.model.propagation.FreeSpacePropagationModel");
        builtIn.add("org.seamcat.model.propagation.P1546ver3PropagationModel");
        builtIn.add("org.seamcat.model.propagation.R370PropagationModel");
        builtIn.add("org.seamcat.eventprocessing.DemoEPP_1_collectIntermediaryResults");
        builtIn.add("org.seamcat.eventprocessing.DemoEPP_2_developNewAlgorithm");
        builtIn.add("org.seamcat.eventprocessing.DemoEPP_3_developNewAlgorithm_checkTxPower");
        builtIn.add("org.seamcat.eventprocessing.DemoEPP_4_generate_CoverI_results");
        builtIn.add("org.seamcat.eventprocessing.DemoEPP_5_OFDMA");
        builtIn.add("org.seamcat.eventprocessing.DemoEPP_6_CellularInternals");
    }


    @Override
    public void migrate(File originalFile, File migratedFile, List<MigrationIssue> migrationIssues) {
        Document document = XmlUtils.parse(originalFile);
        migrate(document);
        XmlUtils.write(document, migratedFile);
    }

    private void migrate(Document document) {
        JXPathContext context = JXPathContext.newContext(document);
        List pms = context.selectNodes("//plugin-configuration");

        for (Object o : pms) {
            patchBuildIn((Element) o);
        }

        updateVersion(document);
    }

    public static void patchBuildIn(Element element) {
        String classname = element.getAttribute("classname");
        if ( builtIn.contains( classname) ) {
            element.setAttribute("location", "BUILT-IN");
        }
    }


    private void updateVersion(Document document) {
        JXPathContext context = JXPathContext.newContext(document);
        context.createPathAndSetValue("seamcat/@settings_format_version", getOriginalVersion().nextVersion().getNumber());
    }

    @Override
    public FormatVersion getOriginalVersion() {
        return new FormatVersion(6);
    }
}
