# Getting Started with EnMAS

## Prerequisites

Ensure the following software is installed on your computer before proceeding with these instructions.

- JDK (1.6 or higher)
- [Git](http://git-scm.com/downloads)
- [giter8](http://github.com/n8han/giter8)
- [SBT](http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html#installing-sbt) (0.12.x or higher)

_Note to Mac users:_ Git, giter8, and SBT are all available via the homebrew package manager.  Remember to `brew update` first.

## Download enmas-client and enmas-server

Visit the [downloads page](http://enmas.org/pages/downloads) on the official project web site to obtain the latest stable version of all three Jar files: `enmas-client`, `enmas-server`, and `enmas-examples`.

## Write your own Agent

### Clone the giter8 template

- To clone the giter8 Agent template, do

        g8 EnMAS/agent

- Follow the prompts, resulting in a subdirectory containing everything you need to compile an Agent and build a Jar file for EnMAS.

### Edit the Agent

- Refer to the [API documentation](http://enmas.github.com/api/enmas-core/#org.enmas.pomdp.Agent) and the [example agents](https://github.com/EnMAS/EnMAS/tree/master/enmas-examples/src/main/scala/org/enmas/examples/agents)
- Edit the resulting stubbed `Agent` subclass, located within the `src` 
directory of the project directory you just created.
- Package your agent into a Jar file using SBT:

        sbt package

- The resulting Jar is placed in `<project-directory>/target/scala-2.10/`.

## Run EnMAS

### Start the Server

    java -jar enmas-server-<version>.jar

### Start the Client

    java -jar enmas-client-<version>.jar

