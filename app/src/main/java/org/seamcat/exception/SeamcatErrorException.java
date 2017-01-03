package org.seamcat.exception;

public class SeamcatErrorException extends RuntimeException {

    public SeamcatErrorException( String message ) {
        super(message);
    }
}
