# Project: CSE 241 Database Systems
## Joelle Dizon

## Description
Program for managing a product catalog, handling customer purchases, & generating sales reports for managers.

## To Execute the Program
* Compile from top-level directory: `javac -cp ojdbc11.jar jkd227/Capacity.java -d jkd227`
    * The .class file will be placed under the jar folder
* Create jar file: `jar cfmv jkd227.jar Manifest.txt Capacity.class`
* Run using: `java -jar jkd227.jar`


## Interface Descriptions

### Catalog Interface

* Browsing: View the entire catalog, filter by Items or Services, search for products by their name
* Purchasing Items: (login)
    * Individual Customers can purchase items (limited to one type of item per purchase).
    * Can use either credit card or installment plan payment methods.
    * Decrements item stock.
    * Includes measures to ensure items of stock <=0 cannot be bought.
* Purchasing Services: (login)
    * Business Customers can purchase services (limited to one type of service per purchase).
    * Pay only with bank account
    * Service stock does not decrement -- reaches -1 if and only if it is removed from the catalog (no longer offered)

### Customer Interface (Requires Login)

* Basic Customer Info: view email, full name, customer type, etc.
* Past Purchases & Total Expenses:
    * can view detailed purchase history (including date) and aggregated expenses 
* Manage Payment Methods
    * Individual: can add a new credit card --> must be NEW not in the table
    * Business: can add a new bank account 


### Manager Interface (Requires Login)

* Product Management: 
    * Add/Remove Products from the Catalog
* Product Management: 
    * Update the stock of products that still exist on the catalog
* Installment Plans: 
    * Populate new plans with terms & conditions and monthly interval
* Reporting:
    1. Most Purchased Product Report
    2. Revenue/Transactions for a Given Day
    3. Revenue/Transactions for a Given Month
    4. All Purchases Made


## Sample Workflow

1. Enter Customer Interface: `2`
2. Login as Individual: `Han Solo`
3. View Past Purchases: `2`
4. Return to Main Menu: `0`
5. Enter Catalog Interface: `1`
6. View Catalog & Choose Items-only View: `1` and `1`
7. Search for Item: `2` and type `Lightsaber`
8. Purchase Item: `3` `1`, login as `Han Solo`, then buy 20 BB-8 Droids!
9. View Past Purchases from Customer Interface & see it updated!
10. Login as Manager: `Yoda`, and check All Purchases Report: `9` to verify purchase.
11. Check daily report, monthly report.
12. Add product to catalog: `2`
13. Edit the product stock: `4`
14. Remove added product from catalog: `3`
15. Check most purchased product report: `6` (should be BB-8)!


## Assumptions: 
* Individuals can only buy items, businesses can only buy services!
* Managers cannot be customers
* Customers cannot edit their personal info
* New customers cannot be created (closed system)
* Bank number only contains numbers
* A product's "searchable description" indicates name
* Customers can only buy one type of item / service at a time. 
* Total cost not included in purchase tables because it is a derived amount 
    * Calculating when needed rather than storing
* Installment plans are not credit card based


## Sources:
* https://www.ibm.com/docs/en/db2-for-zos/12.0.0?topic=applications-retrieving-auto-generated-keys-insert-statement
* https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/TO_CHAR-datetime.html
