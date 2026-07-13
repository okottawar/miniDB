package core;

import model.Column;
import model.Row;
import model.TableSchema;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * The top-level in-memory database: holds a collection of named tables
 * and routes all CRUD operations to the appropriate Table instance.
 *
 * Also exposes a small fluent "TableBuilder" API so callers can write
 * chained column definitions, e.g.:
 *
 *   db.createTable("users")
 *     .addColumn("id", DataType.INT)
 *     .addColumn("name", DataType.STRING)
 *     .build();
 */
public class Database {

    private final Map<String, Table> tables;

    public Database() {
        this.tables = new LinkedHashMap<>();
    }

    /**
     * Starts a fluent table definition. Call .build() to finalize and register it.
     */
    public TableBuilder createTable(String tableName) {
        if (tables.containsKey(tableName)) {
            throw new IllegalArgumentException("Table already exists: " + tableName);
        }
        return new TableBuilder(this, tableName);
    }

    void registerTable(Table table) {
        tables.put(table.getName(), table);
    }

    public boolean dropTable(String tableName) {
        return tables.remove(tableName) != null;
    }

    public boolean hasTable(String tableName) {
        return tables.containsKey(tableName);
    }

    public Table getTable(String tableName) {
        Table table = tables.get(tableName);
        if (table == null) {
            throw new IllegalArgumentException("No such table: " + tableName);
        }
        return table;
    }

    public List<String> listTableNames() {
        return new java.util.ArrayList<>(tables.keySet());
    }

    // --- Convenience CRUD passthroughs -------------------------------------

    public Row insert(String tableName, Map<String, Object> values) {
        return getTable(tableName).insert(values);
    }

    public List<Row> select(String tableName) {
        return getTable(tableName).selectAll();
    }

    public List<Row> select(String tableName, Predicate<Row> filter) {
        return getTable(tableName).select(filter);
    }

    public int update(String tableName, Predicate<Row> filter, Map<String, Object> newValues) {
        return getTable(tableName).update(filter, newValues);
    }

    public int delete(String tableName, Predicate<Row> filter) {
        return getTable(tableName).delete(filter);
    }

    /**
     * Fluent builder used by createTable(...) to define columns before
     * the table is actually constructed and registered with the database.
     */
    public static class TableBuilder {
        private final Database database;
        private final TableSchema schema;

        TableBuilder(Database database, String tableName) {
            this.database = database;
            this.schema = new TableSchema(tableName);
        }

        public TableBuilder addColumn(String name, enums.DataType type) {
            schema.addColumn(new Column(name, type));
            return this;
        }

        public TableBuilder addColumn(String name, enums.DataType type, enums.Constraint... constraints) {
            java.util.EnumSet<enums.Constraint> set = java.util.EnumSet.noneOf(enums.Constraint.class);
            for (enums.Constraint c : constraints) {
                set.add(c);
            }
            schema.addColumn(new Column(name, type, set));
            return this;
        }

        /**
         * Finalizes the table, registers it with the owning database, and
         * returns the built Table instance.
         */
        public Table build() {
            Table table = new Table(schema);
            database.registerTable(table);
            return table;
        }

        public TableSchema getSchema() {
            return schema;
        }
    }
}
