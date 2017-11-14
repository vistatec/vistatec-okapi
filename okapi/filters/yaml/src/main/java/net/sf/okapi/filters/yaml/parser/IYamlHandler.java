package net.sf.okapi.filters.yaml.parser;

public interface IYamlHandler {
	
	/**
     * Called once at the beginning of a new document.
     */
    void handleStart();

    /**
     * Called once at the end of a document.
     */
    void handleEnd();
    
    /**
     * Handle YAML comment
     * process differently based on if this is inside a current TextUnit 
     */
    void handleComment(String c, boolean insideScalar);
    
    /**
     * Handle YAML keys, used in resname
     */
    void handleKey(Key key);
    
    /**
     * Handle separator, i.e., "," or "-"
     */
    void handleMarker(String marker);
    
    /**
     * Handle YAML value
     */
    void handleScalar(Scalar scalar);

    /**
     * Handle YAML whitespace
     * process differently based on if this is inside a current TextUnit 
     */
    void handleWhitespace(String whitespace, boolean insideScalar);
    
    /**
     * Handle Yaml Maps (BLOCK and FLOW)
     * For contextual information.
     */
    void handleMapStart(boolean flow);
    void handleMapEnd(boolean flow);

    /**
     * Handle YAML sequences (BLOCK and FLOW)
     * For contextual information.
     */
    void handleSequenceStart(boolean flow);
    void handleSequenceEnd(boolean flow);
    
    /**
     * Other tokens like ANCHOR, TAG, ALIAS
     * @param other
     */
    void handleOther(String other);
    
    /**
     * Document start token. If non-null then start clean (clear stacks, finish TextUnits etc..)
     * @param start
     */
    void handleDocumentStart(String start);
    
    /**
     * Document end token. If non-null  then clean up (clear stacks, finish TextUnits etc..)
     * @param end
     */
    void handleDocumentEnd(String end);
    
    /**
     * Called after flow or mapping element is done
     */
    void handleMappingElementEnd();
    
    /**
     * Called at the start of a block sequence element (e.g., after the DASH "- test")
     */
    void handleBlockSequenceNodeStart(String dash, int indent);
}
