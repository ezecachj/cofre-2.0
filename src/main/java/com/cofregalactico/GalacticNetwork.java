package com.cofregalactico;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public final class GalacticNetwork {
    private static final String VERSION="1";
    public static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(new ResourceLocation(CofreGalactico.MOD_ID,"main"),()->VERSION,VERSION::equals,VERSION::equals);
    public record Action(int type,int index,boolean shift,String query) {
        static void encode(Action a,FriendlyByteBuf b){b.writeVarInt(a.type);b.writeVarInt(a.index);b.writeBoolean(a.shift);b.writeUtf(a.query,60);}
        static Action decode(FriendlyByteBuf b){return new Action(b.readVarInt(),b.readVarInt(),b.readBoolean(),b.readUtf(60));}
        static void handle(Action a,Supplier<NetworkEvent.Context> supplier){
            var ctx=supplier.get();ctx.enqueueWork(()->{
                var sender=ctx.getSender();if(sender==null||!(sender.containerMenu instanceof GalacticMenu menu)||!menu.stillValid(sender))return;
                switch(a.type){case 0 -> menu.depositAll();case 1 -> menu.changeView(a.query,a.index);case 2 -> menu.withdraw(a.index,a.shift);case 3 -> menu.craftStored();}
            });ctx.setPacketHandled(true);
        }
    }
    public static void init(){CHANNEL.registerMessage(0,Action.class,Action::encode,Action::decode,Action::handle);}
    private GalacticNetwork(){}
}
