package org.chronusartcenter.osc;

import com.alibaba.fastjson2.JSONObject;
import com.illposed.osc.MessageSelector;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCMessageEvent;
import com.illposed.osc.OSCSerializeException;
import com.illposed.osc.transport.udp.OSCPortIn;
import com.illposed.osc.transport.udp.OSCPortOut;
import org.apache.log4j.Logger;
import org.chronusartcenter.Context;
import org.chronusartcenter.cache.CacheService;
import org.chronusartcenter.model.OscClientConfig;
import org.chronusartcenter.news.HeadlineModel;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class OscService {
    private final Context context;
    private ArrayList<OscClientConfig> oscClients;

    private ReentrantLock lock = new ReentrantLock();

    private final Logger logger = Logger.getLogger(Context.class);

    public OscService(Context context) {
        this.context = context;
        oscClients = readClientConfig(context);
    }

    public void start() {

        try {
            int port;
            JSONObject config = context.loadConfig();
            if (config.get("oscServer") == null
                    && config.getJSONObject("oscServer").getInteger("port") == null) {
                port = 4003;
            } else {
                port = config.getJSONObject("oscServer").getInteger("port");
            }
            OSCPortIn server = new OSCPortIn(port);
            logger.info("Start osc server at localhost: " + port);
            server.getDispatcher().addListener(new MessageSelector() {
                @Override
                public boolean isInfoRequired() {
                    return false;
                }

                @Override
                public boolean matches(OSCMessageEvent oscMessageEvent) {
                    String from = oscMessageEvent.getMessage().getAddress();
                    if (from.startsWith("/screen")) {
                        return true;
                    } else {
                        logger.warn("Unknown osc from: " + from);
                        return false;
                    }
                }
            }, oscMessageEvent -> {

                String from = oscMessageEvent.getMessage().getAddress();
                logger.info("Receive osc message, from: " + from);
                // screen ID comes after "/screen"
                int screenId;
                try {
                    screenId = Integer.parseInt(from.substring("/screen".length()));
                } catch (NumberFormatException e) {
                    logger.error("Failed to parse screen id from [" + from + "]");
                    return;
                }

                var params = oscMessageEvent.getMessage().getArguments();
                if ("ready".equals(params.get(0))) {
                    onReady(screenId);
                } else if ("ping".equals(params.get(0))) {
                    onPing();
                } else {
                    logger.warn("Unhandled osc parameter: " + params.get(0));
                }

            });
            server.startListening();

        } catch (IOException exception) {
            logger.error(exception.toString());
        }
    }

    public void updateOscClient() {
        lock.lock();
        try {
            oscClients = readClientConfig(context);
        } finally {
            lock.unlock();
        }

    }

    public void shutDownOscClients() {
        lock.lock();
        try {
            for (var client : oscClients) {
                try {
                    OSCPortOut oscPortOut = new OSCPortOut(InetAddress.getByName(client.getIp()), client.getPort());
                    final OSCMessage msg = new OSCMessage("/shutdown" + client.getId());
                    oscPortOut.send(msg);
                } catch (IOException | OSCSerializeException e) {
                    logger.error(e.toString());
                }
            }
        } finally {
            lock.unlock();
        }

    }

    private void onPing() {
        // notify gui that osc is online
    }

    private void onReady(int id) {
        lock.lock();
        try {
            var oscClient = oscClients.stream().filter(value -> value.getId() == id).toList();
            if (oscClient.size() == 0) {
                logger.error("id " + id + " doesn't exists!");
                return;
            } else if (oscClient.size() > 1) {
                logger.error("id " + id + " is duplicated!");
                return;
            }

            String ip = oscClient.get(0).getIp();
            int port = oscClient.get(0).getPort();
            try {
                OSCPortOut client = new OSCPortOut(InetAddress.getByName(ip), port);
                final List<Object> arg = new LinkedList<>();
                var headline = getRandomHeadline();
                if (headline == null) {
                    logger.warn("Can not get a random headline");
                    return;
                }

                logger.info("Send to Screen " + id
                        + "of socket: " + client.getRemoteAddress()
                        + " of  headline: " + JSONObject.toJSONString(headline));
                arg.add(headline.getTitle());
                arg.add("/image/" + headline.getIndex() + ".jpeg");
                final OSCMessage msg = new OSCMessage("/headline-" + id, arg);
                client.send(msg);
                logger.info("Title: " + headline.getTitle()
                        + ", image link: " + "/image/" + headline.getIndex() + ".jpeg");

                // notify GUI
                context.guiOscImageShow(id, "cache/image/" + headline.getIndex() + ".jpeg");
            } catch (IOException | OSCSerializeException exception) {
                logger.error(exception.toString());
            }
        } finally {
            lock.unlock();
        }
    }

    private HeadlineModel getRandomHeadline() {
        CacheService cacheService = new CacheService(context);
        var headlines = cacheService.loadHeadlines();

        Random rand = new Random(System.currentTimeMillis());
        return headlines.get(rand.nextInt(headlines.size()));
    }

    public ArrayList<OscClientConfig> readClientConfig(Context context) {
        ArrayList<OscClientConfig> result = new ArrayList<>();
        var clientsJson = context.loadConfig().getJSONArray("oscClient");
        if (clientsJson == null) {
            logger.error("No config of osc clients.");
            return result;
        }

        for (Object clientConfig : clientsJson) {
            if (!(clientConfig instanceof JSONObject)) {
                logger.warn("Invalid osc clients config: " + clientConfig.toString());
                break;
            }

            int id = ((JSONObject) clientConfig).getIntValue("id");
            String ip = ((JSONObject) clientConfig).getString("ip");
            int port = ((JSONObject) clientConfig).getIntValue("port");
            try {
                var oscClientConfig = new OscClientConfig(id, ip, port);
                result.add(oscClientConfig);
            } catch (IllegalArgumentException exception) {
                logger.error(exception);
                break;
            }
        }

        return result;
    }

    public void saveClientConfig(Context context, ArrayList<OscClientConfig> oscClientConfigs) {
        if (oscClientConfigs == null || oscClientConfigs.isEmpty()) {
            logger.error("oscClientConfigs is null or empty");
            return;
        }

        var configJson = context.loadConfig();

        if (configJson == null) {
            logger.error("Fail to load configs");
            return;
        }

        configJson.put("oscClient", oscClientConfigs);
        try {
            context.saveConfig(configJson);
        } catch (IOException e) {
            logger.error(e.toString());
        }

        updateOscClient();
        shutDownOscClients();
    }
}
