package ru.datapeice.anomalia.registry;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import ru.datapeice.anomalia.item.BoltItem;

public class ModItems {
    public static final Item BOLT_ITEM = new BoltItem(new Item.Settings().maxCount(64));

    public static void register() {
        Registry.register(Registries.ITEM, new Identifier("anomalia", "bolt"), BOLT_ITEM);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(BOLT_ITEM);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.add(BOLT_ITEM);
        });
    }
}

