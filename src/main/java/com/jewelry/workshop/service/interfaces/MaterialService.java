package com.jewelry.workshop.service.interfaces;

import com.jewelry.workshop.domain.model.dto.material.MaterialCreateDTO;
import com.jewelry.workshop.domain.model.dto.material.MaterialResponseDTO;
import com.jewelry.workshop.domain.model.dto.material.MaterialUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MaterialService {
    MaterialResponseDTO createMaterial(MaterialCreateDTO dto);
    MaterialResponseDTO updateMaterial(Long id, MaterialUpdateDTO dto);

    Page<MaterialResponseDTO> getAllMaterials(Pageable pageable);

    void deleteMaterial(Long id);

}
