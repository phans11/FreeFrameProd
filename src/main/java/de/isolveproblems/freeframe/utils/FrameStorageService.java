package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.api.FrameType;
import de.isolveproblems.freeframe.api.PurchaseProfile;
import de.isolveproblems.freeframe.api.SaleMode;
import de.isolveproblems.freeframe.api.ShopOwnerType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

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
    private final Object asyncLock = new Object();
    private List<FreeFrameData> queuedSnapshot;
    private boolean workerScheduled;

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

        if (!this.freeframe.getPluginConfig().getBoolean("freeframe.storage.asyncQueue.enabled", true)) {
            if (!this.saveToDatabase(type, frames)) {
                this.freeframe.getLogger().warning("Database save failed, writing frames to YAML fallback.");
                this.saveToYaml(frames);
            }
            return;
        }

        synchronized (this.asyncLock) {
            this.queuedSnapshot = this.deepCopy(frames);
            if (!this.workerScheduled) {
                this.workerScheduled = true;
                Bukkit.getScheduler().runTaskAsynchronously(this.freeframe, new Runnable() {
                    @Override
                    public void run() {
                        processQueue();
                    }
                });
            }
        }
    }

    public void flushAndShutdown() {
        StorageType type = this.resolveType();
        if (type == StorageType.YAML) {
            return;
        }

        List<FreeFrameData> pending;
        synchronized (this.asyncLock) {
            pending = this.queuedSnapshot;
            this.queuedSnapshot = null;
            this.workerScheduled = false;
        }

        if (pending != null && !pending.isEmpty()) {
            this.saveToDatabase(type, pending);
        }
    }

    private void processQueue() {
        while (true) {
            List<FreeFrameData> snapshot;
            synchronized (this.asyncLock) {
                snapshot = this.queuedSnapshot;
                this.queuedSnapshot = null;
                if (snapshot == null || snapshot.isEmpty()) {
                    this.workerScheduled = false;
                    return;
                }
            }

            StorageType type = this.resolveType();
            if (!this.saveToDatabase(type, snapshot)) {
                this.freeframe.getLogger().warning("Async database save failed, writing YAML fallback.");
                this.saveToYaml(snapshot);
            }
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

        if (frames != null) {
            for (FreeFrameData data : frames) {
                if (data == null) {
                    continue;
                }
                ConfigurationSection frameSection = root.createSection(data.getId());
                data.writeToSection(frameSection);
            }
        }

        this.freeframe.getConfigHandler().getConfigApi().saveConfig();
    }

    private Map<String, FreeFrameData> loadFromDatabase(StorageType type) {
        Map<String, FreeFrameData> loaded = new LinkedHashMap<String, FreeFrameData>();
        String table = this.resolveTableName();

        try (Connection connection = this.openConnection(type)) {
            this.ensureTable(connection, type);

            String query = "SELECT id,reference,owner_uuid,owner_name,created_at,item_type,price,currency,active,"
                + "stock,max_stock,auto_refill,refill_interval,last_refill,revenue_total,display_entity_uuid,"
                + "frame_type,linked_chest,profiles_text,shop_owner_type,network_id,season_rule_id,branding_id,campaign_id,sale_mode,"
                + "auction_end_at,auction_min_bid,auction_highest_bid,auction_highest_bidder_uuid,"
                + "auction_highest_bidder_name,collected_tax_total "
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
                        resultSet.getString("display_entity_uuid"),
                        FrameType.fromString(resultSet.getString("frame_type")),
                        BlockReference.parse(resultSet.getString("linked_chest")),
                        this.deserializeProfiles(resultSet.getString("profiles_text")),
                        ShopOwnerType.fromString(resultSet.getString("shop_owner_type")),
                        resultSet.getString("network_id"),
                        resultSet.getString("season_rule_id"),
                        resultSet.getString("branding_id"),
                        resultSet.getString("campaign_id"),
                        SaleMode.fromString(resultSet.getString("sale_mode")),
                        resultSet.getLong("auction_end_at"),
                        resultSet.getDouble("auction_min_bid"),
                        resultSet.getDouble("auction_highest_bid"),
                        resultSet.getString("auction_highest_bidder_uuid"),
                        resultSet.getString("auction_highest_bidder_name"),
                        resultSet.getDouble("collected_tax_total")
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
                + "stock,max_stock,auto_refill,refill_interval,last_refill,revenue_total,display_entity_uuid,"
                + "frame_type,linked_chest,profiles_text,shop_owner_type,network_id,season_rule_id,branding_id,campaign_id,sale_mode,"
                + "auction_end_at,auction_min_bid,auction_highest_bid,auction_highest_bidder_uuid,"
                + "auction_highest_bidder_name,collected_tax_total"
                + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

            try (PreparedStatement statement = connection.prepareStatement(insert)) {
                if (frames != null) {
                    for (FreeFrameData data : frames) {
                        if (data == null) {
                            continue;
                        }

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
                        statement.setString(17, data.getFrameType().name());
                        statement.setString(18, data.getLinkedChest() == null ? "" : data.getLinkedChest().serialize());
                        statement.setString(19, this.serializeProfiles(data.getPurchaseProfiles()));
                        statement.setString(20, data.getShopOwnerType().name());
                        statement.setString(21, data.getNetworkId());
                        statement.setString(22, data.getSeasonRuleId());
                        statement.setString(23, data.getBrandingId());
                        statement.setString(24, data.getCampaignId());
                        statement.setString(25, data.getSaleMode().name());
                        statement.setLong(26, data.getAuctionEndAt());
                        statement.setDouble(27, data.getAuctionMinBid());
                        statement.setDouble(28, data.getHighestBid());
                        statement.setString(29, data.getHighestBidderUuid());
                        statement.setString(30, data.getHighestBidderName());
                        statement.setDouble(31, data.getCollectedTaxTotal());
                        statement.addBatch();
                    }
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
                + "display_entity_uuid VARCHAR(64) NOT NULL,"
                + "frame_type VARCHAR(32) NOT NULL DEFAULT 'SHOP',"
                + "linked_chest VARCHAR(255) NOT NULL DEFAULT '',"
                + "profiles_text LONGTEXT,"
                + "shop_owner_type VARCHAR(16) NOT NULL DEFAULT 'USER',"
                + "network_id VARCHAR(64) NOT NULL DEFAULT '',"
                + "season_rule_id VARCHAR(64) NOT NULL DEFAULT '',"
                + "branding_id VARCHAR(64) NOT NULL DEFAULT '',"
                + "campaign_id VARCHAR(64) NOT NULL DEFAULT '',"
                + "sale_mode VARCHAR(16) NOT NULL DEFAULT 'INSTANT',"
                + "auction_end_at BIGINT NOT NULL DEFAULT 0,"
                + "auction_min_bid DOUBLE NOT NULL DEFAULT 0,"
                + "auction_highest_bid DOUBLE NOT NULL DEFAULT 0,"
                + "auction_highest_bidder_uuid VARCHAR(64) NOT NULL DEFAULT '',"
                + "auction_highest_bidder_name VARCHAR(64) NOT NULL DEFAULT '',"
                + "collected_tax_total DOUBLE NOT NULL DEFAULT 0"
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
                + "display_entity_uuid TEXT NOT NULL,"
                + "frame_type TEXT NOT NULL DEFAULT 'SHOP',"
                + "linked_chest TEXT NOT NULL DEFAULT '',"
                + "profiles_text TEXT,"
                + "shop_owner_type TEXT NOT NULL DEFAULT 'USER',"
                + "network_id TEXT NOT NULL DEFAULT '',"
                + "season_rule_id TEXT NOT NULL DEFAULT '',"
                + "branding_id TEXT NOT NULL DEFAULT '',"
                + "campaign_id TEXT NOT NULL DEFAULT '',"
                + "sale_mode TEXT NOT NULL DEFAULT 'INSTANT',"
                + "auction_end_at INTEGER NOT NULL DEFAULT 0,"
                + "auction_min_bid REAL NOT NULL DEFAULT 0,"
                + "auction_highest_bid REAL NOT NULL DEFAULT 0,"
                + "auction_highest_bidder_uuid TEXT NOT NULL DEFAULT '',"
                + "auction_highest_bidder_name TEXT NOT NULL DEFAULT '',"
                + "collected_tax_total REAL NOT NULL DEFAULT 0"
                + ")";
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute(create);
        }

        this.ensureColumn(connection, table, "frame_type", type == StorageType.MYSQL ? "VARCHAR(32) NOT NULL DEFAULT 'SHOP'" : "TEXT NOT NULL DEFAULT 'SHOP'");
        this.ensureColumn(connection, table, "linked_chest", type == StorageType.MYSQL ? "VARCHAR(255) NOT NULL DEFAULT ''" : "TEXT NOT NULL DEFAULT ''");
        this.ensureColumn(connection, table, "profiles_text", type == StorageType.MYSQL ? "LONGTEXT" : "TEXT");
        this.ensureColumn(connection, table, "shop_owner_type", type == StorageType.MYSQL ? "VARCHAR(16) NOT NULL DEFAULT 'USER'" : "TEXT NOT NULL DEFAULT 'USER'");
        this.ensureColumn(connection, table, "network_id", type == StorageType.MYSQL ? "VARCHAR(64) NOT NULL DEFAULT ''" : "TEXT NOT NULL DEFAULT ''");
        this.ensureColumn(connection, table, "season_rule_id", type == StorageType.MYSQL ? "VARCHAR(64) NOT NULL DEFAULT ''" : "TEXT NOT NULL DEFAULT ''");
        this.ensureColumn(connection, table, "branding_id", type == StorageType.MYSQL ? "VARCHAR(64) NOT NULL DEFAULT ''" : "TEXT NOT NULL DEFAULT ''");
        this.ensureColumn(connection, table, "campaign_id", type == StorageType.MYSQL ? "VARCHAR(64) NOT NULL DEFAULT ''" : "TEXT NOT NULL DEFAULT ''");
        this.ensureColumn(connection, table, "sale_mode", type == StorageType.MYSQL ? "VARCHAR(16) NOT NULL DEFAULT 'INSTANT'" : "TEXT NOT NULL DEFAULT 'INSTANT'");
        this.ensureColumn(connection, table, "auction_end_at", type == StorageType.MYSQL ? "BIGINT NOT NULL DEFAULT 0" : "INTEGER NOT NULL DEFAULT 0");
        this.ensureColumn(connection, table, "auction_min_bid", type == StorageType.MYSQL ? "DOUBLE NOT NULL DEFAULT 0" : "REAL NOT NULL DEFAULT 0");
        this.ensureColumn(connection, table, "auction_highest_bid", type == StorageType.MYSQL ? "DOUBLE NOT NULL DEFAULT 0" : "REAL NOT NULL DEFAULT 0");
        this.ensureColumn(connection, table, "auction_highest_bidder_uuid", type == StorageType.MYSQL ? "VARCHAR(64) NOT NULL DEFAULT ''" : "TEXT NOT NULL DEFAULT ''");
        this.ensureColumn(connection, table, "auction_highest_bidder_name", type == StorageType.MYSQL ? "VARCHAR(64) NOT NULL DEFAULT ''" : "TEXT NOT NULL DEFAULT ''");
        this.ensureColumn(connection, table, "collected_tax_total", type == StorageType.MYSQL ? "DOUBLE NOT NULL DEFAULT 0" : "REAL NOT NULL DEFAULT 0");
    }

    private void ensureColumn(Connection connection, String table, String column, String definition) {
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        } catch (Exception ignored) {
            // Column already exists or backend does not require migration.
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

    private String serializeProfiles(List<PurchaseProfile> profiles) {
        YamlConfiguration configuration = new YamlConfiguration();
        if (profiles != null) {
            int index = 0;
            for (PurchaseProfile profile : profiles) {
                ConfigurationSection section = configuration.createSection("profiles." + index++);
                section.set("slot", profile.getSlot());
                section.set("amount", profile.getAmount());
                section.set("price", profile.getPrice());
                section.set("displayName", profile.getDisplayName());
            }
        }
        return configuration.saveToString();
    }

    private List<PurchaseProfile> deserializeProfiles(String serialized) {
        List<PurchaseProfile> profiles = new ArrayList<PurchaseProfile>();
        if (serialized == null || serialized.trim().isEmpty()) {
            return profiles;
        }

        YamlConfiguration configuration = new YamlConfiguration();
        try {
            configuration.loadFromString(serialized);
        } catch (InvalidConfigurationException exception) {
            this.freeframe.getLogger().warning("Could not parse stored purchase profiles: " + exception.getMessage());
            return profiles;
        }

        ConfigurationSection section = configuration.getConfigurationSection("profiles");
        if (section == null) {
            return profiles;
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection profileSection = section.getConfigurationSection(key);
            if (profileSection == null) {
                continue;
            }
            profiles.add(new PurchaseProfile(
                profileSection.getInt("slot", 0),
                profileSection.getInt("amount", 1),
                profileSection.getDouble("price", 0.0D),
                profileSection.getString("displayName", "")
            ));
        }
        return profiles;
    }

    private List<FreeFrameData> deepCopy(List<FreeFrameData> frames) {
        List<FreeFrameData> copy = new ArrayList<FreeFrameData>();
        if (frames == null) {
            return copy;
        }

        for (FreeFrameData frame : frames) {
            if (frame != null) {
                copy.add(frame.copy());
            }
        }
        return copy;
    }
}
