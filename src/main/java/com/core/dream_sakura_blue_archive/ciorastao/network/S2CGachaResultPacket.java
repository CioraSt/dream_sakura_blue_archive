package com.core.dream_sakura_blue_archive.ciorastao.network;

import com.core.dream_sakura_blue_archive.ciorastao.client.screen.GachaClientScreens;
import com.core.dream_sakura_blue_archive.ciorastao.gacha.Gacha;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record S2CGachaResultPacket(List<Gacha.Reward> rewards) {
    public static void encode(S2CGachaResultPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.rewards.size());
        for (Gacha.Reward reward : packet.rewards) { buffer.writeEnum(reward.rarity()); buffer.writeItem(reward.stack()); }
    }
    public static S2CGachaResultPacket decode(FriendlyByteBuf buffer) {
        List<Gacha.Reward> rewards = new ArrayList<>();
        for (int i = buffer.readVarInt(); i > 0; i--) rewards.add(new Gacha.Reward(buffer.readEnum(Gacha.Rarity.class), buffer.readItem()));
        return new S2CGachaResultPacket(rewards);
    }
    public static void handle(S2CGachaResultPacket packet, Supplier<NetworkEvent.Context> supplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> GachaClientScreens.openSignature(packet.rewards));
        supplier.get().setPacketHandled(true);
    }
}
