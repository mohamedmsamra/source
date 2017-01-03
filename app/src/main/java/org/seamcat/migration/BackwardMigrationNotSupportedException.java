package org.seamcat.migration;


public class BackwardMigrationNotSupportedException extends MigrationException {

	public BackwardMigrationNotSupportedException() {
	   super();
   }

	public BackwardMigrationNotSupportedException(String message, Throwable cause) {
	   super(message, cause);
   }

	public BackwardMigrationNotSupportedException(String message) {
	   super(message);
   }

	public BackwardMigrationNotSupportedException(Throwable cause) {
	   super(cause);
   }

}
