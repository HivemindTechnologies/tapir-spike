package example

import java.util.concurrent.atomic.AtomicReference

import cats.effect._
import cats.syntax.all._
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli._
import sttp.tapir._
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._
import sttp.tapir.openapi.OpenAPI
import sttp.tapir.openapi.circe.yaml._
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.http4s.SwaggerHttp4s

import scala.concurrent.ExecutionContext

object Main extends App {
  // endpoint descriptions
  case class Author(name: String)
  case class Book(title: String, year: Int, author: Author)

  val booksListing: Endpoint[Unit, Unit, Vector[Book], Any] = endpoint.get
    .in("books")
    .in("list" / "all")
    .out(jsonBody[Vector[Book]])

  val addBook: Endpoint[Book, Unit, Unit, Any] = endpoint.post
    .in("books")
    .in("add")
    .in(
      jsonBody[Book]
        .description("The book to add")
        .example(Book("Pride and Prejudice", 1813, Author("Jane Austen")))
    )

  // server-side logic
  implicit val ec: ExecutionContext           = scala.concurrent.ExecutionContext.Implicits.global
  implicit val contextShift: ContextShift[IO] = IO.contextShift(ec)
  implicit val timer: Timer[IO]               = IO.timer(ec)

  val books = new AtomicReference(
    Vector(
      Book("The Sorrows of Young Werther", 1774, Author("Johann Wolfgang von Goethe")),
      Book("Iliad", -8000, Author("Homer")),
      Book("Nad Niemnem", 1888, Author("Eliza Orzeszkowa")),
      Book("The Colour of Magic", 1983, Author("Terry Pratchett")),
      Book("The Art of Computer Programming", 1968, Author("Donald Knuth")),
      Book("Pharaoh", 1897, Author("Boleslaw Prus"))
    )
  )

  val booksListingRoutes: HttpRoutes[IO] = Http4sServerInterpreter.toRoutes(booksListing)(_ => IO(books.get().asRight[Unit]))
  val addBookRoutes: HttpRoutes[IO] =
    Http4sServerInterpreter.toRoutes(addBook)(book => IO((books.getAndUpdate(books => books :+ book): Unit).asRight[Unit]))
  val routes: HttpRoutes[IO] = booksListingRoutes <+> addBookRoutes

  // generating the documentation in yml; extension methods come from imported packages
  val openApiDocs: OpenAPI = OpenAPIDocsInterpreter.toOpenAPI(List(booksListing, addBook), "The tapir library", "1.0.0")
  val openApiYml: String   = openApiDocs.toYaml

  // starting the server
  BlazeServerBuilder[IO](ec)
    .bindHttp(8080, "localhost")
    .withHttpApp(Router("/" -> (routes <+> new SwaggerHttp4s(openApiYml).routes[IO])).orNotFound)
    .resource
    .use { _ =>
      IO {
        println("Go to: http://localhost:8080/docs")
        println("Press any key to exit ...")
        scala.io.StdIn.readLine()
      }
    }
    .unsafeRunSync()
}
