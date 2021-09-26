# Notes for the YT series

1. Intro to the project and WireMock

- quick inspect of the code (uses Spring Boot, but the idea and this testing recipe can be used as an inspiration for other frameworks/setups)
- backend API that delegates to an HTTP client to call a remote system
- explain downsides of using the remote system during IT (depends on the uptime, additional load, create costs, modify states) with a nice-looking diagram
- WireMock helps here as we can start a local Jetty and mock HTTP responses
- This also helps to test error scenarios like 5xx or slow responses
- Outline upcoming parts: more insights to WireMock, JUnit 5 and JUnit 4 setup
- we'll start with a manual setup
- show first simple test


2. WireMock features

- build on top of the existing tests
- show request matching capability
- explain priority of multiple requests
- reset the request stubs after ech tests
- request matching error in console
- get access to the recorded requests

3. JUnit 5 setup

- no JUnit Jupiter extension available from WireMock
- community examples (show the two most common)
  - https://github.com/tomakehurst/wiremock/issues/684
  - https://github.com/JensPiegsa/wiremock-extension
  - https://github.com/phxql/wiremock-junit5
  - https://github.com/lanwen/wiremock-junit5
- we could also write our own
- alternative that nicely integrates with Spring is a ApplicationContextInitializer

4. JUnit 4 setup

- official rule
- still showcase it because there a lot of projects that haven't migrated yet
- caveats of JUnit 4 vs. JUnit 5 in the same project
