package com.yzchnb.gpacrawler.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
public class GpaCrawlerApplication implements WebMvcConfigurer {

    @Value("${web.tempImgs.fullpath}")
    private String tempImgsPath;


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/tempImgs/**").addResourceLocations(
                "file:" + tempImgsPath);

    }

    public static void main(String[] args) {

        SpringApplication.run(GpaCrawlerApplication.class, args);
    }

}
