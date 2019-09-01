package smeo.experiments.liquibase.domain.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "authors")
@Data
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class Author {

  /**
   * Explaining strategies: https://thoughts-on-java.org/jpa-generate-primary-keys/
   */
  @Id
  //@GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, unique = true)
  private String name;

  public Author(Integer id, String name) {
    this.id = id;
    this.name = name;
  }

}
