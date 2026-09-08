package dev.zymekoh.kohsoffhandwhitelist;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public final class KoHsOffhandWhitelistModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return KoHsOffhandWhitelistConfigScreen::create;
    }
}
