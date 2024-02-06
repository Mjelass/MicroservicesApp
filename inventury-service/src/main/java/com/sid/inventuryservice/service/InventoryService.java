package com.sid.inventuryservice.service;

import com.sid.inventuryservice.dto.InventuryResponse;
import com.sid.inventuryservice.model.Inventory;
import com.sid.inventuryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    @Transactional(readOnly = true)
    public  List<InventuryResponse> isInStock(List<String> skuCode){
        return inventoryRepository.findBySkuCodeIn(skuCode).stream()
                .map(inv ->
                    InventuryResponse.builder()
                            .skuCode(inv.getSkuCode())
                            .isInStock(inv.getQuantity() > 0)
                            .build()

                ).toList();
    }
}
