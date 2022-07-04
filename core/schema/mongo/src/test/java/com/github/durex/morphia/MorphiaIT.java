package com.github.durex.morphia;

import static dev.morphia.query.experimental.filters.Filters.eq;
import static dev.morphia.query.experimental.filters.Filters.gt;
import static dev.morphia.query.experimental.filters.Filters.lte;
import static dev.morphia.query.experimental.updates.UpdateOperators.inc;
import static org.hamcrest.MatcherAssert.assertThat;

import com.github.durex.morphia.schema.Book;
import com.github.durex.morphia.schema.Employee;
import com.github.durex.morphia.schema.Publisher;
import com.mongodb.client.MongoClients;
import com.mongodb.client.result.UpdateResult;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.query.Query;
import org.bson.types.ObjectId;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MorphiaIT {
  private static Datastore datastore;

  @BeforeEach
  public void setUp() {
    var conn = "mongodb://root:secret@localhost:27017";
    var client = MongoClients.create(conn);

    datastore = Morphia.createDatastore(client, "myDb");
    // tell Morphia where to find your classes
    // can be called multiple times with different packages or classes
    datastore.getMapper().mapPackage("com.github.durex.morphia.schema");
    datastore.ensureIndexes();
  }

  @Test
  void testCreateEntity() {
    var elmer = new Employee("Elmer Fudd", 50000.0);
    datastore.save(elmer);

    var daffy = new Employee("Daffy Duck", 40000.0);
    datastore.save(daffy);

    var pepe = new Employee("Pepé Le Pew", 25000.0);
    datastore.save(pepe);

    elmer.getDirectReports().add(daffy);
    elmer.getDirectReports().add(pepe);

    datastore.save(elmer);

    Query<Employee> query = datastore.find(Employee.class);
    final long employees = query.count();

    assertThat(employees, Matchers.greaterThanOrEqualTo(3L));

    long underpaid = datastore.find(Employee.class).filter(lte("salary", 30000)).count();
    assertThat(underpaid, Matchers.greaterThanOrEqualTo(1L));

    final Query<Employee> underPaidQuery =
        datastore.find(Employee.class).filter(lte("salary", 30000));
    final UpdateResult results = underPaidQuery.update(inc("salary", 10000)).execute();

    assertThat(results.getModifiedCount(), Matchers.greaterThanOrEqualTo(1L));

    datastore.find(Employee.class).filter(gt("salary", 100000)).findAndDelete();
  }

  @Test
  void testBook() {
    var publisher = new Publisher(new ObjectId(), "Awesome Publisher");
    var book = new Book("9781565927186", "Learning Java", 3.95, publisher);
    var companionBook = new Book("9789332575103", "Java Performance Companion", 1.95, publisher);

    book.addCompanionBook(companionBook);

    datastore.save(companionBook);
    datastore.save(book);
    var isbnFilter = eq("isbn", "9781565927186");
    var result = datastore.find(Book.class).filter(isbnFilter).stream().count();
    assertThat(result, Matchers.greaterThanOrEqualTo(1L));
  }
}
