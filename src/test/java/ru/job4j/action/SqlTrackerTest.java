package ru.job4j.action;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.job4j.tracker.Item;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.*;

/**
 * 3. Liquibase. Интеграционные тесты. [#504894 #507701].
 */
public class SqlTrackerTest {

    private static Connection connection;

    @BeforeAll
    public static void initConnection() {
        try (InputStream in = SqlTracker.class.getClassLoader().getResourceAsStream("db/liquibase_test.properties")) {
            Properties config = new Properties();
            config.load(in);
            Class.forName(config.getProperty("driver-class-name"));
            connection = DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password")

            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @AfterAll
    public static void closeConnection() throws SQLException {
        connection.close();
    }

    @AfterEach
    public void wipeTable() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("delete from items")) {
            statement.execute();
        }
    }

    @Test
    public void whenSaveItemAndFindByGeneratedIdThenMustBeTheSame() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item = new Item("item");
        tracker.add(item);
        assertThat(tracker.findById(item.getId())).isEqualTo(item);
    }

    @Test
    void whenReplaceItemAndFindByGeneratedIdThenMustBeTheSame() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item = tracker.add(new Item("itemNew"));
        Item item1 = new Item("item1");
        tracker.replace(item.getId(), item1);
        item1.setId(item.getId());
        assertThat(tracker.findById(item.getId())).isEqualTo(item1);
    }

    @Test
    public void whenDeleteItemThenCannotFindById() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item = tracker.add(new Item("item"));
        tracker.delete(item.getId());
        assertThat(tracker.findById(item.getId())).isNull();
    }

    @Test
    public void whenFindAllThenReturnAllItems() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item1 = tracker.add(new Item("item1"));
        Item item2 = tracker.add(new Item("item2"));
        List<Item> expected = new ArrayList<>();
        expected.add(item1);
        expected.add(item2);
        assertThat(tracker.findAll()).isEqualTo(expected);
    }

    @Test
    public void whenFindByNameThenReturnMatchingItems() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item1 = tracker.add(new Item("item"));
        Item item2 = tracker.add(new Item("item"));
        Item item3 = tracker.add(new Item("otherItem"));
        List<Item> expected = new ArrayList<>();
        expected.add(item1);
        expected.add(item2);
        assertThat(tracker.findByName("item")).isEqualTo(expected);
    }
}