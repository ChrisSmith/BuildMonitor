package org.collegelabs.buildmonitor.buildmonitor2.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import retrofit.client.Response;
import timber.log.Timber;

public class DiskUtil {

    public static String WriteBytesToDisk(Response response, File file) {

        final long startTime = System.currentTimeMillis();


        try(InputStream inputStream = response.getBody().in()){
            try(OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {

                final int bufferSize = 8 * 1024;
                byte[] buffer = new byte[bufferSize];

                int length;
                while ((length = inputStream.read(buffer, 0, bufferSize)) > 0) {
                    out.write(buffer, 0, length);
                }

                Timber.d((TimeUtil.human(System.currentTimeMillis() - startTime)) + " to download: " + file.getName());

                return file.getAbsolutePath();
            }
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
}
