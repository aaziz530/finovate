package org.esprit.finovate.utils;

/** Stub for testing. Implements ILoggedInUser with id only. */
public class StubLoggedInUser implements ILoggedInUser {
    private final Long id;

    public StubLoggedInUser(Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return id;
    }
}
