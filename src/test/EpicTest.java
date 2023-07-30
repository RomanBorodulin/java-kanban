package test;

import model.Epic;
import model.Subtask;
import model.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.KVServer;
import service.Managers;
import service.TaskManager;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static model.TaskStatus.NEW;
import static model.TaskStatus.IN_PROGRESS;
import static model.TaskStatus.DONE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EpicTest {
    TaskManager manager;
    KVServer kvServer;
    Epic epic;
    int epicId;
    static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @BeforeEach
    void beforeEach() throws IOException {
        kvServer = new KVServer();
        kvServer.start();
        epic = new Epic("Переезд", "Большой переезд");
        manager = Managers.getDefault();
        manager.createEpic(epic);
        epicId = epic.getId();
    }

    @AfterEach
    void afterEach() {
        kvServer.stop();
    }

    @Test
    void shouldReturnStatusNewWhenListOfSubtasksIsEmpty() {
        assertEquals(getTaskStatus(), NEW);

    }

    @Test
    void shouldReturnStatusNewWhenAllSubtasksAreNew() {
        createSubtasks(NEW, NEW);
        assertEquals(getTaskStatus(), NEW);
    }

    @Test
    void shouldReturnStatusDoneWhenAllSubtasksAreDone() {
        createSubtasks(DONE, DONE);
        assertEquals(getTaskStatus(), DONE);
    }

    @Test
    void shouldReturnStatusInProgressWhenSubtasksAreNewAndDone() {
        createSubtasks(NEW, DONE);
        assertEquals(getTaskStatus(), IN_PROGRESS);

    }

    @Test
    void shouldReturnStatusInProgressWhenAllSubtasksAreInProgress() {
        createSubtasks(IN_PROGRESS, IN_PROGRESS);
        assertEquals(getTaskStatus(), IN_PROGRESS);
    }

    @Test
    void shouldReturnStartTimeEpochWhenSubtasksAreNotExist() {
        assertEquals(LocalDate.EPOCH, manager.getEpicById(epicId).getStartTime());
    }

    @Test
    void shouldReturnDuration0WhenSubtasksAreNotExist() {
        assertEquals(Duration.ZERO, manager.getEpicById(epicId).getDuration());
    }

    @Test
    void shouldReturnEndTimeEqualsStartTimePlusDurationWhenSubtasksAreNotExist() {
        assertEquals(LocalDateTime.of(LocalDate.EPOCH, LocalTime.MIN).plus(Duration.ZERO),
                manager.getEpicById(epicId).getEndTime());
    }

    @Test
    void shouldReturnSmallestSubtaskStartTimeValueForEpic() {
        createSubtasks(IN_PROGRESS, IN_PROGRESS);
        assertEquals("10.07.2023", manager.getEpicById(epicId).getStartTime().format(DATE_FORMATTER));
    }

    @Test
    void shouldReturnSumOfDurationSubtasksForEpic() {
        createSubtasks(IN_PROGRESS, IN_PROGRESS);
        assertEquals(Duration.ofMinutes(120L).plus(Duration.ofMinutes(11500L)),
                manager.getEpicById(epicId).getDuration());
    }

    @Test
    void shouldReturnLargestSubtaskEndTimeValueForEpic() {
        createSubtasks(IN_PROGRESS, IN_PROGRESS);
        assertEquals("18.07.2023 02:00", manager.getEpicById(epicId).getEndTime().format(DATE_TIME_FORMATTER));
    }

    void createSubtasks(TaskStatus firstStatus, TaskStatus secondStatus) {
        manager.createSubtask(new Subtask("Собрать коробки", "", firstStatus, epicId,
                LocalDate.parse("18.07.2023", DATE_FORMATTER), Duration.ofMinutes(120L)));
        manager.createSubtask(new Subtask("Упаковать кошку", "", secondStatus, epicId,
                LocalDate.parse("10.07.2023", DATE_FORMATTER), Duration.ofMinutes(11500L)));
    }

    TaskStatus getTaskStatus() {
        return manager.getEpicById(epicId).getTaskStatus();
    }


}
