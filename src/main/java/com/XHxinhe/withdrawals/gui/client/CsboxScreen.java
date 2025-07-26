// 文件路径: src/main/java/com/XHxinhe/withdrawals/gui/client/CsboxScreen.java

// 定义包名
package com.XHxinhe.withdrawals.gui.client;

// 导入所需的类
import com.XHxinhe.withdrawals.gui.widget.TexturedButtonWithText; // 导入自定义的带文本的纹理按钮
import com.XHxinhe.withdrawals.util.IconListTools; // 导入图标列表工具类
import com.mojang.blaze3d.systems.RenderSystem; // 导入Blaze3D的渲染系统，用于图形渲染控制
import com.XHxinhe.withdrawals.item.ItemCsgoBox; // 导入CSGO箱子物品类
import com.XHxinhe.withdrawals.packet.ModPackets; // 导入模组的网络数据包定义
import com.XHxinhe.withdrawals.util.BlurHandler; // 导入模糊效果处理器
import com.XHxinhe.withdrawals.util.GuiItemMove; // 导入GUI内物品移动的工具类
import net.fabricmc.api.EnvType; // 导入环境类型枚举
import net.fabricmc.api.Environment; // 导入环境注解，用于标记代码只在特定环境（客户端/服务端）加载
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking; // 导入Fabric客户端网络API
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs; // 导入Fabric网络数据包缓冲区工具
import net.minecraft.client.MinecraftClient; // 导入Minecraft客户端主类
import net.minecraft.client.gui.DrawContext; // 导入绘图上下文，用于在屏幕上绘制
import net.minecraft.client.gui.screen.Screen; // 导入屏幕基类
import net.minecraft.client.gui.widget.ButtonWidget; // 导入按钮控件
import net.minecraft.entity.player.PlayerEntity; // 导入玩家实体类
import net.minecraft.item.ItemStack; // 导入物品栈类
import net.minecraft.item.Items; // 导入原版物品类
import net.minecraft.network.PacketByteBuf; // 导入网络数据包缓冲区
import net.minecraft.registry.Registries; // 导入注册表
import net.minecraft.sound.SoundEvents; // 导入原版声音事件
import net.minecraft.text.Style; // 导入文本样式类
import net.minecraft.text.Text; // 导入文本类
import net.minecraft.util.Identifier; // 导入标识符类（用于资源定位）
import net.minecraft.util.math.random.Random; // 导入随机数生成器
import net.minecraft.world.World; // 导入世界类

import java.util.ArrayList; // 导入ArrayList类
import java.util.Collections; // 导入Collections工具类
import java.util.List; // 导入List接口
import java.util.Map; // 导入Map接口

// 标记这个类只在客户端环境下加载
@Environment(EnvType.CLIENT)
public class CsboxScreen extends Screen {

    // --- 原有字段保持不变 (Fields remain unchanged) ---
    private final World world; // 当前世界
    private final PlayerEntity entity; // 当前玩家实体
    private final ItemStack itemMenu; // 当前打开的箱子物品栈
    private final Map<ItemStack, Integer> itemGroup; // 箱子内物品及其等级的映射
    private final List<ItemStack> itemsList; // 从itemGroup处理后得到的物品列表
    private final List<Integer> gradeList; // 与itemsList对应的等级列表
    private ItemStack itemKey = ItemStack.EMPTY; // 打开箱子所需的钥匙物品栈，默认为空
    private int boxKeyCount = 0; // 玩家背包中钥匙的数量
    public float itemRotX; // 物品展示的X轴旋转角度
    public float itemRotY; // 物品展示的Y轴旋转角度
    private int gameTick = 0; // 游戏刻计时器
    private ButtonWidget openButton; // “打开”按钮的实例

    // --- 动画相关状态变量 (Animation-related state variables) ---
    private boolean isAnimating = false; // 标记是否正在播放开箱动画
    private int animationTicks = 0; // 动画播放的刻数计时器
    private float scrollOffset = 0.0f; // 物品滚动列表的偏移量
    private float scrollSpeed = 0.0f; // 物品滚动的速度
    private List<ItemStack> visualItems = new ArrayList<>(); // 动画中显示的物品列表（用于视觉效果）
    private ItemStack finalItem = null; // 服务器返回的最终抽中的物品
    private int finalItemIndex = -1; // 最终物品在visualItems列表中的索引
    private static final int ITEM_CELL_WIDTH = 80; // 动画中每个物品单元格的宽度
    private static final int VISUAL_ITEM_COUNT = 100; // 动画视觉效果列表中物品的总数
    private static final int ANIMATION_DURATION_TICKS = 200; // 动画的总持续时间（以游戏刻为单位）

    // 构造函数，接收一个物品栈作为参数
    public CsboxScreen(ItemStack stack) {
        super(Text.literal("cs_screen")); // 调用父类构造函数，设置屏幕标题
        this.client = MinecraftClient.getInstance(); // 获取Minecraft客户端实例

        // 确保客户端和玩家存在
        if (this.client != null && this.client.player != null) {
            this.entity = this.client.player; // 获取当前玩家
            this.world = entity.getWorld(); // 获取当前世界
            this.itemMenu = stack; // 设置当前操作的箱子物品

            // 检查物品是否为ItemCsgoBox的实例
            if (this.itemMenu.getItem() instanceof ItemCsgoBox) {
                // 从物品的NBT中获取物品池和对应的等级
                this.itemGroup = ItemCsgoBox.getItemGroup(this.itemMenu);
                // 处理物品池，生成有序的物品列表和等级列表
                this.itemsList = itemsListProgress(this.itemGroup);
                this.gradeList = gradeListProgress(this.itemGroup);

                // 获取箱子所需的钥匙ID
                String keyId = ItemCsgoBox.getKey(this.itemMenu);
                if (keyId != null && !keyId.isEmpty()) {
                    // 如果需要钥匙，则根据ID查找对应的物品并创建物品栈
                    Registries.ITEM.getOrEmpty(new Identifier(keyId))
                            .ifPresent(item -> this.itemKey = new ItemStack(item));
                }
            } else {
                // 如果不是CSGO箱子物品，则初始化为空
                this.itemGroup = Map.of();
                this.itemsList = List.of();
                this.gradeList = List.of();
            }
        } else {
            // 如果客户端或玩家不存在，则将所有相关字段设为安全默认值
            this.entity = null;
            this.world = null;
            this.itemMenu = ItemStack.EMPTY;
            this.itemGroup = Map.of();
            this.itemsList = List.of();
            this.gradeList = List.of();
        }
    }

    // 初始化GUI时调用，用于添加按钮等控件
    @Override
    protected void init() {
        super.init(); // 调用父类的init方法
        BlurHandler.updateShaderState(true); // 开启背景模糊效果

        // 创建并添加“打开”按钮
        this.openButton = new TexturedButtonWithText(
                this.width * 69 / 100, this.height * 94 / 100, // 按钮位置
                this.width * 4 / 100, this.height * 5 / 100, // 按钮尺寸
                0, 0, 64, // 纹理U,V坐标和悬停时的V偏移
                new Identifier("withdrawals", "textures/screens/atlas/open_box.png"), // 按钮纹理
                82, 128, // 纹理文件总宽度和高度
                button -> startAnimation(), // 按钮点击时执行的动作
                Text.translatable("gui.withdrawals.csgo_box.open_box"), // 按钮显示的文本
                this.textRenderer, // 文本渲染器
                0.8f // 文本缩放比例
        );
        this.addDrawableChild(this.openButton); // 将按钮添加到屏幕的可绘制子元素中

        // 创建并添加“返回”按钮
        this.addDrawableChild(new TexturedButtonWithText(
                this.width * 75 / 100, this.height * 94 / 100, // 按钮位置
                this.width * 4 / 100, this.height * 5 / 100, // 按钮尺寸
                0, 0, 64, // 纹理U,V坐标和悬停时的V偏移
                new Identifier("withdrawals", "textures/screens/atlas/back_box.png"), // 按钮纹理
                82, 128, // 纹理文件总宽度和高度
                button -> closeScreen(), // 按钮点击时执行的动作
                Text.translatable("gui.withdrawals.csgo_box.back_box"), // 按钮显示的文本
                this.textRenderer, // 文本渲染器
                0.8f // 文本缩放比例
        ));
    }

    // 开始开箱动画的方法
    private void startAnimation() {
        if (isAnimating) return; // 如果动画已在进行，则直接返回

        // 检查是否需要钥匙
        String keyId = ItemCsgoBox.getKey(itemMenu);
        boolean needsKey = keyId != null && !keyId.isEmpty();
        // 检查物品是否正确，以及如果需要钥匙，玩家是否拥有钥匙
        if (!(itemMenu.getItem() instanceof ItemCsgoBox && (!needsKey || hasKey(keyId)))) {
            if (this.client != null && this.client.player != null) {
                // 如果条件不满足，向玩家发送提示信息
                this.client.player.sendMessage(Text.literal("§c钥匙不足或物品错误!"), false);
            }
            return; // 中断执行
        }

        // 设置动画状态
        this.isAnimating = true;
        this.animationTicks = 0;
        this.finalItem = null; // 重置最终物品
        this.finalItemIndex = VISUAL_ITEM_COUNT - 15; // 预设最终物品停在倒数第15个位置
        this.scrollOffset = 0; // 重置滚动偏移
        this.openButton.active = false; // 禁用“打开”按钮

        // 准备动画中滚动的物品列表
        this.visualItems.clear();
        if (!this.itemsList.isEmpty()) {
            Random random = Random.create(); // 创建随机数生成器
            // 填充视觉列表，随机从奖池中抽取物品
            for (int i = 0; i < VISUAL_ITEM_COUNT; i++) {
                this.visualItems.add(this.itemsList.get(random.nextInt(this.itemsList.size())).copy());
            }
        } else {
            // 如果奖池为空，用石头作为占位符
            for (int i = 0; i < VISUAL_ITEM_COUNT; i++) {
                this.visualItems.add(new ItemStack(Items.STONE));
            }
        }

        // 计算目标偏移量，使最终物品停在屏幕中央
        float targetOffset = (this.finalItemIndex * ITEM_CELL_WIDTH) - (this.width / 2f) + (ITEM_CELL_WIDTH / 2f);
        // 计算初始滚动速度
        this.scrollSpeed = (targetOffset / (float)ANIMATION_DURATION_TICKS) * 1.6f;

        // 创建并发送网络数据包到服务器，通知服务器开始开箱逻辑
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(needsKey); // 写入是否需要钥匙
        if (needsKey) {
            buf.writeString(keyId); // 如果需要，写入钥匙ID
        }
        ClientPlayNetworking.send(ModPackets.CSGO_PROGRESS_ID, buf); // 发送数据包
    }

    // 由网络数据包回调调用，设置服务器返回的开箱结果
    public void setAnimationResult(ItemStack result) {
        this.finalItem = result; // 设置最终抽中的物品
        // 将视觉列表中的预定位置替换为真实的最终物品
        if (this.visualItems.size() > this.finalItemIndex) {
            this.visualItems.set(this.finalItemIndex, this.finalItem);
        }
    }

    // 检查玩家背包中是否有所需的钥匙
    private boolean hasKey(String keyId) {
        // 遍历玩家主物品栏
        for (ItemStack stack : this.entity.getInventory().main) {
            Identifier stackId = Registries.ITEM.getId(stack.getItem()); // 获取物品ID
            // 比较ID是否与所需的钥匙ID匹配
            if (stackId.toString().equals(keyId)) {
                return true; // 找到钥匙，返回true
            }
        }
        return false; // 遍历完毕未找到，返回false
    }

    // 屏幕渲染方法，每帧调用
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (isAnimating) {
            // 如果正在播放动画，则渲染动画场景
            renderAnimation(context, mouseX, mouseY, delta);
        } else {
            // 否则，渲染默认的静态界面
            renderDefault(context, mouseX, mouseY, delta);
        }
    }

    // 渲染默认界面
    private void renderDefault(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.client != null && this.client.world != null) {
            // 绘制带模糊效果的背景
            context.fillGradient(0, 0, this.width, this.height, BlurHandler.getBackgroundColor(), BlurHandler.getBackgroundColor());
        } else {
            // 如果世界不存在，则渲染普通背景
            this.renderBackground(context);
        }
        // 渲染背景元素
        renderBg(context, delta, mouseX, mouseY);
        // 渲染文本标签
        renderLabels(context, mouseX, mouseY);
        // 调用父类方法渲染子控件（如按钮）
        super.render(context, mouseX, mouseY, delta);
    }

    // 渲染开箱动画
    private void renderAnimation(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context); // 渲染背景

        int screenCenterX = this.width / 2; // 屏幕中心X坐标
        int screenCenterY = this.height / 2; // 屏幕中心Y坐标

        // 在屏幕中心绘制一条金色的指示线
        context.fill(screenCenterX - 1, screenCenterY - 40, screenCenterX + 1, screenCenterY + 40, 0xFFFFD700);

        // 遍历视觉物品列表并渲染
        for (int i = 0; i < this.visualItems.size(); i++) {
            ItemStack stack = this.visualItems.get(i);
            // 计算每个物品的X坐标，根据滚动偏移量变化
            int itemX = (int) (screenCenterX - (ITEM_CELL_WIDTH / 2f) + (i * ITEM_CELL_WIDTH) - this.scrollOffset);
            int itemY = screenCenterY - 16; // 物品的Y坐标

            // 只渲染在屏幕可见范围内的物品
            if (itemX > -ITEM_CELL_WIDTH && itemX < this.width) {
                // 获取物品的等级
                int grade = this.itemGroup.getOrDefault(stack, 1);

                // --- [代码修正] ---
                // 先进行矩阵变换（缩放），再调用渲染方法
                context.getMatrices().push(); // 保存当前的矩阵状态
                // 将坐标原点移动到物品中心，进行缩放，再移回去，以实现中心缩放
                context.getMatrices().translate(itemX + 16, itemY + 16, 0); // 移动到中心点
                context.getMatrices().scale(2.0f, 2.0f, 1.0f); // 放大2倍
                context.getMatrices().translate(-(itemX + 16), -(itemY + 16), 0); // 移回原位

                // 调用原始的、正确的 renderItemFrame 方法来绘制带边框的物品
                IconListTools.renderItemFrame(this.entity, context, stack, itemX, itemY, this.width, this.height, grade);

                context.getMatrices().pop(); // 恢复矩阵状态，防止影响其他渲染
                // --- [修正结束] ---
            }
        }

        // 当动画结束且有最终物品时，显示中奖信息
        if (!isAnimating && this.finalItem != null) {
            Text winText = Text.translatable("gui.withdrawals.csgo_box.win_label").append(this.finalItem.getName());
            int textWidth = this.textRenderer.getWidth(winText);
            context.drawTextWithShadow(this.textRenderer, winText, screenCenterX - textWidth / 2, screenCenterY + 50, 0xFFFFFF);
        }
    }

    // 渲染背景元素
    protected void renderBg(DrawContext context, float partialTicks, int gx, int gy) {
        RenderSystem.enableBlend(); // 开启混合模式
        if (this.client != null) this.client.options.hudHidden = true; // 隐藏HUD

        // 绘制两条装饰性的横线
        context.fill(this.width * 3 / 100, this.height * 53 / 100, this.width * 97 / 100, this.height * 53 / 100 + 1, 0xFFD3D3D3);
        context.fill(this.width * 25 / 100, this.height * 92 / 100, this.width * 75 / 100, this.height * 92 / 100 + 1, 0xFFD3D3D3);

        // 计算并渲染中央可拖动的3D物品展示
        float scale = (width * 26F / 100F) / 16F;
        GuiItemMove.renderItemInInventoryFollowsMouse(context, this.width * 50 / 100, this.height * 32 / 100, this.itemRotX, this.itemRotY, itemMenu, this.entity, scale);

        // 渲染箱子内可能开出的物品列表预览
        int x = 0, y = 0;
        for (int i = 0; i < itemsList.size(); i++) {
            int py = 55, px = i; // 默认在第一行
            if (i > 9) { py = 73; px = i - 10; } // 超过10个则换行
            ItemStack itemStack1 = itemsList.get(i);
            int grade = gradeList.get(i);
            x = px; y = py;
            if (grade == 5) break; // 如果遇到金色品质物品，则停止渲染普通物品
            IconListTools.renderItemFrame(this.entity, context, itemStack1, this.width * 4 / 100 + px * this.width * 9 / 100, this.height * py / 100, this.width, this.height, grade);
        }
        // 如果列表中有金色品质物品，则在末尾渲染一个特殊的金色占位框
        if (!gradeList.isEmpty() && gradeList.get(gradeList.size() - 1) == 5) {
            IconListTools.renderItemFrame(this.entity, context, ItemStack.EMPTY, this.width * 4 / 100 + x * this.width * 9 / 100, this.height * y / 100, this.width, this.height, 5);
        }

        // 如果需要钥匙，则渲染钥匙图标
        if (!itemKey.isEmpty()) {
            IconListTools.renderGuiItem(this.entity, this.world, context, itemKey, this.width * 25F / 100, this.height * 93F / 100, 1);
        }

        RenderSystem.disableBlend(); // 关闭混合模式
    }

    // 渲染所有文本标签
    protected void renderLabels(DrawContext context, int mouseX, int mouseY) {
        Style boldStyle = Style.EMPTY.withBold(true); // 创建一个粗体样式
        renderTitleSection(context, boldStyle); // 渲染标题部分
        renderItemListSection(context, boldStyle); // 渲染物品列表部分
        renderBottomSection(context, boldStyle); // 渲染底部信息部分
    }

    // 渲染标题区域的文本
    private void renderTitleSection(DrawContext context, Style boldStyle) {
        Style colorStyle = Style.EMPTY.withColor(0xFFFFFF00); // 创建一个黄色样式
        Text titleText = Text.translatable("gui.withdrawals.csgo_box.title").fillStyle(boldStyle).fillStyle(colorStyle);; // "开箱" 标题
        renderText(context, titleText, middleOf(titleText.getString(), 1.5f), this.height * 5.9F / 100F, 1.5F); // 居中渲染大号标题

        // 渲染箱子名称
        Text labelText = Text.translatable("gui.withdrawals.csgo_box.label_box"); // "当前开启: "
        Text boxNameText = itemMenu.getName(); // 获取箱子名称
        // 计算总宽度以实现居中
        float labelWidth = this.textRenderer.getWidth(labelText) * 0.7F;
        float boxNameWidth = this.textRenderer.getWidth(boxNameText) * 0.7F;
        float totalWidth = labelWidth + boxNameWidth + 5;
        float startX = (this.width - totalWidth) / 2;

        // 渲染文本
        renderText(context, labelText, startX, this.height * 13F / 100F, 0.7F);
        renderText(context, boxNameText, startX + labelWidth + 5, this.height * 13F / 100F, 0.7F);
    }

    // 渲染物品列表区域的文本
    private void renderItemListSection(DrawContext context, Style boldStyle) {
        // 渲染 "包含以下物品:" 标签
        renderText(context, Text.translatable("gui.withdrawals.csgo_box.label_items").fillStyle(boldStyle), this.width * 3F / 100F, this.height * 49F / 100F, 0.8F);

        int lastRenderedPx = 0;
        int lastRenderedPy = 0;
        // 遍历物品列表，渲染它们的名称
        for (int i = 0; i < itemsList.size(); i++) {
            int py = 67, px = i; // 默认在第一行
            if (i > 9) { py = 85; px = i - 10; } // 换行
            int grade = gradeList.get(i);
            if (grade > 4) { // 如果是金色品质
                lastRenderedPx = px;
                lastRenderedPy = py;
                break; // 停止渲染，因为金色物品有特殊标签
            }
            // 在对应物品图标下方渲染名称
            renderText(context, itemsList.get(i).getName(), this.width * 4F / 100 + px * this.width * 9F / 100, this.height * py / 100F, 0.6F);
            lastRenderedPx = px;
            lastRenderedPy = py;
        }

        // 如果有金色物品，渲染 "极其罕见的特殊物品" 标签
        if (!gradeList.isEmpty() && gradeList.get(gradeList.size() - 1) == 5) {
            renderText(context, Text.translatable("gui.withdrawals.csgo_box.label_gold"), this.width * 4F / 100 + lastRenderedPx * this.width * 9F / 100, this.height * lastRenderedPy / 100F, 0.6F);
        }
    }

    // 渲染底部区域的文本（钥匙信息）
    private void renderBottomSection(DrawContext context, Style boldStyle) {
        float iconX = this.width * 25F / 100F; // 钥匙图标X坐标
        float iconY = this.height * 93F / 100F; // 钥匙图标Y坐标
        float textX = iconX + 20; // 文本X坐标
        float textY = iconY + 5; // 文本Y坐标

        // 如果需要钥匙
        if (!itemKey.isEmpty()) {
            if (boxKeyCount > 0) {
                // 如果玩家有钥匙，显示拥有数量
                renderText(context, Text.literal(" × " + boxKeyCount), textX-5, textY, 0.8F);
            } else {
                // 如果没有，显示提示信息
                String tip = "需要使用 1个 " + itemKey.getName().getString() + " 打开 ";
                renderText(context, Text.literal(tip), textX, textY, 0.8F);
            }
        }
    }


    // 每游戏刻调用一次的更新方法
    @Override
    public void tick() {
        super.tick(); // 调用父类tick
        // 安全检查，如果客户端或玩家不存在，则关闭屏幕
        if (this.client == null || this.client.player == null) {
            closeScreen();
            return;
        }
        // 确保玩家存活
        if (this.client.player.isAlive() && !this.client.player.isRemoved()) {
            if (isAnimating) {
                // 如果在播放动画，则更新动画逻辑
                animationTick();
            } else {
                // 否则，更新常规GUI逻辑
                containerTick();
            }
        } else {
            // 如果玩家死亡或被移除，关闭屏幕
            closeScreen();
        }
    }

    // 更新动画的逻辑
    private void animationTick() {
        animationTicks++; // 动画计时器增加

        // 每隔4刻播放一次点击音效，模拟滚动声，且速度较快时才播放
        if (animationTicks % 4 == 0 && scrollSpeed > 1.0f) {
            this.client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.4f, 1.5f);
        }

        // 如果已经收到了服务器的结果
        if (finalItem != null) {
            // 计算到目标位置的距离
            float targetOffset = (this.finalItemIndex * ITEM_CELL_WIDTH) - (this.width / 2f) + (ITEM_CELL_WIDTH / 2f);
            float distance = targetOffset - this.scrollOffset;
            // 根据距离动态调整速度，实现减速效果
            this.scrollSpeed = Math.max(0.2f, distance * 0.02f);

            // 如果非常接近目标位置，则直接定位并结束动画
            if (Math.abs(distance) < 0.5f) {
                this.scrollOffset = targetOffset; // 精准定位
                this.isAnimating = false; // 结束动画状态
                this.openButton.active = true; // 重新激活“打开”按钮
                this.client.player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, 0.8f, 1.0f); // 播放中奖音效
            }
        }

        // 更新滚动偏移量
        this.scrollOffset += this.scrollSpeed;
    }

    // 常规GUI的tick逻辑
    public void containerTick() {
        gameTick++; // 游戏刻计时器增加
        // 每20刻（1秒）更新一次玩家背包中的钥匙数量
        if (gameTick % 20 == 1) {
            this.boxKeyCount = getKeyCount();
        }
        if (gameTick > 100000) gameTick = 0; // 防止计时器溢出
    }

    // 获取玩家背包中指定钥匙的数量
    private int getKeyCount() {
        String keyId = ItemCsgoBox.getKey(itemMenu); // 获取所需的钥匙ID
        if (keyId == null || keyId.isEmpty()) return 0; // 如果不需要钥匙，返回0
        int count = 0;
        // 遍历玩家主物品栏
        for (ItemStack stack : entity.getInventory().main) {
            Identifier stackId = Registries.ITEM.getId(stack.getItem());
            // 如果物品ID匹配
            if (stackId.toString().equals(keyId)) {
                count += stack.getCount(); // 累加数量
            }
        }
        return count; // 返回总数
    }

    // 处理鼠标拖拽事件
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isAnimating) return false; // 动画期间不允许拖拽
        // 检查鼠标是否在中央物品展示的有效区域内
        boolean isInRange = (mouseX >= this.width * 37F / 100 && mouseX <= this.width * 37F / 100 + 200) && (mouseY >= this.height * 12F / 100 && mouseY <= this.height * 12F / 100 + 176);
        if (button == 0 && isInRange) { // 如果是左键拖拽且在区域内
            // 根据鼠标位移更新物品的旋转角度
            this.itemRotX = GuiItemMove.renderRotAngleX(deltaX, this.itemRotX);
            this.itemRotY = GuiItemMove.renderRotAngleY(deltaY, this.itemRotY);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    // 处理键盘按键事件
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isAnimating) return false; // 动画期间不响应键盘
        if (super.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (keyCode == 256) { // 256是ESC键
            closeScreen(); // 关闭屏幕
            return true;
        }
        return false;
    }

    // 关闭屏幕的逻辑
    private void closeScreen() {
        if (isAnimating) return; // 动画期间不允许关闭
        if (this.client != null && this.client.player != null) {
            this.client.player.closeHandledScreen(); // 通知玩家关闭当前容器屏幕
        }
        this.close(); // 调用自身的关闭方法
    }

    // 屏幕关闭时调用
    @Override
    public void close() {
        BlurHandler.updateShaderState(false); // 关闭背景模糊效果
        if (this.client != null) {
            this.client.options.hudHidden = false; // 恢复显示HUD
        }
        super.close(); // 调用父类关闭方法
    }

    // 返回游戏是否应该在GUI打开时暂停
    @Override
    public boolean shouldPause() {
        return false; // 不暂停游戏
    }

    // 工具方法：计算文本居中渲染的起始X坐标
    private float middleOf(String text, float scale) {
        return (this.width - this.textRenderer.getWidth(text) * scale) * 0.5F;
    }

    // 工具方法：以指定缩放比例在指定位置渲染文本
    private void renderText(DrawContext context, Text text, float px, float py, float scale) {
        context.getMatrices().push(); // 保存矩阵状态
        context.getMatrices().translate(px, py, 0); // 移动到目标位置
        context.getMatrices().scale(scale, scale, 1.0F); // 进行缩放
        context.drawText(this.textRenderer, text, 0, 0, 0xFFD3D3D3, false); // 绘制文本
        context.getMatrices().pop(); // 恢复矩阵状态
    }

    // 静态工具方法：将物品Map按等级排序后转换为List<ItemStack>
    public static List<ItemStack> itemsListProgress(Map<ItemStack, Integer> itemList) {
        List<ItemStack> itemStacks = new ArrayList<>();
        // 遍历等级1到5
        for (int i = 1; i < 6; i++) {
            // 遍历输入的Map
            for (Map.Entry<ItemStack, Integer> entry : itemList.entrySet()) {
                // 如果物品等级与当前遍历的等级匹配，则添加到列表中
                if (entry.getValue() == i) {
                    itemStacks.add(entry.getKey());
                }
            }
        }
        return itemStacks; // 返回排序后的列表
    }

    // 静态工具方法：将物品Map按等级排序后转换为List<Integer>（等级列表）
    public static List<Integer> gradeListProgress(Map<ItemStack, Integer> itemList) {
        List<Integer> grades = new ArrayList<>();
        // 逻辑与上面方法相同，但添加的是等级而不是物品
        for (int i = 1; i < 6; i++) {
            for (Map.Entry<ItemStack, Integer> entry : itemList.entrySet()) {
                if (entry.getValue() == i) {
                    grades.add(i);
                }
            }
        }
        return grades; // 返回排序后的等级列表
    }
}