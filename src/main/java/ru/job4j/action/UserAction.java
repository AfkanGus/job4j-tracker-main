package ru.job4j.action;

import ru.job4j.tracker.Input;
import ru.job4j.tracker.MemTracker;
/**
 * 2. Трекер SQL [#1734].
 */
public interface UserAction {
    String name();

    boolean execute(Input input, Store tracker);
}
