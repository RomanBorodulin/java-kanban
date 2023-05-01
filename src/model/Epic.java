package model;

import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Integer> subtasksId = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description);

    }

    public ArrayList<Integer> getSubtasks() {
        return new ArrayList<>(subtasksId);
    }

    @Override
    public void setTaskStatus(TaskStatus taskStatus) {
    }

    // Изменение статусов эпиков
    public void changeTaskStatus(ArrayList<Subtask> subtasks) {
        if (subtasks == null) {
            return;
        }
        ArrayList<Integer> listForCheck = new ArrayList<>();
        for (Subtask subtask : subtasks) {
            listForCheck.add(subtask.getId());
        }
        if (!listForCheck.containsAll(subtasksId)) {
            return;
        }
        if (subtasks.size() == 0) {
            taskStatus = TaskStatus.NEW;
            return;
        }
        int counterNew = 0;
        int counterDone = 0;
        for (Subtask subtask : subtasks) {
            switch (subtask.getTaskStatus()) {
                case NEW:
                    counterNew++;
                    break;
                case DONE:
                    counterDone++;
                    break;
                case IN_PROGRESS:
                    taskStatus = TaskStatus.IN_PROGRESS;
                    return;
            }
        }
        if (counterNew == subtasks.size()) {
            taskStatus = TaskStatus.NEW;
        } else if (counterDone == subtasks.size()) {
            taskStatus = TaskStatus.DONE;
        } else {
            taskStatus = TaskStatus.IN_PROGRESS;
        }
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

