package util;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Task<T> implements Callable<Boolean> {
    private final BiConsumer<List<T>, Integer> runnable;
    private final List<T> accounts;
    private final int index;

    public Task(BiConsumer<List<T>, Integer> runnable, List<T> accounts, int index) {
        this.runnable = runnable;
        this.accounts = accounts;
        this.index = index;
    }

    @Override
    public Boolean call() throws Exception {
        runnable.accept(accounts, index);
        return true;
    }

}
