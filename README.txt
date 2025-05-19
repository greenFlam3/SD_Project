How to Execute the "Googol" Project

This guide explains how to install and run the "Googol" project step by step. Please follow the instructions in order to ensure proper execution.

--> Requirements:
	- Java 8 or higher
	- IntelliJ IDEA (recommended) or any other Java IDE
	- Active internet connection
	- All project dependencies correctly installed (RMI enabled)

--> Execution Steps:
	1.Compile the project: Ensure all .java files are compiled. If you are using an IDE, build the project first;
	2.Start the required servers in this order:
		- Run StorageBarrelServer.java
		- Run URLQueueServer.java
		- Run GatewayServer.java
	3. Initialize the system:
		- Run RMIClient.java
		- Select Option 1 to insert the first URL (this provides a starting point for the downloader)
	4. Start the downloader:
		- Run Downloader.java (this will begin crawling based on the initial URL)
	5. Use the search functionality:
		- Run RMIClient.java again
		- Select the search option to start querying indexed data


Notes

Make sure to run each server component in a separate terminal window or tab.
The client must insert the initial URL before starting the downloader.
If any service crashes or fails, restart it before proceeding.