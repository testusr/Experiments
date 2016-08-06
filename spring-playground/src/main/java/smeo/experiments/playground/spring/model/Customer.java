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
	private String loadingType;
	//getter and setter methods

	Customer(){
		System.out.println("Customer created");
	}

	@Autowired
	public void setPerson(Person person) {
		this.person = person;
	}

	public Person getPerson() {
		return person;
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

	public void setLoadingType(java.lang.String loadingType) {
		this.loadingType = loadingType;
	}

	public java.lang.String getLoadingType() {
		return loadingType;
	}
}
