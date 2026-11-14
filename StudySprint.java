
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import java.util.Base64;

public class StudySprint extends JFrame {

    private final List<StudyTask> tasks = new ArrayList<>();

    private final String FILE_NAME = "studysprint-data.txt";

    private DefaultTableModel tableModel;
    private JTable taskTable;

    private JLabel totalLabel;
    private JLabel pendingLabel;
    private JLabel completedLabel;

    // ================= CONSTRUCTOR =================

    public StudySprint() {

        setTitle("StudySprint - Personal Study Planner");
        setSize(950, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        loadTasks();

        createHeader();
        createDashboard();
        createTaskTable();
        createButtons();

        refreshTable();
    }

    // ================= HEADER =================

    private void createHeader() {

        JPanel header = new JPanel(new BorderLayout());

        header.setBackground(new Color(35, 47, 75));
        header.setBorder(
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        );

        JLabel title = new JLabel("StudySprint");

        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel(
            "Your Personal Study Planner"
        );

        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(Color.WHITE);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);

        textPanel.add(title);
        textPanel.add(subtitle);

        header.add(textPanel, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);
    }

    // ================= DASHBOARD =================

    private void createDashboard() {

        JPanel dashboard = new JPanel(
            new GridLayout(1, 3, 15, 0)
        );

        dashboard.setBorder(
            BorderFactory.createEmptyBorder(15, 15, 10, 15)
        );

        totalLabel = createCard(dashboard, "Total Tasks");

        pendingLabel = createCard(dashboard, "Pending Tasks");

        completedLabel = createCard(
            dashboard, "Completed Tasks"
        );

        add(dashboard, BorderLayout.BEFORE_FIRST_LINE);
    }

    private JLabel createCard(JPanel dashboard, String heading) {

        JPanel card = new JPanel(new GridLayout(2, 1));

        card.setBackground(new Color(235, 241, 250));

        card.setBorder(
            BorderFactory.createLineBorder(
                new Color(210, 220, 235)
            )
        );

        JLabel title = new JLabel(
            heading, SwingConstants.CENTER
        );

        title.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel value = new JLabel("0", SwingConstants.CENTER);

        value.setFont(new Font("Arial", Font.BOLD, 24));
        value.setForeground(new Color(35, 70, 130));

        card.add(title);
        card.add(value);

        dashboard.add(card);

        return value;
    }

    // ================= TASK TABLE =================

    private void createTaskTable() {

        String[] columns = {
            "Subject",
            "Task Name",
            "Priority",
            "Due Date",
            "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        taskTable = new JTable(tableModel);

        taskTable.setRowHeight(30);

        taskTable.setFont(
            new Font("Arial", Font.PLAIN, 13)
        );

        taskTable.getTableHeader().setFont(
            new Font("Arial", Font.BOLD, 13)
        );

        taskTable.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        );

        // Highlight overdue and completed tasks

        taskTable.setDefaultRenderer(
            Object.class,
            new DefaultTableCellRenderer() {

                @Override
                public Component getTableCellRendererComponent(
                    JTable table,
                    Object value,
                    boolean selected,
                    boolean focused,
                    int row,
                    int column
                ) {

                    Component component =
                        super.getTableCellRendererComponent(
                            table, value, selected,
                            focused, row, column
                        );

                    if (!selected) {

                        int modelRow =
                            table.convertRowIndexToModel(row);

                        StudyTask task =
                            getSortedTasks().get(modelRow);

                        if (task.completed) {

                            component.setBackground(
                                new Color(220, 245, 225)
                            );

                        } else if (task.isOverdue()) {

                            component.setBackground(
                                new Color(255, 220, 220)
                            );

                        } else {

                            component.setBackground(Color.WHITE);
                        }
                    }

                    return component;
                }
            }
        );

        JScrollPane scrollPane = new JScrollPane(taskTable);

        JPanel tablePanel = new JPanel(new BorderLayout());

        tablePanel.setBorder(
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        );

        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(tablePanel, BorderLayout.CENTER);
    }

    // ================= BUTTONS =================

    private void createButtons() {

        JPanel buttonPanel = new JPanel(
            new FlowLayout(FlowLayout.CENTER, 15, 12)
        );

        JButton addButton = createButton(
            "Add Task", new Color(40, 140, 90)
        );

        JButton completeButton = createButton(
            "Mark Completed", new Color(45, 110, 190)
        );

        JButton deleteButton = createButton(
            "Delete Task", new Color(190, 65, 65)
        );

        JButton refreshButton = createButton(
            "Refresh", new Color(100, 100, 110)
        );

        addButton.addActionListener(e -> addTask());

        completeButton.addActionListener(
            e -> markCompleted()
        );

        deleteButton.addActionListener(
            e -> deleteTask()
        );

        refreshButton.addActionListener(
            e -> refreshTable()
        );

        buttonPanel.add(addButton);
        buttonPanel.add(completeButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    // UPDATED BUTTON DESIGN

    private JButton createButton(String text, Color color) {

        JButton button = new JButton(text);

        // Apply custom button UI
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());

        // Set background and text colors
        button.setBackground(color);
        button.setForeground(Color.WHITE);

        // Force background visibility
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);

        // Set font
        button.setFont(
            new Font("Arial", Font.BOLD, 13)
        );

        // Set button spacing
        button.setBorder(
            BorderFactory.createEmptyBorder(12, 20, 12, 20)
        );

        return button;
    }

    // ================= ADD TASK =================

    private void addTask() {

        JTextField subjectField = new JTextField();
        JTextField taskField = new JTextField();

        String[] priorities = {
            "High", "Medium", "Low"
        };

        JComboBox<String> priorityBox =
            new JComboBox<>(priorities);

        JTextField dateField = new JTextField(
            LocalDate.now().toString()
        );

        JPanel panel = new JPanel(
            new GridLayout(4, 2, 10, 10)
        );

        panel.add(new JLabel("Subject:"));
        panel.add(subjectField);

        panel.add(new JLabel("Task Name:"));
        panel.add(taskField);

        panel.add(new JLabel("Priority:"));
        panel.add(priorityBox);

        panel.add(new JLabel("Due Date (YYYY-MM-DD):"));
        panel.add(dateField);

        int result = JOptionPane.showConfirmDialog(
            this,
            panel,
            "Add New Study Task",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String subject = subjectField.getText().trim();

        String taskName = taskField.getText().trim();

        String priority =
            (String) priorityBox.getSelectedItem();

        if (subject.isEmpty() || taskName.isEmpty()) {

            JOptionPane.showMessageDialog(
                this,
                "Please enter both subject and task name."
            );

            return;
        }

        try {

            LocalDate dueDate = LocalDate.parse(
                dateField.getText().trim()
            );

            StudyTask task = new StudyTask(
                subject,
                taskName,
                priority,
                dueDate
            );

            tasks.add(task);

            saveTasks();
            refreshTable();

            JOptionPane.showMessageDialog(
                this,
                "Task added successfully!"
            );

        } catch (DateTimeParseException e) {

            JOptionPane.showMessageDialog(
                this,
                "Invalid date! Use YYYY-MM-DD format.",
                "Date Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ================= COMPLETE TASK =================

    private void markCompleted() {

        int row = taskTable.getSelectedRow();

        if (row == -1) {
            showSelectionMessage();
            return;
        }

        int modelRow =
            taskTable.convertRowIndexToModel(row);

        List<StudyTask> sorted = getSortedTasks();

        StudyTask task = sorted.get(modelRow);

        if (task.completed) {

            JOptionPane.showMessageDialog(
                this,
                "This task is already completed."
            );

            return;
        }

        task.completed = true;

        saveTasks();
        refreshTable();
    }

    // ================= DELETE TASK =================

    private void deleteTask() {

        int row = taskTable.getSelectedRow();

        if (row == -1) {
            showSelectionMessage();
            return;
        }

        int modelRow =
            taskTable.convertRowIndexToModel(row);

        StudyTask task =
            getSortedTasks().get(modelRow);

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Delete task: " + task.taskName + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {

            tasks.remove(task);

            saveTasks();
            refreshTable();
        }
    }

    private void showSelectionMessage() {

        JOptionPane.showMessageDialog(
            this,
            "Please select a task first.",
            "No Task Selected",
            JOptionPane.WARNING_MESSAGE
        );
    }

    // ================= SORT TASKS =================

    private List<StudyTask> getSortedTasks() {

        List<StudyTask> sorted = new ArrayList<>(tasks);

        sorted.sort(
            Comparator.comparing((StudyTask t) -> t.completed)
                .thenComparingInt(
                    t -> getPriorityValue(t.priority)
                )
                .thenComparing(t -> t.dueDate)
        );

        return sorted;
    }

    private int getPriorityValue(String priority) {

        switch (priority) {

            case "High":
                return 1;

            case "Medium":
                return 2;

            case "Low":
                return 3;

            default:
                return 4;
        }
    }

    // ================= REFRESH TABLE =================

    private void refreshTable() {

        tableModel.setRowCount(0);

        List<StudyTask> sorted = getSortedTasks();

        for (StudyTask task : sorted) {

            String status;

            if (task.completed) {
                status = "Completed";
            } else if (task.isOverdue()) {
                status = "Overdue";
            } else {
                status = "Pending";
            }

            tableModel.addRow(new Object[] {

                task.subject,
                task.taskName,
                task.priority,
                task.dueDate,
                status
            });
        }

        totalLabel.setText(
            String.valueOf(tasks.size())
        );

        pendingLabel.setText(
            String.valueOf(getPendingCount())
        );

        completedLabel.setText(
            String.valueOf(getCompletedCount())
        );

        taskTable.repaint();
    }

    private int getCompletedCount() {

        int count = 0;

        for (StudyTask task : tasks) {

            if (task.completed) {
                count++;
            }
        }

        return count;
    }

    private int getPendingCount() {

        return tasks.size() - getCompletedCount();
    }

    // ================= SAVE TASKS =================

    private String encode(String value) {

        return Base64.getEncoder().encodeToString(
            value.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String decode(String value) {

        return new String(
            Base64.getDecoder().decode(value),
            StandardCharsets.UTF_8
        );
    }

    private void saveTasks() {

        try (
            BufferedWriter writer = new BufferedWriter(
                new FileWriter(FILE_NAME, StandardCharsets.UTF_8)
            )
        ) {

            for (StudyTask task : tasks) {

                String line =
                    encode(task.subject) + "|" +
                    encode(task.taskName) + "|" +
                    encode(task.priority) + "|" +
                    task.dueDate + "|" +
                    task.completed;

                writer.write(line);
                writer.newLine();
            }

        } catch (IOException e) {

            JOptionPane.showMessageDialog(
                this,
                "Unable to save tasks: " + e.getMessage(),
                "File Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ================= LOAD TASKS =================

    private void loadTasks() {

        File file = new File(FILE_NAME);

        if (!file.exists()) {
            return;
        }

        try (
            BufferedReader reader = new BufferedReader(
                new FileReader(file, StandardCharsets.UTF_8)
            )
        ) {

            String line;

            while ((line = reader.readLine()) != null) {

                try {

                    String[] parts = line.split("\\|", -1);

                    if (parts.length != 5) {
                        continue;
                    }

                    StudyTask task = new StudyTask(
                        decode(parts[0]),
                        decode(parts[1]),
                        decode(parts[2]),
                        LocalDate.parse(parts[3])
                    );

                    task.completed =
                        Boolean.parseBoolean(parts[4]);

                    tasks.add(task);

                } catch (RuntimeException e) {

                    System.out.println(
                        "Skipping invalid task record."
                    );
                }
            }

        } catch (IOException e) {

            System.out.println(
                "Unable to load saved tasks: " + e.getMessage()
            );
        }
    }

    // ================= MAIN METHOD =================

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            try {

                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
                );

            } catch (Exception e) {

                System.out.println(
                    "Using default application theme."
                );
            }

            StudySprint application = new StudySprint();

            application.setVisible(true);
        });
    }
}

// ================= STUDY TASK CLASS =================

class StudyTask {

    String subject;
    String taskName;
    String priority;
    LocalDate dueDate;
    boolean completed;

    public StudyTask(
        String subject,
        String taskName,
        String priority,
        LocalDate dueDate
    ) {

        this.subject = subject;
        this.taskName = taskName;
        this.priority = priority;
        this.dueDate = dueDate;
        this.completed = false;
    }

    public boolean isOverdue() {

        return !completed &&
               dueDate.isBefore(LocalDate.now());
    }
}
