package org.chronusartcenter.news;

import com.alibaba.fastjson2.JSONArray;
import org.apache.log4j.Logger;
import org.chronusartcenter.Context;
import org.chronusartcenter.network.OkHttpWrapper;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class NewsService {
    private final Context context;

    private Logger logger = Logger.getLogger(Context.class);

    public NewsService(Context globalContext) {
        this.context = globalContext;
    }

    private List<String> getRssUrls() throws Exception {
        try {
            JSONArray jsonArray = context.loadConfig().getJSONArray("rssUrl");
            List<String> urls = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++) {
                urls.add(jsonArray.getString(i));
            }
            return urls;
        } catch (Exception exception) {
            throw new Exception("Failed to get RSS URLs!");
        }
    }

    private List<HeadlineModel> fetchHeadlinesInternal(String rssUrl) {
        OkHttpWrapper okHttpWrapper = new OkHttpWrapper();
        List<HeadlineModel> resultList = new LinkedList<>();
        try {
            var response = okHttpWrapper.get(rssUrl);
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            XMLEventReader reader = xmlInputFactory.createXMLEventReader(new StringReader(response));
            HeadlineModel headlineItem = new HeadlineModel();
            while (reader.hasNext()) {
                XMLEvent nextEvent = reader.nextEvent();
                if (nextEvent.isStartElement()) {
                    StartElement startElement = nextEvent.asStartElement();
                    switch (startElement.getName().getLocalPart()) {
                        case "item":
                            headlineItem = new HeadlineModel();
                            headlineItem.setLanguage(HeadlineModel.Language.ZH);
                            break;
                        case "title":
                            nextEvent = reader.nextEvent();
                            headlineItem.setTitle(nextEvent.asCharacters().getData());
                            break;
                        case "author":
                            nextEvent = reader.nextEvent();
                            headlineItem.setAuthor(nextEvent.asCharacters().getData());
                            break;
                        case "pubDate":
                            nextEvent = reader.nextEvent();
                            headlineItem.setPublishDate(nextEvent.asCharacters().getData());
                            break;
                    }
                }
                if (nextEvent.isEndElement()) {
                    EndElement endElement = nextEvent.asEndElement();
                    if (endElement.getName().getLocalPart().equals("item")) {
                        resultList.add(headlineItem);
                    }
                }
            }
        } catch (Exception exception) {
            logger.error(exception.toString());
        }

        return resultList;
    }

    public List<HeadlineModel> fetchHeadlines() {
        try {
            List<HeadlineModel> ret = new LinkedList<>();
            var rssUrls = getRssUrls();
            for (var url : rssUrls) {
                ret.addAll(fetchHeadlinesInternal(url));
            }
            return ret;
        } catch (Exception exception) {
            logger.error(exception.toString());
        }
        return null;
    }

    public List<HeadlineModel> translateHeadlines(List<HeadlineModel> headlineItemList) {
        BaiduTranslate baiduTranslate = new BaiduTranslate(context);
        var translateTitleList
                = baiduTranslate.translate(headlineItemList.stream().map(HeadlineModel::getTitle).toList());

        if (translateTitleList == null || translateTitleList.size() == 0) {
            return null;
        }

        for (int i = 0; i < headlineItemList.size(); i++) {
            headlineItemList.get(i).setTranslation(translateTitleList.get(i));
        }
        return headlineItemList.stream().filter(
                headlineItem -> {
                    return
                            !headlineItem.getTranslation().isEmpty() && !headlineItem.getTranslation().equals(" ");
                }).toList();

    }
}
