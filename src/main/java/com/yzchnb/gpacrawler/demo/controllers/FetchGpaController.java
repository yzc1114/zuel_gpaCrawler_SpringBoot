package com.yzchnb.gpacrawler.demo.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yzchnb.gpacrawler.demo.CrawlService.Crawler.Crawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;

@RestController
public class FetchGpaController implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private static String ANNOUNCEMENT = "个人开发小程序，能力有限，多多包涵。遇到卡顿可能是由于教务部网站无法登陆，也有可能是我的服务器性能太渣。若有意向与我一同维护该项目，请联系我。qq:1021777674";

    @Autowired
    private Crawler crawler;

    @Value("${web.tempImgs.fullpath}")
    private String tempImgsPath;

    @RequestMapping("/")
    public String index(){
        return "yzchnb Hello";
    }

    @RequestMapping("/get_userinfo")
    public String get_userinfo(@RequestParam String JSCODE) throws Exception{
        String appid = "wxdeee740ab9784055";
        String secret = "9dcffb72be54ae459f895d657a4f78ff";
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid="
                + appid
                + "&secret="
                + secret
                + "&js_code="
                + JSCODE
                + "&grant_type=authorization_code";
        Document doc = Jsoup.connect(url).get();
        System.out.println(doc.body());
        ObjectMapper mapper = new ObjectMapper();
        HashMap map = mapper.readValue(doc.body().wholeText(), HashMap.class);//readValue到一个原始数据类型.
        System.out.println(map.get("session_key")); //session_key
        String session_key = (String) map.get("session_key");
        session_key = session_key.replace("+", "z").replace("/", "s");
        map.put("session_key", session_key);
        return mapper.writeValueAsString(map);
    }

    @RequestMapping(value = "/fetchList", method = RequestMethod.POST)
    public String fetchList(@RequestParam String username,
                            @RequestParam String password,
                            @RequestParam String session_key) throws Exception{

        return crawler.fetchList(username, password, session_key, null);
    }

    @RequestMapping(value = "/fetchListWithCaptcha", method = RequestMethod.POST)
    public String fetchListWithCaptcha(@RequestParam String username,
                                       @RequestParam String password,
                                       @RequestParam String session_key,
                                       @RequestParam String captchaResponse) throws Exception{
        return crawler.fetchList(username, password, session_key, captchaResponse);
    }

    @RequestMapping(value = "/getAnnouncement")
    public String getAnnouncement(){
        return ANNOUNCEMENT;
    }

    @RequestMapping(value = "/setAnnouncement", method = RequestMethod.POST)
    public String setAnnouncement(@RequestParam String announcement){
        ANNOUNCEMENT = announcement;
        return ANNOUNCEMENT;
    }

    @RequestMapping("/get_captcha")
    public String get_captcha(@RequestParam String session_key) throws Exception{
        return crawler.get_captcha(session_key, tempImgsPath);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
