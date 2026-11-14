package com.beta1122.autoassist.feature;

import com.beta1122.autoassist.AutoAssistConfig;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.consume_effects.TeleportRandomlyConsumeEffect;

/**
 * Auto Eat: when the food level is at or below the configured threshold (default
 * 16), eats the best safe food from the hotbar (moving food from the main
 * inventory when necessary).
 */
public final class AutoEat {
	private static final Set<Item> HARMFUL_FOODS = Set.of(
			Items.ROTTEN_FLESH,
			Items.PUFFERFISH,
			Items.SPIDER_EYE,
			Items.POISONOUS_POTATO,
			Items.CHORUS_FRUIT
	);

	private static boolean autoUsingKey;
	private static long nextAttemptTick;

	private AutoEat() {
	}

	/**
	 * Runs before vanilla input handling. It releases our synthetic use key as
	 * soon as eating should stop, so vanilla cannot start using an unrelated item.
	 */
	public static void beforeVanillaInput(Minecraft client) {
		if (!autoUsingKey) {
			return;
		}

		LocalPlayer player = client.player;
		if (!AutoAssistConfig.isAutoEatEnabled()
				|| player == null
				|| client.gui.screen() != null
				|| player.isDeadOrDying()
				|| player.getFoodData().getFoodLevel() >= 20) {
			releaseKey(client);
			return;
		}

		if (!player.isUsingItem()) {
			// If we are no longer using food, stop holding the synthetic key until the
			// end-of-tick handler has selected the next valid food.
			if (player.getFoodData().getFoodLevel() > AutoAssistConfig.getEatThreshold()
					|| !isSafeFood(player.getMainHandItem(), player)) {
				releaseKey(client);
			}
		}
	}

	public static void tick(Minecraft client, LocalPlayer player) {
		if (!AutoAssistConfig.isAutoEatEnabled()) {
			releaseKey(client);
			return;
		}

		if (player.isUsingItem()) {
			return;
		}

		if (player.getFoodData().getFoodLevel() > AutoAssistConfig.getEatThreshold()) {
			releaseKey(client);
			return;
		}

		long gameTime = client.level.getGameTime();
		if (gameTime < nextAttemptTick) {
			return;
		}

		Inventory inventory = player.getInventory();
		int foodSlot = findBestFoodSlot(inventory, 0, 9, player);

		if (foodSlot < 0) {
			int mainSlot = findBestFoodSlot(inventory, 9, 36, player);
			if (mainSlot < 0) {
				releaseKey(client);
				return;
			}

			int targetHotbar = findEmptyHotbarSlot(inventory);
			if (targetHotbar < 0) {
				targetHotbar = inventory.getSelectedSlot();
			}

			client.gameMode.handleContainerInput(
					InventoryMenu.CONTAINER_ID,
					mainSlot,
					targetHotbar,
					ContainerInput.SWAP,
					player
			);
			inventory.setSelectedSlot(targetHotbar);
			foodSlot = targetHotbar;
		}

		inventory.setSelectedSlot(foodSlot);

		if (!client.options.keyUse.isDown()) {
			client.options.keyUse.setDown(true);
			autoUsingKey = true;
		}

		client.gameMode.useItem(player, InteractionHand.MAIN_HAND);

		if (!player.isUsingItem()) {
			releaseKey(client);
			nextAttemptTick = gameTime + 10;
		}
	}

	public static void stop(Minecraft client) {
		releaseKey(client);
	}

	private static void releaseKey(Minecraft client) {
		if (autoUsingKey) {
			client.options.keyUse.setDown(false);
			autoUsingKey = false;
		}
	}

	private static int findBestFoodSlot(Inventory inventory, int fromInclusive, int toExclusive, Player player) {
		int bestSlot = -1;
		int bestScore = -1;

		for (int slot = fromInclusive; slot < toExclusive; slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (!isSafeFood(stack, player)) {
				continue;
			}

			FoodProperties food = stack.get(DataComponents.FOOD);
			int score = food.nutrition() * 1000 + Math.round(food.saturation() * 100.0F);
			if (score > bestScore) {
				bestScore = score;
				bestSlot = slot;
			}
		}

		return bestSlot;
	}

	private static int findEmptyHotbarSlot(Inventory inventory) {
		for (int slot = 0; slot < 9; slot++) {
			if (inventory.getItem(slot).isEmpty()) {
				return slot;
			}
		}

		return -1;
	}

	private static boolean isSafeFood(ItemStack stack, Player player) {
		if (stack.isEmpty() || HARMFUL_FOODS.contains(stack.getItem())) {
			return false;
		}

		FoodProperties food = stack.get(DataComponents.FOOD);
		if (food == null || food.nutrition() <= 0 || !player.canEat(food.canAlwaysEat())) {
			return false;
		}

		Consumable consumable = stack.get(DataComponents.CONSUMABLE);
		if (consumable == null) {
			return true;
		}

		List<ConsumeEffect> effects = consumable.onConsumeEffects();
		for (ConsumeEffect effect : effects) {
			if (effect instanceof TeleportRandomlyConsumeEffect) {
				return false;
			}

			if (effect instanceof ApplyStatusEffectsConsumeEffect applyEffect) {
				for (MobEffectInstance instance : applyEffect.effects()) {
					if (!instance.getEffect().value().isBeneficial()) {
						return false;
					}
				}
			}
		}

		return true;
	}
}
