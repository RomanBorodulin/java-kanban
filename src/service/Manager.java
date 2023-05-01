package service;

import model.*;


import java.util.ArrayList;
import java.util.HashMap;

public class Manager {
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private HashMap<Integer, Epic> epics = new HashMap<>();
    private int id = 1;

    // Методы получения списка всех задач
    public ArrayList<Task> getListOfAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Epic> getListOfAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public ArrayList<Subtask> getListOfAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    // Удаление всех задач
    public void deleteAllTasks() {
        tasks.clear();
    }

    public void deleteAllEpics() {
        subtasks.clear();
        epics.clear();
    }

    public void deleteAllSubtasks() {
        for (Epic epic : epics.values()) {
            epic.clearAllSubtasks();
            epic.changeTaskStatus(new ArrayList<>(getListOfAllEpicSubtasks(epic)));  //TODO
        }
        subtasks.clear();
    }

    // Получение по ID
    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }

    // Создание
    public void createTask(Task task) {
        if (task == null) {
            return;
        }
        task.setId(id++);
        tasks.put(task.getId(), task);
    }

    public void createEpic(Epic epic) {
        if (epic == null) {
            return;
        }
        epic.setId(id++);
        epics.put(epic.getId(), epic);
    }

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
    public void deleteTaskById(int id) {
        boolean isTaskExist = tasks.containsKey(id);
        if (!isTaskExist) {
            return;
        }
        tasks.remove(id);
    }

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


}
