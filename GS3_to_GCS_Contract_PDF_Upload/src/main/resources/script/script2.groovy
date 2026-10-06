import com.sap.gateway.ip.core.customdev.util.Message
import java.util.regex.Pattern
import java.net.URLEncoder

def Message extractCsrfAndCookies(Message message) {
    def html = message.getBody(String.class)

    def csrfPattern = Pattern.compile('id="CSRFToken"[^>]*\\bvalue="([^"]+)"')
    def csrfMatcher = csrfPattern.matcher(html)
    if (!csrfMatcher.find()) {
        throw new Exception("CSRF token not found on login page.")
    }
    def csrfToken = csrfMatcher.group(1)
    message.setProperty("CSRF_TOKEN", csrfToken)

    def vsPattern = Pattern.compile('id="__VIEWSTATE"[^>]*\\bvalue="([^"]*)"')
    def vsMatcher = vsPattern.matcher(html)
    message.setProperty("VIEWSTATE", vsMatcher.find() ? vsMatcher.group(1) : "")

    def evPattern = Pattern.compile('id="__EVENTVALIDATION"[^>]*\\bvalue="([^"]*)"')
    def evMatcher = evPattern.matcher(html)
    message.setProperty("EVENTVALIDATION", evMatcher.find() ? evMatcher.group(1) : "")

    def headers = message.getHeaders()
    def cookies = []
    def setCookieHeader = headers.get("Set-Cookie")
    if (setCookieHeader != null) {
        if (setCookieHeader instanceof List) {
            setCookieHeader.each { cookie ->
                cookies.add(cookie.split(";")[0].trim())
            }
        } else {
            cookies.add(setCookieHeader.toString().split(";")[0].trim())
        }
    }
    message.setProperty("SESSION_COOKIES", cookies.join("; "))

    return message
}