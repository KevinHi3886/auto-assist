package com.beta1122.autoassist;

import com.beta1122.autoassist.feature.AutoBridge;
import com.beta1122.autoassist.feature.AutoEat;
import com.beta1122.autoassist.feature.AutoRefill;
import com.beta1122.autoassist.feature.AutoTool;
import com.beta1122.autoassist.gui.AutoAssistConfigScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

public final class AutoAssistClient implements ClientModInitializer {
	private static final KeyMapping CONFIG_KEY = new KeyMapping(
			"key.autoassist.config",
			ClientCompat.keyboardType(),
			InputConstants.getKey("key.keyboard.f8").getValue(),
			KeyMapping.Category.MISC
	);
	private static final KeyMapping AUTO_TOOL_KEY = new KeyMapping(
			"key.autoassist.auto_tool",
			ClientCompat.keyboardType(),
			InputConstants.getKey("key.keyboard.f7").getValue(),
			KeyMapping.Category.GAMEPLAY
	);

	@Override
	public void onInitializeClient() {
		AutoAssistConfig.load();
		AutoBridge.init();
		KeyMappingHelper.registerKeyMapping(CONFIG_KEY);
		KeyMappingHelper.registerKeyMapping(AUTO_TOOL_KEY);
		ClientTickEvents.START_CLIENT_TICK.register(AutoEat::beforeVanillaInput);
		ClientTickEvents.END_CLIENT_TICK.register(AutoAssistClient::onEndClientTick);
	}

	private static void onEndClientTick(Minecraft client) {
		while (CONFIG_KEY.consumeClick()) {
			if (ClientCompat.screen(client) == null) {
				ClientCompat.setScreen(client, new AutoAssistConfigScreen());
			}
		}

		while (AUTO_TOOL_KEY.consumeClick()) {
			AutoAssistConfig.setAutoToolEnabled(!AutoAssistConfig.isAutoToolEnabled());
			AutoAssistConfig.save();
			LocalPlayer player = client.player;
			if (player != null) {
				player.sendOverlayMessage(Component.translatable(
						AutoAssistConfig.isAutoToolEnabled()
								? "message.autoassist.auto_tool.on"
								: "message.autoassist.auto_tool.off"));
			}
		}

		AutoBridge.tick(client);

		LocalPlayer player = client.player;
		if (player == null || client.level == null || client.gameMode == null) {
			AutoEat.stop(client);
			AutoRefill.reset();
			return;
		}

		// Do not touch inventories while a screen/container is open, while carrying
		// an item with the cursor, in spectator, in creative, or after death.
		if (ClientCompat.screen(client) != null
				|| player.containerMenu != player.inventoryMenu
				|| !player.containerMenu.getCarried().isEmpty()
				|| player.isSpectator()
				|| player.isDeadOrDying()
				|| player.hasInfiniteMaterials()) {
			AutoEat.stop(client);
			AutoRefill.remember(player);
			return;
		}

		AutoEat.tick(client, player);
		AutoRefill.tick(client, player);
		AutoTool.tick(client, player);
	}
}
