package knowingipr.data.connection;

import knowingipr.data.loader.IDbLoadArgs;
import org.bson.Document;

import java.util.Collections;
import java.util.List;

/**
 * Specifies the data structure used for the loading to the MongoDb database.
 */
public class MongoDbLoadArgs implements IDbLoadArgs {

    /**
     * MongoDb inserts a list of BSON documents to the collection.
     */
    private List<Document> documents;

    /**
     * The name of the collection to which we want to insert the documents.
     */
    private String collectionName;

    public MongoDbLoadArgs(String collectionName, List<Document> documents) {
        this.collectionName = collectionName;
        this.documents = documents;
    }

    public MongoDbLoadArgs(String collectionName) {
        this(collectionName, Collections.emptyList());
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public String getCollectionName() {
        return collectionName;
    }
}
