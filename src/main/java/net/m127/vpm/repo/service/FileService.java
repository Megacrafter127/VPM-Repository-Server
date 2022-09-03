package net.m127.vpm.repo.service;

import net.m127.vpm.repo.json.PackageJson;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface FileService {
    record ParsedFileUpload(Path tmpFile, PackageJson parsedJson) {}
    void initializePackage(String packageId) throws IOException;
    void deletePackage(String packageId) throws IOException;
    ParsedFileUpload parseFileUpload(MultipartFile file) throws IOException;
    void saveFileUpload(ParsedFileUpload upload) throws IOException;
    Resource getPackageVersion(String packageId, String version) throws IOException;
}
