package com.meetpraveen

import scala.concurrent.Future
import scala.concurrent.duration._

import com.meetpraveen.actor.CustomerRegistryActor._
import com.meetpraveen.model.{ Customer, Customers, JsonSupport }

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{ delete, get, post }
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import scala.util.Success
import scala.util.Failure
import akka.http.scaladsl.server.ExceptionHandler
import scala.util.Try
import scala.concurrent.ExecutionContext

//#customer-routes-class
trait CustomerRoutes extends JsonSupport {
  //#customer-routes-class

  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[CustomerRoutes])

  // other dependencies that customerRoutes use
  def customerRegistryActor: ActorRef

  // Required by the `ask` (?) method below
  implicit lazy val timeout = Timeout(15.seconds) // usually we'd obtain the timeout from the system's configuration

  implicit val exceptionHandler = ExceptionHandler {
    //TODO: Do more fine grained error handling
    case ex: Throwable => {
      ex.printStackTrace
      complete(StatusCodes.BadRequest, ex.getMessage)
    }
  }
  //#all-routes
  //#customers-get-post
  //#customers-get-delete
  lazy val customerRoutes: Route =
    pathPrefix("customers") {
        //#customers-get-delete
        pathEnd {
            get {
              val customers = (customerRegistryActor ? GetCustomers).mapTo[Try[Customers]]
              complete(customers)
            } ~
            post {
              entity(as[Customer]) { customer =>
                val customerCreated =
               //TODO: Wire this to CreateCustomerRequest
                  (customerRegistryActor ? CreateCustomer(customer)).mapTo[Try[Customer]]
                onSuccess(customerCreated) { performed =>
                  log.info("Created customer [{}]", performed)
                  complete(StatusCodes.Created, performed)
                }
              }
            }
        } ~
        //#customers-get-post
        //#customers-get-delete
        path(JavaUUID) { id =>
            //TODO: Create a new route for POST where id is passed by users. You can use the CreateCustomer(customer)
            //message of the actor to fulfil that request
            //post {
              // TODO: capture the request body 'entity' as 'Customer' 
              //entity(...) { customer =>
                //TODO: Send CreateCustomer(customer) to CustomerRegistryActor 
                //complete(StatusCodes.Created, "")
              //}
            //} ~
            get {
              //#retrieve-customer-info
              //EXPLORE: Notice the use of Option to represent a field which can be there or absent
              val maybecustomer: Future[Try[Option[Customer]]] =
                (customerRegistryActor ? GetCustomer(id)).mapTo[Try[Option[Customer]]]
              implicit val ec: ExecutionContext = system.dispatcher
              val flat = maybecustomer.collect[Option[Customer]] {
                case Success(x) => x
                case Failure(ex) => None
              }
              rejectEmptyResponse {
                complete(maybecustomer)
              }
              //#retrieve-customer-info
            } ~
            delete {
              //#customers-delete-logic
              val customerDeleted: Future[Unit] =
                (customerRegistryActor ? DeleteCustomer(id)).mapTo[Unit]
              onComplete(customerDeleted) { performed =>
                log.info("Deleted customer [{}]", id)
                complete(StatusCodes.OK, "")
              }
              //#customers-delete-logic
            }
        }
      //#customers-get-delete
    }
  //#all-routes
}
