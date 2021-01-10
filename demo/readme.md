# Leaf demo app

## Required programs

### Java 11
*Classic Java installation*

### Maven
*Classic Maven installation*
To allow maven to download the Github package add **settings.xml** file in your **~/.m2** folder:

    <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
    http://maven.apache.org/xsd/settings-1.0.0.xsd">
      <servers>
        <server>
        <id>github</id>
        <username>YOUR GITHUB USERNAME</username>
        <password>YOUR GITHUB TOKEN</password>
      </server>
    </servers>
  </settings>

To create your Github token, follow this link: [https://github.com/settings/tokens](https://github.com/settings/tokens)

### Mongo DB
An instance of MongoDB must be running locally with default port and credential.

## Build
Run this command:

    $ mvn clean package

## Run in dev mode
Run this command:

    $ mvn spring-boot:run
