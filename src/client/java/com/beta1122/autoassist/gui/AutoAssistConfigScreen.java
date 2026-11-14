package com.beta1122.autoassist.gui;

import com.beta1122.autoassist.AutoAssistConfig;
import com.beta1122.autoassist.feature.AutoBridge;
import com.beta1122.autoassist.feature.AutoEat;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class AutoAssistConfigScreen extends Screen {
	private static final int BUTTON_WIDTH = 240;
	private static final int BUTTON_HEIGHT = 20;
	private static final int BUTTON_SPACING = 8;
	private static final int PANEL_HEIGHT = 204;

	private Button autoRefillButton;
	private Button autoEatButton;
	private Button thresholdButton;
	private Button autoBridgeButton;
	private int panelLeft;
	private int panelTop;
	private int panelWidth;

	public AutoAssistConfigScreen() {
		super(Component.translatable("screen.autoassist.config"));
	}

	@Override
	protected void init() {
		this.panelWidth = Math.min(300, this.width - 20);
		this.panelLeft = (this.width - this.panelWidth) / 2;
		this.panelTop = Math.max(8, (this.height - PANEL_HEIGHT) / 2);

		int x = this.width / 2 - BUTTON_WIDTH / 2;
		int y = this.panelTop + 52;

		autoRefillButton = this.addRenderableWidget(Button.builder(
				label("option.autoassist.auto_refill", AutoAssistConfig.isAutoRefillEnabled()),
				button -> {
					AutoAssistConfig.setAutoRefillEnabled(!AutoAssistConfig.isAutoRefillEnabled());
					AutoAssistConfig.save();
					updateLabels();
				}
		).bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());

		autoEatButton = this.addRenderableWidget(Button.builder(
				label("option.autoassist.auto_eat", AutoAssistConfig.isAutoEatEnabled()),
				button -> {
					AutoAssistConfig.setAutoEatEnabled(!AutoAssistConfig.isAutoEatEnabled());
					AutoAssistConfig.save();
					if (!AutoAssistConfig.isAutoEatEnabled()) {
						AutoEat.stop(this.minecraft);
					}
					updateLabels();
				}
		).bounds(x, y + (BUTTON_HEIGHT + BUTTON_SPACING), BUTTON_WIDTH, BUTTON_HEIGHT).build());

		thresholdButton = this.addRenderableWidget(Button.builder(
				thresholdLabel(),
				button -> {
					int next = AutoAssistConfig.getEatThreshold() - 1;
					if (next < 1) {
						next = 20;
					}
					AutoAssistConfig.setEatThreshold(next);
					AutoAssistConfig.save();
					updateLabels();
				}
		).bounds(x, y + (BUTTON_HEIGHT + BUTTON_SPACING) * 2, BUTTON_WIDTH, BUTTON_HEIGHT).build());

		autoBridgeButton = this.addRenderableWidget(Button.builder(
				label("option.autoassist.auto_bridge", AutoBridge.isActive()),
				button -> {
					AutoBridge.setActive(!AutoBridge.isActive());
					updateLabels();
				}
		).bounds(x, y + (BUTTON_HEIGHT + BUTTON_SPACING) * 3, BUTTON_WIDTH, BUTTON_HEIGHT).build());

		this.addRenderableWidget(Button.builder(
				Component.translatable("gui.done"),
				button -> this.onClose()
		).bounds(x, y + (BUTTON_HEIGHT + BUTTON_SPACING) * 4, BUTTON_WIDTH, BUTTON_HEIGHT).build());

		updateLabels();
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fill(this.panelLeft, this.panelTop, this.panelLeft + this.panelWidth, this.panelTop + PANEL_HEIGHT, 0xA0000000);
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		graphics.centeredText(this.font, this.title, this.width / 2, this.panelTop + 14, 0xFFFFFFFF);
		graphics.centeredText(this.font, Component.translatable("screen.autoassist.hint"), this.width / 2,
				this.panelTop + 30, 0xFFA8A8A8);
	}

	private void updateLabels() {
		autoRefillButton.setMessage(label("option.autoassist.auto_refill", AutoAssistConfig.isAutoRefillEnabled()));
		autoEatButton.setMessage(label("option.autoassist.auto_eat", AutoAssistConfig.isAutoEatEnabled()));
		thresholdButton.setMessage(thresholdLabel());
		autoBridgeButton.setMessage(label("option.autoassist.auto_bridge", AutoBridge.isActive()));
	}

	private static Component label(String key, boolean enabled) {
		return Component.translatable(key)
				.append(": ")
				.append(Component.translatable(enabled ? "option.autoassist.on" : "option.autoassist.off"));
	}

	private static Component thresholdLabel() {
		return Component.translatable("option.autoassist.eat_threshold", AutoAssistConfig.getEatThreshold());
	}
}
