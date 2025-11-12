package com.rapicredit.models;

class user {
    private final String email;
    private final String password;

    public user(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public static user of(String email, String password) {
        return new user(email, password);
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "Credentials{email='" + email + "'}";
    }
}


