package com.example.bucket.controller;

import com.example.bucket.service.AmazonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.net.URL;

@RestController
@RequestMapping("/storage/")
public class BucketController {

    private AmazonClient amazonClient;

    @Autowired
    BucketController(AmazonClient amazonClient) {
        this.amazonClient = amazonClient;
    }

    @PostMapping("/uploadFile")
    public String uploadFile(@RequestPart(value="file") byte[] file, @RequestPart(value="fileName") String fileName,  @RequestPart(value="folderName") String folderName) {
        return this.amazonClient.uploadFile(file, fileName, folderName);
    }

    @DeleteMapping("/deleteFile")
    public String deleteFile(@RequestPart(value = "url") String fileUrl) throws MalformedURLException {
        return this.amazonClient.deleteFileFromS3Bucket(fileUrl);
    }

    @GetMapping("getfile")
    public URL getFile(@RequestPart("url") String url){
        return this.amazonClient.getImage(url);
    }

}
