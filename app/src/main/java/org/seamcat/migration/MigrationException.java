package org.seamcat.migration;


public class MigrationException extends RuntimeException {

	public MigrationException() {
	   super();
   }

	public MigrationException(String message, Throwable cause) {
	   super(message, cause);
   }

	public MigrationException(String message) {
	   super(message);
   }

	public MigrationException(Throwable cause) {
	   super(cause);
   }
}
