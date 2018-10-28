package com.meetpraveen.route

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
import scala.util.{ Try, Success, Failure }
import akka.http.scaladsl.server.ExceptionHandler
import scala.concurrent.ExecutionContext
import com.meetpraveen.LogContext
import com.meetpraveen.LogUtils._

//#customer-routes-class
trait CustomerRoutes extends JsonSupport {
  //EXPLORE: Note the use of self type declaration. Its a great
  //way to effect dependency injection with inheritance rather than composition
  self: LogContext =>
  //#customer-routes-class

  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem

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
      pathEndOrSingleSlash {
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
                log"Created customer [${performed.toString}]"
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
          //TODO: capture the request body 'entity' as 'Customer'
          //entity(...) { customer =>
          //TODO: Send CreateCustomer(customer) to CustomerRegistryActor
          //complete(StatusCodes.Created, "")
          //}
          //} ~
          get {
            //#retrieve-customer-info
            //EXPLORE: Notice the use of Option to represent a field which can be there or absent
            val maybecustomer =
              (customerRegistryActor ? GetCustomer(id)).mapTo[Try[Option[Customer]]]
            onSuccess(maybecustomer) { customer =>
              rejectEmptyResponse {
                complete(maybecustomer)
              }
            }
            //#retrieve-customer-info
          } ~
            delete {
              //#customers-delete-logic
              val customerDeleted =
                (customerRegistryActor ? DeleteCustomer(id)).mapTo[Try[Unit]]
              onSuccess(customerDeleted) { performed =>
                log"Deleted customer [${id.toString}]"
                complete(StatusCodes.OK, "Deleted")
              }
              //#customers-delete-logic
            }
        }
      //#customers-get-delete
    }
  //#all-routes
}
