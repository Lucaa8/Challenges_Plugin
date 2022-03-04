package ch.luca008.Utils;

import NBT.NMSManager;
import ch.luca008.Challenges;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.UUID;

public class PromptPlayer {

    private final PacketsListener packetsListener;
    public boolean isPromptable(Player p){
        return !prompt.containsKey(p.getUniqueId())&&Perms.Permission.CADMIN_EDITOR_PROMPTABLE.hasPermission(p);
    }
    public void removePlayer(UUID toRemove){
        packetsListener.rem(Bukkit.getPlayer(toRemove));
        prompt.remove(toRemove);
    }
    public enum SignColor{
        BLACK, BLUE, BROWN, CYAN, GRAY, GREEN, LIGHT_BLUE, LIGHT_GRAY, LIME, MAGENTA, ORANGE, PINK, PURPLE, RED, WHITE, YELLOW;
    }

    private HashMap<UUID, PromptCallback> prompt = new HashMap<>();

    public PromptPlayer(){
        this.packetsListener = new PacketsListener();
        Bukkit.getPluginManager().registerEvents(packetsListener, Challenges.Main);
    }

    public void promptPlayer(PromptCallback callback, Player p, String...initialLines){
        if(!isPromptable(p)){
            callback.getInput(true, new String[0], "");
            return;
        }
        p.closeInventory();
        prompt.put(p.getUniqueId(), callback);
        try {
            prompt(p, initialLines);
        } catch (NoSuchMethodException | NoSuchFieldException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            prompt.remove(p.getUniqueId());
        }
    }

    private void prompt(Player p, String...initialLines) throws NoSuchMethodException, NoSuchFieldException, InvocationTargetException, IllegalAccessException, InstantiationException {
        if(initialLines==null||initialLines.length>4){
            initialLines = new String[]{"","","",""};
        }
        if(initialLines.length<4){
            String[] temp = new String[4];
            for(int i=0;i<4;i++){
                if(i<initialLines.length){
                    temp[i] = initialLines[i];
                }else{
                    temp[i] = "";
                }
            }
            initialLines = new String[4];
            System.arraycopy(temp, 0, initialLines, 0, 4);
        }
        packetsListener.add(p);
        Location l = p.getLocation();
        //WorldServer
        Object ows = l.getWorld().getClass().getMethod("getHandle").invoke(l.getWorld());
        //BlockPosition
        Object obp = NMSManager.getNMSClass("BlockPosition").getConstructor(int.class, int.class, int.class).newInstance(l.getBlockX(), l.getBlockY(), l.getBlockZ());
        //CraftBlockData
        Class<?> cBlockData = NMSManager.getOBCClass("block.data","CraftBlockData");
        Object oBlockData = cBlockData.cast(Material.OAK_SIGN.createBlockData());
        Object oBlock = cBlockData.getMethod("getState").invoke(oBlockData);
        //TileSignEntity
        Object oSign = NMSManager.getNMSClass("TileEntitySign").getConstructor().newInstance();
        oSign.getClass().getMethod("setLocation", NMSManager.getNMSClass("World"), obp.getClass()).invoke(oSign, ows, obp);
        oSign.getClass().getMethod("setColor", NMSManager.getNMSClass("EnumColor")).invoke(oSign, NMSManager.getNMSClass("EnumColor").getMethod("valueOf", String.class).invoke(null, Challenges.getGlobalConfig().getPromptColor().name()));
        Object olines = NMSManager.getOBCClass("block","CraftSign").getMethod("sanitizeLines", String[].class).invoke(null, (Object) initialLines);
        Object olinesField = oSign.getClass().getField("lines").get(oSign);
        System.arraycopy(olines, 0, olinesField, 0, Array.getLength(olinesField));
        Object oNBT = oSign.getClass().getMethod("save", NMSManager.getNMSClass("NBTTagCompound")).invoke(oSign, NMSManager.getNMSClass("NBTTagCompound").getConstructor().newInstance());
        //PacketPlayOutBlockChange
        Object oPacketBC = NMSManager.getNMSClass("PacketPlayOutBlockChange").getConstructor(NMSManager.getNMSClass("IBlockAccess"), obp.getClass()).newInstance(ows, obp);
        oPacketBC.getClass().getField("block").set(oPacketBC, oBlock);
        //PacketPlayOutTileEntityData
        Object oPacketOTED = NMSManager.getNMSClass("PacketPlayOutTileEntityData").getConstructor(obp.getClass(), int.class, oNBT.getClass()).newInstance(obp, 9, oNBT);
        //PacketPlayOutOpenSignEditor
        Object oPacketOSE = NMSManager.getNMSClass("PacketPlayOutOpenSignEditor").getConstructor(obp.getClass()).newInstance(obp);
        //sendPacket
        Object oep = p.getClass().getMethod("getHandle").invoke(p);
        Object playerConnection = oep.getClass().getField("playerConnection").get(oep);
        Method sendPacket = playerConnection.getClass().getMethod("sendPacket", NMSManager.getNMSClass("Packet"));
        sendPacket.invoke(playerConnection, oPacketBC);
        sendPacket.invoke(playerConnection, oPacketOTED);
        sendPacket.invoke(playerConnection, oPacketOSE);
    }

    public void receive(Player sender, String[] lines){
        UUID u = sender.getUniqueId();
        packetsListener.rem(sender);
        if(prompt.containsKey(u)){
            String temp = "";
            for(String s : lines)temp+=s;
            final String line = temp;
            Bukkit.getScheduler().runTask(Challenges.Main, ()->{
                prompt.get(u).getInput(line.equalsIgnoreCase(Challenges.getGlobalConfig().getPrompCancelCmd()), lines, line);
                prompt.remove(u);
            });
        }
    }

    public void clear(){
        if(prompt!=null){
            prompt.clear();
        }
    }

    public interface PromptCallback{
        void getInput(boolean isCancelled, String[] asMultipleLines, String asSingleLine);
    }
}
