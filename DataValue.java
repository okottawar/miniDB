package model;

import enums.DataType;

/**
 * A typed wrapper around a single cell value in a row.
 * Keeps the raw Java object together with the DataType it was validated against.
 */
public class DataValue {

    private final DataType type;
    private final Object value;

    public DataValue(DataType type, Object value) {
        this.type = type;
        this.value = value;
    }

    public DataType getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public boolean isNull() {
        return value == null;
    }

    @Override
    public String toString() {
        return value == null ? "NULL" : value.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataValue)) return false;
        DataValue other = (DataValue) o;
        if (value == null) return other.value == null;
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value == null ? 0 : value.hashCode();
    }
}
