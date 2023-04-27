import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import service.Manager;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        Manager manager = new Manager();
        Epic firstEpic = new Epic("Переезд", "");
        manager.createEpic(firstEpic);
        int epicId = firstEpic.getId();
        manager.createSubtask(new Subtask("Собрать коробки", "", TaskStatus.NEW, epicId));
        manager.createSubtask(new Subtask("Упаковать кошку", "", TaskStatus.NEW, epicId));

        Epic secondEpic = new Epic("Важный эпик 2", "Очень важный");
        manager.createEpic(secondEpic);
        epicId = secondEpic.getId();
        manager.createSubtask(new Subtask("Задача 1", "", TaskStatus.NEW, epicId));
        for (Task task : manager.getListOfAllTasks()) {
            System.out.println(task);
        }
        for (Epic epic : manager.getListOfAllEpics()) {
            System.out.println(epic);
        }
        for (Subtask subtask : manager.getListOfAllSubtasks()) {
            System.out.println(subtask);
        }
        System.out.println("------------------------------------------------------------------");
        ArrayList<Subtask> subtasks = manager.getListOfAllSubtasks();
        for (Subtask subtask : subtasks) {
            subtask.setTaskStatus(TaskStatus.DONE);
            manager.updateSubtask(subtask);
        }
        subtasks.get(0).setTaskStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subtasks.get(0));
        for (Epic epic : manager.getListOfAllEpics()) {
            System.out.println(epic);
        }
        System.out.println("------------------------------------------------------------------");
        manager.deleteSubtaskById(5);
        //manager.deleteAllSubtasks();
        System.out.println(manager.getListOfAllEpics());
        System.out.println(manager.getListOfAllSubtasks());
        //manager.deleteEpicById(1);
        manager.deleteAllEpics();
        System.out.println(manager.getListOfAllEpics());
        System.out.println(manager.getListOfAllSubtasks());
        manager.getSubtaskById(2).setName("Собрать книги");

        manager.updateSubtask(manager.getSubtaskById(2));

    }

}
