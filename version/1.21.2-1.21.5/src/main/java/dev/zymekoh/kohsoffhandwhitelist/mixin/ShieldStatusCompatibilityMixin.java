package dev.zymekoh.kohsoffhandwhitelist.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "walksy.shieldstatus.manager.ShieldStateManager", remap = false)
public abstract class ShieldStatusCompatibilityMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "isCoolingDown", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void kohsOffhandWhitelist$avoidNullPlayerCrash(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (player == null || this.client == null || this.client.player == null) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getCooldownProgress", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void kohsOffhandWhitelist$avoidNullPlayerCrashProgress(PlayerEntity player, CallbackInfoReturnable<Float> cir) {
        if (player == null || this.client == null || this.client.player == null) {
            cir.setReturnValue(0.0F);
        }
    }
}
