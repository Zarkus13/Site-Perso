package controllers

import com.sendgrid.SendGrid
import com.sendgrid.SendGrid.Email
import play.api.Play
import play.api.libs.json.Json
import play.api.libs.ws.WS
import play.api.mvc._
import play.api.data.Forms._
import play.api.data.Form
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future
import play.api.Play.current

object Application extends Controller {

  case class MailForm(name: String, email: String, subject: String, message: String)

  implicit val format = Json.format[MailForm]

  val sendgridUsername = Play.configuration.getString("sendgrid.username").get
  val sendgridPassword = Play.configuration.getString("sendgrid.password").get
  val sendgrid = new SendGrid(sendgridUsername, sendgridPassword)

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
        WS.url(routes.Application.showEmail().absoluteURL()).post(
          Json.toJson(mail)
        ).map(resp => {
          val email = new Email()
          email.addTo("w.alexisweil@gmail.com")
          email.setFrom(mail.email)
          email.setSubject("Prise de contact par " + mail.name)
          email.setHtml(resp.body)

          Ok
        })
      }
    )
  }

}