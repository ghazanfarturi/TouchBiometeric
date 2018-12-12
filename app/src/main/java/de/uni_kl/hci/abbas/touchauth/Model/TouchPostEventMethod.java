package de.uni_kl.hci.abbas.touchauth.Model;

import java.util.concurrent.Callable;

public class TouchPostEventMethod implements Callable<Void> {
    protected TouchEvent event;
    protected StringBuilder sb;

    public void setParam(TouchEvent event, StringBuilder sb) {
        this.event = event;
        this.sb = sb;
    }

    @Override
    public Void call() throws Exception {
        return null;
    }
}
