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

### Main page
### Categories
### Challenges
#### Challenge - INVENTORY
#### Challenge - ISLAND
#### Challenge - STATISTIC
Active challenge ?
#### Challenge - OTHER
### Challenges Rewards
### Staff interactions
/cadmin

