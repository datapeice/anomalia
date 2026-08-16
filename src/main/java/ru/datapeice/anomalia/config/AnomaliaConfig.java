package ru.datapeice.anomalia.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class AnomaliaConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "anomalia.json");

    public boolean enabled = true;
    public double chunkAnomalyChance = 0.40;
    public double bossSpawnChance = 0.002;
    public int minAnomaliesPerChunk = 1;
    public int maxAnomaliesPerChunk = 3;
    public boolean dailyRotation = true;
    public int dailyRotationTicks = 24000;
    public boolean allowBossDragonInOverworld = true;
    public boolean allowBossWitherInOverworld = true;
    public boolean allowBossWardenInOverworld = true;

    private static AnomaliaConfig INSTANCE;

    public static AnomaliaConfig get() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, AnomaliaConfig.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (INSTANCE == null) {
            INSTANCE = new AnomaliaConfig();
            save();
        }
    }

    public static void save() {
        try {
            if (!CONFIG_FILE.getParentFile().exists()) {
                CONFIG_FILE.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

