package com.iuh.fit.readhub.projections;

public interface NoteProjection {
    Long getNoteId();
    String getContent();
    String getSelectedText();
    Long getUserId();
    Long getBookId();
    String getColor();
    String getCfiRange();
    String getCreatedAt();
    String getUpdatedAt();
}
