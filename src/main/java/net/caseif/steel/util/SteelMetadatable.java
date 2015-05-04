package net.caseif.steel.util;

import net.caseif.flint.util.Metadatable;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Barebones implementation of {@link Metadatable}.
 */
public class SteelMetadatable implements Metadatable {

    protected Map<String, Object> metadata = new HashMap<>();

    @Override
    public Optional<Object> getMetadata(String key) {
        return Optional.fromNullable(metadata.get(key));
    }

    @Override
    public void setMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    @Override
    public void removeMetadata(String key) {
        metadata.put(key, null);
    }

    @Override
    public Set<String> getAllMetadata() {
        return ImmutableSet.copyOf(metadata.keySet());
    }
}
