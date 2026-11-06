package com.cofregalactico;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.PacketDistributor;

public class GalacticScreen extends AbstractContainerScreen<GalacticMenu> {
    private EditBox search;
    private int page=0;
    public GalacticScreen(GalacticMenu menu,Inventory inv,Component title){super(menu,inv,title);imageWidth=286;imageHeight=222;inventoryLabelY=128;titleLabelX=8;titleLabelY=7;}
    @Override protected void init(){
        super.init();
        search=new EditBox(font,leftPos+174,topPos+94,104,16,Component.literal("Buscar"));search.setMaxLength(60);search.setHint(Component.literal("Buscar objetos..."));
        search.setResponder(s->{page=0;sendView();});addRenderableWidget(search);
        addRenderableWidget(Button.builder(Component.literal("Fabricar del cofre"),b->GalacticNetwork.CHANNEL.sendToServer(new GalacticNetwork.Action(3,0,false,""))).bounds(leftPos+174,topPos+114,104,18).build());
        addRenderableWidget(Button.builder(Component.literal("Depositar todo"),b->GalacticNetwork.CHANNEL.sendToServer(new GalacticNetwork.Action(0,0,false,""))).bounds(leftPos+174,topPos+135,104,18).build());
        addRenderableWidget(Button.builder(Component.literal("<"),b->{page=Math.max(0,page-1);sendView();}).bounds(leftPos+174,topPos+75,25,16).build());
        addRenderableWidget(Button.builder(Component.literal(">"),b->{page++;sendView();}).bounds(leftPos+202,topPos+75,25,16).build());
    }
    private void sendView(){GalacticNetwork.CHANNEL.sendToServer(new GalacticNetwork.Action(1,page,false,search.getValue()));}
    @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){
        g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xff171326);
        g.fill(leftPos+4,topPos+16,leftPos+170,topPos+114,0xff29223f);
        g.fill(leftPos+173,topPos+16,leftPos+282,topPos+72,0xff29223f);
        g.fill(leftPos+4,topPos+135,leftPos+170,topPos+219,0xff29223f);
        for(int i=0;i<menu.slots.size();i++){
            var s=menu.slots.get(i);int x=leftPos+s.x-1,y=topPos+s.y-1;
            g.fill(x,y,x+18,y+18,0xff55496c);g.fill(x+1,y+1,x+17,y+17,0xff272137);
        }
    }
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        renderBackground(g);super.render(g,mx,my,partial);
        g.drawString(font,"Almacenamiento",leftPos+8,topPos+7,0xffd7baff,false);
        g.drawString(font,"Crafteo 3x3",leftPos+184,topPos+7,0xffd7baff,false);
        g.drawString(font,"Pagina "+(page+1),leftPos+231,topPos+79,0xffeeeeff,false);
        renderTooltip(g,mx,my);
    }
}
