package net.enderkitty.mixin;

import net.enderkitty.Helitra;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ElytraFlightController;
import net.minecraft.util.math.MathHelper;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value = EnvType.CLIENT)
@Mixin(ElytraFlightController.class)
public class ElytraEntityModelMixin {
    @Shadow @Final private LivingEntity entity;
    @Shadow private float leftWingRoll;
    
    @Redirect(method = "update", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/mob/ElytraFlightController;leftWingRoll:F", opcode = Opcodes.PUTFIELD))
    private void helicopter(ElytraFlightController instance, float value) {
        if (Helitra.HELICOPTER_ENABLED) {
            float l = (float) (-Math.PI / 12);
            if (this.entity.isGliding()) {
                float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false);
                l = MathHelper.abs((float) (-Math.PI * 2.0 / 9.0) - this.entity.limbAnimator.getSpeed(tickDelta) * this.entity.limbAnimator.getPos(tickDelta));
            } else if (this.entity.isInSneakingPose()) {
                l = (float) (-Math.PI / 4);
            }
            
            this.leftWingRoll = this.leftWingRoll + (l - this.leftWingRoll) * 0.1f;
            

        } else {
            this.leftWingRoll = value;
            
        }
        
    }
    
    @Mixin(ElytraEntityModel.class)
    public static class ElytraEntityModelMixinTwo {
        @Shadow @Final private ModelPart leftWing;
        @Shadow @Final private ModelPart rightWing;
        
        @Redirect(method = "setAngles(Lnet/minecraft/client/render/entity/state/BipedEntityRenderState;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelPart;roll:F", opcode = Opcodes.PUTFIELD, ordinal = 1))
        private void rightWingRoll(ModelPart rightWing, float value) {
            if (Helitra.HELICOPTER_ENABLED) {
                AbstractClientPlayerEntity player = MinecraftClient.getInstance().player;
                if (player != null && player.isGliding()) rightWing.roll = this.leftWing.roll + 180;
                else rightWing.roll = value;
            } else {
                rightWing.roll = value;
            }
        }
        
        @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/BipedEntityRenderState;)V", at = @At(value = "TAIL"))
        private void xPivot(BipedEntityRenderState bipedEntityRenderState, CallbackInfo ci) {
            if (Helitra.HELICOPTER_ENABLED) {
                AbstractClientPlayerEntity player = MinecraftClient.getInstance().player;
                if (player != null && player.isGliding()) {
                    this.leftWing.pivotX = 0;
                } else {
                    this.leftWing.pivotX = 5.0f;
                }
            } else {
                this.leftWing.pivotX = 5.0f;
            }
            this.rightWing.pivotX = -this.leftWing.pivotX;
        }
    }
}
