package org.onlab.onos.net;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a set of simple annotations that can be used to add arbitrary
 * attributes to various parts of the data model.
 */
public final class DefaultAnnotations implements SparseAnnotations {

    private final Map<String, String> map;

    // For serialization
    private DefaultAnnotations() {
        this.map = null;
    }

    /**
     * Creates a new set of annotations using clone of the specified hash map.
     *
     * @param map hash map of key/value pairs
     */
    private DefaultAnnotations(Map<String, String> map) {
        this.map = map;
    }

    /**
     * Creates a new annotations builder.
     *
     * @return new annotations builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Merges the specified base set of annotations and additional sparse
     * annotations into new combined annotations. If the supplied sparse
     * annotations are empty, the original base annotations are returned.
     * Any keys tagged for removal in the sparse annotations will be omitted
     * in the resulting merged annotations.
     *
     * @param annotations       base annotations
     * @param sparseAnnotations additional sparse annotations
     * @return combined annotations or the original base annotations if there
     * are not additional annotations
     */
    public static DefaultAnnotations merge(DefaultAnnotations annotations,
                                           SparseAnnotations sparseAnnotations) {
        checkNotNull(annotations, "Annotations cannot be null");
        if (sparseAnnotations == null || sparseAnnotations.keys().isEmpty()) {
            return annotations;
        }

        // Merge the two maps. Yes, this is not very efficient, but the
        // use-case implies small maps and infrequent merges, so we opt for
        // simplicity.
        Map<String, String> merged = copy(annotations.map);
        for (String key : sparseAnnotations.keys()) {
            if (sparseAnnotations.isRemoved(key)) {
                merged.remove(key);
            } else {
                merged.put(key, sparseAnnotations.value(key));
            }
        }
        return new DefaultAnnotations(merged);
    }

    @Override
    public Set<String> keys() {
        return map.keySet();
    }

    @Override
    public String value(String key) {
        String value = map.get(key);
        return Objects.equals(Builder.REMOVED, value) ? null : value;
    }

    @Override
    public boolean isRemoved(String key) {
        return Objects.equals(Builder.REMOVED, map.get(key));
    }

    @SuppressWarnings("unchecked")
    private static HashMap<String, String> copy(Map<String, String> original) {
        if (original instanceof HashMap) {
            return (HashMap) ((HashMap) original).clone();
        }
        throw new IllegalArgumentException("Expecting HashMap instance");
    }

    /**
     * Facility for gradually building model annotations.
     */
    public static final class Builder {

        private static final String REMOVED = "~rEmOvEd~";
        private final Map<String, String> builder = new HashMap<>();

        // Private construction is forbidden.
        private Builder() {
        }

        /**
         * Adds the specified annotation. Any previous value associated with
         * the given annotation key will be overwritten.
         *
         * @param key   annotation key
         * @param value annotation value
         * @return self
         */
        public Builder set(String key, String value) {
            builder.put(key, value);
            return this;
        }

        /**
         * Adds the specified annotation. Any previous value associated with
         * the given annotation key will be tagged for removal.
         *
         * @param key annotation key
         * @return self
         */
        public Builder remove(String key) {
            builder.put(key, REMOVED);
            return this;
        }

        /**
         * Returns immutable annotations built from the accrued key/values pairs.
         *
         * @return annotations
         */
        public DefaultAnnotations build() {
            return new DefaultAnnotations(copy(builder));
        }
    }
}
