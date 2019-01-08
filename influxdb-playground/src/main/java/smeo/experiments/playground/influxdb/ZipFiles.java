package smeo.experiments.playground.influxdb;


import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

public class ZipFiles {
    /**
     * Get a lazily loaded stream of lines from a gzipped file, similar to
     * {@link Files#lines(Path)}.
     *
     * @param path
     *          The path to the gzipped file.
     * @return stream with lines.
     */
    public static Stream<String> lines(Path path) {
        InputStream fileIs = null;
        BufferedInputStream bufferedIs = null;
        ZipInputStream zipIs = null;
        try {
            fileIs = Files.newInputStream(path);
            // Even though GZIPInputStream has a buffer it reads individual bytes
            // when processing the header, better add a buffer in-between
            bufferedIs = new BufferedInputStream(fileIs, 65535);
            zipIs = new ZipInputStream(bufferedIs);
        } catch (IOException e) {
            closeSafely(zipIs);
            closeSafely(bufferedIs);
            closeSafely(fileIs);
            throw new UncheckedIOException(e);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(zipIs));
        return reader.lines().onClose(() -> closeSafely(reader));
    }

    public static BufferedReader getBufferedReaderForCompressedFile(String fileIn) throws FileNotFoundException, CompressorException {
        FileInputStream fin = new FileInputStream(fileIn);
        BufferedInputStream bis = new BufferedInputStream(fin);
        CompressorInputStream input = new CompressorStreamFactory().createCompressorInputStream(bis);
        BufferedReader br2 = new BufferedReader(new InputStreamReader(input));
        return br2;
    }

    private static void closeSafely(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
}