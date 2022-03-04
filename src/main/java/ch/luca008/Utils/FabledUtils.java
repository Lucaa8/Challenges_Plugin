package ch.luca008.Utils;

import com.songoda.skyblock.SkyBlock;
import com.songoda.skyblock.api.SkyBlockAPI;
import com.songoda.skyblock.api.island.Island;
import com.songoda.skyblock.api.island.IslandManager;
import com.songoda.skyblock.api.island.IslandRole;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

public class FabledUtils {

    private IslandManager manager;

    public FabledUtils(){
        manager = SkyBlockAPI.getIslandManager();
    }

    public boolean hasIsland(UUID uuid){
        return IslandManager.hasIsland(Bukkit.getOfflinePlayer(uuid));
    }

    /**
     * Get the current island of the given player (only for online players)
     * @param player The player (must be non-null and online)
     * @return His current island, or null if none (loaded)
     */
    @Nullable
    public Island getIsland(@Nonnull Player player){
        return getIsland(player.getUniqueId());
    }

    /**
     * See {@link #getIsland(Player)}
     * @param player The player's uuid
     */
    @Nullable
    public Island getIsland(@Nonnull UUID player){
        Island i = manager.getIsland(Bukkit.getOfflinePlayer(player));
        if(i!=null&&i.getIsland()!=null){
            return i;
        }
        return null;
    }

    /**
     * Get the current island's uuid of the given (offline)player
     * @param player The player
     * @return His current island's uuid, or null if none (created)
     */
    public UUID getIslandUUID(OfflinePlayer player){
        return getIslandUUID(player.getUniqueId());
    }

    /**
     * See {@link #getIslandUUID(OfflinePlayer)}
     * @param player The player's uuid
     */
    public UUID getIslandUUID(UUID player){
        return _getIslandUUID(player);
    }

    public Island retrieveIslandByUUID(UUID islandUUID){
        return manager.getIslandByUUID(islandUUID);
    }

    public long getIsLevel(UUID island){
        return manager.getIslandByUUID(island).getLevel().getLevel();
    }

    public boolean isOnIsland(Player p){
        if(!hasIsland(p.getUniqueId()))return false;
        return manager.isPlayerAtIsland(getIsland(p), p);
    }
    public boolean isOnIsland(Island i, Location l){
        return manager.isLocationAtIsland(i, l);
    }

    public List<OfflinePlayer> getPlayersOnIsland(Island i, boolean addCoop, boolean addVisitor){
        Set<UUID> uuids = new HashSet<>();
        uuids.addAll(i.getPlayersWithRole(IslandRole.OWNER));
        uuids.addAll(i.getPlayersWithRole(IslandRole.OPERATOR));
        uuids.addAll(i.getPlayersWithRole(IslandRole.MEMBER));
        if(addCoop)uuids.addAll(i.getPlayersWithRole(IslandRole.COOP));
        if(addVisitor)uuids.addAll(i.getPlayersWithRole(IslandRole.VISITOR));
        List<OfflinePlayer> players = new ArrayList<>();
        uuids.forEach(p->{
            players.add(Bukkit.getOfflinePlayer(p));
        });
        return players;
    }

    public List<Player> getOnlinePlayersOnIsland(Island i, boolean addCoop, boolean addVisitor){
        List<Player> players = new ArrayList<>();
        for(OfflinePlayer p : getPlayersOnIsland(i,addCoop,addVisitor)){
            if(p.isOnline())players.add(Bukkit.getPlayer(p.getName()));
        }
        return players;
    }

    private UUID _getIslandUUID(UUID playerUUID){
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);
        Island i = getIsland(player.getUniqueId());
        if(i!=null)return i.getIslandUUID();
        SkyBlock sb = SkyBlock.getInstance();
        if(sb!=null){
            UUID island = null;
            if(sb.getPlayerDataManager().hasPlayerData(player.getUniqueId())){
                island = sb.getPlayerDataManager().getPlayerData(player.getUniqueId()).getIsland();
            }else{
                File f = new File(sb.getDataFolder() + "/player-data", player.getUniqueId() + ".yml");
                if(f.exists()){
                    FileConfiguration config = sb.getFileManager().getConfig(f).getFileConfiguration();
                    if(config.contains("Island.Owner")){
                        File isFile = new File(sb.getDataFolder() + "/island-data", config.getString("Island.Owner")+".yml");
                        if(isFile.exists()){
                            island = UUID.fromString(sb.getFileManager().getConfig(isFile).getFileConfiguration().getString("UUID"));
                        }
                    }
                }
            }
            return island;
        }
        return null;
    }

}
