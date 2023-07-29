package test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.HttpTaskServer;
import server.KVServer;
import service.Adapters;
import service.KVTaskClient;
import service.Managers;
import service.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskServerTest {
    static final int PORT = 8080;
    static final String PROTOCOL_AND_HOST_NAME = "http://localhost:";
    TaskManager taskManager;
    KVServer kvServer;
    HttpTaskServer httpTaskServer;
    static HttpClient client;
    static KVTaskClient kvTaskClient;
    static Gson gson;
    Task taskFromJson;
    Epic epicFromJson;
    Subtask subtaskFromJson;
    String task1;
    String task2;
    String epic1;
    String epic2;
    String subtask1;
    String subtask2;
    String path;
    HttpRequest request;
    HttpResponse<String> response;

    @BeforeAll
    static void beforeAll() {

        gson = new GsonBuilder().setPrettyPrinting().serializeNulls()
                .registerTypeAdapter(Task.class, new Adapters.TaskAdapter())
                .registerTypeAdapter(Epic.class, new Adapters.EpicAdapter())
                .registerTypeAdapter(Subtask.class, new Adapters.SubtaskAdapter())
                .create();
        client = HttpClient.newHttpClient();
        kvTaskClient = new KVTaskClient(URI.create("http://localhost:" + KVServer.PORT));
    }

    @BeforeEach
    void beforeEach() throws IOException {
        kvServer = new KVServer();
        kvServer.start();
        httpTaskServer = new HttpTaskServer();
        httpTaskServer.start();
        initializeTasks();
    }

    @AfterEach
    void afterEach() {
        httpTaskServer.stop();
        kvServer.stop();
    }

    @Test
    void shouldPostTask() throws IOException, InterruptedException {
        path = String.format("%s%s/tasks/task/", PROTOCOL_AND_HOST_NAME, PORT);
        request = getRequestPOST(URI.create(path), task1);
        response = getResponse(request);
        assertEquals(201, response.statusCode());
        taskFromJson = gson.fromJson(task1, Task.class);
        taskFromJson.setId(1);
        taskManager = Managers.getDefault();
        assertEquals(taskFromJson, taskManager.getTaskById(1));

    }

    @Test
    void shouldPostEpic() throws IOException, InterruptedException {
        path = String.format("%s%s/tasks/epic/", PROTOCOL_AND_HOST_NAME, PORT);
        request = getRequestPOST(URI.create(path), epic1);
        response = getResponse(request);
        assertEquals(201, response.statusCode());
        epicFromJson = gson.fromJson(epic1, Epic.class);
        epicFromJson.setId(1);
        taskManager = Managers.getDefault();
        assertEquals(epicFromJson, taskManager.getEpicById(1));

    }

    @Test
    void shouldPostSubtask() throws IOException, InterruptedException {
        path = String.format("%s%s/tasks/epic/", PROTOCOL_AND_HOST_NAME, PORT);
        getResponse(getRequestPOST(URI.create(path), epic1));
        epicFromJson = gson.fromJson(epic1, Epic.class);
        epicFromJson.setId(1);
        path = String.format("%s%s/tasks/subtask/", PROTOCOL_AND_HOST_NAME, PORT);
        request = getRequestPOST(URI.create(path), subtask1);
        response = getResponse(request);
        assertEquals(201, response.statusCode());
        subtaskFromJson = gson.fromJson(subtask1, Subtask.class);
        subtaskFromJson.setId(2);
        taskManager = Managers.getDefault();
        assertEquals(subtaskFromJson, taskManager.getSubtaskById(2));
    }

    @Test
    void shouldPostLikeUpdateTaskWhenIdIsExist() throws IOException, InterruptedException {
        shouldPostTask();
        taskFromJson.setName("UpdateTask");
        request = getRequestPOST(URI.create(path), gson.toJson(taskFromJson));
        response = getResponse(request);
        assertEquals(201, response.statusCode());
        taskManager = Managers.getDefault();
        assertEquals(taskFromJson, taskManager.getTaskById(1));

    }

    @Test
    void shouldPostLikeUpdateEpicWhenIdIsExist() throws IOException, InterruptedException {
        shouldPostEpic();
        epicFromJson.setName("UpdateEpic");
        response = getResponse(getRequestPOST(URI.create(path), gson.toJson(epicFromJson)));
        assertEquals(201, response.statusCode());
        taskManager = Managers.getDefault();
        assertEquals(epicFromJson, taskManager.getEpicById(1));

        epicFromJson.setTaskStatus(TaskStatus.DONE);
        response = getResponse(getRequestPOST(URI.create(path), gson.toJson(epicFromJson)));
        assertEquals("Поле taskStatus должно быть пустым", response.body());

    }

    @Test
    void shouldPostLikeUpdateSubtaskWhenIdIsExist() throws IOException, InterruptedException {
        shouldPostSubtask();
        subtaskFromJson.setTaskStatus(TaskStatus.DONE);
        epicFromJson.setTaskStatus(TaskStatus.DONE);
        epicFromJson.setStartTime(subtaskFromJson.getStartTime());
        epicFromJson.setDuration(subtaskFromJson.getDuration());
        epicFromJson.setEndTime(subtaskFromJson.getEndTime());
        epicFromJson.addSubtask(subtaskFromJson.getId());

        response = getResponse(getRequestPOST(URI.create(path), gson.toJson(subtaskFromJson)));
        assertEquals(201, response.statusCode());
        taskManager = Managers.getDefault();
        assertEquals(epicFromJson, taskManager.getEpicById(1));
        assertEquals(subtaskFromJson, taskManager.getSubtaskById(2));

        subtaskFromJson.setEpicId(-1);
        response = getResponse(getRequestPOST(URI.create(path), gson.toJson(subtaskFromJson)));
        assertEquals("Подзадача и/или эпик с таким id не существуют", response.body());

    }

    @Test
    void shouldGetPrioritizedTasks() throws IOException, InterruptedException {
        postAllTasks();
        path = String.format("%s%s/tasks/", PROTOCOL_AND_HOST_NAME, PORT);
        request = getRequestGET(URI.create(path));
        response = getResponse(request);
        taskManager = Managers.getDefault();
        List<Task> listOfPrioritizedTasks = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
        }.getType());
        assertEquals(taskManager.getPrioritizedTasks().stream().map(Task::getId).collect(Collectors.toList()),
                listOfPrioritizedTasks.stream().map(Task::getId).collect(Collectors.toList()));

        String query = "id=1";
        path = String.format("%s%s/tasks/?%s", PROTOCOL_AND_HOST_NAME, PORT, query);
        request = getRequestGET(URI.create(path));
        response = getResponse(request);
        assertEquals("Такого эндпоинта не существует", response.body());

    }

    @Test
    void shouldGetTask() throws IOException, InterruptedException {
        postAllTasks();
        path = String.format("%s%s/tasks/task", PROTOCOL_AND_HOST_NAME, PORT);
        request = getRequestGET(URI.create(path));
        response = getResponse(request);
        taskManager = Managers.getDefault();
        List<Task> listTasks = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
        }.getType());
        assertEquals(taskManager.getListOfAllTasks(), listTasks);

        String query = "id=5";
        path = String.format("%s%s/tasks/task/?%s", PROTOCOL_AND_HOST_NAME, PORT, query);
        request = getRequestGET(URI.create(path));
        response = getResponse(request);
        assertEquals(taskManager.getTaskById(5), listTasks.get(0));

        query = "id";
        path = String.format("%s%s/tasks/task/?%s", PROTOCOL_AND_HOST_NAME, PORT, query);
        request = getRequestGET(URI.create(path));
        response = getResponse(request);
        assertEquals("Некорректный идентификатор задачи", response.body());

        query = "id=-200";
        path = String.format("%s%s/tasks/task/?%s", PROTOCOL_AND_HOST_NAME, PORT, query);
        request = getRequestGET(URI.create(path));
        response = getResponse(request);
        assertEquals("Задача с идентификатором " + query + " не найдена", response.body());

    }

    @Test
    void shouldGetEpics() throws IOException, InterruptedException {
        postAllTasks();
        path = String.format("%s%s/tasks/epic", PROTOCOL_AND_HOST_NAME, PORT);
        request = getRequestGET(URI.create(path));
        response = getResponse(request);
        taskManager = Managers.getDefault();
        List<Epic> listEpics = gson.fromJson(response.body(), new TypeToken<List<Epic>>() {
        }.getType());
        assertEquals(taskManager.getListOfAllEpics(), listEpics);

        String query = "id=1";
        path = String.format("%s%s/tasks/epic/?%s", PROTOCOL_AND_HOST_NAME, PORT, query);
        request = getRequestGET(URI.create(path));
        response = getResponse(request);
        assertEquals(taskManager.getEpicById(1), listEpics.get(0));

        query = "id";
        path = String.format("%s%s/tasks/epic/?%s", PROTOCOL_AND_HOST_NAME, PORT, query);
        request = getRequestGET(URI.create(path));
        response = getResponse(request);
        assertEquals("Некорректный идентификатор эпика", response.body());

        query = "id=-200";
        path = String.format("%s%s/tasks/epic/?%s", PROTOCOL_AND_HOST_NAME, PORT, query);
        request = getRequestGET(URI.create(path));
        response = getResponse(request);
        assertEquals("Эпик с идентификатором " + query + " не найден", response.body());
    }

    @Test
    void shouldGetSubtasks() throws IOException, InterruptedException {
        postAllTasks();
        path = String.format("%s%s/tasks/subtask", PROTOCOL_AND_HOST_NAME, PORT);
        request = getRequestGET(URI.create(path));
        response = getResponse(request);
        taskManager = Managers.getDefault();
        List<Subtask> listSubtasks = gson.fromJson(response.body(), new TypeToken<List<Subtask>>() {
        }.getType());
        assertEquals(taskManager.getListOfAllSubtasks(), listSubtasks);

        String query = "id=3";
        path = String.format("%s%s/tasks/subtask/?%s", PROTOCOL_AND_HOST_NAME, PORT, query);
        request = getRequestGET(URI.create(path));
        response = getResponse(request);
        assertEquals(taskManager.getSubtaskById(3), listSubtasks.get(0));

        query = "id";
        path = String.format("%s%s/tasks/subtask/?%s", PROTOCOL_AND_HOST_NAME, PORT, query);
        request = getRequestGET(URI.create(path));
        response = getResponse(request);
        assertEquals("Некорректный идентификатор подзадачи", response.body());

        query = "id=-200";
        path = String.format("%s%s/tasks/subtask?%s", PROTOCOL_AND_HOST_NAME, PORT, query);
        request = getRequestGET(URI.create(path));
        response = getResponse(request);
        assertEquals("Подзадача с идентификатором " + query + " не найдена", response.body());

    }

    @Test
    void shouldGetTasksHistory() throws IOException, InterruptedException {
        postAllTasks();
        String query = "id=1";
        path = String.format("%s%s/tasks/epic/?%s", PROTOCOL_AND_HOST_NAME, PORT, query);
        getResponse(getRequestGET(URI.create(path)));
        query = "id=6";
        path = String.format("%s%s/tasks/task/?%s", PROTOCOL_AND_HOST_NAME, PORT, query);
        getResponse(getRequestGET(URI.create(path)));
        path = String.format("%s%s/tasks/history", PROTOCOL_AND_HOST_NAME, PORT);
        request = getRequestGET(URI.create(path));
        response = getResponse(request);
        taskManager = Managers.getDefault();
        List<Integer> listOfHistory = gson.fromJson(response.body(), new TypeToken<List<Integer>>() {
        }.getType());
        assertEquals(taskManager.getHistory().stream().map(Task::getId).collect(Collectors.toList()), listOfHistory);

    }

    @Test
    void shouldGetEpicSubtasks() throws IOException, InterruptedException {
        postAllTasks();
        String query = "id=1";
        path = String.format("%s%s/tasks/subtask/epic/?%s", PROTOCOL_AND_HOST_NAME, PORT, query);
        request = getRequestGET(URI.create(path));
        response = getResponse(request);
        taskManager = Managers.getDefault();
        List<Subtask> listOfEpicSubtasks = gson.fromJson(response.body(), new TypeToken<List<Subtask>>() {
        }.getType());
        assertEquals(taskManager.getListOfAllEpicSubtasks(taskManager.getEpicById(1)),
                listOfEpicSubtasks);

        query = "";
        path = String.format("%s%s/tasks/subtask/epic/?%s", PROTOCOL_AND_HOST_NAME, PORT, query);
        request = getRequestGET(URI.create(path));
        response = getResponse(request);
        assertEquals("Такого эндпоинта не существует", response.body());

        query = "id";
        path = String.format("%s%s/tasks/subtask/epic/?%s", PROTOCOL_AND_HOST_NAME, PORT, query);
        request = getRequestGET(URI.create(path));
        response = getResponse(request);
        assertEquals("Некорректный идентификатор эпика", response.body());

        query = "id=-1";
        path = String.format("%s%s/tasks/subtask/epic/?%s", PROTOCOL_AND_HOST_NAME, PORT, query);
        request = getRequestGET(URI.create(path));
        response = getResponse(request);
        assertEquals("Эпик с идентификатором " + query + " не найден", response.body());
    }

    @Test
    void shouldDeleteAllTask() throws IOException, InterruptedException {
        postAllTasks();
        path = String.format("%s%s/tasks/task", PROTOCOL_AND_HOST_NAME, PORT);
        request = getRequestDELETE(URI.create(path));
        response = getResponse(request);
        assertEquals("Все задачи удалены", response.body());
        request = getRequestGET(URI.create(path));
        response = getResponse(request);
        assertEquals("[]", response.body());
    }

    @Test
    void shouldDeleteAllEpics() throws IOException, InterruptedException {
        postAllTasks();
        path = String.format("%s%s/tasks/epic", PROTOCOL_AND_HOST_NAME, PORT);
        request = getRequestDELETE(URI.create(path));
        response = getResponse(request);
        assertEquals("Все эпики удалены", response.body());
        request = getRequestGET(URI.create(path));
        response = getResponse(request);
        assertEquals("[]", response.body());
        path = String.format("%s%s/tasks/subtask", PROTOCOL_AND_HOST_NAME, PORT);
        request = getRequestGET(URI.create(path));
        response = getResponse(request);
        assertEquals("[]", response.body());
    }

    @Test
    void shouldDeleteAllSubtasks() throws IOException, InterruptedException {
        postAllTasks();
        path = String.format("%s%s/tasks/subtask", PROTOCOL_AND_HOST_NAME, PORT);
        request = getRequestDELETE(URI.create(path));
        response = getResponse(request);
        assertEquals("Все подзадачи удалены", response.body());
        request = getRequestGET(URI.create(path));
        response = getResponse(request);
        assertEquals("[]", response.body());
    }

    @Test
    void shouldDeleteTaskById() throws IOException, InterruptedException {
        postAllTasks();
        String query = "id=5";
        path = String.format("%s%s/tasks/task/?%s", PROTOCOL_AND_HOST_NAME, PORT, query);
        request = getRequestDELETE(URI.create(path));
        response = getResponse(request);
        assertEquals("Задача с " + query + " удалена", response.body());
        request = getRequestGET(URI.create(path));
        response = getResponse(request);
        assertEquals("Задача с идентификатором " + query + " не найдена", response.body());

        query = "id";
        path = String.format("%s%s/tasks/task/?%s", PROTOCOL_AND_HOST_NAME, PORT, query);
        request = getRequestDELETE(URI.create(path));
        response = getResponse(request);
        assertEquals("Некорректный идентификатор задачи", response.body());

        query = "id=-1";
        path = String.format("%s%s/tasks/task/?%s", PROTOCOL_AND_HOST_NAME, PORT, query);
        request = getRequestDELETE(URI.create(path));
        response = getResponse(request);
        assertEquals("Невозможно удалить задачу с несуществующим id", response.body());

    }

    @Test
    void shouldDeleteEpicById() throws IOException, InterruptedException {
        postAllTasks();
        String query = "id=1";
        path = String.format("%s%s/tasks/epic/?%s", PROTOCOL_AND_HOST_NAME, PORT, query);
        request = getRequestDELETE(URI.create(path));
        response = getResponse(request);
        assertEquals("Эпик с " + query + " удалён", response.body());
        request = getRequestGET(URI.create(path));
        response = getResponse(request);
        assertEquals("Эпик с идентификатором " + query + " не найден", response.body());

        path = String.format("%s%s/tasks/subtask/", PROTOCOL_AND_HOST_NAME, PORT);
        request = getRequestGET(URI.create(path));
        response = getResponse(request);
        assertEquals("[]", response.body());

        query = "id";
        path = String.format("%s%s/tasks/epic/?%s", PROTOCOL_AND_HOST_NAME, PORT, query);
        request = getRequestDELETE(URI.create(path));
        response = getResponse(request);
        assertEquals("Некорректный идентификатор эпика", response.body());

        query = "id=-1";
        path = String.format("%s%s/tasks/epic/?%s", PROTOCOL_AND_HOST_NAME, PORT, query);
        request = getRequestDELETE(URI.create(path));
        response = getResponse(request);
        assertEquals("Невозможно удалить эпик с несуществующим id", response.body());
    }

    @Test
    void shouldDeleteSubtaskById() throws IOException, InterruptedException {
        postAllTasks();
        String query = "id=3";
        path = String.format("%s%s/tasks/subtask/?%s", PROTOCOL_AND_HOST_NAME, PORT, query);
        request = getRequestDELETE(URI.create(path));
        response = getResponse(request);
        assertEquals("Подзадача с " + query + " удалена", response.body());
        request = getRequestGET(URI.create(path));
        response = getResponse(request);
        assertEquals("Подзадача с идентификатором " + query + " не найдена", response.body());

        query = "id";
        path = String.format("%s%s/tasks/subtask/?%s", PROTOCOL_AND_HOST_NAME, PORT, query);
        request = getRequestDELETE(URI.create(path));
        response = getResponse(request);
        assertEquals("Некорректный идентификатор подзадачи", response.body());

        query = "id=-1";
        path = String.format("%s%s/tasks/subtask/?%s", PROTOCOL_AND_HOST_NAME, PORT, query);
        request = getRequestDELETE(URI.create(path));
        response = getResponse(request);
        assertEquals("Невозможно удалить подзадачу с несуществующим id", response.body());
    }

    private void initializeTasks() {
        epic1 = "{\n" +
                "\t\t\"name\": \"epic1\",\n" +
                "\t\t\"description\": \"description_epic_1\"\n" +
                "\t}";//id-1
        epic2 = "{\n" +
                "\t\t\"name\": \"epic123\",\n" +
                "\t\t\"description\": \"withoutSubtasks\"\n" +
                "\t}";//id-2

        subtask1 = "{\n" +
                "\t\t\"name\": \"sub1\",\n" +
                "\t\t\"description\": \"...\",\n" +
                "\t\t\"taskStatus\": \"NEW\",\n" +
                "\t\t\"startTime\": \"23.08.2023\",\n" +
                "\t\t\"duration\": 1200,\n" +
                "\t\t\"epicId\": 1\n" +
                "\t}";//id-3
        subtask2 = "{\n" +
                "\t\t\"name\": \"sub2\",\n" +
                "\t\t\"description\": \"special_sub\",\n" +
                "\t\t\"taskStatus\": \"IN_PROGRESS\",\n" +
                "\t\t\"startTime\": \"27.08.2023\",\n" +
                "\t\t\"duration\": 1200,\n" +
                "\t\t\"epicId\": 1\n" +
                "\t}"; //id-4
        task1 = "{\"name\": \"chill out\", \n" +
                " \"description\":\"just relax\", \n" +
                " \"startTime\":\"22.03.2023\", \n" +
                " \"duration\": 1200}"; //id-5
        task2 = "{\"name\": \"learn English\", \n" +
                " \"description\":\"learn new words\", \n" +
                " \"startTime\":\"24.03.2023\", \n" +
                " \"duration\": 1200}";//id-6
    }

    private void postAllTasks() throws IOException, InterruptedException {
        path = String.format("%s%s/tasks/epic/", PROTOCOL_AND_HOST_NAME, PORT);
        getResponse(getRequestPOST(URI.create(path), epic1));
        getResponse(getRequestPOST(URI.create(path), epic2));
        path = String.format("%s%s/tasks/subtask/", PROTOCOL_AND_HOST_NAME, PORT);
        getResponse(getRequestPOST(URI.create(path), subtask1));
        getResponse(getRequestPOST(URI.create(path), subtask2));
        path = String.format("%s%s/tasks/task/", PROTOCOL_AND_HOST_NAME, PORT);
        getResponse(getRequestPOST(URI.create(path), task1));
        getResponse(getRequestPOST(URI.create(path), task2));

    }

    private HttpRequest getRequestPOST(URI url, String task) {
        return HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(task)).build();
    }

    private HttpRequest getRequestGET(URI url) {
        return HttpRequest.newBuilder().uri(url).GET().build();
    }

    private HttpRequest getRequestDELETE(URI url) {
        return HttpRequest.newBuilder().uri(url).DELETE().build();
    }

    private HttpResponse<String> getResponse(HttpRequest request) throws IOException, InterruptedException {
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
