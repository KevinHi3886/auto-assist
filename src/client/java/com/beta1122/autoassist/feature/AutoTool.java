package com.beta1122.autoassist.feature;

import com.beta1122.autoassist.AutoAssistConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Auto Tool: while the player is breaking a block, selects the best tool for
 * that block from the hotbar.
 */
public final class AutoTool {
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
		int bestSlot = findBestTool(inventory, state);

		if (bestSlot >= 0 && bestSlot != currentSlot) {
			inventory.setSelectedSlot(bestSlot);
		}
	}

	private static int findBestTool(Inventory inventory, BlockState state) {
		int bestSlot = -1;
		float bestScore = 0.0F;

		for (int slot = 0; slot < 9; slot++) {
			ItemStack stack = inventory.getItem(slot);
			float score = score(stack, state);
			if (score > bestScore) {
				bestScore = score;
				bestSlot = slot;
			}
		}

		return bestSlot;
	}

	private static float score(ItemStack stack, BlockState state) {
		if (stack.isEmpty()) {
			return 0.0F;
		}

		float speed = stack.getDestroySpeed(state);
		boolean correctTool = stack.isCorrectToolForDrops(state);

		// A non-tool item normally has speed 1. Do not switch to it just because
		// the current slot is empty.
		if (!correctTool && speed <= 1.0F) {
			return 0.0F;
		}

		return (correctTool ? 1000.0F : 0.0F) + speed;
	}
}
