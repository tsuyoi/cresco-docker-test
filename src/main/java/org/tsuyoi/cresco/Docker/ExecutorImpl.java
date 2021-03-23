package org.tsuyoi.cresco.Docker;

import io.cresco.library.messaging.MsgEvent;
import io.cresco.library.plugin.Executor;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.utilities.CLogger;

public class ExecutorImpl implements Executor {
    private final PluginBuilder pluginBuilder;
    private final CLogger logger;

    public ExecutorImpl(PluginBuilder pluginBuilder) {
        this.pluginBuilder = pluginBuilder;
        this.logger = pluginBuilder.getLogger(ExecutorImpl.class.getName(), CLogger.Level.Info);
    }

    @Override
    public MsgEvent executeCONFIG(MsgEvent msgEvent) {
        return null;
    }

    @Override
    public MsgEvent executeDISCOVER(MsgEvent msgEvent) {
        return null;
    }

    @Override
    public MsgEvent executeERROR(MsgEvent msgEvent) {
        return null;
    }

    @Override
    public MsgEvent executeINFO(MsgEvent msgEvent) {
        return null;
    }

    @Override
    public MsgEvent executeEXEC(MsgEvent msgEvent) {
        return null;
    }

    @Override
    public MsgEvent executeWATCHDOG(MsgEvent msgEvent) {
        return null;
    }

    @Override
    public MsgEvent executeKPI(MsgEvent msgEvent) {
        return null;
    }
}
