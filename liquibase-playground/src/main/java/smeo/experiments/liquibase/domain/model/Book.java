package smeo.experiments.liquibase.domain.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class Book {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(nullable = false, unique = true)
  private String title;

  @Column(nullable = true, unique = true)
  private String subtitle;

  @ManyToOne
  @JoinColumn(name = "author")
  private Author author;

  public Book(int id, String title, Author author) {
    this.id = id;
    this.title = title;
    this.author = author;
  }
}
