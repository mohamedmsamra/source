package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

public class PropagationModelHata24AndSDWorkspaceMigration extends AbstractScenarioMigration {

    @Override
    void migrateScenarioDocument(Document document) {
        updateVersion(document);

        JXPathContext context = JXPathContext.newContext(document);
        List pms = context.selectNodes("//plugin-configuration");

        for (Object o : pms) {
            migratePropagationModel((Element) o);
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
        return new FormatVersion(13);
    }


    public static void migratePropagationModel(Element node) {
        if ( node.getAttribute("classname").equals("org.seamcat.model.propagation.HataSE24PropagationModel") ) {
            // move param 3, 4, 8, 9 as last
            // rearrange param 3 to 13
            String wl1 = removeAttr(node, "param3");
            String wl2 = removeAttr(node, "param4");
            String wl3 = removeAttr(node, "param8");
            String wl4 = removeAttr(node, "param9");

            String n3 = removeAttr(node, "param5");
            String n4 = removeAttr(node, "param6");
            String n5 = removeAttr(node, "param7");
            String n6 = removeAttr(node, "param10");
            String n7 = removeAttr(node, "param11");
            String n8 = removeAttr(node, "param12");
            String n9 = removeAttr(node, "param13");

            node.setAttribute("param3", n3);
            node.setAttribute("param4", n4);
            node.setAttribute("param5", n5);
            node.setAttribute("param6", n6);
            node.setAttribute("param7", n7);
            node.setAttribute("param8", n8);
            node.setAttribute("param9", n9);

            node.setAttribute("param10", wl1);
            node.setAttribute("param11", wl2);
            node.setAttribute("param12", wl3);
            node.setAttribute("param13", wl4);
        } else if ( node.getAttribute("classname").equals("org.seamcat.model.propagation.SDPropagationModel")) {
            String wl1 = removeAttr(node, "param2");
            String wl2 = removeAttr(node, "param3");
            String wl3 = removeAttr(node, "param6");
            String wl4 = removeAttr(node, "param7");

            String n1 = removeAttr(node, "param4");
            String n2 = removeAttr(node, "param5");
            String n3 = removeAttr(node, "param8");
            String n4 = removeAttr(node, "param9");
            String n5 = removeAttr(node, "param10");
            String n6 = removeAttr(node, "param11");
            String n7 = removeAttr(node, "param12");
            String n8 = removeAttr(node, "param13");
            String n9 = removeAttr(node, "param14");
            String n10= removeAttr(node, "param15");
            String n11= removeAttr(node, "param16");

            node.setAttribute("param2", n1);
            node.setAttribute("param3", n2);
            node.setAttribute("param4", n3);
            node.setAttribute("param5", n4);
            node.setAttribute("param6", n5);
            node.setAttribute("param7", n6);
            node.setAttribute("param8", n7);
            node.setAttribute("param9", n8);
            node.setAttribute("param10", n9);
            node.setAttribute("param11",n10);
            node.setAttribute("param12",n11);

            node.setAttribute("param13", wl1);
            node.setAttribute("param14", wl2);
            node.setAttribute("param15", wl3);
            node.setAttribute("param16", wl4);

        }

    }

    private static String removeAttr( Element node, String att ) {
        String attribute = node.getAttribute(att);
        node.removeAttribute(att);
        return attribute;
    }

}
