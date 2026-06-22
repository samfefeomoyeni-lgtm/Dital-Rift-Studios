import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FinanceTracker {

    // ─── Transaction Model ───────────────────────────────────────────────────────

    enum Type { INCOME, EXPENSE }

    static class Transaction {
        private static int nextId = 1;

        int id;
        String description;
        double amount;
        Type type;
        String category;
        LocalDate date;

        Transaction(String description, double amount, Type type, String category) {
            this.id          = nextId++;
            this.description = description;
            this.amount      = amount;
            this.type        = type;
            this.category    = category;
            this.date        = LocalDate.now();
        }

        @Override
        public String toString() {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return String.format("[%d] %-20s | %-10s | %-12s | $%8.2f | %s",
                    id, description, type, category, amount, date.format(fmt));
        }
    }

    // ─── Finance Tracker Logic ───────────────────────────────────────────────────

    private final List<Transaction> transactions = new ArrayList<>();

    public void addTransaction(String desc, double amount, Type type, String category) {
        if (amount <= 0) {
            System.out.println("  ✗ Amount must be greater than zero.");
            return;
        }
        transactions.add(new Transaction(desc, amount, type, category));
        System.out.printf("  ✓ %s added: %s ($%.2f)%n", type, desc, amount);
    }

    public boolean deleteTransaction(int id) {
        return transactions.removeIf(t -> t.id == id);
    }

    public double getTotalIncome() {
        return transactions.stream()
                .filter(t -> t.type == Type.INCOME)
                .mapToDouble(t -> t.amount)
                .sum();
    }

    public double getTotalExpenses() {
        return transactions.stream()
                .filter(t -> t.type == Type.EXPENSE)
                .mapToDouble(t -> t.amount)
                .sum();
    }

    public double getBalance() {
        return getTotalIncome() - getTotalExpenses();
    }

    public void printSummary() {
        System.out.println("\n  ╔═══════════════════════════════╗");
        System.out.println("  ║       FINANCIAL SUMMARY       ║");
        System.out.println("  ╠═══════════════════════════════╣");
        System.out.printf( "  ║  Total Income  : $%10.2f  ║%n", getTotalIncome());
        System.out.printf( "  ║  Total Expenses: $%10.2f  ║%n", getTotalExpenses());
        System.out.println("  ╠═══════════════════════════════╣");
        double balance = getBalance();
        System.out.printf( "  ║  Net Balance   : $%10.2f  ║%n", balance);
        System.out.println("  ╚═══════════════════════════════╝");
        System.out.println(balance >= 0
                ? "  ✓ You're in the green!\n"
                : "  ✗ Warning: you're spending more than you earn.\n");
    }

    public void printTransactions() {
        if (transactions.isEmpty()) {
            System.out.println("  No transactions recorded yet.");
            return;
        }
        System.out.println("\n  " + "─".repeat(75));
        System.out.printf("  %-4s %-20s | %-10s | %-12s | %-10s | %s%n",
                "ID", "Description", "Type", "Category", "Amount", "Date");
        System.out.println("  " + "─".repeat(75));
        for (Transaction t : transactions) {
            System.out.println("  " + t);
        }
        System.out.println("  " + "─".repeat(75) + "\n");
    }

    public void printByCategory() {
        if (transactions.isEmpty()) {
            System.out.println("  No transactions recorded yet.");
            return;
        }
        Map<String, Double> categoryTotals = new LinkedHashMap<>();
        for (Transaction t : transactions) {
            categoryTotals.merge(t.category, t.amount, Double::sum);
        }
        System.out.println("\n  ── Spending by Category ────────────────");
        categoryTotals.forEach((cat, total) ->
                System.out.printf("  %-15s : $%.2f%n", cat, total));
        System.out.println();
    }

    // ─── Menu & Entry Point ──────────────────────────────────────────────────────

    public static void main(String[] args) {
        FinanceTracker tracker = new FinanceTracker();
        Scanner scanner = new Scanner(System.in);

        // Seed with a few sample transactions
        tracker.addTransaction("Monthly Salary",   3500.00, Type.INCOME,  "Salary");
        tracker.addTransaction("Freelance Work",    800.00, Type.INCOME,  "Freelance");
        tracker.addTransaction("Rent",             1200.00, Type.EXPENSE, "Housing");
        tracker.addTransaction("Groceries",         250.00, Type.EXPENSE, "Food");
        tracker.addTransaction("Netflix",            15.00, Type.EXPENSE, "Entertainment");

        System.out.println("\n  ====================================");
        System.out.println("    Personal Finance Tracker — Java  ");
        System.out.println("  ====================================\n");
        System.out.println("  (Loaded 5 sample transactions to get you started)\n");

        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("  Enter choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    System.out.print("  Description : ");
                    String desc = scanner.nextLine();

                    System.out.print("  Amount      : $");
                    double amount;
                    try {
                        amount = Double.parseDouble(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("  ✗ Invalid amount.\n");
                        break;
                    }

                    System.out.print("  Type (1=Income / 2=Expense): ");
                    Type type = scanner.nextLine().trim().equals("1") ? Type.INCOME : Type.EXPENSE;

                    System.out.print("  Category    : ");
                    String category = scanner.nextLine();

                    tracker.addTransaction(desc, amount, type, category);
                }
                case "2" -> tracker.printTransactions();
                case "3" -> tracker.printSummary();
                case "4" -> tracker.printByCategory();
                case "5" -> {
                    System.out.print("  Enter transaction ID to delete: ");
                    try {
                        int id = Integer.parseInt(scanner.nextLine().trim());
                        boolean removed = tracker.deleteTransaction(id);
                        System.out.println(removed
                                ? "  ✓ Transaction #" + id + " deleted.\n"
                                : "  ✗ Transaction #" + id + " not found.\n");
                    } catch (NumberFormatException e) {
                        System.out.println("  ✗ Invalid ID.\n");
                    }
                }
                case "6" -> {
                    System.out.println("  Goodbye! Stay on budget. 👋\n");
                    running = false;
                }
                default -> System.out.println("  ✗ Invalid choice. Please enter 1–6.\n");
            }
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println("  ┌──────────────────────────────┐");
        System.out.println("  │  1. Add Transaction          │");
        System.out.println("  │  2. View All Transactions    │");
        System.out.println("  │  3. View Summary             │");
        System.out.println("  │  4. View by Category         │");
        System.out.println("  │  5. Delete Transaction       │");
        System.out.println("  │  6. Exit                     │");
        System.out.println("  └──────────────────────────────┘");
    }
}
