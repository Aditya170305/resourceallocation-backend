package com.services.resourceallocation.dto;

/**
 * Payload HOD sends when approving or rejecting a booking.
 */
public class HodReviewDTO {

    private String  action;     // "APPROVE" or "REJECT"
    private String  remarks;    // optional comment from HOD
    private Integer hodId;      // logged-in HOD's user ID

    // ── Getters & Setters ──────────────────────────────────────

    public String  getAction()             { return action; }
    public void    setAction(String v)     { this.action = v; }

    public String  getRemarks()            { return remarks; }
    public void    setRemarks(String v)    { this.remarks = v; }

    public Integer getHodId()              { return hodId; }
    public void    setHodId(Integer v)     { this.hodId = v; }
}