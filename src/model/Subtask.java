package model;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;

public class Subtask extends Task {
    private int epicId;

    public Subtask(String name, String description, TaskStatus taskStatus, int epicId) {
        super(name, description, taskStatus);
        this.epicId = epicId;

    }

    public Subtask(String name, String description, TaskStatus taskStatus, int epicId,
                   LocalDate startTime, Duration duration) {
        super(name, description, taskStatus, startTime, duration);
        this.epicId = epicId;
    }

    public Subtask(Subtask subtask) {
        super(subtask);
        if (subtask != null) {
            epicId = subtask.getEpicId();
        }
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", taskStatus=" + taskStatus +
                ", duration=" + duration.toMinutes() +
                ", startTime=" + startTime +
                ", epicId=" + epicId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Subtask subtask = (Subtask) o;
        return epicId == subtask.epicId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), epicId);
    }
}
