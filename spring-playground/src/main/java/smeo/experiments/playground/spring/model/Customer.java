package smeo.experiments.playground.spring.model;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by truehl on 05.07.16.
 */
public class Customer {
	private Person person;
	private int String;
	private String action;
	private String type;
	//getter and setter methods

	@Autowired
	public void setPerson(Person person) {
		this.person = person;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getAction() {
		return action;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
