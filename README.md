# 💾 CCS

> Attached is my written defence for this project. It was a fun task to illustrate my knowledge about servers :)

### What has been implemented?
In my implementation of CCS, I have implemented a CCS Class, which consists of eight methods:
main, start, handleClient, processRequest, updateStatisticsReport, serviceDiscovery,
startStatisticsReporter and handleArithmetic.

Main method is responsible for ensuring correct usage of port number (above 1024 and checking if
it is indeed an integer) and calling the start method with provided port number.
Start method is responsible for creating a UDP socket for serviceDiscovery (which will be explained
later in this document) and starting the new thread with this socket. Later on, the method tries to
create a TCP Server Socket for client communication and starts Statistic Reporter. During the
execution of the start method, new thread spawns for incoming client connection to ensure
concurrent handling of requests.
HandleClients method is responsible for (as the name suggest) handling clients. It receives request
and finally it removes a client from the connectedClients counter once one disconnects.
ProcessRequest is a helper function for handleClient and it processes the request based on its
contents (using handleArithmetic method). Additionally it throws an error if number of arguments
differs from 3 and counts each incorrect operation.
ServiceDiscovery method implements a service discovery mechanism using UDP – it listens for UDP
messages starting with “CCS DISCOVER” and responds with “CCS FOUND”
– if the first response is
delivered.
Additionally, there are two methods regarding Statistics Report – startStatisticsReporter and
updateStatisticsReport. Both are responsible for making sure that the report sent every 10 seconds
is up to date and correctly displays data.

### What difficulties did I have?
The main difficulty were the threads. I have decided to use synchronised ints whenever I needed,
because for me it is a safer approach – we used synchronised variables when using threads on
classes. However, I have researched and found out AtomicIntegres were also an option. I didn’t use
them previously and I wanted to know what exactly is going on in the project, so I have decided on
the first option.


### What errors are still present, what has not been implemented?
There are no errors during compilation on my device. I have tried to implement everything per
requirements, and I am pretty sure I succeeded. There is however room for improvement, for
example adding more client related activities like time of connection with the server. It wasn’t stated
in the requirements, so I have decided to stick to things I was required to provide in my
implementation of CCS.


### Description of the designed and implemented protocol
In my project I have used UDP protocol for service discovery (the server listens on specified by the
user port for incoming packages and responds with “CCS FOUND” as per requirements) and TCP
protocol for communication between clients and server, and processing requests like “ADD”
,
“SUB”
“MUL”
“DIV"

### Additional: Statistic Report
Attached is a picture of my implementation of CCS running. To check if my code works correctly, I
decided to connect 5 clients and later disconnect all of them at once to see how the statistics report
reacts. As visible on the screenshot, in next thread all 5 clients disconnected, which has been
correctly reported by the Statistics Report

<img width="774" alt="Screenshot 2025-04-25 at 16 41 52" src="https://github.com/user-attachments/assets/d0ecf8d4-d1cc-497e-ae44-f52848efd74c" />

