package com.beta1122.autoassist.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

/**
 * Auto Refill: when the selected hotbar stack is used up, swaps the same item
 * from the main inventory into that hotbar slot.
 */
public final class AutoRefill {
	private static LocalPlayer trackedPlayer;
	private static ClientLevel trackedLevel;
	private static int lastSelectedSlot = -1;
	private static ItemStack lastStack = ItemStack.EMPTY;
	private static int lastCount;

	private AutoRefill() {
	}

	public static void tick(Minecraft client, LocalPlayer player) {
		if (player != trackedPlayer || client.level != trackedLevel) {
			trackedPlayer = player;
			trackedLevel = client.level;
			clearTracker();
		}

		Inventory inventory = player.getInventory();
		int selectedSlot = inventory.getSelectedSlot();
		ItemStack selectedStack = inventory.getSelectedItem();

		if (selectedSlot == lastSelectedSlot
				&& !lastStack.isEmpty()
				&& lastCount > 0
				&& selectedStack.isEmpty()) {
			ItemStack reference = lastStack;

			int mainSlot = findMatchingSlot(inventory, reference, 9, 36);
			if (mainSlot >= 0) {
				client.gameMode.handleContainerInput(
						InventoryMenu.CONTAINER_ID,
						mainSlot,
						selectedSlot,
						ContainerInput.SWAP,
						player
				);
			} else {
				int otherHotbar = findMatchingSlot(inventory, reference, 0, 9);
				if (otherHotbar >= 0) {
					inventory.setSelectedSlot(otherHotbar);
				}
			}
		}

		remember(player);
	}

	public static void remember(LocalPlayer player) {
		Inventory inventory = player.getInventory();
		ItemStack selectedStack = inventory.getSelectedItem();

		if (!selectedStack.isEmpty()
				&& !ItemStack.isSameItemSameComponents(lastStack, selectedStack)) {
			lastStack = selectedStack.copy();
		}

		lastSelectedSlot = inventory.getSelectedSlot();
		lastCount = selectedStack.getCount();
	}

	public static void reset() {
		trackedPlayer = null;
		trackedLevel = null;
		clearTracker();
	}

	private static int findMatchingSlot(Inventory inventory, ItemStack reference, int fromInclusive, int toExclusive) {
		for (int slot = fromInclusive; slot < toExclusive; slot++) {
			if (matches(inventory.getItem(slot), reference)) {
				return slot;
			}
		}

		return -1;
	}

	private static boolean matches(ItemStack candidate, ItemStack reference) {
		if (candidate.isEmpty() || reference.isEmpty() || candidate.getItem() != reference.getItem()) {
			return false;
		}

		return reference.isDamageableItem() || ItemStack.isSameItemSameComponents(candidate, reference);
	}

	private static void clearTracker() {
		lastSelectedSlot = -1;
		lastStack = ItemStack.EMPTY;
		lastCount = 0;
	}
}
