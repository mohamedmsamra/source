package org.seamcat.migration.batch;

import org.seamcat.migration.FormatVersion;


/** Represents versions of the batch file storage format.<br>
 * The format versions are numbered sequentially, with the version number stored in the settings file. <br>
 *     To cater for versions before introduction of the format version number, the pseudo version PREHISTORIC is used.
 */
public class BatchFormatVersionConstants {
	public static final FormatVersion PREHISTORIC = new FormatVersion(-1);
	public static final FormatVersion CURRENT_VERSION = new FormatVersion(2);
}
