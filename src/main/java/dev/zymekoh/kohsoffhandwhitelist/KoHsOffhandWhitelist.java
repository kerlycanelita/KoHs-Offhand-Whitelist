package dev.zymekoh.kohsoffhandwhitelist;

import java.util.LinkedHashSet;
import java.util.Set;
import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class KoHsOffhandWhitelist implements ModInitializer {
    public static final String MOD_ID = "kohs_offhand_whitelist";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final Set<String> DEFAULT_ALLOWED_ITEM_IDS = Set.of(
            "minecraft:totem_of_undying",
            "minecraft:shield",
            "minecraft:end_crystal",
            "minecraft:obsidian",
            "minecraft:crying_obsidian",
            "minecraft:respawn_anchor",
            "minecraft:glowstone",
            "minecraft:enchanted_golden_apple",
            "minecraft:golden_apple");

    private static KoHsOffhandWhitelistConfig config;

    @Override
    public void onInitialize() {
        config = KoHsOffhandWhitelistConfig.load();
        LOGGER.info(
                "[{}] Initialized. whitelistEnabled={}, allowedItems={}",
                MOD_ID,
                config.offhandWhitelistEnabled,
                config.getAllowedItemIds().size());
    }

    public static boolean isOffhandWhitelistEnabled() {
        return getConfig().offhandWhitelistEnabled;
    }

    public static boolean isAllowedOffhandItem(Item item) {
        return getConfig().isItemAllowed(item);
    }

    public static boolean isAllowedOffhandStack(ItemStack stack) {
        return getConfig().isStackAllowed(stack);
    }

    public static KoHsOffhandWhitelistConfig copyConfig() {
        KoHsOffhandWhitelistConfig copy = new KoHsOffhandWhitelistConfig();
        copy.copyFrom(getConfig());
        return copy;
    }

    public static void saveConfig(KoHsOffhandWhitelistConfig updated) {
        KoHsOffhandWhitelistConfig live = getConfig();
        live.copyFrom(updated);
        live.save();
    }

    public static Set<String> defaultAllowedItemIds() {
        return new LinkedHashSet<>(DEFAULT_ALLOWED_ITEM_IDS);
    }

    private static KoHsOffhandWhitelistConfig getConfig() {
        if (config == null) {
            config = KoHsOffhandWhitelistConfig.load();
        }
        return config;
    }
}
