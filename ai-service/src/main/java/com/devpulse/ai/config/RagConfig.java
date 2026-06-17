package com.devpulse.ai.config;


import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RAG PIPELINE CONFIGURATION
 * THREE COMPONENT WORKING TOGETHER
 *
 * 1 : EmbeddingModel: converts text into vector (it is like an array of numbers)
 *     We use ALLMiniLm-L6-v2 -runs locally , no api calls , no cost.
 *     Same model we used in ResumerScreener project
 *
 * 2 : EmbeddingStore (ChromaDB) : stores vector , finds similar ones
 *
 *
 * 3 : ChatModel(Ollama Llama3.2) - generates the actual explanation
 *     takes the error + retrieval context , produces plain English
 *
 * */

@Configuration
public class RagConfig {

    @Value("${langchain4j.ollama.base-url}")
    private String ollamaBaseUrl;

    @Value("${langchain4j.ollama.model-name}")
    private String ollamaModelName;

    @Value("${langchain4j.chroma.base-url}")
    private String chromaBaseUrl;

    @Value("${langchain4j.chroma.collection-name}")
    private String chromaCollectionName;

    @Bean
    public EmbeddingModel embeddingModel(){

        return new AllMiniLmL6V2EmbeddingModel();
    }


    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(){
        return ChromaEmbeddingStore.builder()
                .baseUrl(chromaBaseUrl)
                .collectionName(chromaCollectionName).build();
    }

}
