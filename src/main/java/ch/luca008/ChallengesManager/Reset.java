package ch.luca008.ChallengesManager;

import ch.luca008.Challenges;
import ch.luca008.Utils.JsonUtils;
import org.bukkit.Bukkit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class Reset {

    public final static String timeZone = "Europe/Paris";

    private Timer timer;
    private long resetTime;

    private List<UUID> toReset;

    public Reset(){
        toReset = new ArrayList<>();
        JSONObject j = JsonUtils.readFile(Manager.resetDailyFile);
        if(j!=null&&!j.isEmpty()){
            if(j.containsKey("Reset")){
                JSONArray jarr = (JSONArray) j.get("Reset");
                for (Object o : jarr) {
                    toReset.add(UUID.fromString((String)o));
                }
            }
        }
        resetTime = getMidnight();
        start();
    }

    private long getMidnight(){
        ZoneId zoneId = ZoneId.of(Reset.timeZone);
        return Date.from(ZonedDateTime.now(zoneId).toLocalDate().atStartOfDay(zoneId).plusDays(1).toInstant()).getTime();
    }

    public List<UUID> getIslands(){
        return toReset;
    }

    public void addIsland(UUID island){
        if(toReset!=null&&!toReset.contains(island))
            toReset.add(island);
    }

    public void reset(){
        final long start = System.currentTimeMillis();
        stop(false);
        if(toReset!=null&&!toReset.isEmpty()){
            Challenges.Main.setChallengesEnabled(false, true);
            Challenges.getManager().getAsyncManager().resetDailyStorages(toReset);
            toReset.clear();
            Challenges.Main.setChallengesEnabled(true, true);
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(Challenges.Main, ()->{
            resetTime = getMidnight();
            start();
            System.out.println("Reset done in "+(System.currentTimeMillis()-start)+"ms.");
        }, 1L);
    }

    public void start(){
        stop(false);
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(System.currentTimeMillis()>=resetTime){
                    if(Challenges.getGlobalConfig().doReset()){
                        reset();
                    }
                }
            }
        }, 0, 1000);
    }

    public void stop(boolean unload){
        if(timer!=null){
            timer.cancel();
        }
        if(unload){
            unload();
        }
    }

    public void unload(){
        JSONObject j = new JSONObject();
        JSONArray jarr = new JSONArray();
        for (UUID uuid : toReset) {
            jarr.add(uuid.toString());
        }
        j.put("Reset",jarr);
        JsonUtils.write(Manager.resetDailyFile, JSONObject.toJSONString(j));
    }
}
