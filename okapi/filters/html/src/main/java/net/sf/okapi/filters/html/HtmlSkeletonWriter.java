package net.sf.okapi.filters.html;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlSkeletonWriter extends GenericSkeletonWriter {

    private static final String FIRST_PART_OF_HTML_ELEMENT = "<html";
    private static final String RTL_TEXT_DIRECTION_ATTRIBUTE = " dir=\"rtl\"";
    private static final Pattern TEXT_DIRECTION_ATTRIBUTE_PATTERN = Pattern.compile(" dir=\"(?:rtl|ltr)\"");

    @Override
    public String processDocumentPart(DocumentPart resource) {
        if (resource.getSkeleton() instanceof GenericSkeleton) {
            GenericSkeleton skeleton = (GenericSkeleton) resource.getSkeleton();
            StringBuilder firstPartData = skeleton.getFirstPart().getData();
            clarifyTextDirection(firstPartData);
        }
        return super.processDocumentPart(resource);
    }

    private void clarifyTextDirection(StringBuilder firstPartData) {
        int indexOfHtmlElement = firstPartData.indexOf(FIRST_PART_OF_HTML_ELEMENT);
        if (-1 == indexOfHtmlElement) {
            return;
        }

        Matcher textDirectionMatcher = TEXT_DIRECTION_ATTRIBUTE_PATTERN.matcher(firstPartData);

        if (textDirectionMatcher.find()) {
            firstPartData.replace(textDirectionMatcher.start(), textDirectionMatcher.end(), getTextDirectionAttribute());
        } else {
            firstPartData.insert(indexOfHtmlElement + FIRST_PART_OF_HTML_ELEMENT.length(), getTextDirectionAttribute());
        }
    }

    private String getTextDirectionAttribute() {
        return LocaleId.isBidirectional(getOutputLoc()) ? RTL_TEXT_DIRECTION_ATTRIBUTE : "";
    }
}
