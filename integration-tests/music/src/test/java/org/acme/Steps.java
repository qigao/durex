package org.acme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.List;
import java.util.Map;

public class Steps {
  private static final String USER_ID = "9b5f49ab-eea9-45f4-9d66-bcf56a531b85";
  private static final String USERNAME = "TOOLSQA-Test";
  private static final String PASSWORD = "Test@@123";
  private static final String BASE_URL = "https://bookstore.toolsqa.com";

  private static String token;
  private static Response response;
  private static String jsonString;
  private static String bookId;

  @Given("I am an authorized user")
  public void iAmAnAuthorizedUser() {

    RestAssured.baseURI = BASE_URL;
    RequestSpecification request = RestAssured.given();

    request.header("Content-Type", "application/json");
    response =
        request
            .body("{ \"userName\":\"" + USERNAME + "\", \"password\":\"" + PASSWORD + "\"}")
            .post("/Account/v1/GenerateToken");

    String jsonString = response.asString();
    token = JsonPath.from(jsonString).get("token");
  }

  @Given("A list of books are available")
  public void listOfBooksAreAvailable() {
    RestAssured.baseURI = BASE_URL;
    RequestSpecification request = RestAssured.given();
    response = request.get("/BookStore/v1/Books");

    jsonString = response.asString();
    List<Map<String, String>> books = JsonPath.from(jsonString).get("books");
    assertTrue(books.size() > 0);

    bookId = books.get(0).get("isbn");
  }

  @When("I add a book to my reading list")
  public void addBookInList() {
    RestAssured.baseURI = BASE_URL;
    RequestSpecification request = RestAssured.given();
    request.header("Authorization", "Bearer " + token).header("Content-Type", "application/json");

    response =
        request
            .body(
                "{ \"userId\": \""
                    + USER_ID
                    + "\", "
                    + "\"collectionOfIsbns\": [ { \"isbn\": \""
                    + bookId
                    + "\" } ]}")
            .post("/BookStore/v1/Books");
  }

  @Then("The book is added")
  public void bookIsAdded() {
    assertEquals(201, response.getStatusCode());
  }

  @When("I remove a book from my reading list")
  public void removeBookFromList() {
    RestAssured.baseURI = BASE_URL;
    RequestSpecification request = RestAssured.given();

    request.header("Authorization", "Bearer " + token).header("Content-Type", "application/json");

    response =
        request
            .body("{ \"isbn\": \"" + bookId + "\", \"userId\": \"" + USER_ID + "\"}")
            .delete("/BookStore/v1/Book");
  }

  @Then("The book is removed")
  public void bookIsRemoved() {
    assertEquals(204, response.getStatusCode());

    RestAssured.baseURI = BASE_URL;
    RequestSpecification request = RestAssured.given();

    request.header("Authorization", "Bearer " + token).header("Content-Type", "application/json");

    response = request.get("/Account/v1/User/" + USER_ID);
    assertEquals(200, response.getStatusCode());

    jsonString = response.asString();
    List<Map<String, String>> booksOfUser = JsonPath.from(jsonString).get("books");
    assertEquals(0, booksOfUser.size());
  }
}
