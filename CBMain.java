/*
 * By: Tyler Freese
 */
package TFCB;
import TFCB.Helpers.Box;
import TFCB.Helpers.Pair;
import SCI.API;

import java.awt.Color;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * CBMain is the main class for Checkbook program.
 * @author Tyler Freese
 * @version 3
 */
public class CBMain {
    public static String Name;
    static double Money;
    static CBMainFrame of;
    static NumberFormat nf = NumberFormat.getCurrencyInstance();
    static StringBuilder sb = new StringBuilder();
    static Connection conn = null;
    static API api = null;
    static ReentrantLock serverLock = new ReentrantLock();
    
    public static void main(String[] args) throws 
            IOException, 
            NoSuchAlgorithmException, 
            InvalidKeySpecException, 
            NoSuchPaddingException, 
            InvalidKeyException, 
            IllegalBlockSizeException, 
            BadPaddingException 
    {
        // Connect API to server //
        setAPIConnection();  
            
        //creating outputframe object
        of = new CBMainFrame();
        
        // If no connection established
        if (!api.isConnected) {
            Helpers.notifyWithBox("No database connection established");
            Helpers.wait(8.0); //wait 8 seconds to end the application process
            System.exit(0); //exit the application
        }
            
        //starting server connection thread
        (new Thread(new SessionManager())).start();
        
        //setting default buttons & making frame visible
        Helpers.screenDefaults();
        CBMainFrame.LoginFrame.setVisible(true);
        Helpers.fillMonths();
    }
    
    /**
     * Create a new API class that is connected with the server. First it will
     * try to connect locally, then will connect to remote host.
     * 
     * @throws IOException 
     */
    private static void setAPIConnection() throws 
            IOException,
            NoSuchAlgorithmException, 
            InvalidKeySpecException, 
            NoSuchPaddingException, 
            InvalidKeyException, 
            IllegalBlockSizeException, 
            BadPaddingException 
    {
        // First try and connect to local computer
        try {
            api = new API("LOCAL IP",LOCAL_PORT);
        }
        // On failure, connect to remote host
        catch (IOException e) {
            api = new API("REMOTE_HOST",REMOTE_IP);
        }
    }
   
    /**
     * Handles user log in functionality
     * @throws IOException 
     * @throws java.security.NoSuchAlgorithmException 
     * @throws javax.crypto.NoSuchPaddingException 
     * @throws java.security.spec.InvalidKeySpecException 
     * @throws javax.crypto.IllegalBlockSizeException 
     * @throws javax.crypto.BadPaddingException 
     * @throws java.security.InvalidKeyException 
     */
    public static void userLogin() throws IOException, 
            NoSuchAlgorithmException, 
            NoSuchPaddingException, 
            InvalidKeyException, 
            IllegalBlockSizeException, 
            BadPaddingException, 
            InvalidKeySpecException 
    {
        
        String givenName = CBMainFrame.LoginFrameTextField.getText().trim();
        String passwd = new String(CBMainFrame.loginPasswordField.getPassword());
        
        // NEW API METHOD
        // Build data to be sent
        HashMap<String, Box> payload = new HashMap<>();
        payload.put("name", new Box(givenName));
        payload.put("passwd", new Box(passwd));
        
        // Acquire server communication lock to send
        HashMap<String, Box> result = new HashMap<>();
        serverLock.lock();
        try {
            result = api.route("GETUSER", payload); // Capture result
        } finally {
            serverLock.unlock();
        }
        
        // Convert to data format expected
        Pair<String, Double> user = new Pair<>(
            result.get("name").get().toString(), 
            Double.parseDouble(result.get("money").get().toString())
        );
        
        String name  = user.getF(); // Username
        double money  = user.getS(); // Money
            
        // If they are not in the database
        if (name.equals("nope")) { // Let user know they aren't in the database
            CBMainFrame.LoginFrameTextField.setText("");
            Helpers.notifyWithBox("Not available.");
        }

        // If they ARE in the database
        else {
            Name = name;Money = money; //setting global variables

            //CBMainFrame initialization
            CBMainFrame.LowerNotif.setText("");
            CBMainFrame.LoginFrameTextField.setText("");
            CBMainFrame.NameLabel.setText(Name);
            CBMainFrame.moneyLabel.setText(nf.format(Money));
            of.setVisible(true);
            of.setEnabled(true);
            of.toFront();
            CBMainFrame.LoginFrame.dispose();
        }
            
    }
    
    /**
     * Create new user if possible
     * 
     * @param uname New user name
     * @param money New user money
     */
    public static void createUser(String uname, Double money, String upass) {
        boolean successAddUser = false;
        
        // NEW API METHOD
        HashMap<String, Helpers.Box> payload = new HashMap<>();
        HashMap<String, Box> result = null;
        payload.put("money", new Box(money));
        payload.put("name", new Box(uname));
        payload.put("pass", new Box(upass));
        try {

            // Acquire server lock to send
            serverLock.lock();
            try {
                // Send and capture
                result = api.route("CREUSER", payload); // Capture result
            } finally {
                serverLock.unlock();
            }
            
            successAddUser = (boolean) (result.get("success").get());

        } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException ex) {
            Logger.getLogger(CBMainFrame.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex);
        }
        
        if (successAddUser)
            Helpers.notifyWithBox("User "+uname+" successfully created!");
        else
            Helpers.notifyWithBox("Could not create user.");
        
        CBMainFrame.NewUserNameTextField.setText("");
        CBMainFrame.NewUserDollarTextField.setText(""); 
        CBMainFrame.NewUserFrame.dispose();
    }
        
         
    
    /**
     * Checkbook class that runs the interface the user sees.
     */
    public static class Checkbook {
        static Scanner sc = new Scanner(System.in);
        static String command = "/";
        static String ib = "", eb = "";
        static List<Transaction> trans = new ArrayList<>();
        static List<String> months = new ArrayList<>();
        
        public static void CommandCenter() {
            // Used to determine command
            
            if (command.isEmpty()) {}
            
            else if (command.equals("help"))
            {   CBMainFrame.HelpTabTextField.setText("Commands:"
                +"\n\tupdate - sums up all IO"
                +"\n\tclear - clears the current transactions"
                +"\n\tremove 'rm/' - remove a transaction by $ amt"
                +"\n\tcn - empties notifications"
                +"\n\tcuser - changes user"
                +"\n\tovrm - override money value"
                +"\n\tload - go to date selector"
                +"\n\texit - exits program and saves"
                +"\n\tsave - saves current income and expenses");
                CBMainFrame.TabbedPane.setSelectedIndex(1);
                CBMainFrame.CommandTextField.setText("");
            }
            
            else if (command.equals("update")) { //takes user to check tab
                CBMainFrame.TabbedPane.setSelectedIndex(2); 
                updateTotalIsAndOs(); 
            }
            
            else if (command.contains("rm/")) { //removes transaction from trans array
                sb = new StringBuilder();
                sb.append(CBMainFrame.CommandTextField.getText());
                double r = Double.parseDouble(sb.substring(sb.lastIndexOf("/")+1,sb.length()));
                removeTrans(r); 
                updateTotalIsAndOs(); 
            }
            
            else if (command.equals("cn")) //clear notifications
            {   CBMainFrame.LowerNotif.setText(""); }
            
            else if (command.equals("clear")) { //clear transaction array
                CBMainFrame.YesOrNoBox.setVisible(true);
            }
            
            else if (command.equals("load")) {
                //takes user to load tab
                CBMainFrame.TabbedPane.setSelectedIndex(3);
            }
            
            else if (command.equals("cuser")) //changing user
            {
                // if user has no transacitons in queue
                if (trans.isEmpty()) {
                    CBMainFrame.LowerNotif.setText("Changing user.");
                    of.setEnabled(false);
                    clearAll();
                    CBMainFrame.LoginFrame.setEnabled(true);
                    CBMainFrame.LoginFrame.setVisible(true);
                    //login button on login frame
                    CBMainFrame.LoginFrame.getRootPane().setDefaultButton(CBMainFrame.LoginFrameGoButton);
                }
                // if user still has transactions in queue, don't let them leave
                else {
                    Helpers.notifyWithBox("You still have transactions in queue!");
                    CBMainFrame.LowerNotif.setText("Clear all transactions or"
                        + " save to exit.");
                }
            }
            
            else if (command.equals("ovrm")) { //override money value
                CBMainFrame.MoneyChangerFrame.setVisible(true);
                CBMainFrame.MoneyChangerFrame.setEnabled(true);
                of.setEnabled(false);
            }
            
            else if (command.equals("save")) //save current transactions to DB
            { Save(); }
                
            else if (command.equals("exit"))
            {
                if (trans.isEmpty())  //if user has no transactions, exit
                {
                    if (api.close())
                        System.exit(0);
                    else
                        System.exit(1);
                }

                //if they HAVE transactions, don't quit application
                else {
                    Helpers.notifyWithBox("You still have transactions in queue!");
                    CBMainFrame.LowerNotif.setText("Clear all transactions or"
                        + " save to exit.");
                    of.setVisible(true);
                    of.toFront();
                    Helpers.screenDefaults();
                }
            
            }
                
            else {CBMainFrame.CommandTextField.setText("");}
            
            //reset command text field to blank
            CBMainFrame.CommandTextField.setText("");
                
        }
        
        /**
         * Retrieves start or end date on the load tab user has selected
         * 
         * @param s String "s" for start date or "e" for end date
         * @return Integer of form YYYYMMDD for start or end date
         */
        public static int getDate(String s) {
            
            //end date retrieved using bottom boxes on load page
            if (s.equals("e")) {
                String enddate = "";
                
                //setting year
                enddate += CBMainFrame.YearComboBoxFiles1.getSelectedItem();
            
                //setting month
                String month = Integer.toString(CBMainFrame.MonthComboBoxFiles1.getSelectedIndex());
                if (month.length() < 2) {  //maintains YYYYMMDD
                    enddate += "0"+month;
                }
                else {
                    enddate+=month;
                }
            
                //setting day
                String day = Integer.toString(CBMainFrame.DayComboBoxFiles1.getSelectedIndex());
                if (day.length() < 2) {  //maintains YYYYMMDD
                    enddate+="0"+day;
                }
                else {
                    enddate+=day;
                }
            
            return Integer.parseInt(enddate); // returns int YYYYMMDD
            }
            
            //start date retrieved from top boxes on load page
            else {
                String enddate = "";
            
                //setting year
                enddate += CBMainFrame.YearComboBoxFiles.getSelectedItem();
            
                //setting month
                String month = Integer.toString(CBMainFrame.MonthComboBoxFiles.getSelectedIndex());
                if (month.length() < 2) { //maintains YYYYMMDD
                    enddate += "0"+month;
                }
                else {
                    enddate+=month;
                }
                
                //setting day
                String day = Integer.toString(CBMainFrame.DayComboBoxFiles.getSelectedIndex());
                if (day.length() < 2) { //maintains YYYYMMDD
                    enddate+="0"+day;
                }
                else {
                    enddate+=day;
                }
            
                return Integer.parseInt(enddate); // returns int YYYYMMDD
            }
            
        }
        
        /**
         * Clear all boxes that may contain transaction data
         */
        public static void clearAll() {
            
            trans.clear(); //clear the array
            CBMainFrame.LowerNotif.setText("Transactions cleared."); 
            updateIncomeBox();
            updateExpenseBox();
            CBMainFrame.CheckTabIncomePane.setText("");
            CBMainFrame.CheckTabExpensePane.setText("");
            CBMainFrame.IncomeBoxLoadResultsTab.setText("");
            CBMainFrame.ExpenseBoxLoadResultsTab.setText("");
            CBMainFrame.DepositsLoadResultsTab.setText("");
            CBMainFrame.ExpensesLoadResultsTab.setText("");
            CBMainFrame.GreaterLessLoadResultsTab.setText("");
            CBMainFrame.numbersTabTextArea.setText("");
            CBMainFrame.searchList.setListData(new String[0]); 
            CBMainFrame.searchTextField.setText("");
            CBMainFrame.TotalIncomeLabel.setText(nf.format(0));
            CBMainFrame.TotalExpenseLabel.setText(nf.format(0));
            CBMainFrame.BigIELabel.setText("");
            CBMainFrame.BigIncomeLabel.setText("");
            CBMainFrame.BigExpenseLabel.setText("");
            CBMainFrame.TabbedPane.setSelectedIndex(0);
            CBMainFrame.YesOrNoBox.dispose();
        }
        
        /**
         * Remove transaction from screen that has not been entered into database
         * 
         * @param r Amount to remove
         */
        public static void removeTrans(double r) {
            String in = ""; //essential "boolean" that declares the amount the
                            //user wants deleted is in the array of transactions
            for (Transaction t : trans) {
                if (t.amount == r) {
                    in = "in";
                }
            }
             
            Transaction remove = null;
            if (in.equals("in")) { //if the amount to remove exists
                for (Transaction t : trans) {
                    if (t.amount == r) {
                        remove = t; //set variable equal to that transaction
                    }
                }
                trans.remove(remove);  //remove it
                updateIncomeBox();
                updateExpenseBox();
            }
             
            //if the amount to remove does not exist
            else {
                Helpers.notifyWithBox("Amount entered NOT recorded.");
            }
        }
        
        /**
         * Sums up income from transactions user has in IO tab
         * 
         * @return double value of income 
         */
        public static double totalIs() {
            //used in updateTotalIsandOs() to set variables on frame
            
            double in = 0.0;
            for (Transaction tran : trans) { //gather all INCOME
                if (tran.plusminus.equals("+")) {
                    in += tran.amount;
                }
            }
            
            CBMainFrame.TotalIncomeLabel.setText(nf.format(in));
            return in;
        }
        
        /**
         * Sums up expenses from transactions user has in IO tab
         * 
         * @return double value of expenses
         */
        public static double totalOs() {
            //used in updateTotalIsandOs() to set variables on frame
            
            double ex = 0.0;
            for (Transaction tran : trans) { //gather all EXPENSES
                if (tran.plusminus.equals("-")) {
                ex += tran.amount;
                }
            }
            
            CBMainFrame.TotalExpenseLabel.setText(nf.format(ex));
            return ex;
        }
        
        /**
         * Lists all costs and incomes in Check tab boxes. Sums up each box and
         * gets net gain/loss for right side of Check tab.
         */
        public static void updateTotalIsAndOs() {
            //updating expenses
            String lista = "";
            for (Transaction tran : trans) {
                if (tran.plusminus.equals("-")) {
                    lista+= nf.format(tran.amount)+"\n"; //add amount ONLY to box
                }
            }
            CBMainFrame.CheckTabExpensePane.setText(lista);
            CBMainFrame.TotalExpenseLabel.setText(nf.format(totalOs()));
            
            
            //updating income
            lista = "";
            for (Transaction tran : trans) {
                if (tran.plusminus.equals("+")) {
                    lista+= nf.format(tran.amount)+"\n"; //add amount ONLY to box
                }
            }
            CBMainFrame.CheckTabIncomePane.setText(lista);
            CBMainFrame.TotalIncomeLabel.setText(nf.format(totalIs()));
            
            //following is used to determine size and color of total box on check tab
            if (totalOs() > totalIs())
            {
                CBMainFrame.BigExpenseLabel.setFont(new java.awt.Font("Nirmala UI",0,24));
                CBMainFrame.BigExpenseLabel.setText(nf.format(totalOs()));
                CBMainFrame.BigIncomeLabel.setFont(new java.awt.Font("Nirmala UI",0,15));
                CBMainFrame.BigIncomeLabel.setText(nf.format(totalIs()));
                CBMainFrame.BigIELabel.setText(" - "+nf.format(totalOs()-totalIs()));
                CBMainFrame.BigIELabel.setForeground(new java.awt.Color(204, 0, 0));
            }
            else if (totalOs() == totalIs()) 
            {
                CBMainFrame.BigIncomeLabel.setFont(new java.awt.Font("Nirmala UI",0,24));
                CBMainFrame.BigIncomeLabel.setText(nf.format(totalIs()));
                CBMainFrame.BigExpenseLabel.setFont(new java.awt.Font("Nirmala UI",0,24));
                CBMainFrame.BigExpenseLabel.setText(nf.format(totalOs()));
                CBMainFrame.BigIELabel.setText("  "+nf.format(totalIs()-totalOs()));
                CBMainFrame.BigIELabel.setForeground(new java.awt.Color(0, 204, 0)); 
            }
            else
            {
                CBMainFrame.BigIncomeLabel.setFont(new java.awt.Font("Nirmala UI",0,24));
                CBMainFrame.BigIncomeLabel.setText(nf.format(totalIs()));
                CBMainFrame.BigExpenseLabel.setFont(new java.awt.Font("Nirmala UI",0,15));
                CBMainFrame.BigExpenseLabel.setText(nf.format(totalOs()));
                CBMainFrame.BigIELabel.setText("  "+nf.format(totalIs()-totalOs()));
                CBMainFrame.BigIELabel.setForeground(new java.awt.Color(0, 204, 0));
            }
        }
        
        /**
         * Populates income box on IO tab with income transactions
         */
        public static void updateIncomeBox() {
            String lista = "";
            for (Transaction tran : trans) {
                if (tran.plusminus.equals("+")) {
                lista+= tran.plusminus+" "+nf.format(tran.amount)+" "+tran.description+"\n";
                }
            }
            CBMainFrame.ActivityOP.setText(lista);
        }
        
        /**
         * Populates expense box on IO tab with expense transactions
         */
        public static void updateExpenseBox() {
            String lista = "";
            for (Transaction tran : trans) {
                if (tran.plusminus.equals("-")) {
                lista+= tran.plusminus+" "+nf.format(tran.amount)+" "+tran.description+"\n";
                }
            }
            CBMainFrame.ProblemOP.setText(lista);
        }
        
        /**
         * Literally updates the name label in top left of GUI
         */
        public void updateNameLabel() {
            CBMainFrame.NameLabel.setText(Name);
        }
        
        /**
         * Either add an expense or income to the trans array
         */
        public static void addTrans() {
            
            if (CBMainFrame.IncomeButton.isSelected()) { //adding income
                // String together transaction date
                String dayte = CBMainFrame.MonthComboBox.getSelectedItem().toString() +
                        " "+CBMainFrame.DayComboBox.getSelectedItem().toString() +
                        " "+CBMainFrame.YearComboBox.getSelectedItem().toString();
                
                // Create new transaction from user entered data
                Transaction t = new Transaction(
                        Double.parseDouble(CBMainFrame.AmountTextField.getText()), // Amount
                        dayte, // Date
                        "+", // Positive amount
                        CBMainFrame.DescriptionTextField.getText(), // Description
                        CBMainFrame.typeComboBox.getSelectedItem().toString().toLowerCase() // Type
                );
                trans.add(t); // Add it to list
                
                // Update text in income boxes
                ib += t.plusminus+" "+t.description+" "+nf.format(t.amount)+"\n";
                updateIncomeBox();
                
                CBMainFrame.DescriptionTextField.setText(""); // Reset fields
                CBMainFrame.AmountTextField.setText("");
                
             }
            else if (CBMainFrame.ExpenseButton.isSelected()) { //adding expense
                String dayte = CBMainFrame.MonthComboBox.getSelectedItem().toString() +
                        " "+CBMainFrame.DayComboBox.getSelectedItem().toString() +
                        " "+CBMainFrame.YearComboBox.getSelectedItem().toString();
                
                Transaction t = new Transaction(
                        Double.parseDouble(CBMainFrame.AmountTextField.getText()), // Amount
                        dayte, // Date
                        "-", // Negative amount
                        CBMainFrame.DescriptionTextField.getText(), // Description
                        CBMainFrame.typeComboBox.getSelectedItem().toString().toLowerCase() // Type
                );
                trans.add(t);
                
                // Update expense box text
                eb += t.plusminus+" "+t.description+" "+nf.format(t.amount)+"\n";
                updateExpenseBox();
                
                CBMainFrame.DescriptionTextField.setText("");
                CBMainFrame.AmountTextField.setText("");
                
            }
              
        }
        
        /**
         * Updates user money on screen and sends a server request to update it
         * in database.
         */
        public static void changeMoney() {
            //user changing their money
            
            //if they leave the text field empty
            if (CBMainFrame.MoneyChangerTextField.getText().isEmpty()) {
                Helpers.notifyWithBox("No amount entered.");
                CBMainFrame.MoneyChangerTextField.setText("");
            }

            //if their input is greater than 13 digits
            else if (CBMainFrame.MoneyChangerTextField.getText().length()>13) {
                Helpers.notifyWithBox("Too big of a number.");
                CBMainFrame.MoneyChangerTextField.setText("");
            }
            
            //if they actually enter something
            else if (!CBMainFrame.MoneyChangerTextField.getText().isEmpty())
            {
                //try and turn it into a double
                try { 
                    CBMain.Money = Double.parseDouble(CBMainFrame.MoneyChangerTextField.getText());
                    //DBI.updateUserMoney(Money, Name);
                    // NEW API METHOD
                    HashMap<String, Helpers.Box> payload = new HashMap<>();
                    HashMap<String, Box> result = null;
                    payload.put("money", new Box(Money));
                    payload.put("name", new Box(Name));
                    try {

                        serverLock.lock();
                        try {
                            // Send and capture
                            api.route("UPDUSRM", payload); // Capture result
                        } finally {
                            serverLock.unlock();
                        }

                    } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException ex) {
                        Logger.getLogger(CBMainFrame.class.getName()).log(Level.SEVERE, null, ex);
                        System.out.println(ex);
                    }
                    
                    of.setEnabled(true);
                    CBMainFrame.moneyLabel.setText(CBMain.nf.format(CBMain.Money));
                    CBMainFrame.MoneyChangerTextField.setText("");
                    CBMainFrame.MoneyChangerFrame.dispose();
                }
                //catch the error if they did not enter a valid number
                catch(NumberFormatException e) 
                {
                    Helpers.notifyWithBox("Enter a real number.");
                    CBMainFrame.MoneyChangerTextField.setText("");
                }
            }
        
        }
        
        /**
         * Saves currently entered transactions to remote database
         */
        public static void Save() {
            //saving transactions to DB
            
            if (trans.isEmpty()) {
                CBMainFrame.LowerNotif.setText("No transactions to save.");
            }
            else {
                saveTransactions();
            }
        }
        
        /**
         * Constructs data structures necessary for inserting transactions into
         * database. Structures: transactions list, transaction query, and name
         */
        public static void saveTransactions() {
            
            //turning all "negative" amounts negative for database storage
            for (int i = 0;i<trans.size();i++) {
                if (trans.get(i).plusminus.equals("-")) {
                    trans.get(i).amount*=-1;
                }
            }
            
            // Send transaction list to be inserted into database
            // NEW API METHOD
            HashMap<String, Helpers.Box> payload = new HashMap<>();
            HashMap<String, Box> result = null;
            payload.put("transList", new Box(trans));
            payload.put("transQuery", new Box(Helpers.constructInsertQuery(trans.size(), "TABLE")));
            payload.put("name", new Box(Name));
            try {

                serverLock.lock();
                try {
                    // Send and capture
                    api.route("INSTRAN", payload); // Capture result
                } finally {
                    serverLock.unlock();
                }

            } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException ex) {
                Logger.getLogger(CBMainFrame.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println(ex);
            }
            
            Checkbook.clearAll(); //clearing all transactions when done
            CBMainFrame.LowerNotif.setText("Transactions saved and cleared.");
        }
        
    }
    
    /**
     * Session manager checks for application dormacy and server connectivity.
     * If the application is dormat, it will commence an exit. If there is no
     * server connectivity, it will block input from user and user's ability to
     * save. Application will try to reconnect to server. 
     */
    private static class SessionManager implements Runnable {
        int elapsedSeconds = 0;
        
        HashMap<String, Box> data = new HashMap<>();
        
        @Override
        public void run() {
            // Add junk data to hashmap
            data.put("message", new Box("Hello!"));
            
            while (true) {
                
                // Checking for dormacy //
                if (System.currentTimeMillis() - CBMainFrame.prevMouseTime > 300000) {
                    Checkbook.clearAll();
                    Checkbook.command = "exit";
                    Checkbook.CommandCenter();
                }
                // End checking for dormacy //
                
                
                // Testing for server connectivity //
                if (elapsedSeconds % 4 == 0) { // Only test database every 4 seconds
                    // ** Checking database connectivity **
                    boolean alive = false;
                    try {
                        // Acquire server communication lock
                        serverLock.lock();
                        try {
                            // Get response from server
                            alive = (boolean) (api.route("HEARTBT", data).get("response").get());
                        } finally {
                            serverLock.unlock();
                        }
                        
                    } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException ex) {
                        System.out.println("Error sending/receiving heartbeat.");
                    }
                    
                    //if server sent something back (it's active)
                    if (alive) {
                        
                        CBMainFrame.dbStatusLabel.setForeground(Color.green);
                        CBMainFrame.dbStatusLabel.setText("Connected");
                        
                        // If connection is alive and the buttons were previously disabled
                        if (!CBMainFrame.TrackButton.isEnabled()) {
                            CBMainFrame.TrackButton.setEnabled(true);
                            CBMainFrame.GoForItButton.setEnabled(true);
                            CBMainFrame.NoWaitButton.setEnabled(true);
                            CBMainFrame.ContinueButtonLoadPane.setEnabled(true);
                            CBMainFrame.ShowMeButtonNumbersPane.setEnabled(true);
                            CBMainFrame.searchButton.setEnabled(true);
                        }
                            
                    }

                    //nothing was received from server (it's down)
                    else {
                        CBMainFrame.dbStatusLabel.setForeground(Color.red);
                        CBMainFrame.dbStatusLabel.setText("NOT Connected");
                        CBMainFrame.LowerNotif.setText("Connection with database lost!");
                        //System.out.println("is NOT connected.");
                        
                        // Disable necessary buttons that would cause errors
                        CBMainFrame.TrackButton.setEnabled(false);
                        CBMainFrame.GoForItButton.setEnabled(false);
                        CBMainFrame.NoWaitButton.setEnabled(false);
                        CBMainFrame.ContinueButtonLoadPane.setEnabled(false);
                        CBMainFrame.ShowMeButtonNumbersPane.setEnabled(false);
                        CBMainFrame.searchButton.setEnabled(false);
                        
                        // Close the current connection
                        api.close();
                        
                        // Try to reconnect
                        try {
                            // Connect API to server //
                            setAPIConnection();
                            
                            CBMainFrame.LowerNotif.setText("Reconnected!");
                            
                        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
                            Logger.getLogger(CBMain.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                    }
                    // ** End checking database connectivity ** 
                }
                
                // sleep testing for 2 seconds
                try {
                    Thread.sleep(2000);
                    elapsedSeconds += 2;
                } catch (InterruptedException ex) {
                    Logger.getLogger(CBMain.class.getName()).log(Level.SEVERE, null, ex);
                }
            
            }
            
        }
    }
    
    /**
     * Transaction class used to store all transaction data in application.
     */
    public static class Transaction {
         double amount;
         String date;
         String plusminus;
         String description;
         String type;
        
        /**
         * Constructor for a Transaction
         * 
         * @param amt Amount for transaction
         * @param date Date transaction occurred
         * @param plusminus "+" for positive, "-" for negative amount
         * @param desc Description of transaction
         * @param t Type of transaction
         */
        public Transaction(double amt,String date,String plusminus,String desc
                                                ,String t) {
            this.amount = amt;
            this.date = date;
            this.plusminus = plusminus;
            this.description = desc;
            this.type = t;
        }
        
        /**
         * Turns the Transaction into a string with values separated by a colon.
         * 
         * @return String representation of a Transaction
         */
        public String serialize() {
            
            String _return = "";
            _return = _return.concat(Double.toString(amount)+":");
            _return = _return.concat(date+":");
            _return = _return.concat(plusminus+":");
            _return = _return.concat(description+":");
            _return = _return.concat(type);
            
            return _return;
        }
        
    }
    
}
    
