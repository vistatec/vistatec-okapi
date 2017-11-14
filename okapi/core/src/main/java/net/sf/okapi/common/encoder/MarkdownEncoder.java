package net.sf.okapi.common.encoder;

/**
 * The Markdown filter handles newlines itself, so encoder is a no-op.
 */
public class MarkdownEncoder extends DefaultEncoder {

    @Override
    public String encode (String text,
            EncoderContext context)
    {
        return text;
    }

    @Override
    public String encode (char value,
            EncoderContext context)
    {
        return String.valueOf(value);
    }

    @Override
    public String encode (int value,
            EncoderContext context)
    {
        if ( Character.isSupplementaryCodePoint(value) ) {
            return new String(Character.toChars(value));
        }
        return String.valueOf((char)value); 
    }
}
