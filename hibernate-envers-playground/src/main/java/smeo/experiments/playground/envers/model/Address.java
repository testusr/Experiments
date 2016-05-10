package smeo.experiments.playground.envers.model;

import org.hibernate.envers.Audited;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by truehl on 10.05.16.
 */
@Audited
@Entity
@Table(name = "Adress")
public class Address {
	@Id
	@Column(name = "ID", unique = true, nullable = false)
	private final String id;

	public Address(String id) {
		this.id = id;
	}

	private String street;
	private long postcode;
	private int houseNo;

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public long getPostcode() {
		return postcode;
	}

	public void setPostcode(long postcode) {
		this.postcode = postcode;
	}

	public int getHouseNo() {
		return houseNo;
	}

	public void setHouseNo(int houseNo) {
		this.houseNo = houseNo;
	}
}
