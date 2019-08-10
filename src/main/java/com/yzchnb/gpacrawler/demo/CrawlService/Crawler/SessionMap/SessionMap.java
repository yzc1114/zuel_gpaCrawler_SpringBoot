package com.yzchnb.gpacrawler.demo.CrawlService.Crawler.SessionMap;

import com.yzchnb.gpacrawler.demo.CrawlService.Crawler.SessionContent.SessionContent;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component("SessionMap")
public class SessionMap {
    private HashMap<String, SessionContent> map = new HashMap<>();

    public SessionContent getSessionContent(String session_key){
        return map.get(session_key);
    }

    public void addSessionContent(String session_key, SessionContent sc){
        map.put(session_key, sc);
    }

    public void deleteSessionContent(String session_key){
        map.remove(session_key);
    }

    public boolean existSession(String session_key){
        return map.containsKey(session_key);
    }

}
