package io.diendanx.labbooking.repository;

import io.diendanx.labbooking.models.Package;

public class PackageRepository extends BaseRepository<Package> {
    public PackageRepository() {
        super("package");
    }
}
