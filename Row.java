package model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a single row of data, mapping column name -> DataValue.
 * Order of insertion is preserved (mirrors column order) via LinkedHashMap.
 */
public class Row {

    private final Map<String, DataValue> values;

    public Row() {
        this.values = new LinkedHashMap<>();
    }

    public void setValue(String columnName, DataValue value) {
        values.put(columnName, value);
    }

    public DataValue getValue(String columnName) {
        return values.get(columnName);
    }

    public Object getRawValue(String columnName) {
        DataValue dv = values.get(columnName);
        return dv == null ? null : dv.getValue();
    }

    public Map<String, DataValue> getValues() {
        return values;
    }

    public boolean hasColumn(String columnName) {
        return values.containsKey(columnName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, DataValue> entry : values.entrySet()) {
            if (!first) sb.append(", ");
            sb.append(entry.getKey()).append("=").append(entry.getValue());
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
}
