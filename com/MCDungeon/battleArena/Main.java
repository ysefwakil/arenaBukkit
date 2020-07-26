package com.MCDungeon.battleArena;

import java.io.BufferedReader;
import java.io.File;

import java.util.*;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_16_R1.CraftServer;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import Files.StartingInv;

public class Main extends JavaPlugin implements Listener {

	private StartingInv startinginv;
	public Inventory inv;
	Deque<Player> deque;
	Deque<Player> inGame;
	boolean gamePlaying = false;
	JavaPlugin plugin;
	int time;
	int taskID;
	boolean someoneDied;
	boolean arenaLeft;
	boolean someoneLeft = false;

	Location spawn1;
	Location spawn2;

	Location SPAWN1;
	Location SPAWN2;

	FileConfiguration config;

	public void saveArenas(String arenaName, Location spawn1, Location spawn2) {
		this.getConfig().set("data." + arenaName.toLowerCase() + ".spawn1", spawn1);
		this.getConfig().set("data." + arenaName.toLowerCase() + ".spawn2", spawn2);
		this.saveConfig();

	}

	public void loadConfig() {
		getConfig().options().copyDefaults(true);
		saveConfig();
	}

	@Override
	public void onEnable() {

		if(!getDataFolder().exists())
		{
			getDataFolder().mkdir();
		}
		
		plugin = this;
		getServer().getPluginManager().registerEvents(this, this);
		loadConfig();
		SPAWN1 = new Location(Bukkit.getWorld("world"), -85.601, 170, 253.70, -180, 0);
		SPAWN2 = new Location(Bukkit.getWorld("world"), -85.601, 170, 227.30, 0, 0);
		createInv();

		this.startinginv = new StartingInv(this);
		startinginv.properInventory();
		// When server starts up
		deque = new LinkedList<Player>();
		inGame = new LinkedList<Player>();


		 
		BukkitScheduler scheduler = getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				// Do something

				if (deque.size() != 0) {
					if (deque.size() >= 2 && gamePlaying == false) {
						time = 90;
						gamePlaying = true;
						Player player1 = deque.pop();
						Player player2 = deque.pop();

						inGame.add(player1);
						inGame.add(player2);

						// Save Player positions
						Location savedPlayerPosition1 = player1.getLocation();
						Location savedPlayerPosition2 = player2.getLocation();

						// save Player Hearts
						double player1Health = player1.getHealth();
						double player2Health = player2.getHealth();

						// save Food levels
						int player1Food = player1.getFoodLevel();
						int player2Food = player2.getFoodLevel();

						// save XP
						int player1EXP = player1.getLevel();
						int player2EXP = player2.getLevel();

						// Set Start Location
//						player1.teleport(new Location(Bukkit.getWorld("world"), -85.601, 170, 253.70, -180, 0));
//						player2.teleport(new Location(Bukkit.getWorld("world"), -85.601, 170, 227.30, 0, 0));

						player1.teleport(SPAWN1);
						player2.teleport(SPAWN2);

						// GIVE ITEMS / GIVE FOOD / GIVE HEALTH
						ItemStack[] armor = { new ItemStack(Material.DIAMOND_BOOTS),
								new ItemStack(Material.DIAMOND_LEGGINGS), new ItemStack(Material.DIAMOND_CHESTPLATE),
								new ItemStack(Material.DIAMOND_HELMET) };

						ItemStack item = new ItemStack(Material.BOW, 1);
						item.addEnchantment(Enchantment.ARROW_FIRE, 1);
						item.addEnchantment(Enchantment.ARROW_DAMAGE, 3);
						
						ItemStack dSword = new ItemStack(Material.DIAMOND_SWORD, 1);
						dSword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
						
						ItemStack[] items = { new ItemStack(dSword), new ItemStack(item),
								new ItemStack(Material.COOKED_BEEF, 16), new ItemStack(Material.ARROW, 32) };

//            			Bukkit.getServer().dispatchCommand(getServer().getConsoleSender(),"heal " + player1.getName());
//            			Bukkit.getServer().dispatchCommand(getServer().getConsoleSender(),"heal " + player2.getName());

						player1.setFoodLevel(20);

						player1.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
						player2.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);

						player1.setHealth(20.0);

						player2.setFoodLevel(20);
						player2.setHealth(20.0);

						// Adds items to inventory
						player1.getInventory().addItem(items);
						player1.getInventory().setArmorContents(armor);
						player2.getInventory().addItem(items);
						player2.getInventory().setArmorContents(armor);

						// Timer
						int count = 30;
						startTimer(player1, player2, savedPlayerPosition1, savedPlayerPosition2, player1Health,
								player2Health, player1Food, player2Food, player1EXP, player2EXP);

					}

				}

			}
		}, 0L, 1L);

	}

	@Override
	public void onDisable() {
		// On Shut down and reload
		startinginv.save();

	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (label.equalsIgnoreCase("Arena")) {

			// check if sender is sending the instance of player
			if (sender instanceof Player) {

				boolean inQueue = false;

				Player player = (Player) sender;
				if (player.hasPermission("arena.join")) {

					if (args.length <= 0) {
						player.openInventory(inv);
						return true;
					}

					if (args[0].equalsIgnoreCase("leave") || args[0].equalsIgnoreCase("l")) {
						boolean inGame = false;
						for (Player p : deque) {
							if (p.getName() == player.getName()) {

								player.sendMessage(ChatColor.BLUE + "" + "You have left the arena queue");
								inQueue = false;
								deque.remove(p);
								inGame = true;
							}

						}
						if (inGame == false) {
							// leaving arena and queue
							player.sendMessage(ChatColor.BLUE + "You are not in queue");
							boolean notInArena = true;
							for (Player p : deque) {
								if (p.getName() == player.getName()) {
									deque.remove(p);
									notInArena = false;
									inQueue = false;

								}

							}
						}

						return true;

					}

					else if (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("j")) {
						
						inQueue = false;
						for (Player p : deque) {
							if (p.getName() == player.getName()) {
								inQueue = true;

							}
						}
						if (inQueue == true) {
							player.sendMessage(ChatColor.BLUE + "" + "You are already in queue!!");

						} else {
							// Check if inventory is empty & in survival
							if ((isPlayerinSurvival(player) == true) && isInvEmpty(player) == true) {
								// Save player spawn point

								player.sendMessage(ChatColor.BLUE + "" + "Joining arena queue...");
								deque.add(player);

								// waiting for someone to queue
								getPositioninQueue(player);
							}
							return true;
						}
					} else if (args[0].equalsIgnoreCase("spawn1") && player.hasPermission("arena.create")) {
						spawn1 = player.getLocation();
						player.sendMessage(ChatColor.DARK_GRAY + "" + "Spawn1 has been set!");

					}

					else if (args[0].equalsIgnoreCase("spawn2") && player.hasPermission("arena.create")) {
						spawn2 = player.getLocation();
						player.sendMessage(ChatColor.DARK_GRAY + "" + "Spawn2 has been set!");

					}

					else if (args[0].equalsIgnoreCase("create")
							|| args[0].equalsIgnoreCase("c") && (player.hasPermission("arena.create"))) {
						if (args.length < 2 || args.length > 2) {
							player.sendMessage(ChatColor.RED + "" + "Specify Arena name /arena create (name)");
						} else {
							// Create new Arena
							// CHECK IF NAME EXISTS
							boolean arenaPresent = false;
							getLogger().info("before");

							File f = new File(getDataFolder(), "config.yml");
							if (f.length() <= 0) {
								// file has data
							} else {

								for (String key : getConfig().getConfigurationSection("data").getKeys(false)) {
									if (key.equalsIgnoreCase(args[1])) {

										arenaPresent = true;
									}
								}
							}

							if (arenaPresent == true) {
								player.sendMessage(ChatColor.RED + "" + "Arena name already in use!");
								getLogger().info("WTF");

								return false;
							} else {
								if (spawn1 == null || spawn2 == null) {
									player.sendMessage(ChatColor.RED + ""
											+ "Make sure to define starting spawns with /arena spawn1 && /arena spawn2");
									return false;
								}

								String arenaName = args[1];

								saveArenas(arenaName, spawn1, spawn2);
								player.sendMessage(ChatColor.GREEN + "" + "Arena has been successfully created!");

								this.getConfig().set("data." + arenaName.toLowerCase() + ".spawn1", spawn1);
								this.getConfig().set("data." + arenaName.toLowerCase() + ".spawn2", spawn2);

								this.saveConfig();
								spawn1 = null;
								spawn2 = null;
							}

							return true;

						}

					} else if (args[0].equalsIgnoreCase("set") && player.hasPermission("arena.create")) {

						// Check if arena name is present
						if (args.length != 2) {
							player.sendMessage("Please enter which arena you would like to set, /arena set (name)");
						}

						else {
							File configFile = new File(plugin.getServer().getWorldContainer().getAbsolutePath()
									+ "/plugins/<Your Plugin Name>/config.yml"); // First we will load the file.
							FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
							boolean isPresent = false;
							// get spawns from yml and set it
							for (String key : getConfig().getConfigurationSection("data").getKeys(false)) {
								if (args[1].equalsIgnoreCase(key)) {

									Location spawn1 = getConfig().getLocation("data." + key + ".spawn1");
									Location spawn2 = getConfig().getLocation("data." + key + ".spawn2");

									SPAWN1 = spawn1;
									SPAWN2 = spawn2;

									player.sendMessage(
											ChatColor.DARK_GREEN + " Arena " + args[1] + " successfuly set!");
									isPresent = true;
								}

							}
							if (isPresent == false) {
								player.sendMessage(ChatColor.RED + "" + "Arena name not present!");
							}

						}

					}

					else if (args[0].equalsIgnoreCase("list") && player.hasPermission("arena.create")) {
						// Display all arenas
						player.sendMessage(ChatColor.YELLOW + "The following arenas have been created:");
						for (String key : getConfig().getConfigurationSection("data").getKeys(false)) {

							player.sendMessage(ChatColor.DARK_GREEN + key);

						}

					} else if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("h")) {
						helpGuide(player);
					}

					else {
						helpGuide(player);
						return true;
					}

				} else {
					player.sendMessage(ChatColor.RED + "NO PERMS");

				}

			} else {
				// Console
				sender.sendMessage("Hello console");
				return true;
			}
		}

		return false;
	}

	public boolean isInvEmpty(Player player) {
		Inventory inv = player.getInventory();
		for (int n = 0; n < inv.getSize(); n++) {
			ItemStack item = inv.getItem(n);
			if (item != null) {
				player.sendMessage(ChatColor.RED + "" + "EMPTY INVENTORY BEFORE JOINING ARENA!!");
				return false;
			}

		}
		return true;
	}

	public boolean isPlayerinSurvival(Player player) {
		if (player.getGameMode() == GameMode.SURVIVAL) {
			return true;
		} else {
			player.sendMessage(ChatColor.RED + "" + "Please Switch to Survival before joining Arena");
			return false;

		}
	}

	public void getPositioninQueue(Player player) {
		// get player position in queue
		Iterator<Player> itr = deque.iterator();
		int count = 0;
		int totaldeque = deque.size();
		int position = 0;
		int totalPlayers = 0;
		if (totaldeque <= 2) {
			totaldeque = 2;
		}
		while (itr.hasNext()) {
			if (player.getName() == itr.next().getName()) {
				position = count;

			}
			totaldeque++;
			count++;
			totalPlayers++;
		}

		player.sendMessage(ChatColor.YELLOW + "" + "You have now joined arena position: " + ChatColor.GREEN + "" + count
				+ "/" + totalPlayers);

	}

	public void startTimer(Player player1, Player player2, Location savedPlayerPosition1, Location savedPlayerPosition2,
			Double player1Health, Double player2Health, int player1Food, int player2Food, int player1EXP,
			int player2EXP) {

		someoneDied = false;
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		taskID = scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {

				if (time <= 0) {
					if (someoneDied == false && someoneLeft == false) {
						player1.sendMessage(ChatColor.RED + "Time is up!");
						player2.sendMessage(ChatColor.RED + "Time is up!");

						Bukkit.broadcastMessage(ChatColor.GOLD + player2.getName() + ChatColor.YELLOW
								+ " went even with " + ChatColor.GOLD + player1.getName() + ChatColor.YELLOW
								+ " in the Battle Arena!");

					}

					if (someoneLeft == true) {
						Bukkit.broadcastMessage(ChatColor.GOLD + player2.getName() + ChatColor.YELLOW
								+ " went even with " + ChatColor.GOLD + player1.getName() + ChatColor.YELLOW
								+ " in the Battle Arena!");
					}

					else {
						player1.sendMessage(ChatColor.GREEN + "" + "Game Over!");
						player2.sendMessage(ChatColor.GREEN + "" + "Game Over!");

					}

					player1.getInventory().clear();
					player2.getInventory().clear();

					// sets all of players info
					player1.setHealth(player1Health);
					player2.setHealth(player2Health);
					player1.setFoodLevel(player1Food);
					player2.setFoodLevel(player2Food);
					player1.setLevel(player1EXP);
					player2.setLevel(player2EXP);
					player1.teleport(savedPlayerPosition1);
					player2.teleport(savedPlayerPosition2);

					inGame.pop();
					inGame.pop();

					someoneLeft = false;
					gamePlaying = false;
					someoneDied = false;

					stopTimer();
					return;
				}
				if (time % 15 == 0) {
					int minutes = time / 60;
					int seconds = time % 60;

					if (minutes >= 1) {
						player1.sendMessage(ChatColor.GOLD + "Time remaining: " + ChatColor.YELLOW + minutes
								+ ChatColor.GOLD + " minute and " + ChatColor.YELLOW + seconds + ChatColor.GOLD
								+ " seconds remaining");
						player2.sendMessage(ChatColor.GOLD + "Time remaining: " + ChatColor.YELLOW + minutes
								+ ChatColor.GOLD + " minute and " + ChatColor.YELLOW + seconds + ChatColor.GOLD
								+ " seconds remaining");

					} else {
						player1.sendMessage(ChatColor.GOLD + "Time remaining: " + ChatColor.YELLOW + time
								+ ChatColor.GOLD + " seconds remaining");
						player2.sendMessage(ChatColor.GOLD + "Time remaining: " + ChatColor.YELLOW + time
								+ ChatColor.GOLD + " seconds remaining");
					}

				}
				if (player1.isDead() == true || player2.isDead() == true) {
					time = 0;
					someoneDied = true;
					if (player1.isDead()) {
						Bukkit.broadcastMessage(ChatColor.GOLD + player2.getName() + ChatColor.YELLOW
								+ " has just defeated " + ChatColor.GOLD + player1.getName() + ChatColor.YELLOW
								+ " in the Battle Arena!");
					} else if (player2.isDead()) {
						Bukkit.broadcastMessage(ChatColor.GOLD + player1.getName() + ChatColor.YELLOW
								+ " has just defeated " + ChatColor.GOLD + player2.getName() + ChatColor.YELLOW
								+ " in the Battle Arena!");
					}

					respawnPlayer(player1);
					respawnPlayer(player2);

					player1.getInventory().clear();
					player2.getInventory().clear();

					// sets all of players info
					player1.setHealth(player1Health);
					player2.setHealth(player2Health);
					player1.setFoodLevel(player1Food);
					player2.setFoodLevel(player2Food);
					player1.setExp(player1EXP);
					player2.setExp(player2EXP);
					player1.teleport(savedPlayerPosition1);
					player2.teleport(savedPlayerPosition2);
					gamePlaying = false;
					stopTimer();
					return;

				}
				time = time - 1;

			}
		}, 0L, 20L);
	}

	public void stopTimer() {
		Bukkit.getScheduler().cancelTask(taskID);
		return;
	}

	public void respawnPlayer(Player paramPlayer) {
		if (paramPlayer.isDead())
			((CraftServer) Bukkit.getServer()).getHandle().moveToWorld(((CraftPlayer) paramPlayer).getHandle(), false);
	}

	@EventHandler
	public void onCommandExecuted(PlayerCommandPreprocessEvent e) {

		Player player = e.getPlayer();
		getLogger().info(e.getMessage());

		// Check if user is in game if so leave the game
		for (Player p : inGame) {
			if (e.getPlayer() != p) {
				getLogger().info(p.getName());
			}

			else if (e.getMessage().equalsIgnoreCase("/arena l")
					|| e.getMessage().equalsIgnoreCase("/arena leave") && (e.getPlayer() == p)) {

				if (p.getName() == player.getName()) {

					player.sendMessage(ChatColor.DARK_PURPLE + "" + "You have Left the game");
					someoneDied = false;
					someoneLeft = true;
					time = 0;
				} else {

					p.sendMessage(ChatColor.DARK_PURPLE + "" + "Your opponent has left the game");
				}
			} else if ((e.getPlayer() == p)) {
				for (Player pl : inGame) {

					if (pl.getName() == player.getName()) {

						if (gamePlaying == true) {
							player.sendMessage(ChatColor.RED + "You Cannot Execute Commands inside the Arena!");
							e.setCancelled(true);
						}
					}
				}

			}

		}

	}

	@EventHandler()
	public void onClick(InventoryClickEvent event) {
		if (!event.getInventory().equals(inv)) {
			return;
		}

		if (event.getCurrentItem() == null)
			return;

		if (event.getCurrentItem().getItemMeta() == null)
			return;

		if (event.getCurrentItem().getItemMeta().getDisplayName() == null)
			return;

		event.setCancelled(true);

		Player player = (Player) event.getWhoClicked();

		if (event.getSlot() == 0) {
			// join arena
			player.chat("/arena join");
			player.closeInventory();
		}
		if (event.getSlot() == 1) {
			// join arena
			player.chat("/arena leave");
			player.closeInventory();
		}
		if (event.getSlot() == 8) {
			player.closeInventory();
		}
		
		if(event.getSlot() == 2)
		{
			player.chat("/arena help");
			player.closeInventory();
		}
		
		return;

	}

	public static void helpGuide(Player player) {
		// Display all arenas
		player.sendMessage(ChatColor.YELLOW + "The following Commands accessable for Battle arena");
		player.sendMessage(ChatColor.RED + " /Arena join " + ChatColor.YELLOW + " - To join Arena");
		player.sendMessage(ChatColor.RED + " /Arena leave " + ChatColor.YELLOW + " - To leave Arena queue or game");

		// Only display perms for admins here
		if (player.hasPermission("arena.create")) {
			player.sendMessage(ChatColor.GREEN + " ---------------------" + ChatColor.RED + " Admin Help"
					+ ChatColor.GREEN + " -------------------------------");
			player.sendMessage(ChatColor.RED + " /Arena list " + ChatColor.YELLOW
					+ " - Display available arenas from config file");
			player.sendMessage(ChatColor.RED + " /Arena spawn1 " + ChatColor.YELLOW
					+ " - Sets first spawnpoint when creating arena");
			player.sendMessage(ChatColor.RED + " /Arena spawn2 " + ChatColor.YELLOW
					+ " - sets second spawnpoint when creating arena");
			player.sendMessage(ChatColor.RED + " /Arena create (name) " + ChatColor.YELLOW
					+ " - Creates an arena after setting 2 spawnpoints");
			player.sendMessage(
					ChatColor.RED + " /Arena set (name)" + ChatColor.YELLOW + " - Sets the Arena to the name provided");

		}
	}

	public void createInv() {
		inv = Bukkit.createInventory(null, 9, ChatColor.GOLD + "" + ChatColor.BOLD + "Arena");

		ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
		ItemMeta meta = item.getItemMeta();

		meta.setDisplayName(ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "Join Arena!");
		List<String> lore = new ArrayList<String>();
		
		lore.add(ChatColor.YELLOW + "Join the Arena!");
		meta.setLore(lore);
		item.setItemMeta(meta);
		inv.setItem(0, item);

		
//		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
//		taskID = scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
//
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				
//			}		}, 0L, 20L);
			
		
//		for(Player p : deque)
//		{
//			lore.add(ChatColor.DARK_GREEN + "" + p.getName());
//		}

//		
		ItemStack item1 = new ItemStack(Material.RED_DYE);
		ItemMeta meta1 = item1.getItemMeta();

		meta1.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Leave Arena!");
		List<String> leave = new ArrayList<String>();
		
		leave.add(ChatColor.YELLOW + "Leave the Arena");
		meta1.setLore(leave);
		item1.setItemMeta(meta1);
		inv.setItem(1, item1);
//		
		ItemStack item3 = new ItemStack(Material.BARRIER);
		ItemMeta meta3 = item3.getItemMeta();

		meta3.setDisplayName(ChatColor.DARK_BLUE+ "" +ChatColor.BOLD + "Close Menu");
		lore.clear();
		meta3.setLore(lore);
		item3.setItemMeta(meta3);
		inv.setItem(8, item3);
		
		ItemStack item4 = new ItemStack(Material.WHITE_WOOL);
		ItemMeta meta4 = item1.getItemMeta();
		
		meta4.setDisplayName(ChatColor.WHITE + "" + ChatColor.BOLD + "Need Help?");
		List<String> help = new ArrayList<String>();
		help.add(ChatColor.YELLOW + "Show possible commands");
		meta4.setLore(help);
		item4.setItemMeta(meta4);
		inv.setItem(2, item4);
		

	}

}
