package ch.luca008.Events;

import ch.luca008.Challenges;
import ch.luca008.ChallengesManager.Categories.Category;
import ch.luca008.ChallengesManager.Challenges.Challenge;
import ch.luca008.ChallengesManager.IslandStorage.Storage;
import ch.luca008.ChallengesManager.Manager;
import ch.luca008.ChallengesManager.Required.CompletableResult;
import ch.luca008.ChallengesManager.Required.Required;
import ch.luca008.ChallengesManager.Required.Stats;
import ch.luca008.Commands.ChallengeAdminCommand;
import ch.luca008.SpigotApi.Api.NBTTagApi;
import ch.luca008.SpigotApi.SpigotApi;
import ch.luca008.UniPlayer;
import ch.luca008.Utils.Broadcast;
import ch.luca008.Utils.Perms;
import ch.luca008.Utils.SkyblockUtils;
import com.bgsoftware.superiorskyblock.api.events.IslandDisbandEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import java.io.File;
import java.util.Optional;
import java.util.UUID;

public class ChallengeRelatedEvents implements Listener {

    //SMALL FIX FOR CHALLENGES STATS WITH 'TIME_SINCE_DEATH'. when an island starts a new stat challenge, current values of all players
    //are recorded and saved into island's storage data. But when a player die his current stat value is reset to 0. So current(0)-initial may be negative
    //so we need to reset the initial value for the involved player

    @EventHandler
    public void FIX_onPlayerDeathUpdateStat(EntityDeathEvent e){
        if(e.getEntityType()== EntityType.PLAYER){
            Player p = (Player)e.getEntity();
            Bukkit.getScheduler().runTaskLater(Challenges.Main, ()->{
                try{
                    Stats.updateStat(p, Statistic.TIME_SINCE_DEATH);
                }catch(Exception ex){
                    System.err.println("Challenges: can't update death stat of " + p.getName() + "'s island.");
                }
            }, 3L);
        }
    }

    //END OF FIX

    @EventHandler
    public void challengeCompleted(ChallengeEvent e){
        Optional<Player> p = e.getPlayer().getPlayer();
        if(p.isPresent()){
            Player player = p.get();
            if(e.isCompleted()){
                e.execute();
                Challenges.getManager().getStats().getStatistic(e.getChallenge()).addCompleted();
            }else{
                Object msg = e.getMessage();
                if(msg!=null){
                    if(msg instanceof String){
                        String txt = msg.toString();
                        if(!txt.isEmpty())player.sendMessage(txt);
                    }
                    else if(msg instanceof Broadcast){
                        Broadcast b = (Broadcast)msg;
                        e.getPlayer().getIsland().ifPresent(island -> SkyblockUtils.getPlayersOnIsland(island).
                                forEach(ispl -> Challenges.getManager().retrieveUniPlayerByUUID(ispl.getUniqueId()).sendMessage(b.getKeyMessage(), b.getReplacements())));
                    }
                    else if(msg instanceof TextComponent){
                        player.spigot().sendMessage((TextComponent) msg);
                    }else if(msg instanceof BaseComponent[]){
                        player.spigot().sendMessage((BaseComponent[]) msg);
                    }
                }
            }
        }
    }

    @EventHandler
    public void challengesClick(InventoryClickEvent e){
        if(!(e.getWhoClicked() instanceof Player))return;
        Player p = (Player)e.getWhoClicked();
        NBTTagApi.NBTItem invInfo = getInventoryInfo(e.getClickedInventory(), 49);
        if(invInfo!=null&&(invInfo.hasTag("Challenges")||invInfo.hasTag("Category"))){
            String access = canAcces(p);
            if(access!=null){
                p.sendMessage(access);
                p.closeInventory();
                return;
            }
            e.setCancelled(true);
            NBTTagApi.NBTItem itemInfo = getItemInfo(e.getCurrentItem());
            if(itemInfo!=null){
                Manager m = Challenges.getManager();
                UniPlayer player = Challenges.getManager().retrieveUniPlayerByUUID(p.getUniqueId());
                Category parent = invInfo.hasTag("Category")?m.retrieveCategoryByUUID(UUID.fromString(invInfo.getString("Category"))).orElse(null):null;
                if(itemInfo.hasTag("UUID")){
                    String tag = itemInfo.getString("UUID");
                    if(tag.equals("Door")){
                        if(parent!=null){
                            player.openMenuInventory(parent.getPage());
                        }else{
                            p.closeInventory();
                        }
                    }
                    if(tag.contains("_")){
                        String[] tags = tag.split("_");
                        if(tags[1].contains("Page")&&tags.length==3){
                            String type = tags[0];
                            int page = Integer.parseInt(tags[2]);
                            if(type.equals("Main")){
                                player.openMenuInventory(page);
                            }else{
                                if(parent!=null){
                                    player.openCategory(parent, page);
                                }
                            }
                        }
                        else if(tags[0].equals("Cat")){
                            Optional<Category> optC = m.retrieveCategoryByUUID(UUID.fromString(tags[1]));
                            if(optC.isPresent()){
                                Category c = optC.get();
                                p.performCommand("c "+c.getName());
                            }
                        }
                        else if(tags[0].equals("Cha")){
                            m.retrieveChallengeByUUID(UUID.fromString(tags[1])).ifPresent(c->{
                                if(e.isLeftClick()){
                                    CompletableResult result = c.complete(player);
                                    if(result!=null){
                                        Bukkit.getServer().getPluginManager().callEvent(new ChallengeEvent(result));
                                        if(parent!=null&&(result.hasProgressed())||result.isCompleted()){
                                            player.openCategory(parent, invInfo.hasTag("Page")?Integer.parseInt(invInfo.getTag("Page").toString()):0);
                                        }
                                    }
                                }
                                else{
                                    Optional<Storage> optStr = player.getIslandStorage();
                                    if(optStr.isPresent()){
                                        Storage str = optStr.get();
                                        if(c.getRequired()!=null&&c.getRequired().getType()==Required.RequiredType.Stats&&e.getClick()==ClickType.MIDDLE){
                                            Optional<Challenge> optStarted = str.getActiveStatChallenge();
                                            if(optStarted.isPresent()&&optStarted.get().equals(c)){
                                                if(p.performCommand("c cancel CONFIRM")){
                                                    player.openCategory(parent, invInfo.hasTag("Page")?Integer.parseInt(invInfo.getTag("Page").toString()):0);
                                                }
                                            }
                                        }else{
                                            if(c.isActive()&&str.isUnlocked(c).isEmpty()){
                                                if(c.getRedoneLimit()==-1||str.getStorage(c.getUuid()).getTotalCompleted()<c.getRedoneLimit()){
                                                    Inventory inv = Challenges.getInventoryManager().asInventory(player,c,0,0);
                                                    if(inv!=null)p.openInventory(inv);
                                                }
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void sampleInventoryClick(InventoryClickEvent e){
        if(!(e.getWhoClicked() instanceof Player))return;
        Player p = (Player)e.getWhoClicked();
        NBTTagApi.NBTItem invInfo = getInventoryInfo(e.getClickedInventory(), 22);
        if(invInfo!=null&&invInfo.hasTag("Challenge")){
            e.setCancelled(true);
            String access = canAcces(p);
            if(access!=null){
                p.sendMessage(access);
                p.closeInventory();
                return;
            }
            NBTTagApi.NBTItem itemInfo = getItemInfo(e.getCurrentItem());
            if(itemInfo!=null){
                Manager m = Challenges.getManager();
                Optional<Challenge> c = m.retrieveChallengeByUUID(UUID.fromString(invInfo.getString("Challenge")));
                int currReq = Integer.parseInt(invInfo.getString("ReqPage"));
                int currRew = Integer.parseInt(invInfo.getString("RewPage"));
                if(c.isPresent()){
                    UniPlayer player = m.retrieveUniPlayerByUUID(p.getUniqueId());
                    Challenge challenge = c.get();
                    if(itemInfo.hasTag("UUID")){
                        String uuid = itemInfo.getString("UUID");
                        if(uuid.equals("Door")){
                            Category parent = challenge.getCategory();
                            if(parent!=null){
                                player.openCategory(parent, challenge.getPage());
                            }else{
                                player.sendMessage("Challenges-Something-Went-Wrong");
                            }
                        }
                        else if(uuid.equals("Next")||uuid.equals("Prev")){
                            int reqMax = Integer.parseInt(invInfo.getString("ReqMax"));
                            int rewMax = Integer.parseInt(invInfo.getString("RewMax"));
                            if(e.isLeftClick()){
                                if(uuid.equals("Next")){
                                    if(currReq+1<reqMax)currReq++;
                                }
                                else{
                                    if(currReq-1>=0)currReq--;
                                }
                            }else{
                                if(uuid.equals("Next")){
                                    if(currRew+1<rewMax)currRew++;
                                }
                                else {
                                    if(currRew-1>=0)currRew--;
                                }
                            }
                            Inventory inv = Challenges.getInventoryManager().asInventory(player, challenge, currReq, currRew);
                            if(inv!=null)p.openInventory(inv);
                        }
                    }
                    else{
                        int slot = e.getSlot();
                        if(slot>=10&&slot<=16){
                            for(Recipe r : Bukkit.getRecipesFor(e.getCurrentItem())){
                                if(r instanceof ShapedRecipe){
                                    p.openInventory(Challenges.getInventoryManager().asInventory(player, challenge.getUuid(), currReq, currRew, (ShapedRecipe) r));
                                    break;
                                }
                            }
                        }
                    }
                }else{
                    e.getWhoClicked().closeInventory();
                }
            }
        }
    }

    @EventHandler
    public void wbClick(InventoryClickEvent e){
        if(!(e.getWhoClicked() instanceof Player))return;
        Player p = (Player)e.getWhoClicked();
        NBTTagApi.NBTItem invInfo = getInventoryInfo(e.getClickedInventory(), 13);
        if(invInfo!=null&&invInfo.hasTag("Req")&&invInfo.hasTag("Rew")){
            e.setCancelled(true);
            String access = canAcces(p);
            if(access!=null){
                p.sendMessage(access);
                p.closeInventory();
                return;
            }
            NBTTagApi.NBTItem itemInfo = getItemInfo(e.getCurrentItem());
            if(itemInfo!=null&&itemInfo.hasTag("UUID")){
                UniPlayer player = Challenges.getManager().retrieveUniPlayerByUUID(p.getUniqueId());
                Challenges.getManager().retrieveChallengeByUUID(UUID.fromString(itemInfo.getString("UUID").split("_")[1])).ifPresent(c->{
                    p.openInventory(Challenges.getInventoryManager().asInventory(player, c, Integer.parseInt(invInfo.getString("Req")), Integer.parseInt(invInfo.getString("Rew"))));
                });
            }
        }
    }

    /**
     * Check first if player's storage is loaded then if the challenges are disabled and if player has permission bypass
     * @param p the player who tries to interact an inventory
     * @return null if the player can interact or a string message with an error (in the player's lang) to explain why he can't interact rn
     */
    private String canAcces(Player p){
        UniPlayer unip = Challenges.getManager().retrieveUniPlayerByUUID(p.getUniqueId());
        if(unip.getIslandStorage().isEmpty()){
            return unip.getMessage("Challenges-Something-Went-Wrong");
        }
        if(!Challenges.Main.areChallengesEnabled()&&!Perms.Permission.CHALLENGE_ADMIN_BYPASS.hasPermission(p)){
            return unip.getMessage("Challenges-Disabled");
        }
        return null;
    }

    private NBTTagApi.NBTItem getInventoryInfo(Inventory inv, int itemToCheck){
        if(inv!=null){
            if(inv.getSize()>itemToCheck){
                ItemStack identifier = inv.getItem(itemToCheck);
                if(identifier!=null&&identifier.getType()!=Material.AIR){
                    return SpigotApi.getNBTTagApi().getNBT(identifier);
                }
            }
        }
        return null;
    }

    private NBTTagApi.NBTItem getItemInfo(ItemStack item){
        if(item!=null&&item.getType()!=Material.AIR){
            return SpigotApi.getNBTTagApi().getNBT(item);
        }
        return null;
    }

    @EventHandler(priority = EventPriority.LOW) //low = s'execute avant UniPlayerManager#quitEvent(PlayerQuitEvent). Car on a encore besoin de l'UniPlayer ici.
    public void playerQuit(PlayerQuitEvent e){
        UniPlayer p = Challenges.getManager().retrieveUniPlayerByUUID(e.getPlayer().getUniqueId());
        Optional<Island> optIs = p.getIsland();
        if(optIs.isPresent()&&p.getIslandStorage().isPresent()){
            if(SkyblockUtils.getOnlinePlayersOnIsland(optIs.get()).size()==1){
                if(!ChallengeAdminCommand.hasIslandSession(optIs.get().getUniqueId())){
                    Challenges.getManager().unloadStorage(optIs.get().getUniqueId());
                }
            }
        }
    }

    @EventHandler
    public void islandDeleteFile(IslandDisbandEvent e){
        UUID is = e.getIsland().getUniqueId();
        //unload si chargée
        Challenges.getManager().unloadStorage(is);
        File f = new File(Manager.islandsBaseDirectory, is.toString()+".json");
        if(f.exists()){
            if(f.delete()){
                System.out.println("Deleted island storage with uuid " + is + ". Cause: The island got deleted.");
            }else{
                System.out.println("Failed to delete island storage with uuid " + is + ".");
            }
        }
    }
}