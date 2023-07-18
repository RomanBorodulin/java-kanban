package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final HistoryManager historyManager = Managers.getDefaultHistory();
    protected int id;

    // Методы получения списка всех задач
    @Override
    public ArrayList<Task> getListOfAllTasks() {
        final List<Task> tasks = new ArrayList<>(this.tasks.values());
        return tasks.stream()
                .map(Task::new).collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ArrayList<Epic> getListOfAllEpics() {
        final List<Epic> epics = new ArrayList<>(this.epics.values());
        return epics.stream()
                .map(Epic::new).collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ArrayList<Subtask> getListOfAllSubtasks() {
        final List<Subtask> subtasks = new ArrayList<>(this.subtasks.values());
        return subtasks.stream()
                .map(Subtask::new).collect(Collectors.toCollection(ArrayList::new));
    }

    // Удаление всех задач
    @Override
    public void deleteAllTasks() {
        clearAllHistory(tasks);
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        clearAllHistory(subtasks);
        clearAllHistory(epics);
        subtasks.clear();
        epics.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        clearAllHistory(subtasks);
        for (Epic epic : epics.values()) {
            epic.clearAllSubtasks();
            changeEpicStatus(epic);
        }
        subtasks.clear();
    }

    // Получение по ID
    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            task = new Task(task);
            historyManager.add(task);
            return task;
        }
        return null;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            epic = new Epic(epic);
            historyManager.add(epic);
            return epic;
        }
        return null;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            subtask = new Subtask(subtask);
            historyManager.add(subtask);
            return subtask;
        }
        return null;
    }

    // Создание
    @Override
    public void createTask(Task task) {
        if (task == null) {
            return;
        }
        task.setId(++id);
        tasks.put(task.getId(), new Task(task));
    }

    @Override
    public void createEpic(Epic epic) {
        if (epic == null) {
            return;
        }
        epic.setId(++id);
        epics.put(epic.getId(), new Epic(epic));
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
        subtask.setId(++id);
        // Добавили в мапу подзадачу
        subtasks.put(subtask.getId(), new Subtask(subtask));
        Epic epic = epics.get(epicId);
        // Добавили знание о подзадаче эпику
        epic.addSubtask(subtask.getId());
        changeEpicStatus(epic);
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
        tasks.put(task.getId(), new Task(task));
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
        epics.put(epic.getId(), new Epic(epic));
        changeEpicStatus(epic);
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
        subtasks.put(subtask.getId(), new Subtask(subtask));
        Epic epic = epics.get(subtask.getEpicId());
        changeEpicStatus(epic);
    }

    // Получение списка всех подзадач определенного эпика
    @Override
    public ArrayList<Subtask> getListOfAllEpicSubtasks(Epic epic) {
        if (epic == null) {
            return new ArrayList<>();
        }
        ArrayList<Subtask> subtasks = new ArrayList<>();
        for (Integer subtaskId : epic.getSubtasks()) {
            subtasks.add(new Subtask(this.subtasks.get(subtaskId)));
        }
        return new ArrayList<>(subtasks);
    }

    // Удаление по идентификатору
    @Override
    public void deleteTaskById(int id) {
        boolean isTaskExist = tasks.containsKey(id);
        if (!isTaskExist) {
            return;
        }
        historyManager.remove(id);
        tasks.remove(id);
    }

    @Override
    public void deleteEpicById(int id) {
        boolean isEpicExist = epics.containsKey(id);
        if (!isEpicExist) {
            return;
        }
        for (Integer key : epics.get(id).getSubtasks()) {
            historyManager.remove(key);
            subtasks.remove(key);
        }
        historyManager.remove(id);
        epics.remove(id);
    }

    @Override
    public void deleteSubtaskById(int id) {
        boolean isSubtaskExist = subtasks.containsKey(id);
        if (!isSubtaskExist) {
            return;
        }
        Epic epic = epics.get(subtasks.get(id).getEpicId());
        historyManager.remove(id);
        subtasks.remove(id);
        epic.removeSubtaskById(id);
        changeEpicStatus(epic);
    }

    private void changeEpicStatus(Epic epic) {
        ArrayList<Subtask> subtasks = getListOfAllEpicSubtasks(epic);

        if (subtasks.size() == 0) {
            epic.setTaskStatus(TaskStatus.NEW);
            return;
        }
        int counterNew = 0;
        int counterDone = 0;
        for (Subtask subtask : subtasks) {
            switch (subtask.getTaskStatus()) {
                case NEW:
                    counterNew++;
                    break;
                case DONE:
                    counterDone++;
                    break;
                case IN_PROGRESS:
                    epic.setTaskStatus(TaskStatus.IN_PROGRESS);
                    return;
            }
        }
        if (counterNew == subtasks.size()) {
            epic.setTaskStatus(TaskStatus.NEW);
        } else if (counterDone == subtasks.size()) {
            epic.setTaskStatus(TaskStatus.DONE);
        } else {
            epic.setTaskStatus(TaskStatus.IN_PROGRESS);
        }
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(historyManager.getHistory());
    }

    private <T extends Task> void clearAllHistory(Map<Integer, T> idToTask) {
        for (Integer id : idToTask.keySet()) {
            historyManager.remove(id);
        }
    }
}
