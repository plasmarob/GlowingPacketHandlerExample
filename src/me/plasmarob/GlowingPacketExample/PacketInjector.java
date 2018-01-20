package me.plasmarob.GlowingPacketExample;

import java.lang.reflect.Field;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import io.netty.channel.Channel;
import net.minecraft.server.v1_12_R1.DataWatcher;
import net.minecraft.server.v1_12_R1.DataWatcherObject;
import net.minecraft.server.v1_12_R1.DataWatcherRegistry;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityMetadata;

/*
 * Class for managing all of the handlers, add a method here for a new effect
 */
public class PacketInjector {

	private Field EntityPlayer_playerConnection;
	private Class<?> PlayerConnection;
	private Field PlayerConnection_networkManager;

	private Class<?> NetworkManager;
	private Field channel;
  
	//--------------------------------
	// Add any add/remove/toggle methods here
	
	
	/* 
	 * Glow Example
	 */
	public void addGlow(Player p, Entity e) {
		String uniquename = "Glow_" + p.getEntityId() + "_" + e.getEntityId();
		try {
			Channel ch = getChannel(getNetworkManager(PacketReflection.getNmsPlayer(p)));
			if(ch.pipeline().get(uniquename) == null) {
				Bukkit.getConsoleSender().sendMessage("Adding Glow.");
				GlowPacketHandler h = new GlowPacketHandler(p, e);
				ch.pipeline().addBefore("packet_handler", uniquename, h);
				
				// Send one-time metadata update now, some entities almost never get a metadata packet beyond spawn
				DataWatcher datawatcher = ((CraftLivingEntity)e).getHandle().getDataWatcher();
				//NOTE: this blanks out the other bits in the Metadata, is just easier than getting the byte to OR 0x40
				datawatcher.set(new DataWatcherObject<Byte>(0, DataWatcherRegistry.a), (byte) 0x40); //0x40 is Glow
				PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(e.getEntityId(), datawatcher, true);
                ((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
			} else {
				// Now it toggles!
				Bukkit.getConsoleSender().sendMessage("Removing Glow.");
				removeGlow(p, e);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public void removeGlow(Player p, Entity e) {
		String uniquename = "Glow_" + p.getEntityId() + "_" + e.getEntityId();
		try {
			Channel ch = getChannel(getNetworkManager(PacketReflection.getNmsPlayer(p)));
			if(ch.pipeline().get(uniquename) != null) {
				ch.pipeline().remove(uniquename);
				
				// Send one-time metadata update now, some entities almost never get a metadata packet beyond spawn
				DataWatcher datawatcher = ((CraftLivingEntity)e).getHandle().getDataWatcher();
				datawatcher.set(new DataWatcherObject<Byte>(0, DataWatcherRegistry.a), (byte) 0x00);
				PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(e.getEntityId(), datawatcher, true);
                ((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
			}
		} catch (Throwable t) {
	      t.printStackTrace();
		}
	}
	
	
	
	
	/* 
	 * Fire Example (same code, just 0x01 instead of 0x40)
	 */
	public void addFire(Player p, Entity e) {
		String uniquename = "Fire_" + p.getEntityId() + "_" + e.getEntityId();
		try {
			Channel ch = getChannel(getNetworkManager(PacketReflection.getNmsPlayer(p)));
			if(ch.pipeline().get(uniquename) == null) {
				Bukkit.getConsoleSender().sendMessage("Adding Fire.");
				FirePacketHandler h = new FirePacketHandler(p, e);
				ch.pipeline().addBefore("packet_handler", uniquename, h);
				
				// Send one-time metadata update now, some entities almost never get a metadata packet beyond spawn
				DataWatcher datawatcher = ((CraftLivingEntity)e).getHandle().getDataWatcher();
				datawatcher.set(new DataWatcherObject<Byte>(0, DataWatcherRegistry.a), (byte) 0x01); //0x01 is Fire
				PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(e.getEntityId(), datawatcher, true);
                ((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
			} else {
				// Now it toggles!
				Bukkit.getConsoleSender().sendMessage("Removing Fire.");
				removeFire(p, e);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public void removeFire(Player p, Entity e) {
		String uniquename = "Fire_" + p.getEntityId() + "_" + e.getEntityId();
		try {
			Channel ch = getChannel(getNetworkManager(PacketReflection.getNmsPlayer(p)));
			if(ch.pipeline().get(uniquename) != null) {
				ch.pipeline().remove(uniquename);
				
				// Send one-time metadata update now, some entities almost never get a metadata packet beyond spawn
				DataWatcher datawatcher = ((CraftLivingEntity)e).getHandle().getDataWatcher();
				datawatcher.set(new DataWatcherObject<Byte>(0, DataWatcherRegistry.a), (byte) 0x00);
				PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(e.getEntityId(), datawatcher, true);
                ((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
			}
		} catch (Throwable t) {
	      t.printStackTrace();
		}
	}
	
	
	//----------------------------------------------------------------------------------------------------
	
	public PacketInjector() {
		try {
			EntityPlayer_playerConnection = PacketReflection.getField(PacketReflection.getClass("{nms}.EntityPlayer"), "playerConnection");
			PlayerConnection = PacketReflection.getClass("{nms}.PlayerConnection");
			PlayerConnection_networkManager = PacketReflection.getField(PlayerConnection, "networkManager");
			NetworkManager = PacketReflection.getClass("{nms}.NetworkManager");
			channel = PacketReflection.getField(NetworkManager, "channel");
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private Object getNetworkManager(Object ep) {
		return PacketReflection.getFieldValue(PlayerConnection_networkManager, PacketReflection.getFieldValue(EntityPlayer_playerConnection, ep));
	}

	private Channel getChannel(Object networkManager) {
		Channel ch = null;
		try {
			ch = PacketReflection.getFieldValue(channel, networkManager);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ch;
	}
}
