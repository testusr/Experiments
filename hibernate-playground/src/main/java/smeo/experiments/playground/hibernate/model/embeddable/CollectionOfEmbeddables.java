package smeo.experiments.playground.hibernate.model.embeddable;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * https://en.wikibooks.org/wiki/Java_Persistence/ElementCollection#Primary_keys_in_CollectionTable
 */
@Entity
public class CollectionOfEmbeddables {

	@Id
	private String id;

	@ElementCollection
	@CollectionTable(name = "EMB_INSTITUTION", joinColumns = @JoinColumn(name = "OWNER_ID"))
	public List<Institution> institutions = new ArrayList<>();

	private CollectionOfEmbeddables(String id) {
		this.id = id;
	}

	public static CollectionOfEmbeddables create() {
		return new CollectionOfEmbeddables(UUID.randomUUID().toString());
	}

}
