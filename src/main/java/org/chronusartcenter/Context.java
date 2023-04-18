package org.chronusartcenter;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CopyOnWriteArrayList;

public class Context {
    public static final String CONFIG_FILE_PATH = "src/main/resources/config.json";

    private CopyOnWriteArrayList<GuiListener> guiListeners;
    private Logger logger = Logger.getLogger(Context.class);

    public Context() {
        this.guiListeners = new CopyOnWriteArrayList<>();
    }

    public JSONObject loadConfig() {
        try {
            Path path = Path.of(CONFIG_FILE_PATH);
            String content = Files.readString(path);
            JSONObject config = JSONObject.parseObject(content);
            return config;
        } catch (IOException exception) {
            logger.error(exception.toString());
            return null;
        }
    }

    public void saveConfig(JSONObject config) throws IOException {
        try {
            String content = config.toJSONString(JSONWriter.Feature.PrettyFormat);
            Path path = Path.of(CONFIG_FILE_PATH);
            Files.writeString(path, content);
        } catch (IOException exception) {
            logger.error(exception.toString());
        }
    }

    public interface GuiListener {
        void onMessage(@NotNull String message);

        void onOscImage(int oscId, @NotNull String imagePath);
    }

    public void addGuiListener(GuiListener listener) {
        if (listener == null || guiListeners.contains(listener)) {
            return;
        }

        guiListeners.add(listener);
    }

    public void removeGuiListener(GuiListener listener) {
        if (listener == null || !guiListeners.contains(listener)) {
            return;
        }

        guiListeners.remove(listener);
    }

    public void guiConsolePrint(String message) {
        for (var listener : guiListeners) {
            listener.onMessage(message);
        }
    }

    public void guiOscImageShow(int id, String imagePath) {
        for (var listener : guiListeners) {
            listener.onOscImage(id, imagePath);
        }
    }
}
