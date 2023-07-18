package test;

import model.Epic;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.FileBackedTasksManager;
import service.ManagerSaveException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileBackedTasksManagerTest extends TaskManagerTest<FileBackedTasksManager> {
    static Path path;

    @BeforeEach
    void beforeEach() {
        path = Paths.get("test.csv");
        manager = new FileBackedTasksManager(path);
    }

    @AfterEach
    void afterEach() {
        manager = new FileBackedTasksManager(path);
        manager.getEpicById(-1); //save() empty
    }

    @AfterAll
    static void afterAll() {
        try {
            Files.delete(path);
            path = Path.of("test1.csv");
            Files.delete(path);
        } catch (NoSuchFileException e) {
            System.err.format("%s: такого файла нет%n", path);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    void shouldThrowExceptionWhenFileIsNotExist() throws RuntimeException {
        final ManagerSaveException ex = assertThrows(ManagerSaveException.class,
                () -> {
                    path = Paths.get("test1.csv");
                    readFile();
                });
        assertEquals("Ошибка чтения файла", ex.getMessage());

    }

    @Test
    void shouldLoadEmptyListOfTasksWhenFileIsEmpty() {
        manager = FileBackedTasksManager.loadFromFile(path);
        assertTrue(manager.getListOfAllTasks().isEmpty());
        assertTrue(manager.getListOfAllEpics().isEmpty());
        assertTrue(manager.getListOfAllSubtasks().isEmpty());
        assertTrue(manager.getHistory().isEmpty());

    }

    @Test
    void shouldLoadEpicWhenEpicWithoutSubtask() {
        Epic epic = getExpectedEpics().get(1);
        manager.createEpic(epic);
        int idEpic = manager.getListOfAllEpics().get(0).getId();
        manager = FileBackedTasksManager.loadFromFile(path);
        assertEquals(epic, manager.getEpicById(idEpic));
        assertTrue(manager.getEpicById(idEpic).getSubtasks().isEmpty());
        assertEquals(List.of(epic), manager.getHistory());
    }

    @Test
    void shouldLoadEmptyListOfHistory() {
        createAll();
        assertTrue(manager.getHistory().isEmpty());

    }

    @Test
    void shouldLoadHistory() {
        createAll();
        manager.getTaskById(2);
        assertEquals(List.of(manager.getListOfAllTasks().get(1)), manager.getHistory());
    }

    @Test
    void shouldSaveEmptyFileWhenListOfTasksIsEmpty() {
        assertTrue(manager.getListOfAllTasks().isEmpty());
        assertTrue(manager.getListOfAllEpics().isEmpty());
        assertTrue(manager.getListOfAllSubtasks().isEmpty());
        assertNull(manager.getEpicById(3)); //save()
        String string = readFile();
        assertTrue(string.isEmpty());

    }

    @Test
    void shouldSaveEpicWithoutSubtask() {
        Epic epic = getExpectedEpics().get(1);
        manager.createEpic(epic);
        manager = FileBackedTasksManager.loadFromFile(path);
        assertNotNull(manager.getEpicById(1));
        assertTrue(manager.getEpicById(1).getSubtasks().isEmpty());

    }

    @Test
    void shouldSaveEmptyHistory() {
        createAll();
        manager = FileBackedTasksManager.loadFromFile(path);
        assertTrue(manager.getHistory().isEmpty());

    }

    String readFile() {
        StringBuilder stringBuilder = new StringBuilder();
        try (Reader fileReader = new FileReader(path.getFileName().toString(), StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(fileReader)) {
            while (br.ready()) {
                String line = br.readLine();
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка чтения файла");
        }
        return stringBuilder.toString();
    }
}
