import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import okhttp3.*;
import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;

public class ApiTestJava {

    String baseURL = "https://fakerestapi.azurewebsites.net/api/v1/";
    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .build();

    @Test
    public void getRequest() throws IOException {
//        String endpoint = "Activities";
        String endpoint = "Activities/2";
        Request request = new Request.Builder()
                .url(baseURL + endpoint)
                .get()
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        String json = Objects.requireNonNull(response.body()).string();
        //System.out.println(response.peekBody(4096).string());
        System.out.println(json);

        // Test Array
//        JSONArray jsonArray = new JSONArray(json);
//        JSONObject jsonObject = jsonArray.getJSONObject(0);
//        String dueDateValue = jsonObject.getString("dueDate");
//        System.out.println(dueDateValue);
//        Assert.assertTrue(dueDateValue.contains("2021"), "Response Due Date did not contain the year '2021'");

        // Test Element
        JSONObject jsonObject = new JSONObject(json);
        System.out.println(jsonObject.getString("dueDate"));
        Assert.assertTrue(jsonObject.getString("dueDate").contains("2022"), "Response Due Date did not contain the year '2022'");
    }

    @Test
    public void sendGetWithCurl() throws IOException {
        String command = "curl -X GET https://fakerestapi.azurewebsites.net/api/v1/Activities/2";
        Process process = Runtime.getRuntime().exec(command);

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String output;
        StringBuilder response = new StringBuilder();
        while ((output = stdInput.readLine()) != null) {
            response.append(output);
            System.out.println(output);
        }

        Assert.assertTrue(response.toString().contains("2022"), "Response does not contain '2022'");

        process.destroy();
    }

    @Test
    public void sendGetWithRestAssured() {
        RestAssured.baseURI = "https://fakerestapi.azurewebsites.net/api/v1/";
        RequestSpecification httpRequest = given();
        io.restassured.response.Response response = httpRequest.get("Activities/2");
        io.restassured.response.ResponseBody body = response.getBody();

        String bodyAsString = body.asString();
        System.out.println(bodyAsString);
        Assert.assertTrue(bodyAsString.contains("2022"), "Response body contains 2022");

        httpRequest.given()
                .when()
                .get("https://fakerestapi.azurewebsites.net/api/v1/Activities")
                .then()
                .assertThat()
                .statusCode(200)
                .assertThat()
                .body("title", Matchers.hasItem("Activity 2"));
    }

    @Test
    public void postRequest() throws IOException {
        String endpoint = "Activities";
        String title = "My New Activity";
        String reqBody = String.format("'{" +
                "  \"id\": 2," +
                "  \"title\": \"%s\"," +
                "  \"dueDate\": \"2022-02-10T23:16:22.469Z\"," +
                "  \"completed\": true\n" +
                "}", title);

        RequestBody body = RequestBody.create(reqBody, MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(baseURL + endpoint)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        String strResp = Objects.requireNonNull(response.body()).string();
        System.out.println(strResp);
    }

    @Test
    public void sendPostWithCurl() throws Exception {
        String title = "My New Activity";
        String postBody = String.format("{'id': 2, 'title': %s,'[dueDate': '2022-02-10T23:16:22.469Z','completed': true}", title);

        String command = String.format("curl -X POST 'https://fakerestapi.azurewebsites.net/api/v1/Activities' --header 'Accept application/json' --data-raw '%s'", postBody);
        System.out.println(command);
        Process process = Runtime.getRuntime().exec(command);

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String output;
        StringBuilder response = new StringBuilder();
        while ((output = stdInput.readLine()) != null) {
            response.append(output);
            System.out.println(output);
        }

        System.out.println(response);

        process.destroy();
    }

    @Test
    public void sendPostWithRestAssured() {
        RestAssured.baseURI = "https://fakerestapi.azurewebsites.net/api/v1/";
        String title = "My New Activity";
        String postBody = String.format("{'id': 2, 'title': %s,'[dueDate': '2022-02-10T23:16:22.469Z','completed': true}", title);

        io.restassured.response.Response response = given()
                .header("Content-type", "application/json")
                .and()
                .body(postBody)
                .when()
                .post(baseURL+ "Activity/2")
                .then()
                .assertThat()
                .body("title", Matchers.hasItem(title))
                .extract().response();
    }



    @Test
    public void putTest() throws IOException {
        String endpoint = "Activities/2";
        String title = "Updated Activity";
        String reqBody = String.format(
                "{" +
                "  \"id\": 2," +
                "  \"title\": \"%s\"," +
                "  \"dueDate\": \"2022-02-10T23:16:22.469Z\"," +
                "  \"completed\": true\n" +
                "}", title);
        RequestBody body = RequestBody.create(reqBody, MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(baseURL + endpoint)
                .put(body)
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        String strResp = Objects.requireNonNull(response.body()).string();
        System.out.println("Response: " + strResp);
    }

    @Test
    public void deleteTest() throws IOException {
        String id = "5";
        String endpoint = String.format("Activities/%s", id);

        Request request = new Request.Builder()
                .url(baseURL + endpoint)
                .delete()
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        int statusCode = response.code();
        System.out.println(statusCode);

        Assert.assertEquals(statusCode, 200, "Status code was not 200 as expected.");
    }

}
