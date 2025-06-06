package chat.tortuga.discord.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookupFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Objects;

@Slf4j
public class ConfigLoader {

    public static final CoreConfig CORE = ConfigLoader.getDefault();
    public static final String CONFIG_FILE = "config.yml";

    private final ObjectMapper objectMapper;
    private final StringSubstitutor stringSubstitutor;

    public ConfigLoader() {
        this.objectMapper = new ObjectMapper(new YAMLFactory())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.stringSubstitutor = new StringSubstitutor(StringLookupFactory.INSTANCE.environmentVariableStringLookup());
    }

    public <T> T load(String file, Class<T> clazz) {
        return load(Objects.requireNonNull(clazz.getClassLoader().getResourceAsStream(file)), clazz);
    }

    public <T> T load(InputStream config, Class<T> clazz) {
        try {
            String contents = this.stringSubstitutor.replace(new String(config.readAllBytes()));
            return this.objectMapper.readValue(contents, clazz);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static CoreConfig getDefault() {
        ConfigLoader loader = new ConfigLoader();
        try (InputStream resource = CoreConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            return loader.load(Objects.requireNonNull(resource), CoreConfig.class);
        } catch (IOException e) {
            log.error("Error reading {}", CONFIG_FILE);
            throw new UncheckedIOException(e);
        }
    }

}
