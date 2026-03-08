package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.FreeFrame;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FrameStorageService {
    private static final String FRAMES_DATA_PATH = "freeframe.framesData";

    public enum StorageType {
        YAML,
        SQLITE,
        MYSQL
    }

    private final FreeFrame freeframe;

    public FrameStorageService(FreeFrame freeframe) {
        this.freeframe = freeframe;
    }

    public StorageType resolveType() {
        String configured = this.freeframe.getPluginConfig().getString("freeframe.storage.type", "yaml");
        if (configured == null) {
            return StorageType.YAML;
        }

        String normalized = configured.trim().toLowerCase(Locale.ENGLISH);
        if ("sqlite".equals(normalized)) {
            return StorageType.SQLITE;
        }
        if ("mysql".equals(normalized)) {
            return StorageType.MYSQL;
        }
        return StorageType.YAML;
    }

    public Map<String, FreeFrameData> loadFrames() {
        StorageType type = this.resolveType();
        if (type == StorageType.YAML) {
            return this.loadFromYaml();
        }

        Map<String, FreeFrameData> dbFrames = this.loadFromDatabase(type);
        if (dbFrames != null) {
            return dbFrames;
        }

        this.freeframe.getLogger().warning("Falling back to YAML storage after database load error.");
        return this.loadFromYaml();
    }

    public void saveFrames(List<FreeFrameData> frames) {
        StorageType type = this.resolveType();
        if (type == StorageType.YAML) {
            this.saveToYaml(frames);
            return;
        }

        if (!this.saveToDatabase(type, frames)) {
            this.freeframe.getLogger().warning("Database save failed, writing frames to YAML fallback.");
            this.saveToYaml(frames);
        }
    }

    private Map<String, FreeFrameData> loadFromYaml() {
        Map<String, FreeFrameData> loaded = new LinkedHashMap<String, FreeFrameData>();
        ConfigurationSection section = this.freeframe.getPluginConfig().getConfigurationSection(FRAMES_DATA_PATH);
        if (section == null) {
            return loaded;
        }

        for (String id : section.getKeys(false)) {
            FreeFrameData data = FreeFrameData.fromSection(id, section.getConfigurationSection(id));
            if (data != null) {
                loaded.put(data.getId(), data);
            }
        }
        return loaded;
    }

    private void saveToYaml(List<FreeFrameData> frames) {
        this.freeframe.getPluginConfig().set(FRAMES_DATA_PATH, null);
        ConfigurationSection root = this.freeframe.getPluginConfig().createSection(FRAMES_DATA_PATH);

        for (FreeFrameData data : frames) {
            ConfigurationSection frameSection = root.createSection(data.getId());
            data.writeToSection(frameSection);
        }

        this.freeframe.getConfigHandler().getConfigApi().saveConfig();
    }

    private Map<String, FreeFrameData> loadFromDatabase(StorageType type) {
        Map<String, FreeFrameData> loaded = new LinkedHashMap<String, FreeFrameData>();
        String table = this.resolveTableName();

        try (Connection connection = this.openConnection(type)) {
            this.ensureTable(connection, type);

            String query = "SELECT id,reference,owner_uuid,owner_name,created_at,item_type,price,currency,active,"
                + "stock,max_stock,auto_refill,refill_interval,last_refill,revenue_total,display_entity_uuid "
                + "FROM " + table;

            try (PreparedStatement statement = connection.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    String id = resultSet.getString("id");
                    FrameReference reference = FrameReference.parse(resultSet.getString("reference"));
                    if (id == null || reference == null) {
                        continue;
                    }

                    FreeFrameData data = new FreeFrameData(
                        id,
                        reference,
                        resultSet.getString("owner_uuid"),
                        resultSet.getString("owner_name"),
                        resultSet.getLong("created_at"),
                        resultSet.getString("item_type"),
                        resultSet.getDouble("price"),
                        resultSet.getString("currency"),
                        resultSet.getBoolean("active"),
                        resultSet.getInt("stock"),
                        resultSet.getInt("max_stock"),
                        resultSet.getBoolean("auto_refill"),
                        resultSet.getLong("refill_interval"),
                        resultSet.getLong("last_refill"),
                        resultSet.getDouble("revenue_total"),
                        resultSet.getString("display_entity_uuid")
                    );
                    loaded.put(data.getId(), data);
                }
            }
            return loaded;
        } catch (Exception exception) {
            this.freeframe.getLogger().warning("Could not load frames from " + type.name() + ": " + exception.getMessage());
            return null;
        }
    }

    private boolean saveToDatabase(StorageType type, List<FreeFrameData> frames) {
        String table = this.resolveTableName();
        try (Connection connection = this.openConnection(type)) {
            this.ensureTable(connection, type);
            connection.setAutoCommit(false);

            try (Statement wipe = connection.createStatement()) {
                wipe.executeUpdate("DELETE FROM " + table);
            }

            String insert = "INSERT INTO " + table + " ("
                + "id,reference,owner_uuid,owner_name,created_at,item_type,price,currency,active,"
                + "stock,max_stock,auto_refill,refill_interval,last_refill,revenue_total,display_entity_uuid"
                + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

            try (PreparedStatement statement = connection.prepareStatement(insert)) {
                for (FreeFrameData data : frames) {
                    statement.setString(1, data.getId());
                    statement.setString(2, data.getReference().serialize());
                    statement.setString(3, data.getOwnerUuid());
                    statement.setString(4, data.getOwnerName());
                    statement.setLong(5, data.getCreatedAt());
                    statement.setString(6, data.getItemType());
                    statement.setDouble(7, data.getPrice());
                    statement.setString(8, data.getCurrency());
                    statement.setBoolean(9, data.isActive());
                    statement.setInt(10, data.getStock());
                    statement.setInt(11, data.getMaxStock());
                    statement.setBoolean(12, data.isAutoRefill());
                    statement.setLong(13, data.getRefillIntervalMillis());
                    statement.setLong(14, data.getLastRefillAt());
                    statement.setDouble(15, data.getRevenueTotal());
                    statement.setString(16, data.getDisplayEntityUuid());
                    statement.addBatch();
                }
                statement.executeBatch();
            }

            connection.commit();
            return true;
        } catch (Exception exception) {
            this.freeframe.getLogger().warning("Could not save frames to " + type.name() + ": " + exception.getMessage());
            return false;
        }
    }

    private void ensureTable(Connection connection, StorageType type) throws Exception {
        String table = this.resolveTableName();
        String create;
        if (type == StorageType.MYSQL) {
            create = "CREATE TABLE IF NOT EXISTS " + table + " ("
                + "id VARCHAR(32) PRIMARY KEY,"
                + "reference VARCHAR(255) NOT NULL,"
                + "owner_uuid VARCHAR(64) NOT NULL,"
                + "owner_name VARCHAR(64) NOT NULL,"
                + "created_at BIGINT NOT NULL,"
                + "item_type VARCHAR(64) NOT NULL,"
                + "price DOUBLE NOT NULL,"
                + "currency VARCHAR(16) NOT NULL,"
                + "active TINYINT(1) NOT NULL,"
                + "stock INT NOT NULL,"
                + "max_stock INT NOT NULL,"
                + "auto_refill TINYINT(1) NOT NULL,"
                + "refill_interval BIGINT NOT NULL,"
                + "last_refill BIGINT NOT NULL,"
                + "revenue_total DOUBLE NOT NULL,"
                + "display_entity_uuid VARCHAR(64) NOT NULL"
                + ")";
        } else {
            create = "CREATE TABLE IF NOT EXISTS " + table + " ("
                + "id TEXT PRIMARY KEY,"
                + "reference TEXT NOT NULL,"
                + "owner_uuid TEXT NOT NULL,"
                + "owner_name TEXT NOT NULL,"
                + "created_at INTEGER NOT NULL,"
                + "item_type TEXT NOT NULL,"
                + "price REAL NOT NULL,"
                + "currency TEXT NOT NULL,"
                + "active INTEGER NOT NULL,"
                + "stock INTEGER NOT NULL,"
                + "max_stock INTEGER NOT NULL,"
                + "auto_refill INTEGER NOT NULL,"
                + "refill_interval INTEGER NOT NULL,"
                + "last_refill INTEGER NOT NULL,"
                + "revenue_total REAL NOT NULL,"
                + "display_entity_uuid TEXT NOT NULL"
                + ")";
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute(create);
        }
    }

    private Connection openConnection(StorageType type) throws Exception {
        if (type == StorageType.SQLITE) {
            String fileName = this.freeframe.getPluginConfig().getString("freeframe.storage.sqlite.file", "freeframe.db");
            if (fileName == null || fileName.trim().isEmpty()) {
                fileName = "freeframe.db";
            }

            File databaseFile = new File(this.freeframe.getDataFolder(), fileName);
            File parent = databaseFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            String url = "jdbc:sqlite:" + databaseFile.getAbsolutePath();
            return DriverManager.getConnection(url);
        }

        String host = this.freeframe.getPluginConfig().getString("freeframe.storage.mysql.host", "127.0.0.1");
        int port = this.freeframe.getPluginConfig().getInt("freeframe.storage.mysql.port", 3306);
        String database = this.freeframe.getPluginConfig().getString("freeframe.storage.mysql.database", "freeframe");
        String username = this.freeframe.getPluginConfig().getString("freeframe.storage.mysql.username", "root");
        String password = this.freeframe.getPluginConfig().getString("freeframe.storage.mysql.password", "");
        boolean ssl = this.freeframe.getPluginConfig().getBoolean("freeframe.storage.mysql.ssl", false);

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database
            + "?useSSL=" + ssl + "&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        return DriverManager.getConnection(url, username, password);
    }

    public List<String> getSupportedBackends() {
        List<String> backends = new ArrayList<String>();
        backends.add("yaml");
        backends.add("sqlite");
        backends.add("mysql");
        return backends;
    }

    private String resolveTableName() {
        String configured = this.freeframe.getPluginConfig().getString("freeframe.storage.mysql.table", "freeframe_frames");
        if (configured == null || configured.trim().isEmpty()) {
            return "freeframe_frames";
        }

        String sanitized = configured.trim().replaceAll("[^A-Za-z0-9_]", "_");
        if (sanitized.isEmpty()) {
            return "freeframe_frames";
        }
        return sanitized;
    }
}
