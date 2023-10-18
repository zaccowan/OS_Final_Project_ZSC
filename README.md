# OS_Final_Project_ZSC

**Work in progress final project for Operating Systems class at Bellarmine University.**
<br>

The project seeks to use an Http Server to model some important dynamics of system regulation. 
<br>
The main focus of this project is using ***multiple threads of execution while simultaneously maintaining a critical section.***

<br>
<br>

## My Specific Application

The application I am building is a server that allows users to chat, send and recieve files, and also to manage some server information (name, description, etc).
<br>
There is no database management going on since the focus of this project is managing read/write access of each client. This will be done using mutual exclusion or a form of semaphore. 

<br>
<br>

## Currently Implemented Features

### Client Features
- Multiple clients can connect
- Clients have a customizeable username connected to their unique socket
- Clients can post messages to main server

### Server Features
- Server displays number of actively connected clients upon a change (client connection/disconnection)
- Server displays messages from clients with username 

<br>
<br>

## Known Problems / Things To Implement

### Client Side
- "/" Client commands: change username, ...
- Using "@{username or client id}" to send a message to a specific client
- Ability to send files

### Server Side
- Server does not yet handle termination of client connection
- Ability to have server details: name, description, ...
- Ability to kick users from server

<br>
<br>

## Features I Might Add for Fun
- GUI so clients can have a better experience than a console based interface



