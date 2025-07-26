package com.XHxinhe.withdrawals.datagen;

import com.XHxinhe.withdrawals.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

public class ModDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        pack.addProvider((output, registriesFuture) -> new FabricRecipeProvider(output) {
            @Override
            public void generate(Consumer<RecipeJsonProvider> exporter) {
                // 普通钥匙配方 (CSGO_KEY0)
                ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.ITEM_CSGO_KEY0)
                        .pattern("IGI")
                        .pattern("GDG")
                        .pattern("IGI")
                        .input('I', Items.IRON_INGOT)
                        .input('G', Items.GOLD_INGOT)
                        .input('D', Items.DIAMOND)
                        .criterion(FabricRecipeProvider.hasItem(Items.DIAMOND),
                                FabricRecipeProvider.conditionsFromItem(Items.DIAMOND))
                        .offerTo(exporter, new Identifier("withdrawals", "csgo_key0"));

                // 进阶钥匙配方 (CSGO_KEY1)
                ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.ITEM_CSGO_KEY1)
                        .pattern("IGI")
                        .pattern("GEG")
                        .pattern("IGI")
                        .input('I', Items.IRON_INGOT)
                        .input('G', Items.GOLD_INGOT)
                        .input('E', Items.EMERALD)
                        .criterion(FabricRecipeProvider.hasItem(Items.EMERALD),
                                FabricRecipeProvider.conditionsFromItem(Items.EMERALD))
                        .offerTo(exporter, new Identifier("withdrawals", "csgo_key1"));

                // 高级钥匙配方 (CSGO_KEY2)
                ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.ITEM_CSGO_KEY2)
                        .pattern("NGN")
                        .pattern("GDG")
                        .pattern("NGN")
                        .input('N', Items.NETHERITE_INGOT)
                        .input('G', Items.GOLD_BLOCK)
                        .input('D', Items.DIAMOND_BLOCK)
                        .criterion(FabricRecipeProvider.hasItem(Items.NETHERITE_INGOT),
                                FabricRecipeProvider.conditionsFromItem(Items.NETHERITE_INGOT))
                        .offerTo(exporter, new Identifier("withdrawals", "csgo_key2"));

                // 稀有钥匙配方 (CSGO_KEY3)
                ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.ITEM_CSGO_KEY3)
                        .pattern("NEN")
                        .pattern("EDE")
                        .pattern("NEN")
                        .input('N', Items.NETHERITE_BLOCK)
                        .input('E', Items.EMERALD_BLOCK)
                        .input('D', Items.DIAMOND_BLOCK)
                        .criterion(FabricRecipeProvider.hasItem(Items.NETHERITE_BLOCK),
                                FabricRecipeProvider.conditionsFromItem(Items.NETHERITE_BLOCK))
                        .offerTo(exporter, new Identifier("withdrawals", "csgo_key3"));

                // 开箱工具配方 (CSGO_BOX_CRAFT)
                ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.ITEM_CSGO_CRAFT)
                        .pattern(" G ")
                        .pattern("GDG")
                        .pattern(" G ")
                        .input('G', Items.GOLD_INGOT)
                        .input('D', Items.DIAMOND)
                        .criterion(FabricRecipeProvider.hasItem(Items.DIAMOND),
                                FabricRecipeProvider.conditionsFromItem(Items.DIAMOND))
                        .offerTo(exporter, new Identifier("withdrawals", "csgo_box_craft"));
            }
        });
    }
}