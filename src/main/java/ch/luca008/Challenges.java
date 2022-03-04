package ch.luca008;

import ch.luca008.Admin.Editor.SessionManager;
import ch.luca008.Admin.Editor.Sockets.Packets.ChallengeStatePacket;
import ch.luca008.Admin.IslandInventoryEvents;
import ch.luca008.ChallengesManager.Inventory.ChallengesInventory;
import ch.luca008.ChallengesManager.IslandStorage.Storage;
import ch.luca008.ChallengesManager.Manager;
import ch.luca008.ChallengesManager.Reset;
import ch.luca008.Commands.ChallengeAdminCommand;
import ch.luca008.Commands.ChallengeCommand;
import ch.luca008.Events.ChallengeRelatedEvents;
import ch.luca008.Events.UniPlayerManager;
import ch.luca008.Utils.FabledUtils;
import ch.luca008.Utils.PromptPlayer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

//Command to add nether access
//cadmin island acs type playername true|false
//with type=nether or end not case sensitive
public class Challenges extends JavaPlugin implements Listener {

    private File trueFile = new File(getDataFolder(), "true.state");
    public static Challenges Main;
    private boolean areChallengesEnabled = false;
    private Manager challenges;
    private ChallengesInventory inventoryManager;
    private FabledUtils api;
    private Config globalConfig;
    private Lang lang;
    private Economy economy;
    private Reset reset;
    private PromptPlayer adminPrompt;
    private SessionManager editor;
    public final static String clientVersion = "1.1.1";

    private void commandsRegister(){
        this.getCommand("challenge").setExecutor(new ChallengeCommand());
        this.getCommand("challengeadmin").setExecutor(new ChallengeAdminCommand(this));
    }

    private void eventsRegister(){
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(this,this);
        pm.registerEvents(new UniPlayerManager(), this);
        pm.registerEvents(new ChallengeRelatedEvents(), this);
        //Admin
        pm.registerEvents(new IslandInventoryEvents(), this);
    }

    public void onEnable() {
        Main = this;
        hookFabled();
        challenges = new Manager();
        commandsRegister();
        eventsRegister();
        challenges.load();
        lang = Lang.getInstance(new File(getDataFolder(),"Lang"));
        inventoryManager = new ChallengesInventory();
        setupEconomy();
        globalConfig = new Config();
        reset = new Reset();
        adminPrompt = new PromptPlayer();
        editor = new SessionManager();
    }

    public void onDisable(){
        setStateFile(); //créé true.state si areChallengesEnabled() (pour save l'état global actuel des challenges)
        for(Player p : Bukkit.getOnlinePlayers()){
            p.closeInventory();//Pour éviter qu'un joueur puisse récupérer les items dans un des inventaires d'un challenge
        }
        if(ChallengeAdminCommand.getIslandSessions()!=null){
            ChallengeAdminCommand.unloadSessions();
        }
        if(reset!=null){
            reset.stop(true);
        }
        if(challenges!=null){
            challenges.unload();
            if(challenges.getAsyncManager()!=null) {
                challenges.getAsyncManager().shutdown();
            }
            challenges = null;
        }
        if(adminPrompt!=null){
            adminPrompt.clear();
        }
        if(getEditor()!=null){
            SessionManager.Session current = getEditor().getCurrent();
            if(current!=null){
                current.sendMessage("§cLa session avec l'éditeur va être fermée. §7Raison: §8Le plugin Challenges a été désactivé.");
                getEditor().stopSession("Challenges shutdowning...");
            }
        }
    }

    public void setChallengesEnabled(boolean enabled, boolean sendPacket){
        this.areChallengesEnabled = enabled;
        if(!enabled){
            long start = System.currentTimeMillis();
            List<Storage> storages = getManager().getLoadedStorages();
            CompletableFuture<?>[] t = new CompletableFuture[storages.size()];
            for(int i=0;i<t.length;i++){
                CompletableFuture<Void> c = CompletableFuture.runAsync(storages.get(i)::unload);
                t[i] = c;
            }
            try {
                CompletableFuture.allOf(t).get(7, TimeUnit.SECONDS);
                System.out.println("Unloaded " + storages.size() + " island storages in " + (System.currentTimeMillis()-start) + " milliseconds.");
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                System.err.println("Les îles n'ont pas pû être déchargées... Time out (>7sec)?");
                e.printStackTrace();
            }
            getManager().getLoadedStorages().clear();
            System.out.println("Challenges are now disabled.");
        }
        if(sendPacket&&getEditor()!=null&&getEditor().getCurrent()!=null&&getEditor().getCurrent().getClient()!=null){
            getEditor().getCurrent().getClient().send(new ChallengeStatePacket(-1, areChallengesEnabled));
        }
    }
    public boolean areChallengesEnabled(){
        return areChallengesEnabled;
    }

    public static Manager getManager(){
        return Main.challenges;
    }

    public static FabledUtils getFabledApi(){
        return Main.api;
    }

    public static Lang getLangManager(){
        return Main.lang;
    }

    public static Economy getEconomy(){
        return Main.economy;
    }

    /**
     * Need to be called when a category or challenge changes (from editor)
     */
    public static void updateInventoryManager(){
        Main.inventoryManager = new ChallengesInventory();
    }
    public static ChallengesInventory getInventoryManager(){
        return Main.inventoryManager;
    }

    public static Config getGlobalConfig(){
        return Main.globalConfig;
    }

    public static Reset getResetManager(){
        return Main.reset;
    }

    public static PromptPlayer getAdminPrompt(){
        return Main.adminPrompt;
    }

    public static SessionManager getEditor() {
        return Main.editor;
    }

    @EventHandler
    public void fabledLoad(PluginEnableEvent e){
        if(e.getPlugin().getName().equals("FabledSkyBlock")){
            hookFabled();
        }
    }

    private void hookFabled(){
        if(getServer().getPluginManager().isPluginEnabled("FabledSkyBlock")){
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, ()->{
                api = new FabledUtils();
                if(getStateFile()){
                    setChallengesEnabled(true, false);//false car aucune session ne peut etre encore etre crée jusque ici
                }
                System.out.println("Challenges now hooked to FabledSkyBlock! Challenges enabled.");
            }, 60L);
        }
    }

    public static void reloadLang(){
        Main.lang = Lang.getInstance(new File(Main.getDataFolder(),"Lang"));
    }

    public static void reloadGlobalConfig(){
        getGlobalConfig().reload();
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null)
            return;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
            return;
        this.economy = (Economy)rsp.getProvider();
    }

    private boolean getStateFile(){
        if(trueFile.exists()){
            trueFile.delete();
            return true;
        }
        return false;
    }
    private void setStateFile(){
        if(areChallengesEnabled()){
            if(!trueFile.exists()){
                try {
                    trueFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            if(trueFile.exists()){
                trueFile.delete();
            }
        }
    }
}
