package service;

import model.*;


import java.util.ArrayList;
import java.util.List;

public interface TaskManager {

    // Методы получения списка всех задач
    ArrayList<Task> getListOfAllTasks();

    ArrayList<Epic> getListOfAllEpics();

    ArrayList<Subtask> getListOfAllSubtasks();

    // Удаление всех задач
    void deleteAllTasks();

    void deleteAllEpics();

    void deleteAllSubtasks();

    // Получение по ID
    Task getTaskById(int id);

    Epic getEpicById(int id);

    Subtask getSubtaskById(int id);

    // Создание
    void createTask(Task task);

    void createEpic(Epic epic);

    void createSubtask(Subtask subtask);

    // Обновление
    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubtask(Subtask subtask);

    // Получение списка всех подзадач определенного эпика
    ArrayList<Subtask> getListOfAllEpicSubtasks(Epic epic);

    // Удаление по идентификатору
    void deleteTaskById(int id);

    void deleteEpicById(int id);

    void deleteSubtaskById(int id);

    List<Task> getHistory();


}
