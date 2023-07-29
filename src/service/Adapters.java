package service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


public final class Adapters {
    static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    static private String name;
    static private String description;
    static private String id;
    static private String taskStatus;
    static private String startTime;
    static private String duration;

    private Adapters() {

    }

    private static void readJsonTask(JsonObject jsonObject) {

        name = jsonObject.get("name") == null ? "" : jsonObject.get("name").getAsString();
        description = jsonObject.get("description") == null ? "" : jsonObject.get("description").getAsString();
        id = jsonObject.get("id") == null ? "0" : jsonObject.get("id").getAsString();
        taskStatus = jsonObject.get("taskStatus") == null ? "NEW" : jsonObject.get("taskStatus").getAsString();
        startTime = jsonObject.get("startTime") == null ?
                LocalDate.EPOCH.format(DATE_FORMATTER) : jsonObject.get("startTime").getAsString();
        duration = jsonObject.get("duration") == null ?
                Long.toString(Duration.ZERO.toMinutes()) : Long.toString(jsonObject.get("duration").getAsLong());
    }

    private static void writeJsonTask(JsonWriter jsonWriter, Task task) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("name").value(task.getName());
        jsonWriter.name("description").value(task.getDescription());
        jsonWriter.name("id").value(task.getId());
        jsonWriter.name("taskStatus").value(task.getTaskStatus().toString());
        jsonWriter.name("startTime").value(task.getStartTime().format(DATE_FORMATTER));
        jsonWriter.name("duration").value(task.getDuration().toMinutes());
    }

    public static class TaskAdapter extends TypeAdapter<Task> {
        @Override
        public void write(JsonWriter jsonWriter, Task task) throws IOException {
            writeJsonTask(jsonWriter, task);
            jsonWriter.endObject();
        }

        @Override
        public Task read(JsonReader jsonReader) {
            Task task;
            JsonElement jsonElement = JsonParser.parseReader(jsonReader);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            readJsonTask(jsonObject);
            task = new Task(name, description, TaskStatus.valueOf(taskStatus),
                    LocalDate.parse(startTime, DATE_FORMATTER), Duration.ofMinutes(Long.parseLong(duration)));
            task.setId(Integer.parseInt(id));
            return task;

        }
    }

    public static class EpicAdapter extends TypeAdapter<Epic> {

        @Override
        public void write(JsonWriter jsonWriter, Epic epic) throws IOException {
            writeJsonTask(jsonWriter, epic);
            jsonWriter.name("subtasksId");
            jsonWriter.beginArray();
            for (Integer integer : epic.getSubtasks()) {
                jsonWriter.value(integer);
            }
            jsonWriter.endArray();
            //jsonWriter.name("subtasksId").value(epic.getSubtasks().toString());
            jsonWriter.name("endTime").value(epic.getEndTime().format(DATE_TIME_FORMATTER));
            jsonWriter.endObject();
        }

        @Override
        public Epic read(JsonReader jsonReader) {
            JsonElement jsonElement = JsonParser.parseReader(jsonReader);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            readJsonTask(jsonObject);
            Epic epic = new Epic(name, description);
            JsonArray subtasks;
            String endTime;
            ArrayList<Integer> subList = new ArrayList<>();

            epic.setId(Integer.parseInt(id));
            epic.setTaskStatus(TaskStatus.valueOf(taskStatus));
            epic.setStartTime(LocalDate.parse(startTime, DATE_FORMATTER));
            epic.setDuration(Duration.ofMinutes(Long.parseLong(duration)));

            if (jsonObject.get("subtasksId") == null) {
                epic.setSubtasks(new ArrayList<>());
            } else {
                subtasks = jsonObject.get("subtasksId").getAsJsonArray();
                for (int i = 0; i < subtasks.size(); i++) {
                    subList.add(subtasks.get(i).getAsInt());
                }
                epic.setSubtasks(subList);
            }
            if (jsonObject.get("endTime") == null) {
                epic.setEndTime(LocalDateTime.of(LocalDate.EPOCH, LocalTime.MIN));
            } else {
                endTime = jsonObject.get("endTime").getAsString();
                epic.setEndTime(LocalDateTime.parse(endTime, DATE_TIME_FORMATTER));
            }
            return epic;
        }
    }

    public static class SubtaskAdapter extends TypeAdapter<Subtask> {
        @Override
        public void write(JsonWriter jsonWriter, Subtask subtask) throws IOException {
            writeJsonTask(jsonWriter, subtask);
            jsonWriter.name("epicId").value(subtask.getEpicId());
            jsonWriter.endObject();
        }

        @Override
        public Subtask read(JsonReader jsonReader) {
            JsonElement jsonElement = JsonParser.parseReader(jsonReader);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            readJsonTask(jsonObject);
            Subtask subtask;

            int epicId = jsonObject.get("epicId").getAsInt();
            subtask = new Subtask(name, description, TaskStatus.valueOf(taskStatus), epicId,
                    LocalDate.parse(startTime, DATE_FORMATTER), Duration.ofMinutes(Long.parseLong(duration)));
            subtask.setId(Integer.parseInt(id));
            return subtask;
        }
    }
}
