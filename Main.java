import core.Database;
import enums.Constraint;
import enums.DataType;
import service.DbService;
import ui.MainFrame;

import javax.swing.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Entry point for MiniDB.
 * Seeds a couple of sample tables with data, then launches the Swing UI.
 */
public class Main {

    public static void main(String[] args) {
        DbService dbService = new DbService();
        seedSampleData(dbService);

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(dbService);
            frame.setVisible(true);
        });
    }

    private static void seedSampleData(DbService dbService) {
        Database db = dbService.getDatabase();

        db.createTable("users")
                .addColumn("id", DataType.INT, Constraint.PRIMARY_KEY, Constraint.NOT_NULL)
                .addColumn("name", DataType.STRING, Constraint.NOT_NULL)
                .addColumn("active", DataType.BOOLEAN)
                .build();

        dbService.insertRow("users", 1, "Alice", true);
        dbService.insertRow("users", 2, "Bob", true);
        dbService.insertRow("users", 3, "Charlie", false);

        db.createTable("products")
                .addColumn("sku", DataType.STRING, Constraint.PRIMARY_KEY)
                .addColumn("title", DataType.STRING, Constraint.NOT_NULL)
                .addColumn("price", DataType.DOUBLE)
                .build();

        dbService.insertRow("products", "SKU-001", "Widget", 9.99);
        dbService.insertRow("products", "SKU-002", "Gadget", 19.99);

        // --- Simple console demo of the engine, mirroring README usage ---
        System.out.println("Tables: " + dbService.listTables());
        System.out.println("Users: " + dbService.selectAll("users"));
        System.out.println("Products: " + dbService.selectAll("products"));

        Map<String, Object> update = new LinkedHashMap<>();
        update.put("active", false);
        dbService.updateByPrimaryKey("users", 2, update);
        System.out.println("After update: " + dbService.selectAll("users"));
    }
}
