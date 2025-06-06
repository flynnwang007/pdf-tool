package com.yourcompany.pdfapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableConfigurationProperties
@EnableAsync
public class PdfAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(PdfAppApplication.class, args);
    }
} 