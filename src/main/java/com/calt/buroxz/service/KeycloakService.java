package com.calt.buroxz.service;

import lombok.AllArgsConstructor;
import org.keycloak.admin.client.Keycloak;

@AllArgsConstructor
public class KeycloakService {

    private Keycloak keycloak;
    private String targetRealm = "jhipster";

    //injected by constructor
    public KeycloakService(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    public void createAndSetPassword(String username, String email, String password) {
        //        // 1. Create the User object
        //        UserRepresentation user = new UserRepresentation();
        //        user.setUsername(username);
        //        user.setEmail(email);
        //        user.setEnabled(true);
        //
        //        // 2. Set the Password
        //        CredentialRepresentation cred = new CredentialRepresentation();
        //        cred.setType(CredentialRepresentation.PASSWORD);
        //        cred.setValue(password);
        //        cred.setTemporary(false); // Admin chooses if user must change it
        //        user.setCredentials(Collections.singletonList(cred));
        //
        //        // 3. THE CALL: This line sends the request to Keycloak
        //        keycloak.realm(targetRealm).users().create(user);
    }
}
