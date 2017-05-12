package smeo.experiments.chronicle.replication.echo;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.ExcerptTailer;

import java.io.IOException;

/**
 * Receive echos and write them with received timestamps to a result chronicle
 */
public class EchoReceiver {
	final ExcerptTailer tailer;
	final ExcerptAppender resultAppender;

	public EchoReceiver(Chronicle reflectedEchosChronicle, ExcerptAppender resultAppender) throws IOException {
		this.tailer = reflectedEchosChronicle.createTailer().toEnd();
		this.resultAppender = resultAppender;
	}

	public void start() {
		new Thread(new Runnable() {
			EchoData dummyEcho = EchoInitiator.preallocateEchoDataObj();

			@Override
			public void run() {
				while (true) {
					if (tailer.nextIndex()) {
						try {
							dummyEcho.readExternal(tailer);
							dummyEcho.relectionReceived();
							resultAppender.startExcerpt();
							dummyEcho.writeExternal(resultAppender);
							resultAppender.finish();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
					}
				}
			}

		}).start();
	}

}
