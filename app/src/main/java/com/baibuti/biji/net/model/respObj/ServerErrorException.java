package com.baibuti.biji.net.model.respObj;

import java.util.Locale;

public class ServerErrorException extends Exception {

    private String errorTitle;
    private String errorDetail;
    private int errorCode;

    public ServerErrorException(String errorTitle, String errorDetail, int errorCode) {
        super(String.format(Locale.CHINA, "%d: %s, %s", errorCode, errorTitle, errorDetail));
        this.errorTitle = errorTitle;
        this.errorDetail = errorDetail;
        this.errorCode = errorCode;
    }

    public String getErrorTitle() {
        return errorTitle;
    }

    public String getErrorDetail() {
        return errorDetail;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        // TODO
        return String.format(Locale.CHINA, "(%d) %s: %s", errorCode, errorTitle, errorDetail);
    }
}