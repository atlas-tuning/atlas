package com.github.manevolent.atlas.ui;


import com.github.manevolent.atlas.logging.Log;

import java.util.logging.Level;

/**
 * See: https://stackoverflow.com/questions/95767/how-can-i-catch-awt-thread-exceptions-in-java
 */
public class AwtExceptionHandler {

    /**
     * WARNING: Don't change the signature of this method!
     */
    public void handle(Throwable throwable) {
        Log.get().log(Level.WARNING, "Uncaught AWT exception", throwable);
    }

}