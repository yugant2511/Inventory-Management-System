import comparators.*;
import java.util.*;
import model.Product;
import transaction.Transaction;

public class InventoryManagementSystem {

    private HashSet<Product> productSet = new HashSet<>();
    private LinkedList<Transaction> transactionHistory = new LinkedList<>();
    private Stack<Product> undoStack = new Stack<>();
    private Queue<Product> lowStockQueue = new LinkedList<>();

    private static final int LOW_STOCK_LIMIT = 10;
    private Scanner sc = new Scanner(System.in);

    
    public void start() {
        int choice;

        do {
            System.out.println("\n=== INVENTORY MANAGEMENT SYSTEM ===");
            System.out.println("1. Add Product");
            System.out.println("2. Update Quantity");
            System.out.println("3. View Products (Sorted)");
            System.out.println("4. Search Products");
            System.out.println("5. Low Stock Alerts");
            System.out.println("6. Transaction History");
            System.out.println("7. Inventory Statistics");
            System.out.println("8. Undo Last Update");
            System.out.println("9. Exit");
            System.out.print("Enter your choice: ");

            choice = sc.nextInt();
            sc.nextLine(); 

            switch (choice) {
                case 1 -> addProduct();
                case 2 -> updateQuantity();
                case 3 -> viewSorted();
                case 4 -> searchProduct();
                case 5 -> showLowStock();
                case 6 -> showTransactions();
                case 7 -> showStatistics();
                case 8 -> undo();
                case 9 -> System.out.println("Exiting system...");
                default -> System.out.println("Invalid choice!");
            }
        } while (choice != 9);
    }

   
    private void addProduct() {
        System.out.println("\n=== ADD NEW PRODUCT ===");

        System.out.print("Enter SKU: ");
        String sku = sc.nextLine();

        System.out.print("Enter Name: ");
        String name = sc.nextLine();

        System.out.print("Enter Category: ");
        String category = sc.nextLine();

        System.out.print("Enter Price: ");
        double price = sc.nextDouble();

        System.out.print("Enter Quantity: ");
        int qty = sc.nextInt();

        Product product = new Product(sku, name, category, price, qty);

        if (productSet.add(product)) {
            transactionHistory.addFirst(
                    new Transaction("ADD: " + sku + " - " + name + " (Qty: " + qty + ")")
            );

            System.out.println(" Product added successfully!");

            if (qty < LOW_STOCK_LIMIT) {
                lowStockQueue.add(product);
                System.out.println(" Low stock alert for " + sku + "!");
            }
        } else {
            System.out.println(" Product already exists!");
        }
    }

   
    private void updateQuantity() {
        System.out.print("\nEnter SKU to update: ");
        String sku = sc.nextLine();

        for (Product p : productSet) {
            if (p.getSku().equalsIgnoreCase(sku)) {

                System.out.print("Enter new quantity: ");
                int newQty = sc.nextInt();

                // Save for undo
                undoStack.push(new Product(
                        p.getSku(), p.getName(), p.getCategory(),
                        p.getPrice(), p.getQuantity()
                ));

                int oldQty = p.getQuantity();
                p.setQuantity(newQty);

                transactionHistory.addFirst(
                        new Transaction("UPDATE: " + sku +
                                " - Quantity changed from " + oldQty + " to " + newQty)
                );

                System.out.println(" Quantity updated successfully!");
                return;
            }
        }
        System.out.println(" Product not found!");
    }

    
    private void viewSorted() {
        System.out.print("\nSort by (sku/price/value/name): ");
        String criteria = sc.nextLine();

        List<Product> list = new ArrayList<>(productSet);

        switch (criteria.toLowerCase()) {
            case "sku" -> Collections.sort(list);
            case "price" -> list.sort(new PriceComparator());
            case "value" -> list.sort(new ValueComparator());
            case "name" -> list.sort(new NameComparator());
            default -> {
                System.out.println("Invalid sort option!");
                return;
            }
        }

        System.out.println("\n=== PRODUCTS SORTED BY " + criteria.toUpperCase() + " ===");
        System.out.printf("%-10s %-20s %-15s %-10s %-8s %-12s\n",
                "SKU", "Name", "Category", "Price", "Qty", "Value");
        System.out.println("-".repeat(85));

        for (Product p : list) {
            System.out.printf("%-10s %-20s %-15s ₹%-9.2f %-8d ₹%-11.2f\n",
                    p.getSku(), p.getName(), p.getCategory(),
                    p.getPrice(), p.getQuantity(), p.getInventoryValue());
        }
    }

    
    private void searchProduct() {
        System.out.print("Enter category to search: ");
        String category = sc.nextLine();

        productSet.stream()
                .filter(p -> p.getCategory().equalsIgnoreCase(category))
                .forEach(System.out::println);
    }

    private void showLowStock() {
        System.out.println("\n=== LOW STOCK ALERTS ===");

        if (lowStockQueue.isEmpty()) {
            System.out.println("No low stock items!");
            return;
        }

        int i = 1;
        for (Product p : lowStockQueue) {
            System.out.println(i++ + ". " + p.getSku() + " - " +
                    p.getName() + " (Current Stock: " + p.getQuantity() + ")");
        }
    }

    private void showTransactions() {
        System.out.print("\nEnter number of transactions to view: ");
        int count = sc.nextInt();

        System.out.println("\n=== LAST " + count + " TRANSACTIONS ===");

        transactionHistory.stream()
                .limit(count)
                .forEach(System.out::println);
    }

    private void showStatistics() {
        System.out.println("\n=== INVENTORY STATISTICS ===");

        double totalValue = 0;
        Map<String, Double> categoryValue = new HashMap<>();
        Map<String, Integer> categoryCount = new HashMap<>();

        for (Product p : productSet) {
            double value = p.getInventoryValue();
            totalValue += value;

            categoryValue.put(
                    p.getCategory(),
                    categoryValue.getOrDefault(p.getCategory(), 0.0) + value
            );
            categoryCount.put(
                    p.getCategory(),
                    categoryCount.getOrDefault(p.getCategory(), 0) + 1
            );
        }

        System.out.println("Total Products: " + productSet.size());
        System.out.println("Total Inventory Value: ₹" + totalValue);

        System.out.println("\nCategory-wise Breakdown:");
        for (String cat : categoryValue.keySet()) {
            double val = categoryValue.get(cat);
            double percent = (val / totalValue) * 100;

            System.out.printf("• %s: %d products, Value: ₹%.2f (%.1f%%)\n",
                    cat, categoryCount.get(cat), val, percent);
        }
    }

    private void undo() {
        if (undoStack.isEmpty()) {
            System.out.println("No operation to undo!");
            return;
        }

        Product prev = undoStack.pop();
        for (Product p : productSet) {
            if (p.getSku().equals(prev.getSku())) {
                p.setQuantity(prev.getQuantity());
                System.out.println("\n Last update undone!");
                System.out.println("Quantity for " + p.getSku() +
                        " reverted to " + p.getQuantity());
                return;
            }
        }
    }

    public static void main(String[] args) {
        new InventoryManagementSystem().start();
    }
}
