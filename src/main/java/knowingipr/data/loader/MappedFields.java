package knowingipr.data.loader;

/**
 * Fields, that are mapped in the external mapping configuration
 */
public enum MappedFields {
    ID("number"),
    TITLE("title"),
    ABSTRACT("abstract"),
    YEAR("year"),
    AUTHORS("authors"),
    OWNERS("owners"),
    PUBLISHER("publisher"),
    AFFILIATION(ABSTRACT.value + "/affiliation"),
    URL("url");

    public final String value;

    MappedFields(String value) {
        this.value = value;
    }

    public boolean equalsName(String otherValue) {
        // (otherName == null) check is not needed because name.equals(null) returns false
        return value.equals(otherValue);
    }

    public String toString() {
        return this.value;
    }
}
