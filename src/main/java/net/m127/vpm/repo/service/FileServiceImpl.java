package net.m127.vpm.repo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.m127.vpm.repo.json.PackageJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;

@Service
public class FileServiceImpl implements FileService {
    @Value("${vpm.folder}")
    protected Path basePath;
    @Autowired
    protected ObjectMapper mapper;
    
    @Override
    public void initializePackage(String packageId) throws IOException {
        Files.createDirectories(basePath.resolve(packageId));
    }
    
    @Override
    public void deletePackage(String packageId) throws IOException {
        FileSystemUtils.deleteRecursively(basePath.resolve(packageId));
    }
    
    @Override
    public ParsedFileUpload parseFileUpload(MultipartFile file) throws IOException {
        Path tmp = Files.createTempFile("pkg", ".zip");
        file.transferTo(tmp);
        PackageJson json;
        try (ZipFile zip = new ZipFile(tmp.toFile())) {
            json = mapper.readValue(zip.getInputStream(zip.getEntry("package.json")), PackageJson.class);
        }
        return new ParsedFileUpload(tmp, json);
    }
    
    @Override
    public void saveFileUpload(ParsedFileUpload upload) throws IOException {
        Files.copy(
            upload.tmpFile(),
            basePath.resolve(upload.parsedJson().name())
                    .resolve(upload.parsedJson().version() + ".zip")
        );
    }
    
    @Override
    public Resource getPackageVersion(String packageId, String version) {
        return new PathResource(basePath.resolve(packageId).resolve(version + ".zip"));
    }
}
