package me.duart.blockSpawners.manager;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface DataStorage {
    void saveSpawnerData(Map<Location, ItemStack> spawners) throws SQLException;
    void removeSpawnerData(Location location) throws SQLException;
    Map<Location, ItemStack> loadSpawnerData() throws SQLException;
    CompletableFuture<Void> saveSpawnerDataAsync(Map<Location, ItemStack> spawners);
    CompletableFuture<Map<Location, ItemStack>> loadSpawnerDataAsync();
}
