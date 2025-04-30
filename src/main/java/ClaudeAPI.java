import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.*;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.Base64;
import java.util.List;

public class ClaudeAPI {

    private static String API_KEY = "";
    /*
    Tokens - limits AI response, saves money on API hits
    Temperature - randomness of the text generation (0-1.0, higher number means more random, creative, varied answers)
    Stop Sequence - a string of text that, when encountered during text generation,
    causes the model to stop generating further output
    System Prompt - a way to provide instructions and context to the AI before it interacts with the user.
    It's essentially a set of guidelines that helps Claude understand the type of role, tone, and style it should adopt during a conversation.
   */
    public static void chat(String query) {

        AnthropicClient client = AnthropicOkHttpClient.builder()
                .apiKey(API_KEY)
                .build();

        MessageCreateParams createParams = MessageCreateParams.builder()
                .model(Model.CLAUDE_3_5_SONNET_LATEST)
                .maxTokens(2048)
                .addUserMessage(query)
                .temperature(0)
                .addStopSequence("stop")
                .system("You are a snarky CEO")
                .build();

        client.messages().create(createParams).content().stream()
                .flatMap(contentBlock -> contentBlock.text().stream())
                .forEach(textBlock -> System.out.println(textBlock.text()));

    }

    private static DocumentBlockParam readPDF(File file) throws IOException {
        byte[] fileData = new byte[(int) file.length()];
        FileInputStream in = new FileInputStream(file);
        in.read(fileData);
        in.close();
        String pdfBase64 = Base64.getEncoder().encodeToString(fileData);
        DocumentBlockParam documentParam = DocumentBlockParam.builder()
                .base64PdfSource(pdfBase64)
                .build();

        return documentParam;

    }

    public static void summarizePDF(File file) throws IOException {

        DocumentBlockParam documentParam = readPDF(file);
        MessageCreateParams params = MessageCreateParams.builder()
                .model(Model.CLAUDE_3_7_SONNET_LATEST)
                .maxTokens(1024)
                .addUserMessageOfBlockParams(
                        List.of(
                                ContentBlockParam.ofDocument(documentParam),
                                ContentBlockParam.ofText(TextBlockParam.builder().text("Summarize this document").build())
                        )
                )
                .build();

        AnthropicClient client = AnthropicOkHttpClient.builder()
                .apiKey(API_KEY) // removed API key
                .build();

        Message message = client.messages().create(params);
        message.content().stream()
                .flatMap(contentBlock -> contentBlock.text().stream())
                .forEach(textBlock -> System.out.println(textBlock.text()));

    }

    public static void toQTIQuiz(File file) throws IOException, ParserConfigurationException, SAXException, TransformerException {
        DocumentBlockParam documentParam = readPDF(file);
        MessageCreateParams params = MessageCreateParams.builder()
                .model(Model.CLAUDE_3_7_SONNET_LATEST)
                .maxTokens(5000)
                .addStopSequence("</questestinterop>")
                .addUserMessageOfBlockParams(
                        List.of(
                                ContentBlockParam.ofDocument(documentParam),
                                ContentBlockParam.ofText(TextBlockParam.builder().text("From this PDF, generate a multiple choice quiz " +
                                        "with 2 questions, in QTI format in a .xml file").build())
                        )
                )
                .build();

        AnthropicClient client = AnthropicOkHttpClient.builder()
                .apiKey(API_KEY) // removed API key
                .build();

        Message message = client.messages().create(params);
        System.out.println(message.content().getFirst().text().get().text().indexOf("<"));
        int startIndex = message.content().getFirst().text().get().text().indexOf("<");
        String xmlSource = message.content().getFirst().text().get().text().substring(startIndex) + "\n</questestinterop>";

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(new InputSource(new StringReader(xmlSource)));
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();

        String desktopPath = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "quiz.xml";
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(desktopPath));
        transformer.transform(source, result);

    }



    //byte[]
    // DocumentBlockParam -> specific to Claude
}
