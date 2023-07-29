package service;

import java.net.URI;

import static server.KVServer.PORT;

public final class Managers {
    private Managers() {
    }

    public static TaskManager getDefault() {
        return new HttpTaskManager(URI.create("http://localhost:" + PORT));
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
