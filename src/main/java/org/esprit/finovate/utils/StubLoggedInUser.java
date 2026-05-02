package org.esprit.finovate.utils;

import org.esprit.finovate.entities.User;

/** Minimal user for placeholder login and tests when only an id is known. */
public final class StubLoggedInUser extends User {

    public StubLoggedInUser(long id) {
        super();
        setId(id);
        setRole("USER");
        setSolde(500f);
        setFirstName("User");
        setLastName("#" + id);
    }
}
