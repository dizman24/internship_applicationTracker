import java.util.Scanner;
import java.time.LocalDate;
// import java.util.ArrayList;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class ApplicationTracker {
    public static void main(String[] args){
        
        String databaseUrl = "jdbc:sqlite:applications.db";

        try (
            Connection connection = DriverManager.getConnection(databaseUrl);
            Statement statement = connection.createStatement()
            ) {
            String sql = """
                CREATE TABLE IF NOT EXISTS applications (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    company TEXT NOT NULL,
                    position TEXT NOT NULL,
                    requirements TEXT,
                    deadline TEXT NOT NULL

                )
                """;

            statement.executeUpdate(sql);

            System.out.println("Database is ready!");

            boolean hasRequirements = false;

            try (ResultSet columns =
                    statement.executeQuery("PRAGMA table_info(applications)")) {

                while (columns.next()) {
                    if ("requirements".equalsIgnoreCase(columns.getString("name"))) {
                        hasRequirements = true;
                    }
                }
            }

            if (!hasRequirements) {
                statement.executeUpdate(
                    "ALTER TABLE applications ADD COLUMN requirements TEXT"
                );
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return;
        }

        Scanner scnr = new Scanner(System.in);

        System.out.println("\nWelcome!");
        System.out.println("\n1 : Add an application");
        System.out.println("2 : Display saved applications");
        System.out.println("3 : Delete an application");
        System.out.print("\nChoose an option: ");

        String choice = scnr.nextLine();

        if(choice.equals("1")){
            System.out.println("Enter company name: ");
            String company = scnr.nextLine();

            System.out.println("Enter position: ");
            String position = scnr.nextLine();

            System.out.println("Enter requirements: ");
            String requirements = scnr.nextLine();

            System.out.println("Enter application deadline (YYYY-MM-DD): ");
            String dateInput = scnr.nextLine();
            LocalDate deadline = LocalDate.parse(dateInput);

            String insertSql = """
                INSERT INTO applications (company, position, requirements, deadline)
                VALUES (?, ?, ?,?)
                """;

            try (
                Connection connection = DriverManager.getConnection(databaseUrl);
                PreparedStatement statement = connection.prepareStatement(insertSql)
            ) {
                statement.setString(1, company);
                statement.setString(2, position);
                statement.setString(3, requirements);
                statement.setString(4, deadline.toString());
                


                statement.executeUpdate();

                System.out.println("Application saved to the database!");

            } catch (SQLException e) {
                System.out.println("Could not save application: " + e.getMessage());
            }
        }else if(choice.equals("2")){
            String selectSql = """
            SELECT id, company, position, deadline, requirements
            FROM applications
            ORDER BY id
            """;

            try (
                Connection connection = DriverManager.getConnection(databaseUrl);
                Statement statement = connection.createStatement();
                ResultSet results = statement.executeQuery(selectSql)
            ) {
                System.out.println("\n--- Saved Applications ---");

                boolean found = false;

                while (results.next()) {
                    found = true;

                    System.out.println("\nApplication " + results.getInt("id"));
                    System.out.println("Company: " + results.getString("company"));
                    System.out.println("Position: " + results.getString("position"));
                    String savedRequirements = results.getString("requirements");

                    System.out.println(
                        "Requirements: " +
                        (savedRequirements == null ? "Not provided" : savedRequirements)
                    );
                    System.out.println("Deadline: " + results.getString("deadline"));
                }

                if (!found) {
                    System.out.println("No saved applications.");
                }

            } catch (SQLException e) {
                System.out.println("Could not load applications: " + e.getMessage());
            }
        }else if (choice.equals("3")){
            System.out.print("Enter the application number to delete: ");
    String deleteInput = scnr.nextLine();

    try {
        int applicationId = Integer.parseInt(deleteInput.trim());

        if (applicationId <= 0) {
            System.out.println("Please enter a positive application number.");
        } else {
            String findSql = """
                SELECT company, position
                FROM applications
                WHERE id = ?
                """;

            try (
                Connection connection =
                    DriverManager.getConnection(databaseUrl);
                PreparedStatement findStatement =
                    connection.prepareStatement(findSql)
            ) {
                findStatement.setInt(1, applicationId);

                boolean found = false;
                String savedCompany = "";
                String savedPosition = "";

                try (ResultSet result = findStatement.executeQuery()) {
                    if (result.next()) {
                        found = true;
                        savedCompany = result.getString("company");
                        savedPosition = result.getString("position");
                    }
                }

                if (!found) {
                    System.out.println("Application not found.");
                } else {
                    System.out.println(
                        "Application " + applicationId + ": " +
                        savedCompany + " - " + savedPosition
                    );

                    System.out.print("Permanently delete it? (yes/no): ");
                    String confirmation = scnr.nextLine();

                    if (confirmation.trim().equalsIgnoreCase("yes")) {
                        String deleteSql =
                            "DELETE FROM applications WHERE id = ?";

                        try (PreparedStatement deleteStatement =
                                connection.prepareStatement(deleteSql)) {

                            deleteStatement.setInt(1, applicationId);
                            int deletedRows = deleteStatement.executeUpdate();

                            if (deletedRows > 0) {
                                System.out.println("Application deleted.");
                            } else {
                                System.out.println("Application not found.");
                            }
                        }
                    } else {
                        System.out.println("Deletion canceled.");
                    }
                }

                    } catch (SQLException e) {
                        System.out.println(
                            "Could not delete application: " + e.getMessage()
                        );
                    }
                }

            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }

        }else{
            System.out.println("Invalid option.");
        }
                
        System.out.println();
        scnr.close();

    }
}