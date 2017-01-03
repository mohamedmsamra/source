package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.function.DiscreteFunction;
import org.seamcat.migration.FormatVersion;
import org.seamcat.model.MigrationIssue;
import org.seamcat.model.functions.Point2D;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class AntennaDMAPatternWorkspaceMigration extends AbstractScenarioMigration {

    @Override
    void migrateScenarioDocument(Document document) {

        JXPathContext context = JXPathContext.newContext(document);
        Element ws = (Element) context.selectNodes("//Workspace").get(0);

        List systems = context.selectNodes("//system");

        for (Object sys : systems) {
            Element system = (Element) sys;
            Element composite = (Element) system.getFirstChild();
            String clazz = composite.getAttribute("class");
            if ( clazz.equals("org.seamcat.model.systems.cdma.SystemModelCDMAUpLink") ||
                    clazz.equals("org.seamcat.model.systems.cdma.SystemModelCDMADownLink") ||
                    clazz.equals("org.seamcat.model.systems.ofdma.SystemModelOFDMAUpLink") ||
                    clazz.equals("org.seamcat.model.systems.ofdma.SystemModelOFDMADownLink")) {

                Element desc = (Element) composite.getFirstChild();
                String name = desc.getAttribute("name");

                Element bs = (Element) composite.getElementsByTagName("baseStation").item(0);
                NodeList horizontals = bs.getElementsByTagName("horizontal");

                if ( horizontals.getLength() > 0 ) {
                    // check if this is according to standard
                    Element item = (Element) horizontals.item(0);
                    NodeList pts = item.getElementsByTagName("point2d");

                    List<Point2D> points = new ArrayList<>();
                    for ( int i=0; i<pts.getLength(); i++) {
                        Element pElement = (Element) pts.item(i);
                        points.add( new Point2D( Double.parseDouble(pElement.getAttribute("x")),
                                Double.parseDouble(pElement.getAttribute("y"))));
                    }

                    double min = points.get(0).getX();
                    double max = points.get( points.size()-1).getX();
                    if (min != 0.0 || max != 360.0) {
                        if (min == -180.0 && max == 180.0) {
                            getMigrationIssues().add(new MigrationIssue("System '"+name+"' contained a horizontal antenna pattern in the range -180 to 180. This has been converted to range 0 to 360"));
                            List<Point2D> converted = convertFrom180180To0360(points);

                            Node discreteFunction = item.getFirstChild().getFirstChild();
                            while (discreteFunction.hasChildNodes()) {
                                discreteFunction.removeChild( discreteFunction.getFirstChild());
                            }

                            for (Point2D point : converted) {
                                Element point2d = document.createElement("point2d");
                                point2d.setAttribute("x", Double.toString(point.getX()));
                                point2d.setAttribute("y", Double.toString(point.getY()));
                                discreteFunction.appendChild(point2d);
                            }

                        }
                    }
                }
            }
        }

        updateVersion(document);
    }

    @Override
    void migrateResultsDocument(Document document) {
        // nothing to do here
    }

    private void updateVersion(Document document) {
        JXPathContext context = JXPathContext.newContext(document);
        context.createPathAndSetValue("Workspace/@workspace_format_version", getOriginalVersion().nextVersion().getNumber());
    }

    @Override
    public FormatVersion getOriginalVersion() {
        return new FormatVersion(45);
    }


    public static List<Point2D> convertFrom180180To0360( List<Point2D> points ) {
        List<Point2D> converted = new ArrayList<Point2D>();
        for (Point2D p : points) {
            if ( p.getX() < -179.999 ) continue;

            if ( p.getX() < 0 ) {
                converted.add( new Point2D( p.getX() + 360, p.getY()));
            } else {
                converted.add( p );
            }
            if ( Math.abs(p.getX()) < 0.001 ) {
                converted.add( new Point2D( 360, p.getY()));
            }
        }
        DiscreteFunction result = new DiscreteFunction(converted);
        result.sortPoints();
        return result.getPoints();
    }

}
