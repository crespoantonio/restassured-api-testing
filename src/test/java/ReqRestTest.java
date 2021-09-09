import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static io.restassured.path.json.JsonPath.from;


public class ReqRestTest {

    @BeforeAll
    public static void setUp(){
        RestAssured.baseURI = "https://reqres.in";
        RestAssured.basePath = "/api";
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());

        RestAssured.requestSpecification = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .build();
    }

    @Test
    public void loginTest(){
        given()
                .body("{\n" +
                        "    \"email\": \"eve.holt@reqres.in\",\n" +
                        "    \"password\": \"cityslicka\"\n" +
                        "}")
                .post("/login")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("token", notNullValue());
    }

    @Test
    public void getSingleUserTest(){
        given()
                .get("/users/2")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("data.id", equalTo(2));
    }

    @Test
    public void deleteUserTest(){
        given()
                .delete("/users/2")
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    public void patchUserTest(){
        String nameUpdated = given()
                .when()
                .body("{\n" +
                        "    \"name\": \"morpheus\",\n" +
                        "    \"job\": \"zion resident\"\n" +
                        "}")
                .patch("/users/2")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getString("name");

        assertThat(nameUpdated, equalTo("morpheus"));

    }

    @Test
    public void putUserTest(){
        String jobUpdated = given()
                .when()
                .body("{\n" +
                        "    \"name\": \"morpheus\",\n" +
                        "    \"job\": \"zion resident\"\n" +
                        "}")
                .put("/users/2")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getString("job");

        assertThat(jobUpdated, equalTo("zion resident"));
    }

    @Test
    public void getAllUserTest(){
       Response response = given()
                .get("users?page=2");

       Headers headers =  response.getHeaders();
       int statusCode = response.getStatusCode();
       String body =  response.getBody().asString();
       String contentType = response.getContentType();
       assertThat(statusCode, equalTo(HttpStatus.SC_OK));
       System.out.println("body: "+body);
       System.out.println("content type: "+contentType);
       System.out.println("Headers: "+headers.toString());
    }

    @Test
    public void getAllUsersTest2(){
        String response = given()
                .when().get("users?page=2").then().extract().body().asString();

        int page = from(response).get("page");
        int totalPages = from(response).get("total_pages");
        int idFirstUser = from(response).get("data[0].id");

        System.out.println("Page: " + page);
        System.out.println("Total pages: " + totalPages);
        System.out.println("id first: " + idFirstUser);

        List<Map> userWithIdGreaterThan10 = from(response).get("data.findAll {user -> user.id>10}");
        String email = userWithIdGreaterThan10.get(0).get("email").toString();

        List<Map> user = from(response).get("data.findAll {user -> user.id>10 && user.last_name == 'Howell'}");
        int id = Integer.valueOf(user.get(0).get("id").toString());
    }

    @Test
    public void createUserTest() {
        String response = given()
                .when()
                .body("{\n" +
                        "    \"name\": \"morpheus\",\n" +
                        "    \"job\": \"leader\"\n" +
                        "}")
                .post("users")
                .then().extract().body().asString();
        User user = from(response).getObject("", User.class);
        System.out.println(user.getId());
        System.out.println(user.getJob());

    }
}
