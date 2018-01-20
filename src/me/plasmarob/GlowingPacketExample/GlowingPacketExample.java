package me.plasmarob.GlowingPacketExample;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
/*
 * Main class AND listener for convenience
 */
public class GlowingPacketExample extends JavaPlugin implements Listener {
	
	public static final PacketInjector packetInjector = new PacketInjector();
	
	@Override
	public void onEnable() {
		 Bukkit.getPluginManager().registerEvents(this, this);
	}
	
	@EventHandler
	public void interactEvent(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player) {
			Player p = (Player)event.getDamager();
			Entity e = event.getEntity();
			getLogger().info("Toggling...");
			
			// Method in packet Injector made to toggle glow
			//NOTE: don't run both, they'll fight and you'll get unexpected behavior
			packetInjector.addGlow(p, e);
			// Or for fun:
			//packetInjector.addFire(p, e);
		}
	}
}
