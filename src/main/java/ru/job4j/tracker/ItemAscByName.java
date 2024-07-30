package ru.job4j.tracker;

import java.util.Comparator;
/**
 * 2. Трекер SQL [#1734].
 */
public class ItemAscByName implements Comparator<Item> {
    @Override
    public int compare(Item left, Item right) {
        return left.getName().compareTo(right.getName());
    }
}
