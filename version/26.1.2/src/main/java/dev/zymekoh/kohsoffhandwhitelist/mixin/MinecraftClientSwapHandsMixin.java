package dev.zymekoh.kohsoffhandwhitelist.mixin;

import dev.zymekoh.kohsoffhandwhitelist.KoHsOffhandWhitelist;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftClientSwapHandsMixin {
    @Shadow
    @Final
    public Options options;

    @Inject(method = "handleKeybinds()V", at = @At("HEAD"))
    private void kohsOffhandWhitelist$consumeDisallowedOffhandSwapQueue(CallbackInfo ci) {
        if (!KoHsOffhandWhitelist.isOffhandWhitelistEnabled()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null || client.screen != null) {
            return;
        }

        ItemStack mainHand = client.player.getMainHandItem();
        if (mainHand.isEmpty() || KoHsOffhandWhitelist.isAllowedOffhandStack(mainHand)) {
            return;
        }

        while (this.options.keySwapOffhand.consumeClick()) {
            // Consume disallowed swap-to-offhand actions for non-whitelisted items.
        }
    }
}
