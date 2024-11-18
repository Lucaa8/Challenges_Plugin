# Challenges

Every person who has played the "Skyblock" game mode in Minecraft knows how important and fun challenges are. Unfortunately, in most Skyblock plugins, the challenges are very limited to just a few tasks, such as "Have x carrots in your inventory" or "Place x stone blocks on your island." The same goes for the range of rewards, which is often very restricted. The goal of this "Challenges" plugin is to bring a real sense of challenge to players by introducing new, never-before-seen tasks while maintaining a simple and efficient management system for both players and staff. A variety of unique rewards can be offered to players to motivate them to complete these increasingly crazy tasks.

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

## Commands

## Permissions

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
#### Command
#### Message
#### Money and Experience
#### Items
### Staff interactions
/cadmin

