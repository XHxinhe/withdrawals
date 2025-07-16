package com.XHxinhe.withdrawals.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.item.ItemStack;

// 接口定义了组件的功能
public interface CsboxComponent extends Component {

    long getPlayerSeed();
    void setPlayerSeed(long seed);

    int getMode();
    void setMode(int mode);

    ItemStack getItem();
    void setItem(ItemStack item);

    int getGrade();
    void setGrade(int grade);
}