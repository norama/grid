# GRID
Reservation system model.

# The model
This is a sample web application representing a simple reservation system.
Items to be reserved are displayed as cells in a grid. 
An item can have 3 states:
- free (green)
- reserved (yellow)
- taken (darkred)

## Workflow:
A free item can be reserved. Then the client fills in the data and takes the item,
or else the client can cancel the operation and in this case the cell is free again.
The cell is also returned to the free state after timeout 
(context parameter "settings/expiration").
A free item can also be taken right away if the data is supplied.
Reservation makes it possible to ensure some time for the client to fill in the data
after the item has benn selected.
A taken item can be freed by the same client who has taken it.
The system returns a ticket to the client at reservation and when the item is taken,
clients identify themselves by this ticket. If the ticket is not matched with the one
stored in the database, the operation is denied.

## State diagram:
```
                                            take
          +------------------>------------------------------->----------------+
          |                                                                   v
    +-----------+    reserve    +------------+     take with ticket      +---------+
    |    FREE   |  ---------->  |  RESERVED  |  -----------------------> |  TAKEN  |
    +-----------+               +------------+                           +---------+
          ^                            |                                      |
          |     free with ticket       |                                      |
          |           or timeout       |                                      |
          +----------------------------+                                      |
          |                                                                   |
          |                            free with ticket                       |
          +------------------<-------------------------------<----------------+
```
 
# Architecture

### Server: 
 - business logic: REST web service in JAVA JAX-RS
 - database layer: DreamFactory REST web services 
 
### Client: 
 - simple grid representation in Vue.js
