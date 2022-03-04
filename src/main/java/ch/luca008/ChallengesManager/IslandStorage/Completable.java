package ch.luca008.ChallengesManager.IslandStorage;

import org.json.simple.JSONObject;

public interface Completable {

    enum CompletableType{
        INVENTORY("InventoryCompletable"),
        STATISTIC("StatisticCompletable");

        private String clazz;
        CompletableType(String str){
            this.clazz = str;
        }
        public String getClazz(){return clazz;}
    }

    public JSONObject toJson();

    public CompletableType getType();

    @Override
    public String toString();

}
