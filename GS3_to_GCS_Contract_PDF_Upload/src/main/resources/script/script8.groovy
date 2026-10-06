import com.sap.gateway.ip.core.customdev.util.Message;

def Message processData(Message message) {
    def body = message.getBody(String.class);
    def sizeHeader = message.getHeaders().get("CamelFileLength");
    long fileSize = sizeHeader != null ? sizeHeader.toLong() : 0;
    
    // Check if size is small OR if body is completely empty/whitespace
    if (fileSize <= 1063 || body == null || body.trim().isEmpty()) {
        message.setProperty("isEmptyFile", "true");
    } else {
        message.setProperty("isEmptyFile", "false");
    }
    
    return message;
}