package controllers

import play.api.Logger
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.libs.mailer.{Email, MailerPlugin}
import play.api.libs.ws.WS
import play.api.mvc._

import scala.concurrent.Future

object Application extends Controller {

  case class MailForm(name: String, email: String, subject: String, message: String)

  implicit val format = Json.format[MailForm]

  val mailForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "email" -> email,
      "subject" -> nonEmptyText,
      "message" -> nonEmptyText
    )(MailForm.apply)(MailForm.unapply)
  )

  def index = Action {
    Ok(views.html.index())
  }

  def showEmail = Action(parse.json) { implicit req =>
    mailForm.bind(req.body).fold(
      errors => BadRequest,
      mail => Ok(
        views.html.emails.email(
          mail.name,
          mail.email,
          mail.subject,
          mail.message
        )
      )
    )
  }

  def sendMail = Action.async { implicit req =>
    mailForm.bindFromRequest().fold(
      errors => Future(BadRequest),
      mail => {
        Logger.debug(s"email : ${mail.name} (${mail.email}) - Subject : ${mail.subject} - Message : ${mail.message}")

        WS.url(routes.Application.showEmail().absoluteURL()).post(
          Json.toJson(mail)
        ).map(resp => {
          val email = Email(
            "Prise de contact par " + mail.name,
            mail.email,
            Seq("w.alexisweil@gmail.com"),
            bodyHtml = Some(resp.body)
          )

          val res = MailerPlugin.send(email)
          Logger.debug(s"Envoi du mail : $res")

          Ok
        })
      }
    )
  }

}