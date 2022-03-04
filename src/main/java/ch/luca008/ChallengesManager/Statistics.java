package ch.luca008.ChallengesManager;

import ch.luca008.Challenges;
import ch.luca008.ChallengesManager.Challenges.Challenge;
import ch.luca008.Utils.JsonUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class Statistics {

    private List<ChallengeStatistic> challenges = new ArrayList<>();
    //Possibilité d'en ajouter d'autres (pour les catégories, p.ex, etc...)

    public Statistics(JSONObject json){
        if(json!=null&&!json.isEmpty()){
            if(json.containsKey("Challenges")){
                for(Object o : (JSONArray)json.get("Challenges")){
                    ChallengeStatistic c = new ChallengeStatistic((JSONObject)o);
                    challenges.add(c);
                }
            }
        }
    }

    public void unload(){
        try{
            File f = Manager.challengesStatisticsFile;
            if(!f.exists()){
                f.createNewFile();
            }
            Files.write(Paths.get(f.toURI()), JsonUtils.prettyJson(toJson()).getBytes(StandardCharsets.UTF_8));
            challenges.clear();
        }catch (IOException e) {
            System.err.println("Can't unload statistics to stats.json file.");
            e.printStackTrace();
        }
    }

    public JSONObject toJson(){
        JSONObject json = new JSONObject();
        if(challenges.size()>0){
            List<UUID> c = Challenges.getManager().getChallenges().stream().map(Challenge::getUuid).collect(Collectors.toList());
            JSONArray jarr = new JSONArray();
            for(ChallengeStatistic s : challenges){
                if(s.completed>0&&c.contains(s.uuid)){
                    jarr.add(s.toJson());
                }
            }
            if(jarr.size()>0){
                json.put("Challenges", jarr);
            }
        }
        return json;
    }

    /**
     * Get existing statistic or create new one
     * @param c the challenge
     * @return a challenge statistic class
     */
    public ChallengeStatistic getStatistic(Challenge c){
        for(ChallengeStatistic s : challenges){
            if(s.uuid.equals(c.getUuid()))return s;
        }
        ChallengeStatistic s = new ChallengeStatistic(c.getUuid());
        challenges.add(s);
        return s;
    }

    //Possibilité d'ajouter d'autres stats de challenges en+ de completed
    public class ChallengeStatistic{
        private UUID uuid;
        private int completed = 0;

        public ChallengeStatistic(JSONObject json){
            if(json.containsKey("UUID")){
                this.uuid = UUID.fromString((String) json.get("UUID"));
            }
            if(json.containsKey("Completed")){
                this.completed = JsonUtils.getInt(json, "Completed");
            }
        }

        public ChallengeStatistic(UUID uuid){
            this.uuid = uuid;
        }

        public UUID getUuid(){
            return uuid;
        }

        public int getCompleted(){
            return completed;
        }

        public void addCompleted(){
            completed+=1;
        }

        public JSONObject toJson(){
            JSONObject json = new JSONObject();
            json.put("UUID", uuid);
            json.put("Completed", completed);
            return json;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ChallengeStatistic)) return false;
            ChallengeStatistic that = (ChallengeStatistic) o;
            return uuid.equals(that.uuid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uuid);
        }
    }

}
