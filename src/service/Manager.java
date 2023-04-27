package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import service.storage.TaskStorage;

import java.util.ArrayList;
import java.util.HashMap;

public class Manager {
    private TaskStorage taskStorage = new TaskStorage();
    private int id = 1;

    // Методы получения списка всех задач
    public ArrayList<Task> getListOfAllTasks() {
        return new ArrayList<>(taskStorage.getTasks().values());
    }

    public ArrayList<Epic> getListOfAllEpics() {
        return new ArrayList<>(taskStorage.getEpics().values());
    }

    public ArrayList<Subtask> getListOfAllSubtasks() {
        return new ArrayList<>(taskStorage.getSubtasks().values());
    }

    // Удаление всех задач
    public void deleteAllTasks() {
        taskStorage.getTasks().clear();
    }

    public void deleteAllEpics() {
        taskStorage.getSubtasks().clear();
        taskStorage.getEpics().clear();
    }

    public void deleteAllSubtasks() {
        HashMap<Integer, Epic> epics = taskStorage.getEpics();
        for (Epic epic : epics.values()) {
            epic.getSubtasks().clear();
            epic.setTaskStatus(updateEpicStatus(epic));
        }
        taskStorage.getSubtasks().clear();
    }

    // Получение по ID
    public Task getTaskById(int id) {
        return taskStorage.getTasks().get(id);
    }

    public Epic getEpicById(int id) {
        return taskStorage.getEpics().get(id);
    }

    public Subtask getSubtaskById(int id) {
        return taskStorage.getSubtasks().get(id);
    }

    // Создание
    public void createTask(Task task) {
        if (task == null) {
            return;
        }
        task.setId(id++);
        taskStorage.getTasks().put(task.getId(), task);
    }

    public void createEpic(Epic epic) {
        if (epic == null) {
            return;
        }
        epic.setId(id++);
        taskStorage.getEpics().put(epic.getId(), epic);
    }

    public void createSubtask(Subtask subtask) {
        if (subtask == null) {
            return;
        }
        int epicId = subtask.getEpicId();
        boolean isEpicExist = taskStorage.getEpics().containsKey(epicId);
        if (!isEpicExist) {
            return;
        }
        subtask.setId(id++);
        // Добавили в мапу подзадачу
        taskStorage.getSubtasks().put(subtask.getId(), subtask);
        // Добавили знание о подзадаче эпику
        taskStorage.getEpics().get(epicId).getSubtasks().put(subtask.getId(), subtask);
        Epic epic = taskStorage.getEpics().get(epicId);
        epic.setTaskStatus(updateEpicStatus(epic));
    }

    // Обновление
    public void updateTask(Task task) {
        if (task == null) {
            return;
        }
        boolean isTaskExist = taskStorage.getTasks().containsKey(task.getId());
        if (!isTaskExist) {
            return;
        }
        taskStorage.getTasks().put(task.getId(), task);
    }

    public void updateEpic(Epic epic) {
        if (epic == null) {
            return;
        }
        boolean isEpicExist = taskStorage.getEpics().containsKey(epic.getId());
        if (!isEpicExist) {
            return;
        }
        taskStorage.getEpics().put(epic.getId(), epic);
        taskStorage.getEpics().get(epic.getId()).setTaskStatus(updateEpicStatus(epic));
    }

    public void updateSubtask(Subtask subtask) {
        if (subtask == null) {
            return;
        }
        boolean isSubtaskExist = taskStorage.getSubtasks().containsKey(subtask.getId());
        boolean isEpicExist = taskStorage.getEpics().containsKey(subtask.getEpicId());
        if (!isSubtaskExist || !isEpicExist) {
            return;
        }
        taskStorage.getSubtasks().put(subtask.getId(), subtask);
        Epic epic = taskStorage.getEpics().get(subtask.getEpicId());
        epic.setTaskStatus(updateEpicStatus(epic));
    }

    // Управление статусами для эпиков
    private TaskStatus updateEpicStatus(Epic epic) {
        ArrayList<Subtask> listOfSubtasks = getListOfAllEpicSubtasks(epic);
        if (listOfSubtasks.size() == 0) {
            return TaskStatus.NEW;
        }
        int counterNew = 0;
        int counterDone = 0;
        for (Subtask subtask : listOfSubtasks) {
            switch (subtask.getTaskStatus()) {
                case NEW:
                    counterNew++;
                    break;
                case DONE:
                    counterDone++;
                    break;
                case IN_PROGRESS:
                    return TaskStatus.IN_PROGRESS;
            }
        }
        if (counterNew == listOfSubtasks.size()) {
            return TaskStatus.NEW;
        }
        if (counterDone == listOfSubtasks.size()) {
            return TaskStatus.DONE;
        }
        return TaskStatus.IN_PROGRESS;
    }

    // Получение списка всех подзадач определенного эпика
    public ArrayList<Subtask> getListOfAllEpicSubtasks(Epic epic) {
        if (epic == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(epic.getSubtasks().values());
    }

    // Удаление по идентификатору
    public void deleteTaskById(int id) {
        boolean isTaskExist = taskStorage.getTasks().containsKey(id);
        if (!isTaskExist) {
            return;
        }
        taskStorage.getTasks().remove(id);
    }

    public void deleteEpicById(int id) {
        boolean isEpicExist = taskStorage.getEpics().containsKey(id);
        if (!isEpicExist) {
            return;
        }
        for (Integer key : taskStorage.getEpics().get(id).getSubtasks().keySet()) {
            taskStorage.getSubtasks().remove(key);
        }
        taskStorage.getEpics().remove(id);
    }

    public void deleteSubtaskById(int id) {
        boolean isSubtaskExist = taskStorage.getSubtasks().containsKey(id);
        if (!isSubtaskExist) {
            return;
        }
        Epic epic = taskStorage.getEpics().get(taskStorage.getSubtasks().get(id).getEpicId());
        taskStorage.getSubtasks().remove(id);
        epic.getSubtasks().remove(id);
        epic.setTaskStatus(updateEpicStatus(epic));
    }


}
