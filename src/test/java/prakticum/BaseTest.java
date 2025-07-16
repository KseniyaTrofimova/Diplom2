package prakticum;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import org.junit.Before;
import prakticum.util.RestConfig;

public class BaseTest {

    @Before
    public void startUp(){
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .setBaseUri(RestConfig.HOST)
                .setContentType(ContentType.JSON)
                .build();
    }
}
