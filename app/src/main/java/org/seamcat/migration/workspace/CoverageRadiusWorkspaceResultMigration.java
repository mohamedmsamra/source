package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;

public class CoverageRadiusWorkspaceResultMigration extends AbstractScenarioMigration {


	@Override
	void migrateScenarioDocument(Document document) {
		updateVersion(document);
	}

	@Override
	void migrateResultsDocument(Document document) {
        JXPathContext context = JXPathContext.newContext(document);
        List list = context.selectNodes("//workspaceResults");
        if ( list.size() > 0 ) {
            Element results = (Element) list.get(0);
            Element seamcatResults = document.createElement("SEAMCATResults");
            results.appendChild( seamcatResults );

            Element result = document.createElement("SEAMCATResult");
            seamcatResults.appendChild( result );
            result.setAttribute("name", "Calculated Radius");

            Element singleValues = document.createElement("SingleValues");
            result.appendChild( singleValues );
            result.appendChild( document.createElement("VectorGroups"));
            result.appendChild( document.createElement("VectorValues"));
            result.appendChild( document.createElement("ScatterPlots"));
            result.appendChild( document.createElement("BarCharts"));
            result.appendChild( document.createElement("CustomResults"));

            List cr = context.selectNodes("//CoverageRadius");
            for (Object o : cr) {
                Element oldCR = (Element) o;
                Element single = document.createElement("Single");
                single.setAttribute("unit", "km");
                single.setAttribute("type", "double");
                single.setAttribute("name", oldCR.getAttribute("name"));
                single.setAttribute("value", oldCR.getAttribute("value"));
                singleValues.appendChild( single );
            }

            removeNode( context, "//CoverageRadiuses" );
        }
	}

    private void removeNode( JXPathContext context, String searchPath ) {
        List blocking = context.selectNodes( searchPath );
        for (Object o : blocking) {
            if ( o instanceof Node) {
                ((Node) o).getParentNode().removeChild((Node) o );
            }
        }
    }

	private void updateVersion(Document document) {
	   JXPathContext context = JXPathContext.newContext(document);
	   context.createPathAndSetValue("Workspace/@workspace_format_version", getOriginalVersion().nextVersion().getNumber());
   }

	@Override
   public FormatVersion getOriginalVersion() {
	   return new FormatVersion(19);
   }


}
