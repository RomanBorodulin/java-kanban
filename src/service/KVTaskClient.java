package service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KVTaskClient {
    private final HttpClient client;
    private final URI url;
    private final String apiToken;

    public KVTaskClient(URI url) {
        this.url = url;
        client = HttpClient.newHttpClient();
        apiToken = getApiToken(url);
    }

    public void put(String key, String json) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "/save/" + key + "?API_TOKEN=" + apiToken))
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new KVTaskClientException("Что-то пошло не так. Сервер вернул код состояния: "
                        + response.statusCode());
            }
        } catch (IOException | InterruptedException ex) {
            throw new KVTaskClientException("Во время выполнения сохранения возникла ошибка.");
        }

    }

    public String load(String key) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "/load/" + key + "?API_TOKEN=" + apiToken)).GET().build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                throw new KVTaskClientException("Что-то пошло не так. Сервер вернул код состояния: "
                        + response.statusCode());
            }
        } catch (IOException | InterruptedException ex) {
            throw new KVTaskClientException("Во время выполнения загрузки возникла ошибка.");
        }
    }

    private String getApiToken(final URI url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "/register")).GET().build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                throw new KVTaskClientException("Что-то пошло не так. Сервер вернул код состояния: "
                        + response.statusCode());
            }
        } catch (IOException | InterruptedException ex) {
            throw new KVTaskClientException("Во время выполнения регистрации возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    static class KVTaskClientException extends RuntimeException {
        public KVTaskClientException(String message) {
            super(message);
        }
    }
}

