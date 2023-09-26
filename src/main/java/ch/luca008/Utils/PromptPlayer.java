package ch.luca008.Utils;

import ch.luca008.SpigotApi.Api.PromptApi;
import ch.luca008.SpigotApi.SpigotApi;
import org.bukkit.entity.Player;

public class PromptPlayer {

    public static void promptPlayer(Player p, PromptApi.PromptCallback callback, String...initialLines){
        if(!Perms.Permission.CADMIN_EDITOR_PROMPTABLE.hasPermission(p)){
            callback.getInput(true, new String[0], "");
            return;
        }
        p.closeInventory();
        if(!SpigotApi.getPromptApi().promptPlayer(p, callback, initialLines)){
            callback.getInput(true, new String[0], "");
        }
    }

}
