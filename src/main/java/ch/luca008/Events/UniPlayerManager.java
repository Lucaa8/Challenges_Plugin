package ch.luca008.Events;

import ch.luca008.Challenges;
import ch.luca008.ChallengesManager.Manager;
import ch.luca008.UniPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLocaleChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class UniPlayerManager implements Listener {

    private Manager manager;

    public UniPlayerManager(){
        manager = Challenges.getManager();
    }

    @EventHandler
    public void joinEvent(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        manager.loadPlayer(p.getUniqueId());
    }

    @EventHandler
    public void quitEvent(PlayerQuitEvent e){
        Player p = e.getPlayer();
        manager.unloadPlayer(p.getUniqueId());
    }

    @EventHandler
    public void localeChangedEvent(PlayerLocaleChangeEvent e){
        String loc = e.getLocale();
        if(loc.contains("_")){
            UniPlayer up = manager.retrieveUniPlayerByUUID(e.getPlayer().getUniqueId());
            if(!up.isLangSet()){
                up.changeLang(loc.split("_")[0], false);
            }
        }
    }
}
