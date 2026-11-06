package top.cch.test.client;

import org.springframework.stereotype.Component;

@Component
public class DefaultInventoryClient implements InventoryClient {

    @Override
    public int availableStock(Long productId) {
        return 100;
    }
}
