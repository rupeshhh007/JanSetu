package com.jansetu.backend.source;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

/** Immutable bytes and metadata captured from one approved official source. */
@Entity
@Table(name = "source_snapshots")
public class SourceSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_id", nullable = false)
    private OfficialSource source;

    @Column(name = "source_name_at_capture", nullable = false, length = 160)
    private String sourceNameAtCapture;

    @Column(name = "source_url_at_capture", nullable = false, length = 2048)
    private String sourceUrlAtCapture;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "http_status", nullable = false)
    private int httpStatus;

    @Column(name = "content_length", nullable = false)
    private long contentLength;

    @Column(name = "content_hash", nullable = false, length = 80)
    private String contentHash;

    @Column(name = "content_excerpt", columnDefinition = "TEXT", nullable = false)
    private String contentExcerpt;

    @Column(name = "content_bytes", columnDefinition = "bytea", nullable = false)
    private byte[] contentBytes;

    @Column(name = "captured_at", nullable = false)
    private Instant capturedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public UUID getId() { return id; }
    public OfficialSource getSource() { return source; }
    public void setSource(OfficialSource source) { this.source = source; }
    public String getSourceNameAtCapture() { return sourceNameAtCapture; }
    public void setSourceNameAtCapture(String sourceNameAtCapture) { this.sourceNameAtCapture = sourceNameAtCapture; }
    public String getSourceUrlAtCapture() { return sourceUrlAtCapture; }
    public void setSourceUrlAtCapture(String sourceUrlAtCapture) { this.sourceUrlAtCapture = sourceUrlAtCapture; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public int getHttpStatus() { return httpStatus; }
    public void setHttpStatus(int httpStatus) { this.httpStatus = httpStatus; }
    public long getContentLength() { return contentLength; }
    public void setContentLength(long contentLength) { this.contentLength = contentLength; }
    public String getContentHash() { return contentHash; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }
    public String getContentExcerpt() { return contentExcerpt; }
    public void setContentExcerpt(String contentExcerpt) { this.contentExcerpt = contentExcerpt; }
    public byte[] getContentBytes() { return contentBytes == null ? null : Arrays.copyOf(contentBytes, contentBytes.length); }
    public void setContentBytes(byte[] contentBytes) {
        this.contentBytes = contentBytes == null ? null : Arrays.copyOf(contentBytes, contentBytes.length);
    }
    public Instant getCapturedAt() { return capturedAt; }
    public void setCapturedAt(Instant capturedAt) { this.capturedAt = capturedAt; }
    public Instant getCreatedAt() { return createdAt; }
}
