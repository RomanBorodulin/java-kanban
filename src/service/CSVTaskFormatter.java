package service;

import model.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CSVTaskFormatter {
    private CSVTaskFormatter() {
    }

    static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    static String toString(Task task) {
        StringBuilder epicId = new StringBuilder();
        if (task.getType().equals(TaskType.SUBTASK)) {
            epicId.append(((Subtask) task).getEpicId());
        }
        return String.join(",", Integer.toString(task.getId()), task.getType().toString(),
                task.getName(), task.getTaskStatus().toString(), task.getDescription(), epicId.toString(),
                task.getStartTime().format(DATE_FORMATTER), Long.toString(task.getDuration().toMinutes()),
                task.getEndTime().format(DATE_TIME_FORMATTER));

    }

    static Task fromString(String value) {
        final Task task;
        TaskStatus status;
        String[] elements = value.split(",", -1);
        //0-id,1-type,2-name,3-status,4-description,5-epic,6-startTime,7-duration,8-endTime
        switch (TaskStatus.valueOf(elements[3])) {
            case NEW:
                status = TaskStatus.NEW;
                break;
            case IN_PROGRESS:
                status = TaskStatus.IN_PROGRESS;
                break;
            case DONE:
                status = TaskStatus.DONE;
                break;
            default:
                throw new IllegalArgumentException("Статус не существует");
        }

        switch (TaskType.valueOf(elements[1])) {
            case EPIC:
                task = new Epic(elements[2], elements[4]);
                parseTask(task, elements);
                ((Epic) task).setEndTime(LocalDateTime.parse(elements[8], DATE_TIME_FORMATTER));
                break;
            case TASK:
                task = new Task(elements[2], elements[4], status);
                parseTask(task, elements);
                break;
            case SUBTASK:
                task = new Subtask(elements[2], elements[4], status, Integer.parseInt(elements[5]));
                parseTask(task, elements);
                break;
            default:
                throw new IllegalArgumentException("Тип задачи не существует");
        }
        return task;
    }

    static String historyToString(HistoryManager manager) {
        List<Task> history = manager.getHistory();
        StringBuilder stringBuilder = new StringBuilder();
        for (Task task : history) {
            stringBuilder.append(task.getId() + ",");
        }
        if (stringBuilder.length() > 0) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        return stringBuilder + "\n";
    }

    static List<Integer> historyFromString(String value) {
        if (value.isEmpty()) {
            return new ArrayList<>();
        }
        List<Integer> history = new ArrayList<>();
        String[] elements = value.split(",");
        for (String element : elements) {
            history.add(Integer.parseInt(element));
        }
        return new ArrayList<>(history);
    }

    private static void parseTask(Task task, String[] elements) {
        task.setId(Integer.parseInt(elements[0]));
        task.setStartTime(LocalDate.parse(elements[6], DATE_FORMATTER));
        task.setDuration(Duration.ofMinutes(Long.parseLong(elements[7])));
    }
}
