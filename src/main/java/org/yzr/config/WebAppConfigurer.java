package org.yzr.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.ResourceUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.yzr.service.UserService;

import javax.annotation.Resource;

@Configuration
public class WebAppConfigurer extends WebMvcConfigurationSupport {

    @Resource
    private Environment environment;
    @Resource
    private UserService userService;

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        try {
            String path = ResourceUtils.getURL("").getPath();
            System.out.println("path => " + path);
            String prefix = ResourceUtils.FILE_URL_PREFIX + path;
            String debug = environment.getProperty("config.version");
            if (debug != null && debug.equals("local")) {
                prefix = "classpath:/";
            }
            registry.addResourceHandler("/android/**").addResourceLocations(prefix + "static/upload/android/");
            registry.addResourceHandler("/ios/**").addResourceLocations(prefix + "static/upload/ios/");
            registry.addResourceHandler("/crt/**").addResourceLocations("classpath:static/crt/");
            registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/");
            registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/");
            registry.addResourceHandler("/images/**").addResourceLocations("classpath:/static/images/");

            super.addResourceHandlers(registry);
            userService.initUsers();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
