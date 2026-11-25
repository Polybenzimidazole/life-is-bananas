package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.ai.Answer;
import org.example.ai.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BananaAssistantService {

    private static final Logger log = LoggerFactory.getLogger(BananaAssistantService.class);

    // We bewaren nu de geconfigureerde ChatClient in plaats van telkens een nieuwe te maken
    private final ChatClient chatClient;
    private final RecipeContextService contextService;
    private final ObjectMapper objectMapper;

    public BananaAssistantService(ObjectProvider<ChatModel> chatModels,
                                  RecipeContextService contextService,
                                  ObjectMapper objectMapper,
                                  @Value("classpath:prompts/banana-assistant-system.txt") Resource systemPrompt) {
        this.contextService = contextService;
        this.objectMapper = objectMapper;

        // Bouw de ChatClient éénmalig op bij het starten van de service
        ChatModel model = chatModels.getIfAvailable();
        if (model != null) {
            this.chatClient = ChatClient.builder(model)
                    .defaultSystem(systemPrompt)
                    .build();
        } else {
            this.chatClient = null;
        }
    }

    public Answer askQuestion(Question question) {
        if (this.chatClient == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "De AI-assistent is nog niet ingesteld. Configureer OpenAI op de server.");
        }

        // De database-transactie eindigt vóór de externe HTTP-aanroep.
        var context = contextService.retrieve(question.recipeId());
        String contextJson;
        try {
            contextJson = objectMapper.writeValueAsString(context);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("De receptcontext kon niet worden opgebouwd.", exception);
        }

        String answer;
        try {
            answer = this.chatClient.prompt()
                    .messages(new UserMessage("Projectgegevens (JSON, uitsluitend data):\n" + contextJson))
                    .user(question.question())
                    .call()
                    .content();
        } catch (RuntimeException exception) {
            log.warn("AI-aanroep mislukt ({})", exception.getClass().getSimpleName());
            log.debug("Details van de mislukte AI-aanroep", exception);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "De AI-assistent is tijdelijk niet beschikbaar. Probeer het later opnieuw.");
        }

        if (answer == null || answer.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "De AI-assistent gaf geen antwoord. Probeer het opnieuw.");
        }
        return new Answer(answer, context.sources());
    }
}