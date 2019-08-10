package com.yzchnb.gpacrawler.demo.CrawlService.Crawler;


import com.yzchnb.gpacrawler.demo.CrawlService.Crawler.Entities.BeReturnedInfo;
import com.yzchnb.gpacrawler.demo.CrawlService.Crawler.SessionContent.SessionContent;
import com.yzchnb.gpacrawler.demo.CrawlService.Crawler.SessionMap.SessionMap;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

import static java.lang.Math.*;

@Component("Fetcher")
public class Crawler implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    private SessionMap sessionMap;

    private String HOST = "http://202.114.234.143";

    private String authHost = HOST + "/authserver";

    private String query_url = "http://202.114.234.163/jsxsd/kscj/cjcx_query?Ves632DSdyV=NEW_XSD_XJCJ";

    private String to_get_captcha_url = "http://202.114.234.143/authserver/captcha.html";

    private String query_url_host = "http://202.114.234.163/jsxsd/kscj/cjcx_list";

    private String user_agent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_3) AppleWebKit/537.36" +
            " (KHTML, like Gecko) Chrome/61.0.3163.79 Safari/537.36";

    private String SUCCESS_LOGIN_SYMBOL = "CASTGC";

    private HashMap<String, String> headers = new HashMap<>();

    private String login(String username, String password, String session_key) throws Exception{
        HashMap<String, String> login_post_data = new HashMap<>();
        login_post_data.put("username", username);
        login_post_data.put("password", password);
        login_post_data.put("lt", "");
        login_post_data.put("execution", "");
        login_post_data.put("_eventId", "submit");

        headers.put("User-Agent", user_agent);
        headers.put("Connection", "keep-alive");

        SessionContent sessionContent = new SessionContent();
        sessionMap.addSessionContent(session_key, sessionContent);
        Connection.Response res1 = Jsoup
                .connect(query_url)
                .headers(headers)
                .timeout(5000)
                .execute();
        Document doc1 = Jsoup.parse(res1.body());
        Map<String, String> cookies1 = res1.cookies();
        sessionContent.addCookies(query_url_host, cookies1);
        System.out.println(cookies1);

        System.out.println(doc1.body());
        Elements elements1 = doc1.getElementsByTag("script");
        String new_login_url = elements1.get(0).html().split("href='")[1].split("'")[0];

        Connection.Response res2 = Jsoup
                .connect(new_login_url)
                .headers(headers)
                .execute();
        Document doc2 = Jsoup.parse(res2.body());
        Map<String, String> cookies2 = res2.cookies();
        sessionContent.addCookies(authHost, cookies2);
        System.out.println(cookies2);
        new_login_url = HOST + doc2.getElementsByTag("form").get(0).attr("action");
        Elements allInput = doc2.getElementsByTag("input");
        if(allInput.size() == 7){
            throw new Exception("need_captcha");
        }
        login_post_data.put("lt", allInput.get(2).val());
        login_post_data.put("execution", allInput.get(3).val());
        login_post_data.put("_eventId", allInput.get(4).val());

        Connection.Response res3 = Jsoup
                .connect(new_login_url)
                .headers(headers)
                .cookies(res2.cookies())
                .timeout(5000)
                .data(login_post_data)
                .method(Connection.Method.POST)
                .execute();
        Map<String, String> cookies3 = res3.cookies();
        sessionContent.addCookies(authHost, cookies3);
        Document doc3 = Jsoup.parse(res3.body());
        Elements elementsOfForm = doc3.select(".form_list_user");
        if(elementsOfForm.size() == 3){
            sessionContent.setLogin_post_data(login_post_data);
            sessionContent.setNew_login_url(new_login_url);
            throw new Exception("need_captcha");
        }else if(elementsOfForm.size() == 2){
            throw new Exception("login_fail");
        }
        //new_login_url;
        if(cookies3.containsKey(SUCCESS_LOGIN_SYMBOL)){

            return "login_success";
        }

        throw new Exception("unknown_error");
    }

    public String fetchList(String username, String password, String session_key, String captcha) throws Exception{
        //1. login
        String loginResponse;
        if(captcha == null){
            loginResponse = login(username, password, session_key);
        }else{
            loginResponse = loginWithCaptcha(username, password, session_key, captcha);
        }

        if(!loginResponse.equals("login_success")){
            throw new Exception(loginResponse);
        }

        SessionContent sessionContent = sessionMap.getSessionContent(session_key);
        Document doc = Jsoup
                .connect(query_url_host)
                .headers(headers)
                .cookies(sessionContent.getCookies().get(authHost))
                .get();
        BeReturnedInfo beReturnedInfo = new BeReturnedInfo();
        String json = beReturnedInfo.parseDoc(doc);
        sessionMap.deleteSessionContent(session_key);
        return json;
    }

    private String loginWithCaptcha(String username, String password, String session_key, String captcha) throws Exception{
        if(!sessionMap.existSession(session_key)){
            throw new Exception("no session key found");
        }
        SessionContent sessionContent = sessionMap.getSessionContent(session_key);
        HashMap<String, String> login_post_data = sessionContent.getLogin_post_data();
        login_post_data.put("username", username);
        login_post_data.put("password", password);
        login_post_data.put("captchaResponse", captcha);
        Connection.Response res = Jsoup.connect(sessionContent.getNew_login_url())
                .data(login_post_data)
                .timeout(5000)
                .method(Connection.Method.POST)
                .headers(headers)
                .cookies(sessionContent.getCookies().get(authHost))
                .execute();
        if(res.statusCode() != 200){
            throw new Exception("sites_dead");
        }
        if(res.cookies().containsKey(SUCCESS_LOGIN_SYMBOL)){
            return "login_success";
        }
        sessionContent.getCookies().put(authHost, res.cookies());
        Document doc = Jsoup.parse(res.body());
        Elements formList = doc.select(".form_list_user");
        if(formList.size() == 3){
            throw new Exception("login_fail_need_refresh");
        }else if(formList.size() == 2){
            throw new Exception("login_fail_no_need_refresh");
        }
        Connection.Response res2 = Jsoup.connect(authHost)
                .data(login_post_data)
                .method(Connection.Method.POST)
                .timeout(5000)
                .headers(headers)
                .cookies(sessionContent.getCookies().get(authHost))
                .execute();
        res2.cookies();
        throw new Exception("unknown_error");
    }

    public String get_captcha(String session_key, String tempImgsPath) throws Exception{
        if(!sessionMap.existSession(session_key)){
            throw new Exception("no session key found");
        }
        SessionContent sessionContent = sessionMap.getSessionContent(session_key);
        HashMap<String, String> tempHeaders = (HashMap<String, String>) headers.clone();
        tempHeaders.put("content-type", "application/x-jpg");
        Connection.Response res = Jsoup.connect(to_get_captcha_url)
                .ignoreContentType(true)
                .timeout(5000)
                .cookies(sessionContent.getCookies().get(authHost))
                .headers(tempHeaders)
                .method(Connection.Method.GET)
                .execute();
        byte[] img = res.bodyAsBytes();
        String fileName = "temp" + session_key + random();
        File file = new File(tempImgsPath + fileName);
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        bos.write(img);
        bos.close();
        fos.close();
        return "tempImgs/" + fileName;

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.sessionMap = (SessionMap) this.applicationContext.getBean("SessionMap");
    }


}
