package ru.datapeice.anomalia;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.datapeice.anomalia.config.AnomaliaConfig;
import ru.datapeice.anomalia.network.AnomalyNetworking;
import ru.datapeice.anomalia.registry.ModEntities;
import ru.datapeice.anomalia.registry.ModItems;

public class AnomaliaMod implements ModInitializer {
    public static final String MOD_ID = "anomalia";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("anomalia mod by @datapeice");
        AnomaliaConfig.load();
        ModItems.register();
        ModEntities.register();
        AnomalyNetworking.registerServer();

    }
}

