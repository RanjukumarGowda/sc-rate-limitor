# sc-rate-limitor
Rate limiting is a strategy to limit access to APIs. It restricts the number of API calls that a client can make within a certain timeframe.
  
Bucket4j is a Java rate-limiting library based on the token-bucket algorithm. Bucket4j is a thread-safe library. It also supports in-memory or distributed caching.  
I have used Bucket4j to implement rate limit. below are detailed steps how it works.  
1. Access control to any API should be mentioned in `rate-limit-list.yaml` which has following structure
* `url` : url which have rate limit restrictions
* each `url` section has n number of `clientInformation` which holds client details about `url` rate limit, they are
* `clientId` unique client identifier to identify client.
* `requestLimit` number of request limit per unit of time for ex: 10 or 20 ect..
* `unit` unit of time for ex: minute, second and hour.  
  
2. Whenever client wants to access any API, client should whitelist their clientID, once whitelisted any request should include `X-Client-Id` from which client will be provisioned for API access according to rate limit plan mentioned in `rate-limit-list.yaml`.  

3. Client api access status for any given point of time is maintained in an `CuncurrentHashMap` it is used as caching : for sake of simplicity.  

4. If client did not exceed request quota during API request, request will be served.  

5. If client exceeds request quota, request will be rejected and number of seconds retry after will be responded in response header `X-Rate-Limit-Retry-After-Seconds`.  

## Pre-requisite to run application 
1. Java 11 should be installed.
2. IDE : Intelli idea is preferred.
3. Lombok is used to generate boilerplate code, annotation processor should be enabled in IDE.
4. Gradle is used to manage application build, steps to test and package application.
* cd into where build.gradle file resides.
* Linux/MacOs : `./gradlew clean build`
* Windows : `./gradlew.bat clean build`
5. Once build completes fat jar can be found in `/build/libs/sc-rate-limiter-1.0.0.jar`.
6. jar can be run as like any other jar file. 
