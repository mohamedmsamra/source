package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class SignalsWorkspaceResultMigration extends AbstractScenarioMigration {

    private Collection<String> vectors = Arrays.asList("iRSSVectorListBlocking", "iRSSVectorListIntermodulation", "iRSSVectorListUnwanted", "iRSSVectorListOverloading");


	@Override
	void migrateScenarioDocument(Document document) {
		updateVersion(document);
	}

	@Override
	void migrateResultsDocument(Document document) {
        JXPathContext context = JXPathContext.newContext(document);
        List list = context.selectNodes("//SEAMCATResults");
        if ( list.size() > 0 ) {
            Element results = (Element) list.get(0);

            Element signals = (Element) context.selectSingleNode("//Signals");

            NodeList childNodes = signals.getChildNodes();
            for ( int i=0; i<childNodes.getLength(); i++ ) {
                Element vector = (Element) childNodes.item(i);
                if ( vector.getNodeName().equals("DRSSVector")) {
                    Element migratedResult = document.createElement("SEAMCATResult");
                    migratedResult.setAttribute("name", vector.getAttribute("name"));
                    Element vectorValues = appendEmptyGroups(document, migratedResult);

                    Element values = appendValuesElement(document, vectorValues );
                    NodeList valuesList = vector.getFirstChild().getChildNodes();
                    if ( valuesList.getLength() > 0 ) {
                        for ( int j=0; j<valuesList.getLength(); j++) {
                            Element event = (Element) valuesList.item(j);
                            Element value = document.createElement("value");
                            value.setAttribute("v", event.getAttribute("y"));
                            values.appendChild( value );
                        }

                        results.appendChild( migratedResult );
                    }

                } else if ( vectors.contains( vector.getNodeName() ) ) {
                    vector = (Element) vector.getFirstChild();
                    Element migratedResult = document.createElement("SEAMCATResult");
                    migratedResult.setAttribute("name", vector.getAttribute("name"));
                    Element vectorValues = appendEmptyGroups(document, migratedResult);

                    Element vectorList = (Element) vector.getFirstChild();
                    NodeList subVectors = vectorList.getChildNodes();
                    for ( int j=0; j<subVectors.getLength(); j++) {
                        Element sub = (Element) subVectors.item(j);

                        Element values = appendValuesElement(document, vectorValues );

                        Element actualVector = (Element) sub.getFirstChild().getFirstChild();
                        if ( actualVector.hasChildNodes() ) {
                            NodeList valueList = actualVector.getChildNodes();
                            for ( int k=0; k< valueList.getLength(); k++) {
                                Element v = (Element) valueList.item(k);
                                Element value = document.createElement("value");
                                value.setAttribute("v", v.getAttribute("y"));
                                values.appendChild( value );
                            }

                            results.appendChild( migratedResult );
                        }
                    }
                }

            }

            removeNode(context, "//Signals");
        }
	}

    private Element appendEmptyGroups(Document document, Element result) {
        result.appendChild( document.createElement("SingleValues"));
        result.appendChild( document.createElement("VectorGroups"));
        Element vectorValues = document.createElement("VectorValues");
        result.appendChild(vectorValues);
        result.appendChild( document.createElement("ScatterPlots"));
        result.appendChild( document.createElement("BarCharts"));
        result.appendChild( document.createElement("CustomResults"));
        return vectorValues;
    }

    private Element appendValuesElement(Document document, Element vectorValues ) {
        Element vectorElement = document.createElement("Vector");
        vectorElement.setAttribute("name", "dRSS vector");
        vectorElement.setAttribute("unit", "dBm");
        Element values = document.createElement("values");
        vectorElement.appendChild( values );
        vectorValues.appendChild( vectorElement );
        return values;
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
	   return new FormatVersion(20);
   }


}
