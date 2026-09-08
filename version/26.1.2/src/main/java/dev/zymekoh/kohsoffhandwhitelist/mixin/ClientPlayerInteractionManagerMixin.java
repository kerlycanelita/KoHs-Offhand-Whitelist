package dev.zymekoh.kohsoffhandwhitelist.mixin;

import dev.zymekoh.kohsoffhandwhitelist.KoHsOffhandWhitelist;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.multiplayer.MultiPlayerGameMode.class)
public abstract class ClientPlayerInteractionManagerMixin {
    @Inject(
            method = "handleContainerInput(IIILnet/minecraft/world/inventory/ContainerInput;Lnet/minecraft/world/entity/player/Player;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void kohsOffhandWhitelist$blockDisallowedOffhandInsert(
            int syncId,
            int slotId,
            int button,
            ContainerInput actionType,
            Player player,
            CallbackInfo ci) {
        if (kohsOffhandWhitelist$shouldBlockDisallowedOffhandInsert(slotId, button, actionType, player)) {
            ci.cancel();
        }
    }

    @Unique
    private static boolean kohsOffhandWhitelist$shouldBlockDisallowedOffhandInsert(
            int slotId,
            int button,
            ContainerInput actionType,
            Player player) {
        if (!KoHsOffhandWhitelist.isOffhandWhitelistEnabled()) {
            return false;
        }
        if (player == null || player.containerMenu == null) {
            return false;
        }

        AbstractContainerMenu handler = player.containerMenu;

        if (actionType == ContainerInput.SWAP) {
            if (button == Inventory.SLOT_OFFHAND) {
                if (slotId < 0 || slotId >= handler.slots.size()) {
                    return false;
                }
                ItemStack sourceStack = handler.getSlot(slotId).getItem();
                return kohsOffhandWhitelist$isDisallowedOffhandItem(sourceStack);
            }

            if (button >= 0
                    && button < Inventory.getSelectionSize()
                    && kohsOffhandWhitelist$isPlayerOffhandSlot(player, slotId)) {
                ItemStack sourceStack = player.getInventory().getItem(button);
                return kohsOffhandWhitelist$isDisallowedOffhandItem(sourceStack);
            }

            return false;
        }

        if (actionType != ContainerInput.PICKUP && actionType != ContainerInput.QUICK_CRAFT) {
            return false;
        }
        if (!kohsOffhandWhitelist$isPlayerOffhandSlot(player, slotId)) {
            return false;
        }

        ItemStack cursorStack = handler.getCarried();
        return kohsOffhandWhitelist$isDisallowedOffhandItem(cursorStack);
    }

    @Unique
    private static boolean kohsOffhandWhitelist$isPlayerOffhandSlot(Player player, int slotId) {
        if (player == null || player.containerMenu == null) {
            return false;
        }
        if (slotId < 0 || slotId >= player.containerMenu.slots.size()) {
            return false;
        }

        Slot slot = player.containerMenu.getSlot(slotId);
        return slot != null
                && slot.container == player.getInventory()
                && slot.getContainerSlot() == Inventory.SLOT_OFFHAND;
    }

    @Unique
    private static boolean kohsOffhandWhitelist$isDisallowedOffhandItem(ItemStack stack) {
        return !stack.isEmpty() && !KoHsOffhandWhitelist.isAllowedOffhandStack(stack);
    }
}
