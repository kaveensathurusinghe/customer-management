package com.customermanagement.dto;

import java.util.Date;
import java.util.List;

public class CustomerDTO {

    private Long id;
    private String name;
    private Date dob;
    private String nicNumber;
    private List<MobileDTO> mobiles;
    private List<AddressDTO> addresses;
    private List<FamilyMemberDTO> family;
    private Date createdAt;
    private Date updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Date getDob() { return dob; }
    public void setDob(Date dob) { this.dob = dob; }

    public String getNicNumber() { return nicNumber; }
    public void setNicNumber(String nicNumber) { this.nicNumber = nicNumber; }

    public List<MobileDTO> getMobiles() { return mobiles; }
    public void setMobiles(List<MobileDTO> mobiles) { this.mobiles = mobiles; }

    public List<AddressDTO> getAddresses() { return addresses; }
    public void setAddresses(List<AddressDTO> addresses) { this.addresses = addresses; }

    public List<FamilyMemberDTO> getFamily() { return family; }
    public void setFamily(List<FamilyMemberDTO> family) { this.family = family; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}