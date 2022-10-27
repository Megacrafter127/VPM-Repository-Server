package net.m127.vpm.repo.service;

import net.m127.vpm.repo.jpa.entity.Package;
import net.m127.vpm.repo.json.RepoListing;
import net.m127.vpm.repo.json.SemVersion;

public interface RepoListingFactory {
    String toPackageURL(String url, String name, SemVersion version);
    RepoListing createListing(String url, Iterable<Package> packages);
}
