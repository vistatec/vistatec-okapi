/*===========================================================================
  Copyright (C) 2017 by the Okapi Framework contributors
-----------------------------------------------------------------------------
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
===========================================================================*/

package net.sf.okapi.filters.markdown.parser;

import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.AUTO_LINK;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.BLANK_LINE;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.BLOCK_QUOTE;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.BULLET_LIST;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.BULLET_LIST_ITEM;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.CODE;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.EMPHASIS;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.FENCED_CODE_BLOCK;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.FENCED_CODE_BLOCK_INFO;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.HARD_LINE_BREAK;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.HEADING_PREFIX;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.HEADING_UNDERLINE;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.HTML_BLOCK;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.HTML_COMMENT_BLOCK;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.HTML_ENTITY;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.HTML_INLINE;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.HTML_INLINE_COMMENT;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.HTML_INNER_BLOCK;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.HTML_INNER_BLOCK_COMMENT;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.IMAGE;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.IMAGE_REF;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.INDENTED_CODE_BLOCK;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.LINK;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.LINK_REF;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.MAIL_LINK;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.ORDERED_LIST;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.ORDERED_LIST_ITEM;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.REFERENCE;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.SOFT_LINE_BREAK;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.STRONG_EMPHASIS;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.TABLE_PIPE;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.TEXT;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.THEMATIC_BREAK;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.WHITE_SPACE;
import static net.sf.okapi.filters.markdown.parser.MarkdownTokenType.YAML_METADATA_HEADER;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;

import com.vladsch.flexmark.ast.AutoLink;
import com.vladsch.flexmark.ast.BlankLine;
import com.vladsch.flexmark.ast.Block;
import com.vladsch.flexmark.ast.BlockQuote;
import com.vladsch.flexmark.ast.BulletList;
import com.vladsch.flexmark.ast.BulletListItem;
import com.vladsch.flexmark.ast.Code;
import com.vladsch.flexmark.ast.CustomBlock;
import com.vladsch.flexmark.ast.CustomNode;
import com.vladsch.flexmark.ast.DelimitedNodeImpl;
import com.vladsch.flexmark.ast.Document;
import com.vladsch.flexmark.ast.Emphasis;
import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.ast.HardLineBreak;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.HtmlBlock;
import com.vladsch.flexmark.ast.HtmlBlockBase;
import com.vladsch.flexmark.ast.HtmlCommentBlock;
import com.vladsch.flexmark.ast.HtmlEntity;
import com.vladsch.flexmark.ast.HtmlInline;
import com.vladsch.flexmark.ast.HtmlInlineComment;
import com.vladsch.flexmark.ast.HtmlInnerBlock;
import com.vladsch.flexmark.ast.HtmlInnerBlockComment;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.ImageRef;
import com.vladsch.flexmark.ast.IndentedCodeBlock;
import com.vladsch.flexmark.ast.InlineLinkNode;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.ast.LinkRef;
import com.vladsch.flexmark.ast.ListBlock;
import com.vladsch.flexmark.ast.ListItem;
import com.vladsch.flexmark.ast.MailLink;
import com.vladsch.flexmark.ast.Node;
import com.vladsch.flexmark.ast.NodeVisitor;
import com.vladsch.flexmark.ast.OrderedList;
import com.vladsch.flexmark.ast.OrderedListItem;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.ast.RefNode;
import com.vladsch.flexmark.ast.Reference;
import com.vladsch.flexmark.ast.SoftLineBreak;
import com.vladsch.flexmark.ast.StrongEmphasis;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.ast.TextBase;
import com.vladsch.flexmark.ast.ThematicBreak;
import com.vladsch.flexmark.ast.VisitHandler;
import com.vladsch.flexmark.ast.Visitor;
import com.vladsch.flexmark.ast.WhiteSpace;
import com.vladsch.flexmark.ext.tables.TableBlock;
import com.vladsch.flexmark.ext.tables.TableBody;
import com.vladsch.flexmark.ext.tables.TableCaption;
import com.vladsch.flexmark.ext.tables.TableCell;
import com.vladsch.flexmark.ext.tables.TableHead;
import com.vladsch.flexmark.ext.tables.TableRow;
import com.vladsch.flexmark.ext.tables.TableSeparator;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterBlock;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterNode;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.options.MutableDataHolder;
import com.vladsch.flexmark.util.options.MutableDataSet;
import com.vladsch.flexmark.util.sequence.BasedSequence;

import net.sf.okapi.filters.markdown.Parameters;

public class MarkdownParser {
    private static final MutableDataHolder OPTIONS = new MutableDataSet()
            .set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create(), YamlFrontMatterExtension.create()));

    private static final Parser PARSER = Parser.builder(OPTIONS).build();

    private String newline = System.lineSeparator();
    private Node root = null;
    private Deque<MarkdownToken> tokenQueue = new LinkedList<>();
    private boolean lastAddedTranslatableContent = false;
    private int numNonTranslatableNewlines = 0;
    private Parameters params;

    /**
     * Create a new {@link MarkdownParser} that uses the platform-specific newline.
     */
    public MarkdownParser(Parameters params) {
        this.params = params;
    }

    /**
     * Create a new {@link MarkdownParser} that uses the specified string as a newline.
     * @param newline The newline type that this parser will use
     */
    public MarkdownParser(Parameters params, String newline) {
        this(params);
        this.newline = newline;
    }

    /**
     * Parse the given Markdown content into tokens that can be then retrieved with
     * calls to {@link MarkdownParser#getNextToken()}. Any existing tokens from
     * previous calls to {@link MarkdownParser#parse(String)} will be discarded.
     *
     * @param markdownContent The Markdown content to parse into tokens
     */
    public void parse(String markdownContent) {
        root = PARSER.parse(markdownContent);
        tokenQueue.clear();
        numNonTranslatableNewlines = 0;
        lastAddedTranslatableContent = false;

        visitor.visit(root);
    }

    public boolean hasNextToken() {
        return !tokenQueue.isEmpty();
    }

    /**
     * Returns the next available token.
     *
     * @return The next token
     * @throws IllegalStateException If no more tokens are remaining
     */
    public MarkdownToken getNextToken() {
        if (!hasNextToken()) {
            throw new IllegalStateException("No more tokens remaining");
        }
        return tokenQueue.removeFirst();
    }

    public String getNewline() {
        return newline;
    }

    public void setNewline(String newline) {
        this.newline = newline;
    }

    /**
     * Returns a string representation the AST generated by the parser from the last
     * call to {@link MarkdownParser#parse(String)}.
     * @return String representation of the AST
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        generateAstString(root, 0, builder);
        return builder.toString();
    }

    private void generateAstString(Node node, int depth, StringBuilder builder) {
        builder.append(node + " (depth: " + depth + ")" + newline);
        for (Node child: node.getChildren()) {
            generateAstString(child, depth + 1, builder);
        }
    }

    private void addToQueue(String content, boolean isTranslatable, MarkdownTokenType type, Node node) {
        // Queue may contain at most 2 non-translatable newlines in a row
        if (content.equals(newline) && !isTranslatable) {
            lastAddedTranslatableContent = isTranslatable;
            numNonTranslatableNewlines++;
            if (numNonTranslatableNewlines <= 2) {
                addListPaddingCharacters(content, node);
                tokenQueue.addLast(new MarkdownToken(content, isTranslatable, type));
            }
            return;
        }


        // If this token and the previously added token are translatable, merge them into a single token
        if (lastAddedTranslatableContent && isTranslatable) {
            MarkdownToken token = tokenQueue.peekLast();
            token.setContent(token.getContent() + content);
            return;
        }
        numNonTranslatableNewlines = 0;
        lastAddedTranslatableContent = isTranslatable;
        addListPaddingCharacters(content, node);
        tokenQueue.addLast(new MarkdownToken(content, isTranslatable, type));
    }

    private void addListPaddingCharacters(String content, Node node) {
        // If a newline is the content being inserted, no need for additional whitespace padding
        if (content.equals(newline)) {
            return;
        }
        // Only allow padding after a newline
        MarkdownToken lastToken = tokenQueue.peekLast();
        if (lastToken == null || !lastToken.getContent().equals(newline)) {
            return;
        }

        // Apply additional padding to non-list items
        int depth = 1;
        if ((node instanceof BulletListItem) || (node instanceof OrderedListItem)) {
            depth = 0;
        }
        // Calculate how many nested lists deep this node is
        Node ancestor = node.getAncestorOfType(BulletList.class, OrderedList.class);
        while (ancestor != null) {
            ancestor = ancestor.getAncestorOfType(BulletList.class, OrderedList.class);
            depth++;
        }

        // Add padding based on the calculated list depth
        StringBuilder padding = new StringBuilder();
        for (int i = 1; i < depth; i++) {
            padding.append("   ");
        }
        if (padding.length() > 0) {
            tokenQueue.addLast(new MarkdownToken(padding.toString(), false, WHITE_SPACE));
        }
    }

    private void removeDuplicateNewlinesAtQueueTail() {
        MarkdownToken token = tokenQueue.peekLast();
        boolean removedToken = false;

        while (token != null && Objects.equals(token.getContent(), newline) && !token.isTranslatable()) {
            tokenQueue.removeLast();
            token = tokenQueue.peekLast();
            removedToken = true;
        }

        if (removedToken) {
            tokenQueue.addLast(new MarkdownToken(newline, false, SOFT_LINE_BREAK));
            numNonTranslatableNewlines = 1;
        } else {
            numNonTranslatableNewlines = 0;
        }
    }

    private NodeVisitor visitor = new NodeVisitor(

        /* Core nodes */

        new VisitHandler<>(AutoLink.class, new Visitor<AutoLink>() {
            @Override public void visit(AutoLink node) {
                addToQueue(node.getChars().toString(), false, AUTO_LINK, node);
            }
        }),
        new VisitHandler<>(BlankLine.class, new Visitor<BlankLine>() {
            @Override public void visit(BlankLine node) {
                visitBlock(node, false, BLANK_LINE);
            }
        }),
        new VisitHandler<>(BlockQuote.class, new Visitor<BlockQuote>() {
            @Override public void visit(BlockQuote node) {
                addToQueue(node.getOpeningMarker().toString() + " ", false, BLOCK_QUOTE, node);
                visitor.visitChildren(node);
                addBlockNewlines(node);
            }
        }),
        new VisitHandler<>(BulletList.class, new Visitor<BulletList>() {
            @Override public void visit(BulletList node) {
                visitListBlock(node, BULLET_LIST);
            }
        }),
        new VisitHandler<>(BulletListItem.class, new Visitor<BulletListItem>() {
            @Override public void visit(BulletListItem node) {
                visitListItem(node, BULLET_LIST_ITEM);
            }
        }),
        new VisitHandler<>(Code.class, new Visitor<Code>() {
            @Override public void visit(Code node) {
                if (params.getTranslateCodeBlocks()) {
                    visitDelimitedNode(node, CODE);
                }
                else {
                    StringBuilder sb = new StringBuilder();
                    sb.append(node.getOpeningMarker().toString())
                      .append(node.getText().toString())
                      .append(node.getClosingMarker().toString());
                    addToQueue(sb.toString(), false, CODE, node);
                }
            }
        }),
        new VisitHandler<>(CustomBlock.class, new Visitor<CustomBlock>() {
            @Override public void visit(CustomBlock node) {
                visitor.visitChildren(node);
                addBlockNewlines(node);
            }
        }),
        new VisitHandler<>(CustomNode.class, new Visitor<CustomNode>() {
            @Override public void visit(CustomNode node) {
                visitor.visitChildren(node);
            }
        }),
        new VisitHandler<>(Document.class, new Visitor<Document>() {
            @Override public void visit(Document node) {
                visitor.visitChildren(node);
                addBlockNewlines(node);
            }
        }),
        new VisitHandler<>(Emphasis.class, new Visitor<Emphasis>() {
            @Override public void visit(Emphasis node) {
                visitDelimitedNode(node, EMPHASIS);
            }
        }),
        new VisitHandler<>(FencedCodeBlock.class, new Visitor<FencedCodeBlock>() {
            @Override public void visit(FencedCodeBlock node) {
                addToQueue(node.getOpeningFence().toString(), false, FENCED_CODE_BLOCK, node);
                if (isDefined(node.getInfo())) {
                    addToQueue(node.getInfo().toString(), params.getTranslateCodeBlocks(), FENCED_CODE_BLOCK_INFO, node);
                }
                addToQueue(newline, false, SOFT_LINE_BREAK, node);
                addToQueue(node.getContentChars().toString().trim(), params.getTranslateCodeBlocks(), TEXT, node);
                addToQueue(newline, false, SOFT_LINE_BREAK, node);
                removeDuplicateNewlinesAtQueueTail();
                addToQueue(node.getClosingFence().toString(), false, FENCED_CODE_BLOCK, node);
                addBlockNewlines(node);
            }
        }),
        new VisitHandler<>(YamlFrontMatterBlock.class, new Visitor<YamlFrontMatterBlock>() {
            @Override public void visit(YamlFrontMatterBlock node) {
                if (params.getTranslateHeaderMetadata()) {
                    addToQueue("---", false, THEMATIC_BREAK, node);
                    visitor.visitChildren(node);
                    addToQueue("---", false, THEMATIC_BREAK, node);
                }
                else {
                    addToQueue(node.getContentChars().toString().trim(), false, YAML_METADATA_HEADER, node);
                }
            }
        }),
        new VisitHandler<>(YamlFrontMatterNode.class, new Visitor<YamlFrontMatterNode>() {
            @Override public void visit(YamlFrontMatterNode node) {
                addToQueue(node.getKey() + ": ", false, YAML_METADATA_HEADER, node);
                for (String s : node.getValues()) {
                    addToQueue(s, true, TEXT, node);
                }
                visitor.visitChildren(node);
                addToQueue(node.getKey() + ": ", false, YAML_METADATA_HEADER, node);
            }
        }),
        new VisitHandler<>(HardLineBreak.class, new Visitor<HardLineBreak>() {
            @Override public void visit(HardLineBreak node) {
                addToQueue(newline, true, HARD_LINE_BREAK, node);
                visitor.visitChildren(node);
            }
        }),
        new VisitHandler<>(Heading.class, new Visitor<Heading>() {
            @Override public void visit(Heading node) {
                if (node.getOpeningMarker() != BasedSequence.NULL) {
                    addToQueue(node.getOpeningMarker().toString() + " ", false, HEADING_PREFIX, node);
                }
                visitor.visitChildren(node);
                addToQueue(newline, false, SOFT_LINE_BREAK, node);
                if (node.getClosingMarker() != BasedSequence.NULL) {
                    addToQueue(node.getClosingMarker().toString(), false, HEADING_UNDERLINE, node);
                }
                addBlockNewlines(node);
            }
        }),
        new VisitHandler<>(HtmlBlock.class, new Visitor<HtmlBlock>() {
            @Override public void visit(HtmlBlock node) {
                visitHtmlBlockBase(node, HTML_BLOCK);
            }
        }),
        new VisitHandler<>(HtmlCommentBlock.class, new Visitor<HtmlCommentBlock>() {
            @Override public void visit(HtmlCommentBlock node) {
                visitHtmlBlockBase(node, HTML_COMMENT_BLOCK);
            }
        }),
        new VisitHandler<>(HtmlEntity.class, new Visitor<HtmlEntity>() {
            @Override public void visit(HtmlEntity node) {
                addToQueue(node.getChars().toString(), false, HTML_ENTITY, node);
            }
        }),
        new VisitHandler<>(HtmlInline.class, new Visitor<HtmlInline>() {
            @Override public void visit(HtmlInline node) {
                addToQueue(node.getChars().toString(), false, HTML_INLINE, node);
                visitor.visitChildren(node);
            }
        }),
        new VisitHandler<>(HtmlInlineComment.class, new Visitor<HtmlInlineComment>() {
            @Override public void visit(HtmlInlineComment node) {
                addToQueue(node.getChars().toString(), false, HTML_INLINE_COMMENT, node);
                visitor.visitChildren(node);
            }
        }),
        new VisitHandler<>(HtmlInnerBlock.class, new Visitor<HtmlInnerBlock>() {
            @Override public void visit(HtmlInnerBlock node) {
                visitHtmlBlockBase(node, HTML_INNER_BLOCK);
            }
        }),
        new VisitHandler<>(HtmlInnerBlockComment.class, new Visitor<HtmlInnerBlockComment>() {
            @Override public void visit(HtmlInnerBlockComment node) {
                visitHtmlBlockBase(node, HTML_INNER_BLOCK_COMMENT);
            }
        }),
        new VisitHandler<>(Image.class, new Visitor<Image>() {
            @Override public void visit(Image node) {
                visitInlineLink(node, IMAGE);
            }
        }),
        new VisitHandler<>(ImageRef.class, new Visitor<ImageRef>() {
            @Override public void visit(ImageRef node) {
                visitRefDeclaration(node, IMAGE_REF);
            }
        }),
        new VisitHandler<>(IndentedCodeBlock.class, new Visitor<IndentedCodeBlock>() {
            @Override public void visit(IndentedCodeBlock node) {
                for (BasedSequence seq: node.getContentChars().split(newline)) {
                    addToQueue("    ", false, INDENTED_CODE_BLOCK, node);
                    addToQueue(seq.toString().trim(), true, TEXT, node);
                    addToQueue(newline, false, SOFT_LINE_BREAK, node);
                }
                addBlockNewlines(node);
            }
        }),
        new VisitHandler<>(Link.class, new Visitor<Link>() {
            @Override public void visit(Link node) {
                visitInlineLink(node, LINK);
            }
        }),
        new VisitHandler<>(LinkRef.class, new Visitor<LinkRef>() {
            @Override public void visit(LinkRef node) {
                visitRefDeclaration(node, LINK_REF);
            }
        }),
        new VisitHandler<>(MailLink.class, new Visitor<MailLink>() {
            @Override public void visit(MailLink node) {
                addToQueue(node.getChars().toString(), false, MAIL_LINK, node);
            }
        }),
        new VisitHandler<>(Paragraph.class, new Visitor<Paragraph>() {
            @Override public void visit(Paragraph node) {
                visitor.visitChildren(node);
                addBlockNewlines(node);
            }
        }),
        new VisitHandler<>(OrderedList.class, new Visitor<OrderedList>() {
            @Override public void visit(OrderedList node) {
                visitListBlock(node, ORDERED_LIST);
            }
        }),
        new VisitHandler<>(OrderedListItem.class, new Visitor<OrderedListItem>() {
            @Override public void visit(OrderedListItem node) {
                visitListItem(node, ORDERED_LIST_ITEM);
            }
        }),
        new VisitHandler<>(Reference.class, new Visitor<Reference>() {
            @Override public void visit(Reference node) {
                visitReferenceDefinition(node, REFERENCE);
            }
        }),
        new VisitHandler<>(SoftLineBreak.class, new Visitor<SoftLineBreak>() {
            @Override public void visit(SoftLineBreak node) {
                addToQueue(newline, true, SOFT_LINE_BREAK, node);
                visitor.visitChildren(node);
            }
        }),
        new VisitHandler<>(StrongEmphasis.class, new Visitor<StrongEmphasis>() {
            @Override public void visit(StrongEmphasis node) {
                visitDelimitedNode(node, STRONG_EMPHASIS);
            }
        }),
        new VisitHandler<>(Text.class, new Visitor<Text>() {
            @Override public void visit(Text node) {
                if (node.getChars().toString().isEmpty()) {
                    return; // No content to create token
                }
                // A text node is translatable if it is non-empty and not a separator in a table
                if (node.getChars().toString().trim().isEmpty()
                        || node.getAncestorOfType(TableSeparator.class) != null) {
                    addToQueue(node.getChars().toString(), false, TEXT, node);
                } else {
                    addToQueue(node.getChars().toString(), true, TEXT, node);
                }
            }
        }),
        new VisitHandler<>(TextBase.class, new Visitor<TextBase>() {
            @Override public void visit(TextBase node) {
                visitor.visitChildren(node);
            }
        }),
        new VisitHandler<>(ThematicBreak.class, new Visitor<ThematicBreak>() {
            @Override public void visit(ThematicBreak node) {
                addToQueue(node.getChars().toString(), false, THEMATIC_BREAK, node);
                addBlockNewlines(node);
            }
        }),
        new VisitHandler<>(WhiteSpace.class, new Visitor<WhiteSpace>() {
            @Override public void visit(WhiteSpace node) {
                visitor.visitChildren(node);
            }
        }),


        /* Table nodes */

        new VisitHandler<>(TableBlock.class, new Visitor<TableBlock>() {
            @Override public void visit(TableBlock node) {
                visitor.visitChildren(node);
                addToQueue(newline, false, SOFT_LINE_BREAK, node);
            }
        }),
        new VisitHandler<>(TableBody.class, new Visitor<TableBody>() {
            @Override public void visit(TableBody node) {
                visitor.visitChildren(node); // Has multiple TableRow children
            }
        }),
        new VisitHandler<>(TableCaption.class, new Visitor<TableCaption>() {
            @Override public void visit(TableCaption node) {
                visitor.visitChildren(node);
            }
        }),
        new VisitHandler<>(TableCell.class, new Visitor<TableCell>() {
            @Override public void visit(TableCell node) {
                addToQueue("| ", false, TABLE_PIPE, node); // Start each cell in row with a pipe
                visitor.visitChildren(node);
                addToQueue(" ", false, WHITE_SPACE, node); // Padding after table cell content
            }
        }),
        new VisitHandler<>(TableHead.class, new Visitor<TableHead>() {
            @Override public void visit(TableHead node) {
                visitor.visitChildren(node); // Child is TableRow
            }
        }),
        new VisitHandler<>(TableRow.class, new Visitor<TableRow>() {
            @Override public void visit(TableRow node) {
                visitor.visitChildren(node);
                addToQueue("|", false, TABLE_PIPE, node); // Ending pipe for row
                addToQueue(newline, false, SOFT_LINE_BREAK, node);
            }
        }),
        new VisitHandler<>(TableSeparator.class, new Visitor<TableSeparator>() {
            @Override public void visit(TableSeparator node) {
                visitor.visitChildren(node); // Child is TableRow
            }
        })
    );

    private void visitBlock(Block node, boolean isTranslatable, MarkdownTokenType type) {
        addToQueue(node.getContentChars().toString(), isTranslatable, type, node);
        addBlockNewlines(node);
    }

    private void addBlockNewlines(Node node) {
        addToQueue(newline, false, SOFT_LINE_BREAK, node);
        addToQueue(newline, false, SOFT_LINE_BREAK, node);
    }

    private void visitDelimitedNode(DelimitedNodeImpl node, MarkdownTokenType type) {
        addToQueue(node.getOpeningMarker().toString(), false, type, node);
        addToQueue(node.getText().toString(), true, TEXT, node);
        addToQueue(node.getClosingMarker().toString(), false, type, node);
    }

    private void visitHtmlBlockBase(HtmlBlockBase node, MarkdownTokenType type) {
        addToQueue(node.getChars().toString().trim(), true, type, node);
        addBlockNewlines(node);

        for (Node child: node.getChildren()) {
            visitor.visit(child);
            addBlockNewlines(child);
            removeDuplicateNewlinesAtQueueTail();
        }
    }

    private void visitInlineLink(InlineLinkNode node, MarkdownTokenType type) {
        // Do our best to consolidate this markup into a small number of tags.
        StringBuilder sb = new StringBuilder();
        if (isDefined(node.getText())) {
            if (node instanceof Image && !params.getTranslateImageAltText()) {
                sb.append(node.getTextOpeningMarker())
                    .append(node.getText().toString())
                    .append(node.getTextClosingMarker());
            }
            else {
                addToQueue(node.getTextOpeningMarker().toString(), false, type, node);
                addToQueue(node.getText().toString(), true, MarkdownTokenType.TEXT, node);
                sb.append(node.getTextClosingMarker());
            }
        }
        sb.append(node.getLinkOpeningMarker());
        sb.append(node.getUrlOpeningMarker());
        if (params.getTranslateUrls() && isDefined(node.getUrl())) {
            addToQueue(sb.toString(), false, type, node);
            sb = new StringBuilder();
            addToQueue(node.getUrl().toString(), true, MarkdownTokenType.TEXT, node);
        }
        else {
            sb.append(node.getUrl());
        }
        sb.append(node.getUrlClosingMarker());
        if (isDefined(node.getTitle())) {
            sb.append(node.getTitleOpeningMarker());
            addToQueue(sb.toString(), false, type, node);
            addToQueue(node.getTitle().toString(), true, MarkdownTokenType.TEXT, node);
            sb = new StringBuilder(node.getTitleClosingMarker());
        }
        sb.append(node.getLinkClosingMarker());
        addToQueue(sb.toString(), false, type, node);
    }

    private void visitRefDeclaration(RefNode node, MarkdownTokenType type) {
        if (isDefined(node.getText())) {
            if (node instanceof ImageRef) {
                addToQueue("!" + node.getTextOpeningMarker().toString(), false, type, node);
            } else {
                addToQueue(node.getTextOpeningMarker().toString(), false, type, node);
            }
            addToQueue(node.getText().toString(), false, type, node);
            addToQueue(node.getTextClosingMarker().toString(), false, type, node);
        }
        if (isDefined(node.getReferenceOpeningMarker())) {
            addToQueue(node.getReferenceOpeningMarker().toString(), false, type, node);
        }
        if (isDefined(node.getReference())) {
            addToQueue(node.getReference().toString(), false, type, node);
        }
        if (isDefined(node.getReferenceClosingMarker())) {
            addToQueue(node.getReferenceClosingMarker().toString(), false, type, node);
        }
    }

    private void visitReferenceDefinition(Reference node, MarkdownTokenType type) {
        addToQueue(node.getOpeningMarker().toString(), false, type, node);
        addToQueue(node.getReference().toString(), false, type, node);
        addToQueue(node.getClosingMarker().toString() + " ", false, type, node);
        if (isDefined(node.getUrlOpeningMarker())) {
            addToQueue(node.getUrlOpeningMarker().toString(), false, type, node);
        }
        if (isDefined(node.getUrl())) {
            addToQueue(node.getUrl().toString(), false, type, node);
        }
        if (isDefined(node.getUrlClosingMarker())) {
            addToQueue(node.getUrlClosingMarker().toString(), false, type, node);
        }
        if (isDefined(node.getTitle())) {
            addToQueue(" " + node.getTitleOpeningMarker().toString(), false, type, node);
            addToQueue(node.getTitle().toString(), true, type, node);
            addToQueue(node.getTitleClosingMarker().toString(), false, type, node);
        }
        addToQueue(newline, false, type, node);
    }

    private void visitListBlock(ListBlock listBlock, MarkdownTokenType type) {
        visitor.visitChildren(listBlock);
        addBlockNewlines(listBlock);
    }

    private void visitListItem(ListItem listItem, MarkdownTokenType type) {
        ListBlock innerList = getParentList(listItem); // the list this item belongs to
        ListItem firstItem = getFirstItemOfList(innerList); // first item of the list this item belongs to
        ListBlock outerList = getParentList(innerList); // null if this item is not in a sublist of another list

        if (outerList != null && (listItem.isTight() || (listItem == firstItem && outerList.isTight()))) {
            removeDuplicateNewlinesAtQueueTail();
        }

        addToQueue(listItem.getOpeningMarker().toString() + " ", false, type, listItem);
        if (!listItem.hasChildren()) {
            addBlockNewlines(listItem);
        }
        visitor.visitChildren(listItem);

        if (listItem.isInTightList()) {
            removeDuplicateNewlinesAtQueueTail();
        }
    }

    private ListBlock getParentList(Node node) {
        return node == null ? null : (ListBlock) node.getAncestorOfType(BulletList.class, OrderedList.class);
    }

    private ListItem getFirstItemOfList(ListBlock node) {
        return node == null ? null : (ListItem) node.getFirstChildAny(BulletListItem.class, OrderedListItem.class);
    }

    private boolean isDefined(BasedSequence sequence) {
        return sequence != BasedSequence.NULL && !sequence.isEmpty();
    }

}
