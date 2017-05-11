package smeo.experiments.chronicle.replication.echo.payload;

import org.jetbrains.annotations.NotNull;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

/**
 * Chronicle-serializable stream id object
 */
public class EfficientStreamId implements Externalizable, Comparable<EfficientStreamId> {

	private EfficientId marketMakerName;
	private EfficientId streamName;
	// TODO: we should not need this...
	private boolean isExclusive;

	public EfficientStreamId() {
		marketMakerName = new EfficientId();
		streamName = new EfficientId();
	}

	public EfficientStreamId(String marketMakerName, String streamName, boolean isExclusive) {
		this(new EfficientId(marketMakerName), new EfficientId(streamName), isExclusive);
	}

	public EfficientStreamId(EfficientId marketMakerName, EfficientId streamName, boolean isExclusive) {
		this.streamName = streamName;
		this.marketMakerName = marketMakerName;
		this.isExclusive = isExclusive;
	}

	public EfficientId getStreamName() {
		return streamName;
	}

	public EfficientId getMarketMakerName() {
		return marketMakerName;
	}

	public boolean isExclusive() {
		return isExclusive;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		marketMakerName.writeExternal(out);
		streamName.writeExternal(out);
		out.writeBoolean(isExclusive);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		marketMakerName.readExternal(in);
		streamName.readExternal(in);
		isExclusive = in.readBoolean();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		EfficientStreamId that = (EfficientStreamId) o;
		return isExclusive == that.isExclusive &&
				Objects.equals(marketMakerName, that.marketMakerName) &&
				Objects.equals(streamName, that.streamName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(marketMakerName, streamName, isExclusive);
	}

	@Override
	public String toString() {
		return "{" +
				marketMakerName +
				": " + streamName +
				'}';
	}

	public void internalize(EfficientStreamId streamId) {
		this.marketMakerName.internalize(streamId.marketMakerName);
		this.streamName.internalize(streamId.streamName);
		this.isExclusive = streamId.isExclusive();
	}

	@Override
	/**
	 * compare to might have different results when comparing the original strings ! Delivers 0 if
	 * ids are equal and a defined order if not.
	 */
	public int compareTo(@NotNull EfficientStreamId o) {
		final int result = marketMakerName.compareTo(o.marketMakerName);
		if (result == 0) {
			return streamName.compareTo(o.streamName);
		}
		return result;
	}
}
