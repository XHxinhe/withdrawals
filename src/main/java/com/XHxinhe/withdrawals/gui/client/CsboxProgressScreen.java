package com.XHxinhe.withdrawals.gui.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.XHxinhe.withdrawals.component.ModComponents;
import com.XHxinhe.withdrawals.item.ItemCsgoBox;
import com.XHxinhe.withdrawals.packet.ModPackets;
import com.XHxinhe.withdrawals.packet.PacketGiveItem;
import com.XHxinhe.withdrawals.screen.CsboxScreenHandler;
import com.XHxinhe.withdrawals.sounds.ModSounds;
import com.XHxinhe.withdrawals.util.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries; // 导入 Registries
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.security.SecureRandom;
import java.util.*;

@Environment(EnvType.CLIENT)
public class CsboxProgressScreen extends HandledScreen<CsboxScreenHandler> {

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

    public CsboxProgressScreen(CsboxScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, Text.empty());
        this.entity = inventory.player;
        this.world = entity.getWorld();
        this.boxStack = this.entity.getStackInHand(Hand.MAIN_HAND);

        if (this.boxStack.getItem() instanceof ItemCsgoBox box) {
            this.itemList = box.getItemGroup(this.boxStack);
        } else {
            this.itemList = new HashMap<>();
        }

        if (this.itemList == null) this.itemList = new HashMap<>();
        this.velocityExport = renderCount();
        this.playerInventoryTitleY = 1000;
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = 1000;
        this.startWidth = this.width;
        BlurHandler.updateShaderState(true);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // 空
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        if (this.client != null && this.client.world != null) {
            context.fillGradient(0, 0, this.width, this.height, BlurHandler.getBackgroundColor(), BlurHandler.getBackgroundColor());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.client != null && this.client.player != null) {
            if (this.client.player.isAlive() && !this.client.player.isRemoved()) {
                this.containerTick();
            } else {
                this.close();
            }
        } else {
            this.close();
            return;
        }

        this.drawBackground(context, delta, mouseX, mouseY);
        if (this.client != null) {
            this.client.options.hudHidden = true;
        }

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        if (openTime >= 5 && !gradeInput.isEmpty() && !itemInput.isEmpty()) {
            float widthNewAdd = RenderWidthAdd;
            if (this.width != startWidth) {
                widthNewAdd *= this.width / startWidth;
            }

            float progress = Math.min(1, delta + velocityLerp);

            for (int i = 0; i < 50; i++) {
                int grade = gradeInput.get(i);
                ItemStack itemStack = itemInput.get(i);
                IconListTools.renderItemProgress(
                        this.entity, context, itemStack,
                        this.width * randomWidth / 100F + i * this.width * 20F / 100F - MathHelper.lerp(progress, lastRenderWidth, widthNewAdd),
                        this.height * 37F / 100F, this.width, this.height, grade
                );
            }

            lastRenderWidth = widthNewAdd;

            int goldLineTo = this.height * 37 / 100 + height * 25 / 100;
            context.fill((int)(this.width / 2F), (int)(this.height * 37 / 100F), (int)(width / 2F + 2), goldLineTo + 2, ColorTools.argbColor(128, 255, 215, 0));
        }

        RenderSystem.disableBlend();
        RenderSystem.enableBlend();
        context.drawTexture(new Identifier("withdrawals", "textures/screens/csgo_background.png"), 0, 0, 0, 0, this.width, this.height, this.width, this.height);
        RenderSystem.disableBlend();

        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    /**
     * --- 这是核心修改 ---
     * 生成50个随机物品用于动画展示。
     * 通过对物品列表进行排序，确保客户端和服务器使用相同种子时，生成的物品序列完全一致。
     */
    public void renderGradeItems() {
        seedBlender.setSeed(System.nanoTime());
        seed = seedBlender.nextLong();
        Random rng = new Random(seed);

        gradeInput.clear();
        itemInput.clear();

        // --- 关键修改开始 ---
        // 1. 将原始的 itemList (HashMap) 的条目放入一个 List 中，以便排序。
        List<Map.Entry<ItemStack, Integer>> sortedEntries = new ArrayList<>(this.itemList.entrySet());

        // 2. 对 List 进行排序。我们使用物品的注册表ID作为排序依据，
        //    这是一个稳定且唯一的标识符 (例如 "minecraft:diamond_sword")。
        //    这确保了无论在客户端还是服务器，物品的顺序都是固定的。
        sortedEntries.sort(Comparator.comparing(entry ->
                Registries.ITEM.getId(entry.getKey().getItem()).toString()
        ));

        // 3. 创建一个新的 LinkedHashMap 来保持排序后的顺序。
        //    虽然你的 RandomItem 工具类可能不直接利用这个顺序，但这是最佳实践。
        //    我们将把这个排好序的列表传递给随机选择逻辑。
        Map<ItemStack, Integer> sortedItemList = new LinkedHashMap<>();
        for (Map.Entry<ItemStack, Integer> entry : sortedEntries) {
            sortedItemList.put(entry.getKey(), entry.getValue());
        }
        // --- 关键修改结束 ---

        for (int i = 0; i < 50; i++) {
            int grade = RandomItem.randomItemsGrade(rng, ItemCsgoBox.getRandom(boxStack), this.entity);
            // 使用排好序的 sortedItemList 来选择物品
            ItemStack itemStack = RandomItem.randomItems(rng, grade, sortedItemList);
            gradeInput.add(grade);
            itemInput.add(itemStack);
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

            // 将种子发送给服务器，服务器会用同样的逻辑生成物品列表
            ClientPlayNetworking.send(ModPackets.GIVE_ITEM_ID, new PacketGiveItem(seed).toBuf());

            if (itemInput.size() > 45 && gradeInput.size() > 45) {
                ModComponents.CSBOX_COMPONENT.maybeGet(this.entity).ifPresent(csbox -> {
                    csbox.setItem(itemInput.get(45));
                    csbox.setGrade(gradeInput.get(45));
                });
            }
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
        if (keyCode == 256) { // ESC key
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        BlurHandler.updateShaderState(false);
        if (this.client != null) {
            this.client.options.hudHidden = false;
        }
        super.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}