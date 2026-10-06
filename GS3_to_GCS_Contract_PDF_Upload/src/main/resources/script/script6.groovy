import com.sap.gateway.ip.core.customdev.util.Message
def Message validateLogin(Message message) {
    def responseBody = message.getBody(String.class)
    def headers = message.getHeaders()

    def loginFailed = false
    def reason = ""

    if (responseBody.contains('id="CSRFToken"') && responseBody.contains('txtLogin')) {
        loginFailed = true
        reason = "Response still contains login form"
    }

    def setCookieHeader = headers.get("Set-Cookie")
    def existingCookies = message.getProperty("SESSION_COOKIES") ?: ""
    def cookieMap = [:]

    existingCookies.split("; ").each { c ->
        def parts = c.split("=", 2)
        if (parts.length == 2) cookieMap[parts[0]] = parts[1]
    }

    if (setCookieHeader != null) {
        def newCookies = (setCookieHeader instanceof List) ? setCookieHeader : [setCookieHeader.toString()]
        newCookies.each { cookie ->
            def clean = cookie.split(";")[0].trim()
            def parts = clean.split("=", 2)
            if (parts.length == 2) cookieMap[parts[0]] = parts[1]
        }
    }

    def mergedCookies = cookieMap.collect { k, v -> "${k}=${v}" }.join("; ")
    message.setProperty("SESSION_COOKIES", mergedCookies)

    def log = messageLogFactory.getMessageLog(message)

    if (loginFailed) {
        log.setStringProperty("LoginStatus", "FAILED")
        throw new Exception("GS3 Login Failed: ${reason}")
    }

    log.setStringProperty("LoginStatus", "SUCCESS")
    log.addAttachmentAsString("Authenticated_Cookies", mergedCookies, "text/plain")

    return message
}