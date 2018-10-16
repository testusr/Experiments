package smeo.experiments.playground.influxdb;

import org.apache.commons.lang3.time.DateParser;
import org.apache.commons.lang3.time.FastDateFormat;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class CsvToInfluxDbWriter {
    final List<String[]> floatFieldNames = new ArrayList<>();
    final List<String[]>  stringFieldNames = new ArrayList<>();
    final List<String[]>  tagNames = new ArrayList<>();
    static final String QUOTE = "Q";
    int timeField = 0;

    InfluxDB influxDB;
    int targetedBatchSize = 100000;

   // SimpleDateFormat format = new SimpleDateFormat("dd.MM.yy HH:mm:ss.SSS", Locale.ENGLISH);
    private BatchPoints batchPoints;
    private long allPoints = 0;

    private static final DateParser format = FastDateFormat.getInstance("dd.MM.yy HH:mm:ss.SSS",TimeZone.getTimeZone("GMT"));

    private CsvToInfluxDbWriter() {
    }

    public void prepare() {
        //format..setTimeZone(TimeZone.getTimeZone("GMT"));
        newBatch();
        influxDB = InfluxDBFactory.connect(databaseURL(), userName(), password());
        influxDB.createDatabase(dbName());
        influxDB.createRetentionPolicy(
                "defaultPolicy", dbName(), "90d", 1, true);

        // influxDB.createRetentionPolicy(
        // "defaultPolicy", "baeldung", "30d", 1, true);
    }

    protected void newBatch() {
        this.batchPoints = BatchPoints
                .database(dbName())
                .retentionPolicy("defaultPolicy")
                .build();
    }

    private String databaseURL() {
        return "http://localhost:8086";
    }

    private String userName() {
        return "root";
    }

    private String password() {
        return "root";
    }

    private String dbName() {
        return "edf-audit";
    }

    public void parseCsvFile(String fileName) {
        System.out.println("start parsing file - '"+fileName+"'");
        long start = System.currentTimeMillis();
        if (fileName.endsWith("bz2")){
            System.out.println("ZIP Compressed file detected: " + fileName);
            try (Stream<String> stream = Bz2Files.lines(Paths.get(fileName))) {
                stream.forEach(this::pushCsvToInflux);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
                stream.forEach(this::pushCsvToInflux);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        influxDB.write(batchPoints);
        newBatch();
        double timeInSeconds = (System.currentTimeMillis() - start) / 1000.0;
        System.out.println(" -- done: written '"+this.allPoints+"' in '"+ timeInSeconds +"' seconds ");
    }

    private void pushCsvToInflux(String csvLine) {
        String[] elements = csvLine.split(delimiter());
        if (elements.length < 10 || elements[1].hashCode() != QUOTE.hashCode())
            return;
        int lastTimeHash = -1;
        Long lastTime = -1L;

        for (int c=0; c < this.floatFieldNames.size(); c++) {
            Point.Builder point = Point.measurement(measurement());
            String[] _tagNames = tagNames.get(c);
            String[] _floatFieldNames = floatFieldNames.get(c);
            String[] _stringFieldNames = stringFieldNames.get(c);

            for (int i = 0; i < elements.length; i++) {
                String timef = elements[timeField];
                Long time = lastTime;
                if (lastTimeHash != timef.hashCode()) {
                    lastTime = time = parseTimeField(timef);
                    lastTimeHash = timef.hashCode();
                }

                if (time != null) {
                    point.time(time, TimeUnit.MILLISECONDS);
                    if (_tagNames[i] != null) {
                        point.tag(_tagNames[i], String.valueOf(elements[i]));
                    } else if (_floatFieldNames[i] != null) {
                        point.addField(_floatFieldNames[i], Float.parseFloat(elements[i]));
                    } else if (_stringFieldNames[i] != null) {
                        point.addField(_stringFieldNames[i], String.valueOf(elements[i]));
                    }
                }
            }
            writeToInflux(point);
        }

    }

    private void writeToInflux(Point.Builder point) {
        batchPoints.point(point.build());
        allPoints++;
        if (batchPoints.getPoints().size() >= targetedBatchSize) {
            influxDB.write(batchPoints);
            newBatch();
        }

    }

    private Long parseTimeField(String timeField) {
        try {
            return format.parse(timeField).getTime();
        } catch (ParseException e) {
            return null;
        }
    }

    private static String measurement() {
        return "edf-outgoing";
    }

    private static String delimiter() {
        return ";";
    }

    public static CsvToInfluxDbWriter auditFileReader() {
        CsvToInfluxDbWriter csvToInfluxDbWriter = new CsvToInfluxDbWriter();
        int fieldCount = 49;

        String[] floatFieldNamesA = new String[fieldCount];
        String[] stringFieldNamesA = new String[fieldCount];
        String[] tagNamesA = new String[fieldCount];
        tagNamesA[3] = "ccyPair";
        tagNamesA[6] = "liq";
        floatFieldNamesA[26] = "mid";
        floatFieldNamesA[46] = "spread";

        csvToInfluxDbWriter.addPoint(floatFieldNamesA, stringFieldNamesA, tagNamesA);

        String[] floatFieldNamesB = new String[fieldCount];
        String[] stringFieldNamesB = new String[fieldCount];
        String[] tagNamesB = new String[fieldCount];
        tagNamesB[3] = "ccyPair";
        tagNamesB[7] = "liq";
        floatFieldNamesB[27] = "mid";
        floatFieldNamesB[47] = "spread";

        csvToInfluxDbWriter.addPoint(floatFieldNamesB, stringFieldNamesB, tagNamesB);

        String[] floatFieldNamesC = new String[fieldCount];
        String[] stringFieldNamesC = new String[fieldCount];
        String[] tagNamesC = new String[fieldCount];
        tagNamesC[3] = "ccyPair";
        tagNamesC[8] = "liq";
        floatFieldNamesC[28] = "mid";
        floatFieldNamesC[48] = "spread";

        csvToInfluxDbWriter.addPoint(floatFieldNamesC, stringFieldNamesC, tagNamesC);

        return csvToInfluxDbWriter;
    }

    private CsvToInfluxDbWriter addPoint(String[] floatFieldNames, String[] stringFieldNames, String[] tagNames) {
        this.floatFieldNames.add(floatFieldNames);
        this.stringFieldNames.add(stringFieldNames);
        this.tagNames.add(tagNames);
        return null;
    }

    public static void main(String[] args) {
        CsvToInfluxDbWriter csvToInfluxDbWriter = CsvToInfluxDbWriter.auditFileReader();
        csvToInfluxDbWriter.prepare();
        System.out.println("start " + new Date());
//        csvToInfluxDbWriter.parseCsvFile("/home/smeo/IdeaProjects/Experiments/influxdb-playground/src/main/resources/tesfile.csv");
        csvToInfluxDbWriter.parseAllFilesEndingWith(args[0], "bz2");
        System.out.println("done " + new Date());
        System.out.println("points :  " + csvToInfluxDbWriter.allPoints);
    }

    private void parseAllFilesEndingWith(String directory, String fileEnding) {
        File srcDir = new File(directory);
        if (srcDir.isDirectory()){
            File[] files = srcDir.listFiles();
            for (File file : files) {
                if (file.getAbsolutePath().endsWith(fileEnding)){
                    parseCsvFile(file.getAbsolutePath());
                    file.renameTo(new File(file.getAbsolutePath()+"_processed"));
                }
            }

        } else {
            System.err.println("'"+directory+"' is not a directory");
        }
    }

}

