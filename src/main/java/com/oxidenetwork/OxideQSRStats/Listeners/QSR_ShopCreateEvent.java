package com.oxidenetwork.OxideQSRStats.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.maxgamer.quickshop.Event.ShopCreateEvent;

import com.oxidenetwork.OxideQSRStats.OxideQSRStats;

public class QSR_ShopCreateEvent implements Listener {
	public QSR_ShopCreateEvent(OxideQSRStats pl) {
		OxideQSRStats.debug("QSR_ShopCreateEvent Registered");
	}
	
	@EventHandler
	public void onShopPurchaseEvent(ShopCreateEvent event) {
		OxideQSRStats.debug(event.getPlayer() + " created a shop");
	}
	
}
