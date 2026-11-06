package com.cachjsstorage;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(CofreGalactico.MOD_ID)
public class CofreGalactico {
    public static final String MOD_ID="cachjsstorage";
    public static final DeferredRegister<Block> BLOCKS=DeferredRegister.create(ForgeRegistries.BLOCKS,MOD_ID);
    public static final DeferredRegister<Item> ITEMS=DeferredRegister.create(ForgeRegistries.ITEMS,MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES=DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES,MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(ForgeRegistries.MENU_TYPES,MOD_ID);
    public static final RegistryObject<Block> CHEST=BLOCKS.register("cachjs_chest",()->new GalacticChestBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(2.5f)));
    public static final RegistryObject<Item> CHEST_ITEM=ITEMS.register("cachjs_chest",()->new BlockItem(CHEST.get(),new Item.Properties()));
    public static final RegistryObject<BlockEntityType<GalacticChestBlockEntity>> CHEST_ENTITY=BLOCK_ENTITIES.register("cachjs_chest",()->BlockEntityType.Builder.of(GalacticChestBlockEntity::new,CHEST.get()).build(null));
    public static final RegistryObject<MenuType<GalacticMenu>> MENU=MENUS.register("galactic_menu",()->IForgeMenuType.create((id,inv,data)->new GalacticMenu(id,inv,data)));
    public CofreGalactico(){IEventBus bus=FMLJavaModLoadingContext.get().getModEventBus();BLOCKS.register(bus);ITEMS.register(bus);BLOCK_ENTITIES.register(bus);MENUS.register(bus);GalacticNetwork.init();}
}
