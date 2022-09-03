package net.m127.vpm.repo.json;

import net.m127.vpm.repo.jpa.entity.User;

public record PackageAuthor(String name, String email) {
    public PackageAuthor(User user) {
        this(user.getName(), user.getEmail());
    }
}
