package org.minezy.android;

import android.app.Application;

import java.util.Arrays;
import java.util.List;

import dagger.ObjectGraph;

public class MinezyApplication extends Application {
    private ObjectGraph mGraph;

    @Override
    public void onCreate() {
        super.onCreate();

        mGraph = ObjectGraph.create(getModules().toArray());
    }

    protected List<Object> getModules() {
        return Arrays.asList(
            (Object) new MinezyModule(this)
        );
    }


    public ObjectGraph getObjectGraph() {
        return mGraph;
    }

    public void inject(Object object) {
        mGraph.inject(object);
    }
}
