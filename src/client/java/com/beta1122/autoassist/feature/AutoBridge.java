package com.beta1122.autoassist.feature;

import com.beta1122.autoassist.AutoAssistConfig;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Auto Bridge+: while active, places the block held in the main hand under the
 * player when the block below is empty or fluid. Toggle with V.
 */
public final class AutoBridge {
	private static KeyMapping bridgeKey;
	private static boolean active;

	private AutoBridge() {
	}

	public static void init() {
		active = AutoAssistConfig.isAutoBridgeEnabled();
		bridgeKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.autoassist.bridge",
				InputConstants.Type.KEYBOARD,
				InputConstants.KEY_V,
				KeyMapping.Category.GAMEPLAY
		));
	}

	public static boolean isActive() {
		return active;
	}

	public static void setActive(boolean value) {
		active = value;
		AutoAssistConfig.setAutoBridgeEnabled(value);
		AutoAssistConfig.save();
	}

	public static void tick(Minecraft client) {
		while (bridgeKey != null && bridgeKey.consumeClick()) {
			setActive(!active);
			LocalPlayer player = client.player;
			if (player != null) {
				player.sendOverlayMessage(Component.translatable(
						active ? "message.autoassist.bridge.on" : "message.autoassist.bridge.off"));
			}
		}

		LocalPlayer player = client.player;
		if (!active || player == null || client.level == null || client.gameMode == null || client.gui.screen() != null) {
			return;
		}

		placeBlockUnderPlayer(client, player);
	}

	private static void placeBlockUnderPlayer(Minecraft client, LocalPlayer player) {
		ItemStack held = player.getMainHandItem();
		boolean blockItem = held.getItem() instanceof BlockItem;

		// Keep the original bridge's "sneak when the last block is in hand" behaviour
		// without clobbering the player's sneak key when holding a non-block item.
		if (blockItem && held.getCount() <= 1) {
			Input old = player.input.keyPresses;
			player.input.keyPresses = new Input(
					old.forward(), old.backward(), old.left(), old.right(), old.jump(), true, old.sprint());
		}

		if (!blockItem) {
			return;
		}

		BlockPos below = player.blockPosition().below();

		if (player.fallDistance > 2.5F) {
			tryPlace(client, player, below);
			return;
		}

		BlockState state = client.level.getBlockState(below);
		boolean empty = state.isAir() || !state.getFluidState().isEmpty();
		if (!empty) {
			return;
		}

		double blockTop = below.getY() + 1.0;
		AABB box = player.getBoundingBox();
		if (!(box.minY < blockTop) || !(box.maxY > below.getY())) {
			tryPlace(client, player, below);
		}
	}

	private static void tryPlace(Minecraft client, LocalPlayer player, BlockPos pos) {
		if (!(player.getMainHandItem().getItem() instanceof BlockItem) || client.gameMode == null) {
			return;
		}

		BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
		client.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
	}
}
