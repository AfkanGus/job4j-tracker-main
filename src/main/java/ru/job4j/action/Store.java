package ru.job4j.action;

import ru.job4j.tracker.Item;

import java.util.List;

/**
 * 2. Трекер SQL [#1734].
 */
public interface Store extends AutoCloseable {
    Item add(Item item);

    boolean replace(int id, Item item);

    void delete(int id);

    List<Item> findAll();

    List<Item> findByName(String key);

    Item findById(int id);
}
