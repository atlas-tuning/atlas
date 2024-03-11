package com.github.manevolent.atlas.ui.util;

public class Job {

    public static Thread fork(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.start();
        return thread;
    }

}
