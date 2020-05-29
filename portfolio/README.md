# Cody Rivera Personal Portfolio

This is a personal portfolio web app that targets Google Cloud's
App Engine.

To run a local server, execute this command in Cloud Shell:

```bash
mvn package appengine:run
```

To deploy, set the deploy.projectId field in pom.xml
and execute this command in Cloud Shell:

```bash
mvn package appengine:deploy
```
