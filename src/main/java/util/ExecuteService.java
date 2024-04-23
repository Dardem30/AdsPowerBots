package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

public class ExecuteService {
    private static ExecuteService executeService;

    public static ExecuteService getInstance() {
        if (executeService == null) {
            executeService = new ExecuteService();
        }
        return executeService;
    }

    public <T> void execute(final List<T> list, final BiConsumer<List<T>, Integer> runnable, final int size) throws Exception {
        Collections.shuffle(list);
        final List<Task<T>> tasks = new ArrayList<>();
        int index = 0;
        for (final List<T> chunk : splitList(list, size)) {
            index++;
            tasks.add(new Task<T>(runnable, chunk, index));
        }
        Executors.newFixedThreadPool(tasks.size()).invokeAll(tasks);
    }

    private <T> List<List<T>> splitList(final List<T> list, final int size) {
        final List<List<T>> result = new ArrayList<>();
        List<T> item = null;
        for (int index = 0; index < list.size(); index++) {
            if (index % size == 0) {
                item = new ArrayList<>();
                result.add(item);
            }
            item.add(list.get(index));
        }
        return result;
    }
    public void executeAsynchronously(final Runnable runnable) {
        new Thread(runnable).start();
    }
}
