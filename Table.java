package core;

import model.Column;
import model.DataValue;
import model.Row;
import model.TableSchema;
import utils.Validators;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Represents a single in-memory table: its schema plus the rows of data it holds.
 */
public class Table {

    private final TableSchema schema;
    private final List<Row> rows;

    public Table(TableSchema schema) {
        this.schema = schema;
        this.rows = new ArrayList<>();
    }

    public TableSchema getSchema() {
        return schema;
    }

    public String getName() {
        return schema.getTableName();
    }

    /**
     * Inserts a new row given a map of column name -> raw value.
     * Validates types, NOT NULL, and PRIMARY KEY uniqueness before committing.
     */
    public Row insert(Map<String, Object> rawValues) {
        Validators.validateRowAgainstSchema(schema, rawValues);

        schema.getPrimaryKeyColumn().ifPresent(pk -> {
            Object candidate = rawValues.get(pk.getName());
            Validators.validatePrimaryKeyUniqueness(schema, rows, candidate);
        });

        Row row = new Row();
        for (Column column : schema.getColumns()) {
            Object raw = rawValues.get(column.getName());
            row.setValue(column.getName(), new DataValue(column.getDataType(), raw));
        }
        rows.add(row);
        return row;
    }

    /**
     * Returns all rows currently in the table (a defensive copy of the list).
     */
    public List<Row> selectAll() {
        return new ArrayList<>(rows);
    }

    /**
     * Returns rows matching the given predicate.
     */
    public List<Row> select(Predicate<Row> filter) {
        List<Row> result = new ArrayList<>();
        for (Row row : rows) {
            if (filter == null || filter.test(row)) {
                result.add(row);
            }
        }
        return result;
    }

    /**
     * Updates matching rows by applying newValues (column name -> new raw value).
     * Returns the number of rows updated.
     */
    public int update(Predicate<Row> filter, Map<String, Object> newValues) {
        // Validate that all target columns exist
        for (String key : newValues.keySet()) {
            if (!schema.hasColumn(key)) {
                throw new IllegalArgumentException(
                        "Unknown column '" + key + "' for table '" + schema.getTableName() + "'");
            }
        }

        int updatedCount = 0;
        for (Row row : rows) {
            if (filter == null || filter.test(row)) {
                // Build the prospective full value set to validate constraints
                Map<String, Object> prospective = new LinkedHashMap<>();
                for (Column column : schema.getColumns()) {
                    if (newValues.containsKey(column.getName())) {
                        prospective.put(column.getName(), newValues.get(column.getName()));
                    } else {
                        prospective.put(column.getName(), row.getRawValue(column.getName()));
                    }
                }
                Validators.validateRowAgainstSchema(schema, prospective);

                schema.getPrimaryKeyColumn().ifPresent(pk -> {
                    if (newValues.containsKey(pk.getName())) {
                        List<Row> others = new ArrayList<>(rows);
                        others.remove(row);
                        Validators.validatePrimaryKeyUniqueness(schema, others, newValues.get(pk.getName()));
                    }
                });

                for (Map.Entry<String, Object> entry : newValues.entrySet()) {
                    Column column = schema.getColumn(entry.getKey());
                    row.setValue(column.getName(), new DataValue(column.getDataType(), entry.getValue()));
                }
                updatedCount++;
            }
        }
        return updatedCount;
    }

    /**
     * Deletes rows matching the given predicate. Returns the number of rows deleted.
     */
    public int delete(Predicate<Row> filter) {
        int before = rows.size();
        if (filter == null) {
            rows.clear();
        } else {
            rows.removeIf(filter);
        }
        return before - rows.size();
    }

    public int rowCount() {
        return rows.size();
    }
}
