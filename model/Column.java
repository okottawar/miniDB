package model;

import enums.Constraint;
import enums.DataType;

import java.util.EnumSet;
import java.util.Set;

/**
 * Represents a single column definition within a table schema.
 */
public class Column {

    private final String name;
    private final DataType dataType;
    private final Set<Constraint> constraints;

    public Column(String name, DataType dataType) {
        this(name, dataType, EnumSet.noneOf(Constraint.class));
    }

    public Column(String name, DataType dataType, Set<Constraint> constraints) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Column name cannot be null or empty");
        }
        if (dataType == null) {
            throw new IllegalArgumentException("Column dataType cannot be null");
        }
        this.name = name;
        this.dataType = dataType;
        this.constraints = constraints == null ? EnumSet.noneOf(Constraint.class) : EnumSet.copyOf(constraints);
    }

    public String getName() {
        return name;
    }

    public DataType getDataType() {
        return dataType;
    }

    public Set<Constraint> getConstraints() {
        return constraints;
    }

    public boolean hasConstraint(Constraint constraint) {
        return constraints.contains(constraint);
    }

    public boolean isPrimaryKey() {
        return constraints.contains(Constraint.PRIMARY_KEY);
    }

    public boolean isNotNull() {
        // Primary keys are implicitly NOT NULL
        return constraints.contains(Constraint.NOT_NULL) || isPrimaryKey();
    }

    public void addConstraint(Constraint constraint) {
        constraints.add(constraint);
    }

    @Override
    public String toString() {
        return name + " (" + dataType + (constraints.isEmpty() ? "" : " " + constraints) + ")";
    }
}
