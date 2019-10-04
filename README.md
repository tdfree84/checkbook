# Checkbook Program
Enhanced check-booking capabilities.

From frontend to backend, there are about 5,565 lines of my code as of August 19, 2019. 
See bottom for screenshots.

### About
This application is built for taking transactions off of a regular checkbook page to analyze, organize, and visualize them. My intended use of this program is to provide a tool to maintain your financial situation by creating an interactive checkbook. This application uses your transaction data to answer any inquiries you may have about your money at any point in time.

### Environment
All communication between clients and the server is encrypted.
* MySQL database 
* Ubuntu Server
* Java and Swing
* Development with NetBeans IDE

<img src="img/layout.PNG?raw=true" width="100%"></img>

### Usage
* View transactions in the IO tab as you add them
* Save transactions in a central database
* Load transactions from any period in time OR query ALL transactions stored for:
  * Displaying simple transaction data in the "Load Results" tab
  * View a bar graph of categorized amounts
  * View profit
  * Read a monthly analysis derived from transaction data
* Bring up transactions containing a key word
* Edit transactions you may have messed up on entry
* Delete unneeded transactions

### Future Scope
Thinking ahead, I would like to derive more information from transactions. Questions I want this program to answer are, "What period of time did I make the most money?" "When do I spend the most on fuel/food/entertainment/needs?" "Where did I spend most of my money the last three months?" Another future addition is a better graphing utility (possibly third party applications) that creates more visually appealing graphs of the data.

#### Notes
The complete files are not listed in this repository. The files here are just a showcase of my code. One limitation while developing this app is GUI design; it may not be very visually appealing. 

## The App

Main screen

<img src="img/mainframe.PNG?raw=true" width="100%"></img>


Bar Graph

<img src="img/bargraph.png?raw=true" width="100%"></img>


Loading Results

<img src="img/loadresults.png?raw=true" width="100%"></img>


Monthly Analysis (NEW)

<img src="img/analysis.PNG?raw=true" width="100%"></img>
