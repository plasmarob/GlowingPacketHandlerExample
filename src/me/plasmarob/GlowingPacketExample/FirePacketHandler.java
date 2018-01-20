package me.plasmarob.GlowingPacketExample;

import java.util.List;

import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.server.v1_12_R1.DataWatcher;
/*
 * The handler of a fire
 * - don't confuse the pairs of a() and b()
 * - - - PacketPlayOutEntityMetadata( a=EntityID, b=Metadata=List<DataWatcher.Item<?>> )
 * - - - DataWatcher.Item<?>( a=DataWatcherObject, b=?=int/float/string/byte)
 */
public class FirePacketHandler extends ChannelDuplexHandler {
	@SuppressWarnings("unused")
	private Player p;
	private Entity e;

	public FirePacketHandler(final Player p, final Entity e) {
		this.p = p;
		this.e = e;
	}
	/*
	 * (non-Javadoc)
	 * @see io.netty.channel.ChannelDuplexHandler#write(io.netty.channel.ChannelHandlerContext, java.lang.Object, io.netty.channel.ChannelPromise)
	 */
	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		
		// PacketPlayOutEntityMetadata
		// a = the entity ID
		// b = the metadata - a List of DataWatcher.Item<?>
		
		if(msg.getClass().getSimpleName().equalsIgnoreCase("PacketPlayOutEntityMetadata")) {
			
			// Get EntityID
			Integer eid = (Integer) PacketReflection.getFieldValue(msg, "a");
			// ? could be Byte, String, Integer, Boolean - this is each Metadata
			@SuppressWarnings("unchecked")
			List<DataWatcher.Item<?>> list = (List<DataWatcher.Item<?>>) PacketReflection.getFieldValue(msg, "b");
			
			//If we match the entity requested
			if ( ((CraftEntity)e).getEntityId() == eid) {
				
				// DataWatcher.Item<?>
				// a = DataWatcherObject
				// b = <?> - the string,byte,integer,etc
				
				// we know 0 is the array index of the Entity Metadata byte, as per http://wiki.vg/Entity_metadata#Entity_Metadata_Format
				String type = list.get(0).b().getClass().getSimpleName();
				// NOTE: I've also been getting a float==the entity's health somehow in list.get(0).b(), possible MC bug or is sent by same packet
				// Thus, we gotta make sure it's a Byte, you get a disconnect and a ClassCastException otherwise
				if (type.equals("Byte")) {
					// Get the byte
					Byte databyte = (byte)list.get(0).b();
					//fire is 0x01 part of the byte, replace it in with binary OR
					databyte = (byte)(databyte | 0x01); 
					//Grab the DataWatcherObject and make a new DataWatcher.Item to replace in the list
					@SuppressWarnings({ "rawtypes", "unchecked" })
					DataWatcher.Item<Byte> dwib = new DataWatcher.Item( list.get(0).a(), databyte);
					list.set(0, dwib);
				}
			}
			//finally, change the value of b - the metadata itself
			PacketReflection.setValue(msg, "b", list);
		}
		// Now send the packet
		super.write(ctx, msg, promise);
	}
	@Override
	public void channelRead(ChannelHandlerContext c, Object m) throws Exception {
		super.channelRead(c, m);
	}
}
