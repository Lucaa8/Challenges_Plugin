package ch.luca008;

import ch.luca008.ChallengesManager.Categories.Category;
import ch.luca008.ChallengesManager.IslandStorage.Storage;
import ch.luca008.ChallengesManager.Manager;
import ch.luca008.Utils.JsonUtils;
import com.google.common.collect.ImmutableList;
import com.songoda.skyblock.api.island.Island;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.*;

public class UniPlayer {

    OfflinePlayer player;
    private boolean langSet = false;
    private String lang;
    private Map<UUID, Integer> challengesCompletion = new HashMap<>();

    public UniPlayer(JSONObject json, OfflinePlayer defaultPlayer){
        if(json.containsKey("Lang")){
            lang = (String) json.get("Lang");
            langSet = true;
        }else lang = Lang.defaultLang;
        if(json.containsKey("UUID")){
            player = Bukkit.getOfflinePlayer(UUID.fromString((String)json.get("UUID")));
        }else player = defaultPlayer;
        if(json.containsKey("ChallengesCompletions")){
            for(Map.Entry s : (Set<Map.Entry>)((JSONObject)json.get("ChallengesCompletions")).entrySet()){
                challengesCompletion.put(UUID.fromString((String)s.getKey()), JsonUtils.getInt(s.getValue()));
            }
        }
    }

    public UniPlayer(@Nonnull UUID player){
        this.player = Bukkit.getOfflinePlayer(player);
        this.lang = Lang.defaultLang;
    }

    public JSONObject toJson(){
        JSONObject j = new JSONObject();
        if(langSet){
            j.put("Lang", lang);
        }
        if(challengesCompletion!=null&&!challengesCompletion.isEmpty()){
            JSONObject completions = new JSONObject();
            challengesCompletion.forEach((K,V)->{
                if(V>0)completions.put(K,V);
            });
            if(!completions.isEmpty())j.put("ChallengesCompletions", completions);
        }
        //ajouter tout le reste avant ca
        if(j.isEmpty())return null;
        j.put("UUID", player.getUniqueId());
        return j;
    }

    public void unload(){
        JSONObject json = toJson();
        File f = new File(Manager.playersBaseDirectory, player.getUniqueId().toString()+".json");
        if(json!=null&&!json.isEmpty()){
            JsonUtils.write(f, JsonUtils.prettyJson(json));
        }else{
            if(f.exists())f.delete();
        }
    }

    public OfflinePlayer getOfflinePlayer(){
        return player;
    }

    public Optional<Player> getPlayer(){
        return Optional.ofNullable(Bukkit.getPlayer(player.getUniqueId()));
    }

    /**
     * @param set if player change his lang with command then set = true, if LocaleChangeEvent et isLangSet()=false then set=false
     */
    public boolean changeLang(String lang, boolean set){
        if(Challenges.getLangManager().doLangExist(lang)){
            this.lang = lang.toUpperCase();
            this.langSet = set;
            return true;
        }
        return false;
    }

    public void resetLang(){
        Player p = getPlayer().orElse(null);
        lang = null;
        if(p!=null){
            String loc = p.getLocale();
            if(loc.contains("_")){
                loc = loc.split("_")[0];
                if(Challenges.getLangManager().doLangExist(loc)){
                    lang = loc.toUpperCase();
                }
            }
        }
        lang = lang==null?Lang.defaultLang:lang;
        langSet = false;
    }

    public boolean isLangSet(){
        return langSet;
    }

    public String getLang(){
        return lang;
    }

    public void setChallengesCompletion(UUID challenge, int count){
        challengesCompletion.put(challenge, count);
    }

    public int addChallengeCompletion(UUID challenge, int addValue){
        int c = getChallengeCompletions(challenge)+addValue;
        setChallengesCompletion(challenge, c);
        return c;
    }

    public int getChallengeCompletions(UUID challenge){
        return challengesCompletion.getOrDefault(challenge, 0);
    }

    public void openMainMenuInventory(){
        openMenuInventory(1);
    }

    public void openMenuInventory(int page){
        if(!Challenges.getInventoryManager().doCategoriesPageExists(page))return;
        Optional<Player> player = getPlayer();
        if(player.isPresent()){
            player.get().openInventory(Challenges.getInventoryManager().asInventory(this, page));
        }
    }

    public void openCategory(Category c){
        openCategory(c,1);
    }

    public void openCategory(Category c, int page){
        if(!Challenges.getInventoryManager().doCategoryPageExists(c, page))return;
        Optional<Player> player = getPlayer();
        if(player.isPresent()){
            player.get().openInventory(Challenges.getInventoryManager().asInventory(this, c, page));
        }
    }

    private String _getMessage(boolean message, String key, Map.Entry<String, String>...replacements){
        String msg = Challenges.getLangManager().getMessage(lang, key, message);
        if(replacements.length>0){
            for(Map.Entry<String, String> r : replacements){
                msg = msg.replace(r.getKey(), r.getValue());
            }
        }
        return msg;
    }

    public String getMessage(String key, Map.Entry<String, String>...replacements){
        return _getMessage(true,key,replacements);
    }

    public String getChallengeMessage(String key, Map.Entry<String, String>...replacements){
        return _getMessage(false,key,replacements);
    }

    public String getStatistic(Statistic statistic){
        return Challenges.getLangManager().getStatistic(lang,statistic);
    }

    public String getUnit(String key){
        return Challenges.getLangManager().getUnit(lang,key);
    }

    public void sendMessage(String key, Map.Entry<String, String>...replacements){
        Optional<Player> p = getPlayer();
        p.ifPresent(player -> player.sendMessage(_getMessage(true, key, replacements)));
    }

    public void sendMessage(String key, Map<String,String> replacements){
        if(replacements==null||replacements.isEmpty())sendMessage(key,new Map.Entry[0]);
        Map.Entry<String,String>[] entries = new Map.Entry[replacements.size()];
        int loc = 0;
        for(Map.Entry<String,String> entry : replacements.entrySet()){
            entries[loc] = entry;
            loc++;
        }
        sendMessage(key, entries);
    }

    public Optional<Island> getIsland(){
        Island i = null;
        if(Challenges.getFabledApi().hasIsland(player.getUniqueId())){
            i = Challenges.getFabledApi().getIsland(player.getUniqueId());
        }
        return Optional.ofNullable(i);
    }

    public Optional<Storage> getIslandStorage(){
        Optional<Island> is = getIsland();
        if(is.isPresent()){
            return Challenges.getManager().retrieveStorageByUUID(is.get().getIslandUUID());
        }
        return Optional.empty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UniPlayer)) return false;
        UniPlayer uniPlayer = (UniPlayer) o;
        return player.getUniqueId().equals(uniPlayer.player.getUniqueId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(player.getUniqueId());
    }
}
