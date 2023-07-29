package model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

public class Task {
    protected String name;
    protected String description;
    protected int id = 0;
    protected TaskStatus taskStatus;
    protected Duration duration; //in minutes
    protected LocalDate startTime;

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        this.taskStatus = TaskStatus.NEW;
        this.startTime = LocalDate.EPOCH;
        this.duration = Duration.ZERO;
    }

    public Task(String name, String description, TaskStatus taskStatus) {
        this(name, description);
        this.taskStatus = taskStatus;
    }

    public Task(String name, String description, TaskStatus taskStatus, LocalDate startTime, Duration duration) {
        this(name, description, taskStatus);
        this.startTime = startTime;
        this.duration = duration;
    }

    public Task(Task task) {
        if (task != null) {
            name = task.getName();
            description = task.getDescription();
            taskStatus = task.getTaskStatus();
            id = task.getId();
            startTime = task.getStartTime();
            duration = task.getDuration();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public TaskType getType() {
        return TaskType.TASK;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDate getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDate startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return LocalDateTime.of(startTime, LocalTime.MIN).plus(duration);
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", taskStatus=" + taskStatus +
                ", duration=" + duration.toMinutes() +
                ", startTime=" + startTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id && Objects.equals(name, task.name)
                && Objects.equals(description, task.description) && taskStatus == task.taskStatus
                && Objects.equals(duration, task.duration) && Objects.equals(startTime, task.startTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, id, taskStatus, duration, startTime);
    }
}
