package net.m127.vpm.repo.service;

import net.m127.vpm.repo.jpa.entity.Package;
import net.m127.vpm.repo.json.RepoListing;

public interface RepoListingFactory {
    RepoListing createListing(String url, Iterable<Package> packages);
}
