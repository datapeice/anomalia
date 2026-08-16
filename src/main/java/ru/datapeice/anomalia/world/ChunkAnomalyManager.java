package ru.datapeice.anomalia.world;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import ru.datapeice.anomalia.api.AnomalyType;
import ru.datapeice.anomalia.config.AnomaliaConfig;

import java.util.*;

public class ChunkAnomalyManager {
    private static final Map<Long, Set<AnomalyType>> cache = new HashMap<>();
    private static long lastDay = -1;

    public static Set<AnomalyType> getAnomalies(World world, ChunkPos chunkPos) {
        if (!AnomaliaConfig.get().enabled) {
            return Collections.emptySet();
        }

        if (world.isClient() || !(world instanceof ServerWorld serverWorld)) {
            return Collections.emptySet();
        }

        long seed = serverWorld.getSeed();
        long currentDay = 0;
        if (AnomaliaConfig.get().dailyRotation) {
            currentDay = serverWorld.getTimeOfDay() / AnomaliaConfig.get().dailyRotationTicks;
        }

        if (currentDay != lastDay) {
            cache.clear();
            lastDay = currentDay;
        }

        long chunkKey = chunkPos.toLong();
        if (cache.containsKey(chunkKey)) {
            return cache.get(chunkKey);
        }

        long hash = seed + (currentDay * 71391L) + (chunkKey * 31337L);
        Random random = new Random(hash);

        double chance = AnomaliaConfig.get().chunkAnomalyChance;
        if (random.nextDouble() > chance) {
            Set<AnomalyType> empty = new HashSet<>();
            cache.put(chunkKey, empty);
            return empty;
        }

        AnomalyType[] all = AnomalyType.values();
        int min = AnomaliaConfig.get().minAnomaliesPerChunk;
        int max = AnomaliaConfig.get().maxAnomaliesPerChunk;
        int count = min + random.nextInt(max - min + 1);

        Set<AnomalyType> result = new HashSet<>();
        for (int i = 0; i < count; i++) {
            int randomIndex = random.nextInt(all.length);
            result.add(all[randomIndex]);
        }

        cache.put(chunkKey, result);
        return result;
    }

    public static boolean hasAnomaly(World world, ChunkPos chunkPos, AnomalyType anomalyType) {
        return getAnomalies(world, chunkPos).contains(anomalyType);
    }
}
