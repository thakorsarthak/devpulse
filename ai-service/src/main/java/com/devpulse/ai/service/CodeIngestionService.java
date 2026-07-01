package com.devpulse.ai.service;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Embeds source code files into ChromaDB so the RAG pipeline
 * has actual codebase context, not just past-incident history.
 *
 * WHY CHUNK BY FILE, NOT BY METHOD:
 * Method-level chunking requires a Java parser (JavaParser library) to split correctly
 * File-level chunking is simpler and still gives the LLM enough context — most Java files are a single
 * class, and seeing the whole class shows field relationships,imports, and method interactions a single-method chunk would miss.
 *
 * WHY METADATA MATTERS:
 * We tag each embedded chunk with its filename and class name.
 * When a stack trace mentions "PaymentProcessor.java:52", we can
 * bias retrieval toward chunks tagged with that exact class name —
 * far more precise than pure semantic similarity alone.
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodeIngestionService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    private static final int MAX_FILE_SIZE_CHARS = 8000;

    /**
     * Walks a directory, finds all .java files, embeds each one.
     * Returns count of files successfully ingested.
     */
    public int ingestCodebase(String repoPath, String repoName) {
        Path root = Paths.get(repoPath);

        if (!Files.exists(root)) {
            throw new IllegalArgumentException(
                    "Path does not exist: " + repoPath);
        }

        List<Path> javaFiles;
        try (Stream<Path> walk = Files.walk(root)) {
            javaFiles = walk
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    // Skip generated/test files - low value, high noise
                    .filter(p -> !p.toString().contains("/test/"))
                    .filter(p -> !p.toString().contains("\\test\\"))
                    .filter(p -> !p.toString().contains("/target/"))
                    .filter(p -> !p.toString().contains("\\target\\"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Failed to walk directory: {}", repoPath, e);
            throw new RuntimeException("Failed to read codebase", e);
        }

        log.info("Found {} Java files to ingest from {}",
                javaFiles.size(), repoPath);

        int ingestedCount = 0;
        for (Path file : javaFiles) {
            try {
                ingestFile(file, repoName);
                ingestedCount++;
            } catch (Exception e) {
                log.warn("Skipped file due to error: {} - {}",
                        file, e.getMessage());
            }
        }

        log.info("Successfully ingested {}/{} files for repo: {}",
                ingestedCount, javaFiles.size(), repoName);

        return ingestedCount;
    }

    private void ingestFile(Path file, String repoName)
            throws IOException {

        String content = Files.readString(file);

        // Truncate very large files - keeps embeddings focused
        // and avoids exceeding the embedding model's input limits
        if (content.length() > MAX_FILE_SIZE_CHARS) {
            content = content.substring(0, MAX_FILE_SIZE_CHARS);
        }

        String fileName = file.getFileName().toString();
        String className = fileName.replace(".java", "");

        // Prefix the content with clear labels - helps the LLM
        // understand what it's looking at when retrieved later
        String document = String.format("""
                SOURCE FILE: %s
                CLASS: %s
                REPOSITORY: %s

                %s
                """, fileName, className, repoName, content);

        Metadata metadata = Metadata.from("className", className);
        metadata.put("fileName", fileName);
        metadata.put("repoName", repoName);
        metadata.put("type", "source-code");

        TextSegment segment = TextSegment.from(document, metadata);
        Embedding embedding = embeddingModel.embed(segment).content();

        embeddingStore.add(embedding, segment);

        log.debug("Ingested file: {}", fileName);
    }
}