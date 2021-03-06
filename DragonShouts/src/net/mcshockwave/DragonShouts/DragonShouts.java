package net.mcshockwave.DragonShouts;

import net.mcshockwave.DragonShouts.Commands.ShoutCommand;
import net.mcshockwave.DragonShouts.Utils.PacketUtils;
import net.mcshockwave.DragonShouts.Utils.PacketUtils.ParticleEffect;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

public class DragonShouts extends JavaPlugin {

	public static DragonShouts			ins;

	public static String				prefix;

	public static FileConfiguration		learnedData		= null;
	public static File					learnedDataFile	= null;

	public static FileConfiguration		shoutConfig		= null;
	public static File					shoutConfigFile	= null;

	public static boolean				op_only, bypass_opcool, perms_enabled, perm_auto, enable_cooldown,
			require_learn, enable_ww, broadcast_enabled;
	public static int					broadcast_range;
	public static Material				ww_item;
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
		saveDefaultShoutCon();

		reloadAll();

		if (enable_ww) {
			wwpe = Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
				public void run() {
					for (Block b : word_walls.keySet()) {
						if (b == null || b.getType() != Material.ENCHANTMENT_TABLE)
							return;
						PacketUtils.playParticleEffect(ParticleEffect.ENCHANTMENT_TABLE,
								b.getLocation().add(0.5, 0.5, 0.5), 0, 1, 25);
					}
				}
			}, 100, 10);
		}
	}

	public void setEnabled() {
		this.setEnabled(true);
	}

	@SuppressWarnings("deprecation")
	public void reloadAll() {
		op_only = getConfig().getBoolean("op_only");
		perms_enabled = getConfig().getBoolean("permissions_enabled");
		perm_auto = getConfig().getBoolean("permission_auto_learn");
		enable_cooldown = getConfig().getBoolean("enable_cooldown");
		require_learn = getConfig().getBoolean("require_learn");
		enable_ww = getConfig().getBoolean("enable_word_walls");
		broadcast_enabled = getConfig().getBoolean("broadcast.enabled");
		broadcast_range = getConfig().getInt("broadcast.range");
		broadcast_format = getConfig().getString("broadcast.format");
		bypass_opcool = getConfig().getBoolean("op_bypass_cooldown");
		ww_item = Material.getMaterial(getConfig().getInt("word_wall_item"));

		word_walls = getWordWalls();

		FileConfiguration sc = shoutConfig;
		for (Shout s : Shout.values()) {
			String path = "shouts." + s.name().toLowerCase() + ".";

			// Cooldown configs
			if (sc.contains(path + "cooldown.1")) {
				s.c1 = sc.getInt(path + "cooldown.1");
			} else
				sc.set(path + "cooldown.1", s.c1);
			if (sc.contains(path + "cooldown.2")) {
				s.c2 = sc.getInt(path + "cooldown.2");
			} else
				sc.set(path + "cooldown.2", s.c2);
			if (sc.contains(path + "cooldown.3")) {
				s.c3 = sc.getInt(path + "cooldown.3");
			} else
				sc.set(path + "cooldown.3", s.c3);

			// Power of shout (useless for some)
			if (sc.contains(path + "power")) {
				s.power = sc.getDouble(path + "power");
			} else
				sc.set(path + "power", s.power);
		}

		saveShoutCon();
	}

	public void reloadShoutCon() {
		if (shoutConfigFile == null) {
			shoutConfigFile = new File(getDataFolder(), "shoutConfiguration.yml");
		}
		shoutConfig = YamlConfiguration.loadConfiguration(shoutConfigFile);

		Reader defConfigStream;
		try {
			defConfigStream = new InputStreamReader(this.getResource("shoutConfiguration.yml"), "UTF8");
			if (defConfigStream != null) {
				YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
				learnedData.setDefaults(defConfig);
			}
		} catch (UnsupportedEncodingException e) {
		}
	}

	public FileConfiguration getShoutCon() {
		if (shoutConfig == null) {
			reloadShoutCon();
		}
		return shoutConfig;
	}

	public void saveShoutCon() {
		if (shoutConfig == null || shoutConfigFile == null) {
			return;
		}
		try {
			getShoutCon().save(shoutConfigFile);
		} catch (IOException ex) {
			getLogger().log(Level.SEVERE, "Could not save config to " + shoutConfigFile, ex);
		}
	}

	public void saveDefaultShoutCon() {
		if (shoutConfigFile == null) {
			shoutConfigFile = new File(getDataFolder(), "shoutConfiguration.yml");
		}
		if (!shoutConfigFile.exists()) {
			this.saveResource("shoutConfiguration.yml", false);
		}
	}

	public void reloadLearnedData() {
		if (learnedDataFile == null) {
			learnedDataFile = new File(getDataFolder(), "learnedShouts.yml");
		}
		learnedData = YamlConfiguration.loadConfiguration(learnedDataFile);

		Reader defConfigStream;
		try {
			defConfigStream = new InputStreamReader(this.getResource("learnedShouts.yml"), "UTF8");
			if (defConfigStream != null) {
				YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
				learnedData.setDefaults(defConfig);
			}
		} catch (UnsupportedEncodingException e) {
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
