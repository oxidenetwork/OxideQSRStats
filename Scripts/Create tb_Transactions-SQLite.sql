CREATE TABLE `tb_Transactions` (
    ID 							INTEGER PRIMARY KEY AUTOINCREMENT
  , ShopOwnerName 	TEXT
  , ShopOwnerUUID 	TEXT
  , ItemName 				TEXT
  , PiecePrice 			DOUBLE
  , TotalPrice 			DOUBLE
  , TaxPrice 				DOUBLE
  , Tax 						DOUBLE
  , Quantity 				INTEGER
  , PlayerName 			TEXT
  , PlayerUUID 			TEXT
  , AdminShop 			INTEGER
  , Action 					TEXT
)