package com.jewelry.workshop.service.impl;

import com.jewelry.workshop.domain.model.dto.material.MaterialCreateDTO;
import com.jewelry.workshop.domain.model.dto.material.MaterialResponseDTO;
import com.jewelry.workshop.domain.model.dto.material.MaterialUpdateDTO;
import com.jewelry.workshop.domain.model.entity.Material;
import com.jewelry.workshop.domain.repository.MaterialRepository;
import com.jewelry.workshop.service.interfaces.MaterialService;
import com.jewelry.workshop.service.mapper.MaterialMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MaterialServiceImpl implements MaterialService {

    private final MaterialRepository materialRepository;
    private final MaterialMapper materialMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<MaterialResponseDTO> getAllMaterials(Pageable pageable) {
        return materialRepository.findAll(pageable)
                .map(materialMapper::toDto);
    }

    @Override
    @Transactional
    public MaterialResponseDTO createMaterial(MaterialCreateDTO dto) {
        if (materialRepository.existsByName(dto.getName())) {
            throw new RuntimeException("Материал с названием '" + dto.getName() + "' уже существует");
        }
        Material material = new Material();
        material.setName(dto.getName());
        material.setDescription(dto.getDescription());
        Material saved = materialRepository.save(material);
        return materialMapper.toDto(saved);
    }

    @Override
    @Transactional
    public MaterialResponseDTO updateMaterial(Long id, MaterialUpdateDTO dto) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Материал с ID " + id + " не найден"));

        if (dto.getName() != null) {
            if (!material.getName().equals(dto.getName()) &&
                    materialRepository.existsByName(dto.getName())) {
                throw new RuntimeException("Материал с названием '" + dto.getName() + "' уже существует");
            }
            material.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            material.setDescription(dto.getDescription());
        }

        Material updated = materialRepository.save(material);
        return materialMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void deleteMaterial(Long id) {
        if (!materialRepository.existsById(id)) {
            throw new EntityNotFoundException("Материал с ID " + id + " не найден");
        }
        if (materialRepository.isMaterialInUse(id)) {
            throw new RuntimeException("Нельзя удалить материал, используемый в изделиях");
        }
        materialRepository.deleteById(id);
    }

}