package smeo.experiments.playground.envers.model;

import javax.persistence.Embeddable;

/**
 * Created by truehl on 10.05.16.
 */
@Embeddable
public class EmbeddableClass {
	private String embeddableAttribute1 = "default1";
	private String embeddableAttribute2 = "default2";;

	public String getEmbeddableAttribute2() {
		return embeddableAttribute2;
	}

	public void setEmbeddableAttribute2(String embeddableAttribute2) {
		this.embeddableAttribute2 = embeddableAttribute2;
	}

	public String getEmbeddableAttribute1() {
		return embeddableAttribute1;
	}

	public void setEmbeddableAttribute1(String embeddableAttribute1) {
		this.embeddableAttribute1 = embeddableAttribute1;
	}

}
