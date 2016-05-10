package smeo.experiments.playground.envers.model;

import javax.persistence.Embeddable;

/**
 * Created by truehl on 10.05.16.
 */
@Embeddable
public class EmbeddedPosition {
	private final String description;
	private final long salaryGroup;

	EmbeddedPosition() {
		// needed by hibernate
		description = "";
		salaryGroup = -100;
	}

	public EmbeddedPosition(String description, long salaryGroup) {
		this.description = description;
		this.salaryGroup = salaryGroup;
	}

	public static EmbeddedPosition none() {
		return new EmbeddedPosition("no position", -1);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		EmbeddedPosition that = (EmbeddedPosition) o;

		if (salaryGroup != that.salaryGroup)
			return false;
		return description.equals(that.description);

	}

	@Override
	public int hashCode() {
		int result = description.hashCode();
		result = 31 * result + (int) (salaryGroup ^ (salaryGroup >>> 32));
		return result;
	}

	@Override
	public String toString() {
		return "EmbeddedPosition{" +
				"description='" + description + '\'' +
				", salaryGroup=" + salaryGroup +
				'}';
	}
}
