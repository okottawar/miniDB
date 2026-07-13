package model;

import enums.Constraint;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Defines the structure (columns) of a table.
 * Preserves column declaration order.
 */
public class TableSchema {

    private final String tableName;
    private final Map<String, Column> columns;

    public TableSchema(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }
        this.tableName = tableName;
        this.columns = new LinkedHashMap<>();
    }

    public String getTableName() {
        return tableName;
    }

    public TableSchema addColumn(Column column) {
        if (columns.containsKey(column.getName())) {
            throw new IllegalArgumentException("Duplicate column name: " + column.getName());
        }
        if (column.isPrimaryKey() && getPrimaryKeyColumn().isPresent()) {
            throw new IllegalArgumentException("Table already has a primary key: "
                    + getPrimaryKeyColumn().get().getName());
        }
        columns.put(column.getName(), column);
        return this;
    }

    public List<Column> getColumns() {
        return new ArrayList<>(columns.values());
    }

    public Column getColumn(String name) {
        return columns.get(name);
    }

    public boolean hasColumn(String name) {
        return columns.containsKey(name);
    }

    public Optional<Column> getPrimaryKeyColumn() {
        return columns.values().stream()
                .filter(Column::isPrimaryKey)
                .findFirst();
    }

    public List<String> getColumnNames() {
        return new ArrayList<>(columns.keySet());
    }

    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public String toString() {
        return tableName + columns.values();
    }
}
