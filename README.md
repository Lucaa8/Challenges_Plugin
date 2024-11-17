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

## Functionalities
It’s time to present the plugin's various features. Starting with a brief description:
Challenges are displayed and managed through an inventory interface that any player with an island can open. Once opened, the inventory shows categories, which group together challenges with similar themes—like all challenges related to construction. Players can explore each category to find numerous challenges, all presented in an interactive inventory.

A category or challenge can be locked behind the completion of another challenge. For example, the challenge "Carrot I" must be completed before attempting "Carrot II." Similarly, completing the final challenge in the "Carrot" category may unlock the "Potatoes" category.

When a player completes a challenge, they receive a reward. All players on the island can contribute to the progress of a challenge. For instance, Player1 donates 15 carrots, and Player2 donates 17 carrots to the same challenge; the progress will then show as 32/x carrots donated.

### Main page
### Categories
### Challenges
#### Challenge - INVENTORY
#### Challenge - ISLAND
#### Challenge - STATISTIC
Active challenge ?
#### Challenge - OTHER
### Challenges Rewards
#### Command
#### Message
#### Money and Experience
#### Items
### Staff interactions
/cadmin

