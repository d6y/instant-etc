
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import java.time.Instant

// Example of overriding the Instant mapping in 3.3.0

// Construct our own Profile, extending from whatever Profile we want to modify.
// I'm using H2 as an example.
trait MyH2Profile extends slick.jdbc.JdbcProfile with slick.jdbc.H2Profile {

  import java.sql.{PreparedStatement, ResultSet}
  import slick.ast.FieldSymbol

  override val columnTypes = new JdbcTypes

  // Customise the types...
  class JdbcTypes extends super.JdbcTypes {

    //...specifically the instant type:
    override val instantType = new InstantJdbcType {

      // I'm mapping this to a lONG
      override def sqlTypeName(sym: Option[FieldSymbol]) = "BIGINT"

      // Storing the instant in seconds:
      override def setValue(v: Instant, p: PreparedStatement, idx: Int): Unit = p.setLong(idx, v.getEpochSecond())

      // Read back from a Long:
      override def getValue(r: ResultSet, idx: Int) : Instant = Instant.ofEpochSecond(r.getLong(idx))

      // Update as a Long
      override def updateValue(v: Instant, r: ResultSet, idx: Int) = r.updateLong(idx, v.getEpochSecond())

      // The SQL literal is a number:
      override def valueToSQLLiteral(value: Instant) : String = s"${value.getEpochSecond()}"
    }
  }
}

object MyH2Profile extends MyH2Profile

import MyH2Profile.api._

object Example extends App {

  final case class Message(
    sender:  String,
    when:    Instant,
    content: Option[String],
    id:      Long = 0L)

  def freshTestData = Seq(
    Message("Dave", Instant.now(), Some("Hello, HAL. Do you read me, HAL?")),
    Message("HAL",  Instant.now(), Some("Affirmative, Dave. I read you.")),
    Message("Dave", Instant.now(), Some("Open the pod bay doors, HAL.")),
    Message("HAL",  Instant.now(), Some("I'm sorry, Dave. I'm afraid I can't do that.")),
    Message("Dave", Instant.now(), None)
  )


  final class MessageTable(tag: Tag) extends Table[Message](tag, "message") {

    def id      = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def sender  = column[String]("sender")
    def when    = column[Instant]("ts_when")
    def content = column[Option[String]]("content")

    def * = (sender, when, content, id).mapTo[Message]
  }

  lazy val messages = TableQuery[MessageTable]

  val q = messages

  val program = for {
    _ <- messages.schema.create
    _ = println(messages.schema.create.statements)
    _ <- messages ++= freshTestData
    results <- q.result
  } yield results

  val db = Database.forConfig("example")
  try 
    println(
      Await.result(db.run(program), 2.seconds)
    )
  finally db.close
}
