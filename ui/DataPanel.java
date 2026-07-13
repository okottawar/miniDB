package ui;

import model.Column;
import model.Row;
import model.TableSchema;
import service.DbService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Center panel: renders the currently selected table's rows in a JTable.
 */
public class DataPanel extends JPanel {

    private final DbService dbService;
    private final DefaultTableModel tableModel;
    private final JTable jTable;
    private final JLabel titleLabel;

    private String currentTable;

    public DataPanel(DbService dbService) {
        this.dbService = dbService;
        this.tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only grid; edits happen via CommandPanel
            }
        };
        this.jTable = new JTable(tableModel);
        this.titleLabel = new JLabel("No table selected");

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Data"));

        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        add(titleLabel, BorderLayout.NORTH);
        add(new JScrollPane(jTable), BorderLayout.CENTER);
    }

    public String getCurrentTable() {
        return currentTable;
    }

    public JTable getJTable() {
        return jTable;
    }

    /**
     * Switches the panel to display the given table and reloads its data.
     */
    public void showTable(String tableName) {
        this.currentTable = tableName;
        refresh();
    }

    /**
     * Reloads rows for the currently displayed table.
     */
    public void refresh() {
        if (currentTable == null) {
            titleLabel.setText("No table selected");
            tableModel.setDataVector(new Object[0][0], new Object[0]);
            return;
        }

        TableSchema schema = dbService.getSchema(currentTable);
        List<Column> columns = schema.getColumns();
        titleLabel.setText(currentTable + "  (" + dbService.rowCount(currentTable) + " rows)");

        Object[] columnNames = new Object[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            String label = columns.get(i).getName();
            if (columns.get(i).isPrimaryKey()) {
                label += " [PK]";
            }
            columnNames[i] = label;
        }

        List<Row> rows = dbService.selectAll(currentTable);
        Object[][] data = new Object[rows.size()][columns.size()];
        for (int r = 0; r < rows.size(); r++) {
            Row row = rows.get(r);
            for (int c = 0; c < columns.size(); c++) {
                Object val = row.getRawValue(columns.get(c).getName());
                data[r][c] = val == null ? "NULL" : val;
            }
        }

        tableModel.setDataVector(data, columnNames);
    }

    public int getSelectedRowIndex() {
        return jTable.getSelectedRow();
    }
}
