package com.oxidenetwork.OxideQSRStats.Listeners;

import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.maxgamer.quickshop.Shop.ShopPurchaseEvent;
import org.maxgamer.quickshop.Shop.ShopType;

import com.oxidenetwork.OxideQSRStats.OxideQSRStats;


public class QSR_ShopPurchaseEvent implements Listener {
	private OxideQSRStats plugin;
	
	public QSR_ShopPurchaseEvent(OxideQSRStats pl) {
		plugin = pl;
		OxideQSRStats.debug("QSR_ShopPurchaseEvent Registered");
	}
	
	@EventHandler
	public void onShopPurchaseEvent(ShopPurchaseEvent event) {
		String shopOwnerName = event.getShop().ownerName();
		UUID shopOwnerUUID = event.getShop().getOwner();
		String itemName = event.getShop().getItem().getType().name();
		double pricePiece = event.getShop().getPrice();
		double totalPrice = event.getAmount() * event.getShop().getPrice();
		int quantity = event.getAmount();
		String playerName = event.getPlayer().getName();
		UUID playerUUID = event.getPlayer().getUniqueId();
		boolean adminShop = event.getShop().isUnlimited();

		if (event.getShop().getShopType() == ShopType.SELLING) {
			plugin.getDatabaseHelper().insertIntoSales(shopOwnerName, shopOwnerUUID, itemName, pricePiece, totalPrice, quantity, playerName, playerUUID, adminShop);
			String message = String.format("%s bought %d %s for $ %d from %s", playerName, quantity, itemName, totalPrice, shopOwnerName);
			OxideQSRStats.info(message);
		} else if (event.getShop().getShopType() == ShopType.BUYING) {
			plugin.getDatabaseHelper().insertIntoPurchases(shopOwnerName, shopOwnerUUID, itemName, pricePiece, totalPrice, quantity, playerName, playerUUID, adminShop);
			String message = String.format("%s sold %d %s for $ %d to %s", playerName, quantity, itemName, totalPrice, shopOwnerName);
			OxideQSRStats.info(message);
		}
	}
	
}
