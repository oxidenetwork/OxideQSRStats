package com.oxidenetwork.OxideQSRStats.Listeners;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Shop.ShopPurchaseEvent;
import org.maxgamer.quickshop.Shop.ShopType;

import com.oxidenetwork.OxideQSRStats.OxideQSRStats;


public class QSR_ShopPurchaseEvent implements Listener {
	private OxideQSRStats plugin;
	
	public QSR_ShopPurchaseEvent(OxideQSRStats pl) {
		plugin = pl;
		OxideQSRStats.debug("QSR_ShopPurchaseEvent Registered");
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onShopPurchaseEvent(ShopPurchaseEvent event) {
		if (!event.isCancelled()) { // only save it when the event is not cancelled
			Shop s = event.getShop();
			Player p = event.getPlayer();
			
			String shopOwnerName = s.ownerName();
			UUID shopOwnerUUID = s.getOwner();
			String itemName = s.getItem().getType().name();
			double pricePiece = s.getPrice();
			double totalPrice = event.getAmount() * s.getPrice();
			int quantity = event.getAmount();
			String playerName = p.getName();
			UUID playerUUID = p.getUniqueId();
			boolean adminShop = s.isUnlimited();
			
			double tax = QuickShop.instance.getConfig().getDouble("tax");
			double taxPrice = totalPrice * tax;
			if (s.getOwner().equals(playerUUID)) {
	            tax = 0;
	            taxPrice = 0;
	        }
			
	
			if (s.getShopType() == ShopType.SELLING) {
				plugin.getDatabaseHelper().insertIntoTransactions(shopOwnerName, shopOwnerUUID, itemName, pricePiece, totalPrice, taxPrice, tax, quantity, playerName, playerUUID, adminShop, "SELLING");
				String message = String.format("%s bought %s %s for $ %s from %s", playerName, String.valueOf(quantity), itemName, String.valueOf(totalPrice), shopOwnerName);
				OxideQSRStats.info(message);
			} else if (s.getShopType() == ShopType.BUYING) {
				plugin.getDatabaseHelper().insertIntoTransactions(shopOwnerName, shopOwnerUUID, itemName, pricePiece, totalPrice, taxPrice, tax, quantity, playerName, playerUUID, adminShop, "BUYING");
				String message = String.format("%s sold %s %s for $ %s to %s", playerName, String.valueOf(quantity), itemName, String.valueOf(totalPrice), shopOwnerName);
				OxideQSRStats.info(message);
			}
		}
	}
	
}
