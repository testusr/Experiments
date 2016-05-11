package smeo.experiments.playground.envers.model;

import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by smeo on 09.05.16.
 */
@Audited
@Entity
@Table(name = "Employee")
public class EmployeeEntity implements Serializable {
	private static final long serialVersionUID = -1798070786993154676L;
	@Id
	@Column(name = "ID", nullable = false)
	private Integer employeeId;

	// Attributes
	@Column(name = "EMAIL", unique = true, nullable = false, length = 100)
	private String email;
	@Column(name = "FIRST_NAME", unique = false, nullable = false, length = 100)
	private String firstName;
	@Column(name = "LAST_NAME", unique = false, nullable = false, length = 100)
	private String lastName;

	// References

	@Embedded
	// hibernate does not allow this to be null
	private EmbeddedPosition mainPosition = EmbeddedPosition.none();

	@ElementCollection
	@CollectionTable(name = "LIST_SUBPOSITIONS")
	List<EmbeddedPosition> subPositions = new ArrayList<>();

	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private Address adress;

	public void addSubPosition(EmbeddedPosition embeddedPosition) {
		subPositions.add(embeddedPosition);
	}

	public Integer getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Address getAdress() {
		return adress;
	}

	public void setAdress(Address adress) {
		this.adress = adress;
	}

	public EmbeddedPosition getMainPosition() {
		return mainPosition;
	}

	public void setMainPosition(EmbeddedPosition mainPosition) {
		this.mainPosition = mainPosition;
	}

	@Override
	public String toString() {
		return "EmployeeEntity{" +
				"employeeId=" + employeeId +
				", email='" + email + '\'' +
				", firstName='" + firstName + '\'' +
				", lastName='" + lastName + '\'' +
				", mainPosition=" + mainPosition +
				", subPositions=" + subPositions +
				", adress=" + adress +
				'}';
	}
}
