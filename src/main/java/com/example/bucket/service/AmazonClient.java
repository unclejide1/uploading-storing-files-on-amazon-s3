package com.example.bucket.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.xspec.S;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Date;

@Service
public class AmazonClient {

    private AmazonS3 s3client;

    @Value("${amazonProperties.endpointUrl}")
    private String endpointUrl;
    @Value("${amazonProperties.bucketName}")
    private String bucketName;
    @Value("${amazonProperties.accessKey}")
    private String accessKey;
    @Value("${amazonProperties.secretKey}")
    private String secretKey;



    @PostConstruct
    private void initializeAmazon() {
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
        s3client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion("eu-west-2")
                .build();
    }

    public String uploadFile(byte[] multipartFile, String fileName,String folderName ) {
        String fileUrl = "";
        String uniqueFileName = generateFileName(fileName);
        String folderFileName = null;
        if(folderName.trim().isEmpty()){
            folderFileName = "general/" + uniqueFileName;

        }
        if(!folderName.trim().isEmpty()){
            folderFileName = folderName + "/"+uniqueFileName;
        }
        System.out.println(folderFileName);

        boolean status = false;
        long fileSize = 0;
        try {
            File file = convertBytesToFile(multipartFile,fileName);
            fileUrl = "https://" + bucketName+ "." +endpointUrl+ "/" + folderFileName;
            fileSize = (long) file.length()/1024;
            uploadFileTos3bucket(folderFileName, file);
//            file.delete();
        } catch (Exception e) {
           e.printStackTrace();
        }
        System.out.println(fileSize);
        return fileUrl;
    }

    private File convertBytesToFile(byte[] data, String fileName) throws IOException {
        // Path of a file
        String filename2 = "temp."+fileName.substring(fileName.lastIndexOf(".") + 1);
         File file = new File(filename2);
        try {
            OutputStream os = new FileOutputStream(file);
            os.write(data);
            // Close the file
            os.close();
        }

        catch (Exception e) {
            System.out.println("Exception: " + e);
        }
        return file;
    }

    private String generateFileName(String fileName) {
        return  new Date().getTime() + fileName.replace(" ", "_");
    }

    private void uploadFileTos3bucket(String fileName, File file) {
        s3client.putObject(new PutObjectRequest(bucketName, fileName, file).withCannedAcl(CannedAccessControlList.PublicRead));
    }

    public String deleteFileFromS3Bucket(String fileUrl) throws MalformedURLException {
        String test = "https://" + bucketName+ "." +endpointUrl+ "/";
        String fileName = fileUrl.substring(fileUrl.indexOf(test) + test.length());
//        URL aURL = new URL(fileName);
        System.out.println(fileName);

        System.out.println(fileName);
        s3client.deleteObject(new DeleteObjectRequest(bucketName, fileName));
        return "Successfully deleted";
    }

    public URL  getImage(String url){
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        URL url2 = null;
        try {
            // Set the presigned URL to expire after one hour.
            java.util.Date expiration = new java.util.Date();
            long expTimeMillis = expiration.getTime();
            expTimeMillis += (24*60*60 + 1) * 1000;
            expiration.setTime(expTimeMillis);

            // Generate the presigned URL.
            System.out.println("Generating pre-signed URL.");
            GeneratePresignedUrlRequest generatePresignedUrlRequest =
                    new GeneratePresignedUrlRequest(bucketName, fileName)
                            .withMethod(HttpMethod.GET)
                            .withExpiration(expiration);
            url2 = s3client.generatePresignedUrl(generatePresignedUrlRequest);

            System.out.println("Pre-Signed URL: " + url2.toString());
        } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            e.printStackTrace();
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
        }
        return url2;
    }

}
