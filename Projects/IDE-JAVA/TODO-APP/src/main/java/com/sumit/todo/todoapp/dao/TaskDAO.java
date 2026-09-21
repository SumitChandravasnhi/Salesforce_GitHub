package com.sumit.todo.todoapp.dao;

import com.sumit.todo.todoapp.database.Database;
import com.sumit.todo.todoapp.model.Task;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    public List<Task> getAllTasks() {

        List<Task> tasks = new ArrayList<>();

        String sql = """
                SELECT *
                FROM tasks
                ORDER BY completed ASC,
                         due_date ASC,
                         id DESC
                """;

        try (Connection connection = Database.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                tasks.add(mapTask(resultSet));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Could not load tasks", e);
        }

        return tasks;
    }

    public void addTask(Task task) {

        String sql = """
                INSERT INTO tasks
                (title, description, priority, due_date, completed, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = Database.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, task.getTitle());
            statement.setString(2, task.getDescription());
            statement.setString(3, task.getPriority());
            statement.setString(4, task.getDueDate());
            statement.setInt(5, task.isCompleted() ? 1 : 0);
            statement.setString(
                    6,
                    LocalDateTime.now().toString()
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Could not add task", e);
        }
    }

    public void updateTask(Task task) {

        String sql = """
                UPDATE tasks
                SET title = ?,
                    description = ?,
                    priority = ?,
                    due_date = ?,
                    completed = ?
                WHERE id = ?
                """;

        try (Connection connection = Database.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, task.getTitle());
            statement.setString(2, task.getDescription());
            statement.setString(3, task.getPriority());
            statement.setString(4, task.getDueDate());
            statement.setInt(5, task.isCompleted() ? 1 : 0);
            statement.setInt(6, task.getId());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Could not update task", e);
        }
    }

    public void deleteTask(int id) {

        String sql = "DELETE FROM tasks WHERE id = ?";

        try (Connection connection = Database.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Could not delete task", e);
        }
    }

    public void toggleCompleted(int id, boolean completed) {

        String sql = """
                UPDATE tasks
                SET completed = ?
                WHERE id = ?
                """;

        try (Connection connection = Database.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, completed ? 1 : 0);
            statement.setInt(2, id);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Could not update task status", e
            );
        }
    }

    private Task mapTask(ResultSet resultSet)
            throws SQLException {

        return new Task(
                resultSet.getInt("id"),
                resultSet.getString("title"),
                resultSet.getString("description"),
                resultSet.getString("priority"),
                resultSet.getString("due_date"),
                resultSet.getInt("completed") == 1,
                resultSet.getString("created_at")
        );
    }
}