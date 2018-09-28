package com.huawei.smart.server.validator;

import android.view.View;



/**
 * Created by DuoQi on 2018-02-23.
 */
public class ValidationException extends Exception {

    private View source;

    /**
     * Constructs an Exception with no specified detail message.
     */
    public ValidationException() {
        super();
    }

    /**
     * Constructs an Exception with the specified detail message.
     *
     * @param message The error message.
     */
    public ValidationException(String message) {
        super(message);
    }


    public ValidationException(View source, String message) {
        super(message);
        this.source = source;
    }

    public View getSource() {
        return source;
    }

}