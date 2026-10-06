import com.sap.gateway.ip.core.customdev.util.Message
import com.sap.it.api.ITApiFactory
import com.sap.it.api.securestore.SecureStoreService

def Message Configuration(Message message) {
    try {
        // 1. Retrieve secure credentials using native Groovy typing
        def secureStore = ITApiFactory.getApi(SecureStoreService, null)
        def credential  = secureStore.getUserCredential("GS3_CREDENTIALS")
        
        if (!credential) {
            throw new Exception("Secure Credential 'GS3_CREDENTIALS' is missing from the tenant store.")
        }
        
        // 2. Extract values and commit cleanly to memory properties
        message.setProperty("GS3_USERNAME", credential.username)
        message.setProperty("GS3_PASSWORD", credential.password as String)
        
        // 3. Define configuration baseline routes
        message.setProperty("GS3_LOGIN_URL", "https://gs3.whirlpool.com/page.aspx/en/usr/login") 
        message.setHeader("CamelHttpMethod", "GET")

    } catch (Exception e) {
        throw new Exception("Failed to initialize configuration: ${e.message}")
    }
    return message
}