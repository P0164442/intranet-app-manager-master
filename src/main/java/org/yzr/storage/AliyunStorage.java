package org.yzr.storage;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.stream.Stream;

public class AliyunStorage implements IStorage {
    private final Log logger = LogFactory.getLog(AliyunStorage.class);

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    /**
     *
     *
     * @return ossClient
     */
    private OSSClient getOSSClient() {
        return new OSSClient(endpoint, accessKeyId, accessKeySecret);
    }

    private String getBaseUrl() {
        return "https://" + bucketName + "." + endpoint + "/";
    }

    /**
     *
     */
    @Override
    public void store(InputStream inputStream, long contentLength, String contentType, String keyName) {
        try {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(contentLength);
            objectMetadata.setContentType(contentType);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, keyName, inputStream, objectMetadata);
            PutObjectResult putObjectResult = getOSSClient().putObject(putObjectRequest);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }

    }

    @Override
    public Stream<Path> loadAll() {
        return null;
    }

    @Override
    public Path load(String keyName) {
        return null;
    }

    @Override
    public Resource loadAsResource(String keyName) {
        try {
            URL url = new URL(getBaseUrl() + keyName);
            Resource resource = new UrlResource(url);
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                return null;
            }
        } catch (MalformedURLException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void delete(String keyName) {
        try {
            getOSSClient().deleteObject(bucketName, keyName);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

    @Override
    public String generateUrl(String keyName) {
        return getBaseUrl() + keyName;
    }
}
