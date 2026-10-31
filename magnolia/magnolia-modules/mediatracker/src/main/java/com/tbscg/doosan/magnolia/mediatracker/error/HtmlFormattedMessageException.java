package com.tbscg.doosan.magnolia.mediatracker.error;

/**
 * Marker exception class. Assumption is that message for exception will be formatted with html tags.
 */
@SuppressWarnings("unused")
public class HtmlFormattedMessageException extends RuntimeException {
    public HtmlFormattedMessageException() {
    }

    public HtmlFormattedMessageException(String message) {
        super(message);
    }

    public HtmlFormattedMessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public HtmlFormattedMessageException(Throwable cause) {
        super(cause);
    }
}
