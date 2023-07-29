package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

public class Epic extends Task {
    private ArrayList<Integer> subtasksId = new ArrayList<>();
    private LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description);
        endTime = super.getEndTime();
    }

    public Epic(Epic epic) {
        super(epic);
        if (epic != null) {
            subtasksId.addAll(new ArrayList<>(epic.getSubtasks()));
            endTime = epic.getEndTime();
        }
    }

    public ArrayList<Integer> getSubtasks() {
        return new ArrayList<>(subtasksId);
    }

    public void setSubtasks(final ArrayList<Integer> subtasksId) {
        this.subtasksId = new ArrayList<>(subtasksId);
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
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
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
                ", duration=" + duration.toMinutes() +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", subtasksId=" + subtasksId.size() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return Objects.equals(subtasksId, epic.subtasksId) && Objects.equals(endTime, epic.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subtasksId, endTime);
    }
}

