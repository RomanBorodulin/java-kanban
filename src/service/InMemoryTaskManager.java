package service;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private HashMap<Integer, Epic> epics = new HashMap<>();
    private List<Task> history = new ArrayList<>(10);
    private int id = 1;

    // Методы получения списка всех задач
    @Override
    public ArrayList<Task> getListOfAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Epic> getListOfAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<Subtask> getListOfAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    // Удаление всех задач
    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        subtasks.clear();
        epics.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        for (Epic epic : epics.values()) {
            epic.clearAllSubtasks();
            epic.changeTaskStatus(new ArrayList<>(getListOfAllEpicSubtasks(epic)));  //TODO
        }
        subtasks.clear();
    }

    // Получение по ID
    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            add(task);
        }
        return task;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            add(epic);
        }
        return epic;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            add(subtask);
        }
        return subtask;
    }

    // Создание
    @Override
    public void createTask(Task task) {
        if (task == null) {
            return;
        }
        task.setId(id++);
        tasks.put(task.getId(), task);
    }

    @Override
    public void createEpic(Epic epic) {
        if (epic == null) {
            return;
        }
        epic.setId(id++);
        epics.put(epic.getId(), epic);
    }

    @Override
    public void createSubtask(Subtask subtask) {
        if (subtask == null) {
            return;
        }
        int epicId = subtask.getEpicId();
        boolean isEpicExist = epics.containsKey(epicId);
        if (!isEpicExist) {
            return;
        }
        subtask.setId(id++);
        // Добавили в мапу подзадачу
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(epicId);
        // Добавили знание о подзадаче эпику
        epic.addSubtask(subtask.getId());
        epic.changeTaskStatus(new ArrayList<>(getListOfAllEpicSubtasks(epic))); //TODO
    }

    // Обновление
    @Override
    public void updateTask(Task task) {
        if (task == null) {
            return;
        }
        boolean isTaskExist = tasks.containsKey(task.getId());
        if (!isTaskExist) {
            return;
        }
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null) {
            return;
        }
        boolean isEpicExist = epics.containsKey(epic.getId());
        if (!isEpicExist) {
            return;
        }
        epics.put(epic.getId(), epic);
        epics.get(epic.getId()).changeTaskStatus(new ArrayList<>(getListOfAllEpicSubtasks(epic))); //TODO
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null) {
            return;
        }
        boolean isSubtaskExist = subtasks.containsKey(subtask.getId());
        boolean isEpicExist = epics.containsKey(subtask.getEpicId());
        if (!isSubtaskExist || !isEpicExist) {
            return;
        }
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        epic.changeTaskStatus(new ArrayList<>(getListOfAllEpicSubtasks(epic))); //TODO
    }

    // Получение списка всех подзадач определенного эпика
    @Override
    public ArrayList<Subtask> getListOfAllEpicSubtasks(Epic epic) {
        if (epic == null) {
            return new ArrayList<>();
        }
        ArrayList<Subtask> subtasks = new ArrayList<>();
        for (Integer subtaskId : epic.getSubtasks()) {
            subtasks.add(this.subtasks.get(subtaskId));
        }
        return subtasks;
    }

    // Удаление по идентификатору
    @Override
    public void deleteTaskById(int id) {
        boolean isTaskExist = tasks.containsKey(id);
        if (!isTaskExist) {
            return;
        }
        tasks.remove(id);
    }

    @Override
    public void deleteEpicById(int id) {
        boolean isEpicExist = epics.containsKey(id);
        if (!isEpicExist) {
            return;
        }
        for (Integer key : epics.get(id).getSubtasks()) {
            subtasks.remove(key);
        }
        epics.remove(id);
    }

    @Override
    public void deleteSubtaskById(int id) {
        boolean isSubtaskExist = subtasks.containsKey(id);
        if (!isSubtaskExist) {
            return;
        }
        Epic epic = epics.get(subtasks.get(id).getEpicId());
        subtasks.remove(id);
        epic.removeSubtaskById(id);
        epic.changeTaskStatus(new ArrayList<>(getListOfAllEpicSubtasks(epic))); //TODO
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }

    private void add(Task task) {
        if (history.size() >= 10) {
            history.remove(0);
            history.add(task);
            return;
        }
        history.add(task);
    }
}
