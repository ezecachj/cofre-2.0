package com.cofregalactico;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.inventory.ContainerLevelAccess;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class GalacticMenu extends AbstractContainerMenu {
    public static final int STORAGE_SLOTS=45;
    private final GalacticChestBlockEntity chest;
    private final Container view=new SimpleContainer(STORAGE_SLOTS);
    private final CraftingContainer craft=new CraftingContainer(this,3,3);
    private final ResultContainer result=new ResultContainer();
    private final Player player;
    private String search="";
    private int page=0;
    private final List<ItemStack> shown=new ArrayList<>();
    public GalacticMenu(int id,Inventory inventory,net.minecraft.network.FriendlyByteBuf data){this(id,inventory,inventory.player.level().getBlockEntity(data.readBlockPos()) instanceof GalacticChestBlockEntity c ? c : null);}
    public GalacticMenu(int id,Inventory inventory,GalacticChestBlockEntity chest){
        super(CofreGalactico.MENU.get(),id);this.player=inventory.player;this.chest=chest;
        for(int i=0;i<STORAGE_SLOTS;i++){final int idx=i;addSlot(new Slot(view,i,8+(i%9)*18,20+(i/9)*18){
            @Override public boolean mayPlace(ItemStack stack){return false;}
            @Override public boolean mayPickup(Player player){return false;}
        });}
        for(int i=0;i<9;i++)addSlot(new Slot(craft,i,188+(i%3)*18,20+(i/3)*18));
        addSlot(new ResultSlot(inventory.player,craft,result,0,253,38));
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inventory,col+row*9+9,8+col*18,140+row*18));
        for(int col=0;col<9;col++)addSlot(new Slot(inventory,col,8+col*18,198));
        refresh();
    }
    public GalacticChestBlockEntity chest(){return chest;}
    public int page(){return page;}
    public void changeView(String text,int requestedPage){
        search=text==null?"":text.toLowerCase(Locale.ROOT).substring(0,Math.min(60,text.length()));page=Math.max(0,requestedPage);refresh();
    }
    private void refresh(){
        shown.clear();
        if(chest!=null){
            for(var e:chest.entries())if(e.sample().getHoverName().getString().toLowerCase(Locale.ROOT).contains(search))shown.add(e.sample().copy());
            shown.sort(Comparator.comparing(s->s.getHoverName().getString().toLowerCase(Locale.ROOT)));
        }
        int maxPage=Math.max(0,(shown.size()-1)/STORAGE_SLOTS);page=Math.min(page,maxPage);
        for(int i=0;i<STORAGE_SLOTS;i++){
            int idx=page*STORAGE_SLOTS+i;
            ItemStack stack=idx<shown.size()?shown.get(idx).copy():ItemStack.EMPTY;
            if(!stack.isEmpty()&&chest!=null){for(var e:chest.entries())if(ItemStack.isSameItemSameTags(stack,e.sample())){stack.setCount((int)Math.min(stack.getMaxStackSize(),e.count()));break;}}
            view.setItem(i,stack);
        }
        broadcastChanges();
    }
    public void withdraw(int slot,boolean fullStack){
        if(chest==null||slot<0||slot>=STORAGE_SLOTS)return;
        int idx=page*STORAGE_SLOTS+slot;if(idx>=shown.size())return;
        ItemStack sample=shown.get(idx);
        ItemStack out=chest.extract(sample,fullStack?sample.getMaxStackSize():1);
        if(!out.isEmpty()&&!player.getInventory().add(out)){
            if(!out.isEmpty())chest.insert(out,out.getCount());
        }
        refresh();
    }
    /** Craft using stored ingredients; 3x3 grid serves as a recipe template. */
    public void craftStored(){
        if(chest==null || !(player instanceof ServerPlayer sp))return;
        var recipe=sp.serverLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,craft,sp.serverLevel());
        if(recipe.isEmpty())return;
        ItemStack output=recipe.get().assemble(craft,sp.serverLevel().registryAccess());
        if(output.isEmpty())return;
        // Preflight all quantities, including duplicate ingredients in the pattern.
        for(int i=0;i<9;i++){
            ItemStack ingredient=craft.getItem(i);if(ingredient.isEmpty())continue;
            int needed=0;for(int j=0;j<9;j++)if(ItemStack.isSameItemSameTags(ingredient,craft.getItem(j)))needed++;
            long available=0;for(var e:chest.entries())if(ItemStack.isSameItemSameTags(ingredient,e.sample()))available=e.count();
            if(available<needed)return;
        }
        // Insert result before consuming materials; avoid item loss if inventory is full.
        ItemStack toGive=output.copy();
        if(!player.getInventory().add(toGive)){
            if(!toGive.isEmpty())player.drop(toGive,false);
        }
        var remainder=recipe.get().getRemainingItems(craft);
        for(int i=0;i<9;i++){
            ItemStack ingredient=craft.getItem(i);if(ingredient.isEmpty())continue;
            chest.extract(ingredient,1);
            ItemStack leftover=remainder.get(i);
            if(!leftover.isEmpty()){
                ItemStack copy=leftover.copy();
                if(!player.getInventory().add(copy) && !copy.isEmpty())player.drop(copy,false);
            }
        }
        refresh();
    }
    public void depositAll(){
        if(chest==null)return;
        for(int i=0;i<player.getInventory().items.size();i++){
            ItemStack stack=player.getInventory().items.get(i);
            if(stack.isEmpty())continue;
            long moved=chest.insert(stack,stack.getCount());if(moved>0)stack.shrink((int)moved);
        }
        player.getInventory().setChanged();refresh();
    }
    @Override public void clicked(int slotId,int button,ClickType type,Player player){
        if(slotId>=0&&slotId<STORAGE_SLOTS){if(!player.level().isClientSide)withdraw(slotId,button==1||type==ClickType.QUICK_MOVE);return;}
        super.clicked(slotId,button,type,player);
    }
    @Override public void slotsChanged(Container container){
        if(container==craft && player instanceof ServerPlayer sp){
            var recipe=sp.serverLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,craft,sp.serverLevel());
            result.setItem(0,recipe.map(r->r.assemble(craft,sp.serverLevel().registryAccess())).orElse(ItemStack.EMPTY));
            broadcastChanges();
        }
        super.slotsChanged(container);
    }
    @Override public ItemStack quickMoveStack(Player player,int index){
        ItemStack original=ItemStack.EMPTY;Slot slot=slots.get(index);if(!slot.hasItem())return original;
        ItemStack stack=slot.getItem();original=stack.copy();
        if(index<STORAGE_SLOTS){if(!player.level().isClientSide)withdraw(index,true);return ItemStack.EMPTY;}
        if(index==54){if(!moveItemStackTo(stack,55,91,true))return ItemStack.EMPTY;slot.onQuickCraft(stack,original);}
        else if(index>=55){
            if(chest!=null){long moved=chest.insert(stack,stack.getCount());if(moved>0)stack.shrink((int)moved);refresh();}
        } else if(!moveItemStackTo(stack,55,91,false))return ItemStack.EMPTY;
        if(stack.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();
        if(stack.getCount()==original.getCount())return ItemStack.EMPTY;
        slot.onTake(player,stack);return original;
    }
    @Override public boolean stillValid(Player player){return chest==null||chest.usableBy(player);}
    @Override public void removed(Player player){
        super.removed(player);
        if(!player.level().isClientSide){for(int i=0;i<craft.getContainerSize();i++){ItemStack s=craft.removeItemNoUpdate(i);if(!s.isEmpty()&&!player.getInventory().add(s))player.drop(s,false);}}
    }
}
