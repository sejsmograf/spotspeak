package com.example.spotspeak.service;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.example.spotspeak.entity.Trace;

import lombok.extern.slf4j.Slf4j;

@Service
@Profile({ "local", "remote" })
@Slf4j
public class AIEventNamingService implements EventNamingService {

    private final ChatClient chatClient;

    private final String NAME_EVENT_PROMPT = """
            You are tasked with generating an event name based on a collection of traces. Each trace contains a description of an action or event, and you should extract an appropriate event name from the collection of descriptions provided.

            Please focus on understanding the key theme or activity described in the traces and return a concise and precise event name. Do not provide any extra information, context, or elaboration—just the event name.

            Example:
            If the descriptions of the traces are:
            traces: [
                {description: 'Sędzia zakończył mecz.'},
                {description: 'Zawodnicy podziękowali kibicom.'},
                {description: 'Ładna bramka'},
                {description: 'Zamieszki na trybunach'},
                {description: 'Legia Warszawa wygrała mecz.'},
            ]

            The event name should be: "Mecz Legii Warszawa".

            Now, based on the following descriptions, please provide the event name (do not provide any additional text, just the event name, without quotes):

            """;

    public AIEventNamingService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public String getEventName(List<Trace> associatedTraces) {
        try {
            String prompt = NAME_EVENT_PROMPT + getTracesJson(associatedTraces);
            log.info("Prompting AI with traces: " + prompt);

            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            return response;
        } catch (Exception e) {
            return "Event";
        }
    }

    private String getTracesJson(List<Trace> traces) {
        StringBuilder sb = new StringBuilder("traces: [\n");

        for (Trace trace : traces) {
            sb.append("{description: '").append(trace.getDescription()).append("'},\n");
        }

        return sb.append("]").toString();
    }

}
