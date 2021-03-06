package net.mcshockwave.DragonShouts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import net.mcshockwave.DragonShouts.Utils.LocUtils;
import net.mcshockwave.DragonShouts.Utils.PacketUtils;
import net.mcshockwave.DragonShouts.Utils.PacketUtils.ParticleEffect;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.google.common.collect.Lists;

public enum Shout {

	Unrelenting_Force(
		"Fus",
		"Ro",
		"Dah",
		15,
		20,
		45),
	Fire_Breath(
		"Yol",
		"Toor",
		"Shul",
		30,
		50,
		100),
	Frost_Breath(
		"Fo",
		"Krah",
		"Diin",
		30,
		50,
		100),
	Ice_Form(
		"Iiz",
		"Slen",
		"Nus",
		60,
		90,
		120),
	Storm_Call(
		"Strun",
		"Bah",
		"Qo",
		300,
		480,
		600),
	Whirlwind_Sprint(
		"Wuld",
		"Nah",
		"Kest",
		20,
		25,
		35),
	Become_Ethereal(
		"Feim",
		"Zii",
		"Gron",
		30,
		40,
		50),
	Dragon_Aspect(
		"Mul",
		"Qah",
		"Diiv",
		200,
		400,
		600),
	Aura_Whisper(
		"Laas",
		"Yah",
		"Nir",
		30,
		40,
		50),
	Marked_For_Death(
		"Krii",
		"Lun",
		"Aus",
		20,
		30,
		40),
	Drain_Vitality(
		"Gaan",
		"Lah",
		"Haas",
		30,
		60,
		90),
	Battle_Fury(
		"Mid",
		"Vur",
		"Shaan",
		20,
		30,
		40),
	Elemental_Fury(
		"Su",
		"Grah",
		"Dun",
		30,
		40,
		50),
	Soul_Tear(
		"Rii",
		"Vaaz",
		"Zol",
		5,
		5,
		90),
	Clear_Skies(
		"Lok",
		"Vah",
		"Koor",
		5,
		10,
		15),
	Cyclone(
		"Ven",
		"Gar",
		"Nos",
		30,
		45,
		60),
	Disarm(
		"Zun",
		"Haal",
		"Viik",
		30,
		35,
		50),
	Throw_Voice(
		"Zul",
		"Mey",
		"Gut",
		30,
		15,
		5),
	Animal_Allegiance(
		"Raan",
		"Mir",
		"Tah",
		50,
		60,
		70),
	Call_Dragon(
		"Od",
		"Ah",
		"Viing",
		5,
		5,
		300),
	Summon_Durnehviir(
		"Dur",
		"Neh",
		"Viir",
		5,
		5,
		300),
	Call_of_Valor(
		"Hun",
		"Kaal",
		"Zor",
		5,
		5,
		180),
	Kynes_Peace(
		"Kaan",
		"Drem",
		"Ov",
		40,
		50,
		60),
	Slow_Time(
		"Tiid",
		"Klo",
		"Ul",
		30,
		45,
		60),
	Dragonrend(
		"Joor",
		"Zah",
		"Frul",
		10,
		12,
		15);

	public String	name, w1, w2, w3;
	public int		c1, c2, c3;
	public double	power;

	Shout(String w1, String w2, String w3, int cool1, int cool2, int cool3) {
		this.name = name().replace('_', ' ');

		this.w1 = w1;
		this.w2 = w2;
		this.w3 = w3;
		this.c1 = cool1;
		this.c2 = cool2;
		this.c3 = cool3;

		this.power = 1;
	}

	public int getCooldown(int num) {
		if (num == 1) {
			return c1;
		}
		if (num == 2) {
			return c2;
		}
		if (num >= 3) {
			return c3;
		}
		return 0;
	}

	public Random						rand		= new Random();

	public static HashMap<Player, Long>	cooldown	= new HashMap<>();

	public boolean shoutCooldowns(final Player p, final int num) {
		if (!hasLearnedShout(p, num)) {
			p.sendMessage(DragonShouts.prefix + "You haven't learned that shout yet!");
			return false;
		}

		if (DragonShouts.enable_cooldown && (!p.isOp() || p.isOp() && !DragonShouts.bypass_opcool)) {
			if (cooldown.containsKey(p)
					&& cooldown.get(p) > TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())) {
				p.sendMessage(DragonShouts.prefix + "You can't shout for another "
						+ (cooldown.get(p) - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())) + " seconds!");
				return false;
			} else {
				cooldown.remove(p);
				cooldown.put(p, TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + getCooldown(num));
				Bukkit.getScheduler().runTaskLaterAsynchronously(DragonShouts.ins, new Runnable() {
					public void run() {
						p.sendMessage(DragonShouts.prefix + "You can now shout!");
					}
				}, getCooldown(num) * 20);
			}
		}

		if (DragonShouts.broadcast_enabled) {
			for (Player p2 : Bukkit.getOnlinePlayers()) {
				if (p.getWorld() == p2.getWorld()
						&& p.getLocation().distance(p2.getLocation()) < DragonShouts.broadcast_range
						|| DragonShouts.broadcast_range <= 0) {
					String send = ChatColor.translateAlternateColorCodes(
							'&',
							DragonShouts.broadcast_format
									.replaceAll("%p", p.getDisplayName())
									.replaceAll(
											"%w",
											w1.toUpperCase()
													+ (num > 1 ? " " + w2.toUpperCase()
															+ (num > 2 ? " " + w3.toUpperCase() + "" : "") : ""))
									.replaceAll("%n", name));
					p2.sendMessage(send);
				}
			}
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	public void shout(final Player p, final int num) {
		if (!shoutCooldowns(p, num)) {
			return;
		}

		if (this == Unrelenting_Force) {
			Block[] bs = p.getLineOfSight(null, (num * 4) + 2).toArray(new Block[0]);
			List<Entity> near = p.getNearbyEntities(15, 15, 15);
			p.getWorld().playSound(p.getLocation(), Sound.EXPLODE, 1, 1);
			for (Block b : bs) {
				PacketUtils.playParticleEffect(ParticleEffect.CLOUD, b.getLocation().add(0.5, 0.5, 0.5), 0, 0.3f,
						num * 15);

				for (Entity e : near) {
					if (e.getLocation().distance(b.getLocation()) < 4) {
						e.setVelocity(LocUtils.getVelocity(p.getLocation(), e.getLocation()).multiply(num / 2 + 1)
								.multiply(power).add(new Vector(0, 0.1, 0)));
					}
				}
			}
		}

		if (this == Fire_Breath) {
			Block[] bs = p.getLineOfSight(null, (num * 2) + 2).toArray(new Block[0]);
			List<Entity> near = p.getNearbyEntities(15, 15, 15);
			p.getWorld().playSound(p.getLocation(), Sound.GHAST_FIREBALL, 1, 0);
			for (Block b : bs) {
				PacketUtils.playParticleEffect(ParticleEffect.FLAME, b.getLocation().add(0.5, 0.5, 0.5), 0, 0.1f,
						num * 15);

				for (Entity e : near) {
					if (e.getLocation().distance(b.getLocation()) < 4) {
						e.setFireTicks((int) (100 * num * power));
					}
				}
			}
		}

		if (this == Ice_Form) {
			Block[] bs = p.getLineOfSight(null, (num * 2) + 2).toArray(new Block[0]);
			List<Entity> near = p.getNearbyEntities(15, 15, 15);
			p.getWorld().playSound(p.getLocation(), Sound.GLASS, 1, 0);
			for (Block b : bs) {
				PacketUtils.playParticleEffect(ParticleEffect.INSTANT_SPELL, b.getLocation().add(0.5, 0.5, 0.5), 1,
						0.3f, num * 35);

				for (Entity e : near) {
					if (!(e instanceof LivingEntity)) {
						continue;
					}
					LivingEntity le = (LivingEntity) e;
					if (e.getLocation().distance(b.getLocation()) < 4) {
						le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (num * 20 * power), 25));
					}
				}
			}
		}

		if (this == Frost_Breath) {
			Block[] bs = p.getLineOfSight(null, (num * 2) + 2).toArray(new Block[0]);
			List<Entity> near = p.getNearbyEntities(15, 15, 15);
			p.getWorld().playSound(p.getLocation(), Sound.GLASS, 1, 0);
			for (Block b : bs) {
				PacketUtils.playParticleEffect(ParticleEffect.INSTANT_SPELL, b.getLocation().add(0.5, 0.5, 0.5), 1,
						0.3f, num * 35);

				for (Entity e : near) {
					if (!(e instanceof LivingEntity)) {
						continue;
					}
					LivingEntity le = (LivingEntity) e;
					if (e.getLocation().distance(b.getLocation()) < 4) {
						le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (num * 50 * power), num - 1));
						le.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, (int) (num * 20 * power), num - 1));
					}
				}
			}
		}

		if (this == Whirlwind_Sprint) {
			p.setVelocity(p.getLocation().getDirection().multiply((num * 2) + 3).multiply(power).setY(0.4f));
			p.getWorld().playSound(p.getLocation(), Sound.BAT_TAKEOFF, 1, 0);
		}

		if (this == Become_Ethereal) {
			p.getWorld().playSound(p.getLocation(), Sound.FIZZ, 1, 0);

			p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, (int) (num * 150 * power), 50));
			p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, (int) (num * 150 * power), 50));
		}

		if (this == Dragon_Aspect) {
			p.getWorld().playSound(p.getLocation(), Sound.ENDERDRAGON_GROWL, 1, 1.3f);

			int dur = num * 300;

			p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, (int) (dur * power), 3));
			p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, (int) (dur * power), 2));
			p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, (int) (dur * power), 1));

			for (int i = 0; i < dur / 5; i++) {
				Bukkit.getScheduler().runTaskLater(DragonShouts.ins, new Runnable() {
					public void run() {
						if (p.isOnline()) {
							PacketUtils.playParticleEffect(ParticleEffect.LAVA, p.getLocation(), 0, 0.1f, 10);
						}
					}
				}, i * 5);
			}
		}

		if (this == Storm_Call) {
			Location l = p.getLocation();
			boolean ce = DragonShouts.enable_cooldown;

			for (int i = 0; i < rand.nextInt(5 + num) + (num * 2 * power) + (ce ? 10 : 0); i++) {
				final Location l2 = LocUtils.addRand(l.clone(), 50, 25, 50);

				Bukkit.getScheduler().runTaskLater(DragonShouts.ins, new Runnable() {
					public void run() {
						Location m = l2;
						for (Player p2 : Bukkit.getOnlinePlayers()) {
							if (p2 != p && p2.getLocation().distance(m) < 25) {
								m = p2.getLocation();
								break;
							}
						}

						m.getWorld().strikeLightningEffect(m);

						for (Player p2 : Bukkit.getOnlinePlayers()) {
							if (p2.getWorld() == p.getWorld() && p2 != p && p2.getLocation().distance(m) < 10) {
								p2.setFireTicks((int) (num * 100 * power));
							}
						}
					}
				}, rand.nextInt(ce ? 200 : 100) + i * 10);
			}
		}

		if (this == Dragonrend) {
			Block[] bs = p.getLineOfSight(null, (int) ((num * 6) + 2 * power)).toArray(new Block[0]);
			List<Entity> near = p.getNearbyEntities(35, 35, 35);
			p.getWorld().playSound(p.getLocation(), Sound.WITHER_SHOOT, 0.5f, 0);
			for (Block b : bs) {
				PacketUtils.playParticleEffect(ParticleEffect.MAGIC_CRIT, b.getLocation().add(0.5, 0.5, 0.5), 1, 0.3f,
						num * 15);

				for (Entity e : near) {
					if (!(e instanceof Player)) {
						continue;
					}
					Player p2 = (Player) e;
					if (p2.getAllowFlight() && e.getLocation().distance(b.getLocation()) < 7) {
						PacketUtils.playParticleEffect(ParticleEffect.CLOUD, p2.getLocation(), 2, 0.2f, 50);
						p2.getWorld().playSound(p2.getLocation(), Sound.EXPLODE, 3, 2);
						p2.setAllowFlight(false);
					}
				}
			}
		}

		if (this == Aura_Whisper) {
			p.getWorld().playSound(p.getLocation(), Sound.ENDERDRAGON_HIT, 1, 0);
			p.sendMessage("�a[Aura Whisper] �7All nearby players:");
			for (Player p2 : Bukkit.getOnlinePlayers()) {
				if (p2.getWorld() == p.getWorld() && p2 != p
						&& p2.getLocation().distance(p.getLocation()) < num * 50 * power) {
					p.sendMessage(getAuraString(p2, p));
				}
			}
		}

		if (this == Marked_For_Death) {
			Block[] bs = p.getLineOfSight(null, (num * 6) + 2).toArray(new Block[0]);
			List<Entity> near = p.getNearbyEntities(15, 15, 15);
			p.getWorld().playSound(p.getLocation(), Sound.WITHER_HURT, 2f, 0);
			for (Block b : bs) {
				PacketUtils.playParticleEffect(ParticleEffect.LARGE_SMOKE, b.getLocation().add(0.5, 0.5, 0.5), 1, 0.3f,
						num * 15);

				for (Entity e : near) {
					if (!(e instanceof Player)) {
						continue;
					}
					Player p2 = (Player) e;
					if (e.getLocation().distance(b.getLocation()) < 7) {
						p2.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,
								(int) (num * 120 * power), -num * 2));
						p2.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, (int) (num * 20 * power), num - 1));
					}
				}
			}
		}

		if (this == Drain_Vitality) {
			Block[] bs = p.getLineOfSight(null, (num * 6) + 2).toArray(new Block[0]);
			List<Entity> near = p.getNearbyEntities(15, 15, 15);
			p.getWorld().playSound(p.getLocation(), Sound.WITHER_IDLE, 0.5f, 0.7f);
			for (Block b : bs) {
				PacketUtils.playParticleEffect(ParticleEffect.PORTAL, b.getLocation().add(0.5, 0.5, 0.5), 1, 0.3f,
						num * 15);

				for (Entity e : near) {
					if (!(e instanceof Player)) {
						continue;
					}
					Player p2 = (Player) e;
					if (e.getLocation().distance(b.getLocation()) < 7) {
						p2.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, (int) (num * 20 * power), num * 4));
						if (num > 1) {
							p2.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, (int) (num * 20 * power),
									num * 3));
						}
					}
				}
			}
		}

		if (this == Battle_Fury) {
			p.getWorld().playSound(p.getLocation(), Sound.WITHER_SPAWN, 1, 2);
			for (Player p2 : Bukkit.getOnlinePlayers()) {
				if (p2.getWorld() == p.getWorld() && p2 != p && p2.getLocation().distance(p.getLocation()) < num * 8) {
					p2.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, (int) (num * 100 * power),
							num - 1));
					p2.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, (int) (num * 100 * power),
							num - 1));
				}
			}
		}

		if (this == Elemental_Fury) {
			p.getWorld().playSound(p.getLocation(), Sound.WITHER_SPAWN, 1, 2);
			p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, (int) (num * 100 * power), num - 1));
			p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, (int) (num * 100 * power), num - 2));
		}

		if (this == Soul_Tear) {
			Block[] bs = p.getLineOfSight(null, (num * 6) + 2).toArray(new Block[0]);
			List<Entity> near = p.getNearbyEntities(15, 15, 15);
			p.getWorld().playSound(p.getLocation(), Sound.IRONGOLEM_HIT, 2, 0);
			if (num > 2) {
				for (Block b : bs) {
					PacketUtils.playParticleEffect(ParticleEffect.MOB_SPELL, b.getLocation().add(0.5, 0.5, 0.5), 1,
							0.3f, num * 15);

					for (Entity e : near) {
						if (!(e instanceof Player)) {
							continue;
						}
						Player p2 = (Player) e;
						if (e.getLocation().distance(b.getLocation()) < 7) {
							p2.damage(10 * power);
						}
					}
				}
			}
		}

		if (this == Clear_Skies) {
			p.getWorld().playSound(p.getLocation(), Sound.AMBIENCE_THUNDER, 5, 1);
			if (num >= 3) {
				p.getWorld().setStorm(false);
			} else
				p.getWorld().setWeatherDuration((int) (500 / num / power));
		}

		if (this == Cyclone) {
			Block[] bs = p.getLineOfSight(null, (num * 6) + 2).toArray(new Block[0]);
			List<Entity> near = p.getNearbyEntities(15, 15, 15);
			p.getWorld().playSound(p.getLocation(), Sound.FIREWORK_LARGE_BLAST2, 3, 0);
			for (Block b : bs) {
				PacketUtils.playParticleEffect(ParticleEffect.EXPLODE, b.getLocation().add(0.5, 0.5, 0.5), 0, 0.3f,
						num * 10);

				for (Entity e : near) {
					if (e.getLocation().distance(b.getLocation()) < 4) {
						if (e instanceof LivingEntity) {
							((LivingEntity) e).damage(num * 3);
						}
						e.setVelocity(e.getVelocity().add(new Vector(0, num * 0.08f * power, 0)));
					}
				}
			}
		}

		if (this == Disarm) {
			Block[] bs = p.getLineOfSight(null, (num * 6) + 2).toArray(new Block[0]);
			List<Entity> near = p.getNearbyEntities(15, 15, 15);
			p.getWorld().playSound(p.getLocation(), Sound.ANVIL_LAND, 1, 0);
			if (num > 2) {
				for (Block b : bs) {
					PacketUtils.playParticleEffect(ParticleEffect.CRIT, b.getLocation().add(0.5, 0.5, 0.5), 1, 0.3f,
							num * 15);

					for (Entity e : near) {
						if (!(e instanceof Player)) {
							continue;
						}
						Player p2 = (Player) e;
						if (e.getLocation().distance(b.getLocation()) < 7) {
							ItemStack it = p2.getItemInHand();
							if (it == null || it.getType() == Material.AIR)
								continue;
							Item i = p2.getWorld().dropItem(p2.getEyeLocation(), it);
							i.setVelocity(LocUtils.getVelocity(p.getLocation(), p2.getLocation())
									.multiply(0.05 * power));
							p2.setItemInHand(null);
						}
					}
				}
			}
		}

		if (this == Throw_Voice) {
			Sound s = Sound.values()[rand.nextInt(Sound.values().length)];
			int pit = rand.nextInt(3);
			for (Player p2 : Bukkit.getOnlinePlayers()) {
				if (p2.getWorld() == p.getWorld() && p2.getLocation().distance(p.getLocation()) < num * 30 * power) {
					p2.playSound(LocUtils.addRand(p2.getLocation().clone(), 10, 10, 10), s, 5, pit);
				}
			}
		}

		if (this == Animal_Allegiance) {
			p.getWorld().playSound(p.getLocation(), Sound.WITHER_SPAWN, 1, 1.8f);
			List<Entity> loe = p.getNearbyEntities(num * 10, num * 10, num * 10);
			for (Entity e : loe) {
				if (e instanceof Monster) {
					Monster m = (Monster) e;
					aas.put(m, p);
				}
			}
			int stop = (int) (num * 20 * power);
			for (int i = 0; i < stop; i++) {
				Bukkit.getScheduler().runTaskLater(DragonShouts.ins, new Runnable() {
					public void run() {
						for (Monster m : aas.keySet()) {
							Player p2 = aas.get(m);

							if (p2 == p) {
								if (m.isDead() || !m.isValid()) {
									continue;
								}
								PacketUtils.playParticleEffect(ParticleEffect.MOB_SPELL, m.getEyeLocation(), 0.5f, 1,
										35);
							}
						}
					}
				}, i * 10);
			}
			Bukkit.getScheduler().runTaskLater(DragonShouts.ins, new Runnable() {
				public void run() {
					for (Monster m : Lists.newArrayList(aas.keySet())) {
						Player p2 = aas.get(m);

						if (p2 == p) {
							if (m.isDead() || !m.isValid()) {
								continue;
							}
							aas.remove(m);
							PacketUtils.playBlockParticles(Material.ANVIL, 0, m.getEyeLocation());
							m.getWorld().playSound(m.getEyeLocation(), Sound.ANVIL_LAND, 1, 2);
						}
					}
				}
			}, stop * 10);
		}

		if (this == Call_Dragon) {
			if (num < 3) {
				return;
			}
			final EnderDragon ed = (EnderDragon) p.getWorld().spawnEntity(
					LocUtils.addRand(p.getLocation().clone(), 50, 50, 50).add(0, 50, 0), EntityType.ENDER_DRAGON);
			ed.setCustomName("Odahviing");
			summoned.put(ed, p);

			Bukkit.getScheduler().runTaskLater(DragonShouts.ins, new Runnable() {
				public void run() {
					PacketUtils.playParticleEffect(ParticleEffect.LAVA, ed.getLocation(), 0, 1, 25);
					ed.remove();
				}
			}, (long) (1200 * power));
		}

		if (this == Call_of_Valor) {
			p.getWorld().playSound(p.getLocation(), Sound.WITHER_SPAWN, 1, 1.6f);
			for (int i = 0; i < num; i++) {
				final Wolf w = (Wolf) p.getWorld().spawnEntity(LocUtils.addRand(p.getLocation().clone(), 5, 0, 5),
						EntityType.WOLF);
				w.setTamed(true);
				w.setOwner(p);
				w.setCollarColor(DyeColor.BLACK);
				w.setAngry(true);
				w.setCustomName(p.getName() + "'s Summon");
				w.setCustomNameVisible(true);
				w.setMaxHealth(30);
				w.setHealth(w.getMaxHealth());
				w.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 100000000, 1));
				Bukkit.getScheduler().runTaskLater(DragonShouts.ins, new Runnable() {
					public void run() {
						PacketUtils.playParticleEffect(ParticleEffect.LAVA, w.getLocation(), 0, 1, 25);
						w.setHealth(0);
					}
				}, (long) (1200 * power));
			}
		}

		if (this == Kynes_Peace) {
			p.getWorld().playSound(p.getLocation(), Sound.WITHER_SPAWN, 1, 1.8f);
			List<Entity> loe = p.getNearbyEntities(num * 30, num * 30, num * 30);
			for (Entity e : loe) {
				if (e instanceof Monster) {
					Monster m = (Monster) e;
					kps.add(m);
				}
			}
			int stop = (int) (num * 20 * power);
			for (int i = 0; i < stop; i++) {
				Bukkit.getScheduler().runTaskLater(DragonShouts.ins, new Runnable() {
					public void run() {
						for (Monster m : kps) {
							if (m.isDead() || !m.isValid()) {
								continue;
							}
							PacketUtils.playParticleEffect(ParticleEffect.SPELL, m.getEyeLocation(), 0.5f, 1, 35);
						}
					}
				}, i * 10);
			}
			Bukkit.getScheduler().runTaskLater(DragonShouts.ins, new Runnable() {
				public void run() {
					for (Monster m : Lists.newArrayList(aas.keySet())) {
						Player p2 = aas.get(m);

						if (p2 == p) {
							if (m.isDead() || !m.isValid()) {
								continue;
							}
							kps.remove(m);
							PacketUtils.playBlockParticles(Material.ANVIL, 0, m.getEyeLocation());
							m.getWorld().playSound(m.getEyeLocation(), Sound.ANVIL_LAND, 1, 2);
						}
					}
				}
			}, stop * 10);
		}

		if (this == Slow_Time) {
			p.getWorld().playSound(p.getLocation(), Sound.ENDERDRAGON_GROWL, 1, 0);
			for (Entity e : p.getNearbyEntities(num * 30, num * 30, num * 30)) {
				e.setVelocity(e.getVelocity().multiply(1 / num * 3 * power));
				if (e instanceof LivingEntity) {
					((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, num * 60, num));
				}
			}
		}

		if (this == Summon_Durnehviir) {
			if (num < 3) {
				return;
			}
			final EnderDragon ed = (EnderDragon) p.getWorld().spawnEntity(
					LocUtils.addRand(p.getLocation().clone(), 50, 50, 50).add(0, 50, 0), EntityType.ENDER_DRAGON);
			ed.setCustomName("Durnehviir");
			summoned.put(ed, p);

			Bukkit.getScheduler().runTaskLater(DragonShouts.ins, new Runnable() {
				public void run() {
					PacketUtils.playParticleEffect(ParticleEffect.LAVA, ed.getLocation(), 0, 1, 25);
					ed.remove();
				}
			}, (long) (1200 * power));
		}
	}

	public static HashMap<Monster, Player>		aas			= new HashMap<>();

	public static ArrayList<Monster>			kps			= new ArrayList<>();

	public static HashMap<EnderDragon, Player>	summoned	= new HashMap<>();

	public String getAuraString(Player p, Player n) {
		Location loc = p.getLocation();
		String ret = "�b" + p.getName() + "�7 - �a" + ((int) p.getLocation().distance(n.getLocation()))
				+ " blocks away at " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();

		return ret;
	}

	public boolean hasLearnedShout(Player p, int num) {
		if (DragonShouts.require_learn) {

			if (DragonShouts.perm_auto && DragonShouts.perms_enabled) {
				for (int i = num; i > 0; i--) {
					if (!p.hasPermission("dragonshouts.shout." + name() + "." + i)) {
						return false;
					}
				}
				return true;
			}

			if (DragonShouts.ins.getLearnedData() != null) {
				FileConfiguration ld = DragonShouts.ins.getLearnedData();

				if (ld.getList("learned_data." + p.getName()) != null) {
					List<String> sl = ld.getStringList("learned_data." + p.getName());

					for (String s : sl) {
						String[] ss = s.split(";");
						String sn = ss[0];
						int nn = Integer.parseInt(ss[1]);
						if (sn.equalsIgnoreCase(name()) && nn >= num) {
							return true;
						}
					}
				}
			}

			return false;
		} else
			return true;
	}

	public int getLearnLevel(Player p) {
		if (!hasLearnedShout(p, 1)) {
			return 0;
		}
		if (!hasLearnedShout(p, 2)) {
			return 1;
		}
		if (!hasLearnedShout(p, 3)) {
			return 2;
		}
		return 3;
	}

	public void setLearned(Player p) {
		if (DragonShouts.ins.getLearnedData() != null) {
			FileConfiguration ld = DragonShouts.ins.getLearnedData();

			List<String> sl = new ArrayList<>();
			if (ld.getList("learned_data." + p.getName()) != null) {
				sl = ld.getStringList("learned_data." + p.getName());
			}

			int ll = getLearnLevel(p);
			sl.remove(name() + ";" + ll);
			sl.add(name() + ";" + (ll + 1));

			ld.set("learned_data." + p.getName(), sl);

			DragonShouts.ins.saveLearnedData();
		}
	}

	public static void clearShouts(Player p) {
		if (DragonShouts.ins.getLearnedData() != null) {
			FileConfiguration ld = DragonShouts.ins.getLearnedData();

			List<String> sl = new ArrayList<>();
			if (ld.getList("learned_data." + p.getName()) != null) {
				sl = ld.getStringList("learned_data." + p.getName());
			}

			sl.clear();

			ld.set("learned_data." + p.getName(), sl);

			DragonShouts.ins.saveLearnedData();
		}
	}

	public void setLearnedWithEffect(Player p, Block b) {
		setLearned(p);

		int num = getLearnLevel(p);
		p.sendMessage(DragonShouts.prefix + "Word Learned: �b" + (num >= 3 ? w3 : num == 2 ? w2 : w1) + "�7 - �a"
				+ name);
		for (int i = 0; i < 5; i++) {
			p.playSound(p.getLocation(), Sound.AMBIENCE_THUNDER, 4f, 1.3f);
		}
		if (b != null) {
			DragonShouts.ins.setUsedWW(p, b);
		}
	}

	public static Shout get(String string) {
		for (Shout s : values()) {
			if (s.name().equalsIgnoreCase(string)) {
				return s;
			}
		}
		return null;
	}

}
