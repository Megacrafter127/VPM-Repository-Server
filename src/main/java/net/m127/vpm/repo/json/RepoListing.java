package net.m127.vpm.repo.json;

import java.util.Map;

public record RepoListing(String name, String repoAuthor, String url, Map<String, PackageListing> packages) {
}
