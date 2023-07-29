package test;

import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.KVServer;
import service.HttpTaskManager;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import static server.KVServer.PORT;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskManagerTest extends TaskManagerTest<HttpTaskManager> {
    KVServer kvServer;

    @BeforeEach
    void beforeEach() throws IOException {
        kvServer = new KVServer();
        kvServer.start();
        manager = new HttpTaskManager(URI.create("http://localhost:" + PORT));
    }

    @AfterEach
    void afterEach() {
        kvServer.stop();
    }

    @Test
    void load() {
        createAll();
        manager.getTaskById(1);
        manager.getEpicById(3);
        List<Task> expectedTasks = manager.getListOfAllTasks();
        List<Epic> expectedEpics = manager.getListOfAllEpics();
        List<Subtask> expectedSubtasks = manager.getListOfAllSubtasks();
        List<Task> expectedHistory = manager.getHistory();
        List<Task> expectedPrioritizedTasks = manager.getPrioritizedTasks();
        manager = new HttpTaskManager(URI.create("http://localhost:" + PORT));

        assertEquals(expectedTasks, manager.getListOfAllTasks());
        assertEquals(expectedEpics, manager.getListOfAllEpics());
        assertEquals(expectedSubtasks, manager.getListOfAllSubtasks());
        assertEquals(expectedHistory, manager.getHistory());
        assertEquals(expectedPrioritizedTasks, manager.getPrioritizedTasks());

    }
}
