package com.irp.core.agent.runbook;

import java.util.ArrayList;
import java.util.List;

/** Splits runbook text into roughly fixed-size, non-overlapping chunks on word boundaries. */
final class RunbookChunker {

    private static final int WORDS_PER_CHUNK = 180;

    private RunbookChunker() {
    }

    static List<String> chunk(String content) {
        String[] words = content.trim().split("\\s+");
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int wordCount = 0;

        for (String word : words) {
            if (wordCount == WORDS_PER_CHUNK) {
                chunks.add(current.toString());
                current.setLength(0);
                wordCount = 0;
            }
            if (wordCount > 0) {
                current.append(' ');
            }
            current.append(word);
            wordCount++;
        }
        if (!current.isEmpty()) {
            chunks.add(current.toString());
        }
        return chunks;
    }
}
