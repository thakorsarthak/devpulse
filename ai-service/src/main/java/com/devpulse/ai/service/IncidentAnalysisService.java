package com.devpulse.ai.service;


import com.devpulse.ai.dto.AiIncidentAnalyzedMessage;
import com.devpulse.ai.dto.ErrorSpikeMessage;
import com.devpulse.ai.kafka.AiKafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * full RAG pipeline
 *
 * flow:
 * 1  Retrieve similar past incidents from chromaDb
 * 2 Send error + context to Ollama
 * 3 parse the structured response
 * 4 store this incident in chromadb for future retrieval
 * 5 publish result to kafka for notification-service
 *
 * WHY storing only successfully analyzed cause if we stored before analysis ,
 * failed analysis would pollute our knowledge base with incomplete data
 *
 * */

@Slf4j
@Service
@RequiredArgsConstructor
public class IncidentAnalysisService {

    private final IncidentRetrievalService retrievalService;
    private final  OllamaAnalysisService ollamaService;
    private final AiKafkaProducer kafkaProducer;

    private static final Pattern EXPLANATION_PATTERN =
            Pattern.compile("EXPLANATION:\\s*(.+?)(?=SUGGESTED_FIX:|$)" ,
                    Pattern.DOTALL);

    private static final Pattern FIX_PATTERN =
            Pattern.compile("SUGGESTED_FIX:\\S*(.+)", Pattern.DOTALL);

    public void analyzeIncident(ErrorSpikeMessage errorSpike){

        log.info("Starting RAG analysis for service: {}",
                errorSpike.getServiceName());

        // step 1 : retrieve similar past incidents
        List<String> similarIncidents = retrievalService.
                findSimilarIncident(
                        errorSpike.getErrorMessage(),
                        errorSpike.getStackTrace(),
                        errorSpike.getServiceName());


        // Step 1.5: Retrieve relevant source code (NEW)
        List<String> relevantCode = retrievalService
                .findRelevantSourceCode(errorSpike.getStackTrace(),
                        errorSpike.getServiceName());

        List<String> combinedContext = new java.util.ArrayList<>();
        combinedContext.addAll(relevantCode);
        combinedContext.addAll(similarIncidents);

        // step 2 : generate analysis (circuit breaker)
        String rawResponse = ollamaService.generateAnalysis(
                errorSpike.getErrorMessage(),
                errorSpike.getStackTrace(),
                combinedContext);

        //step 3 : parse structured response
        String explanation = extractSection(rawResponse,EXPLANATION_PATTERN , rawResponse);

        String suggestedFix= extractSection(rawResponse , FIX_PATTERN , "No specific fix Suggested");

        /*why this line , cause when we have exactly same first line when our analysis failed whatever reason....*/
        boolean wasSuccessful = !rawResponse.startsWith("Ai analysis temporarily unavailable");


        //step 4: store for future retrieval (ONLY IF SUCCESSFUL)
        if(wasSuccessful){
            retrievalService.storeIncident(
                    errorSpike.getServiceName(),
                    errorSpike.getErrorMessage(),
                    explanation,
                    errorSpike.getServiceName());
        }

        //step 5: publish result
        AiIncidentAnalyzedMessage result = AiIncidentAnalyzedMessage
                .builder()
                .errorLogId(errorSpike.getErrorLogId())
                .serviceName(errorSpike.getServiceName())
                .originalError(errorSpike.getErrorMessage())
                .aiExplanation(explanation)
                .suggestedFix(suggestedFix)
                .analysisSuccessful(wasSuccessful)
                .analyzedAt(LocalDateTime.now())
                .build();

        kafkaProducer.publishAnalysis(result);
        log.info("Completed RAG analysis for service : {} success: {}", errorSpike.getServiceName(), wasSuccessful);

    }

    private String extractSection(String text , Pattern pattern, String fallback){

        Matcher matcher = pattern.matcher(text);
        if(matcher.find()){
            return matcher.group(1).trim();
        }
        return fallback;
    }
}
