package com.starservice.inventory.inventory_app.entity;

import com.starservice.inventory.inventory_app.enums.Company;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "purchase")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Purchase {

    @Id
    @Column(name = "uuid")
    private String uuid;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "gst_no")
    private String gstNo;

    @Column(name = "invoice_no")
    private String invoiceNo;

    @Column(name = "invoice_type")
    private String invoiceType;

    @Column(name = "gst_percentage")
    private String gstPercentage;

    @Column(name = "invoice_date")
    private String invoiceDate;

    @Column(name = "crtd_date")
    private Instant createdDate;

    @Column(name = "updt_date")
    private Instant updatedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_company")
    private Company defaultCompany;
}
