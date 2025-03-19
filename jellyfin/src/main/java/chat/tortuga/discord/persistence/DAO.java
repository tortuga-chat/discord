package chat.tortuga.discord.persistence;

import io.jsondb.JsonDBTemplate;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
@SuppressWarnings("unused")
public class DAO<E, T> {

    protected final Class<E> type;
    private final JsonDBTemplate jsondb;

    public DAO(Class<E> type) {
        this.type = type;
        this.jsondb = JsonDB.getJsonDB();
        if(!jsondb.collectionExists(type))
            jsondb.createCollection(type);
        if (jsondb.isCollectionReadonly(type))
            log.warn("collection {} is read-only!", type.getSimpleName());
    }

    public boolean exists(T id) {
        return jsondb.findById(id, type) != null;
    }

    public List<E> findAll() {
        return jsondb.findAll(type);
    }

    public Optional<E> findById(T id) {
        return Optional.ofNullable(jsondb.findById(id, type));
    }

    public List<E> find(String query) {
        return jsondb.find(query, type);
    }

    public Optional<E> findOne(String query) {
        return Optional.ofNullable(jsondb.findOne(query, type));
    }

    public void insert(E pojo) {
        jsondb.insert(pojo);
    }

    public void save(E pojo) {
        jsondb.save(pojo, type);
    }

    public void remove(E pojo) {
        jsondb.remove(pojo, type);
    }
}
