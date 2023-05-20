package service;

import model.Task;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final static int SIZE_OF_VIEWED_TASKS = 10;
    private final LinkedList<Task> history = new LinkedList<>();

    @Override
    public void add(Task task) {
        if (history.size() >= SIZE_OF_VIEWED_TASKS) {
            history.removeFirst();
            history.addLast(task);
            return;
        }
        history.add(task);
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }
}
