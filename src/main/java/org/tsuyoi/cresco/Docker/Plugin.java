package org.tsuyoi.cresco.Docker;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Image;
import io.cresco.library.agent.AgentService;
import io.cresco.library.messaging.MsgEvent;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.plugin.PluginService;
import io.cresco.library.utilities.CLogger;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.*;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

@Component(
        service = { PluginService.class },
        scope= ServiceScope.PROTOTYPE,
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        servicefactory = true,
        reference= { @Reference(name="io.cresco.library.agent.AgentService", service= AgentService.class) }
)
public class Plugin implements PluginService {
    public BundleContext context;
    public static PluginBuilder pluginBuilder;
    private CLogger logger;
    private Map<String,Object> map;

    private Timer heartBeatTimer;

    @Activate
    void activate(BundleContext context, Map<String,Object> map) {
        this.context = context;
        this.map = map;
    }

    @Modified
    void modified(BundleContext context, Map<String,Object> map) {
        System.out.println("Modified Config Map PluginID:" + map.get("pluginID"));
    }

    @Deactivate
    void deactivate(BundleContext context, Map<String,Object> map) {
        if(this.context != null) {
            this.context = null;
        }
        if(this.map != null) {
            this.map = null;
        }
    }

    @Override
    public boolean isActive() {
        return pluginBuilder.isActive();
    }

    @Override
    public void setIsActive(boolean isActive) {
        pluginBuilder.setIsActive(isActive);
    }

    @Override
    public boolean inMsg(MsgEvent incoming) {
        pluginBuilder.msgIn(incoming);
        return true;
    }

    @Override
    public boolean isStarted() {
        try {
            if (pluginBuilder == null) {
                pluginBuilder = new PluginBuilder(this.getClass().getName(), context, map);
                this.logger = pluginBuilder.getLogger(Plugin.class.getName(), CLogger.Level.Info);
                pluginBuilder.setExecutor(new ExecutorImpl(pluginBuilder));

                while (!pluginBuilder.getAgentService().getAgentState().isActive()) {
                    logger.info("Plugin " + pluginBuilder.getPluginID() + " waiting on Agent Init");
                    Thread.sleep(1000);
                }
                logger.info("Setting isActive");
                pluginBuilder.setIsActive(true);

                logger.info("Starting");
                heartBeatTimer = new Timer();
                heartBeatTimer.schedule(new ProcessorPluginHeartBeat(), 5000);
            }
            return true;
        } catch(Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isStopped() {
        if (heartBeatTimer != null)
            heartBeatTimer.cancel();
        if (pluginBuilder != null) {
            pluginBuilder.setExecutor(null);
            pluginBuilder.setIsActive(false);
        }
        return true;
    }

    public void sendHeartBeat() {
        logger.info("Checking for image");
        String imageName = "ubuntu:14.04";
        try (DockerClient tmpDockerClient = DefaultDockerClient.fromEnv().build()) {
            List<Image> dockerImages = tmpDockerClient.listImages(DockerClient.ListImagesParam.byName(imageName));
            logger.info("Docker image [{}] exists: {}", imageName);
            dockerImages.forEach(i -> { logger.info("Image: {}", String.join(";", i.repoTags())); });
        } catch (DockerCertificateException e) {
            logger.error("Docker certificates improperly configured on this machine");
        } catch (DockerException e) {
            logger.error("Docker exception encountered: {}", e.getMessage());
        } catch (InterruptedException e) {
            logger.error("Docker command interrupted");
        }
    }

    class ProcessorPluginHeartBeat extends TimerTask {
        ProcessorPluginHeartBeat() { }
        public void run() {
            sendHeartBeat();
        }
    }
}
