package com.beta1122.autoassist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Persistent client settings for Auto Assist.
 */
public final class AutoAssistConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger("autoassist");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance()
			.getConfigDir()
			.resolve("autoassist.json");

	private static boolean autoRefillEnabled = true;
	private static boolean autoEatEnabled = true;
	private static int eatThreshold = 16;
	private static boolean autoBridgeEnabled;

	private AutoAssistConfig() {
	}

	public static void load() {
		if (!Files.isRegularFile(CONFIG_PATH)) {
			return;
		}

		try {
			JsonObject object = JsonParser.parseString(Files.readString(CONFIG_PATH, StandardCharsets.UTF_8))
					.getAsJsonObject();

			if (object.has("autoRefill")) {
				autoRefillEnabled = object.get("autoRefill").getAsBoolean();
			}
			if (object.has("autoEat")) {
				autoEatEnabled = object.get("autoEat").getAsBoolean();
			}
			if (object.has("eatThreshold")) {
				eatThreshold = clampThreshold(object.get("eatThreshold").getAsInt());
			}
			if (object.has("autoBridge")) {
				autoBridgeEnabled = object.get("autoBridge").getAsBoolean();
			}
		} catch (IOException | RuntimeException exception) {
			LOGGER.warn("Could not read {}; keeping defaults.", CONFIG_PATH, exception);
		}
	}

	public static void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			JsonObject object = new JsonObject();
			object.addProperty("autoRefill", autoRefillEnabled);
			object.addProperty("autoEat", autoEatEnabled);
			object.addProperty("eatThreshold", eatThreshold);
			object.addProperty("autoBridge", autoBridgeEnabled);
			Files.writeString(CONFIG_PATH, GSON.toJson(object), StandardCharsets.UTF_8);
		} catch (IOException exception) {
			LOGGER.warn("Could not save {}.", CONFIG_PATH, exception);
		}
	}

	public static boolean isAutoRefillEnabled() {
		return autoRefillEnabled;
	}

	public static void setAutoRefillEnabled(boolean value) {
		autoRefillEnabled = value;
	}

	public static boolean isAutoEatEnabled() {
		return autoEatEnabled;
	}

	public static void setAutoEatEnabled(boolean value) {
		autoEatEnabled = value;
	}

	public static int getEatThreshold() {
		return eatThreshold;
	}

	public static void setEatThreshold(int value) {
		eatThreshold = clampThreshold(value);
	}

	public static boolean isAutoBridgeEnabled() {
		return autoBridgeEnabled;
	}

	public static void setAutoBridgeEnabled(boolean value) {
		autoBridgeEnabled = value;
	}

	private static int clampThreshold(int value) {
		return Math.max(1, Math.min(20, value));
	}
}
