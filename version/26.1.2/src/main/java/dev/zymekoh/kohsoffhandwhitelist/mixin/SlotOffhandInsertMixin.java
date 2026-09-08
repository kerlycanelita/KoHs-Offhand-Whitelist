package dev.zymekoh.kohsoffhandwhitelist.mixin;

import dev.zymekoh.kohsoffhandwhitelist.KoHsOffhandWhitelist;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
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
    public Container container;

    @Shadow
    public abstract int getContainerSlot();

    @Inject(method = "mayPlace(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void kohsOffhandWhitelist$blockDisallowedInsert(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!KoHsOffhandWhitelist.isOffhandWhitelistEnabled() || stack.isEmpty()) {
            return;
        }
        if (!(this.container instanceof Inventory)) {
            return;
        }
        if (this.getContainerSlot() != Inventory.SLOT_OFFHAND) {
            return;
        }
        if (!KoHsOffhandWhitelist.isAllowedOffhandStack(stack)) {
            cir.setReturnValue(false);
        }
    }
}
