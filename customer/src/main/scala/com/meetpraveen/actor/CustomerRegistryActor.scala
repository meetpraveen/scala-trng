package com.meetpraveen.actor

//#customer-registry-actor
import akka.actor.{ Actor, ActorLogging, Props }
import akka.actor.actorRef2Scala
import com.meetpraveen.model.Customer
import com.meetpraveen.model.Customers
import com.meetpraveen.persistency.CassandraPersistency
import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.util.Try

object CustomerRegistryActor {
  final case class InitCassandraSession(cassandraUrl: String, cassandraPort: String)
  final case class ActionPerformed(description: String)
  final case object GetCustomers
  final case class CreateCustomer(customer: Customer)
  final case class UpdateCustomer(customer: Customer)
  final case class GetCustomer(id: UUID)
  final case class DeleteCustomer(id: UUID)

  def props: Props = Props[CustomerRegistryActor]
}

class CustomerRegistryActor extends Actor with ActorLogging with CassandraPersistency {
  import CustomerRegistryActor._

  // EXPLORE: locate the context and assign thread dispatcher to this implicit context
  // TODO: Provide execution context. Each actor has a number of attribute,
  implicit val ex: ExecutionContext = ???

  def receive: Receive = { // Define partial function for the actor, any message not matching here goes to deadletter
    // Each of the cases map to a case class type, make sure its a case class so actors know how to serialize them
    case GetCustomers => {
      //sender() gives a context of actor who has sent the message, in actor system actors sharing the same context
      //can send messages to each other.
      //EXPLORE: Given the context, figure out how else to get references to actors.
      //EXPLORE: Why are we capturing the sender, we could get in after future completes as well. Can we?
      val sndr = sender()
      //TODO: we should avoid using of awaits, refactor using callbacks
      sndr ! Try(Await.result(getCustomers(), 10 seconds))
    }
    case CreateCustomer(customer) => {
      val sndr = sender()
      //TODO: Call upsert in persistency layer
      //sndr ! ???
    }
    case UpdateCustomer(customer) => {
      //TODO: Capture sender
      //TODO: Call upsert in persistency layer
      //sndr ! ???
    }
    case GetCustomer(id) => {
      //TODO: Capture sender
      //TODO: call getCustomer(id) in persistency layer
      //sndr ! ???
    }
    case DeleteCustomer(id) =>
    //TODO: Capture sender
    //TODO: call deleteCustomer(id) in persistency layer
    //sndr ! ???
  }
}
//#customer-registry-actor