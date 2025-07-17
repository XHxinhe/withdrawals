package com.XHxinhe.withdrawals.gui.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.XHxinhe.withdrawals.component.ModComponents;
import com.XHxinhe.withdrawals.item.ItemCsgoBox;
import com.XHxinhe.withdrawals.packet.ModPackets;
import com.XHxinhe.withdrawals.packet.PacketGiveItem;
import com.XHxinhe.withdrawals.gui.CsboxScreenHandler; // 新增导入
import com.XHxinhe.withdrawals.sounds.ModSounds;
import com.XHxinhe.withdrawals.util.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider; // 新增导入
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.security.SecureRandom;
import java.util.*;

@Environment(EnvType.CLIENT)
public class CsboxProgressScreen extends Screen implements ScreenHandlerProvider<CsboxScreenHandler> {
    // 新增字段和方法实现 ScreenHandlerProvider
    private CsboxScreenHandler handler;

    public void setHandler(CsboxScreenHandler handler) {
        this.handler = handler;
    }

    @Override
    public CsboxScreenHandler getScreenHandler() {
        return handler;
    }

    private final World world;
    private final PlayerEntity entity;
    private final ItemStack boxStack;
    public Map<ItemStack, Integer> itemList;
    private final List<ItemStack> itemInput = new ArrayList<>();
    private final List<Integer> gradeInput = new ArrayList<>();

    private long seed;
    private final SecureRandom seedBlender = new SecureRandom();
    private final List<Float> velocityExport;

    private float lastRenderWidth = 0F;
    private boolean startSwitch = true;
    private float velocityLerp = 0;
    private float soundWidthAdd = 0;
    private static final Random random = new Random();
    private final float randomWidth = 93.5F + random.nextFloat() * (111F - 93.5F);

    private int startTime = 0;
    private float startWidth;
    private float RenderWidthAdd = 0F;
    private List<Float> renderExport;
    private int openTime;

    public CsboxProgressScreen() {
        super(Text.literal("cs_progress"));
        this.client = MinecraftClient.getInstance();

        // 确保client不为null
        if (this.client != null) {
            this.width = client.getWindow().getScaledWidth();
            this.height = client.getWindow().getScaledHeight();

            if (this.client.player != null) {
                this.entity = this.client.player;
                this.world = entity.getWorld();
                this.boxStack = this.entity.getStackInHand(Hand.MAIN_HAND);

                if (this.boxStack.getItem() instanceof ItemCsgoBox box) {
                    this.itemList = box.getItemGroup(this.boxStack);
                } else {
                    this.itemList = new HashMap<>();
                }
            } else {
                this.entity = null;
                this.world = null;
                this.boxStack = ItemStack.EMPTY;
                this.itemList = new HashMap<>();
            }
        } else {
            this.entity = null;
            this.world = null;
            this.boxStack = ItemStack.EMPTY;
            this.itemList = new HashMap<>();
        }

        // 确保字段不为null
        if (this.itemList == null) this.itemList = new HashMap<>();
        this.velocityExport = renderCount();
    }

    @Override
    protected void init() {
        super.init();
        this.startWidth = this.width;
        BlurHandler.updateShaderState(true);
    }

    public void renderGradeItems() {
        seedBlender.setSeed(System.nanoTime());
        seed = seedBlender.nextLong();
        Random rng = new Random(seed);

        for (int i = 0; i < 50; i++) {
            int grade = RandomItem.randomItemsGrade(rng, ItemCsgoBox.getRandom(boxStack), this.entity);
            ItemStack itemStack = RandomItem.randomItems(rng, grade, this.itemList);
            gradeInput.add(grade);
            itemInput.add(itemStack);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.client != null && this.client.world != null) {
            context.fillGradient(0, 0, this.width, this.height, BlurHandler.getBackgroundColor(), BlurHandler.getBackgroundColor());
        } else {
            this.renderBackground(context);
        }

        renderBg(context, delta, mouseX, mouseY);
        super.render(context, mouseX, mouseY, delta);
    }

    protected void renderBg(DrawContext context, float partialTicks, int gx, int gy) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        if (this.client != null) {
            this.client.options.hudHidden = true;
        }

        if (openTime < 5) {
            return;
        }

        float widthNewAdd = RenderWidthAdd;
        if (this.width != startWidth) {
            widthNewAdd *= this.width / startWidth;
        }

        float progress = Math.min(1, partialTicks + velocityLerp);

        for (int i = 0; i < 50; i++) {
            int grade = gradeInput.get(i);
            ItemStack itemStack = itemInput.get(i);
            IconListTools.renderItemProgress(this.entity, context, itemStack, this.width * randomWidth / 100F + i * this.width * 20F / 100F - MathHelper.lerp(progress, lastRenderWidth, widthNewAdd), this.height * 37F / 100F, this.width, this.height, grade);
        }

        lastRenderWidth = widthNewAdd;

        int goldLineTo = this.height * 37 / 100 + height * 25 / 100;
        context.fill((int)(this.width / 2F), (int)(this.height * 37 / 100F), (int)(width / 2F + 2), goldLineTo + 2, ColorTools.argbColor(128, 255, 215, 0));
        RenderSystem.disableBlend();

        RenderSystem.enableBlend();
        context.drawTexture(new Identifier("withdrawals", "textures/screens/csgo_background.png"), 0, 0, 0, 0, this.width, this.height, this.width, this.height);
        RenderSystem.disableBlend();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.client == null || this.client.player == null) {
            this.close();
            return;
        }

        if (this.client.player.isAlive() && !this.client.player.isRemoved()) {
            this.containerTick();
        } else {
            this.close();
        }
    }

    public void containerTick() {
        openTime++;
        if (openTime == 2) {
            renderGradeItems();
        }
        if (openTime < 2) {
            return;
        }

        if (startSwitch) {
            startSwitch = false;
            this.renderExport = renderMove(this.velocityExport);

            ClientPlayNetworking.send(ModPackets.GIVE_ITEM_ID, new PacketGiveItem(seed).toBuf());

            ModComponents.CSBOX_COMPONENT.maybeGet(this.entity).ifPresent(csbox -> {
                csbox.setItem(itemInput.get(45));
                csbox.setGrade(gradeInput.get(45));
            });
        }

        if (openTime < 5) {
            return;
        }

        if (startTime < 145) {
            startTime++;
        }

        if (startTime >= 145) {
            if (this.client != null) {
                this.client.setScreen(new CsLookItemScreen());
            }
            return;
        }

        velocityLerp = velocityExport.get(startTime) / 35;
        RenderWidthAdd = renderExport.get(startTime);

        float currentPos = RenderWidthAdd;
        float startSoundZone = startWidth * randomWidth / 100F - startWidth / 2F;

        if (currentPos > startSoundZone) {
            soundWidthAdd += (startWidth / 173F * velocityExport.get(startTime));
            if (soundWidthAdd > (startWidth * 20F / 100F)) {
                soundWidthAdd = 0;
                if (this.world != null && this.entity != null) {
                    this.world.playSound(this.entity, this.entity.getX(), this.entity.getY(), this.entity.getZ(), ModSounds.CS_DITA, SoundCategory.NEUTRAL, 10F, 1F);
                }
            }
        }
    }

    public List<Float> renderCount() {
        List<Float> renderMove = new ArrayList<>();
        for (int i = 0; i < 150; i++) {
            float time = i / 20F;
            float velocity = (1.6F * time + 0.8F) / ((float) Math.pow(2, 1.5 * time - 5.2));
            renderMove.add(Math.max(0, velocity));
        }
        return renderMove;
    }

    public List<Float> renderMove(List<Float> list) {
        List<Float> renderMove = new ArrayList<>();
        float currentRenderWidthAdd = 0;
        for (Float velocity : list) {
            currentRenderWidthAdd += startWidth / 173F * velocity;
            renderMove.add(currentRenderWidthAdd);
        }
        return renderMove;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (keyCode == 256) { // ESC key
            this.close();
            return true;
        }
        return false;
    }

    @Override
    public void close() {
        BlurHandler.updateShaderState(false);
        if (this.client != null) {
            this.client.options.hudHidden = false;
            if (this.client.player != null) {
                this.client.player.closeHandledScreen();
            }
        }
        super.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}