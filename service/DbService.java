package service;

import core.Database;
import core.Table;
import model.Column;
import model.Row;
import model.TableSchema;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Bridges the UI layer and the core Database engine.
 * Provides simplified, UI-friendly methods (e.g. inserting rows using
 * ordered arrays of values instead of maps) and central error handling.
 */
public class DbService {

    private final Database database;

    public DbService() {
        this.database = new Database();
    }

    public DbService(Database database) {
        this.database = database;
    }

    public Database getDatabase() {
        return database;
    }

    public List<String> listTables() {
        return database.listTableNames();
    }

    public TableSchema getSchema(String tableName) {
        return database.getTable(tableName).getSchema();
    }

    public void dropTable(String tableName) {
        database.dropTable(tableName);
    }

    /**
     * Inserts a row using positional values matched against the schema's
     * column order. Handy for UI forms that collect values in column order.
     */
    public Row insertRow(String tableName, Object... orderedValues) {
        Table table = database.getTable(tableName);
        List<Column> columns = table.getSchema().getColumns();
        if (orderedValues.length != columns.size()) {
            throw new IllegalArgumentException(
                    "Expected " + columns.size() + " values for table '" + tableName
                            + "' but got " + orderedValues.length);
        }
        Map<String, Object> values = new LinkedHashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            values.put(columns.get(i).getName(), orderedValues[i]);
        }
        return database.insert(tableName, values);
    }

    public Row insertRow(String tableName, Map<String, Object> values) {
        return database.insert(tableName, values);
    }

    public List<Row> selectAll(String tableName) {
        return database.select(tableName);
    }

    public List<Row> select(String tableName, Predicate<Row> filter) {
        return database.select(tableName, filter);
    }

    public int update(String tableName, Predicate<Row> filter, Map<String, Object> newValues) {
        return database.update(tableName, filter, newValues);
    }

    /**
     * Convenience update-by-primary-key.
     */
    public int updateByPrimaryKey(String tableName, Object pkValue, Map<String, Object> newValues) {
        TableSchema schema = getSchema(tableName);
        String pkName = schema.getPrimaryKeyColumn()
                .orElseThrow(() -> new IllegalStateException(
                        "Table '" + tableName + "' has no primary key defined"))
                .getName();
        return database.update(tableName, row -> {
            Object rowVal = row.getRawValue(pkName);
            return rowVal != null && rowVal.equals(pkValue);
        }, newValues);
    }

    public int delete(String tableName, Predicate<Row> filter) {
        return database.delete(tableName, filter);
    }

    /**
     * Convenience delete-by-primary-key.
     */
    public int deleteByPrimaryKey(String tableName, Object pkValue) {
        TableSchema schema = getSchema(tableName);
        String pkName = schema.getPrimaryKeyColumn()
                .orElseThrow(() -> new IllegalStateException(
                        "Table '" + tableName + "' has no primary key defined"))
                .getName();
        return database.delete(tableName, row -> {
            Object rowVal = row.getRawValue(pkName);
            return rowVal != null && rowVal.equals(pkValue);
        });
    }

    public int rowCount(String tableName) {
        return database.getTable(tableName).rowCount();
    }
}
