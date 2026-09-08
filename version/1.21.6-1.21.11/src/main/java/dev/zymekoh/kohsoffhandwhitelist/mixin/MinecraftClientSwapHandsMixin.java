package dev.zymekoh.kohsoffhandwhitelist.mixin;

import dev.zymekoh.kohsoffhandwhitelist.KoHsOffhandWhitelist;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientSwapHandsMixin {
    @Shadow
    @Final
    public GameOptions options;

    @Inject(method = "handleInputEvents()V", at = @At("HEAD"))
    private void kohsOffhandWhitelist$consumeDisallowedOffhandSwapQueue(CallbackInfo ci) {
        if (!KoHsOffhandWhitelist.isOffhandWhitelistEnabled()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.currentScreen != null) {
            return;
        }

        ItemStack mainHand = client.player.getMainHandStack();
        if (mainHand.isEmpty() || KoHsOffhandWhitelist.isAllowedOffhandStack(mainHand)) {
            return;
        }

        while (this.options.swapHandsKey.wasPressed()) {
            // Consume disallowed swap-to-offhand actions for non-whitelisted items.
        }
    }
}
