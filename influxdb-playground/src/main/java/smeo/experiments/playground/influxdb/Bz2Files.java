package smeo.experiments.playground.influxdb;


import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.zip.ZipInputStream;

public class Bz2Files {
    /**
     * Get a lazily loaded stream of lines from a gzipped file, similar to
     * {@link Files#lines(Path)}.
     *
     * @param path
     *          The path to the gzipped file.
     * @return stream with lines.
     */
    public static Stream<String> lines(Path path) {
        try {
            BufferedReader finalReader  = getBufferedReaderForCompressedFile(path.toString());
            return finalReader.lines().onClose(() -> closeSafely(finalReader));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static BufferedReader getBufferedReaderForCompressedFile(String fileIn) throws FileNotFoundException, CompressorException {
        FileInputStream fin = new FileInputStream(fileIn);
        BufferedInputStream bis = new BufferedInputStream(fin);
        CompressorInputStream input = new CompressorStreamFactory().createCompressorInputStream(bis);
        BufferedReader br2 = null;
        try {
            br2 = new BufferedReader(new InputStreamReader(input, "ISO-8859-1"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
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