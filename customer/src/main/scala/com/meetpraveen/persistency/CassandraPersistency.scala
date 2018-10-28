package com.meetpraveen.persistency

import java.util.UUID

import scala.concurrent.{ ExecutionContext, Future, Promise }
import scala.language.implicitConversions

import com.datastax.driver.core.{ Cluster, PreparedStatement, ResultSet, Session, SimpleStatement }
import com.google.common.util.concurrent.{ FutureCallback, Futures, ListenableFuture }
import com.meetpraveen.model.{ Customer, Customers }
import com.meetpraveen.model.Constants.{ cassandraPort, cassandraUrl }
import com.meetpraveen.LogUtils.LogEnhancer
import org.slf4j.LoggerFactory
import com.meetpraveen.LogContext

//EXPLORE: Traits can be mixed into other traits, classes and objects
object CqlUtils extends LogContext {

  def execute(statement: Future[PreparedStatement], params: Any*)(implicit executionContext: ExecutionContext, session: Session): Future[ResultSet] = {
    statement.map { x =>
      val para = params.map(identity)
      if (!para.isEmpty) x.bind(para.map(_.asInstanceOf[Object]): _*) else x.bind()
    }
      .flatMap(session.executeAsync(_))
  }

  /* implicit class enables the primary constructor available for implicit conversion
   	in this case, we are creating a string interpolation for prepared statement
  */
  //	EXPOLRE:https://www.scala-lang.org/api/2.12.0/scala/StringContext.html
  implicit class CqlEnhancer(val cql: StringContext) extends AnyVal {

    /* def cql(args: Any*)(implicit session: Session): ListenableFuture[PreparedStatement] = {
    	the implicit def for listenableFutureToFuture enables us to replace the line above
    	with the one below
    */
    def cql(args: Any*)(implicit session: Session): Future[PreparedStatement] = {
      val statement = new SimpleStatement(cql.raw(args: _*))
      session.prepareAsync(statement)
    }
  }

  /* Implicit def provides a way to convert the available type to expected type
   * here, available type is ListenableFuture and expected type is Future
   */
  implicit def listenableFutureToFuture[T](listenableFuture: ListenableFuture[T]): Future[T] = {
    val promise = Promise[T]()
    Futures.addCallback(listenableFuture, new FutureCallback[T] {
      def onFailure(error: Throwable): Unit = {
        //EXPLORE: How is this string interpolation supported. Try to pass any non string param inside ${}
        //how is this behavior showing?
        log"Error:: ${error.getMessage}"
        promise.failure(error)
        ()
      }
      def onSuccess(result: T): Unit = {
        log"Success: ${result.toString}"
        promise.success(result)
        ()
      }
    })
    promise.future
  }

  /* Implicit val makes the value of the type available wherever we expect it in
   * the implicit parameter list
   */
  lazy val cluster = new Cluster.Builder().addContactPoints(cassandraUrl).withPort(cassandraPort.toInt).build()
  implicit lazy val session = cluster.connect()
}

// Persistency definition trait
trait Persistency {
  def getCustomers(): Future[Customers]
  def getCustomer(id: UUID): Future[Option[Customer]]
  def deleteCustomer(id: UUID): Future[Unit]
  def upsertCustomer(customer: Customer): Future[Customer]
  // We don't have a pure insert method. Insert function is required is we don't want to give the
  // onus of generating ids to the clients as its a required param in Customer case class
  //TODO: Declare and implement an insert method and wire it with the POST call 
  //def insertCustomer(customerRequest: CustomerRequest): Future[Customer] 
}

trait CassandraPersistency extends Persistency {
  import com.meetpraveen.persistency.CqlUtils._
  import scala.collection.JavaConverters._
  import scala.concurrent.ExecutionContext.Implicits.global

  override def getCustomers(): Future[Customers] = {
    val query = cql"SELECT * FROM myks.customer"
    //EXPLORE: Calling java library returns java Collections asScala helps us out here
    //EXPLORE: Use of map to open up various kinds of containers, this one is future 
    val resultSet = execute(query).map{
      //EXPLORE: Use of map to open up various kinds of containers, first one is an iterable
      _.asScala.map{row => 
        Customer(UUID.fromString(row.getString("id")), row.getString("name"), row.getInt("age"), row.getString("countryOfResidence"))
      }
    }
    //EXPLORE: What type of container is this map opening out for us?
    val resultList = resultSet.map(_.toList)
    //EXPLORE: identity function is a f(x)=>x
    resultList.transform(Customers(_), identity)
  }

  override def getCustomer(id: UUID): Future[Option[Customer]] = {
    val query = cql"SELECT * FROM myks.customer WHERE id = ?"
    val resultset = execute(query, id.toString).map(_.asScala.toStream.take(1).map(row => Customer(UUID.fromString(row.getString("id")), row.getString("name"), row.getInt("age"), row.getString("countryOfResidence"))))
    resultset.transform(stream => stream.take(1).toList.headOption, identity)
  }
  
  override def upsertCustomer(customer: Customer): Future[Customer] = {
    val query = cql"INSERT INTO myks.customer(id, name, age, countryOfResidence) VALUES(?,?,?,?)"
    val rs = execute(query, customer.id.toString, customer.name.toString, customer.age, customer.countryOfResidence.toString)
    rs.transform(_ => customer, identity)
  }

  override def deleteCustomer(id: UUID): Future[Unit] = {
    val query = cql"DELETE from myks.customer WHERE id = ?"
    execute(query, id.toString).transform(_ => (), identity)
  }
}