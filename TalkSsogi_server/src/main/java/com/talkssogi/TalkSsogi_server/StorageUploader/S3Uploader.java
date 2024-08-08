//package com.talkssogi.TalkSsogi_server.StorageUploader;
//
//import com.amazonaws.auth.AWSStaticCredentialsProvider;
//import com.amazonaws.auth.BasicAWSCredentials;
//import com.amazonaws.regions.Regions;
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.AmazonS3ClientBuilder;
//import com.amazonaws.services.s3.model.PutObjectRequest;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import java.io.File;
//
//@Component
//public class S3Uploader {
//    private final AmazonS3 s3Client;
//
//    @Value("${aws.s3.bucket}")
//    private String bucketName;
//
//    public S3Uploader(@Value("${aws.accessKeyId}") String accessKeyId, @Value("${aws.secretKey}") String secretKey) {
//        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKeyId, secretKey);
//        this.s3Client = AmazonS3ClientBuilder.standard()
//                .withRegion(Regions.DEFAULT_REGION)
//                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
//                .build();
//    }
//
//    public String upload(File file, String fileName) {
//        s3Client.putObject(new PutObjectRequest(bucketName, fileName, file));
//        return s3Client.getUrl(bucketName, fileName).toString();
//    }
//}
