package utils;

import model.Column;
import model.DataValue;
import model.Row;
import model.TableSchema;

/**
 * Static helper methods for validating data against a table schema
 * before it is written into the database.
 */
public final class Validators {

    private Validators() {
        // utility class, no instances
    }

    /**
     * Validates that a value is acceptable for the given column
     * (type match + NOT NULL / PRIMARY KEY constraints).
     * Throws IllegalArgumentException with a descriptive message on failure.
     */
    public static void validateValue(Column column, Object rawValue) {
        if (rawValue == null) {
            if (column.isNotNull()) {
                throw new IllegalArgumentException(
                        "Column '" + column.getName() + "' cannot be NULL");
            }
            return;
        }
        if (!column.getDataType().isValidValue(rawValue)) {
            throw new IllegalArgumentException(
                    "Value '" + rawValue + "' is not valid for column '" + column.getName()
                            + "' of type " + column.getDataType());
        }
    }

    /**
     * Validates an entire row (values keyed by column name) against the schema.
     * Ensures every schema column is present (or nullable) and no unknown
     * columns are supplied.
     */
    public static void validateRowAgainstSchema(TableSchema schema, java.util.Map<String, Object> rowValues) {
        for (String key : rowValues.keySet()) {
            if (!schema.hasColumn(key)) {
                throw new IllegalArgumentException(
                        "Unknown column '" + key + "' for table '" + schema.getTableName() + "'");
            }
        }
        for (Column column : schema.getColumns()) {
            Object value = rowValues.get(column.getName());
            validateValue(column, value);
        }
    }

    /**
     * Ensures a primary key value is unique among existing rows.
     */
    public static void validatePrimaryKeyUniqueness(TableSchema schema, java.util.List<Row> existingRows,
                                                      Object candidatePkValue) {
        schema.getPrimaryKeyColumn().ifPresent(pkColumn -> {
            String pkName = pkColumn.getName();
            for (Row row : existingRows) {
                DataValue existing = row.getValue(pkName);
                if (existing != null && !existing.isNull() && existing.getValue().equals(candidatePkValue)) {
                    throw new IllegalArgumentException(
                            "Duplicate value for PRIMARY KEY column '" + pkName + "': " + candidatePkValue);
                }
            }
        });
    }
}
