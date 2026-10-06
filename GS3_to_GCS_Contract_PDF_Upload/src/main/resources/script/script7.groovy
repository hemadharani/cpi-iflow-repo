import com.sap.gateway.ip.core.customdev.util.Message;
import java.io.*;

def Message processData(Message message) {
    Reader reader = message.getBody(Reader.class);
    BufferedReader br = new BufferedReader(reader);
    
    String headerLine = br.readLine();
    if (headerLine == null) return message; // Empty file protection
    
    // Parse raw headers first to find the URL column position before sanitizing names
    List<String> rawHeaders = parseCsvLine(headerLine);
    
    // Find the column index for the URL. Replace 'LinkedSource' with your exact CSV column header name if different.
    int urlColumnIndex = rawHeaders.findIndexOf { it.equalsIgnoreCase("file_download_link") || it.toLowerCase().contains("url") || it.toLowerCase().contains("download") }
    
    // Parse headers and clean up illegal XML tag characters (spaces, ?, /, -)
    List<String> headers = rawHeaders.collect { header ->
        header.replaceAll(/[^a-zA-Z0-9_]/, "") // Keep only letters, numbers, underscores
              .replaceAll(/^([0-9])/, "_\$1")   // XML tags can't start with a number
    }
    
    StringBuilder xml = new StringBuilder();
    xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<ContractsFile>\n");
    
    String line;
    while ((line = br.readLine()) != null) {
        if (line.trim().isEmpty()) continue;
        List<String> values = parseCsvLine(line);
        
        // FILTER: If a URL column was found, check if this specific row's URL value is missing or blank
        if (urlColumnIndex != -1) {
            String urlCheck = urlColumnIndex < values.size() ? values[urlColumnIndex].trim() : "";
            if (urlCheck.isEmpty()) {
                continue; // Omit the whole row and skip to the next loop iteration
            }
        }
        
        xml.append("    <Contract>\n");
        for (int i = 0; i < headers.size(); i++) {
            String tagName = headers[i].isEmpty() ? "Field_" + i : headers[i];
            String val = i < values.size() ? values[i] : "";
            // Escape standard XML characters to prevent syntax corruption
            val = val.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
            xml.append("        <").append(tagName).append(">").append(val).append("</").append(tagName).append(">\n");
        }
        xml.append("    </Contract>\n");
    }
    xml.append("</ContractsFile>");
    
    message.setBody(xml.toString());
    return message;
}

// Helper method to parse CSV lines while respecting data enclosure double quotes
List<String> parseCsvLine(String line) {
    List<String> tokens = new ArrayList<>();
    StringBuilder sb = new StringBuilder();
    boolean inQuotes = false;
    for (int i = 0; i < line.length(); i++) {
        char c = line.charAt(i);
        if (c == '"') {
            inQuotes = !inQuotes;
        } else if (c == ',' && !inQuotes) {
            tokens.add(sb.toString().trim());
            sb.setLength(0);
        } else {
            sb.append(c);
        }
    }
    tokens.add(sb.toString().trim());
    return tokens;
}