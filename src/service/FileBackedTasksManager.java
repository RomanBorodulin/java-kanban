package service;

import model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class FileBackedTasksManager extends InMemoryTaskManager {

    private final Path path;
    private static final String TASK_FIELDS = "id,type,name,status,description,epic";

    public FileBackedTasksManager(Path path) {
        this.path = path;

    }

    private void save() {
        StringBuilder stringBuilder = new StringBuilder();
        List<Task> summaryListOfTasks = new ArrayList<>(getListOfAllTasks());
        summaryListOfTasks.addAll(getListOfAllEpics());
        summaryListOfTasks.addAll(getListOfAllSubtasks());
        Comparator<Task> taskIdComparator = new Comparator<>() {
            @Override
            public int compare(Task t1, Task t2) {
                return Integer.compare(t1.getId(), t2.getId());
            }
        };
        summaryListOfTasks.sort(taskIdComparator);
        stringBuilder.append(TASK_FIELDS + "\n");
        for (Task task : summaryListOfTasks) {
            stringBuilder.append(toString(task) + "\n");
        }
        stringBuilder.append("\n" + historyToString(historyManager));

        try (Writer fileWriter = new FileWriter(path.getFileName().toString(), StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(fileWriter)) {

            bw.write(stringBuilder.toString());

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка записи");
        }

    }

    private String toString(Task task) {
        StringBuilder epicId = new StringBuilder();
        if (task.getType().equals(TaskType.SUBTASK)) {
            epicId.append(((Subtask) task).getEpicId());
        }
        return String.join(",", Integer.toString(task.getId()), task.getType().toString(),
                task.getName(), task.getTaskStatus().toString(), task.getDescription(), epicId.toString());

    }

    private Task fromString(String value) {
        Task task;
        TaskStatus status;
        String[] elements = value.split(",", -1); //0-id,1-type,2-name,3-status,4-description,5-epic

        switch (elements[3]) {
            case "NEW":
                status = TaskStatus.NEW;
                break;
            case "IN_PROGRESS":
                status = TaskStatus.IN_PROGRESS;
                break;
            case "DONE":
                status = TaskStatus.DONE;
                break;
            default:
                throw new IllegalArgumentException("Статус не существует");
        }

        switch (elements[1]) {
            case "EPIC":
                task = new Epic(elements[2], elements[4]);
                task.setId(Integer.parseInt(elements[0]));
                break;
            case "TASK":
                task = new Task(elements[2], elements[4], status);
                task.setId(Integer.parseInt(elements[0]));
                break;
            case "SUBTASK":
                task = new Subtask(elements[2], elements[4], status, Integer.parseInt(elements[5]));
                task.setId(Integer.parseInt(elements[0]));
                break;
            default:
                throw new IllegalArgumentException("Тип задачи не существует");
        }
        task.setId(Integer.parseInt(elements[0]));
        return task;
    }

    private static String historyToString(HistoryManager manager) {
        List<Task> history = manager.getHistory();
        StringBuilder stringBuilder = new StringBuilder();
        for (Task task : history) {
            stringBuilder.append(task.getId() + ",");
        }
        if (stringBuilder.length() > 0) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        return stringBuilder.toString();

    }

    private static List<Integer> historyFromString(String value) {
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

    static FileBackedTasksManager loadFromFile(Path path) {
        FileBackedTasksManager fileManager = new FileBackedTasksManager(path);
        LinkedList<String> dataList = new LinkedList<>();

        try (Reader fileReader = new FileReader(path.getFileName().toString(), StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(fileReader)) {
            while (br.ready()) {
                String line = br.readLine();
                dataList.addLast(line);
            }
        } catch (IOException e) {

        }
        if (dataList.size() != 0) {
            int rememberId = Integer.parseInt(dataList.get(dataList.size() - 3).split(",", 2)[0]);
            dataList.removeFirst();
            id = rememberId;

            for (String data : dataList) {
                if (data.isEmpty()) {
                    break;
                }
                Task task = fileManager.fromString(data);
                if (task instanceof Epic) {
                    fileManager.loadEpic((Epic) task);
                } else if (task instanceof Subtask) {
                    fileManager.loadSubtask((Subtask) task);
                } else {
                    fileManager.loadTask(task);
                }
            }
            String history = dataList.getLast();
            if (!history.isEmpty()) {
                List<Integer> historyList = historyFromString(history);
                for (Integer id : historyList) {
                    if (fileManager.epics.containsKey(id)) {
                        fileManager.getEpicById(id);
                    } else if (fileManager.subtasks.containsKey(id)) {
                        fileManager.getSubtaskById(id);
                    } else if (fileManager.tasks.containsKey(id)) {
                        fileManager.getTaskById(id);
                    }
                }
            }
        }
        return fileManager;
    }

    private void loadTask(Task task) {
        tasks.put(task.getId(), task);
    }

    private void loadEpic(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    private void loadSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public void createTask(Task task) {
        super.createTask(task);
        save();
    }

    @Override
    public void createEpic(Epic epic) {
        super.createEpic(epic);
        save();
    }

    @Override
    public void createSubtask(Subtask subtask) {
        super.createSubtask(subtask);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public Task getTaskById(int id) {
        final Task task = super.getTaskById(id);
        save();
        return task;

    }

    @Override
    public Epic getEpicById(int id) {
        final Epic epic = super.getEpicById(id);
        save();
        return epic;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        final Subtask subtask = super.getSubtaskById(id);
        save();
        return subtask;
    }

    public static void main(String[] args) {
        Path path = Paths.get("backup.csv");
        TaskManager manager = FileBackedTasksManager.loadFromFile(path);

        manager.createTask(new Task("Прочитать книгу", "", TaskStatus.IN_PROGRESS)); //#1
        manager.createTask(new Task("Протестировать программу", "", TaskStatus.IN_PROGRESS)); //#2

        Epic firstEpic = new Epic("Переезд", ""); //#3
        manager.createEpic(firstEpic);
        int epicId = firstEpic.getId();
        manager.createSubtask(new Subtask("Собрать коробки", "", TaskStatus.NEW, epicId)); //#4
        manager.createSubtask(new Subtask("Упаковать кошку", "", TaskStatus.NEW, epicId)); //#5
        manager.createSubtask(new Subtask("Заказать перевозку", "", TaskStatus.NEW, epicId)); //#6

        Epic secondEpic = new Epic("Важный эпик 2", "Очень важный без подзадач"); //#7
        manager.createEpic(secondEpic);

        manager.getTaskById(1);
        manager.getTaskById(2);
        manager.getEpicById(3);
        manager.getSubtaskById(5);
        manager.getSubtaskById(4);
        manager.getTaskById(1);
        manager.getSubtaskById(5);
        manager.getSubtaskById(6);
        System.out.println(manager.getHistory()); // 2-3-4-1-5-6

        manager.deleteTaskById(2);
        manager.getTaskById(8);
        System.out.println(manager.getListOfAllTasks());

    }
}

