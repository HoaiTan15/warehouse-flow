package nosql.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoDBConnection {
    private static MongoClient mongoClient;
    private static MongoDatabase database;

    private static final String MONGO_URI = System.getenv("MONGODB_URI") != null
        ? System.getenv("MONGODB_URI")
        : "mongodb://localhost:27017";

    private static final String DATABASE_NAME = System.getenv("MONGODB_DATABASE") != null
        ? System.getenv("MONGODB_DATABASE")
        : "quanlykhohang_nosql";

    public static MongoDatabase getDatabase() {
        if (database == null) {
            try {
                ConnectionString connectionString = new ConnectionString(MONGO_URI);
                MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .build();

                mongoClient = MongoClients.create(settings);
                database = mongoClient.getDatabase(DATABASE_NAME);

                System.out.println("MongoDB connected successfully to: " + DATABASE_NAME);
            } catch (Exception e) {
                System.err.println("MongoDB connection failed: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return database;
    }

    public static void close() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
            database = null;
            System.out.println("MongoDB connection closed");
        }
    }

    public static boolean isConnected() {
        try {
            if (database != null) {
                database.runCommand(new org.bson.Document("ping", 1));
                return true;
            }
        } catch (Exception e) {
            System.err.println("MongoDB ping failed: " + e.getMessage());
        }
        return false;
    }
}
