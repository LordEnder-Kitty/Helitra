package net.enderkitty.mixin;

import net.enderkitty.Helitra;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import net.minecraft.util.math.MathHelper;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(value = EnvType.CLIENT)
@Mixin(ElytraEntityModel.class)
public class ElytraEntityModelMixin {
    @Shadow @Final private ModelPart leftWing;
    @Shadow @Final private ModelPart rightWing;
    
    @Redirect(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;elytraRoll:F", opcode = Opcodes.PUTFIELD))
    private void helicopter(AbstractClientPlayerEntity player, float value) {
        if (Helitra.HELICOPTER_ENABLED) {
            float l = (float) (-Math.PI / 12);
            if (player.isFallFlying()) {
                float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false);
                l = MathHelper.abs((float) (-Math.PI * 2.0 / 9.0) - player.limbAnimator.getSpeed(tickDelta) * player.limbAnimator.getPos(tickDelta));
            } else if (player.isInSneakingPose()) {
                l = (float) (-Math.PI / 4);
            }

            player.elytraRoll = player.elytraRoll + (l - player.elytraRoll) * 0.1f;

            if (player.isFallFlying()) {
                this.leftWing.pivotX = 0;
            } else {
                this.leftWing.pivotX = 5.0f;
            }
        } else {
            player.elytraRoll = value;
            this.leftWing.pivotX = 5.0f;
        }
        this.rightWing.pivotX = -this.leftWing.pivotX;
    }
    
    @Redirect(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelPart;roll:F", opcode = Opcodes.PUTFIELD, ordinal = 2))
    private void rightWingRoll(ModelPart rightWing, float value) {
        if (Helitra.HELICOPTER_ENABLED) {
            AbstractClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null && player.isFallFlying()) rightWing.roll = this.leftWing.roll + 180;
            else rightWing.roll = value;
        } else {
            rightWing.roll = value;
        }
    }
}
