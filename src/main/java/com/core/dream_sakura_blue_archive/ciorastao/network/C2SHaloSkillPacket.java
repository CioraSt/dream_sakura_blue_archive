package com.core.dream_sakura_blue_archive.ciorastao.network;

import com.core.dream_sakura_blue_archive.ciorastao.halo.HaloSkills;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** J 键只上传一次意图；实际外观、冷却和结算均由服务器决定。 */
public record C2SHaloSkillPacket() {
    public static void encode(C2SHaloSkillPacket packet, FriendlyByteBuf buffer) {}
    public static C2SHaloSkillPacket decode(FriendlyByteBuf buffer) { return new C2SHaloSkillPacket(); }
    public static void handle(C2SHaloSkillPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer player = context.getSender();
        if (player != null) context.enqueueWork(() -> HaloSkills.activate(player));
        context.setPacketHandled(true);
    }
}
