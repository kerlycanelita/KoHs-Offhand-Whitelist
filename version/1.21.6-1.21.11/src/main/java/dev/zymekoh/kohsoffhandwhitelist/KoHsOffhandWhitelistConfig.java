package dev.zymekoh.kohsoffhandwhitelist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public final class KoHsOffhandWhitelistConfig {
    private static final String FILE_NAME = "kohs_offhand_whitelist.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public boolean modEnabled = true;
    public boolean offhandWhitelistEnabled = true;
    public boolean defaultPresetsInitialized;
    private final LinkedHashSet<String> allowedItemIds = new LinkedHashSet<>(KoHsOffhandWhitelist.defaultAllowedItemIds());
    private final LinkedHashMap<String, LinkedHashSet<String>> customPresets = new LinkedHashMap<>();

    public static KoHsOffhandWhitelistConfig load() {
        KoHsOffhandWhitelistConfig config = new KoHsOffhandWhitelistConfig();
        Path path = getPath();

        if (!Files.exists(path)) {
            config.save();
            return config;
        }

        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            RawConfig raw = GSON.fromJson(reader, RawConfig.class);
            if (raw != null) {
                if (raw.modEnabled != null) {
                    config.modEnabled = raw.modEnabled;
                }
                if (raw.offhandWhitelistEnabled != null) {
                    config.offhandWhitelistEnabled = raw.offhandWhitelistEnabled;
                }
                if (raw.defaultPresetsInitialized != null) {
                    config.defaultPresetsInitialized = raw.defaultPresetsInitialized;
                }
                if (raw.allowedItemIds != null) {
                    config.setAllowedItemIds(raw.allowedItemIds);
                }
                if (raw.customPresets != null) {
                    config.setCustomPresets(raw.customPresets);
                }
            }
        } catch (Exception e) {
            KoHsOffhandWhitelist.LOGGER.error(
                    "[{}] Failed to read config '{}': {}",
                    KoHsOffhandWhitelist.MOD_ID,
                    path,
                    e.toString());
        }

        config.save();
        return config;
    }

    public void save() {
        Path path = getPath();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                RawConfig raw = new RawConfig();
                raw.modEnabled = this.modEnabled;
                raw.offhandWhitelistEnabled = this.offhandWhitelistEnabled;
                raw.defaultPresetsInitialized = this.defaultPresetsInitialized;
                raw.allowedItemIds = new ArrayList<>(this.allowedItemIds);
                raw.customPresets = new LinkedHashMap<>();
                for (Map.Entry<String, LinkedHashSet<String>> entry : this.customPresets.entrySet()) {
                    raw.customPresets.put(entry.getKey(), new ArrayList<>(entry.getValue()));
                }
                GSON.toJson(raw, writer);
            }
        } catch (IOException e) {
            KoHsOffhandWhitelist.LOGGER.error(
                    "[{}] Failed to write config '{}': {}",
                    KoHsOffhandWhitelist.MOD_ID,
                    path,
                    e.toString());
        }
    }

    private static Path getPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }

    public void copyFrom(KoHsOffhandWhitelistConfig other) {
        this.modEnabled = other.modEnabled;
        this.offhandWhitelistEnabled = other.offhandWhitelistEnabled;
        this.defaultPresetsInitialized = other.defaultPresetsInitialized;
        this.allowedItemIds.clear();
        this.allowedItemIds.addAll(other.allowedItemIds);
        this.customPresets.clear();
        for (Map.Entry<String, LinkedHashSet<String>> entry : other.customPresets.entrySet()) {
            this.customPresets.put(entry.getKey(), new LinkedHashSet<>(entry.getValue()));
        }
    }

    public Set<String> getAllowedItemIds() {
        return Collections.unmodifiableSet(this.allowedItemIds);
    }

    public void setAllowedItemIds(Collection<String> ids) {
        this.allowedItemIds.clear();
        if (ids == null) {
            this.allowedItemIds.addAll(KoHsOffhandWhitelist.defaultAllowedItemIds());
        } else {
            this.allowedItemIds.addAll(sanitizeExistingItemIds(ids));
        }
    }

    public boolean isItemAllowed(Item item) {
        Identifier id = Registries.ITEM.getId(item);
        return id != null && this.allowedItemIds.contains(id.toString());
    }

    public boolean isStackAllowed(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        Identifier itemId = Registries.ITEM.getId(stack.getItem());
        if (itemId == null) {
            return false;
        }
        if (this.allowedItemIds.contains(itemId.toString())) {
            return true;
        }

        String potionKey = getPotionVariantKey(stack);
        return potionKey != null && this.allowedItemIds.contains(potionKey);
    }

    public Map<String, Set<String>> getCustomPresets() {
        LinkedHashMap<String, Set<String>> copy = new LinkedHashMap<>();
        for (Map.Entry<String, LinkedHashSet<String>> entry : this.customPresets.entrySet()) {
            copy.put(entry.getKey(), Collections.unmodifiableSet(entry.getValue()));
        }
        return Collections.unmodifiableMap(copy);
    }

    public void setCustomPresets(Map<String, ? extends Collection<String>> presets) {
        this.customPresets.clear();
        if (presets == null) {
            return;
        }
        for (Map.Entry<String, ? extends Collection<String>> entry : presets.entrySet()) {
            String name = sanitizePresetName(entry.getKey());
            if (name.isEmpty()) {
                continue;
            }
            LinkedHashSet<String> items = sanitizeExistingItemIds(entry.getValue());
            this.customPresets.put(name, items);
        }
    }

    public static LinkedHashSet<String> sanitizeExistingItemIds(Collection<String> ids) {
        LinkedHashSet<String> sanitized = new LinkedHashSet<>();
        for (String value : ids) {
            if (value == null || value.isBlank()) {
                continue;
            }
            String raw = value.trim();
            if (isValidPotionVariantKey(raw)) {
                sanitized.add(raw);
                continue;
            }

            Identifier id = Identifier.tryParse(raw);
            if (id == null || !Registries.ITEM.containsId(id)) {
                continue;
            }
            if (id.equals(Identifier.ofVanilla("air"))) {
                continue;
            }
            sanitized.add(id.toString());
        }
        return sanitized;
    }

    public static String sanitizePresetName(String name) {
        if (name == null) {
            return "";
        }
        String normalized = name.trim();
        if (normalized.isEmpty()) {
            return "";
        }
        if (normalized.length() > 48) {
            normalized = normalized.substring(0, 48).trim();
        }
        return normalized;
    }

    public static String getPotionVariantKey(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        Identifier itemId = Registries.ITEM.getId(stack.getItem());
        if (itemId == null || !isPotionItemId(itemId)) {
            return null;
        }

        PotionContentsComponent contents = stack.get(DataComponentTypes.POTION_CONTENTS);
        if (contents == null || contents.potion().isEmpty()) {
            return null;
        }
        RegistryEntry<Potion> potionEntry = contents.potion().orElse(null);
        if (potionEntry == null) {
            return null;
        }
        Identifier potionId = Registries.POTION.getId(potionEntry.value());
        if (potionId == null) {
            return null;
        }

        return itemId + "|" + potionId;
    }

    public static boolean isValidPotionVariantKey(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        int separator = value.indexOf('|');
        if (separator <= 0 || separator >= value.length() - 1 || value.indexOf('|', separator + 1) >= 0) {
            return false;
        }

        Identifier itemId = Identifier.tryParse(value.substring(0, separator));
        Identifier potionId = Identifier.tryParse(value.substring(separator + 1));
        if (itemId == null || potionId == null) {
            return false;
        }
        if (!isPotionItemId(itemId)) {
            return false;
        }
        return Registries.POTION.containsId(potionId);
    }

    private static boolean isPotionItemId(Identifier itemId) {
        if (!Registries.ITEM.containsId(itemId)) {
            return false;
        }
        Item item = Registries.ITEM.get(itemId);
        return item == Items.POTION || item == Items.SPLASH_POTION || item == Items.LINGERING_POTION;
    }

    private static final class RawConfig {
        private Boolean modEnabled;
        private Boolean offhandWhitelistEnabled;
        private Boolean defaultPresetsInitialized;
        private List<String> allowedItemIds;
        private Map<String, List<String>> customPresets;
    }
}
