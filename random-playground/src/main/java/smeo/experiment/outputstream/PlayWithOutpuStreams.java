package smeo.experiment.outputstream;

import smeo.experiment.outputstream.models.ExampleClass;

import java.io.*;

/**
 * Created by smeo on 21.03.17.
 */
public class PlayWithOutpuStreams {
    public static void main(String[] args) {
        ExampleClass instanceA = ExampleClass.createRandom();
        ExampleClass toBeRead = ExampleClass.createRandom();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1000);
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

        try {
            final ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
            final int outputInitialSize = byteArrayOutputStream.size();
            final int inputInitialSize = byteArrayInputStream.read();

            instanceA.writeExternal(outputStream);
            outputStream.flush();
            final ObjectInputStream inputStream = new ObjectInputStream(byteArrayInputStream);
            toBeRead.readExternal(inputStream);

            int bytesWrittenPerObject = byteArrayOutputStream.size() - outputInitialSize;
            int bytesRead = byteArrayInputStream.read() - inputInitialSize;

            System.out.println("written: " + byteArrayOutputStream + " read: " + bytesRead);

            instanceA.writeExternal(outputStream);
            outputStream.flush();



        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
