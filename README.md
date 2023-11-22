# OS_Final_Project_ZSC

**Work in progress final project for Operating Systems class at Bellarmine University.**
<br>

The project seeks to use an Http Server to model some important dynamics of system regulation. 
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
- Clients have a customizeable username connected to their unique socket
- Clients can post messages to main server
- "/" Client commands: change username, edit servername, ...
- Client disconnect frame handling

### Server Features
- Server displays number of actively connected clients upon a change (client connection/disconnection)
- Server displays messages from clients with username
- Server has client editable name
- Client Disconnect Handling

<br>
<br>

## Known Problems / Things To Implement

### Client Side
- Using "@{username or client id}" to send a message to a specific client
- Ability to send files

### Server Side
- Ability to kick users from server
- Proper Server termination

<br>
<br>




