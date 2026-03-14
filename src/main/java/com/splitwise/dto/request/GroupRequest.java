package com.splitwise.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class GroupRequest {

    @NotBlank(message = "Group name is required")
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    private String currency = "INR";

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
