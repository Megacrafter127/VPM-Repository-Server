package net.m127.vpm.repo.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.m127.vpm.repo.json.PackageJson;
import org.springframework.stereotype.Service;

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
            zipOutputStream.putNextEntry(entry);
            if ("package.json".equalsIgnoreCase(entry.getName())) {
                json = map.apply(mapper.readValue(zipInputStream, PackageJson.class));
                mapper.writeValue(zipOutputStream, json);
            }
            else {
                byte[] transfer = new byte[4096];
                int h;
                while(-1 != (h = zipInputStream.read(transfer))) {
                    zipOutputStream.write(transfer, 0, h);
                }
            }
            zipOutputStream.closeEntry();
        }
        zipOutputStream.finish();
        return json;
    }
}
