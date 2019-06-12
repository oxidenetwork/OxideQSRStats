package com.oxidenetwork.OxideQSRStats.Listeners;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Event.ShopSuccessPurchaseEvent;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Shop.ShopType;

import com.oxidenetwork.OxideQSRStats.OxideQSRStats;


public class QSR_ShopSuccessPurchaseEvent implements Listener {
	
	public QSR_ShopSuccessPurchaseEvent(OxideQSRStats pl) {
		OxideQSRStats.debug("QSR_ShopSuccessPurchaseEvent Registered");
	}
	
	@EventHandler
	public void onShopSuccessPurchaseEvent(ShopSuccessPurchaseEvent event) {
		OxideQSRStats.debug("ShopSuccessPurchaseEvent fired");
		if (!event.isCancelled()) { // only save it when the event is not cancelled
			Shop s = event.getShop();
			Player p = event.getPlayer();
			double taxed = event.getTax();
			if (s.getOwner().equals(p.getUniqueId())) {
	            taxed = 0;
	        }
			registerPurchase(s, p, event.getAmount(), taxed);
		} else {
			OxideQSRStats.debug("There was a purchase event, but it was set to chanceled");
		}
	}
	
	public void registerPurchase(Shop s, Player p, int quantity, double taxed) {
		String shopOwnerName = s.ownerName();
		UUID shopOwnerUUID = s.getOwner();
		String itemName = s.getItem().getType().name();
		double pricePiece = s.getPrice();
		double totalPrice = quantity * s.getPrice();
		String playerName = p.getName();
		UUID playerUUID = p.getUniqueId();
		boolean adminShop = s.isUnlimited();

		double tax = QuickShop.instance.getConfig().getDouble("tax");
		if (s.getOwner().equals(playerUUID)) {
            taxed = 0;
        }

		// Round the total price to two decimals
		totalPrice = Math.round(totalPrice * 100) / 100.00d;
		taxed = Math.round(taxed * 100) / 100.00d;
		
		if (s.getShopType() == ShopType.SELLING) {
			OxideQSRStats.getDatabaseHelper().insertIntoTransactions(shopOwnerName, shopOwnerUUID, itemName, pricePiece, totalPrice, taxed, tax, quantity, playerName, playerUUID, adminShop, "SELLING");
			String message = String.format("%s bought %s %s for $ %s from %s", playerName, String.valueOf(quantity), itemName, String.valueOf(totalPrice), shopOwnerName);
			OxideQSRStats.info(message);
		} else if (s.getShopType() == ShopType.BUYING) {
			OxideQSRStats.getDatabaseHelper().insertIntoTransactions(shopOwnerName, shopOwnerUUID, itemName, pricePiece, totalPrice, taxed, tax, quantity, playerName, playerUUID, adminShop, "BUYING");
			String message = String.format("%s sold %s %s for $ %s to %s", playerName, String.valueOf(quantity), itemName, String.valueOf(totalPrice), shopOwnerName);
			OxideQSRStats.info(message);
		}		
	}
	
}
