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

// 标注这是客户端环境的代码
@Environment(EnvType.CLIENT)
// 定义箱子开箱进度显示界面类，继承自HandledScreen
public class CsboxProgressScreen extends HandledScreen<CsboxScreenHandler> {

    // 游戏世界实例
    private final World world;
    // 玩家实体
    private final PlayerEntity entity;
    // 玩家手中的箱子物品
    private final ItemStack boxStack;
    // 箱子可能掉落的物品列表及其权重
    public Map<ItemStack, Integer> itemList;
    // 用于动画展示的物品列表
    private final List<ItemStack> itemInput = new ArrayList<>();
    // 物品等级列表
    private final List<Integer> gradeInput = new ArrayList<>();
    // 随机数种子
    private long seed;
    // 用于生成安全随机数的实例
    private final SecureRandom seedBlender = new SecureRandom();
    // 存储动画速度的列表
    private final List<Float> velocityExport;
    // 上一帧的渲染宽度
    private float lastRenderWidth = 0F;
    // 开始开箱的标志
    private boolean startSwitch = true;
    // 速度插值
    private float velocityLerp = 0;
    // 声音播放的累积宽度
    private float soundWidthAdd = 0;
    // 随机数生成器
    private static final Random random = new Random();
    // 随机起始位置（93.5到111之间的随机值）
    private final float randomWidth = 93.5F + random.nextFloat() * (111F - 93.5F);
    // 开始时间计数器
    private int startTime = 0;
    // 初始界面宽度
    private float startWidth;
    // 渲染位置的累积偏移量
    private float RenderWidthAdd = 0F;
    // 存储渲染位置的列表
    private List<Float> renderExport;
    // 开箱时间计数器
    private int openTime;

    // 构造函数
    public CsboxProgressScreen(CsboxScreenHandler handler, PlayerInventory inventory, Text title) {
        // 调用父类构造函数
        super(handler, inventory, Text.empty());
        // 初始化玩家实体
        this.entity = inventory.player;
        // 获取玩家所在世界
        this.world = entity.getWorld();
        // 获取玩家主手中的物品
        this.boxStack = this.entity.getStackInHand(Hand.MAIN_HAND);

        // 如果手中物品是CSGO箱子，获取其物品组
        if (this.boxStack.getItem() instanceof ItemCsgoBox box) {
            this.itemList = box.getItemGroup(this.boxStack);
        } else {
            this.itemList = new HashMap<>();
        }

        // 确保itemList不为空
        if (this.itemList == null) this.itemList = new HashMap<>();
        // 计算动画速度
        this.velocityExport = renderCount();
        // 隐藏玩家物品栏标题
        this.playerInventoryTitleY = 1000;
    }

    // 初始化界面
    @Override
    protected void init() {
        super.init();
        // 隐藏标题
        this.titleX = 1000;
        // 记录初始宽度
        this.startWidth = this.width;
        // 启用模糊效果
        BlurHandler.updateShaderState(true);
    }

    // 绘制前景
    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // 空实现
    }

    // 绘制背景
    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        // 如果客户端和世界存在，绘制渐变背景
        if (this.client != null && this.client.world != null) {
            context.fillGradient(0, 0, this.width, this.height,
                    BlurHandler.getBackgroundColor(), BlurHandler.getBackgroundColor());
        }
    }

    // 渲染界面
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 检查客户端和玩家状态
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

        // 绘制背景
        this.drawBackground(context, delta, mouseX, mouseY);
        // 隐藏HUD
        if (this.client != null) {
            this.client.options.hudHidden = true;
        }

        // 设置渲染状态
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // 渲染物品滚动动画
        if (openTime >= 5 && !gradeInput.isEmpty() && !itemInput.isEmpty()) {
            float widthNewAdd = RenderWidthAdd;
            // 调整宽度比例
            if (this.width != startWidth) {
                widthNewAdd *= this.width / startWidth;
            }

            // 计算动画进度
            float progress = Math.min(1, delta + velocityLerp);

            // 渲染50个物品
            for (int i = 0; i < 50; i++) {
                int grade = gradeInput.get(i);
                ItemStack itemStack = itemInput.get(i);
                // 渲染每个物品
                IconListTools.renderItemProgress(
                        this.entity, context, itemStack,
                        this.width * randomWidth / 100F + i * this.width * 20F / 100F
                                - MathHelper.lerp(progress, lastRenderWidth, widthNewAdd),
                        this.height * 37F / 100F, this.width, this.height, grade
                );
            }

            // 更新上一帧渲染宽度
            lastRenderWidth = widthNewAdd;

            // 绘制中央金色指示线
            int goldLineTo = this.height * 37 / 100 + height * 25 / 100;
            context.fill(
                    (int)(this.width / 2F),
                    (int)(this.height * 37 / 100F),
                    (int)(width / 2F + 2),
                    goldLineTo + 2,
                    ColorTools.argbColor(128, 255, 215, 0)
            );
        }

        // 渲染背景纹理
        RenderSystem.disableBlend();
        RenderSystem.enableBlend();
        context.drawTexture(
                new Identifier("withdrawals", "textures/screens/csgo_background.png"),
                0, 0, 0, 0, this.width, this.height, this.width, this.height
        );
        RenderSystem.disableBlend();

        // 绘制鼠标提示
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    // 生成随机物品序列
    public void renderGradeItems() {
        // 设置随机种子
        seedBlender.setSeed(System.nanoTime());
        seed = seedBlender.nextLong();
        Random rng = new Random(seed);

        // 清空之前的列表
        gradeInput.clear();
        itemInput.clear();

        // 将物品列表转换为可排序的形式
        List<Map.Entry<ItemStack, Integer>> sortedEntries =
                new ArrayList<>(this.itemList.entrySet());

        // 按物品ID排序
        sortedEntries.sort(Comparator.comparing(entry ->
                Registries.ITEM.getId(entry.getKey().getItem()).toString()
        ));

        // 创建有序的物品列表
        Map<ItemStack, Integer> sortedItemList = new LinkedHashMap<>();
        for (Map.Entry<ItemStack, Integer> entry : sortedEntries) {
            sortedItemList.put(entry.getKey(), entry.getValue());
        }

        // 生成50个随机物品
        for (int i = 0; i < 50; i++) {
            int grade = RandomItem.randomItemsGrade(
                    rng, ItemCsgoBox.getRandom(boxStack), this.entity
            );
            ItemStack itemStack = RandomItem.randomItems(rng, grade, sortedItemList);
            gradeInput.add(grade);
            itemInput.add(itemStack);
        }
    }

    // 处理界面更新
    public void containerTick() {
        openTime++;
        // 在第2帧生成物品
        if (openTime == 2) {
            renderGradeItems();
        }
        if (openTime < 2) {
            return;
        }

        // 首次运行时的处理
        if (startSwitch) {
            startSwitch = false;
            this.renderExport = renderMove(this.velocityExport);

            // 向服务器发送种子
            ClientPlayNetworking.send(
                    ModPackets.GIVE_ITEM_ID,
                    new PacketGiveItem(seed).toBuf()
            );

            // 设置最终获得的物品
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

        // 更新动画时间
        if (startTime < 145) {
            startTime++;
        }

        // 动画结束时切换界面
        if (startTime >= 145) {
            if (this.client != null) {
                this.client.setScreen(new CsLookItemScreen());
            }
            return;
        }

        // 更新动画参数
        velocityLerp = velocityExport.get(startTime) / 35;
        RenderWidthAdd = renderExport.get(startTime);

        // 处理声音效果
        float currentPos = RenderWidthAdd;
        float startSoundZone = startWidth * randomWidth / 100F - startWidth / 2F;

        if (currentPos > startSoundZone) {
            soundWidthAdd += (startWidth / 173F * velocityExport.get(startTime));
            if (soundWidthAdd > (startWidth * 20F / 100F)) {
                soundWidthAdd = 0;
                // 播放滚动音效
                if (this.world != null && this.entity != null) {
                    this.world.playSound(
                            this.entity,
                            this.entity.getX(),
                            this.entity.getY(),
                            this.entity.getZ(),
                            ModSounds.CS_DITA,
                            SoundCategory.NEUTRAL,
                            10F,
                            1F
                    );
                }
            }
        }
    }

    // 计算动画速度
    public List<Float> renderCount() {
        List<Float> renderMove = new ArrayList<>();
        for (int i = 0; i < 150; i++) {
            float time = i / 20F;
            // 使用数学公式计算速度变化
            float velocity = (1.6F * time + 0.8F) /
                    ((float) Math.pow(2, 1.5 * time - 5.2));
            renderMove.add(Math.max(0, velocity));
        }
        return renderMove;
    }

    // 计算渲染位置
    public List<Float> renderMove(List<Float> list) {
        List<Float> renderMove = new ArrayList<>();
        float currentRenderWidthAdd = 0;
        // 根据速度计算位置
        for (Float velocity : list) {
            currentRenderWidthAdd += startWidth / 173F * velocity;
            renderMove.add(currentRenderWidthAdd);
        }
        return renderMove;
    }

    // 处理按键事件
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // ESC键关闭界面
        if (keyCode == 256) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // 关闭界面
    @Override
    public void close() {
        // 关闭模糊效果
        BlurHandler.updateShaderState(false);
        // 恢复HUD显示
        if (this.client != null) {
            this.client.options.hudHidden = false;
        }
        super.close();
    }

    // 设置界面不暂停游戏
    @Override
    public boolean shouldPause() {
        return false;
    }
}