package ru.job4j.tracker;
/**
 * 2. Трекер SQL [#1734].
 */
public interface Input {
    String askStr(String question);

    int askInt(String question);
}
