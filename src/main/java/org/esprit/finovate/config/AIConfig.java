package org.esprit.finovate.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Loads AI-related API keys from api_config.properties or .env. */
public final class AIConfig {
    private static final Properties props = new Properties();

    static {
        try (InputStream is = AIConfig.class.getResourceAsStream("/api_config.properties")) {
            if (is != null) props.load(is);
        } catch (IOException ignored) {}
    }

    /** Groq API key for LLM (translation, summarization, etc.). Get one at https://console.groq.com */
    public static String getGroqApiKey() {
        String fromProps = props.getProperty("groq.api.key", "").trim();
        if (!fromProps.isEmpty()) return fromProps;
        String fromEnv = System.getenv("GROQ_API_KEY");
        return fromEnv != null ? fromEnv : "";
    }
}
