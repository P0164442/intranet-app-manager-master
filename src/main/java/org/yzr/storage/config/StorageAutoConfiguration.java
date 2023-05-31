package org.yzr.storage.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yzr.storage.*;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageAutoConfiguration {

    private final StorageProperties properties;

    public StorageAutoConfiguration(StorageProperties properties) {
        this.properties = properties;
    }

    @Bean
    public StorageUtil StorageUtil() {
        StorageUtil StorageUtil = new StorageUtil();
        String active = this.properties.getActive();
        StorageUtil.setActive(active);
        if (active.equals("local")) {
            StorageUtil.setStorage(localStorage());
        } else {
            throw new RuntimeException("Current storage mode " + active + " Not Supported");
        }

        return StorageUtil;
    }

    @Bean
    public LocalStorage localStorage() {
        LocalStorage localStorage = new LocalStorage();
        StorageProperties.Local local = this.properties.getLocal();
        localStorage.setAddress(local.getAddress());
        localStorage.setStoragePath(local.getStoragePath());
        return localStorage;
    }

}
