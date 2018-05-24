package com.mayunfei.downloadmanager.download;

import java.util.Observable;
import java.util.Observer;

public abstract class StatusChangeListener implements Observer {
    @Override
    public void update(Observable o, Object arg) {
        DownEvent downEvent = (DownEvent) arg;
        statusChange(downEvent);
    }

    protected abstract void statusChange(DownEvent downEvent);
}