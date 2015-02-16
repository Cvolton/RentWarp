package cz.michaelbrabec.plugins.rentwarp;

import java.io.File;
import java.io.IOException;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class RentWarpMain extends JavaPlugin {
    public static Economy econ = null;
    File warpsFile = new File(this.getDataFolder(), "warps.yml");
    FileConfiguration warps = YamlConfiguration.loadConfiguration(warpsFile);
    
	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		Player posilac = (Player) sender;
		if(command.getName().toLowerCase().equals("rentwarp")){
			if(args.length != 2){
				sender.sendMessage("Usage: /rentwarp <warpName> <timeInDays> where <timeInDays> is how long in days you actually want to have the warp rented!");
			return true;
			}else{
			String warpname = args[0].toLowerCase();
			String realname = args[0];
			int delka = Integer.parseInt(args[1]);
			int cena = getConfig().getInt("Warps.HowMuchDoesItCostForOneDay") * delka;
			log(String.format("Player %s has rented a warp called %s for %s days! It costed him %s %s and he has now %s", sender.getName(), warpname, delka, cena, econ.currencyNamePlural(), econ.getBalance((Player) sender)));
			File tenhlewarp = new File(this.getDataFolder()+"/warps", warpname+".yml");
			FileConfiguration thiswarp = YamlConfiguration.loadConfiguration(tenhlewarp);
			if(econ.has(posilac, cena)){
			EconomyResponse r = econ.withdrawPlayer(posilac, cena);
			if(r.transactionSuccess()) {
				Boolean existuje = thiswarp.getBoolean("isActive");
				if(existuje == true){
					sender.sendMessage("That warp already exists! Please specify a different name!");
					econ.depositPlayer(posilac, cena);
				} else {
					sender.sendMessage(String.format("You rented the warp %s for %s days! It costed you %s, so you have now %s!", warpname, delka, econ.format(r.amount), econ.format(r.balance)));
					thiswarp.set("isActive", true);
					thiswarp.set("X", posilac.getLocation().getX());
					thiswarp.set("Y", posilac.getLocation().getY());
					thiswarp.set("Z", posilac.getLocation().getZ());
					thiswarp.set("world", posilac.getWorld().getName());
					thiswarp.set("owner", posilac.getUniqueId().toString());
					thiswarp.set("ownername", posilac.getName());
					thiswarp.set("created", System.currentTimeMillis()/1000);
					thiswarp.set("displayname", realname);
					int matika = (int) (System.currentTimeMillis()/1000);
					int matika2 = matika + delka*86400;
					thiswarp.set("expires", matika2);
					try {
						thiswarp.save(tenhlewarp);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				return true;
            } else {
                sender.sendMessage(String.format("There was an error: %s", r.errorMessage));
           return true;
            }} else{
            	sender.sendMessage(String.format("You have %s %s, but you need to have %s %s!",econ.getBalance(posilac),econ.currencyNamePlural(),cena,econ.currencyNameSingular()));
            return true;
            }
			}
		}
		if(command.getName().equalsIgnoreCase("delwarp")){
			if(args.length != 1){
				sender.sendMessage("Usage: /delwarp <warpName>");
				return true;
			}else{
			String warpname = args[0].toLowerCase();
			File tenhlewarp = new File(this.getDataFolder()+"/warps", warpname+".yml");
			FileConfiguration thiswarp = YamlConfiguration.loadConfiguration(tenhlewarp);
			if(posilac.getUniqueId().toString().equals(thiswarp.getString("owner"))){
				Boolean existuje = thiswarp.getBoolean("isActive");
				if(existuje == true){
					 
		    		if(tenhlewarp.delete()){
		    			sender.sendMessage("Warp "+warpname+" has been succesfully deleted!");
		    			log("Player "+posilac.getName()+" has succesfully removed the warp "+warpname);
		    		}else{
		    			sender.sendMessage("Failed to remove the warp "+warpname);
		    		}
				} else {
					sender.sendMessage("§cCouldn't delete the warp "+warpname+"! It doesn't exist!");
				}
			} else if(posilac.hasPermission("rentwarp.admindelete")){
				Boolean existuje = thiswarp.getBoolean("isActive");
				if(existuje == true){
					if(tenhlewarp.delete()){
		    			sender.sendMessage("Warp "+warpname+" has been succesfully deleted!");
		    			log("Player "+posilac.getName()+" has succesfully removed the warp "+warpname);
		    		}else{
		    			sender.sendMessage("Failed to remove the warp "+warpname);
		    		}
				} else {
					sender.sendMessage("§cCouldn't delete the warp "+warpname+"! It doesn't exist!");
				}} else {
					sender.sendMessage("This warp doesn't belong to you, and you don't have the permission to delete other people's warps!");
				}
			}
			return true;
		}
		if(command.getName().equalsIgnoreCase("setwarp"))
		{
			if(args.length != 1){
				sender.sendMessage("Usage: /setwarp <warpName>");
				return true;
			}else{
			String realname = args[0];
			String warpname = args[0].toLowerCase();
			File tenhlewarp = new File(this.getDataFolder()+"/warps", warpname+".yml");
			FileConfiguration thiswarp = YamlConfiguration.loadConfiguration(tenhlewarp);
			int cena = 0;
			log(String.format("Player %s created the warp %s until the server's death or the usage of /delwarp command", sender.getName(), warpname));
			EconomyResponse r = econ.withdrawPlayer(posilac, cena);
			if(econ.has(posilac, cena)){
			if(r.transactionSuccess()) {
				Boolean existuje = thiswarp.getBoolean("isActive");
				if(existuje == true){
					sender.sendMessage("That warp already exists! If you want to move it, please first delete it with the /delwarp command");
					econ.depositPlayer(posilac, cena);
				} else {
					sender.sendMessage(String.format("Warp %s set!", warpname));
					thiswarp.set("isActive", true);
					thiswarp.set("X", posilac.getLocation().getX());
					thiswarp.set("Y", posilac.getLocation().getY());
					thiswarp.set("Z", posilac.getLocation().getZ());
					thiswarp.set("world", posilac.getWorld().getName());
					thiswarp.set("owner", posilac.getUniqueId().toString());
					thiswarp.set("ownername", posilac.getName());
					thiswarp.set("created", System.currentTimeMillis()/1000);
					thiswarp.set("expires", 2147483647);
					thiswarp.set("displayname", realname);
					try {
						thiswarp.save(tenhlewarp);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				return true;
            } else {
                sender.sendMessage(String.format("There was an error: %s", r.errorMessage));
           return true;
            }} else{
            	sender.sendMessage(String.format("You have %s %s, but you need to have %s %s!",econ.getBalance(posilac),econ.currencyNamePlural(),cena,econ.currencyNameSingular()));
            return true;
            }
			}
		}
		if(command.getName().equalsIgnoreCase("rwver")){
			sender.sendMessage("§6Running §a§lRentWarp §6version §c§l1.2§6 by §b§lCvolton");
			return true;
		}
		if(command.getName().equalsIgnoreCase("warp")){
			if(args.length != 1){
				File f = new File(this.getDataFolder()+"/warps");
				String[] names = f.list();
				String ovad = "";
				for(int i = 0; i < names.length; i++){ 
					String ovadobecny = names[i]; 
					if(!ovad.equals("")){
						ovadobecny = ", "+ovadobecny;
					}
					if(ovadobecny.endsWith(".yml")){
						ovadobecny = ovadobecny.replace(".yml", "");
					    ovad = ovad + ovadobecny; 
					}
				}
				sender.sendMessage("List of warps: "+ovad);
				return true;
			}else{
				String warpname = args[0].toLowerCase();
				File tenhlewarp = new File(this.getDataFolder()+"/warps", warpname+".yml");
				FileConfiguration thiswarp = YamlConfiguration.loadConfiguration(tenhlewarp);
				Boolean existuje = thiswarp.getBoolean("isActive");
				if(existuje == true){
					if(thiswarp.getInt("expires") < System.currentTimeMillis()/1000){
					if(tenhlewarp.delete()){
						sender.sendMessage("§cError: This warp doesn't exist!");
					}else{
						sender.sendMessage("§cError: This warp doesn't exist!");
						log("There was an error removing warp "+warpname+", but it just expired, so it's inaccessible. Remove the file yourself or just wait till someone else tries to go to the warp.");
					}
					}
						else{
						String svet = thiswarp.getString("world");
						int X = thiswarp.getInt("X");
						int Y = thiswarp.getInt("Y");
						int Z = thiswarp.getInt("Z");
						Location location = new Location(Bukkit.getWorld(svet),X,Y,Z);
						posilac.teleport(location);
					}	
				
				return true;
				}
				else{
					sender.sendMessage("§cError: This warp doesn't exist!");
					return true;
				}
			}
		}
		if(command.getName().toLowerCase().equals("extendwarp")){
			if(args.length != 2){
				sender.sendMessage("Usage: /extendwarp <warpName> <timeInDays> where <timeInDays> is how long in days you actually want to get the warp extended!");
			return true;
			}else{
			String warpname = args[0].toLowerCase();
			File tenhlewarp = new File(this.getDataFolder()+"/warps", warpname+".yml");
			FileConfiguration thiswarp = YamlConfiguration.loadConfiguration(tenhlewarp);
			int delka = Integer.parseInt(args[1]);
			int cena = getConfig().getInt("Warps.HowMuchDoesItCostForOneDay") * delka;
			log(String.format("Player %s has extended a warp called %s for %s days! It costed him %s %s and he has now %s", sender.getName(), warpname, delka, cena, econ.currencyNamePlural(), econ.getBalance((Player) sender)));
			if(econ.has(posilac, cena)){
			EconomyResponse r = econ.withdrawPlayer(posilac, cena);
			if(r.transactionSuccess()) {
				Boolean existuje = thiswarp.getBoolean("isActive");
				if(existuje == false){
					sender.sendMessage("That warp doesn't exist! You may want to rent it! Do /rentwarp for more info!");
					econ.depositPlayer(posilac, cena);
				} else {
					sender.sendMessage(String.format("You have succesfully extended the warp %s! It cost you %s, so you have now %s!", warpname, delka, econ.format(r.amount), econ.format(r.balance)));
					int matika = thiswarp.getInt("expires");
					int matika2 = matika + delka*86400;
					thiswarp.set("expires", matika2);
					try {
						thiswarp.save(tenhlewarp);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				return true;
            } else {
                sender.sendMessage(String.format("There was an error: %s", r.errorMessage));
           return true;
            }} else{
            	sender.sendMessage(String.format("You have %s %s, but you need to have %s %s!",econ.getBalance(posilac),econ.currencyNamePlural(),cena,econ.currencyNameSingular()));
            return true;
            }
			}
		}
		if(command.getName().equalsIgnoreCase("convertwarp")){
			if(args.length != 1){
				sender.sendMessage("Usage: /convertwarp <warpName>");
			return true;
			}else{
				String warpname = args[0].toLowerCase();
				File tenhlewarp = new File(this.getDataFolder()+"/warps", warpname+".yml");
				FileConfiguration thiswarp = YamlConfiguration.loadConfiguration(tenhlewarp);
				Boolean existuje = thiswarp.getBoolean("isActive");
				if(existuje == false){
					Boolean existuje2 = warps.getBoolean("warps."+warpname+".isActive");
					if(existuje2 == true){
						thiswarp.set("isActive", existuje2);
						String svet = warps.getString("warps."+warpname+".world");
						int X = warps.getInt("warps."+warpname+".X");
						int Y = warps.getInt("warps."+warpname+".Y");
						int Z = warps.getInt("warps."+warpname+".Z");
						thiswarp.set("X", X);
						thiswarp.set("Y", Y);
						thiswarp.set("Z", Z);
						thiswarp.set("world", svet);
						String owner = warps.getString("warps."+warpname+".owner");
						String ownername = warps.getString("warps."+warpname+".ownername");
						thiswarp.set("owner", owner);
						thiswarp.set("ownername", ownername);
						int created = warps.getInt("warps."+warpname+".created");
						thiswarp.set("created", created);
						int expires = warps.getInt("warps."+warpname+".expires");
						thiswarp.set("expires", expires);
						String displayname = warps.getString("warps."+warpname+".displayname");
								if(displayname==null){
									displayname = warpname;
								}
								try {
									thiswarp.save(tenhlewarp);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								sender.sendMessage("Warp "+warpname+" has been succesfully converted!");
					}else{
						sender.sendMessage("§cError: This warp doesn't exist in the old system!");
					}
				}else{
					sender.sendMessage("§cError: This warp already exists in the new system!");
				}
			}
			return true;
		}
		return false;
		}
		
	public void log(String logstring){
		Bukkit.getLogger().info("[RentWarp] "+logstring);
	}

	@Override
	public void onDisable() {
		
	}

	@Override
	public void onEnable() {
		new File(this.getDataFolder()+"/warps").mkdirs();
		log("Alright, let's begin with the work! We don't have forever, right?");
		FileConfiguration LepsiJmenoNevymyslimCus = getConfig();
		LepsiJmenoNevymyslimCus.addDefault("Global.Language", "en");
		LepsiJmenoNevymyslimCus.addDefault("Warps.HowMuchDoesItCostForOneDay", 100);
		saveConfig();
		setupEconomy();
		log("Enabling MCStats PluginMetrics - If you want to opt-out, get Essentials and use /essentials opt-out or edit the PluginMetrics config file!");
	    try {
	        Metrics metrics = new Metrics(this);
	        metrics.start();
	        log("PluginMetrics enabled! And it's your fault! YOU LET THEM TO ENABLE! WHY? WHY DID YOU DO THAT?");
	    } catch (IOException e) {
	        log("Oh god, we failed! And it's YOUR FAULT!!! YOUR!!! (most probabbly)");
	    }
	}
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
    //Commit test, because my old commit was authored to a wrong user!

}
