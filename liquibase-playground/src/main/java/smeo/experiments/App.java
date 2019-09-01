package smeo.experiments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import smeo.experiments.liquibase.domain.model.Author;
import smeo.experiments.liquibase.domain.model.Book;
import smeo.experiments.liquibase.domain.repo.AuthorRepository;
import smeo.experiments.liquibase.domain.repo.BookRepository;

/**
 * Hello world!
 *
 */
@EnableJpaRepositories("smeo.experiments.liquibase.domain.repo")
@EntityScan("smeo.experiments.liquibase.domain.model")
@SpringBootApplication
public class App implements CommandLineRunner {
    public static void main( String[] args )
    {
        SpringApplication.run(App.class, args);
    }

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private AuthorRepository authorRepository;


    @Override
    public void run(String... args) throws Exception {
        logger.info("Student id 10001 -> {}", bookRepository.findById(10001));

        logger.info("All users 1 -> {}", bookRepository.findAll());

        //Insert
        Author meAuthor = new Author(667, "MeNewAuth");
        logger.info("Inserting author -> ", authorRepository.save(meAuthor));

        Book myNewBook = new Book(666, "MyNewBooks", meAuthor);
        logger.info("Inserting book-> {}", bookRepository.save(myNewBook));

        //Update
        meAuthor.setName("MeNewAuthor");
        logger.info("Update author -> {}", authorRepository.save(meAuthor));
        logger.info("All users 2 -> {}", bookRepository.findAll());
        logger.info("waiting for key pressed to continue");
        System.in.read();

        bookRepository.deleteById(myNewBook.getId());
        logger.info("All users 2 -> {}", bookRepository.findAll());
        logger.info("waiting for key pressed to terminate");
        System.in.read();
    }
}
