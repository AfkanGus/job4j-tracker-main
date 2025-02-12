package ru.job4j.action;

import ru.job4j.tracker.Input;
import ru.job4j.tracker.Output;
import ru.job4j.tracker.MemTracker;
/**
 * 2. Трекер SQL [#1734].
 */
public class Exit implements UserAction {
    private final Output out;

    public Exit(Output out) {
        this.out = out;
    }

    @Override
    public String name() {
        return "Завершить программу";
    }

    @Override
    public boolean execute(Input input, Store tracker) {
        out.println("=== Завершение программы ===");
        return false;
    }
}
