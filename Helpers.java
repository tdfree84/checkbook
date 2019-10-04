package TFCB;

import java.util.ArrayList;

/**
 *
 * @author Tyler Freese
 */
public class Helpers {
    
    /**
     * Verifies the transaction to be entered is valid
     * 
     * @param incomeSelected Indicator for income button selected
     * @param expenseSelected Indicator for expense button selected
     * @param descr Description user typed in
     * @param amt String type of the amount the user entered
     * @param type Type of transaction user chose
     * @return Boolean indicator for a valid transaction
     */
    public static boolean checkNewTransIntegrity(boolean incomeSelected, 
            boolean expenseSelected,
            String descr,
            String amt,
            String type) {
        
        //method that validates input for transactions
        
        // User doesn't select in or out
        if (incomeSelected == false && expenseSelected == false) {
            Helpers.notifyWithBox("Please select a data type. (I/E)");
        }
        
        // User selects type income as an expense
        else if (expenseSelected == true && type.equals("Income")) {
            Helpers.notifyWithBox("No expense type income.");
        }
        
        // User does not select type income when inserting income
        else if (incomeSelected == true && !type.equals("Income")) {
            Helpers.notifyWithBox("Need income type/selection.");
        }
        
        // If user leaves amount empty
        else if (amt.trim().isEmpty()) {
            Helpers.notifyWithBox("You didn't enter an amount.");
            CBMainFrame.AmountTextField.setText("");
        }
        
        // Too big of a description is entered
        else if (descr.length() > 30) {
            Helpers.notifyWithBox("Too long of a description.");
            CBMainFrame.DescriptionTextField.setText("");
        }
        
        // Too big of a number is entered
        else if (amt.length() > 11) {
            Helpers.notifyWithBox("Too big of a number.");
            CBMainFrame.AmountTextField.setText("");
        }
        
        // The only thing left to test is the number's integrity
        else {
            // Try to parse the number into a double
            try { 
                double m = Double.parseDouble(amt.trim());
                
                // If the amount is negative
                if (m < 0.0) {
                    Helpers.notifyWithBox("Cannot have a negative number.");
                    CBMainFrame.AmountTextField.setText("");
                }
                else { // amount is acceptable (nonnegative, meets length requirement)
                    return true;
                }
            }
            catch(NumberFormatException e) { // Data entered is invalid somehow
                Helpers.notifyWithBox("Enter a real number.");
                CBMainFrame.AmountTextField.setText("");
                return false;
            }
        }
        
        return false;
    }
    
    public static String constructUpdateTransQuery() {
        // Make query...
        return q;
    }
    
    /**
     * Builds an insert query with N inserts to be performed
     * 
     * @param transSize Number of transactions needed to be inserted
     * @param whichTable Which table in the database to insert into
     * @return Query for insertion set up for PreparedStatement
     */
    public static String constructInsertQuery(int transSize, String whichTable) {
        // Make query...
        return query;
    }
    
    /**
     * Builds a delete query for transactions to be deleted
     * 
     * @param deleteList Transactions to be deleted
     * @return Query to be executed with PreparedStatement
     */
    public static String constructDeleteQuery(CBMain.Transaction[] deleteList) {
        // Make query...
        return query;
    }

    /**
     * Alerts user with graphical box
     * 
     * @param message Message to be displayed
     */
    public static void notifyWithBox(String message) {
        CBMainFrame.NotificationLabel.setText(message);
        CBMainFrame.MessageBox.setVisible(true);
        CBMainFrame.MessageBox.setEnabled(true);
    }

    /**
     * Set default action for command button, login frame button, and 
     * money changer frame button.
     */
    public static void screenDefaults() {
        //allows user to press enter button on keyboard
        //go button on main frame
        CBMain.of.getRootPane().setDefaultButton(CBMainFrame.CommandButton);
        //login button on login frame
        CBMainFrame.LoginFrame.getRootPane().setDefaultButton(CBMainFrame.LoginFrameGoButton);
        //change money button on money frame
        CBMainFrame.MoneyChangerFrame.getRootPane().setDefaultButton(CBMainFrame.MoneyChangerButton);
    }

    /**
     * Fills an array with months of the year.
     */
    public static void fillMonths() {
        CBMain.Checkbook.months.add("January");
        CBMain.Checkbook.months.add("February");
        CBMain.Checkbook.months.add("March");
        CBMain.Checkbook.months.add("April");
        CBMain.Checkbook.months.add("May");
        CBMain.Checkbook.months.add("June");
        CBMain.Checkbook.months.add("July");
        CBMain.Checkbook.months.add("August");
        CBMain.Checkbook.months.add("September");
        CBMain.Checkbook.months.add("October");
        CBMain.Checkbook.months.add("November");
        CBMain.Checkbook.months.add("December");
    }

    
    /**
     * Tell the main thread to wait for X seconds
     * 
     * @param x Whole number seconds to wait (e.g. 2 = two second wait)
     */
    public static void wait(double x) {
        double y = x * 1000.0;
        int yy = (int) y;
        try {
            Thread.sleep(yy);
        } catch (InterruptedException ki) {
        }
    }
    
    // Helper classses below
    
    /**
     * Class two store an arbitrary pair of data
     * 
     * @param <First> Type of first data
     * @param <Second> Type of second data
     */
    public static final class Pair<First, Second> {
        private First f;
        private Second s;
        
        /**
         * Constructor for class Pair
         * 
         * @param f First data to store
         * @param s Second data to store
         */
        public Pair(First f, Second s) {
            setF(f);
            setS(s);
        }
        
        public Pair() {}
        
        public void setF(First fs) { this.f = fs; }
        public void setS(Second ss) { this.s = ss; }
        
        public First getF() { return f; }
        public Second getS() { return s; }
        
    }
    
    /**
     * Box class used to store arbitrary data
     * 
     * @param <T> Type of data
     */
    public static class Box<T> {
        // Taken from https://docs.oracle.com/javase/tutorial/java/generics/types.html

        private T data;

        /**
         * Constructor for class Box
         * 
         * @param data Data to store in the "Box"
         */
        public Box(T data) {
            this.data = data;
        }

        public void set(T t) { this.data = t; }
        public T get() { return data; }
    
    }
    
}
