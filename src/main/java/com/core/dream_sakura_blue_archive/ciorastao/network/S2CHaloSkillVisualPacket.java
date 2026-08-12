package com.core.dream_sakura_blue_archive.ciorastao.network;

import com.core.dream_sakura_blue_archive.ciorastao.client.HaloSkillVisuals;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record S2CHaloSkillVisualPacket(int entityId, int visual, int durationTicks) {
    public static final int ALICE_SWORD_CHARGE = 1;

    public static void encode(S2CHaloSkillVisualPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.entityId);
        buffer.writeVarInt(packet.visual);
        buffer.writeVarInt(packet.durationTicks);
    }
    public static S2CHaloSkillVisualPacket decode(FriendlyByteBuf buffer) {
        return new S2CHaloSkillVisualPacket(buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt());
    }
    public static void handle(S2CHaloSkillVisualPacket packet, Supplier<NetworkEvent.Context> supplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> HaloSkillVisuals.start(
                packet.entityId, packet.visual, packet.durationTicks));
        supplier.get().setPacketHandled(true);
    }
}
