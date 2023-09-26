package ch.luca008.Admin;

import ch.luca008.Challenges;
import ch.luca008.ChallengesManager.Categories.Category;
import ch.luca008.ChallengesManager.Challenges.Challenge;
import ch.luca008.ChallengesManager.IslandStorage.Storage;
import ch.luca008.Commands.ChallengeAdminCommand;
import ch.luca008.SpigotApi.Api.NBTTagApi;
import ch.luca008.SpigotApi.SpigotApi;
import ch.luca008.Utils.PromptPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Optional;
import java.util.UUID;

public class IslandInventoryEvents implements Listener {

    @EventHandler
    public void closeInventory(InventoryCloseEvent e){
        if(e.getPlayer() instanceof Player){
            Player p = (Player)e.getPlayer();
            InventoryView inv = p.getOpenInventory();
            if(inv.getTitle().startsWith("§cÎle de §9")){
                IslandInventory session = ChallengeAdminCommand.getIslandSession(p);
                if(session!=null&&session.doDestroyOnClose()){
                    ChallengeAdminCommand.deleteIslandSession(p.getUniqueId());
                }
            }
        }
    }

    @EventHandler
    public void inventoryClick(InventoryClickEvent e){
        if(e.getWhoClicked() instanceof Player){
            if(e.getClickedInventory()!=null&&e.getView().getTitle().startsWith("§cÎle de §9")){
                e.setCancelled(true);
                Player admin = (Player)e.getWhoClicked();
                Inventory inv = e.getClickedInventory();
                IslandInventory session = ChallengeAdminCommand.getIslandSession(admin);
                if(session!=null){
                    ItemStack item = e.getCurrentItem();
                    if(item!=null&&item.getType()!= Material.AIR){
                        ItemMeta im = item.getItemMeta();
                        if(im!=null&&im.hasDisplayName()){
                            String name = im.getDisplayName();
                            if(name.equals("§aSuivant")){
                                session.displayNext();
                            }
                            else if(name.equals("§aPrécédent")){
                                session.displayPrev();
                            }
                            else if(name.equals("§cRetour")){
                                session.display(null,-1);
                            }
                            else if(item.getType()==Material.OAK_SIGN){
                                NBTTagApi.NBTItem nbt = SpigotApi.getNBTTagApi().getNBT(item);
                                if(nbt.hasTag("UUID")&&nbt.getString("UUID").equals("MainManager")){
                                    if(e.isRightClick()){
                                        for(Category c : Challenges.getManager().getCategories()){
                                            c.reset(session.getIsland());
                                        }
                                        session.getStorage().giveAccess(Storage.AccessType.NETHER, false);
                                        session.getStorage().giveAccess(Storage.AccessType.END, false);
                                        admin.sendMessage("§aToutes les catégories ainsi que les accès aux mondes ont été réinitialisés sur l'île de §b" + session.getTarget().getName()+"§a!");
                                        session.display();
                                        Bukkit.getConsoleSender().sendMessage("§c!!§4Player "+admin.getName()+" identified with UNIQUEID "+admin.getUniqueId()+" reset ALL the challenges of the island identified with UUID "+session.getIsland()+"§c!!");
                                    }else {
                                        session.setDestroyOnClose(false);
                                        PromptPlayer.promptPlayer(admin, (isCancelled, asMultipleLines, asSingleLine) -> {
                                            if(!isCancelled){
                                                for(String l : asMultipleLines){
                                                    if(l.contains(":")){
                                                        String[] args = l.split(":");
                                                        if(args.length>1){
                                                            try{
                                                                session.getStorage().giveAccess(Storage.AccessType.valueOf(args[0].toUpperCase()), args[1].equalsIgnoreCase("oui"));
                                                            }catch(Exception ignored){}
                                                        }
                                                    }
                                                }
                                            }
                                            session.display();
                                        }, "Nether:","End:","Oui | Non");
                                        Bukkit.getScheduler().runTaskLaterAsynchronously(Challenges.Main, ()->session.setDestroyOnClose(true), 10L);
                                    }
                                }
                            }
                            else{
                                NBTTagApi.NBTItem nbt = SpigotApi.getNBTTagApi().getNBT(item);
                                if(nbt.hasTag("UUID")){
                                    String tag = nbt.getString("UUID");
                                    if(tag.startsWith("Category_")){
                                        Optional<Category> category = Challenges.getManager().retrieveCategoryByUUID(UUID.fromString(tag.split("_")[1]));
                                        if(category.isPresent()){
                                            if(e.isRightClick()){
                                                category.get().reset(session.getIsland());
                                                Bukkit.getConsoleSender().sendMessage("§c!!Player "+admin.getName()+" identified with UNIQUEID "+admin.getUniqueId()+" reset all the challenges of category §4"+category.get().getName()+" §cof the island identified with UUID "+session.getIsland()+"§c!!");
                                                admin.sendMessage("§aLa catégorie §b" + category.get().getName() + " §aa bien été réinitialisée sur l'île de §b" + session.getTarget().getName()+"§a!");
                                                session.display();
                                            }else{
                                                category.ifPresent(value -> session.display(value, 1));
                                            }
                                        }
                                    }
                                    else if(tag.startsWith("Challenge_")){
                                        Optional<Challenge> challenge = Challenges.getManager().retrieveChallengeByUUID(UUID.fromString(tag.split("_")[1]));
                                        if(challenge.isPresent()){
                                            if(e.isRightClick()){
                                                challenge.get().reset(session.getIsland());
                                                Bukkit.getConsoleSender().sendMessage("§c!!Player "+admin.getName()+" identified with UNIQUEID "+admin.getUniqueId()+" reset the challenge §4"+challenge.get().getName()+" §cof the island identified with UUID "+session.getIsland()+"§c!!");
                                                session.display();
                                            }else{
                                                session.setDestroyOnClose(false);
                                                PromptPlayer.promptPlayer(admin, (cancelled,lines,line) -> {
                                                    if(!cancelled){
                                                        for(String l : lines){
                                                            if(l.contains(":")){
                                                                String[] args = l.split(":");
                                                                if(args.length>1){
                                                                    int nb = -1;
                                                                    try{
                                                                        nb = Integer.parseInt(args[1]);
                                                                    }catch(NumberFormatException ignored){}
                                                                    if(nb!=-1){
                                                                        if(args[0].equalsIgnoreCase("Total")){
                                                                            session.getStorage().getStorage(challenge.get().getUuid()).setTotalCompleted(nb);
                                                                        }
                                                                        else if(args[0].equalsIgnoreCase("Daily")){
                                                                            session.getStorage().getStorage(challenge.get().getUuid()).setDailyCompleted(nb);
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                    session.display();
                                                }, "Total:","Daily:");
                                                Bukkit.getScheduler().runTaskLaterAsynchronously(Challenges.Main, ()->session.setDestroyOnClose(true), 10L);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
