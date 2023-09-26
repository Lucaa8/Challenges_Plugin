package ch.luca008.ChallengesManager.Challenges;

import ch.luca008.Challenges;
import ch.luca008.ChallengesManager.Categories.Category;
import ch.luca008.ChallengesManager.IslandStorage.Storage;
import ch.luca008.ChallengesManager.Manager;
import ch.luca008.ChallengesManager.Required.*;
import ch.luca008.ChallengesManager.Reward;
import ch.luca008.SpigotApi.Item.ItemBuilder;
import ch.luca008.UniPlayer;
import ch.luca008.Utils.*;
import net.md_5.bungee.api.chat.TextComponent;
import org.json.simple.JSONObject;
import com.bgsoftware.superiorskyblock.api.island.Island;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Challenge {

    public enum ChallengeType{
        INVENTORY, ISLAND, STAT, OTHER;
    }

    private final UUID uuid;
    private UUID categoryUuid;
    private String name;
    private String description;
    private final ChallengeType type;
    private boolean active;
    private SbItem icon;
    private int page;
    private int slot;
    private int redoneLimit;
    private Required required;
    private Reward reward;

    private long lastEdited;
    public boolean asChanged = false;

    public Challenge(UUID uuid, UUID categoryUuid, String name, String description, ChallengeType type, boolean active, SbItem icon, int page, int slot, int redoneLimit, long lastEdited, Required required, Reward reward) {
        this.uuid = uuid;
        this.categoryUuid = categoryUuid;
        this.name = name;
        this.description = description;
        this.type = type;
        this.active = active;
        this.icon = icon;
        this.page = page;
        this.slot = slot;
        this.redoneLimit = redoneLimit;//mettre -1 si aucune limite
        this.lastEdited = lastEdited==-1?System.currentTimeMillis():lastEdited;
        this.required = required;
        this.reward = reward;
    }

    public void flash(JSONObject json){
        if(getCategory()!=null){
            Challenges.getManager().getIndex().removeChallenge(getCategory(), this);
        }
        new ChallengeBuilder(json).flash(this);
        if(getCategory()!=null){
            Challenges.getManager().getIndex().addChallenge(getCategory(), this);
        }
        lastEdited = System.currentTimeMillis();
        asChanged = true;
        if(getRequired() instanceof Stats){
            for(Storage s : Challenges.getManager().getLoadedStorages()){
                s.syncStats();
            }
        }
    }

    //SETTERS/GETTERS
    public UUID getUuid() {
        return uuid;
    }

    public void setCategoryUuid(UUID u){
        this.categoryUuid = u;
    }
    public UUID getCategoryUuid(){
        return categoryUuid;
    }
    public Category getCategory(){
        if(getCategoryUuid()==null)return null;
        return Challenges.getManager().retrieveCategoryByUUID(getCategoryUuid()).orElse(null);
    }

    public void setName(String name){
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }

    public ChallengeType getType() {
        return type;
    }

    public void setActive(boolean active){
        this.active = active;
    }
    public boolean isActive() {
        return active;
    }

    public void setIcon(SbItem icon) {
        this.icon = icon;
    }
    public SbItem getIcon() {
        return icon;
    }

    public void setPage(int page) {
        this.page = page;
    }
    public int getPage() {
        return page;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }
    public int getSlot() {
        return slot;
    }

    public void setRedoneLimit(int redoneLimit) {
        this.redoneLimit = redoneLimit;
    }
    public int getRedoneLimit() {
        return redoneLimit;
    }

    public long getLastEdited(){
        return lastEdited;
    }

    public void setRequired(Required required) {
        this.required = required;
    }
    public Required getRequired() {
        return required;
    }

    public void setReward(Reward reward) {
        this.reward = reward;
    }
    public Reward getReward() {
        return reward;
    }
    //SETTERS/GETTERS

    //MISC
    public SbItem toItem(Storage storage, UniPlayer player){
        ItemBuilder ib = new ItemBuilder();
        if(storage.isUnlocked(this).size()>0||(!isActive()&&!Perms.Permission.CHALLENGE_ADMIN_BYPASS.hasPermission(player))){
            ib = ItemUtils.getUnavailableItem(false,isActive(),player);
        }else{
            Storage.ChallengeStorage chaStr = storage.getStorage(getUuid());
            ib.setMaterial(getIcon().getMaterial());
            if(getIcon().getCustomData()>0){
                ib.setCustomData(getIcon().getCustomData());
            }
            if(getIcon().hasMeta()){
                ib.setMeta(getIcon().getMeta());
            }
            String lore = (!isActive()?"§4(Inactif)§r\n":"")+Challenges.getGlobalConfig().getChallengesDescriptionColor()+getDescription();
            if(!lore.isEmpty())lore+="\n";//si pas de description, alors pas besoin de 2 retours à la ligne
            lore+="\n";
            int completed = chaStr.getTotalCompleted();
            if(completed>=getRedoneLimit()&&getRedoneLimit()!=-1){
                lore+=player.getChallengeMessage("Challenge-Item-Limit-Threshold",Map.entry("{0}",getRedoneLimit()+""))+"\n";
            }else{
                Required required = getRequired();
                boolean customRequired = required instanceof Items&&((Items)required).containsItemMeta();
                Reward._Reward reward = getReward()!=null?getReward().getReward(chaStr.getTotalCompleted()==0):null;
                boolean customReward = reward!=null&&reward.containsItemMeta();
                lore+=player.getChallengeMessage("Challenge-Item-Required")+(customRequired?"§4*§r":"")+(getRequired()==null?"\n"+player.getChallengeMessage("Challenge-Item-Required-None")+"\n":getRequired().toLore(chaStr, player))+"\n";
                lore+=player.getChallengeMessage("Challenge-Item-Reward")+(customReward?"§4*§r":"")+(reward==null?"\n"+player.getChallengeMessage("Challenge-Item-Reward-None")+"\n":reward.toLore(player))+"\n";
                if(getRequired()!=null&&getRequired().getType()==Required.RequiredType.Stats){
                    if(equals(storage.getActiveStatChallenge().orElse(null))){
                        lore+=player.getChallengeMessage("Challenge-Item-Stat-Cancel")+"\n\n";
                    }
                }
                if(customRequired||customReward){
                    lore+="§4*§r"+player.getChallengeMessage("Challenge-Item-Special")+"\n\n";
                }
            }
            lore+=player.getChallengeMessage("Challenge-Item-Completed",Map.entry("{0}",chaStr.getTotalCompleted()+""),Map.entry("{1}",chaStr.getDailyCompleted()+""));
            if(completed<getRedoneLimit()&&getRedoneLimit()==1){
                lore+="\n"+player.getChallengeMessage("Challenge-Item-One-Time-Challenge");
            }
            ib.setLore(StringUtils.asLore(lore));
        }
        ib.setName(Challenges.getGlobalConfig().getChallengeNameColorItem()+getName());
        ib.setUid("Cha_"+getUuid());
        return new SbItem(ib.createItem());
    }

    public static TextComponent getMissingChallenges(List<Challenge> challengeList, String baseMsg, UniPlayer player){
        TextComponent txt = new TextComponent(TextComponent.fromLegacyText(baseMsg));
        txt.addExtra(StringUtils.missingChallengesAsString(challengeList,player));
        return txt;
    }

    public CompletableResult complete(UniPlayer p){
        boolean canBypass = Perms.Permission.CHALLENGE_ADMIN_BYPASS.hasPermission(p);
        if(!Challenges.Main.areChallengesEnabled()&&!canBypass)return null;
        Object msg = "";
        boolean complete = false;
        Category cat = getCategory();
        if(cat!=null&&(cat.isActive()||canBypass)){
            if(isActive()||canBypass){
                Optional<Island> is = p.getIsland();
                if(is.isPresent()){
                    Manager m = Challenges.getManager();
                    Island i = is.get();
                    Storage storage = p.getIslandStorage().orElseGet(() -> m.loadStorage(i.getUniqueId()));
                    List<Challenge> requiredCat = storage.isUnlocked(getCategory());
                    if(requiredCat.isEmpty()){
                        List<Challenge> requiredCha = storage.isUnlocked(this);
                        if(requiredCha.isEmpty()){
                            if(storage.getStorage(getUuid()).getTotalCompleted()<getRedoneLimit()||getRedoneLimit()<0){
                                if(getRequired()!=null){
                                    return getRequired().complete(this, p);
                                }else {
                                    complete = true;
                                }
                            }else msg = p.getMessage("Challenge-Completion-Limit-Threshold", Map.entry("{0}",getRedoneLimit()+""));
                        }else msg = getMissingChallenges(requiredCha, p.getMessage("Challenge-Completion-Challenge-Not-Unlocked", Map.entry("{0}",getName())), p);
                    }else msg = getMissingChallenges(requiredCat, p.getMessage("Challenge-Completion-Category-Not-Unlocked", Map.entry("{0}",getCategory().getName())), p);
                }else msg = p.getMessage("Challenges-No-Island");
            }else msg = p.getMessage("Challenge-Completion-Challenge-Inactive");
        }else msg = p.getMessage("Challenge-Completion-Category-Inactive");

        Object finalMsg = msg;
        boolean finalComplete = complete;
        final Challenge c = this;
        return new CompletableResult() {
            @Override
            public boolean isCompleted() {
                return finalComplete;
            }

            @Override
            public boolean hasProgressed() {
                return false;
            }

            @Override
            public Object getMessage() {
                return finalMsg;
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

    public void reward(UniPlayer player){
        player.getIslandStorage().ifPresent(s->{
            player.getPlayer().ifPresent(p->{
                if(getReward()!=null){
                    Reward._Reward reward = getReward().getReward(s.getStorage(getUuid()).getTotalCompleted() == 1);
                    if(reward!=null){
                        reward.reward(p, this);
                    }
                }
            });
        });
    }

    public void reset(UUID island){
        Optional<Storage> optStr = Challenges.getManager().retrieveStorageByUUID(island);
        if(optStr.isPresent()){
            Storage isStr = optStr.get();
            Storage.ChallengeStorage chStr = isStr.getStorage(getUuid());
            chStr.setDailyCompleted(0);
            chStr.setTotalCompleted(0);
            chStr.setCompletable(null);
            if(getRequired()!=null&&getRequired().getType()==Required.RequiredType.Stats){
                Optional<Challenge> started = isStr.getActiveStatChallenge();
                if(started.isPresent()&&started.get().equals(this)){
                    isStr.setStatisticChallengeActive(null);
                }
            }
        }
    }

    public JSONObject toJson(){
        JSONObject j = new JSONObject();
        j.put("uuid", uuid.toString());
        if(categoryUuid!=null)j.put("category", categoryUuid.toString());
        j.put("name", name);
        if(description!=null&&!description.isEmpty())j.put("description", description);
        j.put("type", type.name());
        j.put("active", active);
        j.put("icon", icon.toJson());
        j.put("page", page);
        j.put("slot", slot);
        if(redoneLimit>=0)j.put("redoneLimit", redoneLimit);
        if(lastEdited!=-1)j.put("lastEdited", lastEdited);
        if(required!=null)j.put("required", new RequiredLoader().unload(required));
        if(reward!=null)j.put("reward", reward.toJson());
        return j;
    }

    public void unload(){
        try{
            if(asChanged){
                File f = new File(Manager.challengesBaseDirectory, getUuid().toString()+".json");
                if(!f.exists()){
                    f.createNewFile();
                }
                Files.write(Paths.get(f.toURI()), JsonUtils.prettyJson(toJson()).getBytes(StandardCharsets.UTF_8));
            }
        }catch (IOException e) {
            System.err.println("Can't unload challenge with uuid " + getUuid() + ".");
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "Challenge{" +
                "uuid='" + uuid + '\'' +
                ", categoryUuid=" + categoryUuid +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", type=" + type +
                ", active=" + active +
                ", icon=" + icon.toString() +
                ", page=" + page +
                ", slot=" + slot +
                ", redoneLimit=" + redoneLimit +
                ", required=" + required +
                ", reward=" + reward +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Challenge)) return false;
        Challenge challenge = (Challenge) o;
        return uuid.equals(challenge.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
