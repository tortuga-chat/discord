package chat.tortuga.discord.persistence;

import io.jsondb.JsonDBTemplate;

public class JsonDB {

    private static final String DB_FILES_LOCATION = "./db";
    private static final String BASE_SCAN_PACKAGE = "chat.tortuga.discord.persistence.model";
    private static final JsonDBTemplate db = new JsonDBTemplate(DB_FILES_LOCATION, BASE_SCAN_PACKAGE);

    private JsonDB(){}

    public static JsonDBTemplate getJsonDB() {
        return db;
    }

}
