package org.seamcat.migration.settings;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FileMigration;
import org.seamcat.migration.FormatVersion;
import org.seamcat.model.MigrationIssue;
import org.seamcat.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.util.List;

public class SensingSettingsMigration implements FileMigration {

    @Override
    public void migrate(File originalFile, File migratedFile, List<MigrationIssue> migrationIssues) {
        Document document = XmlUtils.parse(originalFile);
        migrate(document);
        XmlUtils.write(document, migratedFile);
    }

    private void migrate(Document document) {
        JXPathContext context = JXPathContext.newContext(document);
        List chars = context.selectNodes("//emissionCharacteristics");
        for (Object o : chars) {
            Element emissions = (Element) o;

            appendSensing( emissions, document );
        }

        updateVersion(document);
    }


    public static void appendSensing( Element emissions, Document document) {
        emissions.setAttribute("probabilityOfFailure", "0.0");
        emissions.setAttribute("receptionBandwidth", "200.0");

        Element detectionThreshold = document.createElement("detectionThreshold");
        Element fun = document.createElement("ConstantFunction");
        fun.setAttribute("value", "0.0");
        detectionThreshold.appendChild(fun);
        emissions.appendChild(detectionThreshold);

        Element eirp = document.createElement("eirpMax");
        Element mask = document.createElement("spectrum-emission-mask");
        mask.setAttribute("description", "");
        mask.setAttribute("reference", "Spectrum Emission Mask");
        Element disc = document.createElement("discretefunction2");
        disc.appendChild(point(document, "-100.0", "0.0", "1250.0"));
        disc.appendChild(point(document, "-1.0", "0.0", "1250.0"));
        disc.appendChild(point(document, "1.0", "0.0", "1250.0"));
        disc.appendChild(point(document, "100.0", "0.0", "1250.0"));
        mask.appendChild( disc );
        eirp.appendChild( mask );
        emissions.appendChild( eirp );

        Element pm = document.createElement("propagationModel");
        Element conf = document.createElement("pluginConfiguration");
        conf.setAttribute("adjacentFloorLoss","18.3");
        conf.setAttribute("classname", "org.seamcat.model.propagation.HataSE21PropagationModel");
        conf.setAttribute("description", "");
        conf.setAttribute("empiricalParameters", "0.46");
        conf.setAttribute("floorHeight", "3.0");
        conf.setAttribute("generalEnvironment", "0");
        conf.setAttribute("location", "BUILT-IN");
        conf.setAttribute("name", "Extended Hata");
        conf.setAttribute("propagationEnvironment", "0");
        conf.setAttribute("sizeOfRoom", "4.0");
        conf.setAttribute("variation", "true");
        conf.setAttribute("wallLossInIn", "5.0");
        conf.setAttribute("wallLossStdDev", "10.0");
        pm.appendChild( conf );
        emissions.appendChild( pm );
    }

    private static Element point( Document document, String x, String y, String z) {
        Element point = document.createElement("point3d");
        point.setAttribute("x", x);
        point.setAttribute("y", y);
        point.setAttribute("z", z);
        return point;
    }

    private void updateVersion(Document document) {
        JXPathContext context = JXPathContext.newContext(document);
        context.createPathAndSetValue("seamcat/@settings_format_version", getOriginalVersion().nextVersion().getNumber());
    }

    @Override
    public FormatVersion getOriginalVersion() {
        return new FormatVersion(15);
    }
}
