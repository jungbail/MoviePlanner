package model;

/**
 * Comparison / filter operations supported by the application.
 */
public enum Operations {
    /** Operations to use. */
    EQUALS("=="),
    NOT_EQUALS("!="),
    GREATER_THAN(">"),
    LESS_THAN("<"),
    GREATER_THAN_EQUALS(">="),
    LESS_THAN_EQUALS("<="),
    CONTAINS("~=");

    /** The operation string. */
    private final String operation;

    /**
     * Constructor for the Operations enum.
     *
     * @param operation the operation string.
     */
    Operations(String operation) {
        this.operation = operation;
    }

    /**
     * Getter for the operation string.
     *
     * @return the operation string.
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Resolves an {@link Operations} from free-form text by substring match.
     * Longer tokens (e.g. {@code >=}) are checked before shorter ones ({@code >}) so they are not swallowed.
     *
     * @param str the string to interpret
     * @return the matching operation
     * @throws IllegalArgumentException if {@code str} is null or no operation matches
     */
    public static Operations getOperationFromStr(String str) {
        if (str == null) {
            throw new IllegalArgumentException("Operation string must not be null");
        }
        if (str.contains(">=")) {
            return GREATER_THAN_EQUALS;
        }
        if (str.contains("<=")) {
            return LESS_THAN_EQUALS;
        }
        if (str.contains("==")) {
            return EQUALS;
        }
        if (str.contains("!=")) {
            return NOT_EQUALS;
        }
        if (str.contains("~=")) {
            return CONTAINS;
        }
        if (str.contains(">")) {
            return GREATER_THAN;
        }
        if (str.contains("<")) {
            return LESS_THAN;
        }
        throw new IllegalArgumentException("Invalid operation: " + str);
    }
}
