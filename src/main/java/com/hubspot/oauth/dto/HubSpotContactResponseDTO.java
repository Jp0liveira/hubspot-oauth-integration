package com.hubspot.oauth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

public class HubSpotContactResponseDTO {
    @JsonProperty("id")
    private String id;

    @JsonProperty("properties")
    private ContactProperties properties;


    public static class ContactProperties {
        private String firstname;
        private String lastname;
        private String email;

        public String getFirstname() {
            return firstname;
        }

        public void setFirstname(String firstname) {
            this.firstname = firstname;
        }

        public String getLastname() {
            return lastname;
        }

        public void setLastname(String lastname) {
            this.lastname = lastname;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ContactProperties getProperties() {
        return properties;
    }

    public void setProperties(ContactProperties properties) {
        this.properties = properties;
    }

}
