/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalavit.javulna.services;


import org.apache.commons.io.FilenameUtils;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.errors.EncodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

/**
 * @author peti
 */
@Service
public class FileStorageService {

    private static final Logger LOG = LoggerFactory.getLogger(FileStorageService.class);

    @Value(value = "${javulna.filestore.dir}")
    private String fileStorageDir;

    private static final List<String> ALLOWED_FORMAT =
        Arrays.asList(new String[]{"jpg", "jpeg","pdf"});

    public String storeFile(MultipartFile file) {

        //TODO: Add white listing of the file type.Never accept a filename and its extension directly without having a whitelist filter.
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        FilenameUtils.getExtension(fileName);
        if(ALLOWED_FORMAT.contains(FilenameUtils.getExtension(fileName).toLowerCase()))
        {
            try {
                // Copy file to the target location (Replacing existing file with the same name)
                Path targetLocation = Paths.get(fileStorageDir, fileName);
                LOG.debug("gonna write file to {}", targetLocation.toString());
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
                return fileName;
            } catch (IOException ex) {
                throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
            }
        }else{
            throw new RuntimeException("File Type not allowed " );
        }

    }

    public Resource loadFileAsResource(String fileName) throws EncodingException {
        try {
            Path filePath = Paths.get(fileStorageDir, ESAPI.encoder().encodeForURL(fileName));
            LOG.debug("gonna read file from {}", filePath.toString());
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File not found " + fileName, ex);
        }
    }

}
