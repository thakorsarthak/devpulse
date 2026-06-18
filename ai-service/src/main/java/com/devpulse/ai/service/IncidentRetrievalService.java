package com.devpulse.ai.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
                              String aiExplanation){

        String document = buildIncidentDocument(errorMessage,stackTrace,aiExplanation);

        TextSegment segment = TextSegment.from(document);
        Embedding embedding = embeddingModel.embed(segment).content();

        embeddingStore.add( embedding, segment);

        log.info("Stored incident in ChromaDB for future retrieval");
    }

    /**
     * Find past incidents semantically similar to the current error.
     * Will Returns empty list if ChromaDB has no relevant history yet -
     * this is expected behavior for the first few incidents.
     */

    public List<String> findSimilarIncident(String errorMessage , String stackTrace){


        String query = errorMessage + "\n" + stackTrace;

        Embedding queryEmbedding = embeddingModel.embed(query).content();

        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest
                .builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(MAX_RESULTS)
                .minScore(MIN_SIMILARITY_SCORE)
                .build();

        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(searchRequest);

        List<EmbeddingMatch<TextSegment>> matches = result.matches();

        log.info("Found {} similar past incidents (similarity > = {})" , matches.size() , MIN_SIMILARITY_SCORE);

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

}
