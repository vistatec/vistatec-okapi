package net.sf.okapi.filters.json.parser;

public interface IJsonHandler {
	
	/**
     * Called once at the beginning of a new document.
     */
    void handleStart();

    /**
     * Called once at the end of a document.
     */
    void handleEnd();
    
    /**
     * Handle Json comment (illegal but found in partice)
     */
    void handleComment(String c);
    
    /**
     * Handle json key, used in resname
     */
    void handleKey(String key, JsonValueTypes valueType, JsonKeyTypes keyType);
    
    /**
     * Handle separator, i.e., ":" or ","
     */
    void handleSeparator(String separator);
    
    /**
     * Handle json value, may be double, single quoted string, 
     * number, boolean symbol or null
     */
    void handleValue(String value, JsonValueTypes valueType);

    /**
     * Handle json whitespace
     */
    void handleWhitespace(String whitespace);
    
    /**
     * Handle json Object: i.e., { "key" : "value" }
     * For contextual information.
     */
    void handleObjectStart();
    void handleObjectEnd();

    /**
     * Handle json List: i.e., [ "value1", "value2" ]
     * For contextual information.
     */
    void handleListStart();
    void handleListEnd();
}
