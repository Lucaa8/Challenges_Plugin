package ch.luca008.Utils;

import ch.luca008.Challenges;
import ch.luca008.SpigotApi.SpigotApi;
import io.netty.channel.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PacketsListener implements Listener {

    private List<UUID> registered = new ArrayList<>();

    public void add(Player p){
        if(Perms.Permission.CADMIN_EDITOR_PROMPTABLE.hasPermission(p)&&!registered.contains(p.getUniqueId())){
            registerPlayer(p);
            registered.add(p.getUniqueId());
        }
    }

    public void rem(@Nullable Player p){
        if(p!=null&&registered.contains(p.getUniqueId())){
            unregisterPlayer(p);
            registered.remove(p.getUniqueId());
        }
    }

    @EventHandler
    public void quit(PlayerQuitEvent e) {
        rem(e.getPlayer());
    }

    @EventHandler
    public void unload(PluginDisableEvent e){
        if(e.getPlugin().equals(Challenges.Main)){
            for(Player p : Bukkit.getOnlinePlayers()){
                rem(p);
            }
            registered.clear();
        }
    }

    private void checkIsland(Player sender, Object packet) throws Exception {
        if(packet.getClass().equals(SpigotApi.getReflectionApi().spigot().getNMSClass("PacketPlayInUpdateSign"))){
            Class<?> cBaseBlockPosition = SpigotApi.getReflectionApi().spigot().getNMSClass("BaseBlockPosition");
            Object oBlockPosition = getField(packet, "a");
            Location block = new Location(sender.getWorld(),
                    ((Number)cBaseBlockPosition.getMethod("getX").invoke(oBlockPosition)).doubleValue(),
                    ((Number)cBaseBlockPosition.getMethod("getY").invoke(oBlockPosition)).doubleValue(),
                    ((Number)cBaseBlockPosition.getMethod("getZ").invoke(oBlockPosition)).doubleValue());
            block.getBlock().getState().update();
            Challenges.getAdminPrompt().receive(sender, (String[]) getField(packet, "b"));
        }
    }

    private void registerPlayer(Player p) {
        ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {
            @Override
            public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception {
                //ajouter les methodes qui veulent récuperer un packet
                checkIsland(p,packet);
                super.channelRead(channelHandlerContext, packet);
            }
            @Override
            public void write(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise channelPromise) throws Exception {
                super.write(channelHandlerContext, packet, channelPromise);
            }
        };
        ChannelPipeline pipeline = getNettyChannel(p).pipeline();
        pipeline.addBefore("packet_handler", p.getName(), channelDuplexHandler);
    }

    private void unregisterPlayer(Player p) {
        Channel channel = getNettyChannel(p);
        channel.eventLoop().submit(() -> {
            channel.pipeline().remove(p.getName());
            return null;
        });
    }

    private Channel getNettyChannel(Player p) {
        try {
            Object ep = p.getClass().getMethod("getHandle").invoke(p);
            Object connection = ep.getClass().getField("playerConnection").get(ep);
            Object network = connection.getClass().getField("networkManager").get(connection);
            return (Channel) network.getClass().getField("channel").get(network);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Object getField(Object o, String fieldname) {
        Object value = null;
        try {
            Field f = o.getClass().getDeclaredField(fieldname);
            f.setAccessible(true);
            value = f.get(o);
            f.setAccessible(false);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return value;
    }

}
