package com.yzchnb.gpacrawler.demo;

import net.sourceforge.htmlunit.corejs.javascript.EcmaError;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GpaCrawlerApplicationTests {

    @Test
    public void contextLoads() {
        try {
            System.out.println(ResourceUtils.getURL("classpath:/static/").getPath());
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

}
