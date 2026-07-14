package com.core.dream_sakura_blue_archive.ciorastao.network;

import com.core.dream_sakura_blue_archive.ciorastao.menu.AronaGachaMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record C2SGachaDrawPacket(int count) {
    public static void encode(C2SGachaDrawPacket packet, FriendlyByteBuf buffer) { buffer.writeByte(packet.count); }
    public static C2SGachaDrawPacket decode(FriendlyByteBuf buffer) { return new C2SGachaDrawPacket(buffer.readByte()); }
    public static void handle(C2SGachaDrawPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer player = context.getSender();
        if (player != null && player.containerMenu instanceof AronaGachaMenu menu) menu.draw(player, packet.count);
        context.setPacketHandled(true);
    }
}
