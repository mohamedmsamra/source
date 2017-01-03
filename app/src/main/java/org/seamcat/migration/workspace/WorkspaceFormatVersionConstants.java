package org.seamcat.migration.workspace;

import org.seamcat.migration.FormatVersion;



/** Represents versions of the workspace file storage format.
 * The format versions are numbered sequentially, with the version
 * number stored in the workspace xml. To cater for workspace versions
 * before introduction of the workspace version number, there are a number of pseudo 
 * versions:
 * <ul>
 * 	<li>PREHISTORIC: No version info present, no migrations will take place, reject the workspace</li>
 * 	<li>PRE_3_2_3: Only seamcat_version info present. Before seamcat version 3.2.3</li>
 * 	<li>POST_3_2_3: Only seamcat_version info present. After seamcat version 3.2.3, but
 * 		before introduction of the workspace_format_version</li>
 * 	<li>numbered: workspace_format_version present in XML. Numbering starts at 2.</li>
 * </ul>
 */
public class WorkspaceFormatVersionConstants {
	public static final FormatVersion PREHISTORIC = new FormatVersion(-1);
	public static final FormatVersion PRE_3_2_3 = new FormatVersion(0);
	public static final FormatVersion POST_3_2_3 = new FormatVersion(1);
	public static final FormatVersion CURRENT_VERSION = new FormatVersion(49);
}
