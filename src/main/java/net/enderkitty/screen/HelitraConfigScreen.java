package net.enderkitty.screen;

import net.enderkitty.Helitra;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.TickableSoundInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

@Environment(value = EnvType.CLIENT)
public class HelitraConfigScreen extends Screen {
    public static final Text TITLE = Text.translatable("config.helitra.title");
    private static final Identifier DOGGO_BACKGROUND = Identifier.of(Helitra.MOD_ID, "config/doggo_background");
    private static final ButtonTextures MUTE_TEXTURES = new ButtonTextures(
            Identifier.of(Helitra.MOD_ID, "widget/mute"),
            Identifier.of(Helitra.MOD_ID, "widget/mute_disabled"),
            Identifier.of(Helitra.MOD_ID, "widget/mute_highlighted")
    );
    private static final ButtonTextures MUTED_TEXTURES = new ButtonTextures(
            Identifier.of(Helitra.MOD_ID, "widget/muted"),
            Identifier.of(Helitra.MOD_ID, "widget/muted_disabled"),
            Identifier.of(Helitra.MOD_ID, "widget/muted_highlighted")
    );
    private final HelitraConfigSoundInstance SOUND_INSTANCE = new HelitraConfigSoundInstance(
            Helitra.ITEM_ELYTRA_HELICOPTER.id(), SoundCategory.MASTER, 1, 1, SoundInstance.createRandom(),
            SoundInstance.AttenuationType.NONE, 0.0, 0.0, 0.0, true);
    private final @Nullable Screen parent;
    private int rotationTick = 0;
    private boolean isSoundPlaying = false;
    
    public HelitraConfigScreen(@Nullable Screen parent) {
        super(TITLE);
        this.parent = parent;
    }
    
    @Override
    protected void init() {
        super.init();
        
        CyclingButtonWidget<Boolean> helicopter = CyclingButtonWidget.onOffBuilder(Helitra.HELICOPTER_ENABLED)
                .build(this.width / 2 - 60, this.height / 2 - 22, 120, 20, Text.translatable("config.helitra.option.helicopter"), (button, value) -> {
                    Helitra.HELICOPTER_ENABLED = value;
                    Helitra.saveConfig(Helitra.HELICOPTER_ENABLED, Helitra.SOUNDS_ENABLED);
                });
        CyclingButtonWidget<Boolean> sounds = CyclingButtonWidget.onOffBuilder(Helitra.SOUNDS_ENABLED)
                .build(this.width / 2 - 60, this.height / 2 + 2, 120, 20, Text.translatable("config.helitra.option.sounds"), (button, value) -> {
                    Helitra.SOUNDS_ENABLED = value;
                    Helitra.saveConfig(Helitra.HELICOPTER_ENABLED, Helitra.SOUNDS_ENABLED);
                });
        
        ButtonWidget done = ButtonWidget.builder(ScreenTexts.DONE, button -> this.close()).position(this.width / 2 + 4, this.height - 28).build();
        ButtonWidget refreshConfig = ButtonWidget.builder(Text.translatable("config.helitra.footer.refreshConfig"), button -> {
                    Helitra.loadConfig();
                    this.refresh();
                })
                .position(this.width / 2 - 154, this.height - 28).build();
        ButtonWidget mute = new TexturedButtonWidget(10, 10, 20, 20, Helitra.MUTED ? MUTED_TEXTURES : MUTE_TEXTURES, button -> {
            Helitra.MUTED = !Helitra.MUTED;
            this.refresh();
        });
        mute.setTooltip(Tooltip.of(Text.translatable("config.helitra.mute.tooltip")));
        
        this.addDrawableChild(helicopter);
        this.addDrawableChild(sounds);
        this.addDrawableChild(done);
        this.addDrawableChild(refreshConfig);
        this.addDrawableChild(mute);
        
        if (this.client != null && this.client.isInSingleplayer()) {
            mute.active = false;
        } else {
            if (this.client != null && !isSoundPlaying && !Helitra.MUTED) {
                this.client.getSoundManager().play(SOUND_INSTANCE);
                this.isSoundPlaying = true;
            }
        }
    }
    
    @Override
    public void tick() {
        rotationTick++;
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        
        context.drawCenteredTextWithShadow(this.textRenderer, TITLE, this.width / 2, 10, 0xFFFFFFFF);
        context.drawBorder(this.width / 2 - 70, this.height / 2 - 32, 140, 64, 0xFFFFFFFF);
        
        this.drawItem(context, new ItemStack(Items.ELYTRA), this.width / 4, this.height / 2, 80, rotationTick);
        this.drawItem(context, new ItemStack(Items.ELYTRA), (this.width / 4) * 3, this.height / 2, 80, rotationTick);
    }
    
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawGuiTexture(RenderLayer::getGuiTextured, DOGGO_BACKGROUND, 0, 0, this.width, this.height);
        this.applyBlur();
        this.renderDarkening(context);
    }
    
    @Override
    public void close() {
        SOUND_INSTANCE.setDone();
        this.isSoundPlaying = false;
        if (this.client != null) this.client.setScreen(this.parent);
    }
    
    private void refresh() {
        SOUND_INSTANCE.setDone();
        this.isSoundPlaying = false;
        if (this.client != null) this.client.setScreen(new HelitraConfigScreen(this.parent));
    }
    
    private void drawItem(DrawContext context, ItemStack stack, int x, int y, float scale, float rotate) {
        if (!stack.isEmpty() && this.client != null) {
            BakedModel bakedModel = this.client.getItemRenderer().getModel(stack, this.client.world, this.client.player, 0);
            context.getMatrices().push();
            context.getMatrices().translate((float)(x + 8), (float)(y + 8), (float)(150));
            context.getMatrices().multiply(new Quaternionf().rotateZ(rotate));
            
            try {
                context.getMatrices().scale(scale, -scale, scale);
                boolean bl = !bakedModel.isSideLit();
                if (bl) {
                    DiffuseLighting.disableGuiDepthLighting();
                }
                
                if (stack.isIn(ItemTags.BUNDLES)) {
                    this.client
                            .getItemRenderer()
                            .renderBundle(
                                    stack, ModelTransformationMode.GUI, false, context.getMatrices(), MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers(), 15728880, OverlayTexture.DEFAULT_UV, bakedModel, this.client.world, this.client.player, 0
                            );
                } else {
                    this.client
                            .getItemRenderer()
                            .renderItem(stack, ModelTransformationMode.GUI, false, context.getMatrices(), MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers(), 15728880, OverlayTexture.DEFAULT_UV, bakedModel);
                }
                
                context.draw();
                if (bl) {
                    DiffuseLighting.enableGuiDepthLighting();
                }
            } catch (Throwable var12) {
                CrashReport crashReport = CrashReport.create(var12, "Rendering item");
                CrashReportSection crashReportSection = crashReport.addElement("Item being rendered");
                crashReportSection.add("Item Type", () -> String.valueOf(stack.getItem()));
                crashReportSection.add("Item Components", () -> String.valueOf(stack.getComponents()));
                crashReportSection.add("Item Foil", () -> String.valueOf(stack.hasGlint()));
                throw new CrashException(crashReport);
            }
            
            context.getMatrices().pop();
        }
    }
    
    private static class HelitraConfigSoundInstance extends AbstractSoundInstance implements TickableSoundInstance {
        private boolean done;
        
        public HelitraConfigSoundInstance(Identifier id, SoundCategory category, float volume, float pitch, Random random, 
                                          SoundInstance.AttenuationType attenuationType, double x, double y, double z, boolean relative) {
            super(id, category, random);
            
            this.volume = volume;
            this.pitch = pitch;
            this.x = x;
            this.y = y;
            this.z = z;
            this.attenuationType = attenuationType;
            this.relative = relative;
            
            this.repeat = true;
            this.repeatDelay = 0;
        }
        
        @Override
        public boolean isDone() {
            return this.done;
        }
        public final void setDone() {
            this.done = true;
            this.repeat = false;
        }
        
        @Override public void tick() {}
    }
}
