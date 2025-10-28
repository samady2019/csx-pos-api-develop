package kh.com.csx.posapi.config;

import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpSession;
import java.security.MessageDigest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import org.apache.tika.Tika;

@Configuration
public class SftpConfig {
    @Value("${sftp.host}")
    private String sftpHost;

    @Value("${sftp.port}")
    private int sftpPort;

    @Value("${sftp.username}")
    private String sftpUsername;

    @Value("${sftp.password}")
    private String sftpPassword;

    @Value("${sftp.upload.images}")
    private String sftpUploadImage;

    @Value("${sftp.upload.documents}")
    private String sftpUploadDocuments;

    @Bean
    public DefaultSftpSessionFactory sftpSessionFactory() {
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory();
        factory.setHost(sftpHost);
        factory.setPort(sftpPort);
        factory.setUser(sftpUsername);
        factory.setPassword(sftpPassword);
        factory.setAllowUnknownKeys(true);
        return factory;
    }

    public String filePath(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        String targetPath;
        if (Constant.FileExtension.DOC.contains(extension)) {
            targetPath = sftpUploadDocuments;
        } else if (Constant.FileExtension.IMG.contains(extension)) {
            targetPath = sftpUploadImage;
        } else {
            throw new ApiException("Unsupported file type: " + extension, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }
        return Paths.get(targetPath, fileName).toString().replace(File.separator, "/");
    }

    public String fileContentType(String fileName) {
        Tika tika = new Tika();
        return tika.detect(fileName);
    }

    public String fileChecksum(String path, SftpSession session) throws IOException {
        try (InputStream inputStream = session.readRaw(path)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] byteArray = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesRead);
            }
            byte[] hashBytes = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02X", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new IOException("File checksum failed.", e);
        }
    }
}
