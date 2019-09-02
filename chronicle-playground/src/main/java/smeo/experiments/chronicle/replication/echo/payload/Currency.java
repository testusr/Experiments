package smeo.experiments.chronicle.replication.echo.payload;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Mde implementation of Currency class that allows garbage-less usage
 */
public class Currency implements Externalizable {

	private int index;

	Currency(int index) {
		this.index = index;
	}

	int getIndex() {
		return index;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(index);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		index = in.readInt();
	}

	public void internalize(Currency other) {
		index = other.index;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Currency currency = (Currency) o;
		return index == currency.index;
	}

	@Override
	public int hashCode() {
		return index;
	}
}
