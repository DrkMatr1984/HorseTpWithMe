/*
	Copyright (c) 2015 TheMrJezza
	
	To any person obtaining a copy of this software and associated documentation 
	files (the "Software"). You do not have permission to use, copy, modify, merge, 
	publish, distribute, sublicense, and/or sell copies of the Software. The 
	Software is subject to the following conditions:

	The above copyright notice shall be included in all copies or substantial portions
	of the Software.
	
	If you have obtained permission to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software please note the following:

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
	SOFTWARE.
 */

package au.TheMrJezza.HorseTpWithMe;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class AnimalTeleportEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private LivingEntity entity;
	private Player rider;
	private Location loc;
	private Location cLoc;
	private boolean cancelled;

	public AnimalTeleportEvent(LivingEntity entity, Player rider, Location destination) {
		this.entity = entity;
		this.rider = rider;
		this.loc = destination;
		this.cLoc = entity.getLocation();
	}

	public Player getRider() {
		return this.rider;
	}

	public LivingEntity getEntity() {
		return this.entity;
	}

	public Location getDestination() {
		return this.loc;
	}

	public Location getFrom() {
		return this.cLoc;
	}

	public boolean isCancelled() {
		return this.cancelled;
	}

	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}
	
	public void setCancelled() {
		this.setCancelled(true);
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
