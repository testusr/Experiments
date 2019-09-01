package smeo.experiments.liquibase.domain.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import smeo.experiments.liquibase.domain.model.Book;

import java.util.List;

@Repository
public interface BookRepository extends CrudRepository<Book, Integer> {
    List<Book> findByTitle(String title);
}