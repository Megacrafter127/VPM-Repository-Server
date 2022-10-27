package net.m127.vpm.repo.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.m127.vpm.repo.json.PackageJson;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
public class ZipUtil {
    private final ObjectMapper mapper;
    public PackageJson getPackageJsonFromZip(byte[] zipFile) {
        try(ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(zipFile))) {
            ZipEntry entry;
            for(entry = zip.getNextEntry(); entry != null; zip.closeEntry(),entry = zip.getNextEntry()) {
               if(entry.isDirectory()) continue;
               if("package.json".equalsIgnoreCase(entry.getName())) break;
            }
            return mapper.readValue(zip, PackageJson.class);
        } catch(IOException ex) {
            return null;
        }
    }
}
