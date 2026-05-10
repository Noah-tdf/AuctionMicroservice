package com.ryannoah.auction.utilities;

import org.springframework.http.HttpStatus;

public class DownstreamServiceException extends RuntimeException {

    private final HttpStatus status;
    private final String path;

    public DownstreamServiceException(HttpStatus status, String message, String path) {
        super(message);
        this.status = status;
        this.path = path;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getPath() {
        return path;
    }
}
