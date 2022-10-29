package net.m127.vpm.repo.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.m127.vpm.repo.json.PackageJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.UnaryOperator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class ZipUtil {
    private final ObjectMapper mapper;
    
    private final Logger log = LoggerFactory.getLogger(ZipUtil.class);
    
    public PackageJson alterPackageJSONInFlight(
        InputStream in,
        OutputStream out,
        UnaryOperator<PackageJson> map
    ) throws IOException {
        PackageJson json = null;
        ZipInputStream zipInputStream = new ZipInputStream(in);
        ZipOutputStream zipOutputStream = new ZipOutputStream(out);
        ZipEntry entry;
        for (
            entry = zipInputStream.getNextEntry();
            entry != null;
            zipInputStream.closeEntry(),
                entry = zipInputStream.getNextEntry()
        ) {
            log.debug("Processed {} Entry: {}", entry.isDirectory() ? "Directory" : "File", entry.getName());
            if(entry.isDirectory()) continue;
            if ("package.json".equalsIgnoreCase(entry.getName())) {
                ZipEntry nentry = new ZipEntry(entry.getName());
                nentry.setComment(entry.getComment());
                nentry.setExtra(entry.getExtra());
                nentry.setMethod(entry.getMethod());
                nentry.setCreationTime(entry.getCreationTime());
                zipOutputStream.putNextEntry(nentry);
                json = map.apply(mapper.readValue(zipInputStream, PackageJson.class));
                mapper.writeValue(zipOutputStream, json);
            }
            else {
                zipOutputStream.putNextEntry(entry);
                StreamUtils.copy(zipInputStream, zipOutputStream);
            }
            zipOutputStream.closeEntry();
        }
        zipOutputStream.finish();
        return json;
    }
}
