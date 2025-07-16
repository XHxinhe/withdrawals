package com.XHxinhe.withdrawals.component;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class PlayerCsboxComponent implements CsboxComponent, AutoSyncedComponent {

    private final PlayerEntity player;
    private long playerSeed = 0L;
    private int mode = 0;
    private ItemStack item = ItemStack.EMPTY;
    private int grade = 1;

    public PlayerCsboxComponent(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public long getPlayerSeed() {
        return this.playerSeed;
    }

    @Override
    public void setPlayerSeed(long seed) {
        this.playerSeed = seed;
    }

    @Override
    public int getMode() {
        return this.mode;
    }

    @Override
    public void setMode(int mode) {
        if (mode > -2 && mode < 2) {
            this.mode = mode;
        }
    }

    @Override
    public ItemStack getItem() {
        return this.item;
    }

    @Override
    public void setItem(ItemStack item) {
        this.item = item;
    }

    @Override
    public int getGrade() {
        return this.grade;
    }

    @Override
    public void setGrade(int grade) {
        if (grade > 0 && grade < 6) {
            this.grade = grade;
        }
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.playerSeed = tag.getLong("cs_seed");
        this.mode = tag.getInt("cs_mode");
        this.grade = tag.getInt("cs_grade");
        if (tag.contains("cs_item")) {
            this.item = ItemStack.fromNbt(tag.getCompound("cs_item"));
        } else {
            this.item = ItemStack.EMPTY;
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putLong("cs_seed", this.playerSeed);
        tag.putInt("cs_mode", this.mode);
        tag.putInt("cs_grade", this.grade);
        if (!this.item.isEmpty()) {
            tag.put("cs_item", this.item.writeNbt(new NbtCompound()));
        }
    }
}