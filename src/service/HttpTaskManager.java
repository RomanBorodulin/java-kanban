package service;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.Epic;
import model.Subtask;
import model.Task;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class HttpTaskManager extends FileBackedTasksManager {
    private KVTaskClient client;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls()
            .registerTypeAdapter(Task.class, new Adapters.TaskAdapter())
            .registerTypeAdapter(Epic.class, new Adapters.EpicAdapter())
            .registerTypeAdapter(Subtask.class, new Adapters.SubtaskAdapter())
            .create();

    private HttpTaskManager() {

    }

    public HttpTaskManager(URI url) {
        client = new KVTaskClient(url);
        load();
    }

    @Override
    protected void save() {
        try {
            client.put("tasks", gson.toJson(tasks));
            client.put("epics", gson.toJson(epics));
            client.put("subtasks", gson.toJson(subtasks));
            client.put("history", gson.toJson(getHistory().stream()
                    .map(Task::getId).collect(Collectors.toList())));
            client.put("prioritizedTasks", gson.toJson(getPrioritizedTasks()));
        } catch (IOException | InterruptedException ex) {
            System.out.println("Во время выполнения сохранения возникла ошибка.\n");
        }
    }

    private void load() {
        try {
            tasks.putAll(gson.fromJson(client.load("tasks"), new TypeToken<HashMap<Integer, Task>>() {
            }.getType()));
            epics.putAll(gson.fromJson(client.load("epics"), new TypeToken<HashMap<Integer, Epic>>() {
            }.getType()));
            subtasks.putAll(gson.fromJson(client.load("subtasks"), new TypeToken<HashMap<Integer, Subtask>>() {
            }.getType()));
            tasks.values().stream().map(Task::new).forEach((task) -> prioritizedTasks.add(task));
            subtasks.values().stream().map(Subtask::new).forEach((subtask) -> prioritizedTasks.add(subtask));
            List<Integer> history = gson.fromJson(client.load("history"), new TypeToken<List<Integer>>() {
            }.getType());
            history.forEach((Integer id) -> {
                if (epics.containsKey(id)) {
                    getEpicById(id);
                } else if (subtasks.containsKey(id)) {
                    getSubtaskById(id);
                } else if (tasks.containsKey(id)) {
                    getTaskById(id);
                }
            });
            List<Integer> allIdTasks = new ArrayList<>(tasks.keySet());
            allIdTasks.addAll(epics.keySet());
            allIdTasks.addAll(subtasks.keySet());
            id = allIdTasks.stream().max(Integer::compareTo).orElse(0);
        } catch (Exception ex) {
            System.out.println("Во время выполнения загрузки возникла ошибка.\n");
        }
    }
}
