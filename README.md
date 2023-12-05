# OS_Final_Project_ZSC

**Final project for Operating Systems class at Bellarmine University. Multithreaded chat server with Java Swing GUI, Java sockets, and shared resource mutual exclusion.**
<br>
Watch my [Kaltura Video]([https://video.bellarmine.edu/media/Operating+Systems+Fall+2023+Project/1_9nxtz0pc](https://video.bellarmine.edu/media/Multi-threaded+Chat+Server+%28OS+Final+Project%29/1_vhcd0qom)) to see Application Running and Code Review!

<br>
The main focus of this project is using ***multiple threads of execution while simultaneously maintaining a critical section.***

<br>
<br>

## My Specific Application

The application I am building is a server that allows users to chat and edit the server name.
<br>
There is no database management going on since the focus of this project is managing read/write access of each client. This is essential done with a binary semaphore that is set to true when the critical section is open.
<br>
I only chose one instance in which to protect the read / write access of a piece of data, this occurs in the instance of clients editing the server name information.
<br>
It would be important to only allow one user to edit this information at a time so that the change made is what we anticipate; we dont want a race case to occur.

<br>
<br>

## Currently Implemented Features

### Client Features
- Multiple clients can connect
- Clients have a customizable username connected to their unique socket
- Clients can post messages to main server
- "/" Client commands: change username, edit server name, ...
- Client disconnect frame handling

### Server Features
- Server displays number of actively connected clients upon a change (client connection/disconnection)
- Server displays messages from clients with username
- Server has client editable name
- Client Disconnect Handling

<br>
<br>




