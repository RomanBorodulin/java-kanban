import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import server.KVServer;
import service.Managers;
import service.TaskManager;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        new KVServer().start();
        TaskManager manager = Managers.getDefault();


        manager.createTask(new Task("Прочитать книгу", "", TaskStatus.IN_PROGRESS)); //#1
        manager.createTask(new Task("Протестировать программу", "", TaskStatus.IN_PROGRESS)); //#2

        Epic firstEpic = new Epic("Переезд", ""); //#3
        manager.createEpic(firstEpic);
        int epicId = firstEpic.getId();
        manager.createSubtask(new Subtask("Собрать коробки", "", TaskStatus.NEW, epicId)); //#4
        manager.createSubtask(new Subtask("Упаковать кошку", "", TaskStatus.NEW, epicId)); //#5
        manager.createSubtask(new Subtask("Заказать перевозку", "", TaskStatus.NEW, epicId)); //#6

        Epic secondEpic = new Epic("Важный эпик 2", "Очень важный без подзадач"); //#7
        manager.createEpic(secondEpic);

        manager.getTaskById(1);
        manager.getTaskById(2);
        manager.getEpicById(3);
        manager.getSubtaskById(5);
        manager.getSubtaskById(4);
        manager.getTaskById(1);
        manager.getSubtaskById(5);
        manager.getSubtaskById(6);
        System.out.println(manager.getHistory()); // 2-3-4-1-5-6
        System.out.println("------------------------------------------------------------------");
        manager.deleteTaskById(1); // 2-3-4-5-6
        System.out.println(manager.getHistory());
        System.out.println("------------------------------------------------------------------");
        manager = Managers.getDefault();
        //manager.deleteEpicById(3); // 2
        //System.out.println(manager.getHistory());
    }
}
