
package net.sf.okapi.filters.xini.jaxb;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.andrae_ag.ontram.xini.xml package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Xini_QNAME = new QName("", "Xini");
    private final static QName _TextContentSph_QNAME = new QName("", "sph");
    private final static QName _TextContentSub_QNAME = new QName("", "sub");
    private final static QName _TextContentU_QNAME = new QName("", "u");
    private final static QName _TextContentB_QNAME = new QName("", "b");
    private final static QName _TextContentEph_QNAME = new QName("", "eph");
    private final static QName _TextContentPh_QNAME = new QName("", "ph");
    private final static QName _TextContentSup_QNAME = new QName("", "sup");
    private final static QName _TextContentBr_QNAME = new QName("", "br");
    private final static QName _TextContentI_QNAME = new QName("", "i");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.andrae_ag.ontram.xini.xml
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Element.ElementContent}
     * @return the newly created ElementContent
     */
    public Element.ElementContent createElementElementContent() {
        return new Element.ElementContent();
    }

    /**
     * Create an instance of {@link Empty}
     * @return the newly created Empty
     */
    public Empty createEmpty() {
        return new Empty();
    }

    /**
     * Create an instance of {@link StatisticInfo}
     * @return the newly created StatisticInfo instance
     */
    public StatisticInfo createStatisticInfo() {
        return new StatisticInfo();
    }

    /**
     * Create an instance of {@link FileInfo}
     * @return the newly created FileInfo instance
     */
    public FileInfo createFileInfo() {
        return new FileInfo();
    }

    /**
     * Create an instance of {@link TR}
     * @return the newly created TR
     */
    public TR createTR() {
        return new TR();
    }

    /**
     * Create an instance of {@link INITR}
     * @return the newly created INITR
     */
    public INITR createINITR() {
        return new INITR();
    }

    /**
     * Create an instance of {@link TargetLanguages}
     * @return the newly created TargetLanguages
     */
    public TargetLanguages createTargetLanguages() {
        return new TargetLanguages();
    }

    /**
     * Create an instance of {@link TextContent}
     * @return the newly created TextContent instance
     */
    public TextContent createTextContent() {
        return new TextContent();
    }

    /**
     * Create an instance of {@link TD}
     * @return the newly created TD
     */
    public TD createTD() {
        return new TD();
    }

    /**
     * Create an instance of {@link Xini}
     * @return the newly created Xini instance
     */
    public Xini createXini() {
        return new Xini();
    }

    /**
     * Create an instance of {@link EndPlaceHolder}
     * @return the newly created EndPlaceHolder instance
     */
    public EndPlaceHolder createEndPlaceHolder() {
        return new EndPlaceHolder();
    }

    /**
     * Create an instance of {@link Page.Elements}
     * @return the newly created Page.Elements instance
     */
    public Page.Elements createPageElements() {
        return new Page.Elements();
    }

    /**
     * Create an instance of {@link StartPlaceHolder}
     * @return the newly created StartPlaceHolder instance
     */
    public StartPlaceHolder createStartPlaceHolder() {
        return new StartPlaceHolder();
    }

    /**
     * Create an instance of {@link Field}
     * @return the newly created Field instance
     */
    public Field createField() {
        return new Field();
    }

    /**
     * Create an instance of {@link Table}
     * @return the newly created Table instance
     */
    public Table createTable() {
        return new Table();
    }

    /**
     * Create an instance of {@link Seg}
     * @return the newly created Seg instance
     */
    public Seg createSeg() {
        return new Seg();
    }

    /**
     * Create an instance of {@link ValuePair}
     * @return the newly created ValuePair instance
     */
    public ValuePair createValuePair() {
        return new ValuePair();
    }

    /**
     * Create an instance of {@link Page}
     * @return the newly created Page instance
     */
    public Page createPage() {
        return new Page();
    }

    /**
     * Create an instance of {@link Main}
     * @return the newly created Main instance
     */
    public Main createMain() {
        return new Main();
    }

    /**
     * Create an instance of {@link Fields}
     * @return the newly created Fields instance
     */
    public Fields createFields() {
        return new Fields();
    }

    /**
     * Create an instance of {@link Element}
     * @return the newly created Element instance
     */
    public Element createElement() {
        return new Element();
    }

    /**
     * Create an instance of {@link INITD}
     * @return the newly created INITD instance
     */
    public INITD createINITD() {
        return new INITD();
    }

    /**
     * Create an instance of {@link Trans}
     * @return the newly created Trans instance
     */
    public Trans createTrans() {
        return new Trans();
    }

    /**
     * Create an instance of {@link PlaceHolder}
     * @return the newly created PlaceHolder instance
     */
    public PlaceHolder createPlaceHolder() {
        return new PlaceHolder();
    }

    /**
     * Create an instance of {@link FilterInfo}
     * @return the newly created FilterInfo instance
     */
    public FilterInfo createFilterInfo() {
        return new FilterInfo();
    }

    /**
     * Create an instance of {@link JobInfo}
     * @return the newly created JobInfo instance
     */
    public JobInfo createJobInfo() {
        return new JobInfo();
    }

    /**
     * Create an instance of {@link INITable}
     * @return the newly created INITable instance
     */
    public INITable createINITable() {
        return new INITable();
    }

    /**
     * Create an instance of {@link JAXBElement}{@code <}{@link Xini}{@code >}
     * @return the newly created JAXBElement of Xini instance
     */
    @XmlElementDecl(namespace = "", name = "Xini")
    public JAXBElement<Xini> createXini(Xini value) {
        return new JAXBElement<Xini>(_Xini_QNAME, Xini.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement}{@code <}{@link StartPlaceHolder}{@code >}
     * @return the newly created JAXBElement of StartPlaceHolder instance
     */
    @XmlElementDecl(namespace = "", name = "sph", scope = TextContent.class)
    public JAXBElement<StartPlaceHolder> createTextContentSph(StartPlaceHolder value) {
        return new JAXBElement<StartPlaceHolder>(_TextContentSph_QNAME, StartPlaceHolder.class, TextContent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement}{@code <}{@link TextContent}{@code >}
     * @return the newly created JAXBElement of TextContent instance
     */
    @XmlElementDecl(namespace = "", name = "sub", scope = TextContent.class)
    public JAXBElement<TextContent> createTextContentSub(TextContent value) {
        return new JAXBElement<TextContent>(_TextContentSub_QNAME, TextContent.class, TextContent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement}{@code <}{@link TextContent}{@code >}
     * @return the newly created JAXBElement of TextContent instance
     */
    @XmlElementDecl(namespace = "", name = "u", scope = TextContent.class)
    public JAXBElement<TextContent> createTextContentU(TextContent value) {
        return new JAXBElement<TextContent>(_TextContentU_QNAME, TextContent.class, TextContent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement}{@code <}{@link TextContent}{@code >}
     * @return the newly created JAXBElement of TextContent instance
     */
    @XmlElementDecl(namespace = "", name = "b", scope = TextContent.class)
    public JAXBElement<TextContent> createTextContentB(TextContent value) {
        return new JAXBElement<TextContent>(_TextContentB_QNAME, TextContent.class, TextContent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement}{@code <}{@link EndPlaceHolder}{@code >}
     * @return the newly created JAXBElement of EndPlaceHolder instance
     */
    @XmlElementDecl(namespace = "", name = "eph", scope = TextContent.class)
    public JAXBElement<EndPlaceHolder> createTextContentEph(EndPlaceHolder value) {
        return new JAXBElement<EndPlaceHolder>(_TextContentEph_QNAME, EndPlaceHolder.class, TextContent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement}{@code <}{@link PlaceHolder}{@code >}
     * @return the newly created JAXBElement of PlaceHolder instance
     */
    @XmlElementDecl(namespace = "", name = "ph", scope = TextContent.class)
    public JAXBElement<PlaceHolder> createTextContentPh(PlaceHolder value) {
        return new JAXBElement<PlaceHolder>(_TextContentPh_QNAME, PlaceHolder.class, TextContent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement}{@code <}{@link TextContent}{@code >}
     * @return the newly created JAXBElement of TextContent instance
     */
    @XmlElementDecl(namespace = "", name = "sup", scope = TextContent.class)
    public JAXBElement<TextContent> createTextContentSup(TextContent value) {
        return new JAXBElement<TextContent>(_TextContentSup_QNAME, TextContent.class, TextContent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement}{@code <}{@link Empty}{@code >}
     * @return the newly created JAXBElement of Empty instance
     */
    @XmlElementDecl(namespace = "", name = "br", scope = TextContent.class)
    public JAXBElement<Empty> createTextContentBr(Empty value) {
        return new JAXBElement<Empty>(_TextContentBr_QNAME, Empty.class, TextContent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement}{@code <}{@link TextContent}{@code >}
     * @return the newly created JAXBElement of TextContent instance
     */
    @XmlElementDecl(namespace = "", name = "i", scope = TextContent.class)
    public JAXBElement<TextContent> createTextContentI(TextContent value) {
        return new JAXBElement<TextContent>(_TextContentI_QNAME, TextContent.class, TextContent.class, value);
    }

}
