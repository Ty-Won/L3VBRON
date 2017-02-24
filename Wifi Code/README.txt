In the competition, your robot is required to wirelessly receive important information such as which corner 
it is in, which role it plays or where it needs to go.

The software that we provide to do this is split into two parts: a server and a client.
This zip file contains Eclipse projects for both parts.

The server receives and responds to requests from a client, in exactly the same way your computer is a client that connects to 
servers running websites.  In this case, your robot is the client and you can test with the server on your laptop.
In the final competition, the TA/professor will run the server and they will decide what data to send to your robot.

As provided to you, the EV3 client code is a basic example program that prints the data received 
from the server to the screen.  It is up to you to get the provided code (the wifi package)
working properly with the rest of your software, including using the data such as the starting
corner properly.  You do not need to (and should not) modify the server code.

** Step by step instructions **

To get it working, in Elicpse go to File->Import->General->Projects from Folder or Archive
Select this zip file using the "Archive" button
Ensure the two entries labeled as "Eclipse project" are selected; you can deselect the entry
labeled as "Wifi Code Winter 2017.zip_expanded"
Click Finish

When it's done importing, right click on the EV3WiFiClient project and 
select leJOS EV3->Convert to leJOS project

You can now run the WiFi project as an EV3 project on your robot and the DPMServer project as a 
regular Java program on your laptop.  That said, the WiFi still needs to be set up.

First, connect to the WiFi network with your laptop. There is an access point running in the lab:

SSID: DPM
Password:dddpppmmm

Slightly more complicated is connecting to WiFi with the EV3.  The simple way is to use the 
on-screen menu: plug in the USB WiFi adapter you were given at the start 
of the semester (the adapter must be plugged in before turning the robot on), 
go to the "Wifi" menu, select the "DPM" network, and enter the password.
It should connect, and an IP address in the format "192.168.2.x" will appear on your home screen.
From now on, whenever you start the robot it should connect automatically to the WiFi network.

Alternately, you can use the EV3control GUI tool instead of the on-screen menu.

Once that's all set up, you will need to configure your robot to connect to your laptop.
To do this, first find out what IP address you were assigned by the DPM router.  

On Windows 10, you can do this by clicking on "Properties" on the DPM network once you are connected to it.
The resulting setting page will have your IP address listed at the botton as "IPv4 adress".

On Linux, the ifconfig utility will show it under "inet addr" for a network likely to be named
something along the lines of "wlan0".  Your GUI of choice (GNOME, KDE, etc.) will also display
this information in one way or another.

Regardless of your OS, there are plenty of online guides that will show you how to do this
if you have trouble.

Once you have your IP address, modify the string SERVER_IP in WiFiExample.java to match.
You will also need to modify the TEAM_NUMBER variable as appropriate.
In the competition or beta demo, you will connect to the TA or professor's laptop using
the address 192.168.2.3.

Once that's done, run the DPMServer program as a normal Java program on your computer.  A GUI should appear.

While the DPMServer program is running, launch the EV3WiFiClient program on your robot; it should connect
to your laptop and wait for data.  You should see a message appear in the GUI saying "Team X connected".
You can now enter numbers into the DPMServer GUI or use the "Fill" button, which lets
you specify an XML file to load data from.  There is an example XML file included in this zip file.

Once the data is entered in the text boxes, click "Sart" on the GUI.  You should see a bunch of text show up
describing what happened and if the data was successfully sent or not.  Note that if you leave one of the team number 
boxes empty (or set to 0), the program will understand there is only one team and won't complain about not being able to
connect to some other team.

If all goes well, your EV3 should now display the data you just entered.  The program will end if, after the data is received,
a button is pressed. Generally, if anything goes wrong the program will simply print an error message 
and quit once a button is pressed. In case something goes really wrong and the EV3 is stuck waiting forever for the server,
you can exit the program by pressing the back (a.k.a. escape, upper left) button.  

** What's expected of you (w.r.t. WiFi) in the final competition/beta demo **

* We expect you to use the WifiConnection class as provided to connect to our server.  
* We will run the same server code that is included in this zip but you will NOT have access to it.
* Recall that you will need to change the SERVER_IP variable to 192.168.2.3.  
* The professors will decide what parameters the server will send to your robot.  
* Connecting to the server is the first thing you should do; only once you have received the data is your robot allowed to start moving.


** General Remarks **

The WiFi test code uses System.out.println statements that print to both the screen and, if connected,
the EV3Control console (over WiFi/Bluetooth).  This is particularly useful if you need to debug as reading output
is much easier on your laptop than on the LCD screen.  There is a boolean variable debugPrint in the WifiConnection class 
that will disable printing of the messages if you don't want to see them.

The router in the lab is known to ocassionally require a reboot, particularly when a lot of teams connect to it.
You may power cycle it as necessary but keep in mind you will disconnect everyone.  
My suggestion: try to avoid making everyone in the lab angry at you.

It is possible to use the WiFiConnection code over USB, Bluetooth or your own WiFi hotspot if you use the IP address 
for the correct interface on your laptop.  Feel free to do this when testing, but make sure it actually works over WiFi 
before the beta demo.  We will not accept "it works over XYZ" as an excuse.


If there are any questions or bug reports, please post on the discussion board or e-mail me at
michael.smith6@mail.mcgill.ca
