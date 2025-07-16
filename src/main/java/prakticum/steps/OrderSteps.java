package prakticum.steps;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import prakticum.model.Order;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_OK;

public class OrderSteps {
    private final String CREATE_ORDER = "/api/orders";
    private final String INGREDIENTS_LIST = "/api/ingredients";

    @Step("Создание заказа")
    public ValidatableResponse createOrder(Order order, String accessToken) {
        if (accessToken != null) {
            return given()
                    .header("Authorization", accessToken)
                    .body(order)
                    .when()
                    .post(CREATE_ORDER)
                    .then();
        } else {
            return given()
                    .body(order)
                    .when()
                    .post(CREATE_ORDER)
                    .then();
        }
    }

    @Step("Получение списка валидных ингредиентов")
    public List<String> getValidIngredients() {
        return given()
                .when()
                .get(INGREDIENTS_LIST)
                .then()
                .statusCode(SC_OK)
                .extract()
                .jsonPath()
                .getList("data._id");
    }
}
