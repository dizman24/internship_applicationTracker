import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ApplicationTrackerGUI {

    private static final String DATABASE_URL =
        "jdbc:sqlite:applications.db";

    public static void main(String[] args) {
        // Prepare the database before opening the interface.
        try {
            initializeDatabase();
        } catch (SQLException exception) {
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(
                    null,
                    "Could not prepare database: "
                        + exception.getMessage(),
                    "Database error",
                    JOptionPane.ERROR_MESSAGE
                )
            );
            return;
        }

        SwingUtilities.invokeLater(() -> {
            JFrame window =
                new JFrame("Internship Application Tracker");

            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setSize(1000, 600);
            window.setLocationRelativeTo(null);

            // Main panel
            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.setBorder(
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
            );

            // Input fields
            JTextField companyField = new JTextField();
            JTextField positionField = new JTextField();
            JTextField requirementsField = new JTextField();
            JTextField deadlineField = new JTextField();

            JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));

            form.add(new JLabel("Company:"));
            form.add(companyField);

            form.add(new JLabel("Position:"));
            form.add(positionField);

            form.add(new JLabel("Requirements:"));
            form.add(requirementsField);

            form.add(new JLabel("Deadline (YYYY-MM-DD):"));
            form.add(deadlineField);

            // Buttons
            JButton addButton = new JButton("Add application");
            JButton refreshButton = new JButton("Refresh");
            JButton deleteButton = new JButton("Delete selected");

            JPanel buttons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
            );
            buttons.add(refreshButton);
            buttons.add(addButton);
            buttons.add(deleteButton);

            JPanel topPanel = new JPanel(new BorderLayout(10, 10));
            topPanel.add(form, BorderLayout.CENTER);
            topPanel.add(buttons, BorderLayout.SOUTH);

            // Table
            DefaultTableModel tableModel = new DefaultTableModel(
                new String[]{
                    "Application",
                    "Company",
                    "Position",
                    "Requirements",
                    "Deadline"
                },
                0
            ) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            JTable table = new JTable(tableModel);
            table.setRowHeight(25);
            table.setFillsViewportHeight(true);

            mainPanel.add(topPanel, BorderLayout.NORTH);
            mainPanel.add(
                new JScrollPane(table),
                BorderLayout.CENTER
            );

            window.setContentPane(mainPanel);

            // Enter moves focus to the next field.
            companyField.addActionListener(event ->
                positionField.requestFocusInWindow()
            );

            positionField.addActionListener(event ->
                requirementsField.requestFocusInWindow()
            );

            requirementsField.addActionListener(event ->
                deadlineField.requestFocusInWindow()
            );

            deadlineField.addActionListener(event ->
                addButton.requestFocusInWindow()
            );

            // Save a new application.
            addButton.addActionListener(event -> {
                String company = companyField.getText().trim();
                String position = positionField.getText().trim();
                String requirements =
                    requirementsField.getText().trim();
                String dateInput = deadlineField.getText().trim();

                if (company.isEmpty()) {
                    JOptionPane.showMessageDialog(
                        window,
                        "Please enter a company."
                    );
                    companyField.requestFocusInWindow();
                    return;
                }

                if (position.isEmpty()) {
                    JOptionPane.showMessageDialog(
                        window,
                        "Please enter a position."
                    );
                    positionField.requestFocusInWindow();
                    return;
                }

                LocalDate deadline = null;

                if (!dateInput.isEmpty()) {
                    try {
                        deadline = LocalDate.parse(dateInput);
                    } catch (DateTimeParseException exception) {
                        JOptionPane.showMessageDialog(
                            window,
                            "Please enter a valid date in YYYY-MM-DD format, "
                                + "or leave it blank."
                        );
                        deadlineField.requestFocusInWindow();
                        return;
                    }
                }

                try {
                    saveApplication(
                        company,
                        position,
                        requirements,
                        deadline
                    );
                } catch (SQLException exception) {
                    JOptionPane.showMessageDialog(
                        window,
                        "Could not save application: "
                            + exception.getMessage(),
                        "Database error",
                        JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                companyField.setText("");
                positionField.setText("");
                requirementsField.setText("");
                deadlineField.setText("");

                JOptionPane.showMessageDialog(
                    window,
                    "Application saved!"
                );

                loadApplications(tableModel, window);
                companyField.requestFocusInWindow();
            });

            // Reload saved applications.
            refreshButton.addActionListener(event ->
                loadApplications(tableModel, window)
            );

            loadApplications(tableModel, window);

            // Panel backgrounds and labels
            AppStyle.stylePanel(mainPanel);
            AppStyle.stylePanel(topPanel);
            AppStyle.stylePanel(form);
            AppStyle.stylePanel(buttons);

            // Input fields
            AppStyle.styleField(companyField);
            AppStyle.styleField(positionField);
            AppStyle.styleField(requirementsField);
            AppStyle.styleField(deadlineField);

            // Rounded buttons
            AppStyle.stylePrimaryButton(addButton);
            AppStyle.styleSecondaryButton(refreshButton);
            AppStyle.styleSecondaryButton(deleteButton);

            // Table
            AppStyle.styleTable(table);

            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            deleteButton.addActionListener(event -> {
                int selectedRow = table.getSelectedRow();

                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(
                        window,
                        "Please select an application first."
                    );
                    return;
                }

                int modelRow = table.convertRowIndexToModel(selectedRow);
                int applicationId =
                    ((Number) tableModel.getValueAt(modelRow, 0)).intValue();

                String sql = "DELETE FROM applications WHERE id = ?";

                try (
                    Connection connection =
                        DriverManager.getConnection(DATABASE_URL);
                    PreparedStatement statement =
                        connection.prepareStatement(sql)
                ) {
                    statement.setInt(1, applicationId);
                    statement.executeUpdate();

                } catch (SQLException exception) {
                    JOptionPane.showMessageDialog(
                        window,
                        "Could not delete application: " + exception.getMessage()
                    );
                    return;
                }

                loadApplications(tableModel, window);
            });

            window.setVisible(true);
            companyField.requestFocusInWindow();
        });
    }

    private static void initializeDatabase() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS applications (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                company TEXT NOT NULL,
                position TEXT NOT NULL,
                requirements TEXT,
                deadline TEXT NOT NULL
            )
            """;

        try (
            Connection connection =
                DriverManager.getConnection(DATABASE_URL);
            Statement statement = connection.createStatement()
        ) {
            statement.executeUpdate(sql);

            // Support older databases without a requirements column.
            boolean hasRequirements = false;

            try (
                ResultSet columns =
                    statement.executeQuery(
                        "PRAGMA table_info(applications)"
                    )
            ) {
                while (columns.next()) {
                    if ("requirements".equalsIgnoreCase(
                            columns.getString("name"))) {
                        hasRequirements = true;
                        break;
                    }
                }
            }

            if (!hasRequirements) {
                statement.executeUpdate(
                    "ALTER TABLE applications "
                        + "ADD COLUMN requirements TEXT"
                );
            }
        }
    }

    private static void saveApplication(
        String company,
        String position,
        String requirements,
        LocalDate deadline
    ) throws SQLException {

        String sql = """
            INSERT INTO applications
                (company, position, requirements, deadline)
            VALUES (?, ?, ?, ?)
            """;

        try (
            Connection connection =
                DriverManager.getConnection(DATABASE_URL);
            PreparedStatement statement =
                connection.prepareStatement(sql)
        ) {
            statement.setString(1, company);
            statement.setString(2, position);
            statement.setString(3, requirements);
            statement.setString(4, deadline == null ? "" : deadline.toString());

            statement.executeUpdate();
        }
    }

    private static void loadApplications(
        DefaultTableModel tableModel,
        JFrame window
    ) {
        String sql = """
            SELECT id, company, position, requirements, deadline
            FROM applications
            ORDER BY id
            """;

        try (
            Connection connection =
                DriverManager.getConnection(DATABASE_URL);
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(sql)
        ) {
            // Clear displayed rows, not database records.
            tableModel.setRowCount(0);

            while (results.next()) {
                String requirements =
                    results.getString("requirements");

                if (requirements == null || requirements.isBlank()) {
                    requirements = "Not provided";
                }

                String savedDeadline = results.getString("deadline");
                if (savedDeadline == null || savedDeadline.isBlank()) {
                    savedDeadline = "Not provided";
                }

                tableModel.addRow(new Object[]{
                    results.getInt("id"),
                    results.getString("company"),
                    results.getString("position"),
                    requirements,
                    savedDeadline
                });
            }

        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(
                window,
                "Could not load applications: "
                    + exception.getMessage(),
                "Database error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}