package test;

import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.HistoryManager;
import service.Managers;
import service.TaskManager;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HistoryManagerTest {
    TaskManager taskManager;

    @BeforeEach
    void beforeEach() {
        taskManager = Managers.getDefault();
        Arrays.asList(1, 2, 3, 4).forEach(i ->
                taskManager.createTask(new Task("task" + i, "description" + i)));

    }

    @Test
    void shouldGetEmptyListOfHistoryWhenHistoryIsNotExist() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertTrue(historyManager.getHistory().isEmpty());
        assertTrue(taskManager.getHistory().isEmpty());
    }

    @Test
    void shouldGetHistoryWithoutDuplication() {
        Arrays.asList(1, 2, 3, 4).forEach(i -> taskManager.getTaskById(i));
        taskManager.getTaskById(2);
        assertNotEquals(5, taskManager.getHistory().size());
        assertEquals(List.of(1, 3, 4, 2), taskManager.getHistory().stream()
                .map(Task::getId).collect(Collectors.toList()));
    }

    @Test
    void shouldDeleteHistoryAtTheBeginningMiddleAndEnd() {
        Arrays.asList(1, 2, 3, 4).forEach(i -> taskManager.getTaskById(i));
        taskManager.deleteTaskById(1);
        assertEquals(3, taskManager.getHistory().size());
        assertEquals(List.of(2, 3, 4), taskManager.getHistory().stream()
                .map(Task::getId).collect(Collectors.toList()));

        taskManager.deleteTaskById(3);
        assertEquals(2, taskManager.getHistory().size());
        assertEquals(List.of(2,4), taskManager.getHistory().stream()
                .map(Task::getId).collect(Collectors.toList()));

        taskManager.deleteTaskById(4);
        assertEquals(1, taskManager.getHistory().size());
        assertEquals(2, taskManager.getHistory().get(0).getId());
    }
}
