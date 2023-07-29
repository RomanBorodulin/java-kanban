package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final HistoryManager historyManager = Managers.getDefaultHistory();
    protected Comparator<Task> taskComparatorStartTime = (o1, o2) -> {
        // компаратор, который считает, что EPOCH больше, чем другие
        if (o1.getStartTime().isEqual(LocalDate.EPOCH)) {
            return (o2.getStartTime().isEqual(LocalDate.EPOCH)) ? 0 : 1;
        } else if (o2.getStartTime().isEqual(LocalDate.EPOCH)) {
            return -1;
        } else {
            return o1.getStartTime().compareTo(o2.getStartTime());
        }
    };
    protected TreeSet<Task> prioritizedTasks = new TreeSet<>(taskComparatorStartTime
            .thenComparing(Task::getId));
    protected int id;

    // Методы получения списка всех задач
    @Override
    public ArrayList<Task> getListOfAllTasks() {
        final List<Task> tasks = new ArrayList<>(this.tasks.values());
        return tasks.stream().map(Task::new)
                .sorted(Comparator.comparing(Task::getId)).collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ArrayList<Epic> getListOfAllEpics() {
        final List<Epic> epics = new ArrayList<>(this.epics.values());
        return epics.stream().map(Epic::new)
                .sorted(Comparator.comparing(Task::getId)).collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ArrayList<Subtask> getListOfAllSubtasks() {
        final List<Subtask> subtasks = new ArrayList<>(this.subtasks.values());
        return subtasks.stream().map(Subtask::new)
                .sorted(Comparator.comparing(Task::getId)).collect(Collectors.toCollection(ArrayList::new));
    }

    // Удаление всех задач
    @Override
    public void deleteAllTasks() {
        clearAllHistory(tasks);
        prioritizedTasks.removeAll(tasks.values());
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        clearAllHistory(subtasks);
        clearAllHistory(epics);
        prioritizedTasks.removeAll(subtasks.values());
        subtasks.clear();
        epics.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        clearAllHistory(subtasks);
        for (Epic epic : epics.values()) {
            epic.clearAllSubtasks();
            changeEpicStatus(epic);
            calculateEpicTime(epic);
        }
        prioritizedTasks.removeAll(subtasks.values());
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
        validateCorrectTaskTime(task);
        task.setId(++id);
        tasks.put(task.getId(), new Task(task));
        prioritizedTasks.add(new Task(task));
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
            throw new ManagerSaveException("Эпик с таким id не существует");
        }
        validateCorrectTaskTime(subtask);
        subtask.setId(++id);
        subtasks.put(subtask.getId(), new Subtask(subtask));
        prioritizedTasks.add(new Subtask(subtask));
        Epic epic = epics.get(epicId);
        epic.addSubtask(subtask.getId());
        changeEpicStatus(epic);
        calculateEpicTime(epic);
    }

    // Обновление
    @Override
    public void updateTask(Task task) {
        if (task == null) {
            throw new ManagerSaveException("Получена нулевая задача");
        }
        boolean isTaskExist = tasks.containsKey(task.getId());
        if (!isTaskExist) {
            throw new ManagerSaveException("Задача с таким id не существует");
        }
        if (prioritizedTasks.contains(tasks.get(task.getId()))) {
            prioritizedTasks.remove(tasks.get(task.getId()));
            validateCorrectTaskTime(task);
            prioritizedTasks.add(new Task(task));
        }
        tasks.put(task.getId(), new Task(task));

    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null) {
            throw new ManagerSaveException("Получен нулевой эпик");
        }
        boolean isEpicExist = epics.containsKey(epic.getId());
        if (!isEpicExist) {
            throw new ManagerSaveException("Эпик с таким id не существует");
        }
        if (!subtasks.keySet().containsAll(epic.getSubtasks())) {
            throw new ManagerSaveException("При обновлении был получен эпик с неправильными подзадачами");
        }
        changeEpicStatus(epic);
        calculateEpicTime(epic);
        epics.put(epic.getId(), new Epic(epic));

    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null) {
            throw new ManagerSaveException("Получена нулевая подзадача");
        }
        boolean isSubtaskExist = subtasks.containsKey(subtask.getId());
        boolean isEpicExist = epics.containsKey(subtask.getEpicId());
        if (!isSubtaskExist || !isEpicExist) {
            throw new ManagerSaveException("Подзадача и/или эпик с таким id не существуют");
        }
        if (prioritizedTasks.contains(subtasks.get(subtask.getId()))) {
            prioritizedTasks.remove(subtasks.get(subtask.getId()));
            validateCorrectTaskTime(subtask);
            prioritizedTasks.add(new Subtask(subtask));
        }
        subtasks.put(subtask.getId(), new Subtask(subtask));
        Epic epic = epics.get(subtask.getEpicId());
        changeEpicStatus(epic);
        calculateEpicTime(epic);
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
            throw new ManagerRemoveException("Невозможно удалить задачу с несуществующим id");
        }
        historyManager.remove(id);
        prioritizedTasks.remove(tasks.get(id));
        tasks.remove(id);
    }

    @Override
    public void deleteEpicById(int id) {
        boolean isEpicExist = epics.containsKey(id);
        if (!isEpicExist) {
            throw new ManagerRemoveException("Невозможно удалить эпик с несуществующим id");
        }
        for (Integer key : epics.get(id).getSubtasks()) {
            historyManager.remove(key);
            prioritizedTasks.remove(subtasks.get(key));
            subtasks.remove(key);
        }
        historyManager.remove(id);
        epics.remove(id);
    }

    @Override
    public void deleteSubtaskById(int id) {
        boolean isSubtaskExist = subtasks.containsKey(id);
        if (!isSubtaskExist) {
            throw new ManagerRemoveException("Невозможно удалить подзадачу с несуществующим id");
        }
        Epic epic = epics.get(subtasks.get(id).getEpicId());
        historyManager.remove(id);
        prioritizedTasks.remove(subtasks.get(id));
        subtasks.remove(id);
        epic.removeSubtaskById(id);
        changeEpicStatus(epic);
        calculateEpicTime(epic);
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

    private void calculateEpicTime(Epic epic) {
        List<Subtask> subtasks = getListOfAllEpicSubtasks(epic);
        if (subtasks.isEmpty()) {
            return;
        }
        LocalDate startTime = subtasks.stream().map((Task::getStartTime))
                .min(LocalDate::compareTo).orElse(LocalDate.EPOCH);
        Duration duration = subtasks.stream().map(Task::getDuration)
                .reduce(Duration::plus).orElse(Duration.ZERO);
        LocalDateTime endTime = subtasks.stream().map(Task::getEndTime)
                .max(LocalDateTime::compareTo).orElse(LocalDateTime.of(LocalDate.EPOCH, LocalTime.MIN).plus(duration));
        epic.setStartTime(startTime);
        epic.setDuration(duration);
        epic.setEndTime(endTime);

    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(historyManager.getHistory());
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    private boolean isValidTaskInTime(Task task1, Task task2) {
        LocalDateTime startTimeTask1 = LocalDateTime.of(task1.getStartTime(), LocalTime.MIN);
        LocalDateTime startTimeTask2 = LocalDateTime.of(task2.getStartTime(), LocalTime.MIN);
        if (startTimeTask1.isAfter(task2.getEndTime()) || task1.getEndTime().isBefore(startTimeTask2)) {
            return true;
        }
        return false;
    }

    protected void validateCorrectTaskTime(Task task) throws ManagerSaveException {
        Optional<Task> wrongTimeTask = getPrioritizedTasks().stream()
                .takeWhile(t -> !t.getStartTime().isEqual(LocalDate.EPOCH))
                .filter(t -> !isValidTaskInTime(t, task))
                .findFirst();
        if (wrongTimeTask.isPresent()) {
            throw new ManagerSaveException("У задачи" + task + " есть пересечение во времени");
        }
    }

    private <T extends Task> void clearAllHistory(Map<Integer, T> idToTask) {
        for (Integer id : idToTask.keySet()) {
            historyManager.remove(id);
        }
    }
}
