package knowingipr.data.loader;

public enum MappedFields {
    TITLE("title"),
    ABSTRACT("abstract"),
    YEAR("year"),
    AUTHORS("authors");

    final String value;

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
