package com.talkssogi.TalkSsogi_server.StorageUploader;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class S3DownLoader {

    private final AmazonS3 s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public S3DownLoader(@Value("${cloud.aws.credentials.access-key}") String accessKey,
                        @Value("${cloud.aws.credentials.secret-key}") String secretKey,
                        @Value("${cloud.aws.region.static}") String region) {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();
    }

    // 파일을 S3에 업로드하는 메서드
    public void uploadFile(String key, InputStream inputStream, String contentType) {
        // 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);

        // 업로드 요청 생성
        PutObjectRequest request = new PutObjectRequest(bucketName, key, inputStream, metadata);

        // 파일 업로드
        s3Client.putObject(request);
    }


    public String getFileUrl(String key) {
        // 주어진 키를 사용하여 파일의 URL을 반환합니다.
        return s3Client.getUrl(bucketName, key).toString();
    }

}
