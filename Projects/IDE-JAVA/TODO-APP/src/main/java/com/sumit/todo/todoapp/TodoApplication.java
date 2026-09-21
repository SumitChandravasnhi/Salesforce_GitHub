package com.sumit.todo.todoapp;

import com.sumit.todo.todoapp.dao.TaskDAO;
import com.sumit.todo.todoapp.database.Database;
import com.sumit.todo.todoapp.model.Task;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class TodoApplication extends Application {

    private final TaskDAO taskDAO = new TaskDAO();

    private final ObservableList<Task> tasks =
            FXCollections.observableArrayList();

    private final ListView<Task> taskList =
            new ListView<>(tasks);

    private final TextField searchField =
            new TextField();

    private final ComboBox<String> filterBox =
            new ComboBox<>();

    @Override
    public void start(Stage stage) {

        Database.initialize();

        loadTasks();

        BorderPane root = createLayout();

        Scene scene = new Scene(root, 1000, 650);

        stage.setTitle("My To-Do List");
        stage.setScene(scene);
        stage.setMinWidth(850);
        stage.setMinHeight(550);

        stage.show();
    }

    private BorderPane createLayout() {

        BorderPane root = new BorderPane();

        root.setPadding(new Insets(20));

        VBox top = createHeader();

        root.setTop(top);

        root.setCenter(taskList);

        HBox bottom = createButtons();

        root.setBottom(bottom);

        configureTaskList();

        return root;
    }

    private VBox createHeader() {

        Label title = new Label("My To-Do List");

        title.setStyle("""
                -fx-font-size: 28px;
                -fx-font-weight: bold;
                """);

        searchField.setPromptText("Search tasks...");

        filterBox.getItems().addAll(
                "All",
                "Pending",
                "Completed"
        );

        filterBox.setValue("All");

        searchField.textProperty()
                .addListener((obs, oldValue, newValue)
                        -> refreshList());

        filterBox.valueProperty()
                .addListener((obs, oldValue, newValue)
                        -> refreshList());

        HBox controls = new HBox(
                10,
                searchField,
                filterBox
        );

        controls.setAlignment(Pos.CENTER_LEFT);

        HBox.setHgrow(
                searchField,
                Priority.ALWAYS
        );

        VBox header = new VBox(
                15,
                title,
                controls
        );

        header.setPadding(
                new Insets(0, 0, 20, 0)
        );

        return header;
    }

    private void configureTaskList() {

        taskList.setCellFactory(list ->

                new ListCell<>() {

                    private final CheckBox checkBox =
                            new CheckBox();

                    private final Label title =
                            new Label();

                    private final Label details =
                            new Label();

                    private final VBox textBox =
                            new VBox(5, title, details);

                    private final HBox container =
                            new HBox(12, checkBox, textBox);

                    {
                        container.setAlignment(
                                Pos.CENTER_LEFT
                        );

                        setPadding(
                                new Insets(10)
                        );

                        checkBox.setOnAction(event -> {

                            Task task = getItem();

                            if (task != null) {

                                task.setCompleted(
                                        checkBox.isSelected()
                                );

                                taskDAO.toggleCompleted(
                                        task.getId(),
                                        task.isCompleted()
                                );

                                refreshList();
                            }
                        });

                        setOnMouseClicked(event -> {

                            if (event.getClickCount() == 2) {

                                Task task = getItem();

                                if (task != null) {
                                    showTaskDialog(task);
                                }
                            }
                        });
                    }

                    @Override
                    protected void updateItem(
                            Task task,
                            boolean empty) {

                        super.updateItem(task, empty);

                        if (empty || task == null) {

                            setGraphic(null);

                        } else {

                            checkBox.setSelected(
                                    task.isCompleted()
                            );

                            title.setText(
                                    task.getTitle()
                            );

                            details.setText(
                                    "Priority: "
                                            + task.getPriority()
                                            + "   •   Due: "
                                            + formatDueDate(
                                            task.getDueDate()
                                    )
                            );

                            if (task.isCompleted()) {

                                title.setStyle("""
                                        -fx-strikethrough: true;
                                        -fx-text-fill: gray;
                                        -fx-font-size: 16px;
                                        """);

                            } else {

                                title.setStyle("""
                                        -fx-font-size: 16px;
                                        -fx-font-weight: bold;
                                        """);
                            }

                            setGraphic(container);
                        }
                    }
                }
        );
    }

    private HBox createButtons() {

        Button addButton =
                new Button("+ Add Task");

        Button editButton =
                new Button("Edit");

        Button deleteButton =
                new Button("Delete");

        Button completeButton =
                new Button("Complete");

        addButton.setOnAction(
                event -> showTaskDialog(null)
        );

        editButton.setOnAction(event -> {

            Task task =
                    taskList.getSelectionModel()
                            .getSelectedItem();

            if (task != null) {
                showTaskDialog(task);
            }
        });

        deleteButton.setOnAction(event -> {

            Task task =
                    taskList.getSelectionModel()
                            .getSelectedItem();

            if (task != null) {

                Alert alert = new Alert(
                        Alert.AlertType.CONFIRMATION
                );

                alert.setTitle("Delete Task");
                alert.setHeaderText(
                        "Delete selected task?"
                );
                alert.setContentText(
                        task.getTitle()
                );

                alert.showAndWait().ifPresent(
                        result -> {

                            if (result ==
                                    ButtonType.OK) {

                                taskDAO.deleteTask(
                                        task.getId()
                                );

                                loadTasks();
                            }
                        }
                );
            }
        });

        completeButton.setOnAction(event -> {

            Task task =
                    taskList.getSelectionModel()
                            .getSelectedItem();

            if (task != null) {

                task.setCompleted(
                        !task.isCompleted()
                );

                taskDAO.toggleCompleted(
                        task.getId(),
                        task.isCompleted()
                );

                refreshList();
            }
        });

        HBox buttons = new HBox(
                10,
                addButton,
                editButton,
                deleteButton,
                completeButton
        );

        buttons.setAlignment(
                Pos.CENTER_LEFT
        );

        buttons.setPadding(
                new Insets(20, 0, 0, 0)
        );

        return buttons;
    }

    private void showTaskDialog(Task existingTask) {

        Dialog<Task> dialog = new Dialog<>();

        dialog.setTitle(
                existingTask == null
                        ? "Add Task"
                        : "Edit Task"
        );

        ButtonType saveButtonType =
                new ButtonType(
                        "Save",
                        ButtonBar.ButtonData.OK_DONE
                );

        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(
                        saveButtonType,
                        ButtonType.CANCEL
                );

        TextField titleField =
                new TextField();

        titleField.setPromptText(
                "Task title"
        );

        TextArea descriptionField =
                new TextArea();

        descriptionField.setPromptText(
                "Description"
        );

        descriptionField.setPrefRowCount(4);

        ComboBox<String> priorityBox =
                new ComboBox<>();

        priorityBox.getItems().addAll(
                "LOW",
                "MEDIUM",
                "HIGH"
        );

        priorityBox.setValue("MEDIUM");

        DatePicker dueDatePicker =
                new DatePicker();

        if (existingTask != null) {

            titleField.setText(
                    existingTask.getTitle()
            );

            descriptionField.setText(
                    existingTask.getDescription()
            );

            priorityBox.setValue(
                    existingTask.getPriority()
            );

            if (existingTask.getDueDate() != null
                    && !existingTask.getDueDate().isBlank()) {

                dueDatePicker.setValue(
                        LocalDate.parse(
                                existingTask.getDueDate()
                        )
                );
            }
        }

        GridPane grid = new GridPane();

        grid.setHgap(10);
        grid.setVgap(10);

        grid.setPadding(
                new Insets(20)
        );

        grid.add(
                new Label("Title:"),
                0,
                0
        );

        grid.add(
                titleField,
                1,
                0
        );

        grid.add(
                new Label("Description:"),
                0,
                1
        );

        grid.add(
                descriptionField,
                1,
                1
        );

        grid.add(
                new Label("Priority:"),
                0,
                2
        );

        grid.add(
                priorityBox,
                1,
                2
        );

        grid.add(
                new Label("Due Date:"),
                0,
                3
        );

        grid.add(
                dueDatePicker,
                1,
                3
        );

        dialog.getDialogPane()
                .setContent(grid);

        dialog.setResultConverter(button -> {

            if (button == saveButtonType) {

                String title =
                        titleField.getText().trim();

                if (title.isEmpty()) {

                    Alert alert = new Alert(
                            Alert.AlertType.WARNING
                    );

                    alert.setTitle("Validation");
                    alert.setHeaderText(
                            "Title is required"
                    );
                    alert.showAndWait();

                    return null;
                }

                String dueDate =
                        dueDatePicker.getValue() == null
                                ? ""
                                : dueDatePicker
                                .getValue()
                                .toString();

                if (existingTask == null) {

                    Task task = new Task(
                            title,
                            descriptionField.getText(),
                            priorityBox.getValue(),
                            dueDate
                    );

                    taskDAO.addTask(task);

                } else {

                    existingTask.setTitle(title);

                    existingTask.setDescription(
                            descriptionField.getText()
                    );

                    existingTask.setPriority(
                            priorityBox.getValue()
                    );

                    existingTask.setDueDate(
                            dueDate
                    );

                    taskDAO.updateTask(
                            existingTask
                    );
                }

                loadTasks();
            }

            return null;
        });

        dialog.showAndWait();
    }

    private void loadTasks() {

        tasks.setAll(
                taskDAO.getAllTasks()
        );

        refreshList();
    }

    private void refreshList() {

        String search =
                searchField.getText()
                        .trim()
                        .toLowerCase();

        String filter =
                filterBox.getValue();

        var filtered = tasks.stream()
                .filter(task -> {

                    boolean matchesSearch =
                            search.isEmpty()
                                    || task.getTitle()
                                    .toLowerCase()
                                    .contains(search)
                                    || task.getDescription()
                                    .toLowerCase()
                                    .contains(search);

                    boolean matchesFilter =
                            switch (filter) {

                                case "Pending" ->
                                        !task.isCompleted();

                                case "Completed" ->
                                        task.isCompleted();

                                default ->
                                        true;
                            };

                    return matchesSearch
                            && matchesFilter;
                })
                .toList();

        taskList.setItems(
                FXCollections.observableArrayList(
                        filtered
                )
        );
    }

    private String formatDueDate(String date) {

        if (date == null || date.isBlank()) {
            return "No due date";
        }

        return date;
    }
}