package dev.zymekoh.kohsoffhandwhitelist;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

public final class KoHsOffhandWhitelistConfigScreen extends Screen {
    private enum Tab {
        WHITELIST("Whitelist"),
        CREATE_PRESET("Create Preset"),
        CUSTOM_PRESETS("Custom Presets"),
        REPORT_ISSUES("Report Issues");

        private final String title;

        Tab(String title) {
            this.title = title;
        }
    }

    private static final int ROW_HEIGHT = 22;
    private static final int ROW_BUTTON_SIZE = 18;
    private static final int PANEL_MARGIN = 14;
    private static final int PANEL_TARGET_WIDTH = 860;
    private static final int PANEL_TARGET_HEIGHT = 520;
    private static final int LIST_HEADER_HEIGHT = 20;
    private static final int LIST_GAP = 16;
    private static final int LIST_TOP_GAP = 8;
    private static final int INPUT_FRAME_HEIGHT = 22;
    private static final int INPUT_TEXTBOX_HEIGHT = 18;
    private static final int INPUT_FRAME_PAD_X = 4;
    private static final int INPUT_FRAME_PAD_Y = 2;
    private static final int SEARCH_FIELD_Y_OFFSET = 1;
    private static final int REPORT_DISCORD_BUTTON_WIDTH = 180;
    private static final int TAB_BAR_HEIGHT = 32;
    private static final int TAB_ANIMATION_DURATION_MS = 220;
    private static final int PAGE_TRANSITION_DURATION_MS = 260;

    private static final int COLOR_BG_TOP = 0x14070A16;
    private static final int COLOR_BG_BOTTOM = 0x0A04070F;
    private static final int COLOR_PANEL_FILL = 0x4A120B26;
    private static final int COLOR_PANEL_EDGE = 0xB06F2ED5;
    private static final int COLOR_PANEL_EDGE_SOFT = 0x60582A9B;
    private static final int COLOR_SECTION_FILL = 0x3A120C23;
    private static final int COLOR_SECTION_HEADER = 0x66482E7E;
    private static final int COLOR_TAB_BAR_FILL = 0x3A120A24;
    private static final int COLOR_TAB_IDLE = 0x34201642;
    private static final int COLOR_TAB_HOVER = 0x4A2A1B5A;
    private static final int COLOR_TAB_ACTIVE = 0x6A3A2478;
    private static final int COLOR_ROW_A = 0x23231A37;
    private static final int COLOR_ROW_B = 0x30302349;
    private static final int COLOR_TEXT_MAIN = 0xFFF1E7FF;
    private static final int COLOR_TEXT_SUB = 0xFFCEB8EF;
    private static final int COLOR_TEXT_DIM = 0xFFA58DBF;
    private static final int COLOR_ROW_ACTIVE_PRESET = 0x3F2A6A3A;
    private static final String DISCORD_SUPPORT_URL = "https://discord.gg/TpBbyxhFSF";
    private static final Identifier LOGO_TEXTURE = Identifier.of(KoHsOffhandWhitelist.MOD_ID, "textures/dddd.png");
    private static final Identifier LOGO_TEXTURE_FALLBACK = Identifier.of(KoHsOffhandWhitelist.MOD_ID, "textures/dddd.jpeg");
    private static final Identifier LOGO_TEXTURE_LEGACY = Identifier.of(KoHsOffhandWhitelist.MOD_ID, "dddd.png");
    private static final Identifier LOGO_TEXTURE_LEGACY_FALLBACK = Identifier.of(KoHsOffhandWhitelist.MOD_ID, "dddd.jpeg");

    private static final String PRESET_CRYSTAL_NAME = "Crystal PvP";
    private static final String PRESET_NETHERITE_NAME = "Netherite Pot PvP";
    private static final String PRESET_MACE_NAME = "Mace PvP";

    private static final Set<String> PRESET_CRYSTAL_PVP = Set.of(
            "minecraft:totem_of_undying",
            "minecraft:shield",
            "minecraft:end_crystal",
            "minecraft:obsidian",
            "minecraft:crying_obsidian",
            "minecraft:respawn_anchor",
            "minecraft:glowstone",
            "minecraft:enchanted_golden_apple",
            "minecraft:golden_apple");
    private static final Set<String> PRESET_NETHERITE_POT_PVP = Set.of(
            "minecraft:totem_of_undying",
            "minecraft:shield",
            "minecraft:enchanted_golden_apple",
            "minecraft:golden_apple",
            "minecraft:splash_potion",
            "minecraft:lingering_potion");
    private static final Set<String> PRESET_MACE_PVP = Set.of(
            "minecraft:totem_of_undying",
            "minecraft:mace",
            "minecraft:wind_charge",
            "minecraft:shield",
            "minecraft:enchanted_golden_apple",
            "minecraft:golden_apple");
    private static final LinkedHashMap<String, Set<String>> BUILT_IN_PRESETS = createBuiltInPresets();
    private static final List<Item> POTION_CONTAINER_ITEMS = List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION);

    private final Screen parent;
    private final KoHsOffhandWhitelistConfig working;
    private final LinkedHashSet<String> workingAllowedItemIds;
    private final LinkedHashSet<String> presetBuilderItemIds;
    private final LinkedHashMap<String, LinkedHashSet<String>> customPresets;
    private final List<ItemEntry> allItems;
    private final Set<String> itemRenderWarningIds = new HashSet<>();

    private Tab activeTab = Tab.WHITELIST;
    private String editingPresetName;
    private String activePresetName;

    private List<ItemEntry> leftItems = List.of();
    private List<ItemEntry> rightItems = List.of();
    private List<CustomPresetEntry> customPresetItems = List.of();

    private int[] leftRowMap = new int[0];
    private int[] rightRowMap = new int[0];
    private int[] customRowMap = new int[0];
    private int visibleRows;

    private int leftScrollOffset;
    private int rightScrollOffset;
    private int customScrollOffset;
    private int maxLeftScrollOffset;
    private int maxRightScrollOffset;
    private int maxCustomScrollOffset;

    private int panelLeft;
    private int panelRight;
    private int panelTop;
    private int panelBottom;
    private int leftListLeft;
    private int leftListRight;
    private int rightListLeft;
    private int rightListRight;
    private int listTop;
    private int listBottom;
    private int listBodyTop;
    private int titleY;
    private int subtitleY;
    private int tabY;
    private int toggleY;
    private int defaultToggleX;
    private int defaultToggleY;
    private int defaultToggleWidth;
    private int actionY;
    private int searchY;
    private long screenOpenedAtMs;
    private Identifier cachedLogoTextureId;
    private int cachedLogoTextureWidth = -1;
    private int cachedLogoTextureHeight = -1;
    private float animatedTabIndicatorLeft;
    private float animatedTabIndicatorRight;
    private float tabIndicatorTargetLeft;
    private float tabIndicatorTargetRight;
    private long lastTabAnimationFrameMs;
    private long tabTransitionStartMs;
    private long pageTransitionStartMs;
    private int pageTransitionDirection = 1;

    private TextFieldWidget leftSearchField;
    private TextFieldWidget rightSearchField;
    private TextFieldWidget customSearchField;
    private TextFieldWidget presetNameField;
    private ButtonWidget tabWhitelistButton;
    private ButtonWidget tabCreatePresetButton;
    private ButtonWidget tabCustomPresetsButton;
    private ButtonWidget tabReportIssuesButton;
    private ButtonWidget whitelistToggleButton;
    private ButtonWidget savePresetButton;
    private ButtonWidget clearPresetSelectionButton;
    private ButtonWidget openDiscordButton;

    private final List<ButtonWidget> leftRemoveButtons = new ArrayList<>();
    private final List<ButtonWidget> rightAddButtons = new ArrayList<>();
    private final List<ButtonWidget> customUseButtons = new ArrayList<>();
    private final List<ButtonWidget> customEditButtons = new ArrayList<>();
    private final List<ButtonWidget> customDeleteButtons = new ArrayList<>();

    public static Screen create(Screen parent) {
        return new KoHsOffhandWhitelistConfigScreen(parent);
    }

    private KoHsOffhandWhitelistConfigScreen(Screen parent) {
        super(Text.literal("KoHs Offhand Whitelist"));
        this.parent = parent;
        this.screenOpenedAtMs = Util.getMeasuringTimeMs();
        this.tabTransitionStartMs = this.screenOpenedAtMs;
        this.working = KoHsOffhandWhitelist.copyConfig();
        this.workingAllowedItemIds = new LinkedHashSet<>(this.working.getAllowedItemIds());
        this.presetBuilderItemIds = new LinkedHashSet<>(this.workingAllowedItemIds);
        this.customPresets = new LinkedHashMap<>();
        for (Map.Entry<String, Set<String>> entry : this.working.getCustomPresets().entrySet()) {
            this.customPresets.put(entry.getKey(), new LinkedHashSet<>(entry.getValue()));
        }
        if (!this.working.defaultPresetsInitialized) {
            ensureBuiltInPresets(this.customPresets);
            this.working.defaultPresetsInitialized = true;
            persistConfig();
        }
        this.activePresetName = resolvePresetName(this.workingAllowedItemIds);
        this.allItems = collectMinecraftItems();
    }

    @Override
    protected void init() {
        this.leftRemoveButtons.clear();
        this.rightAddButtons.clear();
        this.customUseButtons.clear();
        this.customEditButtons.clear();
        this.customDeleteButtons.clear();

        int maxPanelWidth = Math.max(320, this.width - PANEL_MARGIN * 2);
        int maxPanelHeight = Math.max(260, this.height - PANEL_MARGIN * 2);
        int panelWidth = Math.min(PANEL_TARGET_WIDTH, maxPanelWidth);
        int panelHeight = Math.min(PANEL_TARGET_HEIGHT, maxPanelHeight);
        this.panelLeft = (this.width - panelWidth) / 2;
        this.panelRight = this.panelLeft + panelWidth;
        this.panelTop = (this.height - panelHeight) / 2;
        this.panelBottom = this.panelTop + panelHeight;

        int listAreaWidth = this.panelRight - this.panelLeft - PANEL_MARGIN * 2;
        int listWidth = (listAreaWidth - LIST_GAP) / 2;
        this.leftListLeft = this.panelLeft + PANEL_MARGIN;
        this.leftListRight = this.leftListLeft + listWidth;
        this.rightListLeft = this.leftListRight + LIST_GAP;
        this.rightListRight = this.panelRight - PANEL_MARGIN;

        this.titleY = this.panelTop + 8;
        this.subtitleY = this.titleY + this.textRenderer.fontHeight + 2;
        this.tabY = this.subtitleY + this.textRenderer.fontHeight + 6;
        this.toggleY = this.tabY + TAB_BAR_HEIGHT + 6;
        this.actionY = this.toggleY + 30;
        this.searchY = this.actionY + 22;

        this.listTop = this.searchY + SEARCH_FIELD_Y_OFFSET + INPUT_FRAME_HEIGHT + LIST_TOP_GAP;
        this.listBottom = this.panelBottom - 34;
        this.listBodyTop = this.listTop + LIST_HEADER_HEIGHT;
        this.visibleRows = Math.max(1, (this.listBottom - this.listBodyTop - 2) / ROW_HEIGHT);

        this.leftRowMap = new int[this.visibleRows];
        this.rightRowMap = new int[this.visibleRows];
        this.customRowMap = new int[this.visibleRows];

        initHeaderWidgets();
        initRowButtons();
        updateTabAnimationTargets(true);

        updateTabButtons();
        updateTabVisibility();
        updateSearchSuggestions();
        updateWhitelistToggleLabel();
        refreshViewData();
        verifyCurrentLayout("init-pass-1");
        verifyCurrentLayout("init-pass-2");
    }

    private void initHeaderWidgets() {
        int tabGap = 6;
        int availableTabsWidth = this.rightListRight - this.leftListLeft;
        int tabWidth = Math.max(84, (availableTabsWidth - tabGap * 3) / 4);
        int tab4Width = availableTabsWidth - tabWidth * 3 - tabGap * 3;
        int columnWidth = this.leftListRight - this.leftListLeft;
        int searchFieldY = this.searchY + SEARCH_FIELD_Y_OFFSET + INPUT_FRAME_PAD_Y;
        int actionFieldY = this.actionY + SEARCH_FIELD_Y_OFFSET + INPUT_FRAME_PAD_Y;
        int searchFieldWidth = Math.max(40, columnWidth - INPUT_FRAME_PAD_X * 2);
        int customSearchFieldWidth = Math.max(40, (this.rightListRight - this.leftListLeft) - INPUT_FRAME_PAD_X * 2);
        int actionRowWidth = Math.max(220, (this.rightListRight - this.leftListLeft) - INPUT_FRAME_PAD_X * 2);
        int actionGap = 8;
        int actionButtonGap = 6;
        int minActionFieldWidth = 96;
        int minActionButtonWidth = 68;
        int maxActionButtonWidth = 120;
        int availableForButtons = actionRowWidth - minActionFieldWidth - actionGap - actionButtonGap;
        int actionButtonWidth = MathHelper.clamp(availableForButtons / 2, minActionButtonWidth, maxActionButtonWidth);
        int actionFieldWidth = Math.max(minActionFieldWidth,
                actionRowWidth - actionGap - actionButtonGap - actionButtonWidth * 2);
        int actionFieldX = this.leftListLeft + INPUT_FRAME_PAD_X;
        int saveButtonX = actionFieldX + actionFieldWidth + actionGap;
        int resetButtonX = saveButtonX + actionButtonWidth + actionButtonGap;

        int toggleWidth = Math.min(210, Math.max(170, availableTabsWidth / 3));
        int toggleX = this.rightListRight - toggleWidth;
        this.defaultToggleX = toggleX;
        this.defaultToggleY = this.toggleY;
        this.defaultToggleWidth = toggleWidth;

        this.tabWhitelistButton = addDrawableChild(createNeonButton(
                this.leftListLeft,
                this.tabY,
                tabWidth,
                20,
                Text.empty(),
                b -> switchTab(Tab.WHITELIST)));
        this.tabCreatePresetButton = addDrawableChild(createNeonButton(
                this.leftListLeft + tabWidth + tabGap,
                this.tabY,
                tabWidth,
                20,
                Text.empty(),
                b -> switchTab(Tab.CREATE_PRESET)));
        this.tabCustomPresetsButton = addDrawableChild(createNeonButton(
                this.leftListLeft + (tabWidth + tabGap) * 2,
                this.tabY,
                tabWidth,
                20,
                Text.empty(),
                b -> switchTab(Tab.CUSTOM_PRESETS)));
        this.tabReportIssuesButton = addDrawableChild(createNeonButton(
                this.leftListLeft + (tabWidth + tabGap) * 3,
                this.tabY,
                tab4Width,
                20,
                Text.empty(),
                b -> switchTab(Tab.REPORT_ISSUES)));
        configureTabButtonVisual(this.tabWhitelistButton);
        configureTabButtonVisual(this.tabCreatePresetButton);
        configureTabButtonVisual(this.tabCustomPresetsButton);
        configureTabButtonVisual(this.tabReportIssuesButton);

        this.whitelistToggleButton = addDrawableChild(createNeonButton(
                toggleX,
                this.toggleY,
                toggleWidth,
                20,
                Text.empty(),
                b -> {
            this.working.offhandWhitelistEnabled = !this.working.offhandWhitelistEnabled;
            updateWhitelistToggleLabel();
            updateWhitelistToggleLayout();
            updateTabVisibility();
            refreshViewData();
            persistConfig();
        }));

        this.leftSearchField = addDrawableChild(new TextFieldWidget(
                this.textRenderer,
                this.leftListLeft + INPUT_FRAME_PAD_X,
                searchFieldY,
                searchFieldWidth,
                INPUT_TEXTBOX_HEIGHT,
                Text.literal("Search whitelist")));
        this.leftSearchField.setMaxLength(64);
        styleInputField(this.leftSearchField);
        this.leftSearchField.setChangedListener(value -> {
            updateSearchSuggestions();
            refreshViewData();
        });

        this.rightSearchField = addDrawableChild(new TextFieldWidget(
                this.textRenderer,
                this.rightListLeft + INPUT_FRAME_PAD_X,
                searchFieldY,
                searchFieldWidth,
                INPUT_TEXTBOX_HEIGHT,
                Text.literal("Search non-whitelist")));
        this.rightSearchField.setMaxLength(64);
        styleInputField(this.rightSearchField);
        this.rightSearchField.setChangedListener(value -> {
            updateSearchSuggestions();
            refreshViewData();
        });

        this.customSearchField = addDrawableChild(new TextFieldWidget(
                this.textRenderer,
                this.leftListLeft + INPUT_FRAME_PAD_X,
                searchFieldY,
                customSearchFieldWidth,
                INPUT_TEXTBOX_HEIGHT,
                Text.literal("Search presets")));
        this.customSearchField.setMaxLength(64);
        styleInputField(this.customSearchField);
        this.customSearchField.setChangedListener(value -> {
            updateSearchSuggestions();
            refreshViewData();
        });

        this.presetNameField = addDrawableChild(new TextFieldWidget(
                this.textRenderer,
                actionFieldX,
                actionFieldY,
                actionFieldWidth,
                INPUT_TEXTBOX_HEIGHT,
                Text.literal("Preset name")));
        this.presetNameField.setMaxLength(48);
        styleInputField(this.presetNameField);

        this.savePresetButton = addDrawableChild(createNeonButton(
                saveButtonX,
                this.actionY + 1,
                actionButtonWidth,
                20,
                Text.literal("Save Preset"),
                b -> saveOrUpdateCustomPreset()));
        this.clearPresetSelectionButton = addDrawableChild(createNeonButton(
                resetButtonX,
                this.actionY + 1,
                actionButtonWidth,
                20,
                Text.literal("Reset Builder"),
                b -> clearPresetBuilder()));

        this.openDiscordButton = addDrawableChild(createNeonButton(
                this.leftListLeft,
                this.panelBottom - 64,
                REPORT_DISCORD_BUTTON_WIDTH,
                20,
                Text.literal("Open Discord Support"),
                b -> {
            if (this.client != null) {
                this.client.keyboard.setClipboard(DISCORD_SUPPORT_URL);
            }
            Util.getOperatingSystem().open(DISCORD_SUPPORT_URL);
        }));
    }

    private void initRowButtons() {
        for (int row = 0; row < this.visibleRows; row++) {
            int rowY = this.listBodyTop + row * ROW_HEIGHT + 2;
            final int rowIndex = row;

            ButtonWidget removeButton = addDrawableChild(createNeonButton(
                    this.leftListRight - ROW_BUTTON_SIZE - 4,
                    rowY,
                    ROW_BUTTON_SIZE,
                    ROW_BUTTON_SIZE,
                    Text.literal("-"),
                    b -> removeFromLeftRow(rowIndex)));
            this.leftRemoveButtons.add(removeButton);

            ButtonWidget addButton = addDrawableChild(createNeonButton(
                    this.rightListRight - ROW_BUTTON_SIZE - 4,
                    rowY,
                    ROW_BUTTON_SIZE,
                    ROW_BUTTON_SIZE,
                    Text.literal("+"),
                    b -> addFromRightRow(rowIndex)));
            this.rightAddButtons.add(addButton);

            ButtonWidget useButton = addDrawableChild(createNeonButton(
                    this.rightListRight - 168,
                    rowY,
                    52,
                    ROW_BUTTON_SIZE,
                    Text.literal("Use"),
                    b -> toggleCustomPresetUse(rowIndex)));
            this.customUseButtons.add(useButton);

            ButtonWidget editButton = addDrawableChild(createNeonButton(
                    this.rightListRight - 112,
                    rowY,
                    50,
                    ROW_BUTTON_SIZE,
                    Text.literal("Edit"),
                    b -> editCustomPreset(rowIndex)));
            this.customEditButtons.add(editButton);

            ButtonWidget deleteButton = addDrawableChild(createNeonButton(
                    this.rightListRight - 56,
                    rowY,
                    52,
                    ROW_BUTTON_SIZE,
                    Text.literal("Delete"),
                    b -> deleteCustomPreset(rowIndex)));
            this.customDeleteButtons.add(deleteButton);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int delta = verticalAmount < 0 ? 1 : verticalAmount > 0 ? -1 : 0;
        if (delta == 0) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        if (!this.working.offhandWhitelistEnabled) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        if (this.activeTab == Tab.CUSTOM_PRESETS) {
            if (isInsideList(mouseX, mouseY, this.leftListLeft, this.rightListRight)) {
                this.customScrollOffset = MathHelper.clamp(this.customScrollOffset + delta, 0, this.maxCustomScrollOffset);
                bindCustomButtons();
                return true;
            }
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        if (this.activeTab == Tab.REPORT_ISSUES) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        if (isInsideList(mouseX, mouseY, this.leftListLeft, this.leftListRight)) {
            this.leftScrollOffset = MathHelper.clamp(this.leftScrollOffset + delta, 0, this.maxLeftScrollOffset);
            bindItemButtons();
            return true;
        }
        if (isInsideList(mouseX, mouseY, this.rightListLeft, this.rightListRight)) {
            this.rightScrollOffset = MathHelper.clamp(this.rightScrollOffset + delta, 0, this.maxRightScrollOffset);
            bindItemButtons();
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float pulse = 0.58F + 0.42F * (float) Math.sin((Util.getMeasuringTimeMs() - this.screenOpenedAtMs) / 420.0D);
        updateTabAnimation();
        boolean whitelistLocked = !this.working.offhandWhitelistEnabled;

        context.fillGradient(0, 0, this.width, this.height, COLOR_BG_TOP, COLOR_BG_BOTTOM);
        renderFrameGlow(context, this.panelLeft, this.panelTop, this.panelRight, this.panelBottom, 0.82F + pulse * 0.18F);
        context.fill(this.panelLeft, this.panelTop, this.panelRight, this.panelBottom, COLOR_PANEL_FILL);
        drawOutline(context, this.panelLeft, this.panelTop, this.panelRight, this.panelBottom, COLOR_PANEL_EDGE);

        context.drawTextWithShadow(this.textRenderer, this.title, this.panelLeft + PANEL_MARGIN, this.titleY, COLOR_TEXT_MAIN);
        context.drawTextWithShadow(this.textRenderer, Text.literal("Status: " + getPresetStatusText()),
                this.panelLeft + PANEL_MARGIN, this.subtitleY, COLOR_TEXT_SUB);
        renderTabNavigation(context, mouseX, mouseY);
        renderTextFieldFrames(context);

        if (whitelistLocked) {
            renderWhitelistLockedPanel(context);
        } else if (this.activeTab == Tab.CUSTOM_PRESETS) {
            renderCustomPresetPanel(context);
        } else if (this.activeTab == Tab.REPORT_ISSUES) {
            renderReportIssuesPanel(context, mouseX, mouseY);
        } else {
            renderItemPanels(context);
        }

        if (!whitelistLocked && this.activeTab != Tab.REPORT_ISSUES) {
            renderBottomStatus(context);
        }
        renderInteractiveButtons(context, mouseX, mouseY);

        super.render(context, mouseX, mouseY, delta);
        renderPageTransition(context);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Disable vanilla background blur/darkening for this custom screen.
    }

    private void renderItemPanels(DrawContext context) {
        String leftHeader = this.activeTab == Tab.WHITELIST ? "Whitelist" : "Preset Items";
        String rightHeader = this.activeTab == Tab.WHITELIST ? "Non-Whitelist" : "Available Items";

        context.fill(this.leftListLeft, this.listTop, this.leftListRight, this.listBottom, COLOR_SECTION_FILL);
        context.fill(this.rightListLeft, this.listTop, this.rightListRight, this.listBottom, COLOR_SECTION_FILL);
        context.fill(this.leftListLeft, this.listTop, this.leftListRight, this.listTop + LIST_HEADER_HEIGHT, COLOR_SECTION_HEADER);
        context.fill(this.rightListLeft, this.listTop, this.rightListRight, this.listTop + LIST_HEADER_HEIGHT, COLOR_SECTION_HEADER);
        drawOutline(context, this.leftListLeft, this.listTop, this.leftListRight, this.listBottom, COLOR_PANEL_EDGE_SOFT);
        drawOutline(context, this.rightListLeft, this.listTop, this.rightListRight, this.listBottom, COLOR_PANEL_EDGE_SOFT);

        context.drawTextWithShadow(this.textRenderer, Text.literal(leftHeader + " (" + this.leftItems.size() + ")"),
                this.leftListLeft + 6, this.listTop + 6, COLOR_TEXT_SUB);
        context.drawTextWithShadow(this.textRenderer, Text.literal(rightHeader + " (" + this.rightItems.size() + ")"),
                this.rightListLeft + 6, this.listTop + 6, COLOR_TEXT_SUB);

        renderItemRows(context, this.leftListLeft, this.leftListRight, this.leftItems, this.leftRowMap, true);
        renderItemRows(context, this.rightListLeft, this.rightListRight, this.rightItems, this.rightRowMap, false);
    }

    private void renderCustomPresetPanel(DrawContext context) {
        context.fill(this.leftListLeft, this.listTop, this.rightListRight, this.listBottom, COLOR_SECTION_FILL);
        context.fill(this.leftListLeft, this.listTop, this.rightListRight, this.listTop + LIST_HEADER_HEIGHT, COLOR_SECTION_HEADER);
        drawOutline(context, this.leftListLeft, this.listTop, this.rightListRight, this.listBottom, COLOR_PANEL_EDGE_SOFT);
        context.drawTextWithShadow(this.textRenderer, Text.literal("Custom Presets (" + this.customPresetItems.size() + ")"),
                this.leftListLeft + 6, this.listTop + 6, COLOR_TEXT_SUB);
        context.drawTextWithShadow(this.textRenderer, Text.literal("Use/Unset button applies the preset"),
                this.rightListRight - 190, this.listTop + 6, COLOR_TEXT_DIM);

        int textMaxWidth = this.rightListRight - this.leftListLeft - 182;
        for (int row = 0; row < this.visibleRows; row++) {
            int idx = this.customRowMap[row];
            if (idx < 0 || idx >= this.customPresetItems.size()) {
                continue;
            }

            CustomPresetEntry entry = this.customPresetItems.get(idx);
            int rowY = this.listBodyTop + row * ROW_HEIGHT;
            boolean activePreset = entry.name().equals(this.activePresetName);
            context.fill(this.leftListLeft + 1, rowY, this.rightListRight - 1, rowY + ROW_HEIGHT - 1,
                    activePreset ? COLOR_ROW_ACTIVE_PRESET : (row % 2 == 0 ? COLOR_ROW_A : COLOR_ROW_B));

            String activeTag = activePreset ? " | Active" : "";
            String label = entry.name() + " (" + entry.itemIds().size() + " items" + activeTag + ")";
            context.drawTextWithShadow(this.textRenderer,
                    Text.literal(this.textRenderer.trimToWidth(label, textMaxWidth)),
                    this.leftListLeft + 8,
                    rowY + 7,
                    activePreset ? 0xFFB9FFB9 : COLOR_TEXT_MAIN);
        }
    }

    private void renderReportIssuesPanel(DrawContext context, int mouseX, int mouseY) {
        int top = this.toggleY + 26;
        int bottom = this.panelBottom - 40;
        int buttonBandHeight = 30;
        int gap = LIST_GAP;
        int contentWidth = this.rightListRight - this.leftListLeft;
        int imageWidth = Math.max(220, (contentWidth - gap) / 2);
        int imageLeft = this.leftListLeft;
        int imageRight = Math.min(this.rightListRight, imageLeft + imageWidth);
        int imageBottom = Math.max(top + 20, bottom - buttonBandHeight);
        int textLeft = imageRight + gap;
        int textRight = this.rightListRight;

        context.fill(textLeft, top, textRight, bottom, COLOR_SECTION_FILL);
        drawOutline(context, textLeft, top, textRight, bottom, COLOR_PANEL_EDGE_SOFT);

        renderReportImage(context, imageLeft, top, imageRight, imageBottom);

        int buttonX = imageLeft + Math.max(2, (imageRight - imageLeft - REPORT_DISCORD_BUTTON_WIDTH) / 2);
        int buttonY = imageBottom + Math.max(2, (buttonBandHeight - 20) / 2);
        this.openDiscordButton.setPosition(buttonX, buttonY);

        int textY = top + 10;
        int maxTextWidth = Math.max(120, textRight - textLeft - 14);
        textY = drawWrappedText(context,
                "KoHs Mod Suite was created by zymekoh.",
                textLeft + 7, textY, maxTextWidth, COLOR_TEXT_MAIN);
        textY = drawWrappedText(context,
                "For support or help creating mods/plugins, contact me using the Open Discord Support button.",
                textLeft + 7, textY + 2, maxTextWidth, COLOR_TEXT_SUB);
    }

    private void renderItemRows(
            DrawContext context,
            int listLeft,
            int listRight,
            List<ItemEntry> items,
            int[] rowMap,
            boolean leftPanel) {
        int nameStartX = listLeft + 24;
        int maxNameWidth = Math.max(80, listRight - listLeft - 48);

        for (int row = 0; row < this.visibleRows; row++) {
            int idx = rowMap[row];
            if (idx < 0 || idx >= items.size()) {
                continue;
            }

            ItemEntry entry = items.get(idx);
            int rowY = this.listBodyTop + row * ROW_HEIGHT;
            context.fill(listLeft + 1, rowY, listRight - 1, rowY + ROW_HEIGHT - 1,
                    row % 2 == 0 ? COLOR_ROW_A : COLOR_ROW_B);
            drawItemSafe(context, entry.previewStack(), listLeft + 4, rowY + 3);
            context.drawTextWithShadow(this.textRenderer,
                    Text.literal(this.textRenderer.trimToWidth(entry.displayName(), maxNameWidth)),
                    nameStartX,
                    rowY + 7,
                    leftPanel ? COLOR_TEXT_SUB : COLOR_TEXT_MAIN);
        }
    }

    private void renderBottomStatus(DrawContext context) {
        String statusText = "Whitelist items: "
                + this.workingAllowedItemIds.size()
                + " | Presets: "
                + this.customPresets.size()
                + " | Active tab: "
                + this.activeTab.title;
        int textWidth = this.textRenderer.getWidth(statusText);
        int boxWidth = Math.min(this.panelRight - this.panelLeft - PANEL_MARGIN * 2, textWidth + 16);
        int boxHeight = 18;
        int boxLeft = this.panelLeft + (this.panelRight - this.panelLeft - boxWidth) / 2;
        int boxTop = this.panelBottom - boxHeight - 8;
        int boxRight = boxLeft + boxWidth;
        int boxBottom = boxTop + boxHeight;
        int textX = boxLeft + (boxWidth - textWidth) / 2;
        int textY = boxTop + (boxHeight - this.textRenderer.fontHeight) / 2;

        context.fill(boxLeft, boxTop, boxRight, boxBottom, 0x4A1B1330);
        drawOutline(context, boxLeft, boxTop, boxRight, boxBottom, COLOR_PANEL_EDGE_SOFT);
        context.drawTextWithShadow(this.textRenderer, Text.literal(statusText), textX, textY, COLOR_TEXT_DIM);
    }

    private void drawItemSafe(DrawContext context, ItemStack stack, int x, int y) {
        try {
            context.drawItem(stack, x, y);
        } catch (Throwable throwable) {
            Identifier itemId = Registries.ITEM.getId(stack.getItem());
            String itemKey = itemId == null ? "<unknown>" : itemId.toString();
            if (this.itemRenderWarningIds.add(itemKey)) {
                KoHsOffhandWhitelist.LOGGER.warn(
                        "[{}] Skipping item icon render for '{}' due to external renderer error: {}",
                        KoHsOffhandWhitelist.MOD_ID,
                        itemKey,
                        throwable.toString());
            }
            context.fill(x, y, x + 16, y + 16, 0x7F2A1A3F);
            drawOutline(context, x, y, x + 16, y + 16, COLOR_PANEL_EDGE_SOFT);
        }
    }

    private void switchTab(Tab tab) {
        if (this.activeTab == tab) {
            return;
        }
        int direction = Integer.compare(tab.ordinal(), this.activeTab.ordinal());
        this.pageTransitionDirection = direction == 0 ? 1 : direction;
        this.tabTransitionStartMs = Util.getMeasuringTimeMs();
        this.pageTransitionStartMs = this.tabTransitionStartMs;
        this.activeTab = tab;
        this.leftScrollOffset = 0;
        this.rightScrollOffset = 0;
        this.customScrollOffset = 0;
        updateTabButtons();
        updateTabAnimationTargets(false);
        updateTabVisibility();
        updateSearchSuggestions();
        refreshViewData();
        verifyCurrentLayout("switch-pass-1");
        verifyCurrentLayout("switch-pass-2");
    }

    private void updateTabButtons() {
        this.tabWhitelistButton.setMessage(Text.literal(Tab.WHITELIST.title));
        this.tabCreatePresetButton.setMessage(Text.literal(Tab.CREATE_PRESET.title));
        this.tabCustomPresetsButton.setMessage(Text.literal(Tab.CUSTOM_PRESETS.title));
        this.tabReportIssuesButton.setMessage(Text.literal(Tab.REPORT_ISSUES.title));
    }

    private void configureTabButtonVisual(ButtonWidget button) {
        if (button != null) {
            button.setAlpha(0.0F);
        }
    }

    private ButtonWidget getTabButton(Tab tab) {
        return switch (tab) {
            case WHITELIST -> this.tabWhitelistButton;
            case CREATE_PRESET -> this.tabCreatePresetButton;
            case CUSTOM_PRESETS -> this.tabCustomPresetsButton;
            case REPORT_ISSUES -> this.tabReportIssuesButton;
        };
    }

    private void updateTabAnimationTargets(boolean snapToTarget) {
        ButtonWidget activeButton = getTabButton(this.activeTab);
        if (activeButton == null) {
            return;
        }

        this.tabIndicatorTargetLeft = activeButton.getX() + 2.0F;
        this.tabIndicatorTargetRight = activeButton.getRight() - 2.0F;
        if (snapToTarget || this.animatedTabIndicatorRight <= this.animatedTabIndicatorLeft) {
            this.animatedTabIndicatorLeft = this.tabIndicatorTargetLeft;
            this.animatedTabIndicatorRight = this.tabIndicatorTargetRight;
        }
    }

    private void updateTabAnimation() {
        long now = Util.getMeasuringTimeMs();
        float lerpFactor = this.lastTabAnimationFrameMs == 0
                ? 1.0F
                : MathHelper.clamp((now - this.lastTabAnimationFrameMs) / 90.0F, 0.08F, 1.0F);
        this.lastTabAnimationFrameMs = now;

        this.animatedTabIndicatorLeft = MathHelper.lerp(lerpFactor, this.animatedTabIndicatorLeft, this.tabIndicatorTargetLeft);
        this.animatedTabIndicatorRight = MathHelper.lerp(lerpFactor, this.animatedTabIndicatorRight, this.tabIndicatorTargetRight);
    }

    private void renderTabNavigation(DrawContext context, int mouseX, int mouseY) {
        int barLeft = this.leftListLeft - 2;
        int barRight = this.rightListRight + 2;
        int barTop = this.tabY - 6;
        int barBottom = this.tabY + TAB_BAR_HEIGHT - 2;

        context.fill(barLeft, barTop, barRight, barBottom, COLOR_TAB_BAR_FILL);
        drawOutline(context, barLeft, barTop, barRight, barBottom, COLOR_PANEL_EDGE_SOFT);

        int indicatorLeft = Math.round(this.animatedTabIndicatorLeft);
        int indicatorRight = Math.round(this.animatedTabIndicatorRight);
        int indicatorTop = barBottom - 4;
        int indicatorBottom = barBottom - 1;
        context.fill(indicatorLeft, indicatorTop, indicatorRight, indicatorBottom, 0xFFC07DFF);

        float transitionPulse = 1.0F - MathHelper.clamp(
                (Util.getMeasuringTimeMs() - this.tabTransitionStartMs) / (float) TAB_ANIMATION_DURATION_MS,
                0.0F,
                1.0F);
        if (transitionPulse > 0.0F) {
            renderFrameGlow(context, indicatorLeft - 1, barTop + 1, indicatorRight + 1, barBottom - 1, 0.2F + transitionPulse * 0.45F);
        }

        for (Tab tab : Tab.values()) {
            ButtonWidget button = getTabButton(tab);
            if (button == null) {
                continue;
            }

            int x1 = button.getX();
            int y1 = button.getY();
            int x2 = button.getRight();
            int y2 = button.getBottom();
            boolean active = this.activeTab == tab;
            boolean hovered = button.isMouseOver(mouseX, mouseY);
            int fillColor = active ? COLOR_TAB_ACTIVE : hovered ? COLOR_TAB_HOVER : COLOR_TAB_IDLE;
            int borderColor = active ? COLOR_PANEL_EDGE : COLOR_PANEL_EDGE_SOFT;

            context.fill(x1, y1, x2, y2, fillColor);
            drawOutline(context, x1, y1, x2, y2, borderColor);
            if (active) {
                renderFrameGlow(context, x1, y1, x2, y2, 0.28F + transitionPulse * 0.20F);
            }

            int textColor = active ? COLOR_TEXT_MAIN : hovered ? COLOR_TEXT_SUB : COLOR_TEXT_DIM;
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(tab.title), (x1 + x2) / 2, y1 + 6, textColor);
        }
    }

    private void renderInteractiveButtons(DrawContext context, int mouseX, int mouseY) {
        renderStyledActionButton(context, this.whitelistToggleButton, mouseX, mouseY, this.working.offhandWhitelistEnabled);
        renderStyledActionButton(context, this.savePresetButton, mouseX, mouseY, false);
        renderStyledActionButton(context, this.clearPresetSelectionButton, mouseX, mouseY, false);
        renderStyledActionButton(context, this.openDiscordButton, mouseX, mouseY, false);

        for (ButtonWidget button : this.leftRemoveButtons) {
            renderStyledActionButton(context, button, mouseX, mouseY, false);
        }
        for (ButtonWidget button : this.rightAddButtons) {
            renderStyledActionButton(context, button, mouseX, mouseY, false);
        }
        for (ButtonWidget button : this.customUseButtons) {
            boolean toggled = button != null && "Unset".equalsIgnoreCase(button.getMessage().getString());
            renderStyledActionButton(context, button, mouseX, mouseY, toggled);
        }
        for (ButtonWidget button : this.customEditButtons) {
            renderStyledActionButton(context, button, mouseX, mouseY, false);
        }
        for (ButtonWidget button : this.customDeleteButtons) {
            renderStyledActionButton(context, button, mouseX, mouseY, false);
        }
    }

    private void renderStyledActionButton(
            DrawContext context,
            ButtonWidget button,
            int mouseX,
            int mouseY,
            boolean toggled) {
        if (button == null || !button.visible) {
            return;
        }

        int x1 = button.getX();
        int y1 = button.getY();
        int x2 = button.getRight();
        int y2 = button.getBottom();
        boolean hovered = button.isMouseOver(mouseX, mouseY);
        boolean enabled = button.active;

        int fillColor;
        int borderColor;
        int textColor;
        if (!enabled) {
            fillColor = 0x2A1A122C;
            borderColor = 0x5C3A2B70;
            textColor = 0xAA8E7FA8;
        } else if (toggled) {
            fillColor = COLOR_TAB_ACTIVE;
            borderColor = COLOR_PANEL_EDGE;
            textColor = COLOR_TEXT_MAIN;
        } else if (hovered) {
            fillColor = COLOR_TAB_HOVER;
            borderColor = COLOR_PANEL_EDGE_SOFT;
            textColor = COLOR_TEXT_SUB;
        } else {
            fillColor = COLOR_TAB_IDLE;
            borderColor = COLOR_PANEL_EDGE_SOFT;
            textColor = COLOR_TEXT_DIM;
        }

        context.fill(x1, y1, x2, y2, fillColor);
        drawOutline(context, x1, y1, x2, y2, borderColor);
        if (enabled && (hovered || toggled)) {
            renderFrameGlow(context, x1, y1, x2, y2, hovered ? 0.26F : 0.34F);
        }

        String label = button.getMessage().getString();
        int textY = y1 + Math.max(1, (y2 - y1 - this.textRenderer.fontHeight) / 2);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(label), (x1 + x2) / 2, textY, textColor);
    }

    private void renderPageTransition(DrawContext context) {
        if (this.pageTransitionStartMs <= 0L) {
            return;
        }

        long elapsed = Util.getMeasuringTimeMs() - this.pageTransitionStartMs;
        if (elapsed >= PAGE_TRANSITION_DURATION_MS) {
            return;
        }

        float progress = MathHelper.clamp(elapsed / (float) PAGE_TRANSITION_DURATION_MS, 0.0F, 1.0F);
        float eased = 1.0F - (float) Math.pow(1.0F - progress, 2.6D);

        int left = this.leftListLeft;
        int right = this.rightListRight;
        int top = this.toggleY + 18;
        int bottom = this.panelBottom - 8;
        if (right <= left || bottom <= top) {
            return;
        }

        int fadeAlpha = MathHelper.clamp((int) ((1.0F - progress) * 56.0F), 0, 255);
        if (fadeAlpha > 0) {
            context.fill(left, top, right, bottom, (fadeAlpha << 24) | 0x00120A24);
        }

        int sweepRadius = Math.max(72, (right - left) / 5);
        int startX = this.pageTransitionDirection >= 0 ? left - sweepRadius : right + sweepRadius;
        int endX = this.pageTransitionDirection >= 0 ? right + sweepRadius : left - sweepRadius;
        int centerX = Math.round(MathHelper.lerp(eased, startX, endX));

        for (int layer = 0; layer < 4; layer++) {
            int alpha = MathHelper.clamp((int) ((1.0F - progress) * (48.0F - layer * 10.0F)), 0, 255);
            if (alpha <= 0) {
                continue;
            }
            int halfWidth = sweepRadius + layer * 26;
            context.fill(centerX - halfWidth, top, centerX + halfWidth, bottom, (alpha << 24) | 0x00B07BD5);
        }
    }

    private void updateTabVisibility() {
        boolean whitelistTab = this.activeTab == Tab.WHITELIST;
        boolean createPresetTab = this.activeTab == Tab.CREATE_PRESET;
        boolean customTab = this.activeTab == Tab.CUSTOM_PRESETS;
        boolean reportTab = this.activeTab == Tab.REPORT_ISSUES;
        boolean whitelistLocked = !this.working.offhandWhitelistEnabled;

        this.whitelistToggleButton.visible = whitelistTab || whitelistLocked;
        updateWhitelistToggleLayout();

        this.leftSearchField.visible = !whitelistLocked && !customTab && !reportTab;
        this.rightSearchField.visible = !whitelistLocked && !customTab && !reportTab;
        this.customSearchField.visible = !whitelistLocked && customTab;

        this.presetNameField.visible = !whitelistLocked && createPresetTab;
        this.savePresetButton.visible = !whitelistLocked && createPresetTab;
        this.clearPresetSelectionButton.visible = !whitelistLocked && createPresetTab;
        this.openDiscordButton.visible = !whitelistLocked && reportTab;

        for (ButtonWidget button : this.leftRemoveButtons) {
            button.visible = !whitelistLocked && !customTab && !reportTab;
        }
        for (ButtonWidget button : this.rightAddButtons) {
            button.visible = !whitelistLocked && !customTab && !reportTab;
        }
        for (ButtonWidget button : this.customUseButtons) {
            button.visible = !whitelistLocked && customTab;
        }
        for (ButtonWidget button : this.customEditButtons) {
            button.visible = !whitelistLocked && customTab;
        }
        for (ButtonWidget button : this.customDeleteButtons) {
            button.visible = !whitelistLocked && customTab;
        }

        verifyCurrentLayout("visibility-pass-1");
        verifyCurrentLayout("visibility-pass-2");
    }

    private void updateSearchSuggestions() {
        if (this.leftSearchField != null) {
            this.leftSearchField.setSuggestion(this.leftSearchField.getText().isEmpty() ? "Search in whitelist" : "");
        }
        if (this.rightSearchField != null) {
            this.rightSearchField.setSuggestion(this.rightSearchField.getText().isEmpty() ? "Search in non-whitelist" : "");
        }
        if (this.customSearchField != null) {
            this.customSearchField.setSuggestion(this.customSearchField.getText().isEmpty() ? "Search preset" : "");
        }
    }

    private void refreshViewData() {
        String customQuery = this.customSearchField == null
                ? ""
                : this.customSearchField.getText().trim().toLowerCase(Locale.ROOT);
        String leftQuery = this.leftSearchField == null
                ? ""
                : this.leftSearchField.getText().trim().toLowerCase(Locale.ROOT);
        String rightQuery = this.rightSearchField == null
                ? ""
                : this.rightSearchField.getText().trim().toLowerCase(Locale.ROOT);

        if (this.activeTab == Tab.CUSTOM_PRESETS) {
            this.customPresetItems = this.customPresets.entrySet().stream()
                    .map(entry -> new CustomPresetEntry(entry.getKey(), new LinkedHashSet<>(entry.getValue())))
                    .filter(entry -> customQuery.isEmpty() || entry.name().toLowerCase(Locale.ROOT).contains(customQuery))
                    .toList();
            this.maxCustomScrollOffset = Math.max(0, this.customPresetItems.size() - this.visibleRows);
            this.customScrollOffset = MathHelper.clamp(this.customScrollOffset, 0, this.maxCustomScrollOffset);
            bindCustomButtons();
            return;
        }
        if (this.activeTab == Tab.REPORT_ISSUES) {
            return;
        }

        LinkedHashSet<String> selected = this.activeTab == Tab.WHITELIST
                ? this.workingAllowedItemIds
                : this.presetBuilderItemIds;

        List<ItemEntry> leftFiltered = leftQuery.isEmpty()
                ? this.allItems
                : this.allItems.stream().filter(entry -> entry.searchKey().contains(leftQuery)).toList();
        List<ItemEntry> rightFiltered = rightQuery.isEmpty()
                ? this.allItems
                : this.allItems.stream().filter(entry -> entry.searchKey().contains(rightQuery)).toList();

        this.leftItems = leftFiltered.stream().filter(entry -> selected.contains(entry.idString())).toList();
        this.rightItems = rightFiltered.stream().filter(entry -> !selected.contains(entry.idString())).toList();

        this.maxLeftScrollOffset = Math.max(0, this.leftItems.size() - this.visibleRows);
        this.maxRightScrollOffset = Math.max(0, this.rightItems.size() - this.visibleRows);
        this.leftScrollOffset = MathHelper.clamp(this.leftScrollOffset, 0, this.maxLeftScrollOffset);
        this.rightScrollOffset = MathHelper.clamp(this.rightScrollOffset, 0, this.maxRightScrollOffset);

        bindItemButtons();
    }

    private void bindItemButtons() {
        boolean whitelistLocked = !this.working.offhandWhitelistEnabled;
        for (int row = 0; row < this.visibleRows; row++) {
            int leftIndex = this.leftScrollOffset + row;
            boolean hasLeft = leftIndex >= 0 && leftIndex < this.leftItems.size();
            this.leftRowMap[row] = hasLeft ? leftIndex : -1;

            ButtonWidget removeButton = this.leftRemoveButtons.get(row);
            removeButton.visible = !whitelistLocked && this.activeTab != Tab.CUSTOM_PRESETS && hasLeft;
            removeButton.active = hasLeft;

            int rightIndex = this.rightScrollOffset + row;
            boolean hasRight = rightIndex >= 0 && rightIndex < this.rightItems.size();
            this.rightRowMap[row] = hasRight ? rightIndex : -1;

            ButtonWidget addButton = this.rightAddButtons.get(row);
            addButton.visible = !whitelistLocked && this.activeTab != Tab.CUSTOM_PRESETS && hasRight;
            addButton.active = hasRight;
        }
    }

    private void bindCustomButtons() {
        boolean whitelistLocked = !this.working.offhandWhitelistEnabled;
        for (int row = 0; row < this.visibleRows; row++) {
            int idx = this.customScrollOffset + row;
            boolean hasEntry = idx >= 0 && idx < this.customPresetItems.size();
            this.customRowMap[row] = hasEntry ? idx : -1;
            boolean isActive = hasEntry && this.customPresetItems.get(idx).name().equals(this.activePresetName);

            ButtonWidget useButton = this.customUseButtons.get(row);
            useButton.visible = !whitelistLocked && this.activeTab == Tab.CUSTOM_PRESETS && hasEntry;
            useButton.active = hasEntry;
            useButton.setMessage(Text.literal(isActive ? "Unset" : "Use"));

            ButtonWidget editButton = this.customEditButtons.get(row);
            editButton.visible = !whitelistLocked && this.activeTab == Tab.CUSTOM_PRESETS && hasEntry;
            editButton.active = hasEntry;

            ButtonWidget deleteButton = this.customDeleteButtons.get(row);
            deleteButton.visible = !whitelistLocked && this.activeTab == Tab.CUSTOM_PRESETS && hasEntry;
            deleteButton.active = hasEntry;
        }
    }

    private void updateWhitelistToggleLabel() {
        this.whitelistToggleButton.setMessage(Text.literal(
                this.working.offhandWhitelistEnabled ? "Whitelist: ON" : "Whitelist: OFF"));
        this.whitelistToggleButton.active = true;
        updateWhitelistToggleLayout();
    }

    private void updateWhitelistToggleLayout() {
        if (this.whitelistToggleButton == null) {
            return;
        }

        if (!this.working.offhandWhitelistEnabled) {
            int centeredX = this.panelLeft + (this.panelRight - this.panelLeft - this.defaultToggleWidth) / 2;
            int centeredY = this.panelTop + (this.panelBottom - this.panelTop) / 2 - 10;
            this.whitelistToggleButton.setPosition(centeredX, centeredY);
            return;
        }

        this.whitelistToggleButton.setPosition(this.defaultToggleX, this.defaultToggleY);
    }

    private void renderWhitelistLockedPanel(DrawContext context) {
        int contentTop = this.toggleY + 30;
        int contentBottom = this.panelBottom - 32;
        if (contentBottom > contentTop) {
            context.fill(this.leftListLeft, contentTop, this.rightListRight, contentBottom, COLOR_SECTION_FILL);
            drawOutline(context, this.leftListLeft, contentTop, this.rightListRight, contentBottom, COLOR_PANEL_EDGE_SOFT);
        }

        int centerX = (this.panelLeft + this.panelRight) / 2;
        int hintY = this.whitelistToggleButton.getBottom() + 12;
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Enable the mod to access all features \u2764"),
                centerX,
                hintY,
                COLOR_TEXT_SUB);
    }

    private void applyWhitelistPreset(String presetName, Set<String> preset) {
        this.working.offhandWhitelistEnabled = true;
        updateWhitelistToggleLabel();
        this.workingAllowedItemIds.clear();
        this.workingAllowedItemIds.addAll(KoHsOffhandWhitelistConfig.sanitizeExistingItemIds(preset));
        this.activePresetName = presetName;
        this.editingPresetName = null;
        refreshViewData();
        persistConfig();
    }

    private void addFromRightRow(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= this.rightRowMap.length) {
            return;
        }
        int idx = this.rightRowMap[rowIndex];
        if (idx < 0 || idx >= this.rightItems.size()) {
            return;
        }

        LinkedHashSet<String> selected = this.activeTab == Tab.WHITELIST
                ? this.workingAllowedItemIds
                : this.presetBuilderItemIds;
        selected.add(this.rightItems.get(idx).idString());
        if (this.activeTab == Tab.WHITELIST) {
            this.activePresetName = resolvePresetName(this.workingAllowedItemIds);
            persistConfig();
        }
        refreshViewData();
    }

    private void removeFromLeftRow(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= this.leftRowMap.length) {
            return;
        }
        int idx = this.leftRowMap[rowIndex];
        if (idx < 0 || idx >= this.leftItems.size()) {
            return;
        }

        LinkedHashSet<String> selected = this.activeTab == Tab.WHITELIST
                ? this.workingAllowedItemIds
                : this.presetBuilderItemIds;
        selected.remove(this.leftItems.get(idx).idString());
        if (this.activeTab == Tab.WHITELIST) {
            this.activePresetName = resolvePresetName(this.workingAllowedItemIds);
            persistConfig();
        }
        refreshViewData();
    }

    private void clearPresetBuilder() {
        this.editingPresetName = null;
        this.presetNameField.setText("");
        this.presetNameField.setSuggestion("");
        this.presetBuilderItemIds.clear();
        refreshViewData();
    }

    private void saveOrUpdateCustomPreset() {
        String name = KoHsOffhandWhitelistConfig.sanitizePresetName(this.presetNameField.getText());
        if (name.isEmpty()) {
            this.presetNameField.setSuggestion("Preset name required");
            return;
        }

        if (this.editingPresetName != null && !this.editingPresetName.equals(name)) {
            this.customPresets.remove(this.editingPresetName);
        }
        this.customPresets.put(name, KoHsOffhandWhitelistConfig.sanitizeExistingItemIds(this.presetBuilderItemIds));
        this.editingPresetName = name;
        this.presetNameField.setText(name);
        this.presetNameField.setSuggestion("");
        persistConfig();
        switchTab(Tab.CUSTOM_PRESETS);
    }

    private void editCustomPreset(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= this.customRowMap.length) {
            return;
        }
        int idx = this.customRowMap[rowIndex];
        if (idx < 0 || idx >= this.customPresetItems.size()) {
            return;
        }

        CustomPresetEntry entry = this.customPresetItems.get(idx);
        this.editingPresetName = entry.name();
        this.presetNameField.setText(entry.name());
        this.presetBuilderItemIds.clear();
        this.presetBuilderItemIds.addAll(entry.itemIds());
        switchTab(Tab.CREATE_PRESET);
    }

    private void deleteCustomPreset(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= this.customRowMap.length) {
            return;
        }
        int idx = this.customRowMap[rowIndex];
        if (idx < 0 || idx >= this.customPresetItems.size()) {
            return;
        }

        String name = this.customPresetItems.get(idx).name();
        this.customPresets.remove(name);
        if (name.equals(this.activePresetName)) {
            this.activePresetName = resolvePresetName(this.workingAllowedItemIds);
        }
        if (name.equals(this.editingPresetName)) {
            this.editingPresetName = null;
            this.presetNameField.setText("");
        }
        persistConfig();
        refreshViewData();
    }

    private boolean isInsideList(double mouseX, double mouseY, int left, int right) {
        return mouseX >= left && mouseX <= right && mouseY >= this.listTop && mouseY <= this.listBottom;
    }

    private void persistConfig() {
        this.working.setAllowedItemIds(this.workingAllowedItemIds);
        this.working.modEnabled = this.working.offhandWhitelistEnabled;
        this.working.setCustomPresets(this.customPresets);
        KoHsOffhandWhitelist.saveConfig(this.working);
    }

    private void applyCustomPreset(CustomPresetEntry entry) {
        if (entry == null) {
            return;
        }
        applyWhitelistPreset(entry.name(), entry.itemIds());
    }

    private void toggleCustomPresetUse(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= this.customRowMap.length) {
            return;
        }
        int idx = this.customRowMap[rowIndex];
        if (idx < 0 || idx >= this.customPresetItems.size()) {
            return;
        }

        CustomPresetEntry entry = this.customPresetItems.get(idx);
        if (entry.name().equals(this.activePresetName)) {
            this.activePresetName = null;
            refreshViewData();
            return;
        }

        applyCustomPreset(entry);
    }

    private String resolvePresetName(Set<String> items) {
        LinkedHashSet<String> normalized = KoHsOffhandWhitelistConfig.sanitizeExistingItemIds(items);
        for (Map.Entry<String, LinkedHashSet<String>> entry : this.customPresets.entrySet()) {
            if (entry.getValue().equals(normalized)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void ensureBuiltInPresets(LinkedHashMap<String, LinkedHashSet<String>> target) {
        LinkedHashMap<String, LinkedHashSet<String>> rebuilt = new LinkedHashMap<>();
        for (Map.Entry<String, Set<String>> entry : BUILT_IN_PRESETS.entrySet()) {
            rebuilt.put(entry.getKey(), KoHsOffhandWhitelistConfig.sanitizeExistingItemIds(entry.getValue()));
        }
        for (Map.Entry<String, LinkedHashSet<String>> entry : target.entrySet()) {
            rebuilt.put(entry.getKey(), KoHsOffhandWhitelistConfig.sanitizeExistingItemIds(entry.getValue()));
        }
        target.clear();
        target.putAll(rebuilt);
    }

    private String getPresetStatusText() {
        if (this.activeTab == Tab.REPORT_ISSUES) {
            return "Support and troubleshooting";
        }
        if (this.activeTab == Tab.CREATE_PRESET) {
            String editingName = KoHsOffhandWhitelistConfig.sanitizePresetName(
                    this.presetNameField == null ? this.editingPresetName : this.presetNameField.getText());
            if (!editingName.isEmpty()) {
                return "Editing preset [" + editingName + "]";
            }
            if (this.editingPresetName != null && !this.editingPresetName.isBlank()) {
                return "Editing preset [" + this.editingPresetName + "]";
            }
            return "Editing preset [new]";
        }
        if (this.activePresetName != null) {
            return "Preset [" + this.activePresetName + "]";
        }
        return "Manual whitelist";
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private ButtonWidget createNeonButton(
            int x,
            int y,
            int width,
            int height,
            Text message,
            ButtonWidget.PressAction onPress) {
        ButtonWidget button = ButtonWidget.builder(message, onPress)
                .dimensions(x, y, width, height)
                .build();
        button.setAlpha(0.0F);
        return button;
    }
    private void styleInputField(TextFieldWidget field) {
        field.setDrawsBackground(false);
        field.setEditableColor(COLOR_TEXT_MAIN);
        field.setUneditableColor(COLOR_TEXT_DIM);
    }

    private void renderTextFieldFrames(DrawContext context) {
        drawInputFrame(context, this.leftSearchField);
        drawInputFrame(context, this.rightSearchField);
        drawInputFrame(context, this.customSearchField);
        drawInputFrame(context, this.presetNameField);
    }

    private void drawInputFrame(DrawContext context, TextFieldWidget field) {
        if (field == null || !field.visible) {
            return;
        }
        int x1 = field.getX() - INPUT_FRAME_PAD_X;
        int y1 = field.getY() - INPUT_FRAME_PAD_Y;
        int x2 = field.getRight() + INPUT_FRAME_PAD_X;
        int y2 = field.getBottom() + INPUT_FRAME_PAD_Y;
        boolean focused = field.isFocused();

        context.fill(x1, y1, x2, y2, focused ? 0x6C271A44 : 0x54201535);
        drawOutline(context, x1, y1, x2, y2, focused ? 0xFFC27DFF : COLOR_PANEL_EDGE_SOFT);
        if (focused) {
            renderFrameGlow(context, x1 - 1, y1 - 1, x2 + 1, y2 + 1, 0.6F);
        }
    }

    private int drawWrappedText(
            DrawContext context,
            String text,
            int x,
            int y,
            int maxWidth,
            int color) {
        List<net.minecraft.text.OrderedText> wrapped = this.textRenderer.wrapLines(Text.literal(text), maxWidth);
        int lineY = y;
        for (net.minecraft.text.OrderedText line : wrapped) {
            context.drawTextWithShadow(this.textRenderer, line, x, lineY, color);
            lineY += this.textRenderer.fontHeight + 1;
        }
        return lineY;
    }

    private void renderReportImage(DrawContext context, int left, int top, int right, int bottom) {
        int innerLeft = left;
        int innerRight = right;
        int innerTop = top;
        int innerBottom = bottom;
        int innerWidth = Math.max(1, innerRight - innerLeft);
        int innerHeight = Math.max(1, innerBottom - innerTop);
        Identifier logoTexture = findAvailableLogoTexture();
        if (logoTexture != null) {
            int[] textureSize = resolveLogoTextureSize(logoTexture);
            int textureWidth = Math.max(1, textureSize[0]);
            int textureHeight = Math.max(1, textureSize[1]);
            float aspect = (float) textureWidth / (float) textureHeight;

            int drawWidth = innerWidth;
            int drawHeight = Math.max(1, Math.round(drawWidth / aspect));
            if (drawHeight > innerHeight) {
                drawHeight = innerHeight;
                drawWidth = Math.max(1, Math.round(drawHeight * aspect));
            }
            int drawX = innerLeft + (innerWidth - drawWidth) / 2;
            int drawY = innerTop + (innerHeight - drawHeight) / 2;

            drawTextureCompat(context, logoTexture, drawX, drawY, drawWidth, drawHeight, textureWidth, textureHeight);
            return;
        }

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("IMAGE NOT FOUND"),
                (left + right) / 2,
                top + Math.max(8, (bottom - top - this.textRenderer.fontHeight) / 2),
                COLOR_TEXT_MAIN);
    }

    private void drawTextureCompat(
            DrawContext context,
            Identifier textureId,
            int x,
            int y,
            int width,
            int height,
            int textureWidth,
            int textureHeight) {
        int safeTextureWidth = Math.max(1, textureWidth);
        int safeTextureHeight = Math.max(1, textureHeight);

        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                textureId,
                x,
                y,
                0.0F,
                0.0F,
                width,
                height,
                safeTextureWidth,
                safeTextureHeight,
                safeTextureWidth,
                safeTextureHeight);
    }
    private Identifier findAvailableLogoTexture() {
        if (this.client == null) {
            return null;
        }
        if (this.client.getResourceManager().getResource(LOGO_TEXTURE).isPresent()) {
            return LOGO_TEXTURE;
        }
        if (this.client.getResourceManager().getResource(LOGO_TEXTURE_FALLBACK).isPresent()) {
            return LOGO_TEXTURE_FALLBACK;
        }
        if (this.client.getResourceManager().getResource(LOGO_TEXTURE_LEGACY).isPresent()) {
            return LOGO_TEXTURE_LEGACY;
        }
        if (this.client.getResourceManager().getResource(LOGO_TEXTURE_LEGACY_FALLBACK).isPresent()) {
            return LOGO_TEXTURE_LEGACY_FALLBACK;
        }
        return null;
    }

    private int[] resolveLogoTextureSize(Identifier textureId) {
        if (this.client == null || textureId == null) {
            return new int[] {1, 1};
        }
        if (textureId.equals(this.cachedLogoTextureId) && this.cachedLogoTextureWidth > 0 && this.cachedLogoTextureHeight > 0) {
            return new int[] {this.cachedLogoTextureWidth, this.cachedLogoTextureHeight};
        }
        try {
            var resource = this.client.getResourceManager().getResource(textureId);
            if (resource.isPresent()) {
                try (var stream = resource.get().getInputStream();
                        NativeImage image = NativeImage.read(stream)) {
                    this.cachedLogoTextureId = textureId;
                    this.cachedLogoTextureWidth = Math.max(1, image.getWidth());
                    this.cachedLogoTextureHeight = Math.max(1, image.getHeight());
                    return new int[] {this.cachedLogoTextureWidth, this.cachedLogoTextureHeight};
                }
            }
        } catch (Exception ignored) {
        }
        return new int[] {1, 1};
    }

    private void drawOutline(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        if (x2 <= x1 || y2 <= y1) {
            return;
        }
        context.fill(x1, y1, x2, y1 + 1, color);
        context.fill(x1, y2 - 1, x2, y2, color);
        context.fill(x1, y1 + 1, x1 + 1, y2 - 1, color);
        context.fill(x2 - 1, y1 + 1, x2, y2 - 1, color);
    }

    private void renderFrameGlow(DrawContext context, int x1, int y1, int x2, int y2, float intensity) {
        float clamped = MathHelper.clamp(intensity, 0.0F, 1.0F);
        for (int layer = 1; layer <= 3; layer++) {
            float layerFactor = clamped * (0.35F / layer);
            int color = applyAlpha(0xBB8A45F0, layerFactor);
            drawOutline(context, x1 - layer, y1 - layer, x2 + layer, y2 + layer, color);
        }
    }

    private int applyAlpha(int color, float factor) {
        int alpha = (color >>> 24) & 0xFF;
        int scaledAlpha = MathHelper.clamp((int) (alpha * factor), 0, 255);
        return (scaledAlpha << 24) | (color & 0x00FFFFFF);
    }

    private void verifyCurrentLayout(String pass) {
        List<LayoutArea> areas = new ArrayList<>();
        addTextArea(areas, "title", this.panelLeft + PANEL_MARGIN, this.titleY, this.title.getString(), false);
        addTextArea(
                areas,
                "subtitle",
                this.panelLeft + PANEL_MARGIN,
                this.subtitleY,
                "Status: " + getPresetStatusText(),
                false);

        addWidgetArea(areas, "tabWhitelist", this.tabWhitelistButton, true);
        addWidgetArea(areas, "tabCreatePreset", this.tabCreatePresetButton, true);
        addWidgetArea(areas, "tabCustomPresets", this.tabCustomPresetsButton, true);
        addWidgetArea(areas, "tabReportIssues", this.tabReportIssuesButton, true);
        addWidgetArea(areas, "whitelistToggle", this.whitelistToggleButton, true);
        addWidgetArea(areas, "leftSearch", this.leftSearchField, true);
        addWidgetArea(areas, "rightSearch", this.rightSearchField, true);
        addWidgetArea(areas, "customSearch", this.customSearchField, true);
        addWidgetArea(areas, "presetName", this.presetNameField, true);
        addWidgetArea(areas, "savePreset", this.savePresetButton, true);
        addWidgetArea(areas, "clearPreset", this.clearPresetSelectionButton, true);
        addWidgetArea(areas, "openDiscord", this.openDiscordButton, true);

        for (int i = 0; i < this.leftRemoveButtons.size(); i++) {
            addWidgetArea(areas, "leftRemove#" + i, this.leftRemoveButtons.get(i), true);
        }
        for (int i = 0; i < this.rightAddButtons.size(); i++) {
            addWidgetArea(areas, "rightAdd#" + i, this.rightAddButtons.get(i), true);
        }
        for (int i = 0; i < this.customUseButtons.size(); i++) {
            addWidgetArea(areas, "customUse#" + i, this.customUseButtons.get(i), true);
        }
        for (int i = 0; i < this.customEditButtons.size(); i++) {
            addWidgetArea(areas, "customEdit#" + i, this.customEditButtons.get(i), true);
        }
        for (int i = 0; i < this.customDeleteButtons.size(); i++) {
            addWidgetArea(areas, "customDelete#" + i, this.customDeleteButtons.get(i), true);
        }

        int overlapCount = 0;
        for (int i = 0; i < areas.size(); i++) {
            LayoutArea a = areas.get(i);
            if (a.mustStayInsidePanel()
                    && (a.left() < this.panelLeft || a.right() > this.panelRight || a.top() < this.panelTop || a.bottom() > this.panelBottom)) {
                overlapCount++;
                KoHsOffhandWhitelist.LOGGER.warn(
                        "[{}] Layout {}: '{}' is outside panel bounds. ({},{})-({},{}) panel=({},{})-({},{})",
                        KoHsOffhandWhitelist.MOD_ID,
                        pass,
                        a.name(),
                        a.left(),
                        a.top(),
                        a.right(),
                        a.bottom(),
                        this.panelLeft,
                        this.panelTop,
                        this.panelRight,
                        this.panelBottom);
            }

            for (int j = i + 1; j < areas.size(); j++) {
                LayoutArea b = areas.get(j);
                if (a.intersects(b)) {
                    overlapCount++;
                    KoHsOffhandWhitelist.LOGGER.warn(
                            "[{}] Layout {}: overlap '{}' with '{}'",
                            KoHsOffhandWhitelist.MOD_ID,
                            pass,
                            a.name(),
                            b.name());
                }
            }
        }

        if (overlapCount == 0 && pass.startsWith("init")) {
            KoHsOffhandWhitelist.LOGGER.info("[{}] Layout {}: OK", KoHsOffhandWhitelist.MOD_ID, pass);
        }
    }

    private void addTextArea(List<LayoutArea> areas, String name, int x, int y, String text, boolean mustStayInsidePanel) {
        int width = this.textRenderer.getWidth(text);
        int height = this.textRenderer.fontHeight;
        areas.add(new LayoutArea(name, x, y, x + width, y + height, mustStayInsidePanel));
    }

    private void addWidgetArea(List<LayoutArea> areas, String name, ClickableWidget widget, boolean mustStayInsidePanel) {
        if (widget == null || !widget.visible) {
            return;
        }
        areas.add(new LayoutArea(name, widget.getX(), widget.getY(), widget.getRight(), widget.getBottom(), mustStayInsidePanel));
    }

    private record LayoutArea(String name, int left, int top, int right, int bottom, boolean mustStayInsidePanel) {
        private boolean intersects(LayoutArea other) {
            return this.right > other.left
                    && this.left < other.right
                    && this.bottom > other.top
                    && this.top < other.bottom;
        }
    }

    private static LinkedHashMap<String, Set<String>> createBuiltInPresets() {
        LinkedHashMap<String, Set<String>> presets = new LinkedHashMap<>();
        presets.put(PRESET_CRYSTAL_NAME, PRESET_CRYSTAL_PVP);
        presets.put(PRESET_NETHERITE_NAME, PRESET_NETHERITE_POT_PVP);
        presets.put(PRESET_MACE_NAME, PRESET_MACE_PVP);
        return presets;
    }

    private static List<ItemEntry> collectMinecraftItems() {
        List<ItemEntry> entries = new ArrayList<>();
        LinkedHashSet<String> keys = new LinkedHashSet<>();
        for (Item item : Registries.ITEM) {
            Identifier id = Registries.ITEM.getId(item);
            if (id == null || id.equals(Identifier.ofVanilla("air"))) {
                continue;
            }
            if (!"minecraft".equals(id.getNamespace())) {
                continue;
            }

            if (isPotionContainerItem(item)) {
                addPotionVariantEntries(entries, keys, item, id);
                continue;
            }

            ItemStack stack = item.getDefaultStack();
            String key = id.toString();
            String displayName = stack.getName().getString();
            String searchKey = (displayName + " " + key).toLowerCase(Locale.ROOT);
            addItemEntry(entries, keys, new ItemEntry(stack, key, displayName, searchKey));
        }
        entries.sort(Comparator
                .comparing((ItemEntry entry) -> entry.displayName().toLowerCase(Locale.ROOT))
                .thenComparing(ItemEntry::idString));
        return List.copyOf(entries);
    }

    private static void addPotionVariantEntries(List<ItemEntry> entries, Set<String> keys, Item containerItem, Identifier containerId) {
        for (Potion potion : Registries.POTION) {
            Identifier potionId = Registries.POTION.getId(potion);
            if (potionId == null || potionId.equals(Identifier.ofVanilla("empty"))) {
                continue;
            }

            RegistryEntry<Potion> entry = Registries.POTION.getEntry(potion);
            ItemStack stack = PotionContentsComponent.createStack(containerItem, entry);
            String key = containerId + "|" + potionId;
            String displayName = stack.getName().getString();
            String searchKey = (displayName + " " + key + " " + containerId + " " + potionId).toLowerCase(Locale.ROOT);
            addItemEntry(entries, keys, new ItemEntry(stack, key, displayName, searchKey));
        }
    }

    private static void addItemEntry(List<ItemEntry> entries, Set<String> keys, ItemEntry entry) {
        if (!keys.add(entry.idString())) {
            return;
        }
        entries.add(entry);
    }

    private static boolean isPotionContainerItem(Item item) {
        return POTION_CONTAINER_ITEMS.contains(item);
    }

    private record ItemEntry(ItemStack previewStack, String idString, String displayName, String searchKey) {
    }

    private record CustomPresetEntry(String name, LinkedHashSet<String> itemIds) {
    }
}

