package ru.job4j.action;

import ru.job4j.tracker.Item;

import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 2. Трекер SQL [#1734].
 */
public class SqlTracker implements Store {
    private Connection connection;

    public SqlTracker() {
        init();
    }

    public SqlTracker(Connection connection) {
        this.connection = connection;
    }

    private void init() {
        try (InputStream input = SqlTracker.class.getClassLoader()
                .getResourceAsStream("db/liquibase.properties")) {
            Properties config = new Properties();
            config.load(input);
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

    @Override
    public Item add(Item item) {
        Timestamp timestampFromLDT = Timestamp.valueOf(item.getCreated());
        String sql = "INSERT INTO items (name, created) VALUES (?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, item.getName());
            statement.setTimestamp(2, timestampFromLDT);
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    item.setId(generatedId);
                    return item;
                } else {
                    throw new SQLException("Creating item failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error adding item to the database", e);
        }
    }

    @Override
    public boolean replace(int id, Item item) {
        Timestamp timestampFromLDT = Timestamp.valueOf(item.getCreated());
        String sql = "UPDATE items SET name = ?, created = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, item.getName());
            statement.setTimestamp(2, timestampFromLDT);
            statement.setInt(3, id);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Error replacing item in the database", e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM items WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Error deleting item from the database", e);
        }
    }

    private Item createItemFromResultSet(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String name = resultSet.getString("name");
        Timestamp timestamp = resultSet.getTimestamp("created");
        Item item = new Item(id, name, timestamp.toLocalDateTime());
        item.setId(id);
        return item;
    }

    @Override
    public List<Item> findAll() {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM items";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                items.add(createItemFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error retrieving items from the database", e);
        }

        return items;
    }

    @Override
    public List<Item> findByName(String key) {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM items WHERE name LIKE ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + key + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    items.add(createItemFromResultSet(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error retrieving items by name from the database", e);
        }

        return items;
    }

    @Override
    public Item findById(int id) {
        Item item = null;
        String sql = "SELECT * FROM items WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    item = createItemFromResultSet(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error retrieving item by ID from the database", e);
        }

        return item;
    }

    @Override
    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }
}
