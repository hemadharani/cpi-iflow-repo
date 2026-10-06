import com.sap.gateway.ip.core.customdev.util.Message

def Message savePdfAsAttachment(Message message) {
    try {
        // 1. Get the downloaded PDF binary content from the HTTP adapter response
        byte[] fileContent = message.getBody(byte[])
        
        if (fileContent == null || fileContent.length == 0) {
            throw new Exception("The HTTP adapter returned an empty file stream.")
        }

        // 2. Safely attach the raw binary data to the Message Monitor
        def log = messageLogFactory.getMessageLog(message)
        if (log != null) {
            log.addAttachmentAsString("Downloaded_File.pdf", new String(fileContent, "ISO-8859-1"), "application/pdf")
        }

    } catch (Exception e) {
        throw new Exception("Failed to attach PDF file: " + e.getMessage())
    }
    return message
}