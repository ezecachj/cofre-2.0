package com.cofregalactico;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.util.ArrayList;
import java.util.List;

/** Server-authoritative storage; preserves NBT variants and long quantities. */
public class GalacticChestBlockEntity extends BlockEntity implements MenuProvider {
    public static final long MAX_PER_TYPE = 2_000_000_000L;
    private final List<Entry> entries = new ArrayList<>();
    public record Entry(ItemStack sample, long count) {}
    public GalacticChestBlockEntity(BlockPos pos, BlockState state) { super(CofreGalactico.CHEST_ENTITY.get(), pos, state); }
    public List<Entry> entries() { return entries; }
    public long insert(ItemStack stack, long requested) {
        if (stack.isEmpty() || requested <= 0) return 0;
        for (int i=0; i<entries.size(); i++) {
            Entry e=entries.get(i);
            if (ItemStack.isSameItemSameTags(e.sample(), stack)) {
                long added=Math.min(requested, MAX_PER_TYPE-e.count());
                if (added>0) { entries.set(i,new Entry(e.sample(),e.count()+added)); setChanged(); }
                return added;
            }
        }
        if (entries.size()>=4096) return 0;
        long added=Math.min(requested,MAX_PER_TYPE);
        ItemStack sample=stack.copy(); sample.setCount(1);
        entries.add(new Entry(sample,added)); setChanged(); return added;
    }
    public ItemStack extract(ItemStack sample, int requested) {
        if (requested<=0) return ItemStack.EMPTY;
        for (int i=0;i<entries.size();i++) {
            Entry e=entries.get(i);
            if (ItemStack.isSameItemSameTags(e.sample(),sample)) {
                int amount=(int)Math.min(Math.min(e.count(),requested),sample.getMaxStackSize());
                if(amount<=0)return ItemStack.EMPTY;
                ItemStack out=sample.copy(); out.setCount(amount);
                if(e.count()==amount) entries.remove(i); else entries.set(i,new Entry(e.sample(),e.count()-amount));
                setChanged(); return out;
            }
        }
        return ItemStack.EMPTY;
    }
    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag list=new ListTag();
        for(Entry e:entries){CompoundTag row=new CompoundTag();row.put("Item",e.sample().save(new CompoundTag()));row.putLong("Amount",e.count());list.add(row);}
        tag.put("StoredItems",list);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag); entries.clear();
        ListTag list=tag.getList("StoredItems",Tag.TAG_COMPOUND);
        for(int i=0;i<list.size();i++){
            CompoundTag row=list.getCompound(i);ItemStack stack=ItemStack.of(row.getCompound("Item"));
            long amount=row.getLong("Amount");if(!stack.isEmpty()&&amount>0){stack.setCount(1);entries.add(new Entry(stack,Math.min(amount,MAX_PER_TYPE)));}
        }
        // Migration from the original 54-slot chest, when upgrading existing worlds.
        if(entries.isEmpty() && tag.contains("Items",Tag.TAG_LIST)) {
            for(Tag t:tag.getList("Items",Tag.TAG_COMPOUND)){
                ItemStack s=ItemStack.of((CompoundTag)t);if(!s.isEmpty())insert(s,s.getCount());
            }
        }
    }
    @Override public Component getDisplayName(){return Component.translatable("container.cofregalactico.galactic_chest");}
    @Override public AbstractContainerMenu createMenu(int id,Inventory inv,Player player){return new GalacticMenu(id,inv,this);}
    public boolean usableBy(Player player){return !isRemoved() && player.level().getBlockEntity(worldPosition)==this && player.distanceToSqr(worldPosition.getX()+0.5,worldPosition.getY()+0.5,worldPosition.getZ()+0.5)<=64;}
}
