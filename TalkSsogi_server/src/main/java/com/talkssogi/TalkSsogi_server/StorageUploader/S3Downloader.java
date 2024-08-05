package com.talkssogi.TalkSsogi_server.StorageUploader;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class S3Downloder {

    private final AmazonS3 s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public S3Downloder(@Value("${cloud.aws.credentials.accessKey}") String accessKey,
                      @Value("${cloud.aws.credentials.secretKey}") String secretKey,
                      @Value("${cloud.aws.region.static}") String region) {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();
    }

    public String getFileUrl(String key) {
        // 주어진 키를 사용하여 파일의 URL을 반환합니다.
        return s3Client.getUrl(bucketName, key).toString();
    }
}

