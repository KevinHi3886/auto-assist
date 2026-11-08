package com.beta1122.autoassist;

import com.beta1122.autoassist.feature.AutoBridge;
import com.beta1122.autoassist.feature.AutoEat;
import com.beta1122.autoassist.feature.AutoRefill;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class AutoAssistClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		AutoBridge.init();
		ClientTickEvents.START_CLIENT_TICK.register(AutoEat::beforeVanillaInput);
		ClientTickEvents.END_CLIENT_TICK.register(AutoAssistClient::onEndClientTick);
	}

	private static void onEndClientTick(Minecraft client) {
		AutoBridge.tick(client);

		LocalPlayer player = client.player;
		if (player == null || client.level == null || client.gameMode == null) {
			AutoEat.stop(client);
			AutoRefill.reset();
			return;
		}

		// Do not touch inventories while a screen/container is open, while carrying
		// an item with the cursor, in spectator, in creative, or after death.
		if (client.gui.screen() != null
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
	}
}
