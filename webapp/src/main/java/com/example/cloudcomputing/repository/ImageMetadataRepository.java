package com.example.cloudcomputing.repository;


import com.example.cloudcomputing.model.ImageMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageMetadataRepository extends JpaRepository<ImageMetadata, String> {

    Optional<ImageMetadata> findByIdAndUserId(String id, String userId);
    Optional<ImageMetadata> findByUserId(String userId);

    List<ImageMetadata> findByFileName(String fileName);

}
