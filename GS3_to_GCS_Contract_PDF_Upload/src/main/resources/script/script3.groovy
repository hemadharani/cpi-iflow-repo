import com.sap.gateway.ip.core.customdev.util.Message
import java.net.URLEncoder

def Message Setuplogin(Message message) {
    def username = message.getProperty("GS3_USERNAME")
    def password = message.getProperty("GS3_PASSWORD")
    def csrfToken = message.getProperty("CSRF_TOKEN")
    def viewState = message.getProperty("VIEWSTATE")
    def eventValidation = message.getProperty("EVENTVALIDATION")
    def cookies = message.getProperty("SESSION_COOKIES")

    def params = []
    params.add("__EVENTTARGET=" + URLEncoder.encode("body:x:btnLogin", "UTF-8"))
    params.add("__EVENTARGUMENT=")
    params.add("body:x:txtLogin=" + URLEncoder.encode(username, "UTF-8"))
    params.add("body:x:txtPass=" + URLEncoder.encode(password, "UTF-8"))
    params.add("body:x:btnLogin=")
    params.add("CSRFToken=" + URLEncoder.encode(csrfToken, "UTF-8"))
    if (viewState) {
        params.add("__VIEWSTATE=" + URLEncoder.encode(viewState, "UTF-8"))
    }
    if (eventValidation) {
        params.add("__EVENTVALIDATION=" + URLEncoder.encode(eventValidation, "UTF-8"))
    }

    message.setBody(params.join("&"))
    message.setHeader("CamelHttpMethod", "POST")
    message.setHeader("Content-Type", "application/x-www-form-urlencoded")
    message.setHeader("Cookie", cookies)

    return message
}