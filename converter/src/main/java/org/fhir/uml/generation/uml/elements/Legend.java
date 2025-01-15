package org.fhir.uml.generation.uml.elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.fhir.uml.generation.uml.types.LegendPosition.XPosition;
import org.fhir.uml.generation.uml.types.LegendPosition.YPosition;
import org.fhir.uml.generation.uml.types.LegendPosition.CommentType;

public class Legend {

    private XPosition xPosition;
    private YPosition yPosition;

    private int currentCommentId = 0;
    private final Map<String, CommentInfo> commentsById = new HashMap<>();

    // 1) Standard version: just add the comment text as is.
    public int addComment(String externalId, String comment) {
        return addOrUpdateComment(externalId, comment);
    }

    // 2) Overloaded version: add comment text with a particular type.
    //    If type is FIXED_VALUE, automatically prepend "Fixed Value: ".
    public Integer addComment(String externalId, String comment, CommentType type) {
        if (comment == null) {
            return null;
        }

        if (type == CommentType.FIXED_VALUE) {
            comment = "**Fixed Value:** " + comment;
        }

        return addOrUpdateComment(externalId, comment);
    }

    /**
     * Helper method that does the actual "add or update" logic.
     */
    private int addOrUpdateComment(String externalId, String comment) {
        CommentInfo info = commentsById.get(externalId);
        if (info == null) {
            currentCommentId++;
            info = new CommentInfo(currentCommentId);
            commentsById.put(externalId, info);
        }
        info.getComments().add(comment);
        return info.getCommentId();
    }

    /**
     * Returns all comments for a particular externalId.
     */
    public List<String> getCommentsForId(String externalId) {
        CommentInfo info = commentsById.get(externalId);
        return (info != null) ? info.getComments() : new ArrayList<>();
    }

    // Example toString() that produces PlantUML legend syntax
    // (grouping comments by numeric ID, separated by " | ").
    @Override
    public String toString() {
        if (commentsById.isEmpty()) {
            return "";
        }

        XPosition xPos = (xPosition != null) ? xPosition : XPosition.RIGHT;
        YPosition yPos = (yPosition != null) ? yPosition : YPosition.TOP;

        // commentId => list of comment texts
        Map<Integer, List<String>> commentsByNumber = new TreeMap<>();
        for (CommentInfo info : commentsById.values()) {
            commentsByNumber
                    .computeIfAbsent(info.getCommentId(), k -> new ArrayList<>())
                    .addAll(info.getComments());
        }

        if (commentsByNumber.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("legend %s %s\n",
                yPos.name().toLowerCase(), xPos.name().toLowerCase()));

        for (Map.Entry<Integer, List<String>> entry : commentsByNumber.entrySet()) {
            int commentId = entry.getKey();
            // Join comments for that ID into one line
            String joinedComments = String.join(" | ", entry.getValue());
            sb.append(String.format("(%d) %s\n", commentId, joinedComments));
        }

        sb.append("end legend\n");
        return sb.toString();
    }

    // getters and setters for xPosition and yPosition
    public XPosition getXPosition() {
        return xPosition;
    }

    public void setXPosition(XPosition xPosition) {
        this.xPosition = xPosition;
    }

    public YPosition getYPosition() {
        return yPosition;
    }

    public void setYPosition(YPosition yPosition) {
        this.yPosition = yPosition;
    }

    // Inner helper class to store comment ID + all associated texts.
    private static class CommentInfo {
        private final int commentId;
        private final List<String> comments = new ArrayList<>();

        private CommentInfo(int commentId) {
            this.commentId = commentId;
        }

        public int getCommentId() {
            return commentId;
        }

        public List<String> getComments() {
            return comments;
        }
    }
}
