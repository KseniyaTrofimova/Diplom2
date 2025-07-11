package prakticum;

import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.ValidatableResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import prakticum.model.User;
import prakticum.steps.UserSteps;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class UserLoginTest extends BaseTest{
    private final UserSteps userSteps = new UserSteps();
    private User user;
    private String accessToken;

    @Before
    public void setUp() {
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        user = User.builder()
                .email(RandomStringUtils.randomAlphabetic(6) + "@example.com")
                .password(RandomStringUtils.randomAlphanumeric(10))
                .name(RandomStringUtils.randomAlphabetic(8))
                .build();

        ValidatableResponse response = userSteps.createUser(user)
                .statusCode(SC_OK);
        accessToken = response.extract().path("accessToken");
    }

    @Test
    @DisplayName("Успешный вход существующего пользователя")
    @Description("Позитивный тест авторизации с валидными учетными данными")
    public void loginExistingUserSuccessfully() {
        userSteps.login(user)
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .body("user.email", equalTo(user.getEmail().toLowerCase()))
                .body("user.name", equalTo(user.getName()));
    }

    @Test
    @DisplayName("Попытка входа с неверным паролем")
    @Description("Негативный тест авторизации с неверным паролем")
    public void loginWithWrongPasswordFails() {
        User wrongPasswordUser = User.builder()
                .email(user.getEmail())
                .password("wrong_" + user.getPassword())
                .build();

        userSteps.login(wrongPasswordUser)
                .statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Попытка входа с неверным email")
    @Description("Негативный тест авторизации с несуществующим email")
    public void loginWithWrongEmailFails() {
        User wrongEmailUser = User.builder()
                .email("wrong_" + user.getEmail())
                .password(user.getPassword())
                .build();

        userSteps.login(wrongEmailUser)
                .statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Попытка входа без пароля")
    @Description("Негативный тест авторизации без указания пароля")
    public void loginWithoutPasswordFails() {
        User noPasswordUser = User.builder()
                .email(user.getEmail())
                .build();

        userSteps.login(noPasswordUser)
                .statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            userSteps.deleteUser(accessToken);
        }
    }
}

