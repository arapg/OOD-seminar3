
package integration;

import util.Amount;
import dto.ItemDTO;
import model.Sale;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles communication with an external inventory system (simulated).
 */
public class InventorySystem {
    private Map<String, ItemDTO> inventory = new HashMap<>();

    /**
     * Creates a new InventorySystem and populates it with some sample items.
     */
    public InventorySystem() {
        // Sample items based on the example output
        inventory.put("abc123", new ItemDTO("abc123", "BigWheel Oatmeal",
                "BigWheel Oatmeal 500g, whole grain oats, high fiber, gluten free",
                new Amount(29.90, "SEK"), 0.06));
        inventory.put("def456", new ItemDTO("def456", "YouGoGo Blueberry",
                "YouGoGo Blueberry 240g, low sugar youghurt, blueberry flavour",
                new Amount(14.90, "SEK"), 0.06));
        // Add another item with different VAT for testing if needed
        inventory.put("ghi789", new ItemDTO("ghi789", "Luxury Chocolate",
                "Dark chocolate 70%",
                new Amount(50.00, "SEK"), 0.12));
    }

    /**
     * Retrieves item information based on its ID.
     * @param itemID The ID of the item to retrieve.
     * @return The {@link ItemDTO} if found, otherwise null.
     */
    public ItemDTO retrieveItemInfo(String itemID) {
        return inventory.get(itemID);
    }

    /**
     * Updates the inventory system after a sale is completed.
     * (Currently a placeholder).
     * @param sale The completed sale information.
     */
    public void updateInventory(Sale sale) {
        // In a real system, this would decrement stock levels
        System.out.println("LOG: Inventory system notified of sale. Items sold:");
        sale.getItems().forEach(itemLine -> 
            System.out.println("LOG: - " + itemLine.getItem().getName() + ", Qty: " + itemLine.getQuantity())
        );
    }
}