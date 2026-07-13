package ui;

import enums.Constraint;
import enums.DataType;
import model.Column;
import model.Row;
import model.TableSchema;
import service.DbService;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bottom panel: interactive controls for creating tables and performing
 * INSERT / UPDATE / DELETE operations on the currently selected table.
 */
public class CommandPanel extends JPanel {

    private final DbService dbService;
    private final TablePanel tablePanel;
    private final DataPanel dataPanel;

    private final JButton createTableBtn = new JButton("Create Table...");
    private final JButton insertBtn = new JButton("Insert Row...");
    private final JButton updateBtn = new JButton("Update Selected...");
    private final JButton deleteBtn = new JButton("Delete Selected");
    private final JButton refreshBtn = new JButton("Refresh");

    public CommandPanel(DbService dbService, TablePanel tablePanel, DataPanel dataPanel) {
        this.dbService = dbService;
        this.tablePanel = tablePanel;
        this.dataPanel = dataPanel;

        setLayout(new FlowLayout(FlowLayout.LEFT));
        setBorder(BorderFactory.createTitledBorder("Commands"));

        add(createTableBtn);
        add(insertBtn);
        add(updateBtn);
        add(deleteBtn);
        add(refreshBtn);

        createTableBtn.addActionListener(e -> onCreateTable());
        insertBtn.addActionListener(e -> onInsertRow());
        updateBtn.addActionListener(e -> onUpdateRow());
        deleteBtn.addActionListener(e -> onDeleteRow());
        refreshBtn.addActionListener(e -> refreshAll());
    }

    private void refreshAll() {
        tablePanel.refresh();
        dataPanel.refresh();
    }

    private String currentTableOrWarn() {
        String table = dataPanel.getCurrentTable();
        if (table == null) {
            JOptionPane.showMessageDialog(this, "Select a table first.", "No table selected",
                    JOptionPane.WARNING_MESSAGE);
        }
        return table;
    }

    // --- Create Table --------------------------------------------------

    private void onCreateTable() {
        String tableName = JOptionPane.showInputDialog(this, "Table name:");
        if (tableName == null || tableName.trim().isEmpty()) return;

        int colCount;
        try {
            String countStr = JOptionPane.showInputDialog(this, "Number of columns:");
            if (countStr == null) return;
            colCount = Integer.parseInt(countStr.trim());
        } catch (NumberFormatException ex) {
            showError("Invalid column count.");
            return;
        }

        try {
            core.Database.TableBuilder builder = dbService.getDatabase().createTable(tableName.trim());
            for (int i = 0; i < colCount; i++) {
                JPanel panel = new JPanel(new GridLayout(0, 1));
                JTextField nameField = new JTextField();
                JComboBox<DataType> typeBox = new JComboBox<>(DataType.values());
                JCheckBox notNullBox = new JCheckBox("NOT NULL");
                JCheckBox pkBox = new JCheckBox("PRIMARY KEY");

                panel.add(new JLabel("Column " + (i + 1) + " name:"));
                panel.add(nameField);
                panel.add(new JLabel("Type:"));
                panel.add(typeBox);
                panel.add(notNullBox);
                panel.add(pkBox);

                int result = JOptionPane.showConfirmDialog(this, panel,
                        "Define column " + (i + 1) + "/" + colCount, JOptionPane.OK_CANCEL_OPTION);
                if (result != JOptionPane.OK_OPTION) return;

                String colName = nameField.getText().trim();
                if (colName.isEmpty()) {
                    showError("Column name cannot be empty.");
                    return;
                }
                DataType type = (DataType) typeBox.getSelectedItem();
                java.util.EnumSet<Constraint> constraints = java.util.EnumSet.noneOf(Constraint.class);
                if (notNullBox.isSelected()) constraints.add(Constraint.NOT_NULL);
                if (pkBox.isSelected()) constraints.add(Constraint.PRIMARY_KEY);

                builder.getSchema().addColumn(new Column(colName, type, constraints));
            }
            builder.build();
            refreshAll();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    // --- Insert ----------------------------------------------------------

    private void onInsertRow() {
        String table = currentTableOrWarn();
        if (table == null) return;

        TableSchema schema = dbService.getSchema(table);
        List<Column> columns = schema.getColumns();

        JPanel panel = new JPanel(new GridLayout(0, 2));
        Map<Column, JTextField> fields = new LinkedHashMap<>();
        for (Column column : columns) {
            panel.add(new JLabel(column.getName() + " (" + column.getDataType() + ")"
                    + (column.isPrimaryKey() ? " [PK]" : "")));
            JTextField field = new JTextField();
            fields.put(column, field);
            panel.add(field);
        }

        int result = JOptionPane.showConfirmDialog(this, panel, "Insert into " + table,
                JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            Map<String, Object> values = new LinkedHashMap<>();
            for (Map.Entry<Column, JTextField> entry : fields.entrySet()) {
                values.put(entry.getKey().getName(),
                        parseValue(entry.getKey().getDataType(), entry.getValue().getText()));
            }
            dbService.insertRow(table, values);
            dataPanel.refresh();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    // --- Update ------------------------------------------------------------

    private void onUpdateRow() {
        String table = currentTableOrWarn();
        if (table == null) return;

        int selectedRow = dataPanel.getSelectedRowIndex();
        if (selectedRow < 0) {
            showError("Select a row in the table to update.");
            return;
        }

        TableSchema schema = dbService.getSchema(table);
        List<Column> columns = schema.getColumns();
        Row row = dbService.selectAll(table).get(selectedRow);

        JPanel panel = new JPanel(new GridLayout(0, 2));
        Map<Column, JTextField> fields = new LinkedHashMap<>();
        for (Column column : columns) {
            panel.add(new JLabel(column.getName() + " (" + column.getDataType() + ")"
                    + (column.isPrimaryKey() ? " [PK]" : "")));
            Object existing = row.getRawValue(column.getName());
            JTextField field = new JTextField(existing == null ? "" : existing.toString());
            fields.put(column, field);
            panel.add(field);
        }

        int result = JOptionPane.showConfirmDialog(this, panel, "Update row in " + table,
                JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            Map<String, Object> newValues = new LinkedHashMap<>();
            for (Map.Entry<Column, JTextField> entry : fields.entrySet()) {
                newValues.put(entry.getKey().getName(),
                        parseValue(entry.getKey().getDataType(), entry.getValue().getText()));
            }
            // Identify the row by object identity via index re-lookup (simple approach:
            // match on primary key if present, otherwise on full previous value equality).
            List<Row> rows = dbService.selectAll(table);
            Row target = rows.get(selectedRow);
            dbService.update(table, r -> r == target, newValues);
            dataPanel.refresh();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    // --- Delete ------------------------------------------------------------

    private void onDeleteRow() {
        String table = currentTableOrWarn();
        if (table == null) return;

        int selectedRow = dataPanel.getSelectedRowIndex();
        if (selectedRow < 0) {
            showError("Select a row in the table to delete.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected row?", "Confirm delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            List<Row> rows = dbService.selectAll(table);
            Row target = rows.get(selectedRow);
            dbService.delete(table, r -> r == target);
            dataPanel.refresh();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    // --- Helpers -------------------------------------------------------------

    private Object parseValue(DataType type, String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        text = text.trim();
        switch (type) {
            case INT:
                return Integer.parseInt(text);
            case DOUBLE:
                return Double.parseDouble(text);
            case BOOLEAN:
                return Boolean.parseBoolean(text);
            case STRING:
            default:
                return text;
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
