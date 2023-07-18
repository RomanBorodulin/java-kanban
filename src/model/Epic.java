package model;

import java.util.ArrayList;
import java.util.Objects;

public class Epic extends Task {
    private final ArrayList<Integer> subtasksId = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description);
    }

    public Epic(Epic epic) {
        super(epic);
        if (epic != null) {
            subtasksId.addAll(new ArrayList<>(epic.getSubtasks()));
        }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return Objects.equals(subtasksId, epic.subtasksId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subtasksId);
    }
}

