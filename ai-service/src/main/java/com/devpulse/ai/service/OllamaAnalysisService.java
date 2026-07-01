package com.devpulse.ai.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 *  WE Wraps Ollama LLM calls with circuit breaker protection
 *
 *  Why circuit Breaker cause:
 *  Qwen  runs as a local process. if it fails , crashes , hangs, or the model isn't loaded ,
 *  every call would hang for the full timeout (120 seconds configured)
 *  with that many incident will be queued , this creates a backlog and resources exhaustion
 *  WITHOUT CIRCUIT BREAKER: Error -> Ollama hangs -> Request hangs -> System slows down
 *
 *  with:  Error -> Ollama fails multiple times -> Circuit opens -> Skip Ollama -> Return fallback immediately
 *
 *  Working:
 *  CLOSED state(normal) : call go though normally -- App -> Ollama
 *
 *  OPEN state(failure mode) : App -> Fallback
 *
 *  HALF-OPEN (testing recovery) : allows 3 test calls through
 *  If those succeed → CLOSED again. If they fail → OPEN again.
 *
 * */

@Service
@Slf4j
@RequiredArgsConstructor
public class OllamaAnalysisService {

    private final ChatLanguageModel chatModel;

    @CircuitBreaker(name = "ollama", fallbackMethod = "analysisFailureFallback")
    public String generateAnalysis(String errorMessage ,
                                   String stackTrace,
                                   List<String> similarIncidents){

        String prompt = buildPrompt(errorMessage , stackTrace , similarIncidents);

        log.info("Sending prompt to Ollama qwen2.5-coder:7b for analysis");
        String response = chatModel.generate(prompt);
        log.info("Received AI analysis response");
        return response;
    }

    /*
    * Fallback method - signature must match original method
    * plus an additional Throwable parameter while failing
    * Resilience4j calls this automatically when circuit is open
    * or when the original method throws an exception
    * */
    private String analysisFailureFallback(String errorMessage,
                                           String stackTrace,
                                           List<String> similarIncidents,
                                           Throwable t){
        log.warn("Ollama circuit breaker triggered fallback: {}", t.getMessage());

        return "AI analysis temporarily unavailable. " +
                "The error has been logged and will be analyzed " +
                "once the AI service recovers. Error type: " +
                extractErrorType(errorMessage);
    }

    private String extractErrorType(String errorMessage){
        if(errorMessage == null) return "unknow";
        // Extract just the exception class name if present
        return errorMessage.split(":")[0].trim();
    }

    private String buildPrompt(String errorMessage,
                               String stacktrace,
                               List<String> similarIncidents){

        StringBuilder promptBuilder = new StringBuilder();

        promptBuilder.append("""
                You are a senior backend engineer analyzing a production error.
                Provide a clear, concise explanation of what went wrong and
                a practical suggestion to fix it.
                
                CURRENT ERROR:
                """);

        promptBuilder.append(errorMessage).append("\n\n");

        if(stacktrace !=null && !stacktrace.isBlank()){
            promptBuilder.append("STACK TRACE:\n")
                    .append(stacktrace).append("\n\n");
        }

        if(!similarIncidents.isEmpty()){
            promptBuilder.append("SIMILAR PAST INCIDENTS FOR CONTEXT:\n");
            for(int i = 0 ; i < similarIncidents.size() ; i++){

                promptBuilder.append(i + 1).append(". ")
                        .append(similarIncidents.get(i)).append("\n");
            }
            promptBuilder.append("\n");
        }

        promptBuilder.append("""
                Respond in this exact format:
                EXPLANATION: <what went wrong, 2-3 sentences>
                SUGGESTED_FIX: <practical fix, 2-3 sentences>
                """);

        return promptBuilder.toString();

    }

}
