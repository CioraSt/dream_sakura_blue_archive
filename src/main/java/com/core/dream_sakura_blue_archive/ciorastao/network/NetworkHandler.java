package com.core.dream_sakura_blue_archive.ciorastao.network;

import com.core.dream_sakura_blue_archive.ciorastao.dream_sakura_blue_archive;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class NetworkHandler {
    private static final String VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath(dream_sakura_blue_archive.MODID, "main"))
            .networkProtocolVersion(() -> VERSION).clientAcceptedVersions(VERSION::equals)
            .serverAcceptedVersions(VERSION::equals).simpleChannel();
    private static int id;

    public static void register() {
        CHANNEL.messageBuilder(C2SGachaDrawPacket.class, id++).encoder(C2SGachaDrawPacket::encode)
                .decoder(C2SGachaDrawPacket::decode).consumerMainThread(C2SGachaDrawPacket::handle).add();
        CHANNEL.messageBuilder(S2CGachaResultPacket.class, id++).encoder(S2CGachaResultPacket::encode)
                .decoder(S2CGachaResultPacket::decode).consumerMainThread(S2CGachaResultPacket::handle).add();
        CHANNEL.messageBuilder(C2SHaloSkillPacket.class, id++).encoder(C2SHaloSkillPacket::encode)
                .decoder(C2SHaloSkillPacket::decode).consumerMainThread(C2SHaloSkillPacket::handle).add();
        CHANNEL.messageBuilder(S2CHaloSkillVisualPacket.class, id++).encoder(S2CHaloSkillVisualPacket::encode)
                .decoder(S2CHaloSkillVisualPacket::decode).consumerMainThread(S2CHaloSkillVisualPacket::handle).add();
    }

    public static void sendToServer(Object packet) { CHANNEL.sendToServer(packet); }
    public static void sendToPlayer(Object packet, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
    public static void sendTrackingAndSelf(Object packet, net.minecraft.world.entity.Entity entity) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), packet);
    }

    private NetworkHandler() {
    }
}
