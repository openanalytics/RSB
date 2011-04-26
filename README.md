<pre>
 ______ _______ ______ 
|   __ \     __|   __ \
|      <__     |   __ <
|___|__|_______|______/
</pre>

# RSB (R Service Bus)

### Build

Pre-requisite:

- Sun/Oracle JDK 1.6
- Maven 3

To produce rsb.war, run:

    mvn clean install


### Test run

Use:

    mvn jetty:run

to start RSB configured to use a locally running RServi.

### Integration tests

RServi must be running locally before starting the integration tests.

If that is the case, then run:

    mvn -Pit verify

### Smoke test a running instance

TBD

### Deployment

RSB should deploy fine on any Java web-container supporting Servlet 2.5.

### Configuration  

TODO ...

#### Copyright (c) Copyright of OpenAnalytics BVBA, 2010-2011 - GNU AFFERO GENERAL PUBLIC LICENSE
