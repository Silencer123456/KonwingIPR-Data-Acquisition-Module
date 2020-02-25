package knowingipr.data.loader;

public enum LoaderName {
    PATSTAT_LOADER("patstat"),
    SPRINGER_LOADER("springer"),
    MAG_LOADER("mag"),
    USPTO_LOADER("uspto");

    public String name;

    LoaderName(String name) {
        this.name = name;
    }
}