package ch.luca008.Utils;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.sql.*;
import java.util.*;

public class SkyblockUtils {

    public static SuperiorPlayer getPlayer(@Nonnull UUID player){
        return SuperiorSkyblockAPI.getPlayer(player);
    }

    public static boolean hasIsland(UUID uuid){
        return getPlayer(uuid).hasIsland();
    }

    /**
     * Get the current island of the given player (only for online players)
     * @param player The player (must be non-null and online)
     * @return His current island, or null if none (loaded)
     */
    @Nullable
    public static Island getIsland(@Nonnull Player player){
        return getIsland(player.getUniqueId());
    }

    /**
     * See {@link #getIsland(Player)}
     * @param player The player's uuid
     */
    @Nullable
    public static Island getIsland(@Nonnull UUID player){
        return getPlayer(player).getIsland();
    }

    /**
     * Get the current island's uuid of the given (offline)player
     * @param player The player
     * @return His current island's uuid, or null if none (created)
     */
    public static UUID getIslandUUID(OfflinePlayer player){
        return getIslandUUID(player.getUniqueId());
    }

    /**
     * See {@link #getIslandUUID(OfflinePlayer)}
     * @param player The player's uuid
     */
    public static UUID getIslandUUID(UUID player){
        return _getIslandUUID(player);
    }

    public static Island retrieveIslandByUUID(UUID islandUUID){
        return SuperiorSkyblockAPI.getIslandByUUID(islandUUID);
    }

    public static long getIsLevel(UUID island){
        Island i = retrieveIslandByUUID(island);
        if(i != null){
            return i.getIslandLevel().longValue();
        }
        return -1;
    }

    public static boolean isOnIsland(@Nonnull Player p){
        return getPlayer(p.getUniqueId()).isInsideIsland();
    }
    public static boolean isOnIsland(@Nonnull Island i, Location l){
        return i.isInside(l);
    }

    public static List<OfflinePlayer> getPlayersOnIsland(@Nonnull Island i){
        return i.getIslandMembers(true).stream().map(SuperiorPlayer::asOfflinePlayer).filter(Objects::nonNull).toList();
    }

    public static List<Player> getOnlinePlayersOnIsland(@Nonnull Island i){
        return getPlayersOnIsland(i).stream()
                .filter(OfflinePlayer::isOnline)
                .map(OfflinePlayer::getPlayer)
                .filter(Objects::nonNull).toList();
    }

    private static UUID _getIslandUUID(UUID playerUUID){
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);
        Island i = getIsland(player.getUniqueId());
        if(i!=null)return i.getUniqueId();
        Connection conn = null;
        UUID uuidFound = null;
        try {
            String url = "jdbc:sqlite:"+SuperiorSkyblockAPI.getSuperiorSkyblock().getDataFolder().getAbsolutePath()+"/datastore/database.db";
            conn = DriverManager.getConnection(url);

            PreparedStatement sql = conn.prepareStatement("SELECT uuid FROM islands WHERE owner=?;");
            sql.setString(1, playerUUID.toString());
            ResultSet result = sql.executeQuery();
            result.next();
            String strUuid = result.getString("uuid");
            if(strUuid != null){
                uuidFound = UUID.fromString(strUuid);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
        return uuidFound;
    }

}
