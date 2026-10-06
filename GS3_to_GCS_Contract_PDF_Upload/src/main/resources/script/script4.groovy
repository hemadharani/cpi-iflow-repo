import com.sap.gateway.ip.core.customdev.util.Message

def Message setupDownload(Message message) {
    try {
        // 1. Fetch your dynamic download URL
        def fileUrl = message.getProperty("GS3_DYNAMIC_FILE_URL") ?: ""
        
        // 2. Fetch initial cookies and grab the new ones from the login response headers
        def initialCookies = message.getProperty("SESSION_COOKIES") ?: ""
        def newCookieHeader = message.getHeaders().get("Set-Cookie")
        
        def cookieMap = [:]
        
        // Helper to parse individual "key=value" pieces safely
        def parseKeyValuePair = { item ->
            if (item) {
                def cleanPair = item.toString().split(";")[0].trim()
                def parts = cleanPair.split("=")
                if (parts.length == 2) {
                    cookieMap[parts[0].trim()] = parts[1].trim()
                }
            }
        }
        
        // 3. Process initial cookies (split them by semicolon first)
        initialCookies.split(";").each { parseKeyValuePair(it) }
        
        // 4. Process new incoming cookies from Receiver 1
        if (newCookieHeader) {
            [newCookieHeader].flatten().each { parseKeyValuePair(it) }
        }
        
        // 5. Combine them back into a clean string layout
        def finalCookies = cookieMap.collect { k, v -> "${k}=${v}" }.join("; ")

        // 6. Reset message body and headers to prevent route pollution
        message.setBody(null)
        message.getHeaders().clear()
        
        // 7. Apply parameters directly to the final dynamic HTTP adapter
        message.setHeader("CamelHttpUri", fileUrl.trim())
        message.setHeader("CamelHttpMethod", "GET")
        message.setHeader("Cookie", finalCookies)
        message.setHeader("SAPCamelAllowedHTTPRequestHeaders", "Cookie")

    } catch (Exception e) {
        throw new Exception("Error in download setup: " + e.getMessage())
    }
    return message
}