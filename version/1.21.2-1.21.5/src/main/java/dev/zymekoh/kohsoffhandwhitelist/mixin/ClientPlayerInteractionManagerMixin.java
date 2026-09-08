package dev.zymekoh.kohsoffhandwhitelist.mixin;

import dev.zymekoh.kohsoffhandwhitelist.KoHsOffhandWhitelist;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.network.ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {
    @Inject(
            method = "clickSlot(IIILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/entity/player/PlayerEntity;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void kohsOffhandWhitelist$blockDisallowedOffhandInsert(
            int syncId,
            int slotId,
            int button,
            SlotActionType actionType,
            PlayerEntity player,
            CallbackInfo ci) {
        if (kohsOffhandWhitelist$shouldBlockDisallowedOffhandInsert(slotId, button, actionType, player)) {
            ci.cancel();
        }
    }

    @Unique
    private static boolean kohsOffhandWhitelist$shouldBlockDisallowedOffhandInsert(
            int slotId,
            int button,
            SlotActionType actionType,
            PlayerEntity player) {
        if (!KoHsOffhandWhitelist.isOffhandWhitelistEnabled()) {
            return false;
        }
        if (player == null || player.currentScreenHandler == null) {
            return false;
        }

        ScreenHandler handler = player.currentScreenHandler;

        if (actionType == SlotActionType.SWAP) {
            if (button == PlayerInventory.OFF_HAND_SLOT) {
                if (slotId < 0 || slotId >= handler.slots.size()) {
                    return false;
                }
                ItemStack sourceStack = handler.getSlot(slotId).getStack();
                return kohsOffhandWhitelist$isDisallowedOffhandItem(sourceStack);
            }

            if (button >= 0
                    && button < PlayerInventory.getHotbarSize()
                    && kohsOffhandWhitelist$isPlayerOffhandSlot(player, slotId)) {
                ItemStack sourceStack = player.getInventory().getStack(button);
                return kohsOffhandWhitelist$isDisallowedOffhandItem(sourceStack);
            }

            return false;
        }

        if (actionType != SlotActionType.PICKUP && actionType != SlotActionType.QUICK_CRAFT) {
            return false;
        }
        if (!kohsOffhandWhitelist$isPlayerOffhandSlot(player, slotId)) {
            return false;
        }

        ItemStack cursorStack = handler.getCursorStack();
        return kohsOffhandWhitelist$isDisallowedOffhandItem(cursorStack);
    }

    @Unique
    private static boolean kohsOffhandWhitelist$isPlayerOffhandSlot(PlayerEntity player, int slotId) {
        if (player == null || player.currentScreenHandler == null) {
            return false;
        }
        if (slotId < 0 || slotId >= player.currentScreenHandler.slots.size()) {
            return false;
        }

        Slot slot = player.currentScreenHandler.getSlot(slotId);
        return slot != null
                && slot.inventory == player.getInventory()
                && slot.getIndex() == PlayerInventory.OFF_HAND_SLOT;
    }

    @Unique
    private static boolean kohsOffhandWhitelist$isDisallowedOffhandItem(ItemStack stack) {
        return !stack.isEmpty() && !KoHsOffhandWhitelist.isAllowedOffhandStack(stack);
    }
}
