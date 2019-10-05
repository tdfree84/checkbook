package TFCB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * CBReport is used to analyze transactions for the program.
 * @author Tyler Freese
 */
public class CBReport {
    
    /**
     * Analysis is used to gather statistics on each month.
     * 
     * @author Tyler Freese
     */
    public static class Analysis {
        
        int month = 0; // Which month it is. Format: YYYYMM01
        CBMain.Transaction bigE = new CBMain.Transaction(0.0, "Jan 01 2000", "+", "None", "None"); // Biggest expense
        CBMain.Transaction bigI = new CBMain.Transaction(0.0, "Jan 01 2000", "+", "None", "None"); // Greatest income
        int count = 0; // How many trans per month
        double income = 0.0, expense = 0.0; // Total income and expense for this month
        double profit = 0.0; // Profit for the month
        Map<String, Helpers.Pair> categories = new HashMap<>(); // Where money went (food, entmt, fuel,...) AND count(*) each
        
        /**
         * Calculates the following statistics about a group of transactions. Only
         * referenced by monthlyAnalysis in class Analytics.
         * Total amounts for each category (food, fuel, need, entm, and income)
         * Frequency of each category
         * @param transactions Transaction list to analyze
         */
        public Analysis(ArrayList<CBMain.Transaction> transactions) {
            setMonth(transactions.get(0));
            
            double food = 0.0, need = 0.0, entm = 0.0, incm = 0.0, fuel = 0.0;
            int foodCount = 0, needCount = 0, entmCount = 0, incmCount = 0, fuelCount = 0;
            
            // Iterating through transactions
            for (int i = 0; i < transactions.size(); i++) {
                this.count++; // Adding one transaction
                
                // Adding total income and expense
                if (transactions.get(i).plusminus.equals("-")) { this.expense += -1*transactions.get(i).amount; }
                else { this.income += transactions.get(i).amount; }
                
                // Testing for greatest expense and income
                if (transactions.get(i).amount < bigE.amount) { bigE = transactions.get(i); }
                if (transactions.get(i).amount > bigI.amount) { bigI = transactions.get(i); }
                
                // Assign each transaction's amount to appropriate category
                switch (transactions.get(i).type) {
                case "food":
                    food+=-1*transactions.get(i).amount;
                    foodCount++;
                    break;
                case "need":
                    need+=-1*transactions.get(i).amount;
                    needCount++;
                    break;
                case "entertainment":
                    entm+=-1*transactions.get(i).amount;
                    entmCount++;
                    break;
                case "income":
                    incm+=transactions.get(i).amount;
                    incmCount++;
                    break;
                default:
                    fuel+=-1*transactions.get(i).amount;
                    fuelCount++;
                    break;
                }
            
            } // End for iterating through transactions
            
            // Setting profit
            this.profit = this.income - this.expense;
            
            // Setting categories
            Helpers.Pair<Double, Integer> x;
            x = new Helpers.Pair<>();
            x.setF(food);
            x.setS(foodCount);
            categories.put("food", x);
            
            x = new Helpers.Pair<>();
            x.setF(need);
            x.setS(needCount);
            categories.put("need", x);
            
            x = new Helpers.Pair<>();
            x.setF(entm);
            x.setS(entmCount);
            categories.put("entm", x);
            
            x = new Helpers.Pair<>();
            x.setF(incm);
            x.setS(incmCount);
            categories.put("incm", x);
            
            x = new Helpers.Pair<>();
            x.setF(fuel);
            x.setS(fuelCount);
            categories.put("fuel", x);
        }
        
        private void setMonth(CBMain.Transaction t) {
            this.month = Integer.parseInt(
                    Integer.toString(Helpers.getDate(t.date))
                            .substring(0, 6)+"01");
        }
    }
    
    /**
     * Takes in a list of transactions, goes through in order received in list, and
     * captures them monthly to make a new Analysis class on each month's transactions
     * in the list received. 
     * @param firstDate Oldest date received in the list (first in the list)
     * @param thetrans Transaction list to analyze
     * @return List of Analysis classes on the transactions
     */
    public static ArrayList<Analysis> monthlyAnalysis (String firstDate
            , ArrayList<CBMain.Transaction> thetrans) 
    {
        int currDate = Integer.parseInt(firstDate); // Getting the first date in the list as int YYYYMMDD
        ArrayList<CBMain.Transaction> send_off = new ArrayList<>(); // Sending off transactions to class for analytics
        ArrayList<Analysis> send_back = new ArrayList<>(); // Return array list full of monthly analysis
        
        for (int t = 0; t < thetrans.size(); t++) {
            
            // If the current transaction was made in the current month
            if (Helpers.getDate(thetrans.get(t).date) < currDate+100 
                    && Helpers.getDate(thetrans.get(t).date) >= currDate) {
                send_off.add(thetrans.get(t)); // Add transaction to list to be sent off
            }
            
            // The current transaction is in a new month
            else {
                t--; // Going back one transaction so the month can get the transaction that broke the if above
                send_back.add(new Analysis(send_off)); // Getting monthly analysis for this month & adding it to return list
                send_off.clear(); // Clearing send off list
                
                // Advancing one month
                currDate += 100;
                if (Integer.toString(currDate).substring(4,6).equals("13")) {
                    currDate -= 1200;
                    currDate += 10000;
                }
                
            }
        } // End for loop 
        
        // add the last month (it may not have gotten in while in the for loop
        send_back.add(new Analysis(send_off));
        send_off.clear(); // emptying the list in case it is huge
        
        return send_back;
        
    }

}
