This project demonstrates what appears to be a bug in the mysql jdbc driver (Connector/J).

When using the driver with the Hikari connection pool and a query timeout is encountered the driver throws a com.mysql.cj.jdbc.exceptions.MySQLTimeoutException this is caugth by the hikari pool and the underlying connection is closed, but there seems to be some kind race condition when multiple threads are working on the connection because the driver ends up throwing a NullPointerException com.mysql.cj.jdbc.StatementImpl::getWarnings.

I have only been able to reproduce this by using the jooq framework where a ForkJoinPool is used when executing stuff in a transaction.

To run the code you will need:

* Docker environment (testcontainers spins up a mysql database)
* Maven
* Java 11

Then just clone the project and run:

`mvn test`
