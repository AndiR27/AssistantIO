package heg.backendspring.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
@UtilityClass

public class FileUploadForm {
    /**
     * Transforme le contenu d’un MultipartFile en tableau de lignes.
     */
    public static String[] transformData(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            log.info("File is null or empty");
            return new String[0];
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().toArray(String[]::new);
        }
    }

    /**
     * Transforme le contenu d'un MultipartFile en InputStream.
     */
    public static InputStream getFileInputStream(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        return file.getInputStream();
    }
}
