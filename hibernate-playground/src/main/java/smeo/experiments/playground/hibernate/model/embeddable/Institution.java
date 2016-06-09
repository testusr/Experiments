package smeo.experiments.playground.hibernate.model.embeddable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.UUID;

/**
 * Created by truehl on 09.06.16.
 */
@Embeddable
public class Institution {
	@Basic
	@Column(nullable = false)
	private String institutionId;

	@Basic
	private String name;

	private Institution() {
		// neede by hibernate
	}

	private Institution(String institutionId, String name) {
		this.institutionId = institutionId;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static Institution create(String name) {
		return new Institution(UUID.randomUUID().toString(), name);
	}
}
