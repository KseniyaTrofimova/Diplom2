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

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class CreateUserTest extends BaseTest {
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

    }

    @Test
    @DisplayName("Успешное создание уникального пользователя")
    @Description("Позитивный тест создания пользователя с валидными данными")
    public void createUniqueUser() {
        ValidatableResponse response = userSteps.createUser(user)
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .body("user.email", equalTo(user.getEmail().toLowerCase()))
                .body("user.name", equalTo(user.getName()));

        accessToken = response.extract().path("accessToken");
    }

    @Test
    @DisplayName("Попытка создания уже зарегистрированного пользователя")
    @Description("Негативный тест создания пользователя с существующим email")
    public void createDuplicateUser() {
        ValidatableResponse createResponse = userSteps.createUser(user);
        accessToken = createResponse.extract().path("accessToken");

        userSteps.createUser(user)
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @Test
    @DisplayName("Попытка создания пользователя без пароля")
    @Description("Негативный тест создания пользователя без обязательного поля password")
    public void createUserWithoutPassword() {
        User userWithoutPassword = User.builder()
                .email(user.getEmail())
                .name(user.getName())
                .build();

        userSteps.createUser(userWithoutPassword)
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Попытка создания пользователя без email")
    @Description("Негативный тест создания пользователя без обязательного поля email")
    public void createUserWithoutEmail() {
        User userWithoutEmail = User.builder()
                .password(user.getPassword())
                .name(user.getName())
                .build();

        userSteps.createUser(userWithoutEmail)
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Попытка создания пользователя без имени")
    @Description("Негативный тест создания пользователя без обязательного поля name")
    public void createUserWithoutName() {
        User userWithoutName = User.builder()
                .email(user.getEmail())
                .password(user.getPassword())
                .build();

        userSteps.createUser(userWithoutName)
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            userSteps.deleteUser(accessToken);
        }
    }
}