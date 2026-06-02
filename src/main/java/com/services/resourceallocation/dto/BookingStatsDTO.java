package com.services.resourceallocation.dto;

/**
 * Summary counts shown on Faculty and HOD dashboards.
 */
public class BookingStatsDTO {

    private long total;
    private long approved;
    private long pending;
    private long rejected;
    private long cancelled;

    public BookingStatsDTO() {}

    public BookingStatsDTO(long total, long approved,
                           long pending, long rejected, long cancelled) {
        this.total     = total;
        this.approved  = approved;
        this.pending   = pending;
        this.rejected  = rejected;
        this.cancelled = cancelled;
    }

    public long getTotal()     { return total; }
    public long getApproved()  { return approved; }
    public long getPending()   { return pending; }
    public long getRejected()  { return rejected; }
    public long getCancelled() { return cancelled; }

    public void setTotal(long v)     { this.total = v; }
    public void setApproved(long v)  { this.approved = v; }
    public void setPending(long v)   { this.pending = v; }
    public void setRejected(long v)  { this.rejected = v; }
    public void setCancelled(long v) { this.cancelled = v; }
}