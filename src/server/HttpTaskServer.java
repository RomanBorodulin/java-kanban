package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import service.Adapters;
import service.ManagerRemoveException;
import service.ManagerSaveException;
import service.Managers;
import service.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.stream.Collectors;

import static server.HttpTaskServer.Endpoint.DELETE_TASKS;
import static server.HttpTaskServer.Endpoint.GET_TASKS;
import static server.HttpTaskServer.Endpoint.POST_TASKS;
import static server.HttpTaskServer.Endpoint.UNKNOWN;

public class HttpTaskServer {

    private static final int PORT = 8080;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private final HttpServer httpServer;
    private final Gson gson;
    private final TaskManager taskManager;


    public HttpTaskServer() throws IOException {
        this(Managers.getDefault());
    }

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        gson = new GsonBuilder().setPrettyPrinting().serializeNulls()
                .registerTypeAdapter(Task.class, new Adapters.TaskAdapter())
                .registerTypeAdapter(Epic.class, new Adapters.EpicAdapter())
                .registerTypeAdapter(Subtask.class, new Adapters.SubtaskAdapter())
                .create();
        httpServer = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        httpServer.createContext("/tasks", this::handle);

    }

    public void start() {
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(0);
    }

    private void handle(HttpExchange httpExchange) throws IOException {
        Endpoint endpoint = getEndpoint(httpExchange);

        switch (endpoint) {
            case GET_TASKS:
                handleGetTasks(httpExchange);
                break;
            case POST_TASKS:
                handlePostTasks(httpExchange);
                break;
            case DELETE_TASKS:
                handleDeleteTasks(httpExchange);
                break;
            default:
                writeResponse(httpExchange, "Такого эндпоинта не существует", 404);
        }
    }

    private void handleGetTasks(HttpExchange httpExchange) throws IOException {
        String[] pathParts = httpExchange.getRequestURI().getPath().split("/");
        String query = httpExchange.getRequestURI().getQuery();
        Optional<Integer> id = getId(httpExchange);

        if (pathParts.length == 2) {
            writeResponse(httpExchange, gson.toJson(taskManager.getPrioritizedTasks()), 200);
        }
        if (pathParts.length == 4) {
            if (id.isEmpty()) {
                writeResponse(httpExchange, "Некорректный идентификатор эпика", 400);
                return;
            }
            Epic epic = taskManager.getEpicById(id.get());
            if (epic != null) {
                writeResponse(httpExchange, gson.toJson(taskManager.getListOfAllEpicSubtasks(epic)), 200);
                return;
            }
            writeResponse(httpExchange, "Эпик с идентификатором id=" + id.get()
                    + " не найден", 404);
        }
        if (pathParts.length == 3) {
            if (pathParts[2].equals("task")) {
                if (query == null) {
                    writeResponse(httpExchange, gson.toJson(taskManager.getListOfAllTasks()), 200);
                    return;
                }
                if (id.isEmpty()) {
                    writeResponse(httpExchange, "Некорректный идентификатор задачи", 400);
                    return;
                }
                Task task = taskManager.getTaskById(id.get());
                if (task != null) {
                    writeResponse(httpExchange, gson.toJson(task), 200);
                    return;
                }
                writeResponse(httpExchange, "Задача с идентификатором id=" + id.get()
                        + " не найдена", 404);
            }

            if (pathParts[2].equals("epic")) {
                if (query == null) {
                    writeResponse(httpExchange, gson.toJson(taskManager.getListOfAllEpics()), 200);
                    return;
                }
                if (id.isEmpty()) {
                    writeResponse(httpExchange, "Некорректный идентификатор эпика", 400);
                    return;
                }
                Epic epic = taskManager.getEpicById(id.get());
                if (epic != null) {
                    writeResponse(httpExchange, gson.toJson(epic), 200);
                    return;
                }
                writeResponse(httpExchange, "Эпик с идентификатором id=" + id.get()
                        + " не найден", 404);
            }

            if (pathParts[2].equals("subtask")) {
                if (query == null) {
                    writeResponse(httpExchange, gson.toJson(taskManager.getListOfAllSubtasks()), 200);
                    return;
                }
                if (id.isEmpty()) {
                    writeResponse(httpExchange, "Некорректный идентификатор подзадачи", 400);
                    return;
                }
                Subtask subtask = taskManager.getSubtaskById(id.get());
                if (subtask != null) {
                    writeResponse(httpExchange, gson.toJson(subtask), 200);
                    return;
                }
                writeResponse(httpExchange, "Подзадача с идентификатором id=" + id.get()
                        + " не найдена", 404);
            }

            if (pathParts[2].equals("history")) {
                writeResponse(httpExchange, gson.toJson(taskManager.getHistory().stream()
                        .map(Task::getId).collect(Collectors.toList())), 200);
            }
        }
    }

    private void handlePostTasks(HttpExchange httpExchange) throws IOException {
        String path = httpExchange.getRequestURI().getPath().substring("/tasks/".length());

        try (InputStream inputStream = httpExchange.getRequestBody()) {
            String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
            try {
                if (path.equals("task/")) {
                    Task task = gson.fromJson(body, Task.class);
                    if (task.getId() != 0) {
                        taskManager.updateTask(task);
                        writeResponse(httpExchange, "Задача обновлена", 201);
                        return;
                    }
                    taskManager.createTask(task);
                    writeResponse(httpExchange, "Задача добавлена", 201);
                    return;
                }

                if (path.equals("epic/")) {
                    Epic epic = gson.fromJson(body, Epic.class);
                    if (!epic.getTaskStatus().equals(TaskStatus.NEW)) {
                        writeResponse(httpExchange, "Поле taskStatus должно быть пустым", 400);
                        return;
                    }
                    if (!epic.getStartTime().isEqual(LocalDate.EPOCH)) {
                        writeResponse(httpExchange, "Поле startTime должно быть пустым", 400);
                        return;
                    }
                    if (!epic.getDuration().equals(Duration.ZERO)) {
                        writeResponse(httpExchange, "Поле duration должно быть пустым", 400);
                        return;
                    }
                    if (!epic.getEndTime().equals(LocalDateTime.of(LocalDate.EPOCH, LocalTime.MIN))) {
                        writeResponse(httpExchange, "Поле getEndTime должно быть пустым", 400);
                        return;
                    }
                    if (epic.getId() != 0) {
                        taskManager.updateEpic(epic);
                        writeResponse(httpExchange, "Эпик обновлён", 201);
                        return;
                    }
                    if (!epic.getSubtasks().isEmpty()) {
                        writeResponse(httpExchange, "Поле subtasksId при добавлении Эпика" +
                                " должно быть пустым", 400);
                        return;
                    }
                    taskManager.createEpic(epic);
                    writeResponse(httpExchange, "Эпик добавлен", 201);
                    return;
                }

                if (path.equals("subtask/")) {
                    Subtask subtask = gson.fromJson(body, Subtask.class);
                    if (subtask.getEpicId() == 0) {
                        writeResponse(httpExchange, "Поле epicId не может быть пустым", 400);
                        return;
                    }
                    if (subtask.getId() != 0) {
                        taskManager.updateSubtask(subtask);
                        writeResponse(httpExchange, "Подзадача обновлена", 201);
                        return;
                    }
                    taskManager.createSubtask(subtask);
                    writeResponse(httpExchange, "Подзадача добавлена", 201);
                }

            } catch (ManagerSaveException ex) {
                writeResponse(httpExchange, ex.getMessage(), 400);
            } catch (Exception ex) {
                writeResponse(httpExchange, "Получен некорректный JSON", 400);
            }
        }
    }

    private void handleDeleteTasks(HttpExchange httpExchange) throws IOException {
        String[] pathPart = httpExchange.getRequestURI().getPath().split("/");
        String query = httpExchange.getRequestURI().getQuery();
        Optional<Integer> id = getId(httpExchange);
        try {
            if (pathPart[2].equals("task")) {
                if (query == null) {
                    taskManager.deleteAllTasks();
                    writeResponse(httpExchange, "Все задачи удалены", 201);
                    return;
                }
                if (id.isEmpty()) {
                    writeResponse(httpExchange, "Некорректный идентификатор задачи", 400);
                    return;
                }
                taskManager.deleteTaskById(id.get());
                writeResponse(httpExchange, "Задача с id=" + id.get() + " удалена", 201);
            }

            if (pathPart[2].equals("epic")) {
                if (query == null) {
                    taskManager.deleteAllEpics();
                    writeResponse(httpExchange, "Все эпики удалены", 201);
                    return;
                }
                if (id.isEmpty()) {
                    writeResponse(httpExchange, "Некорректный идентификатор эпика", 400);
                    return;
                }
                taskManager.deleteEpicById(id.get());
                writeResponse(httpExchange, "Эпик с id=" + id.get() + " удалён", 201);
            }

            if (pathPart[2].equals("subtask")) {
                if (query == null) {
                    taskManager.deleteAllSubtasks();
                    writeResponse(httpExchange, "Все подзадачи удалены", 201);
                    return;
                }
                if (id.isEmpty()) {
                    writeResponse(httpExchange, "Некорректный идентификатор подзадачи", 400);
                    return;
                }
                taskManager.deleteSubtaskById(id.get());
                writeResponse(httpExchange, "Подзадача с id=" + id.get() + " удалена", 201);
            }
        } catch (ManagerRemoveException ex) {
            writeResponse(httpExchange, ex.getMessage(), 400);
        }

    }

    private Endpoint getEndpoint(HttpExchange httpExchange) {
        String[] pathParts = httpExchange.getRequestURI().getPath().split("/");
        String requestMethod = httpExchange.getRequestMethod();
        String query = httpExchange.getRequestURI().getQuery();


        if (pathParts.length == 2 && pathParts[1].equals("tasks") && query == null) {
            return GET_TASKS;
        }
        if (pathParts.length == 3 && pathParts[1].equals("tasks")) {
            boolean isValidPath = pathParts[2].equals("task")
                    || pathParts[2].equals("epic") || pathParts[2].equals("subtask");
            if (requestMethod.equals("GET") && (isValidPath || pathParts[2].equals("history"))) {
                return GET_TASKS;
            }
            if (requestMethod.equals("POST") && isValidPath) {
                return POST_TASKS;
            }
            if (requestMethod.equals("DELETE") && isValidPath) {
                return DELETE_TASKS;
            }
        }
        if (pathParts.length == 4 && pathParts[1].equals("tasks") && pathParts[2].equals("subtask") &&
                pathParts[3].equals("epic") && query != null) {
            return GET_TASKS;
        }
        return UNKNOWN;
    }

    private Optional<Integer> getId(HttpExchange httpExchange) {
        String query = httpExchange.getRequestURI().getQuery();
        try {
            query = query.substring("id=".length());
            return Optional.of(Integer.parseInt(query));
        } catch (NumberFormatException | NullPointerException | StringIndexOutOfBoundsException ex) {
            return Optional.empty();
        }
    }

    private void writeResponse(HttpExchange exchange, String responseString, int responseCode) throws IOException {
        if (responseString.isBlank()) {
            exchange.sendResponseHeaders(responseCode, 0);
        } else {
            byte[] bytes = responseString.getBytes(DEFAULT_CHARSET);
            exchange.sendResponseHeaders(responseCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
        exchange.close();
    }

    enum Endpoint {GET_TASKS, POST_TASKS, DELETE_TASKS, UNKNOWN}

    public static void main(String[] args) throws IOException {
        HttpTaskServer httpTaskServer = new HttpTaskServer();
        httpTaskServer.start();
    }
}
