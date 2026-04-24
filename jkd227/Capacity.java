import java.sql.*;
import java.util.*;
import java.io.Console;

public class Capacity {

    public static void main(String[] args) {
        Console console = System.console();
        if (console == null) {
            System.err.println("Console not available.");
            System.exit(1);
        }

        String url = "jdbc:oracle:thin:@//rocordb01.cse.lehigh.edu:1522/cse241pdb";
        Connection conn = null;

        try { 
            Class.forName("oracle.jdbc.OracleDriver");

            boolean connected = false;
            String user = "";
            String password = "";

            while (!connected) {
                // Prompt for user ID and password
                user = console.readLine("Enter Oracle user id: ");
                char[] passwordChar = console.readPassword("Enter Oracle password for %s: ", user);
                password = new String(passwordChar);
                
                System.out.println("Connecting to Oracle database...");

                try {
                    conn = DriverManager.getConnection(url, user, password);
                    connected = true; // Connection success, exit loop
                    System.out.println("Connection successful!");
                } catch (SQLException e) {
                    if (e.getErrorCode() == 1017) { // invalid username or pw but don't exit completely
                        System.err.println("\nERROR: Invalid username or password. Please try again.\n");
                    } else {// throw to outer catch bloc
                        throw e; 
                    }
                }
            }
            try (Connection finalConn = conn) {

                String[] dinoArt = {
                    "\n\n========================= WELCOME TO LUSHOP! =========================",
                    "   ☆                ☆                   .     .",
                    "         ☆                      ☆       / `. .' \" ",
                    "                    .---.  <   > <   >  .---.",  
                    "     ☆          ☆   |   \\  \\ - ~ ~ - /  /   |",
                    "            _____          ..-~                 ~-..-~",
                    "          |     |   \\~~~\\.'                       `/~~~/",
                    "   ☆      ---------   \\__/                          \\__/",
                    "         .' O   \\     /              /       \\  \"",
                    "       (_____,   `._.'              |         }  \\/~~~/",
                    "         `----.       /       }   |        /    \\__/",
                    "     ☆         `-.    |     /     |       /       `. ,~~|",
                    "                 ~-.__|     /_ - ~ ^|     /- _      `..-'   ",
                    "    ☆        ☆         |     /        |     /   ~-.      `-. _ _ _",
                    "                       |_____|        |_____|       ~ - . _ _ _ _ _>"
                };

                for (String line : dinoArt) {
                    System.out.println(line);
                }

                // start main interfaces menu --------------------------------------
                while(true) {

                    System.out.println("\n\n================ LUSHOP MAIN MENU! ================");
                    System.out.println("1. Catalog Interface (Browse/Purchase)");
                    System.out.println("2. Customer Interface (Login req.)");
                    System.out.println("3. Manager Interface (Login req.)");
                    System.out.println("0. Exit Application");
                    System.out.println("==================================================");

                    String choice = console.readLine("Choose an interface: ").trim();

                    switch (choice) {
                        case "1":
                            viewCatalogInterface(conn, console);
                            break;
                        case "2":
                            customerInterface(conn, console);
                            break;
                        case "3":
                            int managerId = managerLogin(conn, console);
                            if (managerId != -1) {
                                managerInterface(conn, console, managerId);
                            }
                            break;
                        case "0":
                            System.out.println("Exiting the shop. Goodbye!");
                            return;
                        default:
                            System.out.println("Invalid option. Please try again.");
                    }
                }  
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Error: JDBC driver not loaded: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected Error: " + e.getMessage());
        }
    }


    // =========================================== FREQUENTLY USED METHODS ===========================================
    
    /**
     *  Customer login loop
     *  @return String array containing the customer's ID, name, and type for future use
     */
    private static String[] customerLogin(Connection conn, Console console) throws SQLException {
        int custId = -1;
        String custName = "";
        String custType = "";

        // Prompt for name as credentials
        System.out.println("\n●・○・●・○・●  Customer Login  ●・○・●・○・●");
        System.out.println("-- for existing customer, enter Han Solo (indiv.) or Imperial Logistics (bus.) \n");
        String name = console.readLine("Enter your name (or 0 to return): ").trim();

        if (name.equals("0")) {
            return null;
        }

        // individual login -------------------
        String indivSql = "SELECT cust_id, i_name FROM individual WHERE LOWER(i_name) = LOWER(?)";
        try (PreparedStatement ps = conn.prepareStatement(indivSql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    custId = rs.getInt("cust_id");
                    custName = rs.getString("i_name");
                    custType = "individual";
                }
            }
        }

        // business login -------------------
        if (custId == -1) {
            String busSql = "SELECT cust_id, b_name FROM business WHERE LOWER(b_name) = LOWER(?)";
            try (PreparedStatement ps = conn.prepareStatement(busSql)) {
                ps.setString(1, name);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        custId = rs.getInt("cust_id");
                        custName = rs.getString("b_name");
                        custType = "business";
                    }
                }
            }
        }

        // Return array of login info or null
        if (custId != -1) {
            System.out.println("Welcome, " + custName + "!");
            String[] login = new String[]{String.valueOf(custId), custName, custType};
            return login;
        } else {
            System.out.println("Login failed: customer does not exist.");
            return null;
        }

    }

    /**
     *  Manager login loop
     *  @return manager ID upon success, -1 otherwise
     */
    private static int managerLogin(Connection conn, Console console) throws SQLException {
        System.out.println("\n\n°。°。°。°。°。 Manager Login 。°。°。°。°。°");
        System.out.println("-- for existing manager, enter Yoda.\n");
        String name = console.readLine("Enter your name (or 0 to return): ").trim();

        if (name.equals("0")) {
            return -1;
        }

        // SQL to find the manager's ID
        String managerSql = "SELECT m_id, m_name FROM manager WHERE LOWER(m_name) = LOWER(?)";
        
        try (PreparedStatement ps = conn.prepareStatement(managerSql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int mId = rs.getInt("m_id");
                    String mName = rs.getString("m_name");
                    System.out.println("Welcome, Manager " + mName + "!");
                    return mId;
                } else {
                    System.out.println("Login failed: Manager of this name does not exist.");
                    return -1;
                }
            }
        }
    }


    // =========================================== INTERFACE METHODS ===========================================

    /**
     *  CATALOG INTERFACE - main menu
     *  Options: view catalog, search for products, purchase a product
     */
    private static void viewCatalogInterface(Connection conn, Console console) throws SQLException {
        while (true) {           
            System.out.println("\n\n═✿══✿══✿══✿═╡ CATALOG INTERFACE ╞═✿══✿══✿══✿═");
            System.out.println("1. View Catalog");
            System.out.println("2. Search for Products");
            System.out.println("3. Purchase a Product (Login required!)");
            System.out.println("0. Return to Main Menu");
            System.out.println("=============================================");

            String choice = console.readLine("Choose an option: ").trim();

            switch (choice) {
                case "1":
                    // Submenu for different viewing options
                    while (true) {
                        System.out.println("\n\n--- ❶ Catalog View Types ---");
                        System.out.println("1. View Items only");
                        System.out.println("2. View Services only");
                        System.out.println("3. View Entire Catalog");
                        System.out.println("0. Return to Catalog Menu");
                        String view = console.readLine("\nChoose a view option: ").trim();

                        switch (view) {
                            case "1": 
                                viewCatalog(conn, console, "item");
                                break;
                            case "2":
                                viewCatalog(conn, console, "service");
                                break;
                            case "3":
                                viewCatalog(conn, console, "all");
                                break;
                            case "0":
                                break;
                            default:
                                System.out.println("Invalid option. Please try again.");
                        }
                        break;
                    }
                    break;
                case "2":
                    searchProducts(conn, console);
                    break; 
                case "3":
                    purchaseProduct(conn, console);
                    break; 
                case "0":
                    System.out.println("Returning to Main Menu..");
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    /**
     * CUSTOMER INTERFACE - main menu
     * Send to login, then go to different interface based on customer type
     */
    private static void customerInterface(Connection conn, Console console) throws SQLException {
        String[] loginInfo = customerLogin(conn, console);

        // based on login info, customize interface towards individual or business
        if (loginInfo != null) {
            int custId = Integer.parseInt(loginInfo[0]);
            String custName = loginInfo[1];
            String custType = loginInfo[2];

            if (custType.equals("individual")) {
                indivInterface(conn, console, custId, custName);
            } else if (custType.equals("business")) {
                busInterface(conn, console, custId, custName);
            }
        }
    }

    private static void indivInterface(Connection conn, Console console, int custId, String custName) throws SQLException {
        while (true) {
            System.out.println("\n\n═✿══✿══✿══✿═╡ INDIVIDUAL CUSTOMER INTERFACE ╞═✿══✿══✿══✿═");
            System.out.println("Welcome back, " + custName + " (ID: " + custId + ")");
            System.out.println("1. View My Info");
            System.out.println("2. View Past Purchases");
            System.out.println("3. Manage Payment Methods");
            System.out.println("0. Logout and Return to Main Menu");
            System.out.println("=======================================================");

            String choice = console.readLine("Choose an option: ").trim();

            switch (choice) {
                case "1":
                    viewIndivInfo(conn, custId, custName);
                    console.readLine("\n□ Hit Enter to return to the previous menu: "); // cool trick
                    break;
                case "2":
                    viewIndivPurchases(conn, console, custId);
                    console.readLine("\n□ Hit Enter to return to the previous menu: ");
                    break;
                case "3":
                    managePaymentMethods(conn, console, custId, "individual");
                    console.readLine("\n□ Hit Enter to return to the previous menu: ");
                    break;
                case "0":
                    System.out.println("Logging out.");
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }

        }
    }

    private static void busInterface(Connection conn, Console console, int custId, String custName) throws SQLException {
        while (true) {
            System.out.println("\n\n═✿══✿══✿══✿═╡ BUSINESS CUSTOMER INTERFACE ╞═✿══✿══✿══✿═");
            System.out.println("Welcome back, " + custName + " (ID: " + custId + ")");
            System.out.println("1. View my Business Info");
            System.out.println("2. View Past Purchases");
            System.out.println("3. Manage Payment Methods");
            System.out.println("0. Logout and Return to Main Menu");
            System.out.println("=============================================");

            String choice = console.readLine("Choose an option: ").trim();

            switch (choice) {
                case "1":
                    viewBusInfo(conn, custId, custName);
                    console.readLine("\n□ Hit Enter to return to the previous menu: ");
                    break;
                case "2":
                    viewBusPurchases(conn, custId);
                    console.readLine("\n□ Hit Enter to return to the previous menu: ");
                    break;
                case "3":
                    managePaymentMethods(conn, console, custId, "business");
                    console.readLine("\n□ Hit Enter to return to the previous menu: ");
                    break;
                case "0":
                    System.out.println("Logging out.");
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    /**
     * MANAGER INTERFACE - main menu
     * note: Must login first before prompting with menu
     */
    private static void managerInterface(Connection conn, Console console, int mId) throws SQLException {
        while (true) {
            System.out.println("\n\n═✿══✿══✿══✿══✿═╡ MANAGER INTERFACE (ID: " + mId + ") ╞═✿══✿══✿══✿══✿═\n");
            System.out.println("--- Product Management ---");
            System.out.println("1. View Catalog (with all products)");
            System.out.println("2. Add a New Product (Item or Service)");
            System.out.println("3. Remove a Product");
            System.out.println("4. Edit the Product Stock");
            System.out.println("5. Add a New Installment Plan");
            System.out.println("\n--- Reporting Options ---");
            System.out.println("6. Report: Most Purchased");
            System.out.println("7. Report: Purchases on a Given Day");
            System.out.println("8. Report: Purchases for a Given Month");
            System.out.println("9. Report: All Purchases");
            System.out.println("0. Logout and Return to Main Menu");
            System.out.println("=================================================================");

            String choice = console.readLine("Choose an option: ").trim();

            try {
                switch (choice) {
                    case "1":
                        viewCatalog(conn, console, "all");
                        break;
                    case "2":
                        addProduct(conn, console, mId);
                        break;
                    case "3":
                        removeProduct(conn, console);
                        break;
                    case "4":
                        updateStock(conn, console);
                        break;
                    case "5":
                        addInstallmentPlan(conn, console, mId);
                        console.readLine("\n□ Hit Enter to return to the manager menu: ");
                        break;
                    case "6":
                        mostPurchasedReport(conn, console);
                        console.readLine("\n□ Hit Enter to return to the manager menu: ");
                        break;
                    case "7":
                        dailyPurchasesReport(conn, console);
                        console.readLine("\n□ Hit Enter to return to the manager menu: ");
                        break;
                    case "8":
                        monthlyPurchasesReport(conn, console);
                        console.readLine("\n□ Hit Enter to return to the manager menu: ");
                        break;
                    case "9":
                        allPurchasesReport(conn, console);
                        console.readLine("\n□ Hit Enter to return to the manager menu: ");
                        break;
                    case "0":
                        System.out.println("Logging out..");
                        return;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
            }
            
        }
    }


    // =========================================== MANAGER METHODS ===========================================

    // Option #1: Add a product to the catalog
    private static void addProduct(Connection conn, Console console, int mId) throws SQLException {
        viewCatalog(conn, console, "all");

        System.out.println("\n-✵-✵-✵-✵-✵-✵- Add New Product -✵-✵-✵-✵-✵-✵-");
        String name = console.readLine("Enter product name (or 0 to cancel): ").trim();
        if (name.equals("0")) {
            return;
        }
        if (name.equals("")) {
            System.out.println("ERROR: Product name must not be empty.\n");
            return;
        }
        String description = console.readLine("Enter product description (or 0 to cancel): ").trim();
        if (description.equals("0")) {
            return;
        }
        String vendor = console.readLine("Enter product vendor (or 0 to cancel): ").trim();
        if (vendor.equals("0")) {
            return;
        }

        double price = 0.0;
        while (price <= 0) {
            try {
                price = Double.parseDouble(console.readLine("Enter price: ").trim());
                if (price <= 0) {
                    System.out.println("ERROR: Price must be greater than zero.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid price format.");
            }
        }

        int stock = -1;
        String prodType = "";
        // Checking product type
        while (prodType.isEmpty()) {
            System.out.println("Is this an Item (I) or a Service (S)?");
            String typeInput = console.readLine("Enter I or S: ").trim().toUpperCase();
            if (typeInput.equals("I")) {
                prodType = "item";
                // Validating stock 
                while (stock < 0) {
                    try {
                        stock = Integer.parseInt(console.readLine("Enter initial stock quantity (>= 0): ").trim());
                        if (stock < 0) System.out.println("Stock cannot be negative.");
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid stock input.");
                        stock = -1;
                    }
                }
            } else if (typeInput.equals("S")) {
                prodType = "service";
                stock = 1; // Services have static stock
            } else {
                System.out.println("Invalid choice.");
            }
        }

        // Duration validation -- for services only!
        Integer duration = null;
        if (prodType.equals("service")) {
            while (duration == null || duration <= 0) {
                try {
                    duration = Integer.parseInt(console.readLine("Enter service duration in hours (> 0): ").trim());
                    if (duration <= 0) System.out.println("Duration must be greater than zero!");
                } catch (NumberFormatException e) {
                    System.out.println("Invalid duration input.");
                    duration = null;
                }
            }
        }

        conn.setAutoCommit(false); // BEGIN TRANSACTION
        try {
            // Insert into product table (all)
            String prodSql = "INSERT INTO product (prod_name, price, stock, description, m_id, vendor) VALUES (?, ?, ?, ?, ?, ?)";
            int prodId = -1;
            try (PreparedStatement ps = conn.prepareStatement(prodSql, new String[]{"PROD_ID"})) {
                ps.setString(1, name);
                ps.setDouble(2, price);
                ps.setInt(3, stock);
                ps.setString(4, description);
                ps.setInt(5, mId);
                ps.setString(6, vendor);
                ps.executeUpdate();

                // Verify by getting the ID
                // source: https://www.ibm.com/docs/en/db2-for-zos/12.0.0?topic=applications-retrieving-auto-generated-keys-insert-statement
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        prodId = rs.getInt(1);
                    } else {
                        throw new SQLException("Failed to obtain product ID after insertion.");
                    }
                }
            }

            // Insert into corresponding item or service table 
            if (prodType.equals("item")) {
                String itemSql = "INSERT INTO item (prod_id) VALUES (?)";
                try (PreparedStatement ps = conn.prepareStatement(itemSql)) {
                    ps.setInt(1, prodId);
                    ps.executeUpdate();
                }
                System.out.println("Item '" + name + "' successfully added with product ID: " + prodId);
            } else if (prodType.equals("service")) {
                String serviceSql = "INSERT INTO service (prod_id, duration) VALUES (?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(serviceSql)) {
                    ps.setInt(1, prodId);
                    ps.setInt(2, duration);
                    ps.executeUpdate();
                }
                System.out.println("Service '" + name + "' successfully added with product ID: " + prodId);
            }
            conn.commit();
        } catch (SQLException e) {
            // ROLLBACK
            conn.rollback();
            System.err.println("Failed to add product, cancelling: " + e.getMessage());
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // Option #2: Remove a product from the catalog VIEW
    private static void removeProduct(Connection conn, Console console) throws SQLException {
        System.out.println("\n-✵-✵-✵-✵-✵-✵- Remove Product from Catalog View -✵-✵-✵-✵-✵-✵-");
        viewCatalog(conn, console, "all"); // print catalog of existing items to begin

        int prodId = -1;
        String input = console.readLine("Enter the product ID to remove (or 0 to cancel): ").trim();

        if (input.equals("0")) {
            return;
        }

        try {
            prodId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format.");
            return;
        }

        // Check if product exists and is not already removed
        String existsSql = "SELECT prod_name, stock FROM product WHERE prod_id = ?";
        String prodName = null;
        int currStock = 2;
        try (PreparedStatement ps = conn.prepareStatement(existsSql)) {
            ps.setInt(1, prodId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    prodName = rs.getString("prod_name");
                    currStock = rs.getInt("stock");
                } else {
                    System.out.println("ERROR: ID not found.");
                    return;
                }
            }
        }

        // Item removed if stock is 0 or less, Service removed if stock is -1
        if (currStock == -1 || currStock == 0) {
            System.out.println("Product '" + prodName + "' is already marked as removed from view.");
            return;
        }

        String confirmation = console.readLine("Confirm removal of '" + prodName + "' (ID: " + prodId + ")? (Y/N): ").trim().toLowerCase();
        
        if (confirmation.equals("y")) {
            String updateSql = "UPDATE product SET stock = -1 WHERE prod_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setInt(1, prodId);
                int rowUpdated = ps.executeUpdate();
                if (rowUpdated > 0) {
                    System.out.println("Product '" + prodName + "' has been removed from the catalog.");
                } else {
                    System.out.println("ERROR: Failed to remove product.");
                }
            }
        } else {
            System.out.println("Removal cancelled.");
        }
        viewCatalog(conn, console, "all");
    }

    // Option #3: Add an Installment Plan 
    private static void addInstallmentPlan(Connection conn, Console console, int mId) throws SQLException {
        System.out.println("\n-✵-✵-✵-✵-✵-✵- Add New Installment Plan -✵-✵-✵-✵-✵-✵-");
        String terms = console.readLine("Enter a BRIEF terms & conditions (0 to cancel): ").trim();
        if (terms.equals("0")) {
            return;
        }
        if (terms.isEmpty()) {
            System.out.println("ERROR: Terms and conditions cannot be empty.");
            return;
        }

        int monthly = -1;
        while (monthly < 1 || monthly > 60) { 
            String input = console.readLine("Enter the monthly payment term in months (min: 1, max: 60): ").trim();
            if (input.equals("0")) {
                return;
            }
            try {
                monthly = Integer.parseInt(input);
                if (monthly < 1 || monthly > 60) {
                    System.out.println("ERROR: Term must be between 1 & 60 months.");
                }
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Invalid number.");
                monthly = -1; 
            }
        }

        // Insert new plan
        String insertSql = "INSERT INTO installment_plan (terms_and_cond, monthly, m_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, new String[]{"plan_id"})) {
            ps.setString(1, terms);
            ps.setInt(2, monthly);
            ps.setInt(3, mId);
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    System.out.println("New Installment Plan added with ID: " + rs.getInt(1) + ".");
                } else {
                    System.out.println("New Installment Plan added.");
                }
            }
        }
        viewInstallments(conn, console); // print list of installments to verify
    }

    // Option #4: Adjust stock
    private static void updateStock(Connection conn, Console console) throws SQLException {
        System.out.println("\n-✵-✵-✵-✵-✵-✵-Update Item Stock -✵-✵-✵-✵-✵-✵-");

        // Show existing items
        viewCatalog(conn, console, "item");
        
        int prodId = -1;
        while (prodId == -1) {
            String input = console.readLine("Enter the ID of the Item to update (or 0 to cancel): ").trim();
            if (input.equals("0")) return;
            
            try {
                prodId = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid product ID. Please enter an integer.");
                continue;
            }

            String checkSql = 
                "SELECT p.prod_name, p.stock, i.item_id " + "FROM product p JOIN item i ON p.prod_id = i.prod_id " + 
                "WHERE p.prod_id = ? AND p.stock != -1"; 
            // can only update stock that is not removed
            
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, prodId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String prodName = rs.getString("prod_name");
                        int currentStock = rs.getInt("stock");
                        System.out.printf("Selected Item: %s (Current Stock: %d)\n", prodName, currentStock);
                    } else {
                        System.out.println("ERROR: Product not found or removed (stock = -1).");
                        prodId = -1; // Reset to loop again
                    }
                }
            }
        }
        
        int newStock = -1;
        while (newStock < 0) {
            String input = console.readLine("Enter the new stock (must be >= 0): ").trim();
            try {
                newStock = Integer.parseInt(input);
                if (newStock < 0) {
                    System.out.println("ERROR: Stock quantity must be at least 0.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid format. Please enter a non-negative whole number.");
                newStock = -1;
            }
        }
        
        // Execute update
        String updateSql = "UPDATE product SET stock = ? WHERE prod_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setInt(1, newStock);
            ps.setInt(2, prodId);
            
            int change = ps.executeUpdate(); // verify if the stock was updated by capturing update value
            
            if (change > 0) {
                System.out.printf("Stock for Product (ID %d) updated to %d.\n", prodId, newStock);       
            } else {
                System.out.println("ERROR: Failed to update stock.");
            }
        } catch (SQLException e) {
            System.err.println("Database error while updating stock: " + e.getMessage());
        }

        console.readLine("\nUpdate complete. Hit Enter to view the updated catalog & return: ");
        viewCatalog(conn, console, "all");
    }

    // ========= REPORT METHODS
    private static void mostPurchasedReport(Connection conn, Console console) throws SQLException {
        System.out.println("\n-✵-✵-✵-✵-✵-✵- REPORT: Most Purchased Products -✵-✵-✵-✵-✵-✵-");

        // needed to UNION ALL purchases (combine individual & business types)
        String reportSql = 
            "WITH all_purchases AS (SELECT i.prod_id, ip.quantity FROM individual_purchase ip JOIN item i ON ip.item_id = i.item_id " +
            "UNION ALL " + "SELECT s.prod_id, bp.quantity FROM business_purchase bp JOIN service s ON bp.service_id = s.service_id) " +
            "SELECT p.prod_id, p.prod_name, SUM(ap.quantity) AS total_sold " +
            "FROM all_purchases ap JOIN product p ON ap.prod_id = p.prod_id " +
            "GROUP BY p.prod_id, p.prod_name " +
            "ORDER BY total_sold DESC";
        
        try (PreparedStatement ps = conn.prepareStatement(reportSql);
            ResultSet rs = ps.executeQuery()) {

            if (!rs.isBeforeFirst()) {
                System.out.println("No purchases recorded!");
                return;
            }

            System.out.println("---------------------------------------------------------------");
            System.out.printf("%-10s | %-40s | %s\n", "Prod ID", "Product Name", "Total Amount Sold");
            System.out.println("---------------------------------------------------------------");
            
            while (rs.next()) {
                System.out.printf("%-10d | %-40s | %d\n", 
                                  rs.getInt("prod_id"), 
                                  rs.getString("prod_name"), 
                                  rs.getInt("total_sold"));
            }
            System.out.println("---------------------------------------------------------------");
        }

    }

    private static void dailyPurchasesReport(Connection conn, Console console) throws SQLException {
        System.out.println("\n-✵-✵-✵-✵-✵-✵- REPORT: Total Purchases for a Given Day -✵-✵-✵-✵-✵-✵-\n");
        System.out.println("Samples: 2025-11-01, 2025-11-02, 2025-11-05\n");
        String dateInput = console.readLine("Enter the date (YYYY-MM-DD): ").trim();

        String reportSql = 
            "WITH daily_purchases AS (" +
            "    SELECT purchase_date, p.price * ip.quantity AS purchase_amount " +
            "    FROM individual_purchase ip JOIN item i ON ip.item_id = i.item_id JOIN product p ON i.prod_id = p.prod_id " +
            "    UNION ALL " +
            "    SELECT purchase_date, p.price * bp.quantity AS purchase_amount " +
            "    FROM business_purchase bp JOIN service s ON bp.service_id = s.service_id JOIN product p ON s.prod_id = p.prod_id " +
            ") " +
            "SELECT COUNT(*) AS total_transactions, COALESCE(SUM(purchase_amount), 0) AS total_daily_revenue " +
            "FROM daily_purchases " +
            "WHERE TRUNC(purchase_date) = TO_DATE(?, 'YYYY-MM-DD')"; // TRUNC TO IGNORE TIMESTAMP!
    
        try (PreparedStatement ps = conn.prepareStatement(reportSql)) {
            ps.setString(1, dateInput);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int totalTransactions = rs.getInt("total_transactions");
                    double totalRevenue = rs.getDouble("total_daily_revenue");
                    
                    System.out.println("------------------------------------------------");
                    System.out.println("Report for Date: " + dateInput);
                    System.out.println("------------------------------------------------");
                    System.out.printf("%-25s | %d\n", "Total Transactions", totalTransactions);
                    System.out.printf("%-25s | $%,.2f\n", "Total Revenue", totalRevenue);
                    System.out.println("------------------------------------------------");
                }
            }
        } catch (SQLException e) {
             System.err.println("SQL ERROR: " + e.getMessage());
        }
    }

    private static void monthlyPurchasesReport(Connection conn, Console console) throws SQLException {
        System.out.println("\n-✵-✵-✵-✵-✵-✵- REPORT: Total Purchases for a Given Month -✵-✵-✵-✵-✵-✵-");
        System.out.println("Sample: 2025-11\n");
        String monthInput = console.readLine("Enter the month and year (YYYY-MM): ").trim();

        String reportSql = 
            "WITH monthly_purchases AS (" +
            "SELECT ip.purchase_date, p.price * ip.quantity AS purchase_amount " + 
            "FROM individual_purchase ip JOIN item i ON ip.item_id = i.item_id JOIN product p ON i.prod_id = p.prod_id " +
            "UNION ALL " +
            "SELECT bp.purchase_date, p.price * bp.quantity AS purchase_amount " + 
            "FROM business_purchase bp JOIN service s ON bp.service_id = s.service_id JOIN product p ON s.prod_id = p.prod_id " +
            ") " +
            "SELECT COUNT(*) AS total_transactions, COALESCE(SUM(purchase_amount), 0) AS total_monthly_revenue " +
            "FROM monthly_purchases " +
            "WHERE TO_CHAR(purchase_date, 'YYYY-MM') = ?"; // convert datetime to char & truncate
            // source: https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/TO_CHAR-datetime.html

        try (PreparedStatement ps = conn.prepareStatement(reportSql)) {
            ps.setString(1, monthInput);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int totalTransactions = rs.getInt("total_transactions");
                    double totalRevenue = rs.getDouble("total_monthly_revenue");
                    
                    System.out.println("------------------------------------------------");
                    System.out.println("Report for Month: " + monthInput);
                    System.out.println("------------------------------------------------");
                    System.out.printf("%-25s | %d\n", "Total Transactions", totalTransactions);
                    System.out.printf("%-25s | $%,.2f\n", "Total Revenue", totalRevenue);
                    System.out.println("------------------------------------------------");
                }
            }
        } catch (SQLException e) {
             System.err.println("SQL Error: " + e.getMessage());
        }
    }

    private static void allPurchasesReport(Connection conn, Console console) throws SQLException {
        System.out.println("\n-✵-✵-✵-✵-✵-✵- REPORT: All Purchases Made -✵-✵-✵-✵-✵-✵-\n");

        String sql =
            "WITH all_transactions AS (" +
            "  SELECT ip.purchase_date, p.prod_name, ip.quantity, p.price, p.price * ip.quantity AS total_amnt," +
            "  i.i_name AS customer_name, 'Individual' AS type" + " FROM individual_purchase ip" +
            "  JOIN item it ON ip.item_id = it.item_id" + " JOIN product p ON it.prod_id = p.prod_id" + " JOIN individual i ON ip.cust_id = i.cust_id" +
            "  UNION ALL " +
            "  SELECT bp.purchase_date, p.prod_name, bp.quantity, p.price, p.price * bp.quantity AS total_amnt," +
            "  b.b_name AS customer_name, 'Business' AS type" +
            "  FROM business_purchase bp" +
            "  JOIN service s ON bp.service_id = s.service_id" +
            "  JOIN product p ON s.prod_id = p.prod_id" +
            "  JOIN business b ON bp.cust_id = b.cust_id" + ")" +
            "SELECT * FROM all_transactions ORDER BY purchase_date DESC"; // descending purchase date to put most recent first

        try (PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            if (!rs.isBeforeFirst()) { // return automatically if no purchases found
                System.out.println("No purchases found!");
                return;
            }

            System.out.printf("%-24s | %-30s | %-20s | %-40s | %-8s | %-10s | %s\n", 
                              "Date", "Customer Name", "Type", "Product", "Quantity", "Unit Price", "Total Sale");
            System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------------");

            while (rs.next()) {
                System.out.printf(
                    "%-24s | %-30s | %-20s | %-40s | %-8d | $%-9.2f | $%,.2f\n",
                    rs.getTimestamp("purchase_date").toString(),
                    rs.getString("customer_name"),
                    rs.getString("type"),
                    rs.getString("prod_name"),
                    rs.getInt("quantity"),
                    rs.getDouble("price"),
                    rs.getDouble("total_amnt")
                );
            }
        }
    }

    private static void viewInstallments(Connection conn, Console console) throws SQLException {
        System.out.println("\n===== INSTALLMENT PLANS =====");
            
        String plansSql = "SELECT plan_id, terms_and_cond, monthly FROM installment_plan ORDER BY plan_id";
        boolean found = false;

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(plansSql)) { // no input req.
            System.out.println("--------------------------------------------------------------------------------------------------");
            System.out.printf("%-10s | %-8s | %s\n", "Plan ID", "Term (Mths)", "Terms and Conditions");
            System.out.println("--------------------------------------------------------------------------------------------------");
                
            while (rs.next()) {
                String terms = rs.getString("terms_and_cond");
                System.out.printf("%-10d | %-8d | %s\n", rs.getInt("plan_id"), rs.getInt("monthly"), terms);
                found = true;
            }
            System.out.println("---------------------------------------------------------------------------------------------");
        }
        if (!found) {
            System.out.println("no installment plans found in the system.");
            return;
        }
    }




    // =========================================== INDIVIDUAL CUSTOMER METHODS ===========================================

    // Option #1: View Info
    private static void viewIndivInfo(Connection conn, int custId, String custName) throws SQLException {
        System.out.println("\n--- ➀ Viewing Personal Info ---");
        
        // Get email from the customer table
        String infoSql = "SELECT c.cust_email FROM customer c WHERE c.cust_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(infoSql)) {
            ps.setInt(1, custId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String email = rs.getString("cust_email");
                    System.out.println("Customer Type: Individual");
                    System.out.println("Name: " + custName);
                    System.out.println("Customer ID: " + custId);
                    System.out.println("Email: " + (email == null ? "No email given" : email));
                }
            }
        }
    }

    // Option #2: Viewing Past Purchase
    private static void viewIndivPurchases(Connection conn, Console console, int custId) {
        System.out.println("\n--- ➁ Viewing All Past Purchases ---");
        
        // Get purchase columns for the particular custId -- note: need to join with product table for base price!
        String purchaseSql = 
            "SELECT p.prod_name, ip.quantity, (p.price * ip.quantity) AS total_item_cost, ip.purchase_date, ip.card_id, ip.plan_id " +
            "FROM individual_purchase ip " +
            "JOIN item i ON ip.item_id = i.item_id " +
            "JOIN product p ON i.prod_id = p.prod_id " +
            "WHERE ip.cust_id = ? " +
            "ORDER BY ip.purchase_date DESC";

        double totalExpenses = 0.0; // accumulator
            
        try (PreparedStatement ps = conn.prepareStatement(purchaseSql)) {
            ps.setInt(1, custId);
            try (ResultSet rs = ps.executeQuery()) {
                // No purchases made yet
                if (!rs.isBeforeFirst()) {
                    System.out.println("No past purchases found for this individual!");
                    return;
                }
                
                System.out.println("----------------------------------------------------------------------------------------------------");
                System.out.printf("%-30s %-10s %-15s %-10s %-10s %s\n", "Product Name", "Quantity", "Total Cost", "Card ID", "Plan ID", "Date");
                System.out.println("----------------------------------------------------------------------------------------------------");
                
                while (rs.next()) {
                    String prodName = rs.getString("prod_name");
                    int quantity = rs.getInt("quantity");
                    double totalCost = rs.getDouble("total_item_cost");
                    String date = rs.getString("purchase_date");
                    Integer cardId = rs.getObject("card_id", Integer.class);
                    Integer planId = rs.getObject("plan_id", Integer.class);
                    
                    String cardString = cardId != null ? String.valueOf(cardId) : "N/A";
                    String planString = planId != null ? String.valueOf(planId) : "N/A";
                    
                    totalExpenses += totalCost;
                    System.out.printf("%-30s %-10d $%-14.2f %-10s %-10s %s\n", prodName, quantity, totalCost, cardString, planString, date);
                }
                System.out.println("----------------------------------------------------------------------------------------------------");
                System.out.printf("%-41s $%,.2f\n", "→→ TOTAL EXPENSES:", totalExpenses); 
            }
        } catch (SQLException e) {
            System.err.println("Database error while trying to view purchases: " + e.getMessage());
        }
    }

    // Option #3: Manage Payment Methods -- NOTE THIS IS FOR BOTH INDIVIDUAL AND BUSINESS
    private static void managePaymentMethods(Connection conn, Console console, int custId, String customerType) throws SQLException {
        while (true) {
            System.out.println("\n\n============ ➂ MANAGE PAYMENT METHODS ============");

            // Split into different paths based on customer type & redirect
            System.out.println("\n--- Add New Payment Method ---");
            if (customerType.equals("individual")) {
                System.out.println("1. Add New Credit Card");
                System.out.println("0. Return to previous menu");
            } else if (customerType.equals("business")) {
                System.out.println("1. Add New Bank Account");
                System.out.println("0. Return to previos menu");
            }

            String choice = console.readLine("\nChoose an option: ").trim();

            if (choice.equals("0")) {
                return;
            }

            try {
                if (customerType.equals("individual")) {
                    if (choice.equals("1")) {
                        addNewCreditCard(conn, console);
                    } else {
                         System.out.println("Invalid option");
                    }
                } else if (customerType.equals("business")) {
                    if (choice.equals("1")) {
                        addNewBankAccount(conn, console);
                    } else {
                        System.out.println("Invalid option");
                    }
                }
            } catch (SQLException e) {
                System.err.println("\nFailed to add payment method: " + e.getMessage());
            }
        }
    }
    // Helper method to add a new credit card
    private static void addNewCreditCard(Connection conn, Console console) throws SQLException {
        System.out.println("\n--- Add New Credit Card ---");
        String cardNum = console.readLine("Enter your 12-digit credit card number: ").trim();

        if (cardNum.length() != 12 || !cardNum.matches("\\d+")) { // if length does not match or not all digits
            System.out.println("ERROR: Card number must have exactly 12 digits.");
            return;
        }

        // Verify if card already exists in the system
        String checkSql = "SELECT card_id FROM credit WHERE card_num = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, cardNum);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Card is already registered (ID: " + rs.getInt("card_id") + ")!");
                    return;
                }
            }
        }

        // If not, insert a new card
        String insertSql = "INSERT INTO credit (card_num) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, new String[]{"card_id"})) {
            ps.setString(1, cardNum);
            ps.executeUpdate();
        }
        String verifySql = "SELECT card_id FROM credit WHERE card_num = ?";
        try (PreparedStatement ps2 = conn.prepareStatement(verifySql)) {
            ps2.setString(1, cardNum);

            try (ResultSet rs = ps2.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Card successfully added! (ID: " + rs.getInt("card_id"));
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR: "  + e.getMessage());
        }
    }
    // Helper method to add new bank
    private static void addNewBankAccount(Connection conn, Console console) throws SQLException {
        System.out.println("\n--- Add New Bank Account ---");
        String bankNum = console.readLine("Enter bank account number: ").trim();
        String bankName = console.readLine("Enter bank name: ").trim();

        if (bankNum.isEmpty() || bankName.isEmpty() || bankNum.length() > 30) {
            System.out.println("ERROR: Bank number/name cannot be empty. Max bank number is 30 characters.");
            return;
        }

        // Check if bank number already exists
        String checkSql = "SELECT bank_id FROM bank_account WHERE bank_num = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, bankNum);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Bank account number already registered (ID: " + rs.getInt("bank_id") + ").");
                    return;
                }
            }
        }

        // Insert new bank account
        String insertSql = "INSERT INTO bank_account (bank_num, bank_name) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, new String[]{"BANK_ID"})) {
            ps.setString(1, bankNum);
            ps.setString(2, bankName);
            ps.executeUpdate();
        }
        // Verify additon with select statement
        String verifySql = "SELECT bank_id FROM bank_account WHERE bank_num = ?";
        try (PreparedStatement ps2 = conn.prepareStatement(verifySql)) {
            ps2.setString(1, bankNum);

            try (ResultSet rs = ps2.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Bank account successfully added! (ID: " + rs.getInt("bank_id"));
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR: Verification failed - "  + e.getMessage());
        }
    }

    // =========================================== BUSINESS CUSTOMER METHODS ===========================================
    
    // Option #1: View Info
    private static void viewBusInfo(Connection conn, int custId, String customerName) throws SQLException {
        System.out.println("\n-- Viewing Business Info ---");
        
        String infoSql = 
            "SELECT c.cust_email, b.industry " +
            "FROM customer c JOIN business b ON c.cust_id = b.cust_id " +
            "WHERE c.cust_id = ?";
            
        try (PreparedStatement ps = conn.prepareStatement(infoSql)) {
            ps.setInt(1, custId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String email = rs.getString("cust_email");
                    String industry = rs.getString("industry");
                    
                    System.out.println("Customer Type: Business");
                    System.out.println("Business Name: " + customerName);
                    System.out.println("Customer ID: " + custId);
                    System.out.println("Industry: " + (industry == null ? "Space" : industry));
                    System.out.println("Email: " + (email == null ? "No email added" : email));
                }
            }
        }
    }

    // Option #2: View Purchases
    private static void viewBusPurchases(Connection conn, int custId) {
        System.out.println("\n--- ➁ Viewing All Past Purchases ---");

        String purchaseSql = 
            "SELECT p.prod_name, bp.quantity, (p.price * bp.quantity) AS total_service_cost, bp.purchase_date, bp.bank_id " +
            "FROM business_purchase bp " +
            "JOIN service s ON bp.service_id = s.service_id " + "JOIN product p ON s.prod_id = p.prod_id " +
            "WHERE bp.cust_id = ? " + "ORDER BY bp.purchase_date DESC";

        double totalExpenses = 0.0; // accumulator

        try (PreparedStatement ps = conn.prepareStatement(purchaseSql)) {
            ps.setInt(1, custId);
            try (ResultSet rs = ps.executeQuery()) {
                // No purchases made so far
                if (!rs.isBeforeFirst()) {
                    System.out.println("No past purchases found for this business!");
                    return;
                }
                
                System.out.println("--------------------------------------------------------------------------------------------");
                System.out.printf("%-30s %-10s %-15s %-10s %s\n", "Product Name", "Quantity", "Total Cost", "Bank ID", "Date");
                System.out.println("--------------------------------------------------------------------------------------------");
                
                while (rs.next()) {
                    String prodName = rs.getString("prod_name");
                    int quantity = rs.getInt("quantity");
                    double totalCost = rs.getDouble("total_service_cost");
                    String date = rs.getString("purchase_date");
                    int bankId = rs.getInt("bank_id");
                    System.out.printf("%-30s %-10d $%-14.2f %-10d %s\n", prodName, quantity, totalCost, bankId, date);
                    totalExpenses += totalCost;
                }
                System.out.println("--------------------------------------------------------------------------------------------");
                System.out.printf("%-40s $%,.2f\n", "→→ TOTAL EXPENSES:", totalExpenses); 
            }
        } catch (SQLException e) {
            System.err.println("Error while trying to view purchases: " + e.getMessage());
        }
        
    }


    // =========================================== CATALOG METHODS ===========================================
    
    // Option #1: View the catalog
    private static void viewCatalog(Connection conn, Console console, String viewType) throws SQLException {
        
        String viewSql = "";

        // Varying view based on keyword
        if (viewType.equals("item")) {
            System.out.println("\n--❊-- Catalog View: In-Stock Items Only --❊--");

            viewSql = "SELECT p.prod_id, p.prod_name, p.price, p.stock, s.duration, i.item_id, s.service_id " + 
                      "FROM product p " + "LEFT JOIN item i ON p.prod_id = i.prod_id " +
                      "LEFT JOIN service s ON p.prod_id = s.prod_id " +
                      "WHERE i.item_id IS NOT NULL AND p.stock > 0 ORDER BY p.prod_name";

        } else if (viewType.equals("service")) {
            System.out.println("\n--❊-- Catalog View: Services Only --❊--");

            // note: for a service, a stock of -1 means it's no longer offered!
            viewSql = "SELECT p.prod_id, p.prod_name, p.price, p.stock, s.duration, i.item_id, s.service_id " + 
                      "FROM product p " + "LEFT JOIN item i ON p.prod_id = i.prod_id " +
                      "LEFT JOIN service s ON p.prod_id = s.prod_id " +
                      "WHERE s.service_id IS NOT NULL AND p.stock <> -1 ORDER BY p.prod_name";
        } else {
            System.out.println("\n--❊-- Catalog View: All Items/Services --❊--");

            viewSql = "SELECT p.prod_id, p.prod_name, p.price, p.stock, s.duration, i.item_id, s.service_id " + 
                      "FROM product p " + "LEFT JOIN item i ON p.prod_id = i.prod_id " +
                      "LEFT JOIN service s ON p.prod_id = s.prod_id " +
                      "WHERE (i.item_id IS NOT NULL AND p.stock > 0) OR (s.service_id IS NOT NULL AND p.stock <> -1) " +
                      "ORDER BY p.prod_name";
        }

        try (PreparedStatement ps = conn.prepareStatement(viewSql)) {
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("-------------------------------------------------------------------------------------------------------");
                System.out.printf("%-6s | %-10s | %-40s | %-10s | %-6s | %s\n", "ID", "Type", "Product Name", "Price", "Stock", "Duration (hours)");
                System.out.println("-------------------------------------------------------------------------------------------------------");
            
                while (rs.next()) {
                    int id = rs.getInt("prod_id");
                    String name = rs.getString("prod_name");
                    double price = rs.getDouble("price");
                    int stock = rs.getInt("stock");
                    String duration = rs.getString("duration");

                    boolean isItem = rs.getInt("item_id") > 0;
                    boolean isServ = rs.getInt("service_id") > 0;
                    String type = "?";

                    // Checking for type to avoid incorrect or null values in duration & stock
                    if (isServ) {
                        type = "service";
                    } else if (isItem) {
                        type = "item";
                    } else {
                        continue;
                    }

                    String stockString = type.equals("item") ? String.valueOf(stock) : "N/A";
                    String durationString = type.equals("service") && duration != null ? String.valueOf(duration) : "N/A";

                    System.out.printf("%-6d | %-10s | %-40s | $%-9.2f | %-6s | %s\n", id, type, name, price, stockString, durationString);
                    System.out.println("-------------------------------------------------------------------------------------------------------");
                }
            }
        }
        console.readLine("\n□ Hit Enter to continue: ");
    }

    // Option #2: Search for products
    private static void searchProducts(Connection conn, Console console) throws SQLException {
        System.out.println("\n--- ❷ Searching Products (by name) ---");
        System.out.println("[ for testing, type 'Lightsaber' ]\n");

        String searchTerm = console.readLine("Enter product name to search (or 0 to return): ").trim();

        if (searchTerm.equals("0")) {
            return;
        }

        String searchSql = "SELECT p.prod_id, p.prod_name, p.price, p.stock, s.duration, i.item_id, s.service_id, p.description " +
                           "FROM product p " + "LEFT JOIN item i ON p.prod_id = i.prod_id " +
                           "LEFT JOIN service s ON p.prod_id = s.prod_id " +
                           "WHERE LOWER(p.prod_name) = LOWER(?)";
        
        try (PreparedStatement ps = conn.prepareStatement(searchSql)) {
            ps.setString(1, searchTerm);

            try (ResultSet rs = ps.executeQuery()) {
                // IF ITEM OR SERVICE DOES NOT EXIST
                if (!rs.isBeforeFirst()) {
                    System.out.println("\n Whoops! No products found matching the name: '" + searchTerm + "'.");
                    return;
                }

                System.out.println("--------------------------------------------------------------------------------------------");
                System.out.printf("%-6s | %-10s | %-30s | %-10s | %-6s | %s\n", "ID", "Type", "Product Name", "Price", "Stock", "Duration (hours)");
                System.out.println("--------------------------------------------------------------------------------------------");
                
                while (rs.next()) {
                    int id = rs.getInt("prod_id");
                    String name = rs.getString("prod_name");
                    double price = rs.getDouble("price");
                    int stock = rs.getInt("stock");
                    String duration = rs.getString("duration");

                    // Checking whether product is item or service
                    boolean isItem = rs.getInt("item_id") > 0;
                    boolean isService = rs.getInt("service_id") > 0;
                    String type = "?";

                    if (isItem) {
                        type = "item";
                    } else if (isService) {
                        type = "service";
                    } else {
                        continue; 
                    }
                    
                    String stockString = type.equals("item") ? String.valueOf(stock) : "N/A";
                    String durationString = type.equals("service") && duration != null ? String.valueOf(duration) : "N/A";
                    
                    System.out.printf("%-6d | %-10s | %-30s | $%-9.2f | %-6s | %s\n", id, type, name, price, stockString, durationString);
                }
                System.out.println("--------------------------------------------------------------------------------------------");
            }
        }
        console.readLine("\n□ Hit Enter to return to the previous menu: ");
    }

    // Option #3: Purchase a product :0
    // basically redirects, not much logic here
    private static void purchaseProduct(Connection conn, Console console) throws SQLException {
        while (true) {
            System.out.println("\n---------- ❸ Purchase a Product! ----------");
            System.out.println("1. Purchase an Item (Individuals ONLY)");
            System.out.println("2. Purchase a Service (Businesses ONLY)");
            System.out.println("0. Return to Catalog Menu\n");
            String purchaseChoice = console.readLine("Choose purchase type: ").trim();

            if (purchaseChoice.equals("1")) {
                purchaseItem(conn, console);
                return;
            } else if (purchaseChoice.equals("2")) {
                purchaseService(conn, console);
                return;
            } else if (purchaseChoice.equals("0")) {
                return;
            } else {
                System.out.println("Invalid option. Please try again.");
            }
        }
    }
 
    // Option #3a: Purchase an Item
    private static void purchaseItem(Connection conn, Console console) throws SQLException {
        System.out.println("\n\n✧⋄⋆⋅⋆⋄✧⋄⋆⋅⋆⋄✧✧⋄⋆⋅⋆⋄✧⋄ Purchase Items Here! ✧⋄⋆⋅⋆⋄✧⋄✧⋄⋆⋅⋆⋄✧⋄⋆⋅⋆⋄✧");

        // Login first to enforce items & service check
        String[] loginInfo = customerLogin(conn, console);
        if (loginInfo == null) return;

        int custId = Integer.parseInt(loginInfo[0]);
        String custType = loginInfo[2];
        
        // CASE #1: Customer is a Business.
        if (!custType.equals("individual")) {
            System.out.println("ERROR: Only Individuals can purchase Items! Rerouting to previous menu...");
            return;
        }

        // CASE #2: Individuals
        int itemId = -1;
        int prodId = -1;
        String prodName = "";
        double base_price = 0.0;
        int stock = 0;
        String description = ""; 

        // 1. Confirm selected item is valid
        while (itemId == -1) {
            System.out.println("\n[ ex: 6004 for BB-8 Droid ]");
            String input = console.readLine("Enter the Product ID for the Item you want to purchase (or 0 to cancel): ").trim();
            if (input.equals("0")) {
                return;
            }
            try {
                int prodIdInput = Integer.parseInt(input);
                String fetchSql = "SELECT i.item_id, p.prod_id, p.prod_name, p.price, p.stock, p.description " +
                                  "FROM item i JOIN product p ON i.prod_id = p.prod_id " +
                                  "WHERE p.prod_id = ?";

                // Get item from the table
                try (PreparedStatement ps = conn.prepareStatement(fetchSql)) {
                    ps.setInt(1, prodIdInput);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            if (rs.getInt("stock") <= 0) {
                                System.out.println("ERROR: Item is currently out of stock.");
                            } else {
                                itemId = rs.getInt("item_id");
                                prodId = rs.getInt("prod_id"); // for stock update
                                prodName = rs.getString("prod_name");
                                base_price = rs.getDouble("price");
                                stock = rs.getInt("stock");
                                description = rs.getString("description");
                                
                                System.out.println("Selected: " + prodName + ", Base Price: $" + base_price + ", Stock: " + stock);
                            }
                        } else {
                            System.out.println("ERROR: Invalid product ID OR product is not an item.");
                        }
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Please enter a valid integer for Item ID.");
            }
        }

        // 2. Confirm quantity is valid
        int quantity = -1;
        while (quantity <= 0) {
            String input = console.readLine("Enter quantity: ").trim();
            try {
                quantity = Integer.parseInt(input);
                if (quantity <= 0) {
                    System.out.println("ERROR: Quantity must be > 0.");
                } else if (quantity > stock) {
                    System.out.printf("ERROR: Out of bounds. Stock: %d.\n", stock);
                    quantity = -1; 
                }
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Please enter a valid quantity.");
            }
        }

        double totalCost = base_price * quantity;
        double monthlyPayment = 0.0;

        // 3. Payment method
        System.out.println("\n--- Payment Method ---");
        System.out.println("1. Credit card");
        System.out.println("2. Installments");
        String paymentChoice = console.readLine("Choose payment: ").trim();

        Integer cardId = null;
        Integer planId = null;
        String paymentDetails = "";
        String paymentType = "";
        boolean valid = false;

        if (paymentChoice.equals("1")) { // Credit Card
            paymentType = "Credit Card";
            System.out.println("[ ex: 123456780000 ]\n");
            String cardInput = console.readLine("Enter the credit card number (or 0 to cancel): ").trim();

            if (cardInput.equals("0")) {
                return;
            }
            
            // Look up card ID
            String cardSql = "SELECT card_id FROM credit WHERE card_num = ?";
            try (PreparedStatement ps = conn.prepareStatement(cardSql)) {
                ps.setString(1, cardInput);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        cardId = rs.getInt("card_id");
                        paymentDetails = "Credit Card";
                        valid = true;
                    } else {
                        System.out.println("ERROR: Credit card number not found. Cancelling purchase.");
                    }
                }
            }
        } else if (paymentChoice.equals("2")) { // Installments
            paymentType = "Installment Plan";
            System.out.println("\n===== INSTALLMENT PLANS =====");
            
            String plansSql = "SELECT plan_id, terms_and_cond, monthly FROM installment_plan ORDER BY plan_id";
            boolean found = false;

            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(plansSql)) { // no input req.
                System.out.println("--------------------------------------------------------------------------------------------------");
                System.out.printf("%-10s | %-8s | %s\n", "Plan ID", "Term (Mnths)", "Terms and Conditions");
                System.out.println("--------------------------------------------------------------------------------------------------");
                
                while (rs.next()) {
                    String terms = rs.getString("terms_and_cond");
                    System.out.printf("%-10d | %-8d | %s\n", rs.getInt("plan_id"), rs.getInt("monthly"), terms);
                    found = true;
                }
                System.out.println("---------------------------------------------------------------------------------------------");
            }
            if (!found) {
                System.out.println("no installment plans in the system. Cancelling purchase.");
                return;
            }

            String planInput = console.readLine("Enter the Plan ID you would like to use: ").trim();
            try {
                int planIdInput = Integer.parseInt(planInput);
                
                // Verify plan ID
                String verifySql = "SELECT plan_id, terms_and_cond, monthly FROM installment_plan WHERE plan_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(verifySql)) {
                    ps.setInt(1, planIdInput);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            planId = rs.getInt("plan_id");
                            int termMonths = rs.getInt("monthly");
                            // Calculate monthly payment
                            monthlyPayment = totalCost / termMonths; 
                            paymentDetails = rs.getString("terms_and_cond");
                            valid = true;
                        } else {
                            System.out.println("ERROR: Invalid plan ID. Cancelling purchase.");
                        }
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Invalid input for plan ID. Cancelling purchase.");
            }
        } else {
            System.out.println("Invalid payment. Purchase cancelled.");
        }
        // just in case lol
        if (!valid) return;

        System.out.println("\nYou selected:");
        System.out.printf("\t\tItem: %s\n", prodName);
        System.out.printf("\t\tBase Price: $%.2f\n", base_price);
        System.out.printf("\t\tQuantity: %d\n", quantity);

        if (paymentChoice.equals("1")) {
             System.out.printf("\t\tPayment Method: Credit Card\n");
        } else {
            System.out.printf("\t\tPayment Method: Installment Plan (ID %d)\n", planId);
            System.out.printf("\t\tMonthly Payment: $%.2f\n", monthlyPayment);
        }

        String confirm = console.readLine("\nConfirm purchase? (Y/N): ").trim().toLowerCase();

        if (confirm.equals("y")) {
            conn.setAutoCommit(false); // BEGIN TRANSACTION!
            try {
                String purchaseInsertSql = 
                    "INSERT INTO individual_purchase (purchase_date, quantity, item_id, cust_id, card_id, plan_id) " +
                    "VALUES (SYSDATE, ?, ?, ?, ?, ?)"; 
                try (PreparedStatement ps = conn.prepareStatement(purchaseInsertSql)) {
                    ps.setInt(1, quantity);
                    ps.setInt(2, itemId);
                    ps.setInt(3, custId);
                    
                    if (cardId != null) { // Card num is null if plan was used
                        ps.setInt(4, cardId);
                        ps.setNull(5, Types.INTEGER); 
                    } else {
                        ps.setNull(4, Types.INTEGER);
                        ps.setInt(5, planId); 
                    }
                    ps.executeUpdate();
                }
                // UPDATE THE STOCK -----------------
                String stockUpdateSql = "UPDATE product SET stock = stock - ? WHERE prod_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(stockUpdateSql)) {
                    ps.setInt(1, quantity);
                    ps.setInt(2, prodId);
                    ps.executeUpdate();
                }
                // COMMIT TRANSACTION ---------------
                conn.commit(); 
                System.out.println("\nPurchase recorded successfully!");
            } catch (SQLException e) {
                conn.rollback(); // ROLLBACK ROLLBACK
                System.err.println("Purchase failed due to database error. Rolling back: " + e.getMessage());
            } finally {
                conn.setAutoCommit(true); // End transaction REGARDLESS
            }
        } else {
            System.out.println("Purchase cancelled.");
        }
        console.readLine("\n□ Hit Enter to return to previous menu: ");
    }

    // Option #3b: Purchase a Service
    private static void purchaseService(Connection conn, Console console) throws SQLException {
        System.out.println("\n✧⋄⋆⋅⋆⋄✧⋄⋆⋅⋆⋄✧✧⋄⋆⋅⋆⋄✧⋄ Purchase Services Here! ✧⋄⋆⋅⋆⋄✧⋄✧⋄⋆⋅⋆⋄✧⋄⋆⋅⋆⋄✧");

        // Login first to enforce items & service check
        String[] loginInfo = customerLogin(conn, console);
        if (loginInfo == null) return;

        int custId = Integer.parseInt(loginInfo[0]);
        String custType = loginInfo[2];
        
        // CASE #1: Customer is an Individual.
        if (!custType.equals("business")) {
            System.out.println("ERROR: Only Businesses can purchase Services! Rerouting to previous menu...");
            return;
        }

        // CASE #2: Businesses
        int serviceId = -1;
        String prodName = "";
        double base_price = 0.0;

        // 1. Confirm service exists
        while (serviceId == -1) {
            System.out.println("\n[ ex: 6012 for Starship Repair ]");
            String input = console.readLine("Enter the Product ID for the Service (or 0 to cancel): ").trim();
            if (input.equals("0")) {
                return;
            }
            try {
                int prodIdInput = Integer.parseInt(input);
                String fetchSql = 
                    "SELECT s.service_id, p.prod_name, p.price " +
                    "FROM service s JOIN product p ON s.prod_id = p.prod_id " +
                    "WHERE (p.stock <> -1 AND p.prod_id = ?)";
                try (PreparedStatement ps = conn.prepareStatement(fetchSql)) {
                    ps.setInt(1, prodIdInput);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            serviceId = rs.getInt("service_id");
                            prodName = rs.getString("prod_name");
                            base_price = rs.getDouble("price");
                            System.out.printf("Selected: %s , Price: $%.2f\n", prodName, base_price);
                        } else {
                            System.out.println("ERROR: Invalid product ID or product is not a Service.");
                        }
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Please enter a valid integer for Product ID.");
            }
        }

        // 2. Confirm quantity
        int quantity = -1;
        while (quantity <= 0) {
            String input = console.readLine("Enter quantity (e.g., 1, 2): ").trim();
            try {
                quantity = Integer.parseInt(input);
                if (quantity <= 0) {
                    System.out.println("ERROR: Quantity must be greater than zero.");
                }
                // note: no out of bounds because services don't have a stock (unless they are no longer offered)
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Please enter a valid integer for quantity.");
            }
        }

        double totalCost = base_price * quantity;

        // 3. Verify Payment
        Integer bankId = null;
        String bankInput = console.readLine("Enter your bank account number (e.g. 100014 for Droid Bank): ").trim();
        
        String bankSql = "SELECT bank_id FROM bank_account WHERE bank_num = ?";
        try (PreparedStatement ps = conn.prepareStatement(bankSql)) {
            ps.setString(1, bankInput);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    bankId = rs.getInt("bank_id");
                } else {
                    System.out.println("ERROR: Bank account number not found. Cancelling purchase.");
                    return;
                }
            }
        }

        System.out.println("\nYou selected:");
        System.out.printf("\t\tService: %s\n", prodName);
        System.out.printf("\t\tPrice: $%.2f (per unit time)\n", base_price);
        System.out.printf("\t\tQuantity: %d\n", quantity);
        System.out.printf("\t\tTotal Cost: $%.2f\n", totalCost);
        // secret bank number because why not
        System.out.printf("Payment: Bank Account (XXX-%s)\n", bankInput.substring(bankInput.length() - 4));

        String confirm = console.readLine("Confirm purchase? (Y/N): ").trim().toLowerCase();

        if (confirm.equals("y")) {
            conn.setAutoCommit(false); // BEGIN TRANSACTION
            try {
                String purchaseSql = 
                    "INSERT INTO business_purchase (purchase_date, quantity, service_id, cust_id, bank_id) " +
                    "VALUES (SYSDATE, ?, ?, ?, ?)"; 
                
                try (PreparedStatement ps = conn.prepareStatement(purchaseSql)) {
                    ps.setInt(1, quantity);
                    ps.setInt(2, serviceId);
                    ps.setInt(3, custId);
                    ps.setInt(4, bankId);
                    ps.executeUpdate();
                }
                conn.commit(); // COMMIT
                System.out.println("\nPurchase recorded successfully!");
            } catch (SQLException e) {
                conn.rollback(); // ROLLBACK
                System.err.println("Purchase failed due to database error. Rolling back: " + e.getMessage());
            } finally {
                conn.setAutoCommit(true); // END
            }
        }
        console.readLine("\n□ Hit Enter to return to the previous menu: ");
    }
}