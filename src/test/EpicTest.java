package test;

import model.Epic;
import model.Subtask;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.Managers;
import service.TaskManager;

import static model.TaskStatus.NEW;
import static model.TaskStatus.IN_PROGRESS;
import static model.TaskStatus.DONE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EpicTest {
    TaskManager manager;
    Epic epic;
    int epicId;

    @BeforeEach
    void beforeEach() {
        epic = new Epic("Переезд", "Большой переезд");
        manager = Managers.getDefault();
        manager.createEpic(epic);
        epicId = epic.getId();
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

    void createSubtasks(TaskStatus firstStatus, TaskStatus secondStatus) {
        manager.createSubtask(new Subtask("Собрать коробки", "", firstStatus, epicId));
        manager.createSubtask(new Subtask("Упаковать кошку", "", secondStatus, epicId));
    }

    TaskStatus getTaskStatus() {
        return manager.getEpicById(epicId).getTaskStatus();
    }


}
