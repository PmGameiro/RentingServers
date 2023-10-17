# Java threads work

### Summary

Cloud computing platforms such as Google Cloud and Amazon EC2 allow you to reserve and use virtual servers for processing and storage. These platforms provide several types of servers, each identified by a name (e.g., "t3.micro", "m5.large", ...), corresponding to a hardware configuration and a fixed nominal price ( e.g., 0.99e per hour). This work aims to develop a service for allocating servers in the cloud and accounting for the cost incurred by users.

A server can be reserved on request or via auction, or as they are known in Amazon EC2, respectively, on demand instances or spot instances. When booking on request, a server remains allocated until released by the user, and the nominal hourly price corresponding to the time used is charged. In auction reservation, a user proposes the price he is willing to pay for the server reservation of a certain type. You will only be allocated a server when the proposed hourly price is the highest among bidders for that type of server. Furthermore, the reservation of a server at auction can be canceled by the cloud in order to satisfy an on-demand reservation when there are no other servers of the desired type available.

### Functionality

This service must support the following functionalities:

- Authentication and user registration: given email and password. Whenever a user wishes to interact with the service, they must establish a connection and be authenticated by the server.
- Reserve a server on request. An authenticated user will be able to request the reservation of a server of a certain type (for a nominal price).
- Reserve an instance at auction. An authenticated user can request a reservation for a server of a certain type, indicating the hourly price he or she is willing to pay.
- Free a server. An authenticated user will be able to terminate a server reservation that was previously granted to them.
- Consult your current account. An authenticated user will be able to check the amount owed according to the use of the cloud computing platform's resources.

### Server Catalog:

- It is assumed that the types of available servers are fixed and known in advance.
- It is assumed that the number of available servers of each type is fixed and known in advance.
- It is assumed that each type of server has an identification (for example, "t3.micro", "m5.large") and previously known fixed nominal price.

### Assigning servers on request:

- The on-demand server reservation operation must wait until it is granted and return a reservation identifier that will later be used to terminate it.
- In case of unavailability of servers of the requested type, reservations granted in an auction to obtain the desired server may be cancelled.
- Release of a reservation granted upon request can be made by the user to whom it was granted using the corresponding identifier. The server must consider the freed instance as available for future reservation.

### Allocation of servers in auction:

- The reservation operation of a server in an auction must indicate the desired price, wait until it is granted and return a reservation identifier, which will later be used to complete it.
- If there are several reservation operations in auction for a type of server that becomes available, the one offering the highest value must be considered.
- If a reservation granted in an auction is canceled by the cloud, the user must be notified using the corresponding identifier.
- The release of a reservation granted at auction can be done by the user to whom it was granted using the corresponding identifier. The server must consider the freed instance as available for future reservation.

### Client

A client must be made available that offers a user interface that allows the functionality described above to be supported. This client must be written in Java using threads and socketsTCP

### Server

The server must also be written in Java, using TCP threads and sockets, keeping the relevant information in memory to support the functionalities described above, receiving connections and input from clients, as well as sending the desired information to them. The protocol between client and server must be text-based, line-oriented. To prevent the server from being vulnerable to slow clients, each thread should not write to more than one socket.