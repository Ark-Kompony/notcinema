package kg.cinema.service;

import kg.cinema.entity.Promocode;
import kg.cinema.repository.PromocodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromocodeService {

    private final PromocodeRepository promocodeRepository;

    /**
     * Validate promocode
     */
    public Promocode validatePromocode(String code) {
        Promocode promocode = promocodeRepository.findByCodeAndIsActiveTrue(code)
            .orElseThrow(() -> new RuntimeException("Invalid promocode"));

        if (!promocode.isValid()) {
            throw new RuntimeException("Promocode is expired or has reached maximum uses");
        }

        return promocode;
    }

    /**
     * Get promocode by code
     */
    public Promocode getPromocodeByCode(String code) {
        return promocodeRepository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Promocode not found"));
    }

    /**
     * Get all promocodes
     */
    public List<Promocode> getAllPromocodes() {
        return promocodeRepository.findAll();
    }

    /**
     * Create promocode
     */
    @Transactional
    public Promocode createPromocode(Promocode promocode) {
        // Validate code is unique
        if (promocodeRepository.findByCode(promocode.getCode()).isPresent()) {
            throw new RuntimeException("Promocode already exists");
        }

        return promocodeRepository.save(promocode);
    }

    /**
     * Update promocode
     */
    @Transactional
    public Promocode updatePromocode(Long id, Promocode promocodeDetails) {
        Promocode promocode = promocodeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Promocode not found"));

        promocode.setDiscountType(promocodeDetails.getDiscountType());
        promocode.setDiscountValue(promocodeDetails.getDiscountValue());
        promocode.setMaxUses(promocodeDetails.getMaxUses());
        promocode.setValidFrom(promocodeDetails.getValidFrom());
        promocode.setValidUntil(promocodeDetails.getValidUntil());
        promocode.setIsActive(promocodeDetails.getIsActive());

        return promocodeRepository.save(promocode);
    }

    /**
     * Deactivate promocode
     */
    @Transactional
    public void deactivatePromocode(Long id) {
        Promocode promocode = promocodeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Promocode not found"));

        promocode.setIsActive(false);
        promocodeRepository.save(promocode);
    }

    /**
     * Increment usage count (called when order is paid)
     */
    @Transactional
    public void incrementUsage(Long promocodeId) {
        Promocode promocode = promocodeRepository.findById(promocodeId)
            .orElseThrow(() -> new RuntimeException("Promocode not found"));

        promocode.setUsedCount(promocode.getUsedCount() + 1);
        promocodeRepository.save(promocode);
    }
}
