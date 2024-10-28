package org.chronusartcenter.cache;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.chronusartcenter.Context;
import org.chronusartcenter.news.HeadlineModel;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.alibaba.fastjson2.JSONWriter.Feature.PrettyFormat;

public class CacheService {
    private final Context context;

    private Logger logger = Logger.getLogger(CacheService.class);

    final private int HEADLINE_CACHE_LIMIT = 30;

    public CacheService(Context context) {
        this.context = context;
    }

    // return the index of the headline
    // -1: error occurs or duplicated headline
    public int saveHeadline(HeadlineModel insertHeadline) {
        if (insertHeadline == null) {
            return -1;
        }

        var headlineList = loadHeadlines();
        if (headlineList == null) {
            var index = 0;
            insertHeadline.setIndex(index);
            var newHeadlineList = new ArrayList<>(List.of(insertHeadline));
            saveHeadlineList(newHeadlineList);
            return index;
        }

        // check if duplicated
        var duplicatedHeadline = headlineList.stream().filter(headline -> headline.getTitle().equals(insertHeadline.getTitle()));
        if (duplicatedHeadline.findFirst().isPresent()) {
            return -1;
        }

        // check if the cache number exceeds the limitation
        if (headlineList.size() < HEADLINE_CACHE_LIMIT) {
            var index = headlineList.size();
            headlineList.add(insertHeadline);
            insertHeadline.setIndex(index);
            saveHeadlineList(headlineList);
            return index;
        }

        // pick up a headline randomly
        // and copy it to HEADLINE_CACHE_LIMIT
        // then insert the new headline at the picked
        Random random = new Random(System.currentTimeMillis());
        var randomIndex = random.nextInt(HEADLINE_CACHE_LIMIT);
        if (headlineList.size() == HEADLINE_CACHE_LIMIT) {
            headlineList.add(headlineList.get(randomIndex));
        } else {
            headlineList.set(HEADLINE_CACHE_LIMIT, headlineList.get(randomIndex));
        }
        insertHeadline.setIndex(randomIndex);
        headlineList.set(randomIndex, insertHeadline);
        saveHeadlineList(headlineList);

        return insertHeadline.getIndex();
    }

    public void removeHeadline(int index) {
        if (index < 0) {
            logger.error("removeHeadline, invalid index of " + index);
            return;
        }

        var headlineList = loadHeadlines();
        if (index >= headlineList.size()) {
            logger.error("removeHeadline, invalid index of " + index + ", and the size of list is " + headlineList.size());
            return;
        }

        if (index <= headlineList.size() - 1) {
            headlineList.remove(index);
            saveHeadlineList(headlineList);
            return;
        }

        if (headlineList.size() > HEADLINE_CACHE_LIMIT) {
            headlineList.set(index, headlineList.get(HEADLINE_CACHE_LIMIT));
            headlineList.remove(HEADLINE_CACHE_LIMIT);
            saveHeadlineList(headlineList);
        }
    }

    private void saveHeadlineList(List<HeadlineModel> headlineModelList) {
        if (headlineModelList == null || headlineModelList.isEmpty()) {
            return;
        }

        String directory = getHeadlineDirectory();
        // The directory doesn't exist but fail to create it!
        if (!createDirectoryIfNecessary(directory)) {
            return;
        }

        // Overwrite old cache file if it exists!
        try (BufferedWriter writer =
                     new BufferedWriter(new FileWriter(directory + File.separator + getHeadlineFileName()))) {
            String jsonString = JSON.toJSONString(headlineModelList, PrettyFormat);
            writer.write(jsonString);
        } catch (IOException exception) {
            logger.error(exception.toString());
        }
    }

    public List<HeadlineModel> loadHeadlines() {
        try (InputStream stream =
                     new FileInputStream(getHeadlineDirectory() + File.separator + getHeadlineFileName())) {
            byte buffer[] = new byte[stream.available()];
            stream.read(buffer);
            String jsonString = new String(buffer);
            JSONArray jsonArray = JSON.parseArray(jsonString);
            ArrayList<HeadlineModel> list = new ArrayList<>();
            for (var headlineJson : jsonArray) {
                list.add(((JSONObject) headlineJson).toJavaObject(HeadlineModel.class));
            }
            return list;
        } catch (IOException exception) {
            logger.info(exception.toString());
            return null;
        }
    }

    public void saveImage(String fileName, String base64Image) {
        String directory = "src/main/resources/cache/image";
        if (!createDirectoryIfNecessary(directory)) {
            logger.error("Failed to mkdir of " + directory);
            return;
        }

        try (OutputStream stream = new FileOutputStream(directory + File.separator + fileName)) {
            byte[] decodedBytes = Base64.decodeBase64(base64Image.getBytes(StandardCharsets.UTF_8));
            stream.write(decodedBytes);
        } catch (IOException exception) {
            logger.error(exception.toString());
        }

        // Set the image file to be executable, otherwise it cannot be accessed through apache service
        File file = new File(directory + File.separator + fileName);
        try {
            var result = file.setExecutable(true);
            if (!result) {
                logger.warn("Can't set " + file.getAbsolutePath()  + "to be executable.");
            }
        } catch (Exception exception) {
            logger.warn("Exception is thrown when set " + file.getAbsolutePath()  + "to be executable. Exception: "
                    + exception);
        }
    }

    public String loadImage(String fileName) {
        String directory = "src/main/resources/cache/image";
        try (InputStream stream = new FileInputStream(directory + File.separator + fileName)) {
            byte byteImage[] = new byte[stream.available()];
            stream.read(byteImage);
            return new String(Base64.encodeBase64(byteImage));
        } catch (IOException exception) {
            logger.error(exception.toString());
            return null;
        }
    }

    private Path getHeadlinePath() throws InvalidPathException {
        String headlinePath = context.loadConfig().getString("headlinePath");
        return Paths.get(headlinePath);
    }

    private String getHeadlineDirectory() {
        try {
            return getHeadlinePath().getParent().toString();
        } catch (InvalidPathException exception) {
            return "src/main/resources/cache";
        }
    }

    private String getHeadlineFileName() {
        try {
            return getHeadlinePath().getFileName().toString();
        } catch (InvalidPathException exception) {
            return "headlines.json";
        }
    }

    private boolean createDirectoryIfNecessary(String directory) {
        File cacheDirectory = new File(directory);
        return cacheDirectory.exists() || cacheDirectory.mkdir();
    }


}
