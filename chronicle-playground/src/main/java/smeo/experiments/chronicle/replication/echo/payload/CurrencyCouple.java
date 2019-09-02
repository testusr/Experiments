package smeo.experiments.chronicle.replication.echo.payload;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

public class CurrencyCouple implements Externalizable {
	private Currency base;
	private Currency quote;

	public CurrencyCouple() {
		base = new Currency(1);
		quote = new Currency(2);
	}

	public CurrencyCouple(String baseCy, String quoteCy) {
		this(new Currency(1), new Currency(2));
	}

	public CurrencyCouple(final Currency base, final Currency quote) {
		this.base = base;
		this.quote = quote;
	}

	public Currency getBase() {
		return base;
	}

	public Currency getQuote() {
		return quote;
	}

	/**
	 * Creates a currency couple from currency couple iso format
	 * example: EURUSD
	 */
	public static CurrencyCouple parseTexIso(String string) {
		if (string.length() != 6) {
			throw new RuntimeException("Incorrect CCY pair format");
		}
		return new CurrencyCouple(string.substring(0, 3), string.substring(3));
	}

	/**
	 * Creates a currency couple from currency couple symbol format
	 * example: EUR/USD
	 */
	public static CurrencyCouple parseSymbol(String string) {
		if (string.length() != 7) {
			throw new RuntimeException("Incorrect CCY pair format");
		}
		return new CurrencyCouple(string.substring(0, 3), string.substring(4));
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		base.writeExternal(out);
		quote.writeExternal(out);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		base.readExternal(in);
		quote.readExternal(in);
	}

	public static CurrencyCouple newPreallocate() {
		return new CurrencyCouple();
	}

	public CurrencyCouple internalize(CurrencyCouple source) {
		this.base.internalize(source.base);
		this.quote.internalize(source.quote);
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		CurrencyCouple that = (CurrencyCouple) o;

		if (!Objects.equals(base, that.base))
			return false;

		return Objects.equals(quote, that.quote);

	}

	@Override
	public int hashCode() {
		int result = base.hashCode();
		result = 31 * result + quote.hashCode();
		return result;
	}

}
