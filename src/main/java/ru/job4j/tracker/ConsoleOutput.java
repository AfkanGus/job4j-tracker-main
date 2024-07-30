package ru.job4j.tracker;
/**
 * 2. Трекер SQL [#1734].
 */
public class ConsoleOutput implements Output {
    @Override
    public void println(Object obj) {
        System.out.println(obj);
    }
}
