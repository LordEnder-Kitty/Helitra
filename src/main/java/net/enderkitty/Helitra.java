package net.enderkitty;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Helitra implements ClientModInitializer {
	public static final String MOD_ID = "helitra";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static boolean HELICOPTER_ENABLED = true;
	public static boolean SOUNDS_ENABLED = true;
    public static final File CONFIG = new File(FabricLoader.getInstance().getConfigDir().toFile(), MOD_ID + ".txt");
    public static final SoundEvent ITEM_ELYTRA_HELICOPTER = Registry.register(Registries.SOUND_EVENT, Identifier.of(MOD_ID, "item.elytra.helicopter"), SoundEvent.of(Identifier.of(MOD_ID, "item.elytra.helicopter")));
    public static boolean MUTED = false;
    
	@Override
	public void onInitializeClient() {
        Helitra.loadConfig();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("helicopter").executes(context -> {
            HELICOPTER_ENABLED = !HELICOPTER_ENABLED;
            if (HELICOPTER_ENABLED) context.getSource().getPlayer().sendMessage(Text.translatable("text.helitra.helicopter.enabled"), false);
            if (!HELICOPTER_ENABLED) context.getSource().getPlayer().sendMessage(Text.translatable("text.helitra.helicopter.disabled"), false);
            Helitra.saveConfig(HELICOPTER_ENABLED, SOUNDS_ENABLED);
            return 1;
        }).then(ClientCommandManager.literal("helicopter").executes(context -> {
            SOUNDS_ENABLED = !SOUNDS_ENABLED;
            if (SOUNDS_ENABLED) context.getSource().getPlayer().sendMessage(Text.translatable("text.helitra.sounds.enabled"), false);
            if (!SOUNDS_ENABLED) context.getSource().getPlayer().sendMessage(Text.translatable("text.helitra.sounds.disabled"), false);
            Helitra.saveConfig(HELICOPTER_ENABLED, SOUNDS_ENABLED);
            return 1;
        }).then(ClientCommandManager.literal("helicopter").executes(context -> {
            Helitra.loadConfig();
            context.getSource().getPlayer().sendMessage(Text.translatable("text.helitra.refreshConfig"), false);
            return 1;
        })))));
	}
    
    public static void loadConfig() {
        if (CONFIG.exists()) {
            try {
                String contents = Files.readString(CONFIG.toPath());
                if (contents.contains("helicopter = true")) {
                    HELICOPTER_ENABLED = true;
                } else if (contents.contains("helicopter = false")) {
                    HELICOPTER_ENABLED = false;
                }
                if (contents.contains("sounds = true")) {
                    SOUNDS_ENABLED = true;
                } else if (contents.contains("sounds = false")) {
                    SOUNDS_ENABLED = false;
                }
            } catch (IOException ignored) {}
        } else {
            createConfig();
        }
    }
    public static void saveConfig(boolean helicopter, boolean sounds) {
        String contents = "helicopter = " + helicopter + "\nsounds = " + sounds;
        try {
            if (CONFIG.exists()) {
                Files.writeString(CONFIG.toPath(), contents);
                loadConfig();
            } else {
                LOGGER.warn("Failed to save config because no config exists. Attempting to create one.");
                createConfig();
                saveConfig(helicopter, sounds);
            }
        } catch (IOException ignored) {}
    }
    public static void createConfig() {
        try {
            if (!CONFIG.exists()) {
                Files.createFile(CONFIG.toPath());
                if (CONFIG.exists()) {
                    saveConfig(true, true);
                    LOGGER.info("Created config");
                } else LOGGER.error("Failed to create config!");
            } else LOGGER.warn("Cannot create config. A config already exists!");
        } catch (IOException ignored) {}
    }
}
