package com.yzchnb.gpacrawler.demo.CrawlService.Crawler.SessionContent;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;

import java.util.HashMap;
import java.util.Map;

public class SessionContent {

    private String new_login_url = "";

    private HashMap<String, String> login_post_data = new HashMap<>();

    public HashMap<String, Map<String, String>> getCookies() {
        return cookies;
    }

    public void addCookies(String domain, Map<String, String> cookies){
        if(!this.cookies.containsKey(domain)){
            this.cookies.put(domain, cookies);
            return;
        }
        Map<String, String> originCookies = this.cookies.get(domain);
        for(Map.Entry<String, String> entry : cookies.entrySet()){
            originCookies.put(entry.getKey(), entry.getValue());
        }
    }

    public void setCookies(HashMap<String, Map<String, String>> cookies) {
        this.cookies = cookies;
    }

    private HashMap<String, Map<String, String>> cookies = new HashMap<>();


    public HashMap<String, String> getLogin_post_data() {
        return login_post_data;
    }

    public void setLogin_post_data(HashMap<String, String> login_post_data) {
        this.login_post_data = login_post_data;
    }

    public String getNew_login_url() {
        return new_login_url;
    }

    public void setNew_login_url(String new_login_url) {
        this.new_login_url = new_login_url;
    }

}
