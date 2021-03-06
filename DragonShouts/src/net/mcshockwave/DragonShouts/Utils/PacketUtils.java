package net.mcshockwave.DragonShouts.Utils;

import net.minecraft.server.v1_7_R4.Block;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayOutBlockChange;
import net.minecraft.server.v1_7_R4.PacketPlayOutWorldParticles;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PacketUtils {

	public static enum ParticleEffect {
		HUGE_EXPLOSION(
			"hugeexplosion"),
		LARGE_EXPLODE(
			"largeexplode"),
		FIREWORKS_SPARK(
			"fireworksSpark"),
		BUBBLE(
			"bubble"),
		SUSPEND(
			"suspend"),
		DEPTH_SUSPEND(
			"depthSuspend"),
		TOWN_AURA(
			"townaura"),
		CRIT(
			"crit"),
		MAGIC_CRIT(
			"magicCrit"),
		MOB_SPELL(
			"mobSpell"),
		MOB_SPELL_AMBIENT(
			"mobSpellAmbient"),
		SPELL(
			"spell"),
		INSTANT_SPELL(
			"instantSpell"),
		WITCH_MAGIC(
			"witchMagic"),
		NOTE(
			"note"),
		PORTAL(
			"portal"),
		ENCHANTMENT_TABLE(
			"enchantmenttable"),
		EXPLODE(
			"explode"),
		FLAME(
			"flame"),
		LAVA(
			"lava"),
		FOOTSTEP(
			"footstep"),
		SPLASH(
			"splash"),
		LARGE_SMOKE(
			"largesmoke"),
		CLOUD(
			"cloud"),
		RED_DUST(
			"reddust"),
		SNOWBALL_POOF(
			"snowballpoof"),
		DRIP_WATER(
			"dripWater"),
		DRIP_LAVA(
			"dripLava"),
		SNOW_SHOVEL(
			"snowshovel"),
		SLIME(
			"slime"),
		HEART(
			"heart"),
		ANGRY_VILLAGER(
			"angryVillager"),
		HAPPY_VILLAGER(
			"happyVillager");

		public String	particleName;

		ParticleEffect(String particleName) {
			this.particleName = particleName;
		}

	}

	public static void playBlockParticles(Material m, int data, Location l) {
		sendPacketGlobally(l, 20, generateBlockParticles(m, data, l));
	}

	public static void playParticleEffect(ParticleEffect pe, Location l, float rad, float speed, int amount) {
		sendPacketGlobally(l, 20, generateParticles(pe, l, rad, speed, amount));
	}

	@SuppressWarnings("deprecation")
	public static PacketPlayOutWorldParticles generateBlockParticles(Material m, int data, Location l) {
		l = l.add(0.5, 0.5, 0.5);
		String icn = m.isBlock() ? "tilecrack_" : "iconcrack_";
		icn += m.getId() + "_" + data;
		PacketPlayOutWorldParticles pack = new PacketPlayOutWorldParticles(icn, (float) l.getX(), (float) l.getY(),
				(float) l.getZ(), 0.5f, 0.5f, 0.5f, 1, 50);
		return pack;
	}

	public static PacketPlayOutWorldParticles generateParticles(ParticleEffect particle, Location l, float rad,
			float speed, int amount) {
		PacketPlayOutWorldParticles pack = new PacketPlayOutWorldParticles(particle.particleName, (float) l.getX(),
				(float) l.getY(), (float) l.getZ(), rad, rad, rad, speed, amount);
		return pack;
	}

	public static PacketPlayOutBlockChange setBlockFromPacket(Material m, int data, Location loc) {
		PacketPlayOutBlockChange pack = new PacketPlayOutBlockChange();
		pack.block = (Block) loc.getBlock();
		pack.data = data;
		return pack;
	}

	public static void sendPacket(Player p, Packet pack) {
		((CraftPlayer) p).getHandle().playerConnection.sendPacket(pack);
	}

	public static void sendPacketGlobally(Location l, int distance, Packet pack) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (p.getWorld() == l.getWorld() && p.getLocation().distance(l) <= distance) {
				sendPacket(p, pack);
			}
		}
	}
}
