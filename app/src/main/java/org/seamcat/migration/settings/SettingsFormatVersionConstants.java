package org.seamcat.migration.settings;

import org.seamcat.migration.FormatVersion;



/** Represents versions of the settings file storage format.
 * The format versions are numbered sequentially, with the version
 * number stored in the settings file. To cater for versions
 * before introduction of the format version number, the pseudo version PREHISTORIC
 * is used.
 */
public class SettingsFormatVersionConstants {
	public static final FormatVersion PREHISTORIC = new FormatVersion(-1);
	public static final FormatVersion CURRENT_VERSION = new FormatVersion(18);
}
