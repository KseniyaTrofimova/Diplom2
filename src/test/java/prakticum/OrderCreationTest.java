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
import prakticum.model.Order;
import prakticum.model.User;
import prakticum.steps.OrderSteps;
import prakticum.steps.UserSteps;

import java.util.Collections;
import java.util.List;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.*;

public class OrderCreationTest extends BaseTest {
    private final UserSteps userSteps = new UserSteps();
    private final OrderSteps orderSteps = new OrderSteps();
    private User user;
    private String accessToken;
    private List<String> validIngredients;

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

        validIngredients = orderSteps.getValidIngredients();
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и валидными ингредиентами")
    @Description("Позитивный тест создания заказа авторизованным пользователем")
    public void createOrderWithAuthAndValidIngredients() {
        Order order = new Order(validIngredients);

        orderSteps.createOrder(order, accessToken)
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("order.owner.name", equalTo(user.getName()))
                .body("order.ingredients", not(empty()));
    }

    @Test
    @DisplayName("Создание заказа без авторизации")
    @Description("Создание заказа без передачи токена авторизации")
    public void createOrderWithoutAuthorization() {
        Order order = new Order(validIngredients);

        orderSteps.createOrder(order, null)
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("order.owner", nullValue())
                .body("order.ingredients", not(empty()));
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов")
    @Description("Негативный тест создания заказа без указания ингредиентов")
    public void createOrderWithoutIngredients() {
        Order emptyOrder = new Order(Collections.emptyList());

        orderSteps.createOrder(emptyOrder, accessToken)
                .statusCode(SC_BAD_REQUEST)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создание заказа с неверным хешем ингредиентов")
    @Description("Негативный тест с невалидными идентификаторами ингредиентов")
    public void createOrderWithInvalidIngredientHash() {
        List<String> invalidIngredients = List.of("invalid_ingredient_1", "invalid_ingredient_2");
        Order invalidOrder = new Order(invalidIngredients);

        orderSteps.createOrder(invalidOrder, accessToken)
                .statusCode(SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("Создание заказа с неполным списком ингредиентов")
    @Description("Негативный тест с частично невалидными ингредиентами")
    public void createOrderWithPartiallyValidIngredients() {
        List<String> mixedIngredients = List.of(
                validIngredients.get(0),
                "invalid_ingredient_1",
                validIngredients.get(1)
        );
        Order mixedOrder = new Order(mixedIngredients);

        orderSteps.createOrder(mixedOrder, accessToken)
                .statusCode(SC_INTERNAL_SERVER_ERROR);
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            userSteps.deleteUser(accessToken);
        }
    }
}
