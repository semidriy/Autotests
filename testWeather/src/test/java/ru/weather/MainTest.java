package ru.weather;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.weather.WebDriverSettings.getDriver;
@Slf4j
public class MainTest {

    private final String URL = "https://weather.visualcrossing.com/";
    private final String ENDPOINT = "VisualCrossingWebServices/rest/services/timeline/moscow?unitGroup=metric&key=FQTSRJCRDBYP2TZ7HJ68WGWKF";

    @Test
    void tempTest() {
        Double weatherFromYandex = getWeatherFromYandex();
        Double weatherFromApi = getWeatherFromApi();
        double weatherReslt = (Math.max(weatherFromApi, weatherFromYandex) - Math.min(weatherFromApi, weatherFromYandex));
        weatherReslt = new BigDecimal(weatherReslt).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
        assertEquals(weatherFromYandex, weatherFromApi, "\nОшибка, данные о погоде не совпадают \nДанные отличаются на "
                + weatherReslt);
    }
    private Double getWeatherFromApi() {
        return getTempMax(given()
                .baseUri(URL)
                .contentType(ContentType.JSON).accept(ContentType.JSON)
                .when()
                .get(ENDPOINT)
                .then()
                .assertThat().statusCode(200)
                .extract().response());
    }
    private Double getTempMax(Response response) {
        String tempMax = null;
        try {
            Object obj = new JSONParser().parse(response.asString());
            // Кастим obj в JSONObject
            JSONObject jo = (JSONObject) obj;
            // Достаем массив дней
            JSONArray days = (JSONArray) jo.get("days");
            JSONObject day = (JSONObject) days.get(0);
            tempMax = day.get("tempmax").toString();
            log.info("Погода в Москве, как говорит сервис Weather Data & API, равна " + tempMax );
        } catch (Exception e) {
            log.error("Ошибка в преобразовании " + e.getMessage());
        }
        return Double.valueOf(tempMax);
    }
    private Double getWeatherFromYandex() {
        WebDriver driver = getDriver();
        driver.get("https://yandex.ru/pogoda/");
        String title = driver.getTitle();
        assertEquals("Прогноз погоды в Москве на 10 дней — Яндекс.Погода", title);
        String temp = driver.findElement(By.className("temp__value_with-unit")).getText();
        log.info("Погода в Москве, как говорит сервис Яндекс, равна " + temp);
        driver.quit();
        return Double.valueOf(temp.replace("−","-"));
    }
}