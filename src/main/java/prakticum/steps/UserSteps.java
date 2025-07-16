package prakticum.steps;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import prakticum.model.User;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_ACCEPTED;

public class UserSteps {
    private final String REGISTER_USER = "/api/auth/register";
    private final String DELETE_USER = "/api/auth/user";
    private final String LOGIN_USER = "/api/auth/login";

    @Step ("Метод создания пользователя")
    public ValidatableResponse createUser(User user) {
        return given()
                .body(user)
                .when()
                .post(REGISTER_USER)
                .then();
    }

    @Step("Удаление пользователя")
    public void deleteUser(String accessToken) {
        given()
                .header("Authorization", accessToken)
                .when()
                .delete(DELETE_USER)
                .then()
                .statusCode(SC_ACCEPTED);
    }

    @Step("Логин пользователя")
    public ValidatableResponse login(User user) {
        return given()
                .body(user)
                .when()
                .post(LOGIN_USER)
                .then();
    }
}
