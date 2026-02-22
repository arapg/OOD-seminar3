package se.kth.iv1350.model;

import se.kth.iv1350.dto.ItemDTO;
import se.kth.iv1350.dto.SaleStateDTO;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import se.kth.iv1350.util.Amount;

/**
 * Represents a single sale transaction.
 */
public class Sale {
    private LocalDateTime saleTimestamp;
    private List<SalesLineItem> items;
    private Amount runningTotalIncludingVAT;
    private Amount currentTotalVAT;
    private Receipt receipt; // According to diagram, Sale creates Receipt

    /**
     * Creates a new Sale instance.
     * Initializes the sale time, item list, and totals.
     */
    public Sale() {
        this.saleTimestamp = LocalDateTime.now(); // Set when sale starts
        this.items = new ArrayList<>();
        this.runningTotalIncludingVAT = new Amount(0, "SEK");
        this.currentTotalVAT = new Amount(0, "SEK");
        this.receipt = new Receipt(); // Create receipt object
    }

    /**
     * Sets the time of the sale. Typically called when the sale is initiated.
     */
    public void setTimeOfSale() {
        this.saleTimestamp = LocalDateTime.now();
    }
    
    /**
     * Gets the timestamp of when the sale was initiated.
     * @return The sale timestamp.
     */
    public LocalDateTime getSaleTimestamp() {
        return saleTimestamp;
    }

    /**
     * Gets the list of items currently in the sale.
     * @return A list of {@link SalesLineItem}s.
     */
    public List<SalesLineItem> getItems() {
        return new ArrayList<>(items); // Return a copy to prevent external modification
    }
    
    /**
     * Gets the current running total of the sale, including VAT.
     * @return The running total.
     */
    public Amount getRunningTotalIncludingVAT() {
        return runningTotalIncludingVAT;
    }

    /**
     * Gets the current total VAT accumulated for the sale.
     * @return The total VAT.
     */
    public Amount getCurrentTotalVAT() {
        return currentTotalVAT;
    }


    /**
     * Adds an item to the sale or increases its quantity if already present.
     * Updates the running total and VAT.
     * @param itemInfo The DTO of the item to add.
     * @param quantity The quantity of the item to add.
     * @return A {@link SaleStateDTO} representing the current state of the sale after adding the item.
     *         Returns null if itemInfo is null.
     */
    public SaleStateDTO addItemToSale(ItemDTO itemInfo, int quantity) {
        if (itemInfo == null) {
            return null; // Or handle as an invalid item scenario
        }

        SalesLineItem existingItem = findItem(itemInfo.getItemID());
        if (existingItem != null) {
            existingItem.increaseQuantity(quantity);
        } else {
            SalesLineItem newItemLine = new SalesLineItem(itemInfo, quantity);
            items.add(newItemLine);
        }
        
        calculateRunningTotalAndVAT(); // Recalculate totals
        
        return new SaleStateDTO(itemInfo, runningTotalIncludingVAT, currentTotalVAT);
    }

    private SalesLineItem findItem(String itemID) {
        for (SalesLineItem lineItem : items) {
            if (lineItem.getItem().getItemID().equals(itemID)) {
                return lineItem;
            }
        }
        return null;
    }
    
    /**
     * Calculates the running total and total VAT for all items currently in the sale.
     * This is a private helper method called after items are added/updated.
     */
    private void calculateRunningTotalAndVAT() {
        Amount newRunningTotal = new Amount(0, "SEK");
        Amount newTotalVAT = new Amount(0, "SEK");

        for (SalesLineItem lineItem : items) {
            Amount itemPrice = lineItem.getItem().getPrice();
            double vatRate = lineItem.getItem().getVatRate();
            int quantity = lineItem.getQuantity();

            Amount itemSubtotal = itemPrice.multiply(quantity);
            Amount itemVAT = itemPrice.multiply(vatRate).multiply(quantity);
            
            newRunningTotal = newRunningTotal.add(itemSubtotal.add(itemVAT));
            newTotalVAT = newTotalVAT.add(itemVAT);
        }
        this.runningTotalIncludingVAT = newRunningTotal;
        this.currentTotalVAT = newTotalVAT;
    }

    /**
     * Calculates the final total price of the sale (including VAT).
     * In this simple model, it's the same as the running total.
     * @return The total price.
     */
    public Amount calculateTotal() {
        // In a more complex system, this might apply sale-wide discounts
        return runningTotalIncludingVAT;
    }

    /**
     * Processes the payment for the sale.
     * @param amountTendered The amount of money tendered by the customer.
     * @return The change to be given back to the customer. Returns null if payment is insufficient.
     */
    public Amount makePayment(Amount amountTendered) {
        if (amountTendered == null || amountTendered.getValue() < runningTotalIncludingVAT.getValue()) {
            return null; // Insufficient payment
        }
        Amount change = amountTendered.subtract(runningTotalIncludingVAT);
        // The receipt is populated after payment in the controller or here
        this.receipt.populateReceipt(this, amountTendered, change);
        return change;
    }
    
    /**
     * Gets the receipt associated with this sale.
     * The receipt should be populated after payment.
     * @return The {@link Receipt} object.
     */
    public Receipt getReceipt() {
        return receipt;
    }
}
