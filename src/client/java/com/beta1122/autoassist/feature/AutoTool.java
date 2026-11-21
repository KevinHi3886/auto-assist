package com.beta1122.autoassist.feature;

import com.beta1122.autoassist.AutoAssistConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Auto Tool: while the player is breaking a block, selects the best tool for
 * that block from the hotbar. Tools with 10 or less durability remaining are
 * excluded and produce a chat warning once per slot.
 */
public final class AutoTool {
	private static final int MIN_DURABILITY = 10;
	private static final boolean[] warnedSlots = new boolean[9];
	private static final Item[] warnedItems = new Item[9];

	private AutoTool() {
	}

	public static void tick(Minecraft client, LocalPlayer player) {
		if (!AutoAssistConfig.isAutoToolEnabled()
				|| client.gameMode == null
				|| client.level == null
				|| !client.gameMode.isDestroying()
				|| player.isUsingItem()
				|| !(client.hitResult instanceof BlockHitResult hit)) {
			return;
		}

		BlockPos pos = hit.getBlockPos();
		BlockState state = client.level.getBlockState(pos);
		if (state.isAir() || state.getDestroySpeed(client.level, pos) < 0.0F) {
			return;
		}

		Inventory inventory = player.getInventory();
		int currentSlot = inventory.getSelectedSlot();
		updateWarningState(inventory);

		ItemStack currentStack = inventory.getItem(currentSlot);
		int currentDurability = remainingDurability(currentStack);
		if (currentStack.isDamageableItem()
				&& currentDurability <= MIN_DURABILITY
				&& !warnedSlots[currentSlot]) {
			player.sendSystemMessage(Component.translatable(
					"message.autoassist.auto_tool.low_durability",
					currentStack.getHoverName(),
					currentDurability
			));
			warnedSlots[currentSlot] = true;
			warnedItems[currentSlot] = currentStack.getItem();
		}

		int bestSlot = findBestTool(inventory, state);
		if (bestSlot >= 0 && bestSlot != currentSlot) {
			inventory.setSelectedSlot(bestSlot);
		}
	}

	private static void updateWarningState(Inventory inventory) {
		for (int slot = 0; slot < 9; slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (!stack.isDamageableItem()
					|| remainingDurability(stack) > MIN_DURABILITY
					|| warnedItems[slot] != stack.getItem()) {
				warnedSlots[slot] = false;
				warnedItems[slot] = null;
			}
		}
	}

	private static int findBestTool(Inventory inventory, BlockState state) {
		int bestSlot = -1;
		boolean bestCorrect = false;
		int bestDurability = -1;
		float bestSpeed = 0.0F;

		for (int slot = 0; slot < 9; slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (stack.isEmpty()) {
				continue;
			}

			boolean correctTool = stack.isCorrectToolForDrops(state);
			float speed = stack.getDestroySpeed(state);

			// A non-tool item normally has speed 1. Do not switch to it just
			// because the current slot is empty.
			if (!correctTool && speed <= 1.0F) {
				continue;
			}

			int durability = remainingDurability(stack);
			if (durability <= MIN_DURABILITY) {
				continue;
			}

			boolean better = bestSlot < 0
					|| (correctTool && !bestCorrect)
					|| (correctTool == bestCorrect && durability > bestDurability)
					|| (correctTool == bestCorrect && durability == bestDurability && speed > bestSpeed);

			if (better) {
				bestSlot = slot;
				bestCorrect = correctTool;
				bestDurability = durability;
				bestSpeed = speed;
			}
		}

		return bestSlot;
	}

	private static int remainingDurability(ItemStack stack) {
		if (!stack.isDamageableItem()) {
			return Integer.MAX_VALUE;
		}

		return stack.getMaxDamage() - stack.getDamageValue();
	}
}
