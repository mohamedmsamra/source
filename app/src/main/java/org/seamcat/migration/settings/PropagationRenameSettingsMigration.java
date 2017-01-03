package org.seamcat.migration.settings;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FileMigration;
import org.seamcat.migration.FormatVersion;
import org.seamcat.model.MigrationIssue;
import org.seamcat.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropagationRenameSettingsMigration implements FileMigration {

    @Override
    public void migrate(File originalFile, File migratedFile, List<MigrationIssue> migrationIssues) {
        Document document = XmlUtils.parse(originalFile);
        migrate(document);
        XmlUtils.write(document, migratedFile);
    }

    private void migrate(Document document) {
        JXPathContext context = JXPathContext.newContext(document);
        List pms = context.selectNodes("//pluginConfiguration");

        for (Object o : pms) {
            patchBuildIn((Element) o);
        }

        updateVersion(document);
    }

    public static void patchBuildIn(Element element) {
        String classname = element.getAttribute("classname");
        if ( "org.seamcat.model.propagation.R370PropagationModel".equals(classname) ) {
            element.removeAttribute("classname");
            element.setAttribute("classname", "org.seamcat.model.propagation.P1546ver1PropagationModel");
        }
        if ( "org.seamcat.model.propagation.P1546ver3PropagationModel".equals(classname)) {
            element.removeAttribute("classname");
            element.setAttribute("classname", "org.seamcat.model.propagation.P1546ver4PropagationModel");
        }

        String name = names.get(element.getAttribute("classname"));
        if ( name != null ) {
            element.setAttribute("name", name);
        }
    }


    private static Map<String, String> names = new HashMap<>();

    static {
        names.put("org.seamcat.model.propagation.HataSE21PropagationModel", "Extended Hata");
        names.put("org.seamcat.model.propagation.HataSE24PropagationModel", "Extended Hata - SRD");
        names.put("org.seamcat.model.propagation.FreeSpacePropagationModel", "Free Space (ITU-R P.525)");
        names.put("org.seamcat.model.propagation.LongleyRice_mod", "Longley Rice");
        names.put("org.seamcat.model.propagation.Model_C_IEEE_802_11_rev3", "Model C IEEE 802.11 rev3");
        names.put("org.seamcat.model.propagation.P452ver14PropagationModel", "ITU-R P.452-14");
        names.put("org.seamcat.model.propagation.P1411LowAntennaHeight", "ITU-R P.1411 Low Antenna Height");
        names.put("org.seamcat.model.propagation.P1546ver1PropagationModel", "ITU-R P.1546-1 Annex 8");
        names.put("org.seamcat.model.propagation.P1546ver4PropagationModel", "ITU-R P.1546-4 land");
        names.put("org.seamcat.model.propagation.SDPropagationModel", "Spherical Diffraction (ITU-R P.526-2)");
    }

    private void updateVersion(Document document) {
        JXPathContext context = JXPathContext.newContext(document);
        context.createPathAndSetValue("seamcat/@settings_format_version", getOriginalVersion().nextVersion().getNumber());
    }

    @Override
    public FormatVersion getOriginalVersion() {
        return new FormatVersion(9);
    }
}
