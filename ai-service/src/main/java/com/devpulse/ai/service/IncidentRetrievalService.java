package com.devpulse.ai.service;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

/**
 * Handles the "Retrieval" half of RAG
 *
 * Two main layers
 *  1 : storeIncident() - embed a resolved incident, save to chromaDB.
 *      this builds our knowledge base over time.
 *      every analyzed error becomes searchable context for future errors
 *
 *  2 : findSimilarIncident() - given a new error , find past similar ones via semantic search (cosine similarity)
 *
 * */
@Slf4j
@Service
@RequiredArgsConstructor
public class IncidentRetrievalService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    private static final int MAX_RESULTS = 3;
    private static final double MIN_SIMILARITY_SCORE = 0.5;


    /*
    * Embed and storing a new incident for future retrieval
    * called after the analysis - builds our knowledge base*/

    public void storeIncident(String errorMessage ,
                              String stackTrace,
                              String aiExplanation,
                              String serviceName){

        String document = buildIncidentDocument(errorMessage,stackTrace,aiExplanation);

        Metadata metadata = Metadata.from("serviceName", serviceName);
        metadata.put("type", "past-incident");

        TextSegment segment = TextSegment.from(document);
        Embedding embedding = embeddingModel.embed(segment).content();

        embeddingStore.add( embedding, segment);

        log.info("Stored incident in ChromaDB for service: {}", serviceName);
    }

    /**
     * Find past incidents semantically similar to the current error.
     * Will Returns empty list if ChromaDB has no relevant history yet -
     * this is expected behavior for the first few incidents.
     */

    public List<String> findSimilarIncident(String errorMessage , String stackTrace , String serviceName){


        String query = errorMessage + "\n" + stackTrace;

        Embedding queryEmbedding = embeddingModel.embed(query).content();

        Filter serviceFilter = metadataKey("serviceName").isEqualTo(serviceName);

        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest
                .builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(MAX_RESULTS)
                .minScore(MIN_SIMILARITY_SCORE)
                .filter(serviceFilter)
                .build();

        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(searchRequest);

        List<EmbeddingMatch<TextSegment>> matches = result.matches();

        log.info("Found {} similar past incidents for service: {}",
                result.matches().size(), serviceName);

        return matches.stream()
                .map(match -> match.embedded().text())
                .collect(Collectors.toList());

    }

    private String buildIncidentDocument(String errorMessage,
                                         String stackTrace,
                                         String aiExplanation){

        return String.format("""
                ERROR: %s 
                STACK TRACE: %s
                RESOLUTION: %s""", errorMessage,stackTrace,aiExplanation);
    }

    /**
     * Find source code relevant to a stack trace.
     * Extracts class names mentioned in the trace and searches
     * for matching embedded source files.
     *
     * WHY SEPARATE FROM findSimilarIncidents:
     * Past incidents and source code are different document types in the same ChromaDB collection.
     * We search them separately because the query strategy differs - incidents use semantic
     * similarity on error text, code retrieval should ideally
     * filter by exact class name when possible.
     */
    public List<String> findRelevantSourceCode(String stackTrace , String repoName) {
        List<String> classNames = extractClassNames(stackTrace);

        if (classNames.isEmpty()) {
            return List.of();
        }

        // Build a query combining all extracted class names
        // ChromaDB will find chunks semantically close to these names
        String query = String.join(" ", classNames);
        Embedding queryEmbedding = embeddingModel.embed(query).content();


        Filter repoFilter = metadataKey("repoName").isEqualTo(repoName);

        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest
                .builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(2)
                .minScore(0.4) // lower threshold - class names are short queries
                .filter(repoFilter)
                .build();

        EmbeddingSearchResult<TextSegment> result =
                embeddingStore.search(searchRequest);

        log.info("Found {} relevant source files for classes: {}",
                result.matches().size(), classNames);

        return result.matches().stream()
                .map(match -> match.embedded().text())
                .collect(Collectors.toList());
    }

    /**
     * Extracts Java class names from a stack trace using regex.
     * Matches patterns like: at com.example.PaymentProcessor.charge(...)
     */
    private List<String> extractClassNames(String stackTrace) {
        if (stackTrace == null || stackTrace.isBlank()) {
            return List.of();
        }

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "at\\s+[\\w.]+\\.(\\w+)\\.\\w+\\(");
        java.util.regex.Matcher matcher = pattern.matcher(stackTrace);

        java.util.Set<String> classNames = new java.util.LinkedHashSet<>();
        while (matcher.find() && classNames.size() < 3) {
            classNames.add(matcher.group(1));
        }

        return new java.util.ArrayList<>(classNames);
    }

}
