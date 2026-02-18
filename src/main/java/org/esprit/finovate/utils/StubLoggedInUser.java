package org.esprit.finovate.utils;

/** Stub for testing. Implements ILoggedInUser with id and role. */
public class StubLoggedInUser implements ILoggedInUser {
    private final Long id;
    private final String role;

    public StubLoggedInUser(Long id) {
        this.id = id;
        this.role = "USER";
    }

    public StubLoggedInUser(Long id, String role) {
        this.id = id;
        this.role = role != null ? role.toUpperCase() : "USER";
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getRole() {
        return role;
    }
}
