package test;

import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.Test;

import service.ManagerSaveException;
import service.TaskManager;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static model.TaskStatus.DONE;
import static model.TaskStatus.NEW;
import static model.TaskStatus.IN_PROGRESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T manager;
    int epicId;

    @Test
    void shouldReturnListOfAllTasksWhenTasksAreExist() {
        createAll();
        assertEquals(getExpectedTasks(), manager.getListOfAllTasks());

    }

    @Test
    void shouldReturnEmptyListWhenTasksAreNotExist() {
        assertTrue(manager.getListOfAllTasks().isEmpty());
    }

    @Test
    void shouldReturnListOfAllEpicsWhenEpicsAreExist() {
        createAll();
        assertEquals(getExpectedEpics(), manager.getListOfAllEpics());
    }

    @Test
    void shouldReturnEmptyListWhenEpicsAreNotExist() {
        assertTrue(manager.getListOfAllEpics().isEmpty());
    }

    @Test
    void shouldReturnListOfAllSubtasksWhenSubtasksAreExist() {
        createAll();
        assertEquals(getExpectedSubtasks(), manager.getListOfAllSubtasks());
    }

    @Test
    void shouldReturnEmptyListWhenSubtasksAreNotExist() {
        assertTrue(manager.getListOfAllSubtasks().isEmpty());
    }

    @Test
    void shouldDeleteAllTasksWhenTasksAreExistAndNotExist() {
        manager.deleteAllTasks();
        assertTrue(manager.getListOfAllTasks().isEmpty());
        createAll();
        manager.deleteAllTasks();
        assertTrue(manager.getListOfAllTasks().isEmpty());
    }

    @Test
    void shouldDeleteAllEpicsIncludingSubtasksWhenEpicsAreExistAndNotExist() {
        manager.deleteAllEpics();
        assertTrue(manager.getListOfAllEpics().isEmpty());
        assertTrue(manager.getListOfAllSubtasks().isEmpty());
        createAll();
        manager.deleteAllEpics();
        assertTrue(manager.getListOfAllEpics().isEmpty());
        assertTrue(manager.getListOfAllSubtasks().isEmpty());
    }

    @Test
    void shouldDeleteAllSubtasksAndChangeEpicsStatusWhenSubtasksAreExist() {
        createAll();
        manager.deleteAllSubtasks();
        assertTrue(manager.getListOfAllSubtasks().isEmpty());
        assertEquals(NEW, manager.getListOfAllEpics().get(0).getTaskStatus());
        assertEquals(NEW, manager.getListOfAllEpics().get(1).getTaskStatus());
    }

    @Test
    void shouldGetTaskWhenIdIsExist() {
        createAll();
        assertEquals(getExpectedTasks().get(0), manager.getTaskById(1));
        assertEquals(getExpectedEpics().get(0), manager.getEpicById(3));
        assertEquals(getExpectedSubtasks().get(0), manager.getSubtaskById(5));
    }

    @Test
    void shouldGetNullTaskWhenIdIsNotExist() {
        createAll();
        assertNull(manager.getTaskById(-1));
        assertNull(manager.getEpicById(-1));
        assertNull(manager.getSubtaskById(-1));
    }

    @Test
    void shouldCreateTask() {
        Task task = getExpectedTasks().get(0);
        manager.createTask(task);

        final List<Task> tasks = manager.getListOfAllTasks();
        final int idTask = tasks.get(0).getId();
        final Task savedTask = manager.getTaskById(idTask);
        assertNotNull(savedTask);
        assertEquals(task, savedTask);
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        assertEquals(task, tasks.get(0));
    }

    @Test
    void shouldCreateEpic() {
        Epic epic = getExpectedEpics().get(0);
        manager.createEpic(epic);

        final List<Epic> epics = manager.getListOfAllEpics();
        final int idEpic = epics.get(0).getId();
        final Epic savedEpic = manager.getEpicById(idEpic);
        assertNotNull(savedEpic);
        assertEquals(epic, savedEpic);
        assertNotNull(epics);
        assertEquals(1, epics.size());
        assertEquals(epic, epics.get(0));

    }

    @Test
    void shouldCreateSubtask() {
        Epic epic = new Epic("name", "");
        epic.addSubtask(2);
        manager.createEpic(epic);
        final List<Epic> epics = manager.getListOfAllEpics();
        Subtask subtask = new Subtask("name", "", NEW, epics.get(0).getId());
        manager.createSubtask(subtask);

        final List<Subtask> subtasks = manager.getListOfAllSubtasks();

        final Epic savedEpic = epics.get(0);
        final Subtask savedSubtask = subtasks.get(0);
        assertEquals(savedSubtask.getEpicId(), savedEpic.getId());
        assertEquals(savedEpic.getSubtasks().get(0), savedSubtask.getId());
        assertNotNull(savedSubtask);
        assertNotNull(subtasks);
        assertEquals(1, subtasks.size());
        assertEquals(subtask, subtasks.get(0));

    }

    @Test
    void shouldUpdateTaskWhenExistAndShouldNotUpdateWhenNot() {
        Task task = getExpectedTasks().get(0);
        manager.createTask(task);
        List<Task> tasks = manager.getListOfAllTasks();
        final int idTask = tasks.get(0).getId();
        Task savedTask = manager.getTaskById(idTask);

        savedTask.setTaskStatus(DONE);
        manager.updateTask(savedTask);
        tasks = manager.getListOfAllTasks();
        assertEquals(savedTask, tasks.get(0));
        assertNotNull(savedTask);
        assertNotNull(manager.getTaskById(savedTask.getId()));
        assertNotNull(tasks);
        assertEquals(1, tasks.size());

        savedTask.setId(-1);
        manager.updateTask(savedTask);
        tasks = manager.getListOfAllTasks();
        assertNull(manager.getTaskById(savedTask.getId()));
        assertNotEquals(-1, tasks.get(0).getId());
    }

    @Test
    void shouldUpdateEpicWhenExistAndShouldNotUpdateWhenNot() {
        Epic epic = new Epic("name", "");
        manager.createEpic(epic);
        List<Epic> epics = manager.getListOfAllEpics();
        final int idEpic = epics.get(0).getId();
        Epic savedEpic = manager.getEpicById(idEpic);

        savedEpic.setDescription("New Description");
        manager.updateEpic(savedEpic);
        epics = manager.getListOfAllEpics();
        assertEquals(savedEpic, epics.get(0));
        assertNotNull(savedEpic);
        assertNotNull(manager.getEpicById(savedEpic.getId()));
        assertNotNull(epics);
        assertEquals(1, epics.size());

        savedEpic.setId(-1);
        manager.updateEpic(savedEpic);
        epics = manager.getListOfAllEpics();
        assertNull(manager.getEpicById(savedEpic.getId()));
        assertNotEquals(-1, epics.get(0).getId());
    }

    @Test
    void shouldUpdateSubtaskWhenExistAndShouldNotUpdateWhenNot() {
        Epic epic = new Epic("name", "");
        epic.addSubtask(2);
        manager.createEpic(epic);
        List<Epic> epics = manager.getListOfAllEpics();
        Subtask subtask = new Subtask("name", "", NEW, epics.get(0).getId());
        manager.createSubtask(subtask);
        List<Subtask> subtasks = manager.getListOfAllSubtasks();
        final int idSubtask = subtasks.get(0).getId();
        Subtask saveSubtask = manager.getSubtaskById(idSubtask);

        saveSubtask.setTaskStatus(DONE);
        manager.updateSubtask(saveSubtask);
        epics = manager.getListOfAllEpics();
        subtasks = manager.getListOfAllSubtasks();
        assertEquals(saveSubtask, subtasks.get(0));
        assertNotNull(saveSubtask);
        assertNotNull(subtasks);
        assertNotEquals(0, subtasks.get(0).getEpicId());
        assertEquals(1, subtasks.size());
        assertNotNull(epics);
        assertEquals(DONE, epics.get(0).getTaskStatus());

        saveSubtask.setId(-1);
        manager.updateSubtask(saveSubtask);
        subtasks = manager.getListOfAllSubtasks();
        assertNull(manager.getSubtaskById(saveSubtask.getId()));
        assertNotEquals(-1, subtasks.get(0).getId());

    }

    @Test
    void shouldGetListOfAllEpicSubtasksWhenEpicAndSubtaskAreExist() {
        createAll();
        assertEquals(manager.getListOfAllSubtasks(), manager.getListOfAllEpicSubtasks(manager.getEpicById(3)));
    }

    @Test
    void shouldGetEmptyListOfAllEpicSubtasksWhenEpicIsExistAndSubtaskIsNot() {
        Epic epic = new Epic("epic", "");
        manager.createEpic(epic);
        assertTrue(manager.getListOfAllEpics().get(0).getSubtasks().isEmpty());
    }

    @Test
    void shouldDeleteTaskByIdWhenIdIsExist() {
        createAll();
        manager.deleteTaskById(1);
        assertNull(manager.getTaskById(1));
    }

    @Test
    void shouldDeleteEpicByIdWhenIdIsExistIncludingAllEpicSubtasks() {
        createAll();
        manager.deleteEpicById(3);
        assertNull(manager.getEpicById(3));
        assertTrue(manager.getListOfAllSubtasks().isEmpty());
    }

    @Test
    void shouldDeleteSubtaskByIdWhenIdIsExistIncludingDeleteIdSubtaskFromEpicAndChangeEpicStatus() {
        createAll();
        manager.deleteSubtaskById(6);
        assertNull(manager.getSubtaskById(6));
        assertEquals(1, manager.getEpicById(3).getSubtasks().size());
        assertEquals(NEW, manager.getEpicById(3).getTaskStatus());
    }

    @Test
    void shouldGetListOfPrioritizedTasks() {
        createTasks();
        createEpics();
        manager.createSubtask(new Subtask("Собрать коробки", "", NEW, epicId,
                LocalDate.parse("18.07.2023", EpicTest.DATE_FORMATTER), Duration.ofMinutes(120L))); //#5
        manager.createSubtask(new Subtask("Упаковать кошку", "", DONE, epicId,
                LocalDate.parse("10.07.2023", EpicTest.DATE_FORMATTER), Duration.ofMinutes(11500L))); //#6
        assertEquals(manager.getListOfAllSubtasks().get(1), manager.getPrioritizedTasks().get(0));
        assertEquals(List.of(6, 5, 1, 2), manager.getPrioritizedTasks().stream().map(Task::getId)
                .collect(Collectors.toList()));
    }

    @Test
    void shouldThrowExceptionWhenIntersectionInTimeIsExist() {
        createTasks();
        createEpics();
        manager.createSubtask(new Subtask("Собрать коробки", "", NEW, epicId,
                LocalDate.parse("18.07.2023", EpicTest.DATE_FORMATTER), Duration.ofMinutes(12000L))); //#5
        Subtask interSubtask = new Subtask("Упаковать кошку", "", DONE, epicId,
                LocalDate.parse("19.07.2023", EpicTest.DATE_FORMATTER), Duration.ofMinutes(11500L)); //#6
        final ManagerSaveException ex = assertThrows(ManagerSaveException.class,
                () -> manager.createSubtask(interSubtask));
        assertEquals("У задачи" + interSubtask + " есть пересечение во времени", ex.getMessage());
    }

    List<Task> getExpectedTasks() {
        Task firstTask = new Task("Прочитать книгу", "", NEW);
        firstTask.setId(1);
        Task secondTask = new Task("Протестировать программу", "", IN_PROGRESS);
        secondTask.setId(2);
        return new ArrayList<>(List.of(firstTask, secondTask));
    }

    List<Epic> getExpectedEpics() {
        Epic firstEpic = new Epic("Переезд", "Большой переезд");
        firstEpic.setId(3);
        firstEpic.addSubtask(5);
        firstEpic.addSubtask(6);
        firstEpic.setTaskStatus(IN_PROGRESS);
        Epic secondEpic = new Epic("Важный эпик 2", "Очень важный без подзадач");
        secondEpic.setId(4);
        return new ArrayList<>(List.of(firstEpic, secondEpic));
    }

    List<Subtask> getExpectedSubtasks() {
        Subtask firstSubtask = new Subtask("Собрать коробки", "", NEW, 3);
        firstSubtask.setId(5);
        Subtask secondSubtask = new Subtask("Упаковать кошку", "", DONE, 3);
        secondSubtask.setId(6);
        return new ArrayList<>(List.of(firstSubtask, secondSubtask));

    }

    void createTasks() {
        manager.createTask(new Task("Прочитать книгу", "", NEW)); //#1
        manager.createTask(new Task("Протестировать программу", "", IN_PROGRESS)); //#2
    }

    void createEpics() {
        Epic firstEpic = new Epic("Переезд", "Большой переезд");
        manager.createEpic(firstEpic); //#3
        epicId = firstEpic.getId();
        manager.createEpic(new Epic("Важный эпик 2", "Очень важный без подзадач")); //#4
    }

    void createSubtasks() {
        manager.createSubtask(new Subtask("Собрать коробки", "", NEW, epicId)); //#5
        manager.createSubtask(new Subtask("Упаковать кошку", "", DONE, epicId)); //#6
    }

    void createAll() {
        createTasks();
        createEpics();
        createSubtasks();
    }


}
