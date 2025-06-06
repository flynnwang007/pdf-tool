package com.yourcompany.pdfapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.concurrent.Executor;

@Configuration
public class AppConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Bean(name = "pdfTaskExecutor")
    public Executor pdfTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("PDF-Task-");
        executor.initialize();
        return executor;
    }

    @ConfigurationProperties(prefix = "app.file")
    @Configuration
    public static class FileProperties {
        private String uploadPath;
        private String tempPath;
        private long maxFileSize;
        private List<String> allowedTypes;

        // Getters and Setters
        public String getUploadPath() {
            return uploadPath;
        }

        public void setUploadPath(String uploadPath) {
            this.uploadPath = uploadPath;
        }

        public String getTempPath() {
            return tempPath;
        }

        public void setTempPath(String tempPath) {
            this.tempPath = tempPath;
        }

        public long getMaxFileSize() {
            return maxFileSize;
        }

        public void setMaxFileSize(long maxFileSize) {
            this.maxFileSize = maxFileSize;
        }

        public List<String> getAllowedTypes() {
            return allowedTypes;
        }

        public void setAllowedTypes(List<String> allowedTypes) {
            this.allowedTypes = allowedTypes;
        }
    }

    @ConfigurationProperties(prefix = "app.pdf")
    @Configuration
    public static class PdfProperties {
        private Processing processing = new Processing();
        private Ocr ocr = new Ocr();
        private Fonts fonts = new Fonts();

        public Processing getProcessing() {
            return processing;
        }

        public void setProcessing(Processing processing) {
            this.processing = processing;
        }

        public Ocr getOcr() {
            return ocr;
        }

        public void setOcr(Ocr ocr) {
            this.ocr = ocr;
        }
        
        public Fonts getFonts() {
            return fonts;
        }
        
        public void setFonts(Fonts fonts) {
            this.fonts = fonts;
        }

        public static class Processing {
            private int threadPoolSize;
            private int maxPoolSize;
            private int queueCapacity;

            // Getters and Setters
            public int getThreadPoolSize() {
                return threadPoolSize;
            }

            public void setThreadPoolSize(int threadPoolSize) {
                this.threadPoolSize = threadPoolSize;
            }

            public int getMaxPoolSize() {
                return maxPoolSize;
            }

            public void setMaxPoolSize(int maxPoolSize) {
                this.maxPoolSize = maxPoolSize;
            }

            public int getQueueCapacity() {
                return queueCapacity;
            }

            public void setQueueCapacity(int queueCapacity) {
                this.queueCapacity = queueCapacity;
            }
        }

        public static class Ocr {
            private boolean enabled;
            private String languages;
            private String tesseractPath;
            private String dataPath;

            // Getters and Setters
            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public String getLanguages() {
                return languages;
            }

            public void setLanguages(String languages) {
                this.languages = languages;
            }

            public String getTesseractPath() {
                return tesseractPath;
            }

            public void setTesseractPath(String tesseractPath) {
                this.tesseractPath = tesseractPath;
            }

            public String getDataPath() {
                return dataPath;
            }

            public void setDataPath(String dataPath) {
                this.dataPath = dataPath;
            }
        }
        
        public static class Fonts {
            private List<String> chinesePaths;

            public List<String> getChinesePaths() {
                return chinesePaths;
            }

            public void setChinesePaths(List<String> chinesePaths) {
                this.chinesePaths = chinesePaths;
            }
        }
    }
} 