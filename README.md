# ShopProjectSpring
Project made with Spring boot and Hibernate compiled with jdk 17.

## Compiling
**mvn install**

## Running an app
command: **java -jar [jar_name] --create-admin=true**  
--create-admin argument is not neccessary if we already have our admin user.

PostgreSQL server running on localhost:5432 is required, username, password, database name etc. in application.properties.
To order some products or reset a password mail service listening on local port **1025** is required.

## Example Features:
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
- products' archivization

## Default admin:
username: **admin**  
password: **admin**
