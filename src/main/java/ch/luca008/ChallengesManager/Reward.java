package ch.luca008.ChallengesManager;

import ch.luca008.Challenges;
import ch.luca008.ChallengesManager.Challenges.Challenge;
import ch.luca008.ChallengesManager.Required.Items;
import ch.luca008.ChallengesManager.Required.Others;
import ch.luca008.Items.ItemBuilder;
import ch.luca008.UniPlayer;
import ch.luca008.Utils.ItemUtils;
import ch.luca008.Utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Reward {

    _Reward first;
    _Reward next;

    public Reward(JSONObject j){
        if(j.containsKey("First")){
            JSONObject jFirst = (JSONObject) j.get("First");
            if(!jFirst.isEmpty()){
                first = new _Reward(jFirst);
                first.broadcastMessage = true;
            }
        }
        if(j.containsKey("Next")){
            JSONObject jNext = (JSONObject) j.get("Next");
            if(!jNext.isEmpty()){
                next = new _Reward(jNext);
            }
        }
    }
    public Reward(_Reward first, _Reward next) {
        this.first = first;
        this.first.broadcastMessage = true;
        this.next = next;
    }

    public _Reward getReward(boolean first){
        if(first)return this.first;
        else return next;
    }
    public void setReward(boolean first, _Reward reward){
        if(first)this.first=reward;
        else this.next = reward;
    }

    public JSONObject toJson(){
        JSONObject j = new JSONObject();
        if(first!=null){
            j.put("First", first.toJson());
        }
        if(next!=null){
            j.put("Next", next.toJson());
        }
        return j;
    }
    @Override
    public String toString(){
        return "Rewards{"+(first!=null?"First:"+first:"")+(next!=null?(first!=null?",":"")+"Next:"+next:"")+"}";
    }

    public static class _Reward{
        List<Items.Item> items;
        List<String> commands;
        boolean broadcastMessage = false;
        String message;
        Others.Money money;
        Others.Experience exp;

        public _Reward(JSONObject json){
            if(json.containsKey("Items")){
                items = new ArrayList<>();
                JSONArray jarr = (JSONArray) json.get("Items");
                for (Object o : jarr) {
                    items.add(new Items.Item((JSONObject) o));
                }
                Items.sort(items);
            }
            if(json.containsKey("Commands")){
                commands = new ArrayList<>();
                JSONArray jarr = (JSONArray) json.get("Commands");
                for (Object o : jarr) {
                    commands.add((String) o);
                }
            }
            if(json.containsKey("Message")){
                message = (String) json.get("Message");
            }
            if(json.containsKey("Money")){
                money = new Others.Money((JSONObject) json.get("Money"));
            }
            if(json.containsKey("Experience")){
                exp = new Others.Experience((JSONObject) json.get("Experience"));
            }
        }
        public _Reward(List<Items.Item> items, List<String> commands, String message, Others.Money money, Others.Experience exp) {
            this.items = items;
            Items.sort(this.items);
            this.commands = commands;
            this.message = message;
            this.money = money;
            this.exp = exp;
        }

        @Override
        public String toString(){
            String i = "";
            if(items!=null&&!items.isEmpty()){
                i+="Items:{";
                for (Items.Item item : items) {
                    i+=item+",";
                }
                i = i.substring(0,i.length()-1) + "}";
            }
            String c = "";
            if(commands!=null&&!commands.isEmpty()){
                c+=(i.isEmpty()?"":",")+"Commands:{";
                for (String command : commands) {
                    c+="{"+command+"},";
                }
                c = c.substring(0,c.length()-1) + "}";
            }
            String msg = "";
            if(message!=null&&!message.isEmpty()){
                msg+=(!i.isEmpty()||!c.isEmpty()?",":"")+"Message:{"+message+"}";
            }
            String m = "";
            if(money!=null){
                m+=(!i.isEmpty()||!c.isEmpty()||!msg.isEmpty()?",":"")+money;
            }
            String e = "";
            if(exp!=null){
                e+=(!i.isEmpty()||!c.isEmpty()||!msg.isEmpty()||!m.isEmpty()?",":"")+exp;
            }
            return "Reward{"+i+c+msg+m+e+"}";
        }
        public JSONObject toJson(){
            JSONObject j = new JSONObject();
            if(items!=null&&!items.isEmpty()){
                JSONArray jarr = new JSONArray();
                for (Items.Item item : items) {
                    jarr.add(item.toJson());
                }
                j.put("Items", jarr);
            }
            if(commands!=null&&!commands.isEmpty()){
                JSONArray jarr = new JSONArray();
                for (String command : commands) {
                    jarr.add(command);
                }
                j.put("Commands", jarr);
            }
            if(message!=null&&!message.isEmpty()){
                j.put("Message", message);
            }
            if(money!=null){
                j.put("Money", money.toJson());
            }
            if(exp!=null){
                j.put("Experience", exp.toJson());
            }
            return j;
        }
        public String toLore(UniPlayer player){
            String lore="";
            if(items!=null&&!items.isEmpty()){
                for(Items.Item i : items){
                    lore+="\n §f- §b"+i.getCount()+" §a"+ StringUtils.enumName(i.getItem().getMaterial()) + (i.getIncrement()>0?" "+player.getChallengeMessage("Challenge-Item-Reward-Luck",Map.entry("{0}",""+i.getIncrement())):"");
                }
            }
            if(money!=null&&money.getCount()>0){
                lore+="\n §f- §b"+money.getCount()+" §a"+ Challenges.getGlobalConfig().getCurrencyName();
            }
            if(exp!=null&&exp.getCount()>0){
                lore+="\n §f- §b"+exp.getCount()+" §a"+player.getChallengeMessage(exp.getType().getLangKey());
            }
            return lore+"\n";
        }

        public boolean containsItemMeta(){
            if(items==null)return false;
            for(Items.Item i : items){
                if(i.getItem()!=null&&i.getItem().hasItemMeta())return true;
            }
            return false;
        }

        public void reward(Player player, Challenge challenge){
            String challengeName = challenge.getName();
            if(getItems()!=null&&!getItems().isEmpty()){
                giveItems(player);
            }
            if(getCommands()!=null&&!getCommands().isEmpty()){
                getCommands().forEach(c-> Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), c.replace("{C}", challengeName).replace("{P}", player.getName())));
            }
            if(getMessage()!=null&&!getMessage().isEmpty()){
                UniPlayer unip = Challenges.getManager().retrieveUniPlayerByUUID(player.getUniqueId());
                if(isBroadcastMessage()){
                    if(Challenges.Main.areChallengesEnabled()&&challenge.isActive()){
                        if(challenge.getCategory()!=null&&challenge.getCategory().isActive()){//conditions pour ne pas envoyer de bmsg lorsqu'un joueur bypass termine le challenge
                            for(Player p : Bukkit.getOnlinePlayers()){
                                UniPlayer up = Challenges.getManager().retrieveUniPlayerByUUID(p.getUniqueId());
                                up.sendMessage(getMessage(), Map.entry("{C}",challengeName), Map.entry("{P}", player.getName()));
                            }
                        }
                    }
                }
                //if(isBroadcastMessage())Bukkit.broadcastMessage(unip.getMessage(getMessage(), Map.entry("{C}",challengeName), Map.entry("{P}", player.getName()))); //Langue de joueur pour le bmsg, faux..
                else unip.sendMessage(getMessage(), Map.entry("{C}",challengeName), Map.entry("{P}", player.getName()));
            }
            if(getMoney()!=null){
                getMoney().give(player);
            }
            if(getExp()!=null){
                getExp().give(player);
            }
        }

        public List<Items.Item> getItems() {
            return items;
        }
        private void giveItems(Player player){
            Random r = new Random();
            getItems().forEach(i->{
                if(i.getIncrement()>0&&i.getIncrement()<=100){
                    int luck = r.nextInt(100);
                    if(luck<i.getIncrement()){
                        i.getItem().giveOrDrop(player, i.getCount());
                    }
                }else{
                    i.getItem().giveOrDrop(player, i.getCount());
                }
            });
        }

        public List<String> getCommands() {
            return commands;
        }

        public boolean isBroadcastMessage(){
            return broadcastMessage;
        }
        public String getMessage() {
            return message;
        }

        public Others.Money getMoney() {
            return money;
        }

        public Others.Experience getExp() {
            return exp;
        }


    }
}
