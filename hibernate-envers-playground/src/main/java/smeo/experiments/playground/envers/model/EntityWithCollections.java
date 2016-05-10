package smeo.experiments.playground.envers.model;

import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by truehl on 10.05.16.
 */
@Entity
@Audited
public class EntityWithCollections {
	@Id
	@Column(name = "ID", unique = true, nullable = false)
	private final String id;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "LIST_OF_EMBEDDABLES")
	private List<EmbeddableClass> listOfEmbeddables = new ArrayList<>();

	public EntityWithCollections(String id) {
		this.id = id;
	}

	public void addEmbedabble(EmbeddableClass embeddable) {
		listOfEmbeddables.add(embeddable);
	}

}
