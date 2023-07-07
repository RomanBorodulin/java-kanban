package service;

import model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
            stringBuilder.append(CSVTaskFormatter.toString(task) + "\n");
        }
        stringBuilder.append("\n" + CSVTaskFormatter.historyToString(historyManager));

        try (Writer fileWriter = new FileWriter(path.getFileName().toString(), StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(fileWriter)) {

            bw.write(stringBuilder.toString());

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка записи");
        }

    }

    static FileBackedTasksManager loadFromFile(Path path) {
        if (!Files.exists(path)) {
            return new FileBackedTasksManager(path);
        }
        FileBackedTasksManager fileManager = new FileBackedTasksManager(path);
        LinkedList<String> dataList = new LinkedList<>();

        try (Reader fileReader = new FileReader(path.getFileName().toString(), StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(fileReader)) {
            while (br.ready()) {
                String line = br.readLine();
                dataList.addLast(line);
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка чтения файла");
        }
        if (!dataList.isEmpty()) {
            int rememberId = Integer.parseInt(dataList.get(dataList.size() - 3).split(",", 2)[0]);
            dataList.removeFirst();
            id = rememberId;

            for (String data : dataList) {
                if (data.isEmpty()) {
                    break;
                }
                Task task = CSVTaskFormatter.fromString(data);
                if (task.getType() == TaskType.EPIC) {
                    fileManager.loadEpic((Epic) task);
                } else if (task.getType() == TaskType.SUBTASK) {
                    fileManager.loadSubtask((Subtask) task);
                } else {
                    fileManager.loadTask(task);
                }
            }
            String history = dataList.getLast();
            if (!history.isEmpty()) {
                List<Integer> historyList = CSVTaskFormatter.historyFromString(history);
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

