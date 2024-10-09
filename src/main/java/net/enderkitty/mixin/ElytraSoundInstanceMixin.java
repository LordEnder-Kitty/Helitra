package net.enderkitty.mixin;

import net.enderkitty.HelicopterSoundInstance;
import net.enderkitty.Helitra;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.ElytraSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(value = EnvType.CLIENT)
@Mixin(ClientPlayerEntity.class)
public class ElytraSoundInstanceMixin {
    
    @Redirect(method = "onTrackedDataSet", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sound/SoundManager;play(Lnet/minecraft/client/sound/SoundInstance;)V"))
    private void sound(SoundManager soundManager, SoundInstance sound) {
        ClientPlayerEntity thisObject = (ClientPlayerEntity)(Object)this;
        soundManager.play(Helitra.SOUNDS_ENABLED ? new HelicopterSoundInstance(thisObject) : new ElytraSoundInstance(thisObject));
    }
}
