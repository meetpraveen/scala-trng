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

POST /customer/v1/api HTTP 1.1 
{
  "name": "praveen",
  "age": 34
} 
Sample response - 201 Created

GET /customer/v1/api/f58fa847-f3e4-47f3-9278-193142a77c0b HTTP 1.1 
Sample response - 200 OK
{ 
  "id": "58fa847-f3e4-47f3-9278-193142a77c0b", 
  "name": "praveen", 
  "age": 34
}

PUT /customer/v1/api/ HTTP 1.1 
{
  "name": "praveen",
  "age": 34
} 
Sample response - 200 OK

DELETE /customer/v1/api/ HTTP 1.1 
Sample response - 200 OK
``` 

# Create project

1. We will use sbt as our build and package tooling. There are a number of templates one can start from, some of these are maintained as giter8 templates and are good starting point.

`sbt -Dsbt.version=0.13.15 new akka/akka-http-quickstart-scala.g8`

	_We can start without template as well, in that case one needs configure and specify the project config details on their own._

	`sbt new`
2. Now that we have the project please jump on to the code example in the customer folder. It has the necessary documentation to complete the implementation. For the impatient refer to the branch 'implementation' for the concrete implementation.

# Running

```shell
~/git/scala-trng$> cd customer
~/git/scala-trng/customer$> sbt run
...
...
Server online at http://127.0.0.1:8080/
~/git/scala-trng/customer$> curl http://127.0.0.1:8080/customers -vv
*   Trying 127.0.0.1...
* TCP_NODELAY set
* Connected to 127.0.0.1 (127.0.0.1) port 8080 (#0)
> GET /customers HTTP/1.1
> Host: 127.0.0.1:8080
> User-Agent: curl/7.54.0
> Accept: */*
>
< HTTP/1.1 200 OK
< X-Correlation-Header: d6b4877d-d2e2-44de-b973-5e83e992960f
< Server: akka-http/10.1.4
< Date: Fri, 31 Jan 2020 09:00:38 GMT
< Content-Type: application/json
< Content-Length: 112
<
* Connection #0 to host 127.0.0.1 left intact
{"customers":[{"id":"a7358c40-b77d-4171-b0f6-1e86c1353d08","name":"test","age":1,"countryOfResidence":"India"}]}%
```

