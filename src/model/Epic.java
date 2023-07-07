package model;

import java.util.ArrayList;

public class Epic extends Task {
    private final ArrayList<Integer> subtasksId = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description);
    }

    public ArrayList<Integer> getSubtasks() {
        return new ArrayList<>(subtasksId);
    }

    public void clearAllSubtasks() {
        subtasksId.clear();
    }

    public void removeSubtaskById(Integer id) {
        subtasksId.remove(id);
    }

    public void addSubtask(int id) {
        subtasksId.add(id);
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", taskStatus=" + taskStatus +
                ", subtasks.length=" + subtasksId.size() +
                '}';
    }


}

