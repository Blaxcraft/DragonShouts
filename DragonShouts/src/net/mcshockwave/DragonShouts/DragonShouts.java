package net.mcshockwave.DragonShouts;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import net.mcshockwave.DragonShouts.Commands.ShoutCommand;
import net.mcshockwave.DragonShouts.Utils.PacketUtils;
import net.mcshockwave.DragonShouts.Utils.PacketUtils.ParticleEffect;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class DragonShouts extends JavaPlugin {

	public static DragonShouts			ins;

	public static String				prefix;

	public static FileConfiguration		learnedData		= null;
	public static File					learnedDataFile	= null;

	public static boolean				op_only, perms_enabled, enable_cooldown, require_learn, enable_ww,
			broadcast_enabled;
	public static int					broadcast_range;
	public static String				broadcast_format;

	public static HashMap<Block, Shout>	word_walls		= new HashMap<>();

	public static BukkitTask			wwpe			= null;

	public void onEnable() {
		getCommand("shout").setExecutor(new ShoutCommand());

		Bukkit.getPluginManager().registerEvents(new DefaultListener(), this);

		ins = this;
		prefix = "�a[" + getDescription().getName() + "] �7";

		saveDefaultConfig();
		saveDefaultLD();
		
		reloadAll();

		if (enable_ww) {
			wwpe = Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
				public void run() {
					for (Block b : word_walls.keySet()) {
						PacketUtils.playParticleEffect(ParticleEffect.ENCHANTMENT_TABLE,
								b.getLocation().add(0.5, 0.5, 0.5), 0, 1, 25);
					}
				}
			}, 100, 10);
		}
	}

	public void reloadAll() {
		op_only = getConfig().getBoolean("op_only");
		perms_enabled = getConfig().getBoolean("permissions_enabled");
		enable_cooldown = getConfig().getBoolean("enable_cooldown");
		require_learn = getConfig().getBoolean("require_learn");
		enable_ww = getConfig().getBoolean("enable_word_walls");
		broadcast_enabled = getConfig().getBoolean("broadcast.enabled");
		broadcast_range = getConfig().getInt("broadcast.range");
		broadcast_format = getConfig().getString("broadcast.format");

		word_walls = getWordWalls();
	}

	public void reloadLearnedData() {
		if (learnedDataFile == null) {
			learnedDataFile = new File(getDataFolder(), "learnedShouts.yml");
		}
		learnedData = YamlConfiguration.loadConfiguration(learnedDataFile);

		InputStream defConfigStream = this.getResource("learnedShouts.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			learnedData.setDefaults(defConfig);
		}
	}

	public FileConfiguration getLearnedData() {
		if (learnedData == null) {
			reloadLearnedData();
		}
		return learnedData;
	}

	public void saveLearnedData() {
		if (learnedData == null || learnedDataFile == null) {
			return;
		}
		try {
			getLearnedData().save(learnedDataFile);
		} catch (IOException ex) {
			getLogger().log(Level.SEVERE, "Could not save config to " + learnedDataFile, ex);
		}
	}
	
	public void saveDefaultLD() {
	    if (learnedDataFile == null) {
	    	learnedDataFile = new File(getDataFolder(), "learnedShouts.yml");
	    }
	    if (!learnedDataFile.exists()) {            
	         this.saveResource("learnedShouts.yml", false);
	     }
	}

	public HashMap<Block, Shout> getWordWalls() {
		List<String> wws = getLearnedData().getStringList("word_walls");
		HashMap<Block, Shout> ww = new HashMap<>();

		for (String s : wws) {
			String[] ss = s.split(";");

			try {
				String xs = ss[0];
				String ys = ss[1];
				String zs = ss[2];
				String ws = ss[3];
				String shs = ss[4];

				int x = Integer.parseInt(xs);
				int y = Integer.parseInt(ys);
				int z = Integer.parseInt(zs);
				World w = Bukkit.getWorld(ws);
				Shout sh = Shout.valueOf(shs);

				Location b = new Location(w, x, y, z);
				ww.put(b.getBlock(), sh);

			} catch (Exception e) {
				continue;
			}
		}

		return ww;
	}

	public void addWordWall(Block b, Shout s) {
		HashMap<Block, Shout> wws = word_walls;
		wws.put(b, s);
		List<String> wwsl = new ArrayList<>();
		for (Block c : wws.keySet()) {
			Shout t = wws.get(c);
			Location l = c.getLocation();
			String add = l.getBlockX() + ";" + l.getBlockY() + ";" + l.getBlockZ() + ";" + l.getWorld().getName() + ";"
					+ t.name();

			wwsl.add(add);
		}
		getLearnedData().set("word_walls", wwsl);
		saveLearnedData();
	}

	public String transUseWW(Player p, Block b) {
		Location l = b.getLocation();
		return p.getName() + ";" + l.getBlockX() + ";" + l.getBlockY() + ";" + l.getBlockZ() + ";"
				+ l.getWorld().getName();
	}

	public boolean hasUsedWW(Player p, Block b) {
		List<String> wwu = getLearnedData().getStringList("used_word_walls");
		String s = transUseWW(p, b);
		return wwu.contains(s);
	}

	public void setUsedWW(Player p, Block b) {
		List<String> wwu = getLearnedData().getStringList("used_word_walls");
		String s = transUseWW(p, b);
		wwu.add(s);
		getLearnedData().set("used_word_walls", wwu);
		saveLearnedData();
	}

}