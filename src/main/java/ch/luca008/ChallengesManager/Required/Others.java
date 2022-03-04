package ch.luca008.ChallengesManager.Required;

import ch.luca008.Challenges;
import ch.luca008.ChallengesManager.Challenges.Challenge;
import ch.luca008.ChallengesManager.IslandStorage.Storage;
import ch.luca008.UniPlayer;
import ch.luca008.Utils.JsonUtils;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class Others implements Required{

    private Money money;
    private Experience exp;
    private IsLevel islvl;

    public Others(JSONObject json){
        if(json.containsKey("Money")){
            money = new Money((JSONObject)json.get("Money"));
        }
        if(json.containsKey("Exp")){
            exp = new Experience((JSONObject)json.get("Exp"));
        }
        if(json.containsKey("IsLevel")){
            islvl = new IsLevel((JSONObject)json.get("IsLevel"));
        }
    }

    public Others(Money m, Experience e, IsLevel l){
        this.money = m;
        this.exp = e;
        this.islvl = l;
    }

    @Override
    public JSONObject toJson() {
        JSONObject j = new JSONObject();
        if(money!=null){
            j.put("Money", money.toJson());
        }
        if(exp!=null){
            j.put("Exp", exp.toJson());
        }
        if(islvl!=null){
            j.put("IsLevel", islvl.toJson());
        }
        if(j.isEmpty()){
            j.put("Content","Empty");
        }
        return j;
    }

    @Override
    public String toLore(Storage.ChallengeStorage storage, UniPlayer player) {
        String lore = "";
        if(money!=null){
            lore+="\n"+money.toLore(player);
        }
        if(exp!=null){
            lore+="\n"+exp.toLore(player);
        }
        if(islvl!=null){
            lore+="\n"+islvl.toLore(player);
        }
        return lore+"\n";
    }

    @Override
    public String toString(){
        return "Others{"+(money!=null?"Money:"+money:"")+
                (exp!=null?(money!=null?",":"")+"Experience:"+exp:"")+
                (islvl!=null?(money!=null||exp!=null?",":"")+"IsLevel:"+islvl:"")+"}";
    }

    @Override
    public RequiredType getType() {
        return RequiredType.Others;
    }

    private String getMissingMessage(UniPlayer p, String key, String challenge, int current, int needed){
        return p.getMessage("Challenge-Completion-Others-None-"+key,
                Map.entry("{0}", challenge),
                Map.entry("{1}",current+""),
                Map.entry("{2}", needed+""));
    }

    @Override
    public CompletableResult complete(Challenge c, UniPlayer p) {
        if(p.getPlayer().isPresent()){
            Player player = p.getPlayer().get();
            boolean isCompleted = true;
            String msg = "";
            if(money!=null&&money.getCount()>0){
                int amount = (int)money.get(player);
                if(amount<money.getCount()){
                    isCompleted = false;
                    msg = getMissingMessage(p,"Money",c.getName(),amount,money.getCount());
                }
            }
            if(exp!=null&&exp.getCount()>0){
                int amount = exp.get(player);
                if(amount<exp.getCount()){
                    isCompleted = false;
                    msg += (msg.length()==0?"":"\n")+getMissingMessage(p,"Exp",c.getName(),amount,exp.getCount());
                }
            }
            if(islvl!=null&&islvl.getCount()>0){
                Optional<com.songoda.skyblock.api.island.Island> island = p.getIsland();
                if(island.isPresent()){
                    long lvl = Challenges.getFabledApi().getIsLevel(island.get().getIslandUUID());
                    if(lvl<islvl.getCount()){
                        isCompleted = false;
                        msg += (msg.length()==0?"":"\n")+getMissingMessage(p,"Islvl",c.getName(),(int)lvl,islvl.getCount());
                    }
                }
            }
            if(isCompleted){
                if(money!=null&&money.getCount()>0&&money.doDelete())money.remove(player, money.getCount());
                if(exp!=null&&exp.getCount()>0&&exp.doDelete())exp.remove(player, exp.getCount());
            }
            boolean finalIsCompleted = isCompleted;
            String finalMsg = msg;
            return new CompletableResult() {
                @Override
                public boolean isCompleted() {
                    return finalIsCompleted;
                }

                @Override
                public boolean hasProgressed(){
                    return false;
                }

                @Override
                public Object getMessage() {
                    if(!finalIsCompleted){
                        return p.getMessage("Challenge-Completion-Unable", Map.entry("{0}", c.getName()))+finalMsg;
                    }
                    return "";
                }

                @Override
                public UniPlayer getPlayer(){
                    return p;
                }

                @Override
                public Challenge getChallenge(){
                    return c;
                }
            };
        }
        return null;
    }

    public static class Money{
        UUID uuid;
        int count;
        boolean delete;
        public Money(JSONObject json){
            if(json.containsKey("UUID")){
                uuid = UUID.fromString((String)json.get("UUID"));
            }else uuid = UUID.randomUUID();
            if(json.containsKey("Count")){
                count = JsonUtils.getInt(json, "Count");
            }else count = 1000;
            if(json.containsKey("Delete")){
                delete = (boolean)json.get("Delete");
            }else delete = false;
        }
        public Money(int count, boolean delete){
            this.uuid = UUID.randomUUID();
            this.count = count;
            this.delete = delete;
        }
        public JSONObject toJson(){
            JSONObject j = new JSONObject();
            j.put("UUID", uuid.toString());
            j.put("Count",count);
            if(delete)j.put("Delete", true);
            return j;
        }
        public String toLore(UniPlayer player){
            if(count<=0)return "";
            return "§f -§b " + count + " §a" +Challenges.getGlobalConfig().getCurrencyName()+(delete?"\n"+player.getChallengeMessage("Challenge-Item-Required-Others-Money-Delete"):"");
        }
        @Override
        public String toString() {
            return "Money{" +
                    "uuid=" + uuid +
                    ", count=" + count +
                    ", delete=" + delete +
                    '}';
        }
        public UUID getUuid(){
            return uuid;
        }
        public int getCount(){
            return count;
        }
        public boolean doDelete(){
            return delete;
        }
        public void give(Player p){
            if(p==null)return;
            Challenges.getEconomy().depositPlayer(p, getCount());
        }
        public void remove(Player p, int remove){
            if(p==null)return;
            if(Challenges.getEconomy().has(p, remove==-1?count:remove)){
                Challenges.getEconomy().withdrawPlayer(p, remove==-1?count:remove);
            }else{
                Challenges.getEconomy().withdrawPlayer(p, Challenges.getEconomy().getBalance(p));
            }
        }
        public double get(Player p){
            return Challenges.getEconomy().getBalance(p);
        }
    }

    public static class Experience{
        public enum ExpType{
            ORB("Challenge-Item-Reward-Exp-Orb"), LVL("Challenge-Item-Reward-Exp-Lvl");
            private String key;
            ExpType(String langKey){
               key=langKey;
            }
            public String getLangKey(){
                return key;
            }
        }
        UUID uuid;
        int count;
        ExpType type;
        boolean delete;
        public Experience(JSONObject json){
            if(json.containsKey("UUID")){
                uuid = UUID.fromString((String)json.get("UUID"));
            }else uuid = UUID.randomUUID();
            if(json.containsKey("Type")){
                type = ExpType.valueOf((String)json.get("Type"));
            }else type = ExpType.ORB;
            if(json.containsKey("Count")){
                count = JsonUtils.getInt(json, "Count");
            }else count = type==ExpType.LVL?30:1396;//1396=lvl 30
            if(json.containsKey("Delete")){
                delete = (boolean)json.get("Delete");
            }else delete = false;
        }
        public Experience(int count, ExpType type, boolean delete){
            this.uuid = UUID.randomUUID();
            this.count = count;
            this.type = type==null?ExpType.ORB:type;
            this.delete = delete;
        }
        public JSONObject toJson(){
            JSONObject j = new JSONObject();
            j.put("UUID", uuid.toString());
            j.put("Count", count);
            if(type!=null){
                j.put("Type", type.name());
            }
            if(delete)j.put("Delete", true);
            return j;
        }
        public String toLore(UniPlayer player){
            if(count<=0)return "";
            return "§f -§b " + count + " §a" +player.getChallengeMessage(type.getLangKey())+(delete?"\n"+player.getChallengeMessage("Challenge-Item-Required-Others-Exp-Delete"):"");
        }
        public UUID getUuid(){
            return uuid;
        }
        @Override
        public String toString(){
            return "Experience{UUID:"+uuid.toString()+",Count:"+count+",Delete:"+delete+",Type:"+(type==null?ExpType.ORB:type.name())+"}";
        }
        public int getCount(){
            return count;
        }
        public ExpType getType(){
            return type;
        }
        public boolean doDelete(){
            return delete;
        }
        public void give(Player p){
            if(p==null)return;
            if(type==ExpType.LVL)p.giveExpLevels(count);
            else p.giveExp(count);
        }
        public void remove(Player p, int remove){
            if(p==null)return;
            if(remove==-1)remove=count;
            if(type==ExpType.LVL){
                int newCount = p.getLevel()-remove;
                p.setLevel(Math.max(newCount, 0));
            }else{
                int newCount = p.getTotalExperience()-remove;
                p.setTotalExperience(Math.max(newCount, 0));
            }
        }
        public int get(Player p){
            if(p==null)return 0;
            if(type==ExpType.LVL)return p.getLevel();
            else return p.getTotalExperience();
        }
    }

    public static class IsLevel{
        UUID uuid;
        int count;
        public IsLevel(JSONObject json){
            if(json.containsKey("UUID")){
                uuid = UUID.fromString((String)json.get("UUID"));
            }else uuid = UUID.randomUUID();
            if(json.containsKey("Count")){
                count = JsonUtils.getInt(json, "Count");
            }else count = 1000;
        }
        public IsLevel(int count){
            this.uuid = UUID.randomUUID();
            this.count = count;
        }
        public JSONObject toJson(){
            JSONObject j = new JSONObject();
            j.put("UUID", uuid.toString());
            j.put("Count", count);
            return j;
        }
        public String toLore(UniPlayer player){
            if(count<=0)return "";
            return " §7- §b"+count+"§a "+player.getChallengeMessage("Challenge-Item-Required-Others-Islvl");
        }
        public UUID getUuid(){
            return uuid;
        }
        @Override
        public String toString(){
            return "IsLevel{UUID:"+uuid.toString()+",Count:"+count+"}";
        }
        public int getCount(){
            return count;
        }
    }

}
