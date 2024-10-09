package me.duart.blockSpawners.manager;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import me.duart.blockSpawners.BlockSpawners;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SQLiteDataStorage implements DataStorage {
    private final BlockSpawners plugin;
    private final LoadBlockSpawners loadBlockSpawners;
    private final String databaseUrl;

    public SQLiteDataStorage(@NotNull BlockSpawners plugin, LoadBlockSpawners loadBlockSpawners) {
        this.plugin = plugin;
        this.loadBlockSpawners = loadBlockSpawners;

        File dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            plugin.getLogger().severe("Failed to create data folder: " + dataFolder.getAbsolutePath());
            throw new RuntimeException("Could not create data folder");
        }

        File dbFile = new File(dataFolder, "spawners.db");
        this.databaseUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS spawners (" +
                    "world TEXT, x INTEGER, y INTEGER, z INTEGER, item BLOB, item_key TEXT, PRIMARY KEY (world, x, y, z))");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize SQLite database: " + e.getMessage());
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(databaseUrl);
    }

    @Override
    public void saveSpawnerData(@NotNull Map<Location, ItemStack> spawners) {
        String insertQuery = "INSERT OR REPLACE INTO spawners (world, x, y, z, item, item_key) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

            for (Map.Entry<Location, ItemStack> entry : spawners.entrySet()) {
                Location loc = entry.getKey();
                ItemStack item = entry.getValue();
                String itemKey = loadBlockSpawners.getItemKeyFromSpawnerItem(item);

                pstmt.setString(1, loc.getWorld().getName());
                pstmt.setInt(2, loc.getBlockX());
                pstmt.setInt(3, loc.getBlockY());
                pstmt.setInt(4, loc.getBlockZ());
                pstmt.setBytes(5, serializeItemStack(item));
                pstmt.setString(6, itemKey);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not save spawners to database: " + e.getMessage());
        }
    }

    public void removeSpawnerData(@NotNull Location location) throws SQLException {
        String deleteQuery = "DELETE FROM spawners WHERE world = ? AND x = ? AND y = ? AND z = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(deleteQuery)) {
            pstmt.setString(1, location.getWorld().getName());
            pstmt.setInt(2, location.getBlockX());
            pstmt.setInt(3, location.getBlockY());
            pstmt.setInt(4, location.getBlockZ());
            pstmt.executeUpdate();
        }
    }

    @Override
    public Map<Location, ItemStack> loadSpawnerData() {
        String selectQuery = "SELECT * FROM spawners";
        Map<Location, ItemStack> spawners = new HashMap<>();

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(selectQuery);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String world = rs.getString("world");
                int x = rs.getInt("x");
                int y = rs.getInt("y");
                int z = rs.getInt("z");
                byte[] itemData = rs.getBytes("item");
                Location location = new Location(Bukkit.getWorld(world), x, y, z);
                ItemStack item = deserializeItemStack(itemData);
                spawners.put(location, item);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not load spawners from database: " + e.getMessage());
        }

        return spawners;
    }

    private byte[] serializeItemStack(@NotNull ItemStack item) {
        return item.serializeAsBytes();
    }

    private @NotNull ItemStack deserializeItemStack(byte[] data) {
        return ItemStack.deserializeBytes(data);
    }
}