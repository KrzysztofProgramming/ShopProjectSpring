# ShopProjectSpring
Project made with Spring boot and MongoDB compiled with jdk 16  
Frontend code is available here: https://github.com/KrzysztofProgramming/ShopProjectAngular

## Running an app shop-project.jar
command: **java -jar shop-project.jar**

MongoDB server running on **localhost:27017** is required to launch the server.  
To order some products or reset a password mail service listening on local port **1025** is required.

##### Admin user:
username: *admin*  
password: *admin*

## Features:
- creating, editing, filtering products
- creating, editing, filtering authors 
- creating, editing, filtering types
- creating/editing users
- roles
- JWT authorization
- password reseting
- making orders
- email confirmations
- orders history
- storing and scaling products' images up to 3 resolutions

## To do:
- Create second version of the app using PostgreSQL instead of MongoDB
