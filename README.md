# A guide to create a simple scala microservice
The aim of this excercise is to get a headstart on how to create a simple scala microservice. We are going to use the following stack/tools -

1. Build/package - sbt
2. Ide - eclipse (scala IDE)
3. Http server/routes - akka http
4. Logging - logback
5. Persistency - Cassandra (for the excercise we will use embedded cassandra)

With these we will be creating a simple customer service which support CRUD for a customer resource

```
GET /customer/v1/api HTTP 1.1 
Sample response - 200 OK 
{
  "customers": [
    {
      "id": "f58fa847-f3e4-47f3-9278-193142a77c0b",
      "name": "praveen",
      "age": 34
    }
  ]
}

POST /customers/v1/api HTTP 1.1 
{
  "name": "praveen",
  "age": 34
} 
Sample response - 201 Created

GET /customers/v1/api/f58fa847-f3e4-47f3-9278-193142a77c0b HTTP 1.1 
Sample response - 200 OK
{ 
  "id": "58fa847-f3e4-47f3-9278-193142a77c0b", 
  "name": "praveen", 
  "age": 34
}

PUT /customers/v1/api/ HTTP 1.1 
{
  "name": "praveen",
  "age": 34
} 
Sample response - 200 OK

DELETE /customers/v1/api/ HTTP 1.1 
Sample response - 200 OK
``` 

# Create project

1. We will use sbt as our build and package tooling. There are a number of templates one can start from, some of these are maintained as giter8 templates and are good starting point.

`sbt -Dsbt.version=0.13.15 new akka/akka-http-quickstart-scala.g8`

	**We can start without template as well, in that case one needs configure and specify the project config details on their own.

	`sbt new`
2. Now that we have the project please jump on to the code example in the customer folder. It has the necessary documentation to complete the implementation. For the impatient refer to the branch 'solution' for the concrete implementation.

3. For importing the project into IDEs, `sbt eclipse` when you are inside the customers folder does the trick. You can then import it as existing project in eclipse.

# How to go about it

The code is organized in the directory structure as given below. The source files are marked with TODOs and EXPLOREs (How to [track custom TASKs](https://stackoverflow.com/questions/9296338/how-to-get-custom-task-tags-to-work-in-eclipse) in eclipse?). TODOs are to be completed before you get a functional code. EXPLOREs are good self-reading stopovers. You can go at your own pace and sequence, but I have suggested a sequence in the listing below.
```
customer
├── build.sbt [STEP 1]
├── project [STEP 1]
│   ├── build.properties
│   ├── plugins.sbt
│   └── project
└── src
    ├── main
    │   ├── resources
    │   │   └── cassandra.yaml [STEP 6]
    │   └── scala
    │       └── com
    │           └── meetpraveen
    │               ├── LogUtils.scala [STEP 8]
    │               ├── QuickstartServer.scala [STEP 7]
    │               ├── actor
    │               │   └── CustomerRegistryActor.scala [STEP 3]
    │               ├── model
    │               │   ├── Constants.scala [STEP 2]
    │               │   └── CustomerModel.scala [STEP 2]
    │               ├── persistency
    │               │   ├── CassandraPersistency.scala [STEP 5]
    │               │   └── EmbeddedCassandra.scala [STEP 6] 
    │               └── route
    │                   └── CustomerRoutes.scala [STEP 4]
    └── test
        └── scala
            └── com
                └── meetpraveen
                    └── UserRoutesSpec.scala [TBD]
```
As a cheat sheet there is one example implementation in the solution branch. I would recommend not to refer to it until you are really at a deadend.


