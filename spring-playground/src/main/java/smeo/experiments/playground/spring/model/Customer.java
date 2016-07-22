package smeo.experiments.playground.spring.model;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by truehl on 05.07.16.
 */
public class Customer {
	private Person person;
	private int type;
	private String action;
	//getter and setter methods

	@Autowired
	public void setPerson(Person person) {
		this.person = person;
	}
}
