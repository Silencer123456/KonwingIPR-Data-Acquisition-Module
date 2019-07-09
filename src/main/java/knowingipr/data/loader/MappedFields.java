package knowingipr.data.loader;

/**
 * Fields, that are mapped in the external mapping configuration
 */
public enum MappedFields {
    ID("number"),
    TITLE("title"),
    ABSTRACT("abstract"),
    YEAR("year"),
    DATE("date"),
    AUTHORS("authors"),
    OWNERS("owners"),
    PUBLISHER("publisher"),
    AFFILIATION(ABSTRACT.value + "/affiliation"),
    FOS("fos"),
    ISSUE("issue"),
    URL("url"),
    KEYWORDS("keywords"),
    VENUE("venue"),
    LANG("lang");

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
