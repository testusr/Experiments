package smeo.experiments.liquibase.domain.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import smeo.experiments.liquibase.domain.model.Author;

@Repository
public interface AuthorRepository extends CrudRepository<Author, Integer> {
}
