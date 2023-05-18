import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import service.Managers;
import service.TaskManager;


import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();
        Epic firstEpic = new Epic("Переезд", "");
        manager.createEpic(firstEpic);
        int epicId = firstEpic.getId();
        manager.createSubtask(new Subtask("Собрать коробки", "", TaskStatus.NEW, epicId));
        manager.createSubtask(new Subtask("Упаковать кошку", "", TaskStatus.NEW, epicId));

        Epic secondEpic = new Epic("Важный эпик 2", "Очень важный");
        manager.createEpic(secondEpic);
        epicId = secondEpic.getId();
        manager.createSubtask(new Subtask("Задача 1", "", TaskStatus.DONE, epicId));
        manager.createTask(new Task("Задача", "", TaskStatus.IN_PROGRESS));
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
        subtasks.get(0).setName("Измененная подзадача о переезде");
        subtasks.get(0).setId(10); // Значение ID не изменилось
        manager.updateSubtask(subtasks.get(0));
        System.out.println(subtasks.get(0));
        firstEpic.setDescription("Обновили описание");
        manager.updateEpic(firstEpic);
        System.out.println(manager.getListOfAllEpics());
        System.out.println("------------------------------------------------------------------");
        manager.deleteSubtaskById(5);
/*        //manager.deleteAllSubtasks();
        System.out.println(manager.getListOfAllEpics());
        System.out.println(manager.getListOfAllSubtasks());
        manager.deleteEpicById(1);
        //manager.deleteAllEpics();
        System.out.println(manager.getListOfAllEpics());
        System.out.println(manager.getListOfAllSubtasks());*/
        System.out.println("------------------------------------------------------------------");
        manager.getEpicById(1);
        manager.getEpicById(4);
        manager.getSubtaskById(3);
        manager.getSubtaskById(2);
        manager.getSubtaskById(1); // не попадет в историю просмотров, так как не существует
        manager.getTaskById(6);
        manager.getSubtaskById(5);
        manager.getEpicById(1);
        manager.getSubtaskById(2);
        manager.getSubtaskById(2);
        manager.getTaskById(6);
        manager.getEpicById(1);
        System.out.println(manager.getHistory());


    }

}
