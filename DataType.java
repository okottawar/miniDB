package enums;

/**
 * Supported column data types in MiniDB.
 */
public enum DataType {
    INT,
    STRING,
    BOOLEAN,
    DOUBLE;

    /**
     * Checks whether a given Java object is compatible with this DataType.
     */
    public boolean isValidValue(Object value) {
        if (value == null) {
            return true; // null-ness is handled separately via NOT NULL constraint
        }
        switch (this) {
            case INT:
                return value instanceof Integer;
            case STRING:
                return value instanceof String;
            case BOOLEAN:
                return value instanceof Boolean;
            case DOUBLE:
                return value instanceof Double || value instanceof Integer;
            default:
                return false;
        }
    }
}
