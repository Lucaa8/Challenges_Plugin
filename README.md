# Challenges

Every person who has played the "Skyblock" game mode in Minecraft knows how important and fun challenges are. Unfortunately, in most Skyblock plugins, the challenges are very limited to just a few tasks, such as "Have x carrots in your inventory" or "Place x stone blocks on your island." The same goes for the range of rewards, which is often very restricted. The goal of this "Challenges" plugin is to bring a real sense of challenge to players by introducing new, never-before-seen tasks while maintaining a simple and efficient management system for both players and staff. A variety of unique rewards can be offered to players to motivate them to complete these increasingly crazy tasks.

## Copyright


## Dependencies
- Spigot/Paper 1.20
- [SpigotApi](https://github.com/Lucaa8/SpigotApi)
- [SuperiorSkyblock2](https://www.spigotmc.org/resources/%E2%9A%A1%EF%B8%8F-superiorskyblock2-%E2%9A%A1%EF%B8%8F-the-best-core-on-market-%E2%9A%A1%EF%B8%8F-1-21-1-support.87411/)
- [VaultAPI](https://github.com/MilkBowl/VaultAPI)

## Design and conception
The idea behind this plugin didn't come from me. In fact, it originated when the Skyblock staff of the Uni-Craft server wanted to launch a new version of the game mode to bring something fresh for the players. That's when the idea of more advanced challenges came to mind. A large part of the theory, design, and concept was envisioned by the former Skyblock staff, including Auristelle, BlackT8, LRDB, and SuperTV.

## Implementation
I handled the implementation myself, based on what was planned by the staff team.

## The application
The plugin is fully configured using JSON. It is highly advanced, allowing the creation of an infinite number of objects and challenges organized into different categories. However, with hundreds of lines of JSON, human error can happen quickly. That’s why a GUI application was developed (in C#) in addition to the plugin (in Java) to simplify editing and better visualize challenges when staff members need to make changes. The staff member starts an editing session from the Minecraft server and can then log into the C# application using a temporary token. The application connects to the Minecraft server via a socket, and any changes are made live.

- Find the application's repository at the following URL: [Challenges_App](https://github.com/Lucaa8/Challenges_App)

## Functionalities
It’s time to present the plugin's various features. Starting with a brief description:
Challenges are displayed and managed through an inventory interface that any player with an island can open. Once opened, the inventory shows categories, which group together challenges with similar themes—like all challenges related to construction. Players can explore each category to find numerous challenges, all presented in an interactive inventory.

A category or challenge can be locked behind the completion of another challenge. For example, the challenge "Carrot I" must be completed before attempting "Carrot II." Similarly, completing the final challenge in the "Carrot" category may unlock the "Potatoes" category.

When a player completes a challenge, they receive a reward. All players on the island can contribute to the progress of a challenge. For instance, Player1 donates 15 carrots, and Player2 donates 17 carrots to the same challenge; the progress will then show as 32/x carrots donated.

### Main page
Players can open this interface using the **/challenge** or **/c** command once they are on their island. Each item represents a category. The arrows at the bottom allow players to navigate between pages.
| ![main-menu](https://github.com/user-attachments/assets/e7e97e49-963d-4c62-9e07-a1dc1061fe78) | 
|:--:| 
| *The challenges main menu* |

### Categories
A category contains several challenges within it. A category can be either locked or unlocked. If an item icon appears, the category is unlocked. If a red glass pane appears, the category is locked. By clicking on it, players can see which challenge(s) need to be completed to unlock it.
| ![cat-unlocked](https://github.com/user-attachments/assets/ec1e1e47-ce11-476b-bfad-e92d26c8140f) |
|:--:| 
| *An unlocked category* |

| ![cat-locked1](https://github.com/user-attachments/assets/fc4df572-31a3-4368-abe4-9010b7964e3f) | ![cat-locked2](https://github.com/user-attachments/assets/091c89a5-5657-4ed1-9ed3-520c75865ec6) |
|:--:|:--:| 
| *A locked category* | *Missing challenges to unlock* |

| ![cat-locked3](https://github.com/user-attachments/assets/117074ac-b549-48e9-b533-6a1d423c5045) |
|:--:| 
| *For convenience, the player can try to complete the challenge by clicking on the text* |

### Challenges
Let’s now get to the heart of the matter: the challenges. Each category contains a series of challenges. Players can open a category simply by clicking on the icons, as described in the previous section. Just like categories, a challenge can be either locked or unlocked. Similarly, locked challenges are displayed with a red glass pane, and players can click on them to see the missing requirements.
| ![image](https://github.com/user-attachments/assets/8a98eb03-4b57-485c-9798-e9fd1ba83b1c) |
|:--:| 
| *Challenges of the Carrot category* |

A challenge always includes the following:
- **Name**: The title of the challenge.
- **Description**: A brief explanation of the challenge.
- **Requirements**: A list of tasks or items needed to complete it.
- **Rewards**: A list of what players receive upon completion.
- **Completion Count**: How many times the challenge has been completed in total.
- **Daily Completion Count**: How many times it has been completed today.

Some challenges can be completed multiple times, while others cannot. For repeatable challenges, the requirements become progressively more demanding with each completion.
| ![challenge-onetime](https://github.com/user-attachments/assets/d68814f1-2d60-489b-99ac-4b9f193803cc) | ![challenge-infinite](https://github.com/user-attachments/assets/f81f2291-39ff-40e6-bfc8-ef6a694dcb47) |
|:--:|:--:| 
| *This challenge has been completed one time and cannot be completed anymore* | *This challenge is infinite with augmenting requirements each (daily) completion* |

![challenge_inv_infinite](https://github.com/user-attachments/assets/7c49ce30-a9a8-4cd8-9578-51516a9d38af)
|:--:| 
| *The player completed the same challenge multiple times, thus augmenting the carrot needed each time* |

The daily completion count is reset for each island every day at 4:00 AM. As a result, the completion requirements are reset to their simpler initial levels.

Finally, challenges are divided into four different types. See the list below:

#### Challenge - INVENTORY
Inventory challenges are quite straightforward. They require players to have a certain number of specific items in their inventory at the time of completing the challenge. As a player progresses in an inventory challenge, the items used are removed from their inventory.
| ![challenge_inv_progress](https://github.com/user-attachments/assets/ae90c200-e33e-4e97-8068-9e5638d39f96) | ![challenge_inv_completed](https://github.com/user-attachments/assets/2954bd3e-dda3-4a26-a509-bd2277dab385) |
|:--:|:--:| 
| *The player progressed a challenge* | *The player completed a challenge and received the reward* |

Some challenges may require very specific items, such as dyed leather armor, potions, or enchanted books. Since it would take too long to list all the item metadata directly on the challenge, players can right-click on the icon to view a detailed list of the required items and special rewards.
![challenge_inv_details](https://github.com/user-attachments/assets/267aa589-fbd6-4602-8683-d930f6146383)
|:--:| 
| *By right clicking the challenge, more details about requirements are displayed* |

#### Challenge - ISLAND
"Island" challenges are those that require players to have a certain number of blocks/entities placed on their island. These challenges cannot be "progressive." When completing the challenge, the player must have all the required blocks/entities placed around them within a certain radius. Either the requirements are met and the challenge is completed, or they are not, and the challenge is rejected. The blocks/entities counted in the challenge are not removed from the island once the challenge is completed. The goal of these challenges is to encourage players to build monuments and works of art on their islands.
![challenge_is_completed](https://github.com/user-attachments/assets/17efd31c-6955-4ff0-804e-40a51516acf3)
|:--:| 
| *Fails because of the missing squid, then adding it and complete the challenge* |

#### Challenge - STATISTIC
"Statistical" challenges are challenges that track the actions of players. For example, "Jump 500 times," "Craft 256 torches," "Kill 50 skeletons," etc. They work a bit differently from other challenges. These challenges must be activated by a player from the island, and only one challenge can be active at a time. The challenge will remain active until it is either completed or canceled by a player from the island. All players on the island contribute to completing the challenge. This means that if the goal is to kill 50 skeletons and Player1 kills 27 and Player2 kills 23, the challenge can be completed.
| ![image](https://github.com/user-attachments/assets/bd7920d9-c6ac-4ea4-bc7f-5f05c31c9d86) |
|:--:| 
| *An inactive statistic challenge* |

| ![challenge_stat_alreadyactive](https://github.com/user-attachments/assets/180b1d04-4fc2-4069-9891-22dccf49b648) |
|:--:| 
| *A player tried to enable a second statistic challenge before ending the first one* |

| ![challenge_stat_completed](https://github.com/user-attachments/assets/9af317a0-771a-46dc-be3a-ee53ca8430b9) |
|:--:| 
| *A player complete the current active statistic challenge by mining the last missing block* |

#### Challenge - OTHER
And finally, three additional types of challenges.
- **Island Level**: Every time players place blocks on their island, their island level increases. The challenge requires the island to be level x.
- **Money (Uniz)**: The player attempting to complete the challenge must have x amount of money in their bank account.
- **Experience**: The player attempting to complete the challenge must have x levels of experience.

For **Money** and **Experience**, the amount can be deducted from the user's account when completing the challenge, just like inventory challenges, or not. It is up to the administrator to choose this in the challenge configuration.

A challenge can require any combination of the three types, but it is not mandatory. A challenge could very well ask for 1,000$ without requiring 10 levels of experience.

| ![challenge_other_completed](https://github.com/user-attachments/assets/106199aa-8f29-4c6e-9f52-70fcb8f24790) |
|:--:| 
| *Adding a block to increase the island level then completing the challenge* |
Note that in this challenge, neither the money (10,000$) nor the experience levels (10) were deducted.

### Challenges Rewards
The rewards section deserves a whole paragraph in this file because it is very detailed. Each challenge is completed at least once per island. After that, some challenges can be re-completed. This is why the reward system is divided into two sub-sections: "First-reward" and "Next-reward." This way, it is possible to offer significant rewards with unique content for the first completion of a challenge on an island, and then continue rewarding the efforts of "farming" on subsequent completions, but perhaps with a smaller or completely different reward. Of course, a challenge that can only be completed once is not affected by the "Next-reward" and can be ignored.

In a second step, each reward is divided into 5 sub-rewards, which may or may not be included in the final reward. The 5 sub-rewards apply to both the "First-reward" and the "Next-reward" and can be completely different. For example, the "First-reward" may include money and items, while the "Next-reward" may consist of experience levels. The strength of this system lies in the administrator's flexibility with the rewards, allowing them to easily manage which items are available in what quantities at any point during the progression of an island.

Finally, a sub-reward can have a 'proc' chance. If you feel that giving a villager spawn egg for each completion is too powerful, but still want to allow players to obtain some through farming, Luck rewards are the solution!

| ![challenge_rew_first](https://github.com/user-attachments/assets/dd737a1b-6f86-4f39-a223-f4cef977f86d) | ![challenge_rew_next](https://github.com/user-attachments/assets/dd91e8e3-5599-49ac-9855-bf66275b8c66) |
|:--:|:--:| 
| *The First Reward (0 total completions)* | *The Next reward (1+ total completions)* |

Let's dive in the sub-rewards now!

#### Command
The 'Command' reward is not visible to players and does not appear in the \[Reward\] section of a challenge. It is a special reward designed to execute more complex server-side actions. A good example would be unlocking the 'Nether' island when a specific Nether-related challenge is completed. In [SuperiorSkyblock2](https://www.spigotmc.org/resources/%E2%9A%A1%EF%B8%8F-superiorskyblock2-%E2%9A%A1%EF%B8%8F-the-best-core-on-market-%E2%9A%A1%EF%B8%8F-1-21-1-support.87411/), there is a command (´/cad island acs nether <player> true´) that admins can run to unlock the Nether world for any island. The only thing needed to execute this command automatically is to identify the player who completed the challenge. To achieve this, placeholders have been set up. The placeholder '{P}' is replaced with the player’s name when the command is run by the server.

| ![image](https://github.com/user-attachments/assets/4eff5beb-6fde-43fd-b17a-fef1bae12295) |
|:--:| 
| *Helheim is unlocking the Nether Island when completed* |


The challenge's first reward would be configured like this. So from now, when a player complete this challenge, it will unlock the Nether world for his whole island.
```json
{
  "First": {
    "Commands": [
      "cad island acs nether {P} true"
    ]
  }
}
```

#### Message
The "Message" is not really a reward for the player but is part of the system. When a player completes a challenge for the first time, a message is broadcasted to the entire server. For all subsequent completions, a simple personal message is sent to the player as "feedback" confirming that they have successfully completed the challenge.

Since the message is almost always identical, it is stored in global configuration files, and the key is simply referenced in the challenge configuration. This way, if the admin wants to change the message sent upon challenge completion, it is updated for all challenges. At the same time, there is the option to set a custom message for a specific challenge if needed.

| ![image](https://github.com/user-attachments/assets/4b82c7f9-50d9-47f0-ae31-6666625e6753) |
|:--:| 
| *Example when a player complete a challenge for the 2nd time* |

#### Money and Experience
This reward just gives money or experience orbs/levels to the player which complete the challenge.
- **Experience ORB**: Just a sub-division of a full experience level
- **Experience LEVEL**: Well, a full green bar of experience on top of the player's hotbar.

Granting full experience levels isn’t always 'fair' for all players. For example, if you award 3 experience levels, a player already at level 54 will receive 1,228 experience points (enough to go from level 54 to 57), while a player at level 4 will only get 51 experience points (enough to go from level 4 to 7). Keep this in mind and use this option carefully.

#### Items
Grants customisable items to the player upon completion. Items can range from basic ones, like simple dirt or stone blocks, to very complex and unique ones, such as swords with normally impossible enchantments or leather armor in unusual colors!

| ![challenge_rew1](https://github.com/user-attachments/assets/2e2a09fa-59b1-470d-91b9-99677dfeb1da) | ![challenge_rew2](https://github.com/user-attachments/assets/7a012d6d-b3a7-4df1-8892-cd618dbe4505) |
|:--:|:--:| 
| *A custom reward for the Acacia Challenge* | *The given item has a custom meta* |

Here are the metadata currently supported by the plugin (These are handled by my [SpigotApi Meta](https://github.com/Lucaa8/SpigotApi/tree/master/src/main/java/ch/luca008/SpigotApi/Item/Meta) library);
- **Written Book** Add title, author signature and text inside it
- **Leather Armor** Customize the colour
- **Potion** Normal, Splash and Area potions are included. Add as many effects on them, choose the duration and level of each effect
- **Skull** Gives own player skulls, [Minecraft-Heads](https://minecraft-heads.com/) customizable heads like the Earth, letters, etc... or any user's head!
- **TrimArmor** Customize skins on armor
- **TropicalFish** Set an infinite combo of colour and patterns for fish eggs

### Lang
The entire plugin is currently available in English and French, with separate language files stored in the plugin's configuration. Adding a new language is simple: just copy `EN.json`, rename the file to something like `FR.json` or `EN.json` and place it in the `Lang` folder. Then, replace all the JSON values with the appropriate translations for your language. That’s it! Players can then select any supported language directly in the game.

| ![challenge_lang](https://github.com/user-attachments/assets/4e2cbe26-169c-4253-9c55-ff3a52a0f166) ]
|:--:| 
| *Changing the lang in game with /c lang <lang>|reset* |

### Staff interactions
Staffs have special tools to moderate challenges and islands in game, from disabling challenges to resetting an island's challenges completions (i.e. in case of cheating). Find all the permissions at the end of this file.

#### Commands
- **/cadmin <enable>|<disable>|<status>** Can enable or disable the whole plugin for players. For the status argument, check the illustration below.
- **/cadmin reload <lang>|<config>** Can reload the `config.json` file or the `Lang` folder. Which allows minor changes to be affected directly without restarting the server.
- **/cadmin cat|cha <name>** Sends the player a list of information about this category or challenge. See the illustration below.
- **/cadmin toggle cat|cha <name>** Can enable or disable a specific category or challenge for player. Maybe if you're working on it or a bug has been found.
- **/cadmin island <player>** Opens the main menu challenge inventory from the player's island but as admin mode. Check the illustration below.
- **/cadmin editor <new>|<kill \[reason\]>** Generates a token to the player for the Csharp application. The player is able to edit any category/challenge inside the app

#### Illustrations (Mostly in French, sorry!)

| ![challenges_admin_statut](https://github.com/user-attachments/assets/3e2d0276-0677-4d6b-88fd-2c5f654d4117) |
|:--:| 
| *The /cadmin status command gives general information about the plugin's state* |

| ![image](https://github.com/user-attachments/assets/50c9d806-a4b8-4c61-a5d5-9449ca2cc596) |
|:--:| 
| *The /cadmin cha\|cat <name> gives general information about a category or challenge* |

| ![cadmin_island_global](https://github.com/user-attachments/assets/ab5a6ec7-42f8-438e-adfd-eee956ff578a) |
|:--:| 
| *The /cadmin island \<player\> let the admin see general information about the island, enable/disable world islands or reset the whole challenges for this island* |

| ![cadmin_island_reset](https://github.com/user-attachments/assets/d7067728-0a37-405d-90ed-c31fc272a086) | ![image](https://github.com/user-attachments/assets/d47d0e53-3802-4d95-9d12-e736ba65eecf) |
|:--:|:--:| 
| *It also allows an admin to reset a whole category by right clicking on it* | *It shows that only 1 over 7 challenges are now unlocked* |

| ![cadmin_island_edit](https://github.com/user-attachments/assets/a7fc835c-7c7c-424b-87fa-e54045c4b52c) | ![image](https://github.com/user-attachments/assets/dc66b1fc-dce8-4cd3-8c59-dbcd5de140b5) |
|:--:|:--:| 
| *An admin can decide the number of times any challenge has been done in total or daily* | *Final result when, as a player, i'm running the /c command* |

#### Editor
Inside the editor, an admin would be able to fully edit ANY category or challenge, from the name of the challenge to the enchantment on one of the item given as reward. Please check the `README.md` file from this [repository](https://github.com/Lucaa8/Challenges_App/blob/main/README.md) if you're interested on learning how the application is working.

## Commands (Players)
- Open the main menu `/c` or `/challenge`
- Open a category directly without going through the main menu `/c <category name>`
- Try to complete a challenge `/c c <challenge name>` or `/c complete <challenge name>`
- Cancel an active statistical challenge `/c cancel CONFIRM`
- Change the lang `/c lang <lang>|reset`

## Permissions
### Player related
All of them: challenge.use.*
- **challenge.use.command** Enables the player to use the `/c` or `/challenge` command
- **challenge.use.complete** Enables the player to use the `/c complete <challenge name>` command
- **challenge.use.cancel** Enables the player to wheel click on an active statistical challenge or use the `/c cancel CONFIRM` command to drop an active challenge
### Staff related
All of them: **challenge.admin.*** \
All of the toggle: **challenge.admin.toggle.*** \
All of the reload: **challenge.admin.reload.*** \
All of the editor: **challenge.admin.editor.***
- **challenge.admin.use** Enables the player to use the `/cadmin` command which display help
- **challenge.admin.toggle.all** Enables the player to toggle state of the plugin (command `/cadmin enable|disable`)
- **challenge.admin.toggle.cat** Enables the player to toggle a category on/off with the command `/cadmin toggle cat <category>`
- **challenge.admin.toggle.cha** Enables the player to toggle a challenge on/off with the command `/cadmin toggle cha <category>`
- **challenge.admin.reload.config** Enables the player to reload the global configuration file (`config.json`)
- **challenge.admin.reload.lang** Enables the player to reload the folder `Lang` (to update a translation or adding a new lang)
- **challenge.admin.editor.island** Enables the player to edit every island's challenges. From total/daily completion to full reset.
- **challenge.admin.editor.app** Enables the player to use the command `/cadmin editor new|kill` to open or kill a session with the Csharp application
- **challenge.admin.promptable** Allows the player to open a sign interface and write text in it to send it to the server. Useful for the island editor.
- **challenge.admin.bypass** Enables the player to still access any challenge even if the plugin is disabled or a category/challenge is disabled
