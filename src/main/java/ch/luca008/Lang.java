package ch.luca008;

import ch.luca008.Utils.JsonUtils;
import ch.luca008.Utils.StringUtils;
import org.bukkit.Statistic;
import org.json.simple.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Lang {

    public static String defaultLang = "FR";

    private Map<String, Map<String, String>> messages;
    private Map<String, Map<String, String>> challenges;
    private Map<String, Map<String, String>> statistics;
    private Map<String, Map<String, String>> units;

    private Lang(Map<String, Map<String, String>> messages, Map<String, Map<String, String>> challenges, Map<String, Map<String, String>> statistics, Map<String, Map<String, String>> units){
        this.messages = messages;
        this.challenges = challenges;
        this.statistics = statistics;
        this.units = units;
    }

    public static Lang getInstance(File langDirectory){
        Map<String, Map<String, String>> m = new HashMap<>();
        Map<String, Map<String, String>> c = new HashMap<>();
        Map<String, Map<String, String>> s = new HashMap<>();
        Map<String, Map<String, String>> u = new HashMap<>();
        for(File langFile : langDirectory.listFiles()){
            if(langFile.getName().startsWith("Storage"))continue;
            String lang = langFile.getName().split(".json")[0];
            Map<String, String> msg = new HashMap<>();
            Map<String, String> cha = new HashMap<>();
            Map<String, String> sta = new HashMap<>();
            Map<String, String> uni = new HashMap<>();
            JSONObject json = JsonUtils.readFile(langFile);
            for(Map.Entry e : (Set<Map.Entry>)((JSONObject)json.get("Messages")).entrySet()){
                msg.put(e.getKey().toString(), e.getValue().toString());
            }
            for(Map.Entry e : (Set<Map.Entry>)((JSONObject)json.get("Challenges")).entrySet()){
                cha.put(e.getKey().toString(), e.getValue().toString());
            }
            for(Map.Entry e : (Set<Map.Entry>)((JSONObject)json.get("Statistics")).entrySet()){
                sta.put(e.getKey().toString(), e.getValue().toString());
            }
            for(Map.Entry e : (Set<Map.Entry>)((JSONObject)json.get("Base-Unit")).entrySet()){
                uni.put(e.getKey().toString(), e.getValue().toString());
            }
            m.put(lang, msg);
            c.put(lang, cha);
            s.put(lang, sta);
            u.put(lang, uni);
        }
        System.out.println("Loaded lang files.");
        return new Lang(m,c,s,u);
    }

    public boolean doLangExist(String lang){
        return messages.containsKey(lang.toUpperCase());
    }
    public Set<String> getAvailableLangs(){
        return messages.keySet();
    }

    private String getDefault(Map<String, Map<String, String>> type, String key){
        return type.get(defaultLang).getOrDefault(key,"§cUnknown key: §4"+key);
    }

    public String getMessage(String lang, String key, boolean message){
        if(message){
            if(messages.containsKey(lang)){
                return messages.get(lang).getOrDefault(key, getDefault(messages, key));
            }
        }else{
            if(challenges.containsKey(lang)){
                return challenges.get(lang).getOrDefault(key, getDefault(challenges, key));
            }
        }
        return "§cUnknown lang: " + lang;
    }

    public String getStatistic(String lang, Statistic statistic){
        if(statistics.containsKey(lang)){
            return statistics.get(lang).getOrDefault(statistic.name(), StringUtils.enumName(statistic.name()));
        }
        return "§cUnknown lang: " + lang;
    }

    public String getUnit(String lang, String key){
        if(units.containsKey(lang)){
            return units.get(lang).getOrDefault(key, getDefault(units, key));
        }
        return "§cUnknown lang: " + lang;
    }
}
