package dev.zymekoh.kohsoffhandwhitelist.mixin;

import dev.zymekoh.kohsoffhandwhitelist.KoHsOffhandWhitelist;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotOffhandInsertMixin {
    @Shadow
    @Final
    public Inventory inventory;

    @Shadow
    public abstract int getIndex();

    @Inject(method = "canInsert(Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void kohsOffhandWhitelist$blockDisallowedInsert(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!KoHsOffhandWhitelist.isOffhandWhitelistEnabled() || stack.isEmpty()) {
            return;
        }
        if (!(this.inventory instanceof PlayerInventory)) {
            return;
        }
        if (this.getIndex() != PlayerInventory.OFF_HAND_SLOT) {
            return;
        }
        if (!KoHsOffhandWhitelist.isAllowedOffhandStack(stack)) {
            cir.setReturnValue(false);
        }
    }
}
