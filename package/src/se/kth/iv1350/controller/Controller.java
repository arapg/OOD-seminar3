package se.kth.iv1350.controller;

import se.kth.iv1350.util.Amount;
import se.kth.iv1350.dto.*;
import se.kth.iv1350.integration.*;
import se.kth.iv1350.model.*;


/**
 * The application controller that handles all calls from the view to the model and integration layers.
 */
public class Controller {
    private InventorySystem invSys;
    private AccountingSystem accSys;
    private DiscountDatabase discountDB; // Present but not used in core flow
    private Printer printer;
    private Register register;
    private Sale currentSale;

    /**
     * Creates a new Controller instance.
     * @param invSys The inventory system handler.
     * @param accSys The accounting system handler.
     * @param discountDB The discount database handler.
     * @param printer The printer handler.
     * @param register The cash register handler.
     */
    public Controller(InventorySystem invSys, AccountingSystem accSys, DiscountDatabase discountDB, Printer printer, Register register) {
        this.invSys = invSys;
        this.accSys = accSys;
        this.discountDB = discountDB;
        this.printer = printer;
        this.register = register;
    }

    /**
     * Starts a new sale. This must be called before any items can be added.
     */
    public void startSale() {
        this.currentSale = new Sale();
        // The Sale constructor already sets the time, but the diagram shows setTimeOfSale()
        // If Sale's constructor didn't set it, or if it needs to be reset:
        // this.currentSale.setTimeOfSale(); 
    }

    /**
     * Enters an item into the current sale.
     * @param itemID The ID of the item to enter.
     * @param quantity The quantity of the item.
     * @return A {@link SaleStateDTO} representing the current state of the sale,
     *         or null if the item ID is invalid or no sale has been started.
     */
    public SaleStateDTO enterItem(String itemID, int quantity) {
        if (currentSale == null) {
            // System.out.println("Error: No sale started."); // Not allowed to print from controller for errors
            return null; // Indicate error, view should handle this
        }
        ItemDTO itemInfo = invSys.retrieveItemInfo(itemID);
        if (itemInfo == null) {
            // System.out.println("Error: Item ID " + itemID + " not found.");
            return null; // Indicate item not found
        }
        return currentSale.addItemToSale(itemInfo, quantity);
    }

    /**
     * Ends the current sale and returns the total price.
     * @return The total price (including VAT) of the sale as an {@link Amount}.
     *         Returns null if no sale is active.
     */
    public Amount endSale() {
        if (currentSale == null) {
            return null;
        }
        return currentSale.calculateTotal();
    }

    /**
     * Processes the payment for the current sale.
     * Updates external systems, prints a receipt, and updates the register.
     * @param amountTendered The amount paid by the customer.
     * @return The change to be given to the customer as an {@link Amount}.
     *         Returns null if no sale is active or payment is insufficient.
     */
    public Amount enterPayment(Amount amountTendered) {
        if (currentSale == null) {
            return null;
        }
        Amount change = currentSale.makePayment(amountTendered);
        if (change == null) {
            // Insufficient payment, or other issue in makePayment
            return null;
        }

        accSys.registerSale(currentSale);
        invSys.updateInventory(currentSale);
        
        // The amount paid to register is the total sale price, not amountTendered
        Amount amountPaidForSale = currentSale.getRunningTotalIncludingVAT();
        register.updateRegister(amountPaidForSale); 

        Receipt receipt = currentSale.getReceipt(); // Receipt was populated in Sale.makePayment
        printer.printReceipt(receipt);
        
        return change;
    }

    // Method from class diagram, not used in Seminar 3 required flow
    /**
     * Signals a discount request for the current customer.
     * (Not implemented for Seminar 3 required flow).
     * @param customerID The ID of the customer requesting a discount.
     * @return The total price after discount, or null.
     */
    public Amount signalDiscountCustomer(String customerID) {
        if (currentSale == null) {
            return null;
        }
        // Object discountData = discountDB.fetchDiscount(customerID);
        // currentSale.applyDiscount(discountData); // Assuming Sale has applyDiscount
        // return currentSale.calculateTotal();
        System.out.println("LOG: signalDiscountCustomer called. (Not implemented for Seminar 3 scope)");
        return currentSale.calculateTotal(); // Return current total as placeholder
    }
}